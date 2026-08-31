(defn- __gravity_bootstrap_closed_core_fact_table_ownership_facts_09 [state]
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
    [[:ownership-facts
      (into
        (sorted-map)
        (map
          (fn [node]
            (let [node-id (:node-id node) ownership (:ownership node)]
              [node-id
               (cond->
                 {:role (:role ownership),
                  :result-disposition
                  (or (:result-disposition ownership) :not-applicable),
                  :shareability (:shareability ownership),
                  :source-target :jvm,
                  :transfer :not-applicable,
                  :forwarding (forwarding-kind node),
                  :provider-requirement (:provider-requirement ownership),
                  :consume :not-applicable,
                  :derived? true,
                  :move :not-applicable,
                  :managed-reachability (:managed-reachability ownership),
                  :linear-resource :not-applicable,
                  :mutation (:mutation ownership),
                  :allocator-requirement (:allocator-requirement ownership),
                  :region :not-applicable,
                  :borrow :not-applicable,
                  :alias-policy (:alias-policy ownership),
                  :value-id node-id,
                  :escape-policy (:escape-policy ownership),
                  :source-origin-id (get-in node [:source :origin-id]),
                  :core-node-id node-id,
                  :storage (:storage ownership),
                  :owner-id shared-owner-id,
                  :arena :not-applicable,
                  :unsafe-audit :not-applicable,
                  :runtime-check :not-applicable,
                  :cleanup-policy (:cleanup-policy ownership),
                  :profile (:profile node),
                  :fact-id
                  (fact-link-id
                    :ownership
                    (:ownership-analysis-key canonical)
                    node-id),
                  :source-value-ids (source-value-ids node),
                  :model (:model ownership)}
                 (contains? ownership :provider-id)
                 (assoc
                   :provider-id
                   (:provider-id ownership)
                   :lifetime
                   (:lifetime ownership)))]))
          nodes))]]))
