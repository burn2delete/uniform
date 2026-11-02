

(defn p15-s23-seed-readable-validate-scalar!
  [source-path kind value]
  (case kind
    :floating
    (p15-s23-seed-readable-floating-text source-path value)

    :ratio
    (p15-s23-seed-readable-ratio-components! source-path value)

    :character
    (when (Character/isSurrogate (char value))
      (p15-s23-seed-readable-printer-fail!
       source-path :invalid-unicode-scalar
       {:scalar-kind :character}))

    :string
    (p15-s23-seed-readable-validate-utf8! source-path kind value)

    :instant
    (do
      (p15-s23-seed-readable-instant-text source-path value)
      nil)

    :uuid
    (p15-s23-seed-readable-uuid-text value)

    :keyword
    (let [namespace-text (namespace value)
          name-text (name value)
          rendered (p15-s23-seed-readable-named-scalar-text kind value)]
      (when namespace-text
        (p15-s23-seed-readable-validate-utf8!
         source-path kind namespace-text))
      (p15-s23-seed-readable-validate-utf8! source-path kind name-text)
      (when-not (and (seq rendered)
                     (p15-s23-seed-readable-name-safe?
                      name-text false)
                     (or (nil? namespace-text)
                         (p15-s23-seed-readable-name-safe?
                          namespace-text false))
                     (= :keyword (stage1-reader-token-kind rendered))
                     (= value (keyword (subs rendered 1))))
        (p15-s23-seed-readable-printer-fail!
         source-path :unreadable-name
         {:scalar-kind kind})))

    :symbol
    (let [namespace-text (namespace value)
          name-text (name value)
          rendered (p15-s23-seed-readable-named-scalar-text kind value)]
      (when namespace-text
        (p15-s23-seed-readable-validate-utf8!
         source-path kind namespace-text))
      (p15-s23-seed-readable-validate-utf8! source-path kind name-text)
      (when-not (and (seq rendered)
                     (p15-s23-seed-readable-name-safe?
                      name-text true)
                     (or (nil? namespace-text)
                         (p15-s23-seed-readable-name-safe?
                          namespace-text true))
                     (= :symbol (stage1-reader-token-kind rendered))
                     (= value (symbol rendered)))
        (p15-s23-seed-readable-printer-fail!
         source-path :unreadable-name
         {:scalar-kind kind})))

    nil))

(defn p15-s23-seed-readable-snapshot-scalar!
  [source-path kind value]
  (case kind
    :instant (java.util.Date. (.getTime ^java.util.Date value))
    :integer
    (if (identical? clojure.lang.BigInt (class value))
      (let [big-part (.-bipart ^clojure.lang.BigInt value)]
        (when-not (or (nil? big-part)
                      (identical? java.math.BigInteger (class big-part)))
          (p15-s23-seed-readable-printer-fail!
           source-path :invalid-bigint-component-carrier {}))
        (if big-part
          (clojure.lang.BigInt/fromBigInteger big-part)
          (clojure.lang.BigInt/fromLong
           (.-lpart ^clojure.lang.BigInt value))))
      value)
    :ratio
    (let [[numerator denominator]
          (p15-s23-seed-readable-ratio-components! source-path value)]
      (clojure.lang.Ratio. numerator denominator))
    value))
