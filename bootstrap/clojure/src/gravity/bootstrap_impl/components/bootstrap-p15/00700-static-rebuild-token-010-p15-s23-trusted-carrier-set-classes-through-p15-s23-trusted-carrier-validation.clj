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

(defn- p15-s23-host-native-field
  [field-once value]
  (let [access @field-once]
    (when-let [^java.lang.reflect.Field field @access]
      (try (.get field value)
           (catch IllegalAccessException _ (reset! access nil) nil)
           (catch SecurityException _ (reset! access nil) nil)))))

(defn- p15-s23-host-arraymap-storage-valid?
  [value]
  (let [backing (p15-s23-host-native-field
                 p15-s23-stage2-native-arraymap-field-once value)]
    (and (some? backing) (even? (alength ^objects backing)))))

(defn- p15-s23-host-carrier-header
  [value]
  ;; Only semantic native shape, metadata and comparator facts are read here.
  ;; The runtime's descriptive metric header is deliberately not requested.
  (let [kind (class value)
        class-name (some-> kind .getName)
        big-integer? #(identical? java.math.BigInteger (class %))
        set? (contains? #{clojure.lang.PersistentHashSet clojure.lang.PersistentTreeSet} kind)
        backing (when set?
                  (p15-s23-host-native-field
                   p15-s23-stage2-native-hashset-field-once value))
        backing-kind (class backing)
        supported?
        (cond
          (nil? value) true
          (identical? clojure.lang.PersistentArrayMap kind)
          (p15-s23-host-arraymap-storage-valid? value)
          (identical? clojure.lang.PersistentHashSet kind)
          (or (identical? clojure.lang.PersistentHashMap backing-kind)
              (and (identical? clojure.lang.PersistentArrayMap backing-kind)
                   (p15-s23-host-arraymap-storage-valid? backing)))
          (identical? clojure.lang.PersistentTreeSet kind)
          (identical? clojure.lang.PersistentTreeMap backing-kind)
          (identical? clojure.lang.BigInt kind)
          (or (nil? (.-bipart ^clojure.lang.BigInt value))
              (big-integer? (.-bipart ^clojure.lang.BigInt value)))
          (identical? clojure.lang.Ratio kind)
          (and (big-integer? (.-numerator ^clojure.lang.Ratio value))
               (big-integer? (.-denominator ^clojure.lang.Ratio value))
               (not (zero? (.signum ^java.math.BigInteger
                                    (.-denominator ^clojure.lang.Ratio value)))))
          (identical? java.math.BigDecimal kind)
          (big-integer? (.unscaledValue ^java.math.BigDecimal value))
          (identical? clojure.lang.Keyword kind)
          (identical? clojure.lang.Symbol (class (.-sym ^clojure.lang.Keyword value)))
          :else
          (contains? #{String Character Long Integer Short Byte Double Float
                       java.math.BigInteger Boolean clojure.lang.Symbol
                       clojure.lang.PersistentHashMap clojure.lang.PersistentTreeMap
                       clojure.lang.PersistentVector clojure.lang.APersistentVector$SubVector
                       clojure.lang.PersistentList clojure.lang.Cons
                       clojure.lang.PersistentList$EmptyList} kind))
        tree-map? (identical? clojure.lang.PersistentTreeMap kind)
        tree-set? (identical? clojure.lang.PersistentTreeSet kind)]
    (if-not supported?
      [:unsupported false nil]
      [(cond (nil? value) :nil
             (= "clojure.lang.PersistentList$EmptyList" class-name) :empty-list
             tree-map? :tree-map
             tree-set? :tree-set
             :else :native)
       (and (instance? clojure.lang.IObj value)
            (some? (.meta ^clojure.lang.IObj value)))
       (when (or tree-map? tree-set?)
         (if (identical? (.comparator ^clojure.lang.Sorted (if tree-set? backing value))
                         clojure.lang.RT/DEFAULT_COMPARATOR)
           :default-comparator :foreign-comparator))])))

(defn p15-s23-trusted-carrier-validation
  "Validate exact native carriers without foreign callbacks or accounting.
  Shared aliases are valid; only an identity on the ancestor path is a cycle."
  ([value sorted-policy]
   (p15-s23-trusted-carrier-validation value sorted-policy :strict-native))
  ([value sorted-policy policy]
   (when-not (contains? #{:strict-native :hosted-initializer} policy)
     (throw (IllegalArgumentException. "Unknown native carrier policy")))
   (let [hosted? (= :hosted-initializer policy) active (java.util.IdentityHashMap.)]
     (loop [stack [[:visit value]]]
       (if (empty? stack)
         {:status :passed}
         (let [[action item] (peek stack) stack (pop stack)]
           (if (= :leave action)
             (do (.remove active item) (recur stack))
             (let [tail? (= :tail action)
                   class-name (some-> item class .getName)
                   [kind metadata? comparator] (p15-s23-host-carrier-header item)
                   list-carrier? (contains? p15-s23-trusted-carrier-list-classes class-name)
                   container? (or (map? item) (vector? item) (set? item) list-carrier?)
                   view? (identical? clojure.lang.APersistentVector$SubVector (class item))
                   tracked? (or container? (and hosted? metadata?))
                   reason
                   (cond
                     (and tail? (not (or (nil? item) list-carrier?)))
                     :untrusted-list-tail-class
                     (map? item)
                     (when-not (and (not= :unsupported kind)
                                    (or (contains? p15-s23-trusted-carrier-map-classes class-name)
                                        (and (= :tree-map kind) (= :default-only sorted-policy)
                                             (= :default-comparator comparator))))
                       :untrusted-map-class-or-comparator)
                     (vector? item)
                     (when-not (and (not= :unsupported kind)
                                    (or (= "clojure.lang.PersistentVector" class-name)
                                        (and hosted? view?)))
                       :untrusted-vector-class)
                     (set? item)
                     (when-not (and (not= :unsupported kind)
                                    (or (contains? p15-s23-trusted-carrier-set-classes class-name)
                                        (and (= :tree-set kind) (= :default-only sorted-policy)
                                             (= :default-comparator comparator))))
                       :untrusted-set-class-or-comparator)
                     (or (list? item) list-carrier?)
                     (when (= :unsupported kind) :untrusted-list-class)
                     (nil? item) nil
                     :else
                     (when (or (= :unsupported kind)
                               (not (contains? p15-s23-trusted-carrier-scalar-classes class-name)))
                       :untrusted-scalar-class))
                   reason (or reason
                              (when (and metadata? (not hosted?)) :carrier-metadata)
                              (when (and hosted? view?)
                                (p15-s23-hosted-native-view-reason item active))
                              (when (and tracked? (.containsKey active item)) :cyclic-carrier))]
               (if reason
                 {:status :rejected :reason reason :class (or class-name :nil)}
                 (let [pending (if tracked?
                                 (do (.put active item true) (conj stack [:leave item])) stack)
                       pending (if (and hosted? metadata?)
                                 (conj pending [:visit (.meta ^clojure.lang.IObj item)]) pending)
                       pending
                       (cond
                         (map? item)
                         (reduce-kv (fn [pending key child]
                                      (conj pending [:visit key] [:visit child])) pending item)
                         (or (vector? item) (set? item))
                         (reduce #(conj %1 [:visit %2]) pending item)
                         (and list-carrier? (not= :empty-list kind))
                         (conj pending
                               [:tail (if (= "clojure.lang.Cons" class-name)
                                        (.more ^clojure.lang.Cons item)
                                        (.next ^clojure.lang.ISeq item))]
                               [:visit (.first ^clojure.lang.ISeq item)])
                         :else pending)]
                   (recur pending))))))))))
  ;; Transitional callers may supply obsolete limits; no accounting is done.
  ([value sorted-policy _maximum-nodes _maximum-depth _maximum-width]
   (p15-s23-trusted-carrier-validation value sorted-policy))))
