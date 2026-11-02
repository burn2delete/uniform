

(defn p15-s23-closed-core-bounded-value!
  [source-path value]
  (loop [pending [{:value value :depth 0}]
         visited 0
         scalar-bytes 0]
    (if-let [{current :value depth :depth} (peek pending)]
      (let [pending (pop pending)
            visited (inc visited)
            scalar-byte-count
            (cond
              (nil? current) 0
              (boolean? current) (if current 4 5)
              (string? current)
              (let [observation
                    (p15-s23-closed-core-bounded-utf8-count
                     current
                     (max 0 (- p15-s23-closed-core-max-artifact-scalar-bytes
                               scalar-bytes)))]
                (when-not (= :valid (:status observation))
                  (p15-s23-closed-core-fail!
                   "C6-VERIFY" source-path
                   {:observed-string-length (.length ^String current)}
                   {:missing-fact :bounded-canonical-string-scalar
                    :encoding-status (:status observation)
                    :maximum-scalar-bytes
                    p15-s23-closed-core-max-artifact-scalar-bytes}))
                (:bytes observation))
              (char? current)
              (let [code (int current)]
                (cond
                  (<= 0xD800 code 0xDFFF)
                  (p15-s23-closed-core-fail!
                   "C6-VERIFY" source-path current
                   {:missing-fact :unicode-scalar-character})
                  (<= code 0x7F) 1
                  (<= code 0x7FF) 2
                  :else 3))
              (or (keyword? current) (symbol? current))
              (let [namespace-part (namespace current)
                    name-part (name current)
                    prefix-bytes (if (keyword? current) 1 0)
                    separator-bytes (if namespace-part 1 0)
                    remaining
                    (max 0 (- p15-s23-closed-core-max-artifact-scalar-bytes
                              scalar-bytes prefix-bytes separator-bytes))
                    namespace-observation
                    (when namespace-part
                      (p15-s23-closed-core-bounded-utf8-count
                       namespace-part remaining))
                    namespace-bytes (or (:bytes namespace-observation) 0)
                    name-observation
                    (p15-s23-closed-core-bounded-utf8-count
                     name-part (max 0 (- remaining namespace-bytes)))]
                (when-not (and (or (nil? namespace-observation)
                                   (= :valid (:status namespace-observation)))
                               (= :valid (:status name-observation)))
                  (p15-s23-closed-core-fail!
                   "C6-VERIFY" source-path
                   {:scalar-kind (if (keyword? current) :keyword :symbol)}
                   {:missing-fact :bounded-canonical-named-scalar
                    :maximum-scalar-bytes
                    p15-s23-closed-core-max-artifact-scalar-bytes}))
                (+ prefix-bytes separator-bytes namespace-bytes
                   (:bytes name-observation)))
              (integer? current)
              (let [bits (.bitLength (.abs (biginteger current)))]
                (when (> bits p15-s23-closed-core-max-integer-bits)
                  (p15-s23-closed-core-fail!
                   "C6-VERIFY" source-path current
                   {:missing-fact :bounded-closed-core-integer
                    :observed-integer-bits bits
                    :maximum-integer-bits
                    p15-s23-closed-core-max-integer-bits}))
                ;; At 256 bits this rendering is bounded to fewer than 80
                ;; characters, so converting only after the bit guard cannot
                ;; create an attacker-sized allocation.
                (count (str current)))
              (or (and (map? current) (not (record? current)))
                  (vector? current) (set? current))
              0
              :else
              (p15-s23-closed-core-fail!
               "C6-VERIFY" source-path
               {:observed-class (some-> current class .getName)}
               {:missing-fact :closed-artifact-canonical-scalar-domain}))
            scalar-bytes (+ scalar-bytes scalar-byte-count)]
        (when (or (> depth p15-s23-closed-core-max-plan-depth)
                  (> visited p15-s23-closed-core-max-serialized-values)
                  (> scalar-bytes
                     p15-s23-closed-core-max-artifact-scalar-bytes))
          (p15-s23-closed-core-fail!
           "C6-VERIFY" source-path current
           {:missing-fact :bounded-checked-core-value-graph
            :observed-depth depth
            :observed-values visited
            :observed-scalar-bytes scalar-bytes
            :maximum-depth p15-s23-closed-core-max-plan-depth
            :maximum-values
            p15-s23-closed-core-max-serialized-values
            :maximum-scalar-bytes
            p15-s23-closed-core-max-artifact-scalar-bytes}))
        (let [child-count
              (cond
                (and (map? current) (not (record? current)))
                (* 2 (count current))

                (or (vector? current) (set? current))
                (count current)

                ;; No lazy/host sequence is part of the closed artifact
                ;; schema.  Reject it without realizing any element.
                (seq? current)
                (p15-s23-closed-core-fail!
                 "C6-VERIFY" source-path current
                 {:missing-fact :closed-artifact-eager-value})

                :else 0)
              projected-values (+ visited (count pending) child-count)
              _ (when (> projected-values
                         p15-s23-closed-core-max-serialized-values)
                  (p15-s23-closed-core-fail!
                   "C6-VERIFY" source-path
                   {:container-kind
                    (cond
                      (map? current) :map
                      (vector? current) :vector
                      (set? current) :set
                      :else :scalar)
                    :container-count child-count}
                   {:missing-fact :bounded-container-expansion
                    :observed-values visited
                    :pending-values (count pending)
                    :projected-values projected-values
                    :maximum-values
                    p15-s23-closed-core-max-serialized-values}))
              children
              (cond
                (and (map? current) (not (record? current)))
                (mapcat identity current)
                (vector? current) current
                (set? current) current
                :else [])]
          (recur (into pending
                       (map (fn [child]
                              {:value child :depth (inc depth)})
                            children))
                 visited scalar-bytes)))
      {:status :passed
       :observed-values visited
       :observed-scalar-bytes scalar-bytes})))