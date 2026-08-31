(defn- semantic-mid-release-attestation-definition-context
  [reader-source-path definitions]
  (let [release-attestation
        (stage1-reader-release-attestation-seed-retirement-literal-definition-value
         reader-source-path definitions
         'stage1-reader-release-attestation-seed-retirement)
        diagnostics (:diagnostics release-attestation)
        missing-diagnostics
        (remove #(contains? diagnostics %)
                [:missing-entrypoint
                 :unsupported-release-attestation-operation
                 :missing-release-attestation-record
                 :missing-seed-retirement-evidence
                 :nonreproducible-release-custody
                 :unverifiable-supply-chain-manifest
                 :missing-governance-approval
                 :illegal-physical-release-fallback
                 :revoked-release-input
                 :invalid-release-attestation-seed-retirement])
        required-stages
        [:stage1-release-attestation-verify
         :stage1-release-attestation-seed-retirement-evidence
         :stage1-release-attestation-supply-chain-manifest
         :stage1-release-attestation-release-custody-reproducibility
         :stage1-release-attestation-governance-approval
         :stage1-release-attestation-revocation-status
         :stage1-release-attestation-record-provenance]
        direct-stages (:direct-stages release-attestation)]
    {:reader-source-path reader-source-path
     :release-attestation release-attestation
     :diagnostics diagnostics
     :missing-diagnostics missing-diagnostics
     :required-stages required-stages
     :direct-stages direct-stages
     :release-record (:release-attestation-record release-attestation)
     :seed-retirement-evidence (:seed-retirement-evidence release-attestation)
     :supply-chain-manifest (:supply-chain-manifest release-attestation)
     :release-custody-record (:release-custody-record release-attestation)
     :governance-approval-record
     (:governance-approval-record release-attestation)
     :revocation-check-report (:revocation-check-report release-attestation)
     :release-provenance-record
     (:release-provenance-record release-attestation)}))
