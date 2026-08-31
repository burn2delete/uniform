

(defn sh04-syntax-resolved-result!
  [source-path c2-artifact]
  (let [integrity-report
        (c3-validate-c2-reader-artifact! source-path c2-artifact)
        binding (sh04-syntax-current-binding! source-path)
        source-unit (:source-unit-record c2-artifact)
        semantic-source-id
        (sh04-syntax-semantic-source-id source-path source-unit)
        reader-authentication
        (sh04-syntax-reader-binding
         source-path c2-artifact semantic-source-id)
        reader-binding (:reader-binding reader-authentication)
        reader-source-revision
        (:reader-source-revision reader-authentication)
        registered-literals
        (sh04-syntax-registered-literal-registry
         source-path c2-artifact)
        top-level-products (c2-top-level-products c2-artifact)
        seeds (:syntax-seed-stream c2-artifact)
        base-products
        (mapv
         (fn [seed {:keys [form-record]}]
           (let [descriptor
                 (sh04-syntax-source-descriptor
                 seed form-record c2-artifact integrity-report
                  semantic-source-id reader-binding reader-source-revision
                  registered-literals)
                 raw
                 (sh04-syntax-execute!
                  source-path binding 'c3-syntax-build-template [descriptor])]
             (sh04-syntax-resolve-template!
              source-path binding raw reader-binding
              reader-source-revision)))
         seeds top-level-products)
        _ (when (empty? base-products)
            (sh04-syntax-boundary-fail!
             "C3-SHAPE" source-path :nonempty-syntax-object-stream
             base-products {}))
        generated-product
        (sh04-syntax-generated-products!
         source-path binding (:syntax (first base-products))
         semantic-source-id reader-binding reader-source-revision)
        all-products (conj base-products generated-product)
        resolved-products
        (mapv (fn [product]
                {:syntax-object (:syntax product)
                 :digest-requests
                 (get-in product [:raw-result :digest-requests])
                 :resolved-digests (:resolved-digests product)})
              all-products)
        root-syntax-ids (mapv #(get-in % [:syntax :syntax-id])
                              base-products)
        stream-template-result
        (sh04-syntax-execute!
         source-path binding 'c3-syntax-stream-build-template
         [resolved-products reader-binding reader-source-revision
          root-syntax-ids])
        _ (sh04-syntax-require-carrier!
           source-path :gravity-syntax-stream-template
           stream-template-result)
        _ (when-not (and (= :gravity/sh04-syntax-stream-template-result
                            (:artifact stream-template-result))
                         (= :accepted (:status stream-template-result))
                         (= 1 (count (:digest-requests
                                     stream-template-result)))
                         (= false
                            (get-in stream-template-result
                                    [:containment
                                     :downstream-artifacts-forbidden])))
            (sh04-syntax-boundary-fail!
             "C3-ID" source-path :gravity-syntax-stream-template
             stream-template-result {}))
        stream-requests (:digest-requests stream-template-result)
        stream-digests
        (reduce
         (fn [resolved request]
           (let [ordinal (:ordinal request)
                 resolved-preimage
                 (sh04-syntax-resolve-stream-request-preimage!
                  source-path request resolved)]
             (when-not (= ordinal (count resolved))
               (sh04-syntax-boundary-fail!
                "C3-ID" source-path :ordered-syntax-stream-digest-requests
                request {:resolved-count (count resolved)}))
             (conj resolved
                   (p15-s23-c6c10-canonical-digest
                    source-path resolved-preimage))))
         [] stream-requests)
        resolved-stream
        (sh04-syntax-resolve-stream-template!
         source-path (:stream-template stream-template-result)
         stream-digests)
        _ (sh04-syntax-require-carrier!
           source-path :resolved-gravity-syntax-stream resolved-stream)
        stream-verification
        (sh04-syntax-execute!
         source-path binding 'c3-syntax-stream-verify-resolved
         [resolved-stream stream-requests stream-digests])
        _ (sh04-syntax-require-carrier!
           source-path :gravity-syntax-stream-verification
           stream-verification)
        _ (when-not (and
                     (= :gravity/sh04-resolved-syntax-stream-verification-report
                        (:artifact stream-verification))
                     (= :passed (:status stream-verification))
                     (= false
                        (get-in stream-verification
                                [:containment
                                 :downstream-artifacts-forbidden])))
            (sh04-syntax-boundary-fail!
             "C3-ID" source-path :fresh-resolved-syntax-stream-verification
             stream-verification {}))
        serialization
        (sh04-syntax-execute!
         source-path binding 'c3-syntax-serialize-template
         [resolved-stream stream-requests stream-digests])
        _ (sh04-syntax-require-carrier!
           source-path :gravity-syntax-stream-serialization serialization)
        _ (when-not (= :accepted (:status serialization))
            (sh04-syntax-boundary-fail!
             "C3-SERIALIZE" source-path :gravity-syntax-stream-serialization
             serialization {}))
        deserialization
        (sh04-syntax-execute!
         source-path binding 'c3-syntax-deserialize-template
         [(:carrier serialization)])
        _ (sh04-syntax-require-carrier!
           source-path :gravity-syntax-stream-deserialization deserialization)
        _ (when-not (and (= :accepted (:status deserialization))
                         (= (:semantic-payload serialization)
                            (:semantic-payload deserialization)))
            (sh04-syntax-boundary-fail!
             "C3-SERIALIZE" source-path :gravity-syntax-stream-round-trip
             deserialization {}))
        serialization-id
        (p15-s23-c6c10-canonical-digest
         source-path (:payload-id-request serialization))
        resolved-syntax (:syntax-object-stream resolved-stream)
        rich-syntax
        (mapv sh04-syntax-rich-object resolved-syntax
              (repeat serialization-id))
        graph-report (:graph-verification-report resolved-stream)
        syntax-result-id (:artifact-id resolved-stream)
        summary
        {:slice :SH-04 :status :accepted
         :adapter-contract sh04-syntax-adapter-contract
         :semantic-source-id semantic-source-id
         :reader-binding-id (reader-canonical-hash reader-binding)
         :reader-source-revision-id (:revision-id reader-source-revision)
         :syntax-stream-id syntax-result-id
         :serialization-set-id serialization-id
         :graph-id (reader-canonical-hash graph-report)
         :syntax-result-id syntax-result-id}
        descriptor (sh04-syntax-sh02-descriptor source-path binding summary)
        envelope
        (p15-s23-stage2-sh02-descriptor-envelope
         sh04-syntax-envelope-stage sh04-syntax-sealed-artifact-kind
         descriptor source-path)
        _ (p15-s23-stage2-sh02-descriptor-envelope-verify!
           envelope sh04-syntax-envelope-stage
           sh04-syntax-sealed-artifact-kind descriptor source-path)
        reader-authentication-provenance
        {:actual-c2-artifact-id (:artifact-id c2-artifact)
         :actual-reader-product-integrity-hash
         (get-in c2-artifact
                 [:reader-product-integrity :integrity-hash])
         :actual-reader-source-id (:source-id source-unit)
         :actual-sh03-semantic-product-binding
         (sh04-syntax-descriptor-sh03-product-binding
          (get-in c2-artifact
                  [:gravity-reader-boundary
                   :authenticated-envelope-descriptor]))
         :actual-sh03-reader-result-id
         (get-in c2-artifact
                 [:gravity-reader-boundary :resolved-reader-result
                  :incremental-reader-hashes :reader-result])
         :actual-sh03-semantic-envelope-id
         (get-in c2-artifact
                 [:gravity-reader-boundary :authenticated-envelope
                  :semantic-envelope-id])
         :actual-sh03-provenance-binding-id
         (get-in c2-artifact
                 [:gravity-reader-boundary :authenticated-envelope
                  :provenance-binding-id])
         :actual-sh03-authenticated-envelope
         (get-in c2-artifact
                 [:gravity-reader-boundary :authenticated-envelope])
         :actual-sh03-envelope-descriptor
         (get-in c2-artifact
                 [:gravity-reader-boundary
                  :authenticated-envelope-descriptor])
         :semantic-source-id semantic-source-id}]
    {:resolved-result resolved-stream
     :summary summary
     :descriptor descriptor
     :envelope envelope
     :plan-binding (dissoc binding :plan :source-text)
     :rich-syntax rich-syntax
     :raw-template-results (mapv :raw-result all-products)
     :stream-template-result stream-template-result
     :stream-digest-requests stream-requests
     :stream-resolved-digests stream-digests
     :stream-verification stream-verification
     :serialization serialization
     :deserialization deserialization
     :serialization-id serialization-id
     :reader-binding reader-binding
     :reader-source-revision reader-source-revision
     :reader-authentication-provenance
     reader-authentication-provenance}))