

(defn
 sh07-core-children
 [value]
 (gravity.checked-core/children value (checked-core-stage-operations)))

(defn
 sh07-core-decimal-descriptor
 [source-path value evidence projection-form-id]
 (gravity.checked-core/decimal-descriptor
  source-path
  value
  evidence
  projection-form-id
  (checked-core-stage-operations)))

(defn
 sh07-core-decimal-evidence
 [resolution-artifact]
 (gravity.checked-core/decimal-evidence
  resolution-artifact
  (checked-core-stage-operations)))

(defn
 sh07-core-projected-form-id
 [source-revision-id root-syntax-id path kind]
 (gravity.checked-core/projected-form-id
  source-revision-id
  root-syntax-id
  path
  kind
  (checked-core-stage-operations)))

(defn
 sh07-core-projected-syntax-id
 [source-revision-id root-syntax-id path kind trace]
 (gravity.checked-core/projected-syntax-id
  source-revision-id
  root-syntax-id
  path
  kind
  trace
  (checked-core-stage-operations)))

(defn-
 sh07-core-projected-source-mismatch
 [source target]
 (#'gravity.checked-core/projected-source-mismatch
  source
  target
  (checked-core-stage-operations)))

(defn
 sh07-core-neutral-value
 ([source-path
   source-revision-id
   root-syntax-id
   path
   decimal-evidence
   value]
  (gravity.checked-core/neutral-value
   source-path
   source-revision-id
   root-syntax-id
   path
   decimal-evidence
   value
   (checked-core-stage-operations)))
 ([source-path
   source-revision-id
   root-syntax-id
   path
   decimal-evidence
   value
   source-occurrences]
  (gravity.checked-core/neutral-value
   source-path
   source-revision-id
   root-syntax-id
   path
   decimal-evidence
   value
   source-occurrences
   (checked-core-stage-operations))))

(defn
 sh07-core-macro-trace
 [lineage sh05-step]
 (gravity.checked-core/macro-trace
  lineage
  sh05-step
  (checked-core-stage-operations)))

(defn
 sh07-core-root-syntax-id
 [source-revision-id ordinal form]
 (gravity.checked-core/root-syntax-id
  source-revision-id
  ordinal
  form
  (checked-core-stage-operations)))

(defn
 sh07-core-decimal-evidence-complete!
 [source-path decimal-evidence]
 (gravity.checked-core/decimal-evidence-complete!
  source-path
  decimal-evidence
  (checked-core-stage-operations)))

(defn
 sh07-core-semantic-macro-trace
 [trace]
 (gravity.checked-core/semantic-macro-trace
  trace
  (checked-core-stage-operations)))
