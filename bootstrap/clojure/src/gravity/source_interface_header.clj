(ns gravity.source-interface-header
  "Shared exact-native header mechanics; no source or report admission.")

(def legacy-version 1)
(def ^:private current-version 2)
(def ^:private interface-id :gravity/c11-mir)

(defn reject! [reason]
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

(defn pairs! [value closed-count]
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

(defn native-keyword? [x] (identical? clojure.lang.Keyword (class x)))
(defn supported-version! [x]
  (when-not (and (identical? Long (class x))
                 (or (== 1 (.longValue ^Long x))
                     (== 2 (.longValue ^Long x))))
    (reject! :interface-version-class-or-value))
  x)

(defn- native-source-header-version! [header expected-interface]
  ;; This shared parser selects only the two built-in source-interface names.
  ;; It has no caller callback, source pin or verifier authority argument.
  (when-not (and (native-keyword? expected-interface)
                 (or (identical? :gravity/c11-mir expected-interface)
                     (identical? :gravity/c6-c10 expected-interface)))
    (reject! :interface-id))
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
    (when-not (and (native-keyword? interface) (identical? expected-interface interface))
      (reject! :interface-id))
    version))

(defn interface-version!
  "Parse the existing explicit version2 selector; generic owners stay on 1/2."
  ([header] (interface-version! header interface-id))
  ([header expected-interface]
  (let [version (native-source-header-version! header expected-interface)]
    (supported-version! version)
    (when-not (== current-version (.longValue ^Long version))
      (reject! :explicit-interface-version))
    current-version)))

(defn supported-source-version!
  "Closed C6/C11 source discriminants only, not expression-owner versions."
  [x]
  (when-not (and (identical? Long (class x))
                 (or (== 1 (.longValue ^Long x))
                     (== 2 (.longValue ^Long x))
                     (== 3 (.longValue ^Long x))))
    (reject! :interface-version-class-or-value))
  x)

(defn source-interface-version!
  "Explicit C6/C11 source2/3 DATA selection; no source/admission authority."
  ([header] (source-interface-version! header interface-id))
  ([header expected-interface]
   (let [version (native-source-header-version! header expected-interface)]
     (supported-source-version! version)
     (when (== 1 (.longValue ^Long version))
       (reject! :explicit-interface-version))
     version)))
