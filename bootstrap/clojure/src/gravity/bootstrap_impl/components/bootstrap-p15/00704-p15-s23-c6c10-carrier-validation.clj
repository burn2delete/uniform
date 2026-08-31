

(defn p15-s23-c6c10-carrier-validation
  [source-path value]
  (let [stats (atom {:nodes 0 :maximum-depth 0 :maximum-width 0
                     :scalar-bytes 0 :maximum-scalar-bytes 0
                     :maximum-integer-bits 0})]
    (letfn [(visit [depth item]
              (swap! stats update :nodes inc)
              (swap! stats update :maximum-depth max depth)
              (when (or (> (:nodes @stats)
                           p15-s23-c6c10-max-carrier-nodes)
                        (> depth p15-s23-c6c10-max-carrier-depth))
                (p15-s23-c6c10-host-fail!
                 "C6-VERIFY" source-path :bounded-input-carrier @stats))
              (when (and (instance? clojure.lang.IObj item)
                         (some? (meta item)))
                (p15-s23-c6c10-host-fail!
                 "C6-VERIFY" source-path :metadata-free-input-carrier
                 {:class (.getName (class item))}))
              (cond
                (or (nil? item) (boolean? item)) nil
                (integer? item)
                (p15-s23-c6c10-bounded-integer! source-path stats item)
                (ratio? item)
                (do (p15-s23-c6c10-bounded-integer!
                     source-path stats (numerator item))
                    (p15-s23-c6c10-bounded-integer!
                     source-path stats (denominator item)))
                ;; Genuine C2 decimal literals currently retain the host
                ;; reader's IEEE-754 value for C6/C7 source-plan lockstep.
                ;; The strict digest encoder below still rejects raw doubles;
                ;; only authenticated literal positions receive a lossless
                ;; bit descriptor before hashing.
                (instance? Double item) nil
                (instance? java.math.BigDecimal item)
                (let [scale (long (.scale ^java.math.BigDecimal item))]
                  (p15-s23-c6c10-bounded-integer!
                   source-path stats (.unscaledValue ^java.math.BigDecimal item))
                    (when (> (Math/abs scale) 65536)
                      (p15-s23-c6c10-host-fail!
                       "C6-VERIFY" source-path :bounded-decimal-scale
                       {:scale scale})))
                (string? item)
                (p15-s23-c6c10-bounded-string-bytes! source-path stats item)
                (char? item)
                (when (<= 0xD800 (int item) 0xDFFF)
                  (p15-s23-c6c10-host-fail!
                   "C6-VERIFY" source-path :unicode-scalar-character
                   {:code (int item)}))
                (keyword? item)
                (do (when-let [namespace (namespace item)]
                      (p15-s23-c6c10-bounded-string-bytes!
                       source-path stats namespace))
                    (p15-s23-c6c10-bounded-string-bytes!
                     source-path stats (name item)))
                (symbol? item)
                (do (when-let [namespace (namespace item)]
                      (p15-s23-c6c10-bounded-string-bytes!
                       source-path stats namespace))
                    (p15-s23-c6c10-bounded-string-bytes!
                     source-path stats (name item)))
                (record? item)
                (p15-s23-c6c10-host-fail!
                 "C6-VERIFY" source-path :record-free-input-carrier
                 {:class (.getName (class item))})
                (or (vector? item) (map? item) (set? item) (list? item))
                (do
                  (when-not (p15-s23-c6c10-class-supported-carrier? item)
                    (p15-s23-c6c10-host-fail!
                     "C6-VERIFY" source-path :exact-persistent-carrier-class
                     {:class (.getName (class item))}))
                  (when (> (count item)
                           p15-s23-c6c10-max-container-width)
                    (p15-s23-c6c10-host-fail!
                     "C6-VERIFY" source-path :maximum-container-width
                     {:observed-width (count item)}))
                  (swap! stats update :maximum-width max (count item))
                  (if (map? item)
                    (doseq [[key child] item]
                      (visit (inc depth) key)
                      (visit (inc depth) child))
                    (doseq [child item]
                      (visit (inc depth) child))))
                :else
                (p15-s23-c6c10-host-fail!
                 "C6-VERIFY" source-path :input-carrier-value-domain
                 {:class (some-> item class .getName)})))]
      (visit 0 value)
      {:status :passed
       :maximum-carrier-nodes p15-s23-c6c10-max-carrier-nodes
       :maximum-carrier-depth p15-s23-c6c10-max-carrier-depth
       :maximum-container-width p15-s23-c6c10-max-container-width
       :maximum-scalar-bytes p15-s23-c6c10-max-scalar-bytes
       :maximum-integer-bits p15-s23-c6c10-max-integer-bits
       :observed-carrier-nodes (:nodes @stats)
       :observed-carrier-depth (:maximum-depth @stats)
       :observed-container-width (:maximum-width @stats)
       :observed-scalar-bytes (:maximum-scalar-bytes @stats)
       :observed-integer-bits (:maximum-integer-bits @stats)})))

(defn p15-s23-c6c10-path-neutral-value
  "Strip host IObj metadata without rewriting any semantic value.  Physical
  provenance is normalized only by schema-specific helpers below; key-name or
  global scalar rewriting would corrupt valid user maps and source strings."
  [source-content-hash value]
  (letfn [(neutral [item]
            (cond
              (record? item)
              (p15-s23-c6c10-host-fail!
               "C6-VERIFY" source-content-hash
               :record-free-private-input-projection
               {:class (.getName (class item))})

              (map? item)
              (into {}
                    (map (fn [[key child]]
                           [(neutral key) (neutral child)]))
                    item)

              (vector? item) (mapv neutral item)
              (set? item) (into #{} (map neutral) item)
              (list? item) (apply list (map neutral item))
              (and (instance? clojure.lang.IObj item) (some? (meta item)))
              (with-meta item nil)
              :else item))]
    (neutral value)))

(defn p15-s23-c6c10-literal-scalar-descriptor
  "Return a lossless canonical digest descriptor for a host numeric value
  that C2 can retain at a genuine literal position.  This does not broaden
  the strict canonical value domain: callers must authenticate the descriptor
  against fresh C2 literal-decoding records before substituting it."
  [value]
  (cond
    (instance? Double value)
    {:kind :gravity/ieee-754-binary64-literal
     :raw-bits (format "%016x"
                       (Double/doubleToRawLongBits ^Double value))}

    (instance? java.math.BigDecimal value)
    {:kind :gravity/arbitrary-decimal-literal
     :unscaled-value (.toString
                      (.unscaledValue ^java.math.BigDecimal value))
     :scale (.scale ^java.math.BigDecimal value)}

    (ratio? value)
    {:kind :gravity/exact-ratio-literal
     :numerator (.toString (biginteger (numerator value)))
     :denominator (.toString (biginteger (denominator value)))}

    :else nil))

(def p15-s23-c6c10-deferred-ratio-descriptor-keys
  #{:artifact :kind :raw :numerator-spelling :denominator-spelling
    :numerator :denominator :semantic-validation :reason})

(defn p15-s23-c6c10-deferred-ratio-descriptor?
  [value]
  (and (map? value)
       (= p15-s23-c6c10-deferred-ratio-descriptor-keys
          (set (keys value)))
       (= :gravity/deferred-ratio-literal (:artifact value))
       (= :ratio (:kind value))
       (string? (:raw value))
       (string? (:numerator-spelling value))
       (string? (:denominator-spelling value))
       (integer? (:numerator value))
       (integer? (:denominator value))
       (zero? (:denominator value))
       (= :deferred (:semantic-validation value))
       (= :zero-denominator (:reason value))))

(declare p15-s23-c6c10-base-numeric-occurrences
         p15-s23-c6c10-form-numeric-occurrence-index
         c3-c2-reader-integrity-report
         c3-syntax-stream-reader-products-authentic?
         c3-syntax-capability-proof
         c3-artifact-id)