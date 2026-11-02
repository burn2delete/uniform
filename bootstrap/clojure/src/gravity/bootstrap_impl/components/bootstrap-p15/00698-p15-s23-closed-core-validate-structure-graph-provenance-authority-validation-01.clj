(defn- __gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_01 [state]
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
    (when (and effectful? (not= expected-authority-evidence authority-evidence))
      (p15-s23-closed-core-fail!
        "C8-CAPABILITY"
        source-path
        artifact
        {:missing-fact :trusted-mode-exact-authority-evidence-reissue-parity}))
    (when-not (and
                (<= (count nodes) p15-s23-closed-core-max-derived-nodes)
                (<= (count plan-nodes) p15-s23-closed-core-max-plan-nodes)
                (every?
                  #(<= (:plan-depth %) p15-s23-closed-core-max-plan-depth)
                  nodes))
      (p15-s23-closed-core-fail!
        "C6-VERIFY"
        source-path
        artifact
        {:missing-fact :bounded-closed-core-artifact,
         :observed-derived-nodes (when (vector? nodes) (count nodes)),
         :observed-plan-nodes (when (vector? nodes) (count plan-nodes)),
         :maximum-derived-nodes p15-s23-closed-core-max-derived-nodes,
         :maximum-plan-nodes p15-s23-closed-core-max-plan-nodes,
         :maximum-plan-depth p15-s23-closed-core-max-plan-depth}))
    (when-not (and
                (= (count node-ids) (count (set node-ids)))
                (= (count paths) (count (set paths)))
                (every? #(= p15-s23-closed-core-node-keys (set (keys %))) nodes))
      (p15-s23-closed-core-fail!
        "C6-CORE-SHAPE"
        source-path
        artifact
        {:missing-fact :exact-unique-core-node-schema}))
    (when-let [node (first
                      (remove
                        #(p15-s23-closed-core-operation-shape-valid? % node-by-id)
                        nodes))]
      (p15-s23-closed-core-fail!
        "C6-CORE-SHAPE"
        source-path
        (enriched-subject
          node
          {:missing-fact :exact-pure-operation-attribute-and-operand-shape})
        {:missing-fact :exact-pure-operation-attribute-and-operand-shape}))
    state))
