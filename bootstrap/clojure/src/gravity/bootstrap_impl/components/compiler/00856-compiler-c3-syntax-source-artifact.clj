

(defn compiler-c3-syntax-source-artifact
  ([source-path source-text]
   (compiler-c3-syntax-source-artifact
    source-path source-text (reader-project-context-for-source source-path)))
  ([source-path source-text project-context]
   (compiler-c3-syntax-source-artifact
    source-path source-text project-context nil))
  ([source-path source-text project-context precomputed-c2-artifact]
   (if precomputed-c2-artifact
     (c2-reader-fail!
      "C2-HASH" source-path
      {:stage :read-source
       :source-span (source-span source-path 0)
       :reader-options standard-reader-options}
      {:missing-fields [:internal-sh03-precomputed-product-authority]})
     (compiler-c3-syntax-source-artifact
      source-path source-text project-context nil
      sh03-reader-internal-product-authority)))
  ([source-path source-text project-context precomputed-c2-artifact candidate]
  (let [c2-artifact
        (if precomputed-c2-artifact
          (sh03-c3-precomputed-c2-verify!
           candidate source-path precomputed-c2-artifact)
          (compiler-c2-reader-source-artifact
           source-path source-text project-context))
        integrity-report
        (c3-validate-c2-reader-artifact! source-path c2-artifact)
        forms (:parsed-semantic-values c2-artifact)
        overrides (c3-syntax-overrides-from-forms forms)
        _ (c3-syntax-validate-overrides! source-path overrides)
        sh04-products (sh04-syntax-resolved-result! source-path c2-artifact)
        syntax-stream (:rich-syntax sh04-products)
        sh04-result (:resolved-result sh04-products)
        gravity-boundary
        {:slice :SH-04
         :owner :gravity-source
         :plan-binding (:plan-binding sh04-products)
         :reader-semantic-binding (:reader-binding sh04-products)
         :reader-source-revision
         (:reader-source-revision sh04-products)
         :reader-authentication-provenance
         (:reader-authentication-provenance sh04-products)
         :resolved-syntax-result sh04-result
         :resolved-stream-verification
         (:stream-verification sh04-products)
         :stream-digest-requests
         (:stream-digest-requests sh04-products)
         :stream-resolved-digests
         (:stream-resolved-digests sh04-products)
         :gravity-syntax-serialization (:serialization sh04-products)
         :gravity-syntax-deserialization (:deserialization sh04-products)
         :adapter-contract sh04-syntax-adapter-contract
         :authenticated-envelope (:envelope sh04-products)
         :authenticated-envelope-descriptor (:descriptor sh04-products)
         :uncredited-compatibility-facade
         {:module 'gravity.compiler.c3-syntax-object-model
          :source-path sh04-syntax-facade-relative-path
          :status :compatibility-only
          :authentication-credit? false
          :authoritative-result? false
          :self-hosting-credit? false
          :seed-retirement-credit? false
          :release-credit? false}
         :target-source-reread? false
         :clojure-adapter-residual? true
         :self-hosted? false}
        serialization (c3-syntax-serialization-fixture syntax-stream)
        artifact-base {:kind :gravity/stage0-c3-syntax-object-artifact
                       :task "P06-D082"
                       :document-set ["C3"]
                       :governing-document c3-syntax-governing-document
                       :pass {:name :c3-syntax-object-model
                              :input :c2-reader-document-artifact
                              :output :syntax-object-model-proof
                              :requires [:source-unit-record
                                         :syntax-seed-stream
                                         :reader-source-map]
                              :preserves [:source-spans :generated-origin
                                          :namespace-context :profile
                                          :metadata :hygiene]
                              :emits [:syntax-object-schema
                                      :syntax-object-stream
                                      :hygiene-context-map
                                      :origin-chain-graph
                                      :metadata-ledger
                                      :generated-syntax-report
                                      :syntax-verification-report
                                      :syntax-serialization-fixture]
                              :rejects c3-syntax-diagnostic-ids}
                       :source-overrides overrides
                       :gravity-syntax-boundary gravity-boundary
                       :c2-reader-artifact
                       (c3-reader-artifact-view c2-artifact)
                       :syntax-object-schema (c3-syntax-schema)
                       :syntax-object-stream syntax-stream
                       :gravity-hygiene-context-map
                       (:hygiene-context-map sh04-result)
                       :gravity-metadata-ledger
                       (:metadata-ledger sh04-result)
                       :gravity-fact-invalidation-ledger
                       (:fact-invalidation-ledger sh04-result)
                       :gravity-origin-chain-graph
                       (:origin-chain-graph sh04-result)
                       :gravity-syntax-ownership-product
                       (:ownership-product sh04-result)
                       :hygiene-context-map (c3-hygiene-context-map
                                             syntax-stream)
                       :origin-chain-graph (c3-origin-chain-graph
                                            syntax-stream)
                       :metadata-ledger (c3-metadata-ledger syntax-stream)
                       :generated-syntax-report (c3-generated-syntax-report
                                                 syntax-stream)
                       :fact-ledger (c3-fact-ledger syntax-stream)
                       :syntax-serialization-fixture serialization
                       :syntax-verification-report
                       (c3-syntax-verification-report syntax-stream
                                                      serialization
                                                      c2-artifact
                                                      gravity-boundary)
                       :rejected-design-coverage c3-syntax-rejected-designs
                       :diagnostics []}
        _ (c3-syntax-validate! source-path artifact-base)
        capability-proof (c3-syntax-capability-proof artifact-base)
        conformance {:documents ["C3"]
                     :task "P06-D082"
                     :required-diagnostic-ids c3-syntax-diagnostic-ids
                     :schema-status :complete
                     :stream-status :complete
                     :origin-status :complete
                     :hygiene-status :complete
                     :capture-status :complete
                     :metadata-status :complete
                     :fact-invalidation-status :complete
                     :serialization-status :complete
                     :diagnostic-status :complete
                     :status :complete}
        artifact (assoc artifact-base
                        :capability-based-proof capability-proof
                        :c3-syntax-results conformance)]
    (assoc artifact :artifact-id (c3-artifact-id artifact)))))

(defn compiler-c3-syntax-file-artifact
  [path]
  (let [c2-artifact (compiler-c2-reader-file-artifact path)
        source-text (c2-reader-artifact-source-text path c2-artifact)]
    (compiler-c3-syntax-source-artifact
     path source-text (reader-project-context-for-source path) c2-artifact
     sh03-reader-internal-product-authority)))