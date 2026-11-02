

(defn sh03-reader-host-character-value!
  [source-path descriptor]
  (let [codepoint (:codepoint descriptor)]
    (when-not (and (integer? codepoint)
                   (<= 0 codepoint 0x10ffff)
                   (not (<= 0xd800 codepoint 0xdfff)))
      (sh03-reader-boundary-fail!
       source-path :sh03-reader-character-value descriptor {}))
    (if (<= codepoint 0xffff)
      (char codepoint)
      {:artifact :gravity/unicode-scalar-character
       :schema-version 1
       :codepoint codepoint
       :text (String. (Character/toChars (int codepoint)))})))

(defn sh03-reader-host-atomic-value!
  [source-path source-bytes source-content-id scalar-boundaries form descriptor]
  (let [kind (:kind form)
        raw (sh03-reader-accepted-raw-text!
             source-path source-bytes source-content-id scalar-boundaries
             (:raw form) (:span form))]
    (case kind
      :nil nil
      :boolean (:value descriptor)
      :integer (sh03-reader-host-bigint! source-path (:value descriptor))
      :ratio
      (let [numerator (sh03-reader-host-bigint!
                       source-path (:numerator descriptor))
            denominator (sh03-reader-host-bigint!
                         source-path (:denominator descriptor))]
        (if (zero? denominator)
          {:artifact :gravity/deferred-ratio-literal
           :kind :ratio
           :raw raw
           :numerator-spelling
           (sh03-reader-codepoints-text!
            source-path (:numerator-spelling descriptor))
           :denominator-spelling
           (sh03-reader-codepoints-text!
            source-path (:denominator-spelling descriptor))
           :numerator numerator
           :denominator denominator
           :semantic-validation :deferred
           :reason :zero-denominator}
          (/ numerator denominator)))
      :decimal (sh03-reader-host-decimal-value!
                source-path raw descriptor)
      :string (sh03-reader-codepoints-text!
               source-path (:decoded-codepoints descriptor))
      :character (sh03-reader-host-character-value! source-path descriptor)
      :symbol (symbol (sh03-reader-codepoints-text!
                       source-path (:name-codepoints descriptor)))
      :keyword (keyword (sh03-reader-codepoints-text!
                         source-path (:name-codepoints descriptor)))
      (sh03-reader-boundary-fail!
       source-path :sh03-reader-atomic-kind form {:kind kind}))))

(defn sh03-reader-metadata-map!
  [source-path metadata-value metadata-form]
  (cond
    (map? metadata-value) metadata-value
    (keyword? metadata-value) {metadata-value true}
    (or (symbol? metadata-value) (string? metadata-value))
    {:tag metadata-value}
    :else
    (sh03-reader-boundary-fail!
     source-path :sh03-reader-metadata-value metadata-form
     {:metadata-value metadata-value})))

(defn sh03-reader-host-values!
  ([source-path source-bytes source-content-id scalar-boundaries
    form-tree semantic-index source-unit]
   (sh03-reader-host-values! source-path source-bytes source-content-id scalar-boundaries
                            form-tree semantic-index source-unit nil))
  ([source-path source-bytes source-content-id scalar-boundaries
    form-tree semantic-index source-unit implementation]
   (sh03-reader-host-values! source-path source-bytes source-content-id scalar-boundaries
                            form-tree semantic-index source-unit implementation nil))
  ([source-path source-bytes source-content-id scalar-boundaries
    form-tree semantic-index source-unit implementation verified-result]
  (let [forms-by-id (into {} (map (juxt :form-id identity) form-tree))
        resource? (reader-resource-options? (:reader-options source-unit))
        resource-data (when resource?
                        (into {} (map (juxt :form-id identity)
                                      (:invocations (reader-resource-record! verified-result)))))
        sealed? (reader-pure-text/options? (:reader-options source-unit))
        sealed-data (when sealed? (reader-pure-text/returned-data! verified-result))
        pure? (reader-dispatch/pure-source? source-unit)
        tags (mapv #(symbol (sh03-reader-codepoints-text!
                             source-path (:tag-codepoints %)))
                   (filter #(= :tagged-literal (:kind %)) form-tree))
        _ (when (and (seq tags) (not sealed?) (not resource?))
            (if pure?
              (reader-dispatch/validate-pure-tags! source-unit tags)
              (reader-dispatch/validate-standard-tags!
               source-unit standard-reader-policy tags)))]
    (loop [remaining (reverse form-tree)
           values {}
           observations []]
      (if (empty? remaining)
        (if pure?
          {:values values :extension-dispatch-observations []
           :pure-extension-results observations}
          {:values values :extension-dispatch-observations observations})
        (let [form (first remaining)
              kind (:kind form)
              child-values (mapv values (:children form))
              _ (when (some nil? (map #(get forms-by-id %) (:children form)))
                  (sh03-reader-boundary-fail!
                   source-path :sh03-reader-form-child-link form {}))
              dispatch-result
              (when (= :tagged-literal kind)
                (let [tag (symbol (sh03-reader-codepoints-text!
                                   source-path (:tag-codepoints form)))]
                  (when-not (= 1 (count child-values))
                    (sh03-reader-boundary-fail!
                     source-path :sh03-reader-tag-payload form {}))
                  (if resource?
                    (let [invocation (get resource-data (:form-id form))]
                      (when-not (and (= tag 'resource)
                                     (= (first child-values) (get-in invocation [:input-seed :datum])))
                        (sh03-reader-boundary-fail! source-path :original-resource-payload form {}))
                      {:value (get-in invocation [:returned-seed :datum])})
                  (if sealed?
                    (let [datum (get sealed-data (:form-id form))
                          value (sh03-reader-codepoints-text! source-path (:decoded-codepoints datum))]
                      (when-not (= value (first child-values))
                        (sh03-reader-boundary-fail! source-path :sealed-pure-text-payload form {}))
                      {:value value})
                  (if pure?
                    (reader-dispatch/dispatch-pure-tag
                     source-unit tag form (first child-values) implementation)
                    (reader-dispatch/dispatch-standard-tag
                     source-unit standard-reader-policy tag form (first child-values)))))))
              value
              (case kind
                (:nil :boolean :integer :ratio :decimal :string
                 :character :symbol :keyword)
                (let [reference (:value form)
                      descriptor
                      (sh03-reader-semantic-reference-value!
                       source-path semantic-index reference :descriptor)
                      entry (get-in semantic-index
                                    [:by-value-id (:value-id reference)])]
                  (when-not
                   (and (= (:form-id form) (:form-id entry))
                        (= (:open-token form) (:token-id entry))
                        (= kind (:kind entry)))
                    (sh03-reader-boundary-fail!
                     source-path :form-bound-sh03-reader-semantic-value
                     entry {:form-id (:form-id form)}))
                  (sh03-reader-host-atomic-value!
                   source-path source-bytes source-content-id
                   scalar-boundaries form descriptor))

                :list (apply list child-values)
                :vector (vec child-values)
                :map (apply hash-map child-values)
                :set (set child-values)

                :abbreviation
                (let [operator ({:quote 'quote
                                 :syntax-quote 'syntax-quote
                                 :unquote 'unquote
                                 :splice-unquote 'splice-unquote
                                 :deref 'deref}
                                (:abbrev form))]
                  (when-not (and operator (= 1 (count child-values)))
                    (sh03-reader-boundary-fail!
                     source-path :sh03-reader-abbreviation-value form {}))
                  (list operator (first child-values)))

                :metadata-wrapper
                (let [[metadata-value target] child-values
                      metadata (sh03-reader-metadata-map!
                                source-path metadata-value form)]
                  (when-not (and (= 2 (count child-values))
                                 (instance? clojure.lang.IObj target))
                    (sh03-reader-boundary-fail!
                     source-path :sh03-reader-metadata-target form {}))
                  (with-meta target (merge (meta target) metadata)))

                :tagged-literal
                (:value dispatch-result)

                (sh03-reader-boundary-fail!
                 source-path :sh03-reader-form-kind form {:kind kind}))]
          (recur (rest remaining) (assoc values (:form-id form) value)
                 (cond-> observations (and dispatch-result (not sealed?) (not resource?))
                   (conj (if pure? (:pure-result dispatch-result)
                             (:observation dispatch-result)))))))))))

(defn sh03-reader-legacy-trivia-hash
  [token-ids tokens-by-id]
  (reader-canonical-hash
   (mapv (fn [token-id]
           (let [token (tokens-by-id token-id)]
             {:kind (:kind token)
              :raw (:raw token)
              :span (dissoc (:span token) :source :file)}))
         token-ids)))

(defn sh03-reader-contiguous-trivia
  [tokens position direction token-id-map]
  (loop [cursor (+ position direction)
         result []]
    (if (and (<= 0 cursor) (< cursor (count tokens))
             (true? (:trivia? (nth tokens cursor))))
      (recur (+ cursor direction)
             (conj result (token-id-map (:token-id (nth tokens cursor)))))
      (if (neg? direction) (vec (reverse result)) (vec result)))))

(defn sh03-reader-legacy-id
  [value]
  (if (string? value)
    (if-let [[_ family ordinal] (re-matches #"(token|form)/([0-9]+)" value)]
      (keyword (str (if (= family "token") "tok-" "form-") ordinal))
      value)
    value))

(defn sh03-reader-related-records
  [source-path source-id related]
  (mapv
   (fn [record]
     (cond-> record
       (:span record)
       (update :span #(sh03-reader-path-span source-path source-id %))

       (:artifact record)
       (update :artifact sh03-reader-legacy-id)))
   (or related [])))
