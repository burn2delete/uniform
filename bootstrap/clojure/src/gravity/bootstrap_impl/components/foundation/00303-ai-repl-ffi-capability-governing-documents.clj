

(def ai-repl-ffi-capability-governing-documents
  ["docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md"
   "docs/phase-08-runtime-architecture/120-r9-repl-and-interactive-runtime-design.md"
   "docs/phase-08-runtime-architecture/121-r10-ffi-runtime-design.md"
   "docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md"
   "docs/phase-08-runtime-architecture/123-r12-runtime-observability-and-diagnostics-design.md"
   "docs/phase-02-safety/039-safe10-capability-security-model.md"
   "docs/phase-02-safety/040-safe11-taint-tracking-and-input-safety.md"
   "docs/phase-02-safety/041-safe12-generated-code-safety.md"
   "docs/phase-02-safety/042-safe13-ai-tool-safety.md"
   "docs/phase-03-profile-system/048-p3-meta-profile-specification.md"
   "docs/phase-03-profile-system/055-p10-ai-profile-specification.md"])

(def ai-repl-ffi-capability-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity")

(def ai-repl-ffi-capability-diagnostic-ids
  ["R8-MODEL"
   "R8-PROMPT"
   "R8-TOOL"
   "R8-TAINT"
   "R8-SECRET"
   "R8-MEMORY"
   "R8-HUMAN-REVIEW"
   "R8-REPLAY"
   "R8-BUDGET"
   "R8-GENERATED"
   "R8-MANIFEST"
   "R9-PROFILE"
   "R9-CHECKS"
   "R9-CAPABILITY"
   "R9-SESSION"
   "R9-HERMETICITY"
   "R9-HOT-RELOAD"
   "R9-DEBUG"
   "R9-AUDIT"
   "R9-MANIFEST"
   "R10-BINDING"
   "R10-ABI"
   "R10-WRAPPER"
   "R10-POINTER"
   "R10-NULL"
   "R10-EFFECT"
   "R10-CAPABILITY"
   "R10-CALLBACK"
   "R10-DYNAMIC"
   "R10-MANIFEST"
   "R11-GRANT"
   "R11-AMBIENT"
   "R11-PRINCIPAL"
   "R11-DELEGATE"
   "R11-REVOKE"
   "R11-TOOL"
   "R11-SECRET"
   "R11-OBSERVABILITY"
   "R11-AUDIT"
   "R11-MANIFEST"])

(def ai-repl-ffi-capability-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             ai-repl-ffi-capability-diagnostic-ids)))

(defn ai-repl-ffi-capability-source-overrides
  [module]
  (get-in module [:metadata :runtime :ai-repl-ffi] {}))

(defn ai-repl-ffi-capability-missing-policy
  [id]
  (case id
    "R8-MODEL" :model-provider-effect-capability-schema-budget-replay
    "R8-PROMPT" :prompt-provenance-role-policy
    "R8-TOOL" :tool-schema-capability-human-review-timeout-retry
    "R8-TAINT" :validated-model-tool-memory-output-before-trusted-sink
    "R8-SECRET" :secret-redaction-and-denial-policy
    "R8-MEMORY" :memory-retention-trust-privacy-deletion-policy
    "R8-HUMAN-REVIEW" :required-human-review-decision
    "R8-REPLAY" :recorded-model-tool-output-for-replay
    "R8-BUDGET" :ai_budget_trace_and_limits
    "R8-GENERATED" :generated_code_compiler_validation_gate
    "R8-MANIFEST" :complete-ai-runtime-artifact
    "R9-PROFILE" :interactive-runtime-profile-legality
    "R9-CHECKS" :normal-compiler-pipeline-before-evaluation
    "R9-CAPABILITY" :interactive-effect-grants
    "R9-SESSION" :tracked-session-state
    "R9-HERMETICITY" :build-affecting-session-state-artifact
    "R9-HOT-RELOAD" :stale-artifact-invalidation
    "R9-DEBUG" :debugger-capability-and-secret-policy
    "R9-AUDIT" :session-transcript-and-evaluated-form-record
    "R9-MANIFEST" :complete-repl-runtime-artifact
    "R10-BINDING" :complete-ffi-binding-manifest
    "R10-ABI" :abi_calling_convention_layout_symbol_evidence
    "R10-WRAPPER" :safe_wrapper_preconditions_checks_ensures
    "R10-POINTER" :foreign_pointer_handle_lifetime_ownership_policy
    "R10-NULL" :checked_foreign_nullability
    "R10-EFFECT" :foreign_effect_declaration
    "R10-CAPABILITY" :foreign_action_runtime_authority
    "R10-CALLBACK" :callback_thread_taint_error_capability_adapter
    "R10-DYNAMIC" :package_deployment_dynamic_loading_policy
    "R10-MANIFEST" :complete-ffi-runtime-artifact
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

(defn ai-repl-ffi-capability-fail!
  [id source-path subject extra]
  (fail! id
         "P08 AI, REPL, FFI, and capability runtime validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :ai-repl-ffi-capability-runtime
                 :stage :ai-repl-ffi-capability-runtime
                 :profile (or (:profile subject) :ai)
                 :target (or (:target subject) :jvm)
                 :runtime-family (cond
                                   (str/starts-with? id "R8") :ai
                                   (str/starts-with? id "R9") :interactive
                                   (str/starts-with? id "R10") :ffi
                                   :else :capability)
                 :agent-id (:agent-id subject)
                 :model-id (:model-id subject)
                 :tool-id (:tool-id subject)
                 :prompt-role (:prompt-role subject)
                 :session-id (:session-id subject)
                 :binding-id (:binding-id subject)
                 :foreign-symbol (:foreign-symbol subject)
                 :wrapper-id (:wrapper-id subject)
                 :action-id (:action-id subject)
                 :principal (:principal subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :provider (:provider subject)
                 :policy (:policy subject)
                 :taint-category (:taint-category subject)
                 :human-review-requirement (:human-review-requirement subject)
                 :replay-mode (:replay-mode subject)
                 :decision (:decision subject)
                 :redaction-status (:redaction-status subject)
                 :missing-policy (ai-repl-ffi-capability-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-T05 requires explicit AI, REPL, FFI, and runtime capability artifacts with model/tool/memory policy, interactive compiler checks, FFI wrapper and handle safety, deny-by-default capability decisions, redaction, audit, and replay records."}
                extra)))

(defn ai-repl-ffi-capability-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get ai-repl-ffi-capability-override-diagnostics fail-kind)]
      (ai-repl-ffi-capability-fail!
       id source-path
       {:agent-id "agent/failing"
        :model-id "model/failing"
        :tool-id "tool/failing"
        :prompt-role :user
        :session-id "session/failing"
        :binding-id "ffi/failing"
        :foreign-symbol (symbol "foreign" (name fail-kind))
        :wrapper-id "wrapper/failing"
        :action-id (str "action-" (name fail-kind))
        :principal :runtime/failing
        :effect fail-kind
        :capability fail-kind
        :provider fail-kind
        :policy fail-kind
        :taint-category fail-kind
        :human-review-requirement fail-kind
        :replay-mode fail-kind
        :decision :deny
        :redaction-status fail-kind}
       {:missing-fields [fail-kind]}))))