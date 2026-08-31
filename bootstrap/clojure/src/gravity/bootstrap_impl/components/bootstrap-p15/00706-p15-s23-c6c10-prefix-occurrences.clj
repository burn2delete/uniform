

(defn p15-s23-c6c10-prefix-occurrences
  [prefix occurrences]
  (into {}
        (map (fn [[path occurrence]]
               [(into prefix path) occurrence]))
        occurrences))

(defn p15-s23-c6c10-form-numeric-occurrence-index
  "Build all C2 form occurrence maps bottom-up.  The explicit reverse-preorder
  reduction is host-stack independent even for adversarially deep valid form
  chains; equal-valued leaves remain bound to their exact structural child."
  [source-path literal-authentication raw-front-end]
  (let [forms (vec (:form-tree raw-front-end))
        forms-by-id (:forms-by-id literal-authentication)
        evidence-by-form-id (:by-form-id literal-authentication)
        shape-fail!
        (fn [form facts]
          (p15-s23-c6c10-host-fail!
           "C6-ORIGIN" source-path
           :c2-form-structural-numeric-correspondence
           (merge {:form-id (:form-id form)
                   :form-kind (:kind form)} facts)))
        child-occurrences
        (fn [index form child-id]
          (when-not (and (contains? forms-by-id child-id)
                         (contains? index child-id))
            (shape-fail! form {:unindexed-child-form-id child-id}))
          (get index child-id))
        value-index
        (reduce
         (fn [index form]
           (let [occurrences
                 (if (p15-s23-c6c10-literal-scalar-descriptor
                      (:value form))
                   (p15-s23-c6c10-exact-numeric-occurrence
                    source-path [] (:value form)
                    (get evidence-by-form-id (:form-id form)))
                   (cond
                     (or (= :list (:kind form))
                         (= :vector (:kind form)))
                     (let [children (:children form)
                           values (vec (:value form))]
                       (when-not (= (count children) (count values))
                         (shape-fail! form
                                      {:child-count (count children)
                                       :value-count (count values)}))
                       (apply
                        p15-s23-c6c10-merge-occurrences
                        (map-indexed
                         (fn [child-index child-id]
                           (let [child (get forms-by-id child-id)]
                             (when-not (= (nth values child-index)
                                          (:value child))
                               (shape-fail!
                                form {:child-form-id child-id
                                      :child-index child-index}))
                             (p15-s23-c6c10-prefix-occurrences
                              [child-index]
                              (child-occurrences index form child-id))))
                         children)))

                     (= :map (:kind form))
                     (let [children (:children form)]
                       (when (odd? (count children))
                         (shape-fail! form
                                      {:child-count (count children)}))
                       (let [pairs
                             (sort-by
                              (fn [[key-id _]]
                                (p15-s23-c6c10-host-order-key
                                 (:value (get forms-by-id key-id))))
                              (partition 2 children))
                             entries
                             (sort-by
                              (comp p15-s23-c6c10-host-order-key key)
                              (:value form))]
                         (when-not (= (count pairs) (count entries))
                           (shape-fail! form
                                        {:pair-count (count pairs)
                                         :entry-count (count entries)}))
                         (apply
                          p15-s23-c6c10-merge-occurrences
                          (map-indexed
                           (fn [entry-index [[key-id value-id]
                                            [entry-key entry-value]]]
                             (let [key-form (get forms-by-id key-id)
                                   value-form (get forms-by-id value-id)]
                               (when-not
                                (and (= entry-key (:value key-form))
                                     (= entry-value (:value value-form)))
                                 (shape-fail!
                                  form {:entry-index entry-index
                                        :key-form-id key-id
                                        :value-form-id value-id}))
                               (p15-s23-c6c10-merge-occurrences
                                (p15-s23-c6c10-prefix-occurrences
                                 [[:map-key entry-index]]
                                 (child-occurrences index form key-id))
                                (p15-s23-c6c10-prefix-occurrences
                                 (p15-s23-c6c10-map-value-path
                                  [] entry-index entry-key)
                                 (child-occurrences index form value-id)))))
                           (map vector pairs entries)))))

                     (= :set (:kind form))
                     (let [children
                           (sort-by
                            (fn [child-id]
                              (p15-s23-c6c10-host-order-key
                               (:value (get forms-by-id child-id))))
                            (:children form))
                           values
                           (sort-by p15-s23-c6c10-host-order-key
                                    (:value form))]
                       (when-not (= (count children) (count values))
                         (shape-fail! form
                                      {:child-count (count children)
                                       :value-count (count values)}))
                       (apply
                        p15-s23-c6c10-merge-occurrences
                        (map-indexed
                         (fn [child-index [child-id value]]
                           (let [child (get forms-by-id child-id)]
                             (when-not (= value (:value child))
                               (shape-fail!
                                form {:child-form-id child-id
                                      :child-index child-index}))
                             (p15-s23-c6c10-prefix-occurrences
                              [[:set-item child-index]]
                              (child-occurrences index form child-id))))
                         (map vector children values))))

                     (= :abbreviation (:kind form))
                     (let [children (:children form)
                           values (vec (:value form))]
                       (when-not
                        (and (= 1 (count children))
                             (= 2 (count values))
                             (= (second values)
                                (:value (get forms-by-id
                                             (first children)))))
                         (shape-fail! form
                                      {:child-count (count children)
                                       :value-count (count values)}))
                       (p15-s23-c6c10-prefix-occurrences
                        [1]
                        (child-occurrences index form (first children))))

                     (= :metadata-wrapper (:kind form))
                     (let [attached-id (last (:children form))
                           attached (get forms-by-id attached-id)]
                       (when-not (and attached
                                      (= (:value form) (:value attached)))
                         (shape-fail! form
                                      {:attached-form-id attached-id}))
                       (child-occurrences index form attached-id))

                     :else
                     {}))
                 expected
                 (p15-s23-c6c10-collect-numeric-occurrences
                  [] (:value form))
                 observed (update-vals occurrences :descriptor)]
             (when-not (= expected observed)
               (shape-fail!
                form {:expected-occurrences expected
                      :observed-occurrences observed}))
             (assoc index (:form-id form) occurrences)))
         {}
         (rseq forms))
        metadata-index
        (reduce
         (fn [index form]
           (let [expected
                 (p15-s23-c6c10-collect-numeric-occurrences
                  [] (:metadata form))
                 metadata-form-id
                 (when (= :metadata-wrapper (:kind form))
                   (first (:children form)))
                 metadata-form (get forms-by-id metadata-form-id)
                 occurrences
                 (if (empty? expected)
                   {}
                   (when (and metadata-form
                              (= (:metadata form) (:value metadata-form)))
                     (get value-index metadata-form-id)))
                 observed (some-> occurrences (update-vals :descriptor))]
             (when-not (= expected observed)
               (p15-s23-c6c10-host-fail!
                "C6-ORIGIN" source-path
                :c2-metadata-structural-numeric-correspondence
                {:form-id (:form-id form)
                 :metadata-form-id metadata-form-id
                 :expected-occurrences expected
                 :observed-occurrences observed}))
             (assoc index (:form-id form) occurrences)))
         {}
         (rseq forms))]
    {:value-occurrences-by-form-id value-index
     :metadata-occurrences-by-form-id metadata-index}))

(defn p15-s23-c6c10-form-value-numeric-occurrences
  [source-path literal-authentication root-form-id]
  (if (contains? (:value-occurrences-by-form-id literal-authentication)
                 root-form-id)
    (get (:value-occurrences-by-form-id literal-authentication) root-form-id)
    (p15-s23-c6c10-host-fail!
     "C6-ORIGIN" source-path :indexed-c2-form-numeric-occurrences
     {:form-id root-form-id})))

(defn p15-s23-c6c10-form-metadata-numeric-occurrences
  [source-path literal-authentication root-form-id]
  (if (contains? (:metadata-occurrences-by-form-id literal-authentication)
                 root-form-id)
    (get (:metadata-occurrences-by-form-id literal-authentication)
         root-form-id)
    (p15-s23-c6c10-host-fail!
     "C6-ORIGIN" source-path :indexed-c2-metadata-numeric-occurrences
     {:form-id root-form-id})))

(defn p15-s23-c6c10-form-semantic-copy-numeric-occurrences
  [source-path literal-authentication form-id copy-value]
  (let [form (get (:forms-by-id literal-authentication) form-id)
        occurrences
        (p15-s23-c6c10-form-value-numeric-occurrences
         source-path literal-authentication form-id)
        expected
        (p15-s23-c6c10-collect-numeric-occurrences [] copy-value)
        observed (update-vals occurrences :descriptor)]
    (when-not (and form
                   (= copy-value (:value form))
                   (= expected observed))
      (p15-s23-c6c10-host-fail!
       "C6-ORIGIN" source-path
       :exact-c2-expanded-form-numeric-correspondence
       {:form-id form-id
        :expected-occurrences expected
        :observed-occurrences observed}))
    occurrences))