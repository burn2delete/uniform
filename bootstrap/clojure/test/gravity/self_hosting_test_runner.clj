(ns gravity.self-hosting-test-runner
  "Coordinator-owned test routing for the bootstrap and self-hosting suites.

  Dedicated self-hosting tests are discovered below gravity/self_hosting on
  the test classpath. Adding a leaf test therefore does not require an edit to
  gravity.bootstrap-test or to this runner."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as test]))

(import '(java.nio.charset CodingErrorAction StandardCharsets)
        '(java.nio.file Files LinkOption Path Paths StandardCopyOption
          StandardOpenOption))

(def ^:private base-test-namespaces
  '[gravity.diagnostics-test
    gravity.cli-test
    gravity.bootstrap-test
    gravity.p15-public-native-run-test
    gravity.p15-public-native-run-wrapper-test
    gravity.p15-native-launcher-test
    gravity.p15-native-runtime-driver-test
    gravity.p15-native-plan-specialization-test])

(def ^:private explicitly-selectable-test-namespaces
  '#{gravity.darwin-publication-test})

(def ^:private dedicated-test-resource
  "gravity/self_hosting")

(def ^:private batch-report-schema
  :gravity/self-hosting-test-report-v2)

(def ^:private batch-report-max-bytes
  ;; The parent runner bounds retained stdout/stderr independently.  The
  ;; machine report is deliberately smaller and is written through a separate
  ;; contained file so a noisy test cannot hide a final status marker.
  (* 256 1024))

(def ^:private namespace-output-limit-bytes
  8192)

(def ^:private namespace-error-limit-chars
  4096)

(defn- file-resource-directories
  []
  (let [class-loader (.getContextClassLoader (Thread/currentThread))]
    (->> (enumeration-seq
          (.getResources class-loader dedicated-test-resource))
         (mapv
          (fn [resource]
            (when-not (= "file" (.getProtocol resource))
              (throw
               (ex-info
                "Dedicated self-hosting tests require a filesystem test classpath"
                {:id "SH01-TEST-RESOURCE"
                 :resource dedicated-test-resource
                 :protocol (.getProtocol resource)})))
            (io/file (.toURI resource)))))))

(defn- clojure-test-file?
  [file]
  (and (.isFile file)
       (str/ends-with? (.getName file) "_test.clj")))

(defn- test-file->namespace
  [root file]
  (let [relative
        (str (.relativize (.toPath root) (.toPath file)))
        normalized
        (-> relative
            (str/replace java.io.File/separator "/")
            (str/replace #"\.clj$" "")
            (str/replace "_" "-")
            (str/replace "/" "."))]
    (symbol (str "gravity.self-hosting." normalized))))

(defn- dedicated-test-entry
  [root file]
  (let [relative
        (-> (str (.relativize (.toPath root) (.toPath file)))
            (str/replace java.io.File/separator "/"))]
    {:namespace (test-file->namespace root file)
     :relative-path relative}))

(defn dedicated-test-namespaces
  "Returns dedicated self-hosting test namespaces in deterministic order."
  []
  (let [entries
        (->> (file-resource-directories)
             (mapcat
              (fn [root]
                (->> (file-seq root)
                     (filter clojure-test-file?)
                     (map #(dedicated-test-entry root %)))))
             vec)
        collisions
        (->> entries
             (group-by :namespace)
             (keep
              (fn [[namespace matches]]
                (when (< 1 (count matches))
                  [namespace (mapv :relative-path matches)])))
             (into (sorted-map)))]
    (when (seq collisions)
      (throw
       (ex-info
        "Dedicated self-hosting test namespaces must map to one file"
        {:id "SH01-TEST-NAMESPACE-COLLISION"
         :collisions collisions})))
    (->> entries (map :namespace) sort vec)))

(defn test-namespaces
  "Returns the established bootstrap suite followed by dedicated leaf suites."
  []
  (into (vec base-test-namespaces) (dedicated-test-namespaces)))

(defn select-tests
  "Selects a deterministic test run or namespace listing from CLI arguments."
  [arguments]
  (let [arguments (vec arguments)
        dedicated (dedicated-test-namespaces)
        defaults (into (vec base-test-namespaces) dedicated)
        selectable
        (into (set defaults) explicitly-selectable-test-namespaces)]
    (loop [remaining arguments
           mode nil
           namespaces []
           fail-fast? false
           report-file nil]
      (if (empty? remaining)
        (let [default-run? (and (nil? mode)
                                (empty? namespaces)
                                (not fail-fast?)
                                (nil? report-file))
              mode (or mode (when (seq namespaces) :run)
                       (when default-run? :run))
              namespaces (if default-run?
                           defaults
                           (vec (sort namespaces)))]
          (when (and (= :run mode) (empty? namespaces))
            (throw
             (ex-info
              "A namespace selection is required"
              {:id "SH01-TEST-USAGE"
               :arguments arguments})))
          {:mode (or mode :run)
           :namespaces (if (= :run mode) namespaces
                           (vec (sort selectable)))
           :fail-fast? (boolean fail-fast?)
           :report-file report-file})
        (let [argument (first remaining)]
          (cond
            (= "--dedicated" argument)
            (if (or mode (seq namespaces) fail-fast? report-file
                    (not= 1 (count remaining)))
              (throw
               (ex-info
                "--dedicated cannot be combined with other selections"
                {:id "SH01-TEST-USAGE"
                 :arguments arguments}))
              {:mode :run
               :namespaces dedicated
               :fail-fast? false
               :report-file nil})

            (= "--list" argument)
            (if (or mode (seq namespaces) fail-fast? report-file
                    (not= 1 (count remaining)))
              (throw
               (ex-info
                "--list cannot be combined with other selections"
                {:id "SH01-TEST-USAGE"
                 :arguments arguments}))
              {:mode :list
               :namespaces (vec (sort selectable))
               :fail-fast? false
               :report-file nil})

            (= "--fail-fast" argument)
            (if (= :list mode)
              (throw
               (ex-info
                "--fail-fast cannot be used with --list"
                {:id "SH01-TEST-USAGE"
                 :arguments arguments}))
              (recur (subvec remaining 1)
                     (or mode :run)
                     namespaces
                     true
                     report-file))

            (= "--report-file" argument)
            (let [value (second remaining)]
              (when (or (nil? value) (str/starts-with? value "--"))
                (throw
                 (ex-info
                  "--report-file requires a path"
                  {:id "SH01-TEST-USAGE"
                   :arguments arguments})))
              (when (and report-file (not= report-file value))
                (throw
                 (ex-info
                  "--report-file may be supplied only once"
                  {:id "SH01-TEST-USAGE"
                   :arguments arguments})))
              (recur (subvec remaining 2)
                     (or mode :run)
                     namespaces
                     fail-fast?
                     value))

            (= "--namespace" argument)
            (let [value (second remaining)]
              (when (or (nil? value) (str/starts-with? value "--"))
                (throw
                 (ex-info
                  "--namespace requires an owned test namespace"
                  {:id "SH01-TEST-USAGE"
                   :arguments arguments})))
              (let [namespace (symbol value)]
                (when-not (contains? selectable namespace)
                  (throw
                   (ex-info
                    "Requested test namespace is not owned by this runner"
                    {:id "SH01-TEST-NAMESPACE"
                     :namespace namespace
                     :selectable (vec (sort selectable))})))
                (when (some #{namespace} namespaces)
                  (throw
                   (ex-info
                    "Requested test namespaces must be unique"
                    {:id "SH01-TEST-NAMESPACE-DUPLICATE"
                     :namespace namespace
                     :namespaces (conj namespaces namespace)})))
                (recur (subvec remaining 2)
                       (or mode :run)
                       (conj namespaces namespace)
                       fail-fast?
                       report-file)))

            :else
            (throw
             (ex-info
              "Unsupported self-hosting test runner arguments"
              {:id "SH01-TEST-USAGE"
               :arguments arguments
               :argument argument
               :supported
               [[] ["--dedicated"] ["--list"]
                ["--namespace" "<owned-test-namespace>"]
                ["--namespace" "<owned-test-namespace>" "--fail-fast"]
                ["--report-file" "<contained-path>"]]}))))))))

(defn- throwable-result
  [namespace throwable elapsed-ms]
  {:namespace namespace
   :status :error
   :exit-code 1
   :attempted? true
   :elapsed-ms (long elapsed-ms)
   :error-class (str (class throwable))
   :error-message (let [message (or (.getMessage ^Throwable throwable)
                                    (str throwable))]
                    (subs message 0 (min namespace-error-limit-chars
                                          (count message))))
   :summary {:test 0 :pass 0 :fail 0 :error 1}
   :test 0
   :pass 0
   :fail 0
   :error 1})

(defn- fatal-throwable?
  [throwable]
  (or (instance? ThreadDeath throwable)
      (instance? VirtualMachineError throwable)
      (instance? LinkageError throwable)))

(defn- restore-interrupt!
  [throwable]
  (when (instance? InterruptedException throwable)
    (.interrupt (Thread/currentThread))))

(defn- bounded-output-stream
  [limit]
  (let [retained (java.io.ByteArrayOutputStream.)
        observed (atom 0)
        stream
        (proxy [java.io.OutputStream] []
          (write
            ([value]
             (let [byte-value (bit-and (int value) 0xff)
                   current (long @observed)]
               (swap! observed inc)
               (when (< current limit)
                 (.write retained byte-value))))
            ([bytes offset length]
             (let [offset (int offset)
                   length (int length)
                   current (long @observed)
                   remaining (max 0 (- (long limit) current))
                   kept (int (min remaining length))]
               (swap! observed + length)
               (when (pos? kept)
                 (.write retained bytes offset kept)))))
          (flush [] nil)
          (close [] nil))]
    {:stream stream
     :observed observed
     :retained retained
     :limit limit}))

(defn- strict-utf8-prefix
  "Return the longest valid UTF-8 prefix of retained bytes.

  The wire cap may split a code point.  Reports must not contain a replacement
  character that was manufactured by String's lenient decoder, so trim at
  most the three continuation bytes which can trail a UTF-8 sequence."
  [^bytes bytes]
  (loop [length (alength bytes)]
    (let [valid-length
          (try
            (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                            (.onMalformedInput CodingErrorAction/REPORT)
                            (.onUnmappableCharacter CodingErrorAction/REPORT))]
              (str (.decode decoder
                            (java.nio.ByteBuffer/wrap bytes 0 length)))
              length)
            (catch java.nio.charset.CharacterCodingException _ nil))]
      (if (some? valid-length)
        valid-length
        (if (and (pos? length) (<= (- (alength bytes) length) 3))
          (recur (dec length))
          0)))))

(defn- bounded-output-result
  [{:keys [^java.io.ByteArrayOutputStream retained observed limit]}]
  (let [bytes (.toByteArray retained)
        safe-length (strict-utf8-prefix bytes)
        text (String. bytes 0 safe-length StandardCharsets/UTF_8)]
    {:text text
     :bytes safe-length
     :observed-bytes (long @observed)
     :truncated? (> (long @observed) (long limit))
     :limit-bytes limit}))

(defn- emit-visible-output!
  [result]
  ;; Preserve the historical single-namespace CLI behaviour when no machine
  ;; report was requested.  Batch invocations set :visible? false so the
  ;; parent receives bounded per-namespace output only through result.edn.
  (when-let [text (get-in result [:stdout :text])]
    (print text))
  (when-let [text (get-in result [:stderr :text])]
    (binding [*out* *err*]
      (print text)))
  (flush)
  (.flush *err*))

(defn- run-one-namespace
  ([namespace]
   (run-one-namespace namespace require test/run-tests))
  ([namespace require-fn run-tests-fn]
  (let [started (System/nanoTime)
        stdout (bounded-output-stream namespace-output-limit-bytes)
        stderr (bounded-output-stream namespace-output-limit-bytes)
        out-writer (java.io.PrintWriter.
                    (java.io.OutputStreamWriter.
                     ^java.io.OutputStream (:stream stdout)
                     StandardCharsets/UTF_8))
        err-writer (java.io.PrintWriter.
                    (java.io.OutputStreamWriter.
                     ^java.io.OutputStream (:stream stderr)
                     StandardCharsets/UTF_8))
        run-result (atom nil)]
    (try
      (binding [*out* out-writer
                *err* err-writer
                test/*test-out* out-writer]
        ;; Require immediately before the namespace run.  A fail-fast batch
        ;; must not eagerly load the tail, and clojure.test remains responsible
        ;; for once/each fixtures and test-ns-hook semantics.
        (require-fn namespace)
        (let [summary (run-tests-fn namespace)
              passed? (and (zero? (long (or (:fail summary) 0)))
                           (zero? (long (or (:error summary) 0))))
              elapsed-ms (long (/ (- (System/nanoTime) started) 1000000.0))]
          (reset!
           run-result
           {:namespace namespace
            :status (if passed? :passed :failed)
            :exit-code (if passed? 0 1)
            :attempted? true
            :elapsed-ms elapsed-ms
            :summary (select-keys summary [:test :pass :fail :error])
            :test (long (or (:test summary) 0))
            :pass (long (or (:pass summary) 0))
            :fail (long (or (:fail summary) 0))
            :error (long (or (:error summary) 0))})))
      (catch Throwable throwable
        (when (instance? InterruptedException throwable)
          (restore-interrupt! throwable)
          (throw throwable))
        (when (fatal-throwable? throwable)
          (throw throwable))
        (reset!
         run-result
         (throwable-result
          namespace
          throwable
          (long (/ (- (System/nanoTime) started) 1000000.0)))))
      (finally
        (.flush out-writer)
        (.flush err-writer)))
    (merge @run-result
           {:stdout (bounded-output-result stdout)
            :stderr (bounded-output-result stderr)}))))

(defn run-namespaces
  "Runs owned namespaces sequentially in one JVM.

  The returned map is a development receipt only.  A namespace-level failure
  is retained in `:namespace-results`; with `fail-fast?`, the remaining names
  are reported explicitly under `:skipped-namespaces` and are never inferred
  to have passed."
  [{:keys [namespaces fail-fast? require-fn run-tests-fn visible?]}]
  (let [namespaces (vec (sort namespaces))
        require-fn (or require-fn require)
        run-tests-fn (or run-tests-fn test/run-tests)]
    (loop [remaining namespaces
           results []
           summaries []]
      (if-let [namespace (first remaining)]
        (let [result (run-one-namespace namespace require-fn run-tests-fn)
              results (conj results result)
              summaries (conj summaries (:summary result))
              failed? (not= :passed (:status result))
              tail (subvec remaining 1)]
          (when visible?
            (emit-visible-output! result))
          (if (and fail-fast? failed? (seq tail))
            (let [summary (apply merge-with + summaries)]
              {:schema batch-report-schema
               :authority :non-authoritative
               :authoritative? false
               :status :failed
               :exit-code 1
               :namespaces namespaces
               :namespace-results results
               :skipped-namespaces (vec tail)
               :fail-fast? true
               :summary (select-keys summary [:test :pass :fail :error])
               :test (long (or (:test summary) 0))
               :pass (long (or (:pass summary) 0))
               :fail (long (or (:fail summary) 0))
               :error (long (or (:error summary) 0))})
            (if (seq tail)
              (recur tail results summaries)
              (let [summary (apply merge-with + summaries)
                    passed? (every? #(= :passed (:status %)) results)]
                {:schema batch-report-schema
                 :authority :non-authoritative
                 :authoritative? false
                 :status (if passed? :passed :failed)
                 :exit-code (if passed? 0 1)
                 :namespaces namespaces
                 :namespace-results results
                 :skipped-namespaces []
                 :fail-fast? (boolean fail-fast?)
                 :summary (select-keys summary [:test :pass :fail :error])
                 :test (long (or (:test summary) 0))
                 :pass (long (or (:pass summary) 0))
                 :fail (long (or (:fail summary) 0))
                 :error (long (or (:error summary) 0))}))))
        (throw
         (ex-info "At least one namespace is required"
                  {:id "SH01-TEST-EMPTY-SELECTION"}))))))

(defn- strict-report-bytes
  [^String text]
  (.getBytes text StandardCharsets/UTF_8))

(defn- report-counter?
  [value]
  (and (integer? value) (<= 0 (long value))))

(defn- valid-report-output?
  [output]
  (and (map? output)
       (string? (:text output))
       (report-counter? (:bytes output))
       (report-counter? (:observed-bytes output))
       (report-counter? (:limit-bytes output))
       (<= (long (:bytes output)) (long (:observed-bytes output)))
       (<= (long (:bytes output)) (long (:limit-bytes output)))
       (boolean? (:truncated? output))
       (= (alength (.getBytes ^String (:text output) StandardCharsets/UTF_8))
          (long (:bytes output)))))

(defn- valid-report?
  [report]
  (let [namespaces (vec (:namespaces report))
        attempted (vec (:namespace-results report))
        attempted-names (mapv :namespace attempted)
        skipped (vec (:skipped-namespaces report))
        attempted-count (count attempted)
        expected-prefix (subvec namespaces 0 (min attempted-count (count namespaces)))
        expected-tail (subvec namespaces (min attempted-count (count namespaces)))
        summary (:summary report)
        member-valid?
        (fn [member]
          (and (map? member)
               (true? (:attempted? member))
               (contains? #{:passed :failed :error} (:status member))
               (report-counter? (:exit-code member))
               (report-counter? (:elapsed-ms member))
               (= (:exit-code member) (if (= :passed (:status member)) 0 1))
               (report-counter? (:test member))
               (report-counter? (:pass member))
               (report-counter? (:fail member))
               (report-counter? (:error member))
               (valid-report-output? (:stdout member))
               (valid-report-output? (:stderr member))))]
    (and (map? report)
         (= batch-report-schema (:schema report))
         (= :non-authoritative (:authority report))
         (false? (:authoritative? report))
         (seq namespaces)
         (= namespaces (vec (sort namespaces)))
         (= namespaces (vec (distinct namespaces)))
         (= expected-prefix attempted-names)
         (= expected-tail skipped)
         (every? symbol? namespaces)
         (every? member-valid? attempted)
         (boolean? (:fail-fast? report))
         (or (not (:fail-fast? report))
             (= (seq skipped) (seq expected-tail)))
         (every? report-counter?
                 [(:test report) (:pass report) (:fail report) (:error report)
                  (get summary :test) (get summary :pass)
                  (get summary :fail) (get summary :error)])
         (= (:test report) (get summary :test))
         (= (:pass report) (get summary :pass))
         (= (:fail report) (get summary :fail))
         (= (:error report) (get summary :error))
         (= (:status report)
            (if (and (empty? skipped)
                     (every? #(= :passed (:status %)) attempted))
              :passed
              :failed))
         (= (:exit-code report) (if (= :passed (:status report)) 0 1)))))

(defn- report-path-contained?
  [^Path report-path]
  (try
    (let [working (.toRealPath
                   (.toAbsolutePath (Paths/get (System/getProperty "user.dir")
                                                (make-array String 0)))
                   (make-array LinkOption 0))
          parent (.getParent (.toAbsolutePath report-path))
          parent-real (.toRealPath parent (make-array LinkOption 0))]
      (and (.startsWith parent-real working)
           (= "result.edn" (str (.getFileName report-path)))))
    (catch Throwable _ false)))

(defn- write-report!
  [report-file report]
  (let [report-path (.toAbsolutePath (.normalize (Paths/get report-file
                                                           (make-array String 0))))
        parent (.getParent report-path)]
    (when-not (and parent (report-path-contained? report-path))
      (throw
       (ex-info
        "Batch report path must be a result.edn file contained by the working directory"
        {:id "SH01-TEST-REPORT-PATH"
         :path (str report-path)})))
    (let [payload (pr-str report)
          bytes (strict-report-bytes payload)]
      (when-not (valid-report? report)
        (throw
         (ex-info
          "Batch report failed its exact schema before publication"
          {:id "SH01-TEST-REPORT-SCHEMA"})))
      (when (> (alength bytes) batch-report-max-bytes)
        (throw
         (ex-info
          "Batch report exceeds its bounded size"
          {:id "SH01-TEST-REPORT-LIMIT"
           :bytes (alength bytes)
           :limit batch-report-max-bytes})))
      (let [temporary
            (Files/createTempFile
             parent
             ".gravity-sh01-report-"
             ".tmp"
             (make-array java.nio.file.attribute.FileAttribute 0))]
        (try
          (Files/write temporary bytes (into-array StandardOpenOption
                                                  [StandardOpenOption/WRITE
                                                   StandardOpenOption/TRUNCATE_EXISTING]))
          (Files/move temporary report-path
                       (into-array StandardCopyOption
                                   [StandardCopyOption/ATOMIC_MOVE
                                    StandardCopyOption/REPLACE_EXISTING]))
          (finally
            (Files/deleteIfExists temporary)))))))

(defn- print-run-summary
  [report]
  (println
   (str "Clojure validation "
        (if (= :passed (:status report)) "passed" "failed")
        ": " (get-in report [:summary :test] 0) " tests, "
        (get-in report [:summary :pass] 0) " assertions, "
        (count (:namespace-results report)) " attempted namespaces, "
        (count (:skipped-namespaces report)) " skipped namespaces")))

(defn -main
  [& arguments]
  (let [result
        (try
          (let [{:keys [mode namespaces fail-fast? report-file] :as selection}
                (select-tests arguments)]
            (case mode
              :list
              (doseq [namespace namespaces]
                (println namespace))

              :run
              (let [report (run-namespaces {:namespaces namespaces
                                            :fail-fast? fail-fast?
                                            :visible? (nil? report-file)})]
                (when report-file
                  (write-report! report-file report))
                (print-run-summary report)
                report)))
          (finally
            (flush)
            (.flush *err*)
            (.flush System/out)
            (.flush System/err)
            (shutdown-agents)))]
    (when (and (map? result)
               (pos? (long (or (:exit-code result) 0))))
      (System/exit (:exit-code result)))
    result))
