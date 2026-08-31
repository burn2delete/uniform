

(defn c3-syntax-stream-reader-products-authentic?
  ([syntax-stream c2-artifact]
   (c3-syntax-stream-reader-products-authentic?
    syntax-stream c2-artifact nil))
  ([syntax-stream c2-artifact gravity-boundary]
   (try
     (if (:registered-literal-projection c2-artifact)
       (if gravity-boundary
         (let [result (:resolved-syntax-result gravity-boundary)
               result-stream (:syntax-object-stream result)
               source-path
               (get-in c2-artifact [:source-unit-record :path])
               serialization-id
               (p15-s23-c6c10-canonical-digest
                source-path
                (get-in gravity-boundary
                        [:gravity-syntax-serialization
                         :payload-id-request]))
               expected-rich-stream
               (mapv sh04-syntax-rich-object result-stream
                     (repeat serialization-id))]
           (and
            (sh04-syntax-registered-literal-projection-authentic?
             c2-artifact gravity-boundary)
            (= :accepted (:status result))
            (= syntax-stream expected-rich-stream)
            (= :passed
               (get-in gravity-boundary
                       [:resolved-stream-verification :status]))
            (= :accepted
               (get-in gravity-boundary
                       [:gravity-syntax-serialization :status]))
            (= :accepted
               (get-in gravity-boundary
                       [:gravity-syntax-deserialization :status]))))
         false)
       (let [integrity-report (c3-c2-reader-integrity-report c2-artifact)]
        (if gravity-boundary
         (let [result (:resolved-syntax-result gravity-boundary)
               result-stream (:syntax-object-stream result)
               source-path (get-in c2-artifact
                                   [:source-unit-record :path])
               semantic-source-id
               (sh04-syntax-semantic-source-id
                source-path (:source-unit-record c2-artifact))
               expected-reader-authentication
               (sh04-syntax-reader-binding
                source-path c2-artifact semantic-source-id)
               sh03-authentication
               (or (:sh03-reader-authentication c2-artifact)
                   (let [reader-boundary
                         (:gravity-reader-boundary c2-artifact)]
                     {:reader-result-id
                      (get-in reader-boundary
                              [:resolved-reader-result
                               :incremental-reader-hashes
                               :reader-result])
                      :semantic-envelope-id
                      (get-in reader-boundary
                              [:authenticated-envelope
                               :semantic-envelope-id])
                      :provenance-binding-id
                      (get-in reader-boundary
                              [:authenticated-envelope
                               :provenance-binding-id])}))
               reader-authentication-provenance
               (:reader-authentication-provenance gravity-boundary)
               actual-sh03-envelope
               (:actual-sh03-authenticated-envelope
                reader-authentication-provenance)
               actual-sh03-envelope-descriptor
               (:actual-sh03-envelope-descriptor
                reader-authentication-provenance)
               expected-sh03-product-binding
               (sh04-syntax-current-sh03-product-binding c2-artifact)
               actual-sh03-product-binding
               (sh04-syntax-descriptor-sh03-product-binding
                actual-sh03-envelope-descriptor)
               actual-sh03-envelope-verification
               (p15-s23-stage2-sh02-descriptor-envelope-verify!
                actual-sh03-envelope :c2-reader
                :gravity/sh03-reader-products
                actual-sh03-envelope-descriptor source-path)
               binding (sh04-syntax-current-binding! source-path)
               requests (:stream-digest-requests gravity-boundary)
               digests (:stream-resolved-digests gravity-boundary)
               fresh-stream-verification
               (sh04-syntax-execute!
                source-path binding 'c3-syntax-stream-verify-resolved
                [result requests digests])
               fresh-serialization
               (sh04-syntax-execute!
                source-path binding 'c3-syntax-serialize-template
                [result requests digests])
               fresh-deserialization
               (sh04-syntax-execute!
                source-path binding 'c3-syntax-deserialize-template
                [(:carrier fresh-serialization)])
               serialization-id
               (p15-s23-c6c10-canonical-digest
                source-path
                (get-in gravity-boundary
                        [:gravity-syntax-serialization
                         :payload-id-request]))
               expected-rich-stream
               (mapv sh04-syntax-rich-object result-stream
                     (repeat serialization-id))
               source-syntax
               (filterv #(not= :generated-form (get-in % [:form :kind]))
                        syntax-stream)
               generated-syntax
               (filterv #(= :generated-form (get-in % [:form :kind]))
                        syntax-stream)
               expected-form-ids
               (mapv :form-id (:syntax-seed-stream c2-artifact))
               observed-form-ids
               (mapv #(get-in % [:source :form-id]) source-syntax)
               envelope (:authenticated-envelope gravity-boundary)
               envelope-descriptor
               (:authenticated-envelope-descriptor gravity-boundary)
               expected-summary
               {:slice :SH-04 :status :accepted
                :adapter-contract sh04-syntax-adapter-contract
                :semantic-source-id semantic-source-id
                :reader-binding-id
                (reader-canonical-hash
                 (:reader-binding expected-reader-authentication))
                :reader-source-revision-id
                (get-in expected-reader-authentication
                        [:reader-source-revision :revision-id])
                :syntax-stream-id (:artifact-id result)
                :serialization-set-id serialization-id
                :graph-id
                (reader-canonical-hash
                 (:graph-verification-report result))
                :syntax-result-id (:artifact-id result)}
               expected-envelope-descriptor
               (sh04-syntax-sh02-descriptor source-path binding
                                            expected-summary)
               expected-envelope
               (p15-s23-stage2-sh02-descriptor-envelope
                :c3-syntax :gravity/sh04-syntax-products
                expected-envelope-descriptor source-path)
               envelope-verification
               (p15-s23-stage2-sh02-descriptor-envelope-verify!
                envelope :c3-syntax :gravity/sh04-syntax-products
                envelope-descriptor source-path)]
           (and (:authentic? integrity-report)
                (= :SH-04 (:slice gravity-boundary))
                (= :gravity-source (:owner gravity-boundary))
                (= :accepted (:status result))
                (= syntax-stream expected-rich-stream)
                (= (:reader-binding expected-reader-authentication)
                   (:reader-semantic-binding gravity-boundary))
                (= (:reader-source-revision
                    expected-reader-authentication)
                   (:reader-source-revision gravity-boundary))
                (= (:reader-result-id sh03-authentication)
                   (:actual-sh03-reader-result-id
                    reader-authentication-provenance))
                (= (:semantic-envelope-id sh03-authentication)
                   (:actual-sh03-semantic-envelope-id
                    reader-authentication-provenance))
                (= (:provenance-binding-id sh03-authentication)
                   (:actual-sh03-provenance-binding-id
                    reader-authentication-provenance))
                (= :passed actual-sh03-envelope-verification)
                (= expected-sh03-product-binding
                   actual-sh03-product-binding)
                (= expected-sh03-product-binding
                   (:actual-sh03-semantic-product-binding
                    reader-authentication-provenance))
                (= (:semantic-envelope-id actual-sh03-envelope)
                   (:actual-sh03-semantic-envelope-id
                    reader-authentication-provenance))
                (= (:provenance-binding-id actual-sh03-envelope)
                   (:actual-sh03-provenance-binding-id
                    reader-authentication-provenance))
                (= expected-form-ids observed-form-ids)
                (= 1 (count generated-syntax))
                (= (:reader-semantic-binding gravity-boundary)
                   (:reader-binding result))
                (= (:reader-source-revision gravity-boundary)
                   (:reader-source-revision result))
                (= :passed (:status fresh-stream-verification))
                (= fresh-stream-verification
                   (:resolved-stream-verification gravity-boundary))
                (= :accepted (:status fresh-serialization))
                (= fresh-serialization
                   (:gravity-syntax-serialization gravity-boundary))
                (= :accepted (:status fresh-deserialization))
                (= fresh-deserialization
                   (:gravity-syntax-deserialization gravity-boundary))
                (= (:semantic-payload fresh-serialization)
                   (:semantic-payload fresh-deserialization))
                (= expected-envelope-descriptor envelope-descriptor)
                (= expected-envelope envelope)
                (= :passed
                   (get-in result [:graph-verification-report :status]))
                (= :accepted (:status envelope))
                (= :c3-syntax (:stage envelope))
                (= :passed envelope-verification)))
         (let [source-unit (:source-unit-record c2-artifact)
               top-level-products (c2-top-level-products c2-artifact)
               expected-base
               (mapv (fn [seed {:keys [form-record token-record]}]
                       (c3-syntax-object seed form-record token-record
                                         source-unit c2-artifact
                                         integrity-report))
                     (:syntax-seed-stream c2-artifact)
                     top-level-products)
               expected-generated
               (c3-generated-syntax-object
                (or (some #(when (seq (:origin %)) %) expected-base)
                    (first expected-base)))
               expected-stream (conj expected-base expected-generated)]
           (and (:authentic? integrity-report)
                (= expected-stream syntax-stream))))))
     (catch StackOverflowError _ false)
     (catch Exception _ false))))

(declare c3-syntax-verification-report
         c3-syntax-capability-proof
         c3-syntax-validate!)

(defn- c3-syntax-verification-ops
  []
  {:c3-syntax-schema c3-syntax-schema
   :c3-resolvable-span? c3-resolvable-span?
   :c3-syntax-serialization-fixture c3-syntax-serialization-fixture
   :c3-syntax-stream-reader-products-authentic?
   c3-syntax-stream-reader-products-authentic?
   :c3-syntax-verification-report c3-syntax-verification-report
   :c3-syntax-capability-proof c3-syntax-capability-proof
   :c3-syntax-validate! c3-syntax-validate!
   :c3-syntax-fail! c3-syntax-fail!
   :c3-syntax-diagnostic-ids c3-syntax-diagnostic-ids})