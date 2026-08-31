(defn- __gravity_bootstrap_closed_core_fact_context_phase_01 [state]
  (let [{:syms [nodes module plan-id mode authority-evidence]} state
        effectful? (= :effectful-reference mode)
        node-by-id (into {} (map (juxt :node-id identity)) nodes)
        function-node (first (filter #(= :function (:kind %)) nodes))
        canonical (p15-s23-closed-core-canonical-pass-envelopes
                    nodes
                    module
                    plan-id
                    mode
                    authority-evidence)
        capability-proof-by-node-capability (into
                                              {}
                                              (map
                                                (fn
                                                  [record]
                                                  [[(:core-node-id record)
                                                    (:capability record)]
                                                   record]))
                                              (:capability-proof-records canonical))
        expected-capability-proof-pairs (set
                                          (for
                                            [node
                                             nodes
                                             capability
                                             (:capabilities node)]
                                            [(:node-id node) capability]))
        observed-capability-proof-pairs (set
                                          (map
                                            (juxt :core-node-id :capability)
                                            (:capability-proof-records canonical)))
        _ (when (and
                  effectful?
                  (not
                    (and
                      (=
                        expected-capability-proof-pairs
                        observed-capability-proof-pairs)
                      (=
                        (count expected-capability-proof-pairs)
                        (count (:capability-proof-records canonical))
                        (count
                          (set (map :proof-id (:capability-proof-records canonical)))))
                      (every?
                        #(p15-s23-checked-core-capability-proof-record-valid?
                          %
                          authority-evidence)
                        (:capability-proof-records canonical)))))
            (p15-s23-closed-core-fail!
              "C8-CAPABILITY"
              "<checked-core-facts>"
              authority-evidence
              {:missing-fact :exact-unique-node-capability-proof-coverage}))
        shared-owner-id (p15-s23-closed-core-shared-owner-domain-id nodes)
        fact-link-id (fn [family artifact-key node-id]
                       (p15-s23-closed-core-digest
                         {:family family,
                          :artifact-key artifact-key,
                          :core-node-key node-id}))
        source-value-ids (fn [node]
                           (case
                             (:source-operation node)
                             :local
                             [(first (:operands node))]
                             :let-binding
                             [(first (:operands node))]
                             :truthy
                             [(first (:operands node))]
                             :if
                             (vec (rest (:operands node)))
                             :do
                             [(last (:operands node))]
                             :let
                             [(last (:operands node))]
                             :function
                             [(last (:operands node))]
                             []))
        forwarding-kind (fn [node]
                          (case
                            (:source-operation node)
                            :local
                            :from-lexical-binding
                            :let-binding
                            :from-initializer
                            :truthy
                            :derived-from-test
                            :if
                            :from-conditional-incoming-values
                            :do
                            :from-sequence-result
                            :let
                            :from-lexical-scope-result
                            :function
                            :from-entrypoint-result
                            :not-applicable))]
    (assoc
      state
      'effectful?
      effectful?
      'node-by-id
      node-by-id
      'function-node
      function-node
      'canonical
      canonical
      'capability-proof-by-node-capability
      capability-proof-by-node-capability
      'expected-capability-proof-pairs
      expected-capability-proof-pairs
      'observed-capability-proof-pairs
      observed-capability-proof-pairs
      '_
      _
      'shared-owner-id
      shared-owner-id
      'fact-link-id
      fact-link-id
      'source-value-ids
      source-value-ids
      'forwarding-kind
      forwarding-kind)))
