

(defn p15-s23-seed-readable-printer-fail!
  [source-path reason data]
  (let [failure
        (diagnostic
         "L2-BUILTIN-ERROR"
         "Clojure-seed readable printer rejected a value"
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
           "Keep pr-str inputs inside the supported Gravity value algebra until readable printing is implemented and authenticated in Gravity source."}
          data))]
    (when *p15-s23-seed-readable-owned-failures*
      (.put ^java.util.IdentityHashMap
            *p15-s23-seed-readable-owned-failures*
            failure Boolean/TRUE))
    (throw failure)))

(defn p15-s23-seed-readable-valid-utf8?
  [^String text]
  (loop [index 0]
    (if (>= index (.length text))
      true
      (let [unit (.charAt text index)
            high? (Character/isHighSurrogate unit)
            low? (Character/isLowSurrogate unit)]
        (if high?
          (if (and (< (inc index) (.length text))
                   (Character/isLowSurrogate (.charAt text (inc index))))
            (recur (+ index 2))
            false)
          (if low?
            false
            (recur (inc index))))))))

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

(defn p15-s23-seed-readable-validate-utf8!
  [source-path scalar-kind text]
  (when-not (p15-s23-seed-readable-valid-utf8? text)
    (p15-s23-seed-readable-printer-fail!
     source-path :invalid-unicode-scalar
     {:scalar-kind scalar-kind}))
  text)

(defn p15-s23-seed-readable-ratio-components!
  [source-path value]
  (let [numerator (.numerator ^clojure.lang.Ratio value)
        denominator (.denominator ^clojure.lang.Ratio value)]
    (when-not (and (identical? java.math.BigInteger (class numerator))
                   (identical? java.math.BigInteger (class denominator)))
      (p15-s23-seed-readable-printer-fail!
       source-path :invalid-ratio-component-carrier {}))
    (when-not (and (not (zero? (.signum ^java.math.BigInteger numerator)))
                   (pos? (.compareTo ^java.math.BigInteger denominator
                                     java.math.BigInteger/ONE))
                   (= java.math.BigInteger/ONE
                      (.gcd (.abs ^java.math.BigInteger numerator)
                            ^java.math.BigInteger denominator)))
      (p15-s23-seed-readable-printer-fail!
       source-path :noncanonical-ratio {}))
    [numerator denominator]))

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
