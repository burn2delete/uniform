

(defn p15-s23-c6c10-carrier-validation
  [source-path value]
  (let [stats (atom {:nodes 0 :maximum-depth 0 :maximum-width 0
                    :scalar-bytes 0 :maximum-scalar-bytes 0
                    :maximum-integer-bits 0})
        active (java.util.IdentityHashMap.)]
    (loop [stack [[:visit value 0]]]
      (when (seq stack)
        (let [[action item depth] (peek stack) stack (pop stack)]
          (if (= :leave action)
            (do (.remove active item) (recur stack))
            (let [{:keys [kind children]}
                  (try (p15-s23-c6c10-canonical-carrier source-path item)
                       (catch clojure.lang.ExceptionInfo error
                         (let [data (ex-data error)
                               missing (:missing-fact data)
                               input-missing
                               (case missing
                                 :metadata-free-canonical-value :metadata-free-input-carrier
                                 :record-free-canonical-value :record-free-input-carrier
                                 (:exact-persistent-vector-class :exact-persistent-map-class
                                  :exact-persistent-set-class :exact-persistent-list-class)
                                 :exact-persistent-carrier-class
                                 :canonical-value-domain :input-carrier-value-domain
                                 :cyclic-canonical-carrier :cyclic-input-carrier
                                 missing)]
                           (p15-s23-c6c10-host-fail!
                            "C6-VERIFY" source-path input-missing (:subject data)))))]
              (swap! stats update :nodes inc)
              (swap! stats update :maximum-depth max depth)
              (if children
                (do
                  (when (.containsKey active item)
                    (p15-s23-c6c10-host-fail!
                     "C6-VERIFY" source-path :cyclic-input-carrier {}))
                  (.put active item true)
                  (swap! stats update :maximum-width max
                         (if (= :map kind) (quot (count children) 2) (count children)))
                  (recur (into (conj stack [:leave item depth])
                               (map #(vector :visit % (inc depth)) (rseq children)))))
                (do
                  (cond
                    ;; Authenticated C2 decimal literals retain IEEE-754 values;
                    ;; the strict canonical encoder continues to reject them.
                    (instance? Double item) nil
                    (instance? Float item)
                    (p15-s23-c6c10-host-fail!
                     "C6-VERIFY" source-path :input-carrier-value-domain
                     {:class (.getName (class item))})
                    (or (keyword? item) (symbol? item))
                    (do (when-let [namespace (namespace item)]
                          (p15-s23-c6c10-bounded-string-bytes! source-path stats namespace))
                        (if-let [name (name item)]
                          (p15-s23-c6c10-bounded-string-bytes! source-path stats name)
                          (p15-s23-c6c10-host-fail!
                           "C6-VERIFY" source-path :input-carrier-value-domain {})))
                    :else (p15-s23-c6c10-canonical-scalar-form source-path stats item))
                  (recur stack))))))))
    {:status :passed
     :observed-carrier-nodes (:nodes @stats)
     :observed-carrier-depth (:maximum-depth @stats)
     :observed-container-width (:maximum-width @stats)
     :observed-scalar-bytes (:maximum-scalar-bytes @stats)
     :observed-integer-bits (:maximum-integer-bits @stats)}))

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