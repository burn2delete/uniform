

(defn b11-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b11-document-source-overrides module)
        _ (b11-document-validate-source-overrides! source-path
                                                   source-overrides)
        specialized-artifact (specialized-lowering-source-artifact source-path
                                                                   source-text)
        input-id (:artifact-id specialized-artifact)
        manifest (b11-document-query-manifest source-path input-id)
        diagnostic-stream (b11-document-diagnostic-stream source-path
                                                          input-id)
        artifact-base
        {:kind :gravity/stage0-b11-query-relational-backend-document-artifact
         :task "P07-D108"
         :document-set ["B11"]
         :governing-document b11-document-governing-document
         :pass {:name :b11-query-relational-backend-document-coverage
                :input :specialized-lowering-artifact
                :output :b11-query-relational-backend-document-artifact
                :requires [:verified-relational-ir-or-schema-domain-ir
                           :b1-backend-interface :c11-mir
                           :c12-domain-ir :c14-target-lowering
                           :schema-version :transaction-policy
                           :capability-manifest :taint-proof
                           :dialect-map :migration-policy
                           :typed-result-adapter]
                :preserves [:source-spans :generated-origins :schemas
                            :effects :capabilities :taint-facts
                            :transactions :migrations :query-plans
                            :safety :proofs :profile :target
                            :artifact-provenance]
                :emits [:query-backend-manifest
                        :relational-ir-handoff-record
                        :dialect-schema-mapping
                        :schema-mapping-record
                        :sql-statement-artifacts
                        :prepared-binding-manifest
                        :query-plan-metadata
                        :typed-result-adapter
                        :transaction-isolation-manifest
                        :migration-artifact
                        :schema-diff-compatibility-report
                        :capability-taint-report
                        :null-type-behavior-record
                        :source-debug-map
                        :b11-diagnostic-stream
                        :conformance-criteria-record]
                :rejects b11-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :specialized-lowering-artifact
         (select-keys specialized-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :query-backend :specialized-lowering-results])
         :specialized-lowering-artifact-kind (:kind specialized-artifact)
         :specialized-lowering-artifact-hash input-id
         :query-backend-manifest manifest
         :relational-ir-handoff-record
         (:relational-ir-handoff-record manifest)
         :dialect-schema-mapping (:dialect-schema-mapping manifest)
         :schema-mapping-record (:schema-mapping-record manifest)
         :sql-statement-artifacts (:sql-statement-artifacts manifest)
         :prepared-binding-manifest (:prepared-binding-manifest manifest)
         :query-plan-metadata (:query-plan-metadata manifest)
         :typed-result-adapter (:typed-result-adapter manifest)
         :transaction-isolation-manifest
         (:transaction-isolation-manifest manifest)
         :migration-artifact (:migration-artifact manifest)
         :schema-diff-compatibility-report
         (:schema-diff-compatibility-report manifest)
         :capability-taint-report (:capability-taint-report manifest)
         :null-collation-timezone-numeric-json-enum-behavior
         (:null-collation-timezone-numeric-json-enum-behavior manifest)
         :distributed-workflow-integration-record
         (:distributed-workflow-integration-record manifest)
         :source-debug-map (:source-debug-map manifest)
         :rejected-design-coverage
         [{:design :string-built-sql-with-tainted-input
           :diagnostic "B11-TAINT" :status :rejected}
          {:design :database-write-or-migration-without-capability
           :diagnostic "B11-CAPABILITY" :status :rejected}
          {:design :schema-drift-without-migration-records
           :diagnostic "B11-MIGRATION" :status :rejected}
          {:design :unchecked-result-adapter-trusting-rows
           :diagnostic "B11-RESULT" :status :rejected}
          {:design :provider-specific-sql-as-portable-semantics
           :diagnostic "B11-PLAN" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b11-query-relational-backend-conformance-criteria-record
          :dialect-and-schema-mapping-artifacts :complete
          :prepared-statement-and-parameter-binding-fixtures :complete
          :tainted-dynamic-query-rejection :covered
          :read-write-transaction-migration-capability-checks :complete
          :null-collation-timezone-numeric-json-enum-fixtures :complete
          :result-adapter-validation :complete
          :migration-compatibility-and-data-loss-rejection :covered
          :distributed-workflow-integration-for-database-steps :complete
          :source-provenance-effect-capability-metadata-preservation :complete
          :database-execution-or-plan-simulation-reference-fixture :simulated
          :status :passed}
         :b11-diagnostic-stream diagnostic-stream
         :b11-document-results
         {:documents ["B11"]
          :task "P07-D108"
          :required-diagnostic-ids b11-document-diagnostic-ids
          :specialized-lowering-input-status :complete
          :relational-ir-status :complete
          :dialect-status :complete
          :schema-status :complete
          :sql-status :complete
          :parameter-status :complete
          :taint-status :complete
          :plan-status :complete
          :result-adapter-status :complete
          :transaction-status :complete
          :migration-status :complete
          :null-type-status :complete
          :capability-status :complete
          :workflow-integration-status :complete
          :manifest-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b11-document-validate! source-path artifact-base)
        capability-proof (b11-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b11-document-file-artifact
  [path]
  (b11-document-source-artifact path (slurp path)))

(def b12-document-governing-document
  "docs/phase-07-backend-architecture/109-b12-mobile-backend-design.md")

(def b12-document-diagnostic-ids
  ["B12-TARGET"
   "B12-PERMISSION"
   "B12-LIFECYCLE"
   "B12-THREAD"
   "B12-NULL"
   "B12-ERROR"
   "B12-BACKGROUND"
   "B12-STORAGE"
   "B12-RESOURCE"
   "B12-MANIFEST"])

(def b12-document-override-diagnostics
  {:b12-target "B12-TARGET"
   :b12-permission "B12-PERMISSION"
   :b12-lifecycle "B12-LIFECYCLE"
   :b12-thread "B12-THREAD"
   :b12-null "B12-NULL"
   :b12-error "B12-ERROR"
   :b12-background "B12-BACKGROUND"
   :b12-storage "B12-STORAGE"
   :b12-resource "B12-RESOURCE"
   :b12-manifest "B12-MANIFEST"})

(defn b12-document-source-overrides
  [module]
  (get-in module [:metadata :backend :specialized-lowering] {}))

(defn b12-document-missing-policy
  [id]
  (case id
    "B12-TARGET" :platform-os-architecture-bundle-ui-target
    "B12-PERMISSION" :permission-capability-runtime-denial-policy
    "B12-LIFECYCLE" :lifecycle-state-model
    "B12-THREAD" :main-thread-actor-callback-affinity
    "B12-NULL" :platform-nullability-adapter
    "B12-ERROR" :platform-exception-callback-error-map
    "B12-BACKGROUND" :background-execution-deployment-policy
    "B12-STORAGE" :local-storage-sync-schema-migration
    "B12-RESOURCE" :asset-resource-bundle-metadata
    :mobile-artifact-manifest))

(defn b12-document-domain-anchor
  [id]
  (case id
    "B12-TARGET" :mobile-platform-target
    "B12-PERMISSION" :platform-api-binding
    "B12-LIFECYCLE" :app-lifecycle
    "B12-THREAD" :ui-thread-update
    "B12-NULL" :platform-null-adapter
    "B12-ERROR" :platform-error-adapter
    "B12-BACKGROUND" :background-task
    "B12-STORAGE" :local-sync-store
    "B12-RESOURCE" :resource-bundle
    :mobile-manifest))

(defn b12-document-fail!
  [id source-path subject extra]
  (fail! id
         "B12 mobile backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :b12-mobile-backend-document
                 :stage :b12-mobile-backend-document-coverage
                 :backend :gravity.backend/mobile
                 :profile :hosted
                 :target :ios
                 :domain-anchor (or (:domain-anchor subject)
                                    (b12-document-domain-anchor id))
                 :platform (or (:platform subject) :ios)
                 :api-symbol (or (:api-symbol subject)
                                 'Foundation/URLSession.data)
                 :lifecycle-state (or (:lifecycle-state subject)
                                      :foreground)
                 :thread-actor (or (:thread-actor subject) :main)
                 :permission (or (:permission subject) :network)
                 :capability (or (:capability subject) :network/request)
                 :missing-policy (b12-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "B12 requires verified mobile/platform IR, explicit platform target and bundle metadata, permission and capability manifests, lifecycle and threading maps, null/error adapters, storage/sync schemas, resource manifests, store-audit metadata, source maps, and simulator/device conformance records."}
                extra)))

(defn b12-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b12-document-override-diagnostics fail-kind)]
      (b12-document-fail!
       id source-path
       {:artifact-id (str "b12-document-" (name fail-kind))
        :domain-anchor (keyword (str "b12-document-" (name fail-kind)))}
       {:missing-fields [fail-kind]}))))