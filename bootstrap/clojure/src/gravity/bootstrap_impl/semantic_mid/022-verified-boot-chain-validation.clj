(defn- semantic-mid-validate-verified-boot-chain!
  [{:keys [source-path comparison boot-chain trace-value host-primitives
           seed-builtin-fallbacks seed-orchestration-fallbacks
           runner-fallbacks os-boundaries machine-boundaries
           image-fallbacks boot-chain-fallbacks replaced-machine-boundaries]}
   artifact-base]
  (when-not (:forms-equal? comparison)
    (stage1-reader-verified-boot-chain-fail!
     "STAGE1BOOT004" source-path comparison
     {:missing-fields [:stage0-form-parity]}))
  (when-not (= (:artifact boot-chain) (:kind artifact-base))
    (stage1-reader-verified-boot-chain-fail!
     "STAGE1BOOT004" source-path artifact-base
     {:missing-fields [:artifact]}))
  (when-not (= (:diagnostic-stream boot-chain)
               (get-in artifact-base
                       [:stage1-reader-verified-boot-chain-diagnostic-stream
                        :artifact]))
    (stage1-reader-verified-boot-chain-fail!
     "STAGE1BOOT008" source-path artifact-base
     {:missing-fields [:diagnostic-stream]}))
  (doseq [[field value]
          [[:host-primitives host-primitives]
           [:seed-builtin-fallbacks seed-builtin-fallbacks]
           [:seed-orchestration-fallbacks seed-orchestration-fallbacks]
           [:runner-fallbacks runner-fallbacks]
           [:os-boundaries os-boundaries]
           [:machine-boundaries machine-boundaries]
           [:image-fallbacks image-fallbacks]
           [:boot-chain-fallbacks boot-chain-fallbacks]]]
    (when (seq value)
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT007" source-path trace-value {field value})))
  (when-not (= #{:machine-instruction-dispatch
                 :kernel-process-scheduler
                 :artifact-loader}
               (set replaced-machine-boundaries))
    (stage1-reader-verified-boot-chain-fail!
     "STAGE1BOOT005" source-path trace-value
     {:replaced-machine-boundaries replaced-machine-boundaries}))
  (when-not (and (false? (get-in artifact-base
                                 [:trusted-boundary :machine-boundary?]))
                 (false? (get-in artifact-base
                                 [:trusted-boundary
                                  :kernel-process-scheduler-boundary?]))
                 (false? (get-in artifact-base
                                 [:trusted-boundary
                                  :artifact-loader-boundary?]))
                 (true? (get-in artifact-base
                                [:trusted-boundary
                                 :hardware-reset-vector-boundary?]))
                 (true? (get-in artifact-base
                                [:trusted-boundary
                                 :firmware-root-of-trust-boundary?]))
                 (true? (get-in artifact-base
                                [:trusted-boundary
                                 :external-auditor-key-boundary?])))
    (stage1-reader-verified-boot-chain-fail!
     "STAGE1BOOT008" source-path artifact-base
     {:missing-fields [:trusted-boundary]})))
