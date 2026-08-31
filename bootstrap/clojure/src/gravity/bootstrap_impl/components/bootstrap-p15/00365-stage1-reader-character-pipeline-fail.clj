

(declare stage1-reader-character-pipeline-fail!
         stage1-reader-token-classifier-pipeline-fail!
         stage1-reader-token-realizer-pipeline-fail!
         stage1-reader-token-automaton-pipeline-fail!
         stage1-reader-form-builder-pipeline-fail!
         stage1-reader-executor-pipeline-fail!
         stage1-reader-runtime-pipeline-fail!
         stage1-reader-compiled-pipeline-fail!
         stage1-reader-binary-pipeline-fail!
         stage1-reader-self-hosted-runtime-fail!
         stage1-reader-core-bootstrap-fail!
         stage1-reader-compiler-driver-fail!
         stage1-reader-runtime-entrypoint-fail!
         stage1-reader-runtime-image-fail!
         stage1-reader-verified-boot-chain-fail!
         stage1-reader-diverse-bootstrap-verification-fail!
         stage1-reader-release-attestation-seed-retirement-fail!
         stage1-reader-pipeline-fail!
         reader-canonical-hash
         ^:dynamic *stage1-reader-pipeline-trace*)

(defn stage1-reader-character-stream
  [source-path ^String source-text]
  (let [line-starts (line-start-indices source-text)
        source-id (str "sha256:" (sha256-hex source-text))
        source-length (count source-text)
        characters
        (loop [char-offset 0
               scalar-index 0
               records []]
          (if (>= char-offset source-length)
            records
            (let [codepoint (.codePointAt source-text char-offset)
                  char-width (Character/charCount codepoint)
                  end-offset (+ char-offset char-width)
                  raw (subs source-text char-offset end-offset)]
              (recur end-offset
                     (inc scalar-index)
                     (conj records
                           {:index scalar-index
                            :char raw
                            :raw raw
                            :codepoint codepoint
                            :span (stage1-reader-span
                                   source-path source-text line-starts
                                   char-offset end-offset)})))))]
    {:kind :gravity/stage1-reader-character-stream
     :source-path source-path
     :source-id source-id
     :character-count (count characters)
     :utf16-unit-count source-length
     :characters characters
     :status :complete}))

(defn stage1-reader-token-stream-from-characters
  [source-path source-text table character-stream]
  (let [source-id (str "sha256:" (sha256-hex source-text))
        characters (:characters character-stream)]
    (when-not (= :gravity/stage1-reader-character-stream
                 (:kind character-stream))
      (stage1-reader-character-pipeline-fail! "STAGE1CHAR004" source-path
                                              character-stream
                                              {:missing-fields [:kind]}))
    (when-not (= source-path (:source-path character-stream))
      (stage1-reader-character-pipeline-fail! "STAGE1CHAR004" source-path
                                              character-stream
                                              {:missing-fields [:source-path]}))
    (when-not (= source-id (:source-id character-stream))
      (stage1-reader-character-pipeline-fail! "STAGE1CHAR004" source-path
                                              character-stream
                                              {:missing-fields [:source-id]}))
    (when-not (and (vector? characters)
                   (= (count characters)
                      (:character-count character-stream)))
      (stage1-reader-character-pipeline-fail! "STAGE1CHAR004" source-path
                                              character-stream
                                              {:missing-fields
                                               [:characters
                                                :character-count]}))
    (let [source-from-characters (apply str (map :char characters))]
      (when-not (= source-text source-from-characters)
        (stage1-reader-character-pipeline-fail! "STAGE1CHAR004" source-path
                                                character-stream
                                                {:missing-fields
                                                 [:source-character-parity]}))
      (stage1-reader-token-stream source-path source-from-characters table))))

(defn stage1-reader-table-from-token-classifier
  [source-path classifier]
  (let [diagnostics (:diagnostics classifier)
        missing (remove #(contains? diagnostics %)
                        [:unexpected-close :unclosed-form :unclosed-string
                         :unsupported-dispatch :odd-map
                         :invalid-classifier])]
    (when-not (= :gravity-reader-token-classifier-v1 (:engine classifier))
      (stage1-reader-token-classifier-pipeline-fail!
       "STAGE1CLASS004" source-path classifier
       {:missing-fields [:engine]}))
    (doseq [field [:derived-from :whitespace :line-comment :string-delimiter
                   :delimiters :dispatch :literal-kinds :diagnostics
                   :provenance]]
      (when-not (contains? classifier field)
        (stage1-reader-token-classifier-pipeline-fail!
         "STAGE1CLASS004" source-path classifier
         {:missing-fields [field]})))
    (when-not (= :stage1-reader-table (:derived-from classifier))
      (stage1-reader-token-classifier-pipeline-fail!
       "STAGE1CLASS004" source-path classifier
       {:missing-fields [:derived-from]}))
    (when (seq missing)
      (stage1-reader-token-classifier-pipeline-fail!
       "STAGE1CLASS004" source-path diagnostics
       {:missing-fields (vec missing)}))
    {:engine :gravity-reader-table-v1
     :line-comment (:line-comment classifier)
     :string-delimiter (:string-delimiter classifier)
     :whitespace (:whitespace classifier)
     :delimiters (:delimiters classifier)
     :dispatch (:dispatch classifier)
     :literal-kinds (:literal-kinds classifier)
     :diagnostics (assoc (select-keys diagnostics
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

(defn stage1-reader-token-stream-from-classifier
  [source-path source-text classifier character-stream]
  (let [source-id (str "sha256:" (sha256-hex source-text))
        characters (:characters character-stream)
        table (stage1-reader-table-from-token-classifier source-path
                                                         classifier)]
    (when-not (= :gravity/stage1-reader-character-stream
                 (:kind character-stream))
      (stage1-reader-token-classifier-pipeline-fail!
       "STAGE1CLASS004" source-path character-stream
       {:missing-fields [:kind]}))
    (when-not (= source-path (:source-path character-stream))
      (stage1-reader-token-classifier-pipeline-fail!
       "STAGE1CLASS004" source-path character-stream
       {:missing-fields [:source-path]}))
    (when-not (= source-id (:source-id character-stream))
      (stage1-reader-token-classifier-pipeline-fail!
       "STAGE1CLASS004" source-path character-stream
       {:missing-fields [:source-id]}))
    (when-not (and (vector? characters)
                   (= (count characters)
                      (:character-count character-stream)))
      (stage1-reader-token-classifier-pipeline-fail!
       "STAGE1CLASS004" source-path character-stream
       {:missing-fields [:characters :character-count]}))
    (let [source-from-characters (apply str (map :char characters))]
      (when-not (= source-text source-from-characters)
        (stage1-reader-token-classifier-pipeline-fail!
         "STAGE1CLASS004" source-path character-stream
         {:missing-fields [:source-character-parity]}))
      (stage1-reader-token-stream source-path source-from-characters table))))