(defn- semantic-mid-formal-governance-proof-evidence
  [{:keys [artifact formal-governance release-attestation
           diverse-verification boot-chain operation-names direct-stages
           formal-record deployment-custody self-hosting-evidence
           seed-retirement-evidence tcb-delta-record unsafe-audit-report
           formal-provenance-record]}]
  {:gravity-reader-formal-release-governance-seed-retirement-verified?
   (= stage1-reader-formal-release-governance-seed-retirement-entrypoint
      (:gravity-entrypoint artifact))
   :gravity-reader-source-verified?
   (= :complete (get-in artifact
                        [:stage1-bootstrap-source-artifact
                         :capability-based-proof :status]))
   :formal-release-governance-authored?
   (and (= :gravity-reader-formal-release-governance-v1
           (:engine formal-governance))
        (= :gravity-source (get-in formal-governance [:provenance :owner]))
        (= :reader-formal-release-governance-assumption-replacement
           (get-in formal-governance [:provenance :purpose])))
   :formal-governance-direct-stages-covered?
   (= stage1-reader-formal-release-governance-seed-retirement-required-stages
      direct-stages)
   :formal-governance-operations-covered?
   (set/subset?
    (set stage1-reader-formal-release-governance-seed-retirement-required-operations)
    operation-names)
   :formal-governance-links-release-attestation?
   (= :stage1-reader-release-attestation-seed-retirement
      (:release-attestation-seed-retirement formal-governance))
   :release-attestation-authored?
   (= :gravity-reader-release-attestation-seed-retirement-v1
      (:engine release-attestation))
   :diverse-bootstrap-verification-authored?
   (= :gravity-reader-diverse-bootstrap-verification-v1
      (:engine diverse-verification))
   :verified-boot-chain-authored?
   (= :gravity-reader-verified-boot-chain-v1 (:engine boot-chain))
   :artifact-routing-covered?
   (= :gravity/stage1-reader-formal-release-governance-seed-retirement-artifact
      (:artifact formal-governance) (:kind artifact))
   :diagnostic-stream-routing-covered?
   (= :gravity/stage1-reader-formal-release-governance-seed-retirement-diagnostic-stream
      (:diagnostic-stream formal-governance)
      (get-in artifact
              [:stage1-reader-formal-release-governance-seed-retirement-diagnostic-stream
               :artifact]))
   :human-release-governance-boundary-replaced?
   (false? (get-in artifact
                   [:trusted-boundary :human-release-governance-boundary?]))
   :legal-custody-record-retention-boundary-replaced?
   (false? (get-in artifact
                   [:trusted-boundary
                    :legal-custody-record-retention-boundary?]))
   :deployment-environment-custody-boundary-replaced?
   (false? (get-in artifact
                   [:trusted-boundary
                    :deployment-environment-custody-boundary?]))
   :residual-release-governance-boundaries-empty?
   (= [] (:residual-release-governance-boundaries artifact))
   :formal-release-governance-record-covered?
   (and (= :verified (:status formal-record))
        (= :gravity-release-policy-v1 (:policy-engine formal-record))
        (= :versioned-rfc-ledger (:governance-source formal-record))
        (= :machine-checkable-governance (:approval-model formal-record))
        (true? (:human-release-governance-replaced? formal-record))
        (true? (:legal-custody-record-retention-replaced? formal-record))
        (true? (:deployment-environment-custody-replaced? formal-record)))
   :deployment-custody-record-covered?
   (and (= :verifiable (:status deployment-custody))
        (= :content-addressed-release-graph
           (:custody-model deployment-custody))
        (= :policy-verified (:builder-selection deployment-custody))
        (= :hash-and-policy-gated (:artifact-admission deployment-custody))
        (= :disabled (:network deployment-custody))
        (= :forbidden (:mutable-state deployment-custody)))
   :self-hosting-evidence-covered?
   (and (= :verified (:status self-hosting-evidence))
        (= :stage1-reader-claimed-subset (:scope self-hosting-evidence))
        (= :verified (:staged-compiler-artifact self-hosting-evidence))
        (= :verified (:reproducible-rebuild-log self-hosting-evidence))
        (= :verified (:conformance-report self-hosting-evidence))
        (= :verified (:stage-comparison-report self-hosting-evidence))
        (= :verified (:provenance-attestation self-hosting-evidence))
        (= :verified (:tcb-delta self-hosting-evidence))
        (= :not-applicable (:unsafe-audit-report self-hosting-evidence))
        (true? (:claimed-subset-self-hosted? self-hosting-evidence))
        (false? (:full-language-compiler-self-hosted? self-hosting-evidence))
        (false? (:clojure-seed-retired? self-hosting-evidence)))
   :full-language-compiler-self-hosting-not-claimed?
   (false? (:full-language-compiler-self-hosted? self-hosting-evidence))
   :seed-retirement-evidence-covered?
   (and (= :recorded (:status seed-retirement-evidence))
        (true? (:claimed-subset-seed-retired? seed-retirement-evidence))
        (false? (:full-compiler-seed-retired? seed-retirement-evidence))
        (false? (:clojure-seed-retired? seed-retirement-evidence)))
   :tcb-delta-covered?
   (and (= :complete (:status tcb-delta-record))
        (= #{:human-release-governance :legal-custody-record-retention
             :deployment-environment-custody}
           (set (:removed tcb-delta-record)))
        (= #{:full-language-compiler-self-hosting}
           (set (:remaining tcb-delta-record)))
        (= [] (:new-unsafe-islands tcb-delta-record)))
   :unsafe-audit-covered?
   (and (= :not-applicable (:status unsafe-audit-report))
        (false? (:unsafe-required? unsafe-audit-report))
        (= [] (:unsafe-islands unsafe-audit-report)))
   :formal-release-provenance-covered?
   (and (= :complete (:status formal-provenance-record))
        (true? (:canonicalized formal-provenance-record))
        (true? (:auditor-traversable formal-provenance-record))
        (set/subset?
         #{:stage1-reader-formal-release-governance-seed-retirement
           :stage1-reader-release-attestation-seed-retirement
           :stage1-reader-diverse-bootstrap-verification
           :stage1-reader-verified-boot-chain}
         (set (:lineage formal-provenance-record))))})
