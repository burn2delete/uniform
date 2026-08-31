(defn- __gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_transcript_and_evidence_validation_02 [state]
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
    (try
      (when (= :capture (:failure-injection authority))
        (throw
          (ex-info
            "injected transcript handler failure"
            {:failure-injection :capture})))
      (catch
        Exception
        exception
        (let [redaction {:cause-class (.getName (class exception)),
                         :cause-message-hash
                         (str "sha256:" (sha256-hex (or (.getMessage exception) "")))}]
          (reject
            "R1-FAILURE"
            :injected-capture-failure
            (capture-action
              :failed-before-commit
              true
              false
              false
              "R1-FAILURE"
              :repair_or_replace_transcript_handler
              {:redaction redaction, :failure-cause redaction})
            (fixture-decision
              :grant
              :explicit-reference-harness-policy
              {:redaction redaction, :failure-cause redaction})
            {:redaction redaction, :failure-cause redaction}))))
    state))
