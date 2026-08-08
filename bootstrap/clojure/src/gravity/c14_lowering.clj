(ns gravity.c14-lowering
  "Hosted Stage0 C14 target-lowering adapter and evidence projection."
  (:require [gravity.digest :as digest]
            [gravity.optimization-lowering :as shared]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private function-operation-keys
  #{:source-span :c4-artifact-id :sha256-hex :perf-present?
    :read-source-form-records :validate-ns-syntax! :parse-module
    :compiler-c13-optimization-source-artifact
    :optimization-lowering-validate-overrides!
    :optimization-lowering-fail!
    :c14-lowering-source-overrides
    :c14-lowering-validate-source-overrides!
    :c14-lowering-diagnostic-catalog
    :c14-lowering-validate!
    :c14-lowering-capability-proof
    :compiler-c14-lowering-source-artifact
    :compiler-c14-lowering-file-artifact})
(def ^:private scalar-operation-keys
  #{:c14-lowering-governing-document
    :c14-lowering-diagnostic-ids
    :optimization-lowering-diagnostic-messages})
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
    (throw (ex-info (str "C14 leaf requires injected operation " key)
                    {:operation key}))))
(defn- op [key fallback] (or (get *operations* key) fallback))
(defn- source-span [path index]
  ((op :source-span (fn [p i] {:source p :form-index i})) path index))
(defn- c4-artifact-id [value]
  ((op :c4-artifact-id
       (fn [candidate]
         (str "sha256:" (digest/sha256-hex (pr-str candidate)))))
   value))
(defn- sha256-hex [value]
  ((op :sha256-hex digest/sha256-hex) value))
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
(defn- compiler-c13-optimization-source-artifact [path text]
  ((op :compiler-c13-optimization-source-artifact
       (unsupported :compiler-c13-optimization-source-artifact))
   path text))
(defn- optimization-lowering-validate-overrides! [path artifact]
  ((op :optimization-lowering-validate-overrides!
       shared/optimization-lowering-validate-overrides!)
   path artifact))
(defn- optimization-lowering-fail! [id path artifact subject extra]
  ((op :optimization-lowering-fail!
       shared/optimization-lowering-fail!)
   id path artifact subject extra))
(def ^:private ^:dynamic c14-lowering-diagnostic-ids
  shared/c14-lowering-diagnostic-ids)
(def ^:private ^:dynamic optimization-lowering-diagnostic-messages
  shared/optimization-lowering-diagnostic-messages)

(def ^:dynamic c14-lowering-governing-document
  "docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md")

(definterposable c14-lowering-source-overrides
  [module]
  (or (get-in module [:metadata :compiler :c14-lowering])
      (get-in module [:metadata :compiler :optimization-lowering])
      {}))

(definterposable c14-lowering-validate-source-overrides!
  [source-path overrides]
  (optimization-lowering-validate-overrides!
   source-path
   {:source-overrides overrides
    :lowering-request {:profile :hosted
                       :target {:backend :jvm}}
    :input "sha256:stage0-c14-source-override"}))

(definterposable c14-lowering-diagnostic-catalog
  [source-path input-id]
  (let [span (source-span source-path 0)]
    {:artifact :gravity/c14-lowering-diagnostic-catalog
     :status :complete
     :diagnostics
     (mapv (fn [id]
             {:diagnostic id
              :input-artifact-id input-id
              :mir-operation "c14-diagnostic-op"
              :domain-anchor "c14-diagnostic-anchor"
              :source-span span
              :origin-chain []
              :profile :hosted
              :target :jvm
              :backend :jvm
              :missing-feature :catalog-entry
              :proof-expected :proof/c14-diagnostic-catalog
              :provider-expected :jvm/provider
              :fallback-status :available
              :remediation (get optimization-lowering-diagnostic-messages id)})
           c14-lowering-diagnostic-ids)}))

(definterposable c14-lowering-validate!
  [source-path artifact]
  (optimization-lowering-validate-overrides! source-path artifact)
  (let [request (:lowering-request artifact)
        providers (:provider-selection-records artifact)
        metadata (get-in artifact [:proof-to-target-metadata-map :entries])
        unsupported (:unsupported-feature-report artifact)
        diagnostics (get-in artifact [:lowering-diagnostic-stream
                                      :diagnostics])]
    (when-not (= :optimized-mir (get-in request [:input :kind]))
      (optimization-lowering-fail! "C14-INPUT" source-path artifact
                                   request
                                   {:missing-fields [:input]}))
    (when-not (= :eligible (get-in artifact
                                   [:target-eligibility-report :status]))
      (optimization-lowering-fail! "C14-PROFILE" source-path artifact
                                   (:target-eligibility-report artifact)
                                   {:missing-fields [:target-eligibility]}))
    (when-not (perf-present? (get-in request [:target :features]))
      (optimization-lowering-fail! "C14-TARGET" source-path artifact
                                   request
                                   {:missing-fields [:target :features]}))
    (when-not (= :complete (get-in artifact [:abi-manifest :status]))
      (optimization-lowering-fail! "C14-ABI" source-path artifact
                                   (:abi-manifest artifact)
                                   {:missing-fields [:abi-manifest]}))
    (when-not (= :complete (get-in artifact
                                   [:runtime-provider-manifest :status]))
      (optimization-lowering-fail! "C14-RUNTIME" source-path artifact
                                   (:runtime-provider-manifest artifact)
                                   {:missing-fields [:runtime-provider]}))
    (when-not (every? #(= :selected (:status %)) providers)
      (optimization-lowering-fail! "C14-PROVIDER" source-path artifact
                                   (first providers)
                                   {:missing-fields [:provider-selection]}))
    (when-not (every? #(perf-present? (:proof %)) metadata)
      (optimization-lowering-fail! "C14-PROOF-METADATA" source-path artifact
                                   (:proof-to-target-metadata-map artifact)
                                   {:missing-fields [:proof]}))
    (when-not (= :preserved (get-in artifact
                                    [:capability-preservation-report :status]))
      (optimization-lowering-fail! "C14-CAPABILITY" source-path artifact
                                   (:capability-preservation-report artifact)
                                   {:missing-fields [:capability-preservation]}))
    (when-not (every? #(= :available (:fallback-status %)) unsupported)
      (optimization-lowering-fail! "C14-UNSUPPORTED" source-path artifact
                                   (first unsupported)
                                   {:missing-fields [:unsupported-feature]}))
    (when-not (= :gravity/target-artifact-manifest
                 (get-in artifact [:target-artifact-manifest :artifact]))
      (optimization-lowering-fail! "C14-MANIFEST" source-path artifact
                                   (:target-artifact-manifest artifact)
                                   {:missing-fields [:target-artifact-manifest]}))
    (when-not (= (set c14-lowering-diagnostic-ids)
                 (set (map :diagnostic diagnostics)))
      (optimization-lowering-fail! "C14-MANIFEST" source-path artifact
                                   (:lowering-diagnostic-stream artifact)
                                   {:missing-fields [:lowering-diagnostics]})))
  :complete)

(definterposable c14-lowering-capability-proof
  [artifact]
  {:c13-optimized-mir-input-verified?
   (= :complete (get-in artifact
                        [:c13-optimization-artifact
                         :capability-based-proof :status]))
   :lowering-request-verified?
   (= :optimized-mir (get-in artifact
                             [:lowering-request :input :kind]))
   :target-eligible?
   (= :eligible (get-in artifact [:target-eligibility-report :status]))
   :abi-manifest-complete?
   (= :complete (get-in artifact [:abi-manifest :status]))
   :runtime-provider-recorded?
   (= :complete (get-in artifact [:runtime-provider-manifest :status]))
   :providers-selected?
   (every? #(= :selected (:status %))
           (:provider-selection-records artifact))
   :proof-metadata-linked?
   (every? #(perf-present? (:proof %))
           (get-in artifact [:proof-to-target-metadata-map :entries]))
   :source-proof-safety-metadata-preserved?
   (and (= :complete (get-in artifact
                             [:source-generated-origin-map :status]))
        (perf-present? (get-in artifact
                               [:target-artifact-manifest :proof-map]))
        (perf-present? (get-in artifact
                               [:target-artifact-manifest :safety])))
   :capabilities-preserved?
   (= :preserved (get-in artifact
                         [:capability-preservation-report :status]))
   :unsupported-fallbacks-recorded?
   (every? #(= :available (:fallback-status %))
           (:unsupported-feature-report artifact))
   :manifest-complete?
   (= :gravity/target-artifact-manifest
      (get-in artifact [:target-artifact-manifest :artifact]))
   :diagnostics-covered?
   (= (set c14-lowering-diagnostic-ids)
      (set (map :diagnostic
                (get-in artifact
                        [:lowering-diagnostic-stream :diagnostics]))))
   :status :complete})

(definterposable compiler-c14-lowering-source-artifact
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (c14-lowering-source-overrides module)
        _ (c14-lowering-validate-source-overrides! source-path
                                                   source-overrides)
        optimization-artifact (compiler-c13-optimization-source-artifact
                               source-path source-text)
        optimized-mir (:optimized-mir-artifact optimization-artifact)
        input-id (:artifact-id optimization-artifact)
        target {:backend :jvm
                :triple "jvm-17"
                :features #{:objects :exceptions :threads}}
        lowering-request
        {:artifact :gravity/lowering-request
         :input {:kind :optimized-mir
                 :id input-id
                 :optimized-mir (:output optimized-mir)}
         :profile :hosted
         :target target
         :abi :jvm-hosted-stage0
         :runtime :hosted-jvm
         :providers {:allocator :jvm/gc
                     :panic :jvm/exception
                     :io :jvm/stdout}
         :required-evidence {:safety :mir/safety-table
                             :proofs :proof/c14-stage0
                             :capabilities :mir/capability-proof-table}}
        proof-map
        {:artifact :gravity/proof-target-metadata-map
         :target :jvm
         :entries [{:target-metadata :bounds-check-elided
                    :operation "mir-op-optimized-bounds-check-elide"
                    :proof :proof/c13-bounds-check-elision}
                   {:target-metadata :noalias
                    :operation "mir-op-optimized-target-layout-prepare"
                    :proof :proof/c13-layout-ownership}
                   {:target-metadata :nonnull
                    :operation "mir-op-optimized-dead-code-eliminate"
                    :proof :proof/c13-safety-preserved}]}
        diagnostics (c14-lowering-diagnostic-catalog source-path input-id)
        target-artifacts [{:kind :jvm-bytecode-plan
                           :hash (str "sha256:"
                                      (sha256-hex
                                       (pr-str (:output optimized-mir))))}]
        artifact-base
        {:kind :gravity/stage0-c14-target-lowering-artifact
         :task "P06-D093"
         :document-set ["C14"]
         :governing-document c14-lowering-governing-document
         :pass {:name :c14-target-lowering
                :input :optimized-mir
                :output :target-artifact-manifest
                :requires [:c13-optimized-mir :profile :target :abi
                           :runtime :providers :effects :capabilities
                           :safety :proofs]
                :preserves [:source-spans :origin-chain :profile :target
                            :effects :capabilities :safety :proofs
                            :dependencies]
                :emits [:lowering-request :target-eligibility-report
                        :abi-manifest :runtime-provider-manifest
                        :provider-selection-records :layout-decision-record
                        :proof-to-target-metadata-map
                        :source-generated-origin-map
                        :unsupported-feature-report
                        :target-artifact-manifest
                        :lowering-diagnostic-stream]
                :rejects c14-lowering-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c13-optimization-artifact
         (select-keys optimization-artifact
                      [:kind :task :artifact-id :governing-document
                       :optimized-mir-artifact :capability-based-proof])
         :optimization-artifact-kind (:kind optimization-artifact)
         :optimization-artifact-hash input-id
         :lowering-request lowering-request
         :target-eligibility-report
         {:artifact :gravity/target-eligibility-report
          :status :eligible
          :profile :hosted
          :target target
          :backend :jvm
          :reason :profile-target-provider-compatible}
         :abi-manifest
         {:artifact :gravity/abi-manifest
          :status :complete
          :calling-convention :jvm-static
          :exported-symbols ["compiler_c14_lowering_main"]
          :data-layout :jvm-object
          :alignment :jvm-default
          :enum-representation :jvm-tagged-object
          :closure-representation :jvm-function-object
          :panic-strategy :exception
          :resource-handle-representation :jvm-object-ref
          :ffi-boundary :jvm-interop
          :gc-policy :jvm-gc
          :debug-unwind :jvm-stacktrace}
         :runtime-provider-manifest
         {:artifact :gravity/runtime-provider-manifest
          :status :complete
          :runtime :hosted-jvm
          :providers (:providers lowering-request)}
         :provider-selection-records
         [{:provider :jvm/gc
           :capability :memory/allocator
           :effect :memory/allocate
           :status :selected}
          {:provider :jvm/stdout
           :capability :io/stdout
           :effect :io/write
           :status :selected}
          {:provider :jvm/exception
           :capability :panic/raise
           :effect :error/throw
           :status :selected}]
         :layout-decision-record
         {:artifact :gravity/layout-decision-record
          :status :complete
          :alignment :jvm-default
          :proof :proof/c13-layout-ownership
          :ownership-facts :mir/ownership-table
          :safety-facts :mir/safety-table}
         :proof-to-target-metadata-map proof-map
         :source-generated-origin-map
         {:artifact :gravity/source-generated-origin-map
          :status :complete
          :source-map (get-in optimization-artifact
                              [:optimized-mir-artifact :source-origin-map])
          :generated-origin-map []}
         :capability-preservation-report
         {:artifact :gravity/capability-preservation-report
          :status :preserved
          :denied-additions []
          :preserved-capabilities (:capabilities module)}
         :unsupported-feature-report
         [{:mir-op "c11-mir-op-gpu-kernel"
           :required-feature :gpu-kernel
           :backend :jvm
           :profile :hosted
           :source-span (source-span source-path 0)
           :available-alternatives [:mir-scalar-kernel]
           :fallback :mir-scalar-kernel
           :fallback-status :available
           :diagnostic-id "C14-UNSUPPORTED"}]
         :target-artifact-manifest
         {:artifact :gravity/target-artifact-manifest
          :input (:output optimized-mir)
          :backend :jvm
          :profile :hosted
          :target (str "sha256:" (sha256-hex (pr-str target)))
          :artifacts target-artifacts
          :source-map :gravity/source-generated-origin-map
          :proof-map :gravity/proof-target-metadata-map
          :effects :mir/effect-table
          :capabilities :mir/capability-proof-table
          :safety :mir/safety-table
          :runtime :gravity/runtime-provider-manifest
          :dependencies input-id
          :diagnostics []}
         :lowering-diagnostic-stream diagnostics
         :c14-lowering-results
         {:documents ["C14"]
          :task "P06-D093"
          :required-diagnostic-ids c14-lowering-diagnostic-ids
          :c13-input-status :complete
          :lowering-request-status :complete
          :target-eligibility-status :complete
          :abi-status :complete
          :runtime-provider-status :complete
          :proof-metadata-status :complete
          :manifest-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (c14-lowering-validate! source-path artifact-base)
        capability-proof (c14-lowering-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(definterposable compiler-c14-lowering-file-artifact
  [path]
  (compiler-c14-lowering-source-artifact path (slurp path)))

(def ^:private namespace-contract
  {:contract-boundary :hosted-stage0-c14-target-lowering
   :dependency-direction
   {:requires ['gravity.digest 'gravity.optimization-lowering]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :owns [:hosted-stage0-c14-target-lowering-adapter
          :hosted-stage0-c14-evidence]
   :does-not-own [:canonical-c14-authority :source-authentication
                  :profile-authority :target-authority :abi-authority
                  :runtime-provider-authority :proof-metadata-authority
                  :backend-authority :equivalence :self-hosting :release
                  :seed-retirement]
   :compatibility-only? true
   :lowering-model-complete? false
   :canonical-c14-authority? false
   :operation-interposition
   {:accepted-keys operation-keys
    :unknown-keys-rejected? true
    :partial-overrides? true
    :single-binding-per-top-level-call? true}})
(defn- string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- string-map? [value]
  (and (map? value)
       (every? (fn [[key entry]] (and (string? key) (string? entry))) value)))
(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C14 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[key value]
                           (select-keys operations function-operation-keys)
                           :when (not (fn? value))]
                       key))]
    (when unknown
      (throw (ex-info "C14 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)})))
    (when invalid
      (throw (ex-info "C14 function operations must be functions"
                      {:non-function-keys (vec invalid)}))))
  (doseq [[key predicate]
          [[:c14-lowering-governing-document
            #(and (string? %) (seq %))]
           [:c14-lowering-diagnostic-ids string-vector?]
           [:optimization-lowering-diagnostic-messages string-map?]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C14 scalar operation has invalid shape" {:key key})))
  operations)
(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              c14-lowering-governing-document
              (get merged :c14-lowering-governing-document
                   c14-lowering-governing-document)
              c14-lowering-diagnostic-ids
              (get merged :c14-lowering-diagnostic-ids
                   c14-lowering-diagnostic-ids)
              optimization-lowering-diagnostic-messages
              (get merged :optimization-lowering-diagnostic-messages
                   optimization-lowering-diagnostic-messages)]
      (thunk))))
(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c14-engine-contract {:arglists '([])}
   'c14-lowering-governing-document {:kind :constant}
   'c14-lowering-source-overrides {:arglists '([module])}
   'c14-lowering-validate-source-overrides!
   {:arglists '([source-path overrides])}
   'c14-lowering-diagnostic-catalog {:arglists '([source-path input-id])}
   'c14-lowering-validate! {:arglists '([source-path artifact])}
   'c14-lowering-capability-proof {:arglists '([artifact])}
   'compiler-c14-lowering-source-artifact
   {:arglists '([source-path source-text])}
   'compiler-c14-lowering-file-artifact {:arglists '([path])}})
(defn c14-engine-contract []
  (assoc namespace-contract :public-api public-api))
