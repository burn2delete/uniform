; Semantic decomposition of HEAD reader line 10816.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-record-safety-conformance-call!-safe15!
 [kind checker record args]
 (case
  kind
  :safe15-proof-record
  (record-checker!
   checker
   :safe15-proof-records
   (merge
    record
    {:proof-id (dispatch-arg-value args 0),
     :claim (dispatch-arg-value args 1),
     :artifact-node (dispatch-arg-value args 2),
     :assumptions (or (dispatch-arg-value args 3) #{}),
     :method (dispatch-arg-value args 4),
     :provider-id (dispatch-arg-value args 5),
     :invalidated-by (or (dispatch-arg-value args 6) #{}),
     :result (or (dispatch-arg-value args 7) :proven-safe)}))
  :safe15-certificate
  (record-checker!
   checker
   :safe15-certificates
   (merge
    record
    {:certificate-id (dispatch-arg-value args 0),
     :package-id (dispatch-arg-value args 1),
     :compiler-id (dispatch-arg-value args 2),
     :provider-id (dispatch-arg-value args 3),
     :claim (dispatch-arg-value args 4),
     :trust-root (dispatch-arg-value args 5),
     :verification-status (dispatch-arg-value args 6)}))
  :safe15-check-erasure
  (record-checker!
   checker
   :safe15-check-erasure-records
   (merge
    record
    {:check-id (dispatch-arg-value args 0),
     :proof-id (dispatch-arg-value args 1),
     :certificate-id (dispatch-arg-value args 2),
     :condition (dispatch-arg-value args 3),
     :backend-preservation (dispatch-arg-value args 4),
     :status :erasure-allowed}))
  :safe15-trust-record
  (record-checker!
   checker
   :safe15-trust-records
   (merge
    record
    {:certificate-id (dispatch-arg-value args 0),
     :trust-level (dispatch-arg-value args 1),
     :signature (dispatch-arg-value args 2),
     :policy (dispatch-arg-value args 3),
     :accepted? (boolean (dispatch-arg-value args 4))}))
  :safe15-invalidation-record
  (record-checker!
   checker
   :safe15-invalidation-records
   (merge
    record
    {:certificate-id (dispatch-arg-value args 0),
     :invalidating-change (dispatch-arg-value args 1),
     :assumption (dispatch-arg-value args 2),
     :action (dispatch-arg-value args 3),
     :status (dispatch-arg-value args 4)}))
  :safe15-imported-certificate
  (record-checker!
   checker
   :safe15-imported-certificate-verifications
   (merge
    record
    {:certificate-id (dispatch-arg-value args 0),
     :package-digest (dispatch-arg-value args 1),
     :compiler-range (dispatch-arg-value args 2),
     :profile-target-match (dispatch-arg-value args 3),
     :signature-status (dispatch-arg-value args 4),
     :accepted? (boolean (dispatch-arg-value args 5))}))
  :safe15-proof-provider
  (record-checker!
   checker
   :safe15-proof-provider-records
   (merge
    record
    {:provider-id (dispatch-arg-value args 0),
     :version (dispatch-arg-value args 1),
     :claim-families (or (dispatch-arg-value args 2) #{}),
     :trust-level (dispatch-arg-value args 3),
     :policy-status (dispatch-arg-value args 4)}))
  :safe15-unsafe-wrapper-audit
  (record-checker!
   checker
   :safe15-unsafe-wrapper-audit-views
   (merge
    record
    {:wrapper-id (dispatch-arg-value args 0),
     :unsafe-island (dispatch-arg-value args 1),
     :certificate-id (dispatch-arg-value args 2),
     :invariant (dispatch-arg-value args 3),
     :status :invariant-proven}))
  :safe15-backend-preservation
  (record-checker!
   checker
   :safe15-backend-preservation-records
   (merge
    record
    {:proof-id (dispatch-arg-value args 0),
     :backend (dispatch-arg-value args 1),
     :preserved-assumptions (or (dispatch-arg-value args 2) #{}),
     :invalidated-assumptions (or (dispatch-arg-value args 3) #{}),
     :status (or (dispatch-arg-value args 4) :preserved)}))
  :safe15-conformance
  (record-checker!
   checker
   :safe15-conformance-records
   (merge
    record
    {:document :SAFE15,
     :status (or (dispatch-arg-value args 0) :complete),
     :positive-fixtures :passed,
     :negative-fixtures :passed}))
  semantic-early-record-safety-conformance-call!-unhandled))
