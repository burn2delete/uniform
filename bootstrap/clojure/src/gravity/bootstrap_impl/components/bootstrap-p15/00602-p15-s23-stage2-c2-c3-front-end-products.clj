

(defn p15-s23-stage2-c2-c3-front-end-products
  "Build the stage2 source-front-end view from the authoritative C2/C3
  reader products.  The old P15 parser remains available for its direct
  compatibility probes, but stage2 source ingress must not re-read source
  through that simplified parser."
  ([source-path source-text]
   (p15-s23-stage2-c2-c3-front-end-products
    source-path source-text
    (reader-project-context-for-source source-path)))
  ([source-path source-text project-context]
   (p15-s23-stage2-c2-c3-front-end-products
    source-path source-text project-context false))
  ([source-path source-text project-context retain-authenticated-artifacts?]
  (let [c3-artifact (compiler-c3-syntax-source-artifact
                     source-path source-text project-context)
        c2-artifact (:c2-reader-artifact c3-artifact)
        root-form-ids (:top-level-form-ids c2-artifact)
        rich-syntax (vec (take (count root-form-ids)
                               (:syntax-object-stream c3-artifact)))
        records (p15-s23-stage2-c2-c3-records c2-artifact rich-syntax)
        forms (mapv :form records)]
    (cond->
     {:artifact :gravity/p15-s23-stage2-c2-c3-front-end-products
      :source-path source-path
      :source-text source-text
      :source-unit-record (:source-unit-record c2-artifact)
      :source-unit-id (get-in c2-artifact [:source-unit-record :source-id])
      :token-stream (:token-stream c2-artifact)
      :form-tree (:form-tree c2-artifact)
      :top-level-form-ids root-form-ids
      :syntax-seed-stream (:syntax-seed-stream c2-artifact)
      :reader-source-map (:reader-source-map c2-artifact)
      :literal-decoding-records (:literal-decoding-records c2-artifact)
      :semantic-error-deferment-record
      (:semantic-error-deferment-record c2-artifact)
      :reader-extension-invocation-records
      (:reader-extension-invocation-records c2-artifact)
      :reader-diagnostics (:reader-diagnostics c2-artifact)
      :incremental-reader-hashes (:incremental-reader-hashes c2-artifact)
      :reader-product-integrity (:reader-product-integrity c2-artifact)
      :c2-reader-artifact (:c2-reader-artifact c3-artifact)
      :c3-artifact-id (:artifact-id c3-artifact)
      :c3-syntax-object-stream rich-syntax
      :c3-capability-proof (:capability-based-proof c3-artifact)
      :records records
      :forms forms
      :status :complete}
      retain-authenticated-artifacts?
      (assoc :authenticated-c3-source-artifact c3-artifact)))))