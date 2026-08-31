

(defn r9-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (r9-document-source-overrides module)
        _ (r9-document-validate-source-overrides! source-path
                                                  source-overrides)
        ai-artifact
        (ai-repl-ffi-capability-file-artifact
         r9-document-upstream-artifact-path)
        input-id (:artifact-id ai-artifact)
        diagnostic-stream (r9-document-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-r9-repl-runtime-document-artifact
         :task "P08-D120"
         :document-set ["R9"]
         :governing-document r9-document-governing-document
         :pass {:name :r9-repl-runtime-document-coverage
                :input :ai-repl-ffi-capability-runtime-artifact
                :output :r9-document-coverage-artifact
                :requires [:repl-runtime-manifest
                           :session-transcript
                           :evaluated-form-artifact
                           :syntax-object-snapshot
                           :macro-expansion-diff
                           :typed-core-snapshot
                           :mir-domain-ir-snapshot
                           :runtime-decision-log
                           :repl-capability-decision-log
                           :incremental-invalidation-record
                           :hot-reload-record]
                :preserves [:session-id :source-spans :compiler-snapshots
                            :capability-decisions :audit-log
                            :invalidation-keys]
                :emits [:requirements-coverage
                        :rejected-design-coverage
                        :conformance-criteria-record
                        :r9-diagnostic-stream]
                :rejects r9-document-diagnostic-ids}
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
         :upstream-artifact-source r9-document-upstream-artifact-path
         :requirements-coverage
         (r9-document-requirements-coverage ai-artifact)
         :rejected-design-coverage
         [{:design :repl_eval_bypasses_compiler_safety_checks
           :diagnostic "R9-CHECKS" :status :rejected}
          {:design :interactive_runtime_in_no_runtime_firmware_kernel_hardware
           :diagnostic "R9-PROFILE" :status :rejected}
          {:design :session_state_silently_affects_release_builds
           :diagnostic "R9-HERMETICITY" :status :rejected}
          {:design :hot_reload_keeps_stale_mir_or_backend_artifacts
           :diagnostic "R9-HOT-RELOAD" :status :rejected}
          {:design :debugger_access_bypasses_capability_secret_policy
           :diagnostic "R9-DEBUG" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/r9-repl-runtime-conformance-record
          :session_manifests_and_transcripts :complete
          :evaluated_forms_use_normal_compiler_pipeline :complete
          :macro_core_mir_inspection_artifacts :complete
          :capability_checks_for_interactive_io_debug :complete
          :dynamic_eval_rejection_for_incompatible_profiles :complete
          :hermetic_non_hermetic_session_records :complete
          :hot_reload_invalidation_fixtures :complete
          :interactive_diagnostic_provenance_preserved :complete
          :status :passed}
         :r9-diagnostic-stream diagnostic-stream
         :r9-document-results
         {:documents ["R9"]
          :task "P08-D120"
          :required-diagnostic-ids r9-document-diagnostic-ids
          :interactive-runtime-input-status :complete
          :manifest-status :complete
          :session-status :complete
          :evaluated-form-status :complete
          :snapshot-status :complete
          :capability-status :complete
          :invalidation-status :complete
          :hot-reload-status :complete
          :audit-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (r9-document-validate! source-path artifact-base)
        capability-proof (r9-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn r9-document-file-artifact
  [path]
  (r9-document-source-artifact path (slurp path)))

(def r10-document-governing-document
  "docs/phase-08-runtime-architecture/121-r10-ffi-runtime-design.md")

(def r10-document-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity")

(def r10-document-diagnostic-ids
  ["R10-BINDING" "R10-ABI" "R10-WRAPPER" "R10-POINTER" "R10-NULL"
   "R10-EFFECT" "R10-CAPABILITY" "R10-CALLBACK" "R10-DYNAMIC"
   "R10-MANIFEST"])

(def r10-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             r10-document-diagnostic-ids)))

(defn r10-document-source-overrides
  [module]
  (or (get-in module [:metadata :runtime :r10-document])
      (get-in module [:metadata :runtime :ai-repl-ffi])
      {}))

(defn r10-document-missing-policy
  [id]
  (case id
    "R10-BINDING" :complete-ffi-binding-manifest
    "R10-ABI" :abi_calling_convention_layout_symbol_evidence
    "R10-WRAPPER" :safe_wrapper_preconditions_checks_ensures
    "R10-POINTER" :foreign_pointer_handle_lifetime_ownership_policy
    "R10-NULL" :checked_foreign_nullability
    "R10-EFFECT" :foreign_effect_declaration
    "R10-CAPABILITY" :foreign_action_runtime_authority
    "R10-CALLBACK" :callback_thread_taint_error_capability_adapter
    "R10-DYNAMIC" :package_deployment_dynamic_loading_policy
    :complete-ffi-runtime-artifact))

(defn r10-document-fail!
  [id source-path subject extra]
  (fail! id
         "R10 FFI runtime document coverage failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :r10-ffi-runtime-document
                 :stage :r10-document-coverage
                 :document-id "R10"
                 :profile (or (:profile subject) :hosted)
                 :target (or (:target subject) :jvm)
                 :runtime-family :ffi
                 :binding-id (:binding-id subject)
                 :foreign-symbol (:foreign-symbol subject)
                 :wrapper-id (:wrapper-id subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :artifact-id (:artifact-id subject)
                 :missing-policy (r10-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-D121 requires complete FFI binding, ABI/layout, wrapper, pointer/handle, nullability, effect, capability, callback, dynamic loading, unsafe audit, and R10 conformance evidence."}
                extra)))

(defn r10-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get r10-document-override-diagnostics fail-kind)]
      (r10-document-fail!
       id source-path
       {:binding-id (str "binding-" (name fail-kind))
        :foreign-symbol (symbol "foreign" (name fail-kind))
        :wrapper-id (str "wrapper-" (name fail-kind))
        :effect fail-kind
        :capability fail-kind
        :artifact-id (str "r10-document-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))

(defn r10-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/r10-ffi-runtime-diagnostic-stream
   :stage :r10-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :r10-document-coverage
            :document-id "R10"
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-r10-document-syntax-" index)
                      :artifact input-id}
            :profile :hosted
            :target :jvm
            :runtime-family :ffi
            :binding-id "ffi/libc-open"
            :foreign-symbol 'libc/open
            :wrapper-id "wrapper/open"
            :effect (case id
                      "R10-EFFECT" :ffi/call
                      "R10-CAPABILITY" :memory/raw
                      nil)
            :capability (when (= "R10-CAPABILITY" id) :memory/raw)
            :missing-policy (r10-document-missing-policy id)
            :source-generated-origin-chain
            [:ai-repl-ffi-capability-runtime :r10-document-coverage]
            :facts {:foreign-apis-unsafe-by-default true
                    :safe-wrappers-required true
                    :foreign-effects-capability-checked true
                    :callbacks-adapted-through-runtime true}
            :remediation [{:kind :declare-binding}
                          {:kind :validate-abi-layout}
                          {:kind :attach-safe-wrapper-contract}
                          {:kind :record-callback-and-handle-policy}]
            :redactions []
            :ordering-key [id :r10-document-coverage]})
         r10-document-diagnostic-ids
         (range))
   :status :complete})