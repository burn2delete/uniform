(ns gravity.c9-ownership-checker.policy
  "Operation-map policy for the hosted C9 interposition facade.")

(def function-operation-keys
  #{:fail! :source-span :c4-artifact-id :read-source-form-records
    :validate-ns-syntax! :parse-module :compiler-c8-effect-source-artifact
    :c9-ownership-source-overrides :c9-ownership-message :c9-ownership-fail!
    :c9-ownership-validate-overrides! :c9-node-ids :c9-node
    :c9-ownership-graph :c9-borrow-graph :c9-lifetime-interval-map
    :c9-escape-analysis-report :c9-region-lifetime-graph
    :c9-arena-generation-graph :c9-linear-resource-flow-graph
    :c9-transfer-records :c9-runtime-check-records
    :c9-unsafe-audit-references :c9-ownership-diagnostics
    :c9-linear-paths-exact? :c9-ownership-verifier-report
    :c9-ownership-capability-proof :c9-ownership-validate!
    :compiler-c9-ownership-source-artifact
    :compiler-c9-ownership-file-artifact})

(def scalar-operation-keys
  #{:c9-ownership-diagnostic-ids :c9-ownership-governing-document
    :c9-ownership-rejected-designs :c9-ownership-override-diagnostics})

(def operation-keys (into function-operation-keys scalar-operation-keys))

(defn unsupported-host-operation [operation]
  (fn [& _]
    (throw (ex-info (str "C9 leaf requires injected operation " operation)
                    {:operation operation}))))

(defn- string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- vector-of-maps? [value]
  (and (vector? value) (every? map? value)))
(defn- keyword-string-map? [value]
  (and (map? value)
       (every? (fn [[key item]] (and (keyword? key) (string? item))) value)))

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C9 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid-fns (seq (for [[key value] (select-keys operations function-operation-keys)
                               :when (not (fn? value))] key))]
    (when unknown
      (throw (ex-info "C9 operation map contains unknown keys"
                      {:unknown-keys (vec unknown) :allowed-keys operation-keys})))
    (when invalid-fns
      (throw (ex-info "C9 function operation values must be functions"
                      {:non-function-keys (vec invalid-fns)}))))
  (doseq [[key predicate expected]
          [[:c9-ownership-diagnostic-ids string-vector? :non-empty-string-vector]
           [:c9-ownership-governing-document #(and (string? %) (seq %)) :non-empty-string]
           [:c9-ownership-rejected-designs vector-of-maps? :vector-of-maps]
           [:c9-ownership-override-diagnostics keyword-string-map? :keyword-to-string-map]]
          :when (and (contains? operations key) (not (predicate (get operations key))))]
    (throw (ex-info "C9 scalar operation has an invalid shape"
                    {:key key :expected expected :actual (get operations key)})))
  operations)
