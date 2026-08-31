

(defn p15-s23-c6c10-private-c2-derived-products
  [source-path literal-authentication source-unit-record private-source-id
   private-identity-inputs normalized]
  (let [token-hash
        (p15-s23-c6c10-authenticated-semantic-digest
         source-path literal-authentication :c2-token-stream
         {:domain :gravity/c6-c10-private-token-stream-v1
          :records (c2-token-hash-input (:token-stream normalized))})
        form-hash
        (p15-s23-c6c10-authenticated-semantic-digest
         source-path literal-authentication :c2-form-tree
         {:domain :gravity/c6-c10-private-form-tree-v1
          :records (c2-form-hash-input (:form-tree normalized))})
        syntax-seed-hash
        (p15-s23-c6c10-authenticated-semantic-digest
         source-path literal-authentication :c2-syntax-seed-stream
         {:domain :gravity/c6-c10-private-syntax-seed-stream-v1
          :records (c2-syntax-seed-hash-input
                    (:syntax-seed-stream normalized))})
        extension-hash
        (p15-s23-c6c10-canonical-digest
         source-path
         {:domain :gravity/c6-c10-private-reader-extensions-v1
          :records (c2-extension-hash-input
                    (:reader-extension-invocation-records normalized))})
        diagnostic-hash
        (p15-s23-c6c10-canonical-digest
         source-path
         {:domain :gravity/c6-c10-private-reader-diagnostics-v1
          :records (c2-diagnostic-hash-input
                    (:reader-diagnostics normalized))})
        incremental-hashes
        {:artifact :gravity/reader-incremental-hashes
         :source-unit private-source-id
         :token-stream token-hash
         :form-tree form-hash
         :syntax-seed-stream syntax-seed-hash
         :extension-invocation-set extension-hash
         :reader-diagnostics diagnostic-hash
         :retained-trivia-affects-form-tree?
         (true? (get-in source-unit-record
                        [:reader-options :retain-comments]))
         :status :stable}
        literal-records (:literal-decoding-records normalized)
        deferred-records
        (get-in normalized
                [:semantic-error-deferment-record
                 :deferred-literal-records])
        literal-records-hash
        (p15-s23-c6c10-authenticated-semantic-digest
         source-path literal-authentication :c2-literal-decoding-records
         {:domain :gravity/c6-c10-private-literal-records-v1
          :records literal-records})
        deferred-literal-records-hash
        (p15-s23-c6c10-authenticated-semantic-digest
         source-path literal-authentication :c2-deferred-literal-records
         {:domain :gravity/c6-c10-private-deferred-literals-v1
          :records deferred-records})
        integrity-input
        {:source-id private-source-id
         :source-identity-inputs private-identity-inputs
         :source-bytes-hash (:bytes-hash source-unit-record)
         :reader-options (:reader-options source-unit-record)
         :top-level-form-ids (vec (:top-level-form-ids normalized))
         :incremental-reader-hashes incremental-hashes
         :literal-records-hash literal-records-hash
         :deferred-literal-records-hash deferred-literal-records-hash}
        integrity-hash
        (p15-s23-c6c10-canonical-digest
         source-path
         {:domain :gravity/c6-c10-private-reader-integrity-v1
          :input integrity-input})
        integrity
        {:artifact :gravity/c2-reader-product-integrity
         :algorithm :sha256
         :input integrity-input
         :integrity-hash integrity-hash
         :status :verified}]
    {:incremental-reader-hashes incremental-hashes
     :reader-product-integrity integrity}))

(defn p15-s23-c6c10-private-c3-syntax-identity
  [syntax]
  {:domain :gravity/c6-c10-private-c3-syntax-v1
   :form-kind (get-in syntax [:form :kind])
   :form (get-in syntax [:form :value])
   :raw (get-in syntax [:form :raw])
   :span (:span syntax)
   :origin (:origin syntax)
   :namespace (:namespace syntax)
   :phase (:phase syntax)
   :profile (:profile syntax)
   :metadata (:metadata syntax)
   :hygiene (:hygiene syntax)
   :facts (:facts syntax)
   :prior-syntax-ids (:prior-syntax-ids syntax)
   :version (:version syntax)})

(def p15-s23-c6c10-private-c3-semantic-fields
  [:form-kind :form :raw :span :origin :namespace :phase :profile
   :metadata :hygiene :facts :prior-syntax-ids :version])

(defn p15-s23-c6c10-rekey-c3-id-vector
  [id-replacements ids]
  (mapv #(get id-replacements % %) (or ids [])))

(defn p15-s23-c6c10-rekey-private-c3-origin
  [id-replacements origin]
  (cond-> origin
    (contains? origin :inputs)
    (update :inputs #(p15-s23-c6c10-rekey-c3-id-vector
                      id-replacements %))
    (contains? origin :input-syntax-ids)
    (update :input-syntax-ids
            #(p15-s23-c6c10-rekey-c3-id-vector id-replacements %))))

(defn p15-s23-c6c10-normalize-private-c3-syntax
  [private-source-id integrity-hash syntax]
  (let [syntax (p15-s23-c6c10-path-neutral-value
                private-source-id syntax)]
    (-> syntax
        (update :span
                (fn [span]
                  (-> span
                      (update :primary
                              #(p15-s23-c6c10-private-span
                                private-source-id %))
                      (update :all
                              #(mapv (fn [item]
                                       (p15-s23-c6c10-private-span
                                        private-source-id item))
                                     (or % []))))))
        (update :source
                (fn [source]
                  (-> source
                      (dissoc :source-path :path)
                      (assoc :source-id private-source-id))))
        (update :origin
                #(mapv (fn [origin]
                         (p15-s23-c6c10-private-origin
                          private-source-id origin))
                       (or % [])))
        (update :facts
                (fn [facts]
                  (cond-> facts
                    (contains? facts :reader-source-id)
                    (assoc :reader-source-id private-source-id)
                    (contains? facts :reader-product-integrity-hash)
                    (assoc :reader-product-integrity-hash
                           integrity-hash)))))))

(defn p15-s23-c6c10-rekey-private-c3-syntaxes
  [source-path literal-authentication syntaxes]
  (loop [remaining syntaxes
         replacements {}
         projected []]
    (if-let [syntax (first remaining)]
      (let [old-syntax-id (:syntax/id syntax)
            prior-rekeyed
            (-> syntax
                (update :prior-syntax-ids
                        #(p15-s23-c6c10-rekey-c3-id-vector
                          replacements %))
                (update :origin
                        #(mapv (fn [origin]
                                 (p15-s23-c6c10-rekey-private-c3-origin
                                  replacements origin))
                               (or % []))))
            identity-input
            (p15-s23-c6c10-private-c3-syntax-identity prior-rekeyed)
            syntax-authentication
            (p15-s23-c6c10-c3-syntax-authentication
             source-path literal-authentication prior-rekeyed)
            syntax-id
            (p15-s23-c6c10-authenticated-semantic-digest
             source-path syntax-authentication :c3-syntax-identity
             identity-input)
            rekeyed
            (-> prior-rekeyed
                (assoc :syntax/id syntax-id)
                (assoc-in [:identity :algorithm] :sha256)
                (assoc-in [:identity :semantic-fields]
                          p15-s23-c6c10-private-c3-semantic-fields)
                (assoc-in [:identity :input-hash] syntax-id))
            replacements
            (if (string? old-syntax-id)
              (assoc replacements old-syntax-id syntax-id)
              replacements)]
        (recur (rest remaining) replacements (conj projected rekeyed)))
      {:syntax-object-stream projected
       :replacements replacements})))

(defn p15-s23-c6c10-private-c3-capability-proof
  [raw-proof top-level-form-ids syntaxes]
  (let [checks
        {:upstream-capability-proof-passed? (= :complete (:status raw-proof))
         :construction-from-reader-seeds?
         (= (count top-level-form-ids) (count syntaxes))
         :stable-syntax-ids?
         (and (every? #(re-matches #"sha256:[0-9a-f]{64}"
                                   (:syntax/id %))
                      syntaxes)
              (= (count syntaxes)
                 (count (set (map :syntax/id syntaxes)))))
         :identity-inputs-current?
         (every? #(= (:syntax/id %)
                     (get-in % [:identity :input-hash]))
                 syntaxes)
         :source-and-generated-origins?
         (every? #(seq (:origin %)) syntaxes)
         :hygiene-propagated?
         (every? map? (map :hygiene syntaxes))
         :metadata-preserved?
         (every? map? (map :metadata syntaxes))}
        passed? (every? true? (vals checks))]
    (merge {:artifact :gravity/c6-c10-private-c3-capability-proof}
           checks
           {:status (if passed? :complete :failed)})))