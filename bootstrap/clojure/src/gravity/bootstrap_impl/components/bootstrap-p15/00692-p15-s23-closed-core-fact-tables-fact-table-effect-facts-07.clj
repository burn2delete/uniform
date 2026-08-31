(defn- __gravity_bootstrap_closed_core_fact_table_effect_facts_07 [state]
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
    [[:effect-facts
      (into
        (sorted-map)
        (map
          (fn [node]
            (let [node-id (:node-id node)
                  base {:direct
                        (if effectful?
                          (p15-s23-closed-core-intrinsic-effects
                            (:source-operation node))
                          #{}),
                        :transitive (if effectful? (:effects node) #{}),
                        :source-target :jvm,
                        :residual #{},
                        :derived? true,
                        :latent
                        (if (and effectful? (= :function (:kind node)))
                          (:effects node)
                          #{}),
                        :ordering
                        (if (and effectful? (seq (:effects node)))
                          :source-order
                          :none),
                        :source-origin-id (get-in node [:source :origin-id]),
                        :core-node-id node-id,
                        :profile (:profile node),
                        :fact-id
                        (fact-link-id :effects (:effect-graph-key canonical) node-id)}
                  base (cond->
                         base
                         effectful?
                         (assoc
                           :event-index
                           (get-in
                             canonical
                             [:effect-graph :event-index node-id]
                             :not-an-effect-event)))]
              [node-id
               (cond->
                 base
                 (= :function (:kind node))
                 (assoc
                   :function-summary
                   {:declared (if effectful? (:effects node) #{}),
                    :inferred (if effectful? (:effects node) #{}),
                    :latent (if effectful? (:effects node) #{}),
                    :throws #{}}))]))
          nodes))]]))
