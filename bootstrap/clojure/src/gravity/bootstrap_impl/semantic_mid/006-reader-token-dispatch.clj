(defn- semantic-mid-reader-dispatch-token
  [ops {:keys [source-path ^String source-text line-starts source-length dispatch]
    :as context}
   idx token-index]
  (let [dispatch-token ((:char-string ops) context idx)
        next-token (when (< (inc idx) source-length)
                     ((:char-string ops) context (inc idx)))
        dispatch-entry (get-in dispatch [dispatch-token next-token])]
    (if (= :set-open (:kind dispatch-entry))
      (let [end (+ idx 2)]
        [{:index token-index
          :kind (:kind dispatch-entry)
          :lexeme (str dispatch-token next-token)
          :raw (str dispatch-token next-token)
          :decoded :set
          :dispatch dispatch-token
          :close-token (:closes dispatch-entry)
          :span (stage1-reader-span source-path source-text
                                    line-starts idx end)}
         end])
      (let [end ((:atom-end ops) ops context (inc idx))
            raw (subs source-text idx end)
            tag (subs raw 1)]
        (if (contains? #{"inst" "uuid"} tag)
          [{:index token-index
            :kind :tag
            :tag (symbol tag)
            :lexeme raw
            :raw raw
            :decoded (symbol tag)
            :span (stage1-reader-span source-path source-text
                                      line-starts idx end)}
           end]
          (stage1-reader-fail!
           "STAGE1READER004" source-path dispatch-token
           {:source-span
            (stage1-reader-span source-path source-text line-starts idx
                                (min source-length (max (inc idx) end)))
            :token-id (keyword (str "tok-" token-index))
            :raw raw
            :extension-tag (when (seq tag) (symbol tag))}))))))

(defn- semantic-mid-reader-delimiter-token
  [ops {:keys [source-path source-text line-starts] :as context}
   idx token-index delimiter]
  (let [token ((:char-string ops) context idx)]
    [{:index token-index
      :kind (:kind delimiter)
      :lexeme token
      :raw token
      :decoded token
      :close-token (:closes delimiter)
      :span (stage1-reader-span source-path source-text
                                line-starts idx (inc idx))}
     (inc idx)]))

(defn- semantic-mid-reader-next-token
  [ops {:keys [retain-trivia? string-delimiter delimiters dispatch] :as context}
   idx token-index]
  (let [token ((:char-string ops) context idx)
        delimiter (get delimiters token)]
    (cond
      (and retain-trivia? ((:ignored? ops) ops context idx))
      ((:whitespace-token ops) ops context idx token-index)

      (and retain-trivia? ((:comment? ops) ops context idx))
      ((:comment-token ops) context idx token-index)

      (= string-delimiter token)
      ((:string-token ops) ops context idx token-index)

      (= "\\" token)
      ((:character-token ops) ops context idx token-index)

      (contains? #{"'" "`" "~" "^" "@"} token)
      ((:abbreviation-token ops) ops context idx token-index)

      (contains? dispatch token)
      ((:dispatch-token ops) ops context idx token-index)

      delimiter
      ((:delimiter-token ops) ops context idx token-index delimiter)

      :else
      ((:atom-token ops) ops context idx token-index))))

(defn- semantic-mid-reader-tokenize
  [ops {:keys [retain-trivia? source-length source-path source-text table-id]
    :as context}]
  (loop [idx 0
         token-index 0
         tokens []]
    (let [idx (if retain-trivia?
                idx
                ((:skip-ignored ops) ops context idx))]
      (if (>= idx source-length)
        {:kind :gravity/stage1-reader-token-stream
         :source-path source-path
         :source-id (str "sha256:" (sha256-hex source-text))
         :reader-table-id table-id
         :token-count (count tokens)
         :tokens tokens
         :status :complete}
        (let [[token-record next-idx]
              ((:next-token ops) ops context idx token-index)]
          (recur next-idx
                 (inc token-index)
                 (conj tokens token-record)))))))
