(def ^:private __gravity_bootstrap_checked_core_source_helper
  (let [static-rebuild-token (nth __gravity_bootstrap_checked_core_authority_values 0)
      __gravity_bootstrap_checked_core_source_request_and_module_admission __gravity_bootstrap_checked_core_source_request_and_module_admission
      __gravity_bootstrap_checked_core_authoritative_front_end_records __gravity_bootstrap_checked_core_authoritative_front_end_records
      __gravity_bootstrap_checked_core_source_surface_and_metadata_validation __gravity_bootstrap_checked_core_source_surface_and_metadata_validation
      __gravity_bootstrap_checked_core_c7_literal_and_arity_admission __gravity_bootstrap_checked_core_c7_literal_and_arity_admission
      __gravity_bootstrap_checked_core_stage2_plan_and_front_end_preflight __gravity_bootstrap_checked_core_stage2_plan_and_front_end_preflight
      __gravity_bootstrap_checked_core_function_entrypoint_and_core_shape __gravity_bootstrap_checked_core_function_entrypoint_and_core_shape
      __gravity_bootstrap_checked_core_core_node_mapping __gravity_bootstrap_checked_core_core_node_mapping
      __gravity_bootstrap_checked_core_effect_and_capability_admission __gravity_bootstrap_checked_core_effect_and_capability_admission
      __gravity_bootstrap_checked_core_capability_authority_and_runtime_context __gravity_bootstrap_checked_core_capability_authority_and_runtime_context
      __gravity_bootstrap_checked_core_fact_tables_and_semantic_inputs __gravity_bootstrap_checked_core_fact_tables_and_semantic_inputs
      __gravity_bootstrap_checked_core_runtime_execution_evidence __gravity_bootstrap_checked_core_runtime_execution_evidence
      __gravity_bootstrap_checked_core_source_and_authenticated_input __gravity_bootstrap_checked_core_source_and_authenticated_input
      __gravity_bootstrap_checked_core_mapping_and_provenance_bindings __gravity_bootstrap_checked_core_mapping_and_provenance_bindings
      __gravity_bootstrap_checked_core_artifact_identity_and_finalization __gravity_bootstrap_checked_core_artifact_identity_and_finalization
      __gravity_bootstrap_checked_core_artifact_validation_and_return __gravity_bootstrap_checked_core_artifact_validation_and_return]
  (fn p15-s23-stage2-closed-checked-core-source-artifact-internal [source-path
                                                                   source-text
                                                                   requested-target
                                                                   authority-record
                                                                   construction-mode
                                                                   static-execution-evidence
                                                                   static-rebuild-token-candidate]
    (when-not (or
                (and
                  (= :authoritative-artifact-construction construction-mode)
                  (nil? static-execution-evidence)
                  (nil? static-rebuild-token-candidate))
                (and
                  (= :static-verification-rebuild construction-mode)
                  (map? static-execution-evidence)
                  (identical? static-rebuild-token static-rebuild-token-candidate)))
      (p15-s23-closed-core-fail!
        "C8-CAPABILITY"
        source-path
        {:requested-target requested-target}
        {:missing-fact :opaque-successful-verification-replay-static-rebuild-token}))
    (try
      (let [state (hash-map
                    'source-path
                    source-path
                    'source-text
                    source-text
                    'requested-target
                    requested-target
                    'authority-record
                    authority-record
                    'construction-mode
                    construction-mode
                    'static-execution-evidence
                    static-execution-evidence
                    'static-rebuild-token-candidate
                    static-rebuild-token-candidate)
            state (__gravity_bootstrap_checked_core_source_request_and_module_admission
                    state)
            state (__gravity_bootstrap_checked_core_authoritative_front_end_records
                    state)
            state (__gravity_bootstrap_checked_core_source_surface_and_metadata_validation
                    state)
            state (__gravity_bootstrap_checked_core_c7_literal_and_arity_admission
                    state)
            state (__gravity_bootstrap_checked_core_stage2_plan_and_front_end_preflight
                    state)
            state (__gravity_bootstrap_checked_core_function_entrypoint_and_core_shape
                    state)
            state (__gravity_bootstrap_checked_core_core_node_mapping state)
            state (__gravity_bootstrap_checked_core_effect_and_capability_admission
                    state)
            state (__gravity_bootstrap_checked_core_capability_authority_and_runtime_context
                    state)
            state (__gravity_bootstrap_checked_core_fact_tables_and_semantic_inputs
                    state)
            state (__gravity_bootstrap_checked_core_runtime_execution_evidence state)
            state (__gravity_bootstrap_checked_core_source_and_authenticated_input
                    state)
            state (__gravity_bootstrap_checked_core_mapping_and_provenance_bindings
                    state)
            state (__gravity_bootstrap_checked_core_artifact_identity_and_finalization
                    state)]
        (__gravity_bootstrap_checked_core_artifact_validation_and_return state))
      (catch
        StackOverflowError
        error
        (p15-s23-closed-core-fail!
          "C6-VERIFY"
          source-path
          {:missing-fact :host-stack-containment}
          {:contained-host-error (.getName (class error)),
           :maximum-plan-nodes p15-s23-closed-core-max-plan-nodes,
           :maximum-plan-depth p15-s23-closed-core-max-plan-depth}))
      (catch clojure.lang.ExceptionInfo ex (throw ex))
      (catch
        Exception
        error
        (p15-s23-closed-core-fail!
          "C6-VERIFY"
          source-path
          {:missing-fact :contained-host-failure}
          {:contained-host-error (str "sha256:" (sha256-hex (.getName (class error)))),
           :cause-message-hash
           (str "sha256:" (sha256-hex (or (.getMessage error) "")))}))))))
