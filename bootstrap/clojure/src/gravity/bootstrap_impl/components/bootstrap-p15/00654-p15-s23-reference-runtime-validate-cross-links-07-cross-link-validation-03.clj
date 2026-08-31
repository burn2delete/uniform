(defn- __gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_cross_link_validation_03 [state]
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
    (doseq [[grant-id expected] expected-grants]
      (let [grant (get grant-records grant-id)
            proof (get proof-records grant-id)
            common-expected {:phase :runtime,
                             :lifetime :single-reference-execution,
                             :policy-id :gravity.reference/runtime-audit-policy,
                             :reference-invocation :single-reference-execution,
                             :package :gravity/bootstrap,
                             :deployment :reference-harness-only}
            grant-expected (merge
                             common-expected
                             {:authority-source :explicit-grant,
                              :provider-id (:provider expected),
                              :grant-id grant-id,
                              :capability (:capability expected),
                              :scope (:scope expected),
                              :source-declaration-is-grant? false,
                              :audit-policy-id :gravity.reference/runtime-audit-policy,
                              :status :granted,
                              :principal-id (:principal expected)}
                             (when (not=
                                     grant-id
                                     :gravity.reference/grant-managed-allocation)
                               {:live-external-authority? false}))
            proof-expected (merge
                             common-expected
                             {:grant-id grant-id,
                              :principal (:principal expected),
                              :capability (:capability expected),
                              :provider (:provider expected),
                              :scope (:scope expected),
                              :status :accepted}
                             (case
                               grant-id
                               :gravity.reference/grant-reference-stdout
                               {:handler :gravity.reference/transcript-string-handler,
                                :live-external-io? false}
                               :gravity.reference/grant-test-fixture
                               {:source-principal source-principal,
                                :fixture-id
                                :gravity.reference/pinned-runtime-transcript,
                                :delegation :none,
                                :authority-widening? false}
                               {}))]
        (p15-s23-reference-runtime-ensure!
          source-path
          target
          :runtime-contract-grant-authority-schema
          grant-expected
          (select-keys grant (keys grant-expected)))
        (p15-s23-reference-runtime-ensure!
          source-path
          target
          :runtime-contract-proof-authority-schema
          proof-expected
          (select-keys proof (keys proof-expected)))
        (p15-s23-reference-runtime-ensure!
          source-path
          target
          :runtime-contract-proof-nested-grant-schema
          (select-keys
            proof-expected
            [:grant-id
             :principal
             :scope
             :phase
             :lifetime
             :policy-id
             :reference-invocation
             :package
             :deployment])
          (let [nested (:grant proof)]
            (-> (select-keys
                  nested
                  [:grant-id
                   :principal
                   :scope
                   :phase
                   :lifetime
                   :policy-id
                   :reference-invocation
                   :package
                   :deployment])
             (assoc :principal (:principal nested)))))))
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :runtime-contract-failure-owner-table
      owner-table
      (:diagnostic-owner-table failure-policy))
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :runtime-contract-failure-catch-domain
      {:catch-domain "java.lang.Exception",
       :fatal-error-domain "java.lang.Error",
       :fatal-host-oom :unproven-fatal-host-boundary,
       :raw-host-exceptions :forbidden}
      (select-keys
        failure-policy
        [:catch-domain :fatal-error-domain :fatal-host-oom :raw-host-exceptions]))
    state))
