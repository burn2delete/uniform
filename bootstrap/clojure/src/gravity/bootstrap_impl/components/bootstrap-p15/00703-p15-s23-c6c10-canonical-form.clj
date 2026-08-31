

(defn p15-s23-c6c10-canonical-form*
  [source-path stats depth value]
  (swap! stats update :nodes inc)
  (swap! stats update :maximum-depth max depth)
  (when (or (> (:nodes @stats) p15-s23-c6c10-max-carrier-nodes)
            (> depth p15-s23-c6c10-max-carrier-depth))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :bounded-canonical-carrier
     (select-keys @stats [:nodes :maximum-depth])))
  (when (and (instance? clojure.lang.IObj value) (some? (meta value)))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :metadata-free-canonical-value
     {:value-kind (some-> value class .getName)}))
  (cond
    (nil? value) [:nil]
    (boolean? value) [:boolean value]
    (integer? value)
    (do (p15-s23-c6c10-bounded-integer! source-path stats value)
        [:integer (.toString (biginteger value))])
    (ratio? value)
    (let [numerator (numerator value)
          denominator (denominator value)]
      (p15-s23-c6c10-bounded-integer! source-path stats numerator)
      (p15-s23-c6c10-bounded-integer! source-path stats denominator)
      [:ratio
       (.toString (biginteger numerator))
       (.toString (biginteger denominator))])
    (instance? java.math.BigDecimal value)
    (let [decimal ^java.math.BigDecimal value
          unscaled (.unscaledValue decimal)
          scale (long (.scale decimal))]
      (p15-s23-c6c10-bounded-integer! source-path stats unscaled)
      (when (> (Math/abs scale) 65536)
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :bounded-decimal-scale
         {:scale scale :maximum-absolute-scale 65536}))
      [:decimal (.toString unscaled) scale])
    (string? value)
    (do (p15-s23-c6c10-bounded-string-bytes! source-path stats value)
        [:string value])
    (char? value)
    (let [code (int value)]
      (when (<= 0xD800 code 0xDFFF)
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :unicode-scalar-character {:code code}))
      [:character code])
    (keyword? value)
    (let [namespace (namespace value)
          name (name value)]
      (when-not (and (p15-s23-c6c10-valid-named-component? name)
                     (or (nil? namespace)
                         (p15-s23-c6c10-valid-named-component? namespace)))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :canonical-keyword-components
         {:namespace namespace :name name}))
      (when namespace
        (p15-s23-c6c10-bounded-string-bytes! source-path stats namespace))
      (p15-s23-c6c10-bounded-string-bytes! source-path stats name)
      [:keyword namespace name])
    (symbol? value)
    (let [namespace (namespace value)
          name (name value)]
      (when-not (or (and (nil? namespace) (= "/" name))
                    (and (p15-s23-c6c10-valid-named-component? name)
                         (or (nil? namespace)
                             (p15-s23-c6c10-valid-named-component?
                              namespace))))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :canonical-symbol-components
         {:namespace namespace :name name}))
      (when namespace
        (p15-s23-c6c10-bounded-string-bytes! source-path stats namespace))
      (p15-s23-c6c10-bounded-string-bytes! source-path stats name)
      [:symbol namespace name])
    (record? value)
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :record-free-canonical-value
     {:class (.getName (class value))})
    (vector? value)
    (do
      (when-not (contains? p15-s23-c6c10-canonical-vector-classes
                           (.getName (class value)))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :exact-persistent-vector-class
         {:class (.getName (class value))}))
      (when (> (count value) p15-s23-c6c10-max-container-width)
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :maximum-container-width
         {:observed-width (count value)}))
      (swap! stats update :maximum-width max (count value))
      [:vector
       (p15-s23-c6c10-canonical-sequence source-path stats depth value)])
    (map? value)
    (do
      (when-not (contains? p15-s23-c6c10-canonical-map-classes
                           (.getName (class value)))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :exact-persistent-map-class
         {:class (.getName (class value))}))
      (when (> (count value) p15-s23-c6c10-max-container-width)
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :maximum-container-width
         {:observed-width (count value)}))
      (swap! stats update :maximum-width max (count value))
      (let [entries
            (mapv (fn [[key child]]
                    (let [encoded-key
                          (p15-s23-c6c10-canonical-form*
                           source-path stats (inc depth) key)
                          key-sort
                          (p15-s23-c6c10-canonical-sort-key encoded-key)
                          encoded-child
                          (p15-s23-c6c10-canonical-form*
                           source-path stats (inc depth) child)
                          entry
                          [:entry encoded-key encoded-child]]
                      {:key encoded-key
                       :key-sort key-sort
                       :entry entry}))
                  value)
            key-sorts (mapv :key-sort entries)
            ordered (mapv :entry
                          (sort-by :key-sort entries))]
        (when-not (= (count key-sorts) (count (distinct key-sorts)))
          (p15-s23-c6c10-host-fail!
           "C6-VERIFY" source-path :unique-canonical-map-keys {}))
        [:map ordered]))
    (set? value)
    (do
      (when-not (contains? p15-s23-c6c10-canonical-set-classes
                           (.getName (class value)))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :exact-persistent-set-class
         {:class (.getName (class value))}))
      (when (> (count value) p15-s23-c6c10-max-container-width)
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :maximum-container-width
         {:observed-width (count value)}))
      (swap! stats update :maximum-width max (count value))
      (let [items (p15-s23-c6c10-canonical-sequence
                   source-path stats depth value)
            items-with-sorts
            (mapv (fn [item]
                    {:item item
                     :sort-key
                     (p15-s23-c6c10-canonical-sort-key item)})
                  items)
            sort-keys (mapv :sort-key items-with-sorts)
            ordered (mapv :item (sort-by :sort-key items-with-sorts))]
        (when-not (= (count sort-keys) (count (distinct sort-keys)))
          (p15-s23-c6c10-host-fail!
           "C6-VERIFY" source-path :unique-canonical-set-items {}))
        [:set ordered]))
    (list? value)
    (do
      (when-not (contains? p15-s23-c6c10-canonical-list-classes
                           (.getName (class value)))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :exact-persistent-list-class
         {:class (.getName (class value))}))
      (when (> (count value) p15-s23-c6c10-max-container-width)
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :maximum-container-width
         {:observed-width (count value)}))
      (swap! stats update :maximum-width max (count value))
      [:list
       (p15-s23-c6c10-canonical-sequence source-path stats depth value)])
    :else
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :canonical-value-domain
     {:class (some-> value class .getName)})))

(defn p15-s23-c6c10-canonical-record
  ([value]
   (p15-s23-c6c10-canonical-record "<c6-c10-canonical>" value))
  ([source-path value]
   (let [stats (atom {:nodes 0 :maximum-depth 0 :maximum-width 0
                      :scalar-bytes 0 :maximum-scalar-bytes 0
                      :maximum-integer-bits 0})
         form (p15-s23-c6c10-canonical-form* source-path stats 0 value)
         text
         (binding [*print-length* nil
                   *print-level* nil
                   *print-meta* false
                   *print-dup* false
                   *print-readably* true
                   *print-namespace-maps* false]
           (pr-str [:gravity/canonical-edn-v1 form]))]
     {:form form :text text :stats @stats})))

(defn p15-s23-c6c10-canonical-digest
  ([value]
   (p15-s23-c6c10-canonical-digest "<c6-c10-canonical>" value))
  ([source-path value]
   (str "sha256:"
        (sha256-hex (:text (p15-s23-c6c10-canonical-record
                            source-path value))))))

(defn p15-s23-c6c10-class-supported-carrier?
  [value]
  (cond
    (vector? value)
    (contains? p15-s23-c6c10-canonical-vector-classes
               (.getName (class value)))
    (map? value)
    (contains? p15-s23-c6c10-canonical-map-classes
               (.getName (class value)))
    (set? value)
    (contains? p15-s23-c6c10-canonical-set-classes
               (.getName (class value)))
    (list? value)
    (contains? p15-s23-c6c10-canonical-list-classes
               (.getName (class value)))
    :else false))