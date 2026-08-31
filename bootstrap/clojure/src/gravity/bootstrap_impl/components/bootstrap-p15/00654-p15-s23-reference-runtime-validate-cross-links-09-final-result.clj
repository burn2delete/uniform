(defn- __gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_final_result [state]
  (let [{:syms
         [source-path
          target
          definitions
          authoritative-module
          derived
          contract
          checked-core-program-policy
          checked-core-verification-replay-policy
          checked-core-verification-replay-audit-policy
          function-graph
          function-effects
          effect-graph
          allocator
          capture-provider
          capture-handler
          selections
          proofs
          services
          adapter
          failure-policy
          audit-policy
          capability-manifest
          capability-table
          observability
          grants
          deployment
          expected-links
          source-principal
          handler-principal
          handler-scope
          excluded-functions
          grant-records
          proof-records
          expected-grants
          owner-table]} state]
    (doseq [value [capture-handler audit-policy capability-manifest grants]]
      (p15-s23-reference-runtime-ensure!
        source-path
        target
        :runtime-contract-no-delegation
        {:delegation :none,
         :authority-widening? false,
         :source-declaration-is-grant? false}
        (select-keys
          value
          [:delegation :authority-widening? :source-declaration-is-grant?])))))
