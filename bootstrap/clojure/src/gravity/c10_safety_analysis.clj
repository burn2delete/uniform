(ns gravity.c10-safety-analysis
  "Hosted Stage0 C10 safety-analysis pipeline and artifact projection.

  This facade preserves the Clojure seed compatibility API and operation
  interposition while semantic implementations live in focused leaves."
  (:require [clojure.set :as set]
            [gravity.digest :as digest]
            [gravity.c10-safety-analysis.contract :as contract]
            [gravity.c10-safety-analysis.defaults :as defaults]
            [gravity.c10-safety-analysis.evidence :as evidence]
            [gravity.c10-safety-analysis.inventory :as inventory]
            [gravity.c10-safety-analysis.outcomes :as outcomes]
            [gravity.c10-safety-analysis.pipeline :as pipeline]
            [gravity.c10-safety-analysis.policy :as policy]
            [gravity.c10-safety-analysis.verification :as verification]))

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

(defn- default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))
(defn- default-source-span [path index] {:source path :form-index index})
(defn- default-c4-artifact-id [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))
(def ^:private unsupported-host-operation policy/unsupported-host-operation)
(defn- op-fn [key fallback] (or (get *operations* key) fallback))
(defn- fail! [id message data] ((op-fn :fail! default-fail!) id message data))
(defn- source-span [path index]
  ((op-fn :source-span default-source-span) path index))
(defn- c4-artifact-id [artifact]
  ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))
(defn- read-source-form-records [path text]
  ((op-fn :read-source-form-records
          (unsupported-host-operation :read-source-form-records)) path text))
(defn- validate-ns-syntax! [path forms]
  ((op-fn :validate-ns-syntax!
          (unsupported-host-operation :validate-ns-syntax!)) path forms))
(defn- parse-module [path forms]
  ((op-fn :parse-module (unsupported-host-operation :parse-module)) path forms))
(defn- compiler-c9-ownership-source-artifact [path text]
  ((op-fn :compiler-c9-ownership-source-artifact
          (unsupported-host-operation :compiler-c9-ownership-source-artifact))
   path text))

(def ^:dynamic c10-safety-diagnostic-ids defaults/diagnostic-ids)
(def ^:dynamic c10-safety-governing-document defaults/governing-document)
(def ^:dynamic c10-safety-rejected-designs defaults/rejected-designs)
(def ^:dynamic c10-safety-override-diagnostics defaults/override-diagnostics)
(def ^:dynamic c10-safe-outcomes defaults/safe-outcomes)

(definterposable c10-safety-source-overrides [module]
  (defaults/source-overrides module))
(definterposable c10-safety-message [id] (defaults/message id))
(definterposable c10-safety-fail! [id source-path subject extra]
  (defaults/fail! fail! c10-safety-message source-span
                  c10-safety-governing-document id source-path subject extra))
(definterposable c10-safety-validate-overrides! [source-path module overrides]
  (defaults/validate-overrides! c10-safety-fail! source-span
                                c10-safety-override-diagnostics
                                source-path module overrides))
(definterposable c10-safety-operation-inventory [module c9-artifact]
  (inventory/operation-inventory module c9-artifact))
(definterposable c10-safety-outcome-records [module inventory]
  (outcomes/outcome-records source-span module inventory))
(definterposable c10-runtime-check-list [module outcomes]
  (outcomes/runtime-check-list module outcomes))
(definterposable c10-proof-obligation-list [module outcomes]
  (evidence/proof-obligation-list module outcomes))
(definterposable c10-proof-certificate-references [module]
  (evidence/proof-certificate-references module))
(definterposable c10-unsafe-island-audit-manifest [module outcomes]
  (evidence/unsafe-island-audit-manifest module outcomes))
(definterposable c10-taint-capability-safety-report [module]
  (evidence/taint-capability-safety-report module))
(definterposable c10-generated-code-safety-provenance [module]
  (evidence/generated-code-safety-provenance source-span module))
(definterposable c10-optimization-safety-preservation [module]
  (evidence/optimization-safety-preservation module))
(definterposable c10-safety-diagnostics [source-path]
  (defaults/diagnostics source-span c10-safety-diagnostic-ids
                        c10-safety-rejected-designs source-path))
(definterposable c10-safety-verifier-report
  [c9-artifact inventory outcomes checks obligations certificates unsafe report
   generated optimization diagnostics]
  (verification/verifier-report c10-safe-outcomes c10-safety-diagnostic-ids
                                c9-artifact inventory outcomes checks obligations
                                certificates unsafe report generated optimization
                                diagnostics))
(definterposable c10-safety-capability-proof [artifact]
  (verification/capability-proof artifact))
(definterposable c10-safety-validate! [source-path artifact]
  (verification/validate! c10-safety-capability-proof c10-safety-fail!
                          source-path artifact))

(defn- pipeline-operations []
  {:read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :source-overrides c10-safety-source-overrides
   :validate-overrides! c10-safety-validate-overrides!
   :c9-artifact compiler-c9-ownership-source-artifact
   :operation-inventory c10-safety-operation-inventory
   :outcome-records c10-safety-outcome-records
   :runtime-check-list c10-runtime-check-list
   :proof-obligation-list c10-proof-obligation-list
   :proof-certificate-references c10-proof-certificate-references
   :unsafe-island-audit-manifest c10-unsafe-island-audit-manifest
   :taint-capability-safety-report c10-taint-capability-safety-report
   :generated-code-safety-provenance c10-generated-code-safety-provenance
   :optimization-safety-preservation c10-optimization-safety-preservation
   :safety-diagnostics c10-safety-diagnostics
   :verifier-report c10-safety-verifier-report
   :validate! c10-safety-validate!
   :capability-proof c10-safety-capability-proof
   :artifact-id c4-artifact-id})

(definterposable compiler-c10-safety-source-artifact [source-path source-text]
  (pipeline/source-artifact (pipeline-operations)
                            c10-safety-diagnostic-ids
                            c10-safety-governing-document
                            source-path source-text))
(definterposable compiler-c10-safety-file-artifact [path]
  (compiler-c10-safety-source-artifact path (slurp path)))

(def ^:private namespace-contract contract/namespace-contract)

(defn with-operations [operations thunk]
  (policy/validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              c10-safety-diagnostic-ids
              (get merged :c10-safety-diagnostic-ids c10-safety-diagnostic-ids)
              c10-safety-governing-document
              (get merged :c10-safety-governing-document
                   c10-safety-governing-document)
              c10-safety-rejected-designs
              (get merged :c10-safety-rejected-designs c10-safety-rejected-designs)
              c10-safety-override-diagnostics
              (get merged :c10-safety-override-diagnostics
                   c10-safety-override-diagnostics)
              c10-safe-outcomes
              (get merged :c10-safe-outcomes c10-safe-outcomes)]
      (thunk))))

(def public-api contract/public-api)

(defn c10-engine-contract []
  (assoc namespace-contract :public-api public-api))
