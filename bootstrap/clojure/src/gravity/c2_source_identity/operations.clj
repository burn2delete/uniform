(ns gravity.c2-source-identity.operations)

(def function-operation-keys
  #{:sha256-hex
    :reader-canonical-hash
    :gravity-source-extension
    :gravity-source-kind
    :reader-normalize-relative-path
    :reader-platform-neutral-absolute-path?
    :reader-valid-project-relative-path?
    :reader-explicit-project-context
    :reader-valid-options?
    :reader-validate-options!
    :reader-project-root-record
    :reader-source-identity-inputs
    :c2-source-unit-record
    :c2-token-record
    :c2-form-record
    :c2-literal-records
    :c2-trivia-records})

(def operation-keys function-operation-keys)

(defn validate-operations!
  [operations]
  (when-not (map? operations)
    (throw (ex-info "C2 source-identity operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C2 source-identity operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [key function-operation-keys
          :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "C2 source-identity operation must be a function"
                      {:operation key :value (get operations key)}))))
  operations)
