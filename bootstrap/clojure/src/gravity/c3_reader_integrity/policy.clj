(ns gravity.c3-reader-integrity.policy
  "Operation and namespace policy for hosted C3 reader integrity.")

(def function-operation-keys
  #{:c2-lexical-product-validation
    :c2-incremental-hashes
    :c2-literal-records
    :c2-deferred-semantic-literals
    :c3-deferred-ratio-descriptor-from-raw
    :c2-reader-product-integrity-record
    :reader-canonical-hash
    :sha256-hex
    :c2-reader-artifact-id
    :c3-c2-reader-integrity-report
    :c3-validate-c2-reader-artifact!
    :c3-syntax-fail!
    :source-span})

(def scalar-operation-keys #{:max-reader-form-graph-depth})
(def operation-keys (into function-operation-keys scalar-operation-keys))

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C3 reader-integrity operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C3 reader-integrity operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [key function-operation-keys
          :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "C3 reader-integrity operation must be a function"
                      {:operation key :value (get operations key)}))))
  (when (contains? operations :max-reader-form-graph-depth)
    (let [depth (:max-reader-form-graph-depth operations)]
      (when-not (and (integer? depth) (pos? depth))
        (throw (ex-info "C3 reader-integrity depth limit must be positive"
                        {:operation :max-reader-form-graph-depth
                         :value depth})))))
  operations)

(def namespace-contract
  {:namespace 'gravity.c3-reader-integrity
   :contract-boundary :hosted-c3-c2-reader-input-integrity
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'c3-c2-reader-integrity-report {:arglists '([c2-artifact])}
    'c3-validate-c2-reader-artifact!
    {:arglists '([source-path c2-artifact])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true}
   :artifact-inputs [:hosted-c2-reader-document-artifact]
   :artifact-outputs [:hosted-c3-reader-input-integrity-report]
   :ownership
   {:owns [:hosted-c3-reader-input-integrity-recomputation
           :hosted-c3-stale-reader-input-rejection]
    :does-not-own [:canonical-c2-reader-authority
                   :sh03-reader-product-authentication
                   :sh04-syntax-boundary-authentication
                   :source-reading
                   :diagnostic-construction
                   :canonical-c3-syntax-object-authority
                   :proof-authority
                   :attestation-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c2-authority? false
   :canonical-c3-authority? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false})
