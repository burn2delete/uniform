(def p15-s23-b3-llvm-scalar-types gravity.backend.llvm/scalar-types)

(def p15-s23-b3-llvm-forward-opcodes gravity.backend.llvm/forward-opcodes)

(def p15-s23-b3-llvm-comparison-opcodes gravity.backend.llvm/comparison-opcodes)

(def p15-s23-b3-llvm-supported-opcodes gravity.backend.llvm/supported-opcodes)

(defn p15-s23-b3-llvm-signed-i64?
  [value]
  (gravity.backend.llvm/signed-i64? value))

(defn p15-s23-b3-llvm-block-order
  [mir function]
  (gravity.backend.llvm/block-order mir function))

(defn p15-s23-b3-llvm-operation-sequence
  [function block-order]
  (gravity.backend.llvm/operation-sequence function block-order))

(defn p15-s23-b3-llvm-block-labels
  [block-order]
  (gravity.backend.llvm/block-labels block-order))

(defn p15-s23-b3-llvm-operation-reference-allowed?
  [operation operand-id by-id position-by-id block-labels]
  (gravity.backend.llvm/operation-reference-allowed? operation operand-id by-id position-by-id block-labels))

(defn p15-s23-b3-llvm-operation-rejection
  ([operations operation]
   (gravity.backend.llvm/operation-rejection #'p15-s23-b3-llvm-operation-rejection #'p15-s23-b3-llvm-block-labels #'p15-s23-b3-llvm-supported-opcodes #'p15-s23-b3-llvm-signed-i64? #'p15-s23-b3-llvm-forward-opcodes #'p15-s23-b3-llvm-operation-reference-allowed? #'p15-s23-b3-llvm-scalar-types #'p15-s23-b3-llvm-comparison-opcodes operations operation))
  ([operations operation block-labels]
   (gravity.backend.llvm/operation-rejection #'p15-s23-b3-llvm-operation-rejection #'p15-s23-b3-llvm-block-labels #'p15-s23-b3-llvm-supported-opcodes #'p15-s23-b3-llvm-signed-i64? #'p15-s23-b3-llvm-forward-opcodes #'p15-s23-b3-llvm-operation-reference-allowed? #'p15-s23-b3-llvm-scalar-types #'p15-s23-b3-llvm-comparison-opcodes operations operation block-labels)))
