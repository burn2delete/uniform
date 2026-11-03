(ns gravity.backend.query
  "Native B11 diagnostic context, catalog and fixture data; no backend or input admission authority."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {validate-source-overrides!
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
 [:existing-native-projection
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
  :forbids [gravity.bootstrap gravity.cli gravity.release]},
 :governing-contracts
 [D1 D2 D3 D8 D9 C1 C15 B1 B11 B13 B14 BOOT8 TEST2],
 :unsupported
 [:canonical-b11-semantics
  :gravity-b11-source-compilation
  :verified-relational-or-schema-ingress
  :sql-or-plan-emission
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
 [:native-module-metadata
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
  [:native-override-projection
   :native-rule-context-defaults
   :native-catalog-assembly
   :native-fixture-recognition
   :fixed-native-reference-text
   :native-failure-payload-assembly
   :native-explicit-override-refusal],
  :existing-owner
  [:native-artifact-validation-decisions
   :diagnostic-throw-transport
   :source-admission
   :publication]},
 :artifact-outputs
 [:borrowed-source-overrides
  :native-missing-policy
  :native-query-label
  :native-diagnostic-catalog-stream
  :native-fixture-recognition-result
  :fixed-native-reference-text
  :native-refusal-callback-result],
 :consumer-component-paths
 ["bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00258-b10-document-source-artifact.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00259-b11-document-validate-source-overrides.clj"],
 :issues-certificates? false,
 :contract-boundary
 :native-b11-document-context-catalog-and-fixture-data})

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

(doseq [operation [#'source-overrides #'missing-policy #'query-id #'diagnostic-stream #'sql-structurally-valid? #'result-adapter-structurally-valid? #'sql-statement #'migration-sql #'result-adapter-fixture #'fail! #'validate-source-overrides!]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
