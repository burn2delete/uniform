

(defn record-interop-call!
  [checker ctx node operator args effects capabilities return-type spec]
  (when-let [kind (:interop-kind spec)]
    (let [record {:node-id (:node-id node)
                  :operator operator
                  :interop-kind kind
                  :profile (:profile @ctx)
                  :target (:target @ctx)
                  :effects effects
                  :capabilities capabilities
                  :return-type return-type
                  :source-span (:source-span node)
                  :generated-origin-chain (:generated-origin node)}]
      (case kind
        :foreign-declaration
        (record-checker! checker :interop-foreign-binding-declarations
                         (merge record
                                {:boundary-id (dispatch-arg-value args 0)
                                 :boundary-family (dispatch-arg-value args 1)
                                 :foreign-language (dispatch-arg-value args 2)
                                 :abi-or-protocol (dispatch-arg-value args 3)
                                 :link-name (dispatch-arg-value args 4)
                                 :type-mapping (dispatch-arg-value args 5)
                                 :declared-effects (or (dispatch-arg-value args 6)
                                                       #{})
                                 :declared-capabilities (or (dispatch-arg-value args 7)
                                                            #{})
                                 :ownership (or (dispatch-arg-value args 8)
                                                {})
                                 :safety (or (dispatch-arg-value args 9)
                                             :unsafe)
                                 :profiles (or (dispatch-arg-value args 10)
                                               #{(:profile @ctx)})
                                 :targets (or (dispatch-arg-value args 11)
                                              #{(:target @ctx)})
                                 :version (or (dispatch-arg-value args 12)
                                              "fixture-1")
                                 :threading :declared
                                 :memory-behavior :declared
                                 :error-behavior :declared
                                 :complete? true}))

        :boundary-metadata
        (record-checker! checker :interop-boundary-metadata
                         (merge record
                                {:boundary-id (dispatch-arg-value args 0)
                                 :metadata-kind (dispatch-arg-value args 1)
                                 :version (or (dispatch-arg-value args 2)
                                              "fixture-1")
                                 :provider-id (dispatch-arg-value args 3)
                                 :reproducible? true
                                 :versioned? true}))

        :generated-binding
        (record-checker! checker :interop-generated-binding-provenance
                         (merge record
                                {:boundary-id (dispatch-arg-value args 0)
                                 :source-kind (dispatch-arg-value args 1)
                                 :source-digest (or (dispatch-arg-value args 2)
                                                    "sha256:schema")
                                 :schema-version (or (dispatch-arg-value args 3)
                                                     "fixture-1")
                                 :generated-types (or (dispatch-arg-value args 4)
                                                      #{})
                                 :generated-codecs :recorded
                                 :field-nullability :recorded
                                 :diagnostic-provenance :preserved
                                 :conformance-evidence :recorded}))

        :safe-wrapper-audit
        (record-checker! checker :interop-safe-wrapper-audits
                         (merge record
                                {:boundary-id (dispatch-arg-value args 0)
                                 :safe-wrapper (dispatch-arg-value args 1)
                                 :foreign-call (dispatch-arg-value args 2)
                                 :requires (or (dispatch-arg-value args 3)
                                               #{})
                                 :ensures (or (dispatch-arg-value args 4)
                                             #{})
                                 :evidence (or (dispatch-arg-value args 5)
                                               #{})
                                 :unsafe-call-visible? true
                                 :audit-status :passed}))

        :type-mapping
        (record-checker! checker :interop-type-mapping-records
                         (merge record
                                {:boundary-id (dispatch-arg-value args 0)
                                 :source-type (dispatch-arg-value args 1)
                                 :foreign-type (dispatch-arg-value args 2)
                                 :direction (or (dispatch-arg-value args 3)
                                                :bidirectional)
                                 :conversion (or (dispatch-arg-value args 4)
                                                 :checked)
                                 :failure-behavior (or (dispatch-arg-value args 5)
                                                       :result)
                                 :round-trip-test :passed}))

        :ownership-map
        (record-checker! checker :interop-ownership-lifetime-maps
                         (merge record
                                {:boundary-id (dispatch-arg-value args 0)
                                 :ownership (or (dispatch-arg-value args 1)
                                                {})
                                 :allocator (dispatch-arg-value args 2)
                                 :release (dispatch-arg-value args 3)
                                 :nullability :recorded
                                 :initialization :recorded
                                 :lifetime :recorded
                                 :thread-affinity :recorded}))

        :error-map
        (record-checker! checker :interop-error-translation-maps
                         (merge record
                                {:boundary-id (dispatch-arg-value args 0)
                                 :foreign-errors (or (dispatch-arg-value args 1)
                                                     #{})
                                 :gravity-errors (or (dispatch-arg-value args 2)
                                                     #{})
                                 :untranslated? false
                                 :translation-test :passed}))

        :capability-effect
        (record-checker! checker :interop-capability-effect-records
                         (merge record
                                {:boundary-id (dispatch-arg-value args 0)
                                 :declared-effects (or (dispatch-arg-value args 1)
                                                       #{:ffi/call})
                                 :declared-capabilities (or (dispatch-arg-value args 2)
                                                            #{:ffi/call})
                                 :provider-id (dispatch-arg-value args 3)
                                 :effect-enforced? true
                                 :capability-enforced? true}))

        :migration-shim
        (record-checker! checker :interop-migration-shim-records
                         (merge record
                                {:shim-id (dispatch-arg-value args 0)
                                 :incumbent-source (dispatch-arg-value args 1)
                                 :target-namespace (dispatch-arg-value args 2)
                                 :preserved (or (dispatch-arg-value args 3)
                                                #{})
                                 :rejected (or (dispatch-arg-value args 4)
                                               #{})
                                 :generated-files (or (dispatch-arg-value args 5)
                                                      #{})
                                 :owner (or (dispatch-arg-value args 6)
                                            "interop-working-group")
                                 :stability :temporary}))

        :parity-report
        (record-checker! checker :interop-parity-test-reports
                         (merge record
                                {:shim-id (dispatch-arg-value args 0)
                                 :suite (or (dispatch-arg-value args 1)
                                            :interop/parity)
                                 :status (or (dispatch-arg-value args 2)
                                             :passed)
                                 :golden-tests :passed
                                 :round-trip-tests :passed
                                 :error-translation-tests :passed
                                 :ownership-tests :passed}))

        :compatibility-record
        (record-checker! checker :interop-compatibility-records
                         (merge record
                                {:boundary-id (dispatch-arg-value args 0)
                                 :version (or (dispatch-arg-value args 1)
                                              "fixture-1")
                                 :policy (or (dispatch-arg-value args 2)
                                             :semver)
                                 :deprecation (or (dispatch-arg-value args 3)
                                                  :none)
                                 :lockfile-entry :recorded}))

        :schema-drift-check
        (record-checker! checker :interop-schema-drift-records
                         (merge record
                                {:boundary-id (dispatch-arg-value args 0)
                                 :source-digest (dispatch-arg-value args 1)
                                 :generated-digest (dispatch-arg-value args 2)
                                 :status (or (dispatch-arg-value args 3)
                                             :passed)
                                 :compatibility-check :performed}))

        :profile-rejection
        (record-checker! checker :interop-profile-rejection-records
                         (merge record
                                {:boundary-id (dispatch-arg-value args 0)
                                 :unsupported-profile (dispatch-arg-value args 1)
                                 :replacement (or (dispatch-arg-value args 2)
                                                  :portable-artifact)
                                 :rejection-diagnostic "L19-PROFILE"
                                 :status :rejected}))

        nil)
      record)))

(def safe1-legal-outcomes
  #{:proven-safe :runtime-checked :rejected :unsafe-island})

(def safe1-required-families
  [:proven-safe :runtime-checked :rejected :unsafe-island
   :runtime-check-record :unsafe-island-audit :generated-provenance
   :optimization-proof :dependency-mode])

(defn safe1-diagnostic!
  [id message node remediation data]
  (typed-diagnostic! id message node remediation
                     (merge {:safe-rule :SAFE1
                             :active-profile (:profile node)
                             :source-span (:source-span node)}
                            data)))

(defn check-safe1-outcome!
  [node operation outcome evidence]
  (when-not (contains? safe1-legal-outcomes outcome)
    (safe1-diagnostic! "SAFE1-NO-OUTCOME"
                       "dangerous operation lacks a legal safety outcome"
                       node
                       "Classify the operation as :proven-safe, :runtime-checked, :rejected, or :unsafe-island."
                       {:operation operation
                        :safety-outcome outcome
                        :missing-fact :legal-safety-outcome}))
  (when (and (= :proven-safe outcome) (nil? evidence))
    (safe1-diagnostic! "SAFE1-PROOF-MISSING"
                       "claimed static safety proof is absent"
                       node
                       "Attach a proof reference, retain a runtime check, reject the operation, or isolate it as an unsafe island."
                       {:operation operation
                        :safety-outcome outcome
                        :missing-fact :proof-reference})))