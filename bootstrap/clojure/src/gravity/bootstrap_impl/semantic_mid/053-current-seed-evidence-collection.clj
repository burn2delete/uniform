(defn- semantic-mid-current-seed-evidence
  []
  (reduce (fn [evidence evidence-key]
            (assoc evidence evidence-key
                   (p15-s23-current-candidate-artifact-evidence
                    evidence-key)))
          {}
          [:compiler-pipeline-manifest
           :source-unit-and-syntax-serialization-proof
           :core-lowering-and-diagnostic-preservation-report
           :runtime-manifest-and-capability-enforcement-report
           :accepted-app-execution-proof
           :rejected-app-diagnostic-proof
           :reproducible-rebuild-log
           :stage-comparison-report
           :conformance-report
           :provenance-attestation
           :tcb-delta-record
           :unsafe-audit-report
           :whole-language-compiler-artifact
           :governance-and-package-release-record
           :stage2-compiler-nucleus
           :stage2-plan-emitter
           :stage2-runtime-kernel
           :stage2-runtime-executor
           :stage2-front-end-executor
           :stage2-source-front-end
           :stage2-compiler-driver
           :stage2-whole-language-compiler
           :stage3-seedless-compiler-candidate
           :stage3-equivalence-bundle
           :stage3-self-hosted-application-execution]))

(defn- semantic-mid-assoc-present-evidence
  [target evidence mappings]
  (reduce (fn [target [source-key target-key]]
            (let [value (get evidence source-key)]
              (if value
                (assoc target target-key value)
                target)))
          target
          mappings))
