

(defn c2-reader-products-clojure-oracle
  ([source-path source-text reader-options]
   (c2-reader-products-clojure-oracle
    source-path source-text reader-options
    (reader-project-context-for-source source-path)))
  ([source-path source-text reader-options project-context]
   (gravity.reader/compatibility-reader-products
    source-path source-text reader-options project-context
    {:source-unit-record-var #'c2-source-unit-record
     :reader-table-var #'stage1-reader-table
     :token-stream-var #'stage1-reader-token-stream
     :token-record-var #'c2-token-record
     :parsed-products-var #'stage1-reader-products-from-token-stream
     :form-record-var #'c2-form-record
     :reader-fail-var #'c2-reader-fail!
     :source-span-var #'source-span})))

(defn c2-reader-products
  ([source-path source-text reader-options]
   (c2-reader-products source-path source-text reader-options
                       (reader-project-context-for-source source-path)))
  ([source-path source-text reader-options project-context]
   (c2-reader-products source-path source-text reader-options project-context nil))
  ([source-path source-text reader-options project-context reader-source-path]
   (let [source-bytes (.getBytes source-text
                                 java.nio.charset.StandardCharsets/UTF_8)
         resolved (sh03-reader-resolved-result!
                   source-path source-bytes project-context reader-options reader-source-path)]
     (sh03-reader-adapt-products!
      source-path source-text source-bytes reader-options project-context
      resolved))))

(declare c2-syntax-seed-stream
         c2-deferred-semantic-literals
         c2-top-level-products
         c2-reader-capability-proof
         c2-reader-overrides-from-forms
         c2-reader-extension-invocations)

(defn- c2-reader-product-projection-ops
  []
  {:syntax-object-stream syntax-object-stream
   :c2-literal-records c2-literal-records
   :c2-reader-diagnostic-ids c2-reader-diagnostic-ids
   :standard-reader-policy standard-reader-policy
   :c2-syntax-seed-stream c2-syntax-seed-stream
   :c2-deferred-semantic-literals c2-deferred-semantic-literals
   :c2-top-level-products c2-top-level-products
   :c2-reader-capability-proof c2-reader-capability-proof
   :c2-reader-overrides-from-forms c2-reader-overrides-from-forms
   :c2-reader-extension-invocations c2-reader-extension-invocations})
