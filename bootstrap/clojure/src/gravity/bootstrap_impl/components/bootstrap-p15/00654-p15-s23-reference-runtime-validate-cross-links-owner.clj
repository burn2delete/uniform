(defn p15-s23-reference-runtime-validate-cross-links! [source-path
                                                       target
                                                       definitions
                                                       authoritative-module
                                                       derived]
  (let [state (hash-map
                'source-path
                source-path
                'target
                target
                'definitions
                definitions
                'authoritative-module
                authoritative-module
                'derived
                derived)
        state (__gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_runtime_contract_policy
                state)
        state (__gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_function_effect_graph
                state)
        state (__gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_capability_authority
                state)
        state (__gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_expected_cross_links
                state)
        state (__gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_cross_link_validation_01
                state)
        state (__gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_cross_link_validation_02
                state)
        state (__gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_cross_link_validation_03
                state)
        state (__gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_cross_link_validation_04
                state)]
    (__gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_final_result
      state)))
