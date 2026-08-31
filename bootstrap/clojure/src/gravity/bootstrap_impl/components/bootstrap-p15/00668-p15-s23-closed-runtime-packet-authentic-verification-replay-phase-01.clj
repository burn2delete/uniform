(defn- __gravity_bootstrap_packet_authentic_verification_replay_phase_01 [state]
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
          trusted-driver-record
          validation
          execution
          invocation
          adapter-record
          runtime-rule
          emitter-rule
          driver-rule
          driver-record
          driver-plan
          normalize-plan
          packet-plan-base
          trusted-plan-base
          driver-plan-base
          normalize-comparison
          normalize-emitter-rule
          normalize-driver-rule
          normalize-runtime-rule
          raw-source-target
          source-target
          expected-target-eligibility]} state
        derived-plan-id (c4-artifact-id (c-backend-canonical-value packet-plan-base))
        derived-driver-plan-id (c4-artifact-id
                                 (c-backend-canonical-value driver-plan-base))
        runtime-artifact-plan (:runtime-artifact-plan runtime-rule)
        derived-runtime-artifact-hash (p15-s23-reference-runtime-hash
                                        (c-backend-stage2-runtime-artifact-hash-input
                                          runtime-artifact-plan
                                          (:runtime-artifact-authoritative-module
                                            runtime-rule)
                                          (:runtime-contract-definitions runtime-rule)
                                          (:runtime-artifact-function-hashes
                                            runtime-rule)
                                          (get-in
                                            runtime-rule
                                            [:runtime-contract-validation-record
                                             :derived-contract-facts])))
        derived-adapter-output (try
                                 (p15-s23-reference-runtime-adapter-invoke
                                   trusted-runtime-rule
                                   p15-s23-stage2-runtime-artifact-closed-plan-function
                                   [trusted-plan]
                                   (p15-s23-reference-runtime-authority
                                     trusted-plan
                                     trusted-plan-validation))
                                 (catch Exception _ nil))
        derived-execution (:result derived-adapter-output)
        derived-adapter-record (:adapter-record derived-adapter-output)
        execution-hash (str
                         "sha256:"
                         (sha256-hex (pr-str (c-backend-canonical-value execution))))
        stdout-hash (str "sha256:" (sha256-hex (:stdout execution)))]
    (assoc
      state
      'derived-plan-id
      derived-plan-id
      'derived-driver-plan-id
      derived-driver-plan-id
      'runtime-artifact-plan
      runtime-artifact-plan
      'derived-runtime-artifact-hash
      derived-runtime-artifact-hash
      'derived-adapter-output
      derived-adapter-output
      'derived-execution
      derived-execution
      'derived-adapter-record
      derived-adapter-record
      'execution-hash
      execution-hash
      'stdout-hash
      stdout-hash)))
