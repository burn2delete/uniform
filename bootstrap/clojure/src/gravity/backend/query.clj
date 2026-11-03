(ns gravity.backend.query
  "Native B11 diagnostic lookup data; no query or input admission authority.")

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {source-overrides {:arglists ([module]), :admission :none},
  missing-policy {:arglists ([id]), :admission :none},
  query-id {:arglists ([id]), :admission :none}},
 :supported
 [:existing-native-projection
  :borrowed-identity
  :compatibility-var-interposition],
 :dependency-direction
 {:requires [clojure.core],
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
  :seed-retirement],
 :artifact-inputs [:native-module-metadata :native-b11-diagnostic-id],
 :namespace gravity.backend.query,
 :diagnostic-ownership
 {:stage [:native-override-projection :native-rule-context-defaults],
  :existing-owner
  [:b11-refusal-decisions
   :failure-construction
   :diagnostic-stream
   :source-admission
   :publication]},
 :artifact-outputs
 [:borrowed-source-overrides
  :native-missing-policy
  :native-query-label],
 :issues-certificates? false,
 :contract-boundary :native-b11-diagnostic-policy-data})

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

(doseq [operation [#'source-overrides #'missing-policy #'query-id]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
