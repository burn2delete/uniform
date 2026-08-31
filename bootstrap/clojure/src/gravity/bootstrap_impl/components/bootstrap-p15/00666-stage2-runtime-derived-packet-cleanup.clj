(doseq [temporary '[__gravity_bootstrap_runtime_packet_packet_construction_phase_01
                    __gravity_bootstrap_runtime_packet_packet_construction_phase_02
                    __gravity_bootstrap_runtime_packet_packet_construction_phase_03
                    __gravity_bootstrap_runtime_packet_packet_construction_phase_04
                    __gravity_bootstrap_runtime_packet_packet_identity]]
  (ns-unmap (the-ns 'gravity.bootstrap) temporary))
