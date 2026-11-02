(ns gravity.build.parent-producer-observation-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.build.parent-producer-observation :as parent]
            [gravity.self-hosting.sh01-parallel-test-runner :as runner]))

(deftest raw-token-and-terminal-facts-remain-separate
  ;; No Process, process probe, compiler or producer is executed by this control.
  (doseq [result [{:root-exited? true :tracked-owned-processes-terminal? true
                  :cleanup-complete? nil :descendant-coverage :unknown}
                 {:root-exited? false :tracked-owned-processes-terminal? false
                  :cleanup-complete? false :descendant-coverage :unknown
                  :timeout-containment-unproven? true}]]
    (let [process (Object.) token (Object.) handle (Object.) events (atom [])
          options {:timeout-ms 120000 :stdout-limit-bytes 65536}]
      (with-redefs [runner/retain-owned-process!
                    (fn [p] (is (identical? process p))
                      (swap! events conj :retain) token)
                    runner/process-result
                    (fn [p opts owned]
                      (is (identical? process p)) (is (= options opts))
                      (is (identical? token owned))
                      (swap! events conj :wait-capture-finalize) result)
                    runner/retained-process-handles
                    (fn [owned] (is (identical? token owned))
                      (swap! events conj :handles) [handle])
                    runner/finalize-owned-process!
                    (fn [& _] (throw (AssertionError. "duplicate finalization")))]
        (let [observed (parent/observe! process options)]
          (is (= [:retain :wait-capture-finalize :handles] @events))
          (is (identical? token (:owned-process observed)))
          (is (identical? handle (first (:retained-handles observed))))
          (is (identical? result (:process-result observed)))
          (doseq [key [:root-exited? :tracked-owned-processes-terminal?
                       :cleanup-complete? :descendant-coverage]]
            (is (= (get result key) (get observed key))))
          (is (= :unresolved (:selected-workload-reachability observed)))
          (is (false? (:capture-v1-ready? observed)))
          (is (false? (:cache-admission? observed))))))))

(deftest original-wait-throwable-is-not-retried-or-replaced
  (let [token (Object.) original (InterruptedException. "injected wait")
        calls (atom 0)]
    (with-redefs [runner/retain-owned-process! (constantly token)
                  runner/process-result
                  (fn [_ _ owned]
                    (is (identical? token owned)) (swap! calls inc) (throw original))
                  runner/retained-process-handles
                  (fn [& _] (throw (AssertionError. "post-failure observation")))
                  runner/finalize-owned-process!
                  (fn [& _] (throw (AssertionError. "duplicate finalization")))]
      (is (identical? original (try (parent/observe! (Object.) {})
                                    (catch Throwable error error))))
      (is (= 1 @calls)))))
