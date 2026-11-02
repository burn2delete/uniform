(defn- sh03-reader-canonical-stream-header!
  [source-path value]
  (sh03-reader-canonical-metadata! source-path value)
  (when (.containsKey ^java.util.IdentityHashMap p15-s23-c6c10-canonical-ancestors value)
    (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :cyclic-canonical-carrier {}))
  (.put ^java.util.IdentityHashMap p15-s23-c6c10-canonical-ancestors value true))

(defn- sh03-reader-canonical-stream-leave! [value]
  (.remove ^java.util.IdentityHashMap p15-s23-c6c10-canonical-ancestors value))

;; Reader-only encoding of existing canonical EDN v1. V validates in original
;; entry order before output opens; E validates independently in canonical
;; order. Compound keys and sets retain the generic semantic normalizer.
;; Each pass owns fresh identity ancestors shared with that normalizer. Only
;; successfully entered containers are removed; aliases are visited anew.

(defn- ^String sh03-reader-canonical-named-text [^String value]
  ;; Only freshly validated Named components reach this function: no control,
  ;; whitespace or malformed surrogate. Escaped components use the old printer.
  (if (and (neg? (.indexOf value "\"")) (neg? (.indexOf value "\\")))
    (str "\"" value "\"")
    (binding [*print-dup* false *print-readably* true] (pr-str value))))

(defn- ^String sh03-reader-canonical-named-form-text [tag namespace name]
  (str tag (if (nil? namespace) "nil" (sh03-reader-canonical-named-text namespace))
       " " (sh03-reader-canonical-named-text name) "]"))

(defn- ^String sh03-reader-canonical-stream-hex [^bytes bytes]
  (let [^chars characters (char-array (* 2 (alength bytes)))
        ^String digits "0123456789abcdef"]
    (dotimes [index (alength bytes)]
      (let [value (bit-and (aget bytes index) 255) offset (* 2 index)]
        (aset characters (int offset) (.charAt digits (unsigned-bit-shift-right value 4)))
        (aset characters (int (inc offset)) (.charAt digits (bit-and value 15)))))
    (String. characters)))

(defn- ^String sh03-reader-canonical-stream-sort-key [form]
  (if (= :keyword (first form))
    (sh03-reader-canonical-stream-hex
     (.getBytes ^String
                (str "[:gravity/canonical-sort-v1 "
                     (sh03-reader-canonical-named-form-text "[:keyword " (get form 1) (get form 2))
                     "]") java.nio.charset.StandardCharsets/UTF_8))
    (p15-s23-c6c10-canonical-sort-key form)))

(defn- sh03-reader-canonical-stream-unique-keys! [source-path sort-keys]
  (when-not (= (count sort-keys) (count (distinct sort-keys)))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :unique-canonical-map-keys {})))

(defn- sh03-reader-canonical-stream-validate-value! [source-path value]
  (cond
    ;; Original 00703 rejects records before its map branch.
    (record? value)
    (do (sh03-reader-canonical-fallback-form! source-path value) nil)

    (vector? value)
    (do
      (sh03-reader-canonical-stream-header!
       source-path value)
      (try
        (doseq [child value]
          (sh03-reader-canonical-stream-validate-value! source-path child))
        (finally (sh03-reader-canonical-stream-leave! value))))

    (map? value)
    (do
      (sh03-reader-canonical-stream-header!
       source-path value)
      (try
       (let [sort-keys
            (mapv (fn [[key child]]
                    (let [encoded-key (sh03-reader-canonical-key-form!
                                       source-path key)
                          key-sort (sh03-reader-canonical-stream-sort-key encoded-key)]
                      ;; Preserve key -> sort key -> child, including failures.
                      (sh03-reader-canonical-stream-validate-value!
                       source-path child)
                      key-sort))
                  value)]
        ;; All original-order children precede the original duplicate check.
        (sh03-reader-canonical-stream-unique-keys! source-path sort-keys))
       (finally (sh03-reader-canonical-stream-leave! value))))

    (list? value)
    (do
      (sh03-reader-canonical-stream-header!
       source-path value)
      (try
        (doseq [child value]
          (sh03-reader-canonical-stream-validate-value! source-path child))
        (finally (sh03-reader-canonical-stream-leave! value))))

    :else
    ;; Common scalar values validate without allocating discarded forms.
    ;; Sets, uncommon scalars and unsupported domains retain original fallback.
    (sh03-reader-canonical-validate-scalar! source-path value)))

(defn- sh03-reader-canonical-stream-validation! [source-path value]
  (binding [p15-s23-c6c10-canonical-ancestors (java.util.IdentityHashMap.)]
    (sh03-reader-canonical-stream-validate-value! source-path value))
  {:status :passed})

(declare ^:private sh03-reader-canonical-stream-emit-value!)

(defn- sh03-reader-canonical-stream-quoted! [^java.io.Writer writer ^String text]
  ;; Same escape table as Clojure's String printer; emit ordinary runs in bulk.
  (.write writer "\"")
  (loop [index 0 start 0]
    (if (= index (.length text))
      (when (< start index) (.write writer text (int start) (int (- index start))))
      (if-let [escaped (clojure.core/char-escape-string (.charAt text (int index)))]
        (do
          (when (< start index) (.write writer text (int start) (int (- index start))))
          (.write writer ^String escaped)
          (recur (inc index) (inc index)))
        (recur (inc index) start))))
  (.write writer "\""))

(defn- sh03-reader-canonical-stream-sequence!
  [source-path values ^java.io.Writer writer ^String tag]
  (.write writer tag)
  (loop [remaining (seq values) first? true]
    (when remaining
      (when-not first? (.write writer " "))
      (sh03-reader-canonical-stream-emit-value!
       source-path (first remaining) writer)
      (recur (next remaining) false)))
  (.write writer "]]"))

(defn- sh03-reader-canonical-stream-scalar!
  [source-path value ^java.io.Writer writer]
  (sh03-reader-canonical-validate-scalar! source-path value)
  (cond
    (nil? value) (.write writer "[:nil]")
    (boolean? value) (.write writer ^String (if value "[:boolean true]" "[:boolean false]"))
    (integer? value)
    (do (.write writer "[:integer ")
        (sh03-reader-canonical-stream-quoted! writer (sh03-reader-canonical-integer-text value))
        (.write writer "]"))
    (string? value)
    (do (.write writer "[:string ") (sh03-reader-canonical-stream-quoted! writer value)
        (.write writer "]"))
    (char? value) (.write writer ^String (str "[:character " (int value) "]"))
    :else (.write writer (sh03-reader-canonical-named-form-text
                         (if (keyword? value) "[:keyword " "[:symbol ")
                         (namespace value) (name value)))))

(defn- sh03-reader-canonical-stream-emit-value! [source-path value ^java.io.Writer writer]
  (cond
    (record? value)
    (pr (sh03-reader-canonical-fallback-form! source-path value))

    (vector? value)
    (do
      (sh03-reader-canonical-stream-header!
       source-path value)
      (try (sh03-reader-canonical-stream-sequence! source-path value writer "[:vector [")
           (finally (sh03-reader-canonical-stream-leave! value))))

    (map? value)
    (do
      (sh03-reader-canonical-stream-header!
       source-path value)
      (try
       (let [entries
            (mapv (fn [[key child]]
                    (let [encoded-key (sh03-reader-canonical-key-form!
                                       source-path key)]
                      [(sh03-reader-canonical-stream-sort-key encoded-key) encoded-key child])) value)]
        (sh03-reader-canonical-stream-unique-keys! source-path (mapv first entries))
        (.write writer "[:map [")
        (loop [remaining (seq (sort-by first entries)) first? true]
          (when remaining
            (when-not first? (.write writer " "))
            (let [[_ encoded-key child] (first remaining)]
              (.write writer "[:entry ")
              (if (= :keyword (first encoded-key))
                (.write writer (sh03-reader-canonical-named-form-text
                                "[:keyword " (get encoded-key 1) (get encoded-key 2)))
                (pr encoded-key))
              (.write writer " ")
              (sh03-reader-canonical-stream-emit-value! source-path child writer)
              (.write writer "]"))
            (recur (next remaining) false)))
        (.write writer "]]"))
       (finally (sh03-reader-canonical-stream-leave! value))))

    (list? value)
    (do
      (sh03-reader-canonical-stream-header!
       source-path value)
      (try (sh03-reader-canonical-stream-sequence! source-path value writer "[:list [")
           (finally (sh03-reader-canonical-stream-leave! value))))

    (sh03-reader-canonical-stream-scalar-value? value)
    (sh03-reader-canonical-stream-scalar! source-path value writer)

    :else (pr (sh03-reader-canonical-fallback-form! source-path value))))

(defn- sh03-reader-canonical-stream-write!
  [source-path value ^java.io.Writer writer]
  (binding [p15-s23-c6c10-canonical-ancestors (java.util.IdentityHashMap.)
            *out* writer *print-length* nil *print-level* nil *print-meta* false
            *print-dup* false *print-readably* true *print-namespace-maps* false]
    (.write writer "[:gravity/canonical-edn-v1 ")
    (sh03-reader-canonical-stream-emit-value! source-path value writer)
    (.write writer "]"))
  {:status :passed})

(defn- sh03-reader-canonical-stream-digest [source-path value]
  ;; Validation completes before opening the private hashing stream. No
  ;; normalized map values or whole sequence of normalized children survives.
  (sh03-reader-canonical-stream-validation! source-path value)
  (str "sha256:"
         (digest/sha256-output-hex
          (fn [^java.io.OutputStream output]
            (let [writer (java.io.BufferedWriter.
                          (java.io.OutputStreamWriter.
                           output java.nio.charset.StandardCharsets/UTF_8)
                          8192)]
              (try
                (sh03-reader-canonical-stream-write! source-path value writer)
                (catch Throwable primary
                  (try (.close writer)
                       (catch Throwable secondary
                         (when-not (identical? primary secondary)
                           (try (.addSuppressed primary secondary) (catch Throwable _ nil)))))
                  (throw primary)))
              (.close writer))))))
