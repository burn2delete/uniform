(defn- __gravity_bootstrap_closed_core_fact_table_type_facts_06 [state]
  (let [{:syms
         [nodes
          module
          plan-id
          mode
          authority-evidence
          effectful?
          node-by-id
          function-node
          canonical
          capability-proof-by-node-capability
          expected-capability-proof-pairs
          observed-capability-proof-pairs
          _
          shared-owner-id
          fact-link-id
          source-value-ids
          forwarding-kind]} state]
    [[:type-facts
      (into
        (sorted-map)
        (map
          (fn [node]
            (let [node-id (:node-id node)
                  reconstructed-type (p15-s23-closed-core-recomputed-node-type
                                       node
                                       node-by-id)
                  base {:constraints [],
                        :source-target :jvm,
                        :producer-rule
                        (p15-s23-closed-core-type-producer-rule
                          (:source-operation node)),
                        :derived? true,
                        :type-id (get-in canonical [:typed-core :types node-id]),
                        :type reconstructed-type,
                        :source-origin-id (get-in node [:source :origin-id]),
                        :core-node-id node-id,
                        :dependencies (:operands node),
                        :profile (:profile node),
                        :fact-id
                        (fact-link-id :type (:typed-core-key canonical) node-id)}]
              [node-id
               (cond->
                 base
                 (= :function (:kind node))
                 (assoc
                   :params
                   []
                   :return
                   (:return reconstructed-type)
                   :latent-effects
                   (:latent-effects reconstructed-type)
                   :capabilities
                   (:capabilities reconstructed-type)
                   :throws
                   #{}
                   :ownership-constraints
                   #{:persistent-immutable-shareable}
                   :profile-constraints
                   #{:hosted}))]))
          nodes))]]))
