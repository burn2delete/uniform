

(defn record-provider-call!
  [checker ctx node operator args effects capabilities return-type spec]
  (when-let [kind (:provider-kind spec)]
    (let [record {:node-id (:node-id node)
                  :operator operator
                  :provider-kind kind
                  :profile (:profile @ctx)
                  :target (:target @ctx)
                  :effects effects
                  :capabilities capabilities
                  :return-type return-type
                  :source-span (:source-span node)
                  :generated-origin-chain (:generated-origin node)}]
      (case kind
        :declaration
        (record-checker! checker :provider-declaration-records
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :version (or (dispatch-arg-value args 1) "fixture-1")
                                 :category (or (dispatch-arg-value args 2) :provider)
                                 :implements (or (dispatch-arg-value args 3) #{})
                                 :profiles (or (dispatch-arg-value args 4) #{(:profile @ctx)})
                                 :targets (or (dispatch-arg-value args 5) #{(:target @ctx)})
                                 :runtime-effects (or (dispatch-arg-value args 6) #{})
                                 :build-effects (or (dispatch-arg-value args 7) #{})
                                 :contracts (or (dispatch-arg-value args 8)
                                                #{'gravity.contracts/Provider})
                                 :failures (or (dispatch-arg-value args 9) #{})
                                 :trust-level (or (dispatch-arg-value args 10) :trusted-core)
                                 :artifact-schema (or (dispatch-arg-value args 11)
                                                      :gravity.provider/generic-v1)
                                 :conformance-suite (or (dispatch-arg-value args 12)
                                                        :gravity.conformance/provider)
                                 :declaration-complete? true}))

        :grant
        (record-checker! checker :grant-records
                         (merge record
                                {:grant-id (dispatch-arg-value args 0)
                                 :principal (dispatch-arg-value args 1)
                                 :capability (dispatch-arg-value args 2)
                                 :provider (dispatch-arg-value args 3)
                                 :scope (dispatch-arg-value args 4)
                                 :phase (or (dispatch-arg-value args 5) :runtime)
                                 :lifetime (or (dispatch-arg-value args 6) :process)
                                 :audit-policy (or (dispatch-arg-value args 7) :required)
                                 :status :granted}))

        :capability-value
        (record-checker! checker :capability-value-records
                         (merge record
                                {:capability (dispatch-arg-value args 0)
                                 :provider (dispatch-arg-value args 1)
                                 :scope (dispatch-arg-value args 2)
                                 :phase (or (dispatch-arg-value args 3) :runtime)
                                 :lifetime (or (dispatch-arg-value args 4) :process)
                                 :thread-safety (or (dispatch-arg-value args 5) :thread-confined)
                                 :explicit-api-value? true}))

        :selection
        (record-checker! checker :provider-selection-records
                         (merge record
                                {:capability (dispatch-arg-value args 0)
                                 :provider (dispatch-arg-value args 1)
                                 :version (or (dispatch-arg-value args 2) "fixture-1")
                                 :selection (or (dispatch-arg-value args 3) :profile-default)
                                 :selection-deterministic? true
                                 :scope (or (dispatch-arg-value args 4) :namespace)
                                 :phase (or (dispatch-arg-value args 5) :runtime)
                                 :trust-level (or (dispatch-arg-value args 6) :trusted-core)
                                 :artifact-schema (or (dispatch-arg-value args 7)
                                                      :gravity.provider/generic-v1)
                                 :conformance-suite (or (dispatch-arg-value args 8)
                                                        :gravity.conformance/provider)
                                 :active-profile (:profile @ctx)
                                 :target (:target @ctx)}))

        :scope-audit
        (record-checker! checker :capability-scope-audit-logs
                         (merge record
                                {:capability (dispatch-arg-value args 0)
                                 :category (or (dispatch-arg-value args 1)
                                               (provider-scope-kind (dispatch-arg-value args 0)))
                                 :requested-scope (dispatch-arg-value args 2)
                                 :grant-scope (dispatch-arg-value args 3)
                                 :within-grant? (true? (dispatch-arg-value args 4))
                                 :status (if (true? (dispatch-arg-value args 4))
                                           :accepted
                                           :rejected)}))

        :compile-time-replay
        (record-checker! checker :compile-time-provider-replay-records
                         (merge record
                                {:provider (dispatch-arg-value args 0)
                                 :capability (dispatch-arg-value args 1)
                                 :input-digest (dispatch-arg-value args 2)
                                 :output-digest (or (dispatch-arg-value args 3)
                                                    (str "sha256:" (l12-digest {:operator operator
                                                                               :args (mapv :value args)})))
                                 :replayable? (not (false? (dispatch-arg-value args 4)))
                                 :secret-policy (or (dispatch-arg-value args 5) :redacted)
                                 :phase :build
                                 :audit :recorded}))

        :runtime-manifest
        (record-checker! checker :runtime-provider-manifests
                         (merge record
                                {:provider (dispatch-arg-value args 0)
                                 :version (or (dispatch-arg-value args 1) "fixture-1")
                                 :capabilities (or (dispatch-arg-value args 2) #{})
                                 :scopes (or (dispatch-arg-value args 3) {})
                                 :trust-level (or (dispatch-arg-value args 4) :trusted-core)
                                 :contracts-checked? true
                                 :profiles #{(:profile @ctx)}
                                 :targets #{(:target @ctx)}
                                 :safety-audit :enabled}))

        :conformance
        (record-checker! checker :provider-conformance-results
                         (merge record
                                {:provider (dispatch-arg-value args 0)
                                 :suite (dispatch-arg-value args 1)
                                 :status (or (dispatch-arg-value args 2) :passed)
                                 :covered-categories (or (dispatch-arg-value args 3) #{})
                                 :contract-evidence :recorded}))

        :attenuation
        (record-checker! checker :capability-attenuation-records
                         (merge record
                                {:capability (dispatch-arg-value args 0)
                                 :parent-scope (dispatch-arg-value args 1)
                                 :child-scope (dispatch-arg-value args 2)
                                 :authority-narrowed? true
                                 :type-metadata-preserved? true
                                 :audit-log :recorded}))

        :revocation
        (record-checker! checker :capability-revocation-records
                         (merge record
                                {:capability (dispatch-arg-value args 0)
                                 :profile (or (dispatch-arg-value args 1) (:profile @ctx))
                                 :revocation-mode (or (dispatch-arg-value args 2)
                                                      :static-lifetime)
                                 :status (or (dispatch-arg-value args 3) :supported)
                                 :lifetime-evidence :recorded}))

        :replacement
        (record-checker! checker :provider-replacement-records
                         (merge record
                                {:capability (dispatch-arg-value args 0)
                                 :original-provider (dispatch-arg-value args 1)
                                 :replacement-provider (dispatch-arg-value args 2)
                                 :contract-status (or (dispatch-arg-value args 3)
                                                      :contract-preserved)
                                 :safety-status (or (dispatch-arg-value args 4)
                                                    :safe)
                                 :safe-semantics-preserved? true}))

        nil)
      record)))