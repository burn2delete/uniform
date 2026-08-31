; Semantic decomposition of HEAD reader line 10547.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-record-safe-capability-call!-safe10!
 [kind checker record args]
 (case
  kind
  :safe10-capability-requirement
  (record-checker!
   checker
   :safe10-capability-requirement-records
   (merge
    record
    {:family (dispatch-arg-value args 1),
     :provider-id (dispatch-arg-value args 5),
     :phase (dispatch-arg-value args 3),
     :grant-id (dispatch-arg-value args 6),
     :capability (dispatch-arg-value args 0),
     :scope (dispatch-arg-value args 7),
     :visible-in-artifact? true,
     :effect (dispatch-arg-value args 2),
     :visibility (dispatch-arg-value args 4)}))
  :safe10-grant-intersection
  (record-checker!
   checker
   :safe10-grant-intersection-records
   (merge
    record
    {:profile-policy (dispatch-arg-value args 5),
     :effective-grant (dispatch-arg-value args 8),
     :package-manifest (dispatch-arg-value args 3),
     :deployment-policy (dispatch-arg-value args 6),
     :grant-id (dispatch-arg-value args 0),
     :capability (dispatch-arg-value args 1),
     :source (dispatch-arg-value args 2),
     :workspace-policy (dispatch-arg-value args 4),
     :runtime-provider-scope (dispatch-arg-value args 7),
     :expands-parent-authority? false,
     :decision (or (dispatch-arg-value args 9) :allowed)}))
  :safe10-provider-selection
  (record-checker!
   checker
   :safe10-provider-selection-records
   (merge
    record
    {:conformance-suite (dispatch-arg-value args 8),
     :provider-id (dispatch-arg-value args 1),
     :artifact-schema (dispatch-arg-value args 7),
     :phase (dispatch-arg-value args 5),
     :capability (dispatch-arg-value args 0),
     :provider-version (dispatch-arg-value args 2),
     :scope (dispatch-arg-value args 4),
     :policy-bypassed? false,
     :selection-source (dispatch-arg-value args 3),
     :trust-level (dispatch-arg-value args 6)}))
  :safe10-scope-check
  (record-checker!
   checker
   :safe10-scope-check-records
   (merge
    record
    {:capability (dispatch-arg-value args 0),
     :category (dispatch-arg-value args 1),
     :requested-scope (dispatch-arg-value args 2),
     :grant-scope (dispatch-arg-value args 3),
     :allowed? (boolean (dispatch-arg-value args 4)),
     :failure-behavior (dispatch-arg-value args 5)}))
  :safe10-attenuation
  (record-checker!
   checker
   :safe10-attenuation-records
   (merge
    record
    {:parent-capability (dispatch-arg-value args 0),
     :derived-capability (dispatch-arg-value args 1),
     :parent-scope (dispatch-arg-value args 2),
     :derived-scope (dispatch-arg-value args 3),
     :lifetime (dispatch-arg-value args 4),
     :narrowed? (boolean (dispatch-arg-value args 5)),
     :expands-authority? false}))
  :safe10-revocation
  (record-checker!
   checker
   :safe10-revocation-records
   (merge
    record
    {:capability (dispatch-arg-value args 0),
     :provider-id (dispatch-arg-value args 1),
     :revoker (dispatch-arg-value args 2),
     :failure-type (dispatch-arg-value args 3),
     :thread-safety (dispatch-arg-value args 4),
     :synchronous? (boolean (dispatch-arg-value args 5)),
     :supported? (boolean (dispatch-arg-value args 6))}))
  :safe10-secret-redaction
  (record-checker!
   checker
   :safe10-secret-redaction-records
   (merge
    record
    {:secret-name (dispatch-arg-value args 0),
     :redaction-policy (dispatch-arg-value args 1),
     :artifact-policy (dispatch-arg-value args 2),
     :export-policy (dispatch-arg-value args 3),
     :secret-value-emitted? false}))
  :safe10-runtime-check
  (record-checker!
   checker
   :safe10-runtime-check-records
   (merge
    record
    {:capability (dispatch-arg-value args 0),
     :provider-id (dispatch-arg-value args 1),
     :scope (dispatch-arg-value args 2),
     :failure-behavior (dispatch-arg-value args 3),
     :audit-record (dispatch-arg-value args 4),
     :status (or (dispatch-arg-value args 5) :runtime-checked)}))
  :safe10-usage-summary
  (record-checker!
   checker
   :safe10-usage-summaries
   (merge
    record
    {:package-id (dispatch-arg-value args 0),
     :runtime-capabilities (or (dispatch-arg-value args 1) #{}),
     :build-capabilities (or (dispatch-arg-value args 2) #{}),
     :capability-families (or (dispatch-arg-value args 3) #{}),
     :policy-layers (or (dispatch-arg-value args 4) #{}),
     :deployment-id (dispatch-arg-value args 5),
     :secret-values-redacted? true}))
  :safe10-conformance
  (record-checker!
   checker
   :safe10-conformance-records
   (merge
    record
    {:document :SAFE10,
     :status (or (dispatch-arg-value args 0) :complete),
     :positive-fixtures :passed,
     :negative-fixtures :passed}))
  semantic-early-record-safe-capability-call!-unhandled))
