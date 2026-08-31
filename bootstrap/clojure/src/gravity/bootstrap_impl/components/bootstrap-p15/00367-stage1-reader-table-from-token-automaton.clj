

(defn stage1-reader-table-from-token-automaton
  [source-path classifier realizer automaton]
  (let [classifier-diagnostics (:diagnostics classifier)
        realizer-diagnostics (:diagnostics realizer)
        automaton-diagnostics (:diagnostics automaton)
        missing-classifier (remove #(contains? classifier-diagnostics %)
                                   [:unexpected-close :unclosed-form
                                    :unclosed-string :unsupported-dispatch
                                    :odd-map :invalid-classifier])
        missing-realizer (remove #(contains? realizer-diagnostics %)
                                 [:unexpected-close :unclosed-form
                                  :unclosed-string :unsupported-dispatch
                                  :odd-map :invalid-realizer])
        missing-automaton (remove #(contains? automaton-diagnostics %)
                                  [:unexpected-close :unclosed-form
                                   :unclosed-string :unsupported-dispatch
                                   :odd-map :invalid-automaton])
        required-states #{:scan :line-comment :string :atom :dispatch :done}
        required-operations #{:skip-whitespace :skip-line-comment
                              :read-string-token :read-dispatch-token
                              :read-delimiter-token :read-atom-token}]
    (when-not (= :gravity-reader-token-classifier-v1 (:engine classifier))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path classifier
       {:missing-fields [:classifier-engine]}))
    (doseq [field [:derived-from :whitespace :line-comment :string-delimiter
                   :delimiters :dispatch :literal-kinds :diagnostics
                   :provenance]]
      (when-not (contains? classifier field)
        (stage1-reader-token-automaton-pipeline-fail!
         "STAGE1AUTO004" source-path classifier
         {:missing-fields [field]})))
    (when-not (= :stage1-reader-table (:derived-from classifier))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path classifier
       {:missing-fields [:classifier-derived-from]}))
    (when (seq missing-classifier)
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path classifier-diagnostics
       {:missing-fields (vec missing-classifier)}))
    (when-not (= :gravity-reader-token-realizer-v1 (:engine realizer))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path realizer
       {:missing-fields [:realizer-engine]}))
    (doseq [field [:derived-from :classifier-engine :table-derived-from
                   :emits :token-record-fields :preserves :diagnostics
                   :provenance]]
      (when-not (contains? realizer field)
        (stage1-reader-token-automaton-pipeline-fail!
         "STAGE1AUTO004" source-path realizer
         {:missing-fields [field]})))
    (when-not (= :stage1-reader-token-classifier (:derived-from realizer))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path realizer
       {:missing-fields [:realizer-derived-from]}))
    (when-not (= (:engine classifier) (:classifier-engine realizer))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path realizer
       {:missing-fields [:realizer-classifier-engine]}))
    (when-not (= :stage1-reader-table (:table-derived-from realizer))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path realizer
       {:missing-fields [:table-derived-from]}))
    (when-not (= :gravity/stage1-reader-token-stream (:emits realizer))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path realizer
       {:missing-fields [:realizer-emits]}))
    (when (seq missing-realizer)
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path realizer-diagnostics
       {:missing-fields (vec missing-realizer)}))
    (when-not (= :gravity-reader-token-automaton-v1 (:engine automaton))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path automaton
       {:missing-fields [:engine]}))
    (doseq [field [:derived-from :classifier-engine :realizer-engine
                   :input :emits :states :operations :token-record-fields
                   :preserves :diagnostics :provenance]]
      (when-not (contains? automaton field)
        (stage1-reader-token-automaton-pipeline-fail!
         "STAGE1AUTO004" source-path automaton
         {:missing-fields [field]})))
    (when-not (= :stage1-reader-token-realizer (:derived-from automaton))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path automaton
       {:missing-fields [:derived-from]}))
    (when-not (= (:engine classifier) (:classifier-engine automaton))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path automaton
       {:missing-fields [:classifier-engine]}))
    (when-not (= (:engine realizer) (:realizer-engine automaton))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path automaton
       {:missing-fields [:realizer-engine]}))
    (when-not (= :gravity/stage1-reader-character-stream (:input automaton))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path automaton
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-token-stream (:emits automaton))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path automaton
       {:missing-fields [:emits]}))
    (when-not (set/subset? required-states (set (:states automaton)))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path automaton
       {:missing-fields [:states]}))
    (when-not (set/subset? required-operations (set (:operations automaton)))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path automaton
       {:missing-fields [:operations]}))
    (when (seq missing-automaton)
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path automaton-diagnostics
       {:missing-fields (vec missing-automaton)}))
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

(defn stage1-reader-token-stream-from-automaton
  [source-path source-text classifier realizer automaton character-stream]
  (let [source-id (str "sha256:" (sha256-hex source-text))
        characters (:characters character-stream)
        table (stage1-reader-table-from-token-automaton source-path classifier
                                                        realizer automaton)]
    (when-not (= :gravity/stage1-reader-character-stream
                 (:kind character-stream))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path character-stream
       {:missing-fields [:kind]}))
    (when-not (= source-path (:source-path character-stream))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path character-stream
       {:missing-fields [:source-path]}))
    (when-not (= source-id (:source-id character-stream))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path character-stream
       {:missing-fields [:source-id]}))
    (when-not (and (vector? characters)
                   (= (count characters)
                      (:character-count character-stream)))
      (stage1-reader-token-automaton-pipeline-fail!
       "STAGE1AUTO004" source-path character-stream
       {:missing-fields [:characters :character-count]}))
    (let [source-from-characters (apply str (map :char characters))]
      (when-not (= source-text source-from-characters)
        (stage1-reader-token-automaton-pipeline-fail!
         "STAGE1AUTO004" source-path character-stream
         {:missing-fields [:source-character-parity]}))
      (let [automaton-id (str "sha256:" (sha256-hex (pr-str automaton)))
            base-stream (stage1-reader-token-stream source-path
                                                    source-from-characters table)
            tokens
            (mapv (fn [token]
                    (let [[operation state]
                          (case (:kind token)
                            :string [:read-string-token :string]
                            :set-open [:read-dispatch-token :dispatch]
                            :tag [:read-dispatch-token :dispatch]
                            :list-open [:read-delimiter-token :scan]
                            :vector-open [:read-delimiter-token :scan]
                            :map-open [:read-delimiter-token :scan]
                            :close [:read-delimiter-token :scan]
                            [:read-atom-token :atom])]
                      (assoc token
                             :operation operation
                             :automaton-state state)))
                  (:tokens base-stream))]
        (assoc base-stream
               :token-automaton-id automaton-id
               :token-automaton-engine (:engine automaton)
               :executed-operations (vec (distinct (map :operation tokens)))
               :tokens tokens
               :token-count (count tokens))))))