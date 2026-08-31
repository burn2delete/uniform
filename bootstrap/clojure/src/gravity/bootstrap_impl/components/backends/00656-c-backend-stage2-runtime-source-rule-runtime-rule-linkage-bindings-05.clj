(defn- __gravity_bootstrap_runtime_source_rule_runtime_rule_linkage_bindings_05 [state]
  (let [{:syms
         [source-path
          target
          compiler-source
          pinned-source
          source-data
          forms
          runtime
          kernel]} state
        runtime-rule-record (p15-s23-stage2-runtime-executor-rule-record runtime)
        kernel-rule-record (p15-s23-stage2-runtime-kernel-rule-record kernel)
        linked-kernel? (and
                         (= :p15-s23-stage2-runtime-kernel (:runtime-kernel runtime))
                         (= :gravity-stage2-runtime-kernel (:executed-by runtime))
                         (= :gravity-stage2-runtime-kernel-v1 (:engine kernel)))]
    (assoc
      state
      'runtime-rule-record
      runtime-rule-record
      'kernel-rule-record
      kernel-rule-record
      'linked-kernel?
      linked-kernel?)))
