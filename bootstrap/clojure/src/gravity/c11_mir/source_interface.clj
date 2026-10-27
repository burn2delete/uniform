(ns gravity.c11-mir.source-interface
  "Small dispatch-header checks, not MIR/source/report admission.
   Legacy arities select v1 explicitly. Parsing v2 never supplies its missing
   reviewed source identities or authorizes a constructor/verifier callback.")

(def legacy-version 1)
(def ^:private current-version 2)
(def ^:private interface-id :gravity/c11-mir)
(def ^:private artifact-kind :gravity/p15-s23-c11-authenticated-mir-artifact)

(defn- reject! [reason]
  ;; Never retain or format hostile values in the diagnostic.
  (throw (ex-info "Invalid C11 interface header"
                  {:id "C11-VERIFY" :missing-fact :versioned-c11-interface
                   :reason reason})))

(def ^:private array-field
  ;; Cache the observation capability only, never an input or an admission.
  (delay
    (atom
      (try
        (let [f (.getDeclaredField clojure.lang.PersistentArrayMap "array")]
          (when (.trySetAccessible f) f))
        (catch java.lang.ReflectiveOperationException _ nil)
        (catch SecurityException _ nil)))))

(defn- array-storage! [value]
  (let [access @array-field]
    (if-let [^java.lang.reflect.Field f @access]
      (let [storage (try (.get f value)
                        (catch IllegalAccessException _ (reset! access nil) nil)
                        (catch SecurityException _ (reset! access nil) nil))]
        (when-not (and (some? storage) (even? (alength ^objects storage)))
          (reject! :malformed-native-map-storage))
        storage)
      (reject! :native-map-storage-observer-unavailable))))

(defn- pairs! [value closed-count]
  ;; No IMeta, lookup, keys, sequence, equality or hashing on unknown classes.
  (let [kind (class value)]
    (when-not (or (identical? clojure.lang.PersistentArrayMap kind)
                  (identical? clojure.lang.PersistentHashMap kind))
      (reject! :interface-map-class))
    (let [storage (when (identical? clojure.lang.PersistentArrayMap kind)
                    (array-storage! value))
          size (if storage (quot (alength ^objects storage) 2)
                   (.count ^clojure.lang.PersistentHashMap value))]
      ;; Fixed key cardinality is schema legality, not a traversal quota.
      (when (and closed-count (not= size closed-count))
        (reject! :interface-header-keyset))
      (when (some? (.meta ^clojure.lang.IObj value))
        (reject! :interface-header-metadata))
      (if storage
        (loop [i 0 out []]
          (if (= i (alength ^objects storage)) out
            (recur (+ i 2) (conj out [(aget ^objects storage i)
                                     (aget ^objects storage (inc i))]))))
        (reduce-kv (fn [out k v] (conj out [k v])) [] value)))))

(defn- native-keyword? [x] (identical? clojure.lang.Keyword (class x)))
(defn- supported-version! [x]
  (when-not (and (identical? Long (class x))
                 (or (== 1 (.longValue ^Long x))
                     (== 2 (.longValue ^Long x))))
    (reject! :interface-version-class-or-value))
  x)

(defn interface-version!
  "Parse the explicit new-arity selector only. Nil/empty is never legacy v1."
  [header]
  (let [[seen fields]
        (reduce (fn [[seen out] [k v]]
                  (when-not (native-keyword? k) (reject! :interface-header-key-class))
                  (let [slot (cond (identical? :interface k) 0
                                   (identical? :version k) 1
                                   :else (reject! :interface-header-keyset))]
                    (when (bit-test seen slot)
                      (reject! :interface-header-keyset))
                    [(bit-set seen slot) (assoc out slot v)]))
                [0 [nil nil]] (pairs! header 2))
        [interface version] fields]
    (when-not (= 3 seen) (reject! :interface-header-keyset))
    (when-not (and (native-keyword? interface) (identical? interface-id interface))
      (reject! :interface-id))
    (supported-version! version)
    (when-not (== current-version (.longValue ^Long version))
      (reject! :explicit-interface-version))
    current-version))

(defn artifact-version!
  "Inspect only native top-level discriminants, never Core/MIR/report payload.
   This does not validate the artifact's complete keyset or semantic contents."
  [artifact]
  (let [[seen [kind version]]
        (reduce (fn [[seen out] [k v]]
                  (when-not (native-keyword? k) (reject! :artifact-header-key-class))
                  (let [slot (cond (identical? :kind k) 0
                                   (identical? :schema-version k) 1
                                   :else nil)]
                    (if (nil? slot) [seen out]
                      (do (when (bit-test seen slot)
                            (reject! :artifact-header-keyset))
                          [(bit-set seen slot) (assoc out slot v)]))))
                [0 [nil nil]] (pairs! artifact nil))]
    (when-not (= 3 seen) (reject! :artifact-header-keyset))
    (when-not (and (native-keyword? kind) (identical? artifact-kind kind))
      (reject! :artifact-header-kind))
    (supported-version! version)))

(defn require-matching-artifact-version!
  "An early dispatch check only; full typed validation/authentication still follows."
  [artifact selected-version]
  (supported-version! selected-version)
  (let [observed (artifact-version! artifact)]
    (when-not (== (.longValue ^Long selected-version) (.longValue ^Long observed))
      (reject! :mixed-c11-interface-versions)))
  nil)

(defn reject-recognized-v2-on-legacy!
  "Keep malformed legacy inputs on their original owning validation path.
   Only a safely recognized v2 discriminant is an early dispatch rejection."
  [artifact]
  (let [observed (try (artifact-version! artifact)
                      (catch clojure.lang.ExceptionInfo _ nil))]
    (when (and (some? observed) (== 2 (.longValue ^Long observed)))
      (reject! :mixed-c11-interface-versions)))
  nil)
