(defn- __gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_capability_authority [state]
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
          capture-handler]} state
        selections (get definitions 'p15-s23-reference-runtime-provider-selections)
        proofs (get definitions 'p15-s23-reference-runtime-capability-proofs)
        services (get definitions 'p15-s23-reference-runtime-service-manifest)
        adapter (get definitions 'p15-s23-reference-managed-runtime-adapter)
        failure-policy (get definitions 'p15-s23-reference-runtime-failure-policy)
        audit-policy (get definitions 'p15-s23-reference-runtime-audit-policy)
        capability-manifest (get
                              definitions
                              'p15-s23-reference-runtime-capability-manifest)
        capability-table (get definitions 'p15-s23-reference-runtime-capability-table)
        observability (get
                        definitions
                        'p15-s23-reference-runtime-observability-manifest)
        grants (get definitions 'p15-s23-reference-runtime-grant-records)
        deployment (get definitions 'p15-s23-reference-stdout-deployment-requirement)]
    (assoc
      state
      'selections
      selections
      'proofs
      proofs
      'services
      services
      'adapter
      adapter
      'failure-policy
      failure-policy
      'audit-policy
      audit-policy
      'capability-manifest
      capability-manifest
      'capability-table
      capability-table
      'observability
      observability
      'grants
      grants
      'deployment
      deployment)))
