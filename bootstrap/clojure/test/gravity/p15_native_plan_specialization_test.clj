(ns gravity.p15-native-plan-specialization-test
  "Focused evidence for authenticated plan-specialized C artifacts.

  The namespace consumes real stage2 runtime packets and emits the direct C
  backend source.  Tests compile/run that source only inside a test-owned
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

(defn- source-text
  [relative]
  (let [bytes (Files/readAllBytes (path relative))
        decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (str (.decode decoder (ByteBuffer/wrap bytes)))))

(defn- real-packet
  [relative]
  (let [text (source-text relative)]
    {:packet (bootstrap/stage2-runtime-derived-packet relative text :c)
     :context (bootstrap/p15-s23-closed-runtime-packet-context
               relative text :c)}))

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
            (let [{:keys [packet context]}
                  (real-packet (fixture-relative filename))
                  artifact
                  (specialization/specialize-native-runtime-plan
                   packet context)
                  execution (compile-and-run!
                             directory
                             (get-in artifact [:generated-c :source]))]
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
              (is (= :clojure-emitted-plan-specialized-c
                     (get-in artifact [:generated-c :implementation]))
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
  (let [{:keys [packet context]}
        (real-packet (fixture-relative "accepted-print.gravity"))
        validator (var-get #'bootstrap/c-backend-validate-runtime-plan!)
        emitter (var-get #'bootstrap/c-backend-runtime-source)
        validator-calls (atom 0)
        emitter-calls (atom 0)
        changed-source (str (:source-text context) "\n")
        changed-context
        (bootstrap/p15-s23-closed-runtime-packet-context
         (:source-path context) changed-source :c)
        cases [[(assoc packet :status :tampered) context]
               [packet changed-context]
               [(assoc-in packet [:plan :entrypoint] 'tampered) context]]]
    (with-redefs [bootstrap/c-backend-validate-runtime-plan!
                  (fn [& args]
                    (swap! validator-calls inc)
                    (apply validator args))
                  bootstrap/c-backend-runtime-source
                  (fn [& args]
                    (swap! emitter-calls inc)
                    (apply emitter args))]
      (doseq [[candidate candidate-context] cases]
        (is (= "P15NS001"
               (diagnostic-id
                #(specialization/specialize-native-runtime-plan
                  candidate candidate-context)))))
      (is (zero? @validator-calls))
      (is (zero? @emitter-calls)))))

(deftest authenticated-unsupported-plan-rejects-before-emitter
  (let [{:keys [packet context]}
        (real-packet (fixture-relative "unsupported-builtin.gravity"))
        emitter (var-get #'bootstrap/c-backend-runtime-source)
        emitter-calls (atom 0)]
    (with-redefs [bootstrap/c-backend-runtime-source
                  (fn [& args]
                    (swap! emitter-calls inc)
                    (apply emitter args))]
      (is (= "P15NS002"
             (diagnostic-id
              #(specialization/specialize-native-runtime-plan
                packet context))))
      (is (zero? @emitter-calls)))))

(deftest overbound-packet-tamper-rejects-before-validator
  (let [{:keys [packet context]}
        (real-packet (fixture-relative "accepted-print.gravity"))
        entrypoint (:entrypoint (:plan packet))
        instructions (get-in packet [:plan :functions entrypoint :instructions])
        overbound-packet
        (assoc-in packet [:plan :functions entrypoint :instructions]
                  (vec (concat instructions (repeat 128 (first instructions)))))
        validator (var-get #'bootstrap/c-backend-validate-runtime-plan!)
        validator-calls (atom 0)]
    (with-redefs [bootstrap/c-backend-validate-runtime-plan!
                  (fn [& args]
                    (swap! validator-calls inc)
                    (apply validator args))]
      (is (= "P15NS001"
             (diagnostic-id
              #(specialization/specialize-native-runtime-plan
                overbound-packet context))))
      (is (zero? @validator-calls)))))

(defn -main
  [& _]
  (let [result (run-tests 'gravity.p15-native-plan-specialization-test)]
    (when (or (pos? (:fail result)) (pos? (:error result)))
      (System/exit 1))))
