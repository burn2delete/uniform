(def p15-s23-b3-llvm-max-bridge-operations 256)

(defn p15-s23-b3-llvm-preflight!
  [artifact]
  (let [mir (:mir-module artifact)
        function (get-in mir [:functions 'main])
        block-order (when function
                      (p15-s23-b3-llvm-block-order mir function))
        operations (when function
                     (p15-s23-b3-llvm-operation-sequence
                      function block-order))]
    (when-not
     (and (= :gravity/p15-s23-c11-authenticated-mir-artifact
             (:kind artifact))
          (= :passed (:verification-status mir))
          (= :verified-mir-candidate-for-b1
             (get-in artifact [:b1-preflight :status]))
          (true? (:target-independent? mir))
          (= :hosted (:profile mir))
          (= :jvm (:source-target mir))
          (= :llvm-x86_64-linux (:target-request mir))
          (= #{'main} (set (keys (:functions mir))))
          (map? function)
          (contains? #{1 4} (count (:blocks function)))
          (empty? (:latent-effects function))
          (empty? (:capabilities function))
          (empty? (:runtime-check-table mir))
          (empty? (:domain-anchors mir))
          (empty? (:globals mir))
          (empty? (:diagnostics mir))
          (every? empty? (map :effects operations))
          (every? empty? (map :capabilities operations)))
      (p15-s23-b3-llvm-fail!
       "B1-INPUT" (get-in artifact [:provenance :actual-paths :source])
       artifact {:missing-fact :verified-pure-c11-b1-input
                 :c11-mir-id (:mir-id artifact)}))
    (when (> (count operations) p15-s23-b3-llvm-max-bridge-operations)
      (p15-s23-b3-llvm-fail!
       "B1-UNSUPPORTED"
       (get-in artifact [:provenance :actual-paths :source])
       artifact
       {:missing-fact :bounded-c13-c14-b1-operation-count
        :maximum-operation-count p15-s23-b3-llvm-max-bridge-operations
        :observed-operation-count (count operations)
        :c11-mir-id (:mir-id artifact)}))
    (when-let [operation
               (first (filter #(p15-s23-b3-llvm-operation-rejection
                                operations %
                                (p15-s23-b3-llvm-block-labels block-order))
                              operations))]
      (p15-s23-b3-llvm-fail!
       "B1-UNSUPPORTED"
       (get-in artifact [:provenance :actual-paths :source])
       operation
       {:missing-fact
        (p15-s23-b3-llvm-operation-rejection
         operations operation (p15-s23-b3-llvm-block-labels block-order))
        :operation-id (:op-id operation)
        :opcode (:opcode operation)
        :source-operation (:source-operation operation)
        :observed-type (:type operation)
        :c11-mir-id (:mir-id artifact)}))
    {:mir mir :function function :block-order block-order
     :operations operations}))

(defn p15-s23-b3-llvm-value-reference
  [operation-index operation-id]
  (gravity.backend.llvm/value-reference operation-index operation-id))

(defn p15-s23-b3-llvm-constant-number
  [value]
  (gravity.backend.llvm/constant-number value))

(defn p15-s23-b3-llvm-operation-line
  [mir operation operations operation-index block-labels]
  (gravity.backend.llvm/operation-line #'p15-s23-b3-llvm-value-reference #'p15-s23-b3-llvm-constant-number #'p15-s23-b3-llvm-forward-opcodes #'p15-s23-b3-llvm-comparison-opcodes mir operation operations operation-index block-labels))
