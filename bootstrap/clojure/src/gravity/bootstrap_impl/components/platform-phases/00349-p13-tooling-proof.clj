

(defn p13-tooling-proof
  [artifact]
  (gravity.tooling/coverage-record
   #'p13-tooling-documents #'p13-tooling-diagnostic-ids
   #'p13-present? #'p13-task-statuses artifact))