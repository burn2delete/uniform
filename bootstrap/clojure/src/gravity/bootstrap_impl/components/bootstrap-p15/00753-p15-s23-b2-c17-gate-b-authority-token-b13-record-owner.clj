(defn- p15-s23-b2-c17-gate-b-b13-record [gate-a transaction b14 c18]
  (let [state (hash-map 'gate-a gate-a 'transaction transaction 'b14 b14 'c18 c18)
        state (__gravity_bootstrap_gate_b_b13_identity_phase_01 state)
        state (__gravity_bootstrap_gate_b_b13_identity_phase_02 state)
        state (__gravity_bootstrap_gate_b_b13_identity_phase_03 state)]
    (__gravity_bootstrap_gate_b_b13_final_record state)))
