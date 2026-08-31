(defn- __gravity_bootstrap_closed_core_fact_table_capability_facts_08 [state]
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
    [[:capability-facts
      (into
        (sorted-map)
        (map
          (fn [node]
            (let [node-id (:node-id node)
                  required (if effectful? (:capabilities node) #{})
                  proofs (mapv
                           #(get capability-proof-by-node-capability [node-id %])
                           (sort-by pr-str required))]
              [node-id
               (if effectful?
                 {:authority-source :pinned-runtime-contract-policy,
                  :source-target :jvm,
                  :derived? true,
                  :authority-id (:authority-record-id authority-evidence),
                  :grant-ids (mapv :grant-id proofs),
                  :provider-bindings
                  (into
                    (sorted-map)
                    (map
                      (fn [capability] [capability
                                        (get-in
                                          authority-evidence
                                          [:provider-bindings
                                           capability
                                           :provider-selection-id])]))
                    required),
                  :source-origin-id (get-in node [:source :origin-id]),
                  :core-node-id node-id,
                  :granted required,
                  :capability-proof
                  {:status (if (seq required) :proved :not-required),
                   :basis
                   (if (seq required)
                     :authenticated-program-provider-and-grant-records
                     :empty-capability-requirements),
                   :proof-ids (mapv :proof-id proofs)},
                  :profile (:profile node),
                  :required required}
                 {:authority-source :none-required,
                  :source-target :jvm,
                  :derived? true,
                  :authority-id nil,
                  :grant-ids [],
                  :provider-bindings {},
                  :source-origin-id (get-in node [:source :origin-id]),
                  :core-node-id node-id,
                  :granted #{},
                  :capability-proof
                  {:status :not-required, :basis :empty-capability-requirements},
                  :profile (:profile node),
                  :required #{}})]))
          nodes))]]))
