(ns gravity.c13-optimization
  "Hosted Stage0 C13 MIR optimization adapter and evidence projection."
  (:require [gravity.c13-optimization.artifact :as artifact]
            [gravity.c13-optimization.operations :as operations]
            [gravity.c13-optimization.policy :as policy]
            [gravity.c13-optimization.validation :as validation]
            [gravity.optimization-lowering :as shared]))

(def ^:private ^:dynamic c13-optimization-diagnostic-ids shared/c13-optimization-diagnostic-ids)
(def ^:private ^:dynamic optimization-lowering-diagnostic-messages
  shared/optimization-lowering-diagnostic-messages)
(def ^:private ^:dynamic optimization-pass-contract-seed shared/optimization-pass-contract-seed)

(def ^:dynamic c13-optimization-governing-document
  "docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md")

(defn- configuration []
  {:c13-optimization-governing-document c13-optimization-governing-document
   :c13-optimization-diagnostic-ids c13-optimization-diagnostic-ids
   :optimization-lowering-diagnostic-messages optimization-lowering-diagnostic-messages
   :optimization-pass-contract-seed optimization-pass-contract-seed})

(defn c13-optimization-source-overrides [module]
  (operations/invoke :c13-optimization-source-overrides validation/source-overrides module))

(defn c13-optimization-validate-source-overrides! [source-path overrides]
  (operations/invoke :c13-optimization-validate-source-overrides!
                     (fn [path values]
                       (operations/invoke :optimization-lowering-validate-overrides!
                                          shared/optimization-lowering-validate-overrides!
                                          path (validation/source-overrides-artifact values)))
                     source-path overrides))

(defn c13-optimization-diagnostic-catalog [source-path]
  (operations/invoke :c13-optimization-diagnostic-catalog
                     (fn [path]
                       (validation/diagnostic-catalog
                        (configuration) path
                        (fn [source index]
                          (operations/invoke :source-span
                                             (fn [p i] {:source p :form-index i})
                                             source index))))
                     source-path))

(defn c13-optimization-validate! [source-path artifact]
  (operations/invoke :c13-optimization-validate!
                     (fn [path value] (validation/validate! (configuration) path value))
                     source-path artifact))

(defn c13-optimization-capability-proof [artifact]
  (operations/invoke :c13-optimization-capability-proof
                     (fn [value] (validation/capability-proof (configuration) value))
                     artifact))

(defn compiler-c13-optimization-source-artifact [source-path source-text]
  (operations/invoke :compiler-c13-optimization-source-artifact
                     (fn [path text] (artifact/source-artifact (configuration) path text))
                     source-path source-text))

(defn compiler-c13-optimization-file-artifact [path]
  (operations/invoke :compiler-c13-optimization-file-artifact
                     (fn [source-path] (artifact/file-artifact (configuration) source-path))
                     path))

(defn with-operations [operations thunk]
  (policy/validate-operations! operations)
  (let [merged (merge (operations/current-operations) operations)]
    (binding [c13-optimization-governing-document
              (get merged :c13-optimization-governing-document c13-optimization-governing-document)
              c13-optimization-diagnostic-ids
              (get merged :c13-optimization-diagnostic-ids c13-optimization-diagnostic-ids)
              optimization-lowering-diagnostic-messages
              (get merged :optimization-lowering-diagnostic-messages optimization-lowering-diagnostic-messages)
              optimization-pass-contract-seed
              (get merged :optimization-pass-contract-seed optimization-pass-contract-seed)]
      (operations/with-operations operations thunk))))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c13-engine-contract {:arglists '([])}
   'c13-optimization-governing-document {:kind :constant}
   'c13-optimization-source-overrides {:arglists '([module])}
   'c13-optimization-validate-source-overrides! {:arglists '([source-path overrides])}
   'c13-optimization-diagnostic-catalog {:arglists '([source-path])}
   'c13-optimization-validate! {:arglists '([source-path artifact])}
   'c13-optimization-capability-proof {:arglists '([artifact])}
   'compiler-c13-optimization-source-artifact {:arglists '([source-path source-text])}
   'compiler-c13-optimization-file-artifact {:arglists '([path])}})

(defn c13-engine-contract [] (policy/engine-contract public-api))
