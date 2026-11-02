(defn- p15-s23-c6c10-streaming-header!
  [source-path value]
  (p15-s23-c6c10-streaming-metadata! source-path value)
  (when (.containsKey ^java.util.IdentityHashMap p15-s23-c6c10-canonical-ancestors value)
    (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :cyclic-canonical-carrier {}))
  (.put ^java.util.IdentityHashMap p15-s23-c6c10-canonical-ancestors value true))

(defn- p15-s23-c6c10-streaming-leave! [value]
  (.remove ^java.util.IdentityHashMap p15-s23-c6c10-canonical-ancestors value))

;; Shared C6/Reader encoding of existing canonical EDN v1. V validates in original
;; entry order before output opens; E validates independently in canonical
;; order. Compound keys and sets retain the generic semantic normalizer.
;; Each pass owns fresh identity ancestors shared with that normalizer. Only
;; successfully entered containers are removed; aliases are visited anew.

(defn- p15-s23-c6c10-streaming-named-text ^String [^String value]
  ;; Only freshly validated Named components reach this function: no control,
  ;; whitespace or malformed surrogate. Escaped components use the old printer.
  (if (and (neg? (.indexOf value "\"")) (neg? (.indexOf value "\\")))
    (str "\"" value "\"")
    (binding [*print-dup* false *print-readably* true] (pr-str value))))

(defn- p15-s23-c6c10-streaming-named-form-text ^String [tag namespace name]
  (str tag (if (nil? namespace) "nil" (p15-s23-c6c10-streaming-named-text namespace))
       " " (p15-s23-c6c10-streaming-named-text name) "]"))

(defn- p15-s23-c6c10-streaming-hex ^String [^bytes bytes]
  (let [^chars characters (char-array (* 2 (alength bytes)))
        ^String digits "0123456789abcdef"]
    (dotimes [index (alength bytes)]
      (let [value (bit-and (aget bytes index) 255) offset (* 2 index)]
        (aset characters (int offset) (.charAt digits (unsigned-bit-shift-right value 4)))
        (aset characters (int (inc offset)) (.charAt digits (bit-and value 15)))))
    (String. characters)))

(defn- p15-s23-c6c10-streaming-sort-key ^String [form]
  (if (= :keyword (first form))
    (p15-s23-c6c10-streaming-hex
     (.getBytes ^String
                (str "[:gravity/canonical-sort-v1 "
                     (p15-s23-c6c10-streaming-named-form-text "[:keyword " (get form 1) (get form 2))
                     "]") java.nio.charset.StandardCharsets/UTF_8))
    (p15-s23-c6c10-canonical-sort-key form)))

(defn- p15-s23-c6c10-streaming-unique-keys! [source-path sort-keys]
  (when-not (= (count sort-keys) (count (distinct sort-keys)))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :unique-canonical-map-keys {})))

(defn- p15-s23-c6c10-streaming-validate-value! [source-path value]
  (cond
    ;; Original 00703 rejects records before its map branch.
    (record? value)
    (do (p15-s23-c6c10-streaming-fallback-form! source-path value) nil)

    (vector? value)
    (do
      (p15-s23-c6c10-streaming-header!
       source-path value)
      (try
        (doseq [child value]
          (p15-s23-c6c10-streaming-validate-value! source-path child))
        (finally (p15-s23-c6c10-streaming-leave! value))))

    (map? value)
    (do
      (p15-s23-c6c10-streaming-header!
       source-path value)
      (try
       (let [sort-keys
            (mapv (fn [[key child]]
                    (let [encoded-key (p15-s23-c6c10-streaming-key-form!
                                       source-path key)
                          key-sort (p15-s23-c6c10-streaming-sort-key encoded-key)]
                      ;; Preserve key -> sort key -> child, including failures.
                      (p15-s23-c6c10-streaming-validate-value!
                       source-path child)
                      key-sort))
                  value)]
        ;; All original-order children precede the original duplicate check.
        (p15-s23-c6c10-streaming-unique-keys! source-path sort-keys))
       (finally (p15-s23-c6c10-streaming-leave! value))))

    (list? value)
    (do
      (p15-s23-c6c10-streaming-header!
       source-path value)
      (try
        (doseq [child value]
          (p15-s23-c6c10-streaming-validate-value! source-path child))
        (finally (p15-s23-c6c10-streaming-leave! value))))

    :else
    ;; Common scalar values validate without allocating discarded forms.
    ;; Sets, uncommon scalars and unsupported domains retain original fallback.
    (p15-s23-c6c10-streaming-validate-scalar! source-path value)))

(defn- p15-s23-c6c10-streaming-validation! [source-path value]
  (binding [p15-s23-c6c10-canonical-ancestors (java.util.IdentityHashMap.)]
    (p15-s23-c6c10-streaming-validate-value! source-path value))
  {:status :passed})

(declare ^:private p15-s23-c6c10-streaming-emit-value!)

(defn- p15-s23-c6c10-streaming-quoted! [^java.io.Writer writer ^String text]
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

(defn- p15-s23-c6c10-streaming-sequence!
  [source-path values ^java.io.Writer writer ^String tag]
  (.write writer tag)
  (loop [remaining (seq values) first? true]
    (when remaining
      (when-not first? (.write writer " "))
      (p15-s23-c6c10-streaming-emit-value!
       source-path (first remaining) writer)
      (recur (next remaining) false)))
  (.write writer "]]"))

(defn- p15-s23-c6c10-streaming-scalar!
  [source-path value ^java.io.Writer writer]
  (p15-s23-c6c10-streaming-validate-scalar! source-path value)
  (cond
    (nil? value) (.write writer "[:nil]")
    (boolean? value) (.write writer ^String (if value "[:boolean true]" "[:boolean false]"))
    (integer? value)
    (do (.write writer "[:integer ")
        (p15-s23-c6c10-streaming-quoted! writer (p15-s23-c6c10-streaming-integer-text value))
        (.write writer "]"))
    (string? value)
    (do (.write writer "[:string ") (p15-s23-c6c10-streaming-quoted! writer value)
        (.write writer "]"))
    (char? value) (.write writer ^String (str "[:character " (int value) "]"))
    :else (.write writer (p15-s23-c6c10-streaming-named-form-text
                         (if (keyword? value) "[:keyword " "[:symbol ")
                         (namespace value) (name value)))))

(defn- p15-s23-c6c10-streaming-emit-value! [source-path value ^java.io.Writer writer]
  (cond
    (record? value)
    (pr (p15-s23-c6c10-streaming-fallback-form! source-path value))

    (vector? value)
    (do
      (p15-s23-c6c10-streaming-header!
       source-path value)
      (try (p15-s23-c6c10-streaming-sequence! source-path value writer "[:vector [")
           (finally (p15-s23-c6c10-streaming-leave! value))))

    (map? value)
    (do
      (p15-s23-c6c10-streaming-header!
       source-path value)
      (try
       (let [entries
            (mapv (fn [[key child]]
                    (let [encoded-key (p15-s23-c6c10-streaming-key-form!
                                       source-path key)]
                      [(p15-s23-c6c10-streaming-sort-key encoded-key) encoded-key child])) value)]
        (p15-s23-c6c10-streaming-unique-keys! source-path (mapv first entries))
        (.write writer "[:map [")
        (loop [remaining (seq (sort-by first entries)) first? true]
          (when remaining
            (when-not first? (.write writer " "))
            (let [[_ encoded-key child] (first remaining)]
              (.write writer "[:entry ")
              (if (= :keyword (first encoded-key))
                (.write writer (p15-s23-c6c10-streaming-named-form-text
                                "[:keyword " (get encoded-key 1) (get encoded-key 2)))
                (pr encoded-key))
              (.write writer " ")
              (p15-s23-c6c10-streaming-emit-value! source-path child writer)
              (.write writer "]"))
            (recur (next remaining) false)))
        (.write writer "]]"))
       (finally (p15-s23-c6c10-streaming-leave! value))))

    (list? value)
    (do
      (p15-s23-c6c10-streaming-header!
       source-path value)
      (try (p15-s23-c6c10-streaming-sequence! source-path value writer "[:list [")
           (finally (p15-s23-c6c10-streaming-leave! value))))

    (p15-s23-c6c10-streaming-scalar-value? value)
    (p15-s23-c6c10-streaming-scalar! source-path value writer)

    :else (pr (p15-s23-c6c10-streaming-fallback-form! source-path value))))

(defn- p15-s23-c6c10-streaming-write!
  [source-path value ^java.io.Writer writer]
  (binding [p15-s23-c6c10-canonical-ancestors (java.util.IdentityHashMap.)
            *out* writer *print-length* nil *print-level* nil *print-meta* false
            *print-dup* false *print-readably* true *print-namespace-maps* false]
    (.write writer "[:gravity/canonical-edn-v1 ")
    (p15-s23-c6c10-streaming-emit-value! source-path value writer)
    (.write writer "]"))
  {:status :passed})

(defn- p15-s23-c6c10-streaming-digest [source-path value]
  ;; Validation completes before opening the private hashing stream. No
  ;; normalized map values or whole sequence of normalized children survives.
  (p15-s23-c6c10-streaming-validation! source-path value)
  (str "sha256:"
         (digest/sha256-output-hex
          (fn [^java.io.OutputStream output]
            (let [writer (java.io.BufferedWriter.
                          (java.io.OutputStreamWriter.
                           output java.nio.charset.StandardCharsets/UTF_8)
                          8192)]
              (try
                (p15-s23-c6c10-streaming-write! source-path value writer)
                (catch Throwable primary
                  (try (.close writer)
                       (catch Throwable secondary
                         (when-not (identical? primary secondary)
                           (try (.addSuppressed primary secondary) (catch Throwable _ nil)))))
                  (throw primary)))
              (.close writer))))))
