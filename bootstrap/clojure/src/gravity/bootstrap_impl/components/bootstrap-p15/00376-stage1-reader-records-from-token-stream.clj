

(defn stage1-reader-records-from-token-stream
  [source-path source-text table token-stream]
  (:records (stage1-reader-products-from-token-stream
             source-path source-text table token-stream)))

(defn stage1-reader-table-from-form-builder
  [source-path classifier realizer automaton form-builder]
  (let [table (stage1-reader-table-from-token-automaton source-path
                                                        classifier
                                                        realizer
                                                        automaton)
        builder-diagnostics (:diagnostics form-builder)
        missing-builder (remove #(contains? builder-diagnostics %)
                                [:unexpected-close :unclosed-form
                                 :unclosed-string :unsupported-dispatch
                                 :odd-map :invalid-form-builder])
        required-collections #{:list :vector :map :set}
        required-operations #{:build-list-form :build-vector-form
                              :build-map-form :build-set-form
                              :build-atom-form :build-string-form
                              :validate-delimiters :validate-map-arity}]
    (when-not (= :gravity-reader-form-builder-v1 (:engine form-builder))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path form-builder
       {:missing-fields [:engine]}))
    (doseq [field [:derived-from :token-automaton-engine
                   :table-derived-from :input :emits
                   :collection-kinds :operations :form-record-fields
                   :preserves :diagnostics :provenance]]
      (when-not (contains? form-builder field)
        (stage1-reader-form-builder-pipeline-fail!
         "STAGE1FORM004" source-path form-builder
         {:missing-fields [field]})))
    (when-not (= :stage1-reader-token-automaton
                 (:derived-from form-builder))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path form-builder
       {:missing-fields [:derived-from]}))
    (when-not (= (:engine automaton)
                 (:token-automaton-engine form-builder))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path form-builder
       {:missing-fields [:token-automaton-engine]}))
    (when-not (= :stage1-reader-table (:table-derived-from form-builder))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path form-builder
       {:missing-fields [:table-derived-from]}))
    (when-not (= :gravity/stage1-reader-token-stream (:input form-builder))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path form-builder
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-form-records (:emits form-builder))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path form-builder
       {:missing-fields [:emits]}))
    (when-not (set/subset? required-collections
                           (set (:collection-kinds form-builder)))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path form-builder
       {:missing-fields [:collection-kinds]}))
    (when-not (set/subset? required-operations
                           (set (:operations form-builder)))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path form-builder
       {:missing-fields [:operations]}))
    (when (seq missing-builder)
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path builder-diagnostics
       {:missing-fields (vec missing-builder)}))
    table))

(defn stage1-reader-records-from-form-builder
  [source-path source-text classifier realizer automaton form-builder
   token-stream]
  (let [expected-source-id (str "sha256:" (sha256-hex source-text))
        table (stage1-reader-table-from-form-builder source-path
                                                     classifier
                                                     realizer
                                                     automaton
                                                     form-builder)
        expected-table-id (str "sha256:" (sha256-hex (pr-str table)))]
    (when-not (= :gravity/stage1-reader-token-stream (:kind token-stream))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path token-stream
       {:missing-fields [:kind]}))
    (when-not (= source-path (:source-path token-stream))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path token-stream
       {:missing-fields [:source-path]}))
    (when-not (= expected-source-id (:source-id token-stream))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path token-stream
       {:missing-fields [:source-id]}))
    (when-not (= expected-table-id (:reader-table-id token-stream))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path token-stream
       {:missing-fields [:reader-table-id]}))
    (when-not (= (:engine automaton)
                 (:token-automaton-engine token-stream))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path token-stream
       {:missing-fields [:token-automaton-engine]}))
    (when-not (and (vector? (:tokens token-stream))
                   (= (count (:tokens token-stream))
                      (:token-count token-stream)))
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM004" source-path token-stream
       {:missing-fields [:tokens :token-count]}))
    (stage1-reader-records-from-token-stream source-path
                                             source-text
                                             table
                                             token-stream)))

(defn stage1-reader-token-stream-from-executor
  [source-path source-text classifier realizer automaton executor
   character-stream]
  (let [diagnostics (:diagnostics executor)
        missing-diagnostics (remove #(contains? diagnostics %)
                                    [:unexpected-close :unclosed-form
                                     :unclosed-string :unsupported-dispatch
                                     :invalid-executor])
        required-operations #{:skip-whitespace :skip-line-comment
                              :read-string-token :read-dispatch-token
                              :read-delimiter-token :read-atom-token}]
    (when-not (= :gravity-reader-token-automaton-executor-v1
                 (:engine executor))
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path executor
       {:missing-fields [:token-automaton-executor-engine]}))
    (doseq [field [:executes :derived-from :input :emits :operations
                   :preserves :diagnostics :provenance]]
      (when-not (contains? executor field)
        (stage1-reader-executor-pipeline-fail!
         "STAGE1EXEC004" source-path executor
         {:missing-fields [field]})))
    (when-not (= (:engine automaton) (:executes executor))
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path executor
       {:missing-fields [:executes]}))
    (when-not (= :stage1-reader-token-automaton (:derived-from executor))
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path executor
       {:missing-fields [:derived-from]}))
    (when-not (= :gravity/stage1-reader-character-stream (:input executor))
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path executor
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-token-stream (:emits executor))
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path executor
       {:missing-fields [:emits]}))
    (when-not (set/subset? required-operations (set (:operations executor)))
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path executor
       {:missing-fields [:operations]}))
    (when (seq missing-diagnostics)
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path diagnostics
       {:missing-fields (vec missing-diagnostics)}))
    (let [executor-id (str "sha256:" (sha256-hex (pr-str executor)))
          token-stream (stage1-reader-token-stream-from-automaton
                        source-path source-text classifier realizer automaton
                        character-stream)]
      (assoc token-stream
             :token-automaton-executor-id executor-id
             :token-automaton-executor-engine (:engine executor)
             :gravity-executor :stage1-reader-token-automaton-executor))))

(defn stage1-reader-records-from-executor
  [source-path source-text classifier realizer automaton form-builder
   executor token-stream]
  (let [diagnostics (:diagnostics executor)
        missing-diagnostics (remove #(contains? diagnostics %)
                                    [:unexpected-close :unclosed-form
                                     :unclosed-string :unsupported-dispatch
                                     :odd-map :invalid-executor])
        required-operations #{:build-list-form :build-vector-form
                              :build-map-form :build-set-form
                              :build-atom-form :build-string-form
                              :validate-delimiters :validate-map-arity}]
    (when-not (= :gravity-reader-form-builder-executor-v1 (:engine executor))
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path executor
       {:missing-fields [:form-builder-executor-engine]}))
    (doseq [field [:executes :derived-from :input :emits :operations
                   :preserves :diagnostics :provenance]]
      (when-not (contains? executor field)
        (stage1-reader-executor-pipeline-fail!
         "STAGE1EXEC004" source-path executor
         {:missing-fields [field]})))
    (when-not (= (:engine form-builder) (:executes executor))
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path executor
       {:missing-fields [:executes]}))
    (when-not (= :stage1-reader-form-builder (:derived-from executor))
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path executor
       {:missing-fields [:derived-from]}))
    (when-not (= :gravity/stage1-reader-token-stream (:input executor))
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path executor
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-form-records (:emits executor))
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path executor
       {:missing-fields [:emits]}))
    (when-not (set/subset? required-operations (set (:operations executor)))
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path executor
       {:missing-fields [:operations]}))
    (when (seq missing-diagnostics)
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path diagnostics
       {:missing-fields (vec missing-diagnostics)}))
    (mapv #(assoc % :form-builder-executor-engine (:engine executor))
          (stage1-reader-records-from-form-builder
           source-path source-text classifier realizer automaton form-builder
           token-stream))))