

(defn sh03-reader-source-slice-text!
  [source-path source-bytes source-content-id scalar-boundaries raw]
  (let [byte-count (alength source-bytes)
        start (:byte-start raw)
        end (:byte-end raw)
        scalar-start (:scalar-start raw)
        scalar-end (:scalar-end raw)]
    (when-not
     (and (map? raw)
          (= sh03-reader-source-slice-keys (set (keys raw)))
          (= :gravity/source-slice (:artifact raw))
          (= 1 (:schema-version raw))
          (= :utf-8 (:encoding raw))
          (= source-content-id (:source-content-id raw))
          (integer? start) (integer? end)
          (<= 0 start end byte-count)
          (integer? scalar-start) (integer? scalar-end)
          (<= 0 scalar-start scalar-end)
          (= scalar-start (get scalar-boundaries start ::missing))
          (= scalar-end (get scalar-boundaries end ::missing)))
      (sh03-reader-boundary-fail!
       source-path :source-bound-sh03-reader-slice raw
       {:source-byte-count byte-count
        :expected-source-content-id source-content-id}))
    (let [bytes (java.util.Arrays/copyOfRange source-bytes start end)
          text (sh03-reader-decode-raw-bytes! source-path raw bytes)
          observed-scalars (.codePointCount text 0 (.length text))]
      (when-not (= observed-scalars (- scalar-end scalar-start))
        (sh03-reader-boundary-fail!
         source-path :unicode-scalar-bound-sh03-reader-slice raw
         {:observed-scalar-count observed-scalars}))
      text)))

(defn sh03-reader-raw-text!
  ([source-path raw]
   (when-not (and (map? raw)
                  (= :utf-8 (:encoding raw))
                  (vector? (:bytes raw))
                  (every? #(and (integer? %) (<= 0 % 255)) (:bytes raw)))
     (sh03-reader-boundary-fail!
      source-path :sh03-reader-inline-raw-utf8 raw {}))
   (sh03-reader-decode-raw-bytes!
    source-path raw (sh03-reader-byte-array (:bytes raw))))
  ([source-path source-bytes source-content-id raw]
   (if (= :gravity/source-slice (:artifact raw))
     (sh03-reader-source-slice-text!
      source-path source-bytes source-content-id
      (sh03-reader-source-scalar-boundaries!
       source-path
       (sh03-reader-decode-raw-bytes! source-path raw source-bytes)
       source-bytes)
      raw)
     (sh03-reader-raw-text! source-path raw))))

(defn sh03-reader-accepted-raw-text!
  [source-path source-bytes source-content-id scalar-boundaries raw span]
  (when-not
   (and (= :gravity/source-slice (:artifact raw))
        (= (:byte-start raw) (:byte-start span))
        (= (:byte-end raw) (:byte-end span)))
    (sh03-reader-boundary-fail!
     source-path :span-bound-sh03-reader-source-slice raw
     {:owner-span span}))
  (sh03-reader-source-slice-text!
   source-path source-bytes source-content-id scalar-boundaries raw))

(defn sh03-reader-codepoints-text!
  [source-path codepoints]
  (when-not (and (vector? codepoints)
                 (every? #(and (integer? %)
                               (<= 0 % 0x10ffff)
                               (not (<= 0xd800 % 0xdfff)))
                         codepoints))
    (sh03-reader-boundary-fail!
     source-path :valid-sh03-reader-unicode-scalars codepoints {}))
  (let [builder (StringBuilder.)]
    (doseq [codepoint codepoints]
      (.appendCodePoint builder codepoint))
    (.toString builder)))

(defn sh03-reader-semantic-value-index!
  [source-path entries]
  (when-not (vector? entries)
    (sh03-reader-boundary-fail!
     source-path :sh03-reader-semantic-value-table entries {}))
  (let [valid-entry?
        (fn [entry]
          (and (map? entry)
               (= sh03-reader-semantic-value-entry-keys
                  (set (keys entry)))
               (= :gravity/semantic-value (:artifact entry))
               (= 1 (:schema-version entry))
               (string? (:value-id entry))
               (string? (:form-id entry))
               (string? (:token-id entry))
               (keyword? (:kind entry))))
        value-ids (mapv :value-id entries)
        form-ids (mapv :form-id entries)
        token-ids (mapv :token-id entries)]
    (when-not
     (and (every? valid-entry? entries)
          (= (count value-ids) (count (distinct value-ids)))
          (= (count form-ids) (count (distinct form-ids)))
          (= (count token-ids) (count (distinct token-ids))))
      (sh03-reader-boundary-fail!
       source-path :exact-unique-sh03-reader-semantic-values entries {}))
    {:by-value-id (into {} (map (juxt :value-id identity) entries))
     :by-form-id (into {} (map (juxt :form-id identity) entries))
     :by-token-id (into {} (map (juxt :token-id identity) entries))}))

(defn sh03-reader-semantic-reference-value!
  [source-path semantic-index reference expected-field]
  (when-not
   (and (map? reference)
        (= sh03-reader-semantic-value-reference-keys
           (set (keys reference)))
        (= :gravity/semantic-value-reference (:artifact reference))
        (= 1 (:schema-version reference))
        (= expected-field (:field reference)))
    (sh03-reader-boundary-fail!
     source-path :exact-sh03-reader-semantic-value-reference reference
     {:expected-field expected-field}))
  (let [entry (get-in semantic-index [:by-value-id (:value-id reference)])]
    (when-not entry
      (sh03-reader-boundary-fail!
       source-path :resolving-sh03-reader-semantic-value-reference reference {}))
    (get entry expected-field)))

(defn sh03-reader-form-value-reference!
  [source-path reference expected-form-id]
  (when-not
   (and (map? reference)
        (= sh03-reader-form-value-reference-keys (set (keys reference)))
        (= :gravity/form-value-reference (:artifact reference))
        (= 1 (:schema-version reference))
        (= expected-form-id (:form-id reference)))
    (sh03-reader-boundary-fail!
     source-path :exact-sh03-reader-form-value-reference reference
     {:expected-form-id expected-form-id}))
  reference)

(def sh03-reader-atomic-kinds
  #{:nil :boolean :integer :ratio :decimal :string :character :symbol :keyword})

(defn sh03-reader-semantic-value-closure!
  [source-path source-bytes source-content-id scalar-boundaries
   tokens forms semantic-index]
  (let [tokens-by-id (into {} (map (juxt :token-id identity) tokens))
        forms-by-id (into {} (map (juxt :form-id identity) forms))
        atomic-forms (filterv #(contains? sh03-reader-atomic-kinds (:kind %))
                              forms)
        entries (vals (:by-value-id semantic-index))]
    (when-not (= (count atomic-forms) (count entries))
      (sh03-reader-boundary-fail!
       source-path :complete-sh03-reader-semantic-value-closure entries
       {:expected-atomic-form-count (count atomic-forms)}))
    (doseq [entry entries]
      (let [form (forms-by-id (:form-id entry))
            token (tokens-by-id (:token-id entry))
            _ (sh03-reader-semantic-reference-value!
               source-path semantic-index (:value form) :descriptor)
            _ (sh03-reader-semantic-reference-value!
               source-path semantic-index (:semantic-key form) :semantic-key)
            _ (sh03-reader-semantic-reference-value!
               source-path semantic-index (:descriptor token) :descriptor)]
        (when (and (map? (:descriptor entry))
                   (contains? (:descriptor entry) :raw))
          (sh03-reader-accepted-raw-text!
           source-path source-bytes source-content-id scalar-boundaries
           (get-in entry [:descriptor :raw]) (:span form)))
        (when-not
         (and form token
              (= (:kind entry) (:kind form) (:kind token))
              (= (:token-id entry) (:open-token form))
              (= (:value-id entry) (get-in form [:value :value-id]))
              (= :descriptor (get-in form [:value :field]))
              (= (:value-id entry) (get-in form [:semantic-key :value-id]))
              (= :semantic-key (get-in form [:semantic-key :field]))
              (= (:value-id entry) (get-in token [:descriptor :value-id]))
              (= :descriptor (get-in token [:descriptor :field])))
          (sh03-reader-boundary-fail!
           source-path :owned-sh03-reader-semantic-value-reference
           entry {:form form :token token}))))
    :complete))

(defn sh03-reader-path-span
  [source-path source-id span]
  (when-not (map? span)
    (sh03-reader-boundary-fail!
     source-path :sh03-reader-span span {}))
  (assoc span :source source-path :file source-id))

(defn sh03-reader-host-bigint!
  [source-path value]
  (cond
    (integer? value)
    value

    (and (map? value)
         (= :gravity/arbitrary-precision-integer (:artifact value))
         (contains? #{:negative :positive} (:sign value))
         (contains? #{2 10 16} (:radix value))
         (vector? (:digit-codepoints value)))
    (let [digits (sh03-reader-codepoints-text!
                  source-path (:digit-codepoints value))
          magnitude (java.math.BigInteger. digits (int (:radix value)))
          signed (if (= :negative (:sign value))
                   (.negate magnitude)
                   magnitude)]
      (clojure.lang.BigInt/fromBigInteger signed))

    :else
    (sh03-reader-boundary-fail!
     source-path :sh03-reader-integer-descriptor value {})))

(defn sh03-reader-host-decimal-value!
  [source-path raw descriptor]
  (try
    (bigdec raw)
    (catch NumberFormatException _
      {:artifact :gravity/decimal-literal
       :kind :decimal
       :raw raw
       :integer-spelling
       (sh03-reader-codepoints-text!
        source-path (:integer-spelling descriptor))
       :fraction-spelling
       (sh03-reader-codepoints-text!
        source-path (:fraction-spelling descriptor))
       :exponent-spelling
       (sh03-reader-codepoints-text!
        source-path (:exponent-spelling descriptor))
       :semantic-key (:semantic-key descriptor)
       :semantic-validation :deferred
       :reason :host-independent-decimal-range})))