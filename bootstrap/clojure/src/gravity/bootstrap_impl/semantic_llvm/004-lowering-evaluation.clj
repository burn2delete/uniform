(defn p15-s23-b3-llvm-evaluate-operations
  [operations]
  (gravity.backend.llvm/evaluate-operations #'p15-s23-b3-llvm-forward-opcodes operations))

(defn p15-s23-b3-llvm-scalar-exit-code
  [value]
  (gravity.backend.llvm/scalar-exit-code value))
