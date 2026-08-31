

(defn runtime-observability-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (runtime-observability-source-overrides module)
        _ (runtime-observability-validate-source-overrides!
           source-path source-overrides)
        upstream-artifact
        (ai-repl-ffi-capability-file-artifact
         runtime-observability-upstream-artifact-path)
        input-id (:artifact-id upstream-artifact)
        schemas (runtime-observability-schemas source-path input-id)
        diagnostic-stream
        (runtime-observability-diagnostic-stream source-path input-id)
        artifact-base
        (merge
         {:kind :gravity/stage0-runtime-observability-artifact
          :task "P08-T06"
          :document-set ["R12"]
          :governing-documents runtime-observability-governing-documents
          :pass {:name :runtime-observability
                 :input :ai-repl-ffi-capability-runtime-artifact
                 :output :runtime-observability-artifact
                 :requires [:runtime-manifests :artifact-manifest
                            :source-map :redaction-policy
                            :sink-capability]
                 :preserves [:source-spans :generated-origin
                             :artifact-provenance :effects :capabilities
                             :replay-records :safety-outcomes :semantics]
                 :emits [:runtime-observability-manifest
                         :event-schema-registry
                         :structured-log-schema :trace-schema
                         :metric-schema :panic-trap-report-schema
                         :safety-check-failure-report
                         :capability-decision-report
                         :replay-trace-schema :redaction-policy-record
                         :diagnostic-bundle :sampling-policy-record
                         :runtime-observability-diagnostic-stream]
                 :rejects runtime-observability-diagnostic-ids}
          :source-overrides source-overrides
          :module (select-keys module
                               [:module :source-path :profile :target
                                :effects :capabilities :safety :metadata])
          :ai-repl-ffi-capability-artifact
          (select-keys upstream-artifact
                       [:kind :task :artifact-id :capability-based-proof
                        :ai-repl-ffi-capability-results])
          :ai-repl-ffi-capability-artifact-kind (:kind upstream-artifact)
          :ai-repl-ffi-capability-artifact-hash input-id
          :upstream-artifact-source runtime-observability-upstream-artifact-path
          :runtime-observability-manifest
          (runtime-observability-manifest input-id)
          :event-schema-registry
          (observability-event-schema-registry input-id)
          :rejected-design-coverage
          (mapv (fn [id]
                  {:design (keyword (str/lower-case id))
                   :diagnostic id
                   :status :rejected})
                runtime-observability-diagnostic-ids)
          :conformance-criteria-record
          {:artifact :gravity/runtime-observability-conformance-record
           :event-schemas :complete
           :structured-logs-traces-metrics :complete
           :panic-safety-capability-replay-reports :complete
           :sink-capability-enforcement :complete
           :secret-taint-raw-memory-redaction :complete
           :source-provenance-artifact-links :complete
           :sampling-preserves-audit-events :complete
           :diagnostic-bundle :complete
           :status :passed}
          :runtime-observability-diagnostic-stream diagnostic-stream
          :runtime-observability-results
          {:documents ["R12"]
           :task "P08-T06"
           :required-diagnostic-ids runtime-observability-diagnostic-ids
           :ai-repl-ffi-capability-input-status :complete
           :manifest-status :complete
           :schema-status :complete
           :log-trace-metric-status :complete
           :panic-safety-capability-status :complete
           :replay-trace-status :complete
           :redaction-status :complete
           :sampling-status :complete
           :bundle-status :complete
           :diagnostic-status :complete
           :status :complete}
          :diagnostics []}
         schemas)
        _ (runtime-observability-validate! source-path artifact-base)
        capability-proof (runtime-observability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn runtime-observability-file-artifact
  [path]
  (runtime-observability-source-artifact path (slurp path)))

(def r12-document-governing-document
  "docs/phase-08-runtime-architecture/123-r12-runtime-observability-and-diagnostics-design.md")

(def r12-document-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-observability.gravity")

(def r12-document-diagnostic-ids
  runtime-observability-diagnostic-ids)

(def r12-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             r12-document-diagnostic-ids)))

(defn r12-document-source-overrides
  [module]
  (or (get-in module [:metadata :runtime :r12-document])
      (get-in module [:metadata :runtime :observability])
      {}))

(defn r12-document-missing-policy
  [id]
  (runtime-observability-missing-policy id))

(defn r12-document-fail!
  [id source-path subject extra]
  (fail! id
         "R12 runtime observability document coverage failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :r12-runtime-observability-document
                 :stage :r12-document-coverage
                 :document-id "R12"
                 :profile (or (:profile subject) :ai)
                 :target (or (:target subject) :jvm)
                 :runtime-family :observability
                 :event-id (:event-id subject)
                 :artifact-id (:artifact-id subject)
                 :sink (:sink subject)
                 :redaction-policy (:redaction-policy subject)
                 :capability (:capability subject)
                 :missing-schema (:missing-schema subject)
                 :missing-link (:missing-link subject)
                 :missing-policy (r12-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-D123 requires runtime event schemas, structured logs/traces/metrics, panic/safety/capability/replay reports, redaction policy, diagnostic bundles, sink capability checks, source/provenance links, sampling preservation, and R12 conformance evidence."}
                extra)))

(defn r12-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get r12-document-override-diagnostics fail-kind)]
      (r12-document-fail!
       id source-path
       {:event-id (str "event-" (name fail-kind))
        :artifact-id (str "r12-document-" (name fail-kind))
        :sink fail-kind
        :redaction-policy fail-kind
        :capability fail-kind
        :missing-schema fail-kind
        :missing-link fail-kind}
       {:missing-fields [fail-kind]}))))

(defn r12-document-diagnostic-stream
  [source-path input-id]
  (let [stream (runtime-observability-diagnostic-stream source-path input-id)]
    (assoc stream
           :artifact :gravity/r12-runtime-observability-diagnostic-stream
           :stage :r12-document-coverage
           :diagnostics
           (mapv (fn [diagnostic]
                   (assoc diagnostic
                          :stage :r12-document-coverage
                          :document-id "R12"
                          :source-generated-origin-chain
                          [:runtime-observability :r12-document-coverage]
                          :ordering-key [(:diagnostic diagnostic)
                                         :r12-document-coverage]))
                 (:diagnostics stream)))))

(defn r12-document-requirements-coverage
  [observability-artifact]
  (let [manifest (:runtime-observability-manifest observability-artifact)
        registry (:event-schema-registry observability-artifact)
        structured-log (:structured-log-schema observability-artifact)
        trace (:trace-schema observability-artifact)
        metric (:metric-schema observability-artifact)
        panic (:panic-trap-report-schema observability-artifact)
        safety (:safety-check-failure-report observability-artifact)
        capability (:capability-decision-report observability-artifact)
        replay (:replay-trace-schema observability-artifact)
        redaction (:redaction-policy-record observability-artifact)
        bundle (:diagnostic-bundle observability-artifact)
        sampling (:sampling-policy-record observability-artifact)]
    {:artifact :gravity/r12-runtime-observability-requirements-coverage
     :runtime-observability-input (:artifact-id observability-artifact)
     :manifest-status (:status manifest)
     :events (:events manifest)
     :requires (:requires manifest)
     :emits (:emits manifest)
     :rejects (:rejects manifest)
     :development-only? (:development-only? manifest)
     :schema-status (:status registry)
     :missing-schemas (:missing-schemas registry)
     :all-schemas-stable? (every? :stable-id? (:schemas registry))
     :structured-log-status (:status structured-log)
     :structured-log-fields (:fields structured-log)
     :trace-status (:status trace)
     :trace-span-count (count (:spans trace))
     :trace-source-linked?
     (every? #(and (:source-span %) (:artifact-id %)) (:spans trace))
     :metric-status (:status metric)
     :metric-count (count (:metrics metric))
     :panic-status (:status panic)
     :safety-status (:status safety)
     :capability-status (:status capability)
     :mandatory-audit-event? (:mandatory-audit-event? capability)
     :replay-status (:status replay)
     :missing-event-log-links (:missing-event-log-links replay)
     :redaction-status (:status redaction)
     :secret-leaks (:secret-leaks redaction)
     :bundle-status (:status bundle)
     :bundle-contains (:contains bundle)
     :portable? (:portable? bundle)
     :policy-constrained? (:policy-constrained? bundle)
     :sampling-status (:status sampling)
     :required-audit-events-preserved?
     (:required-audit-events-preserved? sampling)
     :dropped-required-audit-events
     (:dropped-required-audit-events sampling)
     :status :complete}))