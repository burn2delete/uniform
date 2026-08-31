(let [static-rebuild-token (nth __gravity_bootstrap_checked_core_authority_values 0)
      p15-s23-stage2-closed-checked-core-source-artifact-internal __gravity_bootstrap_checked_core_source_helper
      p15-s23-stage2-closed-checked-core-rebuild-internal __gravity_bootstrap_checked_core_rebuild_helper]
(defn p15-s23-stage2-closed-checked-core-verification-replay
  [artifact context authority-evidence]
  (p15-s23-checked-core-bounded-context! context)
  (p15-s23-checked-core-bounded-ingress!
   "C6-VERIFY" :verification-replay-artifact artifact
   p15-s23-reference-runtime-max-packet-nodes
   p15-s23-reference-runtime-max-closed-plan-carrier-depth)
  (p15-s23-checked-core-bounded-ingress!
   "C8-CAPABILITY" :verification-replay-program-authority-evidence
   authority-evidence p15-s23-reference-runtime-max-contract-nodes
   p15-s23-reference-runtime-max-contract-depth)
  (when-not (and (= :effectful-reference
                    (p15-s23-stage2-closed-checked-core-context-mode context))
                 (p15-s23-checked-core-authority-record-valid?
                  (:authority-record context)))
    (p15-s23-closed-core-fail!
     "C8-CAPABILITY" "<checked-core-verification-replay>" {}
     {:missing-fact :bounded-authenticated-verification-replay-context}))
  (p15-s23-closed-core-source-request-bounds!
   (:source-path context) (:source-text context)
   (:requested-target context))
  (let [source-path (:source-path context)
        source-text (:source-text context)
        requested-target (:requested-target context)
        emitter-rule
        (c-backend-stage2-plan-emitter-source-rule!
         source-path requested-target)
        plan
        (binding [*additional-bootstrap-targets*
                  stage2-runtime-derived-source-targets]
          (p15-s23-stage2-plan-emitter-compile-source
           (:emitter emitter-rule) source-path source-text))
        validation
        (p15-s23-closed-runtime-plan-validation!
         source-path requested-target plan)
        runtime-rule
        (c-backend-stage2-runtime-source-rule!
         source-path requested-target)
        policy
        (get (:runtime-contract-definitions runtime-rule)
             'p15-s23-checked-core-verification-replay-policy)
        audit-policy
        (get (:runtime-contract-definitions runtime-rule)
             'p15-s23-checked-core-verification-replay-audit-policy)
        reissued-program-authority
        (p15-s23-stage2-closed-checked-core-authority-binding
         source-path source-text requested-target
         p15-s23-checked-core-reference-policy-selector)]
    (when-not (and (= :effectful-reference
                      (p15-s23-stage2-closed-checked-core-context-mode
                       context))
                   (= :jvm requested-target)
                   (= (:source-content-hash authority-evidence)
                      (str "sha256:" (sha256-hex source-text)))
                   (= (:plan-id authority-evidence) (:plan-id plan))
                   (= (:module authority-evidence)
                      (get-in plan [:module :module]))
                   (= (:authority-record-id (:authority-record context))
                      (:authority-record-id authority-evidence))
                   (= reissued-program-authority
                      (:authority-record context))
                   (= (:evidence-id authority-evidence)
                      (get-in artifact
                              [:source-core-input :authority-evidence
                               :evidence-id]))
                   (p15-s23-reference-runtime-rule-authentic? runtime-rule))
      (p15-s23-closed-core-fail!
       "C8-CAPABILITY" source-path authority-evidence
       {:missing-fact
        :verification-replay-source-artifact-plan-runtime-authority-binding}))
    (let [replay-authority
          (p15-s23-checked-core-verification-replay-authority-record
           artifact context plan validation runtime-rule policy audit-policy)
          gate-result
          (p15-s23-checked-core-verification-replay-gate-invoke
           artifact context plan validation runtime-rule replay-authority)
          result (:result gate-result)
          adapter-record
          (p15-s23-reference-runtime-success-adapter-record
           (:plan-id plan) (get-in plan [:source :sha256])
           (contains? (:observed-operation-set validation) :println)
           (boolean (seq (:stdout result))))
          expected-evidence
          (p15-s23-checked-core-reference-execution-evidence
           authority-evidence {:result result :adapter-record adapter-record})]
      {:expected-execution-evidence expected-evidence
       :replay-record (:replay-record gate-result)
       :actual-path-context
       {:source-path source-path
        :runtime-artifact-source-path
        (:runtime-artifact-source-path runtime-rule)
        :identity-bearing? false}})))

(defn- p15-s23-authorized-checked-core-verification-rebuild
    [artifact context authority-evidence execution-evidence source-path]
    (let [replay
          (p15-s23-stage2-closed-checked-core-verification-replay
           artifact context authority-evidence)
          replay-execution-evidence
          (:expected-execution-evidence replay)]
      (when-not (= execution-evidence replay-execution-evidence)
        (p15-s23-closed-core-fail!
         "C10-CHECK" source-path artifact
         {:missing-fact
          :verification-replay-exact-execution-evidence-parity
          :result-committed? false
          :output-committed? false}))
      {:verification-replay replay
       :fresh
       (p15-s23-stage2-closed-checked-core-rebuild-internal
        context replay-execution-evidence)})))
