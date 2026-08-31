(defn- semantic-mid-reader-token-context
  [source-path ^String source-text table reader-options]
  (let [line-starts (line-start-indices source-text)
        whitespace (set (:whitespace table))
        delimiters (:delimiters table)
        dispatch (:dispatch table)
        comment-marker (:line-comment table)
        string-delimiter (:string-delimiter table)
        retain-trivia? (true? (:retain-comments reader-options))
        source-length (count source-text)
        table-id (str "sha256:" (sha256-hex (pr-str table)))]
    {:source-path source-path
     :source-text source-text
     :line-starts line-starts
     :whitespace whitespace
     :delimiters delimiters
     :dispatch dispatch
     :comment-marker comment-marker
     :string-delimiter string-delimiter
     :retain-trivia? retain-trivia?
     :source-length source-length
     :table-id table-id}))

(defn- semantic-mid-reader-char-string
  [{:keys [^String source-text]} idx]
  (str (.charAt source-text idx)))

(defn- semantic-mid-reader-ignored?
  [ops {:keys [source-length whitespace] :as context} idx]
  (and (< idx source-length)
       (contains? whitespace
                  ((:char-string ops) context idx))))

(defn- semantic-mid-reader-comment?
  [ops {:keys [source-length comment-marker] :as context} idx]
  (and (< idx source-length)
       (= comment-marker ((:char-string ops) context idx))))

(defn- semantic-mid-reader-skip-ignored
  [ops {:keys [source-length ^String source-text] :as context} idx]
  (loop [idx idx]
    (cond
      (>= idx source-length) idx
      ((:ignored? ops) ops context idx) (recur (inc idx))
      ((:comment? ops) ops context idx)
      (recur (loop [j idx]
               (if (and (< j source-length)
                        (not (line-terminator-char? (.charAt source-text j))))
                 (recur (inc j))
                 j)))
      :else idx)))

(defn- semantic-mid-reader-whitespace-token
  [ops {:keys [source-path ^String source-text line-starts source-length]
    :as context}
   idx token-index]
  (let [end (loop [end idx]
              (if (and (< end source-length)
                       ((:ignored? ops) ops context end))
                (recur (inc end))
                end))
        raw (subs source-text idx end)]
    [{:index token-index
      :kind :whitespace
      :lexeme raw
      :raw raw
      :decoded nil
      :trivia? true
      :span (stage1-reader-span source-path source-text
                                line-starts idx end)}
     end]))

(defn- semantic-mid-reader-comment-token
  [{:keys [source-path ^String source-text line-starts source-length]}
   idx token-index]
  (let [end (loop [end idx]
              (if (and (< end source-length)
                       (not (line-terminator-char?
                             (.charAt source-text end))))
                (recur (inc end))
                end))
        raw (subs source-text idx end)]
    [{:index token-index
      :kind :comment
      :lexeme raw
      :raw raw
      :decoded nil
      :trivia? true
      :span (stage1-reader-span source-path source-text
                                line-starts idx end)}
     end]))

(defn- semantic-mid-reader-atom-end
  [ops {:keys [source-length string-delimiter delimiters dispatch] :as context}
   idx]
  (loop [idx idx]
    (if (or (>= idx source-length)
            ((:ignored? ops) ops context idx)
            ((:comment? ops) ops context idx)
            (= string-delimiter ((:char-string ops) context idx))
            (contains? #{"'" "`" "~" "^" "@" "\\"}
                       ((:char-string ops) context idx))
            (contains? delimiters
                       ((:char-string ops) context idx))
            (contains? dispatch
                       ((:char-string ops) context idx)))
      idx
      (recur (inc idx)))))
