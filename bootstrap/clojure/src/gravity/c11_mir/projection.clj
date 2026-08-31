(ns gravity.c11-mir.projection)
(defn
 artifact-base
 [configuration
  module
  c10
  operations
  mir-module
  data-flow
  anchors
  diagnostics
  verifier
  overrides]
 {:ownership-table
  (get-in c10 [:c9-ownership-checker-artifact :ownership-graph]),
  :diagnostics [],
  :mir-operations operations,
  :task "P06-D090",
  :operation-family-coverage
  (mapv
   (fn [family] {:family family, :status :represented-by-operation})
   (:c11-mir-required-operation-families configuration)),
  :effect-table
  (into {} (map (fn [op] [(:op-id op) (:effects op)])) operations),
  :domain-anchor-table anchors,
  :document-set ["C11"],
  :c11-mir-spec-results
  {:metadata-table-status :complete,
   :function-block-operation-status :complete,
   :target-lowering-input-status :complete,
   :runtime-check-preservation-status :complete,
   :task "P06-D090",
   :diagnostic-status :complete,
   :module-status :complete,
   :documents ["C11"],
   :status :complete,
   :optimization-invalidation-status :complete,
   :verifier-status (:status verifier),
   :required-diagnostic-ids (:c11-mir-diagnostic-ids configuration),
   :domain-anchor-status :complete},
  :source-origin-map
  (mapv
   (fn* [p1__138#] (select-keys p1__138# [:op-id :source]))
   operations),
  :governing-document (:c11-mir-governing-document configuration),
  :target-lowering-input-validation
  {:input :gravity/mir,
   :requires
   [:mir-verifier-report
    :profile
    :target-request
    :runtime-check-table
    :safety-outcome-table],
   :status :ready-for-target-lowering},
  :mir-diagnostic-stream diagnostics,
  :optimization-invalidation-hooks
  [{:hook :c11-mir-fact-invalidation,
    :invalidates
    [:type-table
     :effect-table
     :ownership-table
     :safety-outcome-table
     :domain-anchor-table],
    :requires [:mir-verifier-report :proof-certificate-table],
    :status :recorded}],
  :metadata-tables
  {:proofs :c11/proof-table,
   :domain-anchors :c11/domain-anchor-table,
   :capabilities :c11/capability-table,
   :runtime-checks :c11/runtime-check-table,
   :types :c11/type-table,
   :profile-target :c11/profile-target-table,
   :status :complete,
   :effects :c11/effect-table,
   :safety :c11/safety-table,
   :ownership :c11/ownership-table,
   :source-origins :c11/source-origin-table},
  :c10-safety-analysis-artifact
  (select-keys
   c10
   [:kind
    :artifact-id
    :safety-operation-inventory
    :safety-outcome-records
    :runtime-check-list
    :capability-based-proof]),
  :module
  (select-keys
   module
   [:module
    :source-path
    :profile
    :target
    :effects
    :capabilities
    :safety
    :metadata]),
  :mir-module mir-module,
  :kind :gravity/stage0-c11-mir-spec-artifact,
  :mir-verifier-report verifier,
  :pass
  {:name :c11-mir-specification,
   :input :safety-checked-core,
   :output :gravity/mir,
   :requires
   [:c10-safety-analysis
    :types
    :effects
    :ownership
    :capabilities
    :safety-outcomes
    :profile
    :target],
   :preserves
   [:source-spans
    :origin-chain
    :profile
    :target
    :types
    :effects
    :ownership
    :capabilities
    :safety-outcomes
    :proofs
    :diagnostics],
   :emits
   [:mir-module
    :mir-operations
    :control-flow-graph
    :data-flow-graph
    :metadata-tables
    :source-origin-map
    :domain-anchor-table
    :mir-verifier-report
    :mir-diagnostic-stream],
   :rejects (:c11-mir-diagnostic-ids configuration)},
  :type-table
  (into
   {}
   (map (fn [op] [(:result op) (:type op)]))
   (filter :result operations)),
  :capability-proof-table
  (get-in
   c10
   [:c9-ownership-checker-artifact :capability-based-proof]),
  :safety-outcome-table (:safety-outcome-records c10),
  :control-flow-graph
  {:entry :entry,
   :blocks
   (get-in
    mir-module
    [:functions (first (keys (:functions mir-module))) :blocks]),
   :status :complete},
  :proof-certificate-table (:proof-certificate-references c10),
  :source-overrides overrides,
  :runtime-check-table (:runtime-check-list c10),
  :data-flow-graph data-flow})
