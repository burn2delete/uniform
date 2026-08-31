(defn- __gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_final_result [state]
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
    (let [result (try
                   (let [candidate (p15-s23-stage2-runtime-artifact-invoke
                                     runtime-rule
                                     function
                                     args)]
                     (when-not (p15-s23-checked-core-reference-result-valid?
                                 candidate
                                 target-plan)
                       (throw
                         (ex-info
                           "invalid checked-core reference result envelope"
                           {:missing-fact
                            :exact-checked-core-reference-result-envelope})))
                     candidate)
                   (catch
                     clojure.lang.ExceptionInfo
                     ex
                     (if (p15-s23-reference-runtime-structured-diagnostic?
                           ex
                           #{(get-in target-plan [:source :path])
                             (:runtime-artifact-source-path runtime-rule)})
                       (throw ex)
                       (reject-host-exception ex)))
                   (catch Exception ex (reject-host-exception ex)))
          record (p15-s23-reference-runtime-success-adapter-record
                   plan-id
                   source-id
                   writes-stdout?
                   (boolean (seq (:stdout result))))]
      {:result result, :adapter-record record})))
