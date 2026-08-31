(defn- __gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_07 [state]
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
    (when-let [node (first
                      (remove
                        #(contains?
                          p15-s23-closed-core-allowed-safety-outcomes
                          (get-in % [:safety :outcome]))
                        nodes))]
      (p15-s23-closed-core-fail!
        "C10-NO-OUTCOME"
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
          {:missing-fact :closed-safety-outcome,
           :proof-id (get-in node [:safety :proof :proof-id])})
        {:missing-fact :closed-safety-outcome,
         :observed-outcome (get-in node [:safety :outcome])}))
    (when-let [node (first
                      (remove
                        (fn [node]
                          (let [outcome (get-in node [:safety :outcome])
                                basis (p15-s23-closed-core-safety-basis
                                        (:source-operation node))]
                            (case
                              outcome
                              :proven-safe
                              (let [proof (get-in node [:safety :proof])]
                                (and
                                  (=
                                    #{:basis :outcome :proof}
                                    (set (keys (:safety node))))
                                  (= basis (get-in node [:safety :basis]))
                                  (if (and
                                        (empty? (:effects node))
                                        (empty? (:capabilities node)))
                                    (=
                                      (p15-s23-closed-core-pure-safety-proof
                                        (:source-content-hash artifact)
                                        (:path node)
                                        (:source-operation node)
                                        (:source node)
                                        (:profile node)
                                        (:type node)
                                        (:effects node)
                                        (:capabilities node)
                                        (:ownership node)
                                        basis)
                                      proof)
                                    (let [child-obligations (p15-s23-closed-core-child-obligation-refs
                                                              node
                                                              node-by-id
                                                              capability-proof-by-node-capability)]
                                      (and
                                        (p15-s23-closed-core-structural-safety-proof-valid?
                                          proof
                                          node
                                          child-obligations)
                                        (=
                                          (p15-s23-closed-core-structural-safety-proof
                                            (:source-content-hash artifact)
                                            (:path node)
                                            (:source-operation node)
                                            (:source node)
                                            (:profile node)
                                            (:type node)
                                            (:effects node)
                                            (:capabilities node)
                                            (:ownership node)
                                            basis
                                            child-obligations)
                                          proof))))))
                              :runtime-checked
                              (let [check (get-in node [:safety :check])
                                    capability (:capability check)
                                    capability-proof (get
                                                       capability-proof-by-node-capability
                                                       [(:node-id node) capability])]
                                (and
                                  effectful?
                                  (=
                                    #{:check :basis :outcome}
                                    (set (keys (:safety node))))
                                  (= basis (get-in node [:safety :basis]))
                                  (p15-s23-closed-core-runtime-check-valid?
                                    check
                                    node
                                    authority-evidence
                                    capability-proof)
                                  (p15-s23-checked-core-capability-proof-record-valid?
                                    capability-proof
                                    authority-evidence)))
                              false)))
                        nodes))]
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
          {:missing-fact :content-addressed-outcome-specific-safety-proof,
           :proof-id (get-in node [:safety :proof :proof-id])})
        {:missing-fact :content-addressed-outcome-specific-safety-proof,
         :proof-id (get-in node [:safety :proof :proof-id])}))
    (when-not (= (:effect-facts artifact) (:effect-facts recomputed-facts))
      (p15-s23-closed-core-fail!
        "C8-VERIFY"
        source-path
        artifact
        {:missing-fact :independently-recomputed-effect-facts}))
    (when-not (= (:capability-facts artifact) (:capability-facts recomputed-facts))
      (p15-s23-closed-core-fail!
        "C8-VERIFY"
        source-path
        artifact
        {:missing-fact :independently-recomputed-capability-facts}))
    state))
