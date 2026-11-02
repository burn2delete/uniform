(defn p15-s23-b3-llvm-reconstructed-lowering
  [mir]
  (let [function (get-in mir [:functions 'main])
        block-order (p15-s23-b3-llvm-block-order mir function)
        operations
        (p15-s23-b3-llvm-operation-sequence function block-order)
        operation-index
        (into {} (map-indexed (fn [index operation]
                               [(:op-id operation) index])) operations)
        block-labels (p15-s23-b3-llvm-block-labels block-order)
        operation-records
        (mapv
         (fn [operation]
           {:mir-operation-id (:op-id operation)
            :mir-block-id (:block-id operation)
            :source-operation (:source-operation operation)
            :mir-opcode (:opcode operation)
            :mir-type (:type operation)
            :llvm-type :i64
            :llvm-value
            (p15-s23-b3-llvm-value-reference
             operation-index (:op-id operation))
            :llvm-instruction
            (p15-s23-b3-llvm-operation-line
             mir operation operations operation-index block-labels)})
         operations)
        records-by-block (group-by :mir-block-id operation-records)
        block-records
        (mapv
         (fn [block-id]
           (let [block (get-in function [:blocks block-id])
                 terminator (:terminator block)
                 kind (:kind terminator)
                 operands (:operands terminator)
                 successors (:successors terminator)
                 terminator-lines
                 (case kind
                   :conditional-branch
                   [(str "  %branchcond = icmp ne i64 "
                         (p15-s23-b3-llvm-value-reference
                          operation-index (first operands)) ", 0")
                    (str "  br i1 %branchcond, label %"
                         (get block-labels (first successors))
                         ", label %"
                         (get block-labels (second successors)))]
                   :branch
                   [(str "  br label %"
                         (get block-labels (first successors)))]
                   :return
                   [(str "  %exit = trunc i64 "
                         (p15-s23-b3-llvm-value-reference
                          operation-index (first operands)) " to i32")
                    "  ret i32 %exit"])]
             {:mir-block-id block-id
              :llvm-label (get block-labels block-id)
              :operation-lines
              (mapv :llvm-instruction (get records-by-block block-id []))
              :terminator-lines terminator-lines}))
         block-order)
        blocks-text
        (apply str
               (map (fn [block]
                      (str (:llvm-label block) ":\n"
                           (str/join "" (map #(str % "\n")
                                             (:operation-lines block)))
                           (str/join "" (map #(str % "\n")
                                             (:terminator-lines block)))))
                    block-records))
        llvm-ir
        (str "; Gravity authenticated B3 bounded LLVM\n"
             "source_filename = \"gravity-b3-internal\"\n"
             "target datalayout = \"" (:data-layout p15-s23-b3-llvm-policy)
             "\"\n"
             "target triple = \"" (:target-triple p15-s23-b3-llvm-policy)
             "\"\n\n"
             "define ccc i32 @main() {\n" blocks-text "}\n")
        values (p15-s23-b3-llvm-evaluate-operations operations)
        return-id
        (first (get-in function
                       [:blocks (last block-order) :terminator :operands]))
        semantic-result (get values return-id)]
    {:artifact :gravity/b3-bounded-llvm-x86_64-linux-lowering
     :schema-version 1
     :status :constructed-unverified
     :policy p15-s23-b3-llvm-source-lowering-policy
     :target :llvm-x86_64-linux
     :canonical-target :llvm-x86_64-linux
     :backend :llvm
     :profile :hosted
     :executable-carrier-kind
     :gravity/b3-llvm-x86_64-linux-elf-emission
     :final-artifact-kind
     :gravity/p15-s23-b3-authenticated-llvm-x86_64-linux-artifact
     :source-mir-id (:module-id mir)
     :block-order block-order
     :block-labels block-labels
     :operation-index operation-index
     :operation-records operation-records
     :block-records block-records
     :llvm-ir llvm-ir
     :semantic-result semantic-result
     :expected-exit-code
     (p15-s23-b3-llvm-scalar-exit-code semantic-result)
     :runtime-providers
     [:linux/process-startup :linux/elf-loader :linux/glibc-2.36]
     :build-providers
     [:llvm-clang-20.1.8 :llvm-llc-20.1.8 :llvm-opt-20.1.8
      :llvm-as-20.1.8 :llvm-dis-20.1.8 :llvm-readobj-20.1.8
      :llvm-objdump-20.1.8 :llvm-lld-20.1.8
      :linux-x86_64-process-loader]
     :emitted-metadata []
     :emitted-function-attributes []
     :proof-to-metadata-map {}
     :ub-sensitive-flags []
     :verified-input-closure
     {:status :pending-host-content-binding
      :c11-full-mir-authenticity :required-upstream-contextual-verifier
      :b3-revalidated-surface :lowering-relevant-closure
      :full-dfg-edge-use-replay :upstream-c11-owned
      :source-core (:source-core mir)
      :pass-execution-record-id
      (get-in mir [:pass-execution-record :record-id])
      :type-fact-count (count (:type-table mir))
      :effect-fact-count (count (:effect-table mir))
      :safety-fact-count (count (:safety-table mir))
      :source-map-count (count (:source-map mir))}
     :diagnostics []
     :clojure-seed-boundary? true
     :self-hosted? false
     :public? false
     :release? false
     :whole-b3? false
     :claims {:clojure-seed-boundary? true
              :public? false :release? false
              :self-hosted? false :whole-b3? false}}))

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
