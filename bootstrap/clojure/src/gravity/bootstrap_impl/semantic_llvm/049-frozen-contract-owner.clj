(defn-
 p15-s23-b3-llvm-frozen-contract-valid?
 [artifact]
 (let
  [base-state
   (semantic-llvm-frozen-contract-base-context artifact)
   build-state
   (semantic-llvm-frozen-contract-build-context artifact base-state)
   frozen-state
   (semantic-llvm-frozen-contract-path-context artifact build-state)]
  (and
   (semantic-llvm-frozen-contract-section-01 artifact frozen-state)
   (semantic-llvm-frozen-contract-section-02 artifact frozen-state)
   (semantic-llvm-frozen-contract-section-03 artifact frozen-state)
   (semantic-llvm-frozen-contract-section-04 artifact frozen-state)
   (semantic-llvm-frozen-contract-section-05 artifact frozen-state)
   (semantic-llvm-frozen-contract-section-06 artifact frozen-state)
   (semantic-llvm-frozen-contract-section-07 artifact frozen-state)
   (semantic-llvm-frozen-contract-section-08 artifact frozen-state)
   (semantic-llvm-frozen-contract-section-09 artifact frozen-state))))
