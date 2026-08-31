(defn- semantic-tail-stage2-whole-compiler-inputs
  [source-path]
  (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-stage2-whole-language-compiler)
        inventory-artifact
        (p15-s23-current-candidate-artifact-evidence
         :compiler-source-inventory)
        pipeline-artifact
        (p15-s23-current-candidate-artifact-evidence
         :compiler-pipeline-manifest)
        whole-compiler-artifact
        (p15-s23-current-candidate-artifact-evidence
         :whole-language-compiler-artifact)
        driver-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-compiler-driver)
        source-front-end-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-source-front-end)
        front-end-executor-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-front-end-executor)
        plan-emitter-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-plan-emitter)
        runtime-executor-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-runtime-executor)
        runtime-kernel-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-runtime-kernel)
        accepted-artifact
        (p15-s23-current-candidate-artifact-evidence
         :accepted-app-execution-proof)
        rejected-artifact
        (p15-s23-current-candidate-artifact-evidence
         :rejected-app-diagnostic-proof)
        stage-comparison-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage-comparison-report)
        conformance-artifact
        (p15-s23-current-candidate-artifact-evidence
         :conformance-report)
        provenance-artifact
        (p15-s23-current-candidate-artifact-evidence
         :provenance-attestation)
        tcb-artifact
        (p15-s23-current-candidate-artifact-evidence
         :tcb-delta-record)
        unsafe-artifact
        (p15-s23-current-candidate-artifact-evidence
         :unsafe-audit-report)]
    {:proof-contract proof-contract
     :inventory-artifact inventory-artifact
     :pipeline-artifact pipeline-artifact
     :whole-compiler-artifact whole-compiler-artifact
     :driver-artifact driver-artifact
     :source-front-end-artifact source-front-end-artifact
     :front-end-executor-artifact front-end-executor-artifact
     :plan-emitter-artifact plan-emitter-artifact
     :runtime-executor-artifact runtime-executor-artifact
     :runtime-kernel-artifact runtime-kernel-artifact
     :accepted-artifact accepted-artifact
     :rejected-artifact rejected-artifact
     :stage-comparison-artifact stage-comparison-artifact
     :conformance-artifact conformance-artifact
     :provenance-artifact provenance-artifact
     :tcb-artifact tcb-artifact
     :unsafe-artifact unsafe-artifact}))
