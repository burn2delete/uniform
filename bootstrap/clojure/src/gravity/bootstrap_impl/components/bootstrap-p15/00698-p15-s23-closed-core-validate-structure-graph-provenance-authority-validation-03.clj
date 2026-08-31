(defn- __gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_03 [state]
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
    (when-not (if effectful?
                (let [function-node (first function-nodes)
                      closure (:pure-capability-closure artifact)
                      provider-capabilities (set
                                              (keys
                                                (:provider-bindings
                                                  authority-evidence)))
                      grant-capabilities (set
                                           (keys
                                             (:grant-bindings authority-evidence)))]
                  (and
                    (not
                      (contains? (:source-core-input artifact) :pure-admission-record))
                    (not
                      (contains? (:authenticated-input artifact) :pure-admission-id))
                    (not
                      (contains?
                        (:authenticated-input artifact)
                        :pre-execution-pure-admitted?))
                    (p15-s23-checked-core-authority-evidence-valid? authority-evidence)
                    (= expected-authority-evidence authority-evidence)
                    (=
                      (:evidence-id authority-evidence)
                      (get-in artifact [:authenticated-input :authority-evidence-id]))
                    (=
                      (:authority-record-id authority-evidence)
                      (get-in artifact [:authenticated-input :authority-record-id]))
                    (p15-s23-checked-core-reference-execution-evidence-valid?
                      execution-evidence
                      authority-evidence)
                    (=
                      execution-evidence
                      (p15-s23-checked-core-expected-reference-execution-evidence
                        authority-evidence
                        execution-evidence))
                    (=
                      declared-effects
                      (:effects function-node)
                      (get-in
                        artifact
                        [:effect-graph :functions (:node-id function-node) :declared])
                      (get-in
                        artifact
                        [:effect-graph :functions (:node-id function-node) :inferred])
                      (get-in
                        artifact
                        [:effect-graph :functions (:node-id function-node) :latent])
                      (get-in artifact [:effect-graph :namespace :declared])
                      (get-in artifact [:effect-graph :namespace :inferred]))
                    (=
                      declared-capabilities
                      (:capabilities function-node)
                      (get-in
                        artifact
                        [:typed-core
                         :functions
                         (:node-id function-node)
                         :capabilities])
                      (:required closure)
                      (:granted closure)
                      provider-capabilities
                      grant-capabilities)
                    (=
                      observed-operation-set
                      (get-in artifact [:source-core-input :operation-set])
                      (get-in artifact [:authenticated-input :operation-set]))
                    (set/subset?
                      observed-operation-set
                      p15-s23-closed-core-recognized-core-operations)
                    (false?
                      (get-in
                        artifact
                        [:source-core-input :effectful-runtime-branches-unreachable?]))
                    (false?
                      (get-in
                        artifact
                        [:authenticated-input
                         :effectful-runtime-branches-unreachable?]))
                    (=
                      :authenticated-reference-interpreter
                      (get-in artifact [:source-core-input :packet-role])
                      (get-in artifact [:authenticated-input :packet-role]))
                    (=
                      :authenticated-and-consumed
                      (get-in
                        artifact
                        [:source-core-input :runtime-module-c8-conformance])
                      (get-in
                        artifact
                        [:authenticated-input :runtime-module-c8-conformance]))))
                (and
                  (not (contains? (:source-core-input artifact) :authority-evidence))
                  (not
                    (contains?
                      (:authenticated-input artifact)
                      :reference-execution-evidence))
                  (p15-s23-closed-core-pure-admission-valid?
                    (get-in artifact [:source-core-input :pure-admission-record]))
                  (=
                    (get-in
                      artifact
                      [:source-core-input :pure-admission-record :admission-id])
                    (get-in artifact [:authenticated-input :pure-admission-id]))
                  (true?
                    (get-in
                      artifact
                      [:authenticated-input :pre-execution-pure-admitted?]))
                  (= #{} declared-effects declared-capabilities)
                  (every?
                    #(and (empty? (:effects %)) (empty? (:capabilities %)))
                    nodes)
                  (=
                    observed-operation-set
                    (get-in artifact [:source-core-input :operation-set])
                    (get-in artifact [:authenticated-input :operation-set]))
                  (set/subset?
                    observed-operation-set
                    p15-s23-closed-core-allowed-operations)
                  (true?
                    (get-in
                      artifact
                      [:source-core-input :effectful-runtime-branches-unreachable?]))
                  (true?
                    (get-in
                      artifact
                      [:authenticated-input :effectful-runtime-branches-unreachable?]))
                  (=
                    :seed-comparison-oracle
                    (get-in artifact [:source-core-input :packet-role])
                    (get-in artifact [:authenticated-input :packet-role]))
                  (=
                    :not-claimed
                    (get-in
                      artifact
                      [:source-core-input :runtime-module-c8-conformance])
                    (get-in
                      artifact
                      [:authenticated-input :runtime-module-c8-conformance]))))
      (p15-s23-closed-core-fail!
        "C8-VERIFY"
        source-path
        artifact
        {:missing-fact
         (if effectful?
           :effectful-reference-authority-declaration-and-execution-closure
           :pure-admission-effect-and-concrete-operation-closure),
         :observed-operation-set observed-operation-set,
         :declared-effects declared-effects,
         :declared-capabilities declared-capabilities}))
    state))
