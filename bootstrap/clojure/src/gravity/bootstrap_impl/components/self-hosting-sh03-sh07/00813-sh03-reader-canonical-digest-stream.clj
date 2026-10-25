(defn- sh03-reader-canonical-stream-stats []
  (object-array [0 0 0 0 0 0]))

;; Reader-only encoding of the existing canonical EDN v1, not a new format.
;; V validates in original entry order before any digest output. E streams in
;; canonical order. Each complete pass has the original capacity and statistics;
;; a second finite pass grants no input capacity and no object-alias discount.
;; Compound keys and sets retain the old bounded normalizer as a fallback.
(defn- sh03-reader-canonical-stream-header!
  [source-path stats depth value classes class-reason]
  (sh03-reader-canonical-visit! source-path stats depth value)
  (when-not (contains? classes (.getName (class value)))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path class-reason {:class (.getName (class value))}))
  (when (> (count value) p15-s23-c6c10-max-container-width)
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :maximum-container-width {:observed-width (count value)}))
  (aset ^objects stats 2 (#'clojure.core/max (aget ^objects stats 2) (count value))))

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

(defn- sh03-reader-canonical-stream-validate-value! [source-path stats depth value]
  (cond
    ;; Original 00703 rejects records before its map branch.
    (record? value)
    (do (sh03-reader-canonical-fallback-form! source-path stats depth value) nil)

    (vector? value)
    (do
      (sh03-reader-canonical-stream-header!
       source-path stats depth value p15-s23-c6c10-canonical-vector-classes
       :exact-persistent-vector-class)
      (doseq [child value]
        (sh03-reader-canonical-stream-validate-value! source-path stats (inc depth) child)))

    (map? value)
    (do
      (sh03-reader-canonical-stream-header!
       source-path stats depth value p15-s23-c6c10-canonical-map-classes
       :exact-persistent-map-class)
      (let [sort-keys
            (mapv (fn [[key child]]
                    (let [encoded-key (sh03-reader-canonical-key-form!
                                       source-path stats (inc depth) key)
                          key-sort (sh03-reader-canonical-stream-sort-key encoded-key)]
                      ;; Preserve key -> sort key -> child, including failures.
                      (sh03-reader-canonical-stream-validate-value!
                       source-path stats (inc depth) child)
                      key-sort))
                  value)]
        ;; All original-order children precede the original duplicate check.
        (sh03-reader-canonical-stream-unique-keys! source-path sort-keys)))

    (list? value)
    (do
      (sh03-reader-canonical-stream-header!
       source-path stats depth value p15-s23-c6c10-canonical-list-classes
       :exact-persistent-list-class)
      (doseq [child value]
        (sh03-reader-canonical-stream-validate-value! source-path stats (inc depth) child)))

    :else
    ;; Common scalar values validate without allocating discarded forms.
    ;; Sets, uncommon scalars and unsupported domains retain original fallback.
    (sh03-reader-canonical-validate-scalar! source-path stats depth value)))

(defn- sh03-reader-canonical-stream-validation! [source-path value]
  (let [stats (sh03-reader-canonical-stream-stats)]
    (sh03-reader-canonical-stream-validate-value! source-path stats 0 value)
    (sh03-reader-canonical-stats-map stats)))

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
  [source-path stats depth values ^java.io.Writer writer ^String tag]
  (.write writer tag)
  (loop [remaining (seq values) first? true]
    (when remaining
      (when-not first? (.write writer " "))
      (sh03-reader-canonical-stream-emit-value!
       source-path stats (inc depth) (first remaining) writer)
      (recur (next remaining) false)))
  (.write writer "]]"))

(defn- sh03-reader-canonical-stream-scalar!
  [source-path stats depth value ^java.io.Writer writer]
  (sh03-reader-canonical-validate-scalar! source-path stats depth value)
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

(defn- sh03-reader-canonical-stream-emit-value! [source-path stats depth value ^java.io.Writer writer]
  (cond
    (record? value)
    (pr (sh03-reader-canonical-fallback-form! source-path stats depth value))

    (vector? value)
    (do
      (sh03-reader-canonical-stream-header!
       source-path stats depth value p15-s23-c6c10-canonical-vector-classes
       :exact-persistent-vector-class)
      (sh03-reader-canonical-stream-sequence! source-path stats depth value writer "[:vector ["))

    (map? value)
    (do
      (sh03-reader-canonical-stream-header!
       source-path stats depth value p15-s23-c6c10-canonical-map-classes
       :exact-persistent-map-class)
      (let [entries
            (mapv (fn [[key child]]
                    (let [encoded-key (sh03-reader-canonical-key-form!
                                       source-path stats (inc depth) key)]
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
              (sh03-reader-canonical-stream-emit-value! source-path stats (inc depth) child writer)
              (.write writer "]"))
            (recur (next remaining) false)))
        (.write writer "]]")))

    (list? value)
    (do
      (sh03-reader-canonical-stream-header!
       source-path stats depth value p15-s23-c6c10-canonical-list-classes
       :exact-persistent-list-class)
      (sh03-reader-canonical-stream-sequence! source-path stats depth value writer "[:list ["))

    (sh03-reader-canonical-stream-scalar-value? value)
    (sh03-reader-canonical-stream-scalar! source-path stats depth value writer)

    :else (pr (sh03-reader-canonical-fallback-form! source-path stats depth value))))

(defn- sh03-reader-canonical-stream-write!
  [source-path value validated-stats ^java.io.Writer writer]
  (let [stats (sh03-reader-canonical-stream-stats)]
    (binding [*out* writer *print-length* nil *print-level* nil *print-meta* false
              *print-dup* false *print-readably* true *print-namespace-maps* false]
      (.write writer "[:gravity/canonical-edn-v1 ")
      (sh03-reader-canonical-stream-emit-value! source-path stats 0 value writer)
      (.write writer "]"))
    (when-not (= validated-stats (sh03-reader-canonical-stats-map stats))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :sh03-canonical-stream-statistics-coherence
       {:validation validated-stats :emission (sh03-reader-canonical-stats-map stats)}))
    (sh03-reader-canonical-stats-map stats)))

(defn- sh03-reader-canonical-stream-digest [source-path value]
  ;; Validation completes before opening the private hashing stream. No
  ;; normalized map values or whole sequence of normalized children survives.
  (let [validated-stats (sh03-reader-canonical-stream-validation! source-path value)]
    (str "sha256:"
         (digest/sha256-output-hex
          (fn [^java.io.OutputStream output]
            (let [writer (java.io.BufferedWriter.
                          (java.io.OutputStreamWriter.
                           output java.nio.charset.StandardCharsets/UTF_8)
                          8192)]
              (try
                (sh03-reader-canonical-stream-write! source-path value validated-stats writer)
                (catch Throwable primary
                  (try (.close writer)
                       (catch Throwable secondary
                         (when-not (identical? primary secondary)
                           (try (.addSuppressed primary secondary) (catch Throwable _ nil)))))
                  (throw primary)))
              (.close writer)))))))
