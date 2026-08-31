(doseq [temporary '[__gravity_bootstrap_gate_b_transaction_phase_01
                    __gravity_bootstrap_gate_b_transaction_phase_02
                    __gravity_bootstrap_gate_b_transaction_phase_03
                    __gravity_bootstrap_gate_b_transaction_validation
                    __gravity_bootstrap_gate_b_transaction_result]]
  (ns-unmap (the-ns 'gravity.bootstrap) temporary))
