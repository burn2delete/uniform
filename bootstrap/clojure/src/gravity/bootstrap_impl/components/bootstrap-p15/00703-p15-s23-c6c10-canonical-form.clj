(defn- p15-s23-c6c10-canonical-scalar-form
  [source-path stats value]
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
    :else
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :canonical-value-domain
     {:class (some-> value class .getName)})))

(defn- p15-s23-c6c10-canonical-collection-form
  [source-path kind children]
  (case kind
    :map
    (let [entries (mapv (fn [[key child]]
                          {:sort-key (p15-s23-c6c10-canonical-sort-key key)
                           :entry [:entry key child]})
                        (partition 2 children))
          sorts (mapv :sort-key entries)]
      (when-not (= (count sorts) (count (distinct sorts)))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :unique-canonical-map-keys {}))
      [:map (mapv :entry (sort-by :sort-key entries))])
    :set
    (let [items (mapv (fn [item]
                        {:sort-key (p15-s23-c6c10-canonical-sort-key item) :item item})
                      children)
          sorts (mapv :sort-key items)]
      (when-not (= (count sorts) (count (distinct sorts)))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :unique-canonical-set-items {}))
      [:set (mapv :item (sort-by :sort-key items))])
    [kind children]))

(defn p15-s23-c6c10-canonical-form*
  [source-path stats depth value]
  (let [active (java.util.IdentityHashMap.)]
    (loop [stack [[:visit value depth]] results []]
      (let [[action item depth kind size] (peek stack) stack (pop stack)]
        (if (= :finish action)
          (let [offset (- (count results) size)
                children (into [] (subvec results offset))
                form (p15-s23-c6c10-canonical-collection-form source-path kind children)]
            (.remove active item)
            (if (empty? stack) form
                (recur stack (conj (subvec results 0 offset) form))))
          (let [{:keys [kind children]} (p15-s23-c6c10-canonical-carrier source-path item)]
            (swap! stats update :nodes inc)
            (swap! stats update :maximum-depth max depth)
            (if children
              (do
                (when (.containsKey active item)
                  (p15-s23-c6c10-host-fail!
                   "C6-VERIFY" source-path :cyclic-canonical-carrier {}))
                (.put active item true)
                (swap! stats update :maximum-width max
                       (if (= :map kind) (quot (count children) 2) (count children)))
                (recur (into (conj stack [:finish item depth kind (count children)])
                             (map #(vector :visit % (inc depth)) (rseq children)))
                       results))
              (let [form (p15-s23-c6c10-canonical-scalar-form source-path stats item)]
                (if (empty? stack) form (recur stack (conj results form)))))))))))

(defn- p15-s23-c6c10-canonical-form-and-stats
  [source-path value]
  (let [stats (atom {:nodes 0 :maximum-depth 0 :maximum-width 0
                     :scalar-bytes 0 :maximum-scalar-bytes 0
                     :maximum-integer-bits 0})
        form (p15-s23-c6c10-canonical-form* source-path stats 0 value)]
    {:form form :stats @stats}))

(defn p15-s23-c6c10-canonical-record
  ([value]
   (p15-s23-c6c10-canonical-record "<c6-c10-canonical>" value))
  ([source-path value]
   (let [{:keys [form stats]}
         (p15-s23-c6c10-canonical-form-and-stats source-path value)
         text
         (binding [*print-length* nil
                   *print-level* nil
                   *print-meta* false
                   *print-dup* false
                   *print-readably* true
                   *print-namespace-maps* false]
           (pr-str [:gravity/canonical-edn-v1 form]))]
     {:form form :text text :stats stats})))

(defn- p15-s23-c6c10-digest-validated-form
  [form]
     (str "sha256:"
       (digest/sha256-output-hex
         (fn [output]
           (let [writer (java.io.OutputStreamWriter.
                         output java.nio.charset.StandardCharsets/UTF_8)]
             (try
               (binding [*out* writer *print-length* nil *print-level* nil
                         *print-meta* false *print-dup* false
                         *print-readably* true *print-namespace-maps* false]
                 (pr [:gravity/canonical-edn-v1 form]))
               (catch Throwable primary
                 (try (.close writer)
                      (catch Throwable secondary
                        (when-not (identical? primary secondary)
                          (try (.addSuppressed primary secondary)
                               (catch Throwable _ nil)))))
                 (throw primary)))
             (.close writer))))))

(defn p15-s23-c6c10-canonical-digest
  ([value]
   (p15-s23-c6c10-canonical-digest "<c6-c10-canonical>" value))
  ([source-path value]
   (p15-s23-c6c10-digest-validated-form
     (:form (p15-s23-c6c10-canonical-form-and-stats source-path value)))))

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
