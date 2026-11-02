(defn p15-s23-b3-llvm-reconstructed-lowering
  [mir]
  (gravity.backend.llvm/reconstructed-lowering #'p15-s23-b3-llvm-block-order #'p15-s23-b3-llvm-operation-sequence #'p15-s23-b3-llvm-block-labels #'p15-s23-b3-llvm-value-reference #'p15-s23-b3-llvm-operation-line #'p15-s23-b3-llvm-evaluate-operations #'p15-s23-b3-llvm-scalar-exit-code #'p15-s23-b3-llvm-policy #'p15-s23-b3-llvm-source-lowering-policy mir))

(defn- p15-s23-b3-llvm-invoke-builder!
  [candidate binding mir source-path]
  (p15-s23-b3-llvm-require-authority!
   candidate source-path :execute-pinned-gravity-b3-builder)
  (let [result
        (try
          (p15-s23-stage2-runtime-execute-function
           {:engine :gravity-b3-pinned-builder-host-runner
            :compiler-artifact-plan? true}
           (:plan binding)
           p15-s23-b3-llvm-builder-function
           [mir])
          (catch StackOverflowError error
            (p15-s23-b3-llvm-fail!
             "B1-UNSUPPORTED" source-path {}
             {:missing-fact :bounded-gravity-b3-builder-host-stack})))]
    (when (= :rejected (:status result))
      (p15-s23-b3-llvm-fail!
       (or (:diagnostic result) "B1-UNSUPPORTED") source-path
       {:op-id (:operation-id result)}
       {:missing-fact (:missing-fact result)
        :operation-id (:operation-id result)
        :opcode (:opcode result)
        :source-operation (:source-operation result)
        :observed-type (:type result)}))
    (let [expected (p15-s23-b3-llvm-reconstructed-lowering mir)]
      (when-not (= expected result)
        (p15-s23-b3-llvm-fail!
         "B3-PASS" source-path result
         {:missing-fact :independent-gravity-lowering-reconstruction}))
      (when-not (and (<= 0 (:expected-exit-code result) 255)
                     (not (re-find
                           #"(?i)(undef|poison|\bnsw\b|\bnuw\b|inbounds|!tbaa|!range)"
                           (:llvm-ir result))))
        (p15-s23-b3-llvm-fail!
         "B3-UB" source-path result
         {:missing-fact :conservative-defined-llvm-subset}))
      result)))
