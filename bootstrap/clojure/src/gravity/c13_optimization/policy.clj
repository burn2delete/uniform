(ns gravity.c13-optimization.policy)

(def function-operation-keys
  #{:source-span :c4-artifact-id :sha256-hex :read-source-form-records
    :validate-ns-syntax! :parse-module :perf-present?
    :compiler-c12-domain-ir-source-artifact :optimization-lowering-validate-overrides!
    :optimization-pass-contract-record :optimization-decision-record
    :optimization-lowering-fail! :c13-optimization-source-overrides
    :c13-optimization-validate-source-overrides! :c13-optimization-diagnostic-catalog
    :c13-optimization-validate! :c13-optimization-capability-proof
    :compiler-c13-optimization-source-artifact :compiler-c13-optimization-file-artifact})

(def scalar-operation-keys
  #{:c13-optimization-governing-document :c13-optimization-diagnostic-ids
    :optimization-lowering-diagnostic-messages :optimization-pass-contract-seed})

(def operation-keys (into function-operation-keys scalar-operation-keys))

(defn unsupported [key]
  (fn [& _]
    (throw (ex-info (str "C13 leaf requires injected operation " key) {:operation key}))))

(defn- string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))

(defn- string-map? [value]
  (and (map? value)
       (every? (fn [[key candidate]] (and (string? key) (string? candidate))) value)))

(defn- vector-of-maps? [value]
  (and (vector? value) (seq value) (every? map? value)))

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C13 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[key value] (select-keys operations function-operation-keys)
                           :when (not (fn? value))]
                       key))]
    (when unknown
      (throw (ex-info "C13 operation map contains unknown keys" {:unknown-keys (vec unknown)})))
    (when invalid
      (throw (ex-info "C13 function operations must be functions"
                      {:non-function-keys (vec invalid)}))))
  (doseq [[key predicate]
          [[:c13-optimization-governing-document #(and (string? %) (seq %))]
           [:c13-optimization-diagnostic-ids string-vector?]
           [:optimization-lowering-diagnostic-messages string-map?]
           [:optimization-pass-contract-seed vector-of-maps?]]
          :when (and (contains? operations key) (not (predicate (get operations key))))]
    (throw (ex-info "C13 scalar operation invalid" {:key key})))
  operations)

(defn engine-contract [public-api]
  {:contract-boundary :hosted-stage0-c13-optimization
   :dependency-direction {:requires ['gravity.digest 'gravity.optimization-lowering]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :owns [:hosted-stage0-c13-optimization-adapter :hosted-stage0-c13-evidence]
   :does-not-own [:canonical-c13-authority :source-authentication
                  :shared-optimization-engine-authority :proof-authority
                  :check-elision-authority :domain-verifier-authority
                  :target-lowering-authority :backend-authority :equivalence
                  :self-hosting :release :seed-retirement]
   :compatibility-only? true :optimization-model-complete? false
   :canonical-c13-authority? false
   :operation-interposition {:accepted-keys operation-keys
                             :unknown-keys-rejected? true :partial-overrides? true
                             :single-binding-per-top-level-call? true}
   :public-api public-api})
