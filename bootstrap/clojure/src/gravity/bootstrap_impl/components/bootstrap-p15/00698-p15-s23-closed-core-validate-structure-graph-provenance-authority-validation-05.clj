(defn- __gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_05 [state]
  (let [{:syms
         [source-path
          artifact
          mode
          expected-authority-evidence
          effectful?
          authority-evidence
          execution-evidence
          nodes
          plan-nodes
          node-ids
          paths
          node-origin-ids
          origin-table
          origin-closure
          declared-effects
          declared-capabilities
          declared-exports
          observed-operation-set
          recomputed-effect-requirements
          entrypoint-visibility
          node-by-id
          expected-managed-node-ids
          capability-proof-by-node-capability
          dependency-graph
          _
          recomputed-facts
          function-nodes
          diagnostic-module
          enriched-subject]} state]
    (when-not (and
                (= (:effect-graph artifact) (:effect-graph recomputed-facts))
                (=
                  (:capability-proof-records artifact)
                  (:capability-proof-records recomputed-facts))
                (=
                  (:pure-capability-closure artifact)
                  (:pure-capability-closure recomputed-facts)))
      (p15-s23-closed-core-fail!
        "C8-VERIFY"
        source-path
        (enriched-subject
          (or (first nodes) {})
          {:missing-fact :canonical-effect-and-capability-envelope-closure})
        {:missing-fact :canonical-effect-and-capability-envelope-closure}))
    (when (and
            effectful?
            (let [graph (:effect-graph artifact)
                  proofs (:capability-proof-records artifact)
                  expected-pairs (set
                                   (for
                                     [node nodes capability (:capabilities node)]
                                     [(:node-id node) capability]))
                  observed-pairs (set (map (juxt :core-node-id :capability) proofs))
                  direct-event-ids (mapv
                                     :node-id
                                     (filterv
                                       #(seq
                                         (p15-s23-closed-core-intrinsic-effects
                                           (:source-operation %)))
                                       nodes))]
              (not
                (and
                  (= :complete (:status graph))
                  (true? (:all-edges-monotone? graph))
                  (true? (:edge-count-bounded? graph))
                  (false? (:exclusive-branch-total-order? graph))
                  (false? (:runtime-sequence-claimed? graph))
                  (= (mapv :node-id nodes) (:ordering-vertices graph))
                  (= direct-event-ids (:event-order graph))
                  (= (count (:event-order graph)) (count (set (:event-order graph))))
                  (every?
                    #(and
                      (contains? node-by-id (:before %))
                      (contains? node-by-id (:after %)))
                    (:event-edges graph))
                  (= expected-pairs observed-pairs)
                  (=
                    (count expected-pairs)
                    (count proofs)
                    (count (set (map :proof-id proofs))))
                  (every?
                    #(p15-s23-checked-core-capability-proof-record-valid?
                      %
                      authority-evidence)
                    proofs)))))
      (p15-s23-closed-core-fail!
        "C8-VERIFY"
        source-path
        artifact
        {:missing-fact
         :bounded-topological-effect-order-and-capability-proof-closure}))
    (when-not (= (:ownership-analysis artifact) (:ownership-analysis recomputed-facts))
      (p15-s23-closed-core-fail!
        "C6-VERIFY"
        source-path
        (enriched-subject
          (or (first nodes) {})
          {:missing-fact :canonical-ownership-analysis-envelope})
        {:missing-fact :canonical-ownership-analysis-envelope}))
    (when-not (every? #(some? (:type %)) nodes)
      (p15-s23-closed-core-fail!
        "C7-VERIFY"
        source-path
        artifact
        {:missing-fact :closed-core-node-type,
         :expected-type :resolved-gravity-type,
         :actual-type :missing,
         :relevant-binding-id :not-applicable}))
    (when-let [node (first
                      (filter
                        #(not=
                          (:type %)
                          (p15-s23-closed-core-recomputed-node-type % node-by-id))
                        nodes))]
      (let [expected-type (p15-s23-closed-core-recomputed-node-type node node-by-id)]
        (p15-s23-closed-core-fail!
          "C7-VERIFY"
          source-path
          (enriched-subject
            node
            {:missing-fact :operation-specific-reconstructed-type,
             :expected-type expected-type,
             :actual-type (:type node),
             :relevant-binding-id
             (or (get-in node [:attributes :resolved-binding]) :not-applicable)})
          {:missing-fact :operation-specific-reconstructed-type,
           :expected-type expected-type,
           :actual-type (:type node),
           :relevant-binding-id
           (or (get-in node [:attributes :resolved-binding]) :not-applicable)})))
    (when-not (= (:type-facts artifact) (:type-facts recomputed-facts))
      (let [node (or
                   (first
                     (filter
                       #(not=
                         (get (:type-facts artifact) (:node-id %))
                         (get (:type-facts recomputed-facts) (:node-id %)))
                       nodes))
                   (first nodes))]
        (p15-s23-closed-core-fail!
          "C7-VERIFY"
          source-path
          (enriched-subject
            node
            {:missing-fact :independently-recomputed-type-facts,
             :expected-type
             (get-in recomputed-facts [:type-facts (:node-id node) :type]),
             :actual-type (get-in artifact [:type-facts (:node-id node) :type]),
             :relevant-binding-id
             (or (get-in node [:attributes :resolved-binding]) :not-applicable)})
          {:missing-fact :independently-recomputed-type-facts})))
    state))
