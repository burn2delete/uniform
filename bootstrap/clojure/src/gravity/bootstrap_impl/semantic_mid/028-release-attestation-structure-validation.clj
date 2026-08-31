(defn- semantic-mid-validate-release-attestation-structure!
  [{:keys [reader-source-path release-attestation required-stages
           direct-stages]}]
  (when-not (map? release-attestation)
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL003" reader-source-path release-attestation
     {:missing-fields [:stage1-reader-release-attestation-seed-retirement]}))
  (doseq [field [:engine :entrypoint :replaces
                 :diverse-bootstrap-verification :verified-boot-chain
                 :input :output :artifact :diagnostic-stream :proof-kind
                 :physical-release-boundaries
                 :replaced-physical-release-boundaries
                 :residual-trust-boundaries
                 :residual-release-governance-boundaries
                 :release-attestation-fallbacks
                 :release-attestation-operations
                 :release-attestation-record :seed-retirement-evidence
                 :supply-chain-manifest :release-custody-record
                 :governance-approval-record :revocation-check-report
                 :release-provenance-record :direct-stages :uses-runtimes
                 :uses-builtins :uses-executors :preserves :diagnostics
                 :provenance]]
    (when-not (contains? release-attestation field)
      (stage1-reader-release-attestation-seed-retirement-fail!
       "STAGE1REL010" reader-source-path release-attestation
       {:missing-fields [field]})))
  (doseq [[expected actual field]
          [[:gravity-reader-release-attestation-seed-retirement-v1
            (:engine release-attestation) :engine]
           [:stage1-read-source-release-attestation-seed-retirement
            (:entrypoint release-attestation) :entrypoint]]]
    (when-not (= expected actual)
      (stage1-reader-release-attestation-seed-retirement-fail!
       "STAGE1REL010" reader-source-path release-attestation
       {:missing-fields [field]})))
  (when-not (set/subset? #{:physical-device-manufacturing
                           :supply-chain-custody
                           :independent-diversity-review}
                         (set (:replaces release-attestation)))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" reader-source-path release-attestation
     {:missing-fields [:replaces]}))
  (doseq [[expected actual field]
          [[:stage1-reader-diverse-bootstrap-verification
            (:diverse-bootstrap-verification release-attestation)
            :diverse-bootstrap-verification]
           [:stage1-reader-verified-boot-chain
            (:verified-boot-chain release-attestation)
            :verified-boot-chain]
           [[:release-attestation :seed-retirement
             :diverse-bootstrap-verification :verified-boot-chain
             :source-path :source-text]
            (:input release-attestation) :input]
           [:gravity/stage1-reader-form-records
            (:output release-attestation) :output]
           [:gravity/stage1-reader-release-attestation-seed-retirement-artifact
            (:artifact release-attestation) :artifact]
           [:gravity/stage1-reader-release-attestation-seed-retirement-diagnostic-stream
            (:diagnostic-stream release-attestation) :diagnostic-stream]
           [:gravity/stage1-reader-release-attestation-seed-retirement-proof
            (:proof-kind release-attestation) :proof-kind]]]
    (when-not (= expected actual)
      (stage1-reader-release-attestation-seed-retirement-fail!
       "STAGE1REL010" reader-source-path release-attestation
       {:missing-fields [field]})))
  (when-not (= [] (:physical-release-boundaries release-attestation))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL008" reader-source-path release-attestation
     {:physical-release-boundaries
      (:physical-release-boundaries release-attestation)}))
  (when-not (set/subset?
             #{:physical-device-manufacturing :supply-chain-custody
               :independent-diversity-review}
             (set (:replaced-physical-release-boundaries
                   release-attestation)))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" reader-source-path release-attestation
     {:missing-fields [:replaced-physical-release-boundaries]}))
  (when-not (= [] (:residual-trust-boundaries release-attestation))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL008" reader-source-path release-attestation
     {:residual-trust-boundaries
      (:residual-trust-boundaries release-attestation)}))
  (when-not (set/subset?
             #{:human-release-governance :legal-custody-record-retention
               :deployment-environment-custody}
             (set (:residual-release-governance-boundaries
                   release-attestation)))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" reader-source-path release-attestation
     {:missing-fields [:residual-release-governance-boundaries]}))
  (when-not (= [] (:release-attestation-fallbacks release-attestation))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL008" reader-source-path release-attestation
     {:release-attestation-fallbacks
      (:release-attestation-fallbacks release-attestation)}))
  (let [operations (:release-attestation-operations release-attestation)
        operation-names (set operations)
        required-operation-set
        (set stage1-reader-release-attestation-seed-retirement-required-operations)]
    (when-not (vector? operations)
      (stage1-reader-release-attestation-seed-retirement-fail!
       "STAGE1REL002" reader-source-path release-attestation
       {:missing-fields [:release-attestation-operations]}))
    (when-not (set/subset? required-operation-set operation-names)
      (stage1-reader-release-attestation-seed-retirement-fail!
       "STAGE1REL002" reader-source-path operations
       {:missing-operations
        (vec (remove operation-names
                     stage1-reader-release-attestation-seed-retirement-required-operations))})))
  (when-not (and (vector? direct-stages)
                 (= required-stages (mapv :op direct-stages)))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" reader-source-path release-attestation
     {:missing-fields [:direct-stages]})))
