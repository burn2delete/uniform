

(defn sh03-reader-adapt-evidence!
  [source-path source-id source-bytes source-content-id scalar-boundaries result
   form-id-map token-id-map token-stream form-tree]
  (let [forms-by-id (into {} (map (juxt :form-id identity) form-tree))
        literal-records (c2-literal-records form-tree)
        literal-by-form (into {} (map (juxt :form-id identity)
                                     literal-records))
        gravity-literal-projection
        (mapv
         (fn [record]
           (let [form-id (form-id-map (:form-id record))
                 _ (sh03-reader-form-value-reference!
                    source-path (:decoded record) (:form-id record))
                 literal (literal-by-form form-id)]
             (when-not
              (and literal
                   (= (:kind record) (:kind literal))
                   (= (sh03-reader-accepted-raw-text!
                       source-path source-bytes source-content-id
                       scalar-boundaries (:raw record) (:span record))
                      (:raw literal))
                   (= (dissoc (:span record) :source :file)
                      (dissoc (:span literal) :source :file)))
               (sh03-reader-boundary-fail!
                source-path :sh03-reader-literal-projection record
                {:adapted-form-id form-id}))
             literal))
         (:literal-decoding-records result))
        _ (when-not (= literal-records gravity-literal-projection)
            (sh03-reader-boundary-fail!
             source-path :complete-sh03-reader-literal-projection
             gravity-literal-projection
             {:expected-literal-count (count literal-records)}))
        deferred-records (c2-deferred-semantic-literals form-tree)
        deferred-by-form (into {} (map (juxt :form-id identity)
                                      deferred-records))
        gravity-deferred
        (get-in result [:semantic-error-deferment-record
                        :deferred-literal-records])
        gravity-deferred-projection
        (mapv
         (fn [record]
           (let [form-id (form-id-map (:form-id record))
                 _ (sh03-reader-form-value-reference!
                    source-path (:descriptor record) (:form-id record))
                 deferred (deferred-by-form form-id)]
             (when-not
              (and deferred
                   (= (:kind record) (:kind deferred))
                   (= (sh03-reader-accepted-raw-text!
                       source-path source-bytes source-content-id
                       scalar-boundaries (:raw record) (:span deferred))
                      (:raw deferred))
                   (= :deferred (:semantic-validation record)))
               (sh03-reader-boundary-fail!
                source-path :sh03-reader-deferred-literal-projection record
                {:adapted-form-id form-id}))
             deferred))
         gravity-deferred)
        _ (when-not (= deferred-records gravity-deferred-projection)
            (sh03-reader-boundary-fail!
             source-path :complete-sh03-reader-deferment-projection
             gravity-deferred-projection
             {:expected-deferred-count (count deferred-records)}))
        extension-records (c2-reader-extension-invocations form-tree)
        gravity-extension-projection
        (mapv
         (fn [record]
           (let [tag (symbol (sh03-reader-codepoints-text!
                              source-path (:tag-codepoints record)))
                 handler
                 (symbol (sh03-reader-codepoints-text!
                          source-path
                          (get-in record [:handler :name-codepoints])))]
             {:artifact :gravity/reader-extension-invocation
              :tag tag
              :handler handler
              :build-effects (:build-effects record)
              :capabilities (:capabilities record)
              :profiles (:profiles record)
              :invocations
              (mapv
               (fn [invocation]
                 {:form-id (form-id-map (:form-id invocation))
                  :span (sh03-reader-path-span
                         source-path source-id (:span invocation))
                  :raw (sh03-reader-accepted-raw-text!
                        source-path source-bytes source-content-id
                        scalar-boundaries (:raw invocation)
                        (:span invocation))})
               (:invocations record))
              :status (:status record)}))
         (:reader-extension-invocation-records result))
        _ (when-not (= extension-records gravity-extension-projection)
            (sh03-reader-boundary-fail!
             source-path :complete-sh03-reader-extension-projection
             gravity-extension-projection
             {:expected-extension-records extension-records}))
        source-map (:reader-source-map result)
        projected-token-spans
        (mapv (fn [record]
                {:token-id (token-id-map (:token-id record))
                 :span (sh03-reader-path-span source-path source-id
                                               (:span record))})
              (:token-spans source-map))
        expected-token-spans
        (mapv #(select-keys % [:token-id :span]) token-stream)
        projected-form-spans
        (mapv (fn [record]
                {:form-id (form-id-map (:form-id record))
                 :span (sh03-reader-path-span source-path source-id
                                               (:span record))
                 :parent-form-id
                 (form-id-map (:parent-form-id record))})
              (:form-spans source-map))
        expected-form-spans
        (mapv #(select-keys % [:form-id :span :parent-form-id]) form-tree)]
    (when-not (and (= expected-token-spans projected-token-spans)
                   (= expected-form-spans projected-form-spans))
      (sh03-reader-boundary-fail!
       source-path :complete-sh03-reader-source-map-projection source-map
       {:expected-token-span-count (count expected-token-spans)
        :expected-form-span-count (count expected-form-spans)}))
    {:literal-decoding-records literal-records
     :deferred-literal-records deferred-records
     :reader-extension-invocation-records extension-records
     :reader-source-map
     {:artifact :gravity/reader-source-map
      :token-spans projected-token-spans
      :form-spans projected-form-spans}}))

(defn- sh03-reader-adapter-summary
  [result source-unit token-stream form-tree extension-records
   token-id-map form-id-map]
  {:slice :SH-03
   :status :accepted
   :adapter-contract :gravity/sh03-to-c2-reader-products-v2
   :source-unit-id (get-in result [:incremental-reader-hashes :source-unit])
   :token-stream-id (get-in result [:incremental-reader-hashes :token-stream])
   :form-tree-id (get-in result [:incremental-reader-hashes :form-tree])
   :extension-invocation-set-id
   (get-in result [:incremental-reader-hashes :extension-invocation-set])
   :reader-result-id
   (get-in result [:incremental-reader-hashes :reader-result])
   :semantic-value-table-id
   (reader-canonical-hash (:semantic-value-table result))
   :adapted-source-unit-id (:source-id source-unit)
   :adapted-token-stream-id
   (reader-canonical-hash (c2-token-hash-input token-stream))
   :adapted-form-tree-id
   (reader-canonical-hash (c2-form-hash-input form-tree))
   :adapted-extension-invocation-set-id
   (reader-canonical-hash (c2-extension-hash-input extension-records))
   :token-id-projection-id (reader-canonical-hash token-id-map)
   :form-id-projection-id (reader-canonical-hash form-id-map)})