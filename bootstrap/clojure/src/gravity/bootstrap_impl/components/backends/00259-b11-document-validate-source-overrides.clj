

(defn b11-document-validate-source-overrides!
  [source-path overrides]
  (gravity.backend.query/validate-source-overrides!
   source-path overrides
   {:override-diagnostics (fn [] b11-document-override-diagnostics)
    :failure-operation (fn [] b11-document-fail!)}))

(def b11-document-sql-statement
  gravity.backend.query/sql-statement)

(def b11-document-migration-sql
  gravity.backend.query/migration-sql)

(def b11-document-result-adapter-fixture
  gravity.backend.query/result-adapter-fixture)

(defn b11-document-sql-structurally-valid?
  [text]
  (gravity.backend.query/sql-structurally-valid? text))

(defn b11-document-result-adapter-structurally-valid?
  [text]
  (gravity.backend.query/result-adapter-structurally-valid? text))

(defn b11-document-diagnostic-stream
  [source-path input-id]
  (gravity.backend.query/diagnostic-stream
   source-path input-id
   {:source-span (fn [path index] (source-span path index))
    :diagnostic-ids (fn [] b11-document-diagnostic-ids)
    :query-id (fn [id] (b11-document-query-id id))
    :missing-policy (fn [id] (b11-document-missing-policy id))}))
