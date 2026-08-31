(defn- __gravity_bootstrap_checked_core_core_node_mapping [state]
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
          authoritative-front-end
          authoritative-module
          authoritative-records
          namespace-record
          function-record
          namespace-subject
          function-subject
          executable-form-records
          executable-form-by-id
          source-surface-validation
          malformed-quote-record
          source-surface-subject
          early-metadata-bearing-form
          c7-source-violation
          unsupported-quote-literal-form
          unsupported-numeric-form
          invalid-str-arity-form
          preflight-stage2-rule
          preflight-driver-rule
          preflight-plan
          preflight-plan-validation
          packet-delay
          front-end
          fresh-front-end
          top-level-forms
          reader-products
          c2-artifact
          form-tree
          root-form-ids
          indexes
          token-stream
          token-ordinal-by-id
          plan
          preflight-effect-requirements
          entrypoint
          functions
          definition
          declared-exports
          entrypoint-binding
          entrypoint-visibility
          source-function-records
          all-source-functions
          root-record-wrapper
          root-form-id
          root-record
          metadata-bearing-form
          root-syntax
          expanded-root-syntax
          function-shape
          instructions
          body-form-ids
          validation
          observed-operation-set]} state
        ctx {:source-content-hash source-content-hash,
             :expanded-root-syntax expanded-root-syntax,
             :source-target (get-in plan [:module :target]),
             :requested-target requested-target,
             :module (get-in plan [:module :module]),
             :source-path source-path,
             :root-syntax root-syntax,
             :token-ordinal-by-id token-ordinal-by-id,
             :safety (get-in plan [:module :safety]),
             :indexes indexes,
             :profile (get-in plan [:module :profile])}
        function-path [:functions entrypoint]
        function-origin (p15-s23-closed-core-origin-products
                          source-path
                          source-content-hash
                          function-path
                          root-record
                          root-syntax
                          expanded-root-syntax
                          indexes
                          token-ordinal-by-id
                          :defn-expansion)
        body-product (p15-s23-closed-core-map-sequence
                       ctx
                       instructions
                       body-form-ids
                       function-path
                       :instructions
                       {}
                       0
                       function-origin
                       :implicit-main-nil)
        body-node-by-path (into {} (map (juxt :path identity)) (:nodes body-product))
        body-node-ids (if (empty? instructions)
                        [(:result-node-id body-product)]
                        (mapv
                          #(get-in
                            body-node-by-path
                            [(p15-s23-closed-core-child-path
                               function-path
                               :instructions
                               %)
                             :node-id])
                          (range (count instructions))))
        function-node (p15-s23-closed-core-node
                        source-content-hash
                        function-path
                        :function
                        :function
                        false
                        0
                        body-node-ids
                        {:name entrypoint,
                         :params [],
                         :arity 0,
                         :visibility entrypoint-visibility}
                        {:params [],
                         :return (:type body-product),
                         :latent-effects (:effects body-product),
                         :capabilities (:capabilities body-product),
                         :throws #{},
                         :ownership-constraints #{:persistent-immutable-shareable},
                         :profile-constraints #{:hosted}}
                        (:effects body-product)
                        (:capabilities body-product)
                        (p15-s23-closed-core-persistent-ownership
                          :entrypoint-function-result
                          {:storage :forwarded-persistent-value,
                           :result-node-id (:result-node-id body-product),
                           :result-disposition :shared-persistent-value-return})
                        {:outcome :proven-safe, :basis :authenticated-closed-function}
                        (:profile ctx)
                        (:source function-origin))
        product (p15-s23-closed-core-add-node
                  body-product
                  function-node
                  function-origin)
        effectful-diagnostic-subject (p15-s23-closed-core-enriched-node-subject
                                       product
                                       function-node
                                       {:module (:module authoritative-module),
                                        :profile (:profile authoritative-module),
                                        :target (:target authoritative-module),
                                        :requested-target requested-target,
                                        :safety (:safety authoritative-module)}
                                       {})
        pre-authority-nodes (:nodes product)]
    (assoc
      state
      'ctx
      ctx
      'function-path
      function-path
      'function-origin
      function-origin
      'body-product
      body-product
      'body-node-by-path
      body-node-by-path
      'body-node-ids
      body-node-ids
      'function-node
      function-node
      'product
      product
      'effectful-diagnostic-subject
      effectful-diagnostic-subject
      'pre-authority-nodes
      pre-authority-nodes)))
