(defn- __gravity_bootstrap_checked_core_effect_and_capability_admission [state]
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
          pre-authority-nodes]} state
        _ (when-not (= (:node-count validation) (:plan-node-count product))
            (p15-s23-closed-core-fail!
              "C6-VERIFY"
              source-path
              product
              {:missing-fact :plan-to-core-node-bijection,
               :plan-node-count (:node-count validation),
               :core-plan-node-count (:plan-node-count product)}))
        derived-effect-requirements {:required-effects (:effects product),
                                     :required-capabilities (:capabilities product)}
        _ (when-not (= preflight-effect-requirements derived-effect-requirements)
            (p15-s23-closed-core-fail!
              "C8-VERIFY"
              source-path
              product
              {:missing-fact :resolved-whole-plan-effect-preflight-parity,
               :plan-preflight preflight-effect-requirements,
               :derived-core derived-effect-requirements}))
        effectful-reference? (boolean
                               (seq
                                 (set/union
                                   (:required-effects derived-effect-requirements)
                                   (:required-capabilities
                                     derived-effect-requirements))))
        _ (when (and effectful-reference? (not= :jvm requested-target))
            (p15-s23-closed-core-fail!
              "C6-LOWERING-GAP"
              source-path
              effectful-diagnostic-subject
              {:missing-fact :effectful-reference-interpreter-requested-jvm-target,
               :requested-target requested-target}))
        _ (when (and
                  effectful-reference?
                  (not=
                    (:effects authoritative-module)
                    (:required-effects derived-effect-requirements)))
            (p15-s23-closed-core-fail!
              "C8-UNDECLARED"
              source-path
              effectful-diagnostic-subject
              {:missing-fact :required-effect-declaration,
               :declared-effects (:effects authoritative-module),
               :required-effects (:required-effects derived-effect-requirements)}))
        _ (when (and
                  effectful-reference?
                  (not=
                    (:capabilities authoritative-module)
                    (:required-capabilities derived-effect-requirements)))
            (p15-s23-closed-core-fail!
              "C8-CAPABILITY"
              source-path
              effectful-diagnostic-subject
              {:missing-fact :required-capability-declaration,
               :declared-capabilities (:capabilities authoritative-module),
               :required-capabilities
               (:required-capabilities derived-effect-requirements)}))
        _ (when (and
                  effectful-reference?
                  (not
                    (set/subset?
                      observed-operation-set
                      p15-s23-closed-core-recognized-plan-operations)))
            (p15-s23-closed-core-fail!
              "C8-VERIFY"
              source-path
              effectful-diagnostic-subject
              {:missing-fact :effectful-reference-concrete-operation-set-closure,
               :observed-operation-set observed-operation-set}))
        pure-admission (when-not effectful-reference?
                         (p15-s23-closed-core-pure-admission-record
                           source-path
                           source-content-hash
                           (:plan-id plan)
                           (assoc
                             authoritative-module
                             :requested-target
                             requested-target)
                           derived-effect-requirements
                           (some? authority-record)
                           product))
        _ (when (and
                  (not effectful-reference?)
                  (not
                    (set/subset?
                      observed-operation-set
                      p15-s23-closed-core-allowed-operations)))
            (p15-s23-closed-core-fail!
              "C8-VERIFY"
              source-path
              product
              {:missing-fact :pure-admission-concrete-operation-set-closure,
               :observed-operation-set observed-operation-set,
               :accepted-pure-operations p15-s23-closed-core-allowed-operations}))
        _ (when (and
                  effectful-reference?
                  (not
                    (p15-s23-checked-core-authority-small-map? authority-record 36)))
            (p15-s23-closed-core-fail!
              "C8-CAPABILITY"
              source-path
              effectful-diagnostic-subject
              {:missing-fact :bounded-typed-fourth-authority-record}))
        _ (when effectful-reference?
            (try
              (p15-s23-reference-runtime-bounded-value!
                source-path
                :jvm
                :checked-core-fourth-authority-record
                authority-record
                p15-s23-reference-runtime-max-contract-nodes
                p15-s23-reference-runtime-max-contract-depth)
              (catch
                Exception
                _
                (p15-s23-closed-core-fail!
                  "C8-CAPABILITY"
                  source-path
                  effectful-diagnostic-subject
                  {:missing-fact :bounded-typed-fourth-authority-record}))))]
    (assoc
      state
      '_
      _
      'derived-effect-requirements
      derived-effect-requirements
      '_
      _
      'effectful-reference?
      effectful-reference?
      '_
      _
      '_
      _
      '_
      _
      '_
      _
      'pure-admission
      pure-admission
      '_
      _
      '_
      _
      '_
      _)))
