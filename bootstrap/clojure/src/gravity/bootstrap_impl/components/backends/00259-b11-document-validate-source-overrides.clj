

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
  (gravity.backend.query/diagnostic-stream
   source-path input-id
   {:source-span (fn [path index] (source-span path index))
    :diagnostic-ids (fn [] b11-document-diagnostic-ids)
    :query-id (fn [id] (b11-document-query-id id))
    :missing-policy (fn [id] (b11-document-missing-policy id))}))
