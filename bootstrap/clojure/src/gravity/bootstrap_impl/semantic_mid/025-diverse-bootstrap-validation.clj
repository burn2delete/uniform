(defn- semantic-mid-validate-diverse-bootstrap!
  [{:keys [source-path comparison diverse-verification trace-value
           host-primitives seed-builtin-fallbacks
           seed-orchestration-fallbacks runner-fallbacks os-boundaries
           machine-boundaries trust-anchor-boundaries image-fallbacks
           boot-chain-fallbacks diverse-verification-fallbacks
           replaced-trust-anchor-boundaries residual-trust-boundaries]}
   artifact-base]
  (when-not (:forms-equal? comparison)
    (stage1-reader-diverse-bootstrap-verification-fail!
     "STAGE1DIV005" source-path comparison
     {:missing-fields [:stage0-form-parity]}))
  (when-not (= (:artifact diverse-verification) (:kind artifact-base))
    (stage1-reader-diverse-bootstrap-verification-fail!
     "STAGE1DIV005" source-path artifact-base
     {:missing-fields [:artifact]}))
  (when-not (= (:diagnostic-stream diverse-verification)
               (get-in artifact-base
                       [:stage1-reader-diverse-bootstrap-verification-diagnostic-stream
                        :artifact]))
    (stage1-reader-diverse-bootstrap-verification-fail!
     "STAGE1DIV009" source-path artifact-base
     {:missing-fields [:diagnostic-stream]}))
  (doseq [[diagnostic field value]
          [["STAGE1DIV008" :host-primitives host-primitives]
           ["STAGE1DIV008" :seed-builtin-fallbacks seed-builtin-fallbacks]
           ["STAGE1DIV008" :seed-orchestration-fallbacks
            seed-orchestration-fallbacks]
           ["STAGE1DIV008" :runner-fallbacks runner-fallbacks]
           ["STAGE1DIV008" :os-boundaries os-boundaries]
           ["STAGE1DIV008" :machine-boundaries machine-boundaries]
           ["STAGE1DIV008" :trust-anchor-boundaries trust-anchor-boundaries]
           ["STAGE1DIV008" :image-fallbacks image-fallbacks]
           ["STAGE1DIV008" :boot-chain-fallbacks boot-chain-fallbacks]
           ["STAGE1DIV008" :diverse-verification-fallbacks
            diverse-verification-fallbacks]]]
    (when (seq value)
      (stage1-reader-diverse-bootstrap-verification-fail!
       diagnostic source-path trace-value {field value})))
  (when-not (= #{:hardware-reset-vector
                 :firmware-root-of-trust
                 :external-auditor-key}
               (set replaced-trust-anchor-boundaries))
    (stage1-reader-diverse-bootstrap-verification-fail!
     "STAGE1DIV008" source-path trace-value
     {:replaced-trust-anchor-boundaries replaced-trust-anchor-boundaries}))
  (when-not (= #{:physical-device-manufacturing
                 :supply-chain-custody
                 :independent-diversity-review}
               (set residual-trust-boundaries))
    (stage1-reader-diverse-bootstrap-verification-fail!
     "STAGE1DIV006" source-path trace-value
     {:residual-trust-boundaries residual-trust-boundaries}))
  (when-not (and
             (false? (get-in artifact-base
                             [:trusted-boundary
                              :hardware-reset-vector-boundary?]))
             (false? (get-in artifact-base
                             [:trusted-boundary
                              :firmware-root-of-trust-boundary?]))
             (false? (get-in artifact-base
                             [:trusted-boundary
                              :external-auditor-key-boundary?]))
             (true? (get-in artifact-base
                            [:trusted-boundary
                             :physical-device-manufacturing-boundary?]))
             (true? (get-in artifact-base
                            [:trusted-boundary
                             :supply-chain-custody-boundary?]))
             (true? (get-in artifact-base
                            [:trusted-boundary
                             :independent-diversity-review-boundary?])))
    (stage1-reader-diverse-bootstrap-verification-fail!
     "STAGE1DIV009" source-path artifact-base
     {:missing-fields [:trusted-boundary]})))
