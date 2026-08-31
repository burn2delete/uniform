

(defn sh07-b11-match-pattern-record-coherent?
  [node-by-id references branch-by-clause record record-by-ordinal]
  (let [parent (when-some [ordinal (:parent-ordinal record)]
                 (get record-by-ordinal ordinal))
        root (get record-by-ordinal (:root-ordinal record))
        branch-record (get branch-by-clause (:clause-ordinal record))
        branch-tree
        (when branch-record
          (sh07-core-descendant-node-ids
           node-by-id (:branch-core-node-id branch-record)))
        expected-uses
        (if (:pattern-binding-id record)
          (->> references
               (filter
                #(and (= (:pattern-binding-id record) (:binding-id %))
                      (contains? branch-tree (:core-node-id %))))
               (mapv :syntax-id))
          [])]
    (and
     (= sh07-b11-match-pattern-record-keys
        (set (keys record)))
     (integer? (:ordinal record))
     (not (neg? (:ordinal record)))
     (integer? (:clause-ordinal record))
     (not (neg? (:clause-ordinal record)))
     (integer? (:local-ordinal record))
     (not (neg? (:local-ordinal record)))
     (= 0 (:root-local-ordinal record))
     (integer? (:root-ordinal record))
     (<= 0 (:root-ordinal record) (:ordinal record))
     (= (:local-ordinal record)
        (- (:ordinal record) (:root-ordinal record)))
     (integer? (:depth record))
     (<= 0 (:depth record) 64)
     (vector? (:path record))
     (= (:depth record) (count (:path record)))
     (every? #(and (integer? %) (not (neg? %))) (:path record))
     (contains? #{:literal :wildcard :binding :vector}
                (:pattern-kind record))
     (p15-s23-sh02-sha256-id? (:pattern-form-id record))
     (p15-s23-sh02-sha256-id? (:pattern-syntax-id record))
     (p15-s23-sh02-sha256-id? (:core-node-id record))
     (p15-s23-sh02-sha256-id?
      (:authenticated-sh06-artifact-id record))
     (p15-s23-sh02-sha256-id?
      (:sh06-semantic-projection-id record))
     (= expected-uses (:pattern-binding-use-syntax-ids record))
     (case (:pattern-kind record)
       :vector
       (and (nil? (:pattern-value record))
            (integer? (:vector-width record))
            (<= 0 (:vector-width record) 256)
            (nil? (:pattern-binding-id record))
            (nil? (:pattern-binding-scope-id record)))

       :binding
       (and (symbol? (:pattern-value record))
            (not= '_ (:pattern-value record))
            (p15-s23-sh02-sha256-id?
             (:pattern-binding-id record))
            (p15-s23-sh02-sha256-id?
             (:pattern-binding-scope-id record))
            (nil? (:vector-width record)))

       :wildcard
       (and (= '_ (:pattern-value record))
            (nil? (:vector-width record))
            (nil? (:pattern-binding-id record))
            (nil? (:pattern-binding-scope-id record)))

       :literal
       (and (nil? (:vector-width record))
            (nil? (:pattern-binding-id record))
            (nil? (:pattern-binding-scope-id record))))
     (if (nil? parent)
       (and (= (:ordinal record) (:root-ordinal record))
            (= 0 (:local-ordinal record))
            (nil? (:parent-local-ordinal record))
            (= 0 (:depth record))
            (empty? (:path record)))
       (and (= :vector (:pattern-kind parent))
            (= (:core-node-id record) (:core-node-id parent))
            (= (:clause-ordinal record) (:clause-ordinal parent))
            (= (:root-ordinal record) (:root-ordinal parent))
            (= (:parent-local-ordinal record)
               (:local-ordinal parent))
            (= (:depth record) (inc (:depth parent)))
            (= (:path parent) (pop (:path record)))
            (< (peek (:path record)) (:vector-width parent))))
     (= (:core-node-id record) (:core-node-id root))
     (= (:clause-ordinal record) (:clause-ordinal root)))))

(defn sh07-b11-match-pattern-graph-complete?
  [pattern-records]
  (let [roots (filterv #(nil? (:parent-ordinal %)) pattern-records)
        root-groups (group-by (juxt :core-node-id :clause-ordinal) roots)]
    (and
     (every? #(= 1 (count %)) (vals root-groups))
     (every?
      (fn [record]
        (if (= :vector (:pattern-kind record))
          (let [children
                (filterv
                 #(= (:ordinal record) (:parent-ordinal %))
                 pattern-records)
                child-indexes (mapv #(peek (:path %)) children)]
            (and
             (= (:vector-width record) (count children))
             (= (count children) (count (set child-indexes)))
             (every?
              #(and (integer? %)
                    (<= 0 %)
                    (< % (:vector-width record)))
              child-indexes)))
          true))
      pattern-records))))

(defn sh07-b11-match-group-coherent?
  [node-by-id references match-node records skeleton pattern-records]
  (let [records (vec (sort-by :clause-ordinal records))
        pattern-records (vec (sort-by :ordinal pattern-records))
        pattern-record-by-ordinal
        (into {} (map (juxt :ordinal identity)) pattern-records)
        branch-by-clause
        (into {} (map (juxt :clause-ordinal identity)) records)
        attributes (:attributes match-node)
        scrutinee-node-id (first (:children match-node))
        branch-node-ids (vec (rest (:children match-node)))
        expected-indexes (mapv inc (range (count records)))
        expected-bindings
        (vec (keep :pattern-binding-id pattern-records))
        expected-clauses
        (mapv #(select-keys % sh07-b11-match-clause-keys) records)]
    (and
     (seq records)
     (= (range (count records)) (map :clause-ordinal records))
     (every? #(= (count records) (:clause-count %)) records)
     (= expected-indexes (mapv :branch-child-index records))
     (= branch-node-ids (mapv :branch-core-node-id records))
     (= #{scrutinee-node-id}
        (set (map :scrutinee-core-node-id records)))
     (= expected-bindings (:resolved-binding-ids match-node))
     (= 0 (:scrutinee-child-index attributes))
     (= (count records) (:branch-count attributes))
     (= expected-indexes (:branch-child-indexes attributes))
     (= expected-clauses (:branch-clauses attributes))
     (= (set (range (count records)))
        (set (map :clause-ordinal pattern-records)))
     (sh07-b11-match-pattern-graph-complete? pattern-records)
     (every?
      #(sh07-b11-match-pattern-record-coherent?
        node-by-id references branch-by-clause %
        pattern-record-by-ordinal)
      pattern-records)
     (= :scrutinee-then-source-ordered-pattern-candidates
        (:evaluation-order attributes))
     (= :not-asserted-by-sh07-b11
        (:runtime-reachability attributes))
     (= :source-ordered-pattern-candidates
        (:selection-policy attributes))
     (= :pending-sh08
        (:result-type-join attributes)
        (:exhaustiveness attributes))
     (= :scrutinee-then-source-ordered-pattern-candidates
        (get-in match-node [:evaluation :kind]))
     (= [{:index 0 :core-node-id scrutinee-node-id}]
        (get-in match-node [:evaluation :order]))
     (every?
      (fn [record]
        (let [branch-tree
              (sh07-core-descendant-node-ids
               node-by-id (:branch-core-node-id record))
              expected-pattern-binding-ids
              (->> pattern-records
                   (filter
                    #(= (:clause-ordinal record)
                        (:clause-ordinal %)))
                   (keep :pattern-binding-id)
                   vec)
              expected-uses
              (if (:pattern-binding-id record)
                (->> references
                     (filter
                      #(and (= (:pattern-binding-id record)
                               (:binding-id %))
                            (contains? branch-tree
                                       (:core-node-id %))))
                     (mapv :syntax-id))
                [])]
          (and
           (= (:node-id match-node) (:core-node-id record))
           (= expected-pattern-binding-ids
              (:pattern-binding-ids record))
           (= expected-uses
              (:pattern-binding-use-syntax-ids record))
           (= :evaluate-scrutinee-once
              (:scrutinee-evaluation record))
           (= :conditionally-evaluate-selected-branch
              (:branch-evaluation record))
           (= {:kind :match-branch
               :match-syntax-id
               (get-in record
                       [:conditional-region :match-syntax-id])
               :clause-ordinal (:clause-ordinal record)}
              (:conditional-region record))
           (= (:authenticated-sh06-artifact-id attributes)
              (:authenticated-sh06-artifact-id record))
           (= (:sh06-semantic-projection-id attributes)
              (:sh06-semantic-projection-id record)))))
      records)
     (= (:node-id match-node) (:core-node-id skeleton))
     (= scrutinee-node-id (:scrutinee-core-node-id skeleton))
     (= branch-node-ids (:branch-core-node-ids skeleton))
     (= :source-ordered-pattern-candidates
        (:selection-policy skeleton))
     (= :not-asserted-by-sh07-b11
        (:runtime-reachability skeleton))
     (= :pending-sh08
        (:result-type-join skeleton)
        (:exhaustiveness skeleton))
     (= (:authenticated-sh06-artifact-id attributes)
        (:authenticated-sh06-artifact-id skeleton))
     (= (:sh06-semantic-projection-id attributes)
        (:sh06-semantic-projection-id skeleton)))))