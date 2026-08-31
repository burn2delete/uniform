(ns gravity.c18-verification
  "Hosted Stage0 C18 pass-correctness/trust evidence projection."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [gravity.compiler-verification-shared :as shared]
            [gravity.digest :as digest]
            [gravity.c18-verification.artifact :as artifact]
            [gravity.c18-verification.catalog :as catalog]
            [gravity.c18-verification.contract :as contract]
            [gravity.c18-verification.diagnostics :as diagnostics]
            [gravity.c18-verification.policy :as policy]
            [gravity.c18-verification.risks :as risks]
            [gravity.c18-verification.verification :as verification]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private function-operation-keys policy/function-operation-keys)
(def ^:private scalar-operation-keys policy/scalar-operation-keys)
(def ^:private operation-keys policy/operation-keys)
(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))
(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))
(defn- unsupported [key] (policy/unsupported key))
(defn- op [key fallback] (or (get *operations* key) fallback))
(defn- fail! [id message data]
  ((op :fail! (fn [rule text payload]
                (throw (ex-info text (assoc (or payload {}) :id rule)))))
   id message data))
(defn- source-span [path index]
  ((op :source-span (fn [p i] {:source p :form-index i})) path index))
(defn- c4-artifact-id [value]
  ((op :c4-artifact-id
       (fn [candidate]
         (str "sha256:" (digest/sha256-hex (pr-str candidate))))) value))
(defn- read-source-form-records [path text]
  ((op :read-source-form-records (unsupported :read-source-form-records))
   path text))
(defn- validate-ns-syntax! [path forms]
  ((op :validate-ns-syntax! (unsupported :validate-ns-syntax!)) path forms))
(defn- parse-module [path forms]
  ((op :parse-module (unsupported :parse-module)) path forms))
(defn- compiler-c17-plugin-source-artifact [path text]
  ((op :compiler-c17-plugin-source-artifact
       (unsupported :compiler-c17-plugin-source-artifact)) path text))

(def ^:private ^:dynamic compiler-verification-diagnostic-messages
  catalog/diagnostic-messages)
(def ^:private ^:dynamic compiler-verification-override-diagnostics
  catalog/override-diagnostics)
(def ^:dynamic c18-verification-governing-document catalog/governing-document)
(def ^:dynamic c18-verification-diagnostic-ids catalog/diagnostic-ids)
(def ^:dynamic c18-pass-risk-required-fields catalog/pass-risk-required-fields)
(def ^:dynamic c18-trust-report-required-fields
  catalog/trust-report-required-fields)

(definterposable c18-verification-source-overrides [module]
  (diagnostics/source-overrides module))
(definterposable c18-verification-fail! [id source-path subject extra]
  (diagnostics/fail! fail! compiler-verification-diagnostic-messages
                     source-span id source-path subject extra))
(definterposable c18-verification-validate-source-overrides!
  [source-path overrides]
  (diagnostics/validate-source-overrides!
   c18-verification-fail! compiler-verification-override-diagnostics
   c18-verification-diagnostic-ids source-path overrides))
(definterposable c18-verification-diagnostic-stream [source-path input-id]
  (diagnostics/diagnostic-stream source-span c18-verification-diagnostic-ids
                                 source-path input-id))
(definterposable c18-pass-risk-records []
  (risks/pass-risk-records))
(definterposable c18-verification-validate! [source-path artifact]
  (verification/validate!
   c18-verification-fail! c18-verification-diagnostic-ids
   c18-pass-risk-required-fields c18-trust-report-required-fields
   source-path artifact))
(definterposable c18-verification-capability-proof [artifact]
  (verification/capability-proof c18-pass-risk-required-fields
                                 c18-verification-diagnostic-ids
                                 artifact))
(definterposable compiler-c18-verification-source-artifact
  [source-path source-text]
  (artifact/source-artifact
   {:read-source-form-records read-source-form-records
    :validate-ns-syntax! validate-ns-syntax!
    :parse-module parse-module
    :source-overrides c18-verification-source-overrides
    :validate-source-overrides! c18-verification-validate-source-overrides!
    :c17-source-artifact compiler-c17-plugin-source-artifact
    :pass-risk-records c18-pass-risk-records
    :diagnostic-stream c18-verification-diagnostic-stream
    :validate! c18-verification-validate!
    :capability-proof c18-verification-capability-proof
    :artifact-id c4-artifact-id
    :governing-document c18-verification-governing-document
    :diagnostic-ids c18-verification-diagnostic-ids}
   source-path source-text))
(definterposable compiler-c18-verification-file-artifact [path]
  (artifact/file-artifact compiler-c18-verification-source-artifact path))

(def ^:private namespace-contract
  (assoc contract/namespace-contract
         :operation-interposition
         {:accepted-keys operation-keys
          :unknown-keys-rejected? true
          :partial-overrides? true
          :single-binding-per-top-level-call? true}))
(defn- string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- keyword-vector? [value]
  (and (vector? value) (seq value) (every? keyword? value)))
(defn- string-map? [value]
  (and (map? value)
       (every? (fn [[key entry]] (and (string? key) (string? entry))) value)))
(defn- override-map? [value]
  (and (map? value)
       (every? (fn [[key entry]]
                 (and (keyword? key) (vector? entry) (= 2 (count entry))
                      (string? (first entry)) (keyword? (second entry))))
               value)))
(defn- validate-operations! [operations]
  (policy/validate-operations! operations))
(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              compiler-verification-diagnostic-messages
              (get merged :compiler-verification-diagnostic-messages
                   compiler-verification-diagnostic-messages)
              compiler-verification-override-diagnostics
              (get merged :compiler-verification-override-diagnostics
                   compiler-verification-override-diagnostics)
              c18-verification-governing-document
              (get merged :c18-verification-governing-document
                   c18-verification-governing-document)
              c18-verification-diagnostic-ids
              (get merged :c18-verification-diagnostic-ids
                   c18-verification-diagnostic-ids)
              c18-pass-risk-required-fields
              (get merged :c18-pass-risk-required-fields
                   c18-pass-risk-required-fields)
              c18-trust-report-required-fields
              (get merged :c18-trust-report-required-fields
                   c18-trust-report-required-fields)]
      (thunk))))
(def public-api contract/public-api)
(defn c18-engine-contract []
  (assoc namespace-contract :public-api public-api))
