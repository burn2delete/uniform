(defn p15-s23-closed-core-validate-structure!
  ([source-path artifact]
    (p15-s23-closed-core-validate-structure! source-path artifact :pure nil))
  ([source-path artifact mode expected-authority-evidence]
    (p15-s23-closed-core-bounded-value! source-path artifact)
    (p15-s23-closed-core-validate-input-shape! source-path artifact mode)
    (let [state (hash-map
                  'source-path
                  source-path
                  'artifact
                  artifact
                  'mode
                  mode
                  'expected-authority-evidence
                  expected-authority-evidence)
          state (__gravity_bootstrap_closed_core_structure_ingress_envelope_and_facts_01
                  state)
          state (__gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_01
                  state)
          state (__gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_02
                  state)
          state (__gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_03
                  state)
          state (__gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_04
                  state)
          state (__gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_05
                  state)
          state (__gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_06
                  state)
          state (__gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_07
                  state)
          state (__gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_08
                  state)]
      (__gravity_bootstrap_closed_core_structure_validation_status state))))
