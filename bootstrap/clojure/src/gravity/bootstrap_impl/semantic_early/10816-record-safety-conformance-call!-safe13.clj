; Semantic decomposition of HEAD reader line 10816.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-record-safety-conformance-call!-safe13!
 [kind checker record args]
 (case
  kind
  :safe13-model-call-trace
  (record-checker!
   checker
   :safe13-model-call-traces
   (merge
    record
    {:model-id (dispatch-arg-value args 1),
     :cost-policy (dispatch-arg-value args 6),
     :model-version (dispatch-arg-value args 2),
     :provider-id (dispatch-arg-value args 0),
     :input-schema (dispatch-arg-value args 4),
     :output-schema (dispatch-arg-value args 5),
     :retention-policy (dispatch-arg-value args 7),
     :prompt-digest (dispatch-arg-value args 3),
     :replay-policy (dispatch-arg-value args 8)}))
  :safe13-tool-call-trace
  (record-checker!
   checker
   :safe13-tool-call-traces
   (merge
    record
    {:tool-id (dispatch-arg-value args 0),
     :input-schema (dispatch-arg-value args 1),
     :output-schema (dispatch-arg-value args 2),
     :side-effect-class (dispatch-arg-value args 3),
     :required-capabilities (or (dispatch-arg-value args 4) #{}),
     :human-review (dispatch-arg-value args 5),
     :replay-policy (dispatch-arg-value args 6)}))
  :safe13-prompt-provenance
  (record-checker!
   checker
   :safe13-prompt-provenance-records
   (merge
    record
    {:prompt-id (dispatch-arg-value args 0),
     :roles (or (dispatch-arg-value args 1) #{}),
     :sources (or (dispatch-arg-value args 2) #{}),
     :untrusted-roles (or (dispatch-arg-value args 3) #{}),
     :policy (dispatch-arg-value args 4),
     :role-preserved? true}))
  :safe13-tool-schema-validation
  (record-checker!
   checker
   :safe13-tool-schema-validation-records
   (merge
    record
    {:tool-id (dispatch-arg-value args 0),
     :schema (dispatch-arg-value args 1),
     :arguments-digest (dispatch-arg-value args 2),
     :validation (dispatch-arg-value args 3),
     :status :validated}))
  :safe13-human-review-record
  (record-checker!
   checker
   :safe13-human-review-records
   (merge
    record
    {:operation (dispatch-arg-value args 0),
     :capabilities-used (or (dispatch-arg-value args 1) #{}),
     :reviewer (dispatch-arg-value args 2),
     :policy-id (dispatch-arg-value args 3),
     :decision (dispatch-arg-value args 4),
     :timestamp (dispatch-arg-value args 5)}))
  :safe13-replay-record
  (record-checker!
   checker
   :safe13-replay-records
   (merge
    record
    {:interaction-id (dispatch-arg-value args 0),
     :replay-policy (dispatch-arg-value args 1),
     :input-digest (dispatch-arg-value args 2),
     :output-digest (dispatch-arg-value args 3),
     :provider-id (dispatch-arg-value args 4),
     :decision-record (dispatch-arg-value args 5)}))
  :safe13-model-output-taint
  (record-checker!
   checker
   :safe13-model-output-taint-records
   (merge
    record
    {:output-id (dispatch-arg-value args 0),
     :taint-categories (or (dispatch-arg-value args 1) #{}),
     :validators (or (dispatch-arg-value args 2) #{}),
     :authorized-sinks (or (dispatch-arg-value args 3) #{}),
     :status :tainted-until-validated}))
  :safe13-generated-code-safety
  (record-checker!
   checker
   :safe13-generated-code-safety-records
   (merge
    record
    {:artifact-id (dispatch-arg-value args 0),
     :model-call (dispatch-arg-value args 1),
     :prompt-provenance (dispatch-arg-value args 2),
     :safety-passes (or (dispatch-arg-value args 3) #{}),
     :review-policy (dispatch-arg-value args 4),
     :status :checked-before-execution}))
  :safe13-memory-retention-policy
  (record-checker!
   checker
   :safe13-memory-retention-policies
   (merge
    record
    {:memory-id (dispatch-arg-value args 0),
     :retention (dispatch-arg-value args 1),
     :redaction (dispatch-arg-value args 2),
     :privacy (dispatch-arg-value args 3),
     :deletion-policy (dispatch-arg-value args 4),
     :secret-exposed? false}))
  :safe13-conformance
  (record-checker!
   checker
   :safe13-conformance-records
   (merge
    record
    {:document :SAFE13,
     :status (or (dispatch-arg-value args 0) :complete),
     :positive-fixtures :passed,
     :negative-fixtures :passed}))
  semantic-early-record-safety-conformance-call!-unhandled))
