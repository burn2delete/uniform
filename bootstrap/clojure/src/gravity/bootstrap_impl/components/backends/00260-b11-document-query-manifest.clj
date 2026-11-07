

(defn b11-document-query-manifest
  [source-path input-id]
  (gravity.backend.query/native-manifest
   {:artifact-id #'c4-artifact-id :sql-statement #'b11-document-sql-statement
    :migration-sql #'b11-document-migration-sql :result-adapter-fixture #'b11-document-result-adapter-fixture}
   source-path input-id))
