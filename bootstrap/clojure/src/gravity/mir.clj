(ns gravity.mir
  "Native seed C11 fact/use records, control-flow and use-dominance algorithms.")

(def ^:private namespace-contract
  '{:namespace gravity.mir
    :contract-boundary :native-seed-c11-control-flow-and-dominance
    :governing-contracts [D1 D2 D3 D8 D9 C1 C6 C7 C8 C9 C10 C11 C15 BOOT8 TEST2]
    :public-api
    {downstream-ids {:arglists ([outside-nodes if-id]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-block-operation-ids {:arglists ([entry-parameter! runtime-check-operation-id path-prefix? checked-core]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-data-flow {:arglists ([runtime-check-token-id nodes operation-by-id]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-uses {:arglists ([data-flow value-id]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-terminator-uses {:arglists ([checked-core]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-terminator-value-uses {:arglists ([terminator-uses value-id]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-value {:arglists ([node-opcode expected-uses expected-terminator-value-uses node data-flow terminator-uses operation] [node-opcode expected-uses expected-terminator-value-uses node data-flow terminator-uses operation parameter]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-runtime-check-value {:arglists ([runtime-check-token-id runtime-check-operation-id expected-uses runtime-check-source node data-flow operation]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     runtime-check-fact-record {:arglists ([fact-id operation-id token-id checked-core node fact-kind]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     add-runtime-check-facts {:arglists ([fact-id fact-record checked-core fact-kind table]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     reindex-fact-table {:arglists ([nodes table id-key suffix]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     profile-target-table {:arglists ([checked-core]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     capability-proof-table {:arglists ([checked-core]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     safety-proof-table {:arglists ([checked-core]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     validate-terminator! {:arglists ([require! terminator-keys source-path checked-core block expected]) :admission :none}
     validate-cfg! {:arglists ([require! validate-terminator! source-path checked-core mir envelope operation-products]) :admission :none}
     valid-dominance-edge? {:arglists ([edge blocks operation-by-id definitions position-by-id join]
                                      [edge blocks operation-by-id definitions position-by-id join parameter]) :admission :none}
     valid-terminator-use? {:arglists ([use blocks operation-by-id position-by-id]) :admission :none}}
    :artifact-inputs [:native-node-path-operand-and-operation-data
                      :explicit-parameter-opcode-id-source-and-use-operations
                      :native-checked-core-node-fact-and-proof-data
                      :explicit-runtime-check-id-and-fact-record-operations
                      :explicit-require-and-terminator-operations :terminator-key-schema
                      :checked-core-and-mir-data :expected-envelope-and-operation-products
                      :definitions :operation-positions :join-and-parameter-data]
    :artifact-outputs [:native-block-placement-data :native-use-definition-and-value-records
                       :native-runtime-check-facts :id-indexed-fact-and-proof-tables
                       :cfg-entry-exit-and-conditional-count :native-rule-outcome :dominance-and-use-predicates]
    :diagnostic-ownership {:stage [:native-use-and-placement-reference-records C11-BLOCK :terminating-pure-control-transfer :canonical-cfg-structure]
                           :existing-caller [C11-TYPE C11-EFFECT C11-SAFETY C11-MODULE C11-VERIFY
                                             :fact-table-equality-and-admission C11-DOMINANCE :predicate-refusal]
                           :supplied-require-operation [:diagnostic-construction :throwing]}
    :dependency-direction {:requires [] :consumed-by [gravity.bootstrap]
                           :forbids [gravity.bootstrap gravity.c11-mir gravity.cli gravity.release]}
    :supported-cfg-families [:single-entry-return :single-conditional-diamond]
    :authenticates-inputs? false :issues-verifier-certificates? false
    :unsupported [:proof-authentication :runtime-check-legality :untrusted-native-ingress :callback-authentication :full-c11-verification
                  :general-cfgs :loops :matches :exceptions :source-admission :b1-admission
                  :backend-or-native-execution :migration :release :self-hosting :seed-retirement]})






(defn downstream-ids
  [outside-nodes if-id]
  (loop [known #{if-id}]
    (let [expanded
          (reduce
           (fn [result node]
             (if (some result (:operands node))
               (conj result (:node-id node))
               result))
           known
           outside-nodes)]
      (if (= known expanded)
        expanded
        (recur expanded)))))

(defn expected-block-operation-ids
  [entry-parameter! runtime-check-operation-id path-prefix? checked-core]
  (let [parameter (entry-parameter! (:source-path checked-core) checked-core)
        nodes (filterv #(not= (:id parameter) (:node-id %)) (:core-nodes checked-core))
        artifact-id (:artifact-id checked-core)
        entry-id (str artifact-id ":mir:entry")
        then-id (str artifact-id ":mir:then")
        else-id (str artifact-id ":mir:else")
        join-id (str artifact-id ":mir:join")
        if-node (first (filter #(= :if (:source-operation %)) nodes))]
    (letfn [(operation-ids [selected]
              (vec
               (mapcat
                (fn [node]
                  (if-let [check (get-in node [:safety :check])]
                    [(runtime-check-operation-id check)
                     (:node-id node)]
                    [(:node-id node)]))
                selected)))]
     (if-not if-node
      {entry-id (operation-ids nodes)}
      (let [then-prefix (conj (:path if-node) :then)
            else-prefix (conj (:path if-node) :else)
            then-ids
            (set (map :node-id
                      (filter #(path-prefix?
                                then-prefix (:path %))
                              nodes)))
            else-ids
            (set (map :node-id
                      (filter #(path-prefix?
                                else-prefix (:path %))
                              nodes)))
            outside-nodes
            (vec (remove #(or (contains? then-ids (:node-id %))
                              (contains? else-ids (:node-id %)))
                         nodes))
            [entry-nodes continuation-nodes]
            (split-with #(not= (:node-id if-node) (:node-id %))
                        outside-nodes)
            entry-ids (set (map :node-id entry-nodes))
            continuation-ids (set (map :node-id continuation-nodes))
            block-for
            (fn [node]
              (let [node-id (:node-id node)]
                (cond
                  (contains? then-ids node-id) then-id
                  (contains? else-ids node-id) else-id
                  (contains? entry-ids node-id) entry-id
                  (contains? continuation-ids node-id) join-id
                  :else entry-id)))]
        (into {}
              (map (fn [block-id]
                     [block-id
                      (operation-ids
                       (filter #(= block-id (block-for %)) nodes))]))
              [entry-id then-id else-id join-id]))))))

(defn expected-data-flow
  [runtime-check-token-id nodes operation-by-id]
  (vec
   (mapcat
   (fn [node]
      (let [node-id (:node-id node)
            block-id (:block-id (get operation-by-id node-id))
            check (get-in node [:safety :check])
            operand-start (if check 1 0)
            operand-edges
            (mapv (fn [operand-index operand]
                    {:from operand
                     :consumer-kind :operation
                     :consumer-id node-id
                     :consumer-block block-id
                     :operand-index operand-index
                     :edge-kind :operand})
                  (range operand-start
                         (+ operand-start (count (:operands node))))
                  (:operands node))]
        (if check
          (into
           [{:from (runtime-check-token-id check)
             :consumer-kind :operation
             :consumer-id node-id
             :consumer-block block-id
             :operand-index 0
             :edge-kind :runtime-check-guard}]
           operand-edges)
          operand-edges)))
    nodes)))

(defn expected-uses
  [data-flow value-id]
  (vec
   (for [edge data-flow
         :when (= value-id (:from edge))]
     {:use-kind :instruction
      :consumer-kind (:consumer-kind edge)
      :consumer-id (:consumer-id edge)
      :consumer-block (:consumer-block edge)
      :operand-index (:operand-index edge)
      :edge-kind (:edge-kind edge)})))

(defn expected-terminator-uses
  [checked-core]
  (let [nodes (:core-nodes checked-core)
        artifact-id (:artifact-id checked-core)
        root-id (first (:root-node-ids checked-core))
        root-node (first (filter #(= root-id (:node-id %)) nodes))
        return-id (last (:operands root-node))
        entry-id (str artifact-id ":mir:entry")
        then-id (str artifact-id ":mir:then")
        else-id (str artifact-id ":mir:else")
        join-id (str artifact-id ":mir:join")
        if-node (first (filter #(= :if (:source-operation %)) nodes))]
    (if-not if-node
      [{:value-id return-id
        :consumer-kind :terminator
        :consumer-id (str entry-id ":return")
        :consumer-block entry-id
        :operand-index 0
        :edge-kind :return-value}]
      (let [[condition-id then-result-id else-result-id]
            (:operands if-node)]
        [{:value-id condition-id
          :consumer-kind :terminator
          :consumer-id (str entry-id ":conditional")
          :consumer-block entry-id
          :operand-index 0
          :edge-kind :condition}
         {:value-id then-result-id
          :consumer-kind :terminator
          :consumer-id (str then-id ":branch")
          :consumer-block then-id
          :operand-index 0
          :edge-kind :branch-value}
         {:value-id else-result-id
          :consumer-kind :terminator
          :consumer-id (str else-id ":branch")
          :consumer-block else-id
          :operand-index 0
          :edge-kind :branch-value}
         {:value-id return-id
          :consumer-kind :terminator
          :consumer-id (str join-id ":return")
          :consumer-block join-id
          :operand-index 0
          :edge-kind :return-value}]))))

(defn expected-terminator-value-uses
  [terminator-uses value-id]
  (vec
   (for [use terminator-uses
         :when (= value-id (:value-id use))]
     {:use-kind :terminator
      :consumer-kind (:consumer-kind use)
      :consumer-id (:consumer-id use)
      :consumer-block (:consumer-block use)
      :operand-index (:operand-index use)
      :edge-kind (:edge-kind use)})))

(defn expected-value
  ([node-opcode expected-uses expected-terminator-value-uses node data-flow terminator-uses operation]
   (expected-value node-opcode expected-uses expected-terminator-value-uses node data-flow terminator-uses operation nil))
  ([node-opcode expected-uses expected-terminator-value-uses node data-flow terminator-uses operation parameter]
  (let [node-id (:node-id node)]
    {:artifact :gravity/mir-value
     :value-id node-id
     :kind (if parameter :parameter :instruction-result)
     :type (:type node)
     :constant-payload
     (if (and (nil? parameter) (= :constant (node-opcode node)))
       {:present? true :value (get-in node [:attributes :value])}
       :not-applicable)
     :defined-by (or (:definition parameter) {:value-id node-id
                  :operation-id node-id
                  :block-id (:block-id operation)})
     :uses (vec (concat
                 (expected-uses data-flow node-id)
                 (expected-terminator-value-uses
                  terminator-uses node-id)))
     :source (:source node)
     :ownership (:ownership node)
     :effects (:effects node)
     :safety (:safety node)})))

(defn expected-runtime-check-value
  [runtime-check-token-id runtime-check-operation-id expected-uses runtime-check-source node data-flow operation]
  (let [check (get-in node [:safety :check])
        value-id (runtime-check-token-id check)]
    {:artifact :gravity/mir-value
     :value-id value-id
     :kind :runtime-check-token
     :type :gravity/runtime-check-token
     :constant-payload :not-applicable
     :defined-by
     {:value-id value-id
      :operation-id (runtime-check-operation-id check)
      :block-id (:block-id operation)}
     :uses (expected-uses data-flow value-id)
     :source (runtime-check-source node)
     :ownership :not-applicable
     :effects #{}
     :safety {:outcome :runtime-check-token
              :runtime-check-id (:check-id check)
              :guarded-operation-id (:node-id node)}}))


(defn runtime-check-fact-record
  [fact-id operation-id token-id checked-core node fact-kind]
  (let [node-id (:node-id node)
        check (get-in node [:safety :check])
        fact-id (fact-id check fact-kind)
        check-op-id (operation-id check)
        token-id (token-id check)
        origin-id (get-in node [:source :origin-id])
        profile (:profile node)]
    (case fact-kind
      :type
      {:fact-id fact-id
       :core-node-id node-id
       :mir-operation-id check-op-id
       :type-id :gravity/runtime-check-token
       :type :gravity/runtime-check-token
       :producer-rule :runtime-check-token
       :dependencies []
       :source-origin-id origin-id
       :profile profile
       :source-target (:source-target checked-core)
       :constraints []
       :derived? true}

      :effect
      {:fact-id fact-id
       :core-node-id node-id
       :mir-operation-id check-op-id
       :direct #{}
       :latent #{}
       :transitive #{}
       :residual #{}
       :ordering :sequence
       :non-dce? true
       :source-origin-id origin-id
       :profile profile
       :source-target (:source-target checked-core)
       :derived? true}

      :capability
      {:fact-id fact-id
       :core-node-id node-id
       :mir-operation-id check-op-id
       :required #{}
       :granted #{}
       :capability-proof-ids [(:capability-proof-id check)]
       :authority-id (:authority-record-id check)
       :source-origin-id origin-id
       :profile profile
       :source-target (:source-target checked-core)
       :derived? true}

      :ownership
      {:fact-id fact-id
       :core-node-id node-id
       :mir-operation-id check-op-id
       :value-id token-id
       :role :runtime-check-token
       :storage :compiler-generated-token
       :model :non-resource-control-token
       :shareability :single-guard-use
       :mutation :forbidden
       :source-origin-id origin-id
       :profile profile
       :source-target (:source-target checked-core)
       :derived? true}

      :safety
      {:outcome-id fact-id
       :artifact :gravity/safety-outcome
       :operation check-op-id
       :guarded-operation-id node-id
       :kind :runtime-check
       :profile profile
       :target (:source-target checked-core)
       :outcome :runtime-checked
       :runtime-check (:check-id check)
       :proof :not-applicable
       :failure-behavior (:failure check)
       :source-origin-id origin-id
       :derived? true}

      :profile-target
      {:fact-id fact-id
       :core-node-id node-id
       :mir-operation-id check-op-id
       :profile-fact (get (:profile-facts checked-core) node-id)
       :profile profile
       :source-target (:source-target checked-core)
       :target-request
       (get-in checked-core [:target-request-metadata :requested-target])
       :derived? true})))

(defn add-runtime-check-facts
  [fact-id fact-record checked-core fact-kind table]
  (reduce
   (fn [result node]
     (if-let [check (get-in node [:safety :check])]
       (assoc result
              (fact-id check fact-kind)
              (fact-record
               checked-core node fact-kind))
       result))
   table
   (:core-nodes checked-core)))

(defn reindex-fact-table
  [nodes table id-key suffix]
  (into (sorted-map)
        (map (fn [node]
               (let [node-id (:node-id node)
                     record (get table node-id)
                     fact-id (or (get record id-key)
                                 (str node-id suffix))]
                 [fact-id (assoc record id-key fact-id)])))
        nodes))

(defn profile-target-table
  [checked-core]
  (into (sorted-map)
        (map (fn [node]
               (let [node-id (:node-id node)
                     fact-id (str node-id ":profile-target-fact")]
                 [fact-id
                  {:fact-id fact-id
                   :core-node-id node-id
                   :profile-fact (get (:profile-facts checked-core) node-id)
                   :profile (:profile node)
                   :source-target (:source-target checked-core)
                   :target-request
                   (get-in checked-core
                           [:target-request-metadata :requested-target])}])))
        (:core-nodes checked-core)))

(defn capability-proof-table
  [checked-core]
  (into (sorted-map)
        (map (juxt :proof-id identity))
        (:capability-proof-records checked-core)))

(defn safety-proof-table
  [checked-core]
  (into (sorted-map)
        (keep (fn [node]
                (when-let [proof-id (get-in node [:safety :proof :proof-id])]
                  [proof-id (get-in node [:safety :proof])])) )
        (:core-nodes checked-core)))


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

(doseq
 [name (keys (:public-api namespace-contract))
  :let [operation (get (ns-interns *ns*) name)]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
