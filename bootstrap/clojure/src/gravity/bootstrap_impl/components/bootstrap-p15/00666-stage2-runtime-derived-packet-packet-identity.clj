(defn- __gravity_bootstrap_runtime_packet_packet_identity [state]
  (let [{:syms
         [source-path
          source-text
          requested-target
          validate-plan!
          stage2-rule
          stage2-runtime-rule
          stage2-driver-rule
          plan
          _
          raw-source-declared-target
          source-declared-target
          target-eligibility
          closed-plan-validation
          compiler-artifact-record
          stage2-driver-run
          driver-plan
          plan-shape
          driver-plan-shape
          runtime-adapter-output
          closed-plan-execution
          runtime-contract-adapter-record
          closed-plan-execution-hash
          closed-plan-invocation
          stage2-runtime-execution
          reference-output]} state]
    {:stage2-compiler-artifact-record compiler-artifact-record,
     :stage2-runtime-rule stage2-runtime-rule,
     :stage2-compiler-driver-rule stage2-driver-rule,
     :stage2-plan-emitter-rule stage2-rule,
     :closed-plan-execution-record closed-plan-execution,
     :requested-target requested-target,
     :closed-plan-invocation-record closed-plan-invocation,
     :stage2-compiler-driver-record stage2-driver-run,
     :status :complete,
     :kind :gravity/target-neutral-stage2-runtime-packet,
     :runtime-contract-adapter-record runtime-contract-adapter-record,
     :closed-plan-validation-record closed-plan-validation,
     :provenance
     {:actual-paths
      {:stage2-compiler-source (:stage2-compiler-source-path stage2-runtime-rule),
       :stage2-expression-lowering-source
       (p15-s23-stage2-compiler-artifact-source-path),
       :stage2-runtime-artifact-source
       (:runtime-artifact-source-path stage2-runtime-rule)}},
     :reference-output reference-output,
     :plan plan,
     :stage2-runtime-execution-record stage2-runtime-execution,
     :target-eligibility target-eligibility}))
