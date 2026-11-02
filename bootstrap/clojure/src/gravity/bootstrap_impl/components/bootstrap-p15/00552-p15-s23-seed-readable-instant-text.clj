

(defn p15-s23-seed-readable-instant-text
  [source-path value]
  (let [calendar
        (doto (java.util.GregorianCalendar.
               (java.util.TimeZone/getTimeZone "GMT")
               java.util.Locale/ROOT)
          (.setGregorianChange (java.util.Date. -12219292800000))
          (.setLenient false)
          (.setTime ^java.util.Date value))
        era (.get calendar java.util.Calendar/ERA)
        calendar-year (.get calendar java.util.Calendar/YEAR)
        year (cond
               (and (= era java.util.GregorianCalendar/BC)
                    (= calendar-year 1))
               0

               (= era java.util.GregorianCalendar/AD)
               calendar-year

               :else nil)]
    (when-not (and (some? year) (<= 0 year 9999))
      (p15-s23-seed-readable-printer-fail!
       source-path :instant-range-limit
       {:minimum-year 0 :maximum-year 9999}))
    (let [builder (doto (StringBuilder.) (.append "#inst \""))]
      (p15-s23-seed-readable-append-fixed-decimal! builder year 4)
      (.append builder \-)
      (p15-s23-seed-readable-append-fixed-decimal!
       builder (inc (.get calendar java.util.Calendar/MONTH)) 2)
      (.append builder \-)
      (p15-s23-seed-readable-append-fixed-decimal!
       builder (.get calendar java.util.Calendar/DAY_OF_MONTH) 2)
      (.append builder \T)
      (p15-s23-seed-readable-append-fixed-decimal!
       builder (.get calendar java.util.Calendar/HOUR_OF_DAY) 2)
      (.append builder \:)
      (p15-s23-seed-readable-append-fixed-decimal!
       builder (.get calendar java.util.Calendar/MINUTE) 2)
      (.append builder \:)
      (p15-s23-seed-readable-append-fixed-decimal!
       builder (.get calendar java.util.Calendar/SECOND) 2)
      (.append builder \.)
      (p15-s23-seed-readable-append-fixed-decimal!
       builder (.get calendar java.util.Calendar/MILLISECOND) 3)
      (.append builder "-00:00\"")
      (.toString builder))))

(defn p15-s23-seed-readable-append-long-hex-range!
  [^StringBuilder builder value high-shift low-shift]
  (let [digits "0123456789abcdef"]
    (doseq [shift (range high-shift (dec low-shift) -4)]
      (.append builder
               (.charAt digits
                        (int (bit-and 15
                                      (bit-shift-right (long value)
                                                       shift)))))))
  builder)

(defn p15-s23-seed-readable-uuid-text
  [value]
  (let [most (.getMostSignificantBits ^java.util.UUID value)
        least (.getLeastSignificantBits ^java.util.UUID value)
        builder (doto (StringBuilder.) (.append "#uuid \""))]
    (p15-s23-seed-readable-append-long-hex-range! builder most 60 32)
    (.append builder \-)
    (p15-s23-seed-readable-append-long-hex-range! builder most 28 16)
    (.append builder \-)
    (p15-s23-seed-readable-append-long-hex-range! builder most 12 0)
    (.append builder \-)
    (p15-s23-seed-readable-append-long-hex-range! builder least 60 48)
    (.append builder \-)
    (p15-s23-seed-readable-append-long-hex-range! builder least 44 0)
    (.append builder \" )
    (.toString builder)))

(defn p15-s23-seed-readable-string-text
  [^String value]
  (let [builder (StringBuilder.)]
    (.append builder \" )
    (loop [index 0]
      (when (< index (.length value))
        (let [codepoint (.codePointAt value index)]
          (case codepoint
            8 (.append builder "\\b")
            9 (.append builder "\\t")
            10 (.append builder "\\n")
            12 (.append builder "\\f")
            13 (.append builder "\\r")
            34 (.append builder "\\\"")
            92 (.append builder "\\\\")
            (if (or (< codepoint 32) (= codepoint 127))
              (do
                (.append builder "\\u")
                (p15-s23-seed-readable-append-hex4! builder codepoint))
              (.appendCodePoint builder codepoint)))
          (recur (+ index (Character/charCount codepoint))))))
    (.append builder \" )
    (.toString builder)))

(defn p15-s23-seed-readable-character-text
  [value]
  (case (int (char value))
    8 "\\backspace"
    9 "\\tab"
    10 "\\newline"
    12 "\\formfeed"
    13 "\\return"
    32 "\\space"
    (let [unit (char value)]
      (if (or (Character/isWhitespace unit)
              (Character/isISOControl unit)
              (contains? #{\, \; \" \' \` \~ \^ \@ \\ \#} unit))
        (let [builder (doto (StringBuilder.) (.append "\\u"))]
          (p15-s23-seed-readable-append-hex4! builder (int unit))
          (.toString builder))
        (str "\\" unit)))))

(defn p15-s23-seed-readable-compare-utf8
  [^String left ^String right]
  (let [left-bytes (.getBytes left java.nio.charset.StandardCharsets/UTF_8)
        right-bytes (.getBytes right java.nio.charset.StandardCharsets/UTF_8)
        common (min (alength left-bytes) (alength right-bytes))]
    (loop [index 0]
      (if (= index common)
        (compare (alength left-bytes) (alength right-bytes))
        (let [left-byte (bit-and 0xff (aget left-bytes index))
              right-byte (bit-and 0xff (aget right-bytes index))]
          (if (= left-byte right-byte)
            (recur (inc index))
            (compare left-byte right-byte)))))))

(declare p15-s23-seed-readable-value-text)

(defn p15-s23-seed-readable-sorted-distinct!
  [source-path collision-reason rendered]
  (let [ordered (vec (sort p15-s23-seed-readable-compare-utf8 rendered))]
    (when (some true? (map = ordered (rest ordered)))
      (p15-s23-seed-readable-printer-fail!
       source-path collision-reason {}))
    ordered))

(defn p15-s23-seed-readable-value-text
  [source-path value]
  (let [kind (p15-s23-seed-readable-value-kind source-path value)]
    (case kind
      :nil "nil"
      :boolean (if value "true" "false")
      :integer (p15-s23-seed-readable-integer-text value)
      :ratio
      (let [[numerator denominator _ _]
            (p15-s23-seed-readable-ratio-components! source-path value)]
        (str (p15-s23-seed-readable-integer-text numerator)
             "/"
             (p15-s23-seed-readable-integer-text denominator)))
      :floating (p15-s23-seed-readable-floating-text source-path value)
      :string (p15-s23-seed-readable-string-text value)
      :character (p15-s23-seed-readable-character-text value)
      :instant (p15-s23-seed-readable-instant-text source-path value)
      :uuid (p15-s23-seed-readable-uuid-text value)
      :keyword (p15-s23-seed-readable-named-scalar-text kind value)
      :symbol (p15-s23-seed-readable-named-scalar-text kind value)
      :vector
      (str "[" (str/join " " (map #(p15-s23-seed-readable-value-text
                                      source-path %)
                                   value)) "]")
      :list
      (str "(" (str/join " " (map #(p15-s23-seed-readable-value-text
                                      source-path %)
                                   value)) ")")
      :set
      (let [rendered
            (mapv #(p15-s23-seed-readable-value-text source-path %) value)]
        (str "#{"
             (str/join
              " "
              (p15-s23-seed-readable-sorted-distinct!
               source-path :set-render-collision rendered))
             "}"))
      :map
      (let [rendered
            (reduce-kv
             (fn [entries key item]
               (conj entries
                     [(p15-s23-seed-readable-value-text source-path key)
                      (p15-s23-seed-readable-value-text source-path item)]))
             [] value)
            ordered
            (vec
             (sort
              (fn [[left-key left-value] [right-key right-value]]
                (let [key-order
                      (p15-s23-seed-readable-compare-utf8
                       left-key right-key)]
                  (if (zero? key-order)
                    (p15-s23-seed-readable-compare-utf8
                     left-value right-value)
                    key-order)))
              rendered))]
        (when (some true? (map #(= (first %1) (first %2))
                               ordered (rest ordered)))
          (p15-s23-seed-readable-printer-fail!
           source-path :map-key-render-collision {}))
        (str "{"
             (str/join ", " (map (fn [[key-text value-text]]
                                    (str key-text " " value-text))
                                  ordered))
             "}")))))