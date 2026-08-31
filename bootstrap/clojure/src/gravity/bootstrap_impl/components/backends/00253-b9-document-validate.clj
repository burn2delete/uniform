

(defn b9-document-validate!
  [source-path artifact]
  (let [specialized (:specialized-lowering-artifact artifact)
        manifest (:hdl-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b9-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-specialized-lowering-artifact
                 (:kind specialized))
      (b9-document-fail! "B9-MANIFEST" source-path specialized
                         {:missing-fields [:specialized-lowering-artifact]}))
    (when-not (= :complete (get-in specialized
                                   [:capability-based-proof :status]))
      (b9-document-fail! "B9-MANIFEST" source-path specialized
                         {:missing-fields [:specialized-lowering-proof]}))
    (when-not (= :systemverilog (get-in manifest [:target :hdl]))
      (b9-document-fail! "B9-TARGET" source-path manifest
                         {:missing-fields [:target-hdl]}))
    (when-not (= :complete (get-in manifest
                                   [:hardware-ir-handoff-record
                                    :status]))
      (b9-document-fail! "B9-MANIFEST" source-path manifest
                         {:missing-fields [:hardware-ir-handoff-record]}))
    (when-not (= :complete (get-in manifest [:hdl-artifacts 0 :status]))
      (b9-document-fail! "B9-MANIFEST" source-path manifest
                         {:missing-fields [:hdl-artifact]}))
    (when-not (b9-document-hdl-structurally-valid? b9-document-hdl-module)
      (b9-document-fail! "B9-MANIFEST" source-path manifest
                         {:missing-fields [:hdl-structure]}))
    (when-not (= :complete (get-in manifest
                                   [:width-and-numeric-report
                                    :status]))
      (b9-document-fail! "B9-WIDTH" source-path manifest
                         {:missing-fields [:width-and-numeric-report]}))
    (when-not (every? #(contains? % :width)
                      (get-in manifest
                              [:interface-port-schema :ports]))
      (b9-document-fail! "B9-WIDTH" source-path manifest
                         {:missing-fields [:port-widths]}))
    (when-not (= :complete (get-in manifest
                                   [:clock-domain-report :status]))
      (b9-document-fail! "B9-CLOCK" source-path manifest
                         {:missing-fields [:clock-domain-report]}))
    (when-not (= :complete (get-in manifest
                                   [:reset-domain-report :status]))
      (b9-document-fail! "B9-RESET" source-path manifest
                         {:missing-fields [:reset-domain-report]}))
    (when-not (and (= :complete (get-in manifest [:cdc-report :status]))
                   (empty? (get-in manifest
                                   [:cdc-report :unmediated-crossings])))
      (b9-document-fail! "B9-CDC" source-path manifest
                         {:missing-fields [:cdc-report]}))
    (when-not (= :complete (get-in manifest
                                   [:runtime-construct-rejection-report
                                    :status]))
      (b9-document-fail! "B9-RUNTIME" source-path manifest
                         {:missing-fields [:runtime-rejection-report]}))
    (when-not (= :complete (get-in manifest
                                   [:state-machine-graph :status]))
      (b9-document-fail! "B9-UNBOUNDED" source-path manifest
                         {:missing-fields [:state-machine-graph]}))
    (when-not (= :complete (get-in manifest
                                   [:interface-port-schema :status]))
      (b9-document-fail! "B9-INTERFACE" source-path manifest
                         {:missing-fields [:interface-port-schema]}))
    (when-not (= :complete (get-in manifest
                                   [:timing-constraint-file :status]))
      (b9-document-fail! "B9-TIMING" source-path manifest
                         {:missing-fields [:timing-constraint-file]}))
    (when-not (b9-document-timing-structurally-valid?
               b9-document-timing-constraints)
      (b9-document-fail! "B9-TIMING" source-path manifest
                         {:missing-fields [:timing-constraint-structure]}))
    (when-not (b9-document-testbench-structurally-valid?
               b9-document-testbench)
      (b9-document-fail! "B9-MANIFEST" source-path manifest
                         {:missing-fields [:testbench-structure]}))
    (when-not (= :complete (get-in manifest
                                   [:simulation-trace-schema
                                    :status]))
      (b9-document-fail! "B9-MANIFEST" source-path manifest
                         {:missing-fields [:simulation-trace-schema]}))
    (when-not (= :preserved (get-in manifest [:source-debug-map :status]))
      (b9-document-fail! "B9-MANIFEST" source-path manifest
                         {:missing-fields [:source-debug-map]}))
    (when-not (every? #(= :complete (:status %))
                      (:hardware-audit-records manifest))
      (b9-document-fail! "B9-MANIFEST" source-path manifest
                         {:missing-fields [:hardware-audit-records]}))
    (when-not (every? #(contains? manifest %)
                      [:hardware-ir-handoff-record
                       :hdl-artifacts
                       :interface-port-schema
                       :clock-domain-report
                       :reset-domain-report
                       :width-and-numeric-report
                       :state-machine-graph
                       :memory-block-manifest
                       :cdc-report
                       :runtime-construct-rejection-report
                       :timing-constraint-file
                       :testbench
                       :simulation-trace-schema
                       :source-debug-map
                       :hardware-audit-records
                       :external-synthesis-validation-record])
      (b9-document-fail! "B9-MANIFEST" source-path manifest
                         {:missing-fields [:hdl-artifact-manifest]}))
    (when-not (= (set b9-document-diagnostic-ids) diagnostics)
      (b9-document-fail! "B9-MANIFEST" source-path
                         (:b9-diagnostic-stream artifact)
                         {:missing-fields [:b9-diagnostics]})))
  :complete)

(defn b9-document-capability-proof
  [artifact]
  (let [manifest (:hdl-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b9-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:specialized-lowering-artifact
                           :capability-based-proof :status]))
     :hdl-target-provider-covered?
     (= :systemverilog (get-in manifest [:target :hdl]))
     :hardware-ir-handoff-covered?
     (= :complete (get-in manifest
                          [:hardware-ir-handoff-record :status]))
     :fixed-widths-covered?
     (= :complete (get-in manifest
                          [:width-and-numeric-report :status]))
     :clock-domain-covered?
     (= :complete (get-in manifest [:clock-domain-report :status]))
     :reset-domain-covered?
     (= :complete (get-in manifest [:reset-domain-report :status]))
     :cdc-covered?
     (and (= :complete (get-in manifest [:cdc-report :status]))
          (empty? (get-in manifest [:cdc-report :unmediated-crossings])))
     :runtime-constructs-rejected?
     (= :complete (get-in manifest
                          [:runtime-construct-rejection-report :status]))
     :finite-control-covered?
     (= :complete (get-in manifest [:state-machine-graph :status]))
     :interface-schema-covered?
     (= :complete (get-in manifest [:interface-port-schema :status]))
     :timing-constraints-covered?
     (= :complete (get-in manifest [:timing-constraint-file :status]))
     :testbench-covered?
     (= :complete (get-in manifest [:testbench :status]))
     :simulation-trace-covered?
     (= :complete (get-in manifest [:simulation-trace-schema :status]))
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :hdl-structurally-valid?
     (b9-document-hdl-structurally-valid? b9-document-hdl-module)
     :testbench-structurally-valid?
     (b9-document-testbench-structurally-valid? b9-document-testbench)
     :timing-structurally-valid?
     (b9-document-timing-structurally-valid?
      b9-document-timing-constraints)
     :manifest-complete?
     (every? #(contains? manifest %)
             [:hardware-ir-handoff-record
              :hdl-artifacts
              :interface-port-schema
              :clock-domain-report
              :reset-domain-report
              :width-and-numeric-report
              :state-machine-graph
              :memory-block-manifest
              :cdc-report
              :runtime-construct-rejection-report
              :timing-constraint-file
              :testbench
              :simulation-trace-schema
              :source-debug-map
              :hardware-audit-records
              :external-synthesis-validation-record])
     :diagnostics-covered?
     (= (set b9-document-diagnostic-ids) diagnostics)
     :external-synthesis-validation?
     (get-in manifest [:external-synthesis-validation-record :status])
     :status :complete}))