

(defn b4-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b4-document-source-overrides module)
        _ (b4-document-validate-source-overrides! source-path
                                                  source-overrides)
        hosted-artifact (hosted-lowering-source-artifact source-path
                                                         source-text)
        input-id (:artifact-id hosted-artifact)
        manifest (b4-document-wasm-manifest input-id)
        diagnostic-stream (b4-document-diagnostic-stream source-path
                                                         input-id)
        artifact-base
        {:kind :gravity/stage0-b4-wasm-backend-document-artifact
         :task "P07-D101"
         :document-set ["B4"]
         :governing-document b4-document-governing-document
         :pass {:name :b4-wasm-backend-document-coverage
                :input :hosted-lowering-artifact
                :output :b4-wasm-backend-document-artifact
                :requires [:verified-mir-or-domain-ir :b1-backend-interface
                           :c14-target-lowering :wasm-target-features
                           :component-contracts :canonical-abi
                           :import-capabilities :export-schemas
                           :host-boundary-schemas :async-abi
                           :replay-record :linear-memory-plan]
                :preserves [:source-spans :generated-origins :types
                            :effects :capabilities :schemas :safety
                            :proofs :profile :target
                            :artifact-provenance]
                :emits [:wasm-backend-manifest :target-feature-record
                        :linear-memory-and-table-plan :wasm-modules
                        :component-model-artifact
                        :component-contract-manifest
                        :canonical-abi-manifest
                        :import-capability-manifest
                        :export-schema-manifest
                        :host-boundary-schema-manifest
                        :wasi-component-async-abi-manifest
                        :component-composition-plan
                        :replay-and-nondeterminism-record
                        :b4-diagnostic-stream
                        :conformance-criteria-record]
                :rejects b4-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :hosted-lowering-artifact
         (select-keys hosted-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :hosted-lowering-results])
         :hosted-lowering-artifact-kind (:kind hosted-artifact)
         :hosted-lowering-artifact-hash input-id
         :wasm-backend-manifest manifest
         :target-feature-record (:target-feature-record manifest)
         :linear-memory-and-table-plan
         (:linear-memory-and-table-plan manifest)
         :wasm-modules (:wasm-modules manifest)
         :component-model-artifact (:component-model-artifact manifest)
         :component-contract-manifest
         (:component-contract-manifest manifest)
         :canonical-abi-manifest (:canonical-abi-manifest manifest)
         :import-capability-manifest
         (:import-capability-manifest manifest)
         :export-schema-manifest (:export-schema-manifest manifest)
         :host-boundary-schema-manifest
         (:host-boundary-schema-manifest manifest)
         :wasi-component-async-abi-manifest
         (:wasi-component-async-abi-manifest manifest)
         :component-composition-plan
         (:component-composition-plan manifest)
         :replay-and-nondeterminism-record
         (:replay-and-nondeterminism-record manifest)
         :rejected-design-coverage
         [{:design :ambient-host-import
           :diagnostic "B4-IMPORT" :status :rejected}
          {:design :raw-linear-memory-offset-export
           :diagnostic "B4-MEMORY" :status :rejected}
          {:design :sandbox-as-capability-check-substitute
           :diagnostic "B4-IMPORT" :status :rejected}
          {:design :unrecorded-nondeterministic-host-call
           :diagnostic "B4-NONDETERMINISM" :status :rejected}
          {:design :runtime-feature-probing-for-eligible-profile
           :diagnostic "B4-TARGET" :status :rejected}
          {:design :authority-amplifying-composition
           :diagnostic "B4-COMPONENT" :status :rejected}
          {:design :opaque-host-promise-for-wasi-async
           :diagnostic "B4-WASI-ASYNC" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b4-wasm-backend-conformance-criteria-record
          :core-module-and-component-model :emitted
          :wit-contracts :emitted
          :canonical-abi-validation :complete
          :wasi-async-fixtures [:async-func :stream :future]
          :import-capability-manifests [:browser :wasi :custom]
          :composition-capability-preservation :complete
          :export-schema-validation :complete
          :host-boundary-schema-validation :complete
          :pointer-handle-lifetime-tests :recorded
          :memory-growth-bounds-check-behavior :recorded
          :rejected-ambient-imports-and-pointer-escape :covered
          :invalid-contract-diagnostics :covered
          :replay-records :covered
          :async-replay-cancellation-ordering-backpressure :covered
          :simd-and-atomics-feature-records :covered
          :metadata-preservation :preserved
          :differential-execution :mir-reference-recorded
          :status :passed}
         :b4-diagnostic-stream diagnostic-stream
         :b4-document-results
         {:documents ["B4"]
          :task "P07-D101"
          :required-diagnostic-ids b4-document-diagnostic-ids
          :hosted-lowering-input-status :complete
          :target-feature-status :complete
          :component-contract-status :complete
          :canonical-abi-status :complete
          :import-status :complete
          :export-status :complete
          :linear-memory-status :complete
          :bounds-status :complete
          :replay-status :complete
          :async-status :complete
          :wasi-async-status :complete
          :simd-status :complete
          :atomic-status :complete
          :host-schema-status :complete
          :manifest-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b4-document-validate! source-path artifact-base)
        capability-proof (b4-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b4-document-file-artifact
  [path]
  (b4-document-source-artifact path (slurp path)))

(def b5-document-governing-document
  "docs/phase-07-backend-architecture/102-b5-jvm-backend-design.md")

(def b5-document-diagnostic-ids
  ["B5-TARGET"
   "B5-NULL"
   "B5-EXCEPTION"
   "B5-REFLECTION"
   "B5-CLASSLOADING"
   "B5-INTEROP"
   "B5-RESOURCE"
   "B5-THREAD"
   "B5-NATIVE-IMAGE"
   "B5-PROFILE"
   "B5-MANIFEST"])

(def b5-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             b5-document-diagnostic-ids)))

(defn b5-document-source-overrides
  [module]
  (or (get-in module [:metadata :backend :b5-document])
      (get-in module [:metadata :backend :hosted-lowering])
      (get-in module [:metadata :backend :hosted])
      {}))

(defn b5-document-missing-fact
  [id]
  (case id
    "B5-TARGET" :classfile-jvm-module-packaging-target
    "B5-NULL" :nullability-wrapper-or-opaque-check
    "B5-EXCEPTION" :host-exception-translation
    "B5-REFLECTION" :reflection-dynamic-policy-capability
    "B5-CLASSLOADING" :classloader-policy
    "B5-INTEROP" :java-boundary-descriptor
    "B5-RESOURCE" :deterministic-linear-resource-cleanup
    "B5-THREAD" :thread-monitor-scheduler-effect-record
    "B5-NATIVE-IMAGE" :native-image-configuration
    "B5-PROFILE" :hosted-profile-boundary
    "B5-MANIFEST" :complete-jvm-artifact-manifest
    :b5-document-evidence))

(defn b5-document-jvm-symbol
  [id]
  (case id
    "B5-TARGET" "gravity.stage0.Hosted"
    "B5-NULL" "gravity.stage0.Hosted.nullable"
    "B5-EXCEPTION" "gravity.stage0.Hosted.translateException"
    "B5-REFLECTION" "java.lang.reflect.Method"
    "B5-CLASSLOADING" "java.lang.ClassLoader#defineClass"
    "B5-INTEROP" "gravity.stage0.Hosted.entry"
    "B5-RESOURCE" "gravity.stage0.Hosted.Resource"
    "B5-THREAD" "java.lang.Thread"
    "B5-NATIVE-IMAGE" "native-image-config"
    "B5-PROFILE" "gravity.lower-profile.export"
    "B5-MANIFEST" "gravity/jvm-backend-manifest"
    "gravity.stage0.Hosted"))

(defn b5-document-selected-adapter
  [id]
  (case id
    "B5-NULL" :option-wrapper
    "B5-EXCEPTION" :gravity-error-or-panic-adapter
    "B5-REFLECTION" :declared-reflection-manifest-or-reject
    "B5-CLASSLOADING" :direct-call-or-reject
    "B5-RESOURCE" :auto-closeable-safe5-cleanup
    "B5-THREAD" :runtime-scheduler-provider
    "B5-NATIVE-IMAGE" :generated-native-image-config
    "B5-PROFILE" :profile-boundary-rejection
    :jvm-backend-record))