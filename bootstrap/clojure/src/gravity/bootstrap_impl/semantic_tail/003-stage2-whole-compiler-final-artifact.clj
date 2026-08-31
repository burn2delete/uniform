(defn- semantic-tail-stage2-whole-compiler-final-artifact
  [source-path
   {:keys [proof-contract inventory-artifact pipeline-artifact
           whole-compiler-artifact driver-artifact
           source-front-end-artifact front-end-executor-artifact
           plan-emitter-artifact runtime-executor-artifact
           runtime-kernel-artifact accepted-artifact rejected-artifact
           stage-comparison-artifact conformance-artifact
           provenance-artifact tcb-artifact unsafe-artifact]}
   {:keys [source-record stage-record accepted-record rejected-record
           evidence-link-record boundary-record lineage-record proof-id
           rejected-records]}]
  (let [artifact-base
        {:kind
         :gravity/p15-s23-stage2-whole-language-compiler-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-stage2-whole-language-compiler
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
                       :compiler-pipeline-manifest
                       :capability-based-proof])
         :whole-language-compiler-artifact
         (select-keys whole-compiler-artifact
                      [:kind :artifact-id :proof-id
                       :compiler-artifact-manifest
                       :accepted-application-compile-record
                       :rejected-application-diagnostic-record
                       :residual-trusted-boundary-record
                       :capability-based-proof])
         :stage2-compiler-driver-artifact
         (select-keys driver-artifact
                      [:kind :artifact-id :proof-id :accepted-record
                       :rejected-record :boundary-record
                       :capability-based-proof])
         :stage2-source-front-end-artifact
         (select-keys source-front-end-artifact
                      [:kind :artifact-id :proof-id
                       :rule-record :accepted-record
                       :boundary-record :capability-based-proof])
         :stage2-front-end-executor-artifact
         (select-keys front-end-executor-artifact
                      [:kind :artifact-id :proof-id
                       :rule-record :accepted-record
                       :boundary-record :capability-based-proof])
         :stage2-plan-emitter-artifact
         (select-keys plan-emitter-artifact
                      [:kind :artifact-id :proof-id
                       :rule-record :accepted-record
                       :boundary-record :capability-based-proof])
         :stage2-runtime-executor-artifact
         (select-keys runtime-executor-artifact
                      [:kind :artifact-id :proof-id
                       :rule-record :accepted-record
                       :boundary-record :capability-based-proof])
         :stage2-runtime-kernel-artifact
         (select-keys runtime-kernel-artifact
                      [:kind :artifact-id :proof-id
                       :rule-record :accepted-record
                       :boundary-record :capability-based-proof])
         :accepted-app-execution-artifact
         (select-keys accepted-artifact
                      [:kind :artifact-id :proof-id
                       :accepted-output-comparison
                       :compiled-plan-execution-trace
                       :capability-based-proof])
         :rejected-app-diagnostic-artifact
         (select-keys rejected-artifact
                      [:kind :artifact-id :proof-id
                       :rejected-app-diagnostic-records
                       :capability-based-proof])
         :stage-comparison-report-artifact
         (select-keys stage-comparison-artifact
                      [:kind :artifact-id :proof-id
                       :capability-based-proof])
         :self-hosting-conformance-report-artifact
         (select-keys conformance-artifact
                      [:kind :artifact-id :proof-id
                       :capability-based-proof])
         :bootstrap-provenance-attestation-artifact
         (select-keys provenance-artifact
                      [:kind :artifact-id :proof-id
                       :bootstrap-provenance-record
                       :compiler-lineage-graph
                       :capability-based-proof])
         :tcb-delta-record-artifact
         (select-keys tcb-artifact
                      [:kind :artifact-id :proof-id
                       :residual-trust-boundary-record
                       :capability-based-proof])
         :unsafe-audit-report-artifact
         (select-keys unsafe-artifact
                      [:kind :artifact-id :proof-id
                       :unsafe-audit-report
                       :unsafe-island-index
                       :capability-based-proof])
         :source-record source-record
         :stage-record stage-record
         :accepted-record accepted-record
         :rejected-record rejected-record
         :evidence-link-record evidence-link-record
         :boundary-record boundary-record
         :lineage-record lineage-record
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-stage2-whole-language-compiler-fixtures
         [{:fixture source-path
           :status :accepted
           :source-components (:observed-components source-record)}
          {:fixture p15-s23-accepted-app-source-path
           :status :accepted
           :stdout (:stage2-output accepted-record)
           :stage2-plan-id (:stage2-plan-id accepted-record)}]
         :rejected-p15-s23-stage2-whole-language-compiler-fixtures
         rejected-records
         :p15-s23-stage2-whole-language-compiler-diagnostic-stream
         (p15-s23-stage2-whole-language-compiler-diagnostic-stream
          source-path proof-id)
         :p15-s23-stage2-whole-language-compiler-results
         {:accepted-fixtures 2
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-stage2-whole-language-compiler-diagnostic-ids)
          :source-component-count
          (count (:observed-components source-record))
          :accepted-app-output (:stage2-output accepted-record)
          :rejected-diagnostics
          (:stage2-observed-diagnostics rejected-record)
          :stage2-compiler-driver-executed?
          (:stage2-compiler-driver-executed? boundary-record)
          :stage2-runtime-kernel-used?
          (:stage2-runtime-kernel-used? boundary-record)
          :clojure-stage0-verifier?
          (:clojure-stage0-verifier? boundary-record)
          :clojure-stage0-release-compiler?
          (:clojure-stage0-release-compiler? boundary-record)
          :full-language-compiler-self-hosted? false
          :clojure-seed-retired? false
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-stage2-whole-language-compiler-proof
               artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))
