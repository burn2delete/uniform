

(defn stage1-reader-table-from-token-realizer
  [source-path classifier realizer]
  (let [classifier-diagnostics (:diagnostics classifier)
        realizer-diagnostics (:diagnostics realizer)
        missing-classifier (remove #(contains? classifier-diagnostics %)
                                   [:unexpected-close :unclosed-form
                                    :unclosed-string :unsupported-dispatch
                                    :odd-map :invalid-classifier])
        missing-realizer (remove #(contains? realizer-diagnostics %)
                                 [:unexpected-close :unclosed-form
                                  :unclosed-string :unsupported-dispatch
                                  :odd-map :invalid-realizer])]
    (when-not (= :gravity-reader-token-classifier-v1 (:engine classifier))
      (stage1-reader-token-realizer-pipeline-fail!
       "STAGE1REAL004" source-path classifier
       {:missing-fields [:classifier-engine]}))
    (doseq [field [:derived-from :whitespace :line-comment :string-delimiter
                   :delimiters :dispatch :literal-kinds :diagnostics
                   :provenance]]
      (when-not (contains? classifier field)
        (stage1-reader-token-realizer-pipeline-fail!
         "STAGE1REAL004" source-path classifier
         {:missing-fields [field]})))
    (when-not (= :stage1-reader-table (:derived-from classifier))
      (stage1-reader-token-realizer-pipeline-fail!
       "STAGE1REAL004" source-path classifier
       {:missing-fields [:classifier-derived-from]}))
    (when (seq missing-classifier)
      (stage1-reader-token-realizer-pipeline-fail!
       "STAGE1REAL004" source-path classifier-diagnostics
       {:missing-fields (vec missing-classifier)}))
    (when-not (= :gravity-reader-token-realizer-v1 (:engine realizer))
      (stage1-reader-token-realizer-pipeline-fail!
       "STAGE1REAL004" source-path realizer
       {:missing-fields [:engine]}))
    (doseq [field [:derived-from :classifier-engine :table-derived-from
                   :emits :token-record-fields :preserves :diagnostics
                   :provenance]]
      (when-not (contains? realizer field)
        (stage1-reader-token-realizer-pipeline-fail!
         "STAGE1REAL004" source-path realizer
         {:missing-fields [field]})))
    (when-not (= :stage1-reader-token-classifier (:derived-from realizer))
      (stage1-reader-token-realizer-pipeline-fail!
       "STAGE1REAL004" source-path realizer
       {:missing-fields [:derived-from]}))
    (when-not (= (:engine classifier) (:classifier-engine realizer))
      (stage1-reader-token-realizer-pipeline-fail!
       "STAGE1REAL004" source-path realizer
       {:missing-fields [:classifier-engine]}))
    (when-not (= :stage1-reader-table (:table-derived-from realizer))
      (stage1-reader-token-realizer-pipeline-fail!
       "STAGE1REAL004" source-path realizer
       {:missing-fields [:table-derived-from]}))
    (when-not (= :gravity/stage1-reader-token-stream (:emits realizer))
      (stage1-reader-token-realizer-pipeline-fail!
       "STAGE1REAL004" source-path realizer
       {:missing-fields [:emits]}))
    (when (seq missing-realizer)
      (stage1-reader-token-realizer-pipeline-fail!
       "STAGE1REAL004" source-path realizer-diagnostics
       {:missing-fields (vec missing-realizer)}))
    {:engine :gravity-reader-table-v1
     :line-comment (:line-comment classifier)
     :string-delimiter (:string-delimiter classifier)
     :whitespace (:whitespace classifier)
     :delimiters (:delimiters classifier)
     :dispatch (:dispatch classifier)
     :literal-kinds (:literal-kinds classifier)
     :diagnostics (assoc (select-keys classifier-diagnostics
                                      [:unexpected-close :unclosed-form
                                       :unclosed-string :unsupported-dispatch
                                       :odd-map])
                         :missing-table "STAGE1READER006")
     :accepted-fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
     :rejected-fixtures {"bootstrap/clojure/fixtures/rejected/stage1-reader-unexpected-close.gravity"
                         "STAGE1READER001"
                         "bootstrap/clojure/fixtures/rejected/stage1-reader-unclosed-list.gravity"
                         "STAGE1READER002"
                         "bootstrap/clojure/fixtures/rejected/stage1-reader-unclosed-string.gravity"
                         "STAGE1READER003"
                         "bootstrap/clojure/fixtures/rejected/stage1-reader-unsupported-dispatch.gravity"
                         "STAGE1READER004"
                         "bootstrap/clojure/fixtures/rejected/stage1-reader-odd-map.gravity"
                         "STAGE1READER005"}}))

(defn stage1-reader-token-stream-from-realizer
  [source-path source-text classifier realizer character-stream]
  (let [source-id (str "sha256:" (sha256-hex source-text))
        characters (:characters character-stream)
        table (stage1-reader-table-from-token-realizer source-path
                                                       classifier
                                                       realizer)]
    (when-not (= :gravity/stage1-reader-character-stream
                 (:kind character-stream))
      (stage1-reader-token-realizer-pipeline-fail!
       "STAGE1REAL004" source-path character-stream
       {:missing-fields [:kind]}))
    (when-not (= source-path (:source-path character-stream))
      (stage1-reader-token-realizer-pipeline-fail!
       "STAGE1REAL004" source-path character-stream
       {:missing-fields [:source-path]}))
    (when-not (= source-id (:source-id character-stream))
      (stage1-reader-token-realizer-pipeline-fail!
       "STAGE1REAL004" source-path character-stream
       {:missing-fields [:source-id]}))
    (when-not (and (vector? characters)
                   (= (count characters)
                      (:character-count character-stream)))
      (stage1-reader-token-realizer-pipeline-fail!
       "STAGE1REAL004" source-path character-stream
       {:missing-fields [:characters :character-count]}))
    (let [source-from-characters (apply str (map :char characters))]
      (when-not (= source-text source-from-characters)
        (stage1-reader-token-realizer-pipeline-fail!
         "STAGE1REAL004" source-path character-stream
         {:missing-fields [:source-character-parity]}))
      (stage1-reader-token-stream source-path source-from-characters table))))