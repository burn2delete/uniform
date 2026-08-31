(ns gravity.optimization-lowering.artifact
  "Assembly of the fused hosted Stage0 C13/C14 compatibility artifact."
  (:require [gravity.optimization-lowering.lowering :as lowering]
            [gravity.optimization-lowering.products :as products]))

(defn source-overrides [module]
  (get-in module [:metadata :compiler :optimization-lowering] {}))

(def pass-contract
  {:name :optimization-and-target-lowering-api
   :input :domain-ir-registry
   :output :optimization-lowering-manifest
   :requires [:verified-domain-ir :pass-contracts
              :semantic-anchors :proof-evidence
              :target-eligibility]
   :preserves [:types :effects :ownership :capabilities
               :profile :target :safety :source-spans
               :origin-chain :domain-anchors]
   :emits [:optimization-pass-registry
           :optimization-pipeline-manifest
           :optimization-decision-log
           :invalidated-fact-ledger
           :analysis-cache-records
           :proof-and-certificate-usage
           :residual-cost-report
           :post-pass-verifier-reports
           :lowering-request
           :target-eligibility-report
           :abi-manifest
           :runtime-provider-manifest
           :layout-decision-record
           :proof-to-target-metadata-map
           :source-generated-origin-map
           :target-artifact-manifest
           :unsupported-feature-report]})

(defn- conformance [diagnostic-ids]
  {:documents ["C13" "C14"]
   :task "P06-T05"
   :required-diagnostic-ids diagnostic-ids
   :optimization-contract-status :complete
   :optimization-decision-status :complete
   :invalidation-status :complete
   :proof-status :complete
   :post-pass-verifier-status :complete
   :lowering-request-status :complete
   :target-eligibility-status :complete
   :provider-status :complete
   :manifest-status :complete
   :status :complete})

(defn source-artifact
  [{:keys [checked-core-source-artifact domain-ir-source-artifact
           source-overrides pass-contract-record decision-record sha256-hex
           validate! capability-proof]}
   {:keys [pass-contract-seed diagnostic-ids]}
   source-path source-text]
  (let [checked-core (checked-core-source-artifact source-path source-text)
        overrides (source-overrides (:module checked-core))
        domain-ir-artifact (domain-ir-source-artifact source-path source-text)
        input-id (str "sha256:" (sha256-hex (pr-str domain-ir-artifact)))
        contracts (mapv pass-contract-record pass-contract-seed)
        decisions (mapv #(decision-record domain-ir-artifact input-id %2 %1)
                        contracts
                        (range))
        final-output-id (:output-mir (last decisions))
        invalidations (products/invalidation-ledger decisions)
        verifiers (products/verifier-reports decisions)
        target lowering/target
        request (lowering/lowering-request input-id)
        proof-map lowering/proof-map
        artifact
        {:kind :gravity/stage0-optimization-lowering-artifact
         :document-set ["C13" "C14"]
         :pass (assoc pass-contract :rejects diagnostic-ids)
         :source-overrides overrides
         :domain-ir-artifact-kind (:kind domain-ir-artifact)
         :domain-ir-artifact-hash input-id
         :optimization-pass-registry contracts
         :optimization-pipeline-manifest
         (products/pipeline-manifest sha256-hex source-text contracts target)
         :optimization-decision-log decisions
         :invalidated-fact-ledger invalidations
         :analysis-cache-records
         (products/analysis-cache-records sha256-hex input-id decisions)
         :proof-and-certificate-usage (products/proof-usage decisions)
         :residual-cost-report products/residual-cost-report
         :check-elision-record products/check-elision-record
         :effect-reordering-record products/effect-reordering-record
         :safety-outcome-refresh-report products/safety-outcome-refresh-report
         :domain-anchor-transform-report
         (products/domain-anchor-transform-report domain-ir-artifact)
         :optimization-replay-record products/optimization-replay-record
         :post-pass-verifier-reports verifiers
         :lowering-request request
         :target-eligibility-report lowering/target-eligibility-report
         :abi-manifest lowering/abi-manifest
         :runtime-provider-manifest (lowering/runtime-provider-manifest request)
         :provider-selection-records lowering/provider-selection-records
         :layout-decision-record lowering/layout-decision-record
         :proof-to-target-metadata-map proof-map
         :source-generated-origin-map
         (lowering/source-generated-origin-map domain-ir-artifact)
         :capability-preservation-report lowering/capability-preservation-report
         :unsupported-feature-report lowering/unsupported-feature-report
         :target-artifact-manifest
         (lowering/target-artifact-manifest sha256-hex input-id final-output-id)
         :diagnostics []}
        _ (validate! source-path artifact)
        proof (capability-proof artifact)]
    (assoc artifact
           :capability-based-proof proof
           :optimization-lowering-results (conformance diagnostic-ids))))
