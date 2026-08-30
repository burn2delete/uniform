(ns gravity.c14-lowering.policy)

(def function-operation-keys
  #{:source-span :c4-artifact-id :sha256-hex :perf-present? :read-source-form-records
    :validate-ns-syntax! :parse-module :compiler-c13-optimization-source-artifact
    :optimization-lowering-validate-overrides! :optimization-lowering-fail!
    :c14-lowering-source-overrides :c14-lowering-validate-source-overrides!
    :c14-lowering-diagnostic-catalog :c14-lowering-validate!
    :c14-lowering-capability-proof :compiler-c14-lowering-source-artifact
    :compiler-c14-lowering-file-artifact})
(def scalar-operation-keys
  #{:c14-lowering-governing-document :c14-lowering-diagnostic-ids
    :optimization-lowering-diagnostic-messages})
(def operation-keys (into function-operation-keys scalar-operation-keys))
(defn unsupported [key]
  (fn [& _] (throw (ex-info (str "C14 leaf requires injected operation " key) {:operation key}))))
(defn- string-vector? [value] (and (vector? value) (seq value) (every? string? value)))
(defn- string-map? [value]
  (and (map? value) (every? (fn [[key entry]] (and (string? key) (string? entry))) value)))
(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C14 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[key value] (select-keys operations function-operation-keys)
                           :when (not (fn? value))] key))]
    (when unknown (throw (ex-info "C14 operation map contains unknown keys" {:unknown-keys (vec unknown)})))
    (when invalid (throw (ex-info "C14 function operations must be functions" {:non-function-keys (vec invalid)}))))
  (doseq [[key predicate]
          [[:c14-lowering-governing-document #(and (string? %) (seq %))]
           [:c14-lowering-diagnostic-ids string-vector?]
           [:optimization-lowering-diagnostic-messages string-map?]]
          :when (and (contains? operations key) (not (predicate (get operations key))))]
    (throw (ex-info "C14 scalar operation has invalid shape" {:key key})))
  operations)
(defn engine-contract [public-api]
  {:contract-boundary :hosted-stage0-c14-target-lowering
   :dependency-direction {:requires ['gravity.digest 'gravity.optimization-lowering]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :owns [:hosted-stage0-c14-target-lowering-adapter :hosted-stage0-c14-evidence]
   :does-not-own [:canonical-c14-authority :source-authentication :profile-authority
                  :target-authority :abi-authority :runtime-provider-authority
                  :proof-metadata-authority :backend-authority :equivalence
                  :self-hosting :release :seed-retirement]
   :compatibility-only? true :lowering-model-complete? false :canonical-c14-authority? false
   :operation-interposition {:accepted-keys operation-keys :unknown-keys-rejected? true
                             :partial-overrides? true :single-binding-per-top-level-call? true}
   :public-api public-api})
