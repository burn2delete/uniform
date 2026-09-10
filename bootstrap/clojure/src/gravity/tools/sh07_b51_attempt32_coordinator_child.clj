(ns gravity.tools.sh07-b51-attempt32-coordinator-child
  "Fresh-process V9 coordinator for the isolated A, B, then M topology.

  Keep this namespace project-free.  Entry validation precedes every dynamic
  project load.  In particular this source never requires, resolves, or invokes
  either checker namespace or the checker-child namespace in M."
  (:require [clojure.edn :as edn]
            [clojure.string :as str])
  (:import [clojure.lang LineNumberingPushbackReader]
           [java.io ByteArrayInputStream ByteArrayOutputStream Closeable File
            InputStream InputStreamReader OutputStream]
           [java.lang ProcessHandle]
           [java.lang.management ManagementFactory]
           [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file Files LinkOption Path Paths StandardOpenOption]
           [java.nio.file.attribute BasicFileAttributes]
           [java.security MessageDigest]
           [java.time Instant]
           [java.util Arrays Optional]
           [java.util.concurrent Callable ExecutionException
            ExecutorCompletionService Executors Future TimeUnit
            TimeoutException]
           [java.util.jar JarFile]))

(def transport-limits
  {:request-max-bytes 4194304
   :response-max-bytes 1073741824
   :stderr-max-bytes 1048576
   :deadline-ms 3600000})

(def closed-environment
  {"LC_ALL" "C"
   "LANG" "C"
   "TZ" "UTC"
   "GIT_CONFIG_NOSYSTEM" "1"
   "GIT_CONFIG_GLOBAL" "/dev/null"
   "GIT_OPTIONAL_LOCKS" "0"
   "GIT_NO_REPLACE_OBJECTS" "1"})

(def profile-bounds
  {:certificate-maximum-nodes 2048
   :certificate-maximum-depth 12
   :certificate-maximum-width 35
   :certificate-maximum-collection-count 64
   :certificate-maximum-scalar-utf8 4096
   :certificate-maximum-canonical-reader-bytes 262144
   :outer-request-maximum-collection-count 65536
   :carrier-maximum-nodes 8388608
   :carrier-maximum-depth 256
   :carrier-maximum-width 65536
   :carrier-maximum-collection-count 524288
   :carrier-partition-maximum-collection-count 65536
   :carrier-maximum-scalar-utf8 65536
   :offline-transcript-maximum-nodes 262144
   :offline-transcript-maximum-records 262144
   :offline-transcript-maximum-call-rows 65536
   :offline-transcript-maximum-bytes 268435456})

(def child-role-specs
  {:producer-census
   {:cli "producer-census" :sequence :a
    :identity-name "checker-a-identity.edn"
    :output-name "checker-a-output.edn"}
   :independent-verifier-census
   {:cli "independent-verifier-census" :sequence :b
    :identity-name "checker-b-identity.edn"
    :output-name "checker-b-output.edn"}})

(def coordinator-event-plan
  [[:m-process-start :m]
   [:a-builder :a]
   [:a-start :a]
   [:a-exit-observed :a]
   [:a-response-validated :a]
   [:b-builder :b]
   [:b-start :b]
   [:b-exit-observed :b]
   [:b-response-validated :b]
   [:m-session-start :m]
   [:m-session-finalize :m]])

(def ^:private coordinator-option-names
  ["--snapshot-commit"
   "--snapshot-tree"
   "--completed-at"
   "--evidence-relative-root"
   "--staging-root"
   "--eligibility-review-commit"
   "--eligibility-review-tree"
   "--eligibility-receipt-path"
   "--eligibility-receipt-blob"
   "--eligibility-receipt-byte-count"
   "--eligibility-receipt-raw-sha256"
   "--eligibility-receipt-id"])

(def ^:private evidence-relative-root
  "docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9")
(def ^:private eligibility-path
  "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/evidence-eligibility-receipt.edn")
(def ^:private completed-at "1970-01-01T00:00:00Z")
(def ^:private child-main-token
  "gravity.tools.sh07-b51-attempt32-checker-child")
(def ^:private coordinator-main-token
  "gravity.tools.sh07-b51-attempt32-coordinator-child")
(def ^:private codec-namespace "gravity.tools.sh07-b51-v9-codec")
(def ^:private bootstrap-namespace "gravity.bootstrap")
(def ^:private eligibility-receipt-keys
  #{:artifact :schema-version :content :freeze :source-descriptors
    :source-descriptors-root :masked-root :pre-evidence-contract-discharge
    :review :state :self-id-omits :nonclaims :receipt-id})
(def ^:private nonclaims
  [:formal-proof :sh07-complete :self-hosting :release :seed-retirement
   :hostile-host])

(def ^:private classpath-source-prefix "bootstrap/clojure/src/")
(def ^:private empty-raw-sha256
  "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
(def ^:private pre-manifest-row-keys
  #{:ordinal :path :namespace :mode :blob-oid :byte-count :raw-sha256
    :materialized-raw-sha256 :requires-project-namespaces})
(def ^:private codec-tcb-load-order
  '[gravity.c2-artifact-identity.canonical
    gravity.digest
    gravity.tools.sh07-b51-v9-codec])
(def ^:private codec-tcb-specs
  [{:ordinal 0
    :path "bootstrap/clojure/src/gravity/c2_artifact_identity/canonical.clj"
    :namespace 'gravity.c2-artifact-identity.canonical
    :requires-project-namespaces []}
   {:ordinal 1
    :path "bootstrap/clojure/src/gravity/digest.clj"
    :namespace 'gravity.digest
    :requires-project-namespaces []}
   {:ordinal 2
    :path "bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"
    :namespace 'gravity.tools.sh07-b51-v9-codec
    :requires-project-namespaces
    '[gravity.c2-artifact-identity.canonical gravity.digest]}])
(def ^:private external-classpath-specs
  [{:ordinal 1 :logical-coordinate 'org.clojure/clojure
    :version "1.12.5" :required-namespace 'clojure.core}
   {:ordinal 2 :logical-coordinate 'org.clojure/core.specs.alpha
    :version "0.4.74" :required-namespace 'clojure.core.specs.alpha}
   {:ordinal 3 :logical-coordinate 'org.clojure/spec.alpha
    :version "0.5.238"
    :required-namespace 'clojure.spec.alpha}])

(def ^:private event-keys
  #{:ordinal :event :role :process-handle-id :argv-id :request-id
    :response-preimage-id :source-session-id :prior-event-root :event-id})
(def ^:private response-keys
  #{:schema :role :transport :result :counters :tcb :diagnostics})
(def ^:private response-transport-keys
  #{:argv-id :request-id :response-preimage-id :self-id-omits})
(def ^:private result-keys
  #{:checker-identity :checker-output :source-read-receipt :production
    :sentinel})
(def ^:private source-receipt-keys
  #{:schema :role :commit :tree :source-descriptors
    :source-descriptors-root :events :event-root :counters :finalized})
(def ^:private product-keys
  #{:m0s :m1 :m2s :bundle-core :execution-bundle-core-id :source-forms
    :module-transactions :source-read-receipt})
(def ^:private source-counter-keys
  #{:acquire-count :decode-count :event-count :finalize-count})
(def ^:private source-descriptor-keys
  #{:ordinal :kind :module-ordinal :path :mode :blob-oid :byte-count
    :raw-sha256})
(def ^:private source-event-keys
  #{:ordinal :domain :source-ordinal :module-ordinal :consumer
    :source-raw-sha256 :product-id})
(def ^:private result-record-keys
  #{:schema :role :status :artifact-id :output-id :authority})
(def ^:private response-counter-keys #{:work :source-session :process :ipc})
(def ^:private work-counter-keys #{:vector :total})
(def ^:private child-process-counter-keys
  #{:child-count :start-count :exit-count :overlap-count})
(def ^:private ipc-counter-keys
  #{:request-count :response-count :request-max-bytes :response-max-bytes
    :stderr-max-bytes :deadline-ms})
(def ^:private tcb-keys
  #{:manifest-id :role :process :runtime-mxbean :java :git :classpath
    :pre-manifest-tcb :eligibility-check :environment :source-closure
    :loaded-namespace-receipt :host-surface})

(defn- reject!
  ([reason] (reject! reason {}))
  ([reason details]
   (throw (ex-info "SH-07 B51 coordinator child rejected"
                   {:id "SH07-B51-CHECKER-PROCESS"
                    :reason reason
                    :details details}))))

(defn- exact-keys? [value expected-keys]
  (and (map? value) (= expected-keys (set (keys value)))))

(defn- git-id? [value]
  (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- sha256-id? [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- nonnegative-long! [value option]
  (try
    (let [parsed (Long/parseLong value)]
      (when (neg? parsed) (reject! :argv-value {:option option}))
      parsed)
    (catch NumberFormatException _
      (reject! :argv-value {:option option}))))

(defn parse-coordinator-arguments!
  "Parse the exact 24-string M option vector without accepting duplicates."
  [arguments]
  (when-not (and (vector? arguments)
                 (= (* 2 (count coordinator-option-names))
                    (count arguments)))
    (reject! :argv {:actual-count (count arguments)}))
  (let [pairs (mapv vec (partition 2 arguments))
        names (mapv first pairs)]
    (when-not (= coordinator-option-names names)
      (reject! :argv-order {:actual names}))
    (let [options (into {} pairs)
          commit (get options "--snapshot-commit")
          tree (get options "--snapshot-tree")
          eligibility
          {:schema :gravity/sh07-b51-external-review-object-v1
           :commit (get options "--eligibility-review-commit")
           :tree (get options "--eligibility-review-tree")
           :parents [commit]
           :path (get options "--eligibility-receipt-path")
           :mode "100644"
           :type "blob"
           :blob-oid (get options "--eligibility-receipt-blob")
           :byte-count
           (nonnegative-long!
            (get options "--eligibility-receipt-byte-count")
            "--eligibility-receipt-byte-count")
           :raw-sha256
           (get options "--eligibility-receipt-raw-sha256")
           :receipt-id (get options "--eligibility-receipt-id")}
          stage (get options "--staging-root")]
      (when-not
       (and (git-id? commit)
            (git-id? tree)
            (= completed-at (get options "--completed-at"))
            (= evidence-relative-root
               (get options "--evidence-relative-root"))
            (string? stage)
            (.isAbsolute (Paths/get stage (make-array String 0)))
            (git-id? (:commit eligibility))
            (git-id? (:tree eligibility))
            (= eligibility-path (:path eligibility))
            (git-id? (:blob-oid eligibility))
            (sha256-id? (:raw-sha256 eligibility))
            (sha256-id? (:receipt-id eligibility)))
        (reject! :argv-value))
      {:arguments arguments
       :content {:commit commit :tree tree}
       :completed-at completed-at
       :evidence-relative-root evidence-relative-root
       :staging-root stage
       :eligibility eligibility})))

(defn validate-closed-environment! [environment]
  (when-not (= closed-environment environment)
    (reject! :environment
             {:expected-keys (set (keys closed-environment))
              :actual-keys (set (keys environment))}))
  environment)

(defn- classpath-entries [classpath]
  (if (string? classpath)
    (str/split classpath
               (re-pattern (java.util.regex.Pattern/quote File/pathSeparator))
               -1)
    []))

(defn validate-literal-classpath! [classpath]
  (let [entries (classpath-entries classpath)]
    (when-not (and (= 4 (count entries))
                   (= 4 (count (distinct entries)))
                   (every? #(and (string? %) (not (str/blank? %))) entries)
                   (every? #(.isAbsolute
                             (Paths/get % (make-array String 0)))
                           entries))
      (reject! :classpath {:entry-count (count entries)}))
    entries))

(defn- canonical-java-path []
  (str (.normalize
        (.toAbsolutePath
         (Paths/get (System/getProperty "java.home")
                    (into-array String ["bin" "java"]))))))

(defn- expected-m-argv [classpath arguments]
  (into [(canonical-java-path) "-Xms64m" "-Xmx8g"
         "-Dfile.encoding=UTF-8" "-Duser.language=en" "-Duser.country=US"
         "-Duser.timezone=UTC" "-Djava.awt.headless=true"
         "-Dclojure.main.report=stderr" "-Duser.home=/nonexistent"
         "-cp" classpath "clojure.main" "-m" coordinator-main-token]
        arguments))

(defn validate-entry-preflight!
  "Run M's project-free argv/environment/working-directory/literal-CP gate."
  [{:keys [arguments environment working-directory classpath process-argv]}]
  (when-not (string? working-directory)
    (reject! :working-directory {:actual working-directory}))
  (let [options (parse-coordinator-arguments! arguments)
        entries (validate-literal-classpath! classpath)
        cwd (Paths/get working-directory (make-array String 0))]
    (validate-closed-environment! environment)
    (when-not (and (.isAbsolute cwd)
                   (Files/isDirectory cwd (make-array LinkOption 0)))
      (reject! :working-directory {:actual working-directory}))
    (let [expected (expected-m-argv classpath arguments)]
      (when-not (= 39 (count expected))
        (reject! :m-argv-count {:actual (count expected)}))
      (when (and process-argv (not= expected process-argv))
        (reject! :m-argv-self-report))
      (assoc options
             :working-directory working-directory
             :environment {:working-directory working-directory
                           :variables environment}
             :classpath entries
             :classpath-string classpath
             :m-argv (or process-argv expected)))))

(defn- sha256-bytes-core [bytes]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256") ^bytes bytes)
        out (StringBuilder. 71)]
    (.append out "sha256:")
    (doseq [value digest]
      (let [unsigned (bit-and 0xff value)]
        (.append out (Character/forDigit (bit-shift-right unsigned 4) 16))
        (.append out (Character/forDigit (bit-and unsigned 0x0f) 16))))
    (str out)))

(defn- strict-utf8-core! [bytes reason]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (try
      (.toString (.decode decoder (ByteBuffer/wrap ^bytes bytes)))
      (catch java.nio.charset.CharacterCodingException _
        (reject! reason)))))

(defn- read-bounded-core! [^InputStream input maximum reason]
  (let [out (ByteArrayOutputStream.)
        buffer (byte-array 8192)]
    (loop [total 0]
      (let [read (.read input buffer)]
        (if (neg? read)
          (.toByteArray out)
          (let [next-total (+ total read)]
            (when (> next-total maximum)
              (reject! reason {:maximum maximum :actual next-total}))
            (.write out buffer 0 read)
            (recur next-total)))))))

(defn- completed-future-value! [^Future future]
  (try
    (.get future)
    (catch ExecutionException error
      (throw (or (.getCause error) error)))))

(declare remaining-millis close-quietly! terminate-process!)

(defn- run-project-free-process!
  [working-directory argv stdout-maximum operation]
  (let [builder (doto (ProcessBuilder. ^java.util.List argv)
                  (.directory (.toFile
                               (Paths/get working-directory
                                          (make-array String 0)))))
        _ (doto (.environment builder)
            (.clear)
            (.putAll closed-environment))
        process (.start builder)
        stdout (.getInputStream process)
        stderr (.getErrorStream process)
        stdin (.getOutputStream process)
        executor (Executors/newFixedThreadPool 3)
        completions (ExecutorCompletionService. executor)
        stdout-task
        (.submit completions ^Callable
                 (reify Callable
                   (call [_]
                     [:stdout (read-bounded-core! stdout stdout-maximum
                                                  :git-stdout-overflow)])))
        stderr-task
        (.submit completions ^Callable
                 (reify Callable
                   (call [_]
                     [:stderr (read-bounded-core!
                               stderr (:stderr-max-bytes transport-limits)
                               :git-stderr-overflow)])))
        _ (.close ^OutputStream stdin)
        process-task
        (.submit completions ^Callable
                 (reify Callable
                   (call [_] [:exit (.waitFor process)])))
        tasks [stdout-task stderr-task process-task]
        deadline (+ (System/nanoTime)
                    (* 1000000 (:deadline-ms transport-limits)))]
    (try
      (let [parts
            (loop [remaining 3 result {}]
              (if (zero? remaining)
                result
                (let [millis (remaining-millis deadline)
                      completed (when (pos? millis)
                                  (.poll completions millis
                                         TimeUnit/MILLISECONDS))]
                  (when-not completed
                    (throw (TimeoutException. "project-free process")))
                  (let [[kind value] (completed-future-value! completed)]
                    (recur (dec remaining) (assoc result kind value))))))
            stdout-bytes (:stdout parts)
            stderr-bytes (:stderr parts)]
        (when-not (and (zero? (:exit parts))
                       (zero? (alength ^bytes stderr-bytes)))
          (reject! :git-object-operation
                   {:operation operation :exit-code (:exit parts)
                    :stderr-byte-count (alength ^bytes stderr-bytes)}))
        {:operation operation :argv argv
         :stdin-byte-count 0 :stdin-raw-sha256 empty-raw-sha256
         :exit-code 0
         :stdout-bytes stdout-bytes
         :stdout-byte-count (alength ^bytes stdout-bytes)
         :stdout-raw-sha256 (sha256-bytes-core stdout-bytes)
         :stderr-byte-count 0 :stderr-raw-sha256 empty-raw-sha256})
      (catch Throwable error
        (doseq [task tasks] (.cancel ^Future task true))
        (close-quietly! stdin)
        (close-quietly! stdout)
        (close-quietly! stderr)
        (terminate-process! process)
        (if (= "SH07-B51-CHECKER-PROCESS" (:id (ex-data error)))
          (throw error)
          (reject! :git-object-operation
                   {:operation operation
                    :cause (.getName (class error))})))
      (finally
        (close-quietly! stdin)
        (close-quietly! stdout)
        (close-quietly! stderr)
        (.shutdownNow executor)
        (.awaitTermination executor 1000 TimeUnit/MILLISECONDS)))))

(defn- git-operation-core!
  [options operation arguments stdout-maximum]
  (run-project-free-process!
   (:working-directory options)
   (into ["/usr/bin/git" "-C" (:working-directory options)] arguments)
   stdout-maximum operation))

(defn- nul-records-core! [bytes reason]
  (let [text (strict-utf8-core! bytes reason)]
    (cond
      (empty? text) []
      (not (str/ends-with? text "\u0000")) (reject! reason)
      :else (vec (butlast (str/split text #"\u0000" -1))))))

(defn- parse-show-core! [bytes]
  (let [text (strict-utf8-core! bytes :git-show-grammar)
        match (re-matches #"([0-9a-f]{40})\n([0-9a-f]{40})\n([0-9a-f ]*)\n"
                          text)]
    (when-not match (reject! :git-show-grammar))
    {:commit (nth match 1) :tree (nth match 2)
     :parents (let [parents (nth match 3)]
                (if (str/blank? parents) [] (str/split parents #" ")))}))

(defn- parse-ls-tree-core! [bytes requested-paths]
  (let [records (nul-records-core! bytes :git-ls-tree-grammar)
        rows
        (mapv
         (fn [record]
           (let [match
                 (re-matches #"([0-7]{6}) ([a-z]+) ([0-9a-f]{40})\t(.+)"
                             record)]
             (when-not match (reject! :git-ls-tree-grammar))
             {:mode (nth match 1) :type (nth match 2)
              :oid (nth match 3) :path (nth match 4)}))
         records)
        by-path (into {} (map (juxt :path identity) rows))]
    (when-not (and (= (count rows) (count by-path))
                   (every? (set requested-paths) (keys by-path)))
      (reject! :git-ls-tree-grammar))
    (mapv #(or (get by-path %)
               {:path % :mode nil :type nil :oid nil})
          requested-paths)))

(defn- show-content-core! [options]
  (let [operation
        (git-operation-core!
         options :show-tree
         ["show" "-s" "--format=%H%n%T%n%P"
          (get-in options [:content :commit])]
         4096)
        parsed (parse-show-core! (:stdout-bytes operation))]
    (when-not (= (:content options) (select-keys parsed [:commit :tree]))
      (reject! :content-coordinate))
    (assoc (dissoc operation :stdout-bytes) :parsed-result parsed)))

(defn- git-path-object-core! [options path]
  (let [commit (get-in options [:content :commit])
        tree-operation
        (git-operation-core! options :ls-tree-z-requested
                             ["ls-tree" "-z" commit "--" path]
                             1048576)
        row (first (parse-ls-tree-core! (:stdout-bytes tree-operation)
                                        [path]))]
    (when-not (and (= "100644" (:mode row)) (= "blob" (:type row)))
      (reject! :c-source-object {:path path :row row}))
    (let [blob-operation
          (git-operation-core! options :cat-file-blob
                               ["cat-file" "blob" (:oid row)]
                               (:response-max-bytes transport-limits))
          bytes (:stdout-bytes blob-operation)]
      {:path path :mode (:mode row) :blob-oid (:oid row)
       :byte-count (alength ^bytes bytes)
       :raw-sha256 (sha256-bytes-core bytes) :bytes bytes})))

(defn- read-source-forms-core! [bytes path]
  (let [text (strict-utf8-core! bytes :trusted-tcb-utf8)]
    (with-open [reader
                (LineNumberingPushbackReader.
                 (InputStreamReader.
                  (ByteArrayInputStream.
                   (.getBytes text StandardCharsets/UTF_8))
                  StandardCharsets/UTF_8))]
      (binding [*read-eval* false]
        (let [eof (Object.)]
          (loop [forms []]
            (let [form (read {:eof eof :read-cond :allow :features #{:clj}}
                             reader)]
              (if (identical? eof form)
                forms
                (recur (conj forms form))))))))))

(defn- require-spec-namespace-core [spec]
  (cond
    (symbol? spec) spec
    (and (vector? spec) (symbol? (first spec))) (first spec)
    :else nil))

(defn- source-namespace-core! [bytes path]
  (let [forms (read-source-forms-core! bytes path)
        ns-forms (filter #(and (seq? %) (= 'ns (first %))) forms)
        first-form (first forms)]
    (when (and (seq ns-forms)
               (or (not= 1 (count ns-forms))
                   (not= first-form (first ns-forms))))
      (reject! :trusted-tcb-namespace {:path path}))
    (when (and first-form (not (seq ns-forms))
               (some #(and (seq? %) (= 'ns (first %))) (rest forms)))
      (reject! :trusted-tcb-namespace {:path path}))
    (when (seq ns-forms)
      (let [ns-form (first ns-forms)
            namespace (second ns-form)
            project-requires
            (->> (drop 2 ns-form)
                 (filter #(and (seq? %) (= :require (first %))))
                 (mapcat rest)
                 (keep require-spec-namespace-core)
                 (filter #(str/starts-with? (str %) "gravity."))
                 vec)]
        {:namespace namespace
         :requires-project-namespaces project-requires}))))

(defn- symlink-free-core? [^Path path]
  (loop [current (.normalize (.toAbsolutePath path))]
    (cond
      (nil? current) true
      (Files/isSymbolicLink current) false
      :else (recur (.getParent current)))))

(defn- materialized-source-bytes-core! [options path maximum]
  (when-not (str/starts-with? path classpath-source-prefix)
    (reject! :materialized-source-path {:path path}))
  (let [root (Paths/get (first (:classpath options)) (make-array String 0))
        relative (subs path (count classpath-source-prefix))
        resolved (.normalize (.resolve root relative))]
    (when-not (and (.startsWith resolved root)
                   (Files/isRegularFile
                    resolved
                    (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   (symlink-free-core? resolved))
      (reject! :materialized-source-path {:path path}))
    (with-open [input (Files/newInputStream resolved (make-array java.nio.file.OpenOption 0))]
      (read-bounded-core! input maximum :materialized-source-overflow))))

(defn pre-manifest-tcb!
  "Authenticate the entrypoint and three codec leaves from C objects before
  loading any project namespace."
  [options entrypoint-namespace entrypoint-path]
  (show-content-core! options)
  (let [entrypoint-spec
        {:ordinal 0 :path entrypoint-path :namespace entrypoint-namespace
         :requires-project-namespaces []}
        row-for
        (fn [spec]
          (let [object (git-path-object-core! options (:path spec))
                materialized
                (materialized-source-bytes-core!
                 options (:path spec) (:response-max-bytes transport-limits))
                parsed (source-namespace-core! (:bytes object) (:path spec))]
            (when-not
             (and (Arrays/equals ^bytes (:bytes object) ^bytes materialized)
                  (= (:namespace spec) (:namespace parsed))
                  (= (:requires-project-namespaces spec)
                     (:requires-project-namespaces parsed)))
              (reject! :trusted-tcb-source {:path (:path spec)}))
            {:ordinal (:ordinal spec) :path (:path spec)
             :namespace (:namespace spec) :mode "100644"
             :blob-oid (:blob-oid object) :byte-count (:byte-count object)
             :raw-sha256 (:raw-sha256 object)
             :materialized-raw-sha256 (sha256-bytes-core materialized)
             :requires-project-namespaces
             (:requires-project-namespaces spec)}))
        entrypoint (row-for entrypoint-spec)
        members (mapv row-for codec-tcb-specs)
        codec-tcb {:schema :gravity/sh07-b51-codec-tcb-v1
                   :load-order codec-tcb-load-order :members members}
        result {:entrypoint entrypoint :codec-tcb codec-tcb}]
    (when-not (and (= pre-manifest-row-keys (set (keys entrypoint)))
                   (every? #(= pre-manifest-row-keys (set (keys %))) members)
                   (= (mapv :namespace members) codec-tcb-load-order)
                   (every? #(= (:raw-sha256 %)
                               (:materialized-raw-sha256 %))
                           (into [entrypoint] members)))
      (reject! :trusted-tcb))
    result))

(defn- resolve-public! [namespace-name var-name]
  (or (ns-resolve (symbol namespace-name) (symbol var-name))
      (reject! :missing-public-var
               {:namespace namespace-name :var var-name})))

(defn load-codec! []
  (require (symbol codec-namespace))
  (let [transport @(resolve-public! codec-namespace "transport-api")
        api
        (merge {:H (resolve-public! codec-namespace "H")
                :W (resolve-public! codec-namespace "W")
                :R (resolve-public! codec-namespace "R")
                :limits @(resolve-public! codec-namespace "transport-limits")}
               transport)]
    (when-not (= (dissoc transport-limits :deadline-ms) (:limits api))
      (reject! :codec-limits {:actual (:limits api)}))
    api))

(defn- parse-recursive-tree-core! [bytes]
  (let [records (nul-records-core! bytes :classpath-tree-grammar)
        rows
        (mapv
         (fn [record]
           (let [match
                 (re-matches #"([0-7]{6}) ([a-z]+) ([0-9a-f]{40})\t(.+)"
                             record)]
             (when-not match (reject! :classpath-tree-grammar))
             {:mode (nth match 1) :type (nth match 2)
              :oid (nth match 3) :path (nth match 4)}))
         records)]
    (when-not (and (seq rows)
                   (= (count rows) (count (set (map :path rows))))
                   (every? #(and (= "100644" (:mode %))
                                 (= "blob" (:type %))
                                 (str/starts-with? (:path %)
                                                   classpath-source-prefix))
                           rows))
      (reject! :classpath-tree))
    rows))

(defn- git-blob-core! [options oid]
  (let [operation
        (git-operation-core! options :cat-file-blob
                             ["cat-file" "blob" oid]
                             (:response-max-bytes transport-limits))]
    (:stdout-bytes operation)))

(defn- file-key-core! [^Path path]
  (let [attributes
        (Files/readAttributes
         path BasicFileAttributes
         (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
        key (.fileKey ^BasicFileAttributes attributes)]
    (when-not key (reject! :classpath-file-key {:path (str path)}))
    (str key)))

(defn- canonical-file-core! [path]
  (let [value (Paths/get path (make-array String 0))
        absolute (.normalize (.toAbsolutePath value))]
    (when-not (and (Files/isRegularFile
                    absolute
                    (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   (symlink-free-core? absolute))
      (reject! :classpath-file {:path path}))
    (.toRealPath absolute (make-array LinkOption 0))))

(defn- jar-source-records-core! [^Path path codec]
  (with-open [jar (JarFile. (.toFile path))]
    (let [records
          (->> (enumeration-seq (.entries jar))
               (remove #(.isDirectory %))
               (filter #(or (str/ends-with? (.getName %) ".clj")
                            (str/ends-with? (.getName %) ".cljc")))
               (keep
                (fn [entry]
                  (with-open [input (.getInputStream jar entry)]
                    (let [bytes
                          (read-bounded-core! input 67108864
                                              :classpath-jar-entry-overflow)
                          parsed
                          (source-namespace-core! bytes (.getName entry))]
                      (when parsed
                        [(:namespace parsed)
                         {:namespace (:namespace parsed)
                          :resource (.getName entry)
                          :byte-count (alength ^bytes bytes)
                          :source-sha256 ((:R codec) bytes)}])))))
               vec)
          by-namespace (into {} records)]
      (when-not (= (count records) (count by-namespace))
        (reject! :classpath-jar-namespace-duplicate {:path (str path)}))
      by-namespace)))

(defn- jar-classpath-entry-core! [path spec codec]
  (let [canonical (canonical-file-core! path)
        filename (str (.getFileName canonical))
        expected-tail (str (:version spec) ".jar")]
    (when-not (str/ends-with? filename expected-tail)
      (reject! :classpath-coordinate
               {:ordinal (:ordinal spec) :file filename
                :version (:version spec)}))
    (let [bytes
          (with-open [input (Files/newInputStream
                             canonical
                             (make-array java.nio.file.OpenOption 0))]
            (read-bounded-core! input 1073741824 :classpath-jar-overflow))
          records (jar-source-records-core! canonical codec)
          namespaces (->> (keys records) (sort-by str) vec)]
      (when-not (some #{(:required-namespace spec)} namespaces)
        (reject! :classpath-coordinate
                 {:ordinal (:ordinal spec)
                  :required-namespace (:required-namespace spec)}))
      {:entry
       {:ordinal (:ordinal spec) :kind :jar
        :logical-coordinate
        {:artifact (:logical-coordinate spec) :version (:version spec)}
        :canonical-path (str canonical) :file-key (file-key-core! canonical)
        :byte-count (alength ^bytes bytes) :sha256 ((:R codec) bytes)
        :provided-namespaces namespaces}
       :external-source-records records})))

(defn validate-complete-classpath!
  "Re-read every entry-0 blob from C, compare every materialized byte, bind
  the exact three external JAR versions, and derive the classpath manifest.
  This must run immediately after the admitted codec loads and before any
  other project namespace is loaded."
  [options codec]
  (let [tree-operation
        (git-operation-core!
         options :ls-tree-z-recursive
         ["ls-tree" "-r" "-z" (get-in options [:content :commit]) "--"
          "bootstrap/clojure/src"]
         (:response-max-bytes transport-limits))
        tree-rows (parse-recursive-tree-core! (:stdout-bytes tree-operation))
        authenticated
        (mapv
         (fn [{:keys [path oid mode]}]
           (let [object-bytes (git-blob-core! options oid)
                 materialized
                 (materialized-source-bytes-core!
                  options path (:response-max-bytes transport-limits))
                 raw ((:R codec) object-bytes)]
             (when-not (Arrays/equals ^bytes object-bytes
                                      ^bytes materialized)
               (reject! :classpath-materialized-byte-drift {:path path}))
             {:path path :mode mode :blob-oid oid
              :byte-count (alength ^bytes object-bytes)
              :raw-sha256 raw :bytes object-bytes}))
         tree-rows)
        manifest-rows
        (mapv (fn [{:keys [path blob-oid byte-count raw-sha256]}]
                [(subs path (count classpath-source-prefix))
                 blob-oid byte-count raw-sha256])
              authenticated)
        manifest-root
        ((:H codec)
         [:gravity/sh07-b51-classpath-manifest-root-v1 manifest-rows])
        project-namespaces
        (->> authenticated
             (filter #(or (str/ends-with? (:path %) ".clj")
                          (str/ends-with? (:path %) ".cljc")))
             (keep #(some-> (source-namespace-core! (:bytes %) (:path %))
                            :namespace))
             vec)
        _ (when-not (= (count project-namespaces)
                       (count (distinct project-namespaces)))
            (reject! :classpath-project-namespace-duplicate))
        root (Paths/get (first (:classpath options)) (make-array String 0))
        project-entry
        {:ordinal 0 :kind :directory
         :logical-coordinate
         {:artifact 'project/bootstrap-clojure-src
          :commit (get-in options [:content :commit])
          :tree (get-in options [:content :tree])}
         :canonical-path (str (.toRealPath root (make-array LinkOption 0)))
         :file-key (file-key-core! root)
         :byte-count (reduce + 0 (map :byte-count authenticated))
         :sha256 manifest-root
         :provided-namespaces (vec (sort-by str project-namespaces))}
        external
        (mapv (fn [path spec]
                (jar-classpath-entry-core! path spec codec))
              (subvec (vec (:classpath options)) 1)
              external-classpath-specs)
        classpath (into [project-entry] (map :entry external))
        namespace-groups (map :provided-namespaces classpath)
        all-namespaces (vec (mapcat identity namespace-groups))
        external-records
        (reduce
         (fn [result item]
           (reduce-kv
            (fn [result namespace record]
              (when (contains? result namespace)
                (reject! :classpath-namespace-shadowing
                         {:namespace namespace}))
              (assoc result namespace record))
            result (:external-source-records item)))
         {} external)]
    (when-not (and (= 4 (count classpath))
                   (= (vec (range 4)) (mapv :ordinal classpath))
                   (= (count all-namespaces) (count (distinct all-namespaces))))
      (reject! :classpath-namespace-shadowing))
    {:classpath-receipt classpath
     :classpath-manifest-rows manifest-rows
     :classpath-manifest-root manifest-root
     :source-authority
     {:content (:content options)
      :source-objects (into {} (map (juxt :path identity) authenticated))
      :external-source-records external-records}}))

(defn checker-request
  "Construct the only child IPC value.  It contains no semantic envelope."
  [options role]
  (when-not (contains? child-role-specs role)
    (reject! :role {:actual role}))
  (when-not (sha256-id? (:architecture-report-sha256 options))
    (reject! :eligibility-check-required
             {:field :architecture-report-sha256}))
  {:schema :gravity/sh07-b51-checker-child-request-v4
   :role role
   :content (:content options)
   :eligibility (:eligibility options)
   :evidence
   {:architecture-report-sha256 (:architecture-report-sha256 options)
    :profile-bounds profile-bounds}
   :limits transport-limits})

(defn child-argv
  "Return exact A/B argv.  The checker-child class name remains opaque text."
  [options role]
  (let [spec (or (get child-role-specs role)
                 (reject! :role {:actual role}))
        java-path
        (str (.normalize
              (.toAbsolutePath
               (Paths/get (System/getProperty "java.home")
                          (into-array String ["bin" "java"]))))) ]
    [java-path "-Xms64m" "-Xmx8g" "-Dfile.encoding=UTF-8"
     "-Duser.language=en" "-Duser.country=US" "-Duser.timezone=UTC"
     "-Djava.awt.headless=true" "-Dclojure.main.report=stderr"
     "-Duser.home=/nonexistent" "-cp" (:classpath-string options)
     "clojure.main" "-m" child-main-token "--role" (:cli spec)]))

(defn build-child
  "Construct, but do not start, one closed-environment checker child."
  [options codec role]
  (let [request (checker-request options role)
        request-bytes ((:bounded-wire-bytes codec) :request request)
        argv (child-argv options role)
        H (:H codec)
        request-id
        (H [:gravity/sh07-b51-checker-request-id-v1 request])
        argv-id (H [:gravity/sh07-b51-process-argv-id-v1 argv])
        builder (doto (ProcessBuilder. ^java.util.List argv)
                  (.directory (.toFile
                               (Paths/get (:working-directory options)
                                          (make-array String 0)))))]
    (doto (.environment builder)
      (.clear)
      (.putAll closed-environment))
    {:role role :sequence (:sequence (get child-role-specs role))
     :builder builder :argv argv :argv-id argv-id
     :request request :request-bytes request-bytes :request-id request-id}))

(defn- optional-value [^Optional value]
  (when (.isPresent value) (.get value)))

(defn- process-start-observation [built ^Process process codec]
  (let [handle (.toHandle process)
        info (.info handle)
        parent (optional-value (.parent handle))
        command (optional-value (.command info))
        arguments (some-> (optional-value (.arguments info)) vec)
        started (optional-value (.startInstant info))
        expected-argv (:argv built)]
    (when-not (and parent command arguments started
                   (= expected-argv (into [command] arguments)))
      (reject! :retained-process-start {:role (:role built)}))
    (let [coordinate
          {:role (:role built)
           :child-sequence (:sequence built)
           :normalized-argv-id (:argv-id built)
           :request-id (:request-id built)}]
      {:role (:role built)
       :process-handle-id
       ((:H codec)
        [:gravity/sh07-b51-process-handle-coordinate-id-v1 coordinate])
       :pid (.pid handle)
       :parent-pid (.pid ^ProcessHandle parent)
       :command command
       :arguments arguments
       :start-instant (str ^Instant started)})))

(defn start-child!
  "Start a previously constructed child and retain its process and pipes."
  [built codec]
  (let [process (.start ^ProcessBuilder (:builder built))]
    (assoc built
           :process process
           :process-start (process-start-observation built process codec))))

(defn- remaining-millis [deadline-nanos]
  (max 0 (long (Math/ceil
                (/ (double (- deadline-nanos (System/nanoTime)))
                   1000000.0)))))

(defn- close-quietly! [value]
  (when (instance? Closeable value)
    (try (.close ^Closeable value) (catch Throwable _ nil))))

(defn- terminate-process! [^Process process]
  (when (.isAlive process)
    (.destroy process)
    (when-not (.waitFor process 250 TimeUnit/MILLISECONDS)
      (.destroyForcibly process)
      (.waitFor process 1000 TimeUnit/MILLISECONDS)))
  (when (.isAlive process)
    (reject! :process-cleanup-timeout)))

(defn join-child!
  "Join writer, child, and concurrent bounded drains under one deadline.

  Parsing is deliberately absent here; the ordinal-3/7 exit observation is
  recorded before the coordinator validates the returned bytes."
  [started codec]
  (let [^Process process (:process started)
        executor (Executors/newFixedThreadPool 4)
        completions (ExecutorCompletionService. executor)
        request-bytes (:request-bytes started)
        stdout (.getInputStream process)
        stderr (.getErrorStream process)
        stdin (.getOutputStream process)
        read-bounded (:read-bounded-bytes codec)
        stdout-task
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_] [:stdout (read-bounded :response stdout)])))
        stderr-task
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_] [:stderr (read-bounded :stderr stderr)])))
        writer-task
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_]
                     ((:bounded-byte-count! codec)
                      :request (alength ^bytes request-bytes))
                     (.write ^OutputStream stdin ^bytes request-bytes)
                     (.flush ^OutputStream stdin)
                     (.close ^OutputStream stdin)
                     [:writer
                      {:byte-count (alength ^bytes request-bytes)
                       :closed true}])))
        process-task
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_]
                     (.waitFor process)
                     [:process (.exitValue process)])))
        tasks [stdout-task stderr-task writer-task process-task]
        deadline (+ (System/nanoTime)
                    (* 1000000 (:deadline-ms transport-limits)))]
    (try
      (let [results
            (loop [remaining-tasks 4
                   results {}]
              (if (zero? remaining-tasks)
                results
                (let [remaining (remaining-millis deadline)
                      _ (when (zero? remaining)
                          (throw (TimeoutException. "child tasks")))
                      completed
                      (.poll completions remaining TimeUnit/MILLISECONDS)]
                  (when-not completed
                    (throw (TimeoutException. "child tasks")))
                  (let [[task value]
                        (try
                          (.get ^Future completed)
                          (catch ExecutionException error
                            (throw (or (.getCause error) error))))]
                    (recur (dec remaining-tasks)
                           (assoc results task value))))))
            writer (:writer results)
            stdout-bytes (:stdout results)
            stderr-bytes (:stderr results)]
        (assoc started
               :exit-code (:process results)
               :stdout-bytes stdout-bytes
               :stderr-bytes stderr-bytes
               :transport
               {:stdin-byte-count (:byte-count writer)
                :stdin-raw-sha256 ((:R codec) request-bytes)
                :stdin-closed-before-wait (:closed writer)
                :stdout-cap (:response-max-bytes transport-limits)
                :stderr-cap (:stderr-max-bytes transport-limits)
                :deadline-ms (:deadline-ms transport-limits)
                :stdout-drain-started-before-wait true
                :stderr-drain-started-before-wait true
                :stdout-drain-complete true
                :stderr-drain-complete true
                :timed-out false
                :destroy-called false
                :destroy-forcibly-called false
                :tasks-joined true}))
      (catch Throwable error
        (doseq [task tasks] (.cancel ^Future task true))
        (close-quietly! stdin)
        (close-quietly! stdout)
        (close-quietly! stderr)
        (terminate-process! process)
        (reject! (if (instance? TimeoutException error)
                   :deadline :transport-task)
                 {:role (:role started)
                  :cause (.getName (class error))}))
      (finally
        (close-quietly! stdin)
        (close-quietly! stdout)
        (close-quietly! stderr)
        (.shutdownNow executor)
        (.awaitTermination executor 1000 TimeUnit/MILLISECONDS)))))

(defn- operation-row-core [ordinal operation parsed]
  (assoc (dissoc operation :stdout-bytes)
         :ordinal ordinal :parsed-result parsed))

(defn- parse-diff-added-core! [bytes content-commit q path]
  (let [tokens (nul-records-core! bytes :eligibility-diff-grammar)]
    (when-not (= ["A" path] tokens)
      (reject! :eligibility-diff-grammar {:actual tokens}))
    {:parent content-commit :commit q
     :entries [{:status :added :path path}]}))

(defn- eligibility-five-operations!
  [options codec]
  (let [coordinate (:eligibility options)
        c (get-in options [:content :commit])
        q (:commit coordinate)
        path (:path coordinate)
        show
        (git-operation-core! options :show-tree
                             ["show" "-s" "--format=%H%n%T%n%P" q] 4096)
        show-value (parse-show-core! (:stdout-bytes show))
        diff
        (git-operation-core!
         options :diff-tree-name-status-z
         ["diff-tree" "--no-commit-id" "--name-status" "-r" "-z" c q]
         1048576)
        diff-value (parse-diff-added-core! (:stdout-bytes diff) c q path)
        tree-q
        (git-operation-core! options :ls-tree-z-requested
                             ["ls-tree" "-z" q "--" path] 1048576)
        q-row (first (parse-ls-tree-core! (:stdout-bytes tree-q) [path]))
        blob
        (git-operation-core! options :cat-file-blob
                             ["cat-file" "blob" (:blob-oid coordinate)]
                             (:response-max-bytes transport-limits))
        bytes (:stdout-bytes blob)
        receipt ((:strict-read-wire codec) :response bytes)
        blob-value {:oid (:blob-oid coordinate)
                    :byte-count (alength ^bytes bytes)
                    :raw-sha256 ((:R codec) bytes)}
        tree-c
        (git-operation-core! options :ls-tree-z-requested
                             ["ls-tree" "-z" c "--" path] 1048576)
        c-row (first (parse-ls-tree-core! (:stdout-bytes tree-c) [path]))
        observations
        [(operation-row-core 0 show show-value)
         (operation-row-core 1 diff diff-value)
         (operation-row-core 2 tree-q
                             {:commit q :requested-paths [path]
                              :rows [(assoc q-row :present true)]})
         (operation-row-core 3 blob blob-value)
         (operation-row-core 4 tree-c
                             {:commit c :requested-paths [path]
                              :rows [(assoc c-row :present false)]})]]
    (when-not
     (and (= {:commit q :tree (:tree coordinate) :parents [c]} show-value)
          (= "100644" (:mode q-row)) (= "blob" (:type q-row))
          (= (:blob-oid coordinate) (:oid q-row))
          (nil? (:mode c-row)) (nil? (:type c-row)) (nil? (:oid c-row))
          (= (:byte-count coordinate) (:byte-count blob-value))
          (= (:raw-sha256 coordinate) (:raw-sha256 blob-value)))
      (reject! :eligibility-object-join))
    {:receipt receipt :observations observations}))

(defn- authenticate-eligibility-input!
  [options codec]
  (let [coordinate (:eligibility options)
        {:keys [receipt observations]}
        (eligibility-five-operations! options codec)
        freeze (:freeze receipt)
        active-record (:active-record-ref freeze)
        container (:container active-record)
        transition (:transition active-record)
        review (:review receipt)
        descriptors (:source-descriptors receipt)
        architecture-report-sha256
        (:architecture-report-sha256 transition)]
    (when-not
     (and (exact-keys? receipt eligibility-receipt-keys)
          (= :gravity/sh07-b51-evidence-eligibility-receipt-v2
             (:artifact receipt))
          (= 2 (:schema-version receipt))
          (= (:content options) (:content receipt))
          (= :evidence-eligible (:state receipt))
          (= [[:receipt-id]] (:self-id-omits receipt))
          (= nonclaims (:nonclaims receipt))
          (= (:receipt-id coordinate) (:receipt-id receipt))
          (= (:receipt-id receipt)
             ((:H codec)
              [:gravity/sh07-b51-evidence-eligibility-receipt-v2
               (dissoc receipt :receipt-id)]))
          (exact-keys? freeze
                       #{:state :candidate-commit :candidate-tree
                         :active-record-ref :frozen-at})
          (= :frozen (:state freeze))
          (= (:content options)
             {:commit (:candidate-commit freeze)
              :tree (:candidate-tree freeze)})
          (exact-keys? active-record
                       #{:container :value-path :record-id :transition})
          (exact-keys? container
                       #{:commit :tree :parents :path :mode :type :blob-oid
                         :byte-count :raw-sha256})
          (= [] (:value-path active-record))
          (= (:record-id active-record) (:raw-sha256 container))
          (exact-keys? transition
                       #{:workstream-id :from :to :candidate-commit
                         :candidate-tree :architecture-report-sha256
                         :history-ordinal})
          (= :draft (:from transition))
          (= :frozen (:to transition))
          (= (:content options)
             {:commit (:candidate-commit transition)
              :tree (:candidate-tree transition)})
          (sha256-id? architecture-report-sha256)
          (exact-keys? review
                       #{:reviewer :candidate-commit :candidate-tree
                         :artifact-path :artifact-raw-sha256 :result
                         :completed-at})
          (= :evidence-eligible (:result review))
          (= (:content options)
             {:commit (:candidate-commit review)
              :tree (:candidate-tree review)})
          (= completed-at (:completed-at review))
          (vector? descriptors) (= 12 (count descriptors))
          (= (:source-descriptors-root receipt)
             ((:H codec)
              [:gravity/sh07-b51-source-descriptors-root-v1 descriptors])))
      (reject! :eligibility-object-invalid))
    (assoc options
           :architecture-report-sha256 architecture-report-sha256
           :eligibility-preflight
           {:status :immutable-q-coordinate-validated
            :coordinate coordinate
            :observations observations
            :receipt-id (:receipt-id receipt)
            :architecture-report-sha256 architecture-report-sha256
            :remaining-gaps
            [:pre-discharge-77-operation-reconstruction]})))

(defn validate-child!
  "Strictly parse and validate a joined child after its exit event is recorded."
  [joined codec]
  (let [role (:role joined)
        stderr-bytes (:stderr-bytes joined)]
    (when-not (and (zero? (:exit-code joined))
                   (zero? (alength ^bytes stderr-bytes)))
      (reject! :child-exit
               {:role role :exit (:exit-code joined)
                :stderr-byte-count (alength ^bytes stderr-bytes)}))
    (let [response
          ((:strict-read-wire codec) :response (:stdout-bytes joined))
          transport (:transport response)
          result (:result response)
          receipt (:source-read-receipt result)
          descriptors (:source-descriptors receipt)
          events (:events receipt)
          production (:production result)
          sentinel (:sentinel result)
          counters (:counters response)
          tcb (:tcb response)
          without-self-id
          (update response :transport dissoc :response-preimage-id)
          response-id
          ((:H codec)
           [:gravity/sh07-b51-response-preimage-id-v1 without-self-id])]
      (when-not
       (and (exact-keys? response response-keys)
            (= :gravity/sh07-b51-checker-child-response-v1
               (:schema response))
            (= role (:role response))
            (exact-keys? transport response-transport-keys)
            (= (:argv-id joined) (:argv-id transport))
            (= (:request-id joined) (:request-id transport))
            (= response-id (:response-preimage-id transport))
            (= [[:transport :response-preimage-id]]
               (:self-id-omits transport))
            (exact-keys? result result-keys)
            (= :accepted (get-in result [:checker-output :status]))
            (every? #(exact-keys? % result-record-keys)
                    [production sentinel])
            (= [role role] [(:role production) (:role sentinel)])
            (= [:accepted :accepted]
               [(:status production) (:status sentinel)])
            (= [true false] [(:authority production) (:authority sentinel)])
            (= [:gravity/sh07-b51-checker-production-v1
                :gravity/sh07-b51-checker-sentinel-v1]
               [(:schema production) (:schema sentinel)])
            (= (:artifact-id production)
               ((:H codec)
                [:gravity/sh07-b51-checker-result-artifact-id-v1
                 (dissoc production :artifact-id :output-id)]))
            (= (:output-id production)
               ((:H codec)
                [:gravity/sh07-b51-checker-result-output-id-v1
                 (dissoc production :output-id)]))
            (= (:artifact-id sentinel)
               ((:H codec)
                [:gravity/sh07-b51-checker-result-artifact-id-v1
                 (dissoc sentinel :artifact-id :output-id)]))
            (= (:output-id sentinel)
               ((:H codec)
                [:gravity/sh07-b51-checker-result-output-id-v1
                 (dissoc sentinel :output-id)]))
            (exact-keys? receipt source-receipt-keys)
            (= role (:role receipt))
            (= (:content (:request joined))
               (select-keys receipt [:commit :tree]))
            (vector? descriptors) (= 12 (count descriptors))
            (every? #(and (exact-keys? % source-descriptor-keys)
                          (= "100644" (:mode %))
                          (git-id? (:blob-oid %))
                          (sha256-id? (:raw-sha256 %)))
                    descriptors)
            (= (vec (range 12)) (mapv :ordinal descriptors))
            (= (:source-descriptors-root receipt)
               ((:H codec)
                [:gravity/sh07-b51-source-descriptors-root-v1 descriptors]))
            (vector? events) (= 39 (count events))
            (every? #(and (exact-keys? % source-event-keys)
                          (sha256-id? (:source-raw-sha256 %))
                          (sha256-id? (:product-id %))
                          (= (get-in descriptors
                                     [(:source-ordinal %) :raw-sha256])
                             (:source-raw-sha256 %)))
                    events)
            (= (vec (range 39)) (mapv :ordinal events))
            (= (:event-root receipt)
               ((:H codec)
                [:gravity/sh07-b51-source-event-root-v1 events]))
            (exact-keys? (:counters receipt) source-counter-keys)
            (= {:acquire-count 12 :decode-count 12
                :event-count 39 :finalize-count 1}
               (:counters receipt))
            (true? (:finalized receipt))
            (exact-keys? counters response-counter-keys)
            (exact-keys? (:work counters) work-counter-keys)
            (= {:vector [3 3 3 2 2] :total 13} (:work counters))
            (= (:counters receipt) (:source-session counters))
            (exact-keys? (:process counters) child-process-counter-keys)
            (= {:child-count 1 :start-count 1 :exit-count 1
                :overlap-count 0}
               (:process counters))
            (exact-keys? (:ipc counters) ipc-counter-keys)
            (= {:request-count 1 :response-count 1
                :request-max-bytes 4194304
                :response-max-bytes 1073741824
                :stderr-max-bytes 1048576 :deadline-ms 3600000}
               (:ipc counters))
            (exact-keys? tcb tcb-keys)
            (= role (:role tcb))
            (= (:manifest-id tcb)
               ((:H codec)
                [:gravity/sh07-b51-checker-child-tcb-manifest-v1
                 (dissoc tcb :manifest-id)]))
            (empty? (:diagnostics response)))
        (reject! :child-response {:role role}))
      (assoc joined
             :response response
             :response-preimage-id response-id
             :response-byte-count (alength ^bytes (:stdout-bytes joined))
             :response-raw-sha256 ((:R codec) (:stdout-bytes joined))))))

(defn- append-event
  [events H event role coordinates]
  (let [ordinal (count events)
        [expected-event expected-role] (nth coordinator-event-plan ordinal nil)]
    (when-not (= [expected-event expected-role] [event role])
      (reject! :event-order
               {:ordinal ordinal
                :expected [expected-event expected-role]
                :actual [event role]}))
    (let [base
          (merge
           {:ordinal ordinal :event event :role role
            :process-handle-id nil :argv-id nil :request-id nil
            :response-preimage-id nil :source-session-id nil
            :prior-event-root (:event-id (peek events))}
           coordinates)
          row (assoc base :event-id
                     (H [:gravity/sh07-b51-coordinator-event-id-v1 base]))]
      (when-not (= event-keys (set (keys row)))
        (reject! :event-shape {:ordinal ordinal}))
      (conj events row))))

(defn coordinate!
  "Run the append-only exact A -> B -> M state machine.

  OPERATIONS is a closed process seam used by focused tests and production.
  Since `:build-child` for B is not called until A validation returns, B argv
  and its ProcessBuilder cannot exist before ordinal 4.  The M session is not
  invoked until B validation returns at ordinal 8."
  [operations H]
  (let [events0
        (append-event [] H :m-process-start :m
                      {:process-handle-id
                       (:m-process-handle-id operations)})
        a-built ((:build-child operations) :producer-census)
        events1 (append-event events0 H :a-builder :a
                              {:argv-id (:argv-id a-built)})
        a-started ((:start-child operations) a-built)
        events2 (append-event events1 H :a-start :a
                              {:process-handle-id
                               (get-in a-started
                                       [:process-start :process-handle-id])})
        a-joined ((:join-child operations) a-started)
        _ (when-not (zero? (:exit-code a-joined))
            (reject! :a-exit {:exit-code (:exit-code a-joined)}))
        events3 (append-event events2 H :a-exit-observed :a
                              {:process-handle-id
                               (get-in a-joined
                                       [:process-start :process-handle-id])})
        a-valid ((:validate-child operations) a-joined)
        events4
        (append-event events3 H :a-response-validated :a
                      {:request-id (:request-id a-valid)
                       :response-preimage-id
                       (:response-preimage-id a-valid)})
        ;; Nothing related to B is evaluated above this binding.
        b-built ((:build-child operations) :independent-verifier-census)
        events5 (append-event events4 H :b-builder :b
                              {:argv-id (:argv-id b-built)})
        b-started ((:start-child operations) b-built)
        events6 (append-event events5 H :b-start :b
                              {:process-handle-id
                               (get-in b-started
                                       [:process-start :process-handle-id])})
        b-joined ((:join-child operations) b-started)
        _ (when-not (zero? (:exit-code b-joined))
            (reject! :b-exit {:exit-code (:exit-code b-joined)}))
        events7 (append-event events6 H :b-exit-observed :b
                              {:process-handle-id
                               (get-in b-joined
                                       [:process-start :process-handle-id])})
        b-valid ((:validate-child operations) b-joined)
        events8
        (append-event events7 H :b-response-validated :b
                      {:request-id (:request-id b-valid)
                       :response-preimage-id
                       (:response-preimage-id b-valid)})
        ;; The synchronous arity-two wrapper opens and finalizes internally.
        ;; Its invocation still occurs strictly after ordinal-8 validation.
        m-session ((:m-session operations))
        session-id (:source-session-id m-session)
        events9 (append-event events8 H :m-session-start :m
                              {:source-session-id session-id})
        events10 (append-event events9 H :m-session-finalize :m
                               {:source-session-id session-id})]
    {:a a-valid
     :b b-valid
     :m-session m-session
     :events events10
     :event-root
     (H [:gravity/sh07-b51-coordinator-event-root-v1 events10])}))

(defn- source-session-id [H receipt]
  (H [:gravity/sh07-b51-governed-source-session-id-v1
      {:role (:role receipt)
       :commit (:commit receipt)
       :tree (:tree receipt)
       :source-descriptors-root (:source-descriptors-root receipt)
       :event-root (:event-root receipt)
       :counters (:counters receipt)}]))

(defn- run-m-session! [options codec]
  (when (some #(str/starts-with?
                (str %) "gravity.tools.sh07-b51-attempt30-")
              (loaded-libs))
    (reject! :m-checker-loaded))
  (require (symbol bootstrap-namespace))
  (let [builder
        (resolve-public!
         bootstrap-namespace
         "p15-s23-sh07-b51-build-receipted-bundle-core-products")
        content (:content options)
        products (builder (:commit content) (:tree content))
        receipt (:source-read-receipt products)]
    (when-not
     (and (exact-keys? products product-keys)
          (exact-keys? receipt source-receipt-keys)
          (= :m (:role receipt))
          (= content (select-keys receipt [:commit :tree]))
          (= {:acquire-count 12 :decode-count 12
              :event-count 39 :finalize-count 1}
             (:counters receipt))
          (true? (:finalized receipt)))
      (reject! :m-source-session))
    {:products products
     :source-read-receipt receipt
     :source-session-id (source-session-id (:H codec) receipt)}))

(defn- current-process-handle-id [options codec]
  (let [handle (ProcessHandle/current)
        info (.info handle)
        command (optional-value (.command info))
        arguments (some-> (optional-value (.arguments info)) vec)
        argv (when (and command arguments) (into [command] arguments))
        argv-id
        (when argv
          ((:H codec) [:gravity/sh07-b51-process-argv-id-v1 argv]))]
    (when-not argv-id (reject! :m-process))
    ((:H codec)
     [:gravity/sh07-b51-process-handle-coordinate-id-v1
      {:role :m :child-sequence :m
       :normalized-argv-id argv-id :request-id nil}])))

(defn run-process-topology!
  "Execute the production two-child topology using bounded asynchronous IPC."
  [options codec]
  (let [operations
        {:m-process-handle-id (current-process-handle-id options codec)
         :build-child #(build-child options codec %)
         :start-child #(start-child! % codec)
         :join-child #(join-child! % codec)
         :validate-child #(validate-child! % codec)
         :m-session #(run-m-session! options codec)}]
    (coordinate! operations (:H codec))))

(defn- payload-values [topology]
  (let [a-response (:response (:a topology))
        b-response (:response (:b topology))
        a-result (:result a-response)
        b-result (:result b-response)
        products (get-in topology [:m-session :products])
        pair [(:certificate (:checker-output a-result))
              (:certificate (:checker-output b-result))]
        pair-valid?
        (resolve-public!
         bootstrap-namespace
         "p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?")
        carrier-builder
        (resolve-public!
         bootstrap-namespace
         "p15-s23-sh07-b51-build-linked-carrier-v4-candidate")
        carrier-valid?
        (resolve-public!
         bootstrap-namespace
         "p15-s23-sh07-b51-linked-carrier-v4-valid?")
        _ (when-not (pair-valid? pair)
            (reject! :certificate-pair))
        carrier (carrier-builder products pair)
        _ (when-not (carrier-valid? carrier)
            (reject! :linked-carrier))]
    {:checker-a-identity.edn (:checker-identity a-result)
     :checker-b-identity.edn (:checker-identity b-result)
     :checker-a-output.edn (:checker-output a-result)
     :checker-b-output.edn (:checker-output b-result)
     :certificate-pair.edn pair
     :linked-carrier-v4-candidate.edn carrier}))

(defn- stage-payloads!
  [options codec topology]
  (let [root (Paths/get (:staging-root options) (make-array String 0))
        values (payload-values topology)]
    (when-not (and (Files/isDirectory root (make-array LinkOption 0))
                   (not (Files/isSymbolicLink root)))
      (reject! :staging-root {:path (str root)}))
    (mapv
     (fn [ordinal [name-key kind]]
       (let [name (name name-key)
             value (get values name-key)
             bytes ((:bounded-wire-bytes codec) :response value)
             path (.resolve root name)]
         (Files/write path bytes
                      (into-array StandardOpenOption
                                  [StandardOpenOption/CREATE_NEW
                                   StandardOpenOption/WRITE]))
         {:ordinal ordinal :name name :kind kind
          :relative-path (str (:evidence-relative-root options) "/" name)
          :wire-codec :gravity/sh07-b51-edn-wire-v1
          :byte-count (alength ^bytes bytes)
          :raw-sha256 ((:R codec) bytes)
          :value-id
          ((:H codec)
           [:gravity/sh07-b51-staged-payload-value-v1
            {:ordinal ordinal :name name :kind kind :value value}])}))
     (range)
     [[:checker-a-identity.edn :checker-identity]
      [:checker-b-identity.edn :checker-identity]
      [:checker-a-output.edn :checker-output]
      [:checker-b-output.edn :checker-output]
      [:certificate-pair.edn :certificate-pair]
      [:linked-carrier-v4-candidate.edn :linked-carrier]])))

(defn- child-process-receipt [codec child triple]
  (let [response (:response child)
        result (:result response)
        base
        {:schema :gravity/sh07-b51-child-process-receipt-v1
         :role (:role child)
         :ordinal-triple triple
         :argv (:argv child)
         :argv-id (:argv-id child)
         :request
         {:value (:request child)
          :request-id (:request-id child)
          :byte-count (alength ^bytes (:request-bytes child))
          :raw-sha256 ((:R codec) (:request-bytes child))}
         :response
         {:transport (:transport response)
          :reconstruction
          {:schema (:schema response) :role (:role response)
           :checker-identity-payload
           (get-in child-role-specs [(:role child) :identity-name])
           :checker-output-payload
           (get-in child-role-specs [(:role child) :output-name])
           :source-read-receipt (:source-read-receipt result)
           :production (:production result) :sentinel (:sentinel result)
           :diagnostics (:diagnostics response)}
          :byte-count (:response-byte-count child)
          :raw-sha256 (:response-raw-sha256 child)
          :response-preimage-id (:response-preimage-id child)}
         :process
         (assoc (:process-start child)
                :lifecycle
                {:domain :coordinator
                 :builder-ordinal (first triple)
                 :start-ordinal (second triple)
                 :exit-ordinal (nth triple 2)}
                :exit-code (:exit-code child))
         :process-join
         {:self-and-retained-pid-equal true
          :self-and-retained-parent-pid-equal true
          :self-and-retained-command-equal true
          :self-and-retained-arguments-equal true
          :self-and-retained-start-instant-equal true
          :mxbean-pid-equal true :mxbean-host-equal true}
         :counters (:counters response)
         :tcb (:tcb response)
         :self-id-omits [[:receipt-id]]}
        receipt-id
        ((:H codec)
         [:gravity/sh07-b51-child-process-receipt-v1 base])]
    (assoc base :receipt-id receipt-id)))

(defn- project-library? [namespace]
  (str/starts-with? (str namespace) "gravity."))

(defn- source-record-index [source-authority]
  (reduce
   (fn [result record]
     (if (or (str/ends-with? (:path record) ".clj")
             (str/ends-with? (:path record) ".cljc"))
       (if-let [namespace
                (some-> (source-namespace-core! (:bytes record) (:path record))
                        :namespace)]
         (do
           (when (contains? result namespace)
             (reject! :loaded-namespace-source-duplicate
                      {:namespace namespace}))
           (assoc result namespace record))
         result)
       result))
   {} (vals (:source-objects source-authority))))

(defn- loaded-project-source-records [options projects]
  (let [source-authority (:source-authority options)
        index (source-record-index source-authority)
        coordinate
        {:artifact 'project/bootstrap-clojure-src
         :commit (get-in options [:content :commit])
         :tree (get-in options [:content :tree])}]
    (mapv
     (fn [namespace]
       (let [record (get index namespace)]
         (when-not record
           (reject! :loaded-namespace-source-missing
                    {:namespace namespace}))
         {:namespace namespace :logical-coordinate coordinate
          :relative-path (:path record) :blob-oid (:blob-oid record)
          :byte-count (:byte-count record) :sha256 (:raw-sha256 record)}))
     projects)))

(defn- m-namespace-receipts [options codec topology]
  (let [libraries (loaded-libs)
        projects (->> libraries (filter project-library?) (sort-by str) vec)
        external (->> libraries (remove project-library?) (sort-by str) vec)
        forbidden
        (->> [(get-in topology
                      [:a :response :tcb :source-closure
                       :forbidden-namespaces 0])
              (get-in topology
                      [:b :response :tcb :source-closure
                       :forbidden-namespaces 0])]
             (filter symbol?) (sort-by str) vec)
        overlap (vec (filter (set forbidden) projects))
        records (loaded-project-source-records options projects)
        loaded-base
        {:role :m :project-namespaces projects
         :external-namespaces external :source-records records
         :forbidden-overlap overlap}
        _ (when (seq overlap)
            (reject! :m-checker-loaded {:overlap overlap}))
        loaded (assoc loaded-base :root
                      ((:H codec)
                       [:gravity/sh07-b51-loaded-namespace-root-v1
                        loaded-base]))
        source-base
        {:role :m
         :entrypoint 'gravity.tools.sh07-b51-attempt32-coordinator-child
         :static-project-namespaces []
         :dynamic-project-namespaces
         (vec (remove
               #{'gravity.tools.sh07-b51-attempt32-coordinator-child}
               projects))
         :source-records records :forbidden-namespaces forbidden}
        source (assoc source-base :root
                      ((:H codec)
                       [:gravity/sh07-b51-source-closure-root-v1
                        source-base]))]
    {:loaded loaded :source source}))

(defn build-m-response
  "Stage six semantic payloads and construct M's closed response."
  [options codec topology]
  (let [descriptors (stage-payloads! options codec topology)
        a-receipt (child-process-receipt codec (:a topology) [2 3 4])
        b-receipt (child-process-receipt codec (:b topology) [6 7 8])
        source-receipt (get-in topology [:m-session :source-read-receipt])
        namespaces (m-namespace-receipts options codec topology)
        coordinator-base
        {:schema :gravity/sh07-b51-coordinator-receipt-v1
         :process {:role :m :pid (.pid (ProcessHandle/current))}
         :runtime-mxbean {:name (.getName (ManagementFactory/getRuntimeMXBean))}
         :java {:canonical-path (first (child-argv options :producer-census))}
         :git {:canonical-path "/usr/bin/git"}
         :pre-manifest-tcb (:pre-manifest-tcb options)
         :eligibility-check (:eligibility-preflight options)
         :classpath-checks []
         :environment (:environment options)
         :source-closure (:source namespaces)
         :load-events []
         :loaded-namespace-receipt (:loaded namespaces)
         :host-surface {:policy
                        :gravity/sh07-b51-trusted-clojure-host-surface-v1}
         :source-read-receipt source-receipt
         :child-process-receipts [a-receipt b-receipt]
         :process-joins [(:process-join a-receipt)
                         (:process-join b-receipt)]
         :coordinator-events (:events topology)
         :coordinator-event-root (:event-root topology)
         :counters
         {:work {:vector [3 3 3 2 2] :total 13}
          :source-session (:counters source-receipt)
          :process {:builder-count 2 :child-count 2 :start-count 2
                    :exit-count 2 :overlap-count 0}
          :ipc {:request-count 2 :response-count 2
                :request-max-bytes 4194304
                :response-max-bytes 1073741824
                :stderr-max-bytes 1048576 :deadline-ms 3600000}}
         :a-validated-before-b-builder true
         :b-validated-before-m-session true
         :launcher-process-handle-id
         (current-process-handle-id options codec)
         :launcher-argv-id
         ((:H codec)
          [:gravity/sh07-b51-process-argv-id-v1 (:m-argv options)])
         :self-id-omits [[:receipt-id]]}
        coordinator-receipt
        (assoc coordinator-base :receipt-id
               ((:H codec)
                [:gravity/sh07-b51-coordinator-receipt-v1 coordinator-base]))
        base
        {:schema :gravity/sh07-b51-m-response-v1
         :status :accepted
         :content (:content options)
         :payload-descriptors descriptors
         :staging-receipt
         {:staging-path (:staging-root options)
          :evidence-relative-root (:evidence-relative-root options)
          :directory-mode "0700" :symlink-free true
          :payload-names (mapv :name descriptors)
          :payload-descriptors descriptors}
         :coordinator-receipt coordinator-receipt
         :source-read-receipt source-receipt
         :counters (:counters coordinator-receipt)
         :transport
         {:response-preimage-id nil
          :self-id-omits [[:transport :response-preimage-id]]}}
        response-id
        ((:H codec)
         [:gravity/sh07-b51-response-preimage-id-v1
          (update base :transport dissoc :response-preimage-id)])]
    (assoc-in base [:transport :response-preimage-id] response-id)))

(defn- actual-preflight-input [arguments]
  (let [handle (ProcessHandle/current)
        info (.info handle)
        command (optional-value (.command info))
        process-arguments (some-> (optional-value (.arguments info)) vec)]
    (when-not (and command process-arguments)
      (reject! :m-argv-self-report))
    {:arguments (vec arguments)
     :environment (into {} (System/getenv))
     :working-directory
     (str (.normalize (.toAbsolutePath
                       (Paths/get (System/getProperty "user.dir")
                                  (make-array String 0)))))
     :classpath (System/getProperty "java.class.path")
     :process-argv (into [command] process-arguments)}))

(defn -main [& arguments]
  (try
    (let [preflight
          (validate-entry-preflight! (actual-preflight-input arguments))
          pre-manifest
          (pre-manifest-tcb!
           preflight
           'gravity.tools.sh07-b51-attempt32-coordinator-child
           "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_coordinator_child.clj")
          codec (load-codec!)
          classpath (validate-complete-classpath! preflight codec)
          preflight (merge preflight classpath
                           {:pre-manifest-tcb pre-manifest})
          preflight (authenticate-eligibility-input! preflight codec)
          _ (when (seq (get-in preflight
                               [:eligibility-preflight :remaining-gaps]))
              (reject! :pre-discharge-reconstruction-unavailable))
          topology (run-process-topology! preflight codec)
          response (build-m-response preflight codec topology)
          bytes ((:bounded-wire-bytes codec) :response response)]
      (.write System/out bytes)
      (.flush System/out))
    (catch Throwable _
      ;; L owns the stable diagnostic.  Rejected M output is empty.
      (System/exit 2))))
