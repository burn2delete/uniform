

(defn p15-s23-closed-core-persistent-aliasing-valid?
  [node]
  (let [ownership (:ownership node)
        operands (:operands node)]
    (and (= :persistent-immutable-value (:model ownership))
         (= :shared (:shareability ownership))
         (= :immutable-sharing (:alias-policy ownership))
         (= :forbidden (:mutation ownership)))))

(def p15-s23-closed-core-persistent-ownership-common-keys
  #{:model :role :storage :shareability :alias-policy :mutation
    :managed-reachability :cleanup-policy :provider-requirement
    :allocator-requirement :escape-policy :derived?})

(defn p15-s23-closed-core-persistent-ownership-expected-keys
  [node managed?]
  (let [ownership (:ownership node)
        extras
        (case (:source-operation node)
          :local #{:binding-node-id}
          :let-binding #{:value-node-id}
          :if #{:incoming-node-ids}
          :do #{:result-node-id}
          :let #{:result-node-id}
          :function #{:result-node-id :result-disposition}
          :str #{:provider-id :lifetime}
          #{})
        extras
        (if managed?
          (set/union extras #{:provider-id :lifetime})
          extras)]
    (set/union p15-s23-closed-core-persistent-ownership-common-keys
               extras)))

(defn p15-s23-closed-core-persistent-ownership-schema-valid?
  ([node]
   (p15-s23-closed-core-persistent-ownership-schema-valid?
    node (= :str (:source-operation node))))
  ([node managed?]
   (= (p15-s23-closed-core-persistent-ownership-expected-keys
       node managed?)
      (set (keys (:ownership node))))))

(defn p15-s23-closed-core-expected-managed-node-ids
  [nodes]
  (reduce
   (fn [managed node]
     (let [managed?
           (or (= :str (:source-operation node))
               (some managed
                     (p15-s23-closed-core-forwarded-value-node-ids node)))]
       (cond-> managed managed? (conj (:node-id node)))))
   #{} nodes))

(defn p15-s23-closed-core-operation-shape-valid?
  [node node-by-id]
  (let [attributes (:attributes node)
        operands (:operands node)
        common-keys
        #{:intrinsic-effects :intrinsic-capabilities
          :aggregate-effects :aggregate-capabilities}
        exact-attributes?
        (fn [extras]
          (= (set/union common-keys extras) (set (keys attributes))))
        aggregate
        (p15-s23-closed-core-node-aggregate-facts node node-by-id)
        common-facts?
        (= (select-keys attributes common-keys)
           aggregate)]
    (and
     common-facts?
     (case (:source-operation node)
       :implicit-nil
       (and (= :literal (:kind node))
            (false? (:plan-node? node))
            (exact-attributes? #{:value :generated-role})
            (nil? (:value attributes))
            (contains? #{:implicit-do-nil :implicit-let-nil
                         :implicit-if-else :implicit-main-nil}
                       (:generated-role attributes))
            (= (last (:path node)) (:generated-role attributes))
            (empty? operands))

       :literal
       (and (= :literal (:kind node))
            (true? (:plan-node? node))
            (exact-attributes? #{:value})
            (some? (p15-s23-closed-core-scalar-literal-type
                    (:value attributes)))
            (empty? operands))

       :quote
       (and (= :quote (:kind node))
            (true? (:plan-node? node))
            (exact-attributes? #{:value})
            (some? (p15-s23-closed-core-scalar-literal-type
                    (:value attributes)))
            (empty? operands))

       :local
       (and (= :local (:kind node))
            (true? (:plan-node? node))
            (exact-attributes? #{:name :resolved-binding})
            (symbol? (:name attributes))
            (= 1 (count operands))
            (= (first operands) (:resolved-binding attributes))
            (= :binding (:kind (get node-by-id (first operands)))))

       :let-binding
       (and (= :binding (:kind node))
            (false? (:plan-node? node))
            (exact-attributes?
             #{:name :shadowed-binding :source-form-id})
            (symbol? (:name attributes))
            (keyword? (:source-form-id attributes))
            (= 1 (count operands)))

       :truthy
       (and (= :truthiness (:kind node))
            (false? (:plan-node? node))
            (exact-attributes? #{:false-values :result-type})
            (= [nil false] (:false-values attributes))
            (= :gravity/bool (:result-type attributes))
            (= 1 (count operands)))

       :if
       (let [truthy-node (get node-by-id (first operands))]
         (and (= :conditional (:kind node))
              (true? (:plan-node? node))
              (exact-attributes? #{:truthiness})
              (= :nil-and-false-only (:truthiness attributes))
              (= 3 (count operands))
              (= :truthy (:source-operation truthy-node))
              (= 1 (count (:operands truthy-node)))
              (every? node-by-id operands)))

       :do
       (let [body-count (:body-count attributes)]
         (and (= :sequence (:kind node))
              (true? (:plan-node? node))
              (exact-attributes? #{:body-count})
              (integer? body-count)
              (not (neg? body-count))
              (= (max 1 body-count) (count operands))
              (every? node-by-id operands)))

       :let
       (let [binding-count (:binding-count attributes)]
         (and (= :let (:kind node))
              (true? (:plan-node? node))
              (exact-attributes? #{:binding-count})
              (integer? binding-count)
              (not (neg? binding-count))
              (< binding-count (count operands))
              (every? node-by-id operands)
              (every? #(= :binding (:kind (get node-by-id %)))
                      (take binding-count operands))))

       :function
       (and (= :function (:kind node))
            (false? (:plan-node? node))
            (exact-attributes? #{:name :params :arity :visibility})
            (= 'main (:name attributes))
            (= [] (:params attributes))
            (zero? (:arity attributes))
            (contains? #{:public :private :stage2-local}
                       (:visibility attributes))
            (seq operands)
            (every? node-by-id operands))

       :str
       (and (= :call (:kind node))
            (true? (:plan-node? node))
            (exact-attributes? #{:function :arity :runtime-check-id})
            (= 'str (:function attributes))
            (contains? #{1 2} (:arity attributes))
            (= (:arity attributes) (count operands))
            (string? (:runtime-check-id attributes))
            (every? node-by-id operands))

       :println
       (and (= :effect (:kind node))
            (true? (:plan-node? node))
            (exact-attributes? #{:arity :ordering :runtime-check-id})
            (integer? (:arity attributes))
            (not (neg? (:arity attributes)))
            (= (:arity attributes) (count operands))
            (= :source-sequence (:ordering attributes))
            (string? (:runtime-check-id attributes))
            (every? node-by-id operands))

       false))))