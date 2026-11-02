

(defn p15-s23-c11-mir-downstream-ids
  [outside-nodes if-id]
  (gravity.mir/downstream-ids outside-nodes if-id))

(defn p15-s23-c11-mir-expected-block-operation-ids
  [checked-core]
  (gravity.mir/expected-block-operation-ids #'p15-s23-c11-mir-entry-parameter! #'p15-s23-c11-mir-runtime-check-operation-id #'p15-s23-c11-mir-path-prefix? checked-core))

(defn p15-s23-c11-mir-expected-data-flow
  [nodes operation-by-id]
  (gravity.mir/expected-data-flow #'p15-s23-c11-mir-runtime-check-token-id nodes operation-by-id))

(defn p15-s23-c11-mir-expected-uses
  [data-flow value-id]
  (gravity.mir/expected-uses data-flow value-id))

(defn p15-s23-c11-mir-expected-terminator-uses
  [checked-core]
  (gravity.mir/expected-terminator-uses checked-core))

(defn p15-s23-c11-mir-expected-terminator-value-uses
  [terminator-uses value-id]
  (gravity.mir/expected-terminator-value-uses terminator-uses value-id))

(defn p15-s23-c11-mir-expected-value
  ([node data-flow terminator-uses operation]
   (p15-s23-c11-mir-expected-value node data-flow terminator-uses operation nil))
  ([node data-flow terminator-uses operation parameter]
   (gravity.mir/expected-value #'p15-s23-c11-mir-node-opcode #'p15-s23-c11-mir-expected-uses #'p15-s23-c11-mir-expected-terminator-value-uses node data-flow terminator-uses operation parameter)))

(defn p15-s23-c11-mir-expected-runtime-check-value
  [node data-flow operation]
  (gravity.mir/expected-runtime-check-value #'p15-s23-c11-mir-runtime-check-token-id #'p15-s23-c11-mir-runtime-check-operation-id #'p15-s23-c11-mir-expected-uses #'p15-s23-c11-mir-runtime-check-source node data-flow operation))

(def p15-s23-c11-mir-module-keys gravity.mir/module-keys)

(def p15-s23-c11-mir-function-keys gravity.mir/function-keys)
