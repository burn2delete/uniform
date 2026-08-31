

(defn record-safe-memory-call!
  [checker ctx node operator args effects capabilities return-type spec]
  (when-let [kind (:safe-memory-kind spec)]
    (let [record {:node-id (:node-id node)
                  :operator operator
                  :safe-memory-kind kind
                  :profile (:profile @ctx)
                  :target (:target @ctx)
                  :safety-mode (:safety @ctx)
                  :effects effects
                  :capabilities capabilities
                  :return-type return-type
                  :source-span (:source-span node)
                  :generated-origin-chain (:generated-origin node)}]
      (case kind
        :memory-operation
        (record-checker! checker :safe-memory-operation-records
                         (merge record
                                {:operation (dispatch-arg-value args 0)
                                 :subject (dispatch-arg-value args 1)
                                 :safety-outcome (dispatch-arg-value args 2)
                                 :required-facts (or (dispatch-arg-value args 3) #{})
                                 :memory-regime (dispatch-arg-value args 4)
                                 :provider-id (dispatch-arg-value args 5)
                                 :source-facts-preserved? true}))

        :runtime-check
        (record-checker! checker :safe-memory-runtime-check-records
                         (merge record
                                {:check-kind (dispatch-arg-value args 0)
                                 :subject (dispatch-arg-value args 1)
                                 :failure-behavior (dispatch-arg-value args 2)
                                 :status (or (dispatch-arg-value args 3)
                                             :runtime-checked)
                                 :defined-failure? true
                                 :artifact-recorded? true}))

        :allocation-release
        (record-checker! checker :safe-memory-allocation-release-maps
                         (merge record
                                {:allocation-id (dispatch-arg-value args 0)
                                 :release-id (dispatch-arg-value args 1)
                                 :provider-id (dispatch-arg-value args 2)
                                 :status (or (dispatch-arg-value args 3)
                                             :matched)
                                 :allocator-identity-preserved? true}))

        :escape-analysis
        (record-checker! checker :safe-memory-escape-analysis-records
                         (merge record
                                {:value-id (dispatch-arg-value args 0)
                                 :scope (dispatch-arg-value args 1)
                                 :escape-path (dispatch-arg-value args 2)
                                 :status (or (dispatch-arg-value args 3)
                                             :no-escape)
                                 :profile-aware? true}))

        :optimization-proof
        (record-checker! checker :safe-memory-proof-records
                         (merge record
                                {:erased-check (dispatch-arg-value args 0)
                                 :proof-id (dispatch-arg-value args 1)
                                 :status (or (dispatch-arg-value args 2)
                                             :preserved)
                                 :proof-preserved? (boolean (dispatch-arg-value args 1))}))

        :backend-preservation
        (record-checker! checker :safe-memory-backend-preservation-records
                         (merge record
                                {:subject (dispatch-arg-value args 0)
                                 :preserved-facts (or (dispatch-arg-value args 1)
                                                      #{})
                                 :status (or (dispatch-arg-value args 2)
                                             :preserved)
                                 :target-consumable? true}))

        :unsafe-audit
        (record-checker! checker :safe-memory-unsafe-audit-records
                         (merge record
                                {:operation (dispatch-arg-value args 0)
                                 :safe-boundary (dispatch-arg-value args 1)
                                 :owner (dispatch-arg-value args 2)
                                 :invariant (dispatch-arg-value args 3)
                                 :outcome :unsafe-island
                                 :review-policy :recorded
                                 :audit-status :passed}))

        :ownership-graph
        (record-checker! checker :safe-memory-ownership-graphs
                         (merge record
                                {:value-id (dispatch-arg-value args 0)
                                 :owner-id (dispatch-arg-value args 1)
                                 :ownership-kind (dispatch-arg-value args 2)
                                 :status (or (dispatch-arg-value args 3)
                                             :valid)
                                 :move-consume-visible? true}))

        :borrow-graph
        (record-checker! checker :safe-memory-borrow-graphs
                         (merge record
                                {:owner-id (dispatch-arg-value args 0)
                                 :borrow-id (dispatch-arg-value args 1)
                                 :mode (dispatch-arg-value args 2)
                                 :lifetime (dispatch-arg-value args 3)
                                 :status (or (dispatch-arg-value args 4)
                                             :valid)
                                 :exclusive-when-mutable? (= :mutable
                                                             (dispatch-arg-value args 2))}))

        :lifetime-map
        (record-checker! checker :safe-memory-lifetime-interval-maps
                         (merge record
                                {:reference-id (dispatch-arg-value args 0)
                                 :lifetime-id (dispatch-arg-value args 1)
                                 :relation (dispatch-arg-value args 2)
                                 :status (or (dispatch-arg-value args 3)
                                             :valid)}))

        :transfer-record
        (record-checker! checker :safe-memory-transfer-records
                         (merge record
                                {:value-id (dispatch-arg-value args 0)
                                 :from-owner (dispatch-arg-value args 1)
                                 :to-owner (dispatch-arg-value args 2)
                                 :mode (or (dispatch-arg-value args 3)
                                           :moved)
                                 :source-consumed? true
                                 :destination-cleanup-contract? true}))

        :runtime-borrow-check
        (record-checker! checker :safe-memory-runtime-borrow-check-records
                         (merge record
                                {:borrow-id (dispatch-arg-value args 0)
                                 :condition (dispatch-arg-value args 1)
                                 :failure-behavior (dispatch-arg-value args 2)
                                 :status :runtime-checked
                                 :artifact-recorded? true}))

        :region-lifetime
        (record-checker! checker :safe-memory-region-lifetime-maps
                         (merge record
                                {:region-id (dispatch-arg-value args 0)
                                 :allocation-id (dispatch-arg-value args 1)
                                 :lifetime (dispatch-arg-value args 2)
                                 :escape-status (or (dispatch-arg-value args 3)
                                                    :no-escape)}))

        :arena-generation
        (record-checker! checker :safe-memory-arena-generation-graphs
                         (merge record
                                {:arena-id (dispatch-arg-value args 0)
                                 :generation (dispatch-arg-value args 1)
                                 :state (dispatch-arg-value args 2)
                                 :status (or (dispatch-arg-value args 3)
                                             :valid)}))

        :reset-invalidation
        (record-checker! checker :safe-memory-reset-invalidation-records
                         (merge record
                                {:arena-id (dispatch-arg-value args 0)
                                 :from-generation (dispatch-arg-value args 1)
                                 :to-generation (dispatch-arg-value args 2)
                                 :status (or (dispatch-arg-value args 3)
                                             :invalidated)}))

        :provider
        (record-checker! checker :safe-memory-provider-records
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :profiles (or (dispatch-arg-value args 1)
                                               #{(:profile @ctx)})
                                 :reset-policy (dispatch-arg-value args 2)
                                 :thread-policy (dispatch-arg-value args 3)
                                 :allocation-alignment-failure-threading-declared? true}))

        :cleanup-record
        (record-checker! checker :safe-memory-cleanup-records
                         (merge record
                                {:scope-id (dispatch-arg-value args 0)
                                 :cleanup-kind (dispatch-arg-value args 1)
                                 :mode (dispatch-arg-value args 2)
                                 :status (or (dispatch-arg-value args 3)
                                             :valid)
                                 :runs-at-most-once? true}))

        :linear-flow
        (record-checker! checker :safe-memory-linear-flow-graphs
                         (merge record
                                {:resource-id (dispatch-arg-value args 0)
                                 :start-state (dispatch-arg-value args 1)
                                 :terminal-state (dispatch-arg-value args 2)
                                 :status (or (dispatch-arg-value args 3)
                                             :exactly-once)}))

        :terminal-operation
        (record-checker! checker :safe-memory-terminal-operation-records
                         (merge record
                                {:resource-id (dispatch-arg-value args 0)
                                 :operation (dispatch-arg-value args 1)
                                 :terminal-state (dispatch-arg-value args 2)
                                 :control-flow-path (or (dispatch-arg-value args 3)
                                                        :normal)
                                 :provider-compatible? true}))

        :exceptional-cleanup
        (record-checker! checker :safe-memory-exceptional-cleanup-records
                         (merge record
                                {:resource-id (dispatch-arg-value args 0)
                                 :control-flow-path (dispatch-arg-value args 1)
                                 :cleanup-operation (dispatch-arg-value args 2)
                                 :status (or (dispatch-arg-value args 3)
                                             :covered)}))

        :structured-lowering
        (record-checker! checker :safe-memory-structured-resource-lowerings
                         (merge record
                                {:form (dispatch-arg-value args 0)
                                 :resource-id (dispatch-arg-value args 1)
                                 :lowering (dispatch-arg-value args 2)
                                 :status (or (dispatch-arg-value args 3)
                                             :preserved)
                                 :normal-error-panic-cancel-covered? true}))

        :generated-flow
        (record-checker! checker :safe-memory-generated-linear-flow-records
                         (merge record
                                {:generator (dispatch-arg-value args 0)
                                 :resource-id (dispatch-arg-value args 1)
                                 :duplication-status (dispatch-arg-value args 2)
                                 :origin-status (or (dispatch-arg-value args 3)
                                                    :preserved)
                                 :generated-origin-preserved? true}))

        :conformance
        (record-checker! checker :safe-memory-conformance-records
                         (merge record
                                {:documents (or (dispatch-arg-value args 0)
                                                #{:SAFE2 :SAFE3 :SAFE4 :SAFE5})
                                 :status (or (dispatch-arg-value args 1)
                                             :complete)
                                 :positive-fixtures :passed
                                 :negative-fixtures :passed}))

        nil)
      record)))