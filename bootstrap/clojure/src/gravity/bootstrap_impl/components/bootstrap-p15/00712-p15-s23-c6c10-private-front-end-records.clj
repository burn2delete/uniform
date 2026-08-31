

(defn p15-s23-c6c10-private-front-end-records
  [private-source-id raw-records token-stream form-tree syntaxes]
  (let [tokens-by-id (into {} (map (juxt :token-id identity) token-stream))
        forms-by-id (into {} (map (juxt :form-id identity) form-tree))]
    (mapv
     (fn [index raw-record]
       (let [syntax (nth syntaxes index)
             form-record (get forms-by-id (:form-id raw-record))
             token-record (get tokens-by-id (:token-id raw-record))
             base (-> (p15-s23-c6c10-path-neutral-value
                       private-source-id raw-record)
                      (dissoc :c2-form-record :c2-token-record
                              :c3-syntax-object))]
         (-> base
             (assoc :source-id private-source-id
                    :span (p15-s23-c6c10-private-span
                           private-source-id (:span raw-record))
                    :source-origin (:origin syntax)
                    :reader-origin
                    (assoc (:reader-origin base) :c3-origin
                           (:origin syntax))
                    :generated-origin
                    (mapv #(p15-s23-c6c10-private-origin
                            private-source-id %)
                          (or (:generated-origin base) []))
                    :c2-form-record form-record
                    :c2-token-record token-record
                    :c3-syntax-object syntax))))
     (range)
     raw-records)))

(defn p15-s23-c6c10-private-program-branches
  [source-content-hash front-end]
  {:forms (p15-s23-c6c10-path-neutral-value
           source-content-hash (:forms front-end))
   :form-values
   (mapv #(select-keys % [:form-id :value :metadata])
         (p15-s23-c6c10-path-neutral-value
          source-content-hash (:form-tree front-end)))
   :literal-values
   (mapv #(select-keys % [:literal-id :form-id :decoded])
         (p15-s23-c6c10-path-neutral-value
          source-content-hash (:literal-decoding-records front-end)))
   :seed-values
   (mapv #(select-keys % [:syntax-id :form :metadata])
         (p15-s23-c6c10-path-neutral-value
          source-content-hash (:syntax-seed-stream front-end)))
   :c3-values
   (mapv #(select-keys % [:form :metadata])
         (p15-s23-c6c10-path-neutral-value
          source-content-hash (:c3-syntax-object-stream front-end)))
   :record-values
   (mapv #(select-keys % [:form-id :form])
         (p15-s23-c6c10-path-neutral-value
          source-content-hash (:records front-end)))})

(defn p15-s23-c6c10-private-provenance-valid?
  [private-source-id front-end]
  (let [span-valid?
        (fn [span]
          (or (not (map? span))
              (and (not (contains? span :source))
                   (or (not (contains? span :file))
                       (= private-source-id (:file span))))))
        token-valid?
        (fn [token]
          (and (not (contains? token :source-path))
               (= private-source-id (:source-id token))
               (span-valid? (:span token))))
        form-valid?
        (fn [form]
          (and (not (contains? form :source-path))
               (= private-source-id (:source-id form))
               (span-valid? (:span form))
               (span-valid? (:surface-span form))))
        syntax-valid?
        (fn [syntax]
          (and (= private-source-id (get-in syntax [:source :source-id]))
               (span-valid? (get-in syntax [:span :primary]))
               (every? span-valid? (get-in syntax [:span :all]))))]
    (and (every? token-valid? (:token-stream front-end))
         (every? form-valid? (:form-tree front-end))
         (every? #(span-valid? (:span %))
                 (:syntax-seed-stream front-end))
         (every? syntax-valid? (:c3-syntax-object-stream front-end)))))

(defn p15-s23-c6c10-verify-private-front-end-projection!
  [source-path raw-front-end literal-authentication front-end]
  (let [raw-source-unit (:source-unit-record raw-front-end)
        expected-source-unit
        (-> (select-keys raw-source-unit
                         p15-s23-c6c10-source-unit-projection-keys)
            (dissoc :source-id :identity-inputs))
        expected-private-identity
        (p15-s23-c6c10-private-source-identity
         source-path expected-source-unit)
        expected-private-identity-inputs
        (:identity-inputs expected-private-identity)
        expected-private-source-id
        (:private-source-id expected-private-identity)
        source-content-hash (:bytes-hash raw-source-unit)
        private-source-id (:source-unit-id front-end)
        source-unit (:source-unit-record front-end)
        private-identity-inputs
        (get-in front-end
                [:reader-product-integrity :input
                 :source-identity-inputs])
        normalized
        (select-keys front-end
                     [:token-stream :form-tree :top-level-form-ids
                      :syntax-seed-stream :literal-decoding-records
                      :semantic-error-deferment-record
                      :reader-extension-invocation-records
                      :reader-diagnostics])
        derived
        (p15-s23-c6c10-private-c2-derived-products
         source-path literal-authentication expected-source-unit
         expected-private-source-id expected-private-identity-inputs
         normalized)
        syntax-identities-current?
        (every?
         (fn [syntax]
           (let [syntax-authentication
                 (p15-s23-c6c10-c3-syntax-authentication
                  source-path literal-authentication syntax)]
             (= (:syntax/id syntax)
                (p15-s23-c6c10-authenticated-semantic-digest
                 source-path syntax-authentication :c3-syntax-identity
                 (p15-s23-c6c10-private-c3-syntax-identity syntax)))))
         (:c3-syntax-object-stream front-end))
        records-current?
        (= (:records front-end)
           (p15-s23-c6c10-private-front-end-records
            private-source-id (:records raw-front-end)
            (:token-stream front-end) (:form-tree front-end)
            (:c3-syntax-object-stream front-end)))
        c3-artifact-current?
        (let [artifact-input (dissoc front-end :c3-artifact-id)
              artifact-authentication
              (p15-s23-c6c10-authentication-with-occurrences
               literal-authentication :private-c3-artifact
               (p15-s23-c6c10-private-c3-artifact-occurrences
                source-path literal-authentication artifact-input))]
          (= (:c3-artifact-id front-end)
             (p15-s23-c6c10-authenticated-semantic-digest
              source-path artifact-authentication :private-c3-artifact
              {:domain :gravity/c6-c10-private-c3-artifact-v1
               :front-end artifact-input})))
        checks
        {:source-unit-current? (= expected-source-unit source-unit)
         :source-identity-inputs-current?
         (= expected-private-identity-inputs private-identity-inputs)
         :source-id-current?
         (= expected-private-source-id private-source-id)
         :incremental-hashes-current?
         (= (:incremental-reader-hashes front-end)
            (:incremental-reader-hashes derived))
         :reader-integrity-current?
         (= (:reader-product-integrity front-end)
            (:reader-product-integrity derived))
         :syntax-identities-current? syntax-identities-current?
         :record-copies-current? records-current?
         :c3-artifact-current? c3-artifact-current?
         :capability-proof-current?
         (= (:c3-capability-proof front-end)
            (p15-s23-c6c10-private-c3-capability-proof
             (:c3-capability-proof raw-front-end)
             (:top-level-form-ids raw-front-end)
             (:c3-syntax-object-stream front-end)))
         :typed-provenance-path-neutral?
         (p15-s23-c6c10-private-provenance-valid?
          private-source-id front-end)
         :program-branches-preserved?
         (= (p15-s23-c6c10-private-program-branches
             source-content-hash raw-front-end)
            (p15-s23-c6c10-private-program-branches
             source-content-hash front-end))}]
    (when-not (every? true? (vals checks))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :verified-private-front-end-projection
       {:checks checks}))
    {:status :passed :checks checks}))