(ns gravity.optimization-lowering.policy
  "Static operation and compatibility contracts for optimization/lowering.")

(def function-operation-keys
  #{:fail!
    :source-span
    :sha256-hex
    :perf-present?
    :checked-core-source-artifact
    :domain-ir-source-artifact
    :optimization-lowering-source-overrides
    :optimization-lowering-fail!
    :optimization-pass-contract-record
    :optimization-decision-record
    :optimization-lowering-validate-overrides!
    :optimization-lowering-validate!
    :optimization-lowering-capability-proof
    :optimization-lowering-source-artifact})

(def scalar-operation-keys
  #{:c13-optimization-diagnostic-ids
    :c14-lowering-diagnostic-ids
    :optimization-lowering-diagnostic-ids
    :optimization-lowering-diagnostic-messages
    :optimization-lowering-override-diagnostics
    :optimization-pass-contract-seed})

(def operation-keys
  (into function-operation-keys scalar-operation-keys))

(defn string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))

(defn string-map? [value]
  (and (map? value)
       (every? (fn [[key item]] (and (string? key) (string? item))) value)))

(defn keyword-vector-map? [value]
  (and (map? value)
       (every? (fn [[key item]] (and (keyword? key) (vector? item))) value)))

(defn vector-maps? [value]
  (and (vector? value) (seq value) (every? map? value)))

(def scalar-shapes
  [[:c13-optimization-diagnostic-ids string-vector? :string-vector]
   [:c14-lowering-diagnostic-ids string-vector? :string-vector]
   [:optimization-lowering-diagnostic-ids string-vector? :string-vector]
   [:optimization-lowering-diagnostic-messages string-map? :string-map]
   [:optimization-lowering-override-diagnostics
    keyword-vector-map?
    :keyword-vector-map]
   [:optimization-pass-contract-seed vector-maps? :vector-maps]])

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "optimization/lowering operation map must be a map"
                    {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[key value]
                           (select-keys operations function-operation-keys)
                           :when (not (fn? value))]
                       key))]
    (when unknown
      (throw (ex-info "optimization/lowering operation map contains unknown keys"
                      {:unknown-keys (vec unknown)})))
    (when invalid
      (throw (ex-info "optimization/lowering function operations must be functions"
                      {:non-function-keys (vec invalid)}))))
  (doseq [[key predicate expected] scalar-shapes
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "optimization/lowering scalar operation has invalid shape"
                    {:key key :expected expected})))
  operations)

(def namespace-contract
  {:contract-boundary :hosted-stage0-optimization-lowering-shared
   :owns [:shared-hosted-optimization-lowering-records
          :shared-hosted-optimization-lowering-validation]
   :dependency-direction {:requires ['gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c13-authority :canonical-c14-authority
                  :source-authentication :proof-authority
                  :target-lowering-authority :backend-authority
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :canonical-authority? false
   :operation-interposition {:accepted-keys operation-keys
                             :unknown-keys-rejected? true
                             :partial-overrides? true}})
