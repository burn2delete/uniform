(defn- __gravity_bootstrap_closed_core_structure_graph_provenance_authority_validation_02 [state]
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
                (=
                  (if effectful?
                    (p15-s23-closed-core-scope-contract
                      :effectful-reference
                      recomputed-effect-requirements)
                    (p15-s23-closed-core-scope-contract))
                  (:scope artifact))
                (=
                  (p15-s23-closed-core-pass-history-for-mode mode)
                  (:pass-history artifact))
                (= 1 (count function-nodes))
                (= [(:node-id (first function-nodes))] (:root-node-ids artifact))
                (=
                  (:entrypoint artifact)
                  (get-in (first function-nodes) [:attributes :name]))
                (=
                  declared-exports
                  (get-in artifact [:authenticated-input :declared-exports]))
                (=
                  entrypoint-visibility
                  (get-in artifact [:authenticated-input :entrypoint-visibility])
                  (get-in (first function-nodes) [:attributes :visibility]))
                (=
                  entrypoint-visibility
                  (if (seq declared-exports)
                    (if (contains? (set declared-exports) (:entrypoint artifact))
                      :public
                      :private)
                    :stage2-local))
                (=
                  (count plan-nodes)
                  (get-in artifact [:bounds :observed-plan-nodes])
                  (get-in artifact [:source-core-input :plan-node-count])
                  (get-in
                    artifact
                    [:authenticated-input :closed-plan-validation-node-count]))
                (= (count nodes) (get-in artifact [:bounds :observed-derived-nodes]))
                (=
                  p15-s23-closed-core-max-plan-nodes
                  (get-in artifact [:bounds :maximum-plan-nodes]))
                (=
                  p15-s23-closed-core-max-plan-depth
                  (get-in artifact [:bounds :maximum-plan-depth]))
                (=
                  p15-s23-closed-core-max-derived-nodes
                  (get-in artifact [:bounds :maximum-derived-nodes]))
                (=
                  p15-s23-closed-core-max-source-bytes
                  (get-in artifact [:bounds :maximum-source-bytes]))
                (=
                  p15-s23-closed-core-max-artifact-scalar-bytes
                  (get-in artifact [:bounds :maximum-artifact-scalar-bytes]))
                (=
                  p15-s23-closed-core-max-integer-bits
                  (get-in artifact [:bounds :maximum-integer-bits]))
                (=
                  (apply max 0 (map :plan-depth plan-nodes))
                  (get-in artifact [:bounds :observed-plan-depth]))
                (=
                  (not effectful?)
                  (get-in artifact [:authenticated-input :packet-context-bound?]))
                (if effectful?
                  (true?
                    (get-in artifact [:authenticated-input :runtime-policy-bound?]))
                  true)
                (=
                  (:source-content-hash artifact)
                  (get-in artifact [:source-core-input :source-content-hash])
                  (get-in artifact [:authenticated-input :source-content-hash]))
                (=
                  (:profile artifact)
                  (get-in artifact [:source-core-input :declared-profile]))
                (= :hosted (:profile artifact))
                (every? #(= :hosted (:profile %)) nodes)
                (= :safe (get-in artifact [:source-core-input :declared-safety]))
                (= :safe (get-in artifact [:authenticated-input :declared-safety]))
                (=
                  (:source-target artifact)
                  (get-in artifact [:source-core-input :declared-target])
                  (get-in artifact [:authenticated-input :source-target])
                  (get-in artifact [:scope :module :source-target]))
                (if effectful?
                  true
                  (=
                    (:source-target artifact)
                    (get-in
                      artifact
                      [:source-core-input :pure-admission-record :source-target])))
                (= :jvm (:source-target artifact))
                (false? (:mir-derived? artifact))
                (false? (:whole-language? artifact))
                (true? (:clojure-seed-boundary? artifact))
                (false? (:self-hosted? artifact))
                (= [] (:diagnostics artifact)))
      (p15-s23-closed-core-fail!
        "C6-CORE-SHAPE"
        source-path
        artifact
        {:missing-fact :closed-core-root-scope-pass-and-boundary-contract}))
    state))
