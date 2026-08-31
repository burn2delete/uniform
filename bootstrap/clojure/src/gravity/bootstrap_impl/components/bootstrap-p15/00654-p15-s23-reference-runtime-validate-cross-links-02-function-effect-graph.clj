(defn- __gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_function_effect_graph [state]
  (let [{:syms
         [source-path
          target
          definitions
          authoritative-module
          derived
          contract
          checked-core-program-policy
          checked-core-verification-replay-policy
          checked-core-verification-replay-audit-policy]} state
        function-graph (get definitions 'p15-s23-reference-runtime-function-graph)
        function-effects (get definitions 'p15-s23-reference-runtime-function-effects)
        effect-graph (get definitions 'p15-s23-reference-runtime-effect-graph)
        allocator (get definitions 'p15-s23-reference-managed-allocator-provider)
        capture-provider (get
                           definitions
                           'p15-s23-reference-transcript-capture-provider)
        capture-handler (get
                          definitions
                          'p15-s23-reference-transcript-capture-handler)]
    (assoc
      state
      'function-graph
      function-graph
      'function-effects
      function-effects
      'effect-graph
      effect-graph
      'allocator
      allocator
      'capture-provider
      capture-provider
      'capture-handler
      capture-handler)))
