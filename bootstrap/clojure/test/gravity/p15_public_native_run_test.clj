(ns gravity.p15-public-native-run-test
  "Focused P15-S23 evidence for the bounded public native runtime route.

  This namespace deliberately exercises only the explicit
  `run --target c --lowering runtime-derived` route.  The default run command
  remains the legacy bootstrap evaluator and the assertions below keep that
  distinction visible in the returned evidence.
  "
  (:require [clojure.test :refer [deftest is testing run-tests]]
            [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]))

(def ^:private native-gravity "examples/hello.gravity")
(def ^:private native-qst "examples/hello.qst")
(def ^:private legacy-core-gravity "examples/core-app.gravity")
(def ^:private legacy-core-qst "examples/core-app.qst")
(def ^:private expected-core-stdout
  "Hello Gravity\n")
(def ^:private no-follow-options
  (into-array java.nio.file.LinkOption
              [java.nio.file.LinkOption/NOFOLLOW_LINKS]))

(defn- diagnostic-data
  [f]
  (try
    (f)
    nil
    (catch clojure.lang.ExceptionInfo exception
      (ex-data exception))))

(defn- parse-native-run
  [source-path & options]
  (bootstrap/p18-t04-parse-run-request
   (into ["run" source-path] options)))

(defn- native-run-request
  [source-path]
  (parse-native-run source-path
                    "--target" "c"
                    "--lowering" "runtime-derived"))

(defn- private-bootstrap-call
  [name & arguments]
  (let [value (ns-resolve 'gravity.bootstrap (symbol name))]
    (apply (var-get value) arguments)))

(defn- private-bootstrap-var
  [name]
  (or (ns-resolve 'gravity.bootstrap (symbol name))
      (throw (ex-info (str "missing bootstrap private var: " name)
                      {:name name}))))

(defn- with-private-redefs
  [bindings f]
  (with-redefs-fn
    (into {}
          (map (fn [[name value]] [(private-bootstrap-var name) value])
               bindings))
    f))

(defn- with-private-staging
  "Run `f` with a real SecureDirectoryStream staging record and always close
  and remove that private root.  The returned evidence retains the path and
  cleanup record so the tests can assert no residue without trusting a
  manifest or replay."
  [source-path target f]
  (let [staging
        (private-bootstrap-call "c-backend-private-staging-directory!"
                                source-path target)
        evidence (atom {:root (:path staging)})]
    (try
      (swap! evidence assoc :result (f staging))
      (catch Throwable error
        (swap! evidence assoc :error error))
      (finally
        (swap! evidence assoc
               :cleanup
               (private-bootstrap-call "c-backend-delete-private-staging!"
                                       staging source-path target))))
    @evidence))

(defn- supervised-process
  [command source-path target role]
  (let [evidence
        (with-private-staging
          source-path target
          (fn [staging]
            (private-bootstrap-call "c-backend-run-process!"
                                    staging command source-path target role)))]
    (if-let [error (:error evidence)]
      (throw ^Throwable error)
      (:result evidence))))

(defn- supervised-process-evidence
  [command source-path target role]
  (with-private-staging
    source-path target
    (fn [staging]
      (private-bootstrap-call "c-backend-run-process!"
                              staging command source-path target role))))

(defn- run-native
  [source-path]
  (bootstrap/p18-t04-run-runtime-derived-c-file!
   (native-run-request source-path)))

(defn- runtime-component
  [record]
  (:application-runtime record))

(defn- source-provenance
  [record]
  (:source record))

(defn- cleanup-evidence
  [record]
  (get-in record [:temporary-artifacts :cleanup]))

(deftest p15-s23-public-native-run-parser-preserves-option-order
  (let [expected
        {:source-path native-gravity
         :target :c
         :target-requested? true
         :lowering-mode :runtime-derived
         :lowering-requested? true}
        requests
        [(bootstrap/p18-t04-parse-run-request
          ["run" native-gravity "--target" "c"
           "--lowering" "runtime-derived"])
         (bootstrap/p18-t04-parse-run-request
          ["run" native-gravity "--lowering" "runtime-derived"
           "--target" "c"])
         (bootstrap/p18-t04-parse-run-request
          ["run" native-gravity "--target" "c"
           "--lowering" "runtime-derived"])]
        expected-keys
        (select-keys expected
                     [:source-path :target :target-requested?
                      :lowering-mode :lowering-requested?])]
    (doseq [request requests]
      (is (= expected-keys (select-keys request (keys expected-keys))) request)
      (is (= ".gravity" (:source-extension request)) request)
      (is (= :gravity-branded-source (:source-kind request)) request)
      (is (true? (:runtime-derived-requested? request)) request)
      (is (nil? (:output-path request)) request))))

(deftest p15-s23-public-native-run-parser-fails-closed
  (doseq [arguments
          [["run" native-gravity "--target" "llvm"
            "--lowering" "runtime-derived"]
           ["run" native-gravity "--target" "c"
            "--lowering" "verified-mir"]
           ["run" native-gravity "--target" "c" "--unknown-option"]
           ["compile" native-gravity "--target" "c"]
           ["run" "--target" "c"]]]
    (let [data (diagnostic-data
                #(bootstrap/p18-t04-parse-run-request arguments))]
      (is (= "P18T04002" (:id data)) data)
      (is (= "P18-T04" (:phase data)) data)
      (is (or (= :p18-t04-public-run-request (:stage data))
              (= :p18-t04-executable-command-contract (:stage data))
              (nil? (:stage data))) data))))

(deftest p15-s23-native-process-success-has-no-capture-residue
  (let [evidence
        (supervised-process-evidence
         ["/bin/sh" "-c" "printf native-out; printf native-err >&2"]
         "p15-native-process-success.gravity" :c :test-success)
        result (:result evidence)
        root (:root evidence)
        cleanup (:cleanup evidence)]
    (is (true? (:finished? result)) result)
    (is (false? (:timed-out? result)) result)
    (is (= 0 (:exit result)) result)
    (is (= "native-out" (:out result)) result)
    (is (= "native-err" (:err result)) result)
    (is (= :test-success (:role result)) result)
    (is (= :complete (:status cleanup)) cleanup)
    (is (true? (:cleanup-complete? cleanup)) cleanup)
    (is (true? (:root-removed? cleanup)) cleanup)
    (is (false? (java.nio.file.Files/exists
                 root
                 (into-array java.nio.file.LinkOption
                             [java.nio.file.LinkOption/NOFOLLOW_LINKS])))
        {:root root :cleanup cleanup})))

(deftest p15-s23-native-process-timeout-reaps-captured-descendants
  (let [data
        (with-private-redefs
          {"*c-backend-process-timeout-ms*" 25}
          (fn []
            (diagnostic-data
             (fn []
               (supervised-process ["/bin/sh" "-c" "sleep 5 & wait"]
                                   "p15-native-process-timeout.gravity"
                                   :c :test-timeout)))))]
    (is (= "B2-DIALECT" (:id data)) data)
    (is (= :c-backend-process-timeout (:missing-fact data)) data)
    (is (= :test-timeout (:role data)) data)
    (is (= 25 (:timeout-ms data)) data)
    (is (true? (get-in data [:termination :kill-requested?])) data)
    (is (true? (get-in data [:termination :captured-process-set-reaped?])) data)
    (is (false? (get-in data [:termination :os-process-containment?])) data)
    (is (false? (get-in data [:termination :whole-process-tree-reaping-proved?])) data)
    (is (zero? (get-in data [:termination :alive-process-count])) data)))

(deftest p15-s23-native-process-bounded-pipes-leave-no-capture-residue
  ;; Output is pumped through bounded pipes rather than retained in staging
  ;; files.  Cleanup therefore has no capture-file residue to remove.
  (let [evidence
        (supervised-process-evidence
         ["/bin/sh" "-c" "printf 123456789"]
         "p15-native-process-capture.gravity" :c :test-capture)
        result (:result evidence)
        cleanup (:cleanup evidence)]
    (is (= 0 (:exit result)) result)
    (is (= "123456789" (:out result)) result)
    (is (= "" (:err result)) result)
    (is (= 9 (:stdout-byte-count result)) result)
    (is (zero? (:stderr-byte-count result)) result)
    (is (= :complete (:status cleanup)) cleanup)
    (is (true? (:root-removed? cleanup)) cleanup)))

(deftest p15-s23-native-process-stream-cap-retains-whole-utf8-and-hashes-wire
  (let [wire-bytes (byte-array [(byte 65) (byte -30) (byte -126)
                                (byte -84) (byte 66)])
        expected-hash (str "sha256:" (bootstrap/sha256-bytes-hex wire-bytes))
        data
        (with-private-redefs
          {"*c-backend-process-max-output-bytes*" 4}
          #(diagnostic-data
            (fn []
              (supervised-process
               ["/bin/sh" "-c" "printf '\\101\\342\\202\\254\\102'"]
               "p15-native-process-cap-boundary.gravity"
               :c :test-cap-boundary))))]
    (is (= "B2-DIALECT" (:id data)) data)
    (is (= :bounded-c-backend-process-output (:missing-fact data)) data)
    (is (= (str "A" (char 0x20ac)) (:text data)) data)
    (is (= 4 (:retained-byte-count data)) data)
    (is (= 5 (:total-byte-count data)) data)
    (is (= expected-hash (:hash data)) data)
    (is (true? (:stream-read-complete? data)) data)))

(deftest p15-s23-native-process-malformed-utf8-drains-and-fails-closed
  (let [data
        (diagnostic-data
         #(supervised-process
           ["/bin/sh" "-c" "printf '\\377tail'"]
           "p15-native-process-malformed-utf8.gravity" :c :test-malformed-utf8))]
    (is (= "B2-DIALECT" (:id data)) data)
    (is (= :c-backend-process-output-read (:missing-fact data)) data)
    (is (true? (:decode-error? data)) data)
    (is (true? (:stream-read-complete? data)) data)
    (is (string? (:hash data)) data)
    (is (map? (:termination data)) data)))

(deftest p15-s23-native-process-census-cap-checks-global-unique-churn-before-retain
  (let [first-snapshot
        (private-bootstrap-call "c-backend-census-merge-ids" #{} [11 12] 3)
        second-snapshot
        (private-bootstrap-call "c-backend-census-merge-ids"
                                (:retained-ids first-snapshot)
                                [12 13] 3)
        churn-snapshot
        (private-bootstrap-call "c-backend-census-merge-ids"
                                (:retained-ids second-snapshot)
                                [14] 3)]
    (is (false? (:overflow? first-snapshot)) first-snapshot)
    (is (false? (:overflow? second-snapshot)) second-snapshot)
    (is (= #{11 12 13} (:retained-ids second-snapshot)) second-snapshot)
    (is (true? (:overflow? churn-snapshot)) churn-snapshot)
    (is (= #{11 12 13} (:retained-ids churn-snapshot)) churn-snapshot)
    (is (= [14] (:new-ids churn-snapshot)) churn-snapshot)))

(deftest p15-s23-native-process-census-final-snapshot-fails-before-overcap-retain
  (let [initial
        (private-bootstrap-call "c-backend-census-consume-ids" #{} [21 22] 2)
        error
        (try
          (private-bootstrap-call "c-backend-census-consume-ids"
                                  (:retained-ids initial) [23] 2)
          nil
          (catch clojure.lang.ExceptionInfo failure
            failure))]
    (is (false? (:overflow? initial)) initial)
    (is (= #{21 22} (:retained-ids initial)) initial)
    (is (some? error) error)
    (is (= :bounded-c-backend-process-descendants
           (:missing-fact (ex-data error))) error)
    (is (= 2 (:captured-count (ex-data error))) error)
    (is (= 1 (:candidate-count (ex-data error))) error)
    (is (= #{21 22} (:retained-ids (ex-data error))) error)))

(deftest p15-s23-native-process-blocking-pump-closes-and-terminates
  (let [reader (java.io.PipedInputStream.)
        writer (java.io.PipedOutputStream. reader)
        pump (private-bootstrap-call "c-backend-start-output-pump!"
                                     reader :test-blocking-pump)]
    (try
      (Thread/sleep 50)
      (let [cleanup (private-bootstrap-call "c-backend-clean-output-pump!"
                                            pump)]
        (is (nil? cleanup) cleanup)
        (is (false? (.isAlive ^Thread (:thread pump))) pump))
      (finally
        (try (.close writer) (catch Exception _ nil))))))

(deftest p15-s23-native-process-fatal-error-identity-is-preserved
  (let [fatal (OutOfMemoryError. "synthetic p15 fatal")
        caught (try
                 (with-private-redefs
                   {"*c-backend-process-start-fn*"
                    (fn [_] (throw fatal))}
                   #(supervised-process
                     ["/bin/true"]
                     "p15-native-process-fatal.gravity" :c :test-fatal))
                 nil
                 (catch Throwable error error))]
    (is (identical? fatal caught) {:expected fatal :actual caught})))

(deftest p15-s23-native-process-pump-fatal-and-interrupt-identities-survive
  (doseq [fatal [(OutOfMemoryError. "synthetic pump OOME")
                 (ThreadDeath.)]]
    (let [caught
          (try
            (with-private-redefs
              {"*c-backend-process-read-stream-fn*"
               (fn [& _] (throw fatal))}
              #(supervised-process
                ["/usr/bin/true"] "p15-native-process-pump-fatal.gravity"
                :c :test-pump-fatal))
            nil
            (catch Throwable error error))]
      (is (identical? fatal caught) {:expected fatal :actual caught})))
  (let [interrupted (InterruptedException. "synthetic pump interrupt")
        caught
        (try
          (with-private-redefs
            {"*c-backend-process-read-stream-fn*"
             (fn [& _] (throw interrupted))}
            #(supervised-process
              ["/usr/bin/true"] "p15-native-process-pump-interrupt.gravity"
              :c :test-pump-interrupt))
          nil
          (catch Throwable error error))]
    (try
      (is (identical? interrupted caught)
          {:expected interrupted :actual caught})
      (finally
        (Thread/interrupted)))))

(deftest p15-s23-native-run-cc-fatal-primary-suppresses-cleanup
  (let [fatal (OutOfMemoryError. "synthetic run-cc fatal")
        cleanup (ex-info "synthetic staging cleanup failure"
                         {:id "TEST-CLEANUP-FAILURE"})
        caught
        (try
          (with-private-redefs
            {"c-backend-private-staging-directory!"
             (fn [& _] {:path (java.nio.file.Paths/get
                               "/tmp" (make-array String 0))})
             "c-backend-run-process!"
             (fn [& _] (throw fatal))
             "c-backend-delete-private-staging!"
             (fn [& _] (throw cleanup))}
            #(private-bootstrap-call
              "c-backend-run-cc!" "/tmp/p15-missing.c"
              "/tmp/p15-missing-program" "p15-run-cc-fatal.gravity" :c))
          nil
          (catch Throwable error error))]
    (is (identical? fatal caught) {:expected fatal :actual caught})
    (is (some #(identical? cleanup %)
              (.getSuppressed ^Throwable caught))
        {:suppressed (vec (.getSuppressed ^Throwable caught))})))

(deftest p15-s23-native-process-output-overflow-terminates-fail-closed
  (let [data
        (with-private-redefs
          {"*c-backend-process-max-output-bytes*" 8}
          (fn []
            (diagnostic-data
             (fn []
               (supervised-process
                ["/bin/sh" "-c" "printf 12345678901234567890"]
                                   "p15-native-process-overflow.gravity"
                                   :c :test-output-overflow)))))]
    (is (= "B2-DIALECT" (:id data)) data)
    (is (= :bounded-c-backend-process-output (:missing-fact data)) data)
    (is (= :stdout (:stream data)) data)
    (is (= :test-output-overflow (:role data)) data)
    (is (= 8 (:maximum-byte-count data)) data)
    (is (> (:observed-byte-count data) 8) data)
    (is (map? (:termination data)) data)
    (is (true? (get-in data [:termination :kill-requested?])) data)
    (is (false? (get-in data [:termination
                              :whole-process-tree-reaping-proved?])) data)))

(deftest p15-s23-private-staging-rejects-symlink-residue
  (let [source-path "p15-native-process-symlink.gravity"
        staging (private-bootstrap-call
                 "c-backend-private-staging-directory!" source-path :c)
        root (:path staging)
        link (.resolve ^java.nio.file.Path root "captured-output")]
    (try
      (java.nio.file.Files/createSymbolicLink
       link
       (java.nio.file.Paths/get "/tmp" (make-array String 0))
       (make-array java.nio.file.attribute.FileAttribute 0))
      (let [data
            (diagnostic-data
             #(private-bootstrap-call "c-backend-delete-private-staging!"
                                      staging source-path :c))]
        (is (= "B2-DIALECT" (:id data)) data)
        (is (= :nofollow-private-process-staging-cleanup
               (:missing-fact data)) data)
        (is (java.nio.file.Files/exists link no-follow-options) data)
        (is (java.nio.file.Files/exists root no-follow-options) data))
      (finally
        ;; The production deleter has already closed its SecureDirectoryStream
        ;; handles on the rejected residue path.  Remove only this test-owned
        ;; link/root so the negative fixture cannot leave temp state behind.
        (try (java.nio.file.Files/deleteIfExists link) (catch Exception _ nil))
        (try (java.nio.file.Files/deleteIfExists root) (catch Exception _ nil))))))

(deftest p15-s23-private-staging-rejects-renamed-root
  (let [source-path "p15-native-process-renamed-root.gravity"
        staging (private-bootstrap-call
                 "c-backend-private-staging-directory!" source-path :c)
        root ^java.nio.file.Path (:path staging)
        replacement (.resolveSibling
                     root
                     (str (.getFileName root) "-replacement"))]
    (try
      (java.nio.file.Files/move
       root replacement
       (make-array java.nio.file.CopyOption 0))
      (let [data
            (diagnostic-data
             #(private-bootstrap-call "c-backend-delete-private-staging!"
                                      staging source-path :c))]
        (is (= "B2-DIALECT" (:id data)) data)
        (is (= :secure-private-process-root-reopen
               (:missing-fact data)) data)
        (is (java.nio.file.Files/exists replacement no-follow-options) data)
        (is (not (java.nio.file.Files/exists root no-follow-options)) data))
      (finally
        (try (java.nio.file.Files/deleteIfExists replacement)
             (catch Exception _ nil))
        (try (java.nio.file.Files/deleteIfExists root)
             (catch Exception _ nil))))))

(deftest p15-s23-private-staging-cleans-partial-initialization
  (let [source-path "p15-native-process-partial-init.gravity"
        original
        (var-get (private-bootstrap-var "c-backend-read-basic-attributes"))
        calls (atom 0)
        created-path (atom nil)
        data
        (with-private-redefs
          {"c-backend-read-basic-attributes"
           (fn [path & arguments]
             (if (= 2 (swap! calls inc))
               (do
                 (reset! created-path path)
                 (throw (ex-info "injected staging initialization failure"
                                 {:id "TEST-PARTIAL-STAGING"})))
               (apply original path arguments)))}
          #(diagnostic-data
            (fn []
              (private-bootstrap-call
               "c-backend-private-staging-directory!" source-path :c))))]
    (is (= "TEST-PARTIAL-STAGING" (:id data)) data)
    (is (some? @created-path) data)
    (is (false? (java.nio.file.Files/exists
                 ^java.nio.file.Path @created-path no-follow-options))
        {:path @created-path :diagnostic data})))

(deftest p15-s23-native-process-interrupt-preserves-interruption
  (let [outcome (promise)
        worker
        (Thread.
         (fn []
           (try
             (supervised-process ["/bin/sh" "-c" "sleep 5"]
                                 "p15-native-process-interrupt.gravity"
                                 :c :test-interrupt)
             (deliver outcome [:completed (.isInterrupted (Thread/currentThread))])
             (catch Throwable error
               (deliver outcome [:failed error
                                 (.isInterrupted (Thread/currentThread))])))))]
    (.start worker)
    (Thread/sleep 100)
    (.interrupt worker)
    (.join worker 5000)
    (let [[status error interrupted?] @outcome]
      (is (= :failed status) outcome)
      (is (instance? InterruptedException error) outcome)
      (is (true? interrupted?) outcome))
    (is (false? (.isAlive worker)))))

(deftest p15-s23-public-native-run-fails-closed-before-source-or-staging-io
  (doseq [[source-path extension] [[native-gravity ".gravity"]
                                   [native-qst ".qst"]]]
    (let [request (native-run-request source-path)
          data
          (with-private-redefs
            {"read-gravity-source-text"
             (fn [& _]
               (throw (ex-info "source I/O reached" {:id "TEST-SOURCE-IO"})))
             "c-backend-private-staging-directory!"
             (fn [& _]
               (throw (ex-info "staging reached" {:id "TEST-STAGING"})))}
            #(diagnostic-data
              (fn []
                (bootstrap/p18-t04-run-runtime-derived-c-file! request))))]
      (testing (str source-path " request")
        (is (= source-path (:source-path request)) request)
        (is (= :c (:target request)) request)
        (is (= :runtime-derived (:lowering-mode request)) request))
      (testing (str source-path " containment gate")
        (is (= "P18T04002" (:id data)) data)
        (is (= :contained-public-native-run (:missing-fact data)) data)
        (is (= source-path (:source data)) data)
        (is (= extension (get-in data [:request :source-extension])) data)
        (is (false? (:native-executable-run? data)) data)
        (is (true? (:clojure-seed-boundary? data)) data)
        (is (false? (:seedless-release? data)) data)))))

(deftest p15-s23-public-native-run-fast-detached-stdio-closed-refuses-before-effects
  ;; A detached child with all standard descriptors closed must not make the
  ;; public gate wait for a pipe or imply ProcessHandle containment.  The gate
  ;; is required to reject before source, staging, or native effects.
  (let [builder (doto (ProcessBuilder.
                       ^java.util.List
                       ["/bin/sh" "-c"
                        "exec 0<&- 1>&- 2>&-; sleep 0.05"])
                  (.redirectOutput java.lang.ProcessBuilder$Redirect/DISCARD)
                  (.redirectError java.lang.ProcessBuilder$Redirect/DISCARD))
        detached (.start builder)
        _ (.close (.getOutputStream detached))
        started (System/nanoTime)
        data
        (with-private-redefs
          {"read-gravity-source-text"
           (fn [& _]
             (throw (ex-info "source I/O reached" {:id "TEST-SOURCE-IO"})))
           "c-backend-private-staging-directory!"
           (fn [& _]
             (throw (ex-info "staging reached" {:id "TEST-STAGING"})))}
          #(diagnostic-data
            (fn []
              (bootstrap/p18-t04-run-runtime-derived-c-file!
               (native-run-request native-gravity)))))
        elapsed-ms (/ (- (System/nanoTime) started) 1000000)]
    (.waitFor detached 1000 java.util.concurrent.TimeUnit/MILLISECONDS)
    (is (= "P18T04002" (:id data)) data)
    (is (= :contained-public-native-run (:missing-fact data)) data)
    (is (false? (:native-executable-run? data)) data)
    (is (< elapsed-ms 1000) {:elapsed-ms elapsed-ms :data data})
    (is (nil? (:termination data)) data)
    (is (not (contains? data :os-process-containment?)) data)))

(deftest p15-s23-public-native-run-does-not-call-legacy-evaluator
  (with-redefs [bootstrap/run-file
                (fn [& arguments]
                  (throw (ex-info "legacy evaluator called by native route"
                                  {:id "TEST-NATIVE-RUN-EVALUATOR-CALLED"
                                   :arguments arguments})))]
    (let [data (diagnostic-data #(run-native native-gravity))]
      (is (= "P18T04002" (:id data)) data)
      (is (= :contained-public-native-run (:missing-fact data)) data)
      (is (false? (:native-executable-run? data)) data))))

(deftest p15-s23-legacy-run-remains-bootstrap-evaluator
  (is (= "core-app\ngravity:19:2\n(:ok 19)\n"
         (bootstrap/run-file legacy-core-gravity)))
  (is (= "core-app\ngravity:19:2\n(:ok 19)\n"
         (bootstrap/run-file legacy-core-qst)))
  (let [request (bootstrap/p18-t04-parse-run-request
                 ["run" legacy-core-gravity])]
    (is (nil? (:target request)))
    (is (nil? (:lowering-mode request)))
    (is (false? (:runtime-derived-requested? request)))))

(deftest p15-s23-public-native-run-rejects-semantic-arity
  (doseq [[source-path semantic-id unsupported-op]
          [["bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity"
            "L2-FUNCTION-ARITY" :function-call]
           ["bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity"
            "L2-BUILTIN-ARITY" :assoc]]]
    (let [legacy-data (diagnostic-data #(bootstrap/run-file source-path))
          data (diagnostic-data
                #(bootstrap/p18-t04-run-runtime-derived-c-file!
                  (native-run-request source-path)))]
      ;; The fixture's semantic owner remains stable in the legacy checker;
      ;; the disabled native route rejects before source I/O or lowering.
      (is (= semantic-id (:id legacy-data)) legacy-data)
      (is (= "P18T04002" (:id data)) data)
      (is (= :contained-public-native-run (:missing-fact data)) data)
      (is (not (true? (:native-executable-run? data))) data)
      (is (= source-path (:source data)) data))))

(defn -main
  [& _]
  (let [result (run-tests 'gravity.p15-public-native-run-test)]
    (when (or (pos? (:fail result)) (pos? (:error result)))
      (System/exit 1))))
