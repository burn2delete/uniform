(ns gravity.self-hosting.sh01-exact-tree-warm-worker-test
  (:require [clojure.test :as test :refer [deftest is testing]]
            [gravity.development-test-runner :as runner]
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
   "--exact" "canonical-semantic-key-is-bounded-type-sensitive-and-path-scoped"])

(def cached-shards ["accepted" "contract" "identity" "rejected" "verification"])

(defn cached-request
  [id shard]
  {:schema worker/request-schema :request-id id :operation :run-cached-shard
   :candidate-commit commit :candidate-tree tree :shard shard})

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
                ["gravity.c2-pass-cache-test/canonical-semantic-key-is-bounded-type-sensitive-and-path-scoped"]
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

(deftest cached-requests-are-closed-and-allowlisted
  (doseq [shard cached-shards]
    (is (= shard
           (:shard (worker/validate-request!
                    (cached-request "cached" shard) expected)))))
  (testing "cached requests use an operation-specific closed key set"
    (doseq [request [(assoc (cached-request "extra" "accepted") :args [])
                     (dissoc (cached-request "missing" "accepted") :shard)]]
      (let [error (try
                    (worker/validate-request! request expected)
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (= "WARM-REQUEST-SHAPE" (:id (ex-data error)))))))
  (testing "unknown and non-string shard values fail closed"
    (doseq [shard ["missing" :accepted]]
      (let [error (try
                    (worker/validate-request!
                     (cached-request "bad-shard" shard) expected)
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (= "WARM-CACHED-SHARD" (:id (ex-data error))))
        (is (= shard (:shard (ex-data error))))))))

(deftest cached-request-identity-remains-tree-bound
  (doseq [[field actual]
          [[:candidate-commit tree]
           [:candidate-tree commit]]]
    (let [error (try
                  (worker/validate-request!
                   (assoc (cached-request "identity" "accepted") field actual)
                   expected)
                  nil
                  (catch clojure.lang.ExceptionInfo error error))]
      (is (= "WARM-TREE-MISMATCH" (:id (ex-data error))))
      (is (= field (:field (ex-data error))))
      (is (= actual (:actual (ex-data error)))))))

(deftest cached-dispatch-preserves-raw-result-and-response-shape
  (let [emitted (atom []) exact-calls (atom []) cached-calls (atom [])
        line (pr-str (cached-request "cached" "accepted"))
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
        lines [(pr-str (cached-request "first" "accepted"))
               (pr-str (cached-request "never" "contract"))]
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
