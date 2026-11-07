

(defn b10-document-workflow-manifest
  [source-path input-id]
  (gravity.backend.workflow/native-manifest
   {:artifact-id #'c4-artifact-id :workflow-graph #'b10-document-workflow-graph
    :replay-fixture #'b10-document-replay-fixture}
   source-path input-id))
