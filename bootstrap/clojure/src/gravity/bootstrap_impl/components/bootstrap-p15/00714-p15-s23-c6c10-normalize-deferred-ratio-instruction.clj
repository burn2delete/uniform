

(defn p15-s23-c6c10-normalize-deferred-ratio-instruction
  [source-path literal-authentication instruction source-form-id]
  (let [forms-by-id (:forms-by-id literal-authentication)]
    (letfn [(form! [form-id]
              (or (get forms-by-id form-id)
                  (p15-s23-c6c10-plan-shape-fail!
                   source-path {:missing-form-id form-id})))
            (normalize-many [instructions form-ids]
              (when-not (= (count instructions) (count form-ids))
                (p15-s23-c6c10-plan-shape-fail!
                 source-path
                 {:normalization-operation (:op instruction)
                  :instruction-count (count instructions)
                  :form-count (count form-ids)}))
              (mapv (fn [child-instruction child-form-id]
                      (normalize child-instruction child-form-id))
                    instructions form-ids))
            (normalize [current-instruction current-form-id]
              (let [source-form (form! current-form-id)
                    metadata-wrapper?
                    (= :metadata-wrapper (:kind source-form))
                    attached-form-id
                    (when metadata-wrapper? (last (:children source-form)))
                    attached-deferred?
                    (and attached-form-id
                         (p15-s23-c6c10-authentic-deferred-ratio-instruction?
                          literal-authentication attached-form-id
                          current-instruction))]
                (cond
                  (p15-s23-c6c10-authentic-deferred-ratio-instruction?
                   literal-authentication current-form-id current-instruction)
                  {:op :literal
                   :value
                   (:descriptor
                    (get (:deferred-ratio-by-form-id
                          literal-authentication)
                         current-form-id))}

                  attached-deferred?
                  ;; Metadata exclusion is intentional and is diagnosed by
                  ;; the Gravity C6 form gate.  Do not erase the wrapper by
                  ;; normalizing its attached deferred ratio into a literal.
                  current-instruction

                  :else
                  (let [form
                        (if metadata-wrapper?
                          (form! attached-form-id)
                          source-form)
                        children (:children form)
                        operation (:op current-instruction)]
                    (case operation
                      :do
                      (assoc current-instruction :body
                             (normalize-many (:body current-instruction)
                                             (vec (rest children))))

                      :if
                      (let [arguments (vec (rest children))]
                        (cond-> current-instruction
                          (>= (count arguments) 1)
                          (assoc :test
                                 (normalize (:test current-instruction)
                                            (nth arguments 0)))
                          (>= (count arguments) 2)
                          (assoc :then
                                 (normalize (:then current-instruction)
                                            (nth arguments 1)))
                          (= (count arguments) 3)
                          (assoc :else
                                 (normalize (:else current-instruction)
                                            (nth arguments 2)))))

                      :let
                      (let [binding-form (form! (second children))
                            pairs (partition 2 (:children binding-form))
                            bindings (:bindings current-instruction)
                            body-form-ids (vec (drop 2 children))]
                        (when-not (= (count pairs) (count bindings))
                          (p15-s23-c6c10-plan-shape-fail!
                           source-path
                           {:normalization-operation operation
                            :binding-count (count bindings)
                            :source-pair-count (count pairs)}))
                        (assoc current-instruction
                               :bindings
                               (mapv
                                (fn [binding [_ expression-id]]
                                  (update binding :expr
                                          #(normalize % expression-id)))
                                bindings pairs)
                               :body
                               (normalize-many (:body current-instruction)
                                               body-form-ids)))

                      (:println :builtin-call :function-call)
                      (assoc current-instruction :args
                             (normalize-many (:args current-instruction)
                                             (vec (rest children))))

                      :vector-literal
                      (assoc current-instruction :items
                             (normalize-many (:items current-instruction)
                                             (vec children)))

                      :set-literal
                      (let [ordered
                            (sort-by
                             (fn [child-id]
                               (p15-s23-c6c10-stage2-order-key
                                (:value (form! child-id))))
                             children)]
                        (assoc current-instruction :items
                               (normalize-many (:items current-instruction)
                                               (vec ordered))))

                      :map-literal
                      (let [ordered
                            (sort-by
                             (fn [[key-id value-id]]
                               (p15-s23-c6c10-stage2-order-key
                                [(:value (form! key-id))
                                 (:value (form! value-id))]))
                             (partition 2 children))
                            entries (:entries current-instruction)]
                        (when-not (= (count ordered) (count entries))
                          (p15-s23-c6c10-plan-shape-fail!
                           source-path
                           {:normalization-operation operation
                            :entry-count (count entries)
                            :source-pair-count (count ordered)}))
                        (assoc current-instruction :entries
                               (mapv
                                (fn [entry [key-id value-id]]
                                  (-> entry
                                      (update :key #(normalize % key-id))
                                      (update :value
                                              #(normalize % value-id))))
                                entries ordered)))

                      current-instruction)))))]
      (normalize instruction source-form-id))))