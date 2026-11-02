

(defn p15-s23-seed-readable-printer-fail!
  [source-path reason data]
  (let [failure
        (diagnostic
         "L2-BUILTIN-ERROR"
         "bounded Clojure-seed readable printer rejected a value"
         (merge
          {:source-span {:source (or source-path "<unknown-source>")}
           :function 'pr-str
           :reason reason
           :printer-boundary :clojure-seed-compatibility
           :clojure-seed-boundary? true
           :self-hosted? false
           :result-committed? false
           :output-committed? false
           :remediation
           "Keep pr-str inputs inside the bounded Gravity value algebra until readable printing is implemented and authenticated in Gravity source."}
          data))]
    (when *p15-s23-seed-readable-owned-failures*
      (.put ^java.util.IdentityHashMap
            *p15-s23-seed-readable-owned-failures*
            failure Boolean/TRUE))
    (throw failure)))

(defn p15-s23-seed-readable-bounded-utf8-observation
  [^String text maximum-bytes]
  (loop [index 0
         bytes 0]
    (if (>= index (.length text))
      {:status :valid :bytes bytes}
      (let [unit (.charAt text index)
            high? (Character/isHighSurrogate unit)
            low? (Character/isLowSurrogate unit)]
        (cond
          (and high?
               (or (>= (inc index) (.length text))
                   (not (Character/isLowSurrogate
                         (.charAt text (inc index))))))
          {:status :invalid-unicode :bytes bytes}

          low?
          {:status :invalid-unicode :bytes bytes}

          :else
          (let [codepoint
                (if high?
                  (Character/toCodePoint unit (.charAt text (inc index)))
                  (int unit))
                width (cond
                        (<= codepoint 0x7f) 1
                        (<= codepoint 0x7ff) 2
                        (<= codepoint 0xffff) 3
                        :else 4)
                next-bytes (+ bytes width)]
            (if (> next-bytes maximum-bytes)
              {:status :byte-limit :bytes next-bytes}
              (recur (+ index (if high? 2 1)) next-bytes))))))))

(defn p15-s23-seed-readable-utf8-bytes
  [^String text]
  (alength (.getBytes text java.nio.charset.StandardCharsets/UTF_8)))

(defn p15-s23-seed-readable-name-safe?
  [^String text _symbol-name?]
  (not-any?
   (fn [unit]
     (or (Character/isWhitespace ^Character unit)
         (Character/isISOControl ^Character unit)
         (contains? #{\( \) \[ \] \{ \} \" \; \, \\ \' \` \~ \^ \@ \#}
                    unit)))
   text))

(defn p15-s23-seed-readable-named-scalar-text
  [kind value]
  (str (when (= :keyword kind) ":")
       (when-let [namespace-text (namespace value)]
         (str namespace-text "/"))
       (name value)))

(defn p15-s23-seed-readable-value-kind
  [source-path value]
  (let [value-class (when (some? value) (class value))
        scalar-kind
        (get p15-s23-seed-readable-printer-scalar-class-kinds value-class)
        collection-kind
        (get p15-s23-seed-readable-printer-collection-class-kinds
             value-class)]
    (cond
      (nil? value) :nil
      scalar-kind scalar-kind
      collection-kind collection-kind
      :else
      (p15-s23-seed-readable-printer-fail!
       source-path :unsupported-value-carrier
       {:observed-kind
        (cond
          (number? value) :unsupported-number
          (map? value) :unsupported-map
          (set? value) :unsupported-set
          (sequential? value) :unsupported-sequence
          (.isArray (class value)) :unsupported-array
          :else :unsupported-host-object)}))))

(defn p15-s23-seed-readable-bounded-text-bytes!
  [source-path scalar-kind text]
  (let [maximum-scalar-bytes
        (:maximum-scalar-bytes p15-s23-seed-readable-printer-limits)
        observation
        (p15-s23-seed-readable-bounded-utf8-observation
         text maximum-scalar-bytes)]
    (case (:status observation)
      :valid (:bytes observation)
      :invalid-unicode
      (p15-s23-seed-readable-printer-fail!
       source-path :invalid-unicode-scalar
       {:scalar-kind scalar-kind})
      :byte-limit
      (p15-s23-seed-readable-printer-fail!
       source-path :scalar-byte-limit
       {:scalar-kind scalar-kind
        :observed-scalar-bytes-at-least (:bytes observation)
        :maximum-scalar-bytes maximum-scalar-bytes}))))

(defn p15-s23-seed-readable-integer-bits
  [value]
  (.bitLength (.abs ^java.math.BigInteger (biginteger value))))

(defn p15-s23-seed-readable-integer-size!
  [source-path value component]
  (let [maximum-integer-bits
        (:maximum-integer-bits p15-s23-seed-readable-printer-limits)
        bits (p15-s23-seed-readable-integer-bits value)]
    (when (> bits maximum-integer-bits)
      (p15-s23-seed-readable-printer-fail!
       source-path :integer-magnitude-limit
       (cond-> {:observed-integer-bits bits
                :maximum-integer-bits maximum-integer-bits}
         component (assoc :integer-component component))))
    (+ 2 (quot (+ bits 2) 3))))

(defn p15-s23-seed-readable-ratio-components!
  [source-path value]
  (let [numerator (.numerator ^clojure.lang.Ratio value)
        denominator (.denominator ^clojure.lang.Ratio value)]
    (when-not (and (identical? java.math.BigInteger (class numerator))
                   (identical? java.math.BigInteger (class denominator)))
      (p15-s23-seed-readable-printer-fail!
       source-path :invalid-ratio-component-carrier {}))
    (let [numerator-size
          (p15-s23-seed-readable-integer-size!
           source-path numerator :numerator)
          denominator-size
          (p15-s23-seed-readable-integer-size!
           source-path denominator :denominator)]
    (when-not (and (not (zero? (.signum ^java.math.BigInteger numerator)))
                   (pos? (.compareTo ^java.math.BigInteger denominator
                                     java.math.BigInteger/ONE))
                   (= java.math.BigInteger/ONE
                      (.gcd (.abs ^java.math.BigInteger numerator)
                            ^java.math.BigInteger denominator)))
      (p15-s23-seed-readable-printer-fail!
       source-path :noncanonical-ratio {}))
      [numerator denominator numerator-size denominator-size])))

(defn p15-s23-seed-readable-floating-text
  [source-path value]
  (let [number (double value)]
    (when-not (Double/isFinite number)
      (p15-s23-seed-readable-printer-fail!
       source-path :non-finite-floating-point
       {:scalar-kind :floating}))
    (Double/toString number)))

(declare p15-s23-seed-readable-instant-text
         p15-s23-seed-readable-uuid-text)