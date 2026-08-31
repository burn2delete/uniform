(defn- __gravity_bootstrap_checked_core_source_and_authenticated_input [state]
  (let [{:syms
         [source-path
          source-text
          requested-target
          authority-record
          construction-mode
          static-execution-evidence
          static-rebuild-token-candidate
          source-byte-count
          source-content-hash
          early-module-products
          module-attempt
          _
          early-module
          authoritative-front-end
          authoritative-module
          authoritative-records
          namespace-record
          function-record
          namespace-subject
          function-subject
          executable-form-records
          executable-form-by-id
          source-surface-validation
          malformed-quote-record
          source-surface-subject
          early-metadata-bearing-form
          c7-source-violation
          unsupported-quote-literal-form
          unsupported-numeric-form
          invalid-str-arity-form
          preflight-stage2-rule
          preflight-driver-rule
          preflight-plan
          preflight-plan-validation
          packet-delay
          front-end
          fresh-front-end
          top-level-forms
          reader-products
          c2-artifact
          form-tree
          root-form-ids
          indexes
          token-stream
          token-ordinal-by-id
          plan
          preflight-effect-requirements
          entrypoint
          functions
          definition
          declared-exports
          entrypoint-binding
          entrypoint-visibility
          source-function-records
          all-source-functions
          root-record-wrapper
          root-form-id
          root-record
          metadata-bearing-form
          root-syntax
          expanded-root-syntax
          function-shape
          instructions
          body-form-ids
          validation
          observed-operation-set
          ctx
          function-path
          function-origin
          body-product
          body-node-by-path
          body-node-ids
          function-node
          product
          effectful-diagnostic-subject
          pre-authority-nodes
          derived-effect-requirements
          effectful-reference?
          pure-admission
          runtime-rule
          authority-policy
          expected-authority-record
          authority-evidence
          ownership-normalized-nodes
          nodes
          core-operation-set
          packet
          packet-observed-operation-set
          packet-context
          pre-execution-facts
          dependency-order
          declared-effects
          declared-capabilities
          c2-semantic-input
          c3-semantic-input
          adapter-output
          reference-execution-evidence
          facts]} state
        source-core-input (cond->
                            {:source-content-hash source-content-hash,
                             :plan-node-count (:node-count validation),
                             :declared-target (get-in plan [:module :target]),
                             :c2-semantic-incremental-hash
                             (p15-s23-closed-core-digest
                               (select-keys
                                 c2-semantic-input
                                 [:tokens :forms :syntax-seeds :top-level-form-ids])),
                             :plan-id (:plan-id plan),
                             :declared-exports declared-exports,
                             :operation-set
                             (if effectful-reference?
                               core-operation-set
                               observed-operation-set),
                             :module (get-in plan [:module :module]),
                             :c2-semantic-product-hash
                             (p15-s23-closed-core-digest c2-semantic-input),
                             :declared-capabilities declared-capabilities,
                             :c3-semantic-syntax-hash
                             (p15-s23-closed-core-digest c3-semantic-input),
                             :kind :gravity/p15-s23-authenticated-closed-plan-input,
                             :declared-safety (get-in plan [:module :safety]),
                             :declared-effects declared-effects,
                             :entrypoint-visibility entrypoint-visibility,
                             :declared-profile (get-in plan [:module :profile])}
                            (not effectful-reference?)
                            (assoc
                              :pure-admission-record
                              pure-admission
                              :effectful-runtime-branches-unreachable?
                              true
                              :packet-role
                              :seed-comparison-oracle
                              :runtime-module-c8-conformance
                              :not-claimed)
                            effectful-reference?
                            (assoc
                              :mode
                              :effectful-reference
                              :authority-evidence
                              authority-evidence
                              :effectful-runtime-branches-unreachable?
                              false
                              :packet-role
                              :authenticated-reference-interpreter
                              :runtime-module-c8-conformance
                              :authenticated-and-consumed))
        authenticated-input (cond->
                              {:source-content-hash source-content-hash,
                               :packet-context-bound? (boolean packet),
                               :source-target (get-in plan [:module :target]),
                               :c2-semantic-incremental-hash
                               (p15-s23-closed-core-digest
                                 (select-keys
                                   c2-semantic-input
                                   [:tokens
                                    :forms
                                    :syntax-seeds
                                    :top-level-form-ids])),
                               :packet-kind
                               (if packet
                                 (:kind packet)
                                 :compile-only-authenticated-reference),
                               :plan-id (:plan-id plan),
                               :declared-exports declared-exports,
                               :operation-set
                               (if effectful-reference?
                                 core-operation-set
                                 observed-operation-set),
                               :module (get-in plan [:module :module]),
                               :c2-semantic-product-hash
                               (p15-s23-closed-core-digest c2-semantic-input),
                               :c3-semantic-syntax-hash
                               (p15-s23-closed-core-digest c3-semantic-input),
                               :declared-safety (get-in plan [:module :safety]),
                               :entrypoint-visibility entrypoint-visibility,
                               :closed-plan-validation-node-count
                               (:node-count validation),
                               :runtime-artifact-hash
                               (if packet
                                 (get-in
                                   packet
                                   [:stage2-runtime-rule :runtime-artifact-hash])
                                 (:runtime-artifact-hash runtime-rule))}
                              (not effectful-reference?)
                              (assoc
                                :pure-admission-id
                                (:admission-id pure-admission)
                                :pre-execution-pure-admitted?
                                (= :accepted-pure-only (:decision pure-admission))
                                :effectful-runtime-branches-unreachable?
                                true
                                :packet-role
                                :seed-comparison-oracle
                                :runtime-module-c8-conformance
                                :not-claimed
                                :compiler-artifact-hash
                                (get-in
                                  packet
                                  [:stage2-compiler-artifact-record :artifact-hash]))
                              effectful-reference?
                              (assoc
                                :authority-evidence-id
                                (:evidence-id authority-evidence)
                                :authority-record-id
                                (:authority-record-id authority-evidence)
                                :reference-execution-evidence
                                reference-execution-evidence
                                :effectful-runtime-branches-unreachable?
                                false
                                :packet-role
                                :authenticated-reference-interpreter
                                :runtime-module-c8-conformance
                                :authenticated-and-consumed
                                :plan-emitter-source-rule-hash
                                (:source-rule-hash preflight-stage2-rule)
                                :runtime-policy-bound?
                                true))]
    (assoc
      state
      'source-core-input
      source-core-input
      'authenticated-input
      authenticated-input)))
