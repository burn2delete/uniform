

(defn p15-s23-closed-core-value-domain!
  [source-path value]
  (loop [pending [value]]
    (if (seq pending)
      (let [current (peek pending)
            pending (pop pending)]
        (do
          (cond
            (nil? current) nil
            (boolean? current) nil
            (string? current)
            (let [observation (p15-s23-closed-core-utf8-count current)]
              (when-not (= :valid (:status observation))
                (p15-s23-closed-core-fail!
                 "C6-VERIFY" source-path
                 {:observed-string-length (.length ^String current)}
                 {:missing-fact :canonical-string-scalar
                  :encoding-status (:status observation)}))
              nil)
            (char? current)
            (let [code (int current)]
              (when (<= 0xD800 code 0xDFFF)
                (p15-s23-closed-core-fail!
                 "C6-VERIFY" source-path current
                 {:missing-fact :unicode-scalar-character})))
            (or (keyword? current) (symbol? current))
            (let [namespace-part (namespace current)
                  name-part (name current)
                  namespace-observation
                  (when namespace-part
                    (p15-s23-closed-core-utf8-count namespace-part))
                  name-observation
                  (p15-s23-closed-core-utf8-count name-part)]
              (when-not (and (or (nil? namespace-observation)
                                 (= :valid (:status namespace-observation)))
                             (= :valid (:status name-observation)))
                (p15-s23-closed-core-fail!
                 "C6-VERIFY" source-path
                 {:scalar-kind (if (keyword? current) :keyword :symbol)}
                 {:missing-fact :canonical-named-scalar}))
              nil)
            (integer? current)
            nil
            (or (and (map? current) (not (record? current)))
                (vector? current) (set? current))
            nil
            :else
            (p15-s23-closed-core-fail!
             "C6-VERIFY" source-path
             {:observed-class (some-> current class .getName)}
             {:missing-fact :closed-artifact-canonical-scalar-domain}))
          (let [children
                (cond
                  (and (map? current) (not (record? current)))
                  (mapcat identity current)
                  (vector? current) current
                  (set? current) current
                ;; No lazy/host sequence is part of the closed artifact
                ;; schema.  Reject it without realizing any element.
                  (seq? current)
                  (p15-s23-closed-core-fail!
                   "C6-VERIFY" source-path current
                   {:missing-fact :closed-artifact-eager-value})
                  :else [])]
            (recur (into pending children)))))
      {:status :passed})))
