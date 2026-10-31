

(defn
 sh07-core-descendant-node-ids
 [node-by-id root-node-id]
 (gravity.checked-core/descendant-node-ids
  node-by-id
  root-node-id
  (checked-core-stage-operations)))

(def ^:private sh07-b9-handler-descriptor-keys
  [:clause-ordinal :handler-child-index
   :catch-clause-form-id :catch-clause-syntax-id
   :error-type-form-id :error-type-syntax-id
   :error-type-binding-id
   :catch-binding-form-id :catch-binding-syntax-id
   :catch-binding-id :catch-binding-scope-id])

(defn
 sh07-b9-handler-group-coherent?
 [node-by-id transfers references try-node handlers]
 (gravity.checked-core/handler-group-coherent?
  node-by-id
  transfers
  references
  try-node
  handlers
  (checked-core-stage-operations)))

(defn
 sh07-core-error-handlers-coherent?
 [core]
 (gravity.checked-core/error-handlers-coherent?
  core
  (checked-core-stage-operations)))

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
