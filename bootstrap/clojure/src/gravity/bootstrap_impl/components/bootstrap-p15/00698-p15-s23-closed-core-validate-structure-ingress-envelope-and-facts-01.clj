(defn- __gravity_bootstrap_closed_core_structure_ingress_envelope_and_facts_01 [state]
  (let [{:syms [source-path artifact mode expected-authority-evidence]} state
        effectful? (= :effectful-reference mode)
        authority-evidence (when effectful?
                             (get-in
                               artifact
                               [:source-core-input :authority-evidence]))
        execution-evidence (when effectful?
                             (get-in
                               artifact
                               [:authenticated-input :reference-execution-evidence]))
        nodes (:core-nodes artifact)
        plan-nodes (filterv :plan-node? nodes)
        node-ids (mapv :node-id nodes)
        paths (mapv :path nodes)
        node-origin-ids (set (map #(get-in % [:source :origin-id]) nodes))
        origin-table (:source-origin-table artifact)
        origin-closure (:origin-closure artifact)
        declared-effects (get-in artifact [:source-core-input :declared-effects])
        declared-capabilities (get-in
                                artifact
                                [:source-core-input :declared-capabilities])
        declared-exports (get-in artifact [:source-core-input :declared-exports])
        observed-operation-set (set (map :source-operation plan-nodes))
        recomputed-effect-requirements {:required-effects
                                        (reduce set/union #{} (map :effects nodes)),
                                        :required-capabilities
                                        (reduce
                                          set/union
                                          #{}
                                          (map :capabilities nodes))}
        entrypoint-visibility (get-in
                                artifact
                                [:source-core-input :entrypoint-visibility])
        node-by-id (into {} (map (juxt :node-id identity)) nodes)
        expected-managed-node-ids (p15-s23-closed-core-expected-managed-node-ids nodes)
        capability-proof-by-node-capability (into
                                              {}
                                              (map
                                                (fn
                                                  [proof]
                                                  [[(:core-node-id proof)
                                                    (:capability proof)]
                                                   proof]))
                                              (:capability-proof-records artifact))
        dependency-graph (p15-s23-closed-core-dependency-order-graph nodes)
        _ (when-not (and
                      (= dependency-graph (:dependency-order-graph artifact))
                      (= :passed (:status dependency-graph))
                      (true? (:all-dependencies-precede-consumers? dependency-graph))
                      (true? (:all-lexical-bindings-resolve? dependency-graph)))
            (p15-s23-closed-core-fail!
              "C6-EVAL-ORDER"
              source-path
              artifact
              {:missing-fact :pre-fact-proven-topological-dependency-closure}))
        recomputed-facts (let [pre-execution (p15-s23-closed-core-fact-tables
                                               nodes
                                               (get-in
                                                 artifact
                                                 [:source-core-input :module])
                                               (get-in
                                                 artifact
                                                 [:source-core-input :plan-id])
                                               mode
                                               authority-evidence)]
                           (if effectful?
                             (p15-s23-checked-core-bind-execution-audit-to-facts
                               pre-execution
                               nodes
                               execution-evidence)
                             pre-execution))
        function-nodes (filterv #(= :function (:kind %)) nodes)
        diagnostic-module {:module (get-in artifact [:source-core-input :module]),
                           :profile (:profile artifact),
                           :target (:source-target artifact),
                           :requested-target
                           (get-in
                             artifact
                             [:target-request-metadata :requested-target]),
                           :safety
                           (get-in artifact [:source-core-input :declared-safety])}
        enriched-subject (fn [node extra]
                           (p15-s23-closed-core-enriched-node-subject
                             artifact
                             node
                             diagnostic-module
                             extra))]
    (assoc
      state
      'effectful?
      effectful?
      'authority-evidence
      authority-evidence
      'execution-evidence
      execution-evidence
      'nodes
      nodes
      'plan-nodes
      plan-nodes
      'node-ids
      node-ids
      'paths
      paths
      'node-origin-ids
      node-origin-ids
      'origin-table
      origin-table
      'origin-closure
      origin-closure
      'declared-effects
      declared-effects
      'declared-capabilities
      declared-capabilities
      'declared-exports
      declared-exports
      'observed-operation-set
      observed-operation-set
      'recomputed-effect-requirements
      recomputed-effect-requirements
      'entrypoint-visibility
      entrypoint-visibility
      'node-by-id
      node-by-id
      'expected-managed-node-ids
      expected-managed-node-ids
      'capability-proof-by-node-capability
      capability-proof-by-node-capability
      'dependency-graph
      dependency-graph
      '_
      _
      'recomputed-facts
      recomputed-facts
      'function-nodes
      function-nodes
      'diagnostic-module
      diagnostic-module
      'enriched-subject
      enriched-subject)))
