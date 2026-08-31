(defn- semantic-mid-validate-release-source-artifact!
  [{:keys [source-path comparison release-attestation trace-value
           host-primitives seed-builtin-fallbacks
           seed-orchestration-fallbacks runner-fallbacks os-boundaries
           machine-boundaries trust-anchor-boundaries
           physical-release-boundaries residual-trust-boundaries
           image-fallbacks boot-chain-fallbacks
           diverse-verification-fallbacks release-attestation-fallbacks
           replaced-physical-release-boundaries
           residual-release-governance-boundaries revocation-check-report]}
   artifact-base]
  (when-not (:forms-equal? comparison)
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" source-path comparison
     {:missing-fields [:stage0-form-parity]}))
  (when-not (= (:artifact release-attestation) (:kind artifact-base))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" source-path artifact-base
     {:missing-fields [:artifact]}))
  (when-not (= (:diagnostic-stream release-attestation)
               (get-in artifact-base
                       [:stage1-reader-release-attestation-seed-retirement-diagnostic-stream
                        :artifact]))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" source-path artifact-base
     {:missing-fields [:diagnostic-stream]}))
  (doseq [[diagnostic field value]
          [["STAGE1REL008" :host-primitives host-primitives]
           ["STAGE1REL008" :seed-builtin-fallbacks seed-builtin-fallbacks]
           ["STAGE1REL008" :seed-orchestration-fallbacks
            seed-orchestration-fallbacks]
           ["STAGE1REL008" :runner-fallbacks runner-fallbacks]
           ["STAGE1REL008" :os-boundaries os-boundaries]
           ["STAGE1REL008" :machine-boundaries machine-boundaries]
           ["STAGE1REL008" :trust-anchor-boundaries trust-anchor-boundaries]
           ["STAGE1REL008" :physical-release-boundaries
            physical-release-boundaries]
           ["STAGE1REL008" :residual-trust-boundaries
            residual-trust-boundaries]
           ["STAGE1REL008" :image-fallbacks image-fallbacks]
           ["STAGE1REL008" :boot-chain-fallbacks boot-chain-fallbacks]
           ["STAGE1REL008" :diverse-verification-fallbacks
            diverse-verification-fallbacks]
           ["STAGE1REL008" :release-attestation-fallbacks
            release-attestation-fallbacks]]]
    (when (seq value)
      (stage1-reader-release-attestation-seed-retirement-fail!
       diagnostic source-path trace-value {field value})))
  (when-not (= #{:physical-device-manufacturing
                 :supply-chain-custody :independent-diversity-review}
               (set replaced-physical-release-boundaries))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL008" source-path trace-value
     {:replaced-physical-release-boundaries
      replaced-physical-release-boundaries}))
  (when-not (= #{:human-release-governance
                 :legal-custody-record-retention
                 :deployment-environment-custody}
               (set residual-release-governance-boundaries))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" source-path trace-value
     {:residual-release-governance-boundaries
      residual-release-governance-boundaries}))
  (when-not (and
             (false? (get-in artifact-base
                             [:trusted-boundary
                              :physical-device-manufacturing-boundary?]))
             (false? (get-in artifact-base
                             [:trusted-boundary
                              :supply-chain-custody-boundary?]))
             (false? (get-in artifact-base
                             [:trusted-boundary
                              :independent-diversity-review-boundary?]))
             (true? (get-in artifact-base
                            [:trusted-boundary
                             :human-release-governance-boundary?]))
             (true? (get-in artifact-base
                            [:trusted-boundary
                             :legal-custody-record-retention-boundary?]))
             (true? (get-in artifact-base
                            [:trusted-boundary
                             :deployment-environment-custody-boundary?])))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" source-path artifact-base
     {:missing-fields [:trusted-boundary]}))
  (when-not (= :clear (:status revocation-check-report))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL009" source-path revocation-check-report
     {:missing-fields [:revocation-check-report]})))
