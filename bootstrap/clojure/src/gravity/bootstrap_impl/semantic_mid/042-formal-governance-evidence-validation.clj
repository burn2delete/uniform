(defn- semantic-mid-validate-formal-governance-evidence!
  [{:keys [reader-source-path formal-record deployment-custody
           self-hosting-evidence seed-retirement-evidence
           tcb-delta-record unsafe-audit-report formal-provenance-record]}]
  (when-not (and (= :verified (:status formal-record))
                 (= :gravity-release-policy-v1 (:policy-engine formal-record))
                 (= :versioned-rfc-ledger (:governance-source formal-record))
                 (= :machine-checkable-governance
                    (:approval-model formal-record))
                 (set/subset? #{:GOV6 :GOV10 :BOOT6 :BOOT7 :BOOT8 :TEST13}
                              (set (:required-policies formal-record)))
                 (true? (:human-release-governance-replaced? formal-record))
                 (true? (:legal-custody-record-retention-replaced?
                         formal-record))
                 (true? (:deployment-environment-custody-replaced?
                         formal-record)))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV003" reader-source-path formal-record
     {:missing-fields [:formal-release-governance-record]}))
  (when-not (and (= :verifiable (:status deployment-custody))
                 (= :content-addressed-release-graph
                    (:custody-model deployment-custody))
                 (= :policy-verified (:builder-selection deployment-custody))
                 (= :hash-and-policy-gated
                    (:artifact-admission deployment-custody))
                 (= :disabled (:network deployment-custody))
                 (= :forbidden (:mutable-state deployment-custody)))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV004" reader-source-path deployment-custody
     {:missing-fields [:deployment-custody-record]}))
  (when-not (and (= :verified (:status self-hosting-evidence))
                 (= :stage1-reader-claimed-subset
                    (:scope self-hosting-evidence))
                 (= :verified (:staged-compiler-artifact self-hosting-evidence))
                 (= :verified (:reproducible-rebuild-log self-hosting-evidence))
                 (= :verified (:conformance-report self-hosting-evidence))
                 (= :verified (:stage-comparison-report self-hosting-evidence))
                 (= :verified (:provenance-attestation self-hosting-evidence))
                 (= :verified (:tcb-delta self-hosting-evidence))
                 (= :not-applicable (:unsafe-audit-report self-hosting-evidence))
                 (true? (:claimed-subset-self-hosted? self-hosting-evidence))
                 (false? (:full-language-compiler-self-hosted?
                          self-hosting-evidence))
                 (false? (:clojure-seed-retired? self-hosting-evidence)))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV005" reader-source-path self-hosting-evidence
     {:missing-fields [:self-hosting-evidence]}))
  (when-not (= :verified (:reproducible-rebuild-log self-hosting-evidence))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV006" reader-source-path self-hosting-evidence
     {:missing-fields [:reproducible-rebuild-log]}))
  (when-not (= :verified (:stage-comparison-report self-hosting-evidence))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV007" reader-source-path self-hosting-evidence
     {:missing-fields [:stage-comparison-report]}))
  (when-not (and (= :recorded (:status seed-retirement-evidence))
                 (true? (:claimed-subset-seed-retired?
                         seed-retirement-evidence))
                 (false? (:full-compiler-seed-retired?
                          seed-retirement-evidence))
                 (false? (:clojure-seed-retired? seed-retirement-evidence))
                 (= #{:human-release-governance
                      :legal-custody-record-retention
                      :deployment-environment-custody}
                    (set (:retired-assumptions seed-retirement-evidence)))
                 (= #{:full-language-compiler-self-hosting}
                    (set (:remaining-boundaries seed-retirement-evidence))))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV005" reader-source-path seed-retirement-evidence
     {:missing-fields [:seed-retirement-evidence]}))
  (when-not (and (= :complete (:status tcb-delta-record))
                 (= #{:human-release-governance
                      :legal-custody-record-retention
                      :deployment-environment-custody}
                    (set (:removed tcb-delta-record)))
                 (= #{:full-language-compiler-self-hosting}
                    (set (:remaining tcb-delta-record)))
                 (= [] (:new-unsafe-islands tcb-delta-record)))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV008" reader-source-path tcb-delta-record
     {:missing-fields [:tcb-delta-record]}))
  (when-not (and (= :not-applicable (:status unsafe-audit-report))
                 (false? (:unsafe-required? unsafe-audit-report))
                 (= [] (:unsafe-islands unsafe-audit-report)))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" reader-source-path unsafe-audit-report
     {:missing-fields [:unsafe-audit-report]}))
  (when-not (and (= :complete (:status formal-provenance-record))
                 (true? (:canonicalized formal-provenance-record))
                 (true? (:auditor-traversable formal-provenance-record))
                 (set/subset?
                  #{:stage1-reader-formal-release-governance-seed-retirement
                    :stage1-reader-release-attestation-seed-retirement
                    :stage1-reader-diverse-bootstrap-verification}
                  (set (:lineage formal-provenance-record))))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" reader-source-path formal-provenance-record
     {:missing-fields [:formal-release-provenance-record]})))
