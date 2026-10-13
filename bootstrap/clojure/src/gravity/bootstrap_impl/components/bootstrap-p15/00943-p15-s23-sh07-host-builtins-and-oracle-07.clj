
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

(defn- p15-s23-sh07-combine-decorations
  [depth node-budget open separator close decorations]
  (let [node-count
        (inc (reduce + 0 (map :node-occurrences decorations)))
        maximum-depth
        (reduce max depth (map :maximum-depth decorations))]
    (if (> node-count node-budget)
      (p15-s23-sh07-decoration-failed :node-count-limit)
      (let [maximum-output-bytes
            (:maximum-output-bytes
             p15-s23-sh07-canonical-decoration-limits)
            builder (StringBuilder.)
            append!
            (fn [bytes text]
              (let [observation
                    (p15-s23-sh07-observe-utf8-byte-count
                     text bytes maximum-output-bytes)]
                (if (not= :accepted (:status observation))
                  nil
                  (do (.append builder ^String text)
                      (:observed-after observation)))))
            first-bytes (append! 0 open)]
        (if (nil? first-bytes)
          (p15-s23-sh07-decoration-failed :output-byte-limit)
          (loop [remaining decorations bytes first-bytes first? true]
            (if (empty? remaining)
              (if-let [complete-bytes (append! bytes close)]
                {:status :decorated
                 :text (.toString builder)
                 :node-occurrences node-count
                 :maximum-depth maximum-depth
                 :output-utf8-bytes complete-bytes}
                (p15-s23-sh07-decoration-failed :output-byte-limit))
              (let [with-separator
                    (if first? bytes (append! bytes separator))
                    with-text
                    (when with-separator
                      (append! with-separator (:text (first remaining))))]
                (if with-text
                  (recur (rest remaining) with-text false)
                  (p15-s23-sh07-decoration-failed
                   :output-byte-limit))))))))))

(def ^:private p15-s23-sh07-o-c2-carrier-keys
  #{:artifact :c2-reader-artifact :root-adapted-form-id
    :decimal-replay-evidence})

(defn- p15-s23-sh07-o-c2-carrier?
  [carrier]
  (and (map? carrier)
       (= p15-s23-sh07-o-c2-carrier-keys (set (keys carrier)))
       (= :gravity/sh07-oracle-c2-carrier-v1 (:artifact carrier))
       (map? (:decimal-replay-evidence carrier))))
