(defn- semantic-mid-formal-governance-proof-context
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:stage1-reader-formal-release-governance-seed-retirement-diagnostic-stream
                           :diagnostics])))
        formal-governance
        (:stage1-reader-formal-release-governance-seed-retirement artifact)]
    {:artifact artifact
     :diagnostics diagnostics
     :formal-governance formal-governance
     :release-attestation
     (:stage1-reader-release-attestation-seed-retirement artifact)
     :diverse-verification
     (:stage1-reader-diverse-bootstrap-verification artifact)
     :boot-chain (:stage1-reader-verified-boot-chain artifact)
     :character-stream (:stage1-reader-character-stream artifact)
     :token-stream (:stage1-reader-token-stream artifact)
     :records (:stage1-reader-records artifact)
     :operation-names
     (set (:formal-governance-operations formal-governance))
     :direct-stages (mapv :op (:direct-stages formal-governance))
     :gravity-runtimes (set (:gravity-runtimes artifact))
     :gravity-executors (set (:gravity-executors artifact))
     :formal-record (:formal-release-governance-record formal-governance)
     :deployment-custody (:deployment-custody-record formal-governance)
     :self-hosting-evidence (:self-hosting-evidence formal-governance)
     :seed-retirement-evidence (:seed-retirement-evidence formal-governance)
     :tcb-delta-record (:tcb-delta-record formal-governance)
     :unsafe-audit-report (:unsafe-audit-report formal-governance)
     :formal-provenance-record
     (:formal-release-provenance-record formal-governance)}))
