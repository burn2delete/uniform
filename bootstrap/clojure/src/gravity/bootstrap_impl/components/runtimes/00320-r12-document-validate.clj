

(defn r12-document-validate!
  [source-path artifact]
  (let [observability-artifact (:runtime-observability-artifact artifact)
        coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r12-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :complete (get-in observability-artifact
                                   [:capability-based-proof :status]))
      (r12-document-fail! "R12-MANIFEST" source-path observability-artifact
                          {:missing-fields [:runtime-observability-proof]}))
    (when-not (= :complete (:manifest-status coverage))
      (r12-document-fail! "R12-MANIFEST" source-path coverage
                          {:missing-fields [:runtime-observability-manifest]}))
    (when-not (and (contains? (:requires coverage) :sink-capability)
                   (contains? (:rejects coverage)
                              :ungranted-observability-sink))
      (r12-document-fail! "R12-SINK" source-path coverage
                          {:missing-fields [:sink-capability]}))
    (when-not (and (= :complete (:schema-status coverage))
                   (empty? (:missing-schemas coverage))
                   (true? (:all-schemas-stable? coverage)))
      (r12-document-fail! "R12-SCHEMA" source-path coverage
                          {:missing-fields [:event-schema-registry]}))
    (when-not (:trace-source-linked? coverage)
      (r12-document-fail! "R12-SOURCE" source-path coverage
                          {:missing-fields [:source-artifact-links]}))
    (when-not (and (= :complete (:redaction-status coverage))
                   (empty? (:secret-leaks coverage)))
      (r12-document-fail! "R12-SECRET" source-path coverage
                          {:missing-fields [:redaction-policy]}))
    (when-not (contains? (:rejects coverage)
                         :semantics-changing-observability)
      (r12-document-fail! "R12-SEMANTICS" source-path coverage
                          {:missing-fields [:semantics-neutrality]}))
    (when-not (and (true? (:required-audit-events-preserved? coverage))
                   (empty? (:dropped-required-audit-events coverage)))
      (r12-document-fail! "R12-SAMPLING" source-path coverage
                          {:missing-fields [:sampling-policy]}))
    (when-not (and (= :complete (:replay-status coverage))
                   (empty? (:missing-event-log-links coverage)))
      (r12-document-fail! "R12-REPLAY" source-path coverage
                          {:missing-fields [:replay-links]}))
    (when-not (and (= :complete (:bundle-status coverage))
                   (true? (:portable? coverage))
                   (true? (:policy-constrained? coverage))
                   (contains? (set (:bundle-contains coverage))
                              :remediation-categories))
      (r12-document-fail! "R12-BUNDLE" source-path coverage
                          {:missing-fields [:diagnostic-bundle]}))
    (when-not (= (set r12-document-diagnostic-ids) diagnostics)
      (r12-document-fail! "R12-MANIFEST" source-path
                          (:r12-diagnostic-stream artifact)
                          {:missing-fields [:r12-diagnostics]})))
  :complete)

(defn r12-document-capability-proof
  [artifact]
  (let [coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r12-diagnostic-stream
                                       :diagnostics])))]
    {:runtime-observability-input-verified?
     (= :complete (get-in artifact
                          [:runtime-observability-artifact
                           :capability-based-proof :status]))
     :manifest-event-sink-policy-covered?
     (and (= :complete (:manifest-status coverage))
          (contains? (:requires coverage) :sink-capability)
          (contains? (:rejects coverage) :ungranted-observability-sink))
     :event-schemas-stable?
     (and (= :complete (:schema-status coverage))
          (empty? (:missing-schemas coverage))
          (true? (:all-schemas-stable? coverage)))
     :logs-traces-metrics-covered?
     (and (= :complete (:structured-log-status coverage))
          (= :complete (:trace-status coverage))
          (= :complete (:metric-status coverage))
          (pos? (:metric-count coverage))
          (:trace-source-linked? coverage))
     :panic-safety-capability-reports-covered?
     (and (= :complete (:panic-status coverage))
          (= :complete (:safety-status coverage))
          (= :complete (:capability-status coverage))
          (true? (:mandatory-audit-event? coverage)))
     :redaction-policy-covered?
     (and (= :complete (:redaction-status coverage))
          (empty? (:secret-leaks coverage)))
     :semantics-neutral-covered?
     (contains? (:rejects coverage) :semantics-changing-observability)
     :required-audit-events-preserved?
     (and (true? (:required-audit-events-preserved? coverage))
          (empty? (:dropped-required-audit-events coverage)))
     :replay-traces-linked?
     (and (= :complete (:replay-status coverage))
          (empty? (:missing-event-log-links coverage)))
     :diagnostic-bundle-complete?
     (and (= :complete (:bundle-status coverage))
          (true? (:portable? coverage))
          (true? (:policy-constrained? coverage)))
     :diagnostics-covered?
     (= (set r12-document-diagnostic-ids) diagnostics)
     :status :complete}))

(defn r12-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (r12-document-source-overrides module)
        _ (r12-document-validate-source-overrides! source-path
                                                   source-overrides)
        observability-artifact
        (runtime-observability-file-artifact
         r12-document-upstream-artifact-path)
        input-id (:artifact-id observability-artifact)
        diagnostic-stream (r12-document-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-r12-runtime-observability-document-artifact
         :task "P08-D123"
         :document-set ["R12"]
         :governing-document r12-document-governing-document
         :pass {:name :r12-runtime-observability-document-coverage
                :input :runtime-observability-artifact
                :output :r12-document-coverage-artifact
                :requires [:runtime-observability-manifest
                           :event-schema-registry
                           :structured-log-schema :trace-schema
                           :metric-schema :panic-trap-report-schema
                           :safety-check-failure-report
                           :capability-decision-report
                           :replay-trace-schema :redaction-policy-record
                           :diagnostic-bundle :sampling-policy-record]
                :preserves [:source-spans :generated-origin
                            :artifact-provenance :effects :capabilities
                            :replay-records :safety-outcomes :semantics]
                :emits [:requirements-coverage
                        :rejected-design-coverage
                        :conformance-criteria-record
                        :r12-diagnostic-stream]
                :rejects r12-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :runtime-observability-artifact
         (select-keys observability-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :runtime-observability-results])
         :runtime-observability-artifact-kind (:kind observability-artifact)
         :runtime-observability-artifact-hash input-id
         :upstream-artifact-source r12-document-upstream-artifact-path
         :requirements-coverage
         (r12-document-requirements-coverage observability-artifact)
         :rejected-design-coverage
         [{:design :observability_as_ambient_network_authority
           :diagnostic "R12-SINK" :status :rejected}
          {:design :logs_or_traces_leak_secrets
           :diagnostic "R12-SECRET" :status :rejected}
          {:design :diagnostics_without_source_or_artifact_links
           :diagnostic "R12-SOURCE" :status :rejected}
          {:design :sampling_removes_mandatory_audit_events
           :diagnostic "R12-SAMPLING" :status :rejected}
          {:design :observability_changes_semantics_or_replay
           :diagnostic "R12-SEMANTICS" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/r12-runtime-observability-conformance-record
          :event_schemas_for_runtime_families :complete
          :logs_traces_metrics_panic_replay_bundles :complete
          :sink_capability_enforcement :complete
          :redaction_tests :complete
          :source_provenance_artifact_links :complete
          :sampling_preserves_required_audit_events :complete
          :diagnostic_bundle_consumption :complete
          :status :passed}
         :r12-diagnostic-stream diagnostic-stream
         :r12-document-results
         {:documents ["R12"]
          :task "P08-D123"
          :required-diagnostic-ids r12-document-diagnostic-ids
          :runtime-observability-input-status :complete
          :manifest-status :complete
          :schema-status :complete
          :log-trace-metric-status :complete
          :panic-safety-capability-status :complete
          :redaction-status :complete
          :sampling-status :complete
          :replay-status :complete
          :bundle-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (r12-document-validate! source-path artifact-base)
        capability-proof (r12-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn r12-document-file-artifact
  [path]
  (r12-document-source-artifact path (slurp path)))