

(defn sh04-syntax-resolve-template!
  [source-path binding raw reader-binding reader-source-revision]
  (sh04-syntax-raise-result! source-path raw)
  (let [requests (:digest-requests raw)
        request-count (count requests)
        _ (when-not (and (= :gravity/sh04-syntax-template-result
                            (:artifact raw))
                         (= 1 (:schema-version raw))
                         (= 2 request-count)
                         (= false
                            (get-in raw
                                    [:containment
                                     :downstream-artifacts-forbidden])))
            (sh04-syntax-boundary-fail!
             "C3-SHAPE" source-path :exact-syntax-template-result raw {}))
        verification
        (sh04-syntax-execute!
         source-path binding 'c3-syntax-verify-template
         [(:syntax-template raw) requests])
        _ (when-not (and (= :gravity/sh04-syntax-verification-report
                            (:artifact verification))
                         (= :passed (:status verification))
                         (= false
                            (get-in verification
                                    [:containment
                                     :downstream-artifacts-forbidden])))
            (sh04-syntax-boundary-fail!
             "C3-ID" source-path :fresh-gravity-syntax-template-replay
             verification {}))
        digests
        (reduce
         (fn [resolved request]
           (let [ordinal (:ordinal request)
                 resolved-preimage
                 (sh04-syntax-resolve-request-preimage!
                  source-path request resolved)]
             (when-not (= ordinal (count resolved))
               (sh04-syntax-boundary-fail!
                "C3-ID" source-path :ordered-syntax-digest-requests
                request {:resolved-count (count resolved)}))
             (conj resolved
                   (p15-s23-c6c10-canonical-digest
                    source-path resolved-preimage))))
         [] requests)
        syntax
        (sh04-syntax-resolve-object-template!
         source-path (:syntax-template raw) digests)
        resolved-verification
        (sh04-syntax-execute!
         source-path binding 'c3-syntax-verify-resolved
         [syntax requests digests reader-binding reader-source-revision])
        _ (when-not (and (= :gravity/sh04-resolved-syntax-verification-report
                            (:artifact resolved-verification))
                         (= :passed (:status resolved-verification))
                         (= (:semantic-binding-id reader-binding)
                            (first digests))
                         (= (:syntax-id syntax) (second digests))
                         (= false
                            (get-in resolved-verification
                                    [:containment
                                     :downstream-artifacts-forbidden])))
            (sh04-syntax-boundary-fail!
             "C3-ID" source-path :fresh-resolved-syntax-verification
             resolved-verification {}))]
    {:raw-result raw
     :verification-report verification
     :resolved-verification-report resolved-verification
     :resolved-digests digests
     :syntax syntax
     :serialization-id (:syntax-id syntax)}))

(defn sh04-syntax-logical-stem
  [source-path source-unit]
  (-> (or (:project-relative-path source-unit)
          (.getName (java.io.File. source-path)))
      reader-normalize-relative-path
      (str/replace #"\.(gravity|qst)$" "")))

(defn sh04-syntax-semantic-source-id
  [source-path source-unit]
  (reader-canonical-hash
   {:domain :gravity/sh04-co-canonical-source-v1
    :logical-source-stem (sh04-syntax-logical-stem source-path source-unit)
    :encoding (:encoding source-unit)
    :bytes-hash (:bytes-hash source-unit)
    :reader-options (:reader-options source-unit)}))

(defn sh04-syntax-semantic-span
  [span semantic-source-id]
  {:source (:source span)
   :file semantic-source-id
   :byte-start (:byte-start span)
   :byte-end (:byte-end span)
   :scalar-start (or (:scalar-start span) (get-in span [:start :char]))
   :scalar-end (or (:scalar-end span) (get-in span [:end :char]))
   :line-start (or (:line-start span) (get-in span [:start :line]))
   :column-start (or (:column-start span) (get-in span [:start :column]))
   :line-end (or (:line-end span) (get-in span [:end :line]))
   :column-end (or (:column-end span) (get-in span [:end :column]))})

(def sh04-syntax-sh03-product-binding-keys
  [:adapter-contract :adapted-source-unit-id :adapted-token-stream-id
   :adapted-form-tree-id :adapted-extension-invocation-set-id])

(defn sh04-syntax-current-sh03-product-binding
  [c2-artifact]
  {:adapter-contract
   (or (get-in c2-artifact [:gravity-reader-boundary :adapter-contract])
       :gravity/sh03-to-c2-reader-products-v2)
   :adapted-source-unit-id
   (get-in c2-artifact [:source-unit-record :source-id])
   :adapted-token-stream-id
   (reader-canonical-hash (c2-token-hash-input (:token-stream c2-artifact)))
   :adapted-form-tree-id
   (reader-canonical-hash (c2-form-hash-input (:form-tree c2-artifact)))
   :adapted-extension-invocation-set-id
   (reader-canonical-hash
    (c2-extension-hash-input
     (:reader-extension-invocation-records c2-artifact)))})

(defn sh04-syntax-descriptor-sh03-product-binding
  [descriptor]
  (let [summary
        (:value
         (some #(when (= :reader-product-identities (:name %)) %)
               (:semantic-projections descriptor)))]
    (when (map? summary)
      (select-keys summary sh04-syntax-sh03-product-binding-keys))))