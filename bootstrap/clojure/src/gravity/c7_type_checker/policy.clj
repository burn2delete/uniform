(ns gravity.c7-type-checker.policy
  "Operation-map policy for the hosted C7 interposition facade.")

(def function-operation-keys
  #{:fail! :source-span :c4-artifact-id :read-source-form-records
    :validate-ns-syntax! :parse-module
    :compiler-c6-lowering-source-artifact
    :c7-type-source-overrides :c7-type-message :c7-type-fail!
    :c7-type-validate-overrides! :c7-literal-type :c7-node-operator
    :c7-node-type :c7-type-fact :c7-type-environment
    :c7-constraint-ledger :c7-function-table
    :c7-dynamic-boundary-records :c7-cast-records
    :c7-generic-instantiations :c7-protocol-dispatch-table
    :c7-schema-links :c7-layout-facts :c7-type-diagnostics
    :c7-typed-core-verifier-report :c7-type-capability-proof
    :c7-type-validate! :compiler-c7-type-source-artifact
    :compiler-c7-type-file-artifact})

(def scalar-operation-keys
  #{:c7-type-diagnostic-ids :c7-type-governing-document
    :c7-type-rejected-designs :c7-type-override-diagnostics})

(def operation-keys
  (into function-operation-keys scalar-operation-keys))

(defn unsupported-host-operation
  [operation]
  (fn [& _]
    (throw (ex-info (str "C7 leaf requires injected operation " operation)
                    {:operation operation}))))

(defn validate-operations!
  [operations operation-keys function-operation-keys scalar-predicates]
  (when-not (map? operations)
    (throw (ex-info "C7 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        non-functions
        (seq (for [[key value] (select-keys operations
                                            function-operation-keys)
                   :when (not (fn? value))]
               key))]
    (when unknown
      (throw (ex-info "C7 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)
                       :allowed-keys operation-keys})))
    (when non-functions
      (throw (ex-info "C7 function operation values must be functions"
                      {:non-function-keys (vec non-functions)}))))
  (doseq [[key predicate expected] scalar-predicates
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C7 scalar operation has an invalid shape"
                    {:key key :expected expected
                     :actual (get operations key)})))
  operations)
