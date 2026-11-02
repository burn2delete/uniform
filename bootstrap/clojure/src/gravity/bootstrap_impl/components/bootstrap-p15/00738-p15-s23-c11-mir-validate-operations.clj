

(defn p15-s23-c11-mir-validate-operations!
  [source-path checked-core mir]
  (gravity.mir/validate-operations!
   {:entry-parameter! #'p15-s23-c11-mir-entry-parameter!
    :operation-sequence #'p15-s23-c11-mir-operation-sequence
    :runtime-check-operation-id #'p15-s23-c11-mir-runtime-check-operation-id
    :expected-block-operation-ids #'p15-s23-c11-mir-expected-block-operation-ids
    :require! #'p15-s23-c11-mir-require!
    :block-keys p15-s23-c11-mir-block-keys
    :node-facts #'p15-s23-c11-mir-node-facts
    :operation-keys p15-s23-c11-mir-operation-keys
    :node-opcode #'p15-s23-c11-mir-node-opcode
    :allowed-opcodes p15-s23-c11-mir-allowed-opcodes
    :allowed-source-operations p15-s23-c11-mir-allowed-source-operations
    :node-operands #'p15-s23-c11-mir-node-operands
    :expected-runtime-check-operation #'p15-s23-c11-mir-expected-runtime-check-operation
    :set-union #'set/union}
   source-path checked-core mir))
