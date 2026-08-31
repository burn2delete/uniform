(ns gravity.core-ast-lowering.traversal
  "Recursive sequence, match, id, and flattening helpers for L2.")

(defn lower-sequential-body
  [lower-core-expr counter module syntax forms context]
  (mapv #(lower-core-expr counter module syntax % context) forms))

(defn extract-pattern-guard
  [pattern]
  (if (and (map? pattern) (contains? pattern :when))
    {:pattern (dissoc pattern :when) :guard (get pattern :when)}
    {:pattern pattern :guard nil}))

(defn lower-match-clauses
  [fail! extract-pattern-guard lower-core-expr
   counter module syntax clauses context]
  (when (odd? (count clauses))
    (fail! "L7-PATTERN-TYPE"
           "match requires pattern/expression clause pairs"
           {:source-span (:span syntax)
            :remediation "Use (match value pattern expr ...)."}))
  (mapv (fn [branch-index [raw-pattern raw-expr]]
          (let [{:keys [pattern guard]}
                (extract-pattern-guard raw-pattern)]
            {:branch-index branch-index
             :raw-pattern raw-pattern
             :pattern pattern
             :guard (when guard
                      (lower-core-expr counter module syntax guard context))
             :body (lower-core-expr
                    counter module syntax raw-expr context)}))
        (range)
        (partition 2 clauses)))

(defn next-node-id
  [counter]
  (let [id @counter]
    (swap! counter inc)
    id))

(defn flatten-core
  [recur-flatten node]
  (let [core-child? #(and (map? %) (:node-id %))
        children (filter core-child?
                         (concat (:children node)
                                 (keep :initializer (:bindings node))
                                 (when-let [v (:value node)] [v])
                                 (when-let [b (:body node)] [b])
                                 (:arguments node)
                                 (mapcat (fn [clause]
                                           (cond-> [(:body clause)]
                                             (:guard clause)
                                             (conj (:guard clause))))
                                         (:clauses node))))]
    (vec (cons node (mapcat recur-flatten children)))))
