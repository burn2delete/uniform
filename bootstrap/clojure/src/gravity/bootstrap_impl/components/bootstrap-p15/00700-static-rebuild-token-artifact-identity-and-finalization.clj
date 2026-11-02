(defn- __gravity_bootstrap_checked_core_artifact_identity_and_finalization [state]
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
          facts
          source-core-input
          authenticated-input
          mapping-id
          provenance-binding-id
          actual-path-binding-id
          instruction-origin-sidecar]} state
        artifact-base (merge
                        {:source-content-hash source-content-hash,
                         :mapping-id mapping-id,
                         :origin-closure (:origin-closure product),
                         :core-nodes nodes,
                         :diagnostics [],
                         :source-target (get-in plan [:module :target]),
                         :whole-language? false,
                         :source-core-input source-core-input,
                         :instruction-origin-sidecar instruction-origin-sidecar,
                         :target-request-metadata
                         {:requested-target requested-target,
                          :identity-bearing? false,
                          :packet-target-eligibility
                          (if packet
                            (:target-eligibility packet)
                            :jvm-reference-interpreter-only)},
                         :provenance-binding-id provenance-binding-id,
                         :dependency-order-graph dependency-order,
                         :source-origin-table (:origin-table product),
                         :root-node-ids [(:node-id function-node)],
                         :scope
                         (if effectful-reference?
                           (p15-s23-closed-core-scope-contract
                             :effectful-reference
                             derived-effect-requirements)
                           (p15-s23-closed-core-scope-contract)),
                         :bounds
                         {:maximum-plan-depth p15-s23-closed-core-max-plan-depth,
                          :observed-plan-depth (:maximum-plan-depth product),
                          :maximum-artifact-scalar-bytes
                          p15-s23-closed-core-max-artifact-scalar-bytes,
                          :maximum-source-bytes p15-s23-closed-core-max-source-bytes,
                          :maximum-plan-nodes p15-s23-closed-core-max-plan-nodes,
                          :maximum-derived-nodes p15-s23-closed-core-max-derived-nodes,
                          :observed-source-bytes source-byte-count,
                          :observed-derived-nodes (count nodes),
                          :maximum-integer-bits p15-s23-closed-core-max-integer-bits,
                          :observed-plan-nodes (:plan-node-count product)},
                         :self-hosted? false,
                         :actual-path-binding-id actual-path-binding-id,
                         :lexical-binding-records (:binding-records product),
                         :pass-history
                         (p15-s23-closed-core-pass-history-for-mode
                           (if effectful-reference? :effectful-reference :pure)),
                         :status
                         (if effectful-reference?
                           :complete-for-authenticated-hosted-jvm-reference-interpreter-slice
                           :complete-for-pure-closed-slice),
                         :authenticated-input authenticated-input,
                         :kind :gravity/p15-s23-stage2-closed-checked-core-artifact,
                         :entrypoint entrypoint,
                         :mir-derived? false,
                         :provenance
                         {:actual-paths
                          {:source source-path,
                           :stage2-expression-lowering-source
                           (if packet
                             (get-in
                               packet
                               [:provenance
                                :actual-paths
                                :stage2-expression-lowering-source])
                             (:source-path preflight-stage2-rule)),
                           :stage2-runtime-artifact-source
                           (if packet
                             (get-in
                               packet
                               [:provenance
                                :actual-paths
                                :stage2-runtime-artifact-source])
                             (:runtime-artifact-source-path runtime-rule))},
                          :requested-target requested-target,
                          :c2-source-unit-id
                          (get-in c2-artifact [:source-unit-record :source-id]),
                          :c2-artifact-id (:artifact-id c2-artifact),
                          :c2-incremental-reader-hashes
                          (:incremental-reader-hashes fresh-front-end),
                          :c2-reader-product-integrity
                          (:reader-product-integrity fresh-front-end),
                          :c3-artifact-id (:c3-artifact-id fresh-front-end),
                          :entrypoint-c3-syntax-id (get root-syntax :syntax/id)},
                         :profile (get-in plan [:module :profile]),
                         :clojure-seed-boundary? true}
                        facts)
        _ (p15-s23-closed-core-bounded-value! source-path artifact-base)
        artifact-id (p15-s23-closed-core-digest
                      (p15-s23-closed-core-semantic-input artifact-base))
        artifact (assoc artifact-base :artifact-id artifact-id)]
    (assoc
      state
      'artifact-base
      artifact-base
      '_
      _
      'artifact-id
      artifact-id
      'artifact
      artifact)))
