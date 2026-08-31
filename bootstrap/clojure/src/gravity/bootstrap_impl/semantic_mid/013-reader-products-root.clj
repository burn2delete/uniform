(defn- semantic-mid-reader-products
  [{:keys [tokens token-count nodes form-order] :as context}]
  (loop [idx 0
         form-index 0
         root-ids []
         records []]
    (let [[idx leading-trivia]
          (semantic-mid-reader-skip-trivia context idx)]
      (if (>= idx token-count)
        (let [raw-tree (mapv @nodes @form-order)
              parent-by-child
              (reduce (fn [parents parent]
                        (reduce #(assoc %1 %2 (:form-id parent))
                                parents
                                (:children parent)))
                      {}
                      raw-tree)
              form-tree
              (mapv #(assoc % :parent-form-id
                            (get parent-by-child (:form-id %)))
                    raw-tree)
              node-by-id (into {} (map (juxt :form-id identity)
                                       form-tree))
              records
              (mapv (fn [record]
                      (assoc record
                             :kind (:kind (node-by-id (:form-id record)))
                             :parent-form-id nil))
                    records)]
          {:records records
           :form-tree form-tree
           :root-form-ids root-ids
           :parsed-values (mapv :form records)})
        (let [[form next-idx span form-id]
              (semantic-mid-reader-read-form context idx leading-trivia)]
          (recur next-idx
                 (inc form-index)
                 (conj root-ids form-id)
                 (conj records
                       {:form form
                        :kind (form-kind form)
                        :form-id form-id
                        :span (assoc span :form-index form-index)})))))))
