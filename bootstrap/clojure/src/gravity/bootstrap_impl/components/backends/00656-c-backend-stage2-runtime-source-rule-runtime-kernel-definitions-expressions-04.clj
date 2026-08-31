(defn- __gravity_bootstrap_runtime_source_rule_runtime_kernel_definitions_expressions_04 [state]
  (let [{:syms
         [source-path
          target
          compiler-source
          pinned-source
          source-data
          forms
          runtime
          kernel]} state]
    (when-not (map? runtime)
      (p15-s23-stage2-runtime-executor-fail!
        "P15S23X001"
        compiler-source
        runtime
        {:requested-source source-path,
         :target target,
         :missing-fields [:p15-s23-stage2-runtime-executor],
         :missing-fact :stage2-runtime-executor-definition}))
    (when-not (map? kernel)
      (p15-s23-stage2-runtime-executor-fail!
        "P15S23X001"
        compiler-source
        kernel
        {:requested-source source-path,
         :target target,
         :missing-fields [:p15-s23-stage2-runtime-kernel],
         :missing-fact :stage2-runtime-kernel-definition}))
    state))
