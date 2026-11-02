(ns gravity.self-hosting.sh01-exact-tree-warm-worker-test
  (:require [clojure.test :as test :refer [deftest is testing]]
            [gravity.development-test-runner :as runner]
            [gravity.digest]
            [gravity.self-hosting.unified-execution-plan :as execution-plan]
            [gravity.self-hosting.sh01-exact-tree-warm-worker :as worker]))

(def commit "1111111111111111111111111111111111111111")
(def tree "2222222222222222222222222222222222222222")
(def expected {:candidate-commit commit :candidate-tree tree})

(defn request [id args]
  {:schema worker/request-schema :request-id id :operation :run-exact
   :candidate-commit commit :candidate-tree tree :args args})

(defn shutdown-request [id]
  {:schema worker/request-schema :request-id id :operation :shutdown
   :candidate-commit commit :candidate-tree tree})

(def exact-args
  ["--namespace" "gravity.c2-pass-cache-test"
   "--exact" "canonical-semantic-key-is-type-sensitive-and-path-scoped"])

(def cached-route-ids
  ["sh07-cached-accepted" "sh07-cached-contract" "sh07-cached-identity"
   "sh07-cached-rejected" "sh07-cached-verification"])

(defn cached-plan
  [route-id]
  (execution-plan/plan {:route-id route-id :lane :fast}))

(defn cached-request
  [id route-id]
  (let [plan (cached-plan route-id)
        executor-request (get-in plan [:executor :request])]
    {:schema worker/cached-request-schema :request-id id
     :operation :run-cached-shard
     :candidate-commit commit :candidate-tree tree
     :lane :fast :route-id route-id :plan-id (:plan-id plan)
     :catalog-id (:catalog-id plan)
     :closure-identity (:closure-identity plan)
     :result-contract-id (:result-contract-id executor-request)
     :shard (:shard executor-request)}))

(def cached-context
  {:request-schema-version 15
   :adapter :gravity/sh07-to-c6-core-products-v16
   :checked-core-source-content-hash
   "sha256:3e15d5707cf4ea37ef37b8e6089ad6ff62712efc5f6c3659a94edf62bae3f092"
   :checked-core-plan-semantic-hash
   "sha256:5bc9aeebb830350031c42814a3b47495205bd6108a617fcea977f8c0b918aebd"
   :java-runtime-version (System/getProperty "java.runtime.version")
   :clojure-version (clojure-version)})

(defn cached-result
  [shard]
  {:test 1 :pass 1 :fail 0 :error 0
   :artifact :gravity/sh07-cached-shard-result
   :shard shard :test-vars 1
   :cache-authoritative? false
   :fresh-authoritative-run-required? true
   :cache-context cached-context
   :elapsed-ms 7
   :cache {:sh06-hits 0 :sh06-misses 1
           :core-hits 0 :core-misses 1
           :verification-hits 0 :verification-misses 1}})

(defn- machine-result
  ([outcome exit-code]
   (machine-result outcome exit-code
                   {:test 1 :pass (if (= :passed outcome) 1 0)
                    :fail (if (= :failed outcome) 1 0) :error 0}))
  ([outcome exit-code summary]
   {:schema runner/machine-result-schema
    :authority :non-authoritative
    :authoritative? false
    :selection {:namespaces ["gravity.c2-pass-cache-test"]
                :test-vars
                ["gravity.c2-pass-cache-test/canonical-semantic-key-is-type-sensitive-and-path-scoped"]
                :fail-fast? false}
    :summary summary
    :outcome outcome
    :exit-code exit-code}))

(deftest requests-are-closed-exact-and-tree-bound
  (is (map? (worker/validate-request! (request "one" exact-args) expected)))
  (doseq [bad [(assoc (request "one" exact-args) :extra true)
               (assoc (request "one" exact-args) :candidate-tree commit)
               (request "one" [])
               (request "one" ["--namespace" "gravity.c2-pass-cache-test"])
               (request "one" ["--namespace" "gravity.c2-pass-cache-test"
                                "--regex" ".*"])]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (worker/validate-request! bad expected)))))

(deftest successful-requests-share-one-session
  (let [emitted (atom []) calls (atom [])
        lines (mapv (comp pr-str #(request % exact-args)) ["one" "two"])]
    (is (zero? (worker/run-session!
                lines #(swap! emitted conj %) expected
                #(assoc expected :clean? true)
                (fn [args]
                  (swap! calls conj args)
                  {:exit 0 :elapsed-ns 10 :out "ok" :err ""
                   :runner-result (machine-result :passed 0)})
                8)))
    (is (= [exact-args exact-args] @calls))
    (is (= [:passed :passed] (mapv :outcome @emitted)))
    (is (= [1 2] (mapv :request-count @emitted)))
    (is (every? #(false? (:authoritative? %)) @emitted))))

(deftest shutdown-response-has-a-closed-boolean-truncation-field
  (let [emitted (atom [])]
    (is (zero? (worker/run-session!
                [(pr-str (shutdown-request "done"))]
                #(swap! emitted conj %) expected
                #(assoc expected :clean? true)
                (fn [_] (throw (AssertionError. "shutdown must not execute"))) 8)))
    (is (false? (:output-truncated? (first @emitted))))))

(deftest failure-or-tree-drift-terminates-reuse
  (testing "a failed request prevents a later request in the same JVM"
    (let [emitted (atom []) calls (atom 0)
          lines [(pr-str (request "bad" exact-args))
                 (pr-str (request "never" exact-args))]]
      (is (= 1 (worker/run-session! lines #(swap! emitted conj %) expected
                                  #(assoc expected :clean? true)
                                  (fn [_] (swap! calls inc)
                                    {:exit 1 :elapsed-ns 1 :out "" :err "failed"
                                     :runner-result (machine-result :failed 1)}) 8)))
      (is (= 1 @calls))
      (is (= [:failed] (mapv :outcome @emitted)))))
  (testing "drift after execution rejects the result and terminates"
    (let [emitted (atom []) observations (atom 0)
          observe (fn []
                    (if (= 1 (swap! observations inc))
                      (assoc expected :clean? true)
                      (assoc expected :clean? false)))]
      (is (= 2 (worker/run-session! [(pr-str (request "drift" exact-args))]
                                    #(swap! emitted conj %) expected observe
                                    (fn [_] {:exit 0 :elapsed-ns 1 :out "" :err ""
                                             :runner-result (machine-result :passed 0)}) 8)))
      (is (= :tree-drift (:outcome (first @emitted))))
      (is (= 2 (:exit-code (first @emitted)))))))

(deftest namespace-authority-and-captured-output-are-bounded
  (testing "an unknown namespace is rejected before runner execution"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"frozen development catalog"
         (worker/validate-request!
          (request "unknown" ["--namespace" "gravity.not-in-static-catalog"
                              "--exact" "anything"])
          expected))))
  (testing "normal and clojure.test output share the capped response writer"
    (let [multibyte (String. (Character/toChars 0x1f642))
          large (apply str (repeat (* 100 1024) multibyte))
          result (with-redefs [runner/run-cli!
                               (fn [_]
                                 (test/do-report {:type :summary :test 1 :pass 1
                                                  :fail 0 :error 0})
                                 (runner/*machine-result-sink*
                                  (machine-result :passed 0))
                                 (print large)
                                 0)]
                   (#'worker/invoke-runner exact-args))]
      (is (= 0 (:exit result)))
      (is (= "" (:err result)))
      (is (<= (alength (.getBytes ^String (:out result) "UTF-8")) (* 256 1024)))
      (is (true? (:out-truncated? result)))
      (is (re-find #"Ran 1 tests" (:out result))))))

(deftest generated-b11-authority-admits-only-reviewed-exact-vars
  (let [namespace "gravity.self-hosting.sh07-vector-pattern-test"
        selector "sh07-b11-gravity-contract-names-l7"
        accepted ["--namespace" namespace "--exact" selector]
        request-for (fn [args]
                      (request (str "b11-" (count args)) args))]
    (is (map? (worker/validate-request! (request-for accepted) expected)))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"requires at least one namespace and exact selector"
         (worker/validate-request!
          (request-for ["--namespace" namespace "--fail-fast" "--fail-fast"])
          expected)))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"outside manifest namespace authority"
         (worker/validate-request!
          (request-for ["--namespace" namespace "--exact" "not-in-manifest"])
          expected)))))

(deftest warm-worker-detects-namespace-authority-drift
  (let [emitted (atom [])
        baseline (runner/namespace-authority)
        calls (atom 0)
        live-authority
        (fn []
          (if (= 1 (swap! calls inc))
            baseline
            (assoc baseline :generated-namespaces [])))]
    (with-redefs [runner/namespace-authority live-authority]
      (is (= 2 (worker/run-session!
                [(pr-str (request "authority-drift" exact-args))]
                #(swap! emitted conj %) expected
                #(assoc expected :clean? true)
                (fn [_] {:exit 0 :elapsed-ns 1 :out "" :err ""
                         :runner-result (machine-result :passed 0)})
                8))))
    (is (= :catalog-drift (:outcome (first @emitted))))
    (is (= 2 (:exit-code (first @emitted))))))

(deftest warm-worker-rejects-public-legacy-catalog-mutation
  (let [mutated (conj runner/namespace-catalog
                      {:namespace 'gravity.injected-test
                       :path "bootstrap/clojure/test/gravity/injected_test.clj"})
        failure
        (with-redefs [runner/namespace-catalog mutated]
          (try
            (worker/run-session!
             [(pr-str (request "catalog-drift" exact-args))]
             (fn [_] nil) expected
             (fn [] (assoc expected :clean? true))
             (fn [_] {:exit 0 :elapsed-ns 1 :out "" :err ""
                      :runner-result (machine-result :passed 0)})
             8)
            nil
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))))]
    (is (= "WARM-CATALOG-DRIFT" (:id failure)))))

(deftest machine-result-is-required-and-emitted-once
  (testing "a missing runner result cannot claim a passed execution"
    (let [result (with-redefs [runner/run-cli! (fn [_] 0)]
                   (#'worker/invoke-runner exact-args))]
      (is (= 2 (:exit result)))
      (is (nil? (:runner-result result)))))
  (testing "a pre-execution runner error carries no fabricated test evidence"
    (let [result (with-redefs [runner/run-cli!
                               (fn [_] (throw (ex-info "load failed" {})))]
                   (#'worker/invoke-runner exact-args))]
      (is (= 2 (:exit result)))
      (is (nil? (:runner-result result)))))
  (testing "a runner cannot overwrite an earlier machine result"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"more than one machine result"
         (with-redefs [runner/run-cli!
                       (fn [_]
                         (runner/*machine-result-sink* (machine-result :passed 0))
                         (runner/*machine-result-sink* (machine-result :passed 0))
                         0)]
           (#'worker/invoke-runner exact-args))))))

(deftest bounded-response-and-fatal-cancellation
  (testing "a response marks already-capped output as truncated"
    (let [emitted (atom [])
          oversized (apply str (repeat (inc (* 256 1024)) "x"))]
      (is (zero? (worker/run-session!
                  [(pr-str (request "large" exact-args))]
                  #(swap! emitted conj %) expected
                  #(assoc expected :clean? true)
                  (fn [_] {:exit 0 :elapsed-ns 1 :out oversized :err ""
                            :runner-result (machine-result :passed 0)}) 1)))
      (is (= (* 256 1024) (count (:stdout (first @emitted)))))
      (is (true? (:output-truncated? (first @emitted))))))
  (testing "causally wrapped linkage errors are never converted into test failures"
    (is (thrown? LinkageError
                 (with-redefs [runner/run-cli!
                               (fn [_]
                                 (throw (ex-info "wrapped" {}
                                                 (LinkageError. "fatal"))))]
                   (#'worker/invoke-runner exact-args)))))
  (testing "interruption is restored and rethrown"
    (try
      (let [interrupted?
            (try
              (with-redefs [runner/run-cli!
                            (fn [_] (throw (InterruptedException. "stop")))]
                (#'worker/invoke-runner exact-args))
              false
              (catch InterruptedException _
                (.isInterrupted (Thread/currentThread))))]
        (is interrupted?))
      (finally (Thread/interrupted)))))

(deftest cached-requests-are-closed-and-plan-derived
  (doseq [route-id cached-route-ids]
    (let [request (cached-request "cached" route-id)
          plan (cached-plan route-id)
          executor-request (get-in plan [:executor :request])]
      (is (= request (worker/validate-request! request expected)))
      (is (= #{:schema :request-id :operation :candidate-commit :candidate-tree
               :lane :route-id :plan-id :catalog-id :closure-identity
               :result-contract-id :shard}
             (set (keys request))))
      (is (= (:shard executor-request) (:shard request)))
      (is (= (:result-contract-id executor-request)
             (:result-contract-id request)))
      (is (= (:plan-id plan) (:plan-id request)))
      (is (= (:catalog-id plan) (:catalog-id request)))
      (is (= (:closure-identity plan) (:closure-identity request)))))
  (testing "cached requests use an operation-specific closed key set"
    (doseq [request [(assoc (cached-request "extra" "sh07-cached-accepted") :args [])
                     (dissoc (cached-request "missing" "sh07-cached-accepted") :shard)]]
      (let [error (try
                    (worker/validate-request! request expected)
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (= "WARM-REQUEST-SHAPE" (:id (ex-data error)))))))
  (testing "unknown routes and non-string shard values fail closed"
    (let [unknown (assoc (cached-request "bad-route" "sh07-cached-accepted")
                         :route-id "sh07-cached-missing")
          non-string (assoc (cached-request "bad-shard" "sh07-cached-accepted")
                            :shard :accepted)]
      (let [error (try
                    (worker/validate-request! unknown expected)
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (= "WARM-CACHED-CATALOG" (:id (ex-data error)))))
      (let [error (try
                    (worker/validate-request!
                     non-string expected)
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (= "WARM-CACHED-REQUEST-IDENTITY" (:id (ex-data error))))))))

(deftest cached-request-identity-remains-tree-bound
  (doseq [[field actual]
          [[:candidate-commit tree]
           [:candidate-tree commit]]]
    (let [error (try
                  (worker/validate-request!
                   (assoc (cached-request "identity" "sh07-cached-accepted") field actual)
                   expected)
                  nil
                  (catch clojure.lang.ExceptionInfo error error))]
      (is (= "WARM-TREE-MISMATCH" (:id (ex-data error))))
      (is (= field (:field (ex-data error))))
      (is (= actual (:actual (ex-data error)))))))

(deftest cached-request-plan-identity-mismatches-fail-before-dispatch
  (let [base (cached-request "identity" "sh07-cached-accepted")]
    (doseq [[field value expected-id]
            [[:schema worker/request-schema "WARM-REQUEST-SCHEMA"]
             [:lane :fresh "WARM-CACHED-REQUEST-IDENTITY"]
             [:plan-id (:catalog-id base) "WARM-CACHED-REQUEST-IDENTITY"]
             [:catalog-id (:plan-id base) "WARM-CACHED-REQUEST-IDENTITY"]
             [:closure-identity (:catalog-id base) "WARM-CACHED-REQUEST-IDENTITY"]
             [:result-contract-id :wrong-contract "WARM-CACHED-REQUEST-IDENTITY"]
             [:shard "not-the-declared-shard" "WARM-CACHED-REQUEST-IDENTITY"]]]
      (let [error (try
                    (worker/validate-request! (assoc base field value) expected)
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (some? error) (str "expected rejection for " field))
        (is (= expected-id (:id (ex-data error))) (str "diagnostic for " field))))
    (let [calls (atom 0)
          invalid (assoc base :shard "not-the-declared-shard")
          error (try
                  (worker/run-session!
                   [(pr-str invalid)] (fn [_] nil) expected
                   #(assoc expected :clean? true)
                   (fn [_] (throw (AssertionError. "exact must not execute")))
                   (fn [_] (swap! calls inc)
                     {:exit 0 :elapsed-ns 1 :out "" :err ""
                      :runner-result (cached-result "accepted")})
                   8)
                  nil
                  (catch clojure.lang.ExceptionInfo error error))]
      (is (= "WARM-CACHED-REQUEST-IDENTITY" (:id (ex-data error))))
      (is (zero? @calls)))))

(deftest cached-plan-drift-fails-closed-after-injected-execution
  (let [request (cached-request "drift" "sh07-cached-accepted")
        emitted (atom [])
        calls (atom [])
        plan-calls (atom 0)
        original-plan execution-plan/plan
        drifting-plan
        (fn [request]
          (let [plan (original-plan request)]
            (if (= 2 (swap! plan-calls inc))
              (assoc plan :plan-id
                     "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
              plan)))]
    (with-redefs [execution-plan/plan drifting-plan]
      (is (= 2 (worker/run-session!
                [(pr-str request)] #(swap! emitted conj %) expected
                #(assoc expected :clean? true)
                (fn [_] (throw (AssertionError. "exact path must not execute")))
                (fn [shard]
                  (swap! calls conj shard)
                  {:exit 0 :elapsed-ns 3 :out "cached" :err ""
                   :runner-result (cached-result shard)})
                8))))
    (is (= 2 @plan-calls))
    (is (= ["accepted"] @calls))
    (is (= 1 (count @emitted)))
    (is (= worker/cached-response-schema (:schema (first @emitted))))
    (is (= :catalog-drift (:outcome (first @emitted))))
    (is (= 2 (:exit-code (first @emitted))))
    (is (nil? (:runner-result (first @emitted))))
    (is (re-find #"WARM-CACHED-CATALOG" (:stderr (first @emitted))))))

(deftest cached-plan-control-failures-are-not-converted
  (let [request (cached-request "control" "sh07-cached-accepted")
        before (cached-plan "sh07-cached-accepted")]
    (testing "plan construction interruption is restored and rethrown"
      (try
        (with-redefs [requiring-resolve
                      (fn [_] (throw (InterruptedException. "stop")))]
          (#'worker/cached-plan-for-route "sh07-cached-accepted"))
        (is false "expected interruption")
        (catch InterruptedException _
          (is (Thread/interrupted)))))
    (testing "wrapped plan interruption is restored and rethrown"
      (try
        (with-redefs [requiring-resolve
                      (fn [_]
                        (throw (ex-info "wrapped" {}
                                        (InterruptedException. "stop"))))]
          (#'worker/cached-plan-for-route "sh07-cached-accepted"))
        (is false "expected wrapped interruption")
        (catch InterruptedException _
          (is (Thread/interrupted)))))
    (testing "wrapped plan linkage errors remain fatal"
      (is (thrown? LinkageError
                   (with-redefs [requiring-resolve
                                 (fn [_]
                                   (throw (ex-info "wrapped" {}
                                                   (LinkageError. "fatal"))))]
                     (#'worker/cached-plan-for-route "sh07-cached-accepted")))))
    (testing "post-execution interruption is restored and rethrown"
      (try
        (with-redefs [execution-plan/plan
                      (fn [_] (throw (InterruptedException. "stop")))]
          (#'worker/cached-plan-after-execution request before))
        (is false "expected interruption")
        (catch InterruptedException _
          (is (Thread/interrupted)))))
    (testing "wrapped post-execution plan linkage errors remain fatal"
      (is (thrown? LinkageError
                   (with-redefs [execution-plan/plan
                                 (fn [_]
                                   (throw (ex-info "wrapped" {}
                                                   (LinkageError. "fatal"))))]
                     (#'worker/cached-plan-after-execution request before)))))))

(deftest cached-dispatch-preserves-raw-result-and-response-shape
  (let [emitted (atom []) exact-calls (atom []) cached-calls (atom [])
        line (pr-str (cached-request "cached" "sh07-cached-accepted"))
        raw (cached-result "accepted")]
    (is (zero? (worker/run-session!
                [line] #(swap! emitted conj %) expected
                #(assoc expected :clean? true)
                (fn [args]
                  (swap! exact-calls conj args)
                  {:exit 0 :elapsed-ns 2 :out "" :err ""
                   :runner-result nil})
                (fn [shard]
                  (swap! cached-calls conj shard)
                  {:exit 0 :elapsed-ns 13 :out "cached" :err ""
                   :runner-result raw})
                8)))
    (is (= [] @exact-calls))
    (is (= ["accepted"] @cached-calls))
    (is (= [:passed] (mapv :outcome @emitted)))
    (is (= worker/cached-response-schema (:schema (first @emitted))))
    (is (= #{:schema :authority :authoritative? :request-id :candidate-commit
             :candidate-tree :lane :route-id :plan-id :catalog-id
             :closure-identity :result-contract-id :shard :outcome :exit-code
             :elapsed-ns :request-count :runner-result :stdout :stderr
             :output-truncated?}
           (set (keys (first @emitted)))))
    (is (= "sh07-cached-accepted" (:route-id (first @emitted))))
    (is (= :fast (:lane (first @emitted))))
    (is (= (:plan-id (cached-request "cached" "sh07-cached-accepted"))
           (:plan-id (first @emitted))))
    (is (= (:catalog-id (cached-request "cached" "sh07-cached-accepted"))
           (:catalog-id (first @emitted))))
    (is (= (:closure-identity (cached-request "cached" "sh07-cached-accepted"))
           (:closure-identity (first @emitted))))
    (is (= worker/cached-result-contract-id (:result-contract-id (first @emitted))))
    (is (= 1 (:request-count (first @emitted))))
    (is (= raw (:runner-result (first @emitted))))
    (is (false? (:authoritative? (first @emitted))))
    (is (= "cached" (:stdout (first @emitted))))))

(deftest cached-default-executor-captures-bounded-utf8-output
  (let [multibyte (String. (Character/toChars 0x1f642))
        large (apply str (repeat (* 100 1024) multibyte))
        result (atom nil)
        escaped
        (with-out-str
          (reset! result
                  (with-redefs [requiring-resolve
                                (fn [qualified-symbol]
                                  (is (= 'gravity.self-hosting.sh07-cached-shard-runner/run-shard
                                         qualified-symbol))
                                  (fn [_]
                                    (print large)
                                    (cached-result "accepted")))]
                    (#'worker/invoke-cached-runner "accepted"))))]
    (is (= "" escaped))
    (is (= 2 (:exit @result)))
    (is (true? (:out-truncated? @result)))
    (is (<= (alength (.getBytes ^String (:out @result) "UTF-8"))
            (* 256 1024)))
    (is (nil? (:runner-result @result)))
    (is (re-find #"WARM-CACHED-OUTPUT" (:err @result)))))

(deftest cached-default-executor-rejects-untrusted-results
  (let [result
        (with-redefs [requiring-resolve
                      (fn [_]
                        (fn [_]
                          (assoc (cached-result "accepted")
                                 :cache-authoritative? true)))]
          (#'worker/invoke-cached-runner "accepted"))]
    (is (= 2 (:exit result)))
    (is (nil? (:runner-result result)))
    (is (re-find #"WARM-CACHED-AUTHORITY" (:err result)))))

(deftest cached-default-executor-preserves-fatal-and-interruption-semantics
  (testing "causally wrapped linkage errors are never converted into failures"
    (is (thrown? LinkageError
                 (with-redefs [requiring-resolve
                               (fn [_]
                                 (fn [_]
                                   (throw (ex-info "wrapped" {}
                                                   (LinkageError. "fatal")))))]
                   (#'worker/invoke-cached-runner "accepted")))))
  (testing "interruption is restored and rethrown"
    (try
      (let [interrupted?
            (try
              (with-redefs [requiring-resolve
                            (fn [_]
                              (fn [_] (throw (InterruptedException. "stop"))))]
                (#'worker/invoke-cached-runner "accepted"))
              false
              (catch InterruptedException _
                (.isInterrupted (Thread/currentThread)) ))]
        (is interrupted?))
      (finally (Thread/interrupted)))))

(deftest cached-results-close-authority-context-and-counters
  (is (= (cached-result "accepted")
         (#'worker/validate-cached-result! "accepted"
                                           (cached-result "accepted"))))
  (doseq [[label result expected-id]
          [["top-level keys"
            (assoc (cached-result "accepted") :authority :authoritative)
            "WARM-CACHED-RESULT-KEYS"]
           ["authority claim"
            (assoc (cached-result "accepted") :cache-authoritative? true)
            "WARM-CACHED-AUTHORITY"]
           ["context keys"
            (assoc-in (cached-result "accepted")
                      [:cache-context :authority] :authoritative)
            "WARM-CACHED-CONTEXT-KEYS"]
           ["cache counter keys"
            (update (cached-result "accepted") :cache dissoc :core-misses)
            "WARM-CACHED-CACHE-KEYS"]
           ["cache counter value"
            (assoc-in (cached-result "accepted") [:cache :core-misses] -1)
            "WARM-CACHED-CACHE"]
           ["counter value"
            (assoc (cached-result "accepted") :test "one")
            "WARM-CACHED-COUNTER"]]]
    (testing label
      (let [error (try
                    (#'worker/validate-cached-result! "accepted" result)
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))))))

(deftest cached-drift-terminates-session-before-reuse
  (let [emitted (atom []) calls (atom []) observations (atom 0)
        lines [(pr-str (cached-request "first" "sh07-cached-accepted"))
               (pr-str (cached-request "never" "sh07-cached-contract"))]
        observe (fn []
                  (if (= 2 (swap! observations inc))
                    (assoc expected :clean? false)
                    (assoc expected :clean? true)))]
    (is (= 2 (worker/run-session!
              lines #(swap! emitted conj %) expected observe
              (fn [_] (throw (AssertionError. "exact path must not execute")))
              (fn [shard]
                (swap! calls conj shard)
                {:exit 0 :elapsed-ns 3 :out "" :err ""
                 :runner-result (cached-result shard)})
              8)))
    (is (= ["accepted"] @calls))
    (is (= [:tree-drift] (mapv :outcome @emitted)))
    (is (= 2 (:exit-code (first @emitted))))))

(deftest artifact-request-is-closed-and-never-carries-a-source-path
  (let [artifact {:schema worker/artifact-request-schema
                  :request-id "artifact-1" :operation :run-fast-artifact
                  :candidate-commit commit :candidate-tree tree
                  :lane :fast :route-id "route" :plan-id "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                  :catalog-id "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
                  :closure-identity "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"
                  :binding-id "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
                  :row-id "row" :stage :reader}]
    (let [error (try (worker/validate-request!
                      (assoc artifact :source-path "/tmp/escape") expected)
                     nil
                     (catch clojure.lang.ExceptionInfo error error))]
      (is (= "WARM-REQUEST-SHAPE" (:id (ex-data error)))))))

(deftest artifact-request-rejects-malformed-row-and-stage-before-planning
  (let [base {:schema worker/artifact-request-schema :request-id "a"
              :operation :run-fast-artifact :candidate-commit commit
              :candidate-tree tree :lane :fast :route-id "route"
              :plan-id "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
              :catalog-id "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
              :closure-identity "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"
              :binding-id "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
              :stage :source}]
    (doseq [[row stage expected-id]
            [["" :source "WARM-ARTIFACT-ROW"]
             [(apply str (repeat 257 "x")) :source "WARM-ARTIFACT-ROW"]
             ["row" :unknown "WARM-ARTIFACT-STAGE"]]]
      (let [error (try (worker/validate-request!
                        (assoc base :row-id row :stage stage) expected)
                       nil
                       (catch clojure.lang.ExceptionInfo error error))]
        (is (= expected-id (:id (ex-data error))))))))

(deftest artifact-path-snapshot-binds-both-peer-bytes-and-rejects-symlinks
  (let [root (java.nio.file.Files/createTempDirectory
              "warm-artifact-" (make-array java.nio.file.attribute.FileAttribute 0))
        family (.resolve root "fixtures/accepted")
        gravity (.resolve family "sample.gravity")
        qst (.resolve family "sample.qst")]
    (try
      (java.nio.file.Files/createDirectories family
                                             (make-array java.nio.file.attribute.FileAttribute 0))
      (java.nio.file.Files/write gravity (.getBytes "(ns sample)\n")
                                (make-array java.nio.file.OpenOption 0))
      (java.nio.file.Files/write qst (.getBytes "(ns sample)\n")
                                (make-array java.nio.file.OpenOption 0))
      (let [snapshot (#'worker/artifact-pair-snapshot
                      root {:gravity-path "fixtures/accepted/sample.gravity"
                            :qst-path "fixtures/accepted/sample.qst"})]
        (is (= (:pair-bytes snapshot) (:bytes-count (:gravity snapshot))))
        (is (= (:pair-sha256 snapshot) (:bytes-sha256 (:gravity snapshot))))
        (java.nio.file.Files/write qst (.getBytes "(ns changed)\n")
                                  (make-array java.nio.file.OpenOption 0))
        (let [error (try (#'worker/artifact-pair-snapshot
                          root {:gravity-path "fixtures/accepted/sample.gravity"
                                :qst-path "fixtures/accepted/sample.qst"})
                         nil
                         (catch clojure.lang.ExceptionInfo error error))]
          (is (= "WARM-ARTIFACT-PAIR" (:id (ex-data error))))))
      (let [outside (.resolve root "outside")
            link-parent (.resolve root "fixtures-link")]
        (java.nio.file.Files/createDirectories outside
                                               (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/createSymbolicLink link-parent outside
                                                (make-array java.nio.file.attribute.FileAttribute 0))
        (let [error (try (#'worker/artifact-pair-snapshot
                          root {:gravity-path "fixtures-link/sample.gravity"
                                :qst-path "fixtures-link/sample.qst"})
                         nil
                         (catch clojure.lang.ExceptionInfo error error))]
          (is (= "WARM-ARTIFACT-SYMLINK" (:id (ex-data error))))))
      (finally
        (with-open [paths (java.nio.file.Files/walk root
                                                  (make-array java.nio.file.FileVisitOption 0))]
          (.forEach (.sorted paths (java.util.Comparator/reverseOrder))
                    (reify java.util.function.Consumer
                      (accept [_ path]
                        (java.nio.file.Files/deleteIfExists path)))))))))

(deftest artifact-runner-uses-derived-path-and-returns-compact-fast-projection
  (let [root (java.nio.file.Files/createTempDirectory
              "warm-artifact-runner-" (make-array java.nio.file.attribute.FileAttribute 0))
        family (.resolve root "fixtures/accepted")
        gravity (.resolve family "sample.gravity")
        qst (.resolve family "sample.qst")
        bytes (.getBytes "(ns sample)\n")
        hash (str "sha256:" (gravity.digest/sha256-bytes-hex bytes))
        fixture-binding {:gravity-path "fixtures/accepted/sample.gravity"
                 :qst-path "fixtures/accepted/sample.qst"
                 :fixture-sha256 hash :fixture-bytes (alength bytes)
                 :binding-id "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"}
        request {:stage :source}
        plan-binding {:plan {} :binding fixture-binding}]
    (try
      (java.nio.file.Files/createDirectories family
                                             (make-array java.nio.file.attribute.FileAttribute 0))
      (java.nio.file.Files/write gravity bytes (make-array java.nio.file.OpenOption 0))
      (java.nio.file.Files/write qst bytes (make-array java.nio.file.OpenOption 0))
      (let [id "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            fake-result {:kind :gravity/authenticated-artifact-pipeline-result
                         :mode :fast :requested-stage :source
                         :result-id id :pipeline :gravity/authenticated-compiler-artifact-pipeline-v1
                         :source-content-hash hash
                         :authority {:authoritative? false}
                         :execution-boundary {:isolation :same-jvm}
                         :stage-artifacts
                         (array-map :source {:artifact :gravity/authenticated-compiler-artifact
                                             :status :accepted :stage :source :mode :fast
                                             :pipeline :gravity/authenticated-compiler-artifact-pipeline-v1
                                             :artifact-id id :input-artifact-ids []
                                             :source-identity {:byte-count (alength bytes)
                                                               :bytes-hash hash}
                                             :provenance {:actual-path (str (.toRealPath gravity (make-array java.nio.file.LinkOption 0)))
                                                          :project-root-path (str (.toRealPath root (make-array java.nio.file.LinkOption 0)))
                                                          :project-relative-path (:gravity-path fixture-binding)}
                                             :cache-policy {:observed-hit? false}
                                             :diagnostics []}
                                             )
                         :artifact {:artifact :gravity/authenticated-compiler-artifact
                                    :status :accepted :stage :source :mode :fast
                                    :pipeline :gravity/authenticated-compiler-artifact-pipeline-v1
                                    :artifact-id id :input-artifact-ids []
                                    :source-identity {:byte-count (alength bytes)
                                                      :bytes-hash hash}
                                    :provenance {:actual-path (str (.toRealPath gravity (make-array java.nio.file.LinkOption 0)))
                                                 :project-root-path (str (.toRealPath root (make-array java.nio.file.LinkOption 0)))
                                                 :project-relative-path (:gravity-path fixture-binding)}
                                    :cache-policy {:observed-hit? false}
                                    :diagnostics []}}]
        (with-redefs [worker/repository-root (constantly root)
                      worker/artifact-plan-for-request (constantly plan-binding)]
          (binding [worker/*artifact-pipeline-runner*
                    (fn [_]
                      (print "pipeline-out")
                      (binding [*out* *err*]
                        (print "pipeline-err"))
                      fake-result)]
            (let [result (#'worker/invoke-artifact-runner request fixture-binding)]
              (is (= :source (get-in result [:projection :requested-stage])))
              (is (= hash (get-in result [:projection :source-content-hash])))
              (is (false? (get-in result [:projection :authority :authoritative?])))
              (is (= "pipeline-out" (:out result)))
              (is (= "pipeline-err" (:err result)))
              (is (false? (:out-truncated? result)))
              (is (false? (:err-truncated? result))))
            (let [noisy (fn [_]
                          (print (apply str (repeat 300000 "x")))
                          fake-result)]
              (with-redefs [worker/repository-root (constantly root)
                            worker/artifact-plan-for-request (constantly plan-binding)]
                (binding [worker/*artifact-pipeline-runner* noisy]
                  (let [result (#'worker/invoke-artifact-runner request fixture-binding)]
                    (is (true? (:out-truncated? result)))))))
            (let [error (try
                          (binding [worker/*artifact-pipeline-runner*
                                    (constantly (assoc fake-result :status :failed))]
                            (#'worker/invoke-artifact-runner request fixture-binding))
                          nil
                          (catch clojure.lang.ExceptionInfo error error))]
              (is (= "WARM-ARTIFACT-PIPELINE" (:id (ex-data error))))))))
      (finally
        (with-open [paths (java.nio.file.Files/walk root
                                                  (make-array java.nio.file.FileVisitOption 0))]
          (.forEach (.sorted paths (java.util.Comparator/reverseOrder))
                    (reify java.util.function.Consumer
                      (accept [_ path]
                        (java.nio.file.Files/deleteIfExists path)))))))))
