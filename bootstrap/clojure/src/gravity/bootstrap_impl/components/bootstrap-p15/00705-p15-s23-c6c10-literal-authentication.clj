

(defn p15-s23-c6c10-literal-authentication
  [source-path raw-front-end]
  (let [embedded-c2 (:c2-reader-artifact raw-front-end)
        integrity-report
        (when (map? embedded-c2)
          (c3-c2-reader-integrity-report embedded-c2))
        duplicated-fields
        [:source-unit-record :token-stream :form-tree :top-level-form-ids
         :syntax-seed-stream :reader-source-map :literal-decoding-records
         :semantic-error-deferment-record
         :reader-extension-invocation-records :reader-diagnostics
         :incremental-reader-hashes :reader-product-integrity]
        duplicated-fields-current?
        (and embedded-c2
             (every? #(= (get raw-front-end %)
                         (get embedded-c2 %))
                     duplicated-fields)
             (= (:source-unit-id raw-front-end)
                (get-in embedded-c2 [:source-unit-record :source-id])))
        authenticated-c3
        (:authenticated-c3-source-artifact raw-front-end)
        authenticated-c3-stream (:syntax-object-stream authenticated-c3)
        expected-rich-syntax
        (when (and authenticated-c3 embedded-c2)
          (vec (take (count (:top-level-form-ids embedded-c2))
                     authenticated-c3-stream)))
        expected-records
        (when expected-rich-syntax
          (p15-s23-stage2-c2-c3-records embedded-c2
                                        expected-rich-syntax))
        fresh-c3-proof
        (when authenticated-c3
          (c3-syntax-capability-proof authenticated-c3))
        authenticated-c3-current?
        (and authenticated-c3
             (= embedded-c2 (:c2-reader-artifact authenticated-c3))
             (= (:artifact-id authenticated-c3)
                (c3-artifact-id authenticated-c3))
             (c3-syntax-stream-reader-products-authentic?
              authenticated-c3-stream embedded-c2
              (:gravity-syntax-boundary authenticated-c3))
             (= :complete (:status fresh-c3-proof))
             (= fresh-c3-proof
                (:capability-based-proof authenticated-c3))
             (= (:c3-artifact-id raw-front-end)
                (:artifact-id authenticated-c3))
             (= (:c3-capability-proof raw-front-end) fresh-c3-proof)
             (= (:c3-syntax-object-stream raw-front-end)
                expected-rich-syntax)
             (= (:records raw-front-end) expected-records)
             (= (:forms raw-front-end) (mapv :form expected-records)))
        _
        (when-not (and (true? (:authentic? integrity-report))
                       duplicated-fields-current?
                       authenticated-c3-current?)
          (p15-s23-c6c10-host-fail!
           "C6-ORIGIN" source-path
           :fresh-authentic-embedded-c2-c3-products
           {:integrity-report integrity-report
            :duplicated-fields-current? duplicated-fields-current?
            :authenticated-c3-current? authenticated-c3-current?}))
        forms-by-id (into {} (map (juxt :form-id identity)
                                   (:form-tree raw-front-end)))
        tokens-by-id (into {} (map (juxt :token-id identity)
                                    (:token-stream raw-front-end)))
        records
        (mapv
         (fn [record]
           (when-let [descriptor
                      (p15-s23-c6c10-literal-scalar-descriptor
                       (:decoded record))]
             (let [form-record (get forms-by-id (:form-id record))
                   token-record (get tokens-by-id (:open-token form-record))
                   expected-kind
                   (if (= :gravity/exact-ratio-literal
                          (:kind descriptor))
                     :ratio
                     :decimal)]
               (when-not (and (= expected-kind (:kind record)
                                  (:kind form-record)
                                  (:kind token-record))
                              (= (:raw record) (:raw form-record)
                                 (:raw token-record))
                              (= (:decoded record) (:value form-record)
                                 (:decoded token-record))
                              (= (:span record) (:span form-record)
                                 (:span token-record)))
                 (p15-s23-c6c10-host-fail!
                  "C6-ORIGIN" source-path
                  :numeric-host-value-bound-to-exact-c2-literal
                  {:literal-id (:literal-id record)
                   :form-id (:form-id record)
                   :expected-kind expected-kind
                   :observed-kind (:kind record)
                   :token-id (:token-id token-record)}))
               {:descriptor descriptor
                :literal-id (:literal-id record)
                :form-id (:form-id record)
                :token-id (:token-id token-record)
                :kind (:kind record)
                :raw (:raw record)})))
         (:literal-decoding-records raw-front-end))
        records (vec (remove nil? records))
        literal-records-by-form
        (group-by :form-id (:literal-decoding-records raw-front-end))
        deferred-records-by-form
        (group-by :form-id
                  (get-in raw-front-end
                          [:semantic-error-deferment-record
                           :deferred-literal-records]))
        deferred-ratio-by-form-id
        (into
         {}
         (for [form (:form-tree raw-front-end)
               :when (and (= :ratio (:kind form))
                          (p15-s23-c6c10-deferred-ratio-descriptor?
                           (:value form)))]
           (let [token (get tokens-by-id (:open-token form))
                 literal-records
                 (get literal-records-by-form (:form-id form))
                 deferred-records
                 (get deferred-records-by-form (:form-id form))
                 literal (first literal-records)
                 deferred (first deferred-records)
                 descriptor (:value form)]
             (when-not
              (and (= 1 (count literal-records))
                   (= 1 (count deferred-records))
                   (= :ratio (:kind token) (:kind literal)
                      (:kind deferred))
                   (= (:raw form) (:raw token) (:raw literal)
                      (:raw deferred) (:raw descriptor))
                   (= descriptor (:decoded token) (:decoded literal)
                      (:value deferred))
                   (= (:span form) (:span token) (:span literal)
                      (:span deferred)))
               (p15-s23-c6c10-host-fail!
                "C6-ORIGIN" source-path
                :exact-deferred-ratio-c2-evidence
                {:form-id (:form-id form)
                 :literal-count (count literal-records)
                 :deferred-count (count deferred-records)}))
             [(:form-id form)
              {:descriptor descriptor
               :form-id (:form-id form)
               :token-id (:token-id token)
               :literal-id (:literal-id literal)
               :kind :ratio
               :raw (:raw form)
               :span (:span form)
               :semantic-validation :deferred
               :reason :zero-denominator}])))]
    (let [authentication
          {:by-form-id (into {} (map (juxt :form-id identity)) records)
           :forms-by-id forms-by-id
           :deferred-ratio-by-form-id deferred-ratio-by-form-id
           :records records}
          authentication
          (merge authentication
                 (p15-s23-c6c10-form-numeric-occurrence-index
                  source-path authentication raw-front-end))]
      (assoc authentication :occurrences
             (p15-s23-c6c10-base-numeric-occurrences
              source-path authentication raw-front-end)))))

(def p15-s23-c6c10-numeric-projection-fields
  #{:c2-token-stream :c2-form-tree :c2-syntax-seed-stream
    :c2-literal-decoding-records :c2-deferred-literal-records
    :c3-syntax-identity :private-c3-artifact
    :private-stage2-plan})

(defn p15-s23-c6c10-host-order-key
  [value]
  (binding [*print-length* nil
            *print-level* nil
            *print-meta* false
            *print-dup* false
            *print-readably* true
            *print-namespace-maps* false]
    (pr-str [(some-> value class .getName) value])))

(defn p15-s23-c6c10-map-value-path
  [path index key]
  (conj path
        (if (or (keyword? key) (symbol? key) (string? key)
                (integer? key) (char? key) (boolean? key) (nil? key))
          key
          [:map-value index])))

(defn p15-s23-c6c10-collect-numeric-occurrences
  [base-path value]
  (let [occurrences (atom {})]
    (letfn [(collect [path item]
              (if-let [descriptor
                       (p15-s23-c6c10-literal-scalar-descriptor item)]
                (swap! occurrences assoc path descriptor)
                (cond
                  (map? item)
                  (doseq [[index [key child]]
                          (map-indexed
                           vector
                           (sort-by (comp p15-s23-c6c10-host-order-key key)
                                    item))]
                    (collect (conj path [:map-key index]) key)
                    (collect (p15-s23-c6c10-map-value-path
                              path index key)
                             child))
                  (vector? item)
                  (doseq [[index child] (map-indexed vector item)]
                    (collect (conj path index) child))
                  (set? item)
                  (doseq [[index child]
                          (map-indexed
                           vector
                           (sort-by p15-s23-c6c10-host-order-key item))]
                    (collect (conj path [:set-item index]) child))
                  (list? item)
                  (doseq [[index child] (map-indexed vector item)]
                    (collect (conj path index) child)))))]
      (collect base-path value)
      @occurrences)))

(defn p15-s23-c6c10-merge-occurrences
  [& occurrence-maps]
  (apply merge occurrence-maps))

(defn p15-s23-c6c10-exact-numeric-occurrence
  [source-path path value evidence]
  (let [descriptor (p15-s23-c6c10-literal-scalar-descriptor value)]
    (when-not (and descriptor evidence
                   (= descriptor (:descriptor evidence)))
      (p15-s23-c6c10-host-fail!
       "C6-ORIGIN" source-path
       :structurally-corresponding-numeric-occurrence
       {:projected-path path
        :descriptor descriptor
        :evidence (some-> evidence
                          (select-keys [:literal-id :form-id :token-id
                                        :descriptor]))}))
    {path {:descriptor descriptor :evidence evidence}}))