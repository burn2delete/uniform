

(defn sh07-core-descendant-node-ids
  [node-by-id root-node-id]
  (loop [frontier [root-node-id]
         visited #{}]
    (if (empty? frontier)
      visited
      (let [node-id (peek frontier)
            frontier (pop frontier)
            node (get node-by-id node-id)]
        (cond
          (contains? visited node-id)
          (recur frontier visited)

          (not (map? node))
          nil

          :else
          (recur (into frontier (:children node))
                 (conj visited node-id)))))))

(def ^:private sh07-b9-handler-descriptor-keys
  [:clause-ordinal :handler-child-index
   :catch-clause-form-id :catch-clause-syntax-id
   :error-type-form-id :error-type-syntax-id
   :error-type-binding-id
   :catch-binding-form-id :catch-binding-syntax-id
   :catch-binding-id :catch-binding-scope-id])

(defn sh07-b9-handler-group-coherent?
  [node-by-id transfers references try-node handlers]
  (let [handlers (vec (sort-by :clause-ordinal handlers))
        attributes (:attributes try-node)
        protected-node-id (first (:children try-node))
        handler-node-ids (mapv :handler-core-node-id handlers)
        expected-children (into [protected-node-id] handler-node-ids)
        expected-bindings
        (vec
         (mapcat
          (juxt :error-type-binding-id :catch-binding-id)
          handlers))
        expected-indexes
        (mapv inc (range (count handlers)))
        expected-descriptors
        (mapv #(select-keys % sh07-b9-handler-descriptor-keys)
              handlers)
        protected-tree
        (sh07-core-descendant-node-ids node-by-id protected-node-id)
        owner (:evaluation-owner-function-syntax-id (first handlers))
        expected-candidates
        (when protected-tree
          (->> transfers
               (filter
                #(and (= owner
                         (:evaluation-owner-function-syntax-id %))
                      (contains? protected-tree (:core-node-id %))))
               (mapv #(select-keys % [:ordinal :core-node-id]))))
        shared-attribute-keys
        [:runtime-reachability :selection-policy :result-policy
         :authenticated-sh06-artifact-id
         :sh06-semantic-projection-id
         :type-coverage-legality :result-type-join-legality
         :effect-registry-legality
         :effect-profile-capability-legality
         :profile-error-lowering-legality
         :ownership-legality :safety-classification]]
    (and
     (seq handlers)
     (= (range (count handlers))
        (map :clause-ordinal handlers))
     (every? #(= (count handlers) (:clause-count %)) handlers)
     (= expected-indexes (mapv :handler-child-index handlers))
     (= expected-children (:children try-node))
     (= expected-bindings (:resolved-binding-ids try-node))
     (= (count handler-node-ids) (count (distinct handler-node-ids)))
     (= (count handlers)
        (count (distinct (map :catch-binding-id handlers))))
     (= (count handlers)
        (count (distinct (map :catch-binding-scope-id handlers))))
     (= :protected-then-ordered-typed-handler-candidates
        (get-in try-node [:evaluation :kind]))
     (= [{:index 0 :core-node-id protected-node-id}]
        (get-in try-node [:evaluation :order]))
     (= 0 (:protected-child-index attributes))
     (= (count handlers) (:handler-count attributes))
     (= expected-indexes (:handler-child-indexes attributes))
     (= expected-descriptors (:handler-clauses attributes))
     (= :protected-then-ordered-typed-handler-candidates
        (:evaluation-order attributes))
     (every?
      (fn [handler]
        (let [handler-tree
              (sh07-core-descendant-node-ids
               node-by-id (:handler-core-node-id handler))
              expected-use-syntax-ids
              (when handler-tree
                (->> references
                     (filter
                      #(and (= (:catch-binding-id handler)
                               (:binding-id %))
                            (contains? handler-tree
                                       (:core-node-id %))))
                     (mapv :syntax-id)))]
          (and
           (= (:node-id try-node) (:core-node-id handler))
           (= protected-node-id (:protected-core-node-id handler))
           (= expected-candidates
              (:candidate-error-transfers handler))
           (= expected-use-syntax-ids
              (:catch-binding-use-syntax-ids handler))
           (= (select-keys attributes shared-attribute-keys)
              (select-keys handler shared-attribute-keys)))))
      handlers))))

(defn sh07-core-error-handlers-coherent?
  [core]
  (let [nodes (:nodes core)
        handlers (:error-handlers core)
        transfers (:error-transfers core)
        references (:reference-uses core)
        try-nodes
        (when (vector? nodes)
          (filterv #(and (map? %) (= :try (:core-form %))) nodes))
        node-by-id
        (when (vector? nodes)
          (into {} (map (juxt :node-id identity)) nodes))
        try-by-id
        (when (vector? try-nodes)
          (into {} (map (juxt :node-id identity)) try-nodes))
        handlers-by-try
        (when (vector? handlers)
          (group-by :core-node-id handlers))]
    (and
     (vector? nodes)
     (vector? handlers)
     (vector? transfers)
     (vector? references)
     (= (count nodes) (count node-by-id))
     (= (count try-nodes) (count try-by-id))
     (= (mapv :ordinal handlers) (vec (range (count handlers))))
     (= (set (keys try-by-id)) (set (keys handlers-by-try)))
     (every?
      (fn [[try-id try-node]]
        (sh07-b9-handler-group-coherent?
         node-by-id transfers references try-node
         (get handlers-by-try try-id)))
      try-by-id))))

(def ^:private sh07-b11-match-clause-keys
  [:clause-ordinal :branch-child-index
   :pattern-kind :pattern-form-id :pattern-syntax-id :pattern-value
   :branch-form-id :branch-syntax-id
   :pattern-binding-id :pattern-binding-scope-id
   :pattern-binding-ids])

(def ^:private sh07-b11-match-pattern-record-keys
  #{:ordinal :clause-ordinal :local-ordinal
    :parent-local-ordinal :root-local-ordinal
    :parent-ordinal :root-ordinal :core-node-id
    :depth :path :pattern-kind
    :pattern-form-id :pattern-syntax-id
    :pattern-value :vector-width
    :pattern-binding-id :pattern-binding-scope-id
    :pattern-binding-use-syntax-ids
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id})

(defn sh07-b11-guard-source [guard]
  (when (map? guard) (select-keys guard [:rule-version :source-child-index :form-id :syntax-id])))

(defn sh07-b11-guard-source-valid? [guard]
  (and (map? guard) (= #{:rule-version :source-child-index :form-id :syntax-id} (set (keys guard)))
       (= :sh07-root-map-guard-v1 (:rule-version guard))
       (integer? (:source-child-index guard)) (pos? (:source-child-index guard))
       (odd? (:source-child-index guard))
       (every? p15-s23-sh02-sha256-id? ((juxt :form-id :syntax-id) guard))))

(defn sh07-b11-guard-valid? [guard ordinal]
  (let [source (sh07-b11-guard-source guard) region (:conditional-region guard)]
    (and (sh07-b11-guard-source-valid? source)
         (integer? (:child-index guard)) (pos? (:child-index guard))
         (= {:kind :match-guard :match-syntax-id (:match-syntax-id region)
             :clause-ordinal ordinal} region)
         (p15-s23-sh02-sha256-id? (:match-syntax-id region))
         (= guard (assoc source :child-index (:child-index guard) :conditional-region region
                         :evaluation :once-after-structural-success
                         :on-pattern-failure :next-candidate :on-true :branch-body
                         :on-false :next-candidate
                         :non-boolean-type-effect-legality :pending-later-legality)))))

(defn sh07-b11-branch-tree [node-by-id record]
  (let [body (sh07-core-descendant-node-ids node-by-id (:branch-core-node-id record))]
    (if (contains? record :guard)
      (into body (sh07-core-descendant-node-ids node-by-id (get-in record [:guard :node-id])))
      body)))

(defn sh07-b11-clause [record]
  (cond-> (select-keys record sh07-b11-match-clause-keys)
    (contains? record :guard) (assoc :guard (when (map? (:guard record)) (dissoc (:guard record) :node-id)))))

(defn sh07-b11-match-pattern-graph-complete?
  [pattern-records]
  (let [roots (filterv #(nil? (:parent-ordinal %)) pattern-records)
        root-groups (group-by (juxt :core-node-id :clause-ordinal) roots)]
    (and (every? #(= 1 (count %)) (vals root-groups))
         (every?
          (fn [record]
            (if (contains? #{:vector :map} (:pattern-kind record))
              (and (if (= :map (:pattern-kind record)) (map? (:pattern-value record)) true)
                   (let [children (filterv #(= (:ordinal record) (:parent-ordinal %)) pattern-records)
                         indexes (mapv #(peek (:path %)) children)
                         map-pattern? (= :map (:pattern-kind record))
                         width (if map-pattern? (count (:pattern-value record)) (:vector-width record))]
                     (and (= (- width (if (contains? record :guard) 1 0)) (count children))
                          (= (count children) (count (set indexes)))
                          (every? #(and (integer? %) (<= 0 %)
                                        (not= % (get-in record [:guard :source-child-index]))
                                        (if map-pattern? (and (odd? %) (< % (* 2 width))) (< % width)))
                                  indexes)))) true))
          pattern-records))))
