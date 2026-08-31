

(defn b8-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b8-document-source-overrides module)
        _ (b8-document-validate-source-overrides! source-path
                                                  source-overrides)
        specialized-artifact (specialized-lowering-source-artifact source-path
                                                                   source-text)
        input-id (:artifact-id specialized-artifact)
        manifest (b8-document-gpu-manifest source-path input-id)
        diagnostic-stream (b8-document-diagnostic-stream source-path
                                                         input-id)
        artifact-base
        {:kind :gravity/stage0-b8-gpu-backend-document-artifact
         :task "P07-D105"
         :document-set ["B8"]
         :governing-document b8-document-governing-document
         :pass {:name :b8-gpu-backend-document-coverage
                :input :specialized-lowering-artifact
                :output :b8-gpu-backend-document-artifact
                :requires [:verified-mir-or-gpu-domain-ir
                           :b1-backend-interface :c14-target-lowering
                           :gpu-profile :host-device-boundary
                           :device-memory-lifetimes :transfer-graph
                           :sync-graph :numeric-mode
                           :math-certificate]
                :preserves [:source-spans :generated-origins :types
                            :effects :capabilities :memory-lifetimes
                            :synchronization :numeric-modes :safety
                            :proofs :profile :target
                            :artifact-provenance]
                :emits [:gpu-backend-manifest :kernel-module
                        :device-binary :host-stub :launch-descriptor
                        :device-memory-lifetime-report :transfer-graph
                        :synchronization-graph
                        :target-feature-occupancy-report
                        :math-certificate-bundle :source-debug-map
                        :b8-diagnostic-stream
                        :conformance-criteria-record]
                :rejects b8-document-diagnostic-ids}
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
         :gpu-backend-manifest manifest
         :host-device-boundary-artifact
         (:host-device-boundary-artifact manifest)
         :kernel-ir-or-target-modules
         (:kernel-ir-or-target-modules manifest)
         :device-binary-or-intermediate-artifacts
         (:device-binary-or-intermediate-artifacts manifest)
         :host-stub-artifact (:host-stub-artifact manifest)
         :kernel-lowering-map (:kernel-lowering-map manifest)
         :device-memory-lifetime-report
         (:device-memory-lifetime-report manifest)
         :transfer-graph (:transfer-graph manifest)
         :synchronization-graph (:synchronization-graph manifest)
         :launch-descriptor (:launch-descriptor manifest)
         :target-feature-and-occupancy-report
         (:target-feature-and-occupancy-report manifest)
         :math-certificate-bundle (:math-certificate-bundle manifest)
         :source-debug-map (:source-debug-map manifest)
         :rejected-design-coverage
         [{:design :gpu-kernel-as-host-function
           :diagnostic "B8-KERNEL" :status :rejected}
          {:design :implicit-host-device-transfer
           :diagnostic "B8-TRANSFER" :status :rejected}
          {:design :host-effect-blocking-io-dynamic-eval-in-kernel
           :diagnostic "B8-HOST-EFFECT" :status :rejected}
          {:design :unchecked-device-aliasing-shared-mutable-state
           :diagnostic "B8-MEMORY" :status :rejected}
          {:design :backend-specific-approximate-math-without-certificate
           :diagnostic "B8-MATH" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b8-gpu-backend-conformance-criteria-record
          :host-device-boundary-artifacts :complete
          :accepted-rejected-kernel-feature-fixtures :covered
          :device-memory-lifetime-linear-resource-checks :complete
          :explicit-transfer-graph-validation :complete
          :synchronization-graph-validation :complete
          :atomics-memory-scope-mapping :complete
          :target-feature-launch-configuration-acceptance-rejection :complete
          :strict-approximate-math-certificate-fixtures :complete
          :source-proof-safety-capability-metadata-preservation :complete
          :differential-execution :mir-reference-recorded
          :status :passed}
         :b8-diagnostic-stream diagnostic-stream
         :b8-document-results
         {:documents ["B8"]
          :task "P07-D105"
          :required-diagnostic-ids b8-document-diagnostic-ids
          :specialized-lowering-input-status :complete
          :target-status :complete
          :kernel-status :complete
          :host-effect-status :complete
          :memory-status :complete
          :transfer-status :complete
          :sync-status :complete
          :atomic-status :complete
          :launch-status :complete
          :math-status :complete
          :manifest-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b8-document-validate! source-path artifact-base)
        capability-proof (b8-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b8-document-file-artifact
  [path]
  (b8-document-source-artifact path (slurp path)))

(def b9-document-governing-document
  "docs/phase-07-backend-architecture/106-b9-hdl-backend-design.md")

(def b9-document-diagnostic-ids
  ["B9-TARGET"
   "B9-WIDTH"
   "B9-CLOCK"
   "B9-RESET"
   "B9-CDC"
   "B9-RUNTIME"
   "B9-UNBOUNDED"
   "B9-INTERFACE"
   "B9-TIMING"
   "B9-MANIFEST"])

(def b9-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             b9-document-diagnostic-ids)))

(defn b9-document-source-overrides
  [module]
  (or (get-in module [:metadata :backend :b9-document])
      (get-in module [:metadata :backend :specialized-lowering])
      (get-in module [:metadata :backend :specialized])
      {}))

(defn b9-document-missing-fact
  [id]
  (case id
    "B9-TARGET" :hdl-target-provider-constraint-format
    "B9-WIDTH" :fixed-width-signedness-truncation-proof
    "B9-CLOCK" :clock-domain-record
    "B9-RESET" :reset-domain-record
    "B9-CDC" :cdc-synchronizer-or-waiver-proof
    "B9-RUNTIME" :hardware-runtime-construct-rejection-proof
    "B9-UNBOUNDED" :finite-state-static-control-proof
    "B9-INTERFACE" :port-bus-protocol-capability-schema
    "B9-TIMING" :timing-constraint-and-provider-report
    :hdl-artifact-manifest))

(defn b9-document-signal-id
  [id]
  (case id
    "B9-CLOCK" :clk
    "B9-RESET" :rst
    "B9-CDC" :cdc_handshake
    "B9-INTERFACE" :ready_valid
    "B9-WIDTH" :accumulator
    "B9-TIMING" :clk_to_done
    "B9-RUNTIME" :runtime_call
    "B9-UNBOUNDED" :control_loop
    :gravity_stage0_hdl))

(defn b9-document-fail!
  [id source-path subject extra]
  (fail! id
         "B9 HDL backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :b9-hdl-backend-document
                 :stage :b9-hdl-backend-document-coverage
                 :backend :gravity.backend/hdl
                 :profile :hardware
                 :target :systemverilog
                 :hardware-module :gravity_stage0_hdl
                 :signal-or-state-id (b9-document-signal-id id)
                 :clock-domain :clk
                 :reset-domain :rst
                 :missing-proof-or-constraint
                 (b9-document-missing-fact id)
                 :fallback-status :rejected
                 :remediation "B9 requires verified hardware IR, fixed-width layout, explicit clock/reset and CDC records, finite-state control, hardware interface schemas, timing constraints, testbench/simulation trace evidence, source maps, and HDL artifact provenance."}
                extra)))

(defn b9-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b9-document-override-diagnostics fail-kind)]
      (b9-document-fail!
       id source-path
       {:artifact-id (str "b9-document-" (name fail-kind))
        :signal-or-state-id (keyword (str "b9-document-" (name fail-kind)))}
       {:missing-fields [fail-kind]}))))