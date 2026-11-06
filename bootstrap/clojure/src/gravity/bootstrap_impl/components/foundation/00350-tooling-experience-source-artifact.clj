

(defn tooling-experience-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (p13-tooling-source-overrides module)
        _ (p13-tooling-validate-source-overrides! source-path source-overrides)]
    (gravity.tooling/native-experience-artifact {:artifact-id (fn [] c4-artifact-id) :accepted-fixtures (fn [] p13-accepted-tooling-fixtures) :rejected-fixtures (fn [] p13-rejected-tooling-fixtures) :task-statuses (fn [] p13-task-statuses) :artifact-keys (fn [] p13-tooling-artifact-keys) :artifact-values (fn [] p13-tooling-artifact-values) :conformance-evidence (fn [] p13-tooling-conformance-evidence) :diagnostic-ids (fn [] p13-tooling-diagnostic-ids) :diagnostic-stream (fn [] p13-tooling-diagnostic-stream) :document-records (fn [] p13-tooling-document-records) :documents (fn [] p13-tooling-documents) :governing-documents (fn [] p13-tooling-phase-governing-documents) :coverage-record (fn [] p13-tooling-proof) :validate-artifact (fn [] p13-tooling-validate!)} source-path module source-overrides)))

(defn tooling-experience-file-artifact
  [path]
  (tooling-experience-source-artifact path (slurp path)))