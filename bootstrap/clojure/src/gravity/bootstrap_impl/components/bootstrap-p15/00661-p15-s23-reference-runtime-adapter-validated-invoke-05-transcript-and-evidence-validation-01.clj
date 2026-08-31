(defn- __gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_transcript_and_evidence_validation_01 [state]
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
          deployment-decision
          allocation-action
          stdout-action
          capture-action
          deployment-action
          preflight-decision
          preflight-action
          noncapability-preflight-action
          noncapability-preflight-decision
          reject
          reject-host-exception
          reject-preflight
          authority-shape?]} state]
    (when-not (authority-shape?)
      (reject-preflight
        "R11-GRANT"
        :runtime-authority-schema
        :supply_exact_runtime_authority_schema))
    (when-not (= function p15-s23-stage2-runtime-artifact-closed-plan-function)
      (reject-preflight
        "P15S23X002"
        :runtime-contract-function-scope
        :use_closed_plan_runtime_entrypoint))
    (when-not (and
                (= source-principal (:source-principal authority))
                (= handler-principal (:handler-principal authority)))
      (reject-preflight
        "R11-GRANT"
        :runtime-principal-binding
        :restore_source_and_handler_principals))
    (when (seq (set/difference (:providers authority) required-provider-ids))
      (reject-preflight
        "R11-GRANT"
        :runtime-provider-authority-widening
        :remove_ungranted_provider_authority))
    (when (seq (set/difference (:grants authority) required-grant-ids))
      (reject-preflight
        "R11-GRANT"
        :runtime-grant-authority-widening
        :remove_ungranted_capability_authority))
    (when-not (contains? (:providers authority) allocation-provider)
      (reject
        "R5-PROVIDER"
        :managed-allocation-provider
        (allocation-action
          :rejected-before-start
          false
          false
          "R5-PROVIDER"
          :restore_managed_allocator_provider)
        (allocation-decision :deny :provider-missing)))
    (when (and
            writes-stdout?
            (not (contains? (:providers authority) capture-provider)))
      (reject
        "L15-PROVIDER-MISSING"
        :transcript-capture-provider
        (stdout-action
          :rejected-before-start
          false
          false
          false
          "L15-PROVIDER-MISSING"
          :restore_reference_transcript_provider)
        (stdout-decision :deny :provider-missing)))
    (doseq [[grant action-record decision-record] (cond->
                                                    [[allocation-grant
                                                      (allocation-action
                                                        :rejected-before-start
                                                        false
                                                        false
                                                        "R11-GRANT"
                                                        :restore_managed_allocation_grant)
                                                      (allocation-decision
                                                        :deny
                                                        :grant-missing)]]
                                                    writes-stdout?
                                                    (into
                                                      [[stdout-grant
                                                        (stdout-action
                                                          :rejected-before-start
                                                          false
                                                          false
                                                          false
                                                          "R11-GRANT"
                                                          :restore_reference_stdout_grant)
                                                        (stdout-decision
                                                          :deny
                                                          :grant-missing)]
                                                       [fixture-grant
                                                        (capture-action
                                                          :rejected-before-start
                                                          false
                                                          false
                                                          false
                                                          "R11-GRANT"
                                                          :restore_test_fixture_grant)
                                                        (fixture-decision
                                                          :deny
                                                          :grant-missing)]]))]
      (when-not (contains? (:grants authority) grant)
        (reject "R11-GRANT" :runtime-capability-grant action-record decision-record)))
    (when (or (:deployment-stdout? authority) (= :deployment (:mode authority)))
      (reject
        "L15-PROVIDER-MISSING"
        :deployment-stdout-provider
        (deployment-action)
        (deployment-decision)))
    (try
      (when (= :allocation (:failure-injection authority))
        (throw
          (ex-info
            "injected allocation provider failure"
            {:failure-injection :allocation})))
      (catch
        Exception
        exception
        (let [redaction {:cause-class (.getName (class exception)),
                         :cause-message-hash
                         (str "sha256:" (sha256-hex (or (.getMessage exception) "")))}]
          (reject
            "R1-FAILURE"
            :injected-allocation-failure
            (allocation-action
              :failed-before-commit
              true
              false
              "R1-FAILURE"
              :repair_or_replace_allocation_provider
              {:redaction redaction, :failure-cause redaction})
            (allocation-decision
              :grant
              :explicit-grant
              {:redaction redaction, :failure-cause redaction})
            {:redaction redaction, :failure-cause redaction}))))
    (when (and (= :capture (:failure-injection authority)) (not writes-stdout?))
      (reject-preflight
        "P15S23X002"
        :inactive-capture-failure-injection
        :remove_inactive_capture_failure_injection))
    state))
