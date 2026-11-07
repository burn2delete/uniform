(ns gravity.backend.query
  "Native B11 document data, diagnostics and artifact checks; no backend or input admission authority."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {native-manifest
  {:arglists ([operations source-path input-id]), :admission :none,
   :contract-boundary :native-query-document-data,
   :required-operations [:artifact-id :sql-statement :migration-sql :result-adapter-fixture],
   :artifact-inputs [:unverified-source-path :unverified-input-id :fixed-native-reference-text],
   :artifact-outputs [:legacy-query-document-manifest],
   :authenticates-inputs? false, :grants-authority? false, :issues-certificates? false},
  validate-native-manifest!
  {:arglists ([operations source-path artifact]), :admission :none,
   :contract-boundary :native-query-document-checks,
   :required-operations [:fail! :sql-statement :result-adapter-fixture
                         :sql-structurally-valid? :result-adapter-structurally-valid? :diagnostic-ids],
   :artifact-inputs [:unverified-document-artifact-data],
   :artifact-outputs [:legacy-structural-completion-or-refusal],
   :diagnostics ["B11-MANIFEST" "B11-DIALECT" "B11-SCHEMA" "B11-TAINT" "B11-PARAMETER"
                 "B11-PLAN" "B11-RESULT" "B11-TRANSACTION" "B11-MIGRATION" "B11-CAPABILITY" "B11-NULL"],
   :diagnostic-publication :caller-owned,
   :authenticates-inputs? false, :grants-authority? false, :issues-certificates? false},
  native-coverage-report
  {:arglists ([operations artifact]), :admission :none,
   :contract-boundary :native-query-document-data,
   :required-operations [:sql-statement :result-adapter-fixture :sql-structurally-valid?
                         :result-adapter-structurally-valid? :diagnostic-ids],
   :artifact-inputs [:unverified-document-artifact-data],
   :artifact-outputs [:legacy-document-coverage-record],
   :authenticates-inputs? false, :grants-authority? false, :issues-certificates? false},
  validate-source-overrides!
  {:arglists ([source-path overrides operations]),
   :admission :none,
   :required-operations [:override-diagnostics :failure-operation]},
  source-overrides {:arglists ([module]), :admission :none},
  fail!
  {:arglists ([id source-path subject extra operations]),
   :admission :none,
   :required-operations
   [:fail! :source-span :query-id :missing-policy],
   :callback-return :propagated},
  sql-statement
  {:value-kind :string,
   :artifact-role :fixed-native-reference-fixture,
   :admission :none},
  migration-sql
  {:value-kind :string,
   :artifact-role :fixed-native-reference-fixture,
   :admission :none},
  query-id {:arglists ([id]), :admission :none},
  result-adapter-fixture
  {:value-kind :string,
   :artifact-role :fixed-native-reference-fixture,
   :admission :none},
  sql-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text},
  result-adapter-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text},
  diagnostic-stream
  {:arglists ([source-path input-id operations]),
   :admission :none,
   :required-operations
   [:source-span :diagnostic-ids :query-id :missing-policy]},
  missing-policy {:arglists ([id]), :admission :none}},
 :supported
 [:native-manifest-construction
  :native-artifact-validation-decisions
  :native-coverage-projection
  :existing-native-projection
  :borrowed-identity
  :compatibility-var-interposition
  :native-catalog-assembly
  :explicit-native-operations
  :existing-native-fixture-recognition
  :fixed-native-reference-text
  :native-failure-payload-assembly
  :native-explicit-override-refusal],
 :dependency-direction
 {:requires [clojure.core clojure.string],
  :consumed-by [gravity.bootstrap],
  :must-not-require [gravity.bootstrap gravity.cli gravity.release]},
 :governing-contracts
 [D1 D2 D3 D8 D9 C1 C15 B1 B11 B13 B14 BOOT8 TEST2],
 :unsupported
 [:canonical-b11-semantics
  :gravity-b11-source-compilation
  :verified-relational-or-schema-ingress
  :canonical-sql-or-plan-emission
  :database-execution
  :dialect-schema-type-or-null-validation
  :parameter-or-taint-validation
  :transaction-or-capability-validation
  :migration-or-result-adapter-validation
  :provider-plan-equivalence
  :downstream-handoff
  :source-authentication
  :public-diagnostic-redaction
  :migration
  :release
  :self-hosting
  :seed-retirement
  :canonical-c15-conformance
  :catalog-as-observed-semantic-findings
  :native-operations-as-proof-authority
  :canonical-target-syntax-validation
  :fixture-text-as-verified-ir
  :fixture-match-as-capability-or-proof-authority
  :fixture-text-as-lowering-or-execution
  :refusal-callback-as-admission-or-proof
  :canonical-diagnostic-publication],
 :artifact-inputs
 [:unverified-native-document-data
  :native-manifest-operations
  :native-module-metadata
  :native-b11-diagnostic-id
  :native-source-path
  :supplied-input-artifact-id
  :native-diagnostic-operations
  :native-document-fixture-text
  :fixed-native-fixture-definitions
  :native-diagnostic-subject
  :native-diagnostic-extra
  :native-refusal-operations],
 :namespace gravity.backend.query,
 :diagnostic-ownership
 {:stage
  [:native-artifact-validation-decisions
   :native-override-projection
   :native-rule-context-defaults
   :native-catalog-assembly
   :native-fixture-recognition
   :fixed-native-reference-text
   :native-failure-payload-assembly
   :native-explicit-override-refusal],
  :existing-owner
  [:diagnostic-throw-transport
   :source-admission
   :publication]},
 :artifact-outputs
 [:legacy-query-document-manifest
  :legacy-structural-completion-or-refusal
  :legacy-document-coverage-record
  :borrowed-source-overrides
  :native-missing-policy
  :native-query-label
  :native-diagnostic-catalog-stream
  :native-fixture-recognition-result
  :fixed-native-reference-text
  :native-refusal-callback-result],
 :consumer-component-paths
 ["bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00258-b10-document-source-artifact.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00259-b11-document-validate-source-overrides.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00260-b11-document-query-manifest.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00261-b11-document-validate.clj"],
 :issues-certificates? false,
 :contract-boundary
 :native-b11-document-data})

(defn source-overrides
  [module]
  (get-in module [:metadata :backend :specialized-lowering] {}))

(defn missing-policy
  [id]
  (case id
    "B11-DIALECT" :dialect-version-type-map
    "B11-SCHEMA" :schema-version-compatibility
    "B11-TAINT" :taint-proof-for-sql-syntax
    "B11-PARAMETER" :prepared-parameter-bindings
    "B11-CAPABILITY" :database-authority-grant
    "B11-TRANSACTION" :transaction-isolation-retry-timeout
    "B11-NULL" :sql-null-three-valued-logic-map
    "B11-MIGRATION" :migration-compatibility-and-data-loss-policy
    "B11-RESULT" :typed-result-adapter-validation
    "B11-PLAN" :provider-plan-semantic-equivalence
    :query-artifact-manifest))

(defn query-id
  [id]
  (case id
    "B11-DIALECT" :dialect-map
    "B11-SCHEMA" :schema-map
    "B11-TAINT" :dynamic-sql
    "B11-PARAMETER" :select-gravity-value
    "B11-CAPABILITY" :write-gravity-value
    "B11-TRANSACTION" :transaction-record
    "B11-NULL" :null-semantics
    "B11-MIGRATION" :stage0-migration
    "B11-RESULT" :gravity-value-adapter
    "B11-PLAN" :provider-plan
    :query-manifest))

(defn diagnostic-stream
  [source-path input-id operations]
  {:artifact :gravity/b11-query-relational-backend-diagnostic-stream
   :stage :b11-query-relational-backend-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b11-query-relational-backend-document-coverage
            :backend :gravity.backend/query-relational
            :message-key (keyword "backend-query" (str/lower-case id))
            :primary {:span ((:source-span operations) source-path index)
                      :syntax-id (str "b11-document-syntax-" index)
                      :artifact input-id}
            :profile :hosted
            :target :postgresql
            :query-id ((:query-id operations) id)
            :schema-id :stage0-v1
            :dialect :postgresql
            :parameter-id (case id
                            "B11-PARAMETER" :gravity-value
                            "B11-TAINT" :dynamic-fragment
                            :gravity-value)
            :column-id (case id
                         "B11-RESULT" :gravity_value
                         "B11-NULL" :gravity_value
                         :gravity_value)
            :effect (case id
                      "B11-CAPABILITY" :db/write
                      "B11-MIGRATION" :db/migrate
                      "B11-TRANSACTION" :db/read
                      :db/read)
            :capability (case id
                          "B11-CAPABILITY" :db/write
                          "B11-MIGRATION" :db/migrate
                          :db/read)
            :taint-category (case id
                              "B11-TAINT" :external-input
                              "B11-PARAMETER" :external-input
                              :validated)
            :migration-state (case id
                               "B11-MIGRATION" :drift-without-policy
                               :compatible)
            :missing-policy ((:missing-policy operations) id)
            :source-generated-origin-chain
            [:mir :c11-mir :c12-query-domain-ir
             :c14-target-lowering :b1-interface
             :b11-query-relational-backend]
            :fallback-status :rejected
            :facts {:prepared-statements-required true
                    :dynamic-tainted-sql-rejected true
                    :result-adapter-validation-required true
                    :migration-data-loss-requires-policy true
                    :provider-plan-movement-requires-evidence true}
            :remediation [{:kind :declare-dialect-schema-provider}
                          {:kind :parameterize-query-inputs}
                          {:kind :attach-capability-taint-policy}
                          {:kind :record-transaction-migration-adapter-plan}]
            :redactions []
            :ordering-key [id :b11-query-relational-backend-document-coverage
                           :postgresql]})
         ((:diagnostic-ids operations))
         (range))
   :status :complete})

(defn sql-structurally-valid?
  [text]
  (and (str/includes? text "$1::bigint")
       (str/includes? text "gravity_value")
       (not (str/includes? text "||"))
       (not (str/includes? text "${"))
       (not (str/includes? text "concat("))))

(defn result-adapter-structurally-valid?
  [text]
  (and (str/includes? text ":gravity-type :I64")
       (str/includes? text ":database-type :bigint")
       (str/includes? text ":nullability :nonnull")
       (str/includes? text ":validation :checked")
       (str/includes? text ":taint-policy :validated")))

(def sql-statement
  "select $1::bigint as gravity_value\n")

(def migration-sql
  (str
   "-- gravity stage0 schema compatibility migration\n"
   "-- no data rewrite; schema id remains stage0-v1\n"
   "select 1 as gravity_stage0_migration_noop;\n"))

(def result-adapter-fixture
  (str
   "{:adapter :gravity-stage0-value-row\n"
   " :columns [{:name :gravity_value\n"
   "            :gravity-type :I64\n"
   "            :database-type :bigint\n"
   "            :nullability :nonnull\n"
   "            :validation :checked}]\n"
   " :taint-policy :validated\n"
   " :status :complete}\n"))

(defn fail!
  [id source-path subject extra operations]
  ((:fail! operations) id
         "B11 query/relational backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  ((:source-span operations) source-path 0))
                 :diagnostic-family :b11-query-relational-backend-document
                 :stage :b11-query-relational-backend-document-coverage
                 :backend :gravity.backend/query-relational
                 :profile :hosted
                 :target :postgresql
                 :query-id (or (:query-id subject)
                               ((:query-id operations) id))
                 :schema-id (or (:schema-id subject) :stage0-v1)
                 :dialect (or (:dialect subject) :postgresql)
                 :parameter-id (or (:parameter-id subject) :gravity-value)
                 :column-id (or (:column-id subject) :gravity_value)
                 :effect (or (:effect subject) :db/read)
                 :capability (or (:capability subject) :db/read)
                 :taint-category (or (:taint-category subject)
                                     :external-input)
                 :migration-state (or (:migration-state subject)
                                      :compatible)
                 :missing-policy ((:missing-policy operations) id)
                 :fallback-status :rejected
                 :remediation "B11 requires verified relational IR, explicit dialect/schema/version/provider records, parameterized SQL, capability and taint evidence, transaction and migration policy, null/type/result adapters, provider-plan evidence, and a complete query artifact manifest."}
                extra)))

(defn validate-source-overrides!
  [source-path overrides operations]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get ((:override-diagnostics operations)) fail-kind)]
      (((:failure-operation operations))
       id source-path
       {:artifact-id (str "b11-document-" (name fail-kind))
        :query-id (keyword (str "b11-document-" (name fail-kind)))}
       {:missing-fields [fail-kind]}))))

(defn- native-document-port [operations key]
  (let [operation (get operations key)]
    (if (var? operation) @operation operation)))

(defn native-manifest
  [operations source-path input-id]
  (let [sql-hash ((native-document-port operations :artifact-id) (native-document-port operations :sql-statement))
        migration-hash ((native-document-port operations :artifact-id) (native-document-port operations :migration-sql))
        adapter-hash ((native-document-port operations :artifact-id) (native-document-port operations :result-adapter-fixture))]
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
       :sql (native-document-port operations :sql-statement)
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
      :content (native-document-port operations :result-adapter-fixture)
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
      :content (native-document-port operations :migration-sql)
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
(defn validate-native-manifest!
  [operations source-path artifact]
  (let [specialized (:specialized-lowering-artifact artifact)
        manifest (:query-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b11-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-specialized-lowering-artifact
                 (:kind specialized))
      ((native-document-port operations :fail!) "B11-MANIFEST" source-path specialized
                          {:missing-fields [:specialized-lowering-artifact]}))
    (when-not (= :complete (get-in specialized
                                   [:capability-based-proof :status]))
      ((native-document-port operations :fail!) "B11-MANIFEST" source-path specialized
                          {:missing-fields [:specialized-lowering-proof]}))
    (when-not (= :postgresql (get-in manifest [:target :dialect]))
      ((native-document-port operations :fail!) "B11-DIALECT" source-path manifest
                          {:missing-fields [:target-dialect]}))
    (when-not (= :complete (get-in manifest
                                   [:relational-ir-handoff-record :status]))
      ((native-document-port operations :fail!) "B11-MANIFEST" source-path manifest
                          {:missing-fields [:relational-ir-handoff-record]}))
    (when-not (= :complete (get-in manifest
                                   [:dialect-schema-mapping :status]))
      ((native-document-port operations :fail!) "B11-DIALECT" source-path manifest
                          {:missing-fields [:dialect-schema-mapping]}))
    (when-not (= :complete (get-in manifest
                                   [:schema-mapping-record :status]))
      ((native-document-port operations :fail!) "B11-SCHEMA" source-path manifest
                          {:missing-fields [:schema-mapping-record]}))
    (when-not (= :complete (get-in manifest
                                   [:sql-statement-artifacts 0 :status]))
      ((native-document-port operations :fail!) "B11-MANIFEST" source-path manifest
                          {:missing-fields [:sql-statement-artifacts]}))
    (when-not ((native-document-port operations :sql-structurally-valid?)
               (native-document-port operations :sql-statement))
      ((native-document-port operations :fail!) "B11-TAINT" source-path manifest
                          {:missing-fields [:parameterized-sql]}))
    (when-not (= :complete (get-in manifest
                                   [:prepared-binding-manifest :status]))
      ((native-document-port operations :fail!) "B11-PARAMETER" source-path manifest
                          {:missing-fields [:prepared-binding-manifest]}))
    (when-not (= :rejected (get-in manifest
                                   [:prepared-binding-manifest
                                    :dynamic-string-construction]))
      ((native-document-port operations :fail!) "B11-TAINT" source-path manifest
                          {:missing-fields [:dynamic-string-rejection]}))
    (when-not (= :complete (get-in manifest
                                   [:query-plan-metadata :status]))
      ((native-document-port operations :fail!) "B11-PLAN" source-path manifest
                          {:missing-fields [:query-plan-metadata]}))
    (when-not (empty? (get-in manifest
                              [:query-plan-metadata
                               :provider-specific-movement]))
      ((native-document-port operations :fail!) "B11-PLAN" source-path manifest
                          {:missing-fields [:provider-plan-equivalence]}))
    (when-not (= :complete (get-in manifest
                                   [:typed-result-adapter :status]))
      ((native-document-port operations :fail!) "B11-RESULT" source-path manifest
                          {:missing-fields [:typed-result-adapter]}))
    (when-not ((native-document-port operations :result-adapter-structurally-valid?)
               (native-document-port operations :result-adapter-fixture))
      ((native-document-port operations :fail!) "B11-RESULT" source-path manifest
                          {:missing-fields [:result-adapter-validation]}))
    (when-not (= :complete (get-in manifest
                                   [:transaction-isolation-manifest
                                    :status]))
      ((native-document-port operations :fail!) "B11-TRANSACTION" source-path manifest
                          {:missing-fields [:transaction-isolation-manifest]}))
    (when-not (= :complete (get-in manifest [:migration-artifact :status]))
      ((native-document-port operations :fail!) "B11-MIGRATION" source-path manifest
                          {:missing-fields [:migration-artifact]}))
    (when-not (= :none (get-in manifest [:migration-artifact :data-loss]))
      ((native-document-port operations :fail!) "B11-MIGRATION" source-path manifest
                          {:missing-fields [:data-loss-policy]}))
    (when-not (= :complete (get-in manifest
                                   [:schema-diff-compatibility-report
                                    :status]))
      ((native-document-port operations :fail!) "B11-SCHEMA" source-path manifest
                          {:missing-fields [:schema-diff-compatibility]}))
    (when-not (= :complete (get-in manifest
                                   [:capability-taint-report :status]))
      ((native-document-port operations :fail!) "B11-CAPABILITY" source-path manifest
                          {:missing-fields [:capability-taint-report]}))
    (when-not (= :rejected (get-in manifest
                                   [:capability-taint-report
                                    :tainted-string-query]))
      ((native-document-port operations :fail!) "B11-TAINT" source-path manifest
                          {:missing-fields [:tainted-string-query-rejection]}))
    (when-not (= :complete
                 (get-in manifest
                         [:null-collation-timezone-numeric-json-enum-behavior
                          :status]))
      ((native-document-port operations :fail!) "B11-NULL" source-path manifest
                          {:missing-fields [:null-type-behavior-map]}))
    (when-not (= :complete (get-in manifest
                                   [:distributed-workflow-integration-record
                                    :status]))
      ((native-document-port operations :fail!) "B11-TRANSACTION" source-path manifest
                          {:missing-fields [:workflow-integration-record]}))
    (when-not (= :preserved (get-in manifest [:source-debug-map :status]))
      ((native-document-port operations :fail!) "B11-MANIFEST" source-path manifest
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
      ((native-document-port operations :fail!) "B11-MANIFEST" source-path manifest
                          {:missing-fields [:query-artifact-manifest]}))
    (when-not (= (set (native-document-port operations :diagnostic-ids)) diagnostics)
      ((native-document-port operations :fail!) "B11-MANIFEST" source-path
                          (:b11-diagnostic-stream artifact)
                          {:missing-fields [:b11-diagnostics]})))
  :complete)

(defn native-coverage-report
  [operations artifact]
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
     ((native-document-port operations :sql-structurally-valid?) (native-document-port operations :sql-statement))
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
          ((native-document-port operations :result-adapter-structurally-valid?)
           (native-document-port operations :result-adapter-fixture)))
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
     (= (set (native-document-port operations :diagnostic-ids)) diagnostics)
     :external-database-validation?
     (get-in manifest [:external-database-validation-record :status])
     :status :complete}))
(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
