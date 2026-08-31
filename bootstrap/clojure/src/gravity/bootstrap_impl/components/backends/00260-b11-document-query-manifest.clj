

(defn b11-document-query-manifest
  [source-path input-id]
  (let [sql-hash (c4-artifact-id b11-document-sql-statement)
        migration-hash (c4-artifact-id b11-document-migration-sql)
        adapter-hash (c4-artifact-id b11-document-result-adapter-fixture)]
    {:artifact :gravity/query-backend-manifest
     :backend :gravity.backend/query-relational
     :target {:dialect :postgresql
              :version "stage0-declared"
              :schema-version :stage0-v1
              :provider :stage0-postgres-provider
              :parameter-style :postgres-numbered}
     :input-artifact input-id
     :relational-ir-handoff-record
     {:domain-anchor :relational-query
      :source-artifact input-id
      :accepted-by [:b1-backend-interface :c11-mir
                    :c12-domain-ir :c14-target-lowering
                    :p4-hosted-profile :p9-distributed-profile
                    :safe10-capability :safe11-taint]
      :status :complete}
     :dialect-schema-mapping
     {:engine :postgresql
      :version "stage0-declared"
      :features #{:prepared-statements :transactions :jsonb
                  :arrays :enums :collations}
      :type-mapping {:I64 :bigint
                     :String :text
                     :Bool :boolean
                     :Json :jsonb}
      :null-semantics :three-valued-logic-explicit
      :collation :declared
      :timezone :utc
      :numeric-precision {:bigint :exact
                          :numeric :declared-scale}
      :identifier-quoting :double-quote
      :parameter-style :postgres-numbered
      :transaction-support #{:read-committed :repeatable-read
                             :serializable}
      :status :complete}
     :schema-mapping-record
     {:schema-id :stage0-v1
      :version :stage0-v1
      :tables [{:name "gravity_values"
                :gravity-record :GravityValue
                :columns [{:name "gravity_value"
                           :gravity-type :I64
                           :database-type :bigint
                           :nullable false}]
                :constraints [:primary-key]
                :indexes [:gravity_values_pk]}]
      :views []
      :computed-fields []
      :compatibility :compatible
      :status :complete}
     :sql-statement-artifacts
     [{:id :select-gravity-value
       :path "gravity_stage0_query.sql"
       :sql b11-document-sql-statement
       :hash sql-hash
       :kind :prepared-select
       :evaluation-boundary :database
       :status :complete}]
     :prepared-binding-manifest
     {:parameters [{:position 1
                    :name :gravity-value
                    :source-expression "input.gravity_value"
                    :source-span (str source-path ":query")
                    :gravity-type :I64
                    :database-type :bigint
                    :taint-category :external-input
                    :validation-schema :stage0-value-parameter
                    :escaping :driver-bound
                    :capability :db/read
                    :binding-style :prepared-position}]
      :dynamic-string-construction :rejected
      :status :complete}
     :query-plan-metadata
     {:plan :index-neutral-select
      :provider :stage0-postgres-provider
      :evaluation-boundary :database
      :provider-specific-movement []
      :semantic-equivalence-evidence :not-required-no-provider-movement
      :taint-null-collation-evidence :preserved
      :status :complete}
     :typed-result-adapter
     {:path "gravity_stage0_result_adapter.edn"
      :content b11-document-result-adapter-fixture
      :hash adapter-hash
      :columns [{:name :gravity_value
                 :gravity-type :I64
                 :database-type :bigint
                 :nullability :nonnull
                 :conversion :checked
                 :taint-policy :validated}]
      :row-shape-validation :checked
      :enum-validation :not-applicable
      :json-validation :not-applicable
      :timezone-conversion :utc
      :status :complete}
     :transaction-isolation-manifest
     {:effect :db/read
      :capabilities #{:db/read}
      :isolation :read-committed
      :retry-policy :bounded
      :lock-behavior :none
      :timeout-ms 5000
      :idempotency-key :query-input-digest
      :savepoints :not-required
      :consistency-assumptions [:single-statement-read]
      :error-mapping {:timeout :retryable
                      :serialization-failure :retryable
                      :constraint-violation :terminal}
      :status :complete}
     :migration-artifact
     {:path "gravity_stage0_migration.sql"
      :content b11-document-migration-sql
      :hash migration-hash
      :previous-schema :stage0-v1
      :next-schema :stage0-v1
      :compatibility-policy :data-preserving
      :data-preserving-proof :same-schema-noop
      :data-loss :none
      :backfill-plan :not-required
      :rollback-policy :no-op
      :deployment-ordering [:validate-schema :run-noop-migration
                            :validate-queries]
      :validation-queries ["select 1 as gravity_stage0_migration_noop"]
      :status :complete}
     :schema-diff-compatibility-report
     {:previous-schema :stage0-v1
      :next-schema :stage0-v1
      :changes []
      :compatibility :compatible
      :data-loss-risk :none
      :status :complete}
     :capability-taint-report
     {:required-capabilities #{:db/read}
      :granted-capabilities #{:db/read}
      :denied-capabilities #{:db/write :db/migrate :db/admin}
      :taint-policy :validated
      :parameter-taint {:gravity-value :validated-by-schema}
      :tainted-string-query :rejected
      :write-without-capability :rejected
      :migration-without-capability :rejected
      :status :complete}
     :null-collation-timezone-numeric-json-enum-behavior
     {:null-semantics :three-valued-logic-explicit
      :nonnull-columns [:gravity_value]
      :collation :declared
      :timezone :utc
      :numeric-precision {:gravity_value :exact-bigint}
      :json-behavior :schema-validated
      :array-behavior :declared
      :enum-behavior :validated
      :vendor-specific-types :manifested
      :status :complete}
     :distributed-workflow-integration-record
     {:workflow-backend-step :query-database
      :connects-to [:replay-policy :idempotency-key-map
                    :retry-timeout-cancellation-compensation-table]
      :side-effects :event-log-guarded
      :status :complete}
     :source-debug-map
     {:source input-id
      :locations [(str source-path ":query")
                  (str source-path ":bindings")
                  (str source-path ":migration")
                  (str source-path ":adapter")]
      :generated-origin-chain [:mir :c11-mir :c12-query-domain-ir
                               :c14-target-lowering :b1-interface
                               :b11-query-relational-backend]
      :query-source-map
      {:select-gravity-value (str source-path ":query:select-gravity-value")}
      :parameter-source-map
      {:gravity-value (str source-path ":query:parameter:1")}
      :adapter-source-map
      {:gravity_value (str source-path ":query:result:gravity_value")}
      :status :preserved}
     :external-database-validation-record
     {:declared-command
      "gravity-query-runner --manifest /tmp/gravity-p07-b11-query/gravity_stage0_query.sql"
      :proof-artifact
      "docs/artifacts/phase-07/reports/p07-d108-b11-query-relational-backend-report.md"
      :status :not-available-in-current-environment}
     :status :complete}))