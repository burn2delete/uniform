(defn- p15-s23-stage2-native-integer-magnitude-bits
  [^long value]
  ;; abs(MIN_VALUE) keeps its unsigned magnitude bit pattern, giving 64 bits.
  (- 64 (Long/numberOfLeadingZeros (Math/abs value))))

(defn- p15-s23-stage2-native-unsupported-header
  []
  [:unsupported nil nil nil nil nil])

(defn- p15-s23-stage2-native-big-integer-magnitude-bits
  [value]
  (when (identical? java.math.BigInteger (class value))
    ;; Positive bitLength reads magnitude headers; negative bitLength may scan.
    (.bitLength (.abs ^java.math.BigInteger value))))

(defn- p15-s23-stage2-native-hashset-field
  []
  (try
    (let [^java.lang.reflect.Field field (.getDeclaredField clojure.lang.APersistentSet "impl")]
      (when (.trySetAccessible field) field))
    (catch java.lang.ReflectiveOperationException _ nil)
    (catch SecurityException _ nil)))

(def ^:private p15-s23-stage2-native-hashset-field-once
  ;; Cache only the fixed field's access result, including denial, never DATA.
  (delay (atom (p15-s23-stage2-native-hashset-field))))

(defn- p15-s23-stage2-native-hashset-backing
  [value]
  (when (identical? clojure.lang.PersistentHashSet (class value))
    (let [access @p15-s23-stage2-native-hashset-field-once]
      (when-let [^java.lang.reflect.Field field @access]
        (try (.get field value)
             (catch IllegalAccessException _ (reset! access nil) nil)
             (catch SecurityException _ (reset! access nil) nil))))))

(defn- p15-s23-stage2-native-map-extent
  [value]
  (let [kind (class value)]
    (cond
      (identical? clojure.lang.PersistentArrayMap kind)
      (.count ^clojure.lang.PersistentArrayMap value)
      (identical? clojure.lang.PersistentHashMap kind)
      (.count ^clojure.lang.PersistentHashMap value)
      (identical? clojure.lang.PersistentTreeMap kind)
      (.count ^clojure.lang.PersistentTreeMap value))))

(defn- p15-s23-stage2-native-meta-present?
  [value]
  ;; Called only after an exact native IObj class branch, never on foreign IObj.
  (not (nil? (.meta ^clojure.lang.IObj value))))

(defn- p15-s23-stage2-native-carrier-header
  [plan value]
  (when-not (and (true? (:compiler-artifact-plan? plan))
                 (= :gravity/stage2-compiler-artifact-plan (:kind plan))
                 (= :meta (get-in plan [:module :profile]))
                 (= :jvm (get-in plan [:module :target])))
    (throw (IllegalArgumentException.
            "bootstrap-native-carrier-header requires a compiler-artifact meta/JVM plan")))
  (let [kind (class value)]
    (cond
      (nil? value) [:nil false nil nil nil nil]
      (identical? Boolean kind) [:boolean false nil nil nil nil]
      (identical? Character kind) [:character false nil nil nil nil]
      (identical? Integer kind)
      [:integer32 false nil
       (p15-s23-stage2-native-integer-magnitude-bits (.longValue ^Integer value)) nil nil]
      (identical? Long kind)
      [:integer64 false nil
       (p15-s23-stage2-native-integer-magnitude-bits (.longValue ^Long value)) nil nil]
      (identical? clojure.lang.BigInt kind)
      (let [backing (.-bipart ^clojure.lang.BigInt value)
            bits (if (nil? backing)
                   (p15-s23-stage2-native-integer-magnitude-bits (.-lpart ^clojure.lang.BigInt value))
                   (p15-s23-stage2-native-big-integer-magnitude-bits backing))]
        (if (some? bits) [:big-int false nil bits nil nil]
          (p15-s23-stage2-native-unsupported-header)))
      (identical? java.math.BigInteger kind)
      [:big-integer false nil (p15-s23-stage2-native-big-integer-magnitude-bits value) nil nil]
      (identical? clojure.lang.Ratio kind)
      (let [numerator (p15-s23-stage2-native-big-integer-magnitude-bits
                       (.-numerator ^clojure.lang.Ratio value))
            denominator (p15-s23-stage2-native-big-integer-magnitude-bits
                         (.-denominator ^clojure.lang.Ratio value))]
        (if (and (some? numerator) (some? denominator))
          [:ratio false nil numerator denominator nil]
          (p15-s23-stage2-native-unsupported-header)))
      (identical? String kind) [:string false nil (.length ^String value) nil nil]
      (identical? clojure.lang.Symbol kind)
      [:symbol (p15-s23-stage2-native-meta-present? value) nil
       (p15-s23-stage2-bootstrap-symbol-utf16-unit-count plan value) nil nil]
      (identical? clojure.lang.Keyword kind)
      (let [^clojure.lang.Symbol spelling (.-sym ^clojure.lang.Keyword value)]
        (if (identical? clojure.lang.Symbol (class spelling))
          [:keyword false nil
           (if (and (nil? (.getName spelling)) (nil? (.getNamespace spelling)))
             5
             (inc (p15-s23-stage2-bootstrap-symbol-utf16-unit-count plan spelling))) nil nil]
          (p15-s23-stage2-native-unsupported-header)))
      (or (identical? clojure.lang.PersistentArrayMap kind)
          (identical? clojure.lang.PersistentHashMap kind))
      [:map (p15-s23-stage2-native-meta-present? value)
       (p15-s23-stage2-native-map-extent value) nil nil nil]
      (identical? clojure.lang.PersistentTreeMap kind)
      [:tree-map (p15-s23-stage2-native-meta-present? value)
       (p15-s23-stage2-native-map-extent value) nil
       (if (identical? clojure.lang.RT/DEFAULT_COMPARATOR
                       (.comparator ^clojure.lang.PersistentTreeMap value))
         :default-comparator :foreign-comparator) nil]
      (identical? clojure.lang.PersistentHashSet kind)
      (let [backing (p15-s23-stage2-native-hashset-backing value)
            backing-kind (class backing)]
        (if (or (identical? clojure.lang.PersistentArrayMap backing-kind)
                (identical? clojure.lang.PersistentHashMap backing-kind))
          [:set (p15-s23-stage2-native-meta-present? value)
           (p15-s23-stage2-native-map-extent backing) nil backing nil]
          (p15-s23-stage2-native-unsupported-header)))
      (identical? clojure.lang.PersistentVector kind)
      [:vector (p15-s23-stage2-native-meta-present? value)
       (.count ^clojure.lang.PersistentVector value) nil nil nil]
      (identical? clojure.lang.APersistentVector$SubVector kind)
      (let [^clojure.lang.APersistentVector$SubVector view value
            start (long (.-start view)) end (long (.-end view))]
        [:subvector (p15-s23-stage2-native-meta-present? value)
         (- end start) nil (.-v view) start])
      (identical? clojure.lang.PersistentList kind)
      [:list-node (p15-s23-stage2-native-meta-present? value)
       (.count ^clojure.lang.PersistentList value) nil
       (.first ^clojure.lang.PersistentList value) (.next ^clojure.lang.PersistentList value)]
      (identical? clojure.lang.Cons kind)
      [:list-node (p15-s23-stage2-native-meta-present? value) nil nil
       (.first ^clojure.lang.Cons value) (.more ^clojure.lang.Cons value)]
      (identical? (class clojure.lang.PersistentList/EMPTY) kind)
      [:empty-list (p15-s23-stage2-native-meta-present? value) 0 nil nil nil]
      :else (p15-s23-stage2-native-unsupported-header))))
