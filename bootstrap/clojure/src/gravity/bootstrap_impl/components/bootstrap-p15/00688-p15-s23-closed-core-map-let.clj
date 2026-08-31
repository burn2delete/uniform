

(defn p15-s23-closed-core-map-let
  [ctx instruction form-record path env depth origin-products]
  (let [form-by-id (get-in ctx [:indexes :form-by-id])
        children (:children form-record)
        binding-vector (get form-by-id (second children))
        binding-form-ids (:children binding-vector)
        bindings (:bindings instruction)
        body-form-ids (vec (drop 2 children))]
    (when-not (and (map? binding-vector)
                   (= :vector (:kind binding-vector))
                   (= (* 2 (count bindings)) (count binding-form-ids)))
      (p15-s23-closed-core-fail!
       "C6-ORIGIN" (:source-path ctx) form-record
       {:missing-fact :let-binding-source-shape
        :plan-path path}))
    (loop [idx 0
           env env
           local-names #{}
           products []]
      (if (< idx (count bindings))
        (let [binding (nth bindings idx)
              name (:name binding)
              name-form (get form-by-id (nth binding-form-ids (* 2 idx)))
              expr-form-id (nth binding-form-ids (inc (* 2 idx)))
              expr-path (conj (p15-s23-closed-core-child-path
                               path :bindings idx)
                              :expr)]
          (when (or (not= name (:value name-form))
                    (contains? local-names name))
            (p15-s23-closed-core-fail!
             "C6-VERIFY" (:source-path ctx) name-form
             {:missing-fact :sequential-unique-let-binding
              :plan-path path
              :binding name}))
          (let [value-product
                (p15-s23-closed-core-map-instruction
                 ctx (:expr binding) expr-form-id expr-path env
                 (inc depth) nil origin-products)
                binder-path (conj (p15-s23-closed-core-child-path
                                   path :bindings idx)
                                  :binder)
                binder-origin
                (p15-s23-closed-core-origin-products
                 (:source-path ctx) (:source-content-hash ctx) binder-path
                 name-form (:root-syntax ctx) (:expanded-root-syntax ctx)
                 (:indexes ctx) (:token-ordinal-by-id ctx) nil)
                binder-product
                (p15-s23-closed-core-binding-node
                 ctx binder-path (inc depth) name name-form value-product
                 binder-origin (get env name))]
            (recur (inc idx)
                   (assoc env name
                          {:node-id (:result-node-id binder-product)
                           :type (:type binder-product)})
                   (conj local-names name)
                   (conj products
                         (p15-s23-closed-core-merge-products
                          [value-product binder-product])))))
        (let [binding-products (p15-s23-closed-core-merge-products products)
              body-product
              (p15-s23-closed-core-map-sequence
               ctx (:body instruction) body-form-ids path :body env depth
               origin-products :implicit-let-nil)
              children-product
              (p15-s23-closed-core-merge-products
               [binding-products body-product])
              binding-node-ids
              (mapv :binding-node-id (:binding-records binding-products))
              body-node-by-path
              (into {} (map (juxt :path identity)) (:nodes body-product))
              body-node-ids
              (if (empty? (:body instruction))
                [(:result-node-id body-product)]
                (mapv #(get-in body-node-by-path
                               [(p15-s23-closed-core-child-path
                                 path :body %)
                                :node-id])
                      (range (count (:body instruction)))))
              node
              (p15-s23-closed-core-node
               (:source-content-hash ctx) path :let :let true depth
               (vec (concat binding-node-ids body-node-ids))
               {:binding-count (count bindings)} (:type body-product)
               (:effects children-product) (:capabilities children-product)
               (p15-s23-closed-core-persistent-ownership
                :lexical-scope-result
                {:storage :forwarded-persistent-value
                 :result-node-id (:result-node-id body-product)})
               {:outcome :proven-safe :basis :sequential-lexical-scope}
               (:profile ctx) (:source origin-products))]
          (p15-s23-closed-core-add-node
           children-product node origin-products))))))

(defn p15-s23-closed-core-map-if
  [ctx instruction form-record path env depth origin-products]
  (let [children (:children form-record)
        test-form-id (second children)
        then-form-id (nth children 2 nil)
        else-form-id (nth children 3 nil)
        test-product
        (p15-s23-closed-core-map-instruction
         ctx (:test instruction) test-form-id (conj path :test) env
         (inc depth) nil origin-products)
        then-product
        (p15-s23-closed-core-map-instruction
         ctx (:then instruction) then-form-id (conj path :then) env
         (inc depth) nil origin-products)
        else-product
        (p15-s23-closed-core-map-instruction
         ctx (:else instruction) else-form-id (conj path :else) env
         (inc depth) (when-not else-form-id :implicit-if-else)
         origin-products)
        truthy-path (conj path :truthy)
        truthy-origin
        (p15-s23-closed-core-generated-origin-products
         (:source-path ctx) (:source-content-hash ctx) truthy-path
         origin-products :truthiness-normalization)
        truthy-node
        (p15-s23-closed-core-node
         (:source-content-hash ctx) truthy-path :truthiness :truthy false
         (inc depth) [(:result-node-id test-product)]
         {:false-values [nil false] :result-type :gravity/bool}
         :gravity/bool (:effects test-product) (:capabilities test-product)
         (p15-s23-closed-core-persistent-ownership
          :truthiness-value {:storage :static-value})
         {:outcome :proven-safe :basis :gravity-truthiness}
         (:profile ctx) (:source truthy-origin))
        truthy-product
        (p15-s23-closed-core-single-node-product truthy-node truthy-origin)
        children-product
        (p15-s23-closed-core-merge-products
         [test-product truthy-product then-product else-product])
        node
        (p15-s23-closed-core-node
         (:source-content-hash ctx) path :conditional :if true depth
         [(:node-id truthy-node) (:result-node-id then-product)
          (:result-node-id else-product)]
         {:truthiness :nil-and-false-only}
         (p15-s23-closed-core-type-join (:type then-product)
                                        (:type else-product))
         (:effects children-product) (:capabilities children-product)
         (p15-s23-closed-core-persistent-ownership
          :conditional-result
          {:storage :forwarded-persistent-value
           :incoming-node-ids [(:result-node-id then-product)
                               (:result-node-id else-product)]})
         {:outcome :proven-safe :basis :closed-conditional}
         (:profile ctx) (:source origin-products))]
    (p15-s23-closed-core-add-node children-product node origin-products)))