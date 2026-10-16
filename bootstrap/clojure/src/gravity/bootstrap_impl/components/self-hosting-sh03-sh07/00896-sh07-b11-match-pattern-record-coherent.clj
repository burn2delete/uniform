

(defn sh07-b11-match-pattern-record-coherent?
  [node-by-id references branch-by-clause record record-by-ordinal]
  (let [parent (when-some [ordinal (:parent-ordinal record)]
                 (get record-by-ordinal ordinal))
        root (get record-by-ordinal (:root-ordinal record))
        branch-record (get branch-by-clause (:clause-ordinal record))
        branch-tree
        (when branch-record
          (sh07-b11-branch-tree node-by-id branch-record))
        expected-uses
        (if (:pattern-binding-id record)
          (->> references
               (filter
                #(and (= (:pattern-binding-id record) (:binding-id %))
                      (contains? branch-tree (:core-node-id %))))
               (mapv :syntax-id))
          [])]
    (and
     (= (cond-> sh07-b11-match-pattern-record-keys (contains? record :guard) (conj :guard))
        (set (keys record)))
     (if (contains? record :guard)
       (and (= :map (:pattern-kind record)) (= 0 (:depth record)) (nil? (:parent-ordinal record))
            (sh07-b11-guard-source-valid? (:guard record))) true)
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
     (contains? #{:literal :wildcard :binding :vector :map}
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

       :map
       (and (map? (:pattern-value record)) (<= (count (:pattern-value record)) 256)
            (every? keyword? (keys (:pattern-value record)))
            (= (contains? record :guard) (contains? (:pattern-value record) :when))
            (if (contains? record :guard)
              (< (get-in record [:guard :source-child-index]) (* 2 (count (:pattern-value record)))) true)
            (nil? (:vector-width record))
            (nil? (:pattern-binding-id record)) (nil? (:pattern-binding-scope-id record)))

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
       (and (contains? #{:vector :map} (:pattern-kind parent))
            (= (:core-node-id record) (:core-node-id parent))
            (= (:clause-ordinal record) (:clause-ordinal parent))
            (= (:root-ordinal record) (:root-ordinal parent))
            (= (:parent-local-ordinal record)
               (:local-ordinal parent))
            (= (:depth record) (inc (:depth parent)))
            (= (:path parent) (pop (:path record)))
            (if (= :map (:pattern-kind parent))
              (and (map? (:pattern-value parent)) (odd? (peek (:path record)))
                   (not= (peek (:path record)) (get-in parent [:guard :source-child-index]))
                   (< (peek (:path record)) (* 2 (count (:pattern-value parent)))))
              (< (peek (:path record)) (:vector-width parent)))))
     (= (:core-node-id record) (:core-node-id root))
     (= (:clause-ordinal record) (:clause-ordinal root)))))

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
        branch-node-ids (mapv :branch-core-node-id records)
        guarded (filterv #(contains? % :guard) records)
        guard-indexes (mapv #(get-in % [:guard :child-index]) guarded)
        expected-indexes (mapv inc (range (count records)))
        expected-bindings
        (vec (keep :pattern-binding-id pattern-records))
        expected-clauses
        (mapv sh07-b11-clause records)]
    (and
     (seq records)
     (not-any? #(contains? #{:wildcard :binding} (:pattern-kind %))
               (butlast records))
     (= (range (count records)) (map :clause-ordinal records))
     (every? #(= (count records) (:clause-count %)) records)
     (= expected-indexes (mapv :branch-child-index records))
     (= branch-node-ids (mapv :branch-core-node-id records))
     (= (:children match-node)
        (into (into [scrutinee-node-id] branch-node-ids) (map #(get-in % [:guard :node-id]) guarded)))
     (= guard-indexes (vec (range (inc (count records)) (+ 1 (count records) (count guarded)))))
     (if (seq guarded)
       (= [guard-indexes :sh07-root-map-guard-v1 :sh07-root-map-guard-v1]
          [(:guard-child-indexes attributes) (:guard-rule-version attributes) (:lowering-rule-version match-node)])
       (and (not (contains? attributes :guard-rule-version)) (not (contains? attributes :guard-child-indexes))))
     (every? #(and (= :map (:pattern-kind %))
                   (sh07-b11-guard-valid? (when (map? (:guard %)) (dissoc (:guard %) :node-id)) (:clause-ordinal %))
                   (p15-s23-sh02-sha256-id? (get-in % [:guard :node-id]))
                   (= (get-in % [:guard :conditional-region])
                      (get-in node-by-id [(get-in % [:guard :node-id]) :evaluation :region]))
                   (= ((juxt :form-id :syntax-id) (:guard %))
                      ((juxt :form-id :syntax-id)
                       (get-in node-by-id [(get-in % [:guard :node-id]) :source])))
                   (= (get-in % [:guard :conditional-region :match-syntax-id])
                      (get-in % [:conditional-region :match-syntax-id]))) guarded)
     (every? #(= (:guard %) (when-let [guard (:guard (get branch-by-clause (:clause-ordinal %)))]
                                    (sh07-b11-guard-source guard)))
             (filter (comp nil? :parent-ordinal) pattern-records))
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
