

(defn c2-reader-products-clojure-oracle
  ([source-path source-text reader-options]
   (c2-reader-products-clojure-oracle
    source-path source-text reader-options
    (reader-project-context-for-source source-path)))
  ([source-path source-text reader-options project-context]
   (let [source-unit (c2-source-unit-record source-path source-text
                                            reader-options project-context)
         table (stage1-reader-table)]
     (try
       (let [stage1-stream (stage1-reader-token-stream source-path source-text
                                                       table reader-options)
             _ (c2-prevalidate-token-depth!
                source-path source-unit (:tokens stage1-stream))
             token-stream (mapv #(c2-token-record % source-unit)
                                (:tokens stage1-stream))
             parser-stream (assoc stage1-stream :tokens token-stream)
             parsed (stage1-reader-products-from-token-stream
                     source-path source-text table parser-stream)
             form-tree (mapv #(c2-form-record % source-unit)
                             (:form-tree parsed))]
         {:source-unit source-unit
          :token-stream token-stream
          :form-tree form-tree
          :root-form-ids (:root-form-ids parsed)
          :parsed-records (:records parsed)
          :parsed-values (:parsed-values parsed)})
       (catch StackOverflowError error
         (c2-reader-fail!
          "C2-HASH" source-path
          {:stage :read-source
           :source-id (:source-id source-unit)
           :source-span (source-span source-path 0)
           :reader-options reader-options}
          {:missing-fields [:stack-safe-reader-form-construction]
           :facts {:maximum-form-depth max-reader-form-depth
                   :failure-kind :reader-resource-depth-limit
                   :contained-host-error (.getName (class error))}}))
       (catch clojure.lang.ExceptionInfo ex
         (throw
          (ex-info (.getMessage ex)
                   (merge {:source-id (:source-id source-unit)
                           :reader-options reader-options}
                          (ex-data ex))
                   ex)))))))

(defn c2-reader-products
  ([source-path source-text reader-options]
   (c2-reader-products source-path source-text reader-options
                       (reader-project-context-for-source source-path)))
  ([source-path source-text reader-options project-context]
   (let [source-bytes (.getBytes source-text
                                 java.nio.charset.StandardCharsets/UTF_8)
         resolved (sh03-reader-resolved-result!
                   source-path source-bytes project-context reader-options)]
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