(ns gravity.c3-literal-projection.spans
  "Compare retained Reader seed spans; the caller still authenticates products.")

(defn- stream-entry [artifact]
  (when (or (identical? clojure.lang.PersistentArrayMap (class artifact))
            (identical? clojure.lang.PersistentHashMap (class artifact)))
    (let [entries (.iterator (.entrySet ^java.util.Map artifact))]
      (loop []
        (when (.hasNext entries)
          (let [entry ^java.util.Map$Entry (.next entries)]
            (if (identical? :syntax-seed-stream (.getKey entry)) entry (recur))))))))

(defn expected-seed-span [seed form-record artifact]
  (when form-record
    (let [index (get-in seed [:span :form-index])
          original (assoc (:span form-record) :form-index index)
          entry (stream-entry artifact)]
      (if-not entry
        original
        (let [seeds (.getValue ^java.util.Map$Entry entry)]
          (when (and (vector? seeds) (integer? index) (<= 0 index) (< index (count seeds)))
            (let [retained (nth seeds index) span (:span retained)
                  scalar-keys [:scalar-start :scalar-end]
                  explicit? (and (map? span) (some #(contains? span %) scalar-keys))]
              (when (and (map? span)
                         (some? (:syntax-id retained))
                         (= (:syntax-id seed) (:syntax-id retained))
                         (= (:form-id form-record) (:form-id retained))
                         (= (apply dissoc original scalar-keys) (apply dissoc span scalar-keys))
                         (every? (fn [key] (or (not (contains? original key))
                                               (= (get original key) (get span key)))) scalar-keys)
                         (or (not explicit?)
                             (and (integer? (:scalar-start span)) (integer? (:scalar-end span))
                                  (<= 0 (:scalar-start span) (:scalar-end span)))))
                span))))))))
