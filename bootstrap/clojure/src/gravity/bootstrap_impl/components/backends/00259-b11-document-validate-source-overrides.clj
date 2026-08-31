

(defn b11-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b11-document-override-diagnostics fail-kind)]
      (b11-document-fail!
       id source-path
       {:artifact-id (str "b11-document-" (name fail-kind))
        :query-id (keyword (str "b11-document-" (name fail-kind)))}
       {:missing-fields [fail-kind]}))))

(def b11-document-sql-statement
  "select $1::bigint as gravity_value\n")

(def b11-document-migration-sql
  (str
   "-- gravity stage0 schema compatibility migration\n"
   "-- no data rewrite; schema id remains stage0-v1\n"
   "select 1 as gravity_stage0_migration_noop;\n"))

(def b11-document-result-adapter-fixture
  (str
   "{:adapter :gravity-stage0-value-row\n"
   " :columns [{:name :gravity_value\n"
   "            :gravity-type :I64\n"
   "            :database-type :bigint\n"
   "            :nullability :nonnull\n"
   "            :validation :checked}]\n"
   " :taint-policy :validated\n"
   " :status :complete}\n"))

(defn b11-document-sql-structurally-valid?
  [text]
  (and (str/includes? text "$1::bigint")
       (str/includes? text "gravity_value")
       (not (str/includes? text "||"))
       (not (str/includes? text "${"))
       (not (str/includes? text "concat("))))

(defn b11-document-result-adapter-structurally-valid?
  [text]
  (and (str/includes? text ":gravity-type :I64")
       (str/includes? text ":database-type :bigint")
       (str/includes? text ":nullability :nonnull")
       (str/includes? text ":validation :checked")
       (str/includes? text ":taint-policy :validated")))

(defn b11-document-diagnostic-stream
  [source-path input-id]
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
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b11-document-syntax-" index)
                      :artifact input-id}
            :profile :hosted
            :target :postgresql
            :query-id (b11-document-query-id id)
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
            :missing-policy (b11-document-missing-policy id)
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
         b11-document-diagnostic-ids
         (range))
   :status :complete})