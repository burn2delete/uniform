

(defn p15-s23-c11-mir-validate-operations!
  [source-path checked-core mir]
  (let [nodes (:core-nodes checked-core)
        node-by-id (into {} (map (juxt :node-id identity)) nodes)
        runtime-nodes
        (filterv #(map? (get-in % [:safety :check])) nodes)
        operations (p15-s23-c11-mir-operation-sequence mir)
        operation-by-id (into {} (map (juxt :op-id identity)) operations)
        runtime-operation-ids
        (set (map #(p15-s23-c11-mir-runtime-check-operation-id
                    (get-in % [:safety :check]))
                  runtime-nodes))
        block-by-id
        (get-in mir [:functions (:entrypoint checked-core) :blocks])
        expected-block-operation-ids
        (p15-s23-c11-mir-expected-block-operation-ids checked-core)
        observed-block-operation-ids
        (into {}
              (map (fn [[block-id block]]
                     [block-id (mapv :op-id (:instructions block))]))
              block-by-id)]
    (p15-s23-c11-mir-require!
     (and (= (+ (count nodes) (count runtime-nodes))
             (count operations))
          (= (set/union (set (keys node-by-id)) runtime-operation-ids)
             (set (keys operation-by-id)))
          (= (count operations) (count operation-by-id)))
     "C11-MODULE" source-path mir
     :one-core-operation-plus-one-derived-operation-per-runtime-check)
    (p15-s23-c11-mir-require!
     (= expected-block-operation-ids observed-block-operation-ids)
     "C11-DOMINANCE" source-path mir
     :canonical-checked-core-instruction-order-and-placement)
    (doseq [[block-id block] block-by-id]
      (p15-s23-c11-mir-require!
       (and (map? block)
            (= p15-s23-c11-mir-block-keys (set (keys block)))
            (= :gravity/mir-block (:artifact block))
            (= block-id (:block-id block))
            (vector? (:arguments block))
            (vector? (:instructions block))
            (vector? (:predecessors block))
            (vector? (:successors block))
            (vector? (:dominators block))
            (= {:module-data-flow-graph-reference true}
               (:data-flow block)))
       "C11-BLOCK" source-path block :well-formed-mir-block)
      (let [positions
            (into {} (map-indexed (fn [index operation]
                                   [(:op-id operation) index])
                                 (:instructions block)))]
        (doseq [node (filter #(= block-id
                                  (:block-id
                                   (get operation-by-id (:node-id %))))
                             nodes)]
          (let [node-id (:node-id node)
                operation (get operation-by-id node-id)
                attributes (:attributes node)
                expected-effects (:intrinsic-effects attributes)
                expected-capabilities (:intrinsic-capabilities attributes)
                facts (p15-s23-c11-mir-node-facts checked-core node)
                check (get-in node [:safety :check])]
            (p15-s23-c11-mir-require!
             (and (= p15-s23-c11-mir-operation-keys
                     (set (keys operation)))
                  (= :gravity/mir-operation (:artifact operation))
                  (= node-id (:op-id operation))
                  (= (p15-s23-c11-mir-node-opcode node)
                     (:opcode operation))
                  (contains? p15-s23-c11-mir-allowed-opcodes
                             (:opcode operation))
                  (contains? p15-s23-c11-mir-allowed-source-operations
                             (:source-operation operation))
                  (= (:source-operation node) (:source-operation operation))
                  (vector? (:operands operation))
                  (= (p15-s23-c11-mir-node-operands node)
                     (:operands operation))
                  (= node-id (:result operation))
                  (= (if (= :gravity/nil (:type node)) :unit :value)
                     (:result-kind operation))
                  (= (if (= :constant
                            (p15-s23-c11-mir-node-opcode node))
                       {:present? true
                        :value (get-in node [:attributes :value])}
                       :not-applicable)
                     (:constant-payload operation))
                  (= block-id (:block-id operation))
                  (= :pending (:verifier-status operation))
                  (nil? (:domain-anchor operation)))
             (if (contains? p15-s23-c11-mir-allowed-opcodes
                            (:opcode operation))
               "C11-MODULE"
               "C11-TARGET-LEAK")
             source-path operation :checked-core-operation-parity)
            (when (contains?
                   #{:integer-eq :integer-lt :integer-lte
                     :integer-gt :integer-gte}
                   (:source-operation node))
              (let [source-operation (:source-operation node)
                    equality? (= :integer-eq source-operation)
                    primitive
                    (case source-operation
                      :integer-eq '=
                      :integer-lt '<
                      :integer-lte '<=
                      :integer-gt '>
                      :integer-gte '>=)
                    basis
                    (if equality?
                      :exact-signed-integer-equality
                      :exact-integer-ordering)
                    attribute-keys
                    (cond->
                     [:primitive :arity :evaluation-order
                      :operand-types :result-type
                      :numeric-semantics :overflow]
                      (not equality?)
                      (conj :numeric-mode :mathematical-mode))
                    expected-attributes
                    (cond->
                     {:primitive primitive
                      :arity 2
                      :evaluation-order :left-to-right
                      :operand-types
                      [:gravity/integer :gravity/integer]
                      :result-type :gravity/bool
                      :numeric-semantics basis
                      :overflow :not-applicable}
                      (not equality?)
                      (assoc :numeric-mode :proof-required
                             :mathematical-mode :integer-exact))
                    schema-missing
                    (if equality?
                      :exact-binary-integer-equality-schema
                      :exact-binary-integer-order-comparison-schema)
                    proof-missing
                    (if equality?
                      :authenticated-pure-integer-equality-proof
                      :authenticated-pure-integer-order-comparison-proof)
                    [left-id right-id] (:operands node)
                    left (get node-by-id left-id)
                    right (get node-by-id right-id)
                    proof (get-in node [:safety :proof])]
                (p15-s23-c11-mir-require!
                 (and (= 2 (count (:operands node)))
                      (map? left) (map? right)
                      (= :gravity/integer (:type left) (:type right))
                      (= :gravity/bool (:type node) (:type operation))
                      (= :not-applicable (:constant-payload operation))
                      (= expected-attributes
                         (select-keys (:attributes node) attribute-keys)))
                 "C11-TYPE" source-path operation
                 schema-missing)
                (p15-s23-c11-mir-require!
                 (and (= :proven-safe (get-in node [:safety :outcome]))
                      (= basis (get-in node [:safety :basis]))
                      (= basis (:basis proof))
                      (= source-operation (:source-operation proof))
                      (= [left-id right-id] (:operand-node-ids proof))
                      (= :gravity/bool (:type proof))
                      (or equality?
                          (and (= :proof-required (:numeric-mode proof))
                               (= :integer-exact
                                  (:mathematical-mode proof))
                               (= :exact-integer-ordering
                                  (:numeric-semantics proof))))
                      (empty? (:effects operation))
                      (empty? (:capabilities operation)))
                 "C11-SAFETY" source-path operation
                 proof-missing)))
            (p15-s23-c11-mir-require!
             (and (= (:type node) (:type operation))
                  (contains? (:type-table mir) (:type-fact-id facts))
                  (contains? (:effect-table mir) (:effect-fact-id facts))
                  (contains? (:capability-table mir)
                             (:capability-fact-id facts))
                  (contains? (:ownership-table mir)
                             (:ownership-fact-id facts))
                  (contains? (:safety-table mir)
                             (:safety-outcome-id facts))
                  (contains? (:profile-target-table mir)
                             (:profile-target-fact-id facts))
                  (every? #(contains? (:capability-proof-table mir) %)
                          (:capability-proof-ids facts))
                  (every? #(or (contains? (:capability-proof-table mir) %)
                               (contains? (get-in mir
                                                 [:proof-certificate-table
                                                  :safety-proofs]) %))
                          (:proof-certificate-ids facts)))
             "C11-TYPE" source-path operation
             :resolvable-id-referenced-operation-facts)
            (p15-s23-c11-mir-require!
             (and (= expected-effects (:effects operation))
                  (= expected-capabilities (:capabilities operation))
                  (= (if (empty? expected-effects) :none :sequence)
                     (:ordering operation))
                  (= (:profile node) (:profile operation)))
             "C11-EFFECT" source-path operation
             :direct-intrinsic-effect-capability-ordering)
            (p15-s23-c11-mir-require!
             (= facts (:facts operation))
             "C11-SAFETY" source-path operation
             :linked-c6-c10-id-references)
            (p15-s23-c11-mir-require!
             (= (:source node) (:source operation))
             "C11-ORIGIN" source-path operation :checked-core-source-origin)
            (when check
              (let [check-op-id
                    (p15-s23-c11-mir-runtime-check-operation-id check)
                    check-operation (get operation-by-id check-op-id)
                    check-facts (:facts check-operation)]
                (p15-s23-c11-mir-require!
                 (and (= (p15-s23-c11-mir-expected-runtime-check-operation
                          node block-id)
                         check-operation)
                      (= (dec (get positions node-id))
                         (get positions check-op-id))
                      (contains? (:type-table mir)
                                 (:type-fact-id check-facts))
                      (contains? (:effect-table mir)
                                 (:effect-fact-id check-facts))
                      (contains? (:capability-table mir)
                                 (:capability-fact-id check-facts))
                      (contains? (:ownership-table mir)
                                 (:ownership-fact-id check-facts))
                      (contains? (:safety-table mir)
                                 (:safety-outcome-id check-facts))
                      (contains? (:profile-target-table mir)
                                 (:profile-target-fact-id check-facts))
                      (every? #(contains? (:capability-proof-table mir) %)
                              (:capability-proof-ids check-facts))
                      (every? #(contains? (:capability-proof-table mir) %)
                              (:proof-certificate-ids check-facts)))
                 "C11-SAFETY" source-path check-operation
                 :derived-runtime-check-operation-facts-and-guard)))))))
    {:nodes nodes
     :node-by-id node-by-id
     :runtime-nodes runtime-nodes
     :operations operations
     :operation-by-id operation-by-id
     :blocks block-by-id}))