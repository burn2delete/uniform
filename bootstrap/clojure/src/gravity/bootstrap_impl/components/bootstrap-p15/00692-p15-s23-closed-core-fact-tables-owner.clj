(defn p15-s23-closed-core-fact-tables
  ([nodes] (p15-s23-closed-core-fact-tables nodes :not-applicable :not-applicable))
  ([nodes module plan-id]
    (p15-s23-closed-core-fact-tables nodes module plan-id :pure nil))
  ([nodes module plan-id mode authority-evidence]
    (let [state (hash-map
                  'nodes
                  nodes
                  'module
                  module
                  'plan-id
                  plan-id
                  'mode
                  mode
                  'authority-evidence
                  authority-evidence)
          state (__gravity_bootstrap_closed_core_fact_context_phase_01 state)]
      (into
        {}
        (concat
          (__gravity_bootstrap_closed_core_fact_table_typed_core_01 state)
          (__gravity_bootstrap_closed_core_fact_table_effect_graph_02 state)
          (__gravity_bootstrap_closed_core_fact_table_capability_proof_records_03
            state)
          (__gravity_bootstrap_closed_core_fact_table_pure_capability_closure_04 state)
          (__gravity_bootstrap_closed_core_fact_table_ownership_analysis_05 state)
          (__gravity_bootstrap_closed_core_fact_table_type_facts_06 state)
          (__gravity_bootstrap_closed_core_fact_table_effect_facts_07 state)
          (__gravity_bootstrap_closed_core_fact_table_capability_facts_08 state)
          (__gravity_bootstrap_closed_core_fact_table_ownership_facts_09 state)
          (__gravity_bootstrap_closed_core_fact_table_safety_facts_10 state)
          (__gravity_bootstrap_closed_core_fact_table_profile_facts_11 state))))))
