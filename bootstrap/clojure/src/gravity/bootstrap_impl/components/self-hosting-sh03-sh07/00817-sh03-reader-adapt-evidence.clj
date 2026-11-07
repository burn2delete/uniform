

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
        resource? (= (reader-resource-policy) (get-in result [:source-unit :reader-options]))
        sealed? (or resource? (reader-pure-text/raw-policy? (get-in result [:source-unit :reader-options])))
        extension-records
        (if sealed?
          (let [record (if resource? (reader-resource-record! result) (reader-pure-text/record! result))]
            [(assoc record :tag (if resource? 'resource 'text)
                    :invocations
                    (mapv (fn [invocation]
                            ;; Project occurrence references; preserve the actual
                            ;; Gravity input/returned seeds and invocation intact.
                            (assoc invocation
                                   :form-id (form-id-map (:form-id invocation))
                                   :payload-form-id (form-id-map (:payload-form-id invocation))
                                   :span (sh03-reader-path-span source-path source-id (:span invocation))
                                   :raw (sh03-reader-accepted-raw-text!
                                         source-path source-bytes source-content-id scalar-boundaries
                                         (:raw invocation) (:span invocation))))
                          (:invocations record)))])
          (c2-reader-extension-invocations form-tree))
        gravity-extension-projection
        (if sealed?
          extension-records
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
         (:reader-extension-invocation-records result)))
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

(def ^:private sh03-reader-decimal-projection-keys
  #{:artifact :schema-version :projection-id :reader-result-id
    :semantic-value-table-id :adapted-source-unit-id
    :adapted-token-stream-id :adapted-form-tree-id
    :token-id-projection-id :form-id-projection-id
    :occurrence-count :occurrences})

(def ^:private sh03-reader-decimal-occurrence-keys
  #{:ordinal :semantic-value-entry :adapted-form-id :adapted-token-id})

(def ^:private sh03-reader-decimal-entry-keys
  #{:artifact :schema-version :value-id :form-id :token-id :kind
    :descriptor :semantic-key})

(def ^:private sh03-reader-decimal-reference-keys
  #{:artifact :schema-version :value-id :field})

(def ^:private sh03-reader-decimal-descriptor-keys
  #{:artifact :kind :raw :integer-spelling :fraction-spelling
    :exponent-spelling :semantic-key :semantic-validation
    :normalization-reason :reason})

(def ^:private sh03-reader-decimal-key-keys
  #{:kind :sign :coefficient :decimal-power})

(defn- sh03-reader-decimal-power?
  [power]
  (or
   (integer? power)
   (and (map? power)
        (= #{:artifact :sign :digit-codepoints} (set (keys power)))
        (= :gravity/signed-decimal-power (:artifact power))
        (contains? #{:negative :positive} (:sign power))
        (vector? (:digit-codepoints power))
        (seq (:digit-codepoints power))
        (every? #(and (integer? %) (<= 48 % 57))
                (:digit-codepoints power))
        (not= 48 (first (:digit-codepoints power))))))

(defn- sh03-reader-decimal-key?
  [value]
  (and (map? value)
       (= sh03-reader-decimal-key-keys (set (keys value)))
       (= :decimal (:kind value))
       (contains? #{-1 1} (:sign value))
       (vector? (:coefficient value))
       (seq (:coefficient value))
       (every? #(and (integer? %) (<= 48 % 57)) (:coefficient value))
       (sh03-reader-decimal-power? (:decimal-power value))))

(defn- sh03-reader-decimal-reference?
  [reference value-id field]
  (and (map? reference)
       (= sh03-reader-decimal-reference-keys (set (keys reference)))
       (= :gravity/semantic-value-reference (:artifact reference))
       (= 1 (:schema-version reference))
       (= value-id (:value-id reference))
       (= field (:field reference))))

(defn- sh03-reader-decimal-state
  [source-path descriptor entry-key adapted-value raw-text]
  (let [common? (and (map? descriptor)
                     (= sh03-reader-decimal-descriptor-keys
                        (set (keys descriptor)))
                     (= :gravity/decimal-literal (:artifact descriptor))
                     (= :decimal (:kind descriptor)))]
    (cond
      (and common?
           (= :accepted (:semantic-validation descriptor))
           (nil? (:normalization-reason descriptor))
           (nil? (:reason descriptor))
           (sh03-reader-decimal-key? (:semantic-key descriptor))
           (= (:semantic-key descriptor) entry-key))
      :accepted

      (and common?
           (= :deferred (:semantic-validation descriptor))
           (nil? (:normalization-reason descriptor))
           (= :host-independent-decimal-range (:reason descriptor))
           (sh03-reader-decimal-key? (:semantic-key descriptor))
           (= (:semantic-key descriptor) entry-key)
           (= #{:artifact :kind :raw :integer-spelling
                :fraction-spelling :exponent-spelling :semantic-key
                :semantic-validation :reason}
              (set (keys adapted-value)))
           (= {:artifact :gravity/decimal-literal
               :kind :decimal
               :raw raw-text
               :integer-spelling
               (sh03-reader-codepoints-text!
                source-path (:integer-spelling descriptor))
               :fraction-spelling
               (sh03-reader-codepoints-text!
                source-path (:fraction-spelling descriptor))
               :exponent-spelling
               (sh03-reader-codepoints-text!
                source-path (:exponent-spelling descriptor))
               :semantic-key entry-key
               :semantic-validation :deferred
               :reason :host-independent-decimal-range}
              adapted-value))
      :host-range-deferred

      ;; The current unmetered reader deliberately removed the old
      ;; work-budget normalization boundary.  A row carrying that retired
      ;; shape is not a compatible occurrence and must fail closed.
      :else nil)))

(defn sh03-reader-decimal-occurrence-projection!
  "Construct the authenticated SH03-to-C2 decimal occurrence join.

  This is the only constructor for the V3 projection.  It joins raw semantic
  entries to the adapted form/token products by their authenticated ids and
  preserves the current reader's accepted and host-range-deferred states."
  [source-path result source-unit raw-tokens raw-forms token-id-map form-id-map
   token-stream form-tree adapted-evidence]
  (let [entries (:semantic-value-table result)
        decimal-entries (filterv #(= :decimal (:kind %)) entries)
        raw-forms-by-id (group-by :form-id raw-forms)
        raw-tokens-by-id (group-by :token-id raw-tokens)
        forms-by-id (group-by :form-id form-tree)
        tokens-by-id (group-by :token-id token-stream)
        literals-by-form
        (group-by :form-id (:literal-decoding-records adapted-evidence))
        deferred-by-form
        (group-by :form-id (:deferred-literal-records adapted-evidence))
        occurrences
        (mapv
         (fn [ordinal entry]
           (let [raw-form-records (get raw-forms-by-id (:form-id entry))
                 raw-token-records (get raw-tokens-by-id (:token-id entry))
                 raw-form (first raw-form-records)
                 raw-token (first raw-token-records)
                 adapted-form-id (get form-id-map (:form-id entry))
                 adapted-token-id (get token-id-map (:token-id entry))
                 adapted-form-records (get forms-by-id adapted-form-id)
                 adapted-token-records (get tokens-by-id adapted-token-id)
                 adapted-form (first adapted-form-records)
                 literal-records (get literals-by-form adapted-form-id)
                 deferred-records (get deferred-by-form adapted-form-id)
                 literal (first literal-records)
                 descriptor (:descriptor entry)
                 raw-text (:raw adapted-form)
                 adapted-value (:value adapted-form)
                 state (sh03-reader-decimal-state
                        source-path descriptor (:semantic-key entry)
                        adapted-value raw-text)]
             (when-not
              (and (= sh03-reader-decimal-entry-keys (set (keys entry)))
                   (= :gravity/semantic-value (:artifact entry))
                   (= 1 (:schema-version entry))
                   (= :decimal (:kind entry))
                   (= 1 (count raw-form-records))
                   (= 1 (count raw-token-records))
                   (= 1 (count adapted-form-records))
                   (= 1 (count adapted-token-records))
                   (= 1 (count literal-records))
                   (= (if (= :accepted state) 0 1)
                      (count deferred-records))
                   state
                   (sh03-reader-decimal-reference?
                    (:value raw-form) (:value-id entry) :descriptor)
                   (sh03-reader-decimal-reference?
                    (:semantic-key raw-form) (:value-id entry) :semantic-key)
                   (sh03-reader-decimal-reference?
                    (:descriptor raw-token) (:value-id entry) :descriptor)
                   (= (:form-id entry) (:form-id raw-form))
                   (= (:token-id entry) (:open-token raw-form))
                   (= (:token-id entry) (:token-id raw-token))
                   (= :decimal (:kind raw-form) (:kind raw-token)
                      (:kind adapted-form) (:kind literal))
                   (= adapted-token-id (:open-token adapted-form)
                      (:token-id adapted-form)
                      (:token-id (first adapted-token-records)))
                   (= #{:literal-id :form-id :kind :raw :decoded :span :facts}
                      (set (keys literal)))
                   (= raw-text (:raw literal))
                   (= adapted-value (:decoded literal))
                   (= (:span adapted-form) (:span literal))
                   (= {:exponent-spelling
                       (second (re-find #"([eE][+-]?[0-9]+)" raw-text))
                       :exact? false}
                      (:facts literal))
                   (or (= :accepted state)
                       (let [deferred (first deferred-records)]
                         (and (= #{:form-id :kind :raw :value :span}
                                 (set (keys deferred)))
                              (= adapted-form-id (:form-id deferred))
                              (= :decimal (:kind deferred))
                              (= raw-text (:raw deferred))
                              (= adapted-value (:value deferred))
                              (= (:span adapted-form) (:span deferred))))))
               (sh03-reader-boundary-fail!
                source-path :internal-sh03-c2-decimal-occurrence-projection
                entry {:ordinal ordinal
                       :adapted-form-id adapted-form-id
                       :adapted-token-id adapted-token-id
                       :decimal-state state}))
             {:ordinal ordinal
              :semantic-value-entry entry
              :adapted-form-id adapted-form-id
              :adapted-token-id adapted-token-id}))
         (range)
         decimal-entries)
        projection-base
        {:artifact :gravity/sh03-c2-decimal-occurrence-projection
         :schema-version 1
         :reader-result-id
         (get-in result [:incremental-reader-hashes :reader-result])
         :semantic-value-table-id (reader-canonical-hash entries)
         :adapted-source-unit-id (:source-id source-unit)
         :adapted-token-stream-id
         (reader-canonical-hash (c2-token-hash-input token-stream))
         :adapted-form-tree-id
         (reader-canonical-hash (c2-form-hash-input form-tree))
         :token-id-projection-id (reader-canonical-hash token-id-map)
         :form-id-projection-id (reader-canonical-hash form-id-map)
         :occurrence-count (count occurrences)
         :occurrences occurrences}
        projection-id
        (reader-canonical-hash
         {:domain :gravity/sh03-c2-decimal-occurrence-projection-v1
          :reader-result-id (:reader-result-id projection-base)
          :semantic-value-table-id (:semantic-value-table-id projection-base)
          :adapted-source-unit-id (:adapted-source-unit-id projection-base)
          :adapted-token-stream-id (:adapted-token-stream-id projection-base)
          :adapted-form-tree-id (:adapted-form-tree-id projection-base)
          :token-id-projection-id (:token-id-projection-id projection-base)
          :form-id-projection-id (:form-id-projection-id projection-base)
          :occurrence-count (:occurrence-count projection-base)
          :occurrences (:occurrences projection-base)})
        projection (assoc projection-base :projection-id projection-id)]
    (when-not (and (= sh03-reader-decimal-projection-keys (set (keys projection)))
                   (every? p15-s23-sh02-sha256-id?
                           (map projection
                                [:projection-id :reader-result-id
                                 :semantic-value-table-id
                                 :adapted-source-unit-id
                                 :adapted-token-stream-id
                                 :adapted-form-tree-id
                                 :token-id-projection-id
                                 :form-id-projection-id]))
                   (= (count decimal-entries) (count occurrences))
                   (= (vec (range (count occurrences)))
                      (mapv :ordinal occurrences))
                   (every? #(= sh03-reader-decimal-occurrence-keys
                               (set (keys %))) occurrences))
      (sh03-reader-boundary-fail!
       source-path :internal-sh03-c2-decimal-occurrence-projection
       projection {}))
    projection))

(defn sh03-reader-decimal-occurrence-projection-from-products!
  "Recompute a V3 projection from a complete authenticated product map."
  [source-path products]
  (let [result (:sh03-reader-result products)
        raw-tokens (:token-stream result)
        raw-forms (:form-tree result)
        token-stream (:token-stream products)
        form-tree (:form-tree products)
        raw-token-ids (mapv :token-id raw-tokens)
        raw-form-ids (mapv :form-id raw-forms)
        adapted-token-ids (mapv :token-id token-stream)
        adapted-form-ids (mapv :form-id form-tree)]
    (when-not (and (= (count raw-token-ids) (count adapted-token-ids))
                   (= (count raw-form-ids) (count adapted-form-ids))
                   (= (count raw-token-ids) (count (distinct raw-token-ids)))
                   (= (count raw-form-ids) (count (distinct raw-form-ids)))
                   (= (count adapted-token-ids)
                      (count (distinct adapted-token-ids)))
                   (= (count adapted-form-ids)
                      (count (distinct adapted-form-ids))))
      (sh03-reader-boundary-fail!
       source-path :internal-sh03-c2-decimal-occurrence-projection
       {:raw-token-count (count raw-token-ids)
        :adapted-token-count (count adapted-token-ids)
        :raw-form-count (count raw-form-ids)
        :adapted-form-count (count adapted-form-ids)}
       {:reason :non-bijective-reader-id-projection}))
    (sh03-reader-decimal-occurrence-projection!
     source-path result (:source-unit products)
     raw-tokens raw-forms
     (into {} (map vector raw-token-ids adapted-token-ids))
     (into {} (map vector raw-form-ids adapted-form-ids))
     token-stream form-tree
     (select-keys products [:literal-decoding-records
                            :deferred-literal-records
                            :reader-extension-invocation-records
                            :gravity-reader-source-map]))))

(defn sh03-reader-decimal-occurrence-projection-shape-valid?
  "Validate the closed projection shape and its content-addressed id.

  The producer-side join above proves the source/table/form/token relations;
  this smaller check is for later carriers that intentionally retain only the
  sealed projection and its identity."
  [projection]
  (try
    (let [occurrences (:occurrences projection)
          occurrence-pairs
          (mapv (juxt :adapted-form-id :adapted-token-id) occurrences)
          occurrence-valid?
          (fn [occurrence]
            (let [entry (:semantic-value-entry occurrence)
                  descriptor (:descriptor entry)
                  semantic-key (:semantic-key entry)]
              (and (= sh03-reader-decimal-occurrence-keys
                      (set (keys occurrence)))
                   (map? entry)
                   (= sh03-reader-decimal-entry-keys (set (keys entry)))
                   (= :gravity/semantic-value (:artifact entry))
                   (= 1 (:schema-version entry))
                   (= :decimal (:kind entry))
                   (map? descriptor)
                   (= sh03-reader-decimal-descriptor-keys
                      (set (keys descriptor)))
                   (= :gravity/decimal-literal (:artifact descriptor))
                   (= :decimal (:kind descriptor))
                   (sh03-reader-decimal-key? semantic-key)
                   (= semantic-key (:semantic-key descriptor))
                   (contains? #{:accepted :deferred}
                              (:semantic-validation descriptor))
                   (nil? (:normalization-reason descriptor))
                   (or (and (= :accepted (:semantic-validation descriptor))
                            (nil? (:reason descriptor)))
                       (and (= :deferred (:semantic-validation descriptor))
                            (= :host-independent-decimal-range
                               (:reason descriptor))))
                   (some? (:value-id entry))
                   (some? (:form-id entry))
                   (some? (:token-id entry)))))
          projection-id
          (:projection-id projection)]
      (and (map? projection)
           (= sh03-reader-decimal-projection-keys (set (keys projection)))
           (= :gravity/sh03-c2-decimal-occurrence-projection
              (:artifact projection))
           (= 1 (:schema-version projection))
           (p15-s23-sh02-sha256-id? projection-id)
           (every? #(p15-s23-sh02-sha256-id? (get projection %))
                   [:reader-result-id :semantic-value-table-id
                    :adapted-source-unit-id :adapted-token-stream-id
                    :adapted-form-tree-id :token-id-projection-id
                    :form-id-projection-id])
           (integer? (:occurrence-count projection))
           (<= 0 (:occurrence-count projection))
           (vector? occurrences)
           (= (:occurrence-count projection) (count occurrences))
           (= (vec (range (:occurrence-count projection)))
              (mapv :ordinal occurrences))
           (= (count occurrence-pairs)
              (count (distinct occurrence-pairs)))
           (every? occurrence-valid? occurrences)
           (= projection-id
              (reader-canonical-hash
               (dissoc (assoc projection
                              :domain
                              :gravity/sh03-c2-decimal-occurrence-projection-v1)
                       :artifact :schema-version :projection-id)))))
    (catch Throwable _ false)))

(defn- sh03-reader-adapter-summary
  [result source-unit token-stream form-tree extension-records
   token-id-map form-id-map decimal-occurrence-projection]
  (cond-> {:slice :SH-03
   :status :accepted
   :adapter-contract :gravity/sh03-to-c2-reader-products-v3
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
   :form-id-projection-id (reader-canonical-hash form-id-map)
   :decimal-occurrence-projection-id
   (:projection-id decimal-occurrence-projection)}
    (reader-pure-text/raw-policy? (get-in result [:source-unit :reader-options]))
    (assoc :pure-text-record-id (reader-canonical-hash (reader-pure-text/record! result)))
    (= (reader-resource-policy) (get-in result [:source-unit :reader-options]))
    (assoc :resource-record-id (reader-canonical-hash (reader-resource-record! result)))))
