(defn c-backend-stage2-runtime-source-rule!
  "Load the Gravity-authored P15-S23 runtime executor and kernel rules.\n\n  Runtime-derived lowering is only authoritative for this slice when the\n  emitted stage2 plan is executed through the source-defined runtime rule\n  record.  Keep the source rule hashes path-neutral; the resolved source path\n  remains a provenance field on the returned binding for auditability."
  [source-path target]
  (let [state (hash-map 'source-path source-path 'target target)
        state (__gravity_bootstrap_runtime_source_rule_compiler_source_pinning_bindings_01
                state)
        state (__gravity_bootstrap_runtime_source_rule_compiler_source_pinning_expressions_02
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_kernel_definitions_bindings_03
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_kernel_definitions_expressions_04
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_rule_linkage_bindings_05
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_rule_linkage_expressions_06
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_artifact_contract_bindings_07
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_artifact_contract_bindings_08
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_artifact_contract_bindings_09
                state)]
    (__gravity_bootstrap_runtime_source_rule_final_runtime_rule_record state)))
