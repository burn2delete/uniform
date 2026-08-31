(defn- semantic-mid-validate-release-attestation-evidence!
  [{:keys [reader-source-path release-record seed-retirement-evidence
           supply-chain-manifest release-custody-record
           governance-approval-record revocation-check-report
           release-provenance-record]}]
  (when-not (and (= :accepted (:status release-record))
                 (= :verified (:provenance release-record))
                 (= :verified (:sbom release-record))
                 (= :verified (:signatures release-record))
                 (= :verified (:artifact-hashes release-record))
                 (= :verified (:capability-manifest release-record))
                 (= :verified (:conformance-report release-record)))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL003" reader-source-path release-record
     {:missing-fields [:release-attestation-record]}))
  (when-not (and (= :recorded (:status seed-retirement-evidence))
                 (true? (:stage1-reader-seed-assumptions-retired?
                         seed-retirement-evidence))
                 (false? (:full-compiler-seed-retired?
                          seed-retirement-evidence))
                 (= #{:physical-device-manufacturing
                      :supply-chain-custody
                      :independent-diversity-review}
                    (set (:retired-assumptions seed-retirement-evidence)))
                 (set/subset?
                  #{:human-release-governance
                    :legal-custody-record-retention
                    :deployment-environment-custody}
                  (set (:remaining-boundaries seed-retirement-evidence))))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL004" reader-source-path seed-retirement-evidence
     {:missing-fields [:seed-retirement-evidence]}))
  (when-not (and (= :verifiable (:status supply-chain-manifest))
                 (= :verified (:source-archive supply-chain-manifest))
                 (= :verified (:lockfile supply-chain-manifest))
                 (= :verified (:builder-identity supply-chain-manifest))
                 (= :verified (:custody-events supply-chain-manifest))
                 (= :verified (:sbom supply-chain-manifest))
                 (= :verified (:signatures supply-chain-manifest)))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL006" reader-source-path supply-chain-manifest
     {:missing-fields [:supply-chain-manifest]}))
  (when-not (and (true? (:reproducible release-custody-record))
                 (true? (:locked-dependencies release-custody-record))
                 (true? (:fixed-time release-custody-record))
                 (= "C" (:locale release-custody-record))
                 (= :canonical (:filesystem-order release-custody-record))
                 (= :disabled (:network release-custody-record))
                 (= :accepted (:status release-custody-record)))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL005" reader-source-path release-custody-record
     {:missing-fields [:release-custody-record]}))
  (when-not (and (= :approved (:status governance-approval-record))
                 (= :accepted (:rfc-state governance-approval-record))
                 (= :GOV6 (:rfc-policy governance-approval-record))
                 (= :GOV10 (:package-policy governance-approval-record))
                 (set/subset?
                  #{:compatibility :security :profile
                    :conformance :package-governance}
                  (set (:review-gates governance-approval-record))))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL007" reader-source-path governance-approval-record
     {:missing-fields [:governance-approval-record]}))
  (when-not (and (= :clear (:status revocation-check-report))
                 (= [] (:revoked-builders revocation-check-report))
                 (= [] (:revoked-dependencies revocation-check-report))
                 (= [] (:revoked-signatures revocation-check-report)))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL009" reader-source-path revocation-check-report
     {:missing-fields [:revocation-check-report]}))
  (when-not (and (= :complete (:status release-provenance-record))
                 (true? (:canonicalized release-provenance-record))
                 (true? (:auditor-traversable release-provenance-record))
                 (set/subset?
                  #{:stage1-reader-release-attestation-seed-retirement
                    :stage1-reader-diverse-bootstrap-verification
                    :stage1-reader-verified-boot-chain}
                  (set (:lineage release-provenance-record))))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" reader-source-path release-provenance-record
     {:missing-fields [:release-provenance-record]})))
