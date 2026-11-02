

(defn c5-param-symbols
  [params]
  (c5-call c5/c5-param-symbols params))

(defn c5-local-bindings-from-params
  [module form syntax-id]
  (c5-call c5/c5-local-bindings-from-params module form syntax-id))

(defn c5-let-binding-symbols
  [form]
  (c5-call c5/c5-let-binding-symbols form))

(defn c5-local-scope-graph
  [module expanded-stream]
  (c5-call c5/c5-local-scope-graph module expanded-stream))

(defn c5-bindings-by-name
  [bindings]
  (c5-call c5/c5-bindings-by-name bindings))

(defn c5-resolve-qualified-symbol
  [module alias-map dependency-map sym]
  (c5-call c5/c5-resolve-qualified-symbol
           module alias-map dependency-map sym))

(defn c5-resolution-record
  [module bindings-by-name alias-map dependency-map local-bindings syntax idx sym]
  (c5-call c5/c5-resolution-record module bindings-by-name alias-map
           dependency-map local-bindings syntax idx sym))

(defn c5-binding-table
  [module definition-bindings macro-bindings lexical-scope-graph
   expanded-stream]
  (c5-call c5/c5-binding-table module definition-bindings macro-bindings
           lexical-scope-graph expanded-stream))

(defn c5-namespace-analysis-artifact
  [module binding-table alias-table import-export-table dependency-graph
   cross-profile-report]
  (c5-call c5/c5-namespace-analysis-artifact module binding-table alias-table
           import-export-table dependency-graph cross-profile-report))

(defn c5-dependency-graph
  [module]
  (c5-call c5/c5-dependency-graph module))

(defn c5-cross-profile-edge-report
  [module dependency-graph]
  (c5-call c5/c5-cross-profile-edge-report module dependency-graph))

(defn c5-incremental-invalidation-keys
  [module c4-artifact binding-table dependency-graph]
  (c5-call c5/c5-incremental-invalidation-keys module c4-artifact
           binding-table dependency-graph))

(defn c5-resolution-diagnostics
  [module]
  (c5-call c5/c5-resolution-diagnostics module))

(defn c5-resolution-verification-report
  [binding-table lexical-scope-graph dependency-graph cross-profile-report
   invalidation]
  (c5-call c5/c5-resolution-verification-report binding-table
           lexical-scope-graph dependency-graph cross-profile-report
           invalidation))

(defn c5-resolution-capability-proof
  [artifact]
  (c5-call c5/c5-resolution-capability-proof artifact))

(defn c5-resolution-validate!
  [source-path artifact]
  (c5-call c5/c5-resolution-validate! source-path artifact))

(defn compiler-c5-resolution-source-artifact
  [source-path source-text]
  (c5-call c5/compiler-c5-resolution-source-artifact source-path source-text))

(defn compiler-c5-resolution-file-artifact
  [path]
  (c5-call c5/compiler-c5-resolution-file-artifact path))

(defn c5-resolution-ops
  []
  {:fail! fail!
   :source-span source-span
   :sha256-hex sha256-hex
   :c4-artifact-id c4-artifact-id
   :read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :module-source-artifact module-source-artifact
   :compiler-c4-macro-source-artifact compiler-c4-macro-source-artifact
   :collect-code-symbols collect-code-symbols
   :ns-form? ns-form?
   :known-source-profiles known-source-profiles
   :supported-targets supported-targets
   :c5-resolution-diagnostic-ids c5-resolution-diagnostic-ids
   :c5-resolution-governing-document c5-resolution-governing-document
   :c5-resolution-rejected-designs c5-resolution-rejected-designs
   :c5-resolution-override-diagnostics c5-resolution-override-diagnostics
   :c5-special-form-symbols c5-special-form-symbols
   :c5-core-auto-imports c5-core-auto-imports
   :c5-type-auto-imports c5-type-auto-imports
   :c5-resolution-source-overrides c5-resolution-source-overrides
   :c5-resolution-message c5-resolution-message
   :c5-resolution-fail! c5-resolution-fail!
   :c5-resolution-validate-overrides! c5-resolution-validate-overrides!
   :c5-package-record c5-package-record
   :c5-binding-id c5-binding-id
   :c5-binding-identity c5-binding-identity
   :c5-definition-binding c5-definition-binding
   :c5-special-form-binding c5-special-form-binding
   :c5-core-binding c5-core-binding
   :c5-type-binding c5-type-binding
   :c5-import-binding c5-import-binding
   :c5-alias-table c5-alias-table
   :c5-import-export-table c5-import-export-table
   :c5-definition-bindings c5-definition-bindings
   :c5-macro-bindings c5-macro-bindings
   :c5-param-symbols c5-param-symbols
   :c5-local-bindings-from-params c5-local-bindings-from-params
   :c5-let-binding-symbols c5-let-binding-symbols
   :c5-local-scope-graph c5-local-scope-graph
   :c5-bindings-by-name c5-bindings-by-name
   :c5-resolve-qualified-symbol c5-resolve-qualified-symbol
   :c5-resolution-record c5-resolution-record
   :c5-binding-table c5-binding-table
   :c5-namespace-analysis-artifact c5-namespace-analysis-artifact
   :c5-dependency-graph c5-dependency-graph
   :c5-cross-profile-edge-report c5-cross-profile-edge-report
   :c5-incremental-invalidation-keys c5-incremental-invalidation-keys
   :c5-resolution-diagnostics c5-resolution-diagnostics
   :c5-resolution-verification-report c5-resolution-verification-report
   :c5-resolution-capability-proof c5-resolution-capability-proof
   :c5-resolution-validate! c5-resolution-validate!
   :compiler-c5-resolution-source-artifact
   compiler-c5-resolution-source-artifact})