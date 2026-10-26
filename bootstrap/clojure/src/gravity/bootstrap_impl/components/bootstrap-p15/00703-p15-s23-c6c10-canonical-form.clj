(defn- p15-s23-c6c10-canonical-scalar-form
  [source-path value]
  (cond
    (nil? value) [:nil]
    (boolean? value) [:boolean value]
    (integer? value)
    [:integer (.toString (biginteger value))]
    (ratio? value)
    (let [numerator (numerator value)
          denominator (denominator value)]
      [:ratio
       (.toString (biginteger numerator))
       (.toString (biginteger denominator))])
    (instance? java.math.BigDecimal value)
    (let [decimal ^java.math.BigDecimal value
          unscaled (.unscaledValue decimal)
          scale (long (.scale decimal))]
      [:decimal (.toString unscaled) scale])
    (string? value)
    (do (p15-s23-c6c10-require-unicode-string! source-path value)
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
        (p15-s23-c6c10-require-unicode-string! source-path namespace))
      (p15-s23-c6c10-require-unicode-string! source-path name)
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
        (p15-s23-c6c10-require-unicode-string! source-path namespace))
      (p15-s23-c6c10-require-unicode-string! source-path name)
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

(def ^:dynamic p15-s23-c6c10-canonical-ancestors
  "Invocation-local identity ancestors shared only within one canonical pass."
  nil)

(defn p15-s23-c6c10-canonical-form*
  [source-path value]
  (let [active (or p15-s23-c6c10-canonical-ancestors (java.util.IdentityHashMap.))]
    (binding [p15-s23-c6c10-canonical-ancestors active]
      (if (identical? clojure.lang.Keyword (class value))
        (do
          (p15-s23-c6c10-canonical-carrier source-path value)
          (p15-s23-c6c10-canonical-scalar-form source-path value))
        (let [entered (java.util.IdentityHashMap.)]
          (try
            (loop [stack [[:visit value]] results []]
              (let [[action item kind size] (peek stack) stack (pop stack)]
                (if (= :finish action)
                  (let [offset (- (count results) size)
                        children (into [] (subvec results offset))
                        form (p15-s23-c6c10-canonical-collection-form source-path kind children)]
                    (.remove active item)
                    (.remove entered item)
                    (if (empty? stack) form
                        (recur stack (conj (subvec results 0 offset) form))))
                  ;; Semantic class/metadata/comparator checks precede cycle checks.
                  (let [{:keys [kind children]} (p15-s23-c6c10-canonical-carrier source-path item)]
                    (if children
                      (do
                        (when (.containsKey active item)
                          (p15-s23-c6c10-host-fail!
                           "C6-VERIFY" source-path :cyclic-canonical-carrier {}))
                        (.put entered item true)
                        (.put active item true)
                        (recur (into (conj stack [:finish item kind (count children)])
                                     (map #(vector :visit %) (rseq children)))
                               results))
                      (let [form (p15-s23-c6c10-canonical-scalar-form source-path item)]
                        (if (empty? stack) form (recur stack (conj results form)))))))))
            (finally
              ;; Preserve markers owned by the caller, including on failed fallback.
              (doseq [item (.keySet entered)] (.remove active item)))))))))

(defn p15-s23-c6c10-canonical-record
  ([value]
   (p15-s23-c6c10-canonical-record "<c6-c10-canonical>" value))
  ([source-path value]
   (let [form (p15-s23-c6c10-canonical-form* source-path value)
         text
         (binding [*print-length* nil
                   *print-level* nil
                   *print-meta* false
                   *print-dup* false
                   *print-readably* true
                   *print-namespace-maps* false]
           (pr-str [:gravity/canonical-edn-v1 form]))]
     {:form form :text text})))

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
   ;; Hashing a newly constructed diagnostic is an independent pass, even when
   ;; its caller is rejecting a cycle under a shared ancestor binding.
   (binding [p15-s23-c6c10-canonical-ancestors nil]
     (p15-s23-c6c10-digest-validated-form
       (p15-s23-c6c10-canonical-form* source-path value)))))

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
