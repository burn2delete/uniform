(defn- __gravity_bootstrap_packet_authentic_source_and_rule_trust [state]
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
          expected-target-eligibility]} state]
    (and
      (=
        #{:stage2-compiler-artifact-record
          :stage2-runtime-rule
          :stage2-compiler-driver-rule
          :stage2-plan-emitter-rule
          :closed-plan-execution-record
          :requested-target
          :closed-plan-invocation-record
          :stage2-compiler-driver-record
          :status
          :kind
          :runtime-contract-adapter-record
          :closed-plan-validation-record
          :provenance
          :reference-output
          :plan
          :stage2-runtime-execution-record
          :target-eligibility}
        (set (keys packet)))
      (= :gravity/target-neutral-stage2-runtime-packet (:kind packet))
      (= :complete (:status packet))
      (= expected-target-eligibility (:target-eligibility packet))
      (=
        (normalize-emitter-rule emitter-rule)
        (normalize-emitter-rule trusted-stage2-rule))
      (=
        (normalize-driver-rule driver-rule)
        (normalize-driver-rule trusted-driver-rule))
      (=
        (normalize-runtime-rule runtime-rule)
        (normalize-runtime-rule trusted-runtime-rule))
      (=
        (:source-path emitter-rule)
        (:source-path trusted-stage2-rule)
        (:stage2-compiler-source-path runtime-rule)
        (:stage2-compiler-source-path trusted-runtime-rule)
        (:driver-source-path driver-rule)
        (:driver-source-path trusted-driver-rule))
      (=
        (:runtime-source-path runtime-rule)
        (:runtime-artifact-source-path runtime-rule)
        (:runtime-source-path trusted-runtime-rule)
        (:runtime-artifact-source-path trusted-runtime-rule))
      (= driver-record trusted-driver-record)
      (=
        {:actual-paths
         {:stage2-compiler-source (:stage2-compiler-source-path trusted-runtime-rule),
          :stage2-expression-lowering-source
          (p15-s23-stage2-compiler-artifact-source-path),
          :stage2-runtime-artifact-source
          (:runtime-artifact-source-path trusted-runtime-rule)}}
        (:provenance packet))
      (=
        (normalize-comparison packet-plan-base)
        (normalize-comparison trusted-plan-base)
        (normalize-comparison driver-plan-base))
      (p15-s23-stage2-compiler-artifact-record-authentic?
        (:stage2-compiler-artifact-record packet))
      (p15-s23-stage2-compiler-artifact-record-matches-plan?
        (:stage2-compiler-artifact-record packet)
        trusted-plan))))
