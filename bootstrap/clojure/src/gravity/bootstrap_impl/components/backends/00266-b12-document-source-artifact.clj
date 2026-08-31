

(defn b12-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b12-document-source-overrides module)
        _ (b12-document-validate-source-overrides! source-path
                                                   source-overrides)
        specialized-artifact (specialized-lowering-source-artifact source-path
                                                                   source-text)
        input-id (:artifact-id specialized-artifact)
        manifest (b12-document-mobile-manifest source-path input-id)
        diagnostic-stream (b12-document-diagnostic-stream source-path
                                                          input-id)
        artifact-base
        {:kind :gravity/stage0-b12-mobile-backend-document-artifact
         :task "P07-D109"
         :document-set ["B12"]
         :governing-document b12-document-governing-document
         :pass {:name :b12-mobile-backend-document-coverage
                :input :specialized-lowering-artifact
                :output :b12-mobile-backend-document-artifact
                :requires [:verified-mir-or-ui-domain-ir
                           :b1-backend-interface :c11-mir
                           :c12-domain-ir :c14-target-lowering
                           :platform-target :permission-manifest
                           :lifecycle-threading-map :capability-manifest
                           :platform-adapter-map :storage-sync-schema
                           :store-audit-metadata]
                :preserves [:source-spans :generated-origins :schemas
                            :effects :capabilities :taint-facts
                            :lifecycle :threading :permissions
                            :resources :store-policy :safety :proofs
                            :profile :target :artifact-provenance]
                :emits [:mobile-backend-manifest
                        :platform-target-record
                        :application-bundle-artifact
                        :platform-binding-descriptors
                        :permission-manifest
                        :resource-asset-manifest
                        :lifecycle-threading-map
                        :ui-bridge-metadata
                        :local-storage-sync-schema-bundle
                        :store-audit-metadata
                        :source-debug-map
                        :device-simulator-conformance-report
                        :b12-diagnostic-stream
                        :conformance-criteria-record]
                :rejects b12-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :specialized-lowering-artifact
         (select-keys specialized-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :mobile-backend :specialized-lowering-results])
         :specialized-lowering-artifact-kind (:kind specialized-artifact)
         :specialized-lowering-artifact-hash input-id
         :mobile-backend-manifest manifest
         :platform-target-record (:platform-target-record manifest)
         :application-bundle-artifact
         (:application-bundle-artifact manifest)
         :platform-binding-descriptors
         (:platform-binding-descriptors manifest)
         :permission-manifest (:permission-manifest manifest)
         :resource-asset-manifest (:resource-asset-manifest manifest)
         :lifecycle-threading-map (:lifecycle-threading-map manifest)
         :ui-bridge-metadata (:ui-bridge-metadata manifest)
         :null-error-callback-adapter-record
         (:null-error-callback-adapter-record manifest)
         :local-storage-sync-schema-bundle
         (:local-storage-sync-schema-bundle manifest)
         :background-task-policy (:background-task-policy manifest)
         :store-audit-metadata (:store-audit-metadata manifest)
         :source-debug-map (:source-debug-map manifest)
         :device-simulator-conformance-report
         (:device-simulator-conformance-report manifest)
         :rejected-design-coverage
         [{:design :platform-api-without-permission-or-capability
           :diagnostic "B12-PERMISSION" :status :rejected}
          {:design :hidden-background-network-or-sync-work
           :diagnostic "B12-BACKGROUND" :status :rejected}
          {:design :unchecked-platform-null-error-callback-or-lifecycle
           :diagnostic "B12-NULL" :status :rejected}
          {:design :ui-update-without-main-thread-or-actor
           :diagnostic "B12-THREAD" :status :rejected}
          {:design :local-storage-sync-without-schema-migration
           :diagnostic "B12-STORAGE" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b12-mobile-backend-conformance-criteria-record
          :ios-and-android-style-platform-target-manifests :complete
          :app-bundle-resource-permission-binding-emission :complete
          :platform-api-capability-acceptance-and-rejection :covered
          :lifecycle-and-threading-diagnostics :complete
          :null-error-callback-adapter-fixtures :complete
          :camera-network-storage-notification-background-permission-tests
          :covered
          :local-schema-migration-offline-sync-artifacts :complete
          :source-provenance-effect-capability-metadata-preservation :complete
          :simulator-device-smoke-records :recorded
          :status :passed}
         :b12-diagnostic-stream diagnostic-stream
         :b12-document-results
         {:documents ["B12"]
          :task "P07-D109"
          :required-diagnostic-ids b12-document-diagnostic-ids
          :specialized-lowering-input-status :complete
          :mobile-ir-status :complete
          :target-status :complete
          :bundle-status :complete
          :binding-status :complete
          :permission-status :complete
          :resource-status :complete
          :lifecycle-status :complete
          :thread-status :complete
          :ui-bridge-status :complete
          :null-error-status :complete
          :background-status :complete
          :storage-sync-status :complete
          :store-audit-status :complete
          :simulator-record-status :complete
          :manifest-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b12-document-validate! source-path artifact-base)
        capability-proof (b12-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b12-document-file-artifact
  [path]
  (b12-document-source-artifact path (slurp path)))

(def b13-document-governing-document
  "docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md")

(def b13-document-diagnostic-ids
  artifact-emission-diagnostic-ids)

(def b13-document-override-diagnostics
  artifact-emission-override-diagnostics)

(defn b13-document-source-overrides
  [module]
  (artifact-emission-source-overrides module))

(defn b13-document-missing-policy
  [id]
  (case id
    "B13-SCHEMA" :common-artifact-manifest-schema
    "B13-HASH" :content-addressed-artifact-hash
    "B13-PROVENANCE" :source-compiler-generator-pass-dependency-provenance
    "B13-SOURCEMAP" :source-debug-generated-origin-map
    "B13-EVIDENCE" :safety-proof-certificate-effect-capability-evidence
    "B13-TARGET" :target-runtime-provider-abi-layout-record
    "B13-CONFORMANCE" :conformance-evidence-reference
    "B13-REPRODUCIBILITY" :reproducibility-nondeterminism-environment-record
    "B13-RELEASE" :release-grade-evidence-gate
    :artifact-graph))

(defn b13-document-fail!
  [id source-path subject extra]
  (fail! id
         "B13 artifact emission specification document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :b13-artifact-emission-document
                 :stage :b13-artifact-emission-document-coverage
                 :backend (or (:backend subject)
                              :gravity.backend/artifact-emission)
                 :profile (or (:profile subject) :hosted)
                 :target (or (:target subject) :multi-target-stage0)
                 :artifact-id (:artifact-id subject)
                 :artifact-kind (or (:artifact-kind subject)
                                    (:kind subject))
                 :missing-policy (b13-document-missing-policy id)
                 :missing-evidence (:missing-evidence subject)
                 :stale-field (:stale-field subject)
                 :release-grade? (:release-grade? subject)
                 :fallback-status :rejected
                 :remediation "B13 requires common artifact manifests, content hashes, provenance, source/debug maps, safety/proof/effect/capability evidence, target/runtime/ABI records, reproducibility, conformance evidence, release gates, and a backend-neutral artifact graph before downstream packaging, tooling, or conformance consumption."}
                extra)))

(defn b13-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b13-document-override-diagnostics fail-kind)]
      (b13-document-fail!
       id source-path
       {:artifact-id (str "b13-document-" (name fail-kind))
        :artifact-kind :gravity/artifact-manifest
        :missing-evidence [fail-kind]
        :stale-field fail-kind
        :release-grade? (= id "B13-RELEASE")}
       {:missing-fields [fail-kind]}))))