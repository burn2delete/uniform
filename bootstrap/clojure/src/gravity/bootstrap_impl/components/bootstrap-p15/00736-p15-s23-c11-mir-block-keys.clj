

(def p15-s23-c11-mir-block-keys
  #{:artifact :block-id :arguments :instructions :terminator
    :predecessors :successors :dominators :data-flow :source})

(def p15-s23-c11-mir-operation-keys
  #{:artifact :op-id :opcode :source-operation :operands :result
    :result-kind :constant-payload :type :effects :capabilities :ordering :source
    :profile :facts :domain-anchor :block-id :verifier-status})

(def p15-s23-c11-mir-terminator-keys
  #{:artifact :terminator-id :kind :operands :successors :effects
    :ordering :source :profile :facts :verifier-status})

(defn p15-s23-c11-mir-validate-module-envelope!
  [source-path checked-core mir]
  (gravity.mir/validate-module-envelope!
   {:entry-parameter! #'p15-s23-c11-mir-entry-parameter!
    :module-keys p15-s23-c11-mir-module-keys
    :function-keys p15-s23-c11-mir-function-keys
    :require! #'p15-s23-c11-mir-require!
    :require-strict-structure! #'p15-s23-c11-mir-require-strict-structure!
    :pass-contract-record #'p15-s23-c11-mir-pass-contract-record}
   source-path checked-core mir))
