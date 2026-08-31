

(def r11-document-diagnostic-ids
  ["R11-GRANT" "R11-AMBIENT" "R11-PRINCIPAL" "R11-DELEGATE"
   "R11-REVOKE" "R11-TOOL" "R11-SECRET" "R11-OBSERVABILITY"
   "R11-AUDIT" "R11-MANIFEST"])

(def r11-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             r11-document-diagnostic-ids)))

(defn r11-document-source-overrides
  [module]
  (or (get-in module [:metadata :runtime :r11-document])
      (get-in module [:metadata :runtime :ai-repl-ffi])
      {}))

(defn r11-document-missing-policy
  [id]
  (case id
    "R11-GRANT" :matching_runtime_capability_grant
    "R11-AMBIENT" :ambient_authority_rejection
    "R11-PRINCIPAL" :runtime_principal_identity
    "R11-DELEGATE" :scoped_delegated_handle
    "R11-REVOKE" :revocation_record_or_supported_assumption
    "R11-TOOL" :tool_plugin_dual_contract_check
    "R11-SECRET" :secret_redaction_policy
    "R11-OBSERVABILITY" :observability_sink_grant
    "R11-AUDIT" :capability_decision_log
    :complete-runtime-capability-artifact))

(defn r11-document-fail!
  [id source-path subject extra]
  (fail! id
         "R11 runtime capability enforcement document coverage failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :r11-runtime-capability-document
                 :stage :r11-document-coverage
                 :document-id "R11"
                 :profile (or (:profile subject) :ai)
                 :target (or (:target subject) :jvm)
                 :runtime-family :capability
                 :action-id (:action-id subject)
                 :principal (:principal subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :provider (:provider subject)
                 :policy (or (:policy subject)
                             (r11-document-missing-policy id))
                 :decision (:decision subject)
                 :redaction-status (:redaction-status subject)
                 :artifact-id (:artifact-id subject)
                 :missing-policy (r11-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-D122 requires deny-by-default runtime capability manifests, principal identity, grant/deny/delegate/revoke records, scoped handles, tool/plugin contract checks, secret redaction, observability authority, audit logs, and R11 conformance evidence."}
                extra)))

(defn r11-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get r11-document-override-diagnostics fail-kind)]
      (r11-document-fail!
       id source-path
       {:action-id (str "action-" (name fail-kind))
        :principal :runtime/failing
        :effect fail-kind
        :capability fail-kind
        :provider fail-kind
        :policy fail-kind
        :decision :deny
        :redaction-status fail-kind
        :artifact-id (str "r11-document-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))

(defn r11-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/r11-runtime-capability-diagnostic-stream
   :stage :r11-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :r11-document-coverage
            :document-id "R11"
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-r11-document-syntax-" index)
                      :artifact input-id}
            :profile :ai
            :target :jvm
            :runtime-family :capability
            :action-id (case id
                         "R11-GRANT" "database/write"
                         "R11-AMBIENT" "network/http"
                         "R11-TOOL" "tool/write-ticket"
                         "R11-SECRET" "secret/read"
                         "R11-OBSERVABILITY" "observability/write"
                         (str "action/" (str/lower-case id)))
            :principal :agent/support
            :effect (case id
                      "R11-GRANT" :database/write
                      "R11-AMBIENT" :network/http
                      "R11-TOOL" :filesystem/write
                      "R11-SECRET" :secrets/read
                      "R11-OBSERVABILITY" :runtime/observability
                      nil)
            :capability (case id
                          "R11-GRANT" :db/write
                          "R11-AMBIENT" :http/client
                          "R11-TOOL" :fs/write
                          "R11-SECRET" :secret/read
                          "R11-OBSERVABILITY" :observability/write
                          nil)
            :provider (case id
                        "R11-GRANT" :db-provider
                        "R11-AMBIENT" :network-provider
                        "R11-TOOL" :tool-provider
                        "R11-SECRET" :secret-store
                        "R11-OBSERVABILITY" :local-observability
                        nil)
            :policy-source :deployment-policy
            :policy (r11-document-missing-policy id)
            :decision (case id
                        "R11-GRANT" :deny
                        "R11-AMBIENT" :deny
                        "R11-TOOL" :deny
                        "R11-SECRET" :deny
                        "R11-OBSERVABILITY" :deny
                        :deny)
            :redaction-status (case id
                                "R11-SECRET" :required
                                "R11-AUDIT" :public-safe
                                :not-sensitive)
            :missing-policy (r11-document-missing-policy id)
            :source-generated-origin-chain
            [:ai-repl-ffi-capability-runtime :r11-document-coverage]
            :facts {:runtime-checks-do-not-grant-authority true
                    :deny-by-default true
                    :delegated-handles-scoped true
                    :decision-logs-redacted true}
            :remediation [{:kind :attach-runtime-grant}
                          {:kind :reject-ambient-authority}
                          {:kind :scope-or-revoke-handle}
                          {:kind :record-redacted-decision-log}]
            :redactions []
            :ordering-key [id :r11-document-coverage]})
         r11-document-diagnostic-ids
         (range))
   :status :complete})

(defn r11-document-requirements-coverage
  [ai-artifact]
  (let [manifest (:runtime-capability-manifest ai-artifact)
        table (:capability-table ai-artifact)
        identity (:principal-identity-record ai-artifact)
        decision-log (:runtime-decision-log ai-artifact)
        delegated (:delegated-handle-record ai-artifact)
        revocation (:revocation-record ai-artifact)
        denial (:denial-diagnostic-record ai-artifact)
        redaction (:redaction-secret-handling-record ai-artifact)
        conformance (:capability-conformance-evidence ai-artifact)
        decisions (set (map :decision (:rows table)))]
    {:artifact :gravity/r11-runtime-capability-requirements-coverage
     :ai-runtime-input (:artifact-id ai-artifact)
     :manifest-status (:status manifest)
     :handles (:handles manifest)
     :manifest-decisions (:decisions manifest)
     :manifest-rejects (:rejects manifest)
     :deny-by-default? (:deny-by-default? manifest)
     :table-status (:status table)
     :table-decisions decisions
     :grant-count (count (filter #(= :grant (:decision %)) (:rows table)))
     :deny-count (count (filter #(= :deny (:decision %)) (:rows table)))
     :observability-authority?
     (boolean (some #(= :observability/write (:capability %)) (:rows table)))
     :principal-status (:status identity)
     :principal-count (count (:principals identity))
     :invalid-principals (:invalid-principals identity)
     :decision-log-status (:status decision-log)
     :decision-count (count (:decisions decision-log))
     :missing-required-audit (:missing-required-audit decision-log)
     :delegated-status (:status delegated)
     :unscoped-handles (:unscoped-handles delegated)
     :revocation-status (:status revocation)
     :use-after-revocation
     (filterv :use-after-revocation? (:records revocation))
     :denial-status (:status denial)
     :denial-count (count (:denials denial))
     :redaction-status (:status redaction)
     :secret-leaks (:secret-leaks redaction)
     :conformance-status (:status conformance)
     :deny-by-default-demonstrated?
     (:deny-by-default-demonstrated? conformance)
     :grant-deny-delegate-revoke-covered?
     (:grant-deny-delegate-revoke-covered? conformance)
     :caller-tool-plugin-contracts-covered?
     (:caller-tool-plugin-contracts-covered? conformance)
     :secret-redaction-covered?
     (:secret-redaction-covered? conformance)
     :deployment-policy-narrowing-covered?
     (:deployment-policy-narrowing-covered? conformance)
     :status :complete}))