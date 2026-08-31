(defn- __gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_08 [state]
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
    (when-not (= (:safety-facts artifact) (:safety-facts recomputed-facts))
      (let [node (or
                   (first
                     (filter
                       #(not=
                         (get (:safety-facts artifact) (:node-id %))
                         (get (:safety-facts recomputed-facts) (:node-id %)))
                       nodes))
                   (first nodes))]
        (p15-s23-closed-core-fail!
          "C10-PROOF"
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
            {:missing-fact :independently-recomputed-safety-facts,
             :proof-id (get-in node [:safety :proof :proof-id])})
          {:missing-fact :independently-recomputed-safety-facts})))
    (when-not (= (:ownership-facts artifact) (:ownership-facts recomputed-facts))
      (p15-s23-closed-core-fail!
        "C6-VERIFY"
        source-path
        artifact
        {:missing-fact :independently-recomputed-ownership-facts}))
    (when-not (every?
                #(p15-s23-closed-core-node-id-valid? (:source-content-hash artifact) %)
                nodes)
      (p15-s23-closed-core-fail!
        "C6-CORE-SHAPE"
        source-path
        artifact
        {:missing-fact :content-addressed-core-node-identity}))
    (when-not (= (:profile-facts artifact) (:profile-facts recomputed-facts))
      (p15-s23-closed-core-fail!
        "C6-VERIFY"
        source-path
        artifact
        {:missing-fact :independently-recomputed-profile-facts}))
    (when-not (=
                (:mapping-id artifact)
                (p15-s23-closed-core-recomputed-mapping-id artifact))
      (p15-s23-closed-core-fail!
        "C6-ORIGIN"
        source-path
        artifact
        {:missing-fact :independently-recomputed-origin-mapping-id}))
    (when-not (=
                (:artifact-id artifact)
                (p15-s23-closed-core-digest
                  (p15-s23-closed-core-semantic-input artifact)))
      (p15-s23-closed-core-fail!
        "C6-VERIFY"
        source-path
        artifact
        {:missing-fact :independently-recomputed-checked-core-artifact-id}))
    state))
