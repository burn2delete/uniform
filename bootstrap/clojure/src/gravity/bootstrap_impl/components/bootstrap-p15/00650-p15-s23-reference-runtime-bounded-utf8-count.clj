

(defn p15-s23-reference-runtime-bounded-utf8-count
  [^String value maximum-bytes]
  (let [length (.length value)]
    (if (> length maximum-bytes)
      {:status :over-limit :bytes (inc maximum-bytes)}
      (loop [index 0 bytes 0]
        (if (< index length)
          (let [code (int (.charAt value index))]
            (cond
              (<= 0xD800 code 0xDBFF)
              (if (and (< (inc index) length)
                       (<= 0xDC00
                           (int (.charAt value (inc index)))
                           0xDFFF))
                (let [next-bytes (+ bytes 4)]
                  (if (> next-bytes maximum-bytes)
                    {:status :over-limit :bytes next-bytes}
                    (recur (+ index 2) next-bytes)))
                {:status :invalid-surrogate :index index :bytes bytes})

              (<= 0xDC00 code 0xDFFF)
              {:status :invalid-surrogate :index index :bytes bytes}

              :else
              (let [width (cond
                            (<= code 0x7F) 1
                            (<= code 0x7FF) 2
                            :else 3)
                    next-bytes (+ bytes width)]
                (if (> next-bytes maximum-bytes)
                  {:status :over-limit :bytes next-bytes}
                  (recur (inc index) next-bytes)))))
          {:status :valid :bytes bytes})))))

(defn p15-s23-reference-runtime-bounded-width
  [value maximum-width]
  (if (counted? value)
    (let [width (count value)]
      {:status (if (<= width maximum-width) :valid :over-limit)
       :width width})
    (loop [values (seq value) width 0]
      (cond
        (nil? values) {:status :valid :width width}
        (>= width maximum-width)
        {:status :over-limit :width (inc maximum-width)}
        :else (recur (next values) (inc width))))))

(defn p15-s23-reference-runtime-hash
  [value]
  (str "sha256:"
       (sha256-hex
        (pr-str (c-backend-canonical-value value)))))

(defn p15-s23-reference-runtime-fail!
  [source-path target missing-fact value extra]
  (p15-s23-stage2-runtime-executor-fail!
   "P15S23X002" source-path value
   (merge {:target target
           :missing-fact missing-fact
           :boundary :pinned-reference-runtime-contract
           :result-committed? false
           :output-committed? false}
          extra)))

(defn p15-s23-reference-runtime-value-children
  [value]
  (cond
    (map? value)
    (reduce (fn [children [key item]]
              (conj children key item))
            [] value)
    (or (vector? value) (set? value) (seq? value))
    (reduce conj [] value)
    :else []))

(defn p15-s23-reference-runtime-scalar-byte-estimate!
  [source-path target definition-name value]
  (let [class-name (when (some? value) (.getName (class value)))
        supported-number?
        (and (number? value)
             (contains?
              p15-s23-reference-runtime-supported-number-class-names
              class-name))
        collection? (or (map? value) (vector? value) (set? value)
                        (seq? value))
        supported-collection?
        (and collection?
             (contains?
              p15-s23-reference-runtime-supported-collection-class-names
              class-name))
        reject!
        (fn [missing-fact scalar-kind extra]
          (p15-s23-reference-runtime-fail!
           source-path target missing-fact nil
           (merge {:runtime-contract-definition definition-name
                   :scalar-kind scalar-kind
                   :scalar-class class-name}
                  extra)))
        bounded-text-bytes
        (fn [^String text scalar-kind]
          (let [measurement
                (p15-s23-reference-runtime-bounded-utf8-count
                 text p15-s23-reference-runtime-max-scalar-bytes)]
            (when-not (= :valid (:status measurement))
              (reject! :runtime-contract-scalar-bounds scalar-kind
                       {:measurement measurement
                        :maximum-bytes
                        p15-s23-reference-runtime-max-scalar-bytes}))
            (:bytes measurement)))]
    (when (and (number? value) (not supported-number?))
      (reject! :runtime-contract-unsupported-scalar :number {}))
    (when (and collection? (not supported-collection?))
      (reject! :runtime-contract-unsupported-collection :collection {}))
    (when (and (some? value) (.isArray (class value)))
      (reject! :runtime-contract-unsupported-scalar :array {}))
    (cond
      (nil? value) 1
      (boolean? value) 1
      (char? value)
      (let [code (int value)]
        (cond
          (<= code 0x7f) 1
          (<= code 0x7ff) 2
          :else 3))
      (string? value) (bounded-text-bytes value :utf8-string)
      (keyword? value)
      (+ 1
         (reduce + 0
                 (map #(bounded-text-bytes % :keyword)
                      (remove nil? [(namespace value) (name value)]))))
      (symbol? value)
      (reduce + 0
              (map #(bounded-text-bytes % :symbol)
                   (remove nil? [(namespace value) (name value)])))
      (integer? value)
      (let [bits (.bitLength ^java.math.BigInteger (biginteger value))]
        (when (> bits p15-s23-reference-runtime-max-integer-bits)
          (reject! :runtime-contract-scalar-bounds :integer
                   {:observed-bits bits
                    :maximum-bits
                    p15-s23-reference-runtime-max-integer-bits}))
        (max 1 (quot (+ bits 7) 8)))
      (ratio? value)
      (reduce
       + 0
       (for [[part integer-value]
             [[:numerator (numerator value)]
              [:denominator (denominator value)]]]
         (let [bits (.bitLength ^java.math.BigInteger
                                (biginteger integer-value))]
           (when (> bits p15-s23-reference-runtime-max-integer-bits)
             (reject! :runtime-contract-scalar-bounds :ratio
                      {:ratio-part part
                       :observed-bits bits
                       :maximum-bits
                       p15-s23-reference-runtime-max-integer-bits}))
           (max 1 (quot (+ bits 7) 8)))))
      (instance? java.math.BigDecimal value)
      (let [decimal ^java.math.BigDecimal value
            bits (.bitLength (.unscaledValue decimal))
            scale (Math/abs (long (.scale decimal)))]
        (when (or (> bits p15-s23-reference-runtime-max-integer-bits)
                  (> scale p15-s23-reference-runtime-max-scalar-bytes))
          (reject! :runtime-contract-scalar-bounds :decimal
                   {:observed-bits bits
                    :observed-scale scale
                    :maximum-bits p15-s23-reference-runtime-max-integer-bits
                    :maximum-scale
                    p15-s23-reference-runtime-max-scalar-bytes}))
        (+ (max 1 (quot (+ bits 7) 8)) 4))
      (or (instance? java.lang.Double value)
          (instance? java.lang.Float value)) 8
      supported-collection? 0
      :else
      (reject! :runtime-contract-unsupported-scalar :object {}))))

(defn p15-s23-reference-runtime-bounded-value!
  ([source-path target definition-name value]
   (p15-s23-reference-runtime-bounded-value!
    source-path target definition-name value
    p15-s23-reference-runtime-max-contract-nodes
    p15-s23-reference-runtime-max-contract-depth))
  ([source-path target definition-name value maximum-nodes maximum-depth]
   (loop [pending [{:value value :depth 0}]
          visited 0
          total-scalar-bytes 0]
     (if-let [{:keys [value depth]} (peek pending)]
       (let [pending (pop pending)
             visited (inc visited)
             scalar-bytes
             (p15-s23-reference-runtime-scalar-byte-estimate!
              source-path target definition-name value)
             total-scalar-bytes (+ total-scalar-bytes scalar-bytes)]
         (when (or (> visited maximum-nodes)
                   (> depth maximum-depth))
           (p15-s23-reference-runtime-fail!
            source-path target :runtime-contract-value-bounds nil
            {:runtime-contract-definition definition-name
             :observed-nodes visited
             :observed-depth depth
             :maximum-nodes maximum-nodes
             :maximum-depth maximum-depth}))
         (when (> total-scalar-bytes
                  p15-s23-reference-runtime-max-total-scalar-bytes)
           (p15-s23-reference-runtime-fail!
            source-path target :runtime-contract-total-scalar-bounds nil
            {:runtime-contract-definition definition-name
             :observed-total-scalar-bytes total-scalar-bytes
             :maximum-total-scalar-bytes
             p15-s23-reference-runtime-max-total-scalar-bytes}))
         (when (or (map? value) (vector? value) (set? value) (seq? value))
           (let [measurement
                 (p15-s23-reference-runtime-bounded-width
                  value p15-s23-reference-runtime-max-collection-width)]
             (when-not (= :valid (:status measurement))
               (p15-s23-reference-runtime-fail!
                source-path target :runtime-contract-collection-width nil
                {:runtime-contract-definition definition-name
                 :measurement measurement
                 :maximum-width
                 p15-s23-reference-runtime-max-collection-width}))))
         (recur
          (into pending
                (map #(hash-map :value % :depth (inc depth))
                     (take (inc maximum-nodes)
                           (p15-s23-reference-runtime-value-children value))))
          visited
          total-scalar-bytes))
       {:node-count visited
        :total-scalar-bytes total-scalar-bytes
        :maximum-depth maximum-depth
        :maximum-nodes maximum-nodes
        :maximum-total-scalar-bytes
        p15-s23-reference-runtime-max-total-scalar-bytes}))))