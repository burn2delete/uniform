(ns gravity.c2-lexical-validation.primitives)

(defn utf8-slice [source-bytes byte-start byte-end]
  (String. (java.util.Arrays/copyOfRange source-bytes byte-start byte-end)
           java.nio.charset.StandardCharsets/UTF_8))

(defn span-encloses? [parent child]
  (and (map? parent) (map? child)
       (integer? (:byte-start parent)) (integer? (:byte-end parent))
       (integer? (:byte-start child)) (integer? (:byte-end child))
       (<= (:byte-start parent) (:byte-start child))
       (>= (:byte-end parent) (:byte-end child))))

(defn spans-source-ordered? [spans]
  (every? (fn [[left right]]
            (and (map? left) (map? right)
                 (integer? (:byte-end left)) (integer? (:byte-start right))
                 (<= (:byte-end left) (:byte-start right))))
          (partition 2 1 spans)))

(defn form-graph-metrics [form-tree]
  (let [forms-by-id (into {} (map (juxt :form-id identity) form-tree))
        form-ids (mapv :form-id form-tree)
        indegrees (reduce (fn [counts form]
                             (reduce (fn [result child-id]
                                       (if (contains? forms-by-id child-id)
                                         (update result child-id (fnil inc 0)) result))
                                     counts (:children form)))
                           (zipmap form-ids (repeat 0)) form-tree)
        initial-ids (filterv #(zero? (get indegrees % 0)) form-ids)
        initial-queue (reduce conj clojure.lang.PersistentQueue/EMPTY initial-ids)]
    (loop [pending initial-queue remaining indegrees
           depths (zipmap initial-ids (repeat 1)) processed 0 max-depth 0]
      (if (empty? pending)
        {:acyclic? (= processed (count form-ids))
         :processed-form-count processed :max-form-depth max-depth}
        (let [form-id (peek pending)
              parent-depth (get depths form-id 1)
              children (filterv #(contains? forms-by-id %)
                                (:children (forms-by-id form-id)))
              [next-queue next-remaining next-depths]
              (reduce (fn [[queue counts known-depths] child-id]
                        (let [next-count (dec (get counts child-id 0))
                              child-depth (max (get known-depths child-id 1)
                                               (inc parent-depth))]
                          [(cond-> queue (zero? next-count) (conj child-id))
                           (assoc counts child-id next-count)
                           (assoc known-depths child-id child-depth)]))
                      [(pop pending) remaining depths] children)]
          (recur next-queue next-remaining next-depths (inc processed)
                 (max max-depth parent-depth)))))))
