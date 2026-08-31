

(defn p15-s23-c6c10-project-authenticated-literal-scalars
  [source-path literal-authentication projection-field value]
  (when-not (contains? p15-s23-c6c10-numeric-projection-fields
                       projection-field)
    (p15-s23-c6c10-host-fail!
     "C6-ORIGIN" source-path
     :typed-authenticated-numeric-projection-field
     {:projection-field projection-field}))
  (let [expected-occurrences
        (or (get-in literal-authentication [:occurrences projection-field]) {})
        consumed (atom #{})]
    (letfn [(project [path item]
              (if-let [descriptor
                       (p15-s23-c6c10-literal-scalar-descriptor item)]
                (if-let [occurrence (get expected-occurrences path)]
                  (if (= descriptor (:descriptor occurrence))
                    (do
                      (swap! consumed conj path)
                      (assoc descriptor
                             :projection-field projection-field
                             :literal-evidence
                             [(select-keys (:evidence occurrence)
                                           [:literal-id :form-id :token-id
                                            :kind :raw])]))
                    (p15-s23-c6c10-host-fail!
                     "C6-ORIGIN" source-path
                     :numeric-host-value-bound-to-fresh-c2-literal
                     {:descriptor descriptor
                      :projection-field projection-field
                      :projected-path path
                      :expected-descriptor
                      (:descriptor occurrence)}))
                  (p15-s23-c6c10-host-fail!
                   "C6-ORIGIN" source-path
                   :numeric-host-value-bound-to-fresh-c2-literal
                   {:descriptor descriptor
                    :projection-field projection-field
                    :projected-path path}))
                (cond
                  (record? item)
                  (p15-s23-c6c10-host-fail!
                   "C6-VERIFY" source-path
                   :record-free-private-digest-projection
                   {:class (.getName (class item))})
                  (map? item)
                  (into
                   {}
                   (map-indexed
                    (fn [index [key child]]
                      [(project (conj path [:map-key index]) key)
                       (project (p15-s23-c6c10-map-value-path
                                 path index key)
                                child)])
                    (sort-by (comp p15-s23-c6c10-host-order-key key)
                             item)))
                  (vector? item)
                  (mapv (fn [index child]
                          (project (conj path index) child))
                        (range) item)
                  (set? item)
                  (into #{}
                        (map-indexed
                         (fn [index child]
                           (project (conj path [:set-item index]) child))
                         (sort-by p15-s23-c6c10-host-order-key item)))
                  (list? item)
                  (apply list
                         (map (fn [index child]
                                (project (conj path index) child))
                              (range) item))
                  :else item)))]
      (let [projected (project [] value)
            expected-paths (set (keys expected-occurrences))]
        (when-not (= expected-paths @consumed)
          (p15-s23-c6c10-host-fail!
           "C6-ORIGIN" source-path
           :exhaustive-authenticated-numeric-occurrences
           {:projection-field projection-field
            :expected-paths expected-paths
            :consumed-paths @consumed
            :missing-paths (set/difference expected-paths @consumed)
            :unexpected-paths (set/difference @consumed expected-paths)}))
        projected))))

(defn p15-s23-c6c10-authenticated-semantic-digest
  [source-path literal-authentication projection-field value]
  (p15-s23-c6c10-canonical-digest
   source-path
   (p15-s23-c6c10-project-authenticated-literal-scalars
    source-path literal-authentication projection-field value)))

(def p15-s23-c6c10-front-end-projection-keys
  [:artifact :status :source-unit-record :source-unit-id
   :token-stream :form-tree :top-level-form-ids :syntax-seed-stream
   :reader-source-map :literal-decoding-records
   :semantic-error-deferment-record :reader-extension-invocation-records
   :reader-diagnostics :incremental-reader-hashes
   :reader-product-integrity :c3-artifact-id :c3-syntax-object-stream
   :c3-capability-proof :records :forms])

(def p15-s23-c6c10-source-unit-projection-keys
  [:artifact :encoding :reader-options :bytes-hash
   :extension-policy :enabled-features])

(defn p15-s23-c6c10-private-span
  [private-source-id span]
  (if (map? span)
    (cond-> (dissoc span :source)
      (contains? span :file) (assoc :file private-source-id))
    span))

(defn p15-s23-c6c10-private-origin
  [private-source-id origin]
  (if (map? origin)
    (let [origin (p15-s23-c6c10-path-neutral-value
                  private-source-id origin)]
      (cond-> (dissoc origin :source-path :path)
        (contains? origin :source-id)
        (assoc :source-id private-source-id)
        (= :reader (get-in origin [:producer :kind]))
        (update :producer
                (fn [producer]
                  (cond-> (dissoc producer :identity)
                    (contains? producer :source-id)
                    (assoc :source-id private-source-id))))
        (contains? origin :span)
        (update :span #(p15-s23-c6c10-private-span
                        private-source-id %))
        (contains? origin :source-span)
        (update :source-span #(p15-s23-c6c10-private-span
                               private-source-id %))
        (contains? origin :from)
        (update :from #(p15-s23-c6c10-private-span
                        private-source-id %))))
    origin))

(defn p15-s23-c6c10-private-token-record
  [private-source-id token]
  (let [token (p15-s23-c6c10-path-neutral-value private-source-id token)]
    (cond-> (dissoc token :source-path)
      (contains? token :source-id) (assoc :source-id private-source-id)
      (contains? token :span)
      (update :span #(p15-s23-c6c10-private-span private-source-id %)))))

(defn p15-s23-c6c10-private-form-record
  [private-source-id form]
  (let [form (p15-s23-c6c10-path-neutral-value private-source-id form)]
    (cond-> (dissoc form :source-path)
      (contains? form :source-id) (assoc :source-id private-source-id)
      (contains? form :span)
      (update :span #(p15-s23-c6c10-private-span private-source-id %))
      (contains? form :surface-span)
      (update :surface-span #(p15-s23-c6c10-private-span
                              private-source-id %))
      (contains? form :origin)
      (update :origin #(p15-s23-c6c10-private-origin
                        private-source-id %))
      (contains? form :generated-origin)
      (update :generated-origin
              #(mapv (fn [origin]
                       (p15-s23-c6c10-private-origin
                        private-source-id origin))
                     (or % []))))))

(defn p15-s23-c6c10-private-syntax-seed
  [private-source-id seed]
  (let [seed (p15-s23-c6c10-path-neutral-value private-source-id seed)]
    (cond-> seed
      (contains? seed :span)
      (update :span #(p15-s23-c6c10-private-span private-source-id %))
      (contains? seed :generated-origin)
      (update :generated-origin
              #(mapv (fn [origin]
                       (p15-s23-c6c10-private-origin
                        private-source-id origin))
                     (or % []))))))

(defn p15-s23-c6c10-private-reader-source-record
  [private-source-id record]
  (let [record (p15-s23-c6c10-path-neutral-value private-source-id record)]
    (cond-> (dissoc record :source-path)
      (contains? record :source-id) (assoc :source-id private-source-id)
      (contains? record :span)
      (update :span #(p15-s23-c6c10-private-span private-source-id %)))))

(defn p15-s23-c6c10-private-literal-record
  [private-source-id record]
  (let [record (p15-s23-c6c10-path-neutral-value private-source-id record)]
    (cond-> record
      (contains? record :source-id) (assoc :source-id private-source-id)
      (contains? record :span)
      (update :span #(p15-s23-c6c10-private-span private-source-id %)))))

(defn p15-s23-c6c10-private-extension-record
  [private-source-id record]
  (let [record (p15-s23-c6c10-path-neutral-value private-source-id record)]
    (cond-> (dissoc record :source-path)
      (contains? record :source-id) (assoc :source-id private-source-id)
      (contains? record :span)
      (update :span #(p15-s23-c6c10-private-span private-source-id %)))))

(defn p15-s23-c6c10-private-diagnostic
  [private-source-id diagnostic]
  (let [diagnostic (p15-s23-c6c10-path-neutral-value
                    private-source-id diagnostic)]
    (cond-> diagnostic
      (contains? diagnostic :source-span)
      (update :source-span #(p15-s23-c6c10-private-span
                             private-source-id %))
      (get-in diagnostic [:primary :span])
      (update-in [:primary :span]
                 #(p15-s23-c6c10-private-span private-source-id %))
      (contains? diagnostic :related)
      (update :related
              #(mapv (fn [related]
                       (cond-> related
                         (contains? related :span)
                         (update :span
                                 (fn [span]
                                   (p15-s23-c6c10-private-span
                                    private-source-id span)))))
                     (or % [])))
      (contains? diagnostic :origin-chain)
      (update :origin-chain
              #(mapv (fn [origin]
                       (p15-s23-c6c10-private-origin
                        private-source-id origin))
                     (or % []))))))