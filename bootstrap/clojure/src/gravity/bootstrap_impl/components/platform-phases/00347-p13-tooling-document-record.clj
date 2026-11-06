

(defn p13-tooling-document-record
  [document]
  (gravity.tooling/native-document-record {:task-id (fn [] p13-task-id) :diagnostics-by-document (fn [] p13-tooling-diagnostics-by-document) :summaries (fn [] p13-tooling-document-summaries) :governing-documents (fn [] p13-tooling-phase-governing-documents) :fixture-names (fn [] p13-tooling-rejected-fixture-names)} document))

(defn p13-tooling-document-records
  []
  (gravity.tooling/native-document-records {:document-record (fn [] p13-tooling-document-record) :documents (fn [] p13-tooling-documents)} ))

(defn p13-accepted-tooling-fixtures
  []
  (gravity.tooling/native-accepted-fixtures {:task-id (fn [] p13-task-id) :summaries (fn [] p13-tooling-document-summaries) :documents (fn [] p13-tooling-documents) :governing-documents (fn [] p13-tooling-phase-governing-documents)} ))

(defn p13-rejected-tooling-fixtures
  []
  (gravity.tooling/native-rejected-fixtures {:task-id (fn [] p13-task-id) :documents (fn [] p13-tooling-documents) :governing-documents (fn [] p13-tooling-phase-governing-documents) :rejected-diagnostics (fn [] p13-tooling-rejected-diagnostics) :fixture-names (fn [] p13-tooling-rejected-fixture-names)} ))

(defn p13-tooling-conformance-evidence
  []
  (gravity.tooling/native-conformance-evidence {:summaries (fn [] p13-tooling-document-summaries) :documents (fn [] p13-tooling-documents) :rejected-diagnostics (fn [] p13-tooling-rejected-diagnostics)} ))

(defn p13-tooling-diagnostic-stream
  [source-path input-id]
  (gravity.tooling/native-diagnostic-stream {:task-id (fn [] p13-task-id) :diagnostic-document (fn [] p13-tooling-diagnostic-document) :diagnostic-ids (fn [] p13-tooling-diagnostic-ids) :source-span (fn [] source-span)} source-path input-id))

(defn p13-present?
  [value]
  (gravity.tooling/native-present? value))