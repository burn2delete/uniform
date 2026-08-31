(ns gravity.c8-effect-checker
  "Hosted Stage0 C8 effect-analysis facade with operation interposition."
  (:require [clojure.set :as set]
            [gravity.digest :as digest]
            [gravity.c8-effect-checker.artifact :as artifact]
            [gravity.c8-effect-checker.catalog :as catalog]
            [gravity.c8-effect-checker.contract :as contract]
            [gravity.c8-effect-checker.diagnostics :as diagnostics]
            [gravity.c8-effect-checker.evidence :as evidence]
            [gravity.c8-effect-checker.facts :as facts]
            [gravity.c8-effect-checker.policy :as policy]
            [gravity.c8-effect-checker.verification :as verification]))

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
         (binding [*active-operation-keys*
                   (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))

(defn- default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))
(defn- default-source-span [path index]
  {:source path :form-index index})
(defn- default-c4-artifact-id [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))
(defn- unsupported-host-operation [operation]
  (policy/unsupported-host-operation operation))
(defn- op-fn [key fallback] (or (get *operations* key) fallback))
(defn- fail! [id message data]
  ((op-fn :fail! default-fail!) id message data))
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
  ((op-fn :parse-module
          (unsupported-host-operation :parse-module)) path forms))
(defn- compiler-c7-type-source-artifact [path text]
  ((op-fn :compiler-c7-type-source-artifact
          (unsupported-host-operation :compiler-c7-type-source-artifact))
   path text))

(def ^:dynamic c8-effect-diagnostic-ids catalog/diagnostic-ids)
(def ^:dynamic c8-effect-governing-document catalog/governing-document)
(def ^:dynamic c8-effect-rejected-designs catalog/rejected-designs)
(def ^:dynamic c8-effect-override-diagnostics catalog/override-diagnostics)
(def ^:dynamic c8-known-effects catalog/known-effects)
(def ^:dynamic c8-effect-capability catalog/effect-capability)
(def ^:dynamic c8-replay-sensitive-effects catalog/replay-sensitive-effects)

(definterposable c8-effect-source-overrides [module]
  (diagnostics/source-overrides module))
(definterposable c8-effect-message [id]
  (catalog/effect-message id))
(definterposable c8-effect-fail! [id source-path subject extra]
  (diagnostics/fail! fail! c8-effect-message source-span
                     c8-effect-governing-document
                     id source-path subject extra))
(definterposable c8-effect-validate-overrides! [source-path module overrides]
  (diagnostics/validate-overrides!
   c8-effect-fail! source-span c8-effect-override-diagnostics
   c8-effect-capability source-path module overrides))

(definterposable c8-fact-direct-effects [fact]
  (facts/fact-direct-effects fact))
(definterposable c8-effectful-facts [type-facts]
  ;; Capture an override before returning the legacy lazy sequence.
  (let [direct-effects (or (current-operation :c8-fact-direct-effects)
                           c8-fact-direct-effects)]
    (facts/effectful-facts direct-effects type-facts)))
(definterposable c8-effect-graph [module type-facts functions]
  (facts/effect-graph c8-effectful-facts c8-fact-direct-effects
                      c8-replay-sensitive-effects module type-facts functions))

(definterposable c8-legality-records [module effect-graph]
  (evidence/legality-records c8-effect-capability module effect-graph))
(definterposable c8-capability-proof-records [module effect-graph]
  (evidence/capability-proof-records c8-effect-capability module effect-graph))
(definterposable c8-build-effect-log [module]
  (evidence/build-effect-log c8-effect-capability module))
(definterposable c8-replay-requirements [effect-graph]
  (evidence/replay-requirements effect-graph))
(definterposable c8-ordering-constraints [effect-graph]
  (evidence/ordering-constraints effect-graph))
(definterposable c8-residual-effect-report [effect-graph]
  (evidence/residual-effect-report effect-graph))
(definterposable c8-effect-diagnostics [source-path type-facts]
  (diagnostics/effect-diagnostics
   source-span c8-effect-diagnostic-ids c8-effect-rejected-designs
   c8-effect-capability source-path type-facts))

(definterposable c8-effect-verifier-report
  [module effect-graph legality capability-proof build-log replay ordering
   residual diagnostics]
  (verification/verifier-report
   c8-known-effects c8-effect-diagnostic-ids module effect-graph legality
   capability-proof build-log replay ordering residual diagnostics))
(definterposable c8-effect-capability-proof [artifact]
  (verification/capability-proof artifact))
(definterposable c8-effect-validate! [source-path artifact]
  (verification/validate! c8-effect-capability-proof c8-effect-fail!
                          source-path artifact))

(defn- artifact-operations []
  {:read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :effect-source-overrides c8-effect-source-overrides
   :effect-validate-overrides! c8-effect-validate-overrides!
   :c7-type-source-artifact compiler-c7-type-source-artifact
   :effect-graph c8-effect-graph
   :legality-records c8-legality-records
   :capability-proof-records c8-capability-proof-records
   :build-effect-log c8-build-effect-log
   :replay-requirements c8-replay-requirements
   :ordering-constraints c8-ordering-constraints
   :residual-effect-report c8-residual-effect-report
   :effect-diagnostics c8-effect-diagnostics
   :effect-verifier-report c8-effect-verifier-report
   :effect-validate! c8-effect-validate!
   :effect-capability-proof c8-effect-capability-proof
   :artifact-id c4-artifact-id
   :diagnostic-ids c8-effect-diagnostic-ids
   :governing-document c8-effect-governing-document})

(definterposable compiler-c8-effect-source-artifact [source-path source-text]
  (artifact/source-artifact (artifact-operations) source-path source-text))
(definterposable compiler-c8-effect-file-artifact [path]
  (artifact/file-artifact compiler-c8-effect-source-artifact path))

(def ^:private namespace-contract
  (assoc contract/namespace-contract
         :operation-interposition
         {:accepted-keys operation-keys
          :unknown-keys-rejected? true
          :partial-overrides? true
          :single-binding-per-top-level-call? true}))

;; Stable private shape predicates remain available to focused compatibility
;; checks even though the policy leaf owns operation-map validation.
(defn- keyword-set? [value]
  (and (set? value) (seq value) (every? keyword? value)))
(defn- string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- vector-of-maps? [value]
  (and (vector? value) (every? map? value)))
(defn- keyword-string-map? [value]
  (and (map? value)
       (every? (fn [[key item]]
                 (and (keyword? key) (string? item))) value)))
(defn- keyword-keyword-map? [value]
  (and (map? value)
       (every? (fn [[key item]]
                 (and (keyword? key) (keyword? item))) value)))
(defn- validate-operations! [operations]
  (policy/validate-operations! operations))

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              c8-effect-diagnostic-ids
              (get merged :c8-effect-diagnostic-ids c8-effect-diagnostic-ids)
              c8-effect-governing-document
              (get merged :c8-effect-governing-document
                   c8-effect-governing-document)
              c8-effect-rejected-designs
              (get merged :c8-effect-rejected-designs c8-effect-rejected-designs)
              c8-effect-override-diagnostics
              (get merged :c8-effect-override-diagnostics
                   c8-effect-override-diagnostics)
              c8-known-effects
              (get merged :c8-known-effects c8-known-effects)
              c8-effect-capability
              (get merged :c8-effect-capability c8-effect-capability)
              c8-replay-sensitive-effects
              (get merged :c8-replay-sensitive-effects
                   c8-replay-sensitive-effects)]
      (thunk))))

(def public-api contract/public-api)

(defn c8-engine-contract []
  (assoc namespace-contract :public-api public-api))
