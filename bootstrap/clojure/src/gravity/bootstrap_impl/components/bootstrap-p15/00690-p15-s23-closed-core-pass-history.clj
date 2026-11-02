

(defn p15-s23-closed-core-pass-history
  []
  [{:pass :c6-closed-core-lowering
    :owner-document "C6"
    :input :pre-execution-validated-stage2-plan-and-fresh-c2-c3
    :output :closed-core-nodes
    :requires [:validated-preflight-plan :fresh-c2-c3
               :lockstep-origin-map]
    :preserves [:source-spans :generated-origin :profile :diagnostics]
    :emits [:core-nodes :dependency-order-graph]
    :status :complete-for-pure-closed-slice}
   {:pass :c7-closed-type-derivation
    :owner-document "C7"
    :input :closed-core-nodes
    :output :closed-type-facts
    :requires [:resolved-lexical-bindings]
    :preserves [:source-origin :profile]
    :emits [:type-facts]
    :status :complete-for-pure-closed-slice}
   {:pass :c8-pure-effect-capability-closure
    :owner-document "C8"
    :input :typed-closed-core
    :output :effected-closed-core
    :requires [:empty-declared-effects :empty-declared-capabilities
               :runtime-module-effectful-residual-rejected]
    :preserves [:types :source-origin :profile]
    :emits [:effect-facts :capability-facts]
    :status :complete-for-pure-closed-slice}
   {:pass :c9-persistent-immutable-value-derivation
    :owner-document "C9"
    :input :effected-closed-core
    :output :ownership-checked-closed-core
    :requires [:lexical-binding-records
               :persistent-immutable-value-semantics]
    :not-applicable [:allocation-ownership :borrows :moves :transfers]
    :preserves [:types :effects :capabilities :source-origin :profile]
    :emits [:ownership-facts]
    :status :complete-for-pure-closed-slice}
   {:pass :c10-pure-safety-proof-derivation
    :owner-document "C10"
    :input :ownership-checked-closed-core
    :output :safety-checked-closed-core
    :requires [:profile-facts :ownership-facts
               :empty-effect-and-capability-facts]
    :not-applicable [:runtime-checks :unsafe-islands :effectful-providers]
    :preserves [:types :effects :capabilities :ownership :source-origin
                :profile]
    :emits [:safety-facts]
    :status :complete-for-pure-closed-slice}])

(defn p15-s23-closed-core-pass-history-for-mode
  [mode]
  (if (= :effectful-reference mode)
    (mapv
     (fn [record]
       (-> record
           (assoc :status
                  :complete-for-authenticated-hosted-jvm-reference-interpreter-slice)
           (cond-> (= :c8-pure-effect-capability-closure (:pass record))
             (assoc :pass :c8-authenticated-program-authority-closure
                    :requires
                    [:declared-effects :declared-capabilities
                     :authenticated-program-provider-selections
                     :authenticated-program-grants]
                    :emits
                    [:effect-facts :capability-facts
                     :capability-proof-records]))
           (cond-> (= :c10-pure-safety-proof-derivation (:pass record))
             (assoc :pass :c10-reference-runtime-check-derivation
                    :requires
                    [:profile-facts :ownership-facts
                     :managed-allocation-check
                     :reference-transcript-delivery-check]
                    :not-applicable [:unsafe-islands]
                    :emits [:safety-facts :runtime-check-evidence]))))
     (p15-s23-closed-core-pass-history))
    (p15-s23-closed-core-pass-history)))

(defn p15-s23-closed-core-type-producer-rule
  [source-operation]
  (case source-operation
    :implicit-nil :compiler-generated-nil
    :literal :literal-type
    :quote :quoted-scalar-type
    :local :resolved-binding-type
    :let-binding :initializer-type
    :truthy :truthiness-bool
    :if :branch-type-join
    :do :sequence-result-type
    :let :lexical-scope-result-type
    :function :closed-function-type
    :str :managed-str-string-result-type
    :println :println-nil-result-type
    :not-applicable))

(defn p15-s23-closed-core-effect-event-ordering
  [nodes]
  (let [direct-event?
        #(seq (p15-s23-closed-core-intrinsic-effects
               (:source-operation %)))
        ordering-vertices (mapv :node-id nodes)
        ordering-index
        (into (sorted-map)
              (map-indexed (fn [index node-id] [node-id index]))
              ordering-vertices)
        event-order (mapv :node-id (filterv direct-event? nodes))
        event-index
        (into (sorted-map)
              (map-indexed (fn [index node-id] [node-id index]))
              event-order)
        entry-vertex-by-node
        (reduce
         (fn [entries node]
           (assoc entries (:node-id node)
                  (if-let [first-operand (first (:operands node))]
                    (get entries first-operand first-operand)
                    (:node-id node))))
         (sorted-map) nodes)
        edges
        (reduce
         (fn [all node]
           (let [operands (:operands node)
                 dependency-edges
                 (set
                  (map-indexed
                   (fn [index operand]
                     {:before operand
                      :after (:node-id node)
                      :reason :operand-before-consumer
                      :operand-index index
                      :control-node-id (:node-id node)})
                   operands))
                 sequence-operation?
                 (contains? #{:do :let :function :str :println}
                            (:source-operation node))
                 sequence-edges
                 (if sequence-operation?
                   (set
                    (map-indexed
                     (fn [index [left right]]
                       {:before left
                        :after (get entry-vertex-by-node right right)
                        :reason :adjacent-source-sequence
                        :operand-index index
                        :control-node-id (:node-id node)})
                     (partition 2 1 operands)))
                   #{})
                 guard-edges
                 (if (= :if (:source-operation node))
                   (let [test-node (first operands)]
                     (set
                      (map-indexed
                       (fn [index branch-node]
                         {:before test-node
                          :after (get entry-vertex-by-node
                                      branch-node branch-node)
                          :reason :guard-before-exclusive-branch
                          :operand-index (inc index)
                          :control-node-id (:node-id node)})
                       (rest operands))))
                   #{})]
             (set/union all dependency-edges sequence-edges guard-edges)))
         #{} nodes)
        event-edges (vec (sort-by pr-str edges))
        maximum-edge-count (* 6 (count nodes))
        edges-bounded? (<= (count event-edges) maximum-edge-count)
        edges-monotone?
        (every? #(< (get ordering-index (:before %))
                     (get ordering-index (:after %)))
                event-edges)]
    {:ordering-vertices ordering-vertices
     :ordering-index ordering-index
     :entry-vertex-by-node entry-vertex-by-node
     :event-order event-order
     :event-index event-index
     :event-edges event-edges
     :event-order-kind
     :topological-core-order-with-guarded-control-flow-partial-order
     :exclusive-branch-total-order? false
     :runtime-sequence-claimed? false
     :maximum-edge-count maximum-edge-count
     :edge-count (count event-edges)
     :edge-count-bounded? edges-bounded?
     :all-edges-monotone? edges-monotone?
     :status (if (and edges-monotone? edges-bounded?)
               :complete :failed)}))