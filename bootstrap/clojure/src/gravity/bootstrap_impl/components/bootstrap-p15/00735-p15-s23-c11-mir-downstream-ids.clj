

(defn p15-s23-c11-mir-downstream-ids
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

(defn p15-s23-c11-mir-expected-block-operation-ids
  [checked-core]
  (let [nodes (:core-nodes checked-core)
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
                    [(p15-s23-c11-mir-runtime-check-operation-id check)
                     (:node-id node)]
                    [(:node-id node)]))
                selected)))]
     (if-not if-node
      {entry-id (operation-ids nodes)}
      (let [then-prefix (conj (:path if-node) :then)
            else-prefix (conj (:path if-node) :else)
            then-ids
            (set (map :node-id
                      (filter #(p15-s23-c11-mir-path-prefix?
                                then-prefix (:path %))
                              nodes)))
            else-ids
            (set (map :node-id
                      (filter #(p15-s23-c11-mir-path-prefix?
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

(defn p15-s23-c11-mir-expected-data-flow
  [nodes operation-by-id]
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
           [{:from (p15-s23-c11-mir-runtime-check-token-id check)
             :consumer-kind :operation
             :consumer-id node-id
             :consumer-block block-id
             :operand-index 0
             :edge-kind :runtime-check-guard}]
           operand-edges)
          operand-edges)))
    nodes)))

(defn p15-s23-c11-mir-expected-uses
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

(defn p15-s23-c11-mir-expected-terminator-uses
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

(defn p15-s23-c11-mir-expected-terminator-value-uses
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

(defn p15-s23-c11-mir-expected-value
  [node data-flow terminator-uses operation]
  (let [node-id (:node-id node)]
    {:artifact :gravity/mir-value
     :value-id node-id
     :kind :instruction-result
     :type (:type node)
     :constant-payload
     (if (= :constant (p15-s23-c11-mir-node-opcode node))
       {:present? true :value (get-in node [:attributes :value])}
       :not-applicable)
     :defined-by {:value-id node-id
                  :operation-id node-id
                  :block-id (:block-id operation)}
     :uses (vec (concat
                 (p15-s23-c11-mir-expected-uses data-flow node-id)
                 (p15-s23-c11-mir-expected-terminator-value-uses
                  terminator-uses node-id)))
     :source (:source node)
     :ownership (:ownership node)
     :effects (:effects node)
     :safety (:safety node)}))

(defn p15-s23-c11-mir-expected-runtime-check-value
  [node data-flow operation]
  (let [check (get-in node [:safety :check])
        value-id (p15-s23-c11-mir-runtime-check-token-id check)]
    {:artifact :gravity/mir-value
     :value-id value-id
     :kind :runtime-check-token
     :type :gravity/runtime-check-token
     :constant-payload :not-applicable
     :defined-by
     {:value-id value-id
      :operation-id (p15-s23-c11-mir-runtime-check-operation-id check)
      :block-id (:block-id operation)}
     :uses (p15-s23-c11-mir-expected-uses data-flow value-id)
     :source (p15-s23-c11-mir-runtime-check-source node)
     :ownership :not-applicable
     :effects #{}
     :safety {:outcome :runtime-check-token
              :runtime-check-id (:check-id check)
              :guarded-operation-id (:node-id node)}}))

(def p15-s23-c11-mir-module-keys
  #{:artifact :schema-version :source-core :module-id :profile
    :source-target :target-request :target-request-metadata :functions
    :globals :control-flow-graph :data-flow-graph :type-table
    :effect-table :effect-order-graph :ownership-table
    :capability-table :capability-proof-table :safety-table
    :profile-target-table :runtime-check-table
    :proof-certificate-table :source-map
    :domain-anchors :diagnostics :provenance :construction
    :pass-contract :pass-execution-record
    :verification-status :target-independent? :b1-preflight :scope
    :clojure-seed-boundary? :self-hosted?})

(def p15-s23-c11-mir-function-keys
  #{:artifact :fn-id :name :params :returns :latent-effects
    :capabilities :blocks :entry :source :facts :provenance})