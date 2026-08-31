(defn- __gravity_bootstrap_packet_authentic_bounded_ingress_phase_02 [state]
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
          trusted-runtime-rule]} state
        trusted-driver-record (when trusted-runtime-rule
                                (try
                                  (binding [*additional-bootstrap-targets* stage2-runtime-derived-source-targets]
                                    (p15-s23-stage2-compiler-driver-run-source
                                      (:driver trusted-driver-rule)
                                      (:front-end trusted-driver-rule)
                                      (:emitter trusted-stage2-rule)
                                      (assoc
                                        (:runtime trusted-runtime-rule)
                                        :runtime-artifact-plan
                                        (:runtime-artifact-plan trusted-runtime-rule)
                                        :runtime-artifact-source-path
                                        (:runtime-artifact-source-path
                                          trusted-runtime-rule)
                                        :runtime-artifact-hash
                                        (:runtime-artifact-hash trusted-runtime-rule))
                                      (:source-path context)
                                      (:source-text context)))
                                  (catch Exception _ nil)))]
    (assoc state 'trusted-driver-record trusted-driver-record)))
