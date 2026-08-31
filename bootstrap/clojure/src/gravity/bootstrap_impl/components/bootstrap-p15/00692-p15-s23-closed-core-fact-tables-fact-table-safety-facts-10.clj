(defn- __gravity_bootstrap_closed_core_fact_table_safety_facts_10 [state]
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
    [[:safety-facts
      (into
        (sorted-map)
        (map
          (fn [node]
            (let [node-id (:node-id node)
                  proof (get-in node [:safety :proof])
                  runtime-check (get-in node [:safety :check])
                  runtime-checked? (=
                                     :runtime-checked
                                     (get-in node [:safety :outcome]))
                  structural-proof? (=
                                      :gravity/p15-s23-structural-safety-proof
                                      (:artifact proof))]
              [node-id
               {:operation node-id,
                :outcome (get-in node [:safety :outcome]),
                :proof (if runtime-checked? :not-applicable (:proof-id proof)),
                :source
                {:core-node node-id,
                 :span (get-in node [:source :span]),
                 :origin-chain [(get-in node [:source :origin-id])]},
                :condition
                (cond
                  runtime-checked? (get runtime-check :kind)
                  structural-proof? (:proof-condition proof)
                  :else :always),
                :kind (:source-operation node),
                :artifact :gravity/safety-outcome,
                :target :jvm,
                :facts
                {:type (fact-link-id :type (:typed-core-key canonical) node-id),
                 :effects
                 (fact-link-id :effects (:effect-graph-key canonical) node-id),
                 :ownership
                 (fact-link-id
                   :ownership
                   (:ownership-analysis-key canonical)
                   node-id)},
                :failure-behavior
                (if runtime-checked? (:failure runtime-check) :not-applicable),
                :unsafe-audit :not-applicable,
                :runtime-check
                (if runtime-checked? (:check-id runtime-check) :not-applicable),
                :profile (:profile node)}]))
          nodes))]]))
