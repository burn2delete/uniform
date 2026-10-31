

(defn
 sh06-resolution-dependency-record
 [source-path module dependency overrides ordinal]
 (gravity.resolution/dependency-record
  source-path
  module
  dependency
  overrides
  ordinal
  (resolution-stage-operations)))

(defn
 sh06-resolution-import-records
 [source-path module overrides]
 (gravity.resolution/import-records
  source-path
  module
  overrides
  (resolution-stage-operations)))

(defn
 sh06-resolution-import-binding-records
 [module imports overrides]
 (gravity.resolution/import-binding-records
  module
  imports
  overrides
  (resolution-stage-operations)))

(defn
 sh06-resolution-explicit-candidate-records
 [module sh05-artifact overrides]
 (gravity.resolution/explicit-candidate-records
  module
  sh05-artifact
  overrides
  (resolution-stage-operations)))

(defn sh06-match-pattern-binding-paths
  [pattern path]
  (cond
    (and (symbol? pattern)
         (not= '_ pattern)
         (not= '& pattern))
    [{:name pattern :path path}]

    (vector? pattern)
    (vec
     (mapcat
      (fn [index item]
        (sh06-match-pattern-binding-paths
         item (conj path index)))
      (range)
      pattern))

    (map? pattern)
    (vec (mapcat (fn [index [key child]]
                   (if (= :when key) []
                     (sh06-match-pattern-binding-paths child (conj path (inc (* 2 index))))))
                 (range) pattern))

    :else
    []))

(defn sh06-fixed-vector-pattern?
  [pattern]
  (cond
    (vector? pattern)
    (every? sh06-fixed-vector-pattern? pattern)

    (map? pattern)
    (and (not (contains? pattern :when))
         (every? keyword? (keys pattern))
         (every? sh06-fixed-vector-pattern? (vals pattern)))

    (symbol? pattern)
    (not= '& pattern)

    :else
    (or (nil? pattern)
        (true? pattern)
        (false? pattern)
        (number? pattern)
        (char? pattern)
        (string? pattern)
        (keyword? pattern)
        (and (seq? pattern) (= 2 (count pattern))
             (= 'quote (first pattern)) (symbol? (second pattern))))))

(defn sh06-unique-match-binding-paths
  [pattern path]
  (let [binding-paths
        (sh06-match-pattern-binding-paths pattern path)
        names (mapv :name binding-paths)]
    (when (= (count names) (count (set names)))
      binding-paths)))

(defn sh06-let-loop-binding-paths
  [pattern path]
  (if (vector? pattern)
    (:bindings
     (letfn [(walk [value current-path state]
               (cond
                 (vector? value)
                 (reduce-kv
                  (fn [next-state index item]
                    (walk item (conj current-path index) next-state))
                  state
                  value)

                 (and (symbol? value)
                      (not= '_ value)
                      (not= '& value)
                      (not (contains? (:seen state) value)))
                 (-> state
                     (update :seen conj value)
                     (update :bindings conj
                             {:name value :path current-path}))

                 :else
                 state))]
       (walk pattern path {:seen #{} :bindings []})))
    (if (symbol? pattern)
      [{:name pattern :path path}]
      [])))
