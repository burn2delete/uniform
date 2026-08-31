

(defn p15-s23-c6c10-exact-digest-ref-present?
  [value]
  (letfn [(present? [item]
            (cond
              (p15-s23-c6c10-digest-ref-shape? item) true
              (map? item)
              (boolean
               (some (fn [[key child]]
                       (or (present? key) (present? child)))
                     item))
              (or (vector? item) (set? item) (list? item))
              (boolean (some present? item))
              :else false))]
    (present? value)))

(defn p15-s23-c6c10-structure-kind
  [value]
  (cond
    (map? value) :map
    (vector? value) :vector
    (list? value) :list
    (set? value) :set
    :else :scalar))

(declare p15-s23-c6c10-strict-first-mismatch)

(defn p15-s23-c6c10-strict-map-index
  [source-path value]
  (into {}
        (map (fn [[key child]]
               [(p15-s23-c6c10-canonical-identity source-path key)
                [key child]]))
        value))

(defn p15-s23-c6c10-strict-first-mismatch
  [source-path expected actual path]
  (let [expected-kind (p15-s23-c6c10-structure-kind expected)
        actual-kind (p15-s23-c6c10-structure-kind actual)]
    (cond
      (not= expected-kind actual-kind)
      {:path path :expected-kind expected-kind :actual-kind actual-kind}

      (= :map expected-kind)
      (let [expected-index
            (p15-s23-c6c10-strict-map-index source-path expected)
            actual-index
            (p15-s23-c6c10-strict-map-index source-path actual)
            expected-keys (set (keys expected-index))
            actual-keys (set (keys actual-index))]
        (if (not= expected-keys actual-keys)
          {:path path
           :expected-kind :map
           :actual-kind :map
           :missing-key-count (count (remove actual-keys expected-keys))
           :unexpected-key-count (count (remove expected-keys actual-keys))}
          (let [ordered-identities
                (->> expected-keys
                     (mapv (fn [identity]
                             {:identity identity
                              :sort-key
                              (p15-s23-c6c10-canonical-sort-key identity)}))
                     (sort-by :sort-key)
                     (mapv :identity))]
            (some
             (fn [identity]
               (let [[key expected-child] (get expected-index identity)
                     [_ actual-child] (get actual-index identity)]
                 (p15-s23-c6c10-strict-first-mismatch
                  source-path expected-child actual-child
                  (conj path
                        [:map-key
                         (p15-s23-c6c10-canonical-digest
                          source-path key)]))))
             ordered-identities))))

      (contains? #{:vector :list} expected-kind)
      (if (not= (count expected) (count actual))
        {:path path
         :expected-kind expected-kind
         :actual-kind actual-kind
         :expected-count (count expected)
         :actual-count (count actual)}
        (some identity
              (map-indexed
               (fn [index [expected-child actual-child]]
                 (p15-s23-c6c10-strict-first-mismatch
                  source-path expected-child actual-child
                  (conj path index)))
               (map vector expected actual))))

      (= :set expected-kind)
      (let [expected-items
            (set (map #(p15-s23-c6c10-canonical-identity
                        source-path %) expected))
            actual-items
            (set (map #(p15-s23-c6c10-canonical-identity
                        source-path %) actual))]
        (when (not= expected-items actual-items)
          {:path path
           :expected-kind :set
           :actual-kind :set
           :expected-count (count expected)
           :actual-count (count actual)}))

      :else
      (when-not (= (p15-s23-c6c10-canonical-identity
                    source-path expected)
                   (p15-s23-c6c10-canonical-identity
                    source-path actual))
        {:path path :expected-kind :scalar :actual-kind :scalar}))))

(defn p15-s23-c6c10-strict-structure!
  [source-path expected actual missing-fact]
  (let [expected-record
        (p15-s23-c6c10-canonical-record source-path expected)
        actual-record
        (p15-s23-c6c10-canonical-record source-path actual)
        mismatch
        (p15-s23-c6c10-strict-first-mismatch
         source-path expected actual [])]
    (when (or mismatch (not= (:form expected-record) (:form actual-record)))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path missing-fact
       (or mismatch {:path [] :expected-kind :canonical
                     :actual-kind :canonical})))
    {:status :passed
     :canonical-digest
     (p15-s23-c6c10-canonical-digest source-path expected)}))

(defn p15-s23-c6c10-canonical-identical?
  [source-path left right]
  (= (p15-s23-c6c10-canonical-identity source-path left)
     (p15-s23-c6c10-canonical-identity source-path right)))

(defn p15-s23-c6c10-rehydrate-candidate-template!
  [source-path expected-raw expected-sealed candidate-sealed
   resolved-digests]
  (let [request-count (count resolved-digests)
        resolve-complete
        (fn [value]
          (p15-s23-c6c10-resolve-digest-references!
           source-path value request-count nil resolved-digests))]
    (letfn
     [(rehydrate [raw sealed candidate]
        (cond
          (p15-s23-c6c10-digest-ref-shape? raw)
          (if (p15-s23-c6c10-canonical-identical?
               source-path sealed candidate)
            raw
            candidate)

          (and (map? raw) (map? sealed) (map? candidate))
          (let [raw-index
                (into {}
                      (map (fn [[raw-key raw-child]]
                             [(p15-s23-c6c10-canonical-identity
                               source-path (resolve-complete raw-key))
                              [raw-key raw-child]]))
                      raw)
                sealed-index
                (p15-s23-c6c10-strict-map-index source-path sealed)
                entries
                (mapv
                 (fn [[candidate-key candidate-child]]
                   (let [identity
                         (p15-s23-c6c10-canonical-identity
                          source-path candidate-key)]
                     (if (and (contains? raw-index identity)
                              (contains? sealed-index identity))
                       (let [[raw-key raw-child] (get raw-index identity)
                             [sealed-key sealed-child]
                             (get sealed-index identity)]
                         [(rehydrate raw-key sealed-key candidate-key)
                          (rehydrate raw-child sealed-child
                                     candidate-child)])
                       [candidate-key candidate-child])))
                 candidate)
                keys (mapv first entries)]
            (p15-s23-c6c10-unique-resolved-values!
             source-path :collision-free-rehydrated-map-keys keys)
            (into {} entries))

          (and (contains? #{:vector :list}
                          (p15-s23-c6c10-structure-kind raw))
               (contains? #{:vector :list}
                          (p15-s23-c6c10-structure-kind sealed))
               (contains? #{:vector :list}
                          (p15-s23-c6c10-structure-kind candidate))
               (= (count raw) (count sealed) (count candidate)))
          (let [values
                (mapv (fn [[raw-child sealed-child candidate-child]]
                        (rehydrate raw-child sealed-child candidate-child))
                      (map vector raw sealed candidate))]
            (if (list? candidate) (apply list values) values))

          (and (set? raw) (set? sealed) (set? candidate))
          (let [raw-index
                (into {}
                      (map (fn [raw-item]
                             [(p15-s23-c6c10-canonical-identity
                               source-path (resolve-complete raw-item))
                              raw-item]))
                      raw)
                sealed-index
                (into {}
                      (map (fn [sealed-item]
                             [(p15-s23-c6c10-canonical-identity
                               source-path sealed-item)
                              sealed-item]))
                      sealed)
                items
                (mapv
                 (fn [candidate-item]
                   (let [identity
                         (p15-s23-c6c10-canonical-identity
                          source-path candidate-item)]
                     (if (and (contains? raw-index identity)
                              (contains? sealed-index identity))
                       (rehydrate (get raw-index identity)
                                  (get sealed-index identity)
                                  candidate-item)
                       candidate-item)))
                 candidate)]
            (p15-s23-c6c10-unique-resolved-values!
             source-path :collision-free-rehydrated-set-items items)
            (into #{} items))

          :else candidate))]
      (rehydrate expected-raw expected-sealed candidate-sealed))))