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
  (let [adjacency (group-by :from edges)]
    (loop [pending (conj clojure.lang.PersistentQueue/EMPTY [root-id 0])
           discovered #{root-id} observed 0]
      (if (empty? pending) observed
        (let [[node depth] (peek pending) pending (pop pending)
              unseen (remove discovered (map :to (get adjacency node [])))]
          (recur (into pending (map #(vector % (inc depth)) unseen))
                 (into discovered unseen) (max observed depth)))))))

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
         p15-s23-sh02-require-carrier!)

(defn- p15-s23-sh02-reference-edge-state
  [stage root-id edge-seq]
  (reduce (fn [state edge]
            {:edges (conj (:edges state) edge)
             :node-ids (conj (:node-ids state) (:from edge) (:to edge))})
          {:edges [] :node-ids #{root-id}} edge-seq)))
