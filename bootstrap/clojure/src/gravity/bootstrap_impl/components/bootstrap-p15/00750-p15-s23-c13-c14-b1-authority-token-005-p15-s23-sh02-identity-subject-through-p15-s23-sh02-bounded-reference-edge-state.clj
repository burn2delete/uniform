(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-sh02-identity-subject
  [name domain preimage]
  (let [preimage (p15-s23-c13-c14-b1-path-neutral-value preimage)]
    {:name name :domain domain :preimage preimage
     :observed-id
     (p15-s23-c6c10-canonical-digest
      "<sh02-identity-subject>"
      {:domain domain :semantic-input preimage})}))

(defn p15-s23-sh02-stage-identity-subjects
  [stage packet]
  (let [record (get packet stage)
        semantic-preimage
        {:kind (:artifact record)
         :record (p15-s23-c13-c14-b1-stage-semantic-input record)}
        artifact-preimage
        {:kind (:artifact record) :schema-version (:schema-version record)
         :semantic-id (:semantic-id record)}]
    (case stage
      :c13
      [(p15-s23-sh02-identity-subject
        :stage-semantic-seal :gravity/sh02-c13-semantic-seal-v1
        {:preimage semantic-preimage :observed-id (:semantic-id record)})
       (p15-s23-sh02-identity-subject
        :stage-artifact-seal :gravity/sh02-c13-artifact-seal-v1
        {:preimage artifact-preimage :observed-id (:artifact-id record)})
       (p15-s23-sh02-identity-subject
        :optimization-decision :gravity/sh02-c13-decision-v1
        {:decision-record (:decision-record record)
         :decision-id (get-in record [:decision-record :decision-id])})]

      :b1
      [(p15-s23-sh02-identity-subject
        :stage-semantic-seal :gravity/sh02-b1-semantic-seal-v1
        {:preimage semantic-preimage :observed-id (:semantic-id record)})
       (p15-s23-sh02-identity-subject
        :stage-artifact-seal :gravity/sh02-b1-artifact-seal-v1
        {:preimage artifact-preimage :observed-id (:artifact-id record)})
       (p15-s23-sh02-identity-subject
        :backend-manifest :gravity/sh02-b1-backend-manifest-v1
        (:backend-manifest record))
       (p15-s23-sh02-identity-subject
        :target-lowering-request :gravity/sh02-c14-request-v1
        {:request
         (dissoc (get-in packet [:c14 :request]) :request-id)
         :observed-id (get-in packet [:c14 :request :request-id])})])))

(defn p15-s23-sh02-stage-lineage
  [stage packet]
  (case stage
    :c13
    [{:stage :c11-mir
      :artifact-kind :gravity/mir
      :semantic-id (get-in packet [:c11 :mir-id])
      :artifact-id (get-in packet [:c11 :artifact-id])
      :verification-id (get-in packet [:c13 :input :verifier-report-id])
      :relation :optimized-from}]

    :b1
    [{:stage :c14-target-lowering
      :artifact-kind (get-in packet [:c14 :artifact])
      :semantic-id (get-in packet [:c14 :semantic-id])
      :artifact-id (get-in packet [:c14 :artifact-id])
      :verification-id (get-in packet [:c14 :request :request-id])
      :relation :backend-admitted-from}]))

(defn p15-s23-sh02-reference-depth
  [root-id edges]
  (let [adjacency (group-by :from edges)
        maximum (:maximum-reference-depth
                 p15-s23-sh02-authenticated-envelope-bounds)]
    (loop [pending (conj clojure.lang.PersistentQueue/EMPTY
                         [root-id 0])
           discovered #{root-id}
           observed 0]
      (if (empty? pending)
        observed
        (let [[node depth] (peek pending)
              pending (pop pending)
              next-depth (inc depth)
              targets (map :to (get adjacency node []))
              unseen (remove discovered targets)]
          (if (and (seq unseen) (> next-depth maximum))
            next-depth
            (recur
             (into pending (map #(vector % next-depth) unseen))
             (into discovered unseen)
             (max observed depth))))))))

(defn p15-s23-sh02-reference-reachable-ids
  [root-id edges]
  (let [adjacency (group-by :from edges)]
    (loop [pending (conj clojure.lang.PersistentQueue/EMPTY root-id)
           discovered #{root-id}]
      (if (empty? pending)
        discovered
        (let [node (peek pending)
              pending (pop pending)
              unseen (remove discovered
                             (map :to (get adjacency node [])))]
          (recur (into pending unseen) (into discovered unseen)))))))

(declare p15-s23-sh02-fail!
         p15-s23-sh02-require-bounded-carrier!)

(defn- p15-s23-sh02-bounded-reference-edge-state
  [stage root-id edge-seq]
  (loop [remaining (seq edge-seq)
         edges []
         node-ids #{root-id}]
    (if (nil? remaining)
      {:edges edges :node-ids node-ids}
      (let [edge (first remaining)
            next-edges (conj edges edge)
            next-node-ids (conj node-ids (:from edge) (:to edge))]
        (when (or (> (count next-edges)
                     (:maximum-reference-edges
                      p15-s23-sh02-authenticated-envelope-bounds))
                    (> (count next-node-ids)
                     (:maximum-reference-nodes
                      p15-s23-sh02-authenticated-envelope-bounds)))
          (p15-s23-sh02-fail!
           "<sh02-reference-closure>" {}
           :bounded-sh02-reference-closure
           {:stage stage
            :observed-reference-nodes (count next-node-ids)
            :observed-reference-edges (count next-edges)
            :maximum-reference-nodes
            (:maximum-reference-nodes
             p15-s23-sh02-authenticated-envelope-bounds)
            :maximum-reference-edges
            (:maximum-reference-edges
             p15-s23-sh02-authenticated-envelope-bounds)}))
        (let [observed-depth
              (p15-s23-sh02-reference-depth root-id next-edges)]
          (when (> observed-depth
                   (:maximum-reference-depth
                    p15-s23-sh02-authenticated-envelope-bounds))
            (p15-s23-sh02-fail!
             "<sh02-reference-closure>" {}
             :bounded-sh02-reference-closure
             {:stage stage
              :observed-reference-nodes (count next-node-ids)
              :observed-reference-edges (count next-edges)
              :observed-reference-depth observed-depth
              :maximum-reference-depth
              (:maximum-reference-depth
               p15-s23-sh02-authenticated-envelope-bounds)})))
        (recur (next remaining) next-edges next-node-ids))))))
