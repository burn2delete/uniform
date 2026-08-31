(defn- __gravity_bootstrap_p15-s23-reference-runtime-adapter-validated-invoke_authority_and_provider_setup [state]
  (let [{:syms
         [runtime-rule
          function
          args
          authority
          target-plan
          plan-id
          source-id
          closed-plan-validation]} state
        source-principal 'gravity.bootstrap.p15-s23.runtime
        handler-principal :gravity.bootstrap/reference-harness
        allocation-provider :gravity.reference/jvm-managed-allocator
        capture-provider :gravity.reference/transcript-capture
        allocation-grant :gravity.reference/grant-managed-allocation
        stdout-grant :gravity.reference/grant-reference-stdout
        fixture-grant :gravity.reference/grant-test-fixture
        writes-stdout? (contains?
                         (:observed-operation-set closed-plan-validation)
                         :println)
        required-provider-ids (cond->
                                #{allocation-provider}
                                writes-stdout?
                                (conj capture-provider))
        required-grant-ids (cond->
                             #{allocation-grant}
                             writes-stdout?
                             (conj stdout-grant fixture-grant))]
    (assoc
      state
      'source-principal
      source-principal
      'handler-principal
      handler-principal
      'allocation-provider
      allocation-provider
      'capture-provider
      capture-provider
      'allocation-grant
      allocation-grant
      'stdout-grant
      stdout-grant
      'fixture-grant
      fixture-grant
      'writes-stdout?
      writes-stdout?
      'required-provider-ids
      required-provider-ids
      'required-grant-ids
      required-grant-ids)))
