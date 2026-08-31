; Semantic decomposition of HEAD reader line 10816.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-record-safety-conformance-call!-safe16!
 [kind checker record args]
 (case
  kind
  :safe16-fixture-manifest
  (record-checker!
   checker
   :safe16-fixture-manifests
   (merge
    record
    {:fixture-id (dispatch-arg-value args 0),
     :document-id (dispatch-arg-value args 1),
     :profile (dispatch-arg-value args 2),
     :target (dispatch-arg-value args 3),
     :source (dispatch-arg-value args 4),
     :expected (dispatch-arg-value args 5)}))
  :safe16-expected-outcome
  (record-checker!
   checker
   :safe16-expected-outcome-manifests
   (merge
    record
    {:fixture-id (dispatch-arg-value args 0),
     :document-id (dispatch-arg-value args 1),
     :expected-verdict (dispatch-arg-value args 2),
     :operation-outcome (dispatch-arg-value args 3),
     :diagnostic-id (dispatch-arg-value args 4),
     :artifact-inspections (or (dispatch-arg-value args 5) #{})}))
  :safe16-diagnostic-match
  (record-checker!
   checker
   :safe16-diagnostic-match-records
   (merge
    record
    {:fixture-id (dispatch-arg-value args 0),
     :expected-diagnostic (dispatch-arg-value args 1),
     :actual-diagnostic (dispatch-arg-value args 2),
     :span-matched? (boolean (dispatch-arg-value args 3)),
     :status :matched}))
  :safe16-runtime-check-inspection
  (record-checker!
   checker
   :safe16-runtime-check-inspections
   (merge
    record
    {:fixture-id (dispatch-arg-value args 0),
     :check-record (dispatch-arg-value args 1),
     :expected-outcome (dispatch-arg-value args 2),
     :status :inspected}))
  :safe16-unsafe-audit-inspection
  (record-checker!
   checker
   :safe16-unsafe-audit-inspections
   (merge
    record
    {:fixture-id (dispatch-arg-value args 0),
     :unsafe-island (dispatch-arg-value args 1),
     :audit-record (dispatch-arg-value args 2),
     :status :inspected}))
  :safe16-certificate-inspection
  (record-checker!
   checker
   :safe16-certificate-inspections
   (merge
    record
    {:fixture-id (dispatch-arg-value args 0),
     :certificate-id (dispatch-arg-value args 1),
     :proof-id (dispatch-arg-value args 2),
     :status :inspected}))
  :safe16-profile-matrix
  (record-checker!
   checker
   :safe16-profile-matrix-reports
   (merge
    record
    {:fixture-id (dispatch-arg-value args 0),
     :profiles (or (dispatch-arg-value args 1) #{}),
     :expected-results (dispatch-arg-value args 2),
     :status :passed}))
  :safe16-backend-preservation
  (record-checker!
   checker
   :safe16-backend-preservation-reports
   (merge
    record
    {:fixture-id (dispatch-arg-value args 0),
     :backend (dispatch-arg-value args 1),
     :preserved-facts (or (dispatch-arg-value args 2) #{}),
     :retained-checks (or (dispatch-arg-value args 3) #{}),
     :status :passed}))
  :safe16-conformance-report
  (record-checker!
   checker
   :safe16-conformance-reports
   (merge
    record
    {:compiler-id (dispatch-arg-value args 0),
     :compiler-version (dispatch-arg-value args 1),
     :fixture-count (or (dispatch-arg-value args 2) 0),
     :diagnostics-matched (or (dispatch-arg-value args 3) 0),
     :artifacts-inspected (or (dispatch-arg-value args 4) 0),
     :status (or (dispatch-arg-value args 5) :passed)}))
  :safe16-conformance
  (record-checker!
   checker
   :safe16-conformance-records
   (merge
    record
    {:document :SAFE16,
     :status (or (dispatch-arg-value args 0) :complete),
     :positive-fixtures :passed,
     :negative-fixtures :passed}))
  semantic-early-record-safety-conformance-call!-unhandled))
