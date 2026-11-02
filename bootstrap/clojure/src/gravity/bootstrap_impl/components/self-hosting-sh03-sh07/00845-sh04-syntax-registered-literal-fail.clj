

(defn sh04-syntax-registered-literal-fail!
  [source-path form-record missing-fields facts]
  (c3-syntax-fail!
   "C3-FACT-STALE" source-path
   {:source-span (or (:span form-record) (source-span source-path 0))
    :producer :gravity.bootstrap.reader
    :form-kind :tagged-literal}
   {:missing-fields missing-fields
    :facts facts}))

(defn sh04-syntax-registered-literal-registry
  [source-path c2-artifact]
  (let [forms (:form-tree c2-artifact)
        forms-by-id (into {} (map (juxt :form-id identity)) forms)
        tagged-forms
        (filterv #(and (= :tagged-literal (:kind %))
                       (contains? #{'inst 'uuid} (:tag %)))
                 forms)
        literals-by-form
        (group-by :form-id (:literal-decoding-records c2-artifact))
        invocations-by-form
        (group-by
         :form-id
         (for [extension (:reader-extension-invocation-records c2-artifact)
               invocation (:invocations extension)]
           (assoc invocation :tag (:tag extension))))
        entries
        (mapv
         (fn [form-record]
           (let [tag (:tag form-record)
                 payload-record
                 (get forms-by-id (first (:children form-record)))
                 literal-records
                 (get literals-by-form (:form-id form-record) [])
                 invocation-records
                 (get invocations-by-form (:form-id form-record) [])
                 literal-record (first literal-records)
                 invocation-record (first invocation-records)
                 decoded (:value form-record)
                 payload (:value payload-record)
                 canonical-text
                 (case tag
                   inst
                   (when (= java.util.Date (class decoded))
                     (let [expected
                           (try
                             (.toEpochMilli
                              (.toInstant
                               (java.time.OffsetDateTime/parse payload)))
                             (catch Exception _ nil))
                           observed (.getTime ^java.util.Date decoded)]
                       (when (= expected observed)
                         (Long/toString observed))))
                   uuid
                   (when (= java.util.UUID (class decoded))
                     (let [expected
                           (try
                             (java.util.UUID/fromString payload)
                             (catch Exception _ nil))
                           observed (.toString ^java.util.UUID decoded)]
                       (when (= expected decoded)
                         observed)))
                   nil)
                 descriptor
                 (case tag
                   inst
                   {:artifact :gravity/registered-literal-value
                    :kind :tagged-literal
                    :tag 'inst
                    :canonical-value
                    {:kind :instant
                     :epoch-milliseconds canonical-text}
                    :semantic-validation :accepted}
                   uuid
                   {:artifact :gravity/registered-literal-value
                    :kind :tagged-literal
                    :tag 'uuid
                    :canonical-value
                    {:kind :uuid :canonical-text canonical-text}
                    :semantic-validation :accepted})]
             (when-not
              (and (= 1 (count (:children form-record)))
                   (= :string (:kind payload-record))
                   (string? payload)
                   (= 1 (count literal-records))
                   (= 1 (count invocation-records))
                   (= tag (get-in literal-record [:facts :tag])
                      (:tag invocation-record))
                   (= decoded (:decoded literal-record))
                   (= (:raw form-record) (:raw literal-record)
                      (:raw invocation-record))
                   (= (:span form-record) (:span literal-record)
                      (:span invocation-record))
                   (string? canonical-text))
               (sh04-syntax-registered-literal-fail!
                source-path form-record
                [:authenticated-registered-literal-occurrence]
                {:tag tag :form-id (:form-id form-record)}))
             {:lookup-key [tag canonical-text]
              :descriptor descriptor
              :binding
              {:form-id (:form-id form-record)
               :literal-id (:literal-id literal-record)
               :tag tag
               :raw (:raw form-record)
               :payload payload
               :descriptor descriptor}}))
         tagged-forms)
        expected-form-ids (set (map :form-id tagged-forms))
        invoked-form-ids
        (set
         (for [[form-id records] invocations-by-form
               :when (some #(contains? #{'inst 'uuid} (:tag %)) records)]
           form-id))]
    (when-not (= expected-form-ids invoked-form-ids)
      (sh04-syntax-registered-literal-fail!
       source-path nil [:registered-literal-invocation-bijection]
       {:expected-form-ids expected-form-ids
        :invoked-form-ids invoked-form-ids}))
    {:lookup
     (reduce
      (fn [lookup {:keys [lookup-key descriptor]}]
        (if-let [current (get lookup lookup-key)]
          (if (= current descriptor)
            lookup
            (sh04-syntax-registered-literal-fail!
             source-path nil [:unambiguous-registered-literal-descriptor]
             {:lookup-key lookup-key}))
          (assoc lookup lookup-key descriptor)))
      {} entries)
     :bindings (mapv :binding entries)}))

(defn sh04-syntax-project-registered-literal-values
  [source-path registry value]
  (letfn [(project [item]
            (cond
              (= java.util.Date (class item))
              (or (get-in registry
                          [:lookup
                           ['inst
                            (Long/toString
                             (.getTime ^java.util.Date item))]])
                  (sh04-syntax-registered-literal-fail!
                   source-path nil
                   [:registered-instant-bound-to-reader-occurrence] {}))

              (= java.util.UUID (class item))
              (or (get-in registry
                          [:lookup ['uuid (.toString ^java.util.UUID item)]])
                  (sh04-syntax-registered-literal-fail!
                   source-path nil
                   [:registered-uuid-bound-to-reader-occurrence] {}))

              (map? item)
              (into (empty item)
                    (map (fn [[key child]]
                           [(project key) (project child)]))
                    item)

              (vector? item) (mapv project item)
              (set? item) (into #{} (map project) item)
              (seq? item) (apply list (map project item))
              :else item))]
    (project value)))