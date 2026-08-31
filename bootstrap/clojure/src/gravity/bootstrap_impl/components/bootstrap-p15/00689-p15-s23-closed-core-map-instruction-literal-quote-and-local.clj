(defn- __gravity_bootstrap_closed_core_literal_quote_and_local [ctx
                                                                instruction
                                                                form-record
                                                                path
                                                                env
                                                                depth
                                                                origin-products]
  (case
    (:op instruction)
    :literal
    (let [type (p15-s23-closed-core-literal-type
                 (:source-path ctx)
                 (:value instruction)
                 (p15-s23-closed-core-prospective-node-subject
                   ctx
                   path
                   :literal
                   :literal
                   origin-products
                   {:expected-type :closed-scalar-literal,
                    :actual-type
                    (or (some-> (:value instruction) class .getName) :nil),
                    :relevant-binding-id :not-applicable}))
          node (p15-s23-closed-core-node
                 (:source-content-hash ctx)
                 path
                 :literal
                 :literal
                 true
                 depth
                 []
                 {:value (:value instruction)}
                 type
                 #{}
                 #{}
                 (p15-s23-closed-core-persistent-ownership
                   :literal-value
                   {:storage :static-or-managed-value})
                 {:outcome :proven-safe, :basis :closed-scalar-literal}
                 (:profile ctx)
                 (:source origin-products))]
      (p15-s23-closed-core-single-node-product node origin-products))
    :quote
    (let [type (p15-s23-closed-core-literal-type
                 (:source-path ctx)
                 (:value instruction)
                 (p15-s23-closed-core-prospective-node-subject
                   ctx
                   path
                   :quote
                   :quote
                   origin-products
                   {:expected-type :closed-scalar-literal,
                    :actual-type
                    (or (some-> (:value instruction) class .getName) :nil),
                    :relevant-binding-id :not-applicable}))
          node (p15-s23-closed-core-node
                 (:source-content-hash ctx)
                 path
                 :quote
                 :quote
                 true
                 depth
                 []
                 {:value (:value instruction)}
                 type
                 #{}
                 #{}
                 (p15-s23-closed-core-persistent-ownership
                   :quoted-value
                   {:storage :static-or-managed-value})
                 {:outcome :proven-safe, :basis :closed-quote}
                 (:profile ctx)
                 (:source origin-products))]
      (p15-s23-closed-core-single-node-product node origin-products))
    :local
    (let [binding (get env (:name instruction)) binding-node-id (:node-id binding)]
      (when-not (string? binding-node-id)
        (p15-s23-closed-core-fail!
          "C6-VERIFY"
          (:source-path ctx)
          form-record
          {:missing-fact :resolved-lexical-binding,
           :plan-path path,
           :local (:name instruction)}))
      (let [type (:type binding)
            node (p15-s23-closed-core-node
                   (:source-content-hash ctx)
                   path
                   :local
                   :local
                   true
                   depth
                   [binding-node-id]
                   {:name (:name instruction), :resolved-binding binding-node-id}
                   type
                   #{}
                   #{}
                   (p15-s23-closed-core-persistent-ownership
                     :local-reference
                     {:storage :shared-persistent-reference,
                      :binding-node-id binding-node-id})
                   {:outcome :proven-safe, :basis :resolved-lexical-local}
                   (:profile ctx)
                   (:source origin-products))]
        (p15-s23-closed-core-single-node-product node origin-products)))))
