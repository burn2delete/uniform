(ns gravity.integration-candidate-verification
  "Fresh, repository-cache-independent verification for an exact integration candidate.

  This repository tool is not compiler or release authority.  It verifies the
  committed candidate from a temporary Git-index checkout, never resumes prior
  test output, and records the exact commands and identities in an EDN receipt."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str])
  (:import [java.nio.file Files LinkOption Path StandardOpenOption]
           [java.nio.file.attribute FileAttribute]
           [java.time Instant]
           [java.util Comparator]
           [java.util.concurrent TimeUnit]))

(def schema :gravity/integration-fresh-verification-receipt-v1)
(def ^:private oid-pattern #"[0-9a-f]{40,64}")
(def ^:private progress-max-bytes (* 32 1024))
(def ^:private telemetry-history-limit 32)
(def ^:private process-pid-limit 256)
(def ^:dynamic *telemetry-interval-ms*
  ;; Sparse heartbeats keep long fresh runs observable without creating a
  ;; second workload. Tests may bind this to a small value.
  30000)
(def ^:dynamic *telemetry-sampler-timeout-ms*
  ;; `ps` is diagnostic input. A broken host utility must never hold the
  ;; authoritative verifier indefinitely.
  1000)
(def ^:dynamic *rss-process-command*
  (fn [pids]
    ["ps" "-o" "pid=,rss=" "-p"
     (str/join "," (map str pids))]))
(defn default-receipt [candidate-commit]
  (str "target/validation/integration-fresh-verification/"
       candidate-commit "/receipt.edn"))

(def full-suite-command
  ["clojure" "-Srepro" "-Sforce" "-M:test"])

(def verification-commands
  [full-suite-command
   ["clojure" "-Srepro" "-Sforce" "-M"
    "tools/validate_gravity_docs.clj"]
   ["clojure" "-Srepro" "-Sforce" "-M"
    "tools/validate_full_language_roadmap.clj"]
   ["clojure" "-Srepro" "-Sforce" "-M"
    "tools/validate_workstream_governance.clj"]
   ["clojure" "-Srepro" "-Sforce" "-M:test" "--namespace"
    "gravity.self-hosting.sh01-language-boundary-test"]])

(defn failure
  ([code message] (failure code message {}))
  ([code message details]
   (ex-info message {:code code :details details})))

(defn validate-publishable-evidence!
  "Reject evidence that came from an incremental/speculative/local-cache lane."
  [evidence]
  (when-not (and (= #{:mode :new-export :resume :repository-cache}
                    (set (keys evidence)))
                 (= :fresh (:mode evidence))
                 (true? (:new-export evidence))
                 (false? (:resume evidence))
                 (false? (:repository-cache evidence)))
    (throw (failure "C16-SPECULATIVE"
                    "speculative or local-cache evidence cannot cross an integration publishable boundary"
                    {:evidence evidence})))
  evidence)

(defn verification-plan
  "Return the fixed, non-authoritative fresh verification plan."
  [identities]
  (doseq [[label value] identities]
    (when-not (and (string? value) (re-matches oid-pattern value))
      (throw (failure "INTEGRATION-FRESH-IDENTITY"
                      (str (name label) " must be a full Git object identity")
                      {:field label :value value}))))
  {:schema schema
   :candidate identities
   :evidence (validate-publishable-evidence!
              {:mode :fresh :new-export true :resume false
               :repository-cache false})
   :commands verification-commands
   :command-policy {:full-suite-preserved true
                    :fresh-basis true
                    :candidate-diff-check true}
   :authority {:integration-evidence :candidate-only
               :release false :self-hosting false :seed-retirement false
               :safety false :performance false :stage3 false :sh07 false
               :reproducible-environment false}
   :external-proof-lanes
   {:stage3 :not-substituted
    :sh07 :not-substituted}
   :residual-host-boundaries [:clojure-jvm :git :dependency-cache-tool-resolution]})

(defn- safe-progress-path?
  [^Path root ^Path candidate]
  (try
    (let [root-path (.normalize (.toAbsolutePath root))
          working (.toRealPath root-path
                               (make-array LinkOption 0))
          candidate (.normalize (.toAbsolutePath candidate))]
      (and (.startsWith candidate root-path)
           (loop [current working
                  remaining (seq (iterator-seq
                                  (.iterator (.relativize root-path candidate))))]
             (if-let [name (first remaining)]
               (let [next-path (.resolve ^Path current ^Path name)]
                 (and (not (Files/isSymbolicLink next-path))
                      (recur next-path (next remaining))))
               true))))
    (catch Throwable _
      false)))

(defn- progress-record
  "Read the child runner's last bounded progress record, if available.

  The progress file is diagnostic-only and lives inside the temporary fresh
  export. Malformed, partial, or unavailable telemetry is ignored so it cannot
  change the verifier's existing exit status or diagnostics."
  [root path]
  (when (and root path
             (safe-progress-path? root path)
             (Files/exists ^Path path
                           (into-array LinkOption [LinkOption/NOFOLLOW_LINKS])))
    (try
      (let [bytes (Files/readAllBytes ^Path path)]
        (when (<= (alength bytes) progress-max-bytes)
          (let [record (edn/read-string
                        (String. bytes java.nio.charset.StandardCharsets/UTF_8))]
            (when (and (map? record)
                       (= :gravity/fresh-verification-progress-v1
                          (:schema record))
                       (integer? (:sequence record))
                       (not (neg? (:sequence record)))
                       (string? (:phase record))
                       (boolean? (:active? record)))
              record))))
      (catch Throwable _
        nil))))

(defn- bounded-pid-sample
  [root-pid descendant-pids]
  (let [observed (vec (take (inc process-pid-limit)
                            (cons root-pid descendant-pids)))
        truncated? (> (count observed) process-pid-limit)]
    {:pids (vec (take process-pid-limit observed))
     :truncated? truncated?}))

(defn- process-pids
  [^Process process]
  (try
    (let [handle (.toHandle process)]
      (with-open [descendants (.descendants handle)]
        (bounded-pid-sample
         (.pid handle)
         (map #(.pid ^java.lang.ProcessHandle %)
              (iterator-seq (.iterator descendants))))))
    (catch Throwable _
      {:pids [] :truncated? false})))

(defn- rss-bytes
  "Best-effort RSS for the verifier process and its descendants.

  `ps` is used only as diagnostic input; a missing platform tool, permission
  error, or malformed line yields nil rather than a verification failure."
  [pids]
  (when (seq pids)
    (try
      (let [builder (ProcessBuilder.
                     ^java.util.List (*rss-process-command* pids))
            process (.start builder)
            stdout (future (slurp (.getInputStream process)))
            stderr (future (slurp (.getErrorStream process)))
            finished? (.waitFor process
                                (long *telemetry-sampler-timeout-ms*)
                                TimeUnit/MILLISECONDS)]
        (if-not finished?
          (do
            (.destroyForcibly process)
            (.waitFor process (long *telemetry-sampler-timeout-ms*)
                      TimeUnit/MILLISECONDS)
            (future-cancel stdout)
            (future-cancel stderr)
            nil)
          (let [exit-code (.exitValue process)
                stdout-future stdout
                stderr-future stderr
                stdout (deref stdout-future
                               (long *telemetry-sampler-timeout-ms*) ::timeout)
                stderr (deref stderr-future
                               (long *telemetry-sampler-timeout-ms*) ::timeout)]
            (when (or (= ::timeout stdout) (= ::timeout stderr))
              (future-cancel stdout-future)
              (future-cancel stderr-future))
            (when (and (zero? exit-code) (string? stdout)
                       (string? stderr))
              (let [values
                    (keep (fn [line]
                            (let [[_ _ rss]
                                  (re-matches #"\s*(\d+)\s+(\d+)\s*"
                                              line)]
                              (try
                                (Long/parseLong rss)
                                (catch Throwable _ nil))))
                          (str/split-lines stdout))]
                (when (seq values)
                  (* 1024 (reduce + 0 values))))))))
      (catch Throwable _
        nil))))

(defn- command-phase
  [command]
  (cond
    (= command full-suite-command) :full-suite
    (= (last command) "tools/validate_gravity_docs.clj") :document-gate
    (= (last command) "tools/validate_full_language_roadmap.clj") :roadmap-gate
    (= (last command) "tools/validate_workstream_governance.clj") :governance-gate
    (= (last command) "gravity.self-hosting.sh01-language-boundary-test")
    :language-boundary-gate
    :else :process))

(defn- phase-history-entry
  [progress elapsed-ms phase-elapsed-ms]
  (when progress
    {:phase (:phase progress)
     :event (:event progress)
     :active? (:active? progress)
     :sequence (:sequence progress)
     :started-elapsed-ms (- elapsed-ms phase-elapsed-ms)
     :phase-elapsed-ms phase-elapsed-ms}))

(defn- telemetry-sample!
  [state process command progress-root progress-path started]
  (locking state
    (try
      (let [elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))
            progress (progress-record progress-root progress-path)
            pid-sample (if process
                         (process-pids process)
                         {:pids [] :truncated? false})
            pids (:pids pid-sample)
            rss (when process (rss-bytes pids))
            previous @state
            current-phase (or (:phase progress)
                              (:last-phase previous)
                              (name (command-phase command)))
            phase-changed? (not= current-phase (:last-phase previous))
            phase-started-elapsed-ms
            (if phase-changed?
              elapsed-ms
              (long (or (:phase-started-elapsed-ms previous)
                        elapsed-ms)))
            phase-elapsed-ms (- elapsed-ms phase-started-elapsed-ms)
            history (if (and phase-changed? current-phase)
                      (let [entry (phase-history-entry progress elapsed-ms
                                                       phase-elapsed-ms)]
                        (vec
                         (take-last
                          telemetry-history-limit
                          (conj (vec (:phase-history previous))
                                (or entry {:phase current-phase
                                           :started-elapsed-ms
                                           phase-started-elapsed-ms
                                           :phase-elapsed-ms phase-elapsed-ms})))))
                      (:phase-history previous))
            high-water (if (number? rss)
                         (max (long (or (:rss-high-water-bytes previous) 0))
                              (long rss))
                         (:rss-high-water-bytes previous))
            next-state (assoc previous
                              :last-phase current-phase
                              :last-progress progress
                              :last-rss-bytes rss
                              :rss-high-water-bytes high-water
                              :process-sample-truncated?
                              (or (:process-sample-truncated? previous)
                                  (:truncated? pid-sample))
                              :sample-count (inc (long (:sample-count previous)))
                              :phase-history history
                              :phase-started-elapsed-ms phase-started-elapsed-ms
                              :phase-elapsed-ms phase-elapsed-ms
                              :last-elapsed-ms elapsed-ms)]
        (reset! state next-state)
        (println
         (str "fresh verification heartbeat: phase=" current-phase
              " elapsed-ms=" elapsed-ms
              " phase-elapsed-ms=" phase-elapsed-ms
              " rss-bytes=" (or rss "unknown")
              " rss-high-water-bytes=" (or high-water "unknown")
              " process-count=" (count pids)
              " process-truncated=" (:truncated? pid-sample)))
        (flush)
        next-state)
      (catch Throwable _
        @state))))

(defn- telemetry-summary
  [state command progress-root progress-path started]
  (let [result (telemetry-sample! state nil command progress-root progress-path
                                  started)]
    (-> result
        (dissoc :last-phase)
        (assoc :schema :gravity/fresh-verification-observability-v1
               :command-phase (command-phase command)
               :progress-source (when progress-path :fresh-child-progress-file)
               :memory-source (when (:rss-high-water-bytes result) :ps-rss)
               :authoritative? false))))

(defn- run-process
  ([directory command] (run-process directory command {}))
  ([directory command environment]
   (let [started (System/nanoTime)
         progress-path
         (when (= command full-suite-command)
           (.resolve ^Path directory ".gravity-fresh-progress.edn"))
         environment
         (cond-> environment
           progress-path
           (assoc "GRAVITY_FRESH_PROGRESS_FILE" (str progress-path)))
         telemetry-state
         (atom {:phase-history []
                :sample-count 0
                :rss-high-water-bytes nil
                :last-rss-bytes nil
                :process-sample-truncated? false
                :last-progress nil
                :last-phase nil
                :last-elapsed-ms 0})
         builder (doto (ProcessBuilder. ^java.util.List command)
                   (.directory (.toFile ^Path directory))
                   (.inheritIO))]
     (doseq [[name value] environment]
       (.put (.environment builder) name value))
     (let [process (.start builder)
           monitor
           (future
             (try
               (loop []
                 (Thread/sleep (long *telemetry-interval-ms*))
                 (telemetry-sample! telemetry-state process command
                                    directory progress-path started)
                 (recur))
               (catch InterruptedException _
                 nil)
               (catch Throwable _
                 nil)))
           exit-code
           (try
             (.waitFor process)
             (finally
               (future-cancel monitor)))]
       (telemetry-sample! telemetry-state process command directory progress-path
                          started)
       {:command command
        :exit-code exit-code
        :elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))
        :telemetry
        (telemetry-summary telemetry-state command directory progress-path
                           started)}))))

(defn- run-captured
  [directory command]
  (let [builder (doto (ProcessBuilder. ^java.util.List command)
                  (.directory (.toFile ^Path directory)))
        process (.start builder)
        stdout (future (slurp (.getInputStream process)))
        stderr (future (slurp (.getErrorStream process)))
        exit-code (.waitFor process)]
    {:exit-code exit-code :stdout @stdout :stderr @stderr}))

(defn- require-success!
  [result code]
  (when-not (zero? (:exit-code result))
    (throw (failure code
                    (str "command failed: " (str/join " " (:command result)))
                    {:result result})))
  result)

(defn- reject-checkout-filters!
  [root candidate-commit]
  (let [result (run-captured
                root ["git" "ls-tree" "-r" "--name-only" "-z"
                      candidate-commit])]
    (when-not (zero? (:exit-code result))
      (throw (failure "INTEGRATION-FRESH-ARCHIVE"
                      "could not inspect candidate tree attributes"
                      {:stderr (str/trim (:stderr result))})))
    (when-let [path (some #(when (re-find #"(^|/)\.gitattributes$" %) %)
                          (str/split (:stdout result) #"\u0000"))]
      (throw (failure "INTEGRATION-FRESH-FILTER"
                      "candidate tree attributes could transform fresh-checkout bytes"
                      {:path path})))
    {:command ["git" "ls-tree" "-r" "--name-only" "-z" candidate-commit]
     :exit-code 0
     :gitattributes-present false}))

(defn- repository-root []
  (let [result (run-captured (.toPath (io/file "."))
                             ["git" "rev-parse" "--show-toplevel"])]
    (when-not (zero? (:exit-code result))
      (throw (failure "INTEGRATION-FRESH-REPOSITORY"
                      "current directory is not a Git repository"
                      {:stderr (str/trim (:stderr result))})))
    (.toPath (io/file (str/trim (:stdout result))))))

(defn- delete-tree!
  [path]
  (when (Files/exists path (make-array LinkOption 0))
    (with-open [paths (Files/walk path (make-array java.nio.file.FileVisitOption 0))]
      (.forEach (.sorted paths (Comparator/reverseOrder))
                (reify java.util.function.Consumer
                  (accept [_ item] (Files/deleteIfExists ^Path item)))))))

(defn- validate-export-boundary!
  [export]
  (let [real-export (.toRealPath ^Path export (make-array LinkOption 0))]
    (with-open [paths (Files/walk export (make-array java.nio.file.FileVisitOption 0))]
      (.forEach
       paths
       (reify java.util.function.Consumer
         (accept [_ item]
           (when (Files/isSymbolicLink ^Path item)
             (let [target (try
                            (.toRealPath ^Path item (make-array LinkOption 0))
                            (catch java.io.IOException error
                              (throw (failure "INTEGRATION-FRESH-ARCHIVE"
                                              "candidate archive contains a dangling symbolic link"
                                              {:path (str item)
                                               :cause (.getMessage error)}))))]
               (when-not (.startsWith target real-export)
                 (throw (failure "INTEGRATION-FRESH-ARCHIVE"
                                 "candidate archive symbolic link escapes the fresh export"
                                 {:path (str item) :target (str target)})))))))))))

(defn- create-safe-directories!
  [root directory]
  (loop [current root
         names (iterator-seq (.iterator (.relativize root directory)))]
    (when-let [name (first names)]
      (let [next-path (.resolve ^Path current ^Path name)]
        (when (Files/isSymbolicLink next-path)
          (throw (failure "INTEGRATION-FRESH-RECEIPT"
                          "receipt directory cannot traverse a symbolic link"
                          {:path (str next-path)})))
        (when-not (Files/exists next-path (make-array LinkOption 0))
          (Files/createDirectory next-path (make-array FileAttribute 0)))
        (recur next-path (next names))))))

(defn- write-receipt!
  [root receipt-path receipt]
  (let [path (.normalize (.resolve ^Path root receipt-path))]
    (when-not (.startsWith path root)
      (throw (failure "INTEGRATION-FRESH-RECEIPT"
                      "receipt path must stay inside the repository"
                      {:path receipt-path})))
    (create-safe-directories! root (.getParent path))
    (when (Files/isSymbolicLink path)
      (throw (failure "INTEGRATION-FRESH-RECEIPT"
                      "receipt cannot replace a symbolic link"
                      {:path (str path)})))
    (with-open [writer (Files/newBufferedWriter
                        path
                        (into-array StandardOpenOption
                                    [StandardOpenOption/CREATE_NEW
                                     StandardOpenOption/WRITE]))]
      (pprint/pprint receipt writer))
    (str path)))

(defn run-verification!
  [{:keys [base-ref candidate-base candidate-commit candidate-tree]}]
  (let [root (.normalize (repository-root))
        identities {:base candidate-base :commit candidate-commit
                    :tree candidate-tree}
        plan (verification-plan identities)
        receipt-path (default-receipt candidate-commit)
        absolute-receipt (.normalize (.resolve ^Path root receipt-path))
        _ (when (Files/exists absolute-receipt
                              (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
            (throw (failure "INTEGRATION-FRESH-RECEIPT-EXISTS"
                            "candidate receipt already exists; fresh verification will not resume or overwrite it"
                            {:path (str absolute-receipt)})))
        temporary (Files/createTempDirectory "gravity-integration-fresh-"
                                             (make-array FileAttribute 0))
        candidate-index (.resolve temporary "candidate.index")
        export (.resolve temporary "candidate")
        started (str (Instant/now))]
    (try
      (let [preflight-command
            ["clojure" "-Srepro" "-Sforce" "-M"
             "tools/check_worktree_preflight.clj" "--mode" "integration"
             "--base-ref" base-ref "--candidate-base" candidate-base
             "--candidate-commit" candidate-commit
             "--candidate-tree" candidate-tree]
            preflight (require-success!
                       (run-process root preflight-command)
                       "INTEGRATION-FRESH-PREFLIGHT")
            diff-check (require-success!
                        (run-process root
                                     ["git" "diff" "--check"
                                      candidate-base candidate-commit])
                        "INTEGRATION-FRESH-DIFF-CHECK")
            _ (Files/createDirectories export (make-array FileAttribute 0))
            filter-check (reject-checkout-filters! root candidate-commit)
            index-environment {"GIT_INDEX_FILE" (str candidate-index)}
            read-tree-result
            (require-success!
             (run-process root ["git" "read-tree" candidate-commit]
                          index-environment)
             "INTEGRATION-FRESH-ARCHIVE")
            checkout-result
            (require-success!
             (run-process root
                          ["git" "-c" "core.attributesFile=/dev/null"
                           "-c" "core.autocrlf=false"
                           "-c" "core.eol=lf"
                           "-c" "core.symlinks=true"
                           "checkout-index" "--all" "--force"
                           (str "--prefix=" export java.io.File/separator)]
                          index-environment)
             "INTEGRATION-FRESH-ARCHIVE")
            _ (validate-export-boundary! export)
            results (loop [commands verification-commands completed []]
                      (if-let [command (first commands)]
                        (let [result (run-process export command)
                              completed (conj completed result)]
                          (if (zero? (:exit-code result))
                            (recur (next commands) completed)
                            (throw (failure "INTEGRATION-FRESH-CHECK"
                                            (str "fresh verification failed: "
                                                 (str/join " " command))
                                            {:results completed}))))
                        completed))
            receipt-value
            (assoc plan
                   :status :passed
                   :exit-code 0
                   :started-at started
                   :completed-at (str (Instant/now))
                   :base-ref base-ref
                   :preflight preflight
                   :candidate-diff-check diff-check
                   :fresh-export {:kind :temporary-git-index-checkout
                                  :filter-check filter-check
                                  :read-tree read-tree-result
                   :checkout checkout-result
                   :symlink-boundary-checked true}
                   :results results
                   :observability
                   {:schema :gravity/fresh-verification-observability-v1
                    :authoritative? false
                    :commands (mapv #(select-keys % [:command :telemetry])
                                    results)}
                   :fresh-workspace {:kind :temporary-git-index-checkout
                                     :retained false
                                     :prior-target-output-visible false})]
        {:receipt receipt-value
         :path (write-receipt! root receipt-path receipt-value)})
      (catch Throwable error
        (let [data (ex-data error)
              receipt-value
              (assoc plan
                     :status :failed
                     :exit-code 1
                     :started-at started
                     :completed-at (str (Instant/now))
                     :base-ref base-ref
                     :diagnostic {:code (or (:code data)
                                            "INTEGRATION-FRESH-UNEXPECTED")
                                  :message (ex-message error)
                                  :details (or (:details data) {})})]
          (write-receipt! root receipt-path receipt-value)
          (throw error)))
      (finally
        (delete-tree! temporary)))))

(defn- parse-args [arguments]
  (loop [remaining (seq arguments) options {}]
    (if-not remaining
      options
      (let [option (first remaining)
            rest-args (next remaining)
            value (fn []
                    (or (first rest-args)
                        (throw (failure "INTEGRATION-FRESH-USAGE"
                                        (str option " requires a value")))))]
        (case option
          "--base-ref" (recur (next rest-args) (assoc options :base-ref (value)))
          "--candidate-base" (recur (next rest-args) (assoc options :candidate-base (value)))
          "--candidate-commit" (recur (next rest-args) (assoc options :candidate-commit (value)))
          "--candidate-tree" (recur (next rest-args) (assoc options :candidate-tree (value)))
          "--help" (assoc options :help true)
          (throw (failure "INTEGRATION-FRESH-USAGE"
                          (str "unknown option: " option))))))))

(defn -main [& arguments]
  (try
    (let [options (parse-args arguments)]
      (if (:help options)
        (do
          (println "Usage: clojure -M tools/verify_integration_candidate.clj")
          (println "  --base-ref REF --candidate-base OID --candidate-commit OID --candidate-tree OID")
          0)
        (let [required [:base-ref :candidate-base :candidate-commit :candidate-tree]
              missing (filterv #(str/blank? (get options %)) required)]
          (when (seq missing)
            (throw (failure "INTEGRATION-FRESH-USAGE"
                            "all exact base and candidate identities are required"
                            {:missing missing})))
          (let [{:keys [path]} (run-verification! options)]
            (println (str "fresh integration verification passed; receipt=" path))
            0))))
    (catch Throwable error
      (binding [*out* *err*]
        (println (str (or (-> error ex-data :code)
                          "INTEGRATION-FRESH-UNEXPECTED")
                      ": " (ex-message error))))
      1)))

(when (some? *command-line-args*)
  (System/exit (int (apply -main *command-line-args*))))
