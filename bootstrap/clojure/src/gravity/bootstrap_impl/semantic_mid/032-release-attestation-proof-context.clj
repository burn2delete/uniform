(defn- semantic-mid-release-attestation-proof-context
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:stage1-reader-release-attestation-seed-retirement-diagnostic-stream
                           :diagnostics])))
        release-attestation
        (:stage1-reader-release-attestation-seed-retirement artifact)]
    {:artifact artifact
     :diagnostics diagnostics
     :release-attestation release-attestation
     :diverse-verification
     (:stage1-reader-diverse-bootstrap-verification artifact)
     :boot-chain (:stage1-reader-verified-boot-chain artifact)
     :character-stream (:stage1-reader-character-stream artifact)
     :token-stream (:stage1-reader-token-stream artifact)
     :records (:stage1-reader-records artifact)
     :operation-names
     (set (:release-attestation-operations release-attestation))
     :direct-stages (mapv :op (:direct-stages release-attestation))
     :gravity-runtimes (set (:gravity-runtimes artifact))
     :gravity-executors (set (:gravity-executors artifact))
     :release-record (:release-attestation-record release-attestation)
     :seed-retirement-evidence
     (:seed-retirement-evidence release-attestation)
     :supply-chain-manifest (:supply-chain-manifest release-attestation)
     :release-custody-record (:release-custody-record release-attestation)
     :governance-approval-record
     (:governance-approval-record release-attestation)
     :revocation-check-report (:revocation-check-report release-attestation)
     :release-provenance-record
     (:release-provenance-record release-attestation)}))
