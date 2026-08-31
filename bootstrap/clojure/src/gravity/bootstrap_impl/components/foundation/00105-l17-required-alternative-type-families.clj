

(def l17-required-alternative-type-families
  [:provider :typed-core-lowering :fact-export :proof-artifact
   :runtime-check :diagnostic-map :reference-compatibility
   :profile-soundness :effect-preservation :capability-preservation
   :ownership-facts :gradual-boundary :domain-fact :optimization-proof
   :source-span :macro-generated-map :safe-profile-evidence
   :l15-capability-export])

(defn alternative-type-conformance-fixture
  [checker-state]
  (let [providers (:alternative-type-provider-declarations checker-state)
        lowerings (:alternative-type-lowering-rules checker-state)
        fact-schemas (:alternative-type-fact-export-schemas checker-state)
        proofs (:alternative-type-proof-artifacts checker-state)
        runtime-checks (:alternative-type-runtime-check-records checker-state)
        diagnostic-maps (:alternative-type-diagnostic-mapping-records
                         checker-state)
        compatibility (:alternative-type-compatibility-reports checker-state)
        soundness (:alternative-type-profile-soundness-evidence checker-state)
        effect-capability (:alternative-type-effect-capability-records
                           checker-state)
        ownership (:alternative-type-ownership-facts checker-state)
        gradual (:alternative-type-gradual-boundaries checker-state)
        domain (:alternative-type-domain-facts checker-state)
        optimization (:alternative-type-optimization-proofs checker-state)
        covered (cond-> #{}
                  (seq providers) (conj :provider)
                  (seq lowerings) (conj :typed-core-lowering)
                  (seq fact-schemas) (conj :fact-export)
                  (seq proofs) (conj :proof-artifact)
                  (seq runtime-checks) (conj :runtime-check)
                  (seq diagnostic-maps) (conj :diagnostic-map)
                  (some #(= :passed (:status %)) compatibility)
                  (conj :reference-compatibility)
                  (some #(= :passed (:status %)) soundness)
                  (conj :profile-soundness)
                  (some #(false? (:effects-erased? %)) effect-capability)
                  (conj :effect-preservation)
                  (some #(false? (:capabilities-erased? %)) effect-capability)
                  (conj :capability-preservation)
                  (seq ownership) (conj :ownership-facts)
                  (seq gradual) (conj :gradual-boundary)
                  (seq domain) (conj :domain-fact)
                  (some :proof-reference-retained? optimization)
                  (conj :optimization-proof)
                  (or (some #(= :preserved (:source-span-map %)) lowerings)
                      (some #(= :preserved (:source-span-map %))
                            diagnostic-maps)
                      (some :source-span-recorded? runtime-checks))
                  (conj :source-span)
                  (or (some #(= :preserved (:macro-generated-map %))
                            lowerings)
                      (some #(= :preserved
                                (:macro-expansion-provenance %))
                            diagnostic-maps))
                  (conj :macro-generated-map)
                  (some :safe-profile-claim? soundness)
                  (conj :safe-profile-evidence)
                  (or (some #(contains? (:fact-families %)
                                        :capability-value)
                            fact-schemas)
                      (some :l15-capability-facts-exported?
                            effect-capability))
                  (conj :l15-capability-export))
        missing (vec (remove covered l17-required-alternative-type-families))]
    {:required-families l17-required-alternative-type-families
     :covered-families (vec (sort-by name covered))
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(def l18-required-alternative-memory-families
  [:provider :deterministic-selection :allocation-strategy
   :lifetime-facts :aliasing-facts :ownership-region-escape
   :unsafe-boundary-audit :layout-metadata :runtime-check
   :release-evidence :device-map :mmio-map :ffi-allocator
   :conformance-report :safety-classification :safe-code-outcomes
   :hidden-allocation-policy :artifact-serialization])

(defn alternative-memory-conformance-fixture
  [checker-state]
  (let [providers (:alternative-memory-provider-declarations checker-state)
        allocation (:alternative-memory-allocation-strategies checker-state)
        lifetime (:alternative-memory-lifetime-facts checker-state)
        audits (:alternative-memory-unsafe-boundary-audits checker-state)
        layouts (:alternative-memory-layout-metadata checker-state)
        runtime-checks (:alternative-memory-runtime-checks checker-state)
        releases (:alternative-memory-release-evidence checker-state)
        devices (:alternative-memory-device-maps checker-state)
        ffi (:alternative-memory-ffi-allocator-records checker-state)
        conformance (:alternative-memory-conformance-reports checker-state)
        classifications (:alternative-memory-safety-classifications
                         checker-state)
        outcomes (set (mapcat :outcomes classifications))
        covered (cond-> #{}
                  (seq providers) (conj :provider)
                  (some :deterministic-selection? providers)
                  (conj :deterministic-selection)
                  (seq allocation) (conj :allocation-strategy)
                  (seq lifetime) (conj :lifetime-facts)
                  (some #(contains? (:fact-families %) :aliasing) lifetime)
                  (conj :aliasing-facts)
                  (some #(and (contains? (:fact-families %) :ownership)
                              (contains? (:fact-families %) :region)
                              (contains? (:fact-families %) :escape))
                        lifetime)
                  (conj :ownership-region-escape)
                  (some :safe-api-boundary? audits)
                  (conj :unsafe-boundary-audit)
                  (some :layout-facts-serialized? layouts)
                  (conj :layout-metadata)
                  (some #(= :runtime-checked (:status %)) runtime-checks)
                  (conj :runtime-check)
                  (some #(= :no-leak (:leak-status %)) releases)
                  (conj :release-evidence)
                  (seq devices) (conj :device-map)
                  (some :mmio-map devices) (conj :mmio-map)
                  (some #(= :compatible (:compatibility %)) ffi)
                  (conj :ffi-allocator)
                  (some #(= :passed (:status %)) conformance)
                  (conj :conformance-report)
                  (seq classifications) (conj :safety-classification)
                  (set/subset? #{:proven-safe :runtime-checked
                                 :rejected :unsafe-island}
                               outcomes)
                  (conj :safe-code-outcomes)
                  (some #(false? (:hidden? %)) allocation)
                  (conj :hidden-allocation-policy)
                  (or (some :serialized? lifetime)
                      (some :layout-facts-serialized? layouts))
                  (conj :artifact-serialization))
        missing (vec (remove covered l18-required-alternative-memory-families))]
    {:required-families l18-required-alternative-memory-families
     :covered-families (vec (sort-by name covered))
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(def l19-required-interop-families
  [:native-abi :managed-host :schema :process :network
   :foreign-declaration :boundary-metadata :generated-binding
   :safe-wrapper :type-mapping :ownership :error-translation
   :capability-effect :migration-shim :parity-report :compatibility
   :schema-drift :profile-rejection :versioned-reproducible])

(defn interop-conformance-fixture
  [checker-state]
  (let [declarations (:interop-foreign-binding-declarations checker-state)
        boundary-families (set (map :boundary-family declarations))
        metadata (:interop-boundary-metadata checker-state)
        generated (:interop-generated-binding-provenance checker-state)
        wrappers (:interop-safe-wrapper-audits checker-state)
        type-maps (:interop-type-mapping-records checker-state)
        ownership (:interop-ownership-lifetime-maps checker-state)
        errors (:interop-error-translation-maps checker-state)
        capability-effects (:interop-capability-effect-records checker-state)
        shims (:interop-migration-shim-records checker-state)
        parity (:interop-parity-test-reports checker-state)
        compatibility (:interop-compatibility-records checker-state)
        drift (:interop-schema-drift-records checker-state)
        profile-rejections (:interop-profile-rejection-records checker-state)
        covered (cond-> #{}
                  (contains? boundary-families :native-abi)
                  (conj :native-abi)
                  (contains? boundary-families :managed-host)
                  (conj :managed-host)
                  (contains? boundary-families :schema)
                  (conj :schema)
                  (contains? boundary-families :process)
                  (conj :process)
                  (contains? boundary-families :network)
                  (conj :network)
                  (seq declarations) (conj :foreign-declaration)
                  (seq metadata) (conj :boundary-metadata)
                  (seq generated) (conj :generated-binding)
                  (some #(= :passed (:audit-status %)) wrappers)
                  (conj :safe-wrapper)
                  (some #(= :passed (:round-trip-test %)) type-maps)
                  (conj :type-mapping)
                  (seq ownership) (conj :ownership)
                  (some #(false? (:untranslated? %)) errors)
                  (conj :error-translation)
                  (some #(and (:effect-enforced? %)
                              (:capability-enforced? %))
                        capability-effects)
                  (conj :capability-effect)
                  (seq shims) (conj :migration-shim)
                  (some #(= :passed (:status %)) parity)
                  (conj :parity-report)
                  (seq compatibility) (conj :compatibility)
                  (some #(= :passed (:status %)) drift)
                  (conj :schema-drift)
                  (some #(= :rejected (:status %)) profile-rejections)
                  (conj :profile-rejection)
                  (and (some :versioned? metadata)
                       (some :reproducible? metadata)
                       (some :lockfile-entry compatibility))
                  (conj :versioned-reproducible))
        missing (vec (remove covered l19-required-interop-families))]
    {:required-families l19-required-interop-families
     :covered-families (vec (sort-by name covered))
     :missing-families missing
     :boundary-families-covered (vec (sort-by name boundary-families))
     :status (if (empty? missing) :complete :incomplete)}))