

(defn p15-s23-closed-core-persistent-forwarding-valid?
  ([node node-by-id]
   (p15-s23-closed-core-persistent-forwarding-valid?
    node node-by-id (= :str (:source-operation node))))
  ([node node-by-id expected-managed?]
  (let [ownership (:ownership node)
        operands (:operands node)
        common-keys p15-s23-closed-core-persistent-ownership-common-keys
        exact-keys?
        (fn [extras]
          (= (set/union common-keys extras
                        (if expected-managed?
                          #{:provider-id :lifetime} #{}))
             (set (keys ownership))))]
    (and
     (= :value-and-program-reachability
        (:managed-reachability ownership))
     (= :no-explicit-cleanup (:cleanup-policy ownership))
     (if expected-managed?
       (and (= :gravity.reference/jvm-managed-allocator
               (:provider-requirement ownership))
            (= :memory/allocator (:allocator-requirement ownership))
            (= :gravity.reference/jvm-managed-allocator
               (:provider-id ownership))
            (= :managed-reachability (:lifetime ownership)))
       (and (= :not-required (:provider-requirement ownership))
            (= :not-required (:allocator-requirement ownership))))
     (= :safe-persistent-value (:escape-policy ownership))
     (true? (:derived? ownership))
     (case (:source-operation node)
       :implicit-nil
       (and (exact-keys? #{})
            (= :compiler-generated-value (:role ownership))
            (= :static-value (:storage ownership))
            (empty? operands))

       :literal
       (and (exact-keys? #{})
            (= :literal-value (:role ownership))
            (= :static-or-managed-value (:storage ownership))
            (empty? operands))

       :quote
       (and (exact-keys? #{})
            (= :quoted-value (:role ownership))
            (= :static-or-managed-value (:storage ownership))
            (empty? operands))

       :local
       (and (exact-keys? #{:binding-node-id})
            (= :local-reference (:role ownership))
            (= :shared-persistent-reference (:storage ownership))
            (= 1 (count operands))
            (= (first operands) (:binding-node-id ownership))
            (= :binding (:kind (get node-by-id (first operands)))))

       :let-binding
       (and (exact-keys? #{:value-node-id})
            (= :lexical-binding (:role ownership))
            (= :forwarded-persistent-value (:storage ownership))
            (= 1 (count operands))
            (= (first operands) (:value-node-id ownership)))

       :truthy
       (and (exact-keys? #{})
            (= :truthiness-value (:role ownership))
            (= :static-value (:storage ownership))
            (= 1 (count operands)))

       :if
       (and (exact-keys? #{:incoming-node-ids})
            (= :conditional-result (:role ownership))
            (= :forwarded-persistent-value (:storage ownership))
            (= 3 (count operands))
            (= (vec (rest operands)) (:incoming-node-ids ownership)))

       :do
       (and (exact-keys? #{:result-node-id})
            (= :sequence-result (:role ownership))
            (= :forwarded-persistent-value (:storage ownership))
            (seq operands)
            (= (last operands) (:result-node-id ownership)))

       :let
       (let [binding-count (get-in node [:attributes :binding-count])
             binding-operands (take binding-count operands)]
         (and (exact-keys? #{:result-node-id})
              (= :lexical-scope-result (:role ownership))
              (= :forwarded-persistent-value (:storage ownership))
              (integer? binding-count)
              (<= 0 binding-count)
              (< binding-count (count operands))
              (every? #(= :binding (:kind (get node-by-id %)))
                      binding-operands)
              (= (last operands) (:result-node-id ownership))))

       :function
       (and (exact-keys? #{:result-node-id :result-disposition})
            (= :entrypoint-function-result (:role ownership))
            (= :forwarded-persistent-value (:storage ownership))
            (seq operands)
            (= (last operands) (:result-node-id ownership))
            (= :shared-persistent-value-return
               (:result-disposition ownership)))

       :str
       (and (exact-keys? #{})
            (= :managed-string-result (:role ownership))
            (= :host-managed-string (:storage ownership))
            (= :gravity.reference/jvm-managed-allocator
               (:provider-id ownership))
            (= :managed-reachability (:lifetime ownership))
            (contains? #{1 2} (count operands))
            (every? node-by-id operands))

       :println
       (and (exact-keys? #{})
            (= :stdout-result (:role ownership))
            (= :static-nil (:storage ownership))
            (every? node-by-id operands))

       false)))))

(defn p15-s23-closed-core-recomputed-mapping-id
  [artifact]
  (p15-s23-closed-core-digest
   {:source-content-hash (:source-content-hash artifact)
    :plan-id (get-in artifact [:source-core-input :plan-id])
    :nodes (mapv #(select-keys % [:node-id :path :source])
                 (:core-nodes artifact))
    :source-origin-table (:source-origin-table artifact)}))

(defn p15-s23-closed-core-recomputed-provenance-binding-id
  [artifact]
  (p15-s23-closed-core-digest
   {:kind :gravity/p15-s23-closed-origin-provenance-binding
    :source-content-hash (:source-content-hash artifact)
    :plan-id (get-in artifact [:source-core-input :plan-id])
    :bindings
    (mapv (fn [[origin-id raw]]
            {:origin-id origin-id
             :binding-hash (:provenance-binding-hash raw)})
          (sort-by key (:origin-closure artifact)))}))

(defn p15-s23-closed-core-recomputed-actual-path-binding-id
  [artifact]
  (p15-s23-closed-core-digest
   {:kind :gravity/p15-s23-closed-origin-actual-path-binding
    :provenance-binding-id (:provenance-binding-id artifact)
    :bindings
    (mapv (fn [[origin-id raw]]
            {:origin-id origin-id
             :actual-path-binding-hash (:actual-path-binding-hash raw)})
          (sort-by key (:origin-closure artifact)))}))

(defn p15-s23-closed-core-instruction-origin-sidecar
  [artifact]
  (let [nodes (:core-nodes artifact)
        plan-entry-count (count (filter :plan-node? nodes))]
    {:artifact :gravity/p15-s23-instruction-origin-sidecar
     :plan-id (get-in artifact [:source-core-input :plan-id])
     :mapping-id (:mapping-id artifact)
     :provenance-binding-id (:provenance-binding-id artifact)
     :actual-path-binding-id (:actual-path-binding-id artifact)
     :plan-entry-count plan-entry-count
     :derived-entry-count (- (count nodes) plan-entry-count)
     :semantic-entry-count (count nodes)
     :origin-entry-count (count (:source-origin-table artifact))
     :semantic-entries
     (mapv
      (fn [node]
        (let [origin-id (get-in node [:source :origin-id])
              origin (get (:source-origin-table artifact) origin-id)]
          (merge
           {:node-id (:node-id node)
            :plan-path (:path node)
            :source-operation (:source-operation node)
            :plan-node? (:plan-node? node)
            :origin-id origin-id}
           (select-keys origin
                        [:form-structural-path :form-kind
                         :generated-role]))))
      (sort-by (comp pr-str :path) nodes))
     :semantic-table-field :source-origin-table
     :raw-table-field :origin-closure
     :status :authenticated}))