(defn- semantic-mid-current-seed-candidate-evidence
  [formal-artifact evidence]
  (semantic-mid-assoc-present-evidence
   {:prior-formal-release-governance-artifact
    {:status :verified
     :artifact-id (:artifact-id formal-artifact)}
    :prior-claimed-subset-self-hosting
    {:status :verified
     :scope :stage1-reader-claimed-subset}}
   evidence
   [[:compiler-pipeline-manifest :compiler-pipeline-manifest]
    [:source-unit-and-syntax-serialization-proof
     :source-unit-and-syntax-serialization-proof]
    [:core-lowering-and-diagnostic-preservation-report
     :core-lowering-and-diagnostic-preservation-report]
    [:runtime-manifest-and-capability-enforcement-report
     :runtime-manifest-and-capability-enforcement-report]
    [:accepted-app-execution-proof :accepted-app-execution-proof]
    [:rejected-app-diagnostic-proof :rejected-app-diagnostic-proof]
    [:reproducible-rebuild-log :reproducible-rebuild-log]
    [:stage-comparison-report :stage-comparison-report]
    [:conformance-report :conformance-report]
    [:provenance-attestation :provenance-attestation]
    [:tcb-delta-record :tcb-delta-record]
    [:unsafe-audit-report :unsafe-audit-report]
    [:whole-language-compiler-artifact :whole-language-compiler-artifact]
    [:governance-and-package-release-record
     :governance-and-package-release-record]
    [:stage2-compiler-nucleus :stage2-compiler-nucleus]
    [:stage2-plan-emitter :stage2-plan-emitter]
    [:stage2-runtime-kernel :stage2-runtime-kernel]
    [:stage2-runtime-executor :stage2-runtime-executor]
    [:stage2-front-end-executor :stage2-front-end-executor]
    [:stage2-source-front-end :stage2-source-front-end]
    [:stage2-compiler-driver :stage2-compiler-driver]
    [:stage2-whole-language-compiler :stage2-whole-language-compiler]
    [:stage3-seedless-compiler-candidate
     :stage3-seedless-compiler-candidate]
    [:stage3-equivalence-bundle :stage3-equivalence-bundle]
    [:stage3-self-hosted-application-execution
     :stage3-self-hosted-application-execution]]))

(defn- semantic-mid-current-seed-final-proof-input
  [evidence]
  (semantic-mid-assoc-present-evidence
   {}
   evidence
   [[:compiler-pipeline-manifest :compiler-pipeline-manifest]
    [:source-unit-and-syntax-serialization-proof
     :source-unit-and-syntax-serialization-proof]
    [:core-lowering-and-diagnostic-preservation-report
     :core-lowering-and-diagnostic-preservation-report]
    [:runtime-manifest-and-capability-enforcement-report
     :runtime-manifest-and-capability-enforcement-report]
    [:accepted-app-execution-proof :accepted-app-execution-proof]
    [:rejected-app-diagnostic-proof :rejected-app-diagnostic-proof]
    [:reproducible-rebuild-log :reproducible-rebuild-log]
    [:stage-comparison-report :stage-comparison-report]
    [:conformance-report :self-hosting-conformance-report]
    [:provenance-attestation :bootstrap-provenance-attestation]
    [:tcb-delta-record :trusted-computing-base-delta-record]
    [:unsafe-audit-report :unsafe-audit-report]
    [:whole-language-compiler-artifact :whole-language-compiler-artifact]
    [:governance-and-package-release-record
     :governance-and-package-release-record]
    [:stage3-seedless-compiler-candidate
     :stage3-seedless-compiler-candidate]
    [:stage3-equivalence-bundle :stage3-equivalence-bundle]
    [:stage3-self-hosted-application-execution
     :stage3-self-hosted-application-execution]]))
