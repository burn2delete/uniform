(doseq [temporary '[__gravity_bootstrap_packet_authentic_bounded_ingress_phase_01
                    __gravity_bootstrap_packet_authentic_bounded_ingress_phase_02
                    __gravity_bootstrap_packet_authentic_trusted_rebuild_preflight
                    __gravity_bootstrap_packet_authentic_trusted_rebuild_phase_01
                    __gravity_bootstrap_packet_authentic_source_and_rule_trust
                    __gravity_bootstrap_packet_authentic_verification_replay_phase_01
                    __gravity_bootstrap_packet_authentic_schema_hash_cross_links_01]]
  (ns-unmap (the-ns 'gravity.bootstrap) temporary))
