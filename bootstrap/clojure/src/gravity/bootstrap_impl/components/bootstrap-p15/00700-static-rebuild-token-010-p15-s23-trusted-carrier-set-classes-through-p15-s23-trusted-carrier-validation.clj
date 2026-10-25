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

(defn- p15-s23-host-carrier-header
  [value]
  ;; Reuse the exact-class, callback-free host observation boundary.
  (if (identical? clojure.lang.PersistentTreeSet (class value))
    (let [access @p15-s23-stage2-native-hashset-field-once
          backing (when-let [^java.lang.reflect.Field field @access]
                    (try (.get field value)
                         (catch IllegalAccessException _ (reset! access nil) nil)
                         (catch SecurityException _ (reset! access nil) nil)))]
      (if (identical? clojure.lang.PersistentTreeMap (class backing))
        [:tree-set (some? (.meta ^clojure.lang.IObj value))
         (.count ^clojure.lang.PersistentTreeMap backing) nil
         (if (identical? (.comparator ^clojure.lang.Sorted backing)
                         clojure.lang.RT/DEFAULT_COMPARATOR)
           :default-comparator :foreign-comparator)]
        [:unsupported nil nil nil nil nil]))
    (p15-s23-stage2-native-carrier-header
     {:kind :gravity/stage2-compiler-artifact-plan :compiler-artifact-plan? true
      :module {:profile :meta :target :jvm}}
     value)))

(defn p15-s23-trusted-carrier-validation
  "Validate exact native carriers iteratively without invoking foreign methods.
  `sorted-policy` is either :reject or :default-only. Shared aliases are valid;
  only an identity already on the current ancestor path is a cycle."
  ([value sorted-policy]
   (let [active (java.util.IdentityHashMap.)]
    (loop [stack [[:visit value 0]] nodes 0 observed-depth 0 classes #{}]
     (if (empty? stack)
      {:status :passed :observed-nodes nodes
       :observed-depth observed-depth :classes classes}
      (let [[action item depth] (peek stack) stack (pop stack)]
       (if (= :leave action)
        (do (.remove active item) (recur stack nodes observed-depth classes))
        (let [tail? (= :tail action)
              nodes (if tail? nodes (inc nodes))
              observed-depth (max observed-depth depth)
              class-name (when (some? item) (.getName (class item)))
              classes (if (and class-name (not tail?))
                        (conj classes class-name) classes)
              [kind metadata? _ _ comparator] (p15-s23-host-carrier-header item)
              list-carrier? (contains? p15-s23-trusted-carrier-list-classes class-name)
              container? (or (map? item) (vector? item) (set? item) list-carrier?)
              reason
              (cond
               (and tail? (not (or (nil? item) list-carrier?))) :untrusted-list-tail-class
               (map? item)
               (when-not (and (not= :unsupported kind)
                             (or (contains? p15-s23-trusted-carrier-map-classes class-name)
                                 (and (= "clojure.lang.PersistentTreeMap" class-name)
                                      (= :default-only sorted-policy)
                                      (= :default-comparator comparator))))
                 :untrusted-map-class-or-comparator)
               (vector? item)
               (when-not (= "clojure.lang.PersistentVector" class-name)
                 :untrusted-vector-class)
               (set? item)
               (when-not (and (not= :unsupported kind)
                             (or (contains? p15-s23-trusted-carrier-set-classes class-name)
                                 (and (= "clojure.lang.PersistentTreeSet" class-name)
                                      (= :default-only sorted-policy)
                                      (identical? (.comparator ^clojure.lang.Sorted item)
                                                  clojure.lang.RT/DEFAULT_COMPARATOR))))
                 :untrusted-set-class-or-comparator)
               (or (list? item) list-carrier?)
               (when (= :unsupported kind) :untrusted-list-class)
               (nil? item) nil
               :else
               (when (or (= :unsupported kind)
                         (not (contains? p15-s23-trusted-carrier-scalar-classes class-name)))
                 :untrusted-scalar-class))
              reason (or reason
                         (when metadata? :carrier-metadata)
                         (when (and container? (.containsKey active item)) :cyclic-carrier))]
         (if reason
          {:status :rejected :reason reason :class (or class-name :nil)
           :observed-nodes nodes :observed-depth observed-depth}
          (let [pending (if container?
                          (do (.put active item true) (conj stack [:leave item depth]))
                          stack)
                child-depth (inc depth)
                pending
                (cond
                 (map? item)
                 (reduce-kv (fn [pending key child]
                              (conj pending [:visit key child-depth] [:visit child child-depth]))
                            pending item)
                 (or (vector? item) (set? item))
                 (reduce #(conj %1 [:visit %2 child-depth]) pending item)
                 (and list-carrier? (not= :empty-list kind))
                 (let [head (.first ^clojure.lang.ISeq item)
                       tail (if (= "clojure.lang.Cons" class-name)
                              (.more ^clojure.lang.Cons item)
                              (.next ^clojure.lang.ISeq item))]
                   (conj pending [:tail tail depth] [:visit head child-depth]))
                 :else pending)]
           (recur pending nodes observed-depth classes))))))))))
  ;; Transitional callers may still supply the removed limits. No argument
  ;; controls admission; this delegates to the ordinary unmetered entrypoint.
  ([value sorted-policy _maximum-nodes _maximum-depth _maximum-width]
   (p15-s23-trusted-carrier-validation value sorted-policy))))
