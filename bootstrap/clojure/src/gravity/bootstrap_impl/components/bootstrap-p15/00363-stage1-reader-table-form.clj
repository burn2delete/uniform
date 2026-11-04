

(defn stage1-reader-table-form
  [source-path]
  (let [source-file (stage1-reader-owned-source-file source-path)
        forms (mapv :form (read-source-form-records source-path
                                                     (slurp source-file)))
        table-form (first (filter #(and (seq? %)
                                        (= 'def (first %))
                                        (= 'stage1-reader-table (second %)))
                                  forms))]
    (when-not (and table-form (= 3 (count table-form)) (map? (nth table-form 2)))
      (stage1-reader-fail! "STAGE1READER006" source-path table-form
                           {:missing-fields [:stage1-reader-table]}))
    (nth table-form 2)))

(defn stage1-reader-validate-table!
  [source-path table]
  (let [diagnostics (:diagnostics table)
        missing (remove #(contains? diagnostics %)
                        [:unexpected-close :unclosed-form :unclosed-string
                         :unsupported-dispatch :odd-map :missing-table])]
    (when-not (= :gravity-reader-table-v1 (:engine table))
      (stage1-reader-fail! "STAGE1READER006" source-path table
                           {:missing-fields [:engine]}))
    (doseq [field [:line-comment :string-delimiter :whitespace :delimiters
                   :dispatch :literal-kinds :diagnostics
                   :accepted-fixture :rejected-fixtures]]
      (when-not (contains? table field)
        (stage1-reader-fail! "STAGE1READER006" source-path table
                             {:missing-fields [field]})))
    (when (seq missing)
      (stage1-reader-fail! "STAGE1READER006" source-path diagnostics
                           {:missing-fields (vec missing)})))
  :complete)

(defn stage1-reader-location
  [source-text line-starts idx]
  (let [idx (min idx (count source-text))
        line-number (loop [line 0]
                      (if (and (< (inc line) (count line-starts))
                               (<= (line-starts (inc line)) idx))
                        (recur (inc line))
                        (inc line)))
        line-start (line-starts (dec line-number))
        line-prefix (subs source-text line-start idx)
        column (inc (.codePointCount line-prefix 0 (.length line-prefix)))]
    {:line line-number
     :column column
     :column-unit :unicode-scalar
     :char idx
     :byte (utf8-byte-count (subs source-text 0 idx))}))

(defn stage1-reader-span
  [source-path source-text line-starts start end]
  (let [start-location (stage1-reader-location source-text line-starts start)
        end-location (stage1-reader-location source-text line-starts end)]
    {:source source-path
     :start start-location
     :end end-location
     :byte-start (:byte start-location)
     :byte-end (:byte end-location)}))

(defn stage1-reader-table
  []
  (let [table (stage1-reader-table-form stage1-reader-source-path)]
    (stage1-reader-validate-table! stage1-reader-source-path table)
    table))

(defn stage1-reader-token-kind
  [token]
  (gravity.reader/classify-compatibility-token #'str/starts-with? token))

(defn stage1-reader-malformed-numeric!
  [source-path raw span token-id cause]
  (gravity.reader/reject-compatibility-numeric! #'stage1-reader-fail! source-path raw span token-id cause))

(defn stage1-reader-decode-atom
  [source-path kind raw span token-id]
  (gravity.reader/decode-compatibility-atom
   {:starts-with-var #'str/starts-with? :split-var #'str/split
    :malformed-numeric-var #'stage1-reader-malformed-numeric!}
   source-path kind raw span token-id))

(defn stage1-reader-decode-string
  [source-path raw span token-id]
  (gravity.reader/decode-compatibility-string
   #'clojure.core/read-string #'stage1-reader-fail! source-path raw span token-id))

(defn stage1-reader-decode-character
  [source-path raw span token-id]
  (gravity.reader/decode-compatibility-character
   #'clojure.core/read-string #'stage1-reader-fail! source-path raw span token-id))
