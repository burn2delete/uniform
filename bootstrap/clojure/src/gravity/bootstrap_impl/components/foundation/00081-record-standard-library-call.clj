

(defn record-standard-library-call!
  [checker ctx node operator args effects capabilities return-type spec]
  (when-let [kind (:standard-library-kind spec)]
    (let [record {:node-id (:node-id node)
                  :operator operator
                  :standard-library-kind kind
                  :profile (:profile @ctx)
                  :target (:target @ctx)
                  :effects effects
                  :capabilities capabilities
                  :return-type return-type
                  :source-span (:source-span node)}]
      (case kind
        :namespace-contract
        (record-checker! checker :standard-library-namespace-contracts
                         (merge record
                                {:namespace (dispatch-arg-value args 0)
                                 :supported-profiles (or (dispatch-arg-value args 1) #{})
                                 :stability (or (dispatch-arg-value args 2) :experimental)
                                 :allocation (or (dispatch-arg-value args 3) :declared)
                                 :since (or (dispatch-arg-value args 4) "stage0")
                                 :contract-source :namespace-metadata}))

        :api-contract
        (record-checker! checker :standard-library-api-contracts
                         (merge record
                                {:symbol (dispatch-arg-value args 0)
                                 :classification (or (dispatch-arg-value args 1) :pure)
                                 :supported-profiles (or (dispatch-arg-value args 2) #{})
                                 :declared-effects (or (dispatch-arg-value args 3) #{})
                                 :declared-capabilities (or (dispatch-arg-value args 4) #{})
                                 :allocation (or (dispatch-arg-value args 5) :none)
                                 :panic (or (dispatch-arg-value args 6) :none)
                                 :blocking (or (dispatch-arg-value args 7) :nonblocking)
                                 :nondeterminism (or (dispatch-arg-value args 8) :deterministic)
                                 :stability :stable}))

        :profile-availability
        (record-checker! checker :standard-library-profile-availability-reports
                         (merge record
                                {:namespace (dispatch-arg-value args 0)
                                 :available-profiles (or (dispatch-arg-value args 1) #{})
                                 :unavailable-profiles (or (dispatch-arg-value args 2) #{})
                                 :generated-from :actual-stage0-contracts}))

        :example
        (record-checker! checker :standard-library-documentation-examples
                         (merge record
                                {:namespace (dispatch-arg-value args 0)
                                 :profile (or (dispatch-arg-value args 1) (:profile @ctx))
                                 :example-id (or (dispatch-arg-value args 2) :stage0-example)
                                 :compile-status :passed
                                 :negative? (true? (dispatch-arg-value args 3))}))

        :unsafe-wrapper
        (record-checker! checker :standard-library-unsafe-wrapper-audits
                         (merge record
                                {:wrapper (dispatch-arg-value args 0)
                                 :unsafe-namespace (or (dispatch-arg-value args 1)
                                                       'gravity.memory.unsafe)
                                 :invariant (or (dispatch-arg-value args 2)
                                                :bounds-and-alignment-checked)
                                 :proof [:stage0-test :runtime-check]
                                 :profile-gates (or (dispatch-arg-value args 3)
                                                    #{:native})
                                 :audit-status :proved
                                 :safe-surface true}))

        :compatibility
        (record-checker! checker :standard-library-compatibility-records
                         (merge record
                                {:namespace (dispatch-arg-value args 0)
                                 :event (or (dispatch-arg-value args 1) :minor-addition)
                                 :from-version (or (dispatch-arg-value args 2) "0.1.0")
                                 :to-version (or (dispatch-arg-value args 3) "0.1.1")
                                 :migration-recorded? true
                                 :profile-support-change (or (dispatch-arg-value args 4) :none)}))

        :numeric-mode
        (record-checker! checker :standard-library-numeric-mode-records
                         (merge record
                                {:symbol (dispatch-arg-value args 0)
                                 :width (or (dispatch-arg-value args 1) :i64)
                                 :overflow (or (dispatch-arg-value args 2) :checked)
                                 :rounding (or (dispatch-arg-value args 3) :exact)
                                 :floating-mode (or (dispatch-arg-value args 4) :deterministic)
                                 :rewrite-proof :required-for-optimization}))

        :resource-api
        (record-checker! checker :standard-library-resource-records
                         (merge record
                                {:symbol (dispatch-arg-value args 0)
                                 :resource (or (dispatch-arg-value args 1) :file)
                                 :lifetime :linear
                                 :release :required
                                 :failure-mode :result
                                 :blocking (or (dispatch-arg-value args 2) :may-block)}))

        nil)
      record)))

(defn record-facet-call!
  [checker ctx node operator args effects capabilities return-type spec]
  (when-let [kind (:facet-kind spec)]
    (let [record {:node-id (:node-id node)
                  :operator operator
                  :facet-kind kind
                  :profile (:profile @ctx)
                  :target (:target @ctx)
                  :effects effects
                  :capabilities capabilities
                  :return-type return-type
                  :source-span (:source-span node)
                  :generated-origin-chain (:generated-origin node)}]
      (case kind
        :manifest
        (record-checker! checker :facet-manifests
                         (merge record
                                {:facet-id (dispatch-arg-value args 0)
                                 :version (or (dispatch-arg-value args 1) "0.1.0")
                                 :surface (or (dispatch-arg-value args 2) #{})
                                 :profiles (or (dispatch-arg-value args 3) #{})
                                 :requires-facets (or (dispatch-arg-value args 4) #{})
                                 :build-effects (or (dispatch-arg-value args 5) #{})
                                 :capabilities-declared (or (dispatch-arg-value args 6) #{})
                                 :lowers-to (or (dispatch-arg-value args 7) #{:gravity.core})
                                 :artifacts (or (dispatch-arg-value args 8) #{})
                                 :stability (or (dispatch-arg-value args 9) :experimental)
                                 :schema-version "stage0-facet-manifest-v1"}))

        :activation
        (record-checker! checker :facet-activation-records
                         (merge record
                                {:active-facets (or (dispatch-arg-value args 0) #{})
                                 :scope :namespace
                                 :activation-source :namespace-metadata
                                 :lexical? true}))

        :generated-code
        (record-checker! checker :facet-generated-gravity-records
                         (merge record
                                {:facet-id (dispatch-arg-value args 0)
                                 :generated-form (dispatch-arg-value args 1)
                                 :validation :passed-normal-gravity-checks
                                 :typecheck :passed
                                 :effect-check :passed
                                 :capability-check :passed
                                 :source-map :preserved
                                 :output-digest (l12-digest {:operator operator
                                                             :args (mapv :value args)})}))

        :domain-ir
        (record-checker! checker :facet-domain-ir-records
                         (merge record
                                {:facet-id (dispatch-arg-value args 0)
                                 :ir-kind (dispatch-arg-value args 1)
                                 :artifact-schema-version (or (dispatch-arg-value args 2)
                                                              "0.1.0")
                                 :source-map :preserved
                                 :generated-origin-map :preserved
                                 :type-annotations :preserved
                                 :effect-annotations :preserved
                                 :profile-assumptions #{(:profile @ctx)}
                                 :target-assumptions #{(:target @ctx)}
                                 :validation-results :passed
                                 :proof-obligations [:stage0-domain-check]}))

        :composition
        (record-checker! checker :facet-composition-records
                         (merge record
                                {:from-facet (dispatch-arg-value args 0)
                                 :to-facet (dispatch-arg-value args 1)
                                 :boundary (dispatch-arg-value args 2)
                                 :effects-visible true
                                 :capabilities-visible true
                                 :artifacts-linked true}))

        :privacy-boundary
        (record-checker! checker :facet-privacy-boundary-records
                         (merge record
                                {:facet-id (dispatch-arg-value args 0)
                                 :private-value (dispatch-arg-value args 1)
                                 :public-output-schema (dispatch-arg-value args 2)
                                 :disclosure-policy (dispatch-arg-value args 3)
                                 :witness-provenance :preserved
                                 :reveal-reason :declared
                                 :boundary-preserved? true}))

        :compatibility
        (record-checker! checker :facet-compatibility-records
                         (merge record
                                {:facet-id (dispatch-arg-value args 0)
                                 :from-version (or (dispatch-arg-value args 1) "0.1.0")
                                 :to-version (or (dispatch-arg-value args 2) "0.1.1")
                                 :event (or (dispatch-arg-value args 3) :migration)
                                 :migration-diagnostic :recorded
                                 :automatic-rewrite (contains? #{:deprecated :minor-addition}
                                                               (dispatch-arg-value args 3))}))

        nil)
      record)))