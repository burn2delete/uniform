

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
  (cond
    (= token "nil") :nil
    (= token "true") :boolean
    (= token "false") :boolean
    (str/starts-with? token ":") :keyword
    (re-matches #"[+-]?(?:0[xX][0-9A-Fa-f]+|0[bB][01]+|[0-9]+)" token)
    :integer
    (re-matches #"[+-]?[0-9]+/[0-9]+" token) :ratio
    (re-matches #"[+-]?(?:(?:(?:[0-9]+\.[0-9]*)|(?:[0-9]*\.[0-9]+))(?:[eE][+-]?[0-9]+)?|(?:[0-9]+[eE][+-]?[0-9]+))"
                token)
    :decimal
    (re-find #"^[+-]?[0-9]" token) :malformed-number
    :else :symbol))

(defn stage1-reader-malformed-numeric!
  [source-path raw span token-id cause]
  (stage1-reader-fail!
   "STAGE1READER007" source-path raw
   {:source-span span
    :token-id token-id
    :raw raw
    :cause-message cause
    :facts {:literal-kind :numeric
            :raw-spelling raw}}))

(defn stage1-reader-decode-atom
  [source-path kind raw span token-id]
  (try
    (case kind
      :nil nil
      :boolean (= "true" raw)
      :keyword (keyword (subs raw 1))
      :integer
      (let [negative? (str/starts-with? raw "-")
            positive? (str/starts-with? raw "+")
            unsigned (if (or negative? positive?) (subs raw 1) raw)
            [radix digits] (cond
                             (re-find #"^0[xX]" unsigned)
                             [16 (subs unsigned 2)]
                             (re-find #"^0[bB]" unsigned)
                             [2 (subs unsigned 2)]
                             :else [10 unsigned])
            value (bigint (java.math.BigInteger. digits radix))]
        (if negative? (- value) value))
      :ratio
      (let [[numerator-spelling denominator-spelling] (str/split raw #"/" 2)
            numerator (bigint numerator-spelling)
            denominator (bigint denominator-spelling)]
        (if (zero? denominator)
          {:artifact :gravity/deferred-ratio-literal
           :kind :ratio
           :raw raw
           :numerator-spelling numerator-spelling
           :denominator-spelling denominator-spelling
           :numerator numerator
           :denominator denominator
           :semantic-validation :deferred
           :reason :zero-denominator}
          (/ numerator denominator)))
      :decimal (Double/parseDouble raw)
      :malformed-number
      (stage1-reader-malformed-numeric!
       source-path raw span token-id
       "numeric lexeme does not match a declared Gravity numeric shape")
      :symbol (symbol raw)
      raw)
    (catch NumberFormatException ex
      (stage1-reader-malformed-numeric! source-path raw span token-id
                                        (.getMessage ex)))
    (catch ArithmeticException ex
      (stage1-reader-malformed-numeric! source-path raw span token-id
                                        (.getMessage ex)))))

(defn stage1-reader-decode-string
  [source-path raw span token-id]
  (try
    (binding [*read-eval* false]
      (read-string raw))
    (catch Exception ex
      (stage1-reader-fail! "STAGE1READER003" source-path raw
                           {:source-span span
                            :token-id token-id
                            :raw raw
                            :cause-message (.getMessage ex)}))))

(defn stage1-reader-decode-character
  [source-path raw span token-id]
  (try
    (let [value (binding [*read-eval* false]
                  (read-string raw))]
      (when-not (char? value)
        (throw (ex-info "reader character token did not decode to a character"
                        {:raw raw})))
      value)
    (catch Exception ex
      (stage1-reader-fail! "STAGE1READER003" source-path raw
                           {:source-span span
                            :token-id token-id
                            :raw raw
                            :cause-message (.getMessage ex)}))))