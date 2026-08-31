(defn p15-s23-reference-runtime-adapter-validated-invoke [runtime-rule
                                                          function
                                                          args
                                                          authority
                                                          target-plan
                                                          plan-id
                                                          source-id
                                                          closed-plan-validation]
  (let [state (hash-map
                'runtime-rule
                runtime-rule
                'function
                function
                'args
                args
                'authority
                authority
                'target-plan
                target-plan
                'plan-id
                plan-id
                'source-id
                source-id
                'closed-plan-validation
                closed-plan-validation)
        state (__gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_authority_and_provider_setup
                state)
        state (__gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_request_decisions
                state)
        state (__gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_provider_actions
                state)
        state (__gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_rejection_boundaries
                state)
        state (__gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_transcript_and_evidence_validation_01
                state)
        state (__gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_transcript_and_evidence_validation_02
                state)]
    (__gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_final_result
      state)))
