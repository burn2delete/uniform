

(defn p15-s23-c6c10-function-merkle-manifest
  [source-path source-content-hash functions]
  (when-not (and (map? functions)
                 (<= (count functions) 256)
                 (every? symbol? (keys functions))
                 (every? map? (vals functions)))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :bounded-compiled-function-table
     {:observed-function-count
      (when (map? functions) (count functions))}))
  (let [ordered-names
        (vec (sort-by #(p15-s23-c6c10-function-order-key
                        source-path %)
                      (keys functions)))
        records
        (mapv
         (fn [function-name]
           (let [definition
                 (p15-s23-c6c10-path-neutral-value
                  source-content-hash (get functions function-name))
                 shape (select-keys definition [:arity :params])]
             {:name function-name
              :shape shape
              :definition-digest
              (p15-s23-c6c10-canonical-digest
               source-path
               {:domain :gravity/c6-c10-compiled-function-v1
                :name function-name
                :definition definition})}))
         ordered-names)
        chunks
        (mapv
         (fn [index chunk-records]
           (let [records (vec chunk-records)
                 chunk-base
                 {:domain :gravity/c6-c10-function-chunk-v1
                  :index index
                  :records records}]
             {:index index
              :count (count records)
              :records records
              :chunk-digest
              (p15-s23-c6c10-canonical-digest
               source-path chunk-base)}))
         (range)
         (partition-all 64 records))
        root-input
        {:domain :gravity/c6-c10-function-manifest-v1
         :function-count (count records)
         :chunk-size 64
         :chunks (mapv #(select-keys %
                                    [:index :count :chunk-digest])
                       chunks)}]
    {:function-count (count records)
     :chunk-size 64
     :chunk-count (count chunks)
     :chunks chunks
     :root-input root-input
     :root-digest
     (p15-s23-c6c10-canonical-digest source-path root-input)}))

(defn p15-s23-c6c10-plan-pin-input
  [source-content-hash plan function-manifest]
  {:domain :gravity/c6-c10-compiled-plan-binding-v1
   :source-content-hash source-content-hash
   :function-count (:function-count function-manifest)
   :function-manifest-root (:root-digest function-manifest)
   :plan
   (p15-s23-c6c10-path-neutral-value
    source-content-hash
    (-> plan
        (dissoc :functions :plan-id)
        (update :source dissoc :path)
        (update :module dissoc :source-path)))})