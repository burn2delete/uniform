(defn- semantic-mid-validate-formal-source-artifact!
  [{:keys [source-path comparison formal-governance trace-value
           host-primitives seed-builtin-fallbacks
           seed-orchestration-fallbacks runner-fallbacks os-boundaries
           machine-boundaries trust-anchor-boundaries
           physical-release-boundaries residual-trust-boundaries
           residual-release-governance-boundaries image-fallbacks
           boot-chain-fallbacks diverse-verification-fallbacks
           release-attestation-fallbacks
           formal-release-governance-fallbacks
           replaced-release-governance-boundaries self-hosting-evidence]}
   artifact-base]
  (when-not (:forms-equal? comparison)
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV007" source-path comparison
     {:missing-fields [:stage0-form-parity]}))
  (when-not (= (:artifact formal-governance) (:kind artifact-base))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" source-path artifact-base
     {:missing-fields [:artifact]}))
  (when-not (= (:diagnostic-stream formal-governance)
               (get-in artifact-base
                       [:stage1-reader-formal-release-governance-seed-retirement-diagnostic-stream
                        :artifact]))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" source-path artifact-base
     {:missing-fields [:diagnostic-stream]}))
  (doseq [[diagnostic field value]
          [["STAGE1GOV009" :host-primitives host-primitives]
           ["STAGE1GOV009" :seed-builtin-fallbacks seed-builtin-fallbacks]
           ["STAGE1GOV009" :seed-orchestration-fallbacks
            seed-orchestration-fallbacks]
           ["STAGE1GOV009" :runner-fallbacks runner-fallbacks]
           ["STAGE1GOV009" :os-boundaries os-boundaries]
           ["STAGE1GOV009" :machine-boundaries machine-boundaries]
           ["STAGE1GOV009" :trust-anchor-boundaries trust-anchor-boundaries]
           ["STAGE1GOV009" :physical-release-boundaries
            physical-release-boundaries]
           ["STAGE1GOV009" :residual-trust-boundaries
            residual-trust-boundaries]
           ["STAGE1GOV009" :residual-release-governance-boundaries
            residual-release-governance-boundaries]
           ["STAGE1GOV009" :image-fallbacks image-fallbacks]
           ["STAGE1GOV009" :boot-chain-fallbacks boot-chain-fallbacks]
           ["STAGE1GOV009" :diverse-verification-fallbacks
            diverse-verification-fallbacks]
           ["STAGE1GOV009" :release-attestation-fallbacks
            release-attestation-fallbacks]
           ["STAGE1GOV009" :formal-release-governance-fallbacks
            formal-release-governance-fallbacks]]]
    (when (seq value)
      (stage1-reader-formal-release-governance-seed-retirement-fail!
       diagnostic source-path trace-value {field value})))
  (when-not (= #{:human-release-governance
                 :legal-custody-record-retention
                 :deployment-environment-custody}
               (set replaced-release-governance-boundaries))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV009" source-path trace-value
     {:replaced-release-governance-boundaries
      replaced-release-governance-boundaries}))
  (when-not (and
             (false? (get-in artifact-base
                             [:trusted-boundary
                              :human-release-governance-boundary?]))
             (false? (get-in artifact-base
                             [:trusted-boundary
                              :legal-custody-record-retention-boundary?]))
             (false? (get-in artifact-base
                             [:trusted-boundary
                              :deployment-environment-custody-boundary?]))
             (false? (get-in artifact-base
                             [:trusted-boundary :clojure-seed-retired?])))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" source-path artifact-base
     {:missing-fields [:trusted-boundary]}))
  (when (and (true? (:clojure-seed-retired? self-hosting-evidence))
             (not (true? (:full-language-compiler-self-hosted?
                          self-hosting-evidence))))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV005" source-path self-hosting-evidence
     {:missing-fields [:full-language-compiler-self-hosted?]})))
