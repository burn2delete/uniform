(ns gravity.c18-verification.policy
  "Operation-map policy for the hosted C18 interposition facade.")

(def function-operation-keys
  #{:fail! :source-span :c4-artifact-id
    :read-source-form-records :validate-ns-syntax! :parse-module
    :compiler-c17-plugin-source-artifact
    :c18-verification-source-overrides :c18-verification-fail!
    :c18-verification-validate-source-overrides!
    :c18-verification-diagnostic-stream :c18-pass-risk-records
    :c18-verification-validate! :c18-verification-capability-proof
    :compiler-c18-verification-source-artifact
    :compiler-c18-verification-file-artifact})

(def scalar-operation-keys
  #{:compiler-verification-diagnostic-messages
    :compiler-verification-override-diagnostics
    :c18-verification-governing-document
    :c18-verification-diagnostic-ids
    :c18-pass-risk-required-fields
    :c18-trust-report-required-fields})

(def operation-keys
  (into function-operation-keys scalar-operation-keys))

(defn unsupported [key]
  (fn [& _]
    (throw (ex-info (str "C18 leaf requires injected operation " key)
                    {:operation key}))))

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

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C18 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[key value]
                           (select-keys operations function-operation-keys)
                           :when (not (fn? value))]
                       key))]
    (when unknown
      (throw (ex-info "C18 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)})))
    (when invalid
      (throw (ex-info "C18 function operations must be functions"
                      {:non-function-keys (vec invalid)}))))
  (doseq [[key predicate]
          [[:compiler-verification-diagnostic-messages string-map?]
           [:compiler-verification-override-diagnostics override-map?]
           [:c18-verification-governing-document #(and (string? %) (seq %))]
           [:c18-verification-diagnostic-ids string-vector?]
           [:c18-pass-risk-required-fields keyword-vector?]
           [:c18-trust-report-required-fields keyword-vector?]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C18 scalar operation has invalid shape" {:key key})))
  operations)
