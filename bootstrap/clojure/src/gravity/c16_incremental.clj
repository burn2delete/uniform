(ns gravity.c16-incremental
  "Hosted Stage0 C16 incremental-compilation schema/evidence projection."
  (:require [gravity.c16-incremental.artifact :as artifact]
            [gravity.c16-incremental.operations :as operations]
            [gravity.c16-incremental.policy :as policy]
            [gravity.c16-incremental.projection :as projection]
            [gravity.c16-incremental.proof :as proof]
            [gravity.c16-incremental.validation :as validation]
            [gravity.compiler-verification-shared :as shared]
            [gravity.digest :as digest]))

(def ^:private ^:dynamic compiler-verification-diagnostic-messages
  shared/compiler-verification-diagnostic-messages)

(def ^:private ^:dynamic compiler-verification-override-diagnostics
  shared/compiler-verification-override-diagnostics)

(def ^:dynamic c16-incremental-governing-document
  "docs/phase-06-compiler-architecture/095-c16-incremental-compilation-design.md")

(def ^:dynamic c16-incremental-diagnostic-ids
  ["C16-KEY" "C16-ENTRY" "C16-STALE" "C16-PROOF" "C16-SPECULATIVE"
   "C16-REPLAY" "C16-POLICY" "C16-DIAGNOSTIC" "C16-GRAPH"])

(def ^:dynamic c16-cache-key-required-fields
  [:stage :source :reader :syntax :macro-expansion :namespace :profile
   :target :compiler :pass-contract :dependencies :build-effects
   :capabilities :language-facets :policy])

(def ^:dynamic c16-invalidation-causes
  [:source-change :reader-option-change :reader-extension-change
   :macro-change :build-grant-change :namespace-change
   :package-dependency-change :type-rule-change :effect-registry-change
   :capability-policy-change :profile-manifest-change :safety-rule-change
   :proof-provider-change :optimization-pass-change :target-feature-change
   :backend-change :runtime-provider-change :facet-set-change
   :diagnostic-schema-change])

(declare c16-incremental-source-overrides
         c16-incremental-fail!
         c16-incremental-validate-source-overrides!
         c16-stage-cache-key
         c16-incremental-diagnostic-stream
         c16-incremental-validate!
         c16-incremental-capability-proof
         compiler-c16-incremental-source-artifact)

(defn- default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))

(defn- source-span [path index]
  (operations/invoke :source-span
                     (fn [source-path form-index]
                       {:source source-path :form-index form-index})
                     path index))

(defn- sha256-hex [value]
  (operations/invoke :sha256-hex digest/sha256-hex value))

(defn- c4-artifact-id [value]
  (operations/invoke :c4-artifact-id
                     (fn [candidate]
                       (str "sha256:" (digest/sha256-hex (pr-str candidate))))
                     value))

(defn- perf-present? [value]
  (operations/invoke :perf-present?
                     (fn [candidate]
                       (and (some? candidate)
                            (not (and (coll? candidate) (empty? candidate)))))
                     value))

(defn- configuration []
  {:diagnostic-messages compiler-verification-diagnostic-messages
   :override-diagnostics compiler-verification-override-diagnostics
   :governing-document c16-incremental-governing-document
   :diagnostic-ids c16-incremental-diagnostic-ids
   :cache-key-required-fields c16-cache-key-required-fields
   :invalidation-causes c16-invalidation-causes
   :fail (fn [id message data]
           (operations/invoke :fail! default-fail! id message data))
   :source-span source-span
   :sha256-hex sha256-hex
   :c4-artifact-id c4-artifact-id
   :perf-present? perf-present?
   :read-source-form-records
   (fn [path text]
     (operations/invoke :read-source-form-records
                        (policy/unsupported :read-source-form-records)
                        path text))
   :validate-ns-syntax!
   (fn [path forms]
     (operations/invoke :validate-ns-syntax!
                        (policy/unsupported :validate-ns-syntax!)
                        path forms))
   :parse-module
   (fn [path forms]
     (operations/invoke :parse-module
                        (policy/unsupported :parse-module)
                        path forms))
   :c15-diagnostics-artifact
   (fn [path text]
     (operations/invoke :compiler-c15-diagnostics-source-artifact
                        (policy/unsupported
                         :compiler-c15-diagnostics-source-artifact)
                        path text))
   :source-overrides c16-incremental-source-overrides
   :incremental-fail! c16-incremental-fail!
   :validate-source-overrides! c16-incremental-validate-source-overrides!
   :stage-cache-key c16-stage-cache-key
   :diagnostic-stream c16-incremental-diagnostic-stream
   :validate! c16-incremental-validate!
   :capability-proof c16-incremental-capability-proof})

(defn c16-incremental-source-overrides [module]
  (operations/invoke :c16-incremental-source-overrides
                     projection/source-overrides module))

(defn c16-incremental-fail! [id source-path subject extra]
  (operations/invoke :c16-incremental-fail!
                     (fn [rule path value details]
                       (validation/fail! (configuration)
                                         rule path value details))
                     id source-path subject extra))

(defn c16-incremental-validate-source-overrides! [source-path overrides]
  (operations/invoke :c16-incremental-validate-source-overrides!
                     (fn [path values]
                       (validation/validate-source-overrides!
                        (configuration) path values))
                     source-path overrides))

(defn c16-stage-cache-key [stage source-hash dependency-hash]
  (operations/invoke :c16-stage-cache-key projection/stage-cache-key
                     stage source-hash dependency-hash))

(defn c16-incremental-diagnostic-stream [source-path input-id]
  (operations/invoke :c16-incremental-diagnostic-stream
                     (fn [path artifact-id]
                       (projection/diagnostic-stream (configuration)
                                                     path artifact-id))
                     source-path input-id))

(defn c16-incremental-validate! [source-path artifact]
  (operations/invoke :c16-incremental-validate!
                     (fn [path value]
                       (validation/validate! (configuration) path value))
                     source-path artifact))

(defn c16-incremental-capability-proof [artifact]
  (operations/invoke :c16-incremental-capability-proof
                     (fn [value]
                       (proof/capability-proof (configuration) value))
                     artifact))

(defn compiler-c16-incremental-source-artifact [source-path source-text]
  (operations/invoke :compiler-c16-incremental-source-artifact
                     (fn [path text]
                       (artifact/source-artifact (configuration) path text))
                     source-path source-text))

(defn compiler-c16-incremental-file-artifact [path]
  (operations/invoke :compiler-c16-incremental-file-artifact
                     (fn [source-path]
                       (artifact/file-artifact
                        compiler-c16-incremental-source-artifact source-path))
                     path))

(defn with-operations [operations thunk]
  (policy/validate-operations! operations)
  (let [merged (merge (gravity.c16-incremental.operations/current-operations)
                      operations)]
    (binding [compiler-verification-diagnostic-messages
              (get merged :compiler-verification-diagnostic-messages compiler-verification-diagnostic-messages)
              compiler-verification-override-diagnostics
              (get merged :compiler-verification-override-diagnostics compiler-verification-override-diagnostics)
              c16-incremental-governing-document
              (get merged :c16-incremental-governing-document c16-incremental-governing-document)
              c16-incremental-diagnostic-ids
              (get merged :c16-incremental-diagnostic-ids c16-incremental-diagnostic-ids)
              c16-cache-key-required-fields
              (get merged :c16-cache-key-required-fields c16-cache-key-required-fields)
              c16-invalidation-causes
              (get merged :c16-invalidation-causes c16-invalidation-causes)]
      (gravity.c16-incremental.operations/with-operations operations thunk))))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c16-engine-contract {:arglists '([])}
   'c16-incremental-governing-document {:kind :constant}
   'c16-incremental-diagnostic-ids {:kind :constant}
   'c16-cache-key-required-fields {:kind :constant}
   'c16-invalidation-causes {:kind :constant}
   'c16-incremental-source-overrides {:arglists '([module])}
   'c16-incremental-fail! {:arglists '([id source-path subject extra])}
   'c16-incremental-validate-source-overrides! {:arglists '([source-path overrides])}
   'c16-stage-cache-key {:arglists '([stage source-hash dependency-hash])}
   'c16-incremental-diagnostic-stream {:arglists '([source-path input-id])}
   'c16-incremental-validate! {:arglists '([source-path artifact])}
   'c16-incremental-capability-proof {:arglists '([artifact])}
   'compiler-c16-incremental-source-artifact {:arglists '([source-path source-text])}
   'compiler-c16-incremental-file-artifact {:arglists '([path])}})

(defn c16-engine-contract []
  (policy/engine-contract public-api))
