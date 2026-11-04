

(def p13-tooling-documents
  gravity.tooling/native-documents)

(def p13-tooling-phase-governing-documents
  gravity.tooling/native-governing-documents)

(def p13-tooling-diagnostics-by-document
  gravity.tooling/native-diagnostics-by-document)

(def p13-tooling-rejected-diagnostics
  gravity.tooling/native-rejected-diagnostics)

(def p13-tooling-rejected-fixture-names
  gravity.tooling/native-rejected-fixture-names)

(def p13-tooling-diagnostic-ids
  (gravity.tooling/native-diagnostic-ids p13-tooling-diagnostics-by-document p13-tooling-documents))

(def p13-tooling-override-diagnostics
  (gravity.tooling/native-override-diagnostics p13-tooling-diagnostic-ids))

(def p13-tooling-artifact-keys
  gravity.tooling/native-artifact-keys)

(def p13-tooling-document-summaries
  gravity.tooling/native-document-summaries)

(defn p13-document-number
  [document]
  (gravity.tooling/native-document-number document))

(defn p13-task-id
  [document]
  (gravity.tooling/native-task-id {:document-number (fn [] p13-document-number)} document))

(defn p13-tooling-source-overrides
  [module]
  (gravity.tooling/native-source-overrides module))

(defn p13-tooling-diagnostic-document
  [diagnostic-id]
  (gravity.tooling/native-diagnostic-document {:diagnostics-by-document (fn [] p13-tooling-diagnostics-by-document) :documents (fn [] p13-tooling-documents)} diagnostic-id))

(defn p13-tooling-fail!
  [id source-path subject extra]
  (gravity.tooling/native-fail! {:transport-fail (fn [] fail!) :task-id (fn [] p13-task-id) :diagnostic-document (fn [] p13-tooling-diagnostic-document) :source-span (fn [] source-span)} id source-path subject extra))

(defn p13-tooling-validate-source-overrides!
  [source-path overrides]
  (gravity.tooling/native-validate-source-overrides! {:diagnostic-document (fn [] p13-tooling-diagnostic-document) :fail (fn [] p13-tooling-fail!) :override-diagnostics (fn [] p13-tooling-override-diagnostics)} source-path overrides))