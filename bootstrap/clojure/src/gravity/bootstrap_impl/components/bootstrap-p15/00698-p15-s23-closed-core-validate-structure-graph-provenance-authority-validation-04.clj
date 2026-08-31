(defn- __gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_04 [state]
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
    (let [graph (p15-s23-closed-core-dependency-order-graph nodes)]
      (when-not (and
                  (= graph (:dependency-order-graph artifact))
                  (= :passed (:status graph))
                  (true? (:all-dependencies-precede-consumers? graph))
                  (true? (:all-lexical-bindings-resolve? graph)))
        (p15-s23-closed-core-fail!
          "C6-EVAL-ORDER"
          source-path
          (enriched-subject
            (or (first nodes) {})
            {:missing-fact :independently-recomputed-dependency-order-graph})
          {:missing-fact :independently-recomputed-dependency-order-graph})))
    (let [recomputed-bindings (p15-s23-closed-core-recomputed-binding-records nodes)]
      (when-not (and
                  (= recomputed-bindings (:lexical-binding-records artifact))
                  (p15-s23-closed-core-binding-shadow-links-valid?
                    nodes
                    recomputed-bindings))
        (let [node (or
                     (first (filter #(= :let-binding (:source-operation %)) nodes))
                     (first nodes))]
          (p15-s23-closed-core-fail!
            "C6-EVAL-ORDER"
            source-path
            (enriched-subject
              node
              {:missing-fact :recomputed-sequential-lexical-binding-closure})
            {:missing-fact :recomputed-sequential-lexical-binding-closure}))))
    (when-not (and
                (=
                  node-origin-ids
                  (set (keys origin-table))
                  (set (keys origin-closure)))
                (every? p15-s23-closed-core-origin-id-valid? (vals origin-table))
                (every?
                  #(contains? origin-table (get-in % [:source :origin-id]))
                  nodes)
                (every?
                  (fn [node]
                    (=
                      (:path node)
                      (:plan-path
                        (get origin-table (get-in node [:source :origin-id])))))
                  nodes)
                (every?
                  (fn [[origin-id raw]]
                    (and
                      (= origin-id (:origin-id raw))
                      (string? (:actual-source-path raw))
                      (= source-path (:actual-source-path raw))
                      (string? (:c2-source-id raw))
                      (keyword? (:c2-form-id raw))
                      (or
                        (nil? (:c2-open-token-id raw))
                        (keyword? (:c2-open-token-id raw)))
                      (or
                        (nil? (:c2-close-token-id raw))
                        (keyword? (:c2-close-token-id raw)))
                      (map? (:c2-span raw))
                      (or (nil? (:c2-surface-span raw)) (map? (:c2-surface-span raw)))
                      (vector? (:c2-reader-generated-origin raw))
                      (string? (:c3-syntax-id raw))
                      (map? (:c3-source raw))
                      (vector? (:c3-origin raw))
                      (vector? (:expanded-generated-origin raw))
                      (or
                        (nil? (:input-origin-id raw))
                        (contains? origin-table (:input-origin-id raw)))
                      (p15-s23-closed-core-raw-provenance-binding-valid? raw)))
                  origin-closure))
      (p15-s23-closed-core-fail!
        "C6-ORIGIN"
        source-path
        artifact
        {:missing-fact :complete-semantic-and-raw-origin-closure}))
    (let [generated-records (mapcat
                              (fn [origin]
                                (mapcat
                                  #(get origin % [])
                                  [:reader-generated-origin
                                   :enclosing-c3-origin
                                   :enclosing-generated-origin
                                   :generated-origin]))
                              (vals origin-table))]
      (when-not (every?
                  (fn [record]
                    (if (contains? record :inputs)
                      (and
                        (vector? (:inputs record))
                        (every? #(contains? origin-table %) (:inputs record)))
                      true))
                  generated-records)
        (p15-s23-closed-core-fail!
          "C6-ORIGIN"
          source-path
          artifact
          {:missing-fact :generated-semantic-origin-input-closure})))
    (when-not (=
                (:provenance-binding-id artifact)
                (p15-s23-closed-core-recomputed-provenance-binding-id artifact))
      (p15-s23-closed-core-fail!
        "C6-ORIGIN"
        source-path
        artifact
        {:missing-fact :content-addressed-raw-origin-provenance-binding}))
    (when-not (=
                (:actual-path-binding-id artifact)
                (p15-s23-closed-core-recomputed-actual-path-binding-id artifact))
      (p15-s23-closed-core-fail!
        "C6-ORIGIN"
        source-path
        artifact
        {:missing-fact :content-addressed-actual-path-origin-binding}))
    (when-not (=
                (:instruction-origin-sidecar artifact)
                (p15-s23-closed-core-instruction-origin-sidecar artifact))
      (p15-s23-closed-core-fail!
        "C6-ORIGIN"
        source-path
        artifact
        {:missing-fact :authenticated-instruction-origin-sidecar}))
    (when-not (= (:typed-core artifact) (:typed-core recomputed-facts))
      (p15-s23-closed-core-fail!
        "C7-VERIFY"
        source-path
        (enriched-subject
          (or (first nodes) {})
          {:missing-fact :canonical-typed-core-envelope,
           :expected-type :recomputed-canonical-typed-core,
           :actual-type :stored-canonical-typed-core,
           :relevant-binding-id :not-applicable})
        {:missing-fact :canonical-typed-core-envelope}))
    state))
