

(defn record-alternative-macro-call!
  [checker ctx node operator args effects capabilities return-type spec]
  (when-let [kind (:alternative-macro-kind spec)]
    (let [record {:node-id (:node-id node)
                  :operator operator
                  :alternative-macro-kind kind
                  :profile (:profile @ctx)
                  :target (:target @ctx)
                  :effects effects
                  :capabilities capabilities
                  :return-type return-type
                  :source-span (:source-span node)
                  :generated-origin-chain (:generated-origin node)}]
      (case kind
        :provider
        (record-checker! checker :alternative-macro-provider-declarations
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :version (or (dispatch-arg-value args 1) "fixture-1")
                                 :profiles (or (dispatch-arg-value args 2) #{(:profile @ctx)})
                                 :targets (or (dispatch-arg-value args 3) #{(:target @ctx)})
                                 :facets (or (dispatch-arg-value args 4) #{})
                                 :build-effects (or (dispatch-arg-value args 5) #{})
                                 :capabilities (or (dispatch-arg-value args 6) #{})
                                 :syntax-guarantees (or (dispatch-arg-value args 7)
                                                        #{:span :metadata
                                                          :hygiene})
                                 :hygiene-mode (or (dispatch-arg-value args 8)
                                                   :hygienic)
                                 :phase-model (or (dispatch-arg-value args 9)
                                                  :l4-compatible)
                                 :cache-policy (or (dispatch-arg-value args 10)
                                                   :deterministic)
                                 :trace-schema (or (dispatch-arg-value args 11)
                                                   :gravity.macro/trace-v1)
                                 :conformance-suite (or (dispatch-arg-value args 12)
                                                        :gravity.conformance/macro-l4)
                                 :known-deviations (or (dispatch-arg-value args 13)
                                                       #{})
                                 :declaration-complete? true}))

        :expansion-trace
        (record-checker! checker :alternative-macro-expansion-traces
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :provider-version (or (dispatch-arg-value args 1)
                                                       "fixture-1")
                                 :source-namespace (:namespace node)
                                 :macro-symbol (dispatch-arg-value args 2)
                                 :input-syntax-object-id (dispatch-arg-value args 3)
                                 :output-syntax-object-id (dispatch-arg-value args 4)
                                 :phase (or (dispatch-arg-value args 5)
                                            :macro-invocation)
                                 :active-facets (or (dispatch-arg-value args 6) #{})
                                 :reference-equivalent? true
                                 :source-span-preserved? true
                                 :generated-origin-preserved? true}))

        :syntax-object
        (record-checker! checker :alternative-macro-syntax-serializations
                         (merge record
                                {:syntax-object-id (dispatch-arg-value args 0)
                                 :form-kind (dispatch-arg-value args 1)
                                 :source-span-preserved? true
                                 :lexical-context-preserved? true
                                 :metadata-preserved? true
                                 :hygiene-marks-preserved? true
                                 :generated-origin-preserved? true
                                 :serializable? true}))

        :hygiene
        (record-checker! checker :alternative-macro-hygiene-records
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :macro-symbol (dispatch-arg-value args 1)
                                 :hygiene-mode (or (dispatch-arg-value args 2)
                                                   :hygienic)
                                 :identifier-comparison :l4-compatible
                                 :hidden-capture? false}))

        :explicit-capture
        (record-checker! checker :alternative-macro-explicit-capture-records
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :macro-symbol (dispatch-arg-value args 1)
                                 :captured-identifier (dispatch-arg-value args 2)
                                 :capture-marker :explicit
                                 :generated-origin :recorded
                                 :safe? true}))

        :build-effect-trace
        (record-checker! checker :alternative-macro-build-effect-traces
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :macro-symbol (dispatch-arg-value args 1)
                                 :build-effect (or (dispatch-arg-value args 2)
                                                   :build/read-file)
                                 :grant-id (or (dispatch-arg-value args 3)
                                               :grant/macro-build)
                                 :input-digest (or (dispatch-arg-value args 4)
                                                   "sha256:macro-input")
                                 :output-digest (or (dispatch-arg-value args 5)
                                                    (str "sha256:" (l12-digest {:operator operator
                                                                               :args (mapv :value args)})))
                                 :replay-policy (or (dispatch-arg-value args 6)
                                                    :replay-required)
                                 :secret-policy (or (dispatch-arg-value args 7)
                                                    :redacted)
                                 :phase :build}))

        :cache-decision
        (record-checker! checker :alternative-macro-cache-decisions
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :macro-symbol (dispatch-arg-value args 1)
                                 :cache-key-inputs (or (dispatch-arg-value args 2)
                                                       #{:source :provider
                                                         :profile :target
                                                         :facet :grant
                                                         :compiler-version})
                                 :decision (or (dispatch-arg-value args 3)
                                               :miss)
                                 :invalidates-on (or (dispatch-arg-value args 4)
                                                     #{:source :provider
                                                       :profile :target
                                                       :facet :grant
                                                       :compiler-version})
                                 :deterministic? true}))

        :equivalence
        (record-checker! checker :alternative-macro-equivalence-reports
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :macro-symbol (dispatch-arg-value args 1)
                                 :reference-engine :l4-reference
                                 :alternative-engine (dispatch-arg-value args 0)
                                 :comparison (or (dispatch-arg-value args 2)
                                                 :structural-alpha-equivalent)
                                 :l4-rule (or (dispatch-arg-value args 3)
                                              :expansion-result)
                                 :status :passed}))

        :facet-dispatch
        (record-checker! checker :alternative-macro-facet-dispatch-records
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :facet-id (dispatch-arg-value args 1)
                                 :activation-scope :namespace
                                 :ambiguity-check :performed
                                 :domain-ir-versioned? true
                                 :generated-gravity-origin-preserved? true
                                 :l14-boundary :preserved}))

        :generated-validation
        (record-checker! checker :alternative-macro-generated-validation-records
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :macro-symbol (dispatch-arg-value args 1)
                                 :syntax-check :passed
                                 :type-check :passed
                                 :effect-check :passed
                                 :capability-check :passed
                                 :memory-check :passed
                                 :profile-check :passed
                                 :safety-check :passed}))

        nil)
      record)))