(defn- sh03-reader-canonical-stream-stats []
  (atom {:nodes 0 :maximum-depth 0 :maximum-width 0 :scalar-bytes 0
         :maximum-scalar-bytes 0 :maximum-integer-bits 0}))

;; Reader-only encoding of the existing canonical EDN v1, not a new format.
;; V validates in original entry order before any digest output. E streams in
;; canonical order. Each complete pass has the original capacity and statistics;
;; a second finite pass grants no input capacity and no object-alias discount.
;; Compound keys and sets retain the old bounded normalizer as a fallback.
(defn- sh03-reader-canonical-stream-header!
  [source-path stats depth value classes class-reason]
  (swap! stats update :nodes inc)
  (swap! stats update :maximum-depth max depth)
  (when (or (> (:nodes @stats) p15-s23-c6c10-max-carrier-nodes)
            (> depth p15-s23-c6c10-max-carrier-depth))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :bounded-canonical-carrier
     (select-keys @stats [:nodes :maximum-depth])))
  (when (and (instance? clojure.lang.IObj value) (some? (meta value)))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :metadata-free-canonical-value
     {:value-kind (.getName (class value))}))
  (when-not (contains? classes (.getName (class value)))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path class-reason {:class (.getName (class value))}))
  (when (> (count value) p15-s23-c6c10-max-container-width)
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :maximum-container-width {:observed-width (count value)}))
  (swap! stats update :maximum-width max (count value)))

(defn- sh03-reader-canonical-stream-unique-keys! [source-path sort-keys]
  (when-not (= (count sort-keys) (count (distinct sort-keys)))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :unique-canonical-map-keys {})))

(defn- sh03-reader-canonical-stream-validate-value! [source-path stats depth value]
  (cond
    ;; Original 00703 rejects records before its map branch.
    (record? value)
    (do (p15-s23-c6c10-canonical-form* source-path stats depth value) nil)

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
                    (let [encoded-key (p15-s23-c6c10-canonical-form*
                                       source-path stats (inc depth) key)
                          key-sort (p15-s23-c6c10-canonical-sort-key encoded-key)]
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
    @stats))

(declare ^:private sh03-reader-canonical-stream-emit-value!)

(defn- sh03-reader-canonical-stream-sequence!
  [source-path stats depth values ^java.io.Writer writer tag]
  (.write writer tag)
  (loop [remaining (seq values) first? true]
    (when remaining
      (when-not first? (.write writer " "))
      (sh03-reader-canonical-stream-emit-value!
       source-path stats (inc depth) (first remaining) writer)
      (recur (next remaining) false)))
  (.write writer "]]"))

(defn- sh03-reader-canonical-stream-emit-value! [source-path stats depth value ^java.io.Writer writer]
  (cond
    (record? value)
    (pr (p15-s23-c6c10-canonical-form* source-path stats depth value))

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
                    (let [encoded-key (p15-s23-c6c10-canonical-form*
                                       source-path stats (inc depth) key)]
                      [(p15-s23-c6c10-canonical-sort-key encoded-key) encoded-key child])) value)]
        (sh03-reader-canonical-stream-unique-keys! source-path (mapv first entries))
        (.write writer "[:map [")
        (loop [remaining (seq (sort-by first entries)) first? true]
          (when remaining
            (when-not first? (.write writer " "))
            (let [[_ encoded-key child] (first remaining)]
              (.write writer "[:entry ")
              (pr encoded-key)
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

    :else (pr (p15-s23-c6c10-canonical-form* source-path stats depth value))))

(defn- sh03-reader-canonical-stream-write!
  [source-path value validated-stats ^java.io.Writer writer]
  (let [stats (sh03-reader-canonical-stream-stats)]
    (binding [*out* writer *print-length* nil *print-level* nil *print-meta* false
              *print-dup* false *print-readably* true *print-namespace-maps* false]
      (.write writer "[:gravity/canonical-edn-v1 ")
      (sh03-reader-canonical-stream-emit-value! source-path stats 0 value writer)
      (.write writer "]"))
    (when-not (= validated-stats @stats)
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :sh03-canonical-stream-statistics-coherence
       {:validation validated-stats :emission @stats}))
    @stats))

(defn- sh03-reader-canonical-stream-digest [source-path value]
  ;; Validation completes before opening the private hashing stream. No
  ;; normalized map values or whole sequence of normalized children survives.
  (let [validated-stats (sh03-reader-canonical-stream-validation! source-path value)]
    (str "sha256:"
         (digest/sha256-output-hex
          (fn [output]
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
