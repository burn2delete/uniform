(defn- __gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_expected_cross_links [state]
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
          deployment]} state
        expected-links {:observability-manifest-id (:manifest-id observability),
                        :effect-graph-id (:graph-id effect-graph),
                        :capability-proof-record-set-id (:record-set-id proofs),
                        :managed-adapter-id (:adapter-id adapter),
                        :memory-provider-id (:provider-id allocator),
                        :function-graph-id (:graph-id function-graph),
                        :capability-manifest-id (:manifest-id capability-manifest),
                        :provider-selection-record-set-id
                        (:selection-record-set-id selections),
                        :grant-record-set-id (:grant-record-set-id grants),
                        :audit-policy-id (:policy-id audit-policy),
                        :service-manifest-id (:manifest-id services),
                        :function-effect-table-id (:table-id function-effects),
                        :failure-policy-id (:policy-id failure-policy)}
        source-principal (:module authoritative-module)
        handler-principal :gravity.bootstrap/reference-harness
        handler-scope (:handler-scope derived)
        excluded-functions (:escaping-io-functions derived)
        grant-records (into {} (map (juxt :grant-id identity) (:grants grants)))
        proof-records (into {} (map (juxt :grant-id identity) (:records proofs)))
        expected-grants #:gravity.reference{:grant-managed-allocation
                                            {:principal source-principal,
                                             :capability :memory/allocator,
                                             :provider
                                             :gravity.reference/jvm-managed-allocator,
                                             :scope :pinned-runtime-plan},
                                            :grant-reference-stdout
                                            {:principal source-principal,
                                             :capability :io/stdout,
                                             :provider
                                             :gravity.reference/transcript-capture,
                                             :scope :closed-plan-interpreter},
                                            :grant-test-fixture
                                            {:principal handler-principal,
                                             :capability :test/fixture,
                                             :provider
                                             :gravity.reference/transcript-capture,
                                             :scope :closed-plan-interpreter}}
        owner-table {:missing-allocation-provider "R5-PROVIDER",
                     :allocation-grant-denied "R11-GRANT",
                     :stdout-grant-denied "R11-GRANT",
                     :test-fixture-grant-denied "R11-GRANT",
                     :catchable-allocation-exception "R1-FAILURE",
                     :catchable-transcript-exception "R1-FAILURE",
                     :deployment-stdout-provider-missing "L15-PROVIDER-MISSING",
                     :host-exception-not-normalized "R4-EXCEPTION"}]
    (assoc
      state
      'expected-links
      expected-links
      'source-principal
      source-principal
      'handler-principal
      handler-principal
      'handler-scope
      handler-scope
      'excluded-functions
      excluded-functions
      'grant-records
      grant-records
      'proof-records
      proof-records
      'expected-grants
      expected-grants
      'owner-table
      owner-table)))
