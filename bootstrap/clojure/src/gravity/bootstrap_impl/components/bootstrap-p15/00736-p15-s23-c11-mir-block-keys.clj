

(def p15-s23-c11-mir-block-keys gravity.mir/block-keys)

(def p15-s23-c11-mir-operation-keys gravity.mir/operation-keys)

(def p15-s23-c11-mir-terminator-keys gravity.mir/terminator-keys)

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
