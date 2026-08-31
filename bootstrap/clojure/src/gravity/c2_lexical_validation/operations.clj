(ns gravity.c2-lexical-validation.operations)

(def operation-keys
  #{:c2-utf8-slice :c2-span-encloses? :c2-spans-source-ordered?
    :c2-form-graph-metrics :c2-lexical-product-validation})

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C2 lexical-validation operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C2 lexical-validation operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [key operation-keys :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "C2 lexical-validation operation must be a function"
                      {:operation key :value (get operations key)}))))
  operations)
