

(defn b8-document-validate!
  [source-path artifact]
  (let [specialized (:specialized-lowering-artifact artifact)
        manifest (:gpu-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b8-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-specialized-lowering-artifact
                 (:kind specialized))
      (b8-document-fail! "B8-MANIFEST" source-path specialized
                         {:missing-fields [:specialized-lowering-artifact]}))
    (when-not (= :complete (get-in specialized
                                   [:capability-based-proof :status]))
      (b8-document-fail! "B8-MANIFEST" source-path specialized
                         {:missing-fields [:specialized-lowering-proof]}))
    (when-not (= :spir-v (get-in manifest [:target :api]))
      (b8-document-fail! "B8-TARGET" source-path manifest
                         {:missing-fields [:target-api]}))
    (when-not (= :declared (get-in manifest
                                   [:host-device-boundary-artifact
                                    :status]))
      (b8-document-fail! "B8-KERNEL" source-path manifest
                         {:missing-fields [:host-device-boundary]}))
    (when-not (= :complete (get-in manifest
                                   [:kernel-lowering-map :status]))
      (b8-document-fail! "B8-KERNEL" source-path manifest
                         {:missing-fields [:kernel-lowering-map]}))
    (when-not (= :complete (get-in manifest
                                   [:device-memory-lifetime-report
                                    :status]))
      (b8-document-fail! "B8-MEMORY" source-path manifest
                         {:missing-fields [:device-memory-lifetime]}))
    (when-not (= :complete (get-in manifest [:transfer-graph :status]))
      (b8-document-fail! "B8-TRANSFER" source-path manifest
                         {:missing-fields [:transfer-graph]}))
    (when-not (= :complete (get-in manifest
                                   [:synchronization-graph :status]))
      (b8-document-fail! "B8-SYNC" source-path manifest
                         {:missing-fields [:synchronization-graph]}))
    (when-not (seq (get-in manifest
                           [:synchronization-graph :atomics]))
      (b8-document-fail! "B8-ATOMIC" source-path manifest
                         {:missing-fields [:atomic-memory-scope]}))
    (when-not (= :complete (get-in manifest [:launch-descriptor :status]))
      (b8-document-fail! "B8-LAUNCH" source-path manifest
                         {:missing-fields [:launch-descriptor]}))
    (when-not (= :complete
                 (get-in manifest
                         [:target-feature-and-occupancy-report
                          :status]))
      (b8-document-fail! "B8-LAUNCH" source-path manifest
                         {:missing-fields [:occupancy-report]}))
    (when-not (= :complete (get-in manifest
                                   [:math-certificate-bundle :status]))
      (b8-document-fail! "B8-MATH" source-path manifest
                         {:missing-fields [:math-certificate-bundle]}))
    (when-not (= :preserved (get-in manifest [:source-debug-map :status]))
      (b8-document-fail! "B8-MANIFEST" source-path manifest
                         {:missing-fields [:source-debug-map]}))
    (when-not (b8-document-kernel-structurally-valid?
               b8-document-kernel-module)
      (b8-document-fail! "B8-KERNEL" source-path manifest
                         {:missing-fields [:kernel-module-structure]}))
    (when-not (b8-document-host-stub-structurally-valid?
               b8-document-host-stub)
      (b8-document-fail! "B8-MANIFEST" source-path manifest
                         {:missing-fields [:host-stub-structure]}))
    (when-not (every? #(contains? manifest %)
                      [:host-device-boundary-artifact
                       :kernel-ir-or-target-modules
                       :device-binary-or-intermediate-artifacts
                       :host-stub-artifact
                       :kernel-lowering-map
                       :device-memory-lifetime-report
                       :transfer-graph
                       :synchronization-graph
                       :launch-descriptor
                       :target-feature-and-occupancy-report
                       :math-certificate-bundle
                       :source-debug-map
                       :spirv-validation-record])
      (b8-document-fail! "B8-MANIFEST" source-path manifest
                         {:missing-fields [:gpu-artifact-manifest]}))
    (when-not (= (set b8-document-diagnostic-ids) diagnostics)
      (b8-document-fail! "B8-MANIFEST" source-path
                         (:b8-diagnostic-stream artifact)
                         {:missing-fields [:b8-diagnostics]})))
  :complete)

(defn b8-document-capability-proof
  [artifact]
  (let [manifest (:gpu-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b8-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:specialized-lowering-artifact
                           :capability-based-proof :status]))
     :target-feature-binary-format-covered?
     (= :spir-v (get-in manifest [:target :api]))
     :host-device-boundary-declared?
     (= :declared (get-in manifest
                          [:host-device-boundary-artifact :status]))
     :kernel-legality-covered?
     (= :complete (get-in manifest [:kernel-lowering-map :status]))
     :host-effect-absence-covered?
     (not (str/includes? b8-document-kernel-module "host_alloc"))
     :device-memory-lifetimes-covered?
     (= :complete (get-in manifest
                          [:device-memory-lifetime-report :status]))
     :explicit-transfer-graph-covered?
     (= :complete (get-in manifest [:transfer-graph :status]))
     :synchronization-graph-covered?
     (= :complete (get-in manifest [:synchronization-graph :status]))
     :atomics-memory-scope-covered?
     (boolean (seq (get-in manifest
                           [:synchronization-graph :atomics])))
     :launch-occupancy-covered?
     (and (= :complete (get-in manifest [:launch-descriptor :status]))
          (= :complete
             (get-in manifest
                     [:target-feature-and-occupancy-report :status])))
     :math-certificate-covered?
     (= :complete (get-in manifest [:math-certificate-bundle :status]))
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :kernel-structurally-valid?
     (b8-document-kernel-structurally-valid? b8-document-kernel-module)
     :host-stub-structurally-valid?
     (b8-document-host-stub-structurally-valid? b8-document-host-stub)
     :manifest-complete?
     (every? #(contains? manifest %)
             [:host-device-boundary-artifact
              :kernel-ir-or-target-modules
              :device-binary-or-intermediate-artifacts
              :host-stub-artifact
              :kernel-lowering-map
              :device-memory-lifetime-report
              :transfer-graph
              :synchronization-graph
              :launch-descriptor
              :target-feature-and-occupancy-report
              :math-certificate-bundle
              :source-debug-map
              :spirv-validation-record])
     :diagnostics-covered?
     (= (set b8-document-diagnostic-ids) diagnostics)
     :external-spirv-validation?
     (get-in manifest [:spirv-validation-record :status])
     :status :complete}))