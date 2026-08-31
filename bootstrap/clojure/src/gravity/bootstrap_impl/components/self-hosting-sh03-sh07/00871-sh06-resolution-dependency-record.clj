

(defn sh06-resolution-dependency-record
  [source-path module dependency overrides ordinal]
  (let [namespace (:module dependency)
        alias (:alias dependency)
        referred (vec (:refer dependency))
        candidate
        (or (first (filter #(= namespace (:namespace %))
                           (:candidate-bindings overrides)))
            {})
        declared-target (or (:target candidate)
                            (first (:target-set candidate)))
        targets (if declared-target
                  [declared-target]
                  [(:target module)])
        foreign? (= :import (:kind dependency))
        semantic-span {:dependency-ordinal ordinal
                       :namespace namespace
                       :alias alias}]
    {:namespace namespace
     :alias alias
     :kind (if foreign? :foreign :namespace)
     :profile (or (:profile dependency) (:profile module))
     :targets targets
     :visibility (or (:visibility candidate)
                     (:visibility dependency) :public)
     :refer referred
     :effects (vec (sort (:effects dependency)))
     :capabilities (vec (sort (or (:capabilities candidate)
                                  (:capabilities dependency))))
     :safety (if foreign? :boundary-checked :safe)
     :boundary (or (:boundary dependency)
                   (when (= :core (:profile dependency)) :pure-core-api))
     :semantic-span semantic-span
     :source-span (source-span source-path 0)
     :dependency-artifact-id
     (reader-canonical-hash
      {:domain :gravity/sh06-dependency-artifact-v1
       :namespace namespace :profile (:profile dependency)
       :targets targets :boundary (:boundary dependency)})
     :foreign-record-complete?
     (if foreign?
       (and (symbol? namespace) (symbol? alias)
            (some? (:boundary dependency)))
       true)}))

(defn sh06-resolution-import-records
  [source-path module overrides]
  (mapv
   (fn [ordinal dependency]
     (sh06-resolution-dependency-record
      source-path module dependency overrides ordinal))
   (range)
   (concat (:requires module) (:imports module))))

(defn sh06-resolution-import-binding-records
  [module imports overrides]
  (vec
   (mapcat
    (fn [ordinal dependency]
      (let [explicit
            (filter #(= (:namespace dependency) (:namespace %))
                    (:candidate-bindings overrides))
            names (distinct
                   (concat (:refer dependency)
                           (map :name explicit)))
            candidates-by-name (group-by :name explicit)]
        (map-indexed
         (fn [name-ordinal name]
           (let [candidate (first (get candidates-by-name name))]
             {:name name
              :kind (if (= :foreign (:kind dependency)) :foreign :var)
              :namespace (:namespace dependency)
              :package (sh06-resolution-package (:namespace dependency))
              :binding-class :import
              :visibility (or (:visibility candidate)
                              (:visibility dependency) :public)
              :profile-set [(:profile dependency)]
              :target-set (or (some-> candidate :target-set vec)
                              (:targets dependency))
              :type-ref (if (= :foreign (:kind dependency))
                          :gravity.interop/foreign-value
                          :gravity.type/imported-var)
              :effects (:effects dependency)
              :capabilities (or (some-> candidate :capabilities vec)
                                (:capabilities dependency))
              :safety (:safety dependency)
              :semantic-span {:dependency-ordinal ordinal
                              :binding-ordinal name-ordinal
                              :namespace (:namespace dependency)
                              :name name}
              :source-span (:source-span dependency)
              :definition-syntax-id
              (reader-canonical-hash
               {:domain :gravity/sh06-import-binding-syntax-v1
                :dependency-id (:dependency-artifact-id dependency)
                :name name})
              :definition-artifact-id
              (:dependency-artifact-id dependency)}))
         names)))
    (range)
    imports)))

(defn sh06-resolution-explicit-candidate-records
  [module sh05-artifact overrides]
  (let [existing-import-names
        (set (mapcat :refer (concat (:requires module) (:imports module))))]
    (->> (:candidate-bindings overrides)
         (remove #(contains? existing-import-names (:name %)))
         (map-indexed
          (fn [ordinal candidate]
            {:name (:name candidate)
             :kind (or (:kind candidate) :var)
             :namespace (or (:namespace candidate) (:module module))
             :package (sh06-resolution-package
                       (or (:namespace candidate) (:module module)))
             :binding-class :namespace
             :visibility (or (:visibility candidate) :public)
             :profile-set [(or (:profile candidate) (:profile module))]
             :target-set (vec (or (:target-set candidate)
                                  [(:target module)]))
             :type-ref :gravity.type/value
             :effects (vec (or (:effects candidate) []))
             :capabilities (vec (or (:capabilities candidate) []))
             :safety :safe
             :semantic-span {:explicit-candidate ordinal
                             :name (:name candidate)
                             :namespace (:namespace candidate)}
             :source-span (source-span (:source-path module) 0)
             :definition-syntax-id
             (reader-canonical-hash
              {:domain :gravity/sh06-explicit-candidate-v1
               :ordinal ordinal :candidate candidate})
             :definition-artifact-id
             (reader-canonical-hash
              {:domain :gravity/sh06-explicit-candidate-artifact-v1
               :owner-artifact-id (:artifact-id sh05-artifact)
               :ordinal ordinal
               :candidate candidate})}))
         vec)))

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

    :else
    []))

(defn sh06-fixed-vector-pattern?
  [pattern]
  (cond
    (vector? pattern)
    (every? sh06-fixed-vector-pattern? pattern)

    (symbol? pattern)
    (not= '& pattern)

    :else
    (or (nil? pattern)
        (true? pattern)
        (false? pattern)
        (number? pattern)
        (char? pattern)
        (string? pattern)
        (keyword? pattern))))

(defn sh06-unique-match-binding-paths
  [pattern path]
  (let [binding-paths
        (sh06-match-pattern-binding-paths pattern path)
        names (mapv :name binding-paths)]
    (when (= (count names) (count (set names)))
      binding-paths)))