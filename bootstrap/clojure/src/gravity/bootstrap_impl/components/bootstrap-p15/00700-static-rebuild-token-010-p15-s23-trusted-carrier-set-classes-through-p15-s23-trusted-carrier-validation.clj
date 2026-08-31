(let [static-rebuild-token (nth __gravity_bootstrap_checked_core_authority_values 0)
      p15-s23-stage2-closed-checked-core-source-artifact-internal __gravity_bootstrap_checked_core_source_helper
      p15-s23-stage2-closed-checked-core-rebuild-internal __gravity_bootstrap_checked_core_rebuild_helper]
(def p15-s23-trusted-carrier-set-classes
  #{"clojure.lang.PersistentHashSet"})

(def p15-s23-trusted-carrier-list-classes
  #{"clojure.lang.PersistentList"
    "clojure.lang.PersistentList$EmptyList"
    "clojure.lang.Cons"})

(def p15-s23-trusted-carrier-scalar-classes
  #{"java.lang.String" "java.lang.Character"
    "java.lang.Long" "java.lang.Integer" "java.lang.Short"
    "java.lang.Byte" "java.lang.Double" "java.lang.Float"
    "java.math.BigInteger" "java.math.BigDecimal"
    "clojure.lang.BigInt" "clojure.lang.Ratio"
    "clojure.lang.Keyword" "clojure.lang.Symbol"
    "java.lang.Boolean"})

(defn p15-s23-trusted-carrier-validation
  "Iteratively validate exact host carrier classes without executing custom
  comparators or descending into an untrusted collection implementation.
  `sorted-policy` is either :reject or :default-only."
  [value sorted-policy maximum-nodes maximum-depth maximum-width]
  (loop [stack [[value 0]]
         nodes 0
         observed-depth 0
         classes #{}]
    (if (empty? stack)
      {:status :passed
       :observed-nodes nodes
       :observed-depth observed-depth
       :classes classes}
      (let [[item depth] (peek stack)
            stack (pop stack)
            nodes (inc nodes)
            observed-depth (max observed-depth depth)
            class-name (when (some? item) (.getName (class item)))
            classes (if class-name (conj classes class-name) classes)
            reject
            (fn [reason]
              (cond->
               {:status :rejected
                :reason reason
                :class (or class-name :nil)
                :observed-nodes nodes
                :observed-depth observed-depth}
                (= :maximum-carrier-nodes reason)
                (assoc :maximum-nodes maximum-nodes)
                (= :maximum-carrier-depth reason)
                (assoc :maximum-depth maximum-depth)
                (= :maximum-carrier-width reason)
                (assoc :maximum-width maximum-width)))]
        (cond
          (> nodes maximum-nodes) (reject :maximum-carrier-nodes)
          (> depth maximum-depth) (reject :maximum-carrier-depth)
          (nil? item)
          (recur stack nodes observed-depth classes)

          (map? item)
          (let [tree? (= "clojure.lang.PersistentTreeMap" class-name)
                allowed?
                (or (contains? p15-s23-trusted-carrier-map-classes
                               class-name)
                    (and tree?
                         (= :default-only sorted-policy)
                         (identical?
                          (.comparator ^clojure.lang.Sorted item)
                          clojure.lang.RT/DEFAULT_COMPARATOR)))]
            (cond
              (not allowed?) (reject :untrusted-map-class-or-comparator)
              (some? (meta item)) (reject :carrier-metadata)
              (> (count item) maximum-width) (reject :maximum-carrier-width)
              :else
              (recur
               (reduce-kv (fn [pending key child]
                            (conj pending [key (inc depth)]
                                  [child (inc depth)]))
                          stack item)
               nodes observed-depth classes)))

          (vector? item)
          (let [allowed? (= "clojure.lang.PersistentVector" class-name)]
           (cond
            (not allowed?)
            (reject :untrusted-vector-class)
            (some? (meta item)) (reject :carrier-metadata)
            (> (count item) maximum-width) (reject :maximum-carrier-width)
            :else
            (recur (reduce #(conj %1 [%2 (inc depth)]) stack item)
                   nodes observed-depth classes)))

          (set? item)
          (let [tree? (= "clojure.lang.PersistentTreeSet" class-name)
                allowed?
                (or (contains? p15-s23-trusted-carrier-set-classes class-name)
                    (and tree?
                         (= :default-only sorted-policy)
                         (identical?
                          (.comparator ^clojure.lang.Sorted item)
                          clojure.lang.RT/DEFAULT_COMPARATOR)))]
            (cond
              (not allowed?) (reject :untrusted-set-class-or-comparator)
              (some? (meta item)) (reject :carrier-metadata)
              (> (count item) maximum-width) (reject :maximum-carrier-width)
              :else
              (recur (reduce #(conj %1 [%2 (inc depth)]) stack item)
                     nodes observed-depth classes)))

          (or (list? item) (= "clojure.lang.Cons" class-name))
          (cond
            (not (contains? p15-s23-trusted-carrier-list-classes class-name))
            (reject :untrusted-list-class)
            (some? (meta item)) (reject :carrier-metadata)
            :else
            (let [expanded
                  (loop [cursor item
                         width 0
                         pending stack]
                    (let [cursor-class
                          (when (some? cursor) (.getName (class cursor)))]
                      (cond
                        (or (nil? cursor)
                            (= "clojure.lang.PersistentList$EmptyList"
                               cursor-class))
                        {:status :passed :stack pending}

                        (>= width maximum-width)
                        {:status :rejected :reason :maximum-carrier-width}

                        (not (contains?
                              p15-s23-trusted-carrier-list-classes
                              cursor-class))
                        {:status :rejected
                         :reason :untrusted-list-tail-class}

                        (some? (meta cursor))
                        {:status :rejected :reason :carrier-metadata}

                        :else
                        (let [pending
                              (conj pending
                                    [(.first ^clojure.lang.ISeq cursor)
                                     (inc depth)])
                              tail
                              (if (= "clojure.lang.Cons" cursor-class)
                                (.more ^clojure.lang.Cons cursor)
                                (.next ^clojure.lang.ISeq cursor))]
                          (recur tail (inc width) pending)))))]
              (if (= :passed (:status expanded))
                (recur (:stack expanded) nodes observed-depth classes)
                (reject (:reason expanded)))))

          (contains? p15-s23-trusted-carrier-scalar-classes class-name)
          (if (and (instance? clojure.lang.IObj item) (some? (meta item)))
            (reject :carrier-metadata)
            (recur stack nodes observed-depth classes))

          :else (reject :untrusted-scalar-class)))))))
