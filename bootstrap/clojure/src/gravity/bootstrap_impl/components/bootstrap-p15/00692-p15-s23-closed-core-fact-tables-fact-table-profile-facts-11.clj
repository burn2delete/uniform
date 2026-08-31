(defn- __gravity_bootstrap_closed_core_fact_table_profile_facts_11 [state]
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
    [[:profile-facts
      (into
        (sorted-map)
        (map
          (fn [node] [(:node-id node)
                      {:profile (:profile node),
                       :operation (:source-operation node),
                       :legal? true,
                       :scope
                       (if effectful?
                         :authenticated-hosted-jvm-reference-interpreter-slice
                         :pure-closed-hosted-jvm-slice)}]))
        nodes)]]))
