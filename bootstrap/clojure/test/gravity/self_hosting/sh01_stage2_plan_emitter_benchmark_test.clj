(ns gravity.self-hosting.sh01-stage2-plan-emitter-benchmark-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh01-stage2-plan-emitter-benchmark
             :as benchmark]))

(def ^:private accepted-source-path
  "bootstrap/clojure/fixtures/accepted/core-app.gravity")

(def ^:private rejected-source-path
  "bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity")

(def ^:private authenticated-envelope-suffix
  "/gravity/compiler/authenticated_envelope.gravity")

(def ^:private synthetic-binding-request
  {:inputs
   {:source-path "/synthetic/authenticated_envelope.gravity"
    :source-byte-count 1
    :source-content-hash "sha256:synthetic-source"
    :emitter-target :jvm
    :emitter-source-path "/synthetic/emitter.gravity"
    :emitter-source-byte-count 1
    :emitter-source-content-hash "sha256:synthetic-emitter"
    :emitter-source-rule-hash "sha256:synthetic-rule"}
   :source-text "synthetic"
   :emitter-rule {:emitter :synthetic}})

(defn- private-bootstrap-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "Private bootstrap helper is absent"
                      {:symbol symbol}))))

(defn- emitter
  [source-path]
  (:emitter
   (bootstrap/c-backend-stage2-plan-emitter-source-rule!
    source-path :jvm)))

(defn- compile-plan
  [source-path]
  (bootstrap/p15-s23-stage2-plan-emitter-compile-source
   (emitter source-path) source-path (slurp source-path)))

(defn- diagnostic
  [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(defn- authenticated-envelope-path?
  [source-path]
  (.endsWith (.replace source-path "\\" "/")
             authenticated-envelope-suffix))

(deftest accepted-and-rejected-emission-preserve-semantics-and-fresh-authority
  (let [original bootstrap/p15-s23-stage2-compiler-artifact-plan
        source-inputs-var
        (ns-resolve 'gravity.bootstrap
                    'p15-s23-sh02-source-binding-inputs!)
        original-source-inputs @source-inputs-var
        active-request (atom nil)
        calls (atom {:accepted 0 :rejected 0})
        authentication-calls (atom {:accepted 0 :rejected 0})
        compile-with-count
        (fn [request source-path]
          (reset! active-request request)
          (try
            (compile-plan source-path)
            (finally
              (reset! active-request nil))))]
    (with-redefs-fn
     {#'bootstrap/p15-s23-stage2-compiler-artifact-plan
      (fn [active-emitter source-path source-text]
        (when (authenticated-envelope-path? source-path)
          (swap! calls update @active-request inc))
        (original active-emitter source-path source-text))
      source-inputs-var
     (fn [candidate source-path]
        (swap! authentication-calls update @active-request inc)
        (original-source-inputs candidate source-path))}
     (fn []
       (let [accepted (compile-with-count :accepted accepted-source-path)
             output (bootstrap/execute-stage0-compiled-plan accepted)
             rejected
             (diagnostic
              #(bootstrap/execute-stage0-compiled-plan
                (compile-with-count :rejected rejected-source-path)))]
         (testing "accepted plan identity, order, diagnostics, and output"
           (is (= "sha256:83fe62a3284e27f8527753434c49b74315148c68e9759aca5fd4b00289e792f0"
                  (:plan-id accepted)))
           (is (= {:builtin-call 14 :literal 18 :local 11 :let 1
                   :function-call 3 :do 1 :println 4 :if 1}
                  (:instruction-summary accepted)))
           (is (= [] (:diagnostics accepted)))
           (is (= "core-app\ngravity:19:2\n(:ok 19)\n" output)))
         (testing "negative diagnostic remains exact"
           (is (= "L2-FUNCTION-ARITY" (:id rejected)))
           (is (= 'add (:function rejected)))
           (is (= 1 (:actual-arity rejected)))
           (is (= 2 (:expected-arity rejected)))
           (is (= rejected-source-path
                  (get-in rejected [:source-span :source]))))
         (testing "each fresh top-level request compiles one authenticated plan"
           (is (= {:accepted 1 :rejected 1} @calls)))
         (testing "every cache lookup still reauthenticates current inputs"
           (is (> (:accepted @authentication-calls)
                  (:accepted @calls)))
           (is (> (:rejected @authentication-calls)
                  (:rejected @calls)))))))))

(deftest cache-key-binds-every-sh02-validation-policy-input
  (let [cache-key-var
        (private-bootstrap-var 'p15-s23-sh02-source-binding-cache-key)
        baseline (@cache-key-var synthetic-binding-request)
        drift-cases
        [[#'bootstrap/p15-s23-sh02-source-byte-count 59496]
         [#'bootstrap/p15-s23-sh02-expected-source-content-hash
          "sha256:drift-source"]
         [#'bootstrap/p15-s23-sh02-expected-plan-semantic-hash
          "sha256:drift-plan"]
         [#'bootstrap/p15-s23-sh02-expected-functions-semantic-hash
          "sha256:drift-functions"]
         [#'bootstrap/p15-s23-sh02-expected-builder-semantic-hash
          "sha256:drift-builder"]
         [#'bootstrap/p15-s23-sh02-expected-verifier-semantic-hash
          "sha256:drift-verifier"]
         [#'bootstrap/p15-s23-sh02-expected-function-count 73]
         [#'bootstrap/p15-s23-sh02-builder-function 'drift-builder]
         [#'bootstrap/p15-s23-sh02-verifier-function 'drift-verifier]
         [#'bootstrap/p15-s23-sh02-required-functions
          {'drift {:arity 0 :params []}}]]]
    (is (= 2 (:schema-version baseline)))
    (is (= (:inputs synthetic-binding-request)
           (:authenticated-inputs baseline)))
    (is (not= baseline
              (with-bindings
               {#'bootstrap/*additional-bootstrap-targets* #{:llvm}}
                (@cache-key-var synthetic-binding-request))))
    (doseq [[policy-var drift-value] drift-cases]
      (is (not= baseline
                (with-redefs-fn
                 {policy-var drift-value}
                 #(@cache-key-var synthetic-binding-request)))
          (str "policy drift was absent from key: " policy-var)))))

(deftest same-policy-reuses-and-policy-or-target-drift-recompiles
  (let [with-context-var
        (private-bootstrap-var 'p15-s23-with-stage2-plan-emission-context)
        cached-binding-var
        (private-bootstrap-var 'p15-s23-sh02-cached-source-binding!)
        compile-binding-var
        (private-bootstrap-var 'p15-s23-sh02-compile-source-binding!)
        policy-var
        #'bootstrap/p15-s23-sh02-expected-plan-semantic-hash
        compile-count (atom 0)]
    (with-redefs-fn
     {compile-binding-var
      (fn [_ _]
        {:compile-number (swap! compile-count inc)
         :policy @policy-var})}
     (fn []
       (@with-context-var
        (fn []
          (let [first-binding
                (@cached-binding-var "synthetic.gravity"
                 synthetic-binding-request)
                same-key-binding
                (@cached-binding-var "synthetic.gravity"
                 synthetic-binding-request)
                drifted-binding
                (with-redefs-fn
                 {policy-var "sha256:drift-plan"}
                 #(@cached-binding-var "synthetic.gravity"
                   synthetic-binding-request))
                target-drifted-binding
                (with-bindings
                 {#'bootstrap/*additional-bootstrap-targets* #{:llvm}}
                  (@cached-binding-var "synthetic.gravity"
                   synthetic-binding-request))
                restored-binding
                (@cached-binding-var "synthetic.gravity"
                 synthetic-binding-request)]
            (testing "same authenticated inputs and policy reuse one binding"
              (is (identical? first-binding same-key-binding))
              (is (identical? first-binding restored-binding)))
            (testing "policy drift cannot reuse the prior binding"
              (is (not (identical? first-binding drifted-binding)))
              (is (= "sha256:drift-plan" (:policy drifted-binding)))
              (is (not (identical? first-binding target-drifted-binding)))
              (is (= 3 @compile-count))))))))))

(deftest failed-build-is-not-cached
  (let [with-context-var
        (private-bootstrap-var 'p15-s23-with-stage2-plan-emission-context)
        cached-binding-var
        (private-bootstrap-var 'p15-s23-sh02-cached-source-binding!)
        compile-binding-var
        (private-bootstrap-var 'p15-s23-sh02-compile-source-binding!)
        compile-count (atom 0)]
    (with-redefs-fn
     {compile-binding-var
      (fn [_ _]
        (let [attempt (swap! compile-count inc)]
          (if (= 1 attempt)
            (throw (ex-info "synthetic compile failure"
                            {:id "SYNTHETIC-COMPILE-FAILURE"}))
            {:compile-number attempt})))}
     (fn []
       (@with-context-var
        (fn []
          (is (= "SYNTHETIC-COMPILE-FAILURE"
                 (:id
                  (diagnostic
                   #(@cached-binding-var "synthetic.gravity"
                     synthetic-binding-request)))))
          (is (= {:compile-number 2}
                 (@cached-binding-var "synthetic.gravity"
                  synthetic-binding-request)))
          (is (= 2 @compile-count))))))))

(deftest concurrent-top-level-requests-have-isolated-contexts
  (let [with-context-var
        (private-bootstrap-var 'p15-s23-with-stage2-plan-emission-context)
        cached-binding-var
        (private-bootstrap-var 'p15-s23-sh02-cached-source-binding!)
        compile-binding-var
        (private-bootstrap-var 'p15-s23-sh02-compile-source-binding!)
        compile-count (atom 0)
        both-started (promise)
        release (promise)]
    (with-redefs-fn
     {compile-binding-var
      (fn [_ _]
        (let [attempt (swap! compile-count inc)]
          (when (= 2 attempt)
            (deliver both-started true))
          @release
          {:compile-number attempt}))}
     (fn []
       (let [build
             #(future
                (@with-context-var
                 (fn []
                   (@cached-binding-var "synthetic.gravity"
                    synthetic-binding-request))))
             left (build)
             right (build)
             started? (= true (deref both-started 5000 :timeout))
             _ (deliver release true)
             left-binding (deref left 5000 :timeout)
             right-binding (deref right 5000 :timeout)]
         (is started?)
         (is (not= :timeout left-binding))
         (is (not= :timeout right-binding))
         (is (= 2 @compile-count))
         (is (not= left-binding right-binding)))))))

(deftest benchmark-report-is-bounded-and-non-authoritative
  (let [fake-plan
        {:kind :gravity/stage2-emitted-core-plan
         :plan-id "sha256:fake"
         :source {:path accepted-source-path :content-hash "sha256:source"}
         :module {:source-path accepted-source-path}
         :functions (sorted-map 'main {:instructions []})
         :instruction-summary {}
         :diagnostics []}
        result
        (with-redefs
         [bootstrap/c-backend-stage2-plan-emitter-source-rule!
          (fn [_ _] {:emitter :fake})
          bootstrap/p15-s23-stage2-plan-emitter-compile-source
          (fn [_ _ _] fake-plan)
          bootstrap/execute-stage0-compiled-plan
          (fn [_] {:stdout "" :value nil})]
          (benchmark/run-benchmark {:iterations 2}))]
    (is (= :gravity/sh01-stage2-plan-emitter-benchmark
           (:artifact result)))
    (is (= :non-authoritative (:authority result)))
    (is (false? (:authoritative? result)))
    (is (true? (:fresh-plan-emission-per-iteration? result)))
    (is (false? (:persistent-cache-authority? result)))
    (is (= 2 (count (:samples result))))
    (is (= "sha256:fake" (get-in result [:semantic-receipt :plan-id]))))
  (testing "CLI and execution bounds fail closed"
    (is (= {:iterations 2}
           (benchmark/parse-arguments ["--iterations" "2"])))
    (doseq [options [{:iterations 0} {:iterations 4}]]
      (is (= "SH01-STAGE2-PLAN-EMITTER-BENCHMARK-COUNT"
             (:id
              (ex-data
               (try
                 (benchmark/run-benchmark options)
                 (catch clojure.lang.ExceptionInfo error error)))))))
    (is (= "SH01-STAGE2-PLAN-EMITTER-BENCHMARK-USAGE"
           (:id
            (ex-data
             (try
               (benchmark/parse-arguments ["--unknown" "1"])
               (catch clojure.lang.ExceptionInfo error error))))))))
