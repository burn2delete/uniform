

(defn p15-s23-source-syntax-c2-artifact
  ([source-path source-text]
   (p15-s23-source-syntax-c2-artifact
    source-path source-text (reader-project-context-for-source source-path)))
  ([source-path source-text project-context]
   (let [authoritative
         (compiler-c2-reader-source-artifact source-path source-text
                                             project-context)
         proof (p15-s23-source-syntax-c2-capability-proof authoritative)
         lexical-validation (:lexical-product-validation authoritative)
         _ (when-not (and (:lexical-token-stream? proof)
                          (:nested-form-tree? proof))
             (p15-s23-source-syntax-serialization-fail!
              "P15S23S003" source-path lexical-validation
              {:missing-fields [:lexical-token-stream :nested-form-tree]}))
         artifact
         (assoc authoritative
                :task "P15-S23"
                :p15-compatibility-pass
                {:name :p15-s23-source-unit-reader-proof
                 :input :gravity-source-bytes
                 :output :authenticated-sh03-c2-reader-products
                 :authoritative-result? true
                 :legacy-reader-constructor-invoked? false}
                :p15-s23-source-syntax-reader-results
                {:source-unit-status :complete
                 :source-map-status :complete
                 :incremental-hash-status :complete
                 :token-stream-status :complete-for-slice
                 :form-tree-status :complete-for-slice
                 :abbreviation-fixture-status
                 :not-required-for-p15-source-proof
                 :status :partial}
                :p15-s23-capability-based-proof proof)]
     (assoc artifact :artifact-id (c2-reader-artifact-id artifact)))))