(ns gravity.p15-native-plan-specialization-test
  "Focused evidence for authenticated plan-specialized C artifacts.

  The namespace consumes real stage2 runtime packets and executes a
  Gravity-authored C-emitter helper through the bounded Clojure rule-runner
  seam. Tests compile/run the returned C source only inside a test-owned
  private root; production does not expose a second process runner.
  "
  (:require [clojure.test :refer [deftest is testing run-tests]]
            [gravity.bootstrap :as bootstrap]
            [gravity.p15-native-plan-specialization :as specialization])
  (:import [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file Files LinkOption OpenOption Path Paths]
           [java.nio.file.attribute FileAttribute PosixFilePermissions]
           [java.util.concurrent TimeUnit]))

(def ^:private compiler "/usr/bin/cc")
(def ^:private fixture-root-relative
  "bootstrap/clojure/fixtures/p15-native-plan-specialization")
(def ^:private process-timeout-ms 10000)
(def ^:private minimal-environment
  {"PATH" "/nonexistent"
   "LANG" "C"
   "LC_ALL" "C"})
(def ^:private no-follow-options
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn- repository-root
  []
  (let [resource
        (clojure.java.io/resource
         "gravity/p15_native_plan_specialization_test.clj")]
    (when-not resource
      (throw (ex-info "plan specialization test source is not on the classpath"
                      {:id "P15NS-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath
                                  (clojure.java.io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "plan specialization repository root is unavailable"
                        {:id "P15NS-TEST-ROOT"}))
        (Files/isRegularFile (.resolve candidate "deps.edn")
                             (make-array LinkOption 0))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (.resolve ^Path @root relative))

(defn- arm64-darwin-toolchain-available?
  []
  (and (= "Mac OS X" (System/getProperty "os.name"))
       (contains? #{"aarch64" "arm64"} (System/getProperty "os.arch"))
       (Files/isExecutable (Paths/get compiler (make-array String 0)))))

(defn- owner-only-attributes
  []
  (into-array FileAttribute
              [(PosixFilePermissions/asFileAttribute
                (PosixFilePermissions/fromString "rwx------"))]))

(defn- delete-tree!
  [^Path directory]
  (when (Files/exists directory no-follow-options)
    (let [entries (with-open [stream (Files/walk
                                      directory
                                      (make-array java.nio.file.FileVisitOption 0))]
                    (vec (iterator-seq (.iterator stream))))]
      (doseq [entry (reverse entries)]
        (Files/deleteIfExists ^Path entry)))))

(defn- with-private-root
  [f]
  (let [directory (Files/createTempDirectory "gravity-p15-native-plan-"
                                             (owner-only-attributes))]
    (try
      (f directory)
      (finally
        (delete-tree! directory)))))

(defn- run-process!
  "Test-only process helper.  It is intentionally not production evidence:
  timeout cleanup is leader-only and path-based; no process-tree or descriptor
  containment claim is made here."
  [^Path directory command]
  (let [stdout (.resolve directory "stdout")
        stderr (.resolve directory "stderr")
        builder (doto (ProcessBuilder. ^java.util.List (vec command))
                  (.directory (.toFile directory))
                  (.redirectOutput (.toFile stdout))
                  (.redirectError (.toFile stderr)))
        environment (.environment builder)]
    (.clear environment)
    (doseq [[key value] minimal-environment]
      (.put environment key value))
    (let [process (.start builder)
          completed? (.waitFor process process-timeout-ms
                               TimeUnit/MILLISECONDS)]
      (when-not completed?
        (.destroyForcibly process)
        (.waitFor process process-timeout-ms TimeUnit/MILLISECONDS))
      {:command (vec command)
       :environment (into {} environment)
       :completed? completed?
       :exit (when completed? (.exitValue process))
       :out (String. (Files/readAllBytes stdout) StandardCharsets/UTF_8)
       :err (String. (Files/readAllBytes stderr) StandardCharsets/UTF_8)})))

(defn- fixture-relative
  [filename]
  (str fixture-root-relative "/" filename))

(def ^:private fixed-real-packet-paths
  (set (map fixture-relative
            ["accepted-print.gravity"
             "accepted-print.qst"
             "accepted-str.gravity"
             "unsupported-builtin.gravity"
             "accepted-bool.gravity"
             "accepted-nonascii.gravity"
             "accepted-control.gravity"
             "accepted-trigraph.gravity"])))

(def ^:private reused-real-packet-paths
  #{(fixture-relative "accepted-print.gravity")})

(defn- source-text
  [relative]
  (let [bytes (Files/readAllBytes (path relative))
        decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (str (.decode decoder (ByteBuffer/wrap bytes)))))

(def ^:dynamic ^:private *real-packet-cache* (atom {}))
(def ^:dynamic ^:private *real-packet-derivation-counts* (atom {}))
(def ^:dynamic ^:private *real-packet-source-loader* source-text)
(def ^:dynamic ^:private *real-packet-deriver*
  (fn [relative text]
    {:packet (bootstrap/stage2-runtime-derived-packet relative text :c)
     :context (bootstrap/p15-s23-closed-runtime-packet-context
               relative text :c)}))

(declare diagnostic-id)

(defn- derive-real-packet!
  [relative text]
  (let [value (*real-packet-deriver* relative text)]
    (when-not (and (instance? clojure.lang.IPersistentMap value)
                   (= #{:packet :context} (set (keys value)))
                   (instance? clojure.lang.IPersistentMap (:packet value))
                   (instance? clojure.lang.IPersistentMap (:context value)))
      (throw (ex-info "real packet cache accepts only exact persistent products"
                      {:id "P15NS-TEST-CACHE-PRODUCT"
                       :source-path relative})))
    (swap! *real-packet-derivation-counts* update relative (fnil inc 0))
    value))

(defn- real-packet
  [relative]
  (when-not (contains? fixed-real-packet-paths relative)
    (throw (ex-info "real packet cache source is not allowlisted"
                    {:id "P15NS-TEST-CACHE-SOURCE"
                     :source-path relative})))
  ;; Read on every call.  The process-local cache removes repeated compiler
  ;; work, but a source mutation must never be hidden from the outer input
  ;; watcher or from a later call in this JVM.
  (if (contains? reused-real-packet-paths relative)
    (locking *real-packet-cache*
      (let [text (*real-packet-source-loader* relative)]
        (if-let [entry (get @*real-packet-cache* relative)]
          (if (= text (:source-text entry))
            (:value entry)
            (let [value (derive-real-packet! relative text)]
              (swap! *real-packet-cache* assoc relative
                     {:source-text text :value value})
              value))
          (let [value (derive-real-packet! relative text)]
            (swap! *real-packet-cache* assoc relative
                   {:source-text text :value value})
            value))))
    (derive-real-packet! relative
                         (*real-packet-source-loader* relative))))

(defn- assert-real-packet-cache-contract!
  []
  (let [relative (fixture-relative "accepted-print.gravity")
        cache (atom {})
        source (atom "source-a")
        reads (atom 0)
        derivations (atom 0)
        derive (fn [path text]
                 (swap! derivations inc)
                 {:packet {:path path :text text}
                  :context {:path path :text text}})]
    (binding [*real-packet-cache* cache
              *real-packet-derivation-counts* (atom {})
              *real-packet-source-loader*
              (fn [_]
                (swap! reads inc)
                @source)
              *real-packet-deriver* derive]
      (let [first-value (real-packet relative)
            repeated-value (real-packet relative)]
        (is (= 2 @reads) "every access rechecks the source snapshot")
        (is (= 1 @derivations) "an unchanged fixed source derives once")
        (is (identical? first-value repeated-value)
            "an unchanged immutable packet/context product is reused")
        (is (= 1 (count @cache)) "the fixed cache remains path bounded")
        (reset! source "source-b")
        (let [changed-value (real-packet relative)]
          (is (= 3 @reads) "a changed source is observed")
          (is (= 2 @derivations) "a changed source is rederived")
          (is (not (identical? first-value changed-value))
              "a changed source never reuses the prior product")
          (is (= "source-b" (get-in changed-value [:packet :text]))))
        (real-packet (fixture-relative "accepted-print.qst"))
        (real-packet (fixture-relative "accepted-print.qst"))
        (is (= 4 @derivations)
            "single-use fixtures derive normally instead of accumulating")
        (is (= 1 (count @cache))
            "only the one repeated fixture is retained")
        (is (= "P15NS-TEST-CACHE-SOURCE"
               (diagnostic-id
                #(real-packet "bootstrap/clojure/fixtures/not-reviewed.gravity"))))
        (is (= 5 @reads) "an unreviewed path rejects before source I/O")))
    (let [attempts (atom 0)]
      (binding [*real-packet-cache* (atom {})
                *real-packet-derivation-counts* (atom {})
                *real-packet-source-loader* (constantly "source")
                *real-packet-deriver*
                (fn [path text]
                  (if (= 1 (swap! attempts inc))
                    (throw (ex-info "synthetic derivation failure"
                                    {:id "P15NS-TEST-CACHE-DERIVE"}))
                    (derive path text)))]
        (is (= "P15NS-TEST-CACHE-DERIVE"
               (diagnostic-id #(real-packet relative))))
        (is (= {:path relative :text "source"}
               (:packet (real-packet relative))))
        (is (= 2 @attempts) "failed derivations are never cached")))
    (let [cache (atom {})]
      (binding [*real-packet-cache* cache
                *real-packet-derivation-counts* (atom {})
                *real-packet-source-loader* (constantly "source")
                *real-packet-deriver* (fn [_ _] [:mutable-or-wrong-shape])]
        (is (= "P15NS-TEST-CACHE-PRODUCT"
               (diagnostic-id #(real-packet relative))))
        (is (empty? @cache) "nonpersistent or malformed products are not cached")))))

(defn- diagnostic-id
  [f]
  (try
    (f)
    nil
    (catch clojure.lang.ExceptionInfo error
      (:id (ex-data error)))))

(defn- compile-and-run!
  [^Path directory generated-source]
  (let [source-path (.resolve directory "generated.c")
        executable-path (.resolve directory "generated")]
    (Files/write source-path
                 (.getBytes ^String generated-source StandardCharsets/UTF_8)
                 (make-array OpenOption 0))
    (let [compiled (run-process!
                    directory
                    [compiler "-std=c11" "-Wall" "-Wextra" "-Werror"
                     (str source-path) "-o" (str executable-path)])]
      (if (and (:completed? compiled) (zero? (:exit compiled))
               (Files/isExecutable executable-path))
        {:compile compiled
         :runtime (run-process! directory [(str executable-path)])}
        {:compile compiled}))))

(deftest fixed-real-packet-cache-is-bounded-and-source-coherent
  (assert-real-packet-cache-contract!))

(deftest authenticated-gravity-and-qst-packets-emit-and-run
  (if-not (arm64-darwin-toolchain-available?)
    (is true "no native claim: ARM64 macOS /usr/bin/cc is unavailable")
    (with-private-root
      (fn [directory]
        (doseq [[filename expected]
                [["accepted-print.gravity" "Hello Gravity\n"]
                 ["accepted-print.qst" "Hello Gravity\n"]
                 ["accepted-str.gravity" "name42\n"]]]
          (testing filename
            (let [relative (fixture-relative filename)
                  {:keys [packet context]} (real-packet relative)
                  artifact
                  (specialization/specialize-native-runtime-plan
                   packet context)
                  execution (compile-and-run!
                             directory
                             (get-in artifact [:generated-c :source]))]
              (is (= 1 (get @*real-packet-derivation-counts* relative))
                  "each fixed fixture derives at most once per JVM")
              (is (= :complete-for-internal-plan-specialized-native-child
                     (:status artifact)) artifact)
              (is (= :authenticated
                     (get-in artifact [:authentication :status])) artifact)
              (is (= :not-exposed (get-in artifact [:runner :status])) artifact)
              (is (= :not-run
                     (get-in artifact [:generated-c :execution])) artifact)
              (is (= :external-focused-test-only
                     (get-in artifact [:generated-c :execution-evidence]))
                  artifact)
              (is (= :gravity-source-emitted-plan-specialized-c
                     (get-in artifact [:generated-c :implementation]))
                  artifact)
              (is (= :gravity-source
                     (get-in artifact [:emitter :semantic-owner])) artifact)
              (is (= :gravity
                     (get-in artifact [:emitter :source-language])) artifact)
              (is (= 'p15-s23-native-c-emit-plan
                     (get-in artifact [:emitter :helper-function])) artifact)
              (is (re-matches
                   #"sha256:[0-9a-f]{64}"
                   (get-in artifact [:emitter
                                     :helper-source-content-hash])) artifact)
              (is (re-matches
                   #"sha256:[0-9a-f]{64}"
                   (get-in artifact [:emitter
                                     :helper-function-semantic-hash])) artifact)
              (is (re-matches
                   #"sha256:[0-9a-f]{64}"
                   (get-in artifact [:emitter :helper-contract-hash])) artifact)
              (is (true? (get-in artifact
                                  [:provenance :c-emitter-helper-executed?]))
                  artifact)
              (is (true? (get-in artifact
                                  [:provenance
                                   :c-emitter-pr-str-primitive-boundary?]))
                  artifact)
              (is (= :gravity
                     (get-in artifact [:provenance :c-emitter-source-language]))
                  artifact)
              (is (false? (get-in artifact
                                  [:provenance
                                   :generic-host-c-packet-interpreter-used?]))
                  artifact)
              (is (not (re-find #"(?i)clojure|java"
                                (get-in artifact [:generated-c :source])))
                  artifact)
              (is (= expected (:expected-output artifact)) artifact)
              (is (= 0 (get-in execution [:compile :exit])) execution)
              (is (= 0 (get-in execution [:runtime :exit])) execution)
              (is (= expected (get-in execution [:runtime :out])) execution)
              (is (= "" (get-in execution [:runtime :err])) execution))))))))

(deftest packet-and-context-tamper-reject-before-validator-or-emitter
  (let [relative (fixture-relative "accepted-print.gravity")
        {:keys [packet context]} (real-packet relative)
        validator (var-get #'bootstrap/c-backend-validate-runtime-plan!)
        validator-calls (atom 0)
        helper-loader (var-get
                       #'specialization/*p15-native-plan-c-emitter-source-loader*)
        helper-loader-calls (atom 0)
        changed-source (str (:source-text context) "\n")
        changed-context
        (bootstrap/p15-s23-closed-runtime-packet-context
         (:source-path context) changed-source :c)
        cases [[(assoc packet :status :tampered) context]
               [packet changed-context]
               [(assoc-in packet [:plan :entrypoint] 'tampered) context]]]
      (is (= 1 (get @*real-packet-derivation-counts* relative))
          "the first accepted-print access derives exactly once")
      (with-redefs [bootstrap/c-backend-validate-runtime-plan!
                    (fn [& args]
                      (swap! validator-calls inc)
                      (apply validator args))
                  specialization/*p15-native-plan-c-emitter-source-loader*
                  (fn [& args]
                    (swap! helper-loader-calls inc)
                    (apply helper-loader args))]
      (doseq [[candidate candidate-context] cases]
        (is (= "P15NS001"
               (diagnostic-id
                #(specialization/specialize-native-runtime-plan
                  candidate candidate-context)))))
      (is (zero? @validator-calls))
      (is (zero? @helper-loader-calls)))))

(deftest authenticated-unsupported-plan-rejects-before-emitter
  (let [{:keys [packet context]}
        (real-packet (fixture-relative "unsupported-builtin.gravity"))
        helper-loader (var-get
                       #'specialization/*p15-native-plan-c-emitter-source-loader*)
        helper-loader-calls (atom 0)]
    (with-redefs [specialization/*p15-native-plan-c-emitter-source-loader*
                  (fn [& args]
                    (swap! helper-loader-calls inc)
                    (apply helper-loader args))]
      (is (= "P15NS002"
             (diagnostic-id
              #(specialization/specialize-native-runtime-plan
                packet context))))
      (is (zero? @helper-loader-calls)))))

(deftest overbound-packet-tamper-rejects-before-validator
  (let [relative (fixture-relative "accepted-print.gravity")
        {:keys [packet context]} (real-packet relative)
        entrypoint (:entrypoint (:plan packet))
        instructions (get-in packet [:plan :functions entrypoint :instructions])
        overbound-packet
        (assoc-in packet [:plan :functions entrypoint :instructions]
                  (vec (concat instructions (repeat 128 (first instructions)))))
        validator (var-get #'bootstrap/c-backend-validate-runtime-plan!)
        validator-calls (atom 0)
        helper-loader (var-get
                       #'specialization/*p15-native-plan-c-emitter-source-loader*)
        helper-loader-calls (atom 0)]
    (is (= 1 (get @*real-packet-derivation-counts* relative))
        "the repeated accepted-print access reuses the first derivation")
    (with-redefs [bootstrap/c-backend-validate-runtime-plan!
                  (fn [& args]
                    (swap! validator-calls inc)
                    (apply validator args))
                  specialization/*p15-native-plan-c-emitter-source-loader*
                  (fn [& args]
                    (swap! helper-loader-calls inc)
                    (apply helper-loader args))]
      (is (= "P15NS001"
             (diagnostic-id
              #(specialization/specialize-native-runtime-plan
                overbound-packet context))))
      (is (zero? @validator-calls))
      (is (zero? @helper-loader-calls)))))

(deftest authenticated-validator-accepted-unsupported-helper-values-reject
  (doseq [[filename expected-fact]
          [["accepted-bool.gravity" :gravity-c-emitter-printable-ascii-subset]
           ["accepted-nonascii.gravity"
            :gravity-c-emitter-printable-ascii-subset]
           ["accepted-control.gravity"
            :gravity-c-emitter-printable-ascii-subset]
           ["accepted-trigraph.gravity"
            :gravity-c-emitter-printable-ascii-subset]]]
    (testing filename
      (let [{:keys [packet context]}
            (real-packet (fixture-relative filename))
            error-data
            (try
              (specialization/specialize-native-runtime-plan
               packet context)
              nil
              (catch clojure.lang.ExceptionInfo error
                (ex-data error)))]
        (is (= "P15GCE002" (:id error-data)) error-data)
        (is (= expected-fact (:missing-fact error-data)) error-data)
        (is (= :gravity-c-emitter-authenticated-subset
               (:diagnostic-family error-data)) error-data)))))

(deftest tampered-gravity-c-emitter-source-rejects-before-helper-execution
  (let [relative (fixture-relative "accepted-print.gravity")
        {:keys [packet context]} (real-packet relative)
        loader (var-get
                #'specialization/*p15-native-plan-c-emitter-source-loader*)]
    (is (= 1 (get @*real-packet-derivation-counts* relative))
        "the emitter-tamper gate reuses the accepted-print derivation")
    (with-redefs [specialization/*p15-native-plan-c-emitter-source-loader*
                  (fn [request-source]
                    (let [snapshot (loader request-source)]
                      (assoc snapshot
                             :source-text
                             (str (:source-text snapshot) "\n"))))]
      (is (= "P15GCE001"
             (diagnostic-id
              #(specialization/specialize-native-runtime-plan
                packet context)))))))

(defn -main
  [& _]
  (let [result (run-tests 'gravity.p15-native-plan-specialization-test)]
    (when (or (pos? (:fail result)) (pos? (:error result)))
      (System/exit 1))))
