(ns gravity.c17-plugin
  "Hosted Stage0 C17 plugin/pass API schema and evidence projection."
  (:require [gravity.c17-plugin.artifact :as artifact]
            [gravity.c17-plugin.diagnostics :as diagnostics]
            [gravity.c17-plugin.policy :as policy]
            [gravity.c17-plugin.proof :as proof]
            [gravity.c17-plugin.validation :as validation]
            [gravity.compiler-verification-shared :as shared]
            [gravity.digest :as digest]))

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
(defn- sha256-hex [value] ((op :sha256-hex digest/sha256-hex) value))
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
(defn- compiler-c16-incremental-source-artifact [path text]
  ((op :compiler-c16-incremental-source-artifact
       (unsupported :compiler-c16-incremental-source-artifact)) path text))

(def ^:private ^:dynamic compiler-verification-diagnostic-messages
  shared/compiler-verification-diagnostic-messages)
(def ^:private ^:dynamic compiler-verification-override-diagnostics
  shared/compiler-verification-override-diagnostics)

(def ^:dynamic c17-plugin-governing-document
  "docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md")
(def ^:dynamic c17-plugin-diagnostic-ids
  ["C17-MANIFEST" "C17-API" "C17-CAPABILITY" "C17-BUILD-EFFECT"
   "C17-SANDBOX" "C17-PASS-CONTRACT" "C17-OUTPUT" "C17-DOMAIN"
   "C17-FACET" "C17-TRUST"])
(def ^:dynamic c17-plugin-manifest-required-fields
  [:artifact :plugin :package :api-version :compiler-compatibility :trust
   :profile :build-effects :capabilities :capability-scopes :passes
   :domains :facets :emits :conformance])
(def ^:dynamic c17-plugin-pass-contract-required-fields
  [:input :output :requires :preserves :invalidates :regenerates
   :proof-obligations :emits])
(def ^:dynamic c17-plugin-cache-key-required-fields
  [:artifact :plugin-package :plugin-version :manifest :grants
   :dependencies :replay-record :pass])

(definterposable c17-plugin-source-overrides [module]
  (diagnostics/source-overrides module))
(definterposable c17-plugin-fail! [id source-path subject extra]
  (diagnostics/fail!
   {:diagnostic-messages compiler-verification-diagnostic-messages
    :fail fail! :source-span source-span}
   id source-path subject extra))
(definterposable c17-plugin-validate-source-overrides! [source-path overrides]
  (diagnostics/validate-source-overrides!
   {:override-diagnostics compiler-verification-override-diagnostics
    :diagnostic-ids c17-plugin-diagnostic-ids
    :plugin-fail! c17-plugin-fail!}
   source-path overrides))
(definterposable c17-plugin-diagnostic-stream
  [source-path plugin-manifest input-id]
  (diagnostics/diagnostic-stream
   {:diagnostic-ids c17-plugin-diagnostic-ids :source-span source-span}
   source-path plugin-manifest input-id))
(definterposable c17-plugin-validate! [source-path artifact]
  (validation/validate!
   {:diagnostic-ids c17-plugin-diagnostic-ids
    :manifest-required-fields c17-plugin-manifest-required-fields
    :pass-contract-required-fields c17-plugin-pass-contract-required-fields
    :plugin-fail! c17-plugin-fail!}
   source-path artifact))
(definterposable c17-plugin-capability-proof [artifact]
  (proof/capability-proof
   {:diagnostic-ids c17-plugin-diagnostic-ids
    :manifest-required-fields c17-plugin-manifest-required-fields
    :pass-contract-required-fields c17-plugin-pass-contract-required-fields
    :cache-key-required-fields c17-plugin-cache-key-required-fields}
   artifact))
(definterposable compiler-c17-plugin-source-artifact [source-path source-text]
  (artifact/source-artifact
   {:governing-document c17-plugin-governing-document
    :diagnostic-ids c17-plugin-diagnostic-ids
    :sha256-hex sha256-hex :c4-artifact-id c4-artifact-id
    :read-source-form-records read-source-form-records
    :validate-ns-syntax! validate-ns-syntax! :parse-module parse-module
    :c16-incremental-artifact compiler-c16-incremental-source-artifact
    :source-overrides c17-plugin-source-overrides
    :validate-source-overrides! c17-plugin-validate-source-overrides!
    :diagnostic-stream c17-plugin-diagnostic-stream
    :validate! c17-plugin-validate!
    :capability-proof c17-plugin-capability-proof}
   source-path source-text))
(definterposable compiler-c17-plugin-file-artifact [path]
  (artifact/file-artifact compiler-c17-plugin-source-artifact path))

(def ^:private namespace-contract policy/namespace-contract)
(defn- string-vector? [value] (policy/string-vector? value))
(defn- keyword-vector? [value] (policy/keyword-vector? value))
(defn- string-map? [value] (policy/string-map? value))
(defn- override-map? [value] (policy/override-map? value))
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
              c17-plugin-governing-document
              (get merged :c17-plugin-governing-document
                   c17-plugin-governing-document)
              c17-plugin-diagnostic-ids
              (get merged :c17-plugin-diagnostic-ids c17-plugin-diagnostic-ids)
              c17-plugin-manifest-required-fields
              (get merged :c17-plugin-manifest-required-fields
                   c17-plugin-manifest-required-fields)
              c17-plugin-pass-contract-required-fields
              (get merged :c17-plugin-pass-contract-required-fields
                   c17-plugin-pass-contract-required-fields)
              c17-plugin-cache-key-required-fields
              (get merged :c17-plugin-cache-key-required-fields
                   c17-plugin-cache-key-required-fields)]
      (thunk))))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c17-engine-contract {:arglists '([])}
   'c17-plugin-governing-document {:kind :constant}
   'c17-plugin-diagnostic-ids {:kind :constant}
   'c17-plugin-manifest-required-fields {:kind :constant}
   'c17-plugin-pass-contract-required-fields {:kind :constant}
   'c17-plugin-cache-key-required-fields {:kind :constant}
   'c17-plugin-source-overrides {:arglists '([module])}
   'c17-plugin-fail! {:arglists '([id source-path subject extra])}
   'c17-plugin-validate-source-overrides! {:arglists '([source-path overrides])}
   'c17-plugin-diagnostic-stream
   {:arglists '([source-path plugin-manifest input-id])}
   'c17-plugin-validate! {:arglists '([source-path artifact])}
   'c17-plugin-capability-proof {:arglists '([artifact])}
   'compiler-c17-plugin-source-artifact
   {:arglists '([source-path source-text])}
   'compiler-c17-plugin-file-artifact {:arglists '([path])}})

(defn c17-engine-contract []
  (assoc namespace-contract :public-api public-api))
