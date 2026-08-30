(ns gravity.c12-domain-ir.policy)

(def function-operation-keys
  #{:source-span :c4-artifact-id :read-source-form-records :validate-ns-syntax!
    :parse-module :compiler-c11-mir-source-artifact :domain-ir-validate-overrides!
    :domain-ir-registration-record :domain-ir-artifact-record :domain-ir-validate!
    :domain-ir-capability-proof :c12-domain-ir-source-overrides
    :c12-domain-ir-validate-source-overrides! :c12-domain-ir-diagnostic-catalog
    :compiler-c12-domain-ir-source-artifact :compiler-c12-domain-ir-file-artifact})

(def scalar-operation-keys
  #{:c12-domain-ir-governing-document :domain-ir-diagnostic-ids
    :domain-ir-diagnostic-messages :domain-ir-required-families :domain-ir-registry-seed})

(def operation-keys (into function-operation-keys scalar-operation-keys))

(defn unsupported [key]
  (fn [& _]
    (throw (ex-info (str "C12 leaf requires injected operation " key) {:operation key}))))

(defn source-overrides [module]
  (or (get-in module [:metadata :compiler :c12-domain-ir])
      (get-in module [:metadata :compiler :domain-ir])
      {}))

(defn source-overrides-artifact [overrides]
  {:source-overrides overrides
   :domain-ir-registry [{:schema "sha256:stage0-c12-source-override"}]})

(defn diagnostic-catalog [configuration source-path source-span]
  (let [span (source-span source-path 0)]
    {:artifact :gravity/c12-domain-diagnostic-catalog
     :status :complete
     :diagnostics
     (mapv (fn [id]
             {:diagnostic id :domain :stage0-domain-ir
              :artifact-id "c12-domain-diagnostic-catalog" :source-span span
              :semantic-anchor {:mir-ops [] :typed-core []} :owner-doc "C12"
              :profile :hosted :target :jvm :verifier :gravity.domain-ir/verify
              :missing-fact :catalog-entry
              :remediation (get (:domain-ir-diagnostic-messages configuration) id)})
           (:domain-ir-diagnostic-ids configuration))}))

(defn- string-vector? [v] (and (vector? v) (seq v) (every? string? v)))
(defn- keyword-vector? [v] (and (vector? v) (seq v) (every? keyword? v)))
(defn- vector-of-maps? [v] (and (vector? v) (seq v) (every? map? v)))
(defn- string-map? [v]
  (and (map? v) (every? (fn [[k x]] (and (string? k) (string? x))) v)))

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C12 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[k v] (select-keys operations function-operation-keys)
                           :when (not (fn? v))] k))]
    (when unknown
      (throw (ex-info "C12 operation map contains unknown keys"
                      {:unknown-keys (vec unknown) :allowed-keys operation-keys})))
    (when invalid
      (throw (ex-info "C12 function operation values must be functions"
                      {:non-function-keys (vec invalid)}))))
  (doseq [[key pred expected]
          [[:c12-domain-ir-governing-document #(and (string? %) (seq %)) :non-empty-string]
           [:domain-ir-diagnostic-ids string-vector? :non-empty-string-vector]
           [:domain-ir-diagnostic-messages string-map? :string-map]
           [:domain-ir-required-families keyword-vector? :non-empty-keyword-vector]
           [:domain-ir-registry-seed vector-of-maps? :non-empty-vector-of-maps]]
          :when (and (contains? operations key) (not (pred (get operations key))))]
    (throw (ex-info "C12 scalar operation has invalid shape"
                    {:key key :expected expected :actual (get operations key)})))
  operations)

(defn engine-contract [public-api]
  {:contract-boundary :hosted-stage0-c12-domain-ir
   :artifact-inputs [:c11-mir-artifact :module-context :domain-registry-seed]
   :artifact-outputs [:domain-ir-registry :domain-ir-artifacts
                      :semantic-anchor-map :entry-pass-records :exit-pass-records
                      :domain-verifier-report :proof-and-certificate-references
                      :lowering-eligibility-matrix :fallback-records :domain-ir-diagnostics]
   :owns [:hosted-stage0-c12-domain-ir-adapter :hosted-stage0-c12-artifact-projection]
   :dependency-direction {:requires ['gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c12-authority :source-authentication
                  :shared-domain-registry-authority :domain-verifier-authority
                  :proof-certificate-authority :plugin-policy-authority
                  :target-lowering-authority :backend-authority :equivalence
                  :self-hosting :release :seed-retirement]
   :compatibility-only? true :domain-model-complete? false
   :canonical-c12-authority? false
   :operation-interposition {:accepted-keys operation-keys
                             :unknown-keys-rejected? true :partial-overrides? true
                             :single-binding-per-top-level-call? true}
   :public-api public-api})
