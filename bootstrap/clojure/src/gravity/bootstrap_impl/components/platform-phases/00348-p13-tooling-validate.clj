

(defn p13-tooling-validate!
  [source-path artifact]
  (gravity.tooling/native-validate-artifact! {:present (fn [] p13-present?) :artifact-keys (fn [] p13-tooling-artifact-keys) :diagnostic-ids (fn [] p13-tooling-diagnostic-ids) :diagnostics-by-document (fn [] p13-tooling-diagnostics-by-document) :summaries (fn [] p13-tooling-document-summaries) :documents (fn [] p13-tooling-documents) :fail (fn [] p13-tooling-fail!)} source-path artifact))

(defn p13-task-statuses
  []
  (gravity.tooling/native-task-statuses {:task-id (fn [] p13-task-id) :documents (fn [] p13-tooling-documents)} ))