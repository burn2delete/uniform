

(defn r11-document-validate!
  [source-path artifact]
  (let [ai-artifact (:ai-repl-ffi-capability-artifact artifact)
        coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r11-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :complete (get-in ai-artifact
                                   [:capability-based-proof :status]))
      (r11-document-fail! "R11-MANIFEST" source-path ai-artifact
                          {:missing-fields [:runtime-capability-proof]}))
    (when-not (and (= :complete (:manifest-status coverage))
                   (true? (:deny-by-default? coverage)))
      (r11-document-fail! "R11-MANIFEST" source-path coverage
                          {:missing-fields [:runtime-capability-manifest]}))
    (when-not (and (pos? (:grant-count coverage))
                   (pos? (:deny-count coverage)))
      (r11-document-fail! "R11-GRANT" source-path coverage
                          {:missing-fields [:grant-deny-table]}))
    (when-not (contains? (:manifest-rejects coverage) :ambient-authority)
      (r11-document-fail! "R11-AMBIENT" source-path coverage
                          {:missing-fields [:ambient-authority-rejection]}))
    (when-not (and (= :complete (:principal-status coverage))
                   (pos? (:principal-count coverage))
                   (empty? (:invalid-principals coverage)))
      (r11-document-fail! "R11-PRINCIPAL" source-path coverage
                          {:missing-fields [:principal-identity]}))
    (when-not (and (= :complete (:delegated-status coverage))
                   (empty? (:unscoped-handles coverage)))
      (r11-document-fail! "R11-DELEGATE" source-path coverage
                          {:missing-fields [:delegated-handle-scope]}))
    (when-not (and (= :complete (:revocation-status coverage))
                   (empty? (:use-after-revocation coverage)))
      (r11-document-fail! "R11-REVOKE" source-path coverage
                          {:missing-fields [:revocation-record]}))
    (when-not (true? (:caller-tool-plugin-contracts-covered? coverage))
      (r11-document-fail! "R11-TOOL" source-path coverage
                          {:missing-fields [:tool-plugin-contract]}))
    (when-not (and (= :complete (:redaction-status coverage))
                   (empty? (:secret-leaks coverage))
                   (true? (:secret-redaction-covered? coverage)))
      (r11-document-fail! "R11-SECRET" source-path coverage
                          {:missing-fields [:secret-redaction]}))
    (when-not (:observability-authority? coverage)
      (r11-document-fail! "R11-OBSERVABILITY" source-path coverage
                          {:missing-fields [:observability-authority]}))
    (when-not (and (= :complete (:decision-log-status coverage))
                   (pos? (:decision-count coverage))
                   (empty? (:missing-required-audit coverage)))
      (r11-document-fail! "R11-AUDIT" source-path coverage
                          {:missing-fields [:decision-log]}))
    (when-not (= :complete (:conformance-status coverage))
      (r11-document-fail! "R11-MANIFEST" source-path coverage
                          {:missing-fields [:conformance]}))
    (when-not (= (set r11-document-diagnostic-ids) diagnostics)
      (r11-document-fail! "R11-MANIFEST" source-path
                          (:r11-diagnostic-stream artifact)
                          {:missing-fields [:r11-diagnostics]})))
  :complete)

(defn r11-document-capability-proof
  [artifact]
  (let [coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r11-diagnostic-stream
                                       :diagnostics])))]
    {:runtime-capability-input-verified?
     (= :complete (get-in artifact
                          [:ai-repl-ffi-capability-artifact
                           :capability-based-proof :status]))
     :manifest-deny-default-covered?
     (and (= :complete (:manifest-status coverage))
          (true? (:deny-by-default? coverage)))
     :grant-deny-delegate-revoke-covered?
     (and (pos? (:grant-count coverage))
          (pos? (:deny-count coverage))
          (true? (:grant-deny-delegate-revoke-covered? coverage)))
     :principal-identity-covered?
     (and (= :complete (:principal-status coverage))
          (pos? (:principal-count coverage))
          (empty? (:invalid-principals coverage)))
     :ambient-authority-rejected?
     (contains? (:manifest-rejects coverage) :ambient-authority)
     :delegated-handle-scoped?
     (and (= :complete (:delegated-status coverage))
          (empty? (:unscoped-handles coverage)))
     :revocation-safe?
     (and (= :complete (:revocation-status coverage))
          (empty? (:use-after-revocation coverage)))
     :tool-plugin-contract-covered?
     (true? (:caller-tool-plugin-contracts-covered? coverage))
     :secret-redaction-covered?
     (and (= :complete (:redaction-status coverage))
          (empty? (:secret-leaks coverage))
          (true? (:secret-redaction-covered? coverage)))
     :observability-authority-covered?
     (:observability-authority? coverage)
     :audit-decision-log-covered?
     (and (= :complete (:decision-log-status coverage))
          (pos? (:decision-count coverage))
          (empty? (:missing-required-audit coverage)))
     :diagnostics-covered?
     (= (set r11-document-diagnostic-ids) diagnostics)
     :status :complete}))

(defn r11-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (r11-document-source-overrides module)
        _ (r11-document-validate-source-overrides! source-path
                                                   source-overrides)
        ai-artifact
        (ai-repl-ffi-capability-file-artifact
         r11-document-upstream-artifact-path)
        input-id (:artifact-id ai-artifact)
        diagnostic-stream (r11-document-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-r11-runtime-capability-document-artifact
         :task "P08-D122"
         :document-set ["R11"]
         :governing-document r11-document-governing-document
         :pass {:name :r11-runtime-capability-document-coverage
                :input :ai-repl-ffi-capability-runtime-artifact
                :output :r11-document-coverage-artifact
                :requires [:runtime-capability-manifest
                           :capability-table
                           :principal-identity-record
                           :runtime-decision-log
                           :delegated-handle-record
                           :revocation-record
                           :denial-diagnostic-record
                           :redaction-secret-handling-record
                           :capability-conformance-evidence]
                :preserves [:action-id :principal :effect :capability
                            :provider :policy :decision :redaction
                            :source-diagnostics]
                :emits [:requirements-coverage
                        :rejected-design-coverage
                        :conformance-criteria-record
                        :r11-diagnostic-stream]
                :rejects r11-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :ai-repl-ffi-capability-artifact
         (select-keys ai-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :ai-repl-ffi-capability-results])
         :ai-repl-ffi-capability-artifact-kind (:kind ai-artifact)
         :ai-repl-ffi-capability-artifact-hash input-id
         :upstream-artifact-source r11-document-upstream-artifact-path
         :requirements-coverage
         (r11-document-requirements-coverage ai-artifact)
         :rejected-design-coverage
         [{:design :runtime_ambient_authority
           :diagnostic "R11-AMBIENT" :status :rejected}
          {:design :compile_time_profile_as_deployment_authority
           :diagnostic "R11-GRANT" :status :rejected}
          {:design :unscoped_capability_handle
           :diagnostic "R11-DELEGATE" :status :rejected}
          {:design :tool_or_plugin_effect_outside_contract
           :diagnostic "R11-TOOL" :status :rejected}
          {:design :decision_log_leaks_secret_or_omits_audit
           :diagnostic "R11-AUDIT" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/r11-runtime-capability-conformance-record
          :deny_by_default_behavior :complete
          :grant_deny_delegate_revoke_records :complete
          :runtime_action_coverage :complete
          :caller_tool_plugin_dual_contract_checks :complete
          :secret_redaction_and_audit_records :complete
          :deployment_policy_narrowing :complete
          :denial_diagnostic_provenance_links :complete
          :status :passed}
         :r11-diagnostic-stream diagnostic-stream
         :r11-document-results
         {:documents ["R11"]
          :task "P08-D122"
          :required-diagnostic-ids r11-document-diagnostic-ids
          :runtime-capability-input-status :complete
          :manifest-status :complete
          :table-status :complete
          :principal-status :complete
          :decision-log-status :complete
          :delegation-status :complete
          :revocation-status :complete
          :denial-status :complete
          :redaction-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (r11-document-validate! source-path artifact-base)
        capability-proof (r11-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn r11-document-file-artifact
  [path]
  (r11-document-source-artifact path (slurp path)))

(def runtime-observability-governing-documents
  ["docs/phase-08-runtime-architecture/123-r12-runtime-observability-and-diagnostics-design.md"
   "docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md"
   "docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md"
   "docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md"
   "docs/phase-06-compiler-architecture/092-c15-diagnostics-and-error-reporting.md"
   "docs/phase-02-safety/039-safe10-capability-security-model.md"
   "docs/phase-02-safety/040-safe11-taint-tracking-and-input-safety.md"
   "docs/phase-02-safety/042-safe13-ai-tool-safety.md"])

(def runtime-observability-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity")

(def runtime-observability-diagnostic-ids
  ["R12-SINK"
   "R12-SCHEMA"
   "R12-SOURCE"
   "R12-SECRET"
   "R12-SEMANTICS"
   "R12-SAMPLING"
   "R12-REPLAY"
   "R12-BUNDLE"
   "R12-MANIFEST"])

(def runtime-observability-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             runtime-observability-diagnostic-ids)))