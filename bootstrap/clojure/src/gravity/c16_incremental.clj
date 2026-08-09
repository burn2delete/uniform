(ns gravity.c16-incremental
  "Hosted Stage0 C16 incremental-compilation schema/evidence projection."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [gravity.compiler-verification-shared :as shared]
            [gravity.digest :as digest]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private function-operation-keys
  #{:fail! :source-span :sha256-hex :c4-artifact-id :perf-present?
    :read-source-form-records :validate-ns-syntax! :parse-module
    :compiler-c15-diagnostics-source-artifact
    :c16-incremental-source-overrides :c16-incremental-fail!
    :c16-incremental-validate-source-overrides! :c16-stage-cache-key
    :c16-incremental-diagnostic-stream :c16-incremental-validate!
    :c16-incremental-capability-proof
    :compiler-c16-incremental-source-artifact
    :compiler-c16-incremental-file-artifact})
(def ^:private scalar-operation-keys
  #{:compiler-verification-diagnostic-messages
    :compiler-verification-override-diagnostics
    :c16-incremental-governing-document
    :c16-incremental-diagnostic-ids
    :c16-cache-key-required-fields
    :c16-invalidation-causes})
(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))
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
(defn- unsupported [key]
  (fn [& _]
    (throw (ex-info (str "C16 leaf requires injected operation " key)
                    {:operation key}))))
(defn- op [key fallback] (or (get *operations* key) fallback))
(defn- fail! [id message data]
  ((op :fail! (fn [rule text payload]
                (throw (ex-info text (assoc (or payload {}) :id rule)))))
   id message data))
(defn- source-span [path index]
  ((op :source-span (fn [p i] {:source p :form-index i})) path index))
(defn- sha256-hex [value]
  ((op :sha256-hex digest/sha256-hex) value))
(defn- c4-artifact-id [value]
  ((op :c4-artifact-id
       (fn [candidate]
         (str "sha256:" (digest/sha256-hex (pr-str candidate)))))
   value))
(defn- perf-present? [value]
  ((op :perf-present?
       (fn [candidate]
         (and (some? candidate)
              (not (and (coll? candidate) (empty? candidate))))))
   value))
(defn- read-source-form-records [path text]
  ((op :read-source-form-records (unsupported :read-source-form-records))
   path text))
(defn- validate-ns-syntax! [path forms]
  ((op :validate-ns-syntax! (unsupported :validate-ns-syntax!)) path forms))
(defn- parse-module [path forms]
  ((op :parse-module (unsupported :parse-module)) path forms))
(defn- compiler-c15-diagnostics-source-artifact [path text]
  ((op :compiler-c15-diagnostics-source-artifact
       (unsupported :compiler-c15-diagnostics-source-artifact))
   path text))
(def ^:private ^:dynamic compiler-verification-diagnostic-messages
  shared/compiler-verification-diagnostic-messages)
(def ^:private ^:dynamic compiler-verification-override-diagnostics
  shared/compiler-verification-override-diagnostics)

(def ^:dynamic c16-incremental-governing-document
  "docs/phase-06-compiler-architecture/095-c16-incremental-compilation-design.md")

(def ^:dynamic c16-incremental-diagnostic-ids
  ["C16-KEY"
   "C16-ENTRY"
   "C16-STALE"
   "C16-PROOF"
   "C16-SPECULATIVE"
   "C16-REPLAY"
   "C16-POLICY"
   "C16-DIAGNOSTIC"
   "C16-GRAPH"])

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

(definterposable c16-incremental-source-overrides
  [module]
  (or (get-in module [:metadata :compiler :c16-incremental])
      (get-in module [:metadata :compiler :verification])
      {}))

(definterposable c16-incremental-fail!
  [id source-path subject extra]
  (fail! id
         (get compiler-verification-diagnostic-messages id
              "incremental compiler validation failed")
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :compiler-incremental
                 :stage (or (:stage subject) :c16-incremental-compilation)
                 :cache-key (:cache-key subject)
                 :artifact-id (:artifact-id subject)
                 :invalidating-input (:invalidating-input subject)
                 :profile (:profile subject)
                 :target (:target subject)
                 :remediation "Regenerate incremental graph, cache keys, cache entries, invalidation traces, replay records, and revalidation reports before reuse."}
                extra)))

(definterposable c16-incremental-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (let [[id subject-kind] (get compiler-verification-override-diagnostics
                                 fail-kind)]
      (when (contains? (set c16-incremental-diagnostic-ids) id)
        (c16-incremental-fail!
         id source-path
         {:stage subject-kind
          :cache-key (str "c16-invalid-cache-" (name fail-kind))
          :artifact-id (str "c16-cache-artifact-" (name fail-kind))
          :invalidating-input fail-kind
          :profile :hosted
          :target :jvm}
         {:missing-fields [fail-kind]})))))

(definterposable c16-stage-cache-key
  [stage source-hash dependency-hash]
  {:artifact :gravity/cache-key
   :stage stage
   :source source-hash
   :reader "sha256:c16-reader-options"
   :syntax "sha256:c16-syntax-stream"
   :macro-expansion "sha256:c16-macro-expansion"
   :namespace "sha256:c16-namespace-analysis"
   :profile "sha256:c16-profile-manifest"
   :target "sha256:c16-target-request"
   :compiler "sha256:gravity-stage0-clojure"
   :pass-contract (str "sha256:c16-pass-" (name stage))
   :dependencies dependency-hash
   :build-effects "sha256:c16-replay-record"
   :capabilities "sha256:c16-capability-policy"
   :language-facets "sha256:c16-facets"
   :policy "sha256:c16-policy"})

(definterposable c16-incremental-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/c16-incremental-diagnostic-stream
   :status :complete
   :stage :c16-incremental-compilation
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic id
            :cache-key (str "sha256:" (sha256-hex id))
            :artifact-id input-id
            :stage :c16-incremental-compilation
            :invalidating-input (keyword (str/lower-case
                                          (str/replace id #"C16-" "")))
            :source-span (source-span source-path 0)
            :profile :hosted
            :target :jvm
            :remediation (get compiler-verification-diagnostic-messages id)})
         c16-incremental-diagnostic-ids)})

(definterposable c16-incremental-validate!
  [source-path artifact]
  (let [required (set c16-cache-key-required-fields)
        stage-keys (:stage-cache-keys artifact)
        entries (:cache-entry-manifest artifact)
        invalidations (set (map :invalidating-input
                                (:invalidation-trace artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:incremental-diagnostic-stream
                                       :diagnostics])))]
    (doseq [cache-key stage-keys]
      (let [present (set (keys cache-key))]
        (when-not (every? #(perf-present? (get cache-key %)) required)
          (c16-incremental-fail! "C16-KEY" source-path cache-key
                                 {:missing-fields
                                  (vec (remove present required))}))))
    (doseq [entry entries]
      (when-not (and (= :gravity/cache-entry (:artifact entry))
                     (:cache-key entry)
                     (:artifact-id entry)
                     (:producer entry)
                     (seq (:inputs entry))
                     (seq (:preserved-facts entry))
                     (seq (:invalidated-by entry))
                     (:diagnostics entry)
                     (:provenance entry)
                     (:revalidation entry))
        (c16-incremental-fail! "C16-ENTRY" source-path entry
                               {:missing-fields [:cache-entry]})))
    (when-not (set/subset? (set c16-invalidation-causes) invalidations)
      (c16-incremental-fail! "C16-STALE" source-path
                             (first (:invalidation-trace artifact))
                             {:missing-fields [:invalidation-trace]}))
    (when-not (= :rejected (get-in artifact
                                   [:stale-proof-rejection-report :status]))
      (c16-incremental-fail! "C16-PROOF" source-path
                             (:stale-proof-rejection-report artifact)
                             {:missing-fields [:stale-proof-rejection]}))
    (when-not (= :blocked-from-release
                 (get-in artifact
                         [:speculative-reuse-record :publish-status]))
      (c16-incremental-fail! "C16-SPECULATIVE" source-path
                             (:speculative-reuse-record artifact)
                             {:missing-fields [:speculative-boundary]}))
    (when-not (= :complete (get-in artifact
                                   [:build-effect-replay-record :status]))
      (c16-incremental-fail! "C16-REPLAY" source-path
                             (:build-effect-replay-record artifact)
                             {:missing-fields [:build-effect-replay]}))
    (when-not (= :compatible (get-in artifact
                                     [:policy-compatibility-report :status]))
      (c16-incremental-fail! "C16-POLICY" source-path
                             (:policy-compatibility-report artifact)
                             {:missing-fields [:policy-compatibility]}))
    (when-not (= :rejected (get-in artifact
                                   [:stale-diagnostic-rejection-report
                                    :status]))
      (c16-incremental-fail! "C16-DIAGNOSTIC" source-path
                             (:stale-diagnostic-rejection-report artifact)
                             {:missing-fields [:diagnostic-revalidation]}))
    (when-not (= :consistent (get-in artifact
                                     [:incremental-dependency-graph
                                      :status]))
      (c16-incremental-fail! "C16-GRAPH" source-path
                             (:incremental-dependency-graph artifact)
                             {:missing-fields [:incremental-graph]}))
    (when-not (= (set c16-incremental-diagnostic-ids) diagnostics)
      (c16-incremental-fail! "C16-GRAPH" source-path
                             (:incremental-diagnostic-stream artifact)
                             {:missing-fields [:incremental-diagnostics]})))
  :complete)

(definterposable c16-incremental-capability-proof
  [artifact]
  {:c15-diagnostics-input-verified?
   (= :complete (get-in artifact
                        [:c15-diagnostics-artifact
                         :capability-based-proof :status]))
   :dependency-graph-consistent?
   (= :consistent (get-in artifact
                          [:incremental-dependency-graph :status]))
   :stage-cache-keys-complete?
   (every? (fn [cache-key]
             (every? #(perf-present? (get cache-key %))
                     c16-cache-key-required-fields))
           (:stage-cache-keys artifact))
   :cache-entries-retain-provenance?
   (every? #(and (= :gravity/cache-entry (:artifact %))
                 (:diagnostics %)
                 (:provenance %)
                 (:revalidation %))
           (:cache-entry-manifest artifact))
   :invalidations-cover-semantic-policy-proof-target?
   (set/subset? (set c16-invalidation-causes)
                (set (map :invalidating-input
                          (:invalidation-trace artifact))))
   :stale-proof-rejected?
   (= :rejected (get-in artifact
                        [:stale-proof-rejection-report :status]))
   :stale-diagnostics-rejected?
   (= :rejected (get-in artifact
                        [:stale-diagnostic-rejection-report :status]))
   :speculative-reuse-blocked-from-release?
   (= :blocked-from-release
      (get-in artifact [:speculative-reuse-record :publish-status]))
   :build-effect-replay-recorded?
   (= :complete (get-in artifact [:build-effect-replay-record :status]))
   :revalidation-passed?
   (= :passed (get-in artifact [:revalidation-report :status]))
   :release-rebuild-reproducible?
   (= :reproducible (get-in artifact
                            [:release-rebuild-record :status]))
   :diagnostics-covered?
   (= (set c16-incremental-diagnostic-ids)
      (set (map :diagnostic
                (get-in artifact
                        [:incremental-diagnostic-stream :diagnostics]))))
   :status :complete})

(definterposable compiler-c16-incremental-source-artifact
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (c16-incremental-source-overrides module)
        _ (c16-incremental-validate-source-overrides! source-path
                                                      source-overrides)
        diagnostics-artifact (compiler-c15-diagnostics-source-artifact
                              source-path source-text)
        input-id (:artifact-id diagnostics-artifact)
        source-hash (str "sha256:" (sha256-hex source-text))
        dependency-hash (str "sha256:" (sha256-hex (pr-str input-id)))
        stage-cache-keys
        (mapv #(c16-stage-cache-key % source-hash dependency-hash)
              [:reader :macro-expansion :type-check :effect-check
               :safety-analysis :mir :diagnostics :target-artifact])
        cache-entry-manifest
        (mapv (fn [cache-key]
                {:artifact :gravity/cache-entry
                 :stage (:stage cache-key)
                 :cache-key (str "sha256:" (sha256-hex (pr-str cache-key)))
                 :artifact-id input-id
                 :producer {:stage (:stage cache-key)
                            :pass-version "stage0-c16"}
                 :inputs [input-id source-hash dependency-hash]
                 :preserved-facts #{:source-spans :origin-chain :diagnostics
                                    :proofs :profile :target}
                 :invalidated-by #{:source-change :macro-change
                                   :profile-change :target-change
                                   :diagnostic-schema-change}
                 :diagnostics :gravity/c16-incremental-diagnostic-stream
                 :provenance :gravity/incremental-dependency-graph
                 :trust :local-build
                 :revalidation :required-before-release})
              stage-cache-keys)
        invalidation-trace
        (mapv (fn [cause]
                {:invalidating-input cause
                 :affected-nodes [:diagnostics :target-artifact :proofs]
                 :downstream-revalidation-stages
                 [:reader :macro-expansion :type-check :effect-check
                  :safety-analysis :mir :diagnostics :target-artifact]
                 :status :recorded})
              c16-invalidation-causes)
        diagnostic-stream (c16-incremental-diagnostic-stream source-path
                                                            input-id)
        artifact-base
        {:kind :gravity/stage0-c16-incremental-compilation-artifact
         :task "P06-D095"
         :document-set ["C16"]
         :governing-document c16-incremental-governing-document
         :pass {:name :c16-incremental-compilation
                :input :diagnostic-artifact-bundle
                :output :incremental-compilation-artifact
                :requires [:c15-diagnostics :source :compiler :profile
                           :target :dependencies :build-effects
                           :capabilities :policy :proofs]
                :preserves [:source-spans :origin-chain :diagnostics
                            :provenance :proofs :profile :target]
                :emits [:incremental-dependency-graph :cache-key-schema
                        :stage-cache-keys :cache-entry-manifest
                        :invalidation-trace :artifact-reuse-report
                        :revalidation-report :stale-proof-rejection-report
                        :stale-diagnostic-rejection-report
                        :build-effect-replay-record
                        :speculative-reuse-record
                        :release-rebuild-record
                        :incremental-diagnostic-stream]
                :rejects c16-incremental-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c15-diagnostics-artifact
         (select-keys diagnostics-artifact
                      [:kind :task :artifact-id :governing-document
                       :diagnostic-stream :capability-based-proof])
         :diagnostics-artifact-kind (:kind diagnostics-artifact)
         :diagnostics-artifact-hash input-id
         :incremental-dependency-graph
         {:artifact :gravity/incremental-dependency-graph
          :status :consistent
          :nodes [:source-unit :syntax-object-stream :macro-expansion-trace
                  :namespace-analysis :typed-core :effect-graph
                  :ownership-graph :safety-outcomes :mir-module
                  :optimization-decisions :domain-ir-artifacts
                  :target-artifacts :diagnostics :proofs-and-certificates
                  :package-provider-manifests]
          :edges [{:from :source-unit :to :syntax-object-stream
                   :field :source}
                  {:from :syntax-object-stream :to :macro-expansion-trace
                   :field :syntax}
                  {:from :macro-expansion-trace :to :typed-core
                   :field :macro-expansion}
                  {:from :typed-core :to :effect-graph
                   :field :type-facts}
                  {:from :effect-graph :to :safety-outcomes
                   :field :effects}
                  {:from :safety-outcomes :to :mir-module
                   :field :safety}
                  {:from :mir-module :to :domain-ir-artifacts
                   :field :mir}
                  {:from :domain-ir-artifacts :to :target-artifacts
                   :field :lowering}
                  {:from :diagnostics :to :target-artifacts
                   :field :diagnostic-schema}
                  {:from :proofs-and-certificates :to :target-artifacts
                   :field :proof-policy}]}
         :cache-key-schema
         {:artifact :gravity/cache-key-schema
          :status :complete
          :required-fields c16-cache-key-required-fields}
         :stage-cache-keys stage-cache-keys
         :cache-entry-manifest cache-entry-manifest
         :invalidation-trace invalidation-trace
         :artifact-reuse-report
         {:artifact :gravity/artifact-reuse-report
          :status :validated
          :unchanged-source-reuse :allowed
          :changed-policy-reuse :rejected
          :release-boundary :requires-full-revalidation}
         :revalidation-report
         {:artifact :gravity/revalidation-report
          :status :passed
          :checks [:cache-key :artifact-schema-version :producer-pass-version
                   :preserved-facts :proof-freshness
                   :profile-target-compatibility
                   :diagnostic-schema-compatibility
                   :dependency-graph-compatibility]}
         :stale-proof-rejection-report
         {:artifact :gravity/stale-proof-rejection-report
          :status :rejected
          :diagnostic "C16-PROOF"
          :reason :proof-inputs-or-policy-changed}
         :stale-diagnostic-rejection-report
         {:artifact :gravity/stale-diagnostic-rejection-report
          :status :rejected
          :diagnostic "C16-DIAGNOSTIC"
          :reason :origin-spans-or-facts-changed}
         :build-effect-replay-record
         {:artifact :gravity/build-effect-replay-record
          :status :complete
          :replay-hash "sha256:c16-replay-record"
          :build-effects #{:build/read-file}
          :hermetic? true}
         :policy-compatibility-report
         {:artifact :gravity/cache-policy-compatibility-report
          :status :compatible
          :profile :hosted
          :target :jvm
          :capabilities (:capabilities module)
          :safety :safe}
         :speculative-reuse-record
         {:artifact :gravity/speculative-cache-reuse
          :reuse :speculative
          :interactive-build? true
          :publish-status :blocked-from-release
          :revalidation :required}
         :release-rebuild-record
         {:artifact :gravity/reproducible-release-rebuild
          :status :reproducible
          :recorded-inputs [source-hash dependency-hash]
          :environment "sha256:c16-hermetic-stage0"}
         :incremental-diagnostic-stream diagnostic-stream
         :c16-incremental-results
         {:documents ["C16"]
          :task "P06-D095"
          :required-diagnostic-ids c16-incremental-diagnostic-ids
          :c15-input-status :complete
          :dependency-graph-status :complete
          :cache-key-status :complete
          :cache-entry-status :complete
          :invalidation-status :complete
          :reuse-status :complete
          :revalidation-status :complete
          :stale-proof-status :complete
          :stale-diagnostic-status :complete
          :speculative-status :complete
          :replay-status :complete
          :release-rebuild-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (c16-incremental-validate! source-path artifact-base)
        capability-proof (c16-incremental-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(definterposable compiler-c16-incremental-file-artifact
  [path]
  (compiler-c16-incremental-source-artifact path (slurp path)))

(def ^:private namespace-contract
  {:contract-boundary :hosted-stage0-c16-incremental-evidence
   :dependency-direction
   {:requires ['clojure.set 'clojure.string
               'gravity.compiler-verification-shared 'gravity.digest]
    :forbids ['gravity.bootstrap 'gravity.diagnostics
              'gravity.c2-pass-cache]}
   :owns [:hosted-stage0-c16-cache-schema
          :hosted-stage0-c16-invalidation-evidence]
   :does-not-own [:canonical-c16-authority :source-authentication
                  :content-addressed-pass-cache :cache-storage
                  :cache-lookup :cache-publication :actual-artifact-reuse
                  :proof-freshness-authority :release-reproducibility-proof
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :cache-implementation? false
   :incremental-model-complete? false
   :canonical-c16-authority? false
   :operation-interposition
   {:accepted-keys operation-keys
    :unknown-keys-rejected? true
    :partial-overrides? true
    :single-binding-per-top-level-call? true}})
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
  (when-not (map? operations)
    (throw (ex-info "C16 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[key value]
                           (select-keys operations function-operation-keys)
                           :when (not (fn? value))]
                       key))]
    (when unknown
      (throw (ex-info "C16 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)})))
    (when invalid
      (throw (ex-info "C16 function operations must be functions"
                      {:non-function-keys (vec invalid)}))))
  (doseq [[key predicate]
          [[:compiler-verification-diagnostic-messages string-map?]
           [:compiler-verification-override-diagnostics override-map?]
           [:c16-incremental-governing-document
            #(and (string? %) (seq %))]
           [:c16-incremental-diagnostic-ids string-vector?]
           [:c16-cache-key-required-fields keyword-vector?]
           [:c16-invalidation-causes keyword-vector?]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C16 scalar operation has invalid shape" {:key key})))
  operations)
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
              c16-incremental-governing-document
              (get merged :c16-incremental-governing-document
                   c16-incremental-governing-document)
              c16-incremental-diagnostic-ids
              (get merged :c16-incremental-diagnostic-ids
                   c16-incremental-diagnostic-ids)
              c16-cache-key-required-fields
              (get merged :c16-cache-key-required-fields
                   c16-cache-key-required-fields)
              c16-invalidation-causes
              (get merged :c16-invalidation-causes c16-invalidation-causes)]
      (thunk))))
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
   'c16-incremental-validate-source-overrides!
   {:arglists '([source-path overrides])}
   'c16-stage-cache-key
   {:arglists '([stage source-hash dependency-hash])}
   'c16-incremental-diagnostic-stream
   {:arglists '([source-path input-id])}
   'c16-incremental-validate! {:arglists '([source-path artifact])}
   'c16-incremental-capability-proof {:arglists '([artifact])}
   'compiler-c16-incremental-source-artifact
   {:arglists '([source-path source-text])}
   'compiler-c16-incremental-file-artifact {:arglists '([path])}})
(defn c16-engine-contract []
  (assoc namespace-contract :public-api public-api))
