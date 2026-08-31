(defn- __gravity_bootstrap_closed_core_sequence_and_control [ctx
                                                             instruction
                                                             form-record
                                                             path
                                                             env
                                                             depth
                                                             origin-products]
  (case
    (:op instruction)
    :do
    (let [body-form-ids (vec (rest (:children form-record)))
          body-product (p15-s23-closed-core-map-sequence
                         ctx
                         (:body instruction)
                         body-form-ids
                         path
                         :body
                         env
                         depth
                         origin-products
                         :implicit-do-nil)
          body-node-by-path (into {} (map (juxt :path identity)) (:nodes body-product))
          body-node-ids (if (empty? (:body instruction))
                          [(:result-node-id body-product)]
                          (mapv
                            #(get-in
                              body-node-by-path
                              [(p15-s23-closed-core-child-path path :body %) :node-id])
                            (range (count (:body instruction)))))
          node (p15-s23-closed-core-node
                 (:source-content-hash ctx)
                 path
                 :sequence
                 :do
                 true
                 depth
                 body-node-ids
                 {:body-count (count (:body instruction))}
                 (:type body-product)
                 (:effects body-product)
                 (:capabilities body-product)
                 (p15-s23-closed-core-persistent-ownership
                   :sequence-result
                   {:storage :forwarded-persistent-value,
                    :result-node-id (:result-node-id body-product)})
                 {:outcome :proven-safe, :basis :ordered-sequence}
                 (:profile ctx)
                 (:source origin-products))]
      (p15-s23-closed-core-add-node body-product node origin-products))
    :if
    (p15-s23-closed-core-map-if
      ctx
      instruction
      form-record
      path
      env
      depth
      origin-products)
    :let
    (p15-s23-closed-core-map-let
      ctx
      instruction
      form-record
      path
      env
      depth
      origin-products)))
