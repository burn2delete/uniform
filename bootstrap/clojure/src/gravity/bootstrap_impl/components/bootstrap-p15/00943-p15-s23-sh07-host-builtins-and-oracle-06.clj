
(defn- p15-s23-sh07-o-integer-text
  [value]
  (let [integer (biginteger value)
        negative? (neg? (.signum integer))
        magnitude (.abs integer)
        ten (java.math.BigInteger/valueOf 10)
        writer (p15-s23-sh07-o-scalar-writer)
        initial-count (if negative? 1 0)]
    (if (zero? (.signum magnitude))
      (do (p15-s23-sh07-o-append-scalar-fragment! writer "0")
          (p15-s23-sh07-o-finish-scalar writer))
      (loop [remaining magnitude digits () digit-count initial-count]
        (if (zero? (.signum remaining))
          (do
            (when negative?
              (p15-s23-sh07-o-append-scalar-fragment! writer "-"))
            (doseq [digit digits]
              (p15-s23-sh07-o-append-scalar-fragment!
               writer (str digit)))
            (p15-s23-sh07-o-finish-scalar writer))
          (let [admission
                (p15-s23-sh07-observe-utf8-byte-count
                 "0" digit-count 32768)
                _ (when-not (= :accepted (:status admission))
                    (throw
                     (ex-info "integer exceeded prospective scalar boundary"
                              {:reason :scalar-byte-limit})))
                digit (.intValue (.remainder remaining ten))]
            (recur (.divide remaining ten)
                   (conj digits (.charAt "0123456789" digit))
                   (:observed-after admission))))))))

(defn- p15-s23-sh07-o-append-hex4!
  [writer code]
  (doseq [shift [12 8 4 0]]
    (p15-s23-sh07-o-append-scalar-fragment!
     writer
     (str (.charAt "0123456789abcdef"
                   (bit-and 15 (bit-shift-right code shift))))))
  writer)

(defn- p15-s23-sh07-o-string-text
  [^String value]
  (let [writer (p15-s23-sh07-o-scalar-writer)]
    (p15-s23-sh07-o-append-scalar-fragment! writer "\"")
    (loop [index 0]
      (when (< index (.length value))
        (let [[codepoint next-index]
              (p15-s23-sh07-next-scalar value index)]
          (case codepoint
            8 (p15-s23-sh07-o-append-scalar-fragment! writer "\\b")
            9 (p15-s23-sh07-o-append-scalar-fragment! writer "\\t")
            10 (p15-s23-sh07-o-append-scalar-fragment! writer "\\n")
            12 (p15-s23-sh07-o-append-scalar-fragment! writer "\\f")
            13 (p15-s23-sh07-o-append-scalar-fragment! writer "\\r")
            34 (p15-s23-sh07-o-append-scalar-fragment! writer "\\\"")
            92 (p15-s23-sh07-o-append-scalar-fragment! writer "\\\\")
            (if (or (< codepoint 32) (= codepoint 127))
              (do (p15-s23-sh07-o-append-scalar-fragment! writer "\\u")
                  (p15-s23-sh07-o-append-hex4! writer codepoint))
              (p15-s23-sh07-o-append-scalar-fragment!
               writer (String. (Character/toChars codepoint)))))
          (recur next-index))))
    (p15-s23-sh07-o-append-scalar-fragment! writer "\"")
    (p15-s23-sh07-o-finish-scalar writer)))

(defn- p15-s23-sh07-o-character-text
  [value]
  (let [unit (char value)
        named (case (int unit)
                8 "\\backspace" 9 "\\tab" 10 "\\newline"
                12 "\\formfeed" 13 "\\return" 32 "\\space" nil)
        writer (p15-s23-sh07-o-scalar-writer)]
    (if named
      (p15-s23-sh07-o-append-scalar-fragment! writer named)
      (if (or (Character/isWhitespace unit)
              (Character/isISOControl unit)
              (contains? #{\, \; \" \' \` \~ \^ \@ \\ \#} unit))
        (do (p15-s23-sh07-o-append-scalar-fragment! writer "\\u")
            (p15-s23-sh07-o-append-hex4! writer (int unit)))
        (do (p15-s23-sh07-o-append-scalar-fragment! writer "\\")
            (p15-s23-sh07-o-append-scalar-fragment! writer (str unit)))))
    (p15-s23-sh07-o-finish-scalar writer)))

(defn- p15-s23-sh07-o-append-fixed-decimal!
  [writer value width]
  (let [text (Long/toString (long value))]
    (dotimes [_ (- width (.length text))]
      (p15-s23-sh07-o-append-scalar-fragment! writer "0"))
    (p15-s23-sh07-o-append-scalar-fragment! writer text))
  writer)

(defn- p15-s23-sh07-o-instant-text
  [value]
  (let [calendar
        (doto (java.util.GregorianCalendar.
               (java.util.TimeZone/getTimeZone "GMT") java.util.Locale/ROOT)
          (.setGregorianChange (java.util.Date. -12219292800000))
          (.setLenient false)
          (.setTime ^java.util.Date value))
        era (.get calendar java.util.Calendar/ERA)
        calendar-year (.get calendar java.util.Calendar/YEAR)
        year (cond
               (and (= era java.util.GregorianCalendar/BC)
                    (= calendar-year 1)) 0
               (= era java.util.GregorianCalendar/AD) calendar-year
               :else nil)]
    (when-not (and year (<= 0 year 9999))
      (throw (ex-info "source oracle instant range"
                      {:reason :unsupported-value-carrier})))
    (let [writer (p15-s23-sh07-o-scalar-writer)]
      (p15-s23-sh07-o-append-scalar-fragment! writer "#inst \"")
      (p15-s23-sh07-o-append-fixed-decimal! writer year 4)
      (p15-s23-sh07-o-append-scalar-fragment! writer "-")
      (p15-s23-sh07-o-append-fixed-decimal!
       writer (inc (.get calendar java.util.Calendar/MONTH)) 2)
      (p15-s23-sh07-o-append-scalar-fragment! writer "-")
      (p15-s23-sh07-o-append-fixed-decimal!
       writer (.get calendar java.util.Calendar/DAY_OF_MONTH) 2)
      (p15-s23-sh07-o-append-scalar-fragment! writer "T")
      (p15-s23-sh07-o-append-fixed-decimal!
       writer (.get calendar java.util.Calendar/HOUR_OF_DAY) 2)
      (p15-s23-sh07-o-append-scalar-fragment! writer ":")
      (p15-s23-sh07-o-append-fixed-decimal!
       writer (.get calendar java.util.Calendar/MINUTE) 2)
      (p15-s23-sh07-o-append-scalar-fragment! writer ":")
      (p15-s23-sh07-o-append-fixed-decimal!
       writer (.get calendar java.util.Calendar/SECOND) 2)
      (p15-s23-sh07-o-append-scalar-fragment! writer ".")
      (p15-s23-sh07-o-append-fixed-decimal!
       writer (.get calendar java.util.Calendar/MILLISECOND) 3)
      (p15-s23-sh07-o-append-scalar-fragment! writer "-00:00\"")
      (p15-s23-sh07-o-finish-scalar writer))))

(defn- p15-s23-sh07-o-uuid-text
  [value]
  (let [digits "0123456789abcdef"
        append-range!
        (fn [writer bits high low]
          (doseq [shift (range high (dec low) -4)]
            (p15-s23-sh07-o-append-scalar-fragment!
             writer
             (str (.charAt digits
                           (int (bit-and 15
                                         (bit-shift-right (long bits)
                                                          shift)))))))
          writer)
        most (.getMostSignificantBits ^java.util.UUID value)
        least (.getLeastSignificantBits ^java.util.UUID value)
        writer (p15-s23-sh07-o-scalar-writer)]
    (p15-s23-sh07-o-append-scalar-fragment! writer "#uuid \"")
    (append-range! writer most 60 32)
    (p15-s23-sh07-o-append-scalar-fragment! writer "-")
    (append-range! writer most 28 16)
    (p15-s23-sh07-o-append-scalar-fragment! writer "-")
    (append-range! writer most 12 0)
    (p15-s23-sh07-o-append-scalar-fragment! writer "-")
    (append-range! writer least 60 48)
    (p15-s23-sh07-o-append-scalar-fragment! writer "-")
    (append-range! writer least 44 0)
    (p15-s23-sh07-o-append-scalar-fragment! writer "\"")
    (p15-s23-sh07-o-finish-scalar writer)))

(defn- p15-s23-sh07-o-scalar-text
  [value]
  (case (p15-s23-sh07-o-scalar-kind value)
    :nil (let [writer (p15-s23-sh07-o-scalar-writer)]
           (p15-s23-sh07-o-append-scalar-fragment! writer "nil")
           (p15-s23-sh07-o-finish-scalar writer))
    :boolean (let [writer (p15-s23-sh07-o-scalar-writer)]
               (p15-s23-sh07-o-append-scalar-fragment!
                writer (if value "true" "false"))
               (p15-s23-sh07-o-finish-scalar writer))
    :integer (p15-s23-sh07-o-integer-text value)
    :ratio (let [writer (p15-s23-sh07-o-scalar-writer)]
             (p15-s23-sh07-o-append-scalar-fragment!
              writer (:text (p15-s23-sh07-o-integer-text
                             (.numerator ^clojure.lang.Ratio value))))
             (p15-s23-sh07-o-append-scalar-fragment! writer "/")
             (p15-s23-sh07-o-append-scalar-fragment!
              writer (:text (p15-s23-sh07-o-integer-text
                             (.denominator ^clojure.lang.Ratio value))))
             (p15-s23-sh07-o-finish-scalar writer))
    :floating (let [number (double value)
                    writer (p15-s23-sh07-o-scalar-writer)]
                (when-not (Double/isFinite number)
                  (throw (ex-info "source oracle floating range"
                                  {:reason :unsupported-value-carrier})))
                (p15-s23-sh07-o-append-scalar-fragment!
                 writer (Double/toString number))
                (p15-s23-sh07-o-finish-scalar writer))
    :string (p15-s23-sh07-o-string-text value)
    :character (p15-s23-sh07-o-character-text value)
    :instant (p15-s23-sh07-o-instant-text value)
    :uuid (p15-s23-sh07-o-uuid-text value)
    :keyword (let [writer (p15-s23-sh07-o-scalar-writer)]
               (p15-s23-sh07-o-append-scalar-fragment! writer ":")
               (when-let [ns-text (namespace value)]
                 (p15-s23-sh07-o-append-scalar-fragment! writer ns-text)
                 (p15-s23-sh07-o-append-scalar-fragment! writer "/"))
               (p15-s23-sh07-o-append-scalar-fragment!
                writer (name value))
               (p15-s23-sh07-o-finish-scalar writer))
    :symbol (let [writer (p15-s23-sh07-o-scalar-writer)]
              (when-let [ns-text (namespace value)]
                (p15-s23-sh07-o-append-scalar-fragment! writer ns-text)
                (p15-s23-sh07-o-append-scalar-fragment! writer "/"))
              (p15-s23-sh07-o-append-scalar-fragment! writer (name value))
              (p15-s23-sh07-o-finish-scalar writer))
    (throw (ex-info "source oracle scalar is unsupported"
                    {:reason :unsupported-value-carrier}))))

;; This Clojure closure is the Attempt-17 independent source oracle only.
;; Producer and Root 6 carry separately authored Gravity implementations; no
;; generic Stage0/Stage2 collection compiler or sort primitive calls this
;; oracle.
(def p15-s23-sh07-canonical-decoration-limits
  {:maximum-nodes 4096
   :maximum-depth 96
   :maximum-collection-width 512
   :maximum-scalar-bytes 32768
   :maximum-output-bytes 262144})

(defn- p15-s23-sh07-decoration-failed [reason]
  {:status :failed :reason reason})

(defn- p15-s23-sh07-active-copy [^java.util.IdentityHashMap active]
  (doto (java.util.IdentityHashMap.) (.putAll active)))

(defn- p15-s23-sh07-insert-by
  [compare-record ordered record]
  (loop [remaining ordered prefix []]
    (if (empty? remaining)
      (conj prefix record)
      (if (neg? (compare-record record (first remaining)))
        (into (conj prefix record) remaining)
        (recur (rest remaining) (conj prefix (first remaining)))))))

(defn- p15-s23-sh07-stable-order-by
  [compare-record records]
  (reduce (partial p15-s23-sh07-insert-by compare-record) [] records))

(declare p15-s23-sh07-canonical-decoration)
