

(def ^:private ^:dynamic *macro-expansion-ops-context* nil)

(defn macro-expansion-ops
  []
  (if *macro-expansion-ops-context*
    *macro-expansion-ops-context*
    {:fail! fail!
   :form-op? form-op?
   :contains-form-op? contains-form-op?
   :collect-symbols collect-symbols
   :local-macro-symbol local-macro-symbol
   :source-span source-span
   :sha256-hex sha256-hex
   :splice-key ::splice
   :max-macro-expansion-depth max-macro-expansion-depth
   :builtin-macros builtin-macros
   :parse-param-list parse-param-list
   :expand-template-items expand-template-items
   :expand-template expand-template
   :macro-env-value macro-env-value
   :parse-syntax-template parse-syntax-template
   :builtin-defn-output builtin-defn-output
   :builtin-when-output builtin-when-output
   :thread-first-step thread-first-step
   :builtin-thread-first-output builtin-thread-first-output
   :built-in-registry built-in-registry
   :parse-defmacro-form parse-defmacro-form
   :macro-registry macro-registry
   :macro-namespace-entry macro-namespace-entry
   :macro-build-effect-record macro-build-effect-record
   :macro-build-grants macro-build-grants
   :assert-build-effects! assert-build-effects!
   :collect-let-bindings collect-let-bindings
   :assert-hygiene! assert-hygiene!
   :assert-generated-profile! assert-generated-profile!
   :assert-generated-unsafe! assert-generated-unsafe!
   :bind-macro-arguments bind-macro-arguments
   :expand-macro-form expand-macro-form
   :expansion-generated-origin expansion-generated-origin
   :macro-call macro-call
   :expand-child-form expand-child-form
   :expand-form-children expand-form-children
   :expansion-trace-record expansion-trace-record
   :distinct-by-pr-str distinct-by-pr-str
   :expand-syntax-object expand-syntax-object}))

(defn macro-source-artifact-from-records
  [source-path source-text records]
  (let [forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        syntax (syntax-object-stream source-path records module)]
    (macro-expansion/with-normalized-operations
     (macro-expansion-ops)
     (fn [operations]
       (binding [*macro-expansion-ops-context* operations]
         (macro-expansion/macro-source-artifact-from-records
          source-path source-text records module syntax operations))))))

(defn macro-source-artifact
  [source-path source-text]
  (macro-source-artifact-from-records
   source-path source-text
   (read-source-form-records source-path source-text)))