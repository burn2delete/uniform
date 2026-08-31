

(defn p15-s23-c6c10-form-copy-field-numeric-occurrences
  [source-path literal-authentication prefix form-id record]
  (p15-s23-c6c10-merge-occurrences
   (if (contains? record :expanded-form)
     (p15-s23-c6c10-prefix-occurrences
      (conj prefix :expanded-form)
      (p15-s23-c6c10-form-semantic-copy-numeric-occurrences
       source-path literal-authentication form-id
       (:expanded-form record)))
     {})
   (apply
    p15-s23-c6c10-merge-occurrences
    (for [[origin-index origin]
          (map-indexed vector (or (:generated-origin record) []))
          :when (contains? origin :expanded-form)]
      (p15-s23-c6c10-prefix-occurrences
       (into prefix [:generated-origin origin-index :expanded-form])
       (p15-s23-c6c10-form-semantic-copy-numeric-occurrences
        source-path literal-authentication form-id
        (:expanded-form origin)))))))

(defn p15-s23-c6c10-authentication-with-occurrences
  [literal-authentication projection-field occurrences]
  (assoc-in literal-authentication
            [:occurrences projection-field]
            occurrences))

(defn p15-s23-c6c10-base-numeric-occurrences
  [source-path literal-authentication raw-front-end]
  (let [evidence-by-token-id
        (into {} (map (juxt :token-id identity)
                      (:records literal-authentication)))
        evidence-by-literal-id
        (into {} (map (juxt :literal-id identity)
                      (:records literal-authentication)))
        token-occurrences
        (apply
         p15-s23-c6c10-merge-occurrences
         (for [[index record]
               (map-indexed vector (:token-stream raw-front-end))
               :let [descriptor
                     (p15-s23-c6c10-literal-scalar-descriptor
                      (:decoded record))]
               :when descriptor]
           (p15-s23-c6c10-exact-numeric-occurrence
            source-path [:records index :decoded] (:decoded record)
            (get evidence-by-token-id (:token-id record)))))
        form-occurrences
        (apply
         p15-s23-c6c10-merge-occurrences
         (for [[index record]
               (map-indexed vector (:form-tree raw-front-end))]
           (p15-s23-c6c10-merge-occurrences
            (p15-s23-c6c10-prefix-occurrences
             [:records index :value]
             (p15-s23-c6c10-form-value-numeric-occurrences
              source-path literal-authentication (:form-id record)))
            (p15-s23-c6c10-prefix-occurrences
             [:records index :metadata]
             (p15-s23-c6c10-form-metadata-numeric-occurrences
              source-path literal-authentication (:form-id record)))
            (p15-s23-c6c10-form-copy-field-numeric-occurrences
             source-path literal-authentication [:records index]
             (:form-id record) record))))
        seed-occurrences
        (apply
         p15-s23-c6c10-merge-occurrences
         (for [[index record]
               (map-indexed vector (:syntax-seed-stream raw-front-end))]
           (p15-s23-c6c10-merge-occurrences
            (p15-s23-c6c10-prefix-occurrences
             [:records index :form]
             (p15-s23-c6c10-form-value-numeric-occurrences
              source-path literal-authentication (:form-id record)))
            (p15-s23-c6c10-prefix-occurrences
             [:records index :metadata]
             (p15-s23-c6c10-form-metadata-numeric-occurrences
              source-path literal-authentication (:form-id record)))
            (p15-s23-c6c10-form-copy-field-numeric-occurrences
             source-path literal-authentication [:records index]
             (:form-id record) record))))
        literal-occurrences
        (apply
         p15-s23-c6c10-merge-occurrences
         (for [[index record]
               (map-indexed vector (:literal-decoding-records raw-front-end))
               :let [descriptor
                     (p15-s23-c6c10-literal-scalar-descriptor
                      (:decoded record))]
               :when descriptor]
           (p15-s23-c6c10-exact-numeric-occurrence
            source-path [:records index :decoded] (:decoded record)
            (get evidence-by-literal-id (:literal-id record)))))
        deferred-occurrences
        (apply
         p15-s23-c6c10-merge-occurrences
         (for [[index record]
               (map-indexed
                vector
                (get-in raw-front-end
                        [:semantic-error-deferment-record
                         :deferred-literal-records]))
               :let [descriptor
                     (p15-s23-c6c10-literal-scalar-descriptor
                      (:value record))]
               :when descriptor]
           (p15-s23-c6c10-exact-numeric-occurrence
            source-path [:records index :value] (:value record)
            (get (:by-form-id literal-authentication) (:form-id record)))))]
    {:c2-token-stream token-occurrences
     :c2-form-tree form-occurrences
     :c2-syntax-seed-stream seed-occurrences
     :c2-literal-decoding-records literal-occurrences
     :c2-deferred-literal-records deferred-occurrences}))

(defn p15-s23-c6c10-c3-syntax-authentication
  [source-path literal-authentication syntax]
  (let [form-id (get-in syntax [:source :form-id])
        occurrences
        (p15-s23-c6c10-merge-occurrences
         (p15-s23-c6c10-prefix-occurrences
          [:form]
          (p15-s23-c6c10-form-value-numeric-occurrences
           source-path literal-authentication form-id))
         (p15-s23-c6c10-prefix-occurrences
          [:metadata]
          (p15-s23-c6c10-form-metadata-numeric-occurrences
           source-path literal-authentication form-id)))]
    (p15-s23-c6c10-authentication-with-occurrences
     literal-authentication :c3-syntax-identity occurrences)))

(defn p15-s23-c6c10-remap-occurrences
  [occurrences target-prefix]
  (into {}
        (map (fn [[path occurrence]]
               [(into target-prefix (rest path)) occurrence]))
        occurrences))

(defn p15-s23-c6c10-private-c3-artifact-occurrences
  [source-path literal-authentication front-end]
  (let [base (:occurrences literal-authentication)
        remapped
        [(p15-s23-c6c10-remap-occurrences
          (:c2-token-stream base) [:front-end :token-stream])
         (p15-s23-c6c10-remap-occurrences
          (:c2-form-tree base) [:front-end :form-tree])
         (p15-s23-c6c10-remap-occurrences
          (:c2-syntax-seed-stream base)
          [:front-end :syntax-seed-stream])
         (p15-s23-c6c10-remap-occurrences
          (:c2-literal-decoding-records base)
          [:front-end :literal-decoding-records])
         (p15-s23-c6c10-remap-occurrences
          (:c2-deferred-literal-records base)
          [:front-end :semantic-error-deferment-record
           :deferred-literal-records])]
        evidence-by-token
        (into {} (map (juxt :token-id identity)
                      (:records literal-authentication)))
        form-value
        (fn [prefix form-id]
          (p15-s23-c6c10-prefix-occurrences
           prefix
           (p15-s23-c6c10-form-value-numeric-occurrences
            source-path literal-authentication form-id)))
        form-metadata
        (fn [prefix form-id]
          (p15-s23-c6c10-prefix-occurrences
           prefix
           (p15-s23-c6c10-form-metadata-numeric-occurrences
            source-path literal-authentication form-id)))
        token-decoded
        (fn [path token-id value]
          (if (p15-s23-c6c10-literal-scalar-descriptor value)
            (p15-s23-c6c10-exact-numeric-occurrence
             source-path path value (get evidence-by-token token-id))
            {}))
        syntax-occurrences
        (apply
         p15-s23-c6c10-merge-occurrences
         (for [[index syntax]
               (map-indexed vector (:c3-syntax-object-stream front-end))]
           (let [form-id (get-in syntax [:source :form-id])]
             (p15-s23-c6c10-merge-occurrences
              (form-value
               [:front-end :c3-syntax-object-stream index :form :value]
               form-id)
              (form-metadata
               [:front-end :c3-syntax-object-stream index :metadata]
               form-id)))))
        record-occurrences
        (apply
         p15-s23-c6c10-merge-occurrences
         (for [[index record] (map-indexed vector (:records front-end))]
           (let [form-id (:form-id record)
                 token-id (:token-id record)]
             (p15-s23-c6c10-merge-occurrences
              (form-value [:front-end :records index :form] form-id)
              (form-metadata
               [:front-end :records index :metadata] form-id)
              (form-value
               [:front-end :records index :c2-form-record :value]
               form-id)
              (form-metadata
               [:front-end :records index :c2-form-record :metadata]
               form-id)
              (p15-s23-c6c10-form-copy-field-numeric-occurrences
               source-path literal-authentication
               [:front-end :records index :c2-form-record]
               form-id (:c2-form-record record))
              (token-decoded
               [:front-end :records index :c2-token-record :decoded]
               token-id (get-in record [:c2-token-record :decoded]))
              (form-value
               [:front-end :records index
                :c3-syntax-object :form :value]
               form-id)
              (form-metadata
               [:front-end :records index :c3-syntax-object :metadata]
               form-id)))))
        form-occurrences
        (apply
         p15-s23-c6c10-merge-occurrences
         (for [[index form] (map-indexed vector (:forms front-end))
               :let [form-id
                     (nth (:top-level-form-ids front-end) index nil)]]
           (form-value [:front-end :forms index] form-id)))]
    (apply p15-s23-c6c10-merge-occurrences
           (concat remapped
                   [syntax-occurrences record-occurrences
                    form-occurrences]))))