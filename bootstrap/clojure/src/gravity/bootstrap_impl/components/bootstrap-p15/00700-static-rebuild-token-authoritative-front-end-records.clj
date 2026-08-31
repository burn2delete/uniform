(defn- __gravity_bootstrap_checked_core_authoritative_front_end_records [state]
  (let [{:syms
         [source-path
          source-text
          requested-target
          authority-record
          construction-mode
          static-execution-evidence
          static-rebuild-token-candidate
          source-byte-count
          source-content-hash
          early-module-products
          module-attempt
          _
          early-module
          authoritative-front-end]} state
        authoritative-module early-module
        authoritative-records (:records authoritative-front-end)
        namespace-record (first authoritative-records)
        function-record (second authoritative-records)
        namespace-subject (p15-s23-closed-core-source-record-subject
                            namespace-record
                            authoritative-module
                            requested-target
                            :pure-closed-module-admission
                            {})
        function-subject (p15-s23-closed-core-source-record-subject
                           function-record
                           authoritative-module
                           requested-target
                           :pure-closed-source-surface
                           {})
        executable-form-records (p15-s23-closed-core-executable-form-records
                                  (:form-tree authoritative-front-end)
                                  (:top-level-form-ids authoritative-front-end))
        executable-form-by-id (into
                                {}
                                (map (juxt :form-id identity))
                                executable-form-records)
        source-surface-validation (p15-s23-closed-core-source-surface-validation
                                    (:forms authoritative-front-end)
                                    executable-form-records)]
    (assoc
      state
      'authoritative-module
      authoritative-module
      'authoritative-records
      authoritative-records
      'namespace-record
      namespace-record
      'function-record
      function-record
      'namespace-subject
      namespace-subject
      'function-subject
      function-subject
      'executable-form-records
      executable-form-records
      'executable-form-by-id
      executable-form-by-id
      'source-surface-validation
      source-surface-validation)))
