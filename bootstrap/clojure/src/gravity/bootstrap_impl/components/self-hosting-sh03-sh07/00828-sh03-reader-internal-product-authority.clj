

(def ^:private sh03-reader-internal-product-authority (Object.))

(defn- sh03-reader-precomputed-products-payload-valid?
  [source-path source-text project-context products]
  (let [result (:sh03-reader-result products)
        raw-tokens (:token-stream result)
        raw-forms (:form-tree result)
        token-stream (:token-stream products)
        form-tree (:form-tree products)
        token-id-map
        (when (= (count raw-tokens) (count token-stream))
          (into {} (map vector (map :token-id raw-tokens)
                               (map :token-id token-stream))))
        form-id-map
        (when (= (count raw-forms) (count form-tree))
          (into {} (map vector (map :form-id raw-forms)
                               (map :form-id form-tree))))
        root-form-ids (mapv form-id-map (:top-level-form-ids result))
        forms-by-id (into {} (map (juxt :form-id identity) form-tree))
        parsed-records
        (mapv (fn [root-index form-id]
                (let [form (forms-by-id form-id)]
                  {:form (:value form)
                   :kind (:kind form)
                   :form-id form-id
                   :span (assoc (:span form) :form-index root-index)
                   :parent-form-id nil}))
              (range) root-form-ids)
        literal-records (c2-literal-records form-tree)
        deferred-records (c2-deferred-semantic-literals form-tree)
        extension-records (c2-reader-extension-invocations form-tree)
        source-map
        {:artifact :gravity/reader-source-map
         :token-spans (mapv #(select-keys % [:token-id :span]) token-stream)
         :form-spans
         (mapv #(select-keys % [:form-id :span :parent-form-id]) form-tree)}
        source-unit
        (c2-source-unit-record source-path source-text standard-reader-options
                               project-context)
        summary
        (when (and token-id-map form-id-map)
          (sh03-reader-adapter-summary
           result source-unit token-stream form-tree extension-records
           token-id-map form-id-map))
        descriptor (:sh02-reader-envelope-descriptor products)
        descriptor-summary
        (:value
         (some #(when (= :reader-product-identities (:name %)) %)
               (:semantic-projections descriptor)))]
    (and token-id-map
         form-id-map
         (= source-unit (:source-unit products))
         (= root-form-ids (:root-form-ids products))
         (= parsed-records (:parsed-records products))
         (= (mapv :form parsed-records) (:parsed-values products))
         (= literal-records (:literal-decoding-records products))
         (= deferred-records (:deferred-literal-records products))
         (= extension-records
            (:reader-extension-invocation-records products))
         (= source-map (:gravity-reader-source-map products))
         (= (:semantic-value-table-id summary)
            (:sh03-semantic-value-table-id products))
         (= summary (:sh03-reader-adapter-descriptor products))
         (= summary descriptor-summary))))

(defn- sh03-reader-precomputed-products-verify!
  [candidate source-path source-text project-context products]
  (when-not (identical? candidate sh03-reader-internal-product-authority)
    (c2-reader-fail!
     "C2-HASH" source-path
     {:stage :read-source
      :source-span (source-span source-path 0)
      :reader-options standard-reader-options}
     {:missing-fields [:internal-sh03-precomputed-product-authority]}))
  (let [expected-binding
        (dissoc (sh03-reader-current-binding! source-path) :plan)
        source-bytes
        (.getBytes source-text java.nio.charset.StandardCharsets/UTF_8)
        input-source-unit
        (sh03-reader-input-source-unit source-path source-bytes project-context)
        input-reader-policy
        (sh03-reader-input-policy standard-reader-options)
        raw-result (:sh03-reader-raw-result products)
        result (:sh03-reader-result products)
        report (:sh03-reader-verification-report products)
        descriptor (:sh02-reader-envelope-descriptor products)
        envelope (:sh02-reader-envelope products)
        _ (sh03-reader-result-preflight!
           source-path input-source-unit input-reader-policy raw-result)
        replayed (sh03-reader-resolve-digest-requests!
                  source-path raw-result source-bytes)
        replayed-result
        (update (:result replayed) :diagnostics
                #(mapv (partial sh03-reader-resolve-diagnostic-id source-path)
                       %))]
    (when-not
     (and (= :gravity/sh03-to-c2-reader-products-v2
             (:sh03-reader-adapter-contract products))
          (= expected-binding (:sh03-reader-plan-binding products))
          (= :gravity/sh03-reader-result (:artifact raw-result))
          (= :accepted (:status raw-result))
          (= :gravity/sh03-reader-result (:artifact result))
          (= :accepted (:status result))
          (= replayed-result result)
          (= :gravity/sh03-reader-verification-report (:artifact report))
          (= :accepted (:status report))
          (true? (:verified? report))
          (= p15-s23-sh02-stage-envelope-keys (set (keys envelope)))
          (= :accepted (:status envelope))
          (= :c2-reader (:stage envelope))
          (= :gravity/sh03-reader-products
             (get-in envelope [:sealed-artifact :artifact-kind]))
          (map? descriptor)
          (try
            (sh03-reader-precomputed-products-payload-valid?
             source-path source-text project-context products)
            (catch InterruptedException interrupted
              (.interrupt (Thread/currentThread))
              (throw interrupted))
            (catch Throwable _ false)))
      (c2-reader-fail!
       "C2-HASH" source-path
       {:stage :read-source
        :source-span (source-span source-path 0)
        :reader-options standard-reader-options}
       {:missing-fields [:authenticated-sh03-precomputed-products]}))
    (sh03-reader-verifier-preflight! source-path raw-result report)
    (p15-s23-stage2-sh02-descriptor-envelope-verify!
     envelope :c2-reader :gravity/sh03-reader-products descriptor source-path)
    products))