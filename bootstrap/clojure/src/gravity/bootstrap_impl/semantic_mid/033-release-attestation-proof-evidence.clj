(defn- semantic-mid-release-attestation-proof-evidence
  [{:keys [artifact release-attestation diverse-verification boot-chain
           operation-names direct-stages release-record
           seed-retirement-evidence supply-chain-manifest
           release-custody-record governance-approval-record
           revocation-check-report release-provenance-record]}]
  {:gravity-reader-release-attestation-seed-retirement-verified?
   (= stage1-reader-release-attestation-seed-retirement-entrypoint
      (:gravity-entrypoint artifact))
   :gravity-reader-source-verified?
   (= :complete (get-in artifact
                        [:stage1-bootstrap-source-artifact
                         :capability-based-proof :status]))
   :release-attestation-seed-retirement-authored?
   (and (= :gravity-reader-release-attestation-seed-retirement-v1
           (:engine release-attestation))
        (= :gravity-source
           (get-in release-attestation [:provenance :owner]))
        (= :reader-release-attestation-seed-retirement-assumption-replacement
           (get-in release-attestation [:provenance :purpose])))
   :release-attestation-seed-retirement-direct-stages-covered?
   (= [:stage1-release-attestation-verify
       :stage1-release-attestation-seed-retirement-evidence
       :stage1-release-attestation-supply-chain-manifest
       :stage1-release-attestation-release-custody-reproducibility
       :stage1-release-attestation-governance-approval
       :stage1-release-attestation-revocation-status
       :stage1-release-attestation-record-provenance]
      direct-stages)
   :release-attestation-seed-retirement-operations-covered?
   (set/subset?
    (set stage1-reader-release-attestation-seed-retirement-required-operations)
    operation-names)
   :release-attestation-links-diverse-bootstrap-verification?
   (= :stage1-reader-diverse-bootstrap-verification
      (:diverse-bootstrap-verification release-attestation))
   :release-attestation-links-verified-boot-chain?
   (= :stage1-reader-verified-boot-chain
      (:verified-boot-chain release-attestation))
   :diverse-bootstrap-verification-authored?
   (= :gravity-reader-diverse-bootstrap-verification-v1
      (:engine diverse-verification))
   :verified-boot-chain-authored?
   (= :gravity-reader-verified-boot-chain-v1 (:engine boot-chain))
   :artifact-routing-covered?
   (= :gravity/stage1-reader-release-attestation-seed-retirement-artifact
      (:artifact release-attestation) (:kind artifact))
   :diagnostic-stream-routing-covered?
   (= :gravity/stage1-reader-release-attestation-seed-retirement-diagnostic-stream
      (:diagnostic-stream release-attestation)
      (get-in artifact
              [:stage1-reader-release-attestation-seed-retirement-diagnostic-stream
               :artifact]))
   :physical-device-manufacturing-boundary-replaced?
   (false? (get-in artifact
                   [:trusted-boundary
                    :physical-device-manufacturing-boundary?]))
   :supply-chain-custody-boundary-replaced?
   (false? (get-in artifact
                   [:trusted-boundary :supply-chain-custody-boundary?]))
   :independent-diversity-review-boundary-replaced?
   (false? (get-in artifact
                   [:trusted-boundary
                    :independent-diversity-review-boundary?]))
   :residual-trust-boundaries-empty?
   (= [] (:residual-trust-boundaries artifact))
   :residual-release-governance-boundaries-explicit?
   (and (true? (get-in artifact
                       [:trusted-boundary
                        :human-release-governance-boundary?]))
        (true? (get-in artifact
                       [:trusted-boundary
                        :legal-custody-record-retention-boundary?]))
        (true? (get-in artifact
                       [:trusted-boundary
                        :deployment-environment-custody-boundary?])))
   :release-attestation-record-covered?
   (and (= :accepted (:status release-record))
        (= :verified (:provenance release-record))
        (= :verified (:sbom release-record))
        (= :verified (:signatures release-record))
        (= :verified (:artifact-hashes release-record))
        (= :verified (:capability-manifest release-record))
        (= :verified (:conformance-report release-record)))
   :seed-retirement-evidence-covered?
   (and (= :recorded (:status seed-retirement-evidence))
        (true? (:stage1-reader-seed-assumptions-retired?
                seed-retirement-evidence))
        (false? (:full-compiler-seed-retired? seed-retirement-evidence)))
   :supply-chain-manifest-covered?
   (and (= :verifiable (:status supply-chain-manifest))
        (= :verified (:source-archive supply-chain-manifest))
        (= :verified (:lockfile supply-chain-manifest))
        (= :verified (:builder-identity supply-chain-manifest))
        (= :verified (:custody-events supply-chain-manifest))
        (= :verified (:sbom supply-chain-manifest))
        (= :verified (:signatures supply-chain-manifest)))
   :release-custody-reproducibility-covered?
   (and (true? (:reproducible release-custody-record))
        (true? (:locked-dependencies release-custody-record))
        (true? (:fixed-time release-custody-record))
        (= "C" (:locale release-custody-record))
        (= :canonical (:filesystem-order release-custody-record))
        (= :disabled (:network release-custody-record)))
   :governance-approval-covered?
   (and (= :approved (:status governance-approval-record))
        (= :accepted (:rfc-state governance-approval-record))
        (= :GOV6 (:rfc-policy governance-approval-record))
        (= :GOV10 (:package-policy governance-approval-record)))
   :revocation-check-covered?
   (and (= :clear (:status revocation-check-report))
        (= [] (:revoked-builders revocation-check-report))
        (= [] (:revoked-dependencies revocation-check-report))
        (= [] (:revoked-signatures revocation-check-report)))
   :release-provenance-covered?
   (and (= :complete (:status release-provenance-record))
        (true? (:canonicalized release-provenance-record))
        (true? (:auditor-traversable release-provenance-record))
        (set/subset?
         #{:stage1-reader-release-attestation-seed-retirement
           :stage1-reader-diverse-bootstrap-verification
           :stage1-reader-verified-boot-chain}
         (set (:lineage release-provenance-record))))})
