(defn- __gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_cross_link_validation_02 [state]
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
    (let [authority-common {:phase :runtime,
                            :lifetime :single-reference-execution,
                            :policy-id :gravity.reference/runtime-audit-policy,
                            :reference-invocation :single-reference-execution,
                            :package :gravity/bootstrap,
                            :deployment :reference-harness-only}
          expected-source-selection (merge
                                      authority-common
                                      {:capability :memory/allocator,
                                       :source :source-annotation,
                                       :scope :pinned-runtime-plan,
                                       :source-declaration-is-grant? false,
                                       :status :selected,
                                       :target :jvm,
                                       :principal source-principal,
                                       :provider
                                       :gravity.reference/jvm-managed-allocator,
                                       :profile :hosted})
          expected-handler-selections #{(merge
                                          authority-common
                                          {:excluded-functions excluded-functions,
                                           :source-principal source-principal,
                                           :direct-handler-function
                                           p15-s23-reference-runtime-handler-function,
                                           :mode :reference-test-interpreter,
                                           :capability :test/fixture,
                                           :source :reference-harness-policy,
                                           :scope :closed-plan-interpreter,
                                           :source-declaration-is-grant? false,
                                           :deployment-authority? false,
                                           :status :selected,
                                           :transitive-function-scope handler-scope,
                                           :target :jvm,
                                           :principal handler-principal,
                                           :provider
                                           :gravity.reference/transcript-capture,
                                           :profile :hosted})
                                        (merge
                                          authority-common
                                          {:excluded-functions excluded-functions,
                                           :direct-handler-function
                                           p15-s23-reference-runtime-handler-function,
                                           :mode :reference-test-interpreter,
                                           :capability :io/stdout,
                                           :source :reference-harness-policy,
                                           :scope :closed-plan-interpreter,
                                           :handler-principal handler-principal,
                                           :source-declaration-is-grant? false,
                                           :deployment-authority? false,
                                           :status :selected,
                                           :transitive-function-scope handler-scope,
                                           :target :jvm,
                                           :principal source-principal,
                                           :provider
                                           :gravity.reference/transcript-capture,
                                           :profile :hosted})}
          expected-capability-rows #{(merge
                                       authority-common
                                       {:excluded-functions excluded-functions,
                                        :direct-handler-function
                                        p15-s23-reference-runtime-handler-function,
                                        :mode :reference-test-interpreter,
                                        :capability :io/stdout,
                                        :scope :closed-plan-interpreter,
                                        :handler-principal handler-principal,
                                        :transitive-function-scope handler-scope,
                                        :effect :io/write,
                                        :principal source-principal,
                                        :provider
                                        :gravity.reference/transcript-capture,
                                        :grant
                                        :gravity.reference/grant-reference-stdout,
                                        :decision :grant})
                                     (merge
                                       authority-common
                                       {:excluded-functions excluded-functions,
                                        :source-principal source-principal,
                                        :direct-handler-function
                                        p15-s23-reference-runtime-handler-function,
                                        :mode :reference-test-interpreter,
                                        :capability :test/fixture,
                                        :scope :closed-plan-interpreter,
                                        :transitive-function-scope handler-scope,
                                        :effect :io/write,
                                        :principal handler-principal,
                                        :provider
                                        :gravity.reference/transcript-capture,
                                        :grant :gravity.reference/grant-test-fixture,
                                        :decision :grant})
                                     (merge
                                       authority-common
                                       {:effect :memory/allocate,
                                        :capability :memory/allocator,
                                        :provider
                                        :gravity.reference/jvm-managed-allocator,
                                        :grant
                                        :gravity.reference/grant-managed-allocation,
                                        :decision :grant,
                                        :principal source-principal,
                                        :scope :pinned-runtime-plan})}]
      (p15-s23-reference-runtime-ensure!
        source-path
        target
        :runtime-contract-source-selection-count
        1
        (count (:source-selections selections)))
      (p15-s23-reference-runtime-ensure!
        source-path
        target
        :runtime-contract-source-selection
        expected-source-selection
        (first (:source-selections selections)))
      (p15-s23-reference-runtime-ensure!
        source-path
        target
        :runtime-contract-handler-selection-set
        {:count 2, :records expected-handler-selections}
        {:count (count (:handler-selections selections)),
         :records (set (:handler-selections selections))})
      (p15-s23-reference-runtime-ensure!
        source-path
        target
        :runtime-contract-ambiguous-selections
        []
        (:ambiguous-selections selections))
      (p15-s23-reference-runtime-ensure!
        source-path
        target
        :runtime-contract-capability-table-rows
        {:count 3, :records expected-capability-rows}
        {:count (count (:rows capability-table)),
         :records (set (:rows capability-table))})
      (p15-s23-reference-runtime-ensure!
        source-path
        target
        :runtime-contract-denied-ambient-authority
        #{:ambient-stdout :ambient-test-fixture :ambient-allocator}
        (:denied-ambient-authority capability-table)))
    state))
