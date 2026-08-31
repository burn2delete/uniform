(defn- __gravity_bootstrap_runtime_source_rule_compiler_source_pinning_bindings_01 [state]
  (let [{:syms [source-path target]} state
        compiler-source (c-backend-resolve-p15-s23-compiler-source-path)]
    (assoc state 'compiler-source compiler-source)))
