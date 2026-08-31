

(defn r1-document-capability-proof
  [artifact]
  (let [coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r1-diagnostic-stream
                                       :diagnostics])))]
    {:runtime-selection-input-verified?
     (= :complete (get-in artifact
                          [:runtime-selection-artifact
                           :capability-based-proof :status]))
     :family-selection-covered?
     (= :complete (:runtime-selection-status coverage))
     :service-classification-covered?
     (= #{:linked :generated :delegated :external :forbidden}
        (:service-classification-kinds coverage))
     :capability-enforcement-covered?
     (= :complete (:capability-enforcement-status coverage))
     :hidden-runtime-rejection-covered?
     (empty? (:hidden-runtime-services coverage))
     :startup-and-failure-covered?
     (and (= :complete (:startup-status coverage))
          (= :complete (:failure-status coverage)))
     :replay-and-audit-covered?
     (= :complete (:replay-record-status coverage))
     :downstream-consumption-covered?
     (= :complete (:downstream-consumption-status coverage))
     :diagnostics-covered?
     (= (set r1-document-diagnostic-ids) diagnostics)
     :status :complete}))

(defn r1-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (r1-document-source-overrides module)
        _ (r1-document-validate-source-overrides! source-path
                                                  source-overrides)
        runtime-artifact
        (runtime-selection-file-artifact r1-document-upstream-artifact-path)
        input-id (:artifact-id runtime-artifact)
        diagnostic-stream (r1-document-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-r1-runtime-architecture-document-artifact
         :task "P08-D112"
         :document-set ["R1"]
         :governing-document r1-document-governing-document
         :pass {:name :r1-runtime-architecture-document-coverage
                :input :runtime-selection-artifact
                :output :r1-document-coverage-artifact
                :requires [:runtime-family-selection-record
                           :runtime-service-table
                           :runtime-capability-enforcement-table
                           :startup-failure-records
                           :runtime-backend-consumption-record]
                :preserves [:profile :target :effects :capabilities
                            :safety :source-spans :artifact-provenance
                            :generated-origin-chain]
                :emits [:requirements-coverage
                        :rejected-design-coverage
                        :conformance-criteria-record
                        :r1-diagnostic-stream]
                :rejects r1-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :runtime-selection-artifact
         (select-keys runtime-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :runtime-selection-results
                       :runtime-family-selection-record
                       :runtime-service-table
                       :runtime-capability-enforcement-table
                       :runtime-backend-consumption-record])
         :runtime-selection-artifact-kind (:kind runtime-artifact)
         :runtime-selection-artifact-hash input-id
         :upstream-artifact-source r1-document-upstream-artifact-path
         :requirements-coverage
         (r1-document-requirements-coverage runtime-artifact)
         :rejected-design-coverage
         [{:design :universal-hidden-runtime
           :diagnostic "R1-FORBIDDEN" :status :rejected}
          {:design :backend-artifact-assumes-unmanifested-service
           :diagnostic "R1-SERVICE" :status :rejected}
          {:design :runtime_api_bypasses_effect_and_capability_checks
           :diagnostic "R1-CAPABILITY" :status :rejected}
          {:design :host-delegation-without-typed-adapter
           :diagnostic "R1-HOST" :status :rejected}
          {:design :replay-sensitive-runtime-reexecutes-nondeterminism
           :diagnostic "R1-REPLAY" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/r1-runtime-architecture-conformance-record
          :runtime-family-selection-for_profiles_and_backends :complete
          :service-classification-records :complete
          :capability-enforcement-tables :complete
          :startup-and-failure-models :complete
          :hidden-runtime-dependency-rejection :covered
          :replay-records-for-nondeterminism :complete
          :backend-package-observability-conformance-consumption :complete
          :status :passed}
         :r1-diagnostic-stream diagnostic-stream
         :r1-document-results
         {:documents ["R1"]
          :task "P08-D112"
          :required-diagnostic-ids r1-document-diagnostic-ids
          :runtime-selection-input-status :complete
          :family-selection-status :complete
          :service-classification-status :complete
          :capability-enforcement-status :complete
          :startup-failure-status :complete
          :hidden-runtime-rejection-status :complete
          :replay-audit-status :complete
          :downstream-consumption-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (r1-document-validate! source-path artifact-base)
        capability-proof (r1-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn r1-document-file-artifact
  [path]
  (r1-document-source-artifact path (slurp path)))

(def r2-document-governing-document
  "docs/phase-08-runtime-architecture/113-r2-no-runtime-execution-model.md")

(def r2-document-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity")

(def r2-document-diagnostic-ids
  ["R2-HIDDEN-SERVICE"
   "R2-STARTUP"
   "R2-MEMORY"
   "R2-DISPATCH"
   "R2-FAILURE"
   "R2-CAPABILITY"
   "R2-PROOF"
   "R2-MANIFEST"])

(def r2-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             r2-document-diagnostic-ids)))

(defn r2-document-source-overrides
  [module]
  (or (get-in module [:metadata :runtime :r2-document])
      (get-in module [:metadata :runtime :selection])
      {}))

(defn r2-document-missing-policy
  [id]
  (case id
    "R2-HIDDEN-SERVICE" :no_hidden_allocator_gc_scheduler_reflection_eval_host
    "R2-STARTUP" :explicit_reset_entry_section_initialization_records
    "R2-MEMORY" :memory_map_stack_bounds_static_allocation_no_heap
    "R2-DISPATCH" :dynamic_dispatch_lowered_or_rejected
    "R2-FAILURE" :explicit_panic_trap_result_reset_signal_policy
    "R2-CAPABILITY" :target_authority_mmio_interrupt_capability_record
    "R2-PROOF" :boundedness_initialization_check_elision_evidence
    :complete_no_runtime_manifest))

(defn r2-document-fail!
  [id source-path subject extra]
  (fail! id
         "R2 no-runtime document coverage failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :r2-no-runtime-document
                 :stage :r2-document-coverage
                 :document-id "R2"
                 :profile (or (:profile subject) :firmware)
                 :target (or (:target subject)
                             {:backend :c :platform :bare-metal})
                 :runtime-family :no-runtime
                 :service-id (:service-id subject)
                 :memory-region (:memory-region subject)
                 :missing-proof (:missing-proof subject)
                 :capability (:capability subject)
                 :artifact-id (:artifact-id subject)
                 :missing-policy (r2-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-D113 requires :runtime :none manifests with explicit startup/reset, section layout, memory map, stack bounds, static allocation, forbidden-service reports, failure policy, target authority capability records, generated support provenance, and proof evidence."}
                extra)))

(defn r2-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get r2-document-override-diagnostics fail-kind)]
      (r2-document-fail!
       id source-path
       {:service-id fail-kind
        :memory-region fail-kind
        :missing-proof fail-kind
        :capability fail-kind
        :artifact-id (str "r2-document-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))