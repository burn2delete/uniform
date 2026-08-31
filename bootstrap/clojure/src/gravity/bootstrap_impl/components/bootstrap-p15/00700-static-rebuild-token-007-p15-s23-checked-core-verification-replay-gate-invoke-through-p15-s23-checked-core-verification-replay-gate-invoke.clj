(let [static-rebuild-token (nth __gravity_bootstrap_checked_core_authority_values 0)
      p15-s23-stage2-closed-checked-core-source-artifact-internal __gravity_bootstrap_checked_core_source_helper
      p15-s23-stage2-closed-checked-core-rebuild-internal __gravity_bootstrap_checked_core_rebuild_helper]
(defn p15-s23-checked-core-verification-replay-gate-invoke
  [artifact context plan validation runtime-rule authority]
  (p15-s23-checked-core-bounded-context! context)
  (doseq [[definition value maximum-nodes maximum-depth]
          [[:verification-gate-artifact artifact
            p15-s23-reference-runtime-max-packet-nodes
            p15-s23-reference-runtime-max-closed-plan-carrier-depth]
           [:verification-gate-plan plan
            p15-s23-reference-runtime-max-contract-nodes
            p15-s23-reference-runtime-max-closed-plan-carrier-depth]
           [:verification-gate-validation validation
            p15-s23-reference-runtime-max-contract-nodes
            p15-s23-reference-runtime-max-contract-depth]
           [:verification-gate-runtime-rule runtime-rule
            p15-s23-reference-runtime-max-rule-nodes
            p15-s23-reference-runtime-max-closed-plan-carrier-depth]
           [:verification-gate-authority authority
            p15-s23-reference-runtime-max-contract-nodes
            p15-s23-reference-runtime-max-contract-depth]]]
    (p15-s23-checked-core-bounded-ingress!
     "C8-CAPABILITY" definition value maximum-nodes maximum-depth))
  (let [source-path (:source-path context)
        policy
        (get (:runtime-contract-definitions runtime-rule)
             'p15-s23-checked-core-verification-replay-policy)
        audit-policy
        (get (:runtime-contract-definitions runtime-rule)
             'p15-s23-checked-core-verification-replay-audit-policy)
        expected
        (p15-s23-checked-core-verification-replay-authority-record
         artifact context plan validation runtime-rule policy audit-policy)
        _
        (when-not
         (p15-s23-checked-core-verification-replay-authority-structurally-valid?
          authority)
          (p15-s23-checked-core-verification-replay-gate-fail!
           "R11-GRANT" source-path expected
           :bounded-verification-replay-authority-schema nil false))]
    (when-not (and (p15-s23-reference-runtime-rule-authentic? runtime-rule)
                   (= p15-s23-checked-core-expected-verification-replay-policy
                      policy)
                   (= p15-s23-checked-core-expected-verification-replay-audit-policy
                      audit-policy)
                   (= expected authority))
      (p15-s23-checked-core-verification-replay-gate-fail!
       "R11-GRANT" source-path expected
       :exact-pinned-verification-replay-authority nil false))
    (try
      (p15-s23-checked-core-verification-replay-provider-preflight!
       authority)
      (catch StackOverflowError error
        (p15-s23-checked-core-verification-replay-gate-fail!
         "R1-FAILURE" source-path expected
         :verification-replay-provider-preflight-host-stack error false))
      (catch Exception exception
        (p15-s23-checked-core-verification-replay-gate-fail!
         "R1-FAILURE" source-path expected
         :verification-replay-provider-preflight exception false)))
    (let [pre-call-audit
          (p15-s23-checked-core-verification-replay-audit-records
           authority false :pre-call)
          _ (when-not
             (p15-s23-checked-core-verification-replay-audit-records-valid?
              authority pre-call-audit false :pre-call)
              (p15-s23-checked-core-verification-replay-gate-fail!
               "R11-GRANT" source-path authority
               :pre-call-verification-grant-decision-audit nil false))
          pre-call-decision-records (:decision-records pre-call-audit)
          result
          (try
            (p15-s23-stage2-runtime-artifact-invoke
             runtime-rule
             p15-s23-stage2-runtime-artifact-closed-plan-function
             [plan])
            (catch clojure.lang.ExceptionInfo exception
              (if (p15-s23-reference-runtime-structured-diagnostic?
                   exception
                   #{(:runtime-artifact-source-path runtime-rule)
                     (get-in plan [:source :path])})
                (p15-s23-checked-core-verification-replay-gate-fail!
                 (:id (ex-data exception)) source-path authority
                 (or (:missing-fact (ex-data exception))
                     :structured-verification-runtime-rejection)
                 exception true)
                (p15-s23-checked-core-verification-replay-gate-fail!
                 "R4-EXCEPTION" source-path authority
                 :untranslated-verification-runtime-exception
                 exception true)))
            (catch StackOverflowError error
              (p15-s23-checked-core-verification-replay-gate-fail!
               "R1-FAILURE" source-path authority
               :verification-runtime-host-stack-failure error true))
            (catch Exception exception
              (p15-s23-checked-core-verification-replay-gate-fail!
               "R4-EXCEPTION" source-path authority
               :untranslated-verification-runtime-exception
               exception true)))
          ]
      (try
        (let [_ (when-not
                 (p15-s23-checked-core-reference-result-valid? result plan)
                  (p15-s23-checked-core-verification-replay-gate-fail!
                   "R4-EXCEPTION" source-path authority
                   :exact-checked-core-reference-result-envelope nil true))
              capture-invoked? (boolean (seq (:stdout result)))
              audit
              (p15-s23-checked-core-verification-replay-audit-records
               authority capture-invoked?)
              _ (when-not
                 (p15-s23-checked-core-verification-replay-audit-records-valid?
                  authority audit capture-invoked?)
                  (p15-s23-checked-core-verification-replay-gate-fail!
                   "R11-GRANT" source-path authority
                   :exact-content-addressed-verification-replay-audit nil true))
              _ (when-not (= pre-call-decision-records
                             (:decision-records audit))
                  (p15-s23-checked-core-verification-replay-gate-fail!
                   "R11-GRANT" source-path authority
                   :pre-call-post-call-verification-decision-parity nil true))
              binding (:binding authority)
              replay-base
              {:kind :gravity/p15-s23-checked-core-verification-replay-record
               :status :passed
               :binding binding
               :replay-authority-record-id (:authority-record-id authority)
               :verification-policy-hash (:policy-hash authority)
               :verification-audit-policy-hash (:audit-policy-hash authority)
               :provider-selection-record-ids
               (mapv :provider-selection-record-id
                     (:provider-selection-records authority))
               :grant-record-ids
               (mapv :grant-record-id (:grant-records authority))
               :decision-records (:decision-records audit)
               :pre-call-decision-record-ids
               (mapv :decision-id pre-call-decision-records)
               :action-records (:action-records audit)
               :result-hash
               (p15-s23-reference-runtime-hash (:entrypoint-result result))
               :transcript-hash
               (str "sha256:" (sha256-hex (:stdout result)))
               :replay-count 1
               :runtime-evaluation-count 1
               :authoritative-adapter-invocation-count 0
               :verification-replay-gate-invocation-count 1
               :verification-authority-consumed? true
               :program-authority-consumed? false
               :program-authorizing? false
               :authoritative-invocation? false
               :included-in-authoritative-invocation-count? false
               :result-producing? true
               :external-io-delivery :in-memory-reference-transcript
               :live-external-io? false
               :deployment-runtime? false
               :clojure-seed-boundary? true
               :self-hosted? false}
              replay-record
              (assoc replay-base :replay-id
                     (p15-s23-reference-runtime-hash replay-base))]
          {:result result
           :replay-record replay-record})
        (catch clojure.lang.ExceptionInfo exception
          (if (= :p15-s23-checked-core-verification-replay-gate
                 (:stage (ex-data exception)))
            (throw exception)
            (p15-s23-checked-core-verification-replay-gate-fail!
             "R4-EXCEPTION" source-path authority
             :verification-replay-post-call-translation
             exception true)))
        (catch StackOverflowError error
          (p15-s23-checked-core-verification-replay-gate-fail!
           "R1-FAILURE" source-path authority
           :verification-replay-post-call-host-stack error true))
        (catch Exception exception
          (p15-s23-checked-core-verification-replay-gate-fail!
           "R4-EXCEPTION" source-path authority
           :verification-replay-post-call-translation exception true)))))))
