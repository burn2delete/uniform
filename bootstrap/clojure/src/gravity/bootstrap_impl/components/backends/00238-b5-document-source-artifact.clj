

(defn b5-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b5-document-source-overrides module)
        _ (b5-document-validate-source-overrides! source-path
                                                  source-overrides)
        hosted-artifact (hosted-lowering-source-artifact source-path
                                                         source-text)
        input-id (:artifact-id hosted-artifact)
        manifest (b5-document-jvm-manifest input-id)
        diagnostic-stream (b5-document-diagnostic-stream source-path
                                                         input-id)
        artifact-base
        {:kind :gravity/stage0-b5-jvm-backend-document-artifact
         :task "P07-D102"
         :document-set ["B5"]
         :governing-document b5-document-governing-document
         :pass {:name :b5-jvm-backend-document-coverage
                :input :hosted-lowering-artifact
                :output :b5-jvm-backend-document-artifact
                :requires [:verified-mir-or-domain-ir :b1-backend-interface
                           :c14-target-lowering :hosted-profile
                           :classfile-jvm-target-record
                           :exception-map :nullability-map
                           :runtime-provider-manifest
                           :interop-descriptor :source-debug-map]
                :preserves [:source-spans :generated-origins :types
                            :effects :capabilities :safety :proofs
                            :profile :target :artifact-provenance]
                :emits [:jvm-backend-manifest :java-source-files
                        :module-descriptors :classfile-target-record
                        :jar-or-module-artifact
                        :java-interop-descriptor
                        :nullability-and-exception-translation-map
                        :reflection-and-dynamic-use-manifest
                        :native-image-configuration
                        :runtime-helper-manifest
                        :source-debug-map :b5-diagnostic-stream
                        :conformance-criteria-record]
                :rejects b5-document-diagnostic-ids}
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
         :jvm-backend-manifest manifest
         :classfile-and-jvm-target-record
         (:classfile-and-jvm-target-record manifest)
         :class-and-module-model (:class-and-module-model manifest)
         :java-source-files (:java-source-files manifest)
         :module-descriptors (:module-descriptors manifest)
         :class-files (:class-files manifest)
         :jar-or-module-artifact (:jar-or-module-artifact manifest)
         :java-interop-descriptor (:java-interop-descriptor manifest)
         :nullability-and-exception-translation-map
         (:nullability-and-exception-translation-map manifest)
         :reflection-and-dynamic-use-manifest
         (:reflection-and-dynamic-use-manifest manifest)
         :native-image-configuration (:native-image-configuration manifest)
         :runtime-helper-manifest (:runtime-helper-manifest manifest)
         :source-debug-map (:source-debug-map manifest)
         :rejected-design-coverage
         [{:design :java-null-as-safe-type-inhabitant
           :diagnostic "B5-NULL" :status :rejected}
          {:design :host-exception-leaks-through-safe-api
           :diagnostic "B5-EXCEPTION" :status :rejected}
          {:design :hidden-reflection-classloading
           :diagnostic "B5-REFLECTION" :status :rejected}
          {:design :gc-finalization-as-deterministic-cleanup
           :diagnostic "B5-RESOURCE" :status :rejected}
          {:design :jvm-only-semantics-through-lower-profile
           :diagnostic "B5-PROFILE" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b5-jvm-backend-conformance-criteria-record
          :classfile-jar-module-source-map :emitted
          :hosted-pure-code-and-interop-lowering :complete
          :nullability-wrapper-acceptance :complete
          :unchecked-null-rejection :covered
          :exception-translation-fixtures :covered
          :reflection-dynamic-loading-policy :covered
          :native-image-configuration :generated
          :linear-resource-cleanup-despite-gc :covered
          :thread-monitor-executor-atomic-effects :recorded
          :profile-boundary-rejection :covered
          :differential-execution :mir-reference-recorded
          :status :passed}
         :b5-diagnostic-stream diagnostic-stream
         :b5-document-results
         {:documents ["B5"]
          :task "P07-D102"
          :required-diagnostic-ids b5-document-diagnostic-ids
          :hosted-lowering-input-status :complete
          :target-status :complete
          :class-module-status :complete
          :value-representation-status :complete
          :interop-status :complete
          :nullability-status :complete
          :exception-status :complete
          :reflection-status :complete
          :classloading-status :complete
          :resource-status :complete
          :thread-status :complete
          :native-image-status :complete
          :profile-status :complete
          :manifest-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b5-document-validate! source-path artifact-base)
        capability-proof (b5-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b5-document-file-artifact
  [path]
  (b5-document-source-artifact path (slurp path)))

(def b6-document-governing-document
  "docs/phase-07-backend-architecture/103-b6-javascript-typescript-backend-design.md")

(def b6-document-diagnostic-ids
  ["B6-TARGET"
   "B6-GLOBAL"
   "B6-IMPORT"
   "B6-NULLISH"
   "B6-EXCEPTION"
   "B6-NUMERIC"
   "B6-EVAL"
   "B6-PROTOTYPE"
   "B6-ASYNC"
   "B6-UI"
   "B6-MANIFEST"])

(def b6-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             b6-document-diagnostic-ids)))

(defn b6-document-source-overrides
  [module]
  (or (get-in module [:metadata :backend :b6-document])
      (get-in module [:metadata :backend :hosted-lowering])
      (get-in module [:metadata :backend :hosted])
      {}))

(defn b6-document-missing-fact
  [id]
  (case id
    "B6-TARGET" :runtime-ecmascript-module-bundler-source-map-target
    "B6-GLOBAL" :host-global-effect-capability-schema
    "B6-IMPORT" :typed-package-import-integrity-and-side-effect-policy
    "B6-NULLISH" :option-result-checked-nullish-adapter
    "B6-EXCEPTION" :exception-promise-callback-translation
    "B6-NUMERIC" :numeric-representation-boundary-validation
    "B6-EVAL" :dynamic-code-loading-policy
    "B6-PROTOTYPE" :prototype-object-layout-policy
    "B6-ASYNC" :async-effect-cancellation-event-loop-record
    "B6-UI" :ui-component-schema-source-map-capability-record
    "B6-MANIFEST" :complete-js-ts-artifact-manifest
    :b6-document-evidence))

(defn b6-document-host-symbol
  [id]
  (case id
    "B6-TARGET" "gravity-stage0.mjs"
    "B6-GLOBAL" "globalThis"
    "B6-IMPORT" "@gravity/stage0"
    "B6-NULLISH" "optionFromNullish"
    "B6-EXCEPTION" "translatePromise"
    "B6-NUMERIC" "checkedNumber"
    "B6-EVAL" "eval"
    "B6-PROTOTYPE" "Object.prototype"
    "B6-ASYNC" "Promise"
    "B6-UI" "GravityStage0Component"
    "B6-MANIFEST" "gravity/js-ts-backend-manifest"
    "gravity-stage0.mjs"))

(defn b6-document-selected-adapter
  [id]
  (case id
    "B6-TARGET" :browser-esm-target-record-or-reject
    "B6-GLOBAL" :host-global-capability-manifest-entry
    "B6-IMPORT" :typed-schema-wrapped-package-import
    "B6-NULLISH" :option-result-opaque-wrapper
    "B6-EXCEPTION" :gravity-error-panic-effect-adapter
    "B6-NUMERIC" :bigint-number-typed-array-checked-helper
    "B6-EVAL" :dynamic-code-rejection
    "B6-PROTOTYPE" :frozen-record-layout
    "B6-ASYNC" :async-effect-boundary-record
    "B6-UI" :component-schema-source-map-capability-record
    :js-ts-backend-record))

(defn b6-document-fail!
  [id source-path subject extra]
  (fail! id
         "B6 JavaScript/TypeScript backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :b6-js-ts-backend-document
                 :stage (or (:stage subject)
                            :b6-js-ts-backend-document-coverage)
                 :backend :gravity.backend/js-ts
                 :profile (or (:profile subject) :hosted)
                 :runtime (or (:runtime subject) :browser)
                 :module-format (or (:module-format subject) :esm)
                 :ecmascript (or (:ecmascript subject) :es2022)
                 :artifact-id (:artifact-id subject)
                 :mir-op (or (:mir-op subject) :host-call)
                 :domain-anchor (:domain-anchor subject)
                 :host-symbol (or (:host-symbol subject)
                                  (b6-document-host-symbol id))
                 :package-id (or (:package-id subject)
                                 (when (= id "B6-IMPORT")
                                   "@gravity/stage0"))
                 :missing-effect-capability-schema-fact
                 (or (:missing-effect-capability-schema-fact subject)
                     (b6-document-missing-fact id))
                 :selected-adapter-or-rejection
                 (or (:selected-adapter-or-rejection subject)
                     (b6-document-selected-adapter id))
                 :fallback-status (or (:fallback-status subject) :rejected)
                 :remediation "Emit JS/TS artifacts only from verified hosted backend input with pinned runtime, ECMAScript, module, package, source-map, host-global, package-import, nullish, exception, async, numeric, UI, and manifest evidence."}
                extra)))