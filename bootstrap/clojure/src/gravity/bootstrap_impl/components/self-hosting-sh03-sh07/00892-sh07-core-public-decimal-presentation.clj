

(defn
 sh07-core-public-decimal-presentation
 "Return an explicit host presentation view. Do not use this view as an\n  authenticated canonical core: its decimal values differ from the preimage."
 [core]
 (gravity.checked-core/public-decimal-presentation
  core
  (checked-core-stage-operations)))

(defn
 sh07-core-canonical-artifact
 [resolved-template]
 (gravity.checked-core/canonical-artifact
  resolved-template
  (checked-core-stage-operations)))

(defn
 sh07-core-raise-diagnostic!
 [source-path template]
 (gravity.checked-core/raise-diagnostic!
  source-path
  template
  (checked-core-stage-operations)))

(defn
 sh07-core-request-diagnostic!
 [request rule-specific]
 (gravity.checked-core/request-diagnostic!
  request
  rule-specific
  (checked-core-stage-operations)))
