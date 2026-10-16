(ns gravity.self-hosting.sh07-b51-attempt24-runtime-machine-test
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-b51-attempt24-plan-linker-test
             :as linker]))

(use-fixtures :each
  (fn [test-function]
    (linker/with-current-emitter-pins test-function)))

(defn linked-runtime []
  (bootstrap/p15-s23-sh07-b51-runtime-context
   @linker/authentic-linked-products {}))

(defn authentic-machine-plan []
  (first (:module-plans @linker/authentic-linked-products)))

(defn authentic-edge-zero-instruction []
  (let [plan (nth (:module-plans @linker/authentic-linked-products) 1)]
    (first
     (for [instruction
           (tree-seq coll? seq
                     (get-in plan
                             [:functions
                              'sh07-b51-all-owner-numbered-preimage-rows
                              :instructions]))
           :when (= 0 (get-in instruction
                              [:external-link :edge-ordinal]))]
       instruction))))

(deftest public-dispatcher-authenticates-a-real-m3-route-before-arguments
  (let [products @linker/authentic-linked-products
        caller-plan (nth (:module-plans products) 1)
        instruction (authentic-edge-zero-instruction)
        runtime (bootstrap/p15-s23-sh07-b51-runtime-context products {})]
    (is (= [1 2]
           (bootstrap/p15-s23-stage2-runtime-execute-instruction
            runtime caller-plan
            {'result [1] 'built {:rows [2]}}
            instruction)))
    (is (= 1 (count @(:sh07-b51-external-call-trace runtime))))
    (is (= 0 (:edge-ordinal
              (first @(:sh07-b51-external-call-trace runtime)))))))

(deftest real-route-mutation-rejects-before-a-missing-local-is-evaluated
  (let [products @linker/authentic-linked-products
        caller-plan (nth (:module-plans products) 1)
        instruction (authentic-edge-zero-instruction)
        bundle (get products :execution-bundle)
        mutated-base
        (-> bundle
            (dissoc :bundle-id)
            (assoc-in [:routes 0 :caller-plan-id]
                      "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
        mutated (assoc mutated-base :bundle-id
                       (bootstrap/p15-s23-sh07-b51-canonical-id mutated-base))
        runtime
        (bootstrap/p15-s23-sh07-b51-runtime-context
         (assoc products :execution-bundle mutated) {})]
    (try
      (bootstrap/p15-s23-stage2-runtime-execute-instruction
       runtime caller-plan {} instruction)
      (is false "mutated M3 route must reject")
      (catch clojure.lang.ExceptionInfo error
        (is (= "L2-UNKNOWN-CORE-FORM" (:id (ex-data error))))
        (is (= :unauthenticated-b51-external-link
               (:reason (ex-data error))))))
    (is (empty? @(:sh07-b51-external-call-trace runtime)))))

(deftest authentic-external-callee-loop-does-not-grow-the-host-stack
  (let [products @linker/authentic-linked-products
        caller-plan (nth (:module-plans products) 1)
        instruction (authentic-edge-zero-instruction)
        runtime (bootstrap/p15-s23-sh07-b51-runtime-context products {})
        rows (vec (range 5000))]
    (is (= rows
           (bootstrap/p15-s23-stage2-runtime-execute-instruction
            runtime caller-plan {'result [] 'built {:rows rows}}
            instruction)))))

(deftest public-root-coordinator-admits-augmentation-only-through-pinned-m3
  (linker/with-current-emitter-pins
    (fn []
      (let [products @linker/authentic-linked-products
            poisoned-runtime
            {:sh07-b51-runtime-context-token :caller-forged
             :sh07-b51-module-source-descriptors [:caller-forged]
             :sh07-b51-link-manifest {:caller :forged}
             :sh07-b51-module-plans [:caller-forged]
             :sh07-b51-execution-bundle {:bundle-id :caller-forged}}]
        (let [result
              (bootstrap/p15-s23-sh07-b51-execute-public-root
               8 [nil] poisoned-runtime)]
          (is (map? result))
          (is (= :gravity/sh07-b51-entrypoint-result-v18
                 (:artifact result)))
          (is (= :boundary-rejected (:status result))))))))

(deftest pure-data-predicates-do-not-admit-an-m3-plan-context
  (let [plan {:module {:module 'ordinary.data}}
        runtime {:sh07-b51-execution-bundle {:bundle-id :caller-forged}}
        invoke
        (fn [candidate-runtime candidate-plan]
          (bootstrap/p15-s23-stage2-runtime-invoke-builtin
           candidate-runtime candidate-plan 'map? [{}]))]
    (doseq [[candidate-runtime candidate-plan]
            [[runtime plan]
             [{} plan]
             [runtime (assoc-in plan [:module :module] 'caller.forged)]
             [(assoc-in runtime
                        [:sh07-b51-execution-bundle :bundle-id]
                        "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
              plan]]]
      (is (true? (invoke candidate-runtime candidate-plan)))
      (is (false? (bootstrap/p15-s23-stage2-compiler-artifact-plan-context? candidate-plan)))
      (is (false? (bootstrap/p15-s23-stage2-pinned-augmented-plan-authorized? candidate-plan))))))

(deftest gravity-runtime-source-owns-the-bounded-machine-contract
  (let [path "bootstrap/gravity/p15_s23/runtime.gravity"
        forms (bootstrap/read-forms path (slurp path))
        contract-form
        (first (filter #(and (seq? %) (= 'def (first %))
                             (= 'p15-s23-runtime-b51-machine-contract
                                (second %))) forms))
        contract (nth contract-form 2)
        names (set (keep #(when (and (seq? %) (= 'defn (first %)))
                            (second %)) forms))]
    (is (= :gravity/sh07-b51-runtime-machine-contract-v1
           (:artifact contract)))
    (is (= 8388608 (:maximum-steps contract)))
    (is (= 4096 (:maximum-frames contract)))
    (is (= 65536 (:maximum-external-calls contract)))
    (is (= :before-argument-evaluation
           (:external-route-authentication contract)))
    (is (= :forbidden (:host-recursion contract)))
    (is (contains? names 'p15-s23-runtime-b51-machine-state))))
