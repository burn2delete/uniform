(defn- __gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_provider_actions [state]
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
          required-grant-ids
          decision
          action
          allocation-decision
          stdout-decision
          fixture-decision
          deployment-decision]} state
        allocation-action (fn [status
                               started?
                               result-committed?
                               &
                               [diagnostic remediation extra]]
                            (action
                              (merge
                                {:remediation (or remediation :none),
                                 :diagnostic diagnostic,
                                 :provider-id allocation-provider,
                                 :output-committed? false,
                                 :action-status status,
                                 :operation :managed-allocation,
                                 :mode :pinned-reference,
                                 :grant-id allocation-grant,
                                 :capability :memory/allocator,
                                 :scope :pinned-runtime-plan,
                                 :action-id
                                 :gravity.reference/action-managed-string-allocation,
                                 :live-external-authority? false,
                                 :result-committed? result-committed?,
                                 :effect :memory/allocate,
                                 :action-started? started?,
                                 :principal-id source-principal}
                                extra)))
        stdout-action (fn [status
                           started?
                           result-committed?
                           output-committed?
                           &
                           [diagnostic remediation]]
                        (action
                          {:remediation (or remediation :none),
                           :diagnostic diagnostic,
                           :provider-id capture-provider,
                           :output-committed? output-committed?,
                           :action-status status,
                           :operation :authorize-reference-stdout,
                           :mode :reference-test-interpreter,
                           :grant-id stdout-grant,
                           :capability :io/stdout,
                           :scope :closed-plan-interpreter,
                           :action-id :gravity.reference/action-transcript-capture,
                           :live-external-authority? false,
                           :result-committed? result-committed?,
                           :handler-principal-id handler-principal,
                           :effect :io/write,
                           :action-started? started?,
                           :principal-id source-principal}))
        capture-action (fn [status
                            started?
                            result-committed?
                            output-committed?
                            &
                            [diagnostic remediation extra]]
                         (action
                           (merge
                             {:remediation (or remediation :none),
                              :diagnostic diagnostic,
                              :source-principal-id source-principal,
                              :provider-id capture-provider,
                              :output-committed? output-committed?,
                              :action-status status,
                              :operation :ordered-string-append,
                              :mode :reference-test-interpreter,
                              :grant-id fixture-grant,
                              :capability :test/fixture,
                              :scope :closed-plan-interpreter,
                              :action-id :gravity.reference/action-transcript-capture,
                              :live-external-authority? false,
                              :source-grant-id stdout-grant,
                              :handled-effect :io/write,
                              :result-committed? result-committed?,
                              :effect :io/write,
                              :action-started? started?,
                              :source-capability :io/stdout,
                              :principal-id handler-principal}
                             extra)))
        deployment-action (fn []
                            (action
                              {:remediation
                               :select_deployment_stdout_provider_and_grant,
                               :diagnostic "L15-PROVIDER-MISSING",
                               :provider-id :unresolved,
                               :output-committed? false,
                               :action-status :rejected-before-start,
                               :operation :deployment-stdout,
                               :mode :deployment,
                               :grant-id :unresolved,
                               :capability :io/stdout,
                               :scope :deployment-runtime,
                               :action-id :gravity.reference/action-deployment-stdout,
                               :live-external-authority? false,
                               :result-committed? false,
                               :effect :io/write,
                               :action-started? false,
                               :principal-id source-principal}))
        preflight-decision (fn [diagnostic reason]
                             (decision
                               {:diagnostic diagnostic,
                                :provider-id allocation-provider,
                                :mode :pinned-reference,
                                :grant-id allocation-grant,
                                :capability :memory/allocator,
                                :scope :pinned-runtime-plan,
                                :action-id
                                :gravity.reference/action-managed-string-allocation,
                                :reason reason,
                                :result :deny,
                                :live-external-authority? false,
                                :effect :memory/allocate,
                                :decision :deny,
                                :principal-id source-principal}))
        preflight-action (fn [diagnostic remediation]
                           (action
                             {:remediation remediation,
                              :diagnostic diagnostic,
                              :provider-id allocation-provider,
                              :output-committed? false,
                              :action-status :rejected-before-start,
                              :operation :managed-allocation-preflight,
                              :mode :pinned-reference,
                              :grant-id allocation-grant,
                              :capability :memory/allocator,
                              :scope :pinned-runtime-plan,
                              :action-id
                              :gravity.reference/action-managed-string-allocation,
                              :live-external-authority? false,
                              :result-committed? false,
                              :effect :memory/allocate,
                              :action-started? false,
                              :principal-id source-principal}))
        noncapability-preflight-action (fn [diagnostic missing-fact remediation]
                                         (let [record {:remediation remediation,
                                                       :source-span
                                                       {:source-id
                                                        (or
                                                          source-id
                                                          (:runtime-artifact-source-content-hash
                                                            runtime-rule))},
                                                       :diagnostic diagnostic,
                                                       :output-committed? false,
                                                       :redaction :none,
                                                       :action-status
                                                       :rejected-before-start,
                                                       :artifact-id
                                                       (or
                                                         plan-id
                                                         (:runtime-artifact-hash
                                                           runtime-rule)),
                                                       :operation
                                                       :runtime-contract-preflight,
                                                       :generated-origin-chain
                                                       [:source-unit
                                                        :p15-s23-reference-runtime-adapter],
                                                       :missing-fact missing-fact,
                                                       :result-committed? false,
                                                       :runtime-function function,
                                                       :artifact
                                                       :gravity/runtime-preflight-failure-record,
                                                       :action-started? false}]
                                           (assoc
                                             record
                                             :record-id
                                             (p15-s23-reference-runtime-hash record))))
        noncapability-preflight-decision (fn [diagnostic missing-fact]
                                           (let [record
                                                 {:diagnostic diagnostic,
                                                  :redaction :none,
                                                  :plan-id plan-id,
                                                  :missing-fact missing-fact,
                                                  :result :reject,
                                                  :runtime-function function,
                                                  :source-id source-id,
                                                  :artifact
                                                  :gravity/runtime-preflight-decision-record,
                                                  :runtime-artifact-hash
                                                  (:runtime-artifact-hash
                                                    runtime-rule)}]
                                             (assoc
                                               record
                                               :decision-id
                                               (p15-s23-reference-runtime-hash
                                                 record))))]
    (assoc
      state
      'allocation-action
      allocation-action
      'stdout-action
      stdout-action
      'capture-action
      capture-action
      'deployment-action
      deployment-action
      'preflight-decision
      preflight-decision
      'preflight-action
      preflight-action
      'noncapability-preflight-action
      noncapability-preflight-action
      'noncapability-preflight-decision
      noncapability-preflight-decision)))
