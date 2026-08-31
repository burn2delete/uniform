(defn- __gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_06 [state]
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
                (every? #(set/subset? (:effects %) declared-effects) nodes)
                (every?
                  (fn [node]
                    (if (= :str (:source-operation node))
                      (contains? (:effects node) :memory/allocate)
                      true))
                  nodes)
                (every?
                  (fn [node]
                    (if (= :println (:source-operation node))
                      (contains? (:effects node) :io/write)
                      true))
                  nodes))
      (p15-s23-closed-core-fail!
        "C8-UNDECLARED"
        source-path
        artifact
        {:missing-fact :closed-effect-capability-declaration,
         :declared-effects declared-effects,
         :declared-capabilities declared-capabilities}))
    (when-not (and
                (every? #(set/subset? (:capabilities %) declared-capabilities) nodes)
                (every?
                  (fn [node]
                    (if (= :str (:source-operation node))
                      (contains? (:capabilities node) :memory/allocator)
                      true))
                  nodes)
                (every?
                  (fn [node]
                    (if (= :println (:source-operation node))
                      (contains? (:capabilities node) :io/stdout)
                      true))
                  nodes))
      (p15-s23-closed-core-fail!
        "C8-CAPABILITY"
        source-path
        artifact
        {:missing-fact :closed-capability-authority,
         :declared-capabilities declared-capabilities}))
    (when-not (every?
                (fn [node]
                  (let [expected (p15-s23-closed-core-node-aggregate-facts
                                   node
                                   node-by-id)]
                    (and
                      (= (:effects node) (:aggregate-effects expected))
                      (=
                        (select-keys
                          (:attributes node)
                          [:intrinsic-effects :aggregate-effects])
                        (select-keys
                          expected
                          [:intrinsic-effects :aggregate-effects])))))
                nodes)
      (p15-s23-closed-core-fail!
        "C8-VERIFY"
        source-path
        artifact
        {:missing-fact :independently-recomputed-intrinsic-and-aggregate-effects}))
    (when-not (every?
                (fn [node]
                  (let [expected (p15-s23-closed-core-node-aggregate-facts
                                   node
                                   node-by-id)]
                    (and
                      (= (:capabilities node) (:aggregate-capabilities expected))
                      (=
                        (select-keys
                          (:attributes node)
                          [:intrinsic-capabilities :aggregate-capabilities])
                        (select-keys
                          expected
                          [:intrinsic-capabilities :aggregate-capabilities])))))
                nodes)
      (p15-s23-closed-core-fail!
        "C8-VERIFY"
        source-path
        artifact
        {:missing-fact
         :independently-recomputed-intrinsic-and-aggregate-capabilities}))
    (when-let [node (first
                      (remove
                        #(p15-s23-closed-core-persistent-ownership-schema-valid?
                          %
                          (contains? expected-managed-node-ids (:node-id %)))
                        nodes))]
      (p15-s23-closed-core-fail!
        "C6-VERIFY"
        source-path
        (enriched-subject
          node
          {:missing-fact :exact-persistent-ownership-record-schema})
        {:missing-fact :exact-persistent-ownership-record-schema}))
    (when-let [node (first
                      (remove p15-s23-closed-core-persistent-aliasing-valid? nodes))]
      (p15-s23-closed-core-fail!
        "C9-MUT-ALIAS"
        source-path
        (p15-s23-closed-core-enriched-node-subject
          artifact
          node
          {:module (get-in artifact [:source-core-input :module]),
           :profile (:profile artifact),
           :target (:source-target artifact),
           :requested-target
           (get-in artifact [:target-request-metadata :requested-target]),
           :safety (get-in artifact [:source-core-input :declared-safety])}
          {:missing-fact :persistent-immutable-alias-and-mutation-policy})
        {:missing-fact :persistent-immutable-alias-and-mutation-policy}))
    (when-let [node (first
                      (remove
                        #(p15-s23-closed-core-persistent-forwarding-valid?
                          %
                          node-by-id
                          (contains? expected-managed-node-ids (:node-id %)))
                        nodes))]
      (p15-s23-closed-core-fail!
        "C9-TRANSFER"
        source-path
        (p15-s23-closed-core-enriched-node-subject
          artifact
          node
          {:module (get-in artifact [:source-core-input :module]),
           :profile (:profile artifact),
           :target (:source-target artifact),
           :requested-target
           (get-in artifact [:target-request-metadata :requested-target]),
           :safety (get-in artifact [:source-core-input :declared-safety])}
          {:missing-fact :persistent-value-forwarding-and-result-disposition})
        {:missing-fact :persistent-value-forwarding-and-result-disposition}))
    state))
