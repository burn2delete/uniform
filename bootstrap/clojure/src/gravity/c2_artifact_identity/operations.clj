(ns gravity.c2-artifact-identity.operations)

(def function-operation-keys
  #{:sha256-hex :c2-form-graph-metrics :c2-reader-fail! :source-span
    :reader-canonical-value :reader-canonical-hash :c2-semantic-form-hash-input
    :c2-path-neutral-span :c2-token-hash-input :c2-form-hash-input
    :c2-syntax-seed-hash-input :c2-extension-hash-input
    :c2-diagnostic-hash-input :c2-incremental-hashes
    :c2-reader-product-integrity-record :c2-reader-artifact-id})

(def scalar-operation-keys #{:max-reader-form-graph-depth})
(def operation-keys (into function-operation-keys scalar-operation-keys))

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C2 artifact-identity operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C2 artifact-identity operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [key function-operation-keys :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "C2 artifact-identity operation must be a function"
                      {:operation key :value (get operations key)}))))
  (when (contains? operations :max-reader-form-graph-depth)
    (let [depth (:max-reader-form-graph-depth operations)]
      (when-not (and (integer? depth) (pos? depth))
        (throw (ex-info "C2 artifact-identity depth limit must be positive"
                        {:operation :max-reader-form-graph-depth :value depth})))))
  operations)
