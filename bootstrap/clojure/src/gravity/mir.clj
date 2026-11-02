(ns gravity.mir
  "Native seed C11 control-flow and use-dominance algorithms.")

(def ^:private namespace-contract
  '{:namespace gravity.mir
    :contract-boundary :native-seed-c11-control-flow-and-dominance
    :governing-contracts [D1 D2 D3 D8 D9 C1 C6 C7 C8 C9 C10 C11 C15 BOOT8 TEST2]
    :public-api
    {validate-terminator! {:arglists ([require! terminator-keys source-path checked-core block expected]) :admission :none}
     validate-cfg! {:arglists ([require! validate-terminator! source-path checked-core mir envelope operation-products]) :admission :none}
     valid-dominance-edge? {:arglists ([edge blocks operation-by-id definitions position-by-id join]
                                      [edge blocks operation-by-id definitions position-by-id join parameter]) :admission :none}
     valid-terminator-use? {:arglists ([use blocks operation-by-id position-by-id]) :admission :none}}
    :artifact-inputs [:explicit-require-and-terminator-operations :terminator-key-schema
                      :checked-core-and-mir-data :expected-envelope-and-operation-products
                      :definitions :operation-positions :join-and-parameter-data]
    :artifact-outputs [:cfg-entry-exit-and-conditional-count :native-rule-outcome :dominance-and-use-predicates]
    :diagnostic-ownership {:stage [C11-BLOCK :terminating-pure-control-transfer :canonical-cfg-structure]
                           :existing-caller [C11-DOMINANCE :predicate-refusal]
                           :supplied-require-operation [:diagnostic-construction :throwing]}
    :dependency-direction {:requires [] :consumed-by [gravity.bootstrap]
                           :forbids [gravity.bootstrap gravity.c11-mir gravity.cli gravity.release]}
    :supported-cfg-families [:single-entry-return :single-conditional-diamond]
    :authenticates-inputs? false :issues-verifier-certificates? false
    :unsupported [:untrusted-native-ingress :callback-authentication :full-c11-verification
                  :general-cfgs :loops :matches :exceptions :source-admission :b1-admission
                  :backend-or-native-execution :migration :release :self-hosting :seed-retirement]})


(defn validate-terminator!
  [require! terminator-keys source-path checked-core block expected]
  (let [terminator (:terminator block)]
    (require!
     (and (map? terminator)
          (= terminator-keys
             (set (keys terminator)))
          (= :gravity/mir-terminator (:artifact terminator))
          (= (:terminator-id expected) (:terminator-id terminator))
          (= (:kind expected) (:kind terminator))
          (vector? (:operands terminator))
          (= (:operands expected) (:operands terminator))
          (vector? (:successors terminator))
          (= (:successors expected) (:successors terminator))
          (= #{} (:effects terminator))
          (= :none (:ordering terminator))
          (= (:source expected) (:source terminator))
          (= (:profile checked-core) (:profile terminator))
          (= (:facts expected) (:facts terminator))
          (= :pending (:verifier-status terminator)))
     "C11-BLOCK" source-path terminator :terminating-pure-control-transfer)))

(defn validate-cfg!
  [require! validate-terminator! source-path checked-core mir envelope operation-products]
  (let [blocks (:blocks operation-products)
        artifact-id (:artifact-id checked-core)
        entry-id (str artifact-id ":mir:entry")
        then-id (str artifact-id ":mir:then")
        else-id (str artifact-id ":mir:else")
        join-id (str artifact-id ":mir:join")
        if-nodes (vec (filter #(= :if (:source-operation %))
                              (:nodes operation-products)))
        conditional-count (count if-nodes)
        cfg (:control-flow-graph mir)
        function (:function envelope)
        return-id (:return-id envelope)]
    (require!
     (and (contains? #{0 1} conditional-count)
          (= entry-id (:entry function))
          (= entry-id (:entry cfg)))
     "C11-BLOCK" source-path mir :bounded-canonical-c11-cfg-entry)
    (if (zero? conditional-count)
      (let [source (:source (first (:nodes operation-products)))
            block (get blocks entry-id)]
        (require!
         (and (= #{entry-id} (set (keys blocks)))
              (vector? (:edges cfg))
              (= [] (:edges cfg))
              (map? (:dominance cfg))
              (every? vector? (vals (:dominance cfg)))
              (= {entry-id [entry-id]} (:dominance cfg))
              (nil? (:join cfg))
              (= [] (:arguments block))
              (= [] (:predecessors block))
              (= [] (:successors block))
              (= [entry-id] (:dominators block))
              (= source (:source block)))
         "C11-BLOCK" source-path block :canonical-linear-c11-cfg)
        (validate-terminator!
         source-path checked-core block
         {:terminator-id (str entry-id ":return")
          :kind :return
          :operands [return-id]
          :successors []
          :source source
          :facts {:returned-value return-id}}))
      (let [if-node (first if-nodes)
            if-id (:node-id if-node)
            [condition-id then-result-id else-result-id] (:operands if-node)
            source (:source if-node)
            expected-edges
            [{:from entry-id :to then-id :kind :true}
             {:from entry-id :to else-id :kind :false}
             {:from then-id :to join-id :kind :join}
             {:from else-id :to join-id :kind :join}]
            expected-dominance
            {entry-id [entry-id]
             then-id [entry-id then-id]
             else-id [entry-id else-id]
             join-id [entry-id join-id]}
            entry-block (get blocks entry-id)
            then-block (get blocks then-id)
            else-block (get blocks else-id)
            join-block (get blocks join-id)]
        (require!
         (and (= #{entry-id then-id else-id join-id} (set (keys blocks)))
              (vector? (:edges cfg))
              (= expected-edges (:edges cfg))
              (map? (:dominance cfg))
              (every? vector? (vals (:dominance cfg)))
              (= expected-dominance (:dominance cfg))
              (vector? (get-in cfg [:join :incoming]))
              (= {:block-id join-id
                  :value-id if-id
                  :incoming
                  [{:predecessor then-id :value then-result-id}
                   {:predecessor else-id :value else-result-id}]}
                 (:join cfg))
              (= [] (:predecessors entry-block))
              (= [then-id else-id] (:successors entry-block))
              (= [entry-id] (:dominators entry-block))
              (= [entry-id] (:predecessors then-block))
              (= [join-id] (:successors then-block))
              (= [entry-id then-id] (:dominators then-block))
              (= [entry-id] (:predecessors else-block))
              (= [join-id] (:successors else-block))
              (= [entry-id else-id] (:dominators else-block))
              (= [then-id else-id] (:predecessors join-block))
              (= [] (:successors join-block))
              (= [entry-id join-id] (:dominators join-block))
              (= source (:source entry-block))
              (= source (:source then-block))
              (= source (:source else-block))
              (= source (:source join-block))
              ;; The explicit :conditional-join instruction is the one SSA
              ;; definition for if-id; CFG :join metadata owns the incoming
              ;; predecessor/value pairing, so no duplicate block argument is
              ;; permitted.
              (= [] (:arguments join-block)))
         "C11-BLOCK" source-path mir :canonical-single-conditional-c11-cfg)
        (validate-terminator!
         source-path checked-core entry-block
         {:terminator-id (str entry-id ":conditional")
          :kind :conditional-branch
          :operands [condition-id]
          :successors [then-id else-id]
          :source source
          :facts {:condition condition-id :join join-id}})
        (validate-terminator!
         source-path checked-core then-block
         {:terminator-id (str then-id ":branch")
          :kind :branch
          :operands [then-result-id]
          :successors [join-id]
          :source source
          :facts {:incoming-value then-result-id}})
        (validate-terminator!
         source-path checked-core else-block
         {:terminator-id (str else-id ":branch")
          :kind :branch
          :operands [else-result-id]
          :successors [join-id]
          :source source
          :facts {:incoming-value else-result-id}})
        (validate-terminator!
         source-path checked-core join-block
         {:terminator-id (str join-id ":return")
          :kind :return
          :operands [return-id]
          :successors []
          :source source
          :facts {:returned-value return-id}})))
    {:conditional-count conditional-count
     :entry-id entry-id
     :exit-id (if (zero? conditional-count) entry-id join-id)}))

(defn valid-dominance-edge?
  ([edge blocks operation-by-id definitions position-by-id join]
   (valid-dominance-edge? edge blocks operation-by-id definitions position-by-id join nil))
  ([edge blocks operation-by-id definitions position-by-id join parameter]
  (let [from-definition (get definitions (:from edge))
        from-operation
        (get operation-by-id (:operation-id from-definition))
        to-operation (get operation-by-id (:consumer-id edge))
        definition-block (:block-id from-operation)
        consumer-block (:consumer-block edge)
        dominators (:dominators (get blocks consumer-block))
        same-block? (= definition-block consumer-block)
        definition-operation-id (:operation-id from-definition)
        ordered-within-block?
        (and same-block?
             (integer? (get position-by-id definition-operation-id))
             (integer? (get position-by-id (:consumer-id edge)))
             (< (get position-by-id definition-operation-id)
                (get position-by-id (:consumer-id edge))))
        predecessor-phi?
        (and (= :conditional-join (:opcode to-operation))
             (= consumer-block (:block-id join))
             (= (:consumer-id edge) (:value-id join))
             (some #(= {:predecessor definition-block
                        :value (:from edge)}
                       %)
                   (:incoming join)))]
    (and (or (map? from-operation)
             (and (= (:from edge) (:id parameter))
                  (= from-definition (:definition parameter))
                  (= :operand (:edge-kind edge))
                  (contains? (set dominators) (:entry-id parameter))))
         (map? to-operation)
         (= :operation (:consumer-kind edge))
         (= (if (= :runtime-check (:opcode from-operation))
              :runtime-check-guard
              :operand)
            (:edge-kind edge))
         (if (= :runtime-check-guard (:edge-kind edge))
           (zero? (:operand-index edge))
           true)
         (= consumer-block (:block-id to-operation))
         (or (and (= (:from edge) (:id parameter)) (= from-definition (:definition parameter)))
             ordered-within-block?
             (and (not same-block?)
                  (contains? (set dominators) definition-block))
             predecessor-phi?)))))

(defn valid-terminator-use?
  [use blocks operation-by-id position-by-id]
  (let [value-operation (get operation-by-id (:value-id use))
        definition-block (:block-id value-operation)
        consumer-block (:consumer-block use)
        block (get blocks consumer-block)
        terminator (:terminator block)
        operand-index (:operand-index use)
        expected-edge-kind
        (case (:kind terminator)
          :conditional-branch :condition
          :branch :branch-value
          :return :return-value
          :unsupported)
        same-block? (= definition-block consumer-block)
        dominates?
        (or (and same-block?
                 (integer? (get position-by-id (:value-id use))))
            (and (not same-block?)
                 (contains? (set (:dominators block)) definition-block)))]
    (and (= #{:value-id :consumer-kind :consumer-id :consumer-block
              :operand-index :edge-kind}
            (set (keys use)))
         (= :terminator (:consumer-kind use))
         (map? value-operation)
         (map? block)
         (map? terminator)
         (= (:consumer-id use) (:terminator-id terminator))
         (integer? operand-index)
         (<= 0 operand-index)
         (< operand-index (count (:operands terminator)))
         (= (:value-id use) (nth (:operands terminator) operand-index))
         (= expected-edge-kind (:edge-kind use))
         dominates?)))

(doseq [operation [#'validate-terminator! #'validate-cfg! #'valid-dominance-edge? #'valid-terminator-use?]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
