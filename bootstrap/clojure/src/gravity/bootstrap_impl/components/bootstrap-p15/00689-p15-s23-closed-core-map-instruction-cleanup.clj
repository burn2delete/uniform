(doseq [temporary '[__gravity_bootstrap_closed_core_literal_quote_and_local
                    __gravity_bootstrap_closed_core_builtin_call
                    __gravity_bootstrap_closed_core_println_effect
                    __gravity_bootstrap_closed_core_sequence_and_control]]
  (ns-unmap (the-ns 'gravity.bootstrap) temporary))
