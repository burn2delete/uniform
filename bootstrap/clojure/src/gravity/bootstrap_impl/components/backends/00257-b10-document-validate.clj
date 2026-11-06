

(defn b10-document-validate!
  [source-path artifact]
  (gravity.backend.workflow/validate-native-manifest!
   {:fail! #'b10-document-fail! :workflow-graph #'b10-document-workflow-graph
    :replay-fixture #'b10-document-replay-fixture
    :workflow-graph-structurally-valid? #'b10-document-workflow-graph-structurally-valid?
    :replay-fixture-structurally-valid? #'b10-document-replay-fixture-structurally-valid?
    :diagnostic-ids #'b10-document-diagnostic-ids}
   source-path artifact))

(defn b10-document-capability-proof
  [artifact]
  (gravity.backend.workflow/native-coverage-report
   {:workflow-graph #'b10-document-workflow-graph :replay-fixture #'b10-document-replay-fixture
    :workflow-graph-structurally-valid? #'b10-document-workflow-graph-structurally-valid?
    :replay-fixture-structurally-valid? #'b10-document-replay-fixture-structurally-valid?
    :diagnostic-ids #'b10-document-diagnostic-ids}
   artifact))
