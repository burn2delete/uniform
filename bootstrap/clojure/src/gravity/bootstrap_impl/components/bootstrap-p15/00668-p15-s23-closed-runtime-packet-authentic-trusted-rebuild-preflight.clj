(defn- __gravity_bootstrap_packet_authentic_trusted_rebuild_preflight [state]
  (let [{:syms
         [packet
          context
          _
          context-valid?
          packet-envelope-valid?
          candidate-runtime-rule-authentic?
          packet-plan
          derived-validation
          trusted-stage2-rule
          trusted-plan
          trusted-plan-validation
          trusted-driver-rule
          trusted-runtime-rule
          trusted-driver-record]} state]
    (and
      derived-validation
      trusted-plan
      trusted-plan-validation
      trusted-driver-rule
      trusted-runtime-rule
      trusted-driver-record)))
