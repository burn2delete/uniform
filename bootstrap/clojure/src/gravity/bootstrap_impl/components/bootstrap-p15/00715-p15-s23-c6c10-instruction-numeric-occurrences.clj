

(defn p15-s23-c6c10-instruction-numeric-occurrences
  [source-path literal-authentication instruction source-form-id]
  (let [forms-by-id (:forms-by-id literal-authentication)]
    (letfn [(form! [form-id]
              (or (get forms-by-id form-id)
                  (p15-s23-c6c10-plan-shape-fail!
                   source-path {:missing-form-id form-id})))
            (effective-form [form-id]
              (let [form (form! form-id)]
                (if (= :metadata-wrapper (:kind form))
                  (form! (last (:children form)))
                  form)))
            (merge-children [path-key instructions child-ids]
              (when-not (= (count instructions) (count child-ids))
                (p15-s23-c6c10-plan-shape-fail!
                 source-path
                 {:operation (:op instruction)
                  :instruction-count (count instructions)
                  :source-child-count (count child-ids)}))
              (apply
               p15-s23-c6c10-merge-occurrences
               (map-indexed
                (fn [index [child-instruction child-id]]
                  (p15-s23-c6c10-prefix-occurrences
                   [path-key index]
                   (walk child-instruction child-id)))
                (map vector instructions child-ids))))
            (walk [current-instruction current-form-id]
              (let [source-form (form! current-form-id)
                    attached-form-id
                    (when (= :metadata-wrapper (:kind source-form))
                      (last (:children source-form)))
                    wrapped-deferred?
                    (and attached-form-id
                         (p15-s23-c6c10-authentic-deferred-ratio-instruction?
                          literal-authentication attached-form-id
                          current-instruction))
                    form (effective-form current-form-id)
                    children (:children form)
                    operation (:op current-instruction)
                    occurrences
                    (if wrapped-deferred?
                      {}
                      (cond
                      (= :literal operation)
                      (do
                        (when-not (= (:value current-instruction)
                                     (:value form))
                          (p15-s23-c6c10-plan-shape-fail!
                           source-path
                           {:operation operation
                            :form-id (:form-id form)
                            :plan-value (:value current-instruction)
                            :source-value (:value form)}))
                        (p15-s23-c6c10-prefix-occurrences
                         [:value]
                         (p15-s23-c6c10-form-value-numeric-occurrences
                          source-path literal-authentication
                          (:form-id form))))

                      (= :quote operation)
                      (let [value (vec (:value form))
                            quoted-form-id
                            (if (= :abbreviation (:kind form))
                              (first children)
                              (second children))]
                        (when-not (and (= 'quote (first value))
                                       (= (:value current-instruction)
                                          (second value))
                                       quoted-form-id)
                          (p15-s23-c6c10-plan-shape-fail!
                           source-path
                           {:operation operation
                            :form-id (:form-id form)}))
                        (p15-s23-c6c10-prefix-occurrences
                         [:value]
                         (p15-s23-c6c10-form-value-numeric-occurrences
                          source-path literal-authentication
                          quoted-form-id)))

                      (= :do operation)
                      (merge-children :body (:body current-instruction)
                                      (vec (rest children)))

                      (= :if operation)
                      (let [source-arguments (vec (rest children))
                            explicit-else? (= 3 (count source-arguments))
                            _ (when-not (or (= 2 (count source-arguments))
                                            explicit-else?)
                                (p15-s23-c6c10-plan-shape-fail!
                                 source-path
                                 {:operation operation
                                  :form-id (:form-id form)
                                  :source-argument-count
                                  (count source-arguments)}))]
                        (p15-s23-c6c10-merge-occurrences
                         (p15-s23-c6c10-prefix-occurrences
                          [:test]
                          (walk (:test current-instruction)
                                (nth source-arguments 0)))
                         (p15-s23-c6c10-prefix-occurrences
                          [:then]
                          (walk (:then current-instruction)
                                (nth source-arguments 1)))
                         (if explicit-else?
                           (p15-s23-c6c10-prefix-occurrences
                            [:else]
                            (walk (:else current-instruction)
                                  (nth source-arguments 2)))
                           (do
                             (when-not (= {:op :literal :value nil}
                                          (:else current-instruction))
                               (p15-s23-c6c10-plan-shape-fail!
                                source-path
                                {:operation operation
                                 :implicit-else
                                 (:else current-instruction)}))
                             {}))))

                      (= :let operation)
                      (let [binding-form (form! (second children))
                            binding-child-ids (:children binding-form)
                            pairs (partition 2 binding-child-ids)
                            plan-bindings (:bindings current-instruction)
                            body-form-ids (vec (drop 2 children))]
                        (when-not (and (= :vector (:kind binding-form))
                                       (even? (count binding-child-ids))
                                       (= (count pairs)
                                          (count plan-bindings)))
                          (p15-s23-c6c10-plan-shape-fail!
                           source-path
                           {:operation operation
                            :form-id (:form-id form)
                            :binding-form-id (:form-id binding-form)}))
                        (p15-s23-c6c10-merge-occurrences
                         (apply
                          p15-s23-c6c10-merge-occurrences
                          (map-indexed
                           (fn [index [[name-id expression-id] binding]]
                             (when-not (= (:name binding)
                                          (:value (form! name-id)))
                               (p15-s23-c6c10-plan-shape-fail!
                                source-path
                                {:operation operation
                                 :binding-index index
                                 :name-form-id name-id}))
                             (p15-s23-c6c10-prefix-occurrences
                              [:bindings index :expr]
                              (walk (:expr binding) expression-id)))
                           (map vector pairs plan-bindings)))
                         (merge-children :body (:body current-instruction)
                                         body-form-ids)))

                      (contains? #{:println :builtin-call :function-call}
                                 operation)
                      (merge-children :args (:args current-instruction)
                                      (vec (rest children)))

                      (= :vector-literal operation)
                      (merge-children :items (:items current-instruction)
                                      (vec children))

                      (= :set-literal operation)
                      (let [ordered-child-ids
                            (sort-by
                             (fn [child-id]
                               (p15-s23-c6c10-stage2-order-key
                                (:value (form! child-id))))
                             children)]
                        (merge-children :items (:items current-instruction)
                                        (vec ordered-child-ids)))

                      (= :map-literal operation)
                      (let [source-pairs (partition 2 children)
                            ordered-pairs
                            (sort-by
                             (fn [[key-id value-id]]
                               (p15-s23-c6c10-stage2-order-key
                                [(:value (form! key-id))
                                 (:value (form! value-id))]))
                             source-pairs)
                            plan-entries (:entries current-instruction)]
                        (when-not (= (count ordered-pairs)
                                     (count plan-entries))
                          (p15-s23-c6c10-plan-shape-fail!
                           source-path
                           {:operation operation
                            :form-id (:form-id form)
                            :entry-count (count plan-entries)
                            :source-pair-count (count ordered-pairs)}))
                        (apply
                         p15-s23-c6c10-merge-occurrences
                         (map-indexed
                          (fn [index [[key-id value-id] entry]]
                            (p15-s23-c6c10-merge-occurrences
                             (p15-s23-c6c10-prefix-occurrences
                              [:entries index :key]
                              (walk (:key entry) key-id))
                             (p15-s23-c6c10-prefix-occurrences
                              [:entries index :value]
                              (walk (:value entry) value-id))))
                          (map vector ordered-pairs plan-entries))))

                        :else {}))
                    expected
                    (p15-s23-c6c10-collect-numeric-occurrences
                     [] current-instruction)
                    observed (update-vals occurrences :descriptor)]
                (when-not (= expected observed)
                  (p15-s23-c6c10-plan-shape-fail!
                   source-path
                   {:operation operation
                    :form-id (:form-id form)
                    :expected-occurrences expected
                    :observed-occurrences observed}))
                occurrences))]
      (walk instruction source-form-id))))

(defn p15-s23-c6c10-main-source-body
  [source-path literal-authentication]
  (let [forms-by-id (:forms-by-id literal-authentication)
        candidates
        (filterv
         (fn [form]
           (let [value (:value form)]
             (and (nil? (:parent-form-id form))
                  (seq? value)
                  (= 'defn (first value))
                  (= 'main (second value)))))
         (vals forms-by-id))]
    (when-not (= 1 (count candidates))
      (p15-s23-c6c10-plan-shape-fail!
       source-path {:main-form-candidates (mapv :form-id candidates)}))
    (let [main-root (first candidates)
          definition-form
          (if (= :metadata-wrapper (:kind main-root))
            (get forms-by-id (last (:children main-root)))
            main-root)]
      {:main-root main-root
       :definition-form definition-form
       :body-form-ids (vec (drop 3 (:children definition-form)))})))