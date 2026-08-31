

(defn record-compile-time-call!
  [checker ctx node operator args effects capabilities return-type spec]
  (when-let [kind (:compile-time-kind spec)]
    (let [build-effects (l12-build-effects effects)
          granted-build-effects (set/intersection build-effects (:build-grants @ctx))
          input-digest (l12-digest (mapv #(select-keys % [:type :value :node-id])
                                         args))
          output-digest (l12-digest {:operator operator
                                     :kind kind
                                     :return-type return-type
                                     :effects effects})
          event {:node-id (:node-id node)
                 :operator operator
                 :compile-time-kind kind
                 :phase :compile-time
                 :profile (:profile @ctx)
                 :target (:target @ctx)
                 :source-span (:source-span node)
                 :effects effects
                 :capabilities capabilities
                 :build-effects build-effects
                 :build-grants granted-build-effects
                 :hermetic? (:hermetic? @ctx)
                 :input-digest input-digest
                 :output-digest output-digest
                 :result-type return-type}]
      (record-checker! checker :compile-time-evaluation-trace event)
      (when (= :pure-constant kind)
        (record-checker! checker :constant-value-table
                         {:node-id (:node-id node)
                          :constant-id (str "stage0-const-" (:node-id node))
                          :type return-type
                          :stable-representation :stage0-edn
                          :value-digest output-digest
                          :target (:target @ctx)
                          :profile (:profile @ctx)
                          :source-span (:source-span node)}))
      (when (:generated-form? spec)
        (record-checker! checker :generated-form-provenance-records
                         {:node-id (:node-id node)
                          :generator operator
                          :phase :compile-time
                          :package (:namespace node)
                          :profile (:profile @ctx)
                          :target (:target @ctx)
                          :input-digest input-digest
                          :output-digest output-digest
                          :build-effects build-effects
                          :generated-origin-chain [{:generator operator
                                                    :phase :compile-time
                                                    :source-span (:source-span node)
                                                    :input-digest input-digest
                                                    :output-digest output-digest}]
                          :syntax-validation :passed
                          :typecheck :passed
                          :effect-check :passed
                          :capability-check :passed
                          :safety-check :passed}))
      (doseq [effect build-effects]
        (record-checker! checker :compile-time-capability-proof-records
                         {:node-id (:node-id node)
                          :effect effect
                          :grant :metadata-build-grant
                          :provider (l12-build-provider effect)
                          :scope (cond
                                   (= :build/read-file effect) (:declared-inputs @ctx)
                                   (= :build/target-probe effect) (:target-manifests @ctx)
                                   :else :namespace)
                          :status (if (contains? granted-build-effects effect)
                                    :authorized
                                    :missing)
                          :phase :compile-time
                          :profile (:profile @ctx)
                          :target (:target @ctx)}))
      (when (or (:hermetic? @ctx) (seq build-effects))
        (record-checker! checker :hermetic-replay-records
                         {:node-id (:node-id node)
                          :operator operator
                          :hermetic? (:hermetic? @ctx)
                          :declared-inputs (:declared-inputs @ctx)
                          :target-manifests (:target-manifests @ctx)
                          :build-grants granted-build-effects
                          :replay-policy (:replay-policy @ctx)
                          :compiler-version :stage0-clojure
                          :input-digest input-digest
                          :output-digest output-digest
                          :status :recorded}))
      (record-checker! checker :cache-key-records
                       {:node-id (:node-id node)
                        :operator operator
                        :cache-key (l12-digest {:operator operator
                                                :source (:source-span node)
                                                :profile (:profile @ctx)
                                                :target (:target @ctx)
                                                :grants (:build-grants @ctx)
                                                :declared-inputs (:declared-inputs @ctx)
                                                :target-manifests (:target-manifests @ctx)
                                                :replay-policy (:replay-policy @ctx)
                                                :compiler-version :stage0-clojure})
                        :reuse-decision (if (= :strict (:cache-policy @ctx))
                                          :reuse-allowed
                                          :rebuild-required)
                        :policy (:cache-policy @ctx)
                        :legal-under-current-policy? (= :strict (:cache-policy @ctx))})
      event)))

(defn record-compile-time-binding!
  [checker ctx node value-fact]
  (when-not (l12-stable-constant-representation? (:type value-fact))
    (typed-diagnostic! "L12-CONST-REPRESENTATION"
                       "compile-time constant lacks a stable target representation"
                       node
                       "Use a stable Gravity value, generated form, or artifact reference for defconst."
                       {:constant (:name node)
                        :type (:type value-fact)
                        :phase :compile-time
                        :profile (:profile @ctx)
                        :target (:target @ctx)}))
  (record-checker! checker :constant-value-table
                   {:node-id (:node-id node)
                    :constant-id (str (:namespace node) "/" (:name node))
                    :name (:name node)
                    :type (:type value-fact)
                    :stable-representation :stage0-edn
                    :value-digest (l12-digest (select-keys value-fact
                                                           [:type :value :node-id]))
                    :source-span (:source-span node)
                    :profile (:profile @ctx)
                    :target (:target @ctx)
                    :binding :defconst})
  (record-checker! checker :compile-time-evaluation-trace
                   {:node-id (:node-id node)
                    :operator 'defconst
                    :compile-time-kind :defconst
                    :phase :compile-time
                    :profile (:profile @ctx)
                    :target (:target @ctx)
                    :source-span (:source-span node)
                    :result-type (:type value-fact)
                    :constant (:name node)
                    :value-node-id (:node-id value-fact)
                    :hermetic? (:hermetic? @ctx)})
  (record-checker! checker :cache-key-records
                   {:node-id (:node-id node)
                    :operator 'defconst
                    :cache-key (l12-digest {:name (:name node)
                                            :value (:node-id value-fact)
                                            :profile (:profile @ctx)
                                            :target (:target @ctx)
                                            :compiler-version :stage0-clojure})
                    :reuse-decision (if (= :strict (:cache-policy @ctx))
                                      :reuse-allowed
                                      :rebuild-required)
                    :policy (:cache-policy @ctx)
                    :legal-under-current-policy? (= :strict (:cache-policy @ctx))}))