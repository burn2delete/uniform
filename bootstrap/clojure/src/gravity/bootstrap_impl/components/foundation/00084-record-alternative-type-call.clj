

(defn record-alternative-type-call!
  [checker ctx node operator args effects capabilities return-type spec]
  (when-let [kind (:alternative-type-kind spec)]
    (let [record {:node-id (:node-id node)
                  :operator operator
                  :alternative-type-kind kind
                  :profile (:profile @ctx)
                  :target (:target @ctx)
                  :effects effects
                  :capabilities capabilities
                  :return-type return-type
                  :source-span (:source-span node)
                  :generated-origin-chain (:generated-origin node)}]
      (case kind
        :provider
        (record-checker! checker :alternative-type-provider-declarations
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :version (or (dispatch-arg-value args 1)
                                              "fixture-1")
                                 :kind :type-system
                                 :profiles (or (dispatch-arg-value args 2)
                                               #{(:profile @ctx)})
                                 :targets (or (dispatch-arg-value args 3)
                                              #{(:target @ctx)})
                                 :facets (or (dispatch-arg-value args 4) #{})
                                 :features (or (dispatch-arg-value args 5) #{})
                                 :build-effects (or (dispatch-arg-value args 6)
                                                    #{})
                                 :capability-requirements (or (dispatch-arg-value args 7)
                                                              #{})
                                 :fact-schema (or (dispatch-arg-value args 8)
                                                  :gravity.types/facts-v1)
                                 :proof-schema (or (dispatch-arg-value args 9)
                                                   :gravity.proof/refinement-v1)
                                 :conformance-suite (or (dispatch-arg-value args 10)
                                                        :gravity.conformance/types-l5)
                                 :soundness-claim (or (dispatch-arg-value args 11)
                                                      :reference-equivalent)
                                 :known-restrictions (or (dispatch-arg-value args 12)
                                                         #{})
                                 :deterministic-selection? true
                                 :typed-core-metadata-recorded? true
                                 :lockfile-recorded? true}))

        :typed-core-lowering
        (record-checker! checker :alternative-type-lowering-rules
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :typed-core-version (or (dispatch-arg-value args 1)
                                                         :typed-core-v1)
                                 :lowering-rules (or (dispatch-arg-value args 2)
                                                     #{:expression-types
                                                       :binding-types
                                                       :function-types})
                                 :preserved-fields (or (dispatch-arg-value args 3)
                                                       #{:effects
                                                         :capabilities
                                                         :panic
                                                         :allocation
                                                         :resource
                                                         :ownership
                                                         :casts
                                                         :proof-refs
                                                         :profile
                                                         :source-span})
                                 :accepted-downstream? true
                                 :source-span-map :preserved
                                 :macro-generated-map :preserved}))

        :fact-export
        (record-checker! checker :alternative-type-fact-export-schemas
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :fact-schema (or (dispatch-arg-value args 1)
                                                  :gravity.types/facts-v1)
                                 :fact-families (or (dispatch-arg-value args 2)
                                                   #{:ownership :region
                                                     :linear
                                                     :initialization
                                                     :nullability :taint
                                                     :schema :domain
                                                     :capability-value})
                                 :consumable-by #{:optimizer
                                                  :safety-checker
                                                  :backend-lowerer
                                                  :documentation-tool
                                                  :package-auditor
                                                  :language-server}
                                 :serialized? true}))

        :proof-artifact
        (record-checker! checker :alternative-type-proof-artifacts
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :proof-id (dispatch-arg-value args 1)
                                 :proof-schema (or (dispatch-arg-value args 2)
                                                   :gravity.proof/refinement-v1)
                                 :status (or (dispatch-arg-value args 3)
                                             :profile-safe)
                                 :serialized? true
                                 :safe-optimization-evidence? true}))

        :runtime-check
        (record-checker! checker :alternative-type-runtime-check-records
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :boundary-id (dispatch-arg-value args 1)
                                 :source-type (or (dispatch-arg-value args 2)
                                                  "Dynamic")
                                 :target-type (or (dispatch-arg-value args 3)
                                                  "User")
                                 :status (or (dispatch-arg-value args 4)
                                             :runtime-checked)
                                 :failure-type (or (dispatch-arg-value args 5)
                                                   "DecodeError")
                                 :blame (or (dispatch-arg-value args 6)
                                            :caller)
                                 :source-span-recorded? true}))

        :diagnostic-map
        (record-checker! checker :alternative-type-diagnostic-mapping-records
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :diagnostic-id (dispatch-arg-value args 1)
                                 :generated-syntax-id (dispatch-arg-value args 2)
                                 :source-path (dispatch-arg-value args 3)
                                 :type-fact-id (or (dispatch-arg-value args 4)
                                                   :fact/type-1)
                                 :macro-expansion-provenance :preserved
                                 :generated-origin-chain :preserved
                                 :source-span-map :preserved}))

        :compatibility-report
        (record-checker! checker :alternative-type-compatibility-reports
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :reference-suite (or (dispatch-arg-value args 1)
                                                      :l5-reference)
                                 :soundness-claim (or (dispatch-arg-value args 2)
                                                      :reference-equivalent)
                                 :status (or (dispatch-arg-value args 3)
                                             :passed)
                                 :positive-fixtures :passed
                                 :negative-fixtures :passed}))

        :profile-soundness
        (record-checker! checker :alternative-type-profile-soundness-evidence
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :profile (or (dispatch-arg-value args 1)
                                              (:profile @ctx))
                                 :evidence-id (dispatch-arg-value args 2)
                                 :status (or (dispatch-arg-value args 3)
                                             :passed)
                                 :safe-profile-claim? true}))

        :effect-capability-preservation
        (record-checker! checker :alternative-type-effect-capability-records
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :function-effects (or (dispatch-arg-value args 1)
                                                       #{})
                                 :capability-requirements (or (dispatch-arg-value args 2)
                                                              #{})
                                 :panic-behavior (or (dispatch-arg-value args 3)
                                                     #{})
                                 :allocation-behavior (or (dispatch-arg-value args 4)
                                                          #{})
                                 :resource-behavior (or (dispatch-arg-value args 5)
                                                        #{})
                                 :effects-erased? false
                                 :capabilities-erased? false
                                 :l15-capability-facts-exported? true}))

        :ownership-facts
        (record-checker! checker :alternative-type-ownership-facts
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :fact-families (or (dispatch-arg-value args 1)
                                                   #{:borrow :region
                                                     :linear
                                                     :initialization})
                                 :memory-safety-consumer :l10
                                 :resource-release-consumer :l10
                                 :concurrency-transfer-consumer :l11
                                 :backend-layout-consumer :mir
                                 :complete? true}))

        :gradual-boundary
        (record-checker! checker :alternative-type-gradual-boundaries
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :source-type (or (dispatch-arg-value args 1)
                                                  "Dynamic")
                                 :target-type (or (dispatch-arg-value args 2)
                                                  "User")
                                 :runtime-check (or (dispatch-arg-value args 3)
                                                    :checked-cast)
                                 :blame (or (dispatch-arg-value args 4)
                                            :caller)
                                 :failure-type (or (dispatch-arg-value args 5)
                                                   "DecodeError")
                                 :explicit? true
                                 :legal-for-profile? true}))

        :domain-facts
        (record-checker! checker :alternative-type-domain-facts
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :facet-id (dispatch-arg-value args 1)
                                 :fact-families (or (dispatch-arg-value args 2)
                                                   #{:schema :nullability
                                                     :taint})
                                 :crosses-facet-boundary? true
                                 :serialized-in-typed-artifact? true}))

        :optimization-proof
        (record-checker! checker :alternative-type-optimization-proofs
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :optimization (dispatch-arg-value args 1)
                                 :proof-id (dispatch-arg-value args 2)
                                 :erased-check (or (dispatch-arg-value args 3)
                                                   :runtime-check)
                                 :proof-reference-retained? true
                                 :valid-for-profile? true
                                 :valid-for-target? true
                                 :serialized? true
                                 :stable-under-transform? true
                                 :invalidates-on #{:source :provider
                                                   :profile :target
                                                   :macro :facet
                                                   :grant}}))

        nil)
      record)))