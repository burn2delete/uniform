(ns gravity.integration-candidate-verification
  "Fresh, repository-cache-independent verification for an exact integration candidate.

  This repository tool is not compiler or release authority.  It verifies the
  committed candidate from a temporary Git-index checkout, never resumes prior
  test output, and records the exact commands and identities in an EDN receipt."
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str])
  (:import [java.nio.file Files LinkOption Path StandardOpenOption]
           [java.nio.file.attribute FileAttribute]
           [java.time Instant]
           [java.util Comparator]))

(def schema :gravity/integration-fresh-verification-receipt-v1)
(def ^:private oid-pattern #"[0-9a-f]{40,64}")
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

(defn- run-process
  ([directory command] (run-process directory command {}))
  ([directory command environment]
   (let [started (System/nanoTime)
         builder (doto (ProcessBuilder. ^java.util.List command)
                   (.directory (.toFile ^Path directory))
                   (.inheritIO))]
     (doseq [[name value] environment]
       (.put (.environment builder) name value))
     (let [process (.start builder)
           exit-code (.waitFor process)]
       {:command command
        :exit-code exit-code
        :elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))}))))

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
