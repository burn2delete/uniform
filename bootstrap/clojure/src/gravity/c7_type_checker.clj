(ns gravity.c7-type-checker
  "Hosted Stage0 C7 type-analysis engine and artifact projection.

  This namespace owns the Clojure seed compatibility implementation of C7.
  Source acquisition and pass routing are injected by gravity.bootstrap. It is
  not canonical Gravity authority and confers no proof or release status."
  (:require [gravity.c7-type-checker.artifact :as artifact]
            [gravity.c7-type-checker.catalog :as catalog]
            [gravity.c7-type-checker.contract :as contract]
            [gravity.c7-type-checker.diagnostics :as diagnostics]
            [gravity.c7-type-checker.evidence :as evidence]
            [gravity.c7-type-checker.inference :as inference]
            [gravity.c7-type-checker.policy :as policy]
            [gravity.c7-type-checker.verification :as verification]
            [gravity.digest :as digest]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private function-operation-keys policy/function-operation-keys)
(def ^:private scalar-operation-keys policy/scalar-operation-keys)
(def ^:private operation-keys policy/operation-keys)

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))
(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))

(defn- default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))
(defn- default-source-span [source-path form-index]
  {:source source-path :form-index form-index})
(defn- default-c4-artifact-id [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))
(defn- unsupported-host-operation [operation]
  (policy/unsupported-host-operation operation))
(defn- op-fn [key fallback] (or (get *operations* key) fallback))
(defn- fail! [id message data] ((op-fn :fail! default-fail!) id message data))
(defn- source-span [source-path form-index]
  ((op-fn :source-span default-source-span) source-path form-index))
(defn- c4-artifact-id [artifact]
  ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))
(defn- read-source-form-records [source-path source-text]
  ((op-fn :read-source-form-records
          (unsupported-host-operation :read-source-form-records))
   source-path source-text))
(defn- validate-ns-syntax! [source-path forms]
  ((op-fn :validate-ns-syntax!
          (unsupported-host-operation :validate-ns-syntax!))
   source-path forms))
(defn- parse-module [source-path forms]
  ((op-fn :parse-module (unsupported-host-operation :parse-module))
   source-path forms))
(defn- compiler-c6-lowering-source-artifact [source-path source-text]
  ((op-fn :compiler-c6-lowering-source-artifact
          (unsupported-host-operation :compiler-c6-lowering-source-artifact))
   source-path source-text))

(def ^:dynamic c7-type-diagnostic-ids catalog/diagnostic-ids)
(def ^:dynamic c7-type-governing-document catalog/governing-document)
(def ^:dynamic c7-type-rejected-designs catalog/rejected-designs)
(def ^:dynamic c7-type-override-diagnostics catalog/override-diagnostics)

(definterposable c7-type-source-overrides [module]
  (get-in module [:metadata :compiler :c7-type-check] {}))
(definterposable c7-type-message [id] (catalog/type-message id))
(definterposable c7-type-fail! [id source-path subject extra]
  (diagnostics/type-fail! fail! source-span c7-type-message
                          c7-type-governing-document
                          id source-path subject extra))
(definterposable c7-type-validate-overrides! [source-path module overrides]
  (diagnostics/validate-overrides! source-span c7-type-fail!
                                   c7-type-override-diagnostics
                                   source-path module overrides))
(definterposable c7-literal-type [value] (inference/literal-type value))
(definterposable c7-node-operator [node] (inference/node-operator node))
(definterposable c7-node-type [node]
  (inference/node-type c7-literal-type c7-node-operator node))
(definterposable c7-type-fact [node] (inference/type-fact c7-node-type node))
(definterposable c7-type-environment [type-facts]
  (inference/type-environment type-facts))
(definterposable c7-constraint-ledger [type-facts]
  (inference/constraint-ledger type-facts))
(definterposable c7-function-table [nodes] (inference/function-table nodes))
(definterposable c7-dynamic-boundary-records [nodes module]
  (evidence/dynamic-boundary-records c7-node-operator nodes module))
(definterposable c7-cast-records [nodes]
  (evidence/cast-records c7-node-operator nodes))
(definterposable c7-generic-instantiations [nodes]
  (evidence/generic-instantiations c7-node-operator nodes))
(definterposable c7-protocol-dispatch-table [nodes]
  (evidence/protocol-dispatch-table c7-node-operator nodes))
(definterposable c7-schema-links [domain-boundaries]
  (evidence/schema-links domain-boundaries))
(definterposable c7-layout-facts [nodes]
  (evidence/layout-facts c7-node-type nodes))
(definterposable c7-type-diagnostics [source-path nodes]
  (diagnostics/type-diagnostics source-span c7-type-diagnostic-ids
                                c7-type-rejected-designs source-path nodes))
(definterposable c7-typed-core-verifier-report
  [nodes type-facts constraints functions dynamic cast generic dispatch schema layout]
  (verification/typed-core-verifier-report
   nodes type-facts constraints functions dynamic cast generic dispatch schema layout))
(definterposable c7-type-capability-proof [artifact]
  (verification/type-capability-proof c7-type-diagnostic-ids artifact))
(definterposable c7-type-validate! [source-path artifact]
  (verification/validate! c7-type-capability-proof c7-type-fail!
                          source-path artifact))

(definterposable compiler-c7-type-source-artifact [source-path source-text]
  (artifact/source-artifact
   {:read-source-form-records read-source-form-records
    :validate-ns-syntax! validate-ns-syntax! :parse-module parse-module
    :c7-type-source-overrides c7-type-source-overrides
    :c7-type-validate-overrides! c7-type-validate-overrides!
    :compiler-c6-lowering-source-artifact compiler-c6-lowering-source-artifact
    :c7-type-fact c7-type-fact :c7-type-environment c7-type-environment
    :c7-constraint-ledger c7-constraint-ledger :c7-function-table c7-function-table
    :c7-dynamic-boundary-records c7-dynamic-boundary-records
    :c7-cast-records c7-cast-records
    :c7-generic-instantiations c7-generic-instantiations
    :c7-protocol-dispatch-table c7-protocol-dispatch-table
    :c7-schema-links c7-schema-links :c7-layout-facts c7-layout-facts
    :c7-type-diagnostics c7-type-diagnostics
    :c7-typed-core-verifier-report c7-typed-core-verifier-report
    :c7-type-validate! c7-type-validate!
    :c7-type-capability-proof c7-type-capability-proof
    :c4-artifact-id c4-artifact-id
    :c7-type-governing-document c7-type-governing-document
    :c7-type-diagnostic-ids c7-type-diagnostic-ids}
   source-path source-text))
(definterposable compiler-c7-type-file-artifact [path]
  (artifact/file-artifact compiler-c7-type-source-artifact path))

(def ^:private namespace-contract (contract/namespace-contract operation-keys))
(defn- valid-string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- valid-rejected-designs? [value]
  (and (vector? value) (every? map? value)))
(defn- valid-override-map? [value]
  (and (map? value)
       (every? (fn [[key item]] (and (keyword? key) (string? item))) value)))
(defn- validate-operations! [operations]
  (policy/validate-operations!
   operations operation-keys function-operation-keys
   [[:c7-type-diagnostic-ids valid-string-vector? :non-empty-string-vector]
    [:c7-type-governing-document #(and (string? %) (seq %)) :non-empty-string]
    [:c7-type-rejected-designs valid-rejected-designs? :vector-of-maps]
    [:c7-type-override-diagnostics valid-override-map? :keyword-to-string-map]]))

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              c7-type-diagnostic-ids
              (get merged :c7-type-diagnostic-ids c7-type-diagnostic-ids)
              c7-type-governing-document
              (get merged :c7-type-governing-document c7-type-governing-document)
              c7-type-rejected-designs
              (get merged :c7-type-rejected-designs c7-type-rejected-designs)
              c7-type-override-diagnostics
              (get merged :c7-type-override-diagnostics c7-type-override-diagnostics)]
      (thunk))))

(def public-api contract/public-api)
(defn c7-engine-contract [] (assoc namespace-contract :public-api public-api))
