(defn- __gravity_bootstrap_runtime_source_rule_runtime_rule_linkage_expressions_06 [state]
  (let [{:syms
         [source-path
          target
          compiler-source
          pinned-source
          source-data
          forms
          runtime
          kernel
          runtime-rule-record
          kernel-rule-record
          linked-kernel?]} state]
    (when-not (and
                (= :complete (:status runtime-rule-record))
                (= :complete (:status kernel-rule-record))
                linked-kernel?)
      (p15-s23-stage2-runtime-executor-fail!
        "P15S23X002"
        compiler-source
        {:runtime runtime-rule-record,
         :kernel kernel-rule-record,
         :linked-kernel? linked-kernel?}
        {:requested-source source-path,
         :target target,
         :missing-fact :stage2-runtime-rule-set,
         :runtime-rule-record runtime-rule-record,
         :kernel-rule-record kernel-rule-record,
         :linked-kernel? linked-kernel?}))
    state))
