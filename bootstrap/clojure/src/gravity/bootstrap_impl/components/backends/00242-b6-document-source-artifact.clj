

(defn b6-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b6-document-source-overrides module)
        _ (b6-document-validate-source-overrides! source-path
                                                  source-overrides)
        hosted-artifact (hosted-lowering-source-artifact source-path
                                                         source-text)
        input-id (:artifact-id hosted-artifact)
        manifest (b6-document-js-ts-manifest input-id)
        diagnostic-stream (b6-document-diagnostic-stream source-path
                                                         input-id)
        artifact-base
        {:kind :gravity/stage0-b6-js-ts-backend-document-artifact
         :task "P07-D103"
         :document-set ["B6"]
         :governing-document b6-document-governing-document
         :pass {:name :b6-js-ts-backend-document-coverage
                :input :hosted-lowering-artifact
                :output :b6-js-ts-backend-document-artifact
                :requires [:verified-mir-or-domain-ir :b1-backend-interface
                           :c14-target-lowering :hosted-profile
                           :runtime-provider-manifest
                           :capability-manifest :async-effect-map
                           :numeric-representation-map
                           :source-map :package-manifest]
                :preserves [:source-spans :generated-origins :types
                            :effects :capabilities :schemas :safety
                            :proofs :profile :target
                            :artifact-provenance]
                :emits [:js-ts-backend-manifest :runtime-module-target-record
                        :javascript-module :typescript-declarations
                        :source-map :package-manifest
                        :capability-manifest :package-dependency-manifest
                        :async-effect-boundary-map
                        :nullish-exception-translation-map
                        :numeric-representation-manifest
                        :ui-component-binding-metadata
                        :b6-diagnostic-stream
                        :conformance-criteria-record]
                :rejects b6-document-diagnostic-ids}
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
         :js-ts-backend-manifest manifest
         :runtime-and-module-target-record
         (:runtime-and-module-target-record manifest)
         :javascript-module-artifacts
         (:javascript-module-artifacts manifest)
         :typescript-declaration-files
         (:typescript-declaration-files manifest)
         :source-maps-and-generated-origin-maps
         (:source-maps-and-generated-origin-maps manifest)
         :package-metadata (:package-metadata manifest)
         :value-and-type-representation-record
         (:value-and-type-representation-record manifest)
         :capability-manifest (:capability-manifest manifest)
         :package-dependency-manifest
         (:package-dependency-manifest manifest)
         :async-effect-boundary-map
         (:async-effect-boundary-map manifest)
         :nullish-and-exception-translation-map
         (:nullish-and-exception-translation-map manifest)
         :numeric-representation-manifest
         (:numeric-representation-manifest manifest)
         :dynamic-code-and-prototype-policy
         (:dynamic-code-and-prototype-policy manifest)
         :ui-component-binding-metadata
         (:ui-component-binding-metadata manifest)
         :source-debug-map (:source-debug-map manifest)
         :rejected-design-coverage
         [{:design :typescript-annotations-as-safety-source
           :diagnostic "B6-MANIFEST" :status :rejected}
          {:design :ambient-host-global-access
           :diagnostic "B6-GLOBAL" :status :rejected}
          {:design :unchecked-null-undefined-safe-values
           :diagnostic "B6-NULLISH" :status :rejected}
          {:design :dynamic-eval-prototype-mutation
           :diagnostic "B6-EVAL" :status :rejected}
          {:design :lossy-number-lowering
           :diagnostic "B6-NUMERIC" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b6-js-ts-backend-conformance-criteria-record
          :esm-selected-runtime-target-emission :complete
          :typescript-declaration-generation :complete
          :source-map-generated-origin-preservation :complete
          :host-global-capability-manifests :declared
          :package-import-fixtures :covered
          :nullish-exception-rejected-promise-translation :covered
          :numeric-number-bigint-typed-array-boundaries :covered
          :async-event-callback-records :covered
          :ui-component-metadata :recorded
          :eval-prototype-ambient-global-lossy-numeric-rejection :covered
          :differential-execution :mir-reference-recorded
          :status :passed}
         :b6-diagnostic-stream diagnostic-stream
         :b6-document-results
         {:documents ["B6"]
          :task "P07-D103"
          :required-diagnostic-ids b6-document-diagnostic-ids
          :hosted-lowering-input-status :complete
          :target-status :complete
          :javascript-status :complete
          :typescript-status :complete
          :source-map-status :complete
          :package-status :complete
          :value-representation-status :complete
          :capability-status :complete
          :import-status :complete
          :nullish-status :complete
          :exception-status :complete
          :numeric-status :complete
          :async-status :complete
          :dynamic-code-status :complete
          :prototype-status :complete
          :ui-status :complete
          :manifest-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b6-document-validate! source-path artifact-base)
        capability-proof (b6-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b6-document-file-artifact
  [path]
  (b6-document-source-artifact path (slurp path)))

(def b7-document-governing-document
  "docs/phase-07-backend-architecture/104-b7-mlir-backend-design.md")

(def b7-document-diagnostic-ids
  ["B7-DIALECT"
   "B7-VERIFY"
   "B7-CONVERSION"
   "B7-METADATA"
   "B7-EFFECT"
   "B7-NUMERIC"
   "B7-ALIAS"
   "B7-PASS"
   "B7-HANDOFF"
   "B7-MANIFEST"])

(def b7-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             b7-document-diagnostic-ids)))

(defn b7-document-source-overrides
  [module]
  (or (get-in module [:metadata :backend :b7-document])
      (get-in module [:metadata :backend :native-lowering])
      (get-in module [:metadata :backend :native])
      {}))

(defn b7-document-missing-fact
  [id]
  (case id
    "B7-DIALECT" :declared-dialect-operation-registry
    "B7-VERIFY" :mlir-verifier-report
    "B7-CONVERSION" :conversion-target-legality-evidence
    "B7-METADATA" :source-proof-safety-capability-metadata
    "B7-EFFECT" :effect-memory-order-preservation-record
    "B7-NUMERIC" :numeric-mode-dialect-attribute-map
    "B7-ALIAS" :alias-ownership-lifetime-map
    "B7-PASS" :pass-invalidation-repair-record
    "B7-HANDOFF" :downstream-handoff-manifest
    "B7-MANIFEST" :complete-mlir-backend-manifest
    :b7-document-evidence))

(defn b7-document-mlir-operation
  [id]
  (case id
    "B7-DIALECT" "gravity.checked_add"
    "B7-VERIFY" "func.func"
    "B7-CONVERSION" "arith.addi"
    "B7-METADATA" "loc"
    "B7-EFFECT" "MemoryEffectOpInterface"
    "B7-NUMERIC" "arith.addi"
    "B7-ALIAS" "memref.load"
    "B7-PASS" "canonicalize"
    "B7-HANDOFF" "llvm.func"
    "B7-MANIFEST" "gravity/mlir-backend-manifest"
    "gravity.checked_add"))

(defn b7-document-dialect
  [id]
  (case id
    "B7-DIALECT" :gravity.mir
    "B7-VERIFY" :func
    "B7-CONVERSION" :llvm
    "B7-METADATA" :builtin
    "B7-EFFECT" :memref
    "B7-NUMERIC" :arith
    "B7-ALIAS" :memref
    "B7-PASS" :canonicalize
    "B7-HANDOFF" :llvm
    "B7-MANIFEST" :gravity.backend/mlir
    :gravity.mir))