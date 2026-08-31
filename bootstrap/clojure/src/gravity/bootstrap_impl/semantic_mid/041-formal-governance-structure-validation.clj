(defn- semantic-mid-validate-formal-governance-structure!
  [{:keys [reader-source-path formal-governance direct-stages]}]
  (when-not (map? formal-governance)
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV003" reader-source-path formal-governance
     {:missing-fields
      [:stage1-reader-formal-release-governance-seed-retirement]}))
  (doseq [field [:engine :entrypoint :replaces
                 :release-attestation-seed-retirement
                 :diverse-bootstrap-verification :verified-boot-chain
                 :input :output :artifact :diagnostic-stream :proof-kind
                 :residual-release-governance-boundaries
                 :formal-release-governance-fallbacks
                 :formal-governance-operations
                 :formal-release-governance-record
                 :deployment-custody-record :self-hosting-evidence
                 :seed-retirement-evidence :tcb-delta-record
                 :unsafe-audit-report :formal-release-provenance-record
                 :direct-stages :uses-runtimes :uses-builtins
                 :uses-executors :preserves :diagnostics :provenance]]
    (when-not (contains? formal-governance field)
      (stage1-reader-formal-release-governance-seed-retirement-fail!
       "STAGE1GOV010" reader-source-path formal-governance
       {:missing-fields [field]})))
  (when-not (= :gravity-reader-formal-release-governance-v1
               (:engine formal-governance))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" reader-source-path formal-governance
     {:missing-fields [:engine]}))
  (when-not (= :stage1-read-source-formal-release-governance-seed-retirement
               (:entrypoint formal-governance))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" reader-source-path formal-governance
     {:missing-fields [:entrypoint]}))
  (when-not (set/subset?
             #{:human-release-governance :legal-custody-record-retention
               :deployment-environment-custody}
             (set (:replaces formal-governance)))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" reader-source-path formal-governance
     {:missing-fields [:replaces]}))
  (doseq [[expected actual field]
          [[:stage1-reader-release-attestation-seed-retirement
            (:release-attestation-seed-retirement formal-governance)
            :release-attestation-seed-retirement]
           [:stage1-reader-diverse-bootstrap-verification
            (:diverse-bootstrap-verification formal-governance)
            :diverse-bootstrap-verification]
           [:stage1-reader-verified-boot-chain
            (:verified-boot-chain formal-governance) :verified-boot-chain]
           [[:formal-release-governance :deployment-custody
             :self-hosting-evidence :release-attestation
             :source-path :source-text]
            (:input formal-governance) :input]
           [:gravity/stage1-reader-form-records
            (:output formal-governance) :output]
           [:gravity/stage1-reader-formal-release-governance-seed-retirement-artifact
            (:artifact formal-governance) :artifact]
           [:gravity/stage1-reader-formal-release-governance-seed-retirement-diagnostic-stream
            (:diagnostic-stream formal-governance) :diagnostic-stream]
           [:gravity/stage1-reader-formal-release-governance-seed-retirement-proof
            (:proof-kind formal-governance) :proof-kind]]]
    (when-not (= expected actual)
      (stage1-reader-formal-release-governance-seed-retirement-fail!
       "STAGE1GOV010" reader-source-path formal-governance
       {:missing-fields [field]})))
  (when-not (= [] (:residual-release-governance-boundaries
                   formal-governance))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV009" reader-source-path formal-governance
     {:residual-release-governance-boundaries
      (:residual-release-governance-boundaries formal-governance)}))
  (when-not (= [] (:formal-release-governance-fallbacks formal-governance))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV009" reader-source-path formal-governance
     {:formal-release-governance-fallbacks
      (:formal-release-governance-fallbacks formal-governance)}))
  (let [operations (:formal-governance-operations formal-governance)
        operation-names (set operations)
        required-operation-set
        (set stage1-reader-formal-release-governance-seed-retirement-required-operations)]
    (when-not (vector? operations)
      (stage1-reader-formal-release-governance-seed-retirement-fail!
       "STAGE1GOV002" reader-source-path formal-governance
       {:missing-fields [:formal-governance-operations]}))
    (when-not (set/subset? required-operation-set operation-names)
      (stage1-reader-formal-release-governance-seed-retirement-fail!
       "STAGE1GOV002" reader-source-path operations
       {:missing-operations
        (vec (remove operation-names
                     stage1-reader-formal-release-governance-seed-retirement-required-operations))})))
  (when-not (and (vector? direct-stages)
                 (= stage1-reader-formal-release-governance-seed-retirement-required-stages
                    (mapv :op direct-stages)))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" reader-source-path formal-governance
     {:missing-fields [:direct-stages]})))
