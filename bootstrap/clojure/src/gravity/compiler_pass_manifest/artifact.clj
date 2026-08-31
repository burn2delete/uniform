(ns gravity.compiler-pass-manifest.artifact
  "Compiler pass manifest artifact emission from upstream conformance."
  (:require [gravity.compiler-pass-manifest.diagnostic-validation :as diagnostic-validation]
            [gravity.compiler-pass-manifest.diagnostics :as diagnostic-data]
            [gravity.compiler-pass-manifest.incremental-validation :as incremental-validation]
            [gravity.compiler-pass-manifest.pipeline-validation :as pipeline-validation]
            [gravity.compiler-pass-manifest.plugin-validation :as plugin-validation]
            [gravity.compiler-pass-manifest.proof :as proof]
            [gravity.compiler-pass-manifest.suite :as suite]
            [gravity.compiler-pass-manifest.verification-validation :as verification-validation]
            [gravity.digest :as digest]))

(defn compiler-pass-source-artifact-from-upstream
  [source-path upstream-artifact]
  (let [
        manifest (:profile-manifest upstream-artifact)
        suite (suite/compiler-pass-suite manifest)
        _ (pipeline-validation/compiler-pass-validate-pipeline! source-path manifest suite)
        _ (diagnostic-validation/compiler-pass-validate-diagnostics! source-path manifest suite)
        _ (incremental-validation/compiler-pass-validate-incremental! source-path manifest suite)
        _ (plugin-validation/compiler-pass-validate-plugins! source-path manifest suite)
        _ (verification-validation/compiler-pass-validate-verification! source-path manifest suite)
        capability-proof (proof/compiler-pass-capability-proof suite)
        conformance {:documents ["C1" "C15" "C16" "C17" "C18"]
                     :task "P06-T01"
                     :required-diagnostic-ids diagnostic-data/compiler-pass-diagnostic-ids
                     :pass-contract-status :complete
                     :diagnostic-registry-status :complete
                     :incremental-contract-status :complete
                     :plugin-api-status :complete
                     :verification-status :complete
                     :status :complete}]
    {:kind :gravity/stage0-pass-contract-manifest-artifact
     :document-set ["C1" "C15" "C16" "C17" "C18"]
     :pass {:name :compiler-pass-contract-manifest
            :input :stage0-checked-capability-stack
            :output :pass-contract-manifest
            :requires [:reader :syntax :macro :core :typed-core
                       :effected-core :profile-compliance :safety
                       :performance :math-conformance]
            :preserves [:source-spans :syntax-identity :origin-chain
                        :profile :target :types :effects :ownership
                        :capabilities :safety-outcomes :proofs
                        :diagnostics]
            :emits [:compiler-pipeline-manifest :pass-contract-registry
                    :diagnostic-registry :incremental-cache-key-schema
                    :plugin-pass-api-manifest :verification-plan
                    :compiler-trust-report]
            :rejects diagnostic-data/compiler-pass-diagnostic-ids}
     :upstream-artifact-kind (:kind upstream-artifact)
     :upstream-artifact-hash
     (str "sha256:" (digest/sha256-hex (pr-str upstream-artifact)))
     :profile-manifest manifest
     :pipeline-stage-order (:stage-order suite)
     :pipeline-manifest (:pipeline-manifest suite)
     :pass-contract-registry (:contracts suite)
     :compiler-diagnostic-registry (:diagnostic-catalog suite)
     :diagnostic-stream-schema (:diagnostic-schema suite)
     :diagnostic-fixtures (:diagnostic-fixtures suite)
     :incremental-cache-key-schema (:cache-key-schema suite)
     :stage-cache-keys (:cache-keys suite)
     :stage-cache-entry-manifest (:cache-entries suite)
     :proof-reuse-records (:proof-reuse-records suite)
     :speculative-reuse-records (:speculative-reuse-records suite)
     :plugin-pass-api-manifest (:plugin-manifest suite)
     :plugin-pass-contracts (:plugin-pass-contracts suite)
     :plugin-execution-traces (:plugin-execution-traces suite)
     :pass-risk-classification (:risk-classification suite)
     :compiler-trust-report (:compiler-trust-report suite)
     :release-gate-report (:release-gate-report suite)
     :capability-based-proof capability-proof
     :compiler-pass-results conformance
     :diagnostics []}))
