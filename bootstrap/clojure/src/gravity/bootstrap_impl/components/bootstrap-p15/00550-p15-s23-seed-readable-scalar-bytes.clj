

(defn p15-s23-seed-readable-scalar-bytes!
  [source-path kind value]
  (case kind
      :nil 1
      :boolean 5
      :integer
      (p15-s23-seed-readable-integer-size! source-path value nil)
      :ratio
      (let [[_ _ numerator-size denominator-size]
            (p15-s23-seed-readable-ratio-components! source-path value)]
        (+ 1 numerator-size denominator-size))
      :floating
      (p15-s23-seed-readable-bounded-text-bytes!
       source-path kind (p15-s23-seed-readable-floating-text source-path value))
      :character
      (let [unit (char value)]
        (when (Character/isSurrogate unit)
          (p15-s23-seed-readable-printer-fail!
           source-path :invalid-unicode-scalar
           {:scalar-kind :character}))
        10)
      :string
      (p15-s23-seed-readable-bounded-text-bytes!
       source-path kind value)
      :instant
      (p15-s23-seed-readable-bounded-text-bytes!
       source-path kind
       (p15-s23-seed-readable-instant-text source-path value))
      :uuid
      (p15-s23-seed-readable-bounded-text-bytes!
       source-path kind (p15-s23-seed-readable-uuid-text value))
      :keyword
      (let [namespace-text (namespace value)
            name-text (name value)
            symbol-name? false
            namespace-bytes
            (if namespace-text
              (p15-s23-seed-readable-bounded-text-bytes!
               source-path kind namespace-text)
              0)
            name-bytes
            (p15-s23-seed-readable-bounded-text-bytes!
             source-path kind name-text)
            named-bytes (+ 1 (if namespace-text 1 0)
                           namespace-bytes name-bytes)
            maximum-scalar-bytes
            (:maximum-scalar-bytes p15-s23-seed-readable-printer-limits)
            named-size-check
            (when (> named-bytes maximum-scalar-bytes)
              (p15-s23-seed-readable-printer-fail!
               source-path :scalar-byte-limit
               {:scalar-kind kind
                :observed-scalar-bytes named-bytes
                :maximum-scalar-bytes maximum-scalar-bytes}))
            rendered
            (p15-s23-seed-readable-named-scalar-text kind value)]
        (when-not (and (seq rendered)
                       (p15-s23-seed-readable-name-safe?
                        name-text symbol-name?)
                       (or (nil? namespace-text)
                           (p15-s23-seed-readable-name-safe?
                            namespace-text false))
                       (= :keyword (stage1-reader-token-kind rendered))
                       (= value (keyword (subs rendered 1))))
          (p15-s23-seed-readable-printer-fail!
           source-path :unreadable-name
           {:scalar-kind kind}))
        named-bytes)
      :symbol
      (let [namespace-text (namespace value)
            name-text (name value)
            symbol-name? true
            namespace-bytes
            (if namespace-text
              (p15-s23-seed-readable-bounded-text-bytes!
               source-path kind namespace-text)
              0)
            name-bytes
            (p15-s23-seed-readable-bounded-text-bytes!
             source-path kind name-text)
            named-bytes (+ (if namespace-text 1 0)
                           namespace-bytes name-bytes)
            maximum-scalar-bytes
            (:maximum-scalar-bytes p15-s23-seed-readable-printer-limits)
            named-size-check
            (when (> named-bytes maximum-scalar-bytes)
              (p15-s23-seed-readable-printer-fail!
               source-path :scalar-byte-limit
               {:scalar-kind kind
                :observed-scalar-bytes named-bytes
                :maximum-scalar-bytes maximum-scalar-bytes}))
            rendered
            (p15-s23-seed-readable-named-scalar-text kind value)]
        (when-not (and (seq rendered)
                       (p15-s23-seed-readable-name-safe?
                        name-text symbol-name?)
                       (or (nil? namespace-text)
                           (p15-s23-seed-readable-name-safe?
                            namespace-text true))
                       (= :symbol (stage1-reader-token-kind rendered))
                       (= value (symbol rendered)))
          (p15-s23-seed-readable-printer-fail!
           source-path :unreadable-name
           {:scalar-kind kind}))
        named-bytes)
      0))

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
    (let [[numerator denominator _ _]
          (p15-s23-seed-readable-ratio-components! source-path value)]
      (clojure.lang.Ratio. numerator denominator))
    value))