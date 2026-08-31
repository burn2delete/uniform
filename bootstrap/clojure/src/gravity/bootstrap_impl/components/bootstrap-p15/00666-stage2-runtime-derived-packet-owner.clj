(defn stage2-runtime-derived-packet
  "Build the target-neutral, verified stage2 packet shared by executable\n  backends.  The packet owns source/front-end/plan/runtime authenticity and\n  differential execution; concrete backends only validate and emit their\n  target instruction subset.\n\n  `:jvm` remains the bootstrap seed target and may be explicitly overridden by\n  a backend lowering request.  A non-seed source target is a real constraint\n  and must equal the requested target.  The eligibility decision is carried as\n  evidence rather than erased or relabelled."
  ([source-path source-text requested-target]
    (stage2-runtime-derived-packet source-path source-text requested-target {}))
  ([source-path source-text requested-target {:keys [validate-plan!]}]
    (when-not (contains? stage2-runtime-derived-source-targets requested-target)
      (stage2-runtime-derived-packet-fail!
        "C14-UNSUPPORTED"
        "requested runtime-derived target is not supported"
        source-path
        requested-target
        nil
        {:requested-target requested-target,
         :supported-targets (vec (sort stage2-runtime-derived-source-targets)),
         :missing-fact :supported-runtime-derived-target}))
    (binding [*additional-bootstrap-targets* stage2-runtime-derived-source-targets]
      (let [state (hash-map
                    'source-path
                    source-path
                    'source-text
                    source-text
                    'requested-target
                    requested-target
                    'validate-plan!
                    validate-plan!)
            state (__gravity_bootstrap_runtime_packet_packet_construction_phase_01
                    state)
            state (__gravity_bootstrap_runtime_packet_packet_construction_phase_02
                    state)
            state (__gravity_bootstrap_runtime_packet_packet_construction_phase_03
                    state)
            state (__gravity_bootstrap_runtime_packet_packet_construction_phase_04
                    state)]
        (__gravity_bootstrap_runtime_packet_packet_identity state)))))
