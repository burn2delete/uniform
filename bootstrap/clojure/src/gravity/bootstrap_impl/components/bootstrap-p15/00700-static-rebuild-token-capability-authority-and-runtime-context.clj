(defn- __gravity_bootstrap_checked_core_capability_authority_and_runtime_context [state]
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
          pure-admission]} state
        runtime-rule (when effectful-reference?
                       (c-backend-stage2-runtime-source-rule!
                         source-path
                         requested-target))
        _ (when (and
                  effectful-reference?
                  (not (p15-s23-reference-runtime-rule-authentic? runtime-rule)))
            (p15-s23-closed-core-fail!
              "C8-CAPABILITY"
              source-path
              effectful-diagnostic-subject
              {:missing-fact :authenticated-pinned-reference-runtime-policy}))
        authority-policy (when effectful-reference?
                           (get
                             (:runtime-contract-definitions runtime-rule)
                             'p15-s23-checked-core-program-authority-policy))
        _ (when (and
                  effectful-reference?
                  (not=
                    p15-s23-checked-core-expected-program-authority-policy
                    authority-policy))
            (p15-s23-closed-core-fail!
              "C8-CAPABILITY"
              source-path
              effectful-diagnostic-subject
              {:missing-fact :authenticated-program-authority-policy}))
        expected-authority-record (when effectful-reference?
                                    (p15-s23-checked-core-authority-record
                                      source-content-hash
                                      plan
                                      authoritative-module
                                      derived-effect-requirements
                                      runtime-rule
                                      authority-policy))
        _ (when (and
                  effectful-reference?
                  (not
                    (p15-s23-checked-core-authority-record-valid? authority-record)))
            (p15-s23-closed-core-fail!
              "C8-CAPABILITY"
              source-path
              effectful-diagnostic-subject
              {:missing-fact :integral-typed-fourth-authority-record}))
        _ (when (and
                  effectful-reference?
                  (not= expected-authority-record authority-record))
            (p15-s23-closed-core-fail!
              "C8-CAPABILITY"
              source-path
              effectful-diagnostic-subject
              {:missing-fact :independently-reissued-typed-fourth-authority-record,
               :expected-authority-record-id
               (:authority-record-id expected-authority-record),
               :observed-authority-record-id (:authority-record-id authority-record)}))
        authority-evidence (when effectful-reference?
                             (p15-s23-checked-core-authority-evidence
                               authority-record))
        ownership-normalized-nodes (p15-s23-closed-core-propagate-managed-ownership
                                     pre-authority-nodes)
        nodes (p15-s23-closed-core-rederive-proven-safety
                source-content-hash
                (if effectful-reference?
                  (p15-s23-checked-core-bind-runtime-check-authority
                    ownership-normalized-nodes
                    authority-evidence)
                  ownership-normalized-nodes)
                authority-evidence)
        core-operation-set (set (map :source-operation (filter :plan-node? nodes)))
        packet (when-not effectful-reference? @packet-delay)
        packet-observed-operation-set (when packet
                                        (p15-s23-closed-core-observed-plan-operations
                                          (:plan packet)))
        packet-context (when packet
                         (p15-s23-closed-runtime-packet-context
                           source-path
                           source-text
                           requested-target))
        _ (when (and
                  packet
                  (not
                    (p15-s23-closed-runtime-packet-authentic? packet packet-context)))
            (p15-s23-closed-core-fail!
              "C6-VERIFY"
              source-path
              packet
              {:missing-fact :authenticated-stage2-packet-context}))
        _ (when (and
                  packet
                  (not
                    (and
                      (= (:plan-id plan) (get-in packet [:plan :plan-id]))
                      (= validation (:closed-plan-validation-record packet))
                      (= observed-operation-set packet-observed-operation-set))))
            (p15-s23-closed-core-fail!
              "C6-VERIFY"
              source-path
              packet
              {:missing-fact :pre-execution-plan-packet-identity,
               :preflight-operation-set observed-operation-set,
               :packet-operation-set packet-observed-operation-set}))]
    (assoc
      state
      'runtime-rule
      runtime-rule
      '_
      _
      'authority-policy
      authority-policy
      '_
      _
      'expected-authority-record
      expected-authority-record
      '_
      _
      '_
      _
      'authority-evidence
      authority-evidence
      'ownership-normalized-nodes
      ownership-normalized-nodes
      'nodes
      nodes
      'core-operation-set
      core-operation-set
      'packet
      packet
      'packet-observed-operation-set
      packet-observed-operation-set
      'packet-context
      packet-context
      '_
      _
      '_
      _)))
