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
          {"c-backend-process-timeout-ms" 25}
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

(deftest p15-s23-native-process-output-overflow-terminates-fail-closed
  (let [data
        (with-private-redefs
          {"c-backend-process-max-output-bytes" 8}
          (fn []
            (diagnostic-data
             (fn []
               (supervised-process
                ["/bin/sh" "-c" "while :; do printf 123456789; done"]
                                   "p15-native-process-overflow.gravity"
                                   :c :test-output-overflow)))))]
    (is (= "B2-DIALECT" (:id data)) data)
    (is (= :bounded-c-backend-process-output (:missing-fact data)) data)
    (is (= :stdout (:stream data)) data)
    (is (= :test-output-overflow (:role data)) data)
    (is (= 8 (:maximum-byte-count data)) data)
    (is (< 8 (:observed-byte-count data)) data)
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
      (is (= "B2-DIALECT" (:id (ex-data error))) outcome)
      (is (= :c-backend-process-interrupted
             (:missing-fact (ex-data error))) outcome)
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
