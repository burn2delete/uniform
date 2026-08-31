;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(defn- encode-value
  [value state depth {:keys [reject-metadata?] :as options}]
  (let [nodes (swap! state inc)]
    (when (> nodes maximum-canonical-nodes)
      (cache-fail! "C16-KEY" "canonical value exceeds the node bound"
                   {:maximum-nodes maximum-canonical-nodes}))
    (when (> depth maximum-canonical-depth)
      (cache-fail! "C16-KEY" "canonical value exceeds the depth bound"
                   {:maximum-depth maximum-canonical-depth}))
    (when (and reject-metadata? (metadata-bearing? value))
      (cache-fail! "C16-KEY"
                   "semantic cache keys cannot depend on host metadata"
                   {:value-class (.getName (class value))}))
    (if (and (not reject-metadata?) (metadata-bearing? value))
      [:meta
       (encode-value (meta value) state (inc depth) options)
       (encode-value (with-meta value nil) state (inc depth) options)]
      (cond
        (nil? value) [:nil]
        (true? value) [:boolean true]
        (false? value) [:boolean false]
        (string? value) [:string value]
        (char? value) [:character (int value)]
        (keyword? value) [:keyword (namespace value) (name value)]
        (symbol? value) [:symbol (namespace value) (name value)]

        (integral-tag value)
        [:integer (integral-tag value) (str value)]

        (ratio? value)
        [:ratio (str (numerator value)) (str (denominator value))]

        (instance? BigDecimal value)
        [:bigdecimal (.toString ^BigDecimal value)]

        (instance? Double value)
        (if (Double/isFinite ^Double value)
          [:double (str (Double/doubleToRawLongBits ^Double value))]
          (cache-fail! "C16-KEY" "nonfinite floating values are not canonical"
                       {:value-class "java.lang.Double"}))

        (instance? Float value)
        (if (Float/isFinite ^Float value)
          [:float (str (Float/floatToRawIntBits ^Float value))]
          (cache-fail! "C16-KEY" "nonfinite floating values are not canonical"
                       {:value-class "java.lang.Float"}))

        (instance? UUID value) [:uuid (str value)]

        (= Date (class value))
        [:date (.getTime ^Date value)]

        (= (class (byte-array 0)) (class value))
        [:bytes (.encodeToString (Base64/getEncoder) ^bytes value)]

        (record? value)
        (cache-fail! "C16-KEY" "records are not supported canonical values"
                     {:value-class (.getName (class value))})

        (map? value)
        (let [entries
              (mapv (fn [[key item]]
                      [(encode-value key state (inc depth) options)
                       (encode-value item state (inc depth) options)])
                    value)]
          [:map (sorted-encoded entries)])

        (set? value)
        [:set (sorted-encoded (encode-children value state depth options))]

        (vector? value)
        [:vector (encode-children value state depth options)]

        (seq? value)
        [:list (encode-children value state depth options)]

        :else
        (cache-fail! "C16-KEY" "unsupported value in canonical cache data"
                     {:value-class (.getName (class value))})))))

(defn- canonical-node
  [value options]
  (encode-value value (atom 0) 0 options))
