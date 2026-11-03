

(defn assert-namespace-effect-and-capability!
  [source-path module inferred-effects]
  (module-analysis-call
   :assert-namespace-effect-and-capability!
   module-analysis/assert-namespace-effect-and-capability!
   source-path module inferred-effects))

(defn- bootstrap-parse-module
  [source-path forms]
  (module-analysis-call
   :parse-module module-analysis/parse-module source-path forms))

#_{:clj-kondo/ignore [:unused-private-var]}
(def ^:private bootstrap-default-parse-module bootstrap-parse-module)

(defn parse-module
  [source-path forms]
  (source-input/module bootstrap-parse-module source-path forms))

(defn uses-println?
  [form]
  (module-analysis-call
   :uses-println? module-analysis/uses-println? form))

(defn validate-module-effects!
  [module]
  (module-analysis-call
   :validate-module-effects! module-analysis/validate-module-effects! module))

(defn module-source-artifact-from-records
  [source-path source-text records]
  (module-analysis-call
   :module-source-artifact-from-records
   module-analysis/module-source-artifact-from-records
   source-path source-text records))

(defn module-source-artifact
  [source-path source-text]
  (module-source-artifact-from-records
   source-path source-text
   (read-source-form-records source-path source-text)))

(defn- bootstrap-module-analysis-identities []
  (into {}
        (for [name '[require-ns parse-clause single-clause-value clause-args
                     parse-options parse-dependency-entry parse-dependencies
                     top-level-definition definition-table collect-symbols
                     collect-code-symbols infer-effects
                     required-capabilities-for-effects
                     profile-direct-import-allowed? assert-unique-aliases!
                     assert-referred-names-unambiguous!
                     assert-qualified-symbols-resolve!
                     assert-profile-boundaries!
                     assert-namespace-effect-and-capability! parse-module
                     uses-println? validate-module-effects!
                     module-source-artifact-from-records module-analysis-call]
              :let [operation-var (ns-resolve 'gravity.bootstrap name)]]
          [name (when operation-var @operation-var)])))

(def ^:private default-bootstrap-module-analysis-identities
  (bootstrap-module-analysis-identities))

(defn- bootstrap-module-analysis-leaf-identities []
  (into {}
        (for [name '[fail! source-span ns-form? bootstrap-target-supported?
                     validate-ns-syntax! syntax-object-stream sha256-hex require-ns
                     parse-clause single-clause-value clause-args parse-options
                     parse-dependency-entry parse-dependencies top-level-definition
                     definition-table collect-symbols collect-code-symbols
                     infer-effects required-capabilities-for-effects
                     profile-direct-import-allowed? assert-unique-aliases!
                     assert-referred-names-unambiguous!
                     assert-qualified-symbols-resolve!
                     assert-profile-boundaries!
                     assert-namespace-effect-and-capability! parse-module
                     uses-println? validate-module-effects!
                     module-source-artifact-from-records with-operations]
              :let [operation-var (ns-resolve 'gravity.module-analysis name)]]
          [name (when operation-var @operation-var)])))

(def ^:private default-bootstrap-module-analysis-leaf-identities
  (bootstrap-module-analysis-leaf-identities))

#_{:clj-kondo/ignore [:unused-private-var]}
(defn- default-module-analysis-state []
  (let [bootstrap-observed (bootstrap-module-analysis-identities)
        leaf-observed (bootstrap-module-analysis-leaf-identities)]
    (and (every? (fn [[name operation]]
                   (identical? operation (get bootstrap-observed name)))
                 default-bootstrap-module-analysis-identities)
         (every? (fn [[name operation]]
                   (identical? operation (get leaf-observed name)))
                 default-bootstrap-module-analysis-leaf-identities))))

#_{:clj-kondo/ignore [:unused-private-var]}
(defn- default-module-analysis-config-state []
  {:known-source-profiles known-source-profiles
   :supported-profiles supported-profiles
   :supported-targets supported-targets
   :effect-capability effect-capability
   :profile-direct-imports profile-direct-imports
   :additional-bootstrap-targets *additional-bootstrap-targets*})

(defn form-op?
  [op form]
  (and (seq? form) (= op (first form))))

(defn contains-form-op?
  [op form]
  (cond
    (form-op? op form) true
    (seq? form) (some #(contains-form-op? op %) form)
    (coll? form) (some #(contains-form-op? op %) form)
    :else false))
