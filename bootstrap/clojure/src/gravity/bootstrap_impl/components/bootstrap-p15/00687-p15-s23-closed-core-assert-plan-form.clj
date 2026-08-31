

(defn p15-s23-closed-core-assert-plan-form!
  [source-path plan-path instruction form-record generated-role
   origin-products]
  (let [op (:op instruction)
        value (:value form-record)
        operator (p15-s23-closed-core-form-operator form-record)
        matches?
        (if generated-role
          (and (= :literal op) (nil? (:value instruction)))
          (case op
            :literal (= (:value instruction) value)
            :quote
            (and (= (:value instruction) (second value))
                 (= 2 (count value))
                 (or
                  (and (= :list (:kind form-record))
                       (= 'quote operator)
                       (= 2 (count (:children form-record))))
                  (and (= :abbreviation (:kind form-record))
                       (= :quote (:abbrev form-record))
                       (= 1 (count (:children form-record)))
                       (some #(= :reader-abbreviation (:producer %))
                             (:generated-origin form-record)))))
            :local (and (symbol? value)
                        (= (:name instruction) value))
            :builtin-call
            (and (= 'str (:function instruction))
                 (= 'str operator)
                 (= (count (:args instruction)) (dec (count value))))
            :println
            (and (= 'println operator)
                 (= (count (:args instruction)) (dec (count value))))
            :do (and (= 'do operator)
                     (= (count (:body instruction)) (dec (count value))))
            :if (and (= 'if operator)
                     (contains? #{3 4} (count value)))
            :let (and (= 'let operator)
                      (vector? (second value)))
            false))]
    (when-not (and (map? instruction)
                   (contains? p15-s23-closed-core-recognized-plan-operations
                              op)
                   matches?)
      (p15-s23-closed-core-fail!
       "C6-ORIGIN" source-path
       (if (map? form-record)
         (assoc form-record
                :syntax-id (get-in origin-products [:raw :c3-syntax-id])
                :c2-form-id (get-in origin-products [:raw :c2-form-id])
                :source-span (get-in origin-products [:raw :c2-span])
                :generated-origin
                (vec
                 (concat
                  (or (get-in origin-products
                              [:raw :c2-reader-generated-origin]) [])
                  (or (get-in origin-products [:raw :c3-origin]) [])
                  (or (get-in origin-products
                              [:raw :expanded-generated-origin]) []))))
         instruction)
       {:missing-fact :lockstep-plan-c2-form-mapping
        :plan-path plan-path
        :plan-operation op
        :source-form-kind (:kind form-record)
        :source-form-operator operator
        :generated-role generated-role}))))

(defn p15-s23-closed-core-child-path
  [path field idx]
  (conj (conj path field) idx))

(declare p15-s23-closed-core-map-instruction)

(defn p15-s23-closed-core-implicit-nil-product
  [ctx path depth base-origin-products generated-role]
  (let [origin-products
        (p15-s23-closed-core-generated-origin-products
         (:source-path ctx) (:source-content-hash ctx) path
         base-origin-products generated-role)
        node
        (p15-s23-closed-core-node
         (:source-content-hash ctx) path :literal :implicit-nil false depth
         [] {:value nil :generated-role generated-role} :gravity/nil #{} #{}
         (p15-s23-closed-core-persistent-ownership
          :compiler-generated-value
          {:storage :static-value})
         {:outcome :proven-safe :basis :compiler-generated-nil}
         (:profile ctx) (:source origin-products))]
    (p15-s23-closed-core-single-node-product node origin-products)))

(defn p15-s23-closed-core-map-sequence
  [ctx instructions form-ids path field env depth base-origin-products
   empty-role]
  (when-not (= (count instructions) (count form-ids))
    (p15-s23-closed-core-fail!
     "C6-ORIGIN" (:source-path ctx) {:missing-fact :sequence-source-arity}
     {:plan-path path
      :field field
      :instruction-count (count instructions)
      :form-count (count form-ids)}))
  (if (empty? instructions)
    (p15-s23-closed-core-implicit-nil-product
     ctx (conj path empty-role) (inc depth) base-origin-products empty-role)
    (p15-s23-closed-core-merge-products
     (mapv (fn [idx instruction form-id]
             (p15-s23-closed-core-map-instruction
              ctx instruction form-id
              (p15-s23-closed-core-child-path path field idx)
              env (inc depth) nil base-origin-products))
           (range (count instructions)) instructions form-ids))))

(defn p15-s23-closed-core-map-arguments
  [ctx instruction form-record path env depth origin-products]
  (let [children (vec (rest (:children form-record)))
        args (:args instruction)]
    (when-not (= (count args) (count children))
      (p15-s23-closed-core-fail!
       "C6-ORIGIN" (:source-path ctx) form-record
       {:missing-fact :argument-source-arity
        :plan-path path
        :argument-count (count args)
        :form-child-count (count children)}))
    (p15-s23-closed-core-merge-products
     (mapv (fn [idx child form-id]
             (p15-s23-closed-core-map-instruction
              ctx child form-id
              (p15-s23-closed-core-child-path path :args idx)
              env (inc depth) nil origin-products))
           (range (count args)) args children))))

(defn p15-s23-closed-core-binding-node
  [ctx path depth name name-form value-product origin-products shadowed]
  (let [node
        (p15-s23-closed-core-node
         (:source-content-hash ctx) path :binding :let-binding false depth
         [(:result-node-id value-product)]
         {:name name
          :shadowed-binding (:node-id shadowed)
          :source-form-id (:form-id name-form)}
         (:type value-product) (:effects value-product)
         (:capabilities value-product)
         (p15-s23-closed-core-persistent-ownership
          :lexical-binding
          {:storage :forwarded-persistent-value
           :value-node-id (:result-node-id value-product)})
         {:outcome :proven-safe :basis :sequential-let-binding}
         (:profile ctx) (:source origin-products))]
    (-> (p15-s23-closed-core-single-node-product node origin-products)
        (assoc :binding-records
               [{:binding-node-id (:node-id node)
                 :name name
                 :path path
                 :value-node-id (:result-node-id value-product)
                 :shadowed-binding (:node-id shadowed)
                 :source-form-id (:form-id name-form)}]))))