(defn- __gravity_bootstrap_runtime_source_rule_compiler_source_pinning_expressions_02 [state]
  (let [{:syms [source-path target compiler-source]} state]
    (when-not (.isFile (java.io.File. compiler-source))
      (p15-s23-stage2-runtime-executor-fail!
        "P15S23X001"
        compiler-source
        nil
        {:requested-source source-path,
         :target target,
         :missing-fields [:compiler-source],
         :missing-fact :stage2-runtime-executor-source}))
    state))
