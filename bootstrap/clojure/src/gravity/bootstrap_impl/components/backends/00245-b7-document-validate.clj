

(defn b7-document-validate!
  [source-path artifact]
  (let [native (:native-lowering-artifact artifact)
        manifest (:mlir-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b7-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-native-lowering-artifact (:kind native))
      (b7-document-fail! "B7-MANIFEST" source-path native
                         {:missing-fields [:native-lowering-artifact]}))
    (when-not (= :complete (get-in native
                                   [:capability-based-proof :status]))
      (b7-document-fail! "B7-MANIFEST" source-path native
                         {:missing-fields [:native-lowering-proof]}))
    (when-not (= :complete (get-in manifest
                                   [:dialect-registry-manifest
                                    :status]))
      (b7-document-fail! "B7-DIALECT" source-path manifest
                         {:missing-fields [:dialect-registry]}))
    (when-not (= :complete (get-in manifest
                                   [:gravity-dialect-operation-schema
                                    :status]))
      (b7-document-fail! "B7-DIALECT" source-path manifest
                         {:missing-fields [:operation-schema]}))
    (when-not (= :complete (get-in manifest
                                   [:standard-dialect-fact-mapping
                                    :status]))
      (b7-document-fail! "B7-METADATA" source-path manifest
                         {:missing-fields [:standard-dialect-mapping]}))
    (when-not (= :complete (get-in manifest
                                   [:operation-and-type-mapping-record
                                    :status]))
      (b7-document-fail! "B7-METADATA" source-path manifest
                         {:missing-fields [:operation-type-mapping]}))
    (when-not (= :passed (get-in manifest
                                 [:conversion-target-and-legality-report
                                  :status]))
      (b7-document-fail! "B7-CONVERSION" source-path manifest
                         {:missing-fields [:conversion-legality]}))
    (when-not (= :passed (get-in manifest
                                 [:mlir-verifier-report :status]))
      (b7-document-fail! "B7-VERIFY" source-path manifest
                         {:missing-fields [:verifier-report]}))
    (when-not (= :complete (get-in manifest
                                   [:metadata-preservation-policy
                                    :status]))
      (b7-document-fail! "B7-METADATA" source-path manifest
                         {:missing-fields [:metadata-policy]}))
    (when-not (every? #(= :complete (:status %))
                      (:downstream-handoff-manifests manifest))
      (b7-document-fail! "B7-HANDOFF" source-path manifest
                         {:missing-fields [:downstream-handoff]}))
    (when-not (every? #(and (= :passed (:verifier-before %))
                            (= :passed (:verifier-after %))
                            (contains? % :facts-preserved)
                            (contains? % :facts-invalidated))
                      (:pass-pipeline-log manifest))
      (b7-document-fail! "B7-PASS" source-path manifest
                         {:missing-fields [:pass-pipeline-log]}))
    (when-not (b7-document-mlir-structurally-valid?
               b7-document-mlir-module)
      (b7-document-fail! "B7-VERIFY" source-path manifest
                         {:missing-fields [:mlir-module-structure]}))
    (when-not (every? #(contains? manifest %)
                      [:dialect-registry-manifest
                       :gravity-dialect-operation-schema
                       :standard-dialect-fact-mapping
                       :operation-and-type-mapping-record
                       :mlir-modules
                       :conversion-target-and-legality-report
                       :pass-pipeline-log
                       :mlir-verifier-report
                       :proof-to-dialect-attribute-map
                       :source-debug-map
                       :downstream-handoff-manifests
                       :metadata-preservation-policy
                       :semantic-authority-record])
      (b7-document-fail! "B7-MANIFEST" source-path manifest
                         {:missing-fields [:mlir-artifact-manifest]}))
    (when-not (= (set b7-document-diagnostic-ids) diagnostics)
      (b7-document-fail! "B7-MANIFEST" source-path
                         (:b7-diagnostic-stream artifact)
                         {:missing-fields [:b7-diagnostics]})))
  :complete)

(defn b7-document-capability-proof
  [artifact]
  (let [manifest (:mlir-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b7-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:native-lowering-artifact
                           :capability-based-proof :status]))
     :dialect-registry-covered?
     (= :complete (get-in manifest
                          [:dialect-registry-manifest :status]))
     :gravity-operation-schema-covered?
     (= :complete (get-in manifest
                          [:gravity-dialect-operation-schema :status]))
     :standard-dialect-fact-mapping-covered?
     (= :complete (get-in manifest
                          [:standard-dialect-fact-mapping :status]))
     :operation-type-mapping-covered?
     (= :complete (get-in manifest
                          [:operation-and-type-mapping-record :status]))
     :mlir-module-emitted?
     (= :complete (get-in manifest [:mlir-modules 0 :status]))
     :conversion-legality-passed?
     (= :passed (get-in manifest
                        [:conversion-target-and-legality-report :status]))
     :pass-pipeline-verified?
     (every? #(and (= :passed (:verifier-before %))
                   (= :passed (:verifier-after %)))
             (:pass-pipeline-log manifest))
     :mlir-verifier-report-passed?
     (= :passed (get-in manifest [:mlir-verifier-report :status]))
     :proof-to-dialect-map-covered?
     (boolean (seq (:proof-to-dialect-attribute-map manifest)))
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :downstream-handoff-covered?
     (= #{:gravity.backend/llvm :gravity.backend/gpu}
        (set (map :destination
                  (:downstream-handoff-manifests manifest))))
     :metadata-preservation-policy-covered?
     (= :complete (get-in manifest
                          [:metadata-preservation-policy :status]))
     :semantic-authority-preserved?
     (= :complete (get-in manifest
                          [:semantic-authority-record :status]))
     :mlir-structurally-valid?
     (b7-document-mlir-structurally-valid? b7-document-mlir-module)
     :manifest-complete?
     (every? #(contains? manifest %)
             [:dialect-registry-manifest
              :gravity-dialect-operation-schema
              :standard-dialect-fact-mapping
              :operation-and-type-mapping-record
              :mlir-modules
              :conversion-target-and-legality-report
              :pass-pipeline-log
              :mlir-verifier-report
              :proof-to-dialect-attribute-map
              :source-debug-map
              :downstream-handoff-manifests
              :metadata-preservation-policy
              :semantic-authority-record])
     :diagnostics-covered?
     (= (set b7-document-diagnostic-ids) diagnostics)
     :external-mlir-toolchain-validation?
     (get-in manifest [:mlir-verifier-report :external-toolchain])
     :status :complete}))