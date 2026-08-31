(ns gravity.self-hosting.sh01-exact-tree-warm-worker-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh01-exact-tree-warm-worker :as worker]))

(def commit "1111111111111111111111111111111111111111")
(def tree "2222222222222222222222222222222222222222")
(def expected {:candidate-commit commit :candidate-tree tree})

(defn request [id args]
  {:schema worker/request-schema :request-id id :operation :run-exact
   :candidate-commit commit :candidate-tree tree :args args})

(def exact-args
  ["--namespace" "gravity.c2-artifact-identity-test"
   "--exact" "canonical-reader-order-is-stable"])

(deftest requests-are-closed-exact-and-tree-bound
  (is (map? (worker/validate-request! (request "one" exact-args) expected)))
  (doseq [bad [(assoc (request "one" exact-args) :extra true)
               (assoc (request "one" exact-args) :candidate-tree commit)
               (request "one" [])
               (request "one" ["--namespace" "gravity.c2-artifact-identity-test"])
               (request "one" ["--namespace" "gravity.c2-artifact-identity-test"
                                "--regex" ".*"])]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (worker/validate-request! bad expected)))))

(deftest successful-requests-share-one-session
  (let [emitted (atom []) calls (atom [])
        lines (mapv (comp pr-str #(request % exact-args)) ["one" "two"])]
    (worker/run-session!
     lines #(swap! emitted conj %) expected
     #(assoc expected :clean? true)
     (fn [args]
       (swap! calls conj args)
       {:exit 0 :elapsed-ns 10 :out "ok" :err ""})
     8)
    (is (= [exact-args exact-args] @calls))
    (is (= [:passed :passed] (mapv :outcome @emitted)))
    (is (= [1 2] (mapv :request-count @emitted)))
    (is (every? #(false? (:authoritative? %)) @emitted))))

(deftest failure-or-tree-drift-terminates-reuse
  (testing "a failed request prevents a later request in the same JVM"
    (let [emitted (atom []) calls (atom 0)
          lines [(pr-str (request "bad" exact-args))
                 (pr-str (request "never" exact-args))]]
      (worker/run-session! lines #(swap! emitted conj %) expected
                           #(assoc expected :clean? true)
                           (fn [_] (swap! calls inc)
                             {:exit 1 :elapsed-ns 1 :out "" :err "failed"}) 8)
      (is (= 1 @calls))
      (is (= [:failed] (mapv :outcome @emitted)))))
  (testing "drift after execution rejects the result and terminates"
    (let [emitted (atom []) observations (atom 0)
          observe (fn []
                    (if (= 1 (swap! observations inc))
                      (assoc expected :clean? true)
                      (assoc expected :clean? false)))]
      (worker/run-session! [(pr-str (request "drift" exact-args))]
                           #(swap! emitted conj %) expected observe
                           (fn [_] {:exit 0 :elapsed-ns 1 :out "" :err ""}) 8)
      (is (= :tree-drift (:outcome (first @emitted))))
      (is (= 2 (:exit-code (first @emitted)))))))
