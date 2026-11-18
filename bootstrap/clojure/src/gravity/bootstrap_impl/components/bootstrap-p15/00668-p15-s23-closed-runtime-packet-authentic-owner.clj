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

(defn current-runtime-development-packet-authentic?
  "Independently rebuild and replay the narrow current-source C development
  packet. This schema is separate from the historical P15 reference packet."
  [packet context]
  (try
    (and
     (= :passed (:status (p15-s23-trusted-carrier-validation
                          context :default-only)))
     (= :passed (:status (p15-s23-trusted-carrier-validation
                          packet :default-only)))
     (map? context)
     (= #{:source-path :source-text :source-content-hash :requested-target}
        (set (keys context)))
     (string? (:source-path context))
     (string? (:source-text context))
     (= :c (:requested-target context))
     (= :valid (:status (p15-s23-closed-core-utf8-count (:source-text context))))
     (= (:source-content-hash context)
        (str "sha256:" (sha256-hex (:source-text context))))
     (map? packet)
     (= :gravity/current-source-runtime-development-packet (:kind packet))
     (= :complete (:status packet))
     (= :development-unadmitted (:revision-status packet))
     (= :not-checked (:effects packet))
     (true? (:clojure-seed-boundary? packet))
     (false? (:self-hosted? packet))
     (let [source-path (:source-path context)
           source-text (:source-text context)
           target (:requested-target context)
           validate-plan!
           (fn [candidate]
             (c-backend-validate-plan! source-path target candidate)
             (c-backend-validate-runtime-plan! source-path target candidate))
           rebuilt
           (stage2-runtime-derived-current-runtime-development-packet
            source-path source-text target
            {:validate-plan! validate-plan!})]
       (= (c-backend-canonical-value packet)
          (c-backend-canonical-value rebuilt))))
    (catch StackOverflowError _ false)
    (catch Exception _ false)))
