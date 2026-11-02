(ns gravity.c15-diagnostics
  "Hosted Stage0 C15 structured-diagnostics adapter and evidence projection."
  (:require [gravity.c15-diagnostics.artifact :as artifact]
            [gravity.c15-diagnostics.operations :as operations]
            [gravity.c15-diagnostics.policy :as policy]
            [gravity.c15-diagnostics.proof :as proof]
            [gravity.c15-diagnostics.records :as records]
            [gravity.c15-diagnostics.validation :as validation]
            [gravity.compiler-verification-shared :as shared]))
(def ^:private ^:dynamic compiler-verification-diagnostic-messages shared/compiler-verification-diagnostic-messages)
(def ^:private ^:dynamic compiler-verification-override-diagnostics shared/compiler-verification-override-diagnostics)
(def ^:dynamic c15-diagnostics-governing-document "docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md")
(def ^:dynamic c15-diagnostics-diagnostic-ids ["C15-SCHEMA" "C15-ID" "C15-SPAN" "C15-ORIGIN" "C15-FACTS" "C15-REMEDIATION" "C15-REDACTION" "C15-ORDER" "C15-GOLDEN"])
(def ^:dynamic c15-diagnostic-required-fields [:artifact :diagnostic-id :rule :severity :stage :message-key :primary :related :origin-chain :profile :target :involved-artifacts :facts :remediation :redactions :lifecycle])
(defn- configuration []
  {:compiler-verification-diagnostic-messages compiler-verification-diagnostic-messages
   :compiler-verification-override-diagnostics compiler-verification-override-diagnostics
   :c15-diagnostics-governing-document c15-diagnostics-governing-document
   :c15-diagnostics-diagnostic-ids c15-diagnostics-diagnostic-ids
   :c15-diagnostic-required-fields c15-diagnostic-required-fields})
(defn c15-diagnostics-source-overrides [module]
  (operations/invoke :c15-diagnostics-source-overrides records/source-overrides module))
(defn c15-stable-diagnostic-id [diagnostic]
  (operations/invoke :c15-stable-diagnostic-id records/stable-id diagnostic))
(defn c15-diagnostics-fail! [id source-path subject extra]
  (validation/fail! (configuration) id source-path subject extra))
(defn c15-diagnostics-validate-source-overrides! [source-path overrides]
  (operations/invoke :c15-diagnostics-validate-source-overrides!
                     (fn [path values] (validation/validate-source-overrides! (configuration) path values)) source-path overrides))
(defn c15-diagnostic-record
  [rule severity stage message-key source-path form-index primary-artifact facts remediation
   & {:keys [related origin-chain redactions lifecycle generated?] :as operation-options}]
  (apply operations/invoke :c15-diagnostic-record records/diagnostic-record
         rule severity stage message-key source-path form-index primary-artifact facts remediation
         (mapcat identity operation-options)))
(defn c15-diagnostic-catalog []
  (operations/invoke :c15-diagnostic-catalog (fn [] (records/catalog (configuration)))))
(defn c15-diagnostics-validate! [source-path artifact]
  (operations/invoke :c15-diagnostics-validate! (fn [path value] (validation/validate! (configuration) path value)) source-path artifact))
(defn c15-diagnostics-capability-proof [artifact]
  (operations/invoke :c15-diagnostics-capability-proof (fn [value] (proof/capability-proof (configuration) value)) artifact))
(defn compiler-c15-diagnostics-source-artifact [source-path source-text]
  (operations/invoke :compiler-c15-diagnostics-source-artifact
                     (fn [path text] (artifact/source-artifact (configuration) path text)) source-path source-text))
(defn compiler-c15-diagnostics-file-artifact [path]
  (operations/invoke :compiler-c15-diagnostics-file-artifact
                     (fn [source-path] (artifact/file-artifact (configuration) source-path)) path))
(defn with-operations [operations thunk]
  (policy/validate-operations! operations)
  (let [merged (merge (operations/current-operations) operations)]
    (binding [compiler-verification-diagnostic-messages (get merged :compiler-verification-diagnostic-messages compiler-verification-diagnostic-messages)
              compiler-verification-override-diagnostics (get merged :compiler-verification-override-diagnostics compiler-verification-override-diagnostics)
              c15-diagnostics-governing-document (get merged :c15-diagnostics-governing-document c15-diagnostics-governing-document)
              c15-diagnostics-diagnostic-ids (get merged :c15-diagnostics-diagnostic-ids c15-diagnostics-diagnostic-ids)
              c15-diagnostic-required-fields (get merged :c15-diagnostic-required-fields c15-diagnostic-required-fields)]
      (operations/with-operations operations thunk))))
(def public-api
  {'public-api {:kind :contract} 'with-operations {:arglists '([operations thunk])}
   'c15-engine-contract {:arglists '([])} 'c15-diagnostics-governing-document {:kind :constant}
   'c15-diagnostics-diagnostic-ids {:kind :constant} 'c15-diagnostic-required-fields {:kind :constant}
   'c15-diagnostics-source-overrides {:arglists '([module])} 'c15-stable-diagnostic-id {:arglists '([diagnostic])}
   'c15-diagnostics-fail! {:arglists '([id source-path subject extra])}
   'c15-diagnostics-validate-source-overrides! {:arglists '([source-path overrides])}
   'c15-diagnostic-record {:arglists '([rule severity stage message-key source-path form-index primary-artifact facts remediation & {:keys [related origin-chain redactions lifecycle generated?] :as operation-options}])}
   'c15-diagnostic-catalog {:arglists '([])} 'c15-diagnostics-validate! {:arglists '([source-path artifact])}
   'c15-diagnostics-capability-proof {:arglists '([artifact])}
   'compiler-c15-diagnostics-source-artifact {:arglists '([source-path source-text])}
   'compiler-c15-diagnostics-file-artifact {:arglists '([path])}})
(defn c15-engine-contract [] (policy/engine-contract public-api))
