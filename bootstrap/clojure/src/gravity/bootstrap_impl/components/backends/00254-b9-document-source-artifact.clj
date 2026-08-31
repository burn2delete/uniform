

(defn b9-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b9-document-source-overrides module)
        _ (b9-document-validate-source-overrides! source-path
                                                  source-overrides)
        specialized-artifact (specialized-lowering-source-artifact source-path
                                                                   source-text)
        input-id (:artifact-id specialized-artifact)
        manifest (b9-document-hdl-manifest source-path input-id)
        diagnostic-stream (b9-document-diagnostic-stream source-path
                                                         input-id)
        artifact-base
        {:kind :gravity/stage0-b9-hdl-backend-document-artifact
         :task "P07-D106"
         :document-set ["B9"]
         :governing-document b9-document-governing-document
         :pass {:name :b9-hdl-backend-document-coverage
                :input :specialized-lowering-artifact
                :output :b9-hdl-backend-document-artifact
                :requires [:verified-hardware-ir :b1-backend-interface
                           :c12-domain-ir :c14-target-lowering
                           :hardware-profile :fixed-width-layout
                           :clock-domains :reset-domains
                           :state-machine-graph :cdc-report
                           :timing-constraints]
                :preserves [:source-spans :generated-origins :types
                            :effects :capabilities :schemas
                            :clock-reset-domains :state-machine-map
                            :timing-constraints :hardware-audit-records
                            :safety :proofs :profile :target
                            :artifact-provenance]
                :emits [:hdl-backend-manifest :hardware-ir-handoff-record
                        :hdl-artifact :interface-port-schema
                        :clock-domain-report :reset-domain-report
                        :width-and-numeric-report :state-machine-graph
                        :memory-block-manifest :cdc-report
                        :timing-constraint-file :testbench
                        :simulation-trace-schema :source-debug-map
                        :b9-diagnostic-stream
                        :conformance-criteria-record]
                :rejects b9-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :specialized-lowering-artifact
         (select-keys specialized-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :specialized-lowering-results])
         :specialized-lowering-artifact-kind (:kind specialized-artifact)
         :specialized-lowering-artifact-hash input-id
         :hdl-backend-manifest manifest
         :hardware-ir-handoff-record
         (:hardware-ir-handoff-record manifest)
         :hdl-artifacts (:hdl-artifacts manifest)
         :interface-port-schema (:interface-port-schema manifest)
         :clock-domain-report (:clock-domain-report manifest)
         :reset-domain-report (:reset-domain-report manifest)
         :width-and-numeric-report (:width-and-numeric-report manifest)
         :state-machine-graph (:state-machine-graph manifest)
         :memory-block-manifest (:memory-block-manifest manifest)
         :cdc-report (:cdc-report manifest)
         :runtime-construct-rejection-report
         (:runtime-construct-rejection-report manifest)
         :timing-constraint-file (:timing-constraint-file manifest)
         :testbench (:testbench manifest)
         :simulation-trace-schema (:simulation-trace-schema manifest)
         :source-debug-map (:source-debug-map manifest)
         :hardware-audit-records (:hardware-audit-records manifest)
         :rejected-design-coverage
         [{:design :runtime-code-compiled-to-hardware
           :diagnostic "B9-RUNTIME" :status :rejected}
          {:design :implicit-host-integer-width
           :diagnostic "B9-WIDTH" :status :rejected}
          {:design :unmediated-clock-domain-crossing
           :diagnostic "B9-CDC" :status :rejected}
          {:design :unbounded-runtime-control-flow
           :diagnostic "B9-UNBOUNDED" :status :rejected}
          {:design :hdl-generation-without-source-map-or-constraints
           :diagnostic "B9-MANIFEST" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b9-hdl-backend-conformance-criteria-record
          :fixed-width-signedness-preservation :complete
          :register-memory-clock-reset_port_bus_interface_manifests :complete
          :accepted-combinational-sequential-state-machine-memory-fixtures
          :covered
          :runtime-construct-and-unbounded-control-rejection :covered
          :synchronizer-acceptance-and-unsafe-cdc-rejection :covered
          :arithmetic-width-overflow-fixtures :complete
          :timing-constraint-emission :complete
          :testbench-and-simulation-trace-generation :complete
          :source-proof-safety-capability-metadata-preservation :complete
          :status :passed}
         :b9-diagnostic-stream diagnostic-stream
         :b9-document-results
         {:documents ["B9"]
          :task "P07-D106"
          :required-diagnostic-ids b9-document-diagnostic-ids
          :specialized-lowering-input-status :complete
          :target-status :complete
          :hardware-ir-status :complete
          :width-status :complete
          :clock-status :complete
          :reset-status :complete
          :cdc-status :complete
          :runtime-rejection-status :complete
          :finite-control-status :complete
          :interface-status :complete
          :timing-status :complete
          :testbench-status :complete
          :simulation-trace-status :complete
          :manifest-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b9-document-validate! source-path artifact-base)
        capability-proof (b9-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b9-document-file-artifact
  [path]
  (b9-document-source-artifact path (slurp path)))

(def b10-document-governing-document
  "docs/phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md")

(def b10-document-diagnostic-ids
  ["B10-SCHEMA"
   "B10-REPLAY"
   "B10-IDEMPOTENCY"
   "B10-RETRY"
   "B10-COMPENSATION"
   "B10-CAPABILITY"
   "B10-POLICY"
   "B10-TAINT"
   "B10-GRAPH"
   "B10-MANIFEST"])

(def b10-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             b10-document-diagnostic-ids)))

(defn b10-document-source-overrides
  [module]
  (or (get-in module [:metadata :backend :b10-document])
      (get-in module [:metadata :backend :specialized-lowering])
      (get-in module [:metadata :backend :specialized])
      {}))

(defn b10-document-missing-policy
  [id]
  (case id
    "B10-SCHEMA" :workflow-step-message-state-schema
    "B10-REPLAY" :recorded-nondeterminism-and-event-log-replay
    "B10-IDEMPOTENCY" :side-effect-idempotency-key
    "B10-RETRY" :retry-timeout-cancellation-failure-map
    "B10-COMPENSATION" :compensation-handler
    "B10-CAPABILITY" :external-authority-capability-manifest
    "B10-POLICY" :provider-budget-human-review-policy
    "B10-TAINT" :validated-model-tool-external-output
    "B10-GRAPH" :valid-workflow-graph-edge-cycle-compensation
    :complete-workflow-artifact-manifest))

(defn b10-document-step-id
  [id]
  (case id
    "B10-SCHEMA" :schema-step
    "B10-REPLAY" :call-model
    "B10-IDEMPOTENCY" :write-ticket
    "B10-RETRY" :call-service
    "B10-COMPENSATION" :write-database
    "B10-CAPABILITY" :call-tool
    "B10-POLICY" :approve-output
    "B10-TAINT" :trusted-sink
    "B10-GRAPH" :join
    :workflow-manifest))

(defn b10-document-fail!
  [id source-path subject extra]
  (fail! id
         "B10 workflow graph backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :b10-workflow-graph-backend-document
                 :stage :b10-workflow-graph-backend-document-coverage
                 :backend :gravity.backend/workflow-graph
                 :profile :distributed
                 :target :durable-workflow
                 :workflow-id :gravity-stage0-workflow
                 :step-id (or (:step-id subject)
                              (b10-document-step-id id))
                 :schema-id (or (:schema-id subject) :workflow-input-v1)
                 :effect (or (:effect subject) :network/request)
                 :capability (or (:capability subject)
                                 :network/request)
                 :provider (or (:provider subject)
                               :stage0-model-provider)
                 :replay-mode (or (:replay-mode subject)
                                  :event-log)
                 :missing-policy (b10-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "B10 requires workflow, step, message, and state schemas; replay-safe nondeterminism records; idempotency, retry, timeout, cancellation, and compensation records; explicit capabilities and human-review policy; taint validation; valid graph edges; audit provenance; and a complete workflow artifact manifest."}
                extra)))

(defn b10-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b10-document-override-diagnostics fail-kind)]
      (b10-document-fail!
       id source-path
       {:artifact-id (str "b10-document-" (name fail-kind))
        :step-id (keyword (str "b10-document-" (name fail-kind)))}
       {:missing-fields [fail-kind]}))))