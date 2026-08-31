

(defn child-context
  [ctx]
  (atom (update @ctx :linear-resources
                (fn [resources]
                  (into {} (map (fn [[k v]] [k (assoc v :consumed? (:consumed? v false))])
                                resources))))))

(defn literal-type
  [value]
  (cond
    (nil? value) "Nil"
    (true? value) "Boolean"
    (false? value) "Boolean"
    (integer? value) "Integer"
    (ratio? value) "ExactRatio"
    (float? value) "F64"
    (string? value) "String"
    (char? value) "Character"
    (symbol? value) "Symbol"
    (keyword? value) "Keyword"
    (seq? value) "List"
    (vector? value) "Vector"
    (map? value) "Map"
    (set? value) "Set"
    :else "Dynamic"))

(defn collect-fact-effects
  [facts]
  (set (mapcat :effects facts)))

(defn collect-fact-capabilities
  [facts]
  (set (mapcat :capabilities facts)))

(defn common-type
  [facts]
  (let [types (stable-vec (keep :type facts))]
    (cond
      (empty? types) "Nil"
      (= 1 (count types)) (first types)
      :else (str "Union[" (str/join "," types) "]"))))

(defn linear-type?
  [type-name]
  (and (string? type-name) (str/starts-with? type-name "Linear[")))

(defn linear-resource-type
  [type-name]
  (subs type-name (count "Linear[") (dec (count type-name))))