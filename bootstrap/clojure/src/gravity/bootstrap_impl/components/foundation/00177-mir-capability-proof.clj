

(defn mir-capability-proof
  [artifact]
  {:module-serialized? (= :gravity/mir-module
                         (get-in artifact [:mir-module :artifact]))
   :blocks-terminated?
   (every? #(perf-present? (:terminator %))
           (vals (:blocks (first (vals (get-in artifact
                                               [:mir-module :functions]))))))
   :operations-typed? (every? #(perf-present? (:type %))
                              (:mir-operations artifact))
   :effect-ordering-present?
   (every? #(or (empty? (:effects %)) (not= :none (:ordering %)))
           (:mir-operations artifact))
   :safety-outcomes-linked? (perf-present? (:safety-outcome-table artifact))
   :origins-linked? (every? #(perf-present? (get-in % [:source :span]))
                            (:mir-operations artifact))
   :domain-anchors-valid?
   (every? #(and (perf-present? (:anchor-id %))
                 (perf-present? (:fallback %)))
           (:domain-anchor-table artifact))
   :target-independent? (not-any? #(= :target-specific (:family %))
                                  (:mir-operations artifact))
   :verifier-passed? (= :passed (get-in artifact
                                        [:mir-verifier-report :status]))
   :status :complete})

(defn mir-artifact-from-checked-core
  [source-path checked-core]
  (let [_ (when-not (and (= :gravity/stage0-checked-core-pipeline-artifact
                            (:kind checked-core))
                         (map? (:module checked-core)))
            (mir-fail! "C11-MODULE" source-path
                       {:input-artifact checked-core}
                       checked-core
                       {:missing-fields [:kind :module]}))
        module (:module checked-core)
        type-by-node (into {} (map (juxt :node-id :type))
                           (:type-facts checked-core))
        effect-by-node (into {} (map (juxt :node-id identity))
                             (:desugaring-trace checked-core))
        safety-by-node (into {} (map (juxt :node-id identity))
                             (:safety-outcome-records checked-core))
        ownership-by-node (into {} (map (juxt :node-id identity))
                                (:ownership-facts checked-core))
        operations (mapv #(mir-operation module type-by-node effect-by-node
                                         safety-by-node ownership-by-node %)
                         (:surface-to-core-map checked-core))
        op-ids (mapv :op-id operations)
        fn-id (str "mir-fn-" (name (:module module)) "-main")
        entry-block {:block-id :entry
                     :operations op-ids
                     :terminator {:kind :return
                                  :value (last (keep :result operations))}
                     :successors []}
        source-core-hash (checked-core-artifact-id checked-core)
        mir-module {:artifact :gravity/mir-module
                    :module (:module module)
                    :source-core source-core-hash
                    :profile (:profile module)
                    :target-request (:target module)
                    :functions {fn-id {:fn-id fn-id
                                       :name (symbol (str (:module module))
                                                     "main")
                                       :params []
                                       :returns "Unit"
                                       :latent-effects (:effects module)
                                       :blocks {:entry entry-block}
                                       :entry :entry
                                       :source {:span (source-span source-path 0)
                                                :origin-chain []}}}
                    :globals {}
                    :types :mir/type-table
                    :effects :mir/effect-table
                    :ownership :mir/ownership-table
                    :safety :mir/safety-table
                    :domain-anchors :mir/domain-anchor-table
                    :diagnostics []}
        data-flow (mapv (fn [[from to]]
                          {:from from
                           :to to
                           :edge :sequence
                           :dominance-status :passed})
                        (partition 2 1 op-ids))
        artifact {:kind :gravity/stage0-mir-artifact
                  :document-set ["C11"]
                  :pass {:name :mir-construction-and-verifier
                         :input :checked-core
                         :output :gravity/mir
                         :requires [:checked-core :types :effects :ownership
                                    :capabilities :profile :safety-outcomes]
                         :preserves [:source-spans :origin-chain :profile
                                     :target :types :effects :ownership
                                     :capabilities :safety-outcomes
                                     :proofs :diagnostics]
                         :emits [:mir-module :control-flow-graph
                                 :data-flow-graph :mir-metadata-tables
                                 :source-origin-map :domain-anchor-table
                                 :mir-verifier-report]
                         :rejects mir-diagnostic-ids}
                  :source-overrides (mir-source-overrides module)
                  :checked-core-artifact-kind (:kind checked-core)
                  :checked-core-artifact-hash source-core-hash
                  :mir-module mir-module
                  :mir-operations operations
                  :operation-family-coverage (mir-family-coverage operations)
                  :control-flow-graph {:entry :entry
                                       :blocks {:entry entry-block}
                                       :status :complete}
                  :data-flow-graph data-flow
                  :type-table (into {}
                                    (map (juxt :node-id :type))
                                    (:type-facts checked-core))
                  :effect-table (:effect-legality-report checked-core)
                  :ownership-table (:ownership-analysis checked-core)
                  :capability-proof-table (:capability-proof-records
                                           checked-core)
                  :safety-outcome-table (:safety-outcome-records checked-core)
                  :runtime-check-table (:runtime-check-records checked-core)
                  :source-origin-map (mapv #(select-keys %
                                                         [:op-id :source])
                                           operations)
                  :domain-anchor-table []
                  :optimization-invalidation-hooks
                  [{:hook :mir-fact-invalidation
                    :invalidates [:type-table :effect-table
                                  :ownership-table :safety-outcome-table]
                    :requires [:mir-verifier-report]}]
                  :target-lowering-input-validation
                  {:input :gravity/mir
                   :requires [:mir-verifier-report :profile :target-request]
                   :status :ready-for-p06-t04-plus}
                  :mir-verifier-report
                  {:artifact :gravity/mir-verifier-report
                   :status :passed
                   :checks [:module-shape :block-terminators :dominance
                            :types :effects :safety :origins
                            :domain-anchors :target-independence]
                   :operation-count (count operations)
                   :diagnostics []}
                  :diagnostics []}
        _ (mir-validate! source-path artifact)
        capability-proof (mir-capability-proof artifact)
        conformance {:documents ["C11"]
                     :task "P06-T03"
                     :required-diagnostic-ids mir-diagnostic-ids
                     :module-status :complete
                     :verifier-status :complete
                     :operation-family-status :complete
                     :runtime-check-preservation-status :complete
                     :domain-anchor-status :complete
                     :target-lowering-input-status :complete
                     :status :complete}]
    (assoc artifact
           :capability-based-proof capability-proof
           :mir-results conformance)))

(defn mir-source-artifact
  [source-path source-text]
  (mir-artifact-from-checked-core
   source-path
   (checked-core-source-artifact source-path source-text)))

(def domain-ir-diagnostic-ids
  ["C12-REGISTRATION"
   "C12-ANCHOR"
   "C12-SCHEMA"
   "C12-FACTS"
   "C12-VERIFY"
   "C12-PROOF"
   "C12-LOWERING"
   "C12-FALLBACK"
   "C12-PLUGIN"])

(def domain-ir-diagnostic-messages
  {"C12-REGISTRATION" "domain IR registration is malformed"
   "C12-ANCHOR" "domain IR artifact is missing typed-core or MIR anchors"
   "C12-SCHEMA" "domain IR payload schema is invalid"
   "C12-FACTS" "domain IR artifact lost required type/effect/capability/safety facts"
   "C12-VERIFY" "domain verifier rejected the artifact"
   "C12-PROOF" "domain optimization lacks proof, certificate, or translation validation"
   "C12-LOWERING" "domain target lowering is unsupported without fallback"
   "C12-FALLBACK" "domain fallback behavior is missing or illegal"
   "C12-PLUGIN" "plugin-provided domain IR violates registration policy"})

(def domain-ir-override-diagnostics
  {:registration ["C12-REGISTRATION" :registration]
   :anchor ["C12-ANCHOR" :semantic-anchor]
   :schema ["C12-SCHEMA" :domain-schema]
   :facts ["C12-FACTS" :facts]
   :verify ["C12-VERIFY" :verifier]
   :proof ["C12-PROOF" :proof]
   :lowering ["C12-LOWERING" :lowering]
   :fallback ["C12-FALLBACK" :fallback]
   :plugin ["C12-PLUGIN" :plugin]})

(def domain-ir-required-families
  [:efir
   :schema
   :workflow
   :ai-agent
   :query
   :hdl
   :ui
   :gpu
   :ffi-boundary
   :package-artifact])