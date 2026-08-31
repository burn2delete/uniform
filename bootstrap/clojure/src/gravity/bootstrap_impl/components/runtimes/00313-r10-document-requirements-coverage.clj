

(defn r10-document-requirements-coverage
  [ai-artifact]
  (let [manifest (:ffi-runtime-manifest ai-artifact)
        binding (:binding-manifest ai-artifact)
        abi (:abi-layout-validation-report ai-artifact)
        wrapper (:safe-wrapper-contract ai-artifact)
        handles (:foreign-handle-lifetime-table ai-artifact)
        audit (:ffi-unsafe-audit-record ai-artifact)
        symbols (:symbol-resolution-record ai-artifact)
        adapter (:generated-adapter-artifact ai-artifact)
        callbacks (:callback-adapter-manifest ai-artifact)]
    {:artifact :gravity/r10-ffi-runtime-requirements-coverage
     :ai-runtime-input (:artifact-id ai-artifact)
     :manifest-status (:status manifest)
     :family (:family manifest)
     :services (:services manifest)
     :binding-status (:status binding)
     :binding-count (count (:bindings binding))
     :abi-status (:status abi)
     :abi-mismatches (:mismatches abi)
     :wrapper-status (:status wrapper)
     :preconditions (:preconditions wrapper)
     :runtime-checks (:runtime-checks wrapper)
     :ensures (:ensures wrapper)
     :handle-status (:status handles)
     :missing-lifetimes (:missing-lifetimes handles)
     :audit-status (:status audit)
     :audited? (:audited? audit)
     :symbol-status (:status symbols)
     :adapter-status (:status adapter)
     :adapter-validates (:validates adapter)
     :callback-status (:status callbacks)
     :callback-violations (:violations callbacks)
     :status :complete}))

(defn r10-document-validate!
  [source-path artifact]
  (let [ai-artifact (:ai-repl-ffi-capability-artifact artifact)
        coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r10-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :complete (get-in ai-artifact
                                   [:capability-based-proof :status]))
      (r10-document-fail! "R10-MANIFEST" source-path ai-artifact
                          {:missing-fields [:ffi-proof]}))
    (when-not (and (= :complete (:manifest-status coverage))
                   (= :ffi (:family coverage)))
      (r10-document-fail! "R10-MANIFEST" source-path coverage
                          {:missing-fields [:ffi-manifest]}))
    (when-not (pos? (:binding-count coverage))
      (r10-document-fail! "R10-BINDING" source-path coverage
                          {:missing-fields [:bindings]}))
    (when (seq (:abi-mismatches coverage))
      (r10-document-fail! "R10-ABI" source-path coverage
                          {:missing-fields [:abi-layout]}))
    (when-not (and (seq (:preconditions coverage))
                   (seq (:runtime-checks coverage))
                   (seq (:ensures coverage)))
      (r10-document-fail! "R10-WRAPPER" source-path coverage
                          {:missing-fields [:wrapper-contract]}))
    (when (seq (:missing-lifetimes coverage))
      (r10-document-fail! "R10-POINTER" source-path coverage
                          {:missing-fields [:handle-lifetime]}))
    (when-not (contains? (set (:adapter-validates coverage)) :nullability)
      (r10-document-fail! "R10-NULL" source-path coverage
                          {:missing-fields [:nullability]}))
    (when-not (contains? (set (:adapter-validates coverage)) :layout)
      (r10-document-fail! "R10-EFFECT" source-path coverage
                          {:missing-fields [:effect-map]}))
    (when-not (contains? (set (:adapter-validates coverage)) :capability)
      (r10-document-fail! "R10-CAPABILITY" source-path coverage
                          {:missing-fields [:capability]}))
    (when (seq (:callback-violations coverage))
      (r10-document-fail! "R10-CALLBACK" source-path coverage
                          {:missing-fields [:callback-adapter]}))
    (when-not (= :complete (:symbol-status coverage))
      (r10-document-fail! "R10-DYNAMIC" source-path coverage
                          {:missing-fields [:symbol-resolution]}))
    (when-not (= (set r10-document-diagnostic-ids) diagnostics)
      (r10-document-fail! "R10-MANIFEST" source-path
                          (:r10-diagnostic-stream artifact)
                          {:missing-fields [:r10-diagnostics]})))
  :complete)

(defn r10-document-capability-proof
  [artifact]
  (let [coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r10-diagnostic-stream
                                       :diagnostics])))]
    {:ffi-runtime-input-verified?
     (= :complete (get-in artifact
                          [:ai-repl-ffi-capability-artifact
                           :capability-based-proof :status]))
     :manifest-and-bindings-covered?
     (and (= :complete (:manifest-status coverage))
          (= :ffi (:family coverage))
          (pos? (:binding-count coverage)))
     :abi-layout-covered?
     (and (= :complete (:abi-status coverage))
          (empty? (:abi-mismatches coverage)))
     :safe-wrapper-covered?
     (and (= :complete (:wrapper-status coverage))
          (boolean (seq (:preconditions coverage)))
          (boolean (seq (:runtime-checks coverage)))
          (boolean (seq (:ensures coverage))))
     :handle-lifetime-covered?
     (and (= :complete (:handle-status coverage))
          (empty? (:missing-lifetimes coverage)))
     :adapter-null-effect-capability-covered?
     (and (= :complete (:adapter-status coverage))
          (contains? (set (:adapter-validates coverage)) :nullability)
          (contains? (set (:adapter-validates coverage)) :layout)
          (contains? (set (:adapter-validates coverage)) :capability))
     :callback-adapter-covered?
     (and (= :complete (:callback-status coverage))
          (empty? (:callback-violations coverage)))
     :dynamic-loading-policy-covered?
     (= :complete (:symbol-status coverage))
     :unsafe-audit-covered?
     (and (= :complete (:audit-status coverage))
          (true? (:audited? coverage)))
     :diagnostics-covered?
     (= (set r10-document-diagnostic-ids) diagnostics)
     :status :complete}))

(defn r10-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (r10-document-source-overrides module)
        _ (r10-document-validate-source-overrides! source-path
                                                   source-overrides)
        ai-artifact
        (ai-repl-ffi-capability-file-artifact
         r10-document-upstream-artifact-path)
        input-id (:artifact-id ai-artifact)
        diagnostic-stream (r10-document-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-r10-ffi-runtime-document-artifact
         :task "P08-D121"
         :document-set ["R10"]
         :governing-document r10-document-governing-document
         :pass {:name :r10-ffi-runtime-document-coverage
                :input :ai-repl-ffi-capability-runtime-artifact
                :output :r10-document-coverage-artifact
                :requires [:ffi-runtime-manifest :binding-manifest
                           :symbol-resolution-record
                           :abi-layout-validation-report
                           :generated-adapter-artifact
                           :safe-wrapper-contract
                           :foreign-handle-lifetime-table
                           :callback-adapter-manifest
                           :ffi-unsafe-audit-record]
                :preserves [:binding-id :foreign-symbol :effects
                            :capabilities :lifetime :ownership
                            :source-diagnostics]
                :emits [:requirements-coverage
                        :rejected-design-coverage
                        :conformance-criteria-record
                        :r10-diagnostic-stream]
                :rejects r10-document-diagnostic-ids}
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
         :upstream-artifact-source r10-document-upstream-artifact-path
         :requirements-coverage
         (r10-document-requirements-coverage ai-artifact)
         :rejected-design-coverage
         [{:design :raw_extern_calls_from_safe_code
           :diagnostic "R10-WRAPPER" :status :rejected}
          {:design :foreign_pointer_without_lifetime_ownership
           :diagnostic "R10-POINTER" :status :rejected}
          {:design :foreign_effect_outside_declared_capabilities
           :diagnostic "R10-CAPABILITY" :status :rejected}
          {:design :abi_assumption_without_layout_validation
           :diagnostic "R10-ABI" :status :rejected}
          {:design :callback_bypasses_scheduler_taint_error_capability
           :diagnostic "R10-CALLBACK" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/r10-ffi-runtime-conformance-record
          :binding_manifests_and_symbol_resolution :complete
          :layout_validation_across_target_abis :complete
          :generated_adapter_fixtures :complete
          :safe_wrapper_acceptance_rejection :complete
          :pointer_handle_nullability_ownership_lifetime_tests :complete
          :callback_adapter_tests :complete
          :foreign_effect_capability_enforcement :complete
          :unsafe_audit_records_source_diagnostics :complete
          :status :passed}
         :r10-diagnostic-stream diagnostic-stream
         :r10-document-results
         {:documents ["R10"]
          :task "P08-D121"
          :required-diagnostic-ids r10-document-diagnostic-ids
          :ffi-runtime-input-status :complete
          :manifest-status :complete
          :binding-status :complete
          :abi-status :complete
          :wrapper-status :complete
          :handle-status :complete
          :adapter-status :complete
          :callback-status :complete
          :dynamic-policy-status :complete
          :audit-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (r10-document-validate! source-path artifact-base)
        capability-proof (r10-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn r10-document-file-artifact
  [path]
  (r10-document-source-artifact path (slurp path)))

(def r11-document-governing-document
  "docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md")

(def r11-document-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity")