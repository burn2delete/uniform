(defn- __gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_runtime_contract_policy [state]
  (let [{:syms [source-path target definitions authoritative-module derived]} state
        contract (get definitions 'p15-s23-reference-runtime-contract)
        checked-core-program-policy (get
                                      definitions
                                      'p15-s23-checked-core-program-authority-policy)
        checked-core-verification-replay-policy (get
                                                  definitions
                                                  'p15-s23-checked-core-verification-replay-policy)
        checked-core-verification-replay-audit-policy (get
                                                        definitions
                                                        'p15-s23-checked-core-verification-replay-audit-policy)]
    (assoc
      state
      'contract
      contract
      'checked-core-program-policy
      checked-core-program-policy
      'checked-core-verification-replay-policy
      checked-core-verification-replay-policy
      'checked-core-verification-replay-audit-policy
      checked-core-verification-replay-audit-policy)))
