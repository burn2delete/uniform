(ns gravity.c12-domain-ir
  "Hosted Stage0 C12 domain-IR registry and artifact projection.

  The leaf preserves the Clojure seed compatibility adapter over shared domain
  helpers. It is not domain-verifier, proof, plugin, backend, self-hosting, or
  release authority."
  (:require [gravity.digest :as digest]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})

(def ^:private function-operation-keys
  #{:source-span
    :c4-artifact-id
    :read-source-form-records
    :validate-ns-syntax!
    :parse-module
    :compiler-c11-mir-source-artifact
    :domain-ir-validate-overrides!
    :domain-ir-registration-record
    :domain-ir-artifact-record
    :domain-ir-validate!
    :domain-ir-capability-proof
    :c12-domain-ir-source-overrides
    :c12-domain-ir-validate-source-overrides!
    :c12-domain-ir-diagnostic-catalog
    :compiler-c12-domain-ir-source-artifact
    :compiler-c12-domain-ir-file-artifact})
(def ^:private scalar-operation-keys
  #{:c12-domain-ir-governing-document
    :domain-ir-diagnostic-ids
    :domain-ir-diagnostic-messages
    :domain-ir-required-families
    :domain-ir-registry-seed})
(def ^:private operation-keys (into function-operation-keys scalar-operation-keys))
(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))
(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))
(defn- default-source-span [path index] {:source path :form-index index})
(defn- default-c4-artifact-id [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))
(defn- unsupported [key] (fn [& _] (throw (ex-info (str "C12 leaf requires injected operation " key) {:operation key}))))
(defn- op-fn [key fallback] (or (get *operations* key) fallback))
(defn- source-span [path index] ((op-fn :source-span default-source-span) path index))
(defn- c4-artifact-id [artifact] ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))
(defn- read-source-form-records [path text]
  ((op-fn :read-source-form-records (unsupported :read-source-form-records)) path text))
(defn- validate-ns-syntax! [path forms]
  ((op-fn :validate-ns-syntax! (unsupported :validate-ns-syntax!)) path forms))
(defn- parse-module [path forms]
  ((op-fn :parse-module (unsupported :parse-module)) path forms))
(defn- compiler-c11-mir-source-artifact [path text]
  ((op-fn :compiler-c11-mir-source-artifact (unsupported :compiler-c11-mir-source-artifact)) path text))
(defn- domain-ir-validate-overrides! [source-path artifact]
  ((op-fn :domain-ir-validate-overrides! (unsupported :domain-ir-validate-overrides!)) source-path artifact))
(defn- domain-ir-registration-record [seed]
  ((op-fn :domain-ir-registration-record (unsupported :domain-ir-registration-record)) seed))
(defn- domain-ir-artifact-record [mir-artifact registration index]
  ((op-fn :domain-ir-artifact-record (unsupported :domain-ir-artifact-record)) mir-artifact registration index))
(defn- domain-ir-validate! [source-path artifact]
  ((op-fn :domain-ir-validate! (unsupported :domain-ir-validate!)) source-path artifact))
(defn- domain-ir-capability-proof [artifact]
  ((op-fn :domain-ir-capability-proof (unsupported :domain-ir-capability-proof)) artifact))

(def ^:private ^:dynamic domain-ir-diagnostic-ids [])
(def ^:private ^:dynamic domain-ir-diagnostic-messages {})
(def ^:private ^:dynamic domain-ir-required-families [])
(def ^:private ^:dynamic domain-ir-registry-seed [])

(def ^:dynamic c12-domain-ir-governing-document
  "docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md")

(definterposable c12-domain-ir-source-overrides
  [module]
  (or (get-in module [:metadata :compiler :c12-domain-ir])
      (get-in module [:metadata :compiler :domain-ir])
      {}))

(definterposable c12-domain-ir-validate-source-overrides!
  [source-path overrides]
  (domain-ir-validate-overrides!
   source-path
   {:source-overrides overrides
    :domain-ir-registry [{:schema "sha256:stage0-c12-source-override"}]}))

(definterposable c12-domain-ir-diagnostic-catalog
  [source-path]
  (let [span (source-span source-path 0)]
    {:artifact :gravity/c12-domain-diagnostic-catalog
     :status :complete
     :diagnostics
     (mapv (fn [id]
             {:diagnostic id
              :domain :stage0-domain-ir
              :artifact-id "c12-domain-diagnostic-catalog"
              :source-span span
              :semantic-anchor {:mir-ops [] :typed-core []}
              :owner-doc "C12"
              :profile :hosted
              :target :jvm
              :verifier :gravity.domain-ir/verify
              :missing-fact :catalog-entry
              :remediation (get domain-ir-diagnostic-messages id)})
           domain-ir-diagnostic-ids)}))

(definterposable compiler-c12-domain-ir-source-artifact
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (c12-domain-ir-source-overrides module)
        _ (c12-domain-ir-validate-source-overrides! source-path
                                                    source-overrides)
        c11-artifact (compiler-c11-mir-source-artifact source-path source-text)
        registrations (mapv domain-ir-registration-record
                            domain-ir-registry-seed)
        domain-artifacts (mapv #(domain-ir-artifact-record c11-artifact %1 %2)
                               registrations
                               (range))
        semantic-anchor-map (mapv #(select-keys %
                                                [:domain :artifact-id
                                                 :semantic-anchor :source])
                                  domain-artifacts)
        entry-pass-records
        (mapv (fn [registration]
                {:domain (:domain registration)
                 :entry-passes (:entry-passes registration)
                 :consumes [:source-forms :typed-core :gravity/mir]
                 :input-artifact (:artifact-id c11-artifact)
                 :preserves [:types :effects :ownership :capabilities
                             :profile :target :safety :provenance]
                 :status :complete})
              registrations)
        exit-pass-records
        (mapv (fn [registration]
                {:domain (:domain registration)
                 :exit-passes (:exit-passes registration)
                 :requires [:domain-verifier-report :proof-or-certificate
                            :fallback-record]
                 :emits [:mir-subgraph :target-provider-call
                         :runtime-manifest :verification-artifact]
                 :status :complete})
              registrations)
        proof-records (mapv (fn [domain-artifact]
                              {:domain (:domain domain-artifact)
                               :artifact-id (:artifact-id domain-artifact)
                               :evidence-kind :translation-validation
                               :proofs (:proofs domain-artifact)
                               :status :accepted})
                            domain-artifacts)
        lowering-matrix
        (mapv (fn [registration]
                (let [target (get-in c11-artifact
                                     [:mir-module :target-request])
                      direct? (contains? (:target-lowerings registration)
                                         target)]
                  {:domain (:domain registration)
                   :target-request target
                   :supported-targets (:target-lowerings registration)
                   :status (if direct? :eligible :fallback)
                   :fallback (:fallback registration)}))
              registrations)
        fallback-records (mapv (fn [registration]
                                 {:domain (:domain registration)
                                  :fallback (:fallback registration)
                                  :status :available
                                  :residual :gravity/mir})
                               registrations)
        diagnostics (c12-domain-ir-diagnostic-catalog source-path)
        artifact-base
        {:kind :gravity/stage0-c12-domain-ir-architecture-artifact
         :task "P06-D091"
         :document-set ["C12"]
         :governing-document c12-domain-ir-governing-document
         :pass {:name :c12-domain-ir-architecture
                :input :gravity/mir
                :output :domain-ir-registry
                :requires [:c11-mir-specification :semantic-anchors
                           :type-facts :effect-facts :ownership-facts
                           :capability-proofs :safety-outcomes
                           :source-provenance]
                :preserves [:types :effects :ownership :capabilities
                            :profile :target :safety :source-spans
                            :origin-chain]
                :emits [:domain-ir-registry :domain-ir-artifacts
                        :semantic-anchor-map :entry-pass-records
                        :exit-pass-records :domain-verifier-report
                        :proof-and-certificate-references
                        :lowering-eligibility-matrix :fallback-records
                        :domain-ir-diagnostic-stream]
                :rejects domain-ir-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c11-mir-spec-artifact
         (select-keys c11-artifact [:kind :task :artifact-id
                                    :governing-document :mir-module
                                    :mir-verifier-report
                                    :capability-based-proof])
         :mir-artifact-kind (:kind c11-artifact)
         :mir-artifact-hash (:artifact-id c11-artifact)
         :mir-artifact c11-artifact
         :domain-ir-registry registrations
         :domain-ir-artifact-schema
         {:artifact :gravity/domain-ir-artifact-schema
          :required-fields [:artifact :domain :artifact-id :source
                            :semantic-anchor :profile :target-request
                            :facts :verifier :proofs :lowering-status]
          :required-domain-families domain-ir-required-families
          :status :complete}
         :domain-ir-artifacts domain-artifacts
         :semantic-anchor-map semantic-anchor-map
         :entry-pass-records entry-pass-records
         :exit-pass-records exit-pass-records
         :domain-verifier-report
         {:artifact :gravity/domain-verifier-report
          :status :passed
          :domains (mapv :domain domain-artifacts)
          :checks [:registration :schema :semantic-anchor
                   :source-provenance :facts :proof-obligations
                   :lowering :fallback :plugin-policy]
          :diagnostics []}
         :proof-and-certificate-references proof-records
         :lowering-eligibility-matrix lowering-matrix
         :fallback-records fallback-records
         :plugin-registration-policy
         {:status :enforced
          :requires [:schema :owner-doc :verifier :semantic-anchor
                     :non-opaque-payload]
          :visibility :package-visible}
         :domain-ir-diagnostic-stream diagnostics
         :c12-domain-ir-results
         {:documents ["C12"]
          :task "P06-D091"
          :required-diagnostic-ids domain-ir-diagnostic-ids
          :c11-input-status :complete
          :registration-status :complete
          :artifact-schema-status :complete
          :anchor-status :complete
          :verifier-status :complete
          :proof-evidence-status :complete
          :lowering-and-fallback-status :complete
          :plugin-policy-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (domain-ir-validate! source-path artifact-base)
        capability-proof (assoc (domain-ir-capability-proof artifact-base)
                                :c11-mir-input-verified?
                                (= :passed
                                   (get-in c11-artifact
                                           [:mir-verifier-report :status]))
                                :diagnostics-covered?
                                (= (set domain-ir-diagnostic-ids)
                                   (set (map :diagnostic
                                             (:diagnostics diagnostics)))))]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(definterposable compiler-c12-domain-ir-file-artifact
  [path]
  (compiler-c12-domain-ir-source-artifact path (slurp path)))

(def ^:private namespace-contract
  {:contract-boundary :hosted-stage0-c12-domain-ir
   :artifact-inputs [:c11-mir-artifact :module-context :domain-registry-seed]
   :artifact-outputs [:domain-ir-registry :domain-ir-artifacts
                      :semantic-anchor-map :entry-pass-records
                      :exit-pass-records :domain-verifier-report
                      :proof-and-certificate-references
                      :lowering-eligibility-matrix :fallback-records
                      :domain-ir-diagnostics]
   :owns [:hosted-stage0-c12-domain-ir-adapter
          :hosted-stage0-c12-artifact-projection]
   :dependency-direction {:requires ['gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c12-authority :source-authentication
                  :shared-domain-registry-authority :domain-verifier-authority
                  :proof-certificate-authority :plugin-policy-authority
                  :target-lowering-authority :backend-authority
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :domain-model-complete? false
   :canonical-c12-authority? false
   :operation-interposition {:accepted-keys operation-keys
                             :unknown-keys-rejected? true
                             :partial-overrides? true
                             :single-binding-per-top-level-call? true}})
(defn- string-vector? [v] (and (vector? v) (seq v) (every? string? v)))
(defn- keyword-vector? [v] (and (vector? v) (seq v) (every? keyword? v)))
(defn- vector-of-maps? [v] (and (vector? v) (seq v) (every? map? v)))
(defn- string-map? [v] (and (map? v) (every? (fn [[k x]] (and (string? k) (string? x))) v)))
(defn- validate-operations! [operations]
  (when-not (map? operations) (throw (ex-info "C12 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[k v] (select-keys operations function-operation-keys) :when (not (fn? v))] k))]
    (when unknown (throw (ex-info "C12 operation map contains unknown keys" {:unknown-keys (vec unknown) :allowed-keys operation-keys})))
    (when invalid (throw (ex-info "C12 function operation values must be functions" {:non-function-keys (vec invalid)}))))
  (doseq [[key pred expected]
          [[:c12-domain-ir-governing-document #(and (string? %) (seq %)) :non-empty-string]
           [:domain-ir-diagnostic-ids string-vector? :non-empty-string-vector]
           [:domain-ir-diagnostic-messages string-map? :string-map]
           [:domain-ir-required-families keyword-vector? :non-empty-keyword-vector]
           [:domain-ir-registry-seed vector-of-maps? :non-empty-vector-of-maps]]
          :when (and (contains? operations key) (not (pred (get operations key))))]
    (throw (ex-info "C12 scalar operation has invalid shape" {:key key :expected expected :actual (get operations key)})))
  operations)
(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              c12-domain-ir-governing-document (get merged :c12-domain-ir-governing-document c12-domain-ir-governing-document)
              domain-ir-diagnostic-ids (get merged :domain-ir-diagnostic-ids domain-ir-diagnostic-ids)
              domain-ir-diagnostic-messages (get merged :domain-ir-diagnostic-messages domain-ir-diagnostic-messages)
              domain-ir-required-families (get merged :domain-ir-required-families domain-ir-required-families)
              domain-ir-registry-seed (get merged :domain-ir-registry-seed domain-ir-registry-seed)]
      (thunk))))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c12-engine-contract {:arglists '([])}
   'c12-domain-ir-governing-document {:kind :constant}
   'c12-domain-ir-source-overrides {:arglists '([module])}
   'c12-domain-ir-validate-source-overrides! {:arglists '([source-path overrides])}
   'c12-domain-ir-diagnostic-catalog {:arglists '([source-path])}
   'compiler-c12-domain-ir-source-artifact {:arglists '([source-path source-text])}
   'compiler-c12-domain-ir-file-artifact {:arglists '([path])}
   })
(defn c12-engine-contract [] (assoc namespace-contract :public-api public-api))
