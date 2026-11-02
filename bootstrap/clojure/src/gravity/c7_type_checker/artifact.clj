(ns gravity.c7-type-checker.artifact
  "C7 source orchestration and typed-core artifact projection.")

(defn source-artifact
  [ops source-path source-text]
  (let [records ((:read-source-form-records ops) source-path source-text)
        forms (mapv :form records)
        _ ((:validate-ns-syntax! ops) source-path forms)
        module ((:parse-module ops) source-path forms)
        overrides ((:c7-type-source-overrides ops) module)
        _ ((:c7-type-validate-overrides! ops) source-path module overrides)
        c6-artifact ((:compiler-c6-lowering-source-artifact ops)
                     source-path source-text)
        nodes (:core-node-table c6-artifact)
        type-facts (mapv (:c7-type-fact ops) nodes)
        environment ((:c7-type-environment ops) type-facts)
        constraints ((:c7-constraint-ledger ops) type-facts)
        functions ((:c7-function-table ops) nodes)
        dynamic ((:c7-dynamic-boundary-records ops) nodes module)
        cast ((:c7-cast-records ops) nodes)
        generic ((:c7-generic-instantiations ops) nodes)
        dispatch ((:c7-protocol-dispatch-table ops) nodes)
        schema ((:c7-schema-links ops) (:domain-boundary-records c6-artifact))
        layout ((:c7-layout-facts ops) nodes)
        diagnostics ((:c7-type-diagnostics ops) source-path nodes)
        typed-core
        {:artifact :gravity/typed-core
         :module (get-in c6-artifact [:core-ast-module :module])
         :core-input (:artifact-id c6-artifact)
         :types (:types environment)
         :locals (:locals environment)
         :functions (:functions functions)
         :constraints (mapv :constraint-id (:constraints constraints))
         :dynamic-boundaries (mapv :boundary-id (:records dynamic))
         :casts (mapv :cast-id (:records cast))
         :layout-facts :c7-layout-facts
         :diagnostics []
         :status :complete}
        verifier ((:c7-typed-core-verifier-report ops)
                  nodes type-facts constraints functions dynamic cast generic
                  dispatch schema layout)
        diagnostic-ids (:c7-type-diagnostic-ids ops)
        governing-document (:c7-type-governing-document ops)
        artifact-base
        {:kind :gravity/stage0-c7-type-checker-artifact
         :task "P06-D086"
         :document-set ["C7"]
         :governing-document governing-document
         :pass {:name :c7-type-checker
                :input :verified-core-ast
                :output :typed-core
                :requires [:core-ast-module :core-node-table
                           :binding-table :profile :target
                           :domain-boundary-records]
                :preserves [:source-spans :generated-origin :metadata
                            :profile :target :effects :capabilities
                            :unsafe-metadata]
                :emits [:typed-core-module :type-environment
                        :constraint-ledger :generic-instantiation-table
                        :protocol-dispatch-type-table
                        :dynamic-boundary-records :cast-conversion-records
                        :layout-facts :schema-type-links
                        :typed-core-verifier-report :type-diagnostics]
                :rejects diagnostic-ids}
         :source-overrides overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c6-core-lowering-artifact
         (select-keys c6-artifact [:kind :artifact-id :core-ast-module
                                   :surface-to-core-map
                                   :evaluation-order-records
                                   :domain-boundary-records])
         :typed-core-module typed-core
         :type-environment environment
         :type-facts type-facts
         :constraint-ledger constraints
         :function-type-table functions
         :generic-instantiation-table generic
         :protocol-dispatch-type-table dispatch
         :dynamic-boundary-records dynamic
         :cast-conversion-records cast
         :layout-facts layout
         :schema-type-links schema
         :typed-core-verifier-report verifier
         :type-diagnostics diagnostics
         :c7-type-check-results
         {:documents ["C7"]
          :task "P06-D086"
          :required-diagnostic-ids diagnostic-ids
          :typed-core-status :complete
          :type-environment-status :complete
          :constraint-status :solved
          :generic-status :complete
          :protocol-status :complete
          :dynamic-boundary-status :complete
          :cast-status :complete
          :schema-link-status :complete
          :layout-status :complete
          :verifier-status (:status verifier)
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ ((:c7-type-validate! ops) source-path artifact-base)
        capability-proof ((:c7-type-capability-proof ops) artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id ((:c4-artifact-id ops)
                         (assoc artifact-base
                                :capability-based-proof
                                capability-proof)))))

(defn file-artifact
  [source-artifact path]
  (source-artifact path (slurp path)))
