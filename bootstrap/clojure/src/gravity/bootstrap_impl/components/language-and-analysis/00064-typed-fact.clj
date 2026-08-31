

(defn typed-fact
  [checker node type-name effects capabilities extra]
  (let [fact (merge {:node-id (:node-id node)
                     :source-kind (:kind node)
                     :source-span (:source-span node)
                     :type type-name
                     :effects (stable-set effects)
                     :capabilities (stable-set capabilities)
                     :memory []
                     :concurrency []
                     :profile-context (:profile node)
                     :namespace-context (:namespace node)}
                    extra)]
    (record-checker! checker :type-facts
                     {:node-id (:node-id fact)
                      :type (:type fact)
                      :source-kind (:source-kind fact)
                      :source-span (:source-span fact)})
    (when-let [category (type-category (:type fact))]
      (when-not (= :unknown category)
        (record-checker! checker :type-category-coverage
                         {:node-id (:node-id fact)
                          :type (:type fact)
                          :category category})))
    (when (or (seq (:effects fact)) (seq (:capabilities fact)))
      (record-checker! checker :effect-facts
                       {:node-id (:node-id fact)
                        :effects (:effects fact)
                        :capabilities (:capabilities fact)
                        :source-kind (:source-kind fact)}))
    fact))

(declare check-typed-node)