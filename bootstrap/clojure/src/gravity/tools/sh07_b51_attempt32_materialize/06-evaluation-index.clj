
(defn- evaluation-index [rc node]
  (let [node-ids (mapv :node-id (:nodes rc))
        evaluation-ids (mapv :core-node-id (:evaluation-order rc))
        matches (keep-indexed #(when (= (:node-id node) (:core-node-id %2)) %1)
                              (:evaluation-order rc))]
    (when-not (and (= (count node-ids) (count (set node-ids)))
                   (= (set node-ids) (set evaluation-ids))
                   (= 1 (count matches)))
      (fail! :core-evaluation-order))
    (first matches)))

(defn- artifact-descriptor [module-ordinal kind source-id value]
  (let [base {:module-ordinal module-ordinal :kind kind
              :source-id source-id :value value}]
    {:artifact-id (semantic-witness-member-id :boot3-artifact base)
     :kind kind :source-id source-id :raw-sha256 (codec/R (codec/W value))}))
