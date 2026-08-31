

(defn sha256-hex
  [text]
  (digest/sha256-hex text))

(defn sha256-bytes-hex
  [bytes]
  (digest/sha256-bytes-hex bytes))
(declare require-ns
         parse-clause
         single-clause-value
         clause-args
         parse-options
         parse-dependency-entry
         parse-dependencies
         top-level-definition
         definition-table
         collect-symbols
         collect-code-symbols
         infer-effects
         required-capabilities-for-effects
         profile-direct-import-allowed?
         assert-unique-aliases!
         assert-referred-names-unambiguous!
         assert-qualified-symbols-resolve!
         assert-profile-boundaries!
         assert-namespace-effect-and-capability!
         parse-module
         uses-println?
         validate-module-effects!
         module-source-artifact-from-records)

(defn- module-analysis-ops
  []
  {:fail! fail!
   :source-span source-span
   :ns-form? ns-form?
   :bootstrap-target-supported? bootstrap-target-supported?
   :validate-ns-syntax! validate-ns-syntax!
   :syntax-object-stream syntax-object-stream
   :sha256-hex sha256-hex
   :known-source-profiles known-source-profiles
   :supported-profiles supported-profiles
   :supported-targets (set/union supported-targets
                                 *additional-bootstrap-targets*)
   :effect-capability effect-capability
   :profile-direct-imports profile-direct-imports
   :require-ns require-ns
   :parse-clause parse-clause
   :single-clause-value single-clause-value
   :clause-args clause-args
   :parse-options parse-options
   :parse-dependency-entry parse-dependency-entry
   :parse-dependencies parse-dependencies
   :top-level-definition top-level-definition
   :definition-table definition-table
   :collect-symbols collect-symbols
   :collect-code-symbols collect-code-symbols
   :infer-effects infer-effects
   :required-capabilities-for-effects required-capabilities-for-effects
   :profile-direct-import-allowed? profile-direct-import-allowed?
   :assert-unique-aliases! assert-unique-aliases!
   :assert-referred-names-unambiguous! assert-referred-names-unambiguous!
   :assert-qualified-symbols-resolve! assert-qualified-symbols-resolve!
   :assert-profile-boundaries! assert-profile-boundaries!
   :assert-namespace-effect-and-capability!
   assert-namespace-effect-and-capability!
   :parse-module parse-module
   :uses-println? uses-println?
   :validate-module-effects! validate-module-effects!
   :module-source-artifact-from-records module-source-artifact-from-records})

(def ^:private ^:dynamic *module-analysis-leaf-call?* false)

(defn- module-analysis-call
  [operation-key operation & args]
  (if *module-analysis-leaf-call?*
    (module-analysis/call-entrypoint-body operation-key operation args)
    (binding [*module-analysis-leaf-call?* true]
      (module-analysis/with-operations
       (module-analysis-ops)
       #(module-analysis/call-entrypoint-body
         operation-key operation args)))))

(defn require-ns
  [source-path forms]
  (module-analysis-call
   :require-ns module-analysis/require-ns source-path forms))

(defn parse-clause
  [source-path clause]
  (module-analysis-call
   :parse-clause module-analysis/parse-clause source-path clause))

(defn single-clause-value
  [source-path clause-map key required?]
  (module-analysis-call
   :single-clause-value module-analysis/single-clause-value
   source-path clause-map key required?))

(defn clause-args
  [clause-map key]
  (module-analysis-call
   :clause-args module-analysis/clause-args clause-map key))

(defn parse-options
  [source-path entry option-items]
  (module-analysis-call
   :parse-options module-analysis/parse-options
   source-path entry option-items))

(defn parse-dependency-entry
  [source-path kind entry]
  (module-analysis-call
   :parse-dependency-entry module-analysis/parse-dependency-entry
   source-path kind entry))

(defn parse-dependencies
  [source-path kind entries]
  (module-analysis-call
   :parse-dependencies module-analysis/parse-dependencies
   source-path kind entries))

(defn top-level-definition
  [syntax]
  (module-analysis-call
   :top-level-definition module-analysis/top-level-definition syntax))

(defn definition-table
  [syntax module]
  (module-analysis-call
   :definition-table module-analysis/definition-table syntax module))

(defn collect-symbols
  [form]
  (module-analysis-call
   :collect-symbols module-analysis/collect-symbols form))

(defn collect-code-symbols
  [form]
  (module-analysis-call
   :collect-code-symbols module-analysis/collect-code-symbols form))

(defn infer-effects
  [forms]
  (module-analysis-call
   :infer-effects module-analysis/infer-effects forms))

(defn required-capabilities-for-effects
  [effects]
  (module-analysis-call
   :required-capabilities-for-effects
   module-analysis/required-capabilities-for-effects effects))