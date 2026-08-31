

(defn b11-document-validate!
  [source-path artifact]
  (let [specialized (:specialized-lowering-artifact artifact)
        manifest (:query-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b11-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-specialized-lowering-artifact
                 (:kind specialized))
      (b11-document-fail! "B11-MANIFEST" source-path specialized
                          {:missing-fields [:specialized-lowering-artifact]}))
    (when-not (= :complete (get-in specialized
                                   [:capability-based-proof :status]))
      (b11-document-fail! "B11-MANIFEST" source-path specialized
                          {:missing-fields [:specialized-lowering-proof]}))
    (when-not (= :postgresql (get-in manifest [:target :dialect]))
      (b11-document-fail! "B11-DIALECT" source-path manifest
                          {:missing-fields [:target-dialect]}))
    (when-not (= :complete (get-in manifest
                                   [:relational-ir-handoff-record :status]))
      (b11-document-fail! "B11-MANIFEST" source-path manifest
                          {:missing-fields [:relational-ir-handoff-record]}))
    (when-not (= :complete (get-in manifest
                                   [:dialect-schema-mapping :status]))
      (b11-document-fail! "B11-DIALECT" source-path manifest
                          {:missing-fields [:dialect-schema-mapping]}))
    (when-not (= :complete (get-in manifest
                                   [:schema-mapping-record :status]))
      (b11-document-fail! "B11-SCHEMA" source-path manifest
                          {:missing-fields [:schema-mapping-record]}))
    (when-not (= :complete (get-in manifest
                                   [:sql-statement-artifacts 0 :status]))
      (b11-document-fail! "B11-MANIFEST" source-path manifest
                          {:missing-fields [:sql-statement-artifacts]}))
    (when-not (b11-document-sql-structurally-valid?
               b11-document-sql-statement)
      (b11-document-fail! "B11-TAINT" source-path manifest
                          {:missing-fields [:parameterized-sql]}))
    (when-not (= :complete (get-in manifest
                                   [:prepared-binding-manifest :status]))
      (b11-document-fail! "B11-PARAMETER" source-path manifest
                          {:missing-fields [:prepared-binding-manifest]}))
    (when-not (= :rejected (get-in manifest
                                   [:prepared-binding-manifest
                                    :dynamic-string-construction]))
      (b11-document-fail! "B11-TAINT" source-path manifest
                          {:missing-fields [:dynamic-string-rejection]}))
    (when-not (= :complete (get-in manifest
                                   [:query-plan-metadata :status]))
      (b11-document-fail! "B11-PLAN" source-path manifest
                          {:missing-fields [:query-plan-metadata]}))
    (when-not (empty? (get-in manifest
                              [:query-plan-metadata
                               :provider-specific-movement]))
      (b11-document-fail! "B11-PLAN" source-path manifest
                          {:missing-fields [:provider-plan-equivalence]}))
    (when-not (= :complete (get-in manifest
                                   [:typed-result-adapter :status]))
      (b11-document-fail! "B11-RESULT" source-path manifest
                          {:missing-fields [:typed-result-adapter]}))
    (when-not (b11-document-result-adapter-structurally-valid?
               b11-document-result-adapter-fixture)
      (b11-document-fail! "B11-RESULT" source-path manifest
                          {:missing-fields [:result-adapter-validation]}))
    (when-not (= :complete (get-in manifest
                                   [:transaction-isolation-manifest
                                    :status]))
      (b11-document-fail! "B11-TRANSACTION" source-path manifest
                          {:missing-fields [:transaction-isolation-manifest]}))
    (when-not (= :complete (get-in manifest [:migration-artifact :status]))
      (b11-document-fail! "B11-MIGRATION" source-path manifest
                          {:missing-fields [:migration-artifact]}))
    (when-not (= :none (get-in manifest [:migration-artifact :data-loss]))
      (b11-document-fail! "B11-MIGRATION" source-path manifest
                          {:missing-fields [:data-loss-policy]}))
    (when-not (= :complete (get-in manifest
                                   [:schema-diff-compatibility-report
                                    :status]))
      (b11-document-fail! "B11-SCHEMA" source-path manifest
                          {:missing-fields [:schema-diff-compatibility]}))
    (when-not (= :complete (get-in manifest
                                   [:capability-taint-report :status]))
      (b11-document-fail! "B11-CAPABILITY" source-path manifest
                          {:missing-fields [:capability-taint-report]}))
    (when-not (= :rejected (get-in manifest
                                   [:capability-taint-report
                                    :tainted-string-query]))
      (b11-document-fail! "B11-TAINT" source-path manifest
                          {:missing-fields [:tainted-string-query-rejection]}))
    (when-not (= :complete
                 (get-in manifest
                         [:null-collation-timezone-numeric-json-enum-behavior
                          :status]))
      (b11-document-fail! "B11-NULL" source-path manifest
                          {:missing-fields [:null-type-behavior-map]}))
    (when-not (= :complete (get-in manifest
                                   [:distributed-workflow-integration-record
                                    :status]))
      (b11-document-fail! "B11-TRANSACTION" source-path manifest
                          {:missing-fields [:workflow-integration-record]}))
    (when-not (= :preserved (get-in manifest [:source-debug-map :status]))
      (b11-document-fail! "B11-MANIFEST" source-path manifest
                          {:missing-fields [:source-debug-map]}))
    (when-not (every? #(contains? manifest %)
                      [:relational-ir-handoff-record
                       :dialect-schema-mapping
                       :schema-mapping-record
                       :sql-statement-artifacts
                       :prepared-binding-manifest
                       :query-plan-metadata
                       :typed-result-adapter
                       :transaction-isolation-manifest
                       :migration-artifact
                       :schema-diff-compatibility-report
                       :capability-taint-report
                       :null-collation-timezone-numeric-json-enum-behavior
                       :distributed-workflow-integration-record
                       :source-debug-map
                       :external-database-validation-record])
      (b11-document-fail! "B11-MANIFEST" source-path manifest
                          {:missing-fields [:query-artifact-manifest]}))
    (when-not (= (set b11-document-diagnostic-ids) diagnostics)
      (b11-document-fail! "B11-MANIFEST" source-path
                          (:b11-diagnostic-stream artifact)
                          {:missing-fields [:b11-diagnostics]})))
  :complete)

(defn b11-document-capability-proof
  [artifact]
  (let [manifest (:query-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b11-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:specialized-lowering-artifact
                           :capability-based-proof :status]))
     :relational-ir-handoff-covered?
     (= :complete (get-in manifest
                          [:relational-ir-handoff-record :status]))
     :dialect-and-schema-mapped?
     (and (= :complete (get-in manifest
                               [:dialect-schema-mapping :status]))
          (= :complete (get-in manifest
                               [:schema-mapping-record :status])))
     :sql-statement-emitted?
     (= :complete (get-in manifest [:sql-statement-artifacts 0 :status]))
     :sql-parameterized?
     (b11-document-sql-structurally-valid? b11-document-sql-statement)
     :prepared-bindings-covered?
     (= :complete (get-in manifest [:prepared-binding-manifest :status]))
     :tainted-dynamic-sql-rejected?
     (and (= :rejected (get-in manifest
                               [:prepared-binding-manifest
                                :dynamic-string-construction]))
          (= :rejected (get-in manifest
                               [:capability-taint-report
                                :tainted-string-query])))
     :query-plan-covered?
     (and (= :complete (get-in manifest [:query-plan-metadata :status]))
          (empty? (get-in manifest
                          [:query-plan-metadata
                           :provider-specific-movement])))
     :typed-result-adapter-covered?
     (and (= :complete (get-in manifest [:typed-result-adapter :status]))
          (b11-document-result-adapter-structurally-valid?
           b11-document-result-adapter-fixture))
     :transaction-and-isolation-covered?
     (= :complete (get-in manifest
                          [:transaction-isolation-manifest :status]))
     :migration-and-compatibility-covered?
     (and (= :complete (get-in manifest [:migration-artifact :status]))
          (= :none (get-in manifest [:migration-artifact :data-loss]))
          (= :complete (get-in manifest
                               [:schema-diff-compatibility-report
                                :status])))
     :capability-and-taint-covered?
     (= :complete (get-in manifest [:capability-taint-report :status]))
     :null-and-type-behavior-covered?
     (= :complete
        (get-in manifest
                [:null-collation-timezone-numeric-json-enum-behavior
                 :status]))
     :workflow-integration-covered?
     (= :complete (get-in manifest
                          [:distributed-workflow-integration-record
                           :status]))
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :manifest-complete?
     (every? #(contains? manifest %)
             [:relational-ir-handoff-record
              :dialect-schema-mapping
              :schema-mapping-record
              :sql-statement-artifacts
              :prepared-binding-manifest
              :query-plan-metadata
              :typed-result-adapter
              :transaction-isolation-manifest
              :migration-artifact
              :schema-diff-compatibility-report
              :capability-taint-report
              :null-collation-timezone-numeric-json-enum-behavior
              :distributed-workflow-integration-record
              :source-debug-map
              :external-database-validation-record])
     :diagnostics-covered?
     (= (set b11-document-diagnostic-ids) diagnostics)
     :external-database-validation?
     (get-in manifest [:external-database-validation-record :status])
     :status :complete}))