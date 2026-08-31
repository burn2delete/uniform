(defn p15-s23-closed-runtime-packet-authentic?
  "Authenticate a packet against source bytes supplied by its trusted caller.\n\n  Packet-local hashes only prove internal consistency, so the one-argument\n  form fails closed.  The context form independently recompiles the trusted\n  source with the pinned Gravity emitter and reloads the pinned Gravity runtime\n  before it validates identities or performs the verification replay."
  ([packet] false)
  ([packet context]
    (try
      (let [state (hash-map 'packet packet 'context context)
            state (__gravity_bootstrap_packet_authentic_bounded_ingress_phase_01 state)
            state (__gravity_bootstrap_packet_authentic_bounded_ingress_phase_02
                    state)]
        (if-not (__gravity_bootstrap_packet_authentic_trusted_rebuild_preflight state)
          false
          (let [state state
                state (__gravity_bootstrap_packet_authentic_trusted_rebuild_phase_01
                        state)]
            (if-not (__gravity_bootstrap_packet_authentic_source_and_rule_trust state)
              false
              (let [state state
                    state (__gravity_bootstrap_packet_authentic_verification_replay_phase_01
                            state)]
                (and
                  (__gravity_bootstrap_packet_authentic_schema_hash_cross_links_01
                    state)))))))
      (catch StackOverflowError _ false)
      (catch Exception _ false))))
