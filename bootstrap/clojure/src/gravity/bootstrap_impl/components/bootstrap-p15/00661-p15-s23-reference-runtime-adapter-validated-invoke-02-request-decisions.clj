(defn- __gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_request_decisions [state]
  (let [{:syms
         [runtime-rule
          function
          args
          authority
          target-plan
          plan-id
          source-id
          closed-plan-validation
          source-principal
          handler-principal
          allocation-provider
          capture-provider
          allocation-grant
          stdout-grant
          fixture-grant
          writes-stdout?
          required-provider-ids
          required-grant-ids]} state
        decision (fn [attributes]
                   (p15-s23-reference-runtime-decision-record
                     runtime-rule
                     (merge {:plan-id plan-id, :source-id source-id} attributes)))
        action (fn [attributes]
                 (p15-s23-reference-runtime-action-record
                   runtime-rule
                   (merge {:plan-id plan-id, :source-id source-id} attributes)))
        allocation-decision (fn [decision-value reason & [extra]]
                              (decision
                                (merge
                                  {:provider-id allocation-provider,
                                   :mode :pinned-reference,
                                   :grant-id allocation-grant,
                                   :capability :memory/allocator,
                                   :scope :pinned-runtime-plan,
                                   :action-id
                                   :gravity.reference/action-managed-string-allocation,
                                   :reason reason,
                                   :result decision-value,
                                   :live-external-authority? false,
                                   :effect :memory/allocate,
                                   :decision decision-value,
                                   :principal-id source-principal}
                                  extra)))
        stdout-decision (fn [decision-value reason]
                          (decision
                            {:provider-id capture-provider,
                             :mode :reference-test-interpreter,
                             :grant-id stdout-grant,
                             :capability :io/stdout,
                             :scope :closed-plan-interpreter,
                             :action-id :gravity.reference/action-transcript-capture,
                             :reason reason,
                             :result decision-value,
                             :live-external-authority? false,
                             :handler-principal-id handler-principal,
                             :effect :io/write,
                             :decision decision-value,
                             :principal-id source-principal}))
        fixture-decision (fn [decision-value reason & [extra]]
                           (decision
                             (merge
                               {:source-principal-id source-principal,
                                :provider-id capture-provider,
                                :mode :reference-test-interpreter,
                                :grant-id fixture-grant,
                                :capability :test/fixture,
                                :scope :closed-plan-interpreter,
                                :action-id
                                :gravity.reference/action-transcript-capture,
                                :reason reason,
                                :result decision-value,
                                :live-external-authority? false,
                                :effect :io/write,
                                :decision decision-value,
                                :principal-id handler-principal}
                               extra)))
        deployment-decision (fn []
                              (decision
                                {:provider-id :unresolved,
                                 :mode :deployment,
                                 :grant-id :unresolved,
                                 :capability :io/stdout,
                                 :scope :deployment-runtime,
                                 :action-id
                                 :gravity.reference/action-deployment-stdout,
                                 :reason :deployment-provider-unresolved,
                                 :result :deny,
                                 :live-external-authority? false,
                                 :effect :io/write,
                                 :decision :deny,
                                 :principal-id source-principal}))]
    (assoc
      state
      'decision
      decision
      'action
      action
      'allocation-decision
      allocation-decision
      'stdout-decision
      stdout-decision
      'fixture-decision
      fixture-decision
      'deployment-decision
      deployment-decision)))
