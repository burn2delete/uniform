(defn- __gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_cross_link_validation_01 [state]
  (let [{:syms
         [source-path
          target
          definitions
          authoritative-module
          derived
          contract
          checked-core-program-policy
          checked-core-verification-replay-policy
          checked-core-verification-replay-audit-policy
          function-graph
          function-effects
          effect-graph
          allocator
          capture-provider
          capture-handler
          selections
          proofs
          services
          adapter
          failure-policy
          audit-policy
          capability-manifest
          capability-table
          observability
          grants
          deployment
          expected-links
          source-principal
          handler-principal
          handler-scope
          excluded-functions
          grant-records
          proof-records
          expected-grants
          owner-table]} state]
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :runtime-contract-authoritative-module
      {:source-module (:module authoritative-module),
       :profile (:profile authoritative-module),
       :target (:target authoritative-module),
       :declared-effects (:effects authoritative-module),
       :declared-capabilities (:capabilities authoritative-module)}
      (select-keys
        contract
        [:source-module :profile :target :declared-effects :declared-capabilities]))
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :checked-core-program-authority-policy
      p15-s23-checked-core-expected-program-authority-policy
      checked-core-program-policy)
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :checked-core-program-authority-policy-schema
      p15-s23-checked-core-program-authority-policy-keys
      (set (keys checked-core-program-policy)))
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :checked-core-verification-replay-policy
      p15-s23-checked-core-expected-verification-replay-policy
      checked-core-verification-replay-policy)
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :checked-core-verification-replay-policy-schema
      p15-s23-checked-core-verification-replay-policy-keys
      (set (keys checked-core-verification-replay-policy)))
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :checked-core-verification-replay-audit-policy
      p15-s23-checked-core-expected-verification-replay-audit-policy
      checked-core-verification-replay-audit-policy)
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :checked-core-verification-replay-audit-policy-schema
      p15-s23-checked-core-verification-replay-audit-policy-keys
      (set (keys checked-core-verification-replay-audit-policy)))
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :checked-core-verification-replay-policy-audit-link
      (:policy-id checked-core-verification-replay-audit-policy)
      (:audit-policy-id checked-core-verification-replay-policy))
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :runtime-contract-cross-links
      expected-links
      (select-keys contract (keys expected-links)))
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :runtime-contract-source-provider-selection
      p15-s23-reference-runtime-source-provider-selections
      (:providers authoritative-module))
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :runtime-contract-provider-identities
      {:allocator :gravity.reference/jvm-managed-allocator,
       :capture :gravity.reference/transcript-capture,
       :handler :gravity.reference/transcript-string-handler,
       :adapter :gravity.reference/jvm-managed-adapter}
      {:allocator (:provider-id allocator),
       :capture (:provider-id capture-provider),
       :handler (:handler-id capture-handler),
       :adapter (:adapter-id adapter)})
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :runtime-contract-principals
      {:source-execution source-principal, :handler handler-principal}
      (:principals audit-policy))
    (doseq [[grant-id expected] expected-grants]
      (let [grant (get grant-records grant-id) proof (get proof-records grant-id)]
        (p15-s23-reference-runtime-ensure!
          source-path
          target
          :runtime-contract-grant
          expected
          {:principal (:principal-id grant),
           :capability (:capability grant),
           :provider (:provider-id grant),
           :scope (:scope grant)})
        (p15-s23-reference-runtime-ensure!
          source-path
          target
          :runtime-contract-capability-proof
          expected
          {:principal (:principal proof),
           :capability (:capability proof),
           :provider (:provider proof),
           :scope (:scope proof)})))
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :runtime-contract-grant-set
      (set (keys expected-grants))
      (set (keys grant-records)))
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :runtime-contract-proof-set
      (set (keys expected-grants))
      (set (keys proof-records)))
    state))
