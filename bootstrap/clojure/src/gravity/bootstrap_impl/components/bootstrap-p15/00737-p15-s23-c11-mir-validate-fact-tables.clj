

(defn p15-s23-c11-mir-validate-fact-tables!
  [source-path checked-core mir]
  (let [nodes (:core-nodes checked-core)
        expected-type
        (p15-s23-c11-mir-add-runtime-check-facts
         checked-core :type
         (p15-s23-c11-mir-reindex-fact-table
          nodes (:type-facts checked-core) :fact-id ":type-fact"))
        expected-effect
        (p15-s23-c11-mir-add-runtime-check-facts
         checked-core :effect
         (p15-s23-c11-mir-reindex-fact-table
          nodes (:effect-facts checked-core) :fact-id ":effect-fact"))
        expected-ownership
        (p15-s23-c11-mir-add-runtime-check-facts
         checked-core :ownership
         (p15-s23-c11-mir-reindex-fact-table
          nodes (:ownership-facts checked-core)
          :fact-id ":ownership-fact"))
        expected-capability
        (p15-s23-c11-mir-add-runtime-check-facts
         checked-core :capability
         (p15-s23-c11-mir-reindex-fact-table
          nodes (:capability-facts checked-core)
          :fact-id ":capability-fact"))
        expected-safety
        (p15-s23-c11-mir-add-runtime-check-facts
         checked-core :safety
         (p15-s23-c11-mir-reindex-fact-table
          nodes (:safety-facts checked-core)
          :outcome-id ":safety-outcome"))
        expected-profile-target
        (p15-s23-c11-mir-add-runtime-check-facts
         checked-core :profile-target
         (p15-s23-c11-mir-profile-target-table checked-core))
        expected-capability-proofs
        (p15-s23-c11-mir-capability-proof-table checked-core)
        expected-safety-proofs
        (p15-s23-c11-mir-safety-proof-table checked-core)]
    (doseq [[expected observed id missing]
            [[expected-type (:type-table mir)
              "C11-TYPE" :id-indexed-c7-type-facts]
             [expected-effect (:effect-table mir)
              "C11-EFFECT" :id-indexed-c8-effect-facts]
             [expected-ownership (:ownership-table mir)
              "C11-VERIFY" :id-indexed-c9-ownership-facts]
             [expected-capability (:capability-table mir)
              "C11-EFFECT" :id-indexed-c8-capability-facts]
             [expected-safety (:safety-table mir)
              "C11-SAFETY" :id-indexed-c10-safety-outcomes]
             [expected-profile-target (:profile-target-table mir)
              "C11-MODULE" :id-indexed-profile-target-facts]
             [expected-capability-proofs (:capability-proof-table mir)
              "C11-EFFECT" :id-indexed-capability-proofs]]]
      (p15-s23-c11-mir-require!
       (= expected observed) id source-path mir missing))
    (p15-s23-c11-mir-require!
     (= {:capability-proof-table-reference :capability-proof-table
         :safety-proofs expected-safety-proofs}
        (:proof-certificate-table mir))
     "C11-SAFETY" source-path mir
     :id-indexed-authenticated-safety-proof-certificates))
  (let [expected-effect-graph
        (p15-s23-closed-core-effect-event-ordering
         (:core-nodes checked-core))
        observed (:effect-order-graph mir)
        effectful?
        (boolean
         (some #(= :runtime-checked (get-in % [:safety :outcome]))
               (:core-nodes checked-core)))
        ordering-keys (set (keys expected-effect-graph))]
    (p15-s23-c11-mir-require!
     (and (= (:effect-graph checked-core) observed)
          (if effectful?
            (and (= expected-effect-graph
                    (select-keys observed ordering-keys))
                 (= (mapv :node-id (:core-nodes checked-core))
                    (:ordering-vertices observed))
                 (= (into (sorted-map)
                          (map-indexed (fn [index node]
                                         [(:node-id node) index]))
                          (:core-nodes checked-core))
                    (:ordering-index observed))
                 (= (into (sorted-map)
                          (map-indexed (fn [index node-id]
                                         [node-id index]))
                          (:event-order observed))
                    (:event-index observed))
                 (every? #(contains? #{:operand-before-consumer
                                       :adjacent-source-sequence
                                       :guard-before-exclusive-branch}
                                     (:reason %))
                         (:event-edges observed))
                 (false? (:exclusive-branch-total-order? observed))
                 (false? (:runtime-sequence-claimed? observed))
                 (= :topological-core-order-with-guarded-control-flow-partial-order
                    (:event-order-kind observed))
                 (true? (:all-edges-monotone? observed))
                 (true? (:edge-count-bounded? observed)))
            (not-any? #(contains? observed %) ordering-keys)))
     "C11-EFFECT" source-path mir
     :authenticated-partial-effect-order-graph)))

(defn p15-s23-c11-mir-validate-runtime-checks!
  [source-path checked-core mir operation-products]
  (let [nodes (:core-nodes checked-core)
        operation-by-id (:operation-by-id operation-products)
        runtime-nodes
        (filterv #(= :runtime-checked (get-in % [:safety :outcome])) nodes)
        expected-table
        (into (sorted-map)
              (map (fn [node]
                     (let [check (get-in node [:safety :check])]
                       [(:check-id check)
                        (assoc check
                               :guarded-operation-id (:node-id node)
                               :check-operation-id
                               (p15-s23-c11-mir-runtime-check-operation-id
                                check)
                               :token-value-id
                               (p15-s23-c11-mir-runtime-check-token-id
                                check))])))
              runtime-nodes)
        observed-table (:runtime-check-table mir)
        check-ids (mapv :check-id (vals observed-table))]
    (p15-s23-c11-mir-require!
     (and (= expected-table observed-table)
          (every? #(map? (get-in % [:safety :check])) runtime-nodes)
          (every? #(if (= :runtime-checked (get-in % [:safety :outcome]))
                     (map? (get-in % [:safety :check]))
                     (nil? (get-in % [:safety :check])))
                  nodes)
          (= (set (map #(get-in % [:safety :check :check-id])
                       runtime-nodes))
             (set (keys observed-table)))
          (= (count check-ids) (count (set check-ids))))
     "C11-SAFETY" source-path mir :canonical-runtime-check-table)
    (doseq [node nodes]
      (let [node-id (:node-id node)
            operation (get operation-by-id node-id)
            check (get-in node [:safety :check])
            safety-fact (get (:safety-facts checked-core) node-id)
            op-reference (get-in operation [:facts :runtime-check-id])
            op-failure (get-in operation [:facts :failure-behavior])]
        (if-not check
          (p15-s23-c11-mir-require!
           (and (not-any? #(= node-id (:guarded-operation-id %))
                          (vals observed-table))
                (= :not-applicable op-reference)
                (= :not-applicable op-failure)
                (= :not-applicable (:runtime-check safety-fact))
                (= :not-applicable (:failure-behavior safety-fact)))
           "C11-SAFETY" source-path operation
           :pure-operation-runtime-check-not-applicable)
          (let [source-operation (:source-operation node)
                expected-kind (case source-operation
                                :str :managed-allocation-result
                                :println :reference-transcript-delivery
                                :unsupported)
                expected-effect
                (first (get-in node [:attributes :intrinsic-effects]))
                expected-capability
                (first (get-in node [:attributes :intrinsic-capabilities]))]
            (p15-s23-c11-mir-require!
             (and (= (assoc check
                            :guarded-operation-id node-id
                            :check-operation-id
                            (p15-s23-c11-mir-runtime-check-operation-id check)
                            :token-value-id
                            (p15-s23-c11-mir-runtime-check-token-id check))
                     (get observed-table (:check-id check)))
                  (= (:check-id check)
                     (get-in node [:attributes :runtime-check-id])
                     (:runtime-check safety-fact)
                     op-reference)
                  (= (:failure check)
                     (:failure-behavior safety-fact)
                     op-failure)
                  (= expected-kind (:kind check))
                  (= expected-effect (:effect check))
                  (= expected-capability (:capability check))
                  (= (:path node) (:path check))
                  (= (get-in node [:source :origin-id]) (:origin-id check))
                  (= :gravity/runtime-check (:artifact check))
                  (= :required (:status check))
                  (= (p15-s23-c11-mir-runtime-check-token-id check)
                     (first (:operands operation)))
                  (keyword? (:provider check))
                  (if (= :println source-operation)
                    (and (keyword? (:handler check))
                         (= :in-memory-reference-transcript
                            (:delivery check))
                         (false? (:live-external-io? check)))
                    (not (contains? check :handler))))
             "C11-SAFETY" source-path operation
             :runtime-check-node-table-safety-operation-parity)))))
    {:runtime-check-count (count runtime-nodes)
     :runtime-check-ids check-ids}))

(defn p15-s23-c11-mir-expected-runtime-check-operation
  [node block-id]
  (let [check (get-in node [:safety :check])
        node-id (:node-id node)]
    {:artifact :gravity/mir-operation
     :op-id (p15-s23-c11-mir-runtime-check-operation-id check)
     :opcode :runtime-check
     :source-operation :runtime-check
     :operands []
     :result (p15-s23-c11-mir-runtime-check-token-id check)
     :result-kind :runtime-check-token
     :constant-payload :not-applicable
     :type :gravity/runtime-check-token
     :effects #{}
     :capabilities #{}
     :ordering :sequence
     :source (p15-s23-c11-mir-runtime-check-source node)
     :profile (:profile node)
     :facts {:type-fact-id
             (p15-s23-c11-mir-runtime-check-fact-id check :type)
             :effect-fact-id
             (p15-s23-c11-mir-runtime-check-fact-id check :effect)
             :capability-fact-id
             (p15-s23-c11-mir-runtime-check-fact-id check :capability)
             :capability-proof-ids [(:capability-proof-id check)]
             :ownership-fact-id
             (p15-s23-c11-mir-runtime-check-fact-id check :ownership)
             :safety-outcome-id
             (p15-s23-c11-mir-runtime-check-fact-id check :safety)
             :safety-proof-id :not-applicable
             :profile-target-fact-id
             (p15-s23-c11-mir-runtime-check-fact-id
              check :profile-target)
             :runtime-check-id (:check-id check)
             :proof-certificate-ids [(:capability-proof-id check)]
             :guarded-operation-id node-id
             :failure-behavior (:failure check)
             :source-origin-id (get-in node [:source :origin-id])}
     :domain-anchor nil
     :block-id block-id
     :verifier-status :pending}))