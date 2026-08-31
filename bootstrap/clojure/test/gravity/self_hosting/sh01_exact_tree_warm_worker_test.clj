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
                  {:exit 0 :elapsed-ns 10 :out "ok" :err ""})
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
                                    {:exit 1 :elapsed-ns 1 :out "" :err "failed"}) 8)))
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
                                    (fn [_] {:exit 0 :elapsed-ns 1 :out "" :err ""}) 8)))
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
                                 (print large)
                                 0)]
                   (#'worker/invoke-runner exact-args))]
      (is (= 0 (:exit result)))
      (is (= "" (:err result)))
      (is (<= (alength (.getBytes ^String (:out result) "UTF-8")) (* 256 1024)))
      (is (true? (:out-truncated? result)))
      (is (re-find #"Ran 1 tests" (:out result))))))

(deftest bounded-response-and-fatal-cancellation
  (testing "a response marks already-capped output as truncated"
    (let [emitted (atom [])
          oversized (apply str (repeat (inc (* 256 1024)) "x"))]
      (is (zero? (worker/run-session!
                  [(pr-str (request "large" exact-args))]
                  #(swap! emitted conj %) expected
                  #(assoc expected :clean? true)
                  (fn [_] {:exit 0 :elapsed-ns 1 :out oversized :err ""}) 1)))
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
