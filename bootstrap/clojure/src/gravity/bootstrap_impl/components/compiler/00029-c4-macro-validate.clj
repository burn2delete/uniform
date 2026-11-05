

(defn c4-macro-validate!
  [source-path artifact]
  (gravity.macro/validate-native! source-path artifact
                                  #'c4-macro-capability-proof #'c4-macro-fail!))

(defn compiler-c4-macro-source-artifact
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        overrides (c4-macro-source-overrides module)
        _ (c4-macro-validate-overrides! source-path overrides)
        c3-artifact (compiler-c3-syntax-source-artifact source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)]
    (gravity.macro/assemble-native-artifact
     source-path module overrides c3-artifact macro-artifact
     {:c4-expansion-input #'c4-expansion-input
      :c4-macro-environment #'c4-macro-environment
      :c4-expanded-syntax-stream #'c4-expanded-syntax-stream
      :c4-trace-records #'c4-trace-records
      :c4-macro-safety-declarations #'c4-macro-safety-declarations
      :c4-expansion-cache-key #'c4-expansion-cache-key
      :c4-hygiene-capture-records #'c4-hygiene-capture-records
      :c4-build-effect-log #'c4-build-effect-log
      :c4-generated-origin-source-map #'c4-generated-origin-source-map
      :c4-trace-replay-report #'c4-trace-replay-report
      :c4-macro-safety-report #'c4-macro-safety-report}
     #'c4-macro-governing-document #'c4-macro-diagnostic-ids #'c4-macro-rejected-designs
     #'c4-macro-validate! #'c4-macro-capability-proof #'c4-artifact-id)))

(defn compiler-c4-macro-file-artifact
  [path]
  (compiler-c4-macro-source-artifact path (slurp path)))

(def c5-resolution-diagnostic-ids
  c5/c5-resolution-diagnostic-ids)
(def c5-resolution-governing-document
  c5/c5-resolution-governing-document)
(def c5-resolution-rejected-designs
  c5/c5-resolution-rejected-designs)
(def c5-resolution-override-diagnostics
  c5/c5-resolution-override-diagnostics)
(def c5-special-form-symbols
  c5/c5-special-form-symbols)
(def c5-core-auto-imports
  c5/c5-core-auto-imports)
(def c5-type-auto-imports
  c5/c5-type-auto-imports)

(declare c5-resolution-ops
         c5-resolution-source-overrides
         c5-resolution-message
         c5-resolution-fail!
         c5-resolution-validate-overrides!
         c5-package-record
         c5-binding-id
         c5-binding-identity
         c5-definition-binding
         c5-special-form-binding
         c5-core-binding
         c5-type-binding
         c5-import-binding
         c5-alias-table
         c5-import-export-table
         c5-definition-bindings
         c5-macro-bindings
         c5-param-symbols
         c5-local-bindings-from-params
         c5-let-binding-symbols
         c5-local-scope-graph
         c5-bindings-by-name
         c5-resolve-qualified-symbol
         c5-resolution-record
         c5-binding-table
         c5-namespace-analysis-artifact
         c5-dependency-graph
         c5-cross-profile-edge-report
         c5-incremental-invalidation-keys
         c5-resolution-diagnostics
         c5-resolution-verification-report
         c5-resolution-capability-proof
         c5-resolution-validate!
         compiler-c5-resolution-source-artifact
         compiler-c5-resolution-file-artifact)

(defn- c5-call
  [operation & args]
  (c5/with-operations (c5-resolution-ops)
    #(apply operation args)))

(defn c5-resolution-source-overrides
  [module]
  (c5-call c5/c5-resolution-source-overrides module))

(defn c5-resolution-message
  [id]
  (c5-call c5/c5-resolution-message id))

(defn c5-resolution-fail!
  [id source-path subject extra]
  (c5-call c5/c5-resolution-fail! id source-path subject extra))

(defn c5-resolution-validate-overrides!
  [source-path module overrides]
  (c5-call c5/c5-resolution-validate-overrides!
           source-path module overrides))

(defn c5-package-record
  [module]
  (c5-call c5/c5-package-record module))

(defn c5-binding-id
  [binding]
  (c5-call c5/c5-binding-id binding))

(defn c5-binding-identity
  [binding]
  (c5-call c5/c5-binding-identity binding))

(defn c5-definition-binding
  [module definition artifact-id]
  (c5-call c5/c5-definition-binding module definition artifact-id))

(defn c5-special-form-binding
  [sym module]
  (c5-call c5/c5-special-form-binding sym module))

(defn c5-core-binding
  [sym module]
  (c5-call c5/c5-core-binding sym module))

(defn c5-type-binding
  [sym module]
  (c5-call c5/c5-type-binding sym module))

(defn c5-import-binding
  [module dependency imported-name artifact-id]
  (c5-call c5/c5-import-binding module dependency imported-name artifact-id))

(defn c5-alias-table
  [module]
  (c5-call c5/c5-alias-table module))

(defn c5-import-export-table
  [module]
  (c5-call c5/c5-import-export-table module))

(defn c5-definition-bindings
  [module module-artifact c4-artifact]
  (c5-call c5/c5-definition-bindings module module-artifact c4-artifact))

(defn c5-macro-bindings
  [module c4-artifact]
  (c5-call c5/c5-macro-bindings module c4-artifact))