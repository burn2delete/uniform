

(defn- c3-syntax-diagnostics-call
  [operation & args]
  (if *c3-syntax-diagnostics-leaf-call?*
    (apply operation args)
    (binding [*c3-syntax-diagnostics-leaf-call?* true]
      (c3-syntax-diagnostics/with-operations
       (c3-syntax-diagnostics-ops)
       #(apply operation args)))))

(defn c3-syntax-source-overrides
  [module]
  (c3-syntax-diagnostics-call
   c3-syntax-diagnostics/c3-syntax-source-overrides module))

(defn c3-syntax-overrides-from-forms
  [forms]
  (c3-syntax-diagnostics-call
   c3-syntax-diagnostics/c3-syntax-overrides-from-forms forms))

(defn c3-syntax-message
  [id]
  (c3-syntax-diagnostics-call
   c3-syntax-diagnostics/c3-syntax-message id))

(defn c3-syntax-fail!
  [id source-path subject extra]
  (c3-syntax-diagnostics-call
   c3-syntax-diagnostics/c3-syntax-fail!
   id source-path subject extra))

(defn c3-syntax-validate-overrides!
  [source-path overrides]
  (c3-syntax-diagnostics-call
   c3-syntax-diagnostics/c3-syntax-validate-overrides!
   source-path overrides))

(defn c3-origin-chain
  [seed source-unit]
  (syntax-origin/c3-origin-chain seed source-unit))

(declare c3-c2-reader-integrity-report
         c3-validate-c2-reader-artifact!)

(defn- c3-reader-integrity-ops
  []
  {:c2-lexical-product-validation c2-lexical-product-validation
   :c2-incremental-hashes c2-incremental-hashes
   :c2-literal-records c2-literal-records
   :c2-deferred-semantic-literals c2-deferred-semantic-literals
   :c3-deferred-ratio-descriptor-from-raw
   c3-deferred-ratio-descriptor-from-raw
   :c2-reader-product-integrity-record c2-reader-product-integrity-record
   :reader-canonical-hash reader-canonical-hash
   :sha256-hex sha256-hex
   :c2-reader-artifact-id c2-reader-artifact-id
   :c3-c2-reader-integrity-report c3-c2-reader-integrity-report
   :c3-validate-c2-reader-artifact! c3-validate-c2-reader-artifact!
   :c3-syntax-fail! c3-syntax-fail!
   :source-span source-span
   :max-reader-form-graph-depth max-reader-form-graph-depth})