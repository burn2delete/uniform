(ns gravity.c14-lowering
  "Hosted Stage0 C14 target-lowering adapter and evidence projection."
  (:require [gravity.c14-lowering.artifact :as artifact]
            [gravity.c14-lowering.operations :as operations]
            [gravity.c14-lowering.policy :as policy]
            [gravity.c14-lowering.validation :as validation]
            [gravity.optimization-lowering :as shared]))
(def ^:private ^:dynamic c14-lowering-diagnostic-ids shared/c14-lowering-diagnostic-ids)
(def ^:private ^:dynamic optimization-lowering-diagnostic-messages shared/optimization-lowering-diagnostic-messages)
(def ^:dynamic c14-lowering-governing-document
  "docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md")
(defn- configuration []
  {:c14-lowering-governing-document c14-lowering-governing-document
   :c14-lowering-diagnostic-ids c14-lowering-diagnostic-ids
   :optimization-lowering-diagnostic-messages optimization-lowering-diagnostic-messages})
(defn c14-lowering-source-overrides [module]
  (operations/invoke :c14-lowering-source-overrides validation/source-overrides module))
(defn c14-lowering-validate-source-overrides! [source-path overrides]
  (operations/invoke :c14-lowering-validate-source-overrides!
                     (fn [path values]
                       (operations/invoke :optimization-lowering-validate-overrides!
                                          shared/optimization-lowering-validate-overrides!
                                          path (validation/source-overrides-artifact values)))
                     source-path overrides))
(defn c14-lowering-diagnostic-catalog [source-path input-id]
  (operations/invoke :c14-lowering-diagnostic-catalog
                     (fn [path identifier]
                       (validation/diagnostic-catalog
                        (configuration) path identifier
                        (fn [source index]
                          (operations/invoke :source-span (fn [p i] {:source p :form-index i}) source index))))
                     source-path input-id))
(defn c14-lowering-validate! [source-path artifact]
  (operations/invoke :c14-lowering-validate!
                     (fn [path value] (validation/validate! (configuration) path value))
                     source-path artifact))
(defn c14-lowering-capability-proof [artifact]
  (operations/invoke :c14-lowering-capability-proof
                     (fn [value] (validation/capability-proof (configuration) value)) artifact))
(defn compiler-c14-lowering-source-artifact [source-path source-text]
  (operations/invoke :compiler-c14-lowering-source-artifact
                     (fn [path text] (artifact/source-artifact (configuration) path text))
                     source-path source-text))
(defn compiler-c14-lowering-file-artifact [path]
  (operations/invoke :compiler-c14-lowering-file-artifact
                     (fn [source-path] (artifact/file-artifact (configuration) source-path)) path))
(defn with-operations [operations thunk]
  (policy/validate-operations! operations)
  (let [merged (merge (operations/current-operations) operations)]
    (binding [c14-lowering-governing-document
              (get merged :c14-lowering-governing-document c14-lowering-governing-document)
              c14-lowering-diagnostic-ids
              (get merged :c14-lowering-diagnostic-ids c14-lowering-diagnostic-ids)
              optimization-lowering-diagnostic-messages
              (get merged :optimization-lowering-diagnostic-messages optimization-lowering-diagnostic-messages)]
      (operations/with-operations operations thunk))))
(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c14-engine-contract {:arglists '([])}
   'c14-lowering-governing-document {:kind :constant}
   'c14-lowering-source-overrides {:arglists '([module])}
   'c14-lowering-validate-source-overrides! {:arglists '([source-path overrides])}
   'c14-lowering-diagnostic-catalog {:arglists '([source-path input-id])}
   'c14-lowering-validate! {:arglists '([source-path artifact])}
   'c14-lowering-capability-proof {:arglists '([artifact])}
   'compiler-c14-lowering-source-artifact {:arglists '([source-path source-text])}
   'compiler-c14-lowering-file-artifact {:arglists '([path])}})
(defn c14-engine-contract [] (policy/engine-contract public-api))
