(defn- semantic-mid-reader-string-token
  [ops {:keys [source-path ^String source-text line-starts source-length
           string-delimiter] :as context}
   idx token-index]
  (let [start idx]
    (loop [idx (inc idx)]
      (cond
        (>= idx source-length)
        (stage1-reader-fail!
         "STAGE1READER003" source-path nil
         {:source-span (stage1-reader-span source-path source-text
                                           line-starts start idx)
          :token-id (keyword (str "tok-" token-index))
          :raw (subs source-text start idx)})

        (= string-delimiter ((:char-string ops) context idx))
        (let [end (inc idx)
              raw (subs source-text start end)
              span (stage1-reader-span source-path source-text
                                       line-starts start end)
              decoded (stage1-reader-decode-string
                       source-path raw span
                       (keyword (str "tok-" token-index)))]
          [{:index token-index
            :kind :string
            :lexeme decoded
            :raw raw
            :decoded decoded
            :span span}
           end])

        (= \\ (.charAt source-text idx))
        (if (< (inc idx) source-length)
          (recur (+ idx 2))
          (stage1-reader-fail!
           "STAGE1READER003" source-path nil
           {:source-span (stage1-reader-span source-path source-text
                                             line-starts start idx)
            :token-id (keyword (str "tok-" token-index))
            :raw (subs source-text start idx)}))
        :else (recur (inc idx))))))

(defn- semantic-mid-reader-atom-token
  [ops {:keys [source-path ^String source-text line-starts] :as context}
   idx token-index]
  (let [start idx
        end ((:atom-end ops) ops context idx)
        token (subs source-text start end)
        kind (stage1-reader-token-kind token)
        token-id (keyword (str "tok-" token-index))
        span (stage1-reader-span source-path source-text
                                 line-starts start end)]
    [{:index token-index
      :kind kind
      :lexeme token
      :raw token
      :decoded (stage1-reader-decode-atom source-path kind token
                                          span token-id)
      :span span}
     end]))

(defn- semantic-mid-reader-character-token
  [ops {:keys [source-path ^String source-text line-starts source-length
           delimiters] :as context}
   idx token-index]
  (let [word-start (inc idx)
        end (cond
              (>= word-start source-length) word-start
              (or ((:ignored? ops) ops context word-start)
                  ((:comment? ops) ops context word-start))
              word-start
              (contains? delimiters
                         ((:char-string ops) context word-start))
              (inc word-start)
              :else ((:atom-end ops) ops context word-start))
        raw (subs source-text idx end)
        span (stage1-reader-span source-path source-text
                                 line-starts idx end)
        decoded (stage1-reader-decode-character
                 source-path raw span
                 (keyword (str "tok-" token-index)))]
    [{:index token-index
      :kind :character
      :lexeme decoded
      :raw raw
      :decoded decoded
      :span span}
     end]))

(defn- semantic-mid-reader-abbreviation-token
  [ops {:keys [source-path ^String source-text line-starts source-length]
    :as context}
   idx token-index]
  (let [splice? (and (= "~" ((:char-string ops) context idx))
                     (< (inc idx) source-length)
                     (= "@" ((:char-string ops) context (inc idx))))
        end (+ idx (if splice? 2 1))
        raw (subs source-text idx end)
        abbrev (case raw
                 "'" :quote
                 "`" :syntax-quote
                 "~" :unquote
                 "~@" :splice-unquote
                 "^" :metadata
                 "@" :deref)]
    [{:index token-index
      :kind :abbreviation
      :abbrev abbrev
      :lexeme raw
      :raw raw
      :decoded abbrev
      :span (stage1-reader-span source-path source-text
                                line-starts idx end)}
     end]))
