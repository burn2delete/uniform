

(defn p15-s23-c6c10-digest-ref-shape?
  "Recognize only the reserved one-key request reference shape.  Maps that
  merely contain :digest-ref alongside another key remain ordinary semantic
  maps and are traversed normally."
  [value]
  (and (map? value)
       (= #{:digest-ref} (set (keys value)))))

(defn p15-s23-c6c10-exact-digest-ref-ordinal!
  [source-path value request-count consumer-ordinal]
  (when-not (p15-s23-c6c10-digest-ref-shape? value)
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :exact-one-key-digest-reference
     {:observed value}))
  (let [ordinal (:digest-ref value)]
    (when-not (and (integer? ordinal)
                   (<= 0 ordinal)
                   (< ordinal request-count)
                   (or (nil? consumer-ordinal)
                       (< ordinal consumer-ordinal)))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :bounded-prior-digest-reference
       {:digest-ref ordinal
        :consumer-ordinal consumer-ordinal
        :request-count request-count}))
    ordinal))

(defn p15-s23-c6c10-collect-digest-ref-ordinals!
  [source-path value request-count consumer-ordinal]
  (letfn [(collect [item ordinals]
            (cond
              (p15-s23-c6c10-digest-ref-shape? item)
              (conj ordinals
                    (p15-s23-c6c10-exact-digest-ref-ordinal!
                     source-path item request-count consumer-ordinal))

              (map? item)
              (reduce (fn [result [key child]]
                        (collect child (collect key result)))
                      ordinals
                      item)

              (or (vector? item) (set? item) (list? item))
              (reduce (fn [result child]
                        (collect child result))
                      ordinals
                      item)

              :else ordinals))]
    (collect value [])))

(defn p15-s23-c6c10-canonical-identity
  [source-path value]
  (p15-s23-c6c10-canonical-form* source-path value))

(defn p15-s23-c6c10-unique-resolved-values!
  [source-path missing-fact values]
  (let [canonical-identities
        (mapv #(p15-s23-c6c10-canonical-identity source-path %) values)]
    ;; Canonical identity separates vector/list and every scalar type.  The
    ;; second check is also required because Clojure maps and sets would merge
    ;; certain canonically distinct but host-equal values during construction.
    (when-not (and (= (count values)
                      (count (distinct canonical-identities)))
                   (= (count values)
                      (count (distinct values))))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path missing-fact
       {:value-count (count values)
        :canonical-identity-count
        (count (distinct canonical-identities))
        :host-equality-count (count (distinct values))})))
  values)

(defn p15-s23-c6c10-resolve-digest-references!
  [source-path value request-count consumer-ordinal resolved-digests]
  (when-not
   (and (vector? resolved-digests)
        (= (count resolved-digests)
           (if (nil? consumer-ordinal)
             request-count
             consumer-ordinal))
        (every? #(and (string? %)
                      (boolean (re-matches #"sha256:[0-9a-f]{64}" %)))
                resolved-digests))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :exact-prior-resolved-digest-vector
     {:request-count request-count
      :consumer-ordinal consumer-ordinal
      :resolved-digest-count
      (when (vector? resolved-digests) (count resolved-digests))}))
  (letfn [(resolve-value [item]
            (cond
              (p15-s23-c6c10-digest-ref-shape? item)
              (let [ordinal
                    (p15-s23-c6c10-exact-digest-ref-ordinal!
                     source-path item request-count consumer-ordinal)
                    digest (get resolved-digests ordinal ::missing)]
                (when (= ::missing digest)
                  (p15-s23-c6c10-host-fail!
                   "C6-VERIFY" source-path :resolved-prior-digest-reference
                   {:digest-ref ordinal
                    :consumer-ordinal consumer-ordinal}))
                digest)

              (map? item)
              (let [entries
                    (mapv (fn [[key child]]
                            [(resolve-value key) (resolve-value child)])
                          item)
                    keys (mapv first entries)]
                (p15-s23-c6c10-unique-resolved-values!
                 source-path :collision-free-resolved-map-keys keys)
                (into {} entries))

              (vector? item)
              (mapv resolve-value item)

              (set? item)
              (let [items (mapv resolve-value item)]
                (p15-s23-c6c10-unique-resolved-values!
                 source-path :collision-free-resolved-set-items items)
                (into #{} items))

              (list? item)
              (apply list (map resolve-value item))

              :else item))]
    (resolve-value value)))

(def p15-s23-c6c10-accepted-builder-result-keys
  #{:artifact-template :authority :diagnostics :digest-graph-root
    :digest-graph-roots :digest-requests :status})

(def p15-s23-c6c10-rejected-builder-result-keysets
  #{#{:artifact-template :containment :diagnostics :digest-graph-roots
      :digest-requests :fatal? :status}
    #{:artifact-template :containment :diagnostics :digest-graph-roots
      :digest-requests :fatal? :propagated-upstream? :status}})

(defn p15-s23-c6c10-validate-builder-result-canonical-carrier!
  [source-path raw-result]
  ;; Validate native classes and cycles before looking up any result fields.
  (p15-s23-c6c10-canonical-form* source-path raw-result)
  (let [requests (when (map? raw-result) (:digest-requests raw-result))]
    (when-not (and (map? raw-result) (vector? requests))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :digest-request-vector
       {:request-count (when (vector? requests) (count requests))})))
  {:status :passed})

(defn p15-s23-c6c10-validate-builder-result-envelope!
  [source-path raw-result]
  ;; This establishes metadata-free canonical native carriers and rejects cycles before
  ;; any recursive graph traversal below.
  (p15-s23-c6c10-validate-builder-result-canonical-carrier!
   source-path raw-result)
  (let [status (:status raw-result)
        keyset (when (map? raw-result) (set (keys raw-result)))
        sub? (= :checked-i64-sub-v1 (get-in raw-result [:artifact-template :scope :family]))
        mul? (= :checked-i64-mul-v1 (get-in raw-result [:artifact-template :scope :family]))]
    (when-not
     (and (map? raw-result)
          (contains? #{:accepted :rejected} status)
          (if (= :accepted status)
            (and (= p15-s23-c6c10-accepted-builder-result-keys keyset)
                 (map? (:artifact-template raw-result))
                 (vector? (:diagnostics raw-result))
                 (empty? (:diagnostics raw-result))
                 (or
                  (and (= {:semantic-owner :gravity-source
                           :host-role :generic-validation-hashing-and-instantiation
                           :scope :bounded-pure-c6-c10 :self-hosted? false}
                          (:authority raw-result))
                       (not (contains? #{:checked-i64-add-v1 :checked-i64-sub-v1 :checked-i64-mul-v1}
                              (get-in raw-result [:artifact-template :scope :family]))))
                  (and (= {:semantic-owner :gravity-source
                           :host-role :generic-validation-hashing-and-instantiation
                           :scope (if mul? :bounded-checked-i64-mul-c6-c10
                                    (if sub? :bounded-checked-i64-sub-c6-c10 :bounded-checked-i64-add-c6-c10)) :self-hosted? false}
                          (:authority raw-result))
                       (= [:gravity/p15-s23-stage2-gravity-checked-core-artifact 1
                           (if mul? :complete-for-checked-i64-mul-hosted-safe-jvm-slice
                             (if sub? :complete-for-checked-i64-sub-hosted-safe-jvm-slice
                               :complete-for-checked-i64-add-hosted-safe-jvm-slice)) :hosted :jvm]
                          ((juxt :kind :schema-version :status :profile :source-target) (:artifact-template raw-result)))
                       (= {:artifact :gravity/c6-c10-checked-core-scope-contract
                           :family (if mul? :checked-i64-mul-v1 (if sub? :checked-i64-sub-v1 :checked-i64-add-v1))
                           :profiles [:hosted] :safety-modes [:safe] :source-targets [:jvm]
                           :source-operations [:parameter :local (if mul? :integer-mul (if sub? :integer-sub :integer-add)) :function]
                           :scalar-literals [] :intrinsic-effects #{:error/raise} :intrinsic-capabilities #{}
                           :ownership :persistent-immutable :safety :runtime-checked :whole-language? false}
                          (get-in raw-result [:artifact-template :scope])))))
            (and (contains?
                  p15-s23-c6c10-rejected-builder-result-keysets keyset)
                 (true? (:fatal? raw-result))
                 (nil? (:artifact-template raw-result))
                 (vector? (:diagnostics raw-result))
                 (not (empty? (:diagnostics raw-result)))
                 (map? (:containment raw-result)))))
     (p15-s23-c6c10-host-fail!
      "C6-VERIFY" source-path :exact-gravity-builder-result-envelope
      {:status status :keys keyset}))
    raw-result))

(defn p15-s23-c6c10-request-graph-reachable-ordinals
  [requests root-ordinals]
  (loop [pending (vec root-ordinals)
         reachable #{}]
    (if (empty? pending)
      reachable
      (let [ordinal (peek pending)
            pending (pop pending)]
        (if (contains? reachable ordinal)
          (recur pending reachable)
          (recur (into pending (:depends-on (get requests ordinal)))
                 (conj reachable ordinal)))))))
