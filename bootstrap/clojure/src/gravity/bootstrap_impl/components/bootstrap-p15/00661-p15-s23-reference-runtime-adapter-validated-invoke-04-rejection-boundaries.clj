(defn- __gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_rejection_boundaries [state]
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
          noncapability-preflight-decision]} state
        reject (fn [diagnostic missing-fact action-record decision-record & [extra]]
                 (p15-s23-reference-runtime-adapter-fail!
                   diagnostic
                   runtime-rule
                   action-record
                   decision-record
                   missing-fact
                   (or extra {})))
        reject-host-exception (fn [exception]
                                (let [redaction {:cause-class
                                                 (.getName (class exception)),
                                                 :cause-message-hash
                                                 (str
                                                   "sha256:"
                                                   (sha256-hex
                                                     (or (.getMessage exception) "")))}
                                      action-record (action
                                                      {:remediation
                                                       :translate_host_exception_at_runtime_adapter,
                                                       :diagnostic "R4-EXCEPTION",
                                                       :provider-id :unresolved,
                                                       :output-committed? false,
                                                       :redaction redaction,
                                                       :action-status
                                                       :failed-before-commit,
                                                       :failure-cause redaction,
                                                       :operation
                                                       :generic-host-boundary,
                                                       :mode
                                                       :untranslated-host-exception,
                                                       :grant-id :unresolved,
                                                       :capability :unresolved,
                                                       :scope :generic-host-boundary,
                                                       :action-id
                                                       :gravity.reference/action-generic-host-boundary,
                                                       :live-external-authority? false,
                                                       :result-committed? false,
                                                       :effect :unresolved,
                                                       :action-started? true,
                                                       :principal-id source-principal})
                                      decision-record (decision
                                                        {:provider-id :unresolved,
                                                         :redaction redaction,
                                                         :failure-cause redaction,
                                                         :mode
                                                         :untranslated-host-exception,
                                                         :grant-id :unresolved,
                                                         :capability :unresolved,
                                                         :scope :generic-host-boundary,
                                                         :action-id
                                                         :gravity.reference/action-generic-host-boundary,
                                                         :reason
                                                         :untranslated-host-exception,
                                                         :result :deny,
                                                         :live-external-authority?
                                                         false,
                                                         :effect :unresolved,
                                                         :decision :deny,
                                                         :principal-id
                                                         source-principal})]
                                  (p15-s23-reference-runtime-adapter-fail!
                                    "R4-EXCEPTION"
                                    runtime-rule
                                    action-record
                                    decision-record
                                    :untranslated-host-exception
                                    {:redaction redaction,
                                     :failure-cause redaction,
                                     :boundary :generic-host-runner})))
        reject-preflight (fn [diagnostic missing-fact remediation]
                           (if (str/starts-with? diagnostic "P15S23")
                             (reject
                               diagnostic
                               missing-fact
                               (noncapability-preflight-action
                                 diagnostic
                                 missing-fact
                                 remediation)
                               (noncapability-preflight-decision
                                 diagnostic
                                 missing-fact))
                             (reject
                               diagnostic
                               missing-fact
                               (preflight-action diagnostic remediation)
                               (preflight-decision diagnostic missing-fact))))
        authority-shape? (fn []
                           (and
                             (map? authority)
                             (=
                               p15-s23-reference-runtime-authority-keys
                               (set (keys authority)))
                             (contains?
                               #{:closed-plan-reference :deployment}
                               (:mode authority))
                             (symbol? (:source-principal authority))
                             (keyword? (:handler-principal authority))
                             (set? (:providers authority))
                             (set? (:grants authority))
                             (contains?
                               #{nil :capture :allocation}
                               (:failure-injection authority))
                             (boolean? (:deployment-stdout? authority))))]
    (assoc
      state
      'reject
      reject
      'reject-host-exception
      reject-host-exception
      'reject-preflight
      reject-preflight
      'authority-shape?
      authority-shape?)))
