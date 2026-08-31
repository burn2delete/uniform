

(defn b12-document-validate!
  [source-path artifact]
  (let [specialized (:specialized-lowering-artifact artifact)
        manifest (:mobile-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b12-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-specialized-lowering-artifact
                 (:kind specialized))
      (b12-document-fail! "B12-MANIFEST" source-path specialized
                          {:missing-fields [:specialized-lowering-artifact]}))
    (when-not (= :complete (get-in specialized
                                   [:capability-based-proof :status]))
      (b12-document-fail! "B12-MANIFEST" source-path specialized
                          {:missing-fields [:specialized-lowering-proof]}))
    (when-not (= :ios (get-in manifest [:target :platform]))
      (b12-document-fail! "B12-TARGET" source-path manifest
                          {:missing-fields [:target-platform]}))
    (when-not (= :complete (get-in manifest
                                   [:mobile-ir-handoff-record :status]))
      (b12-document-fail! "B12-MANIFEST" source-path manifest
                          {:missing-fields [:mobile-ir-handoff-record]}))
    (when-not (= :complete (get-in manifest
                                   [:platform-target-record :status]))
      (b12-document-fail! "B12-TARGET" source-path manifest
                          {:missing-fields [:platform-target-record]}))
    (when-not (= :complete (get-in manifest
                                   [:application-bundle-artifact :status]))
      (b12-document-fail! "B12-RESOURCE" source-path manifest
                          {:missing-fields [:application-bundle-artifact]}))
    (when-not (b12-document-bundle-structurally-valid?
               b12-document-app-bundle)
      (b12-document-fail! "B12-RESOURCE" source-path manifest
                          {:missing-fields [:application-bundle-structure]}))
    (when-not (seq (:platform-binding-descriptors manifest))
      (b12-document-fail! "B12-MANIFEST" source-path manifest
                          {:missing-fields [:platform-binding-descriptors]}))
    (when-not (= :complete (get-in manifest [:permission-manifest :status]))
      (b12-document-fail! "B12-PERMISSION" source-path manifest
                          {:missing-fields [:permission-manifest]}))
    (when-not (b12-document-permission-structurally-valid?
               b12-document-permission-manifest-fixture)
      (b12-document-fail! "B12-PERMISSION" source-path manifest
                          {:missing-fields [:permission-policy]}))
    (when-not (= :complete (get-in manifest
                                   [:resource-asset-manifest :status]))
      (b12-document-fail! "B12-RESOURCE" source-path manifest
                          {:missing-fields [:resource-asset-manifest]}))
    (when-not (= :complete (get-in manifest
                                   [:lifecycle-threading-map :status]))
      (b12-document-fail! "B12-LIFECYCLE" source-path manifest
                          {:missing-fields [:lifecycle-threading-map]}))
    (when-not (= :main (get-in manifest
                               [:lifecycle-threading-map :ui-thread]))
      (b12-document-fail! "B12-THREAD" source-path manifest
                          {:missing-fields [:main-thread-policy]}))
    (when-not (= :complete (get-in manifest [:ui-bridge-metadata :status]))
      (b12-document-fail! "B12-TARGET" source-path manifest
                          {:missing-fields [:ui-bridge-metadata]}))
    (when-not (= :complete (get-in manifest
                                   [:null-error-callback-adapter-record
                                    :status]))
      (b12-document-fail! "B12-NULL" source-path manifest
                          {:missing-fields [:null-error-callback-adapters]}))
    (when-not (= :mapped-to-gravity-errors
                 (get-in manifest
                         [:null-error-callback-adapter-record
                          :exceptions]))
      (b12-document-fail! "B12-ERROR" source-path manifest
                          {:missing-fields [:platform-error-mapping]}))
    (when-not (= :complete (get-in manifest
                                   [:local-storage-sync-schema-bundle
                                    :status]))
      (b12-document-fail! "B12-STORAGE" source-path manifest
                          {:missing-fields [:local-storage-sync-schema]}))
    (when-not (= :complete (get-in manifest
                                   [:background-task-policy :status]))
      (b12-document-fail! "B12-BACKGROUND" source-path manifest
                          {:missing-fields [:background-task-policy]}))
    (when-not (= :rejected (get-in manifest
                                   [:background-task-policy
                                    :hidden-background-execution]))
      (b12-document-fail! "B12-BACKGROUND" source-path manifest
                          {:missing-fields [:hidden-background-rejection]}))
    (when-not (= :complete (get-in manifest [:store-audit-metadata :status]))
      (b12-document-fail! "B12-MANIFEST" source-path manifest
                          {:missing-fields [:store-audit-metadata]}))
    (when-not (= :preserved (get-in manifest [:source-debug-map :status]))
      (b12-document-fail! "B12-MANIFEST" source-path manifest
                          {:missing-fields [:source-debug-map]}))
    (when-not (= :complete (get-in manifest
                                   [:device-simulator-conformance-report
                                    :status]))
      (b12-document-fail! "B12-MANIFEST" source-path manifest
                          {:missing-fields [:device-simulator-conformance]}))
    (when-not (every? #(contains? manifest %)
                      [:mobile-ir-handoff-record
                       :platform-target-record
                       :application-bundle-artifact
                       :platform-binding-descriptors
                       :permission-manifest
                       :resource-asset-manifest
                       :lifecycle-threading-map
                       :ui-bridge-metadata
                       :null-error-callback-adapter-record
                       :local-storage-sync-schema-bundle
                       :background-task-policy
                       :store-audit-metadata
                       :source-debug-map
                       :device-simulator-conformance-report
                       :external-mobile-validation-record])
      (b12-document-fail! "B12-MANIFEST" source-path manifest
                          {:missing-fields [:mobile-artifact-manifest]}))
    (when-not (= (set b12-document-diagnostic-ids) diagnostics)
      (b12-document-fail! "B12-MANIFEST" source-path
                          (:b12-diagnostic-stream artifact)
                          {:missing-fields [:b12-diagnostics]})))
  :complete)

(defn b12-document-capability-proof
  [artifact]
  (let [manifest (:mobile-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b12-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:specialized-lowering-artifact
                           :capability-based-proof :status]))
     :mobile-ir-handoff-covered?
     (= :complete (get-in manifest
                          [:mobile-ir-handoff-record :status]))
     :platform-target-covered?
     (= :complete (get-in manifest
                          [:platform-target-record :status]))
     :application-bundle-emitted?
     (and (= :complete (get-in manifest
                               [:application-bundle-artifact :status]))
          (b12-document-bundle-structurally-valid?
           b12-document-app-bundle))
     :platform-bindings-covered?
     (every? #(= :complete (:status %))
             (:platform-binding-descriptors manifest))
     :permission-manifest-covered?
     (and (= :complete (get-in manifest [:permission-manifest :status]))
          (b12-document-permission-structurally-valid?
           b12-document-permission-manifest-fixture))
     :resource-asset-manifest-covered?
     (= :complete (get-in manifest
                          [:resource-asset-manifest :status]))
     :lifecycle-and-threading-covered?
     (and (= :complete (get-in manifest
                               [:lifecycle-threading-map :status]))
          (= :main (get-in manifest
                           [:lifecycle-threading-map :ui-thread])))
     :ui-bridge-covered?
     (= :complete (get-in manifest [:ui-bridge-metadata :status]))
     :null-and-error-adapters-covered?
     (and (= :complete (get-in manifest
                               [:null-error-callback-adapter-record
                                :status]))
          (= :mapped-to-gravity-errors
             (get-in manifest
                     [:null-error-callback-adapter-record
                      :exceptions])))
     :storage-sync-covered?
     (= :complete (get-in manifest
                          [:local-storage-sync-schema-bundle :status]))
     :background-work-rejected?
     (= :rejected (get-in manifest
                          [:background-task-policy
                           :hidden-background-execution]))
     :store-audit-covered?
     (= :complete (get-in manifest [:store-audit-metadata :status]))
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :device-simulator-record-covered?
     (= :complete (get-in manifest
                          [:device-simulator-conformance-report
                           :status]))
     :manifest-complete?
     (every? #(contains? manifest %)
             [:mobile-ir-handoff-record
              :platform-target-record
              :application-bundle-artifact
              :platform-binding-descriptors
              :permission-manifest
              :resource-asset-manifest
              :lifecycle-threading-map
              :ui-bridge-metadata
              :null-error-callback-adapter-record
              :local-storage-sync-schema-bundle
              :background-task-policy
              :store-audit-metadata
              :source-debug-map
              :device-simulator-conformance-report
              :external-mobile-validation-record])
     :diagnostics-covered?
     (= (set b12-document-diagnostic-ids) diagnostics)
     :external-mobile-validation?
     (get-in manifest [:external-mobile-validation-record :status])
     :status :complete}))