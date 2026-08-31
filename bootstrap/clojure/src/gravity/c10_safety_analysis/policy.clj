(ns gravity.c10-safety-analysis.policy
  "Operation and scalar policy for the hosted C10 facade.

  The facade owns dynamic values and interposition; this namespace owns the
  stable accepted-key and shape contract so it can be tested independently.")

(def function-operation-keys
  #{:fail! :source-span :c4-artifact-id :read-source-form-records
    :validate-ns-syntax! :parse-module :compiler-c9-ownership-source-artifact
    :c10-safety-source-overrides :c10-safety-message :c10-safety-fail!
    :c10-safety-validate-overrides! :c10-safety-operation-inventory
    :c10-safety-outcome-records :c10-runtime-check-list
    :c10-proof-obligation-list :c10-proof-certificate-references
    :c10-unsafe-island-audit-manifest :c10-taint-capability-safety-report
    :c10-generated-code-safety-provenance :c10-optimization-safety-preservation
    :c10-safety-diagnostics :c10-safety-verifier-report
    :c10-safety-capability-proof :c10-safety-validate!
    :compiler-c10-safety-source-artifact :compiler-c10-safety-file-artifact})

(def scalar-operation-keys
  #{:c10-safety-diagnostic-ids :c10-safety-governing-document
    :c10-safety-rejected-designs :c10-safety-override-diagnostics
    :c10-safe-outcomes})

(def operation-keys (into function-operation-keys scalar-operation-keys))

(defn unsupported-host-operation [operation]
  (fn [& _]
    (throw (ex-info (str "C10 leaf requires injected operation " operation)
                    {:operation operation}))))

(defn- string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- vector-of-maps? [value]
  (and (vector? value) (every? map? value)))
(defn- keyword-string-map? [value]
  (and (map? value)
       (every? (fn [[key item]] (and (keyword? key) (string? item))) value)))
(defn- keyword-set? [value]
  (and (set? value) (seq value) (every? keyword? value)))

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C10 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid-fns (seq (for [[key value] (select-keys operations function-operation-keys)
                               :when (not (fn? value))] key))]
    (when unknown
      (throw (ex-info "C10 operation map contains unknown keys"
                      {:unknown-keys (vec unknown) :allowed-keys operation-keys})))
    (when invalid-fns
      (throw (ex-info "C10 function operation values must be functions"
                      {:non-function-keys (vec invalid-fns)}))))
  (doseq [[key predicate expected]
          [[:c10-safety-diagnostic-ids string-vector? :non-empty-string-vector]
           [:c10-safety-governing-document #(and (string? %) (seq %)) :non-empty-string]
           [:c10-safety-rejected-designs vector-of-maps? :vector-of-maps]
           [:c10-safety-override-diagnostics keyword-string-map? :keyword-to-string-map]
           [:c10-safe-outcomes keyword-set? :non-empty-keyword-set]]
          :when (and (contains? operations key) (not (predicate (get operations key))))]
    (throw (ex-info "C10 scalar operation has an invalid shape"
                    {:key key :expected expected :actual (get operations key)})))
  operations)
