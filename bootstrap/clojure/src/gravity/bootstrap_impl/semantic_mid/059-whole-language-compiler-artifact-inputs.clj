(defn- semantic-mid-whole-language-compiler-artifact-inputs
  [{:keys [source-path proof-id proof-contract inventory-artifact
           pipeline-artifact source-syntax-artifact core-artifact
           runtime-artifact accepted-artifact rejected-artifact
           rebuild-artifact stage-comparison-artifact conformance-artifact
           provenance-artifact tcb-artifact unsafe-artifact
           hosted-compiler-artifact]}]
  {:kind :gravity/p15-s23-whole-language-compiler-artifact
   :phase "15"
   :task "P15-S23"
   :stage :p15-s23-whole-language-compiler-artifact
   :source-path source-path
   :proof-id proof-id
   :proof-contract proof-contract
   :compiler-source-inventory-artifact
   (select-keys inventory-artifact
                [:kind :artifact-id :inventory-id
                 :source-inventory :capability-based-proof])
   :compiler-pipeline-manifest-artifact
   (select-keys pipeline-artifact
                [:kind :artifact-id :manifest-id
                 :compiler-pipeline-manifest :capability-based-proof])
   :source-syntax-serialization-artifact
   (select-keys source-syntax-artifact
                [:kind :artifact-id :proof-id :source-unit-record
                 :syntax-serialization-roundtrip :capability-based-proof])
   :core-lowering-diagnostic-artifact
   (select-keys core-artifact
                [:kind :artifact-id :proof-id :core-module-record
                 :diagnostic-preservation-record :capability-based-proof])
   :runtime-manifest-capability-artifact
   (select-keys runtime-artifact
                [:kind :artifact-id :proof-id :runtime-manifest
                 :runtime-capability-manifest :capability-based-proof])
   :accepted-app-execution-artifact
   (select-keys accepted-artifact
                [:kind :artifact-id :proof-id :accepted-output-comparison
                 :compiled-plan-execution-trace :trusted-boundary-record
                 :capability-based-proof])
   :rejected-app-diagnostic-artifact
   (select-keys rejected-artifact
                [:kind :artifact-id :proof-id
                 :rejected-app-diagnostic-records
                 :diagnostic-preservation-record :capability-based-proof])
   :reproducible-rebuild-log-artifact
   (select-keys rebuild-artifact
                [:kind :artifact-id :proof-id
                 :artifact-identity-comparison
                 :environment-provenance-record :capability-based-proof])
   :stage-comparison-report-artifact
   (select-keys stage-comparison-artifact
                [:kind :artifact-id :proof-id :stage-boundary-record
                 :accepted-output-stage-comparison
                 :rejected-diagnostic-stage-comparison
                 :capability-based-proof])
   :self-hosting-conformance-report-artifact
   (select-keys conformance-artifact
                [:kind :artifact-id :proof-id
                 :stage-support-conformance-record
                 :conformance-suite-link-table
                 :diagnostic-conformance-record :capability-based-proof])
   :bootstrap-provenance-attestation-artifact
   (select-keys provenance-artifact
                [:kind :artifact-id :proof-id :bootstrap-provenance-record
                 :compiler-lineage-graph :capability-based-proof])
   :tcb-delta-record-artifact
   (select-keys tcb-artifact
                [:kind :artifact-id :proof-id :tcb-delta-record
                 :residual-trust-boundary-record :capability-based-proof])
   :unsafe-audit-report-artifact
   (select-keys unsafe-artifact
                [:kind :artifact-id :proof-id :unsafe-audit-report
                 :unsafe-island-index :capability-based-proof])
   :hosted-core-compiled-compiler-artifact
   (select-keys hosted-compiler-artifact
                [:kind :artifact-id :compiler-report :compiled-plan
                 :accepted-run :capability-based-proof])})
