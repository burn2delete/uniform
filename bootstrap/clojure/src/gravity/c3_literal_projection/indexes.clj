(ns gravity.c3-literal-projection.indexes
  "Pure lookup factories and callback-identity comparison; no authentication.")

(defn form-index [artifact]
  (into {} (map (juxt :form-id identity) (:form-tree artifact))))
(defn token-index [artifact]
  (into {} (map (juxt :token-id identity) (:token-stream artifact))))

(defn- native-keyword-map? [value]
  (and (or (identical? clojure.lang.PersistentArrayMap (class value))
           (identical? clojure.lang.PersistentHashMap (class value)))
       (let [entries (.iterator (.entrySet ^java.util.Map value))]
         (loop []
           (if (.hasNext entries)
             (and (identical? clojure.lang.Keyword
                              (class (.getKey ^java.util.Map$Entry (.next entries))))
                  (recur))
             true)))))
(defn operations-current? [actual expected]
  (and (native-keyword-map? actual) (native-keyword-map? expected)
       (== (.count ^clojure.lang.Counted actual) (.count ^clojure.lang.Counted expected))
       (let [entries (.iterator (.entrySet ^java.util.Map expected))]
         (loop []
           (if (.hasNext entries)
             (let [entry ^java.util.Map$Entry (.next entries) key (.getKey entry)]
               (and (identical? clojure.lang.Keyword (class key))
                    (identical? (.getValue entry) (.valAt ^clojure.lang.ILookup actual key))
                    (recur)))
             true)))))
