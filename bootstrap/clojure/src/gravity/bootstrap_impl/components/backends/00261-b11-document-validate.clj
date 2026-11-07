

(defn b11-document-validate!
  [source-path artifact]
  (gravity.backend.query/validate-native-manifest!
   {:fail! #'b11-document-fail! :sql-statement #'b11-document-sql-statement
    :result-adapter-fixture #'b11-document-result-adapter-fixture
    :sql-structurally-valid? #'b11-document-sql-structurally-valid?
    :result-adapter-structurally-valid? #'b11-document-result-adapter-structurally-valid?
    :diagnostic-ids #'b11-document-diagnostic-ids}
   source-path artifact))

(defn b11-document-capability-proof
  [artifact]
  (gravity.backend.query/native-coverage-report
   {:sql-statement #'b11-document-sql-statement :result-adapter-fixture #'b11-document-result-adapter-fixture
    :sql-structurally-valid? #'b11-document-sql-structurally-valid?
    :result-adapter-structurally-valid? #'b11-document-result-adapter-structurally-valid?
    :diagnostic-ids #'b11-document-diagnostic-ids}
   artifact))
