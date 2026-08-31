; Semantic decomposition of HEAD reader line 10125.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-record-boundary-safety-call!-safe9!
 [kind checker record args]
 (case
  kind
  :safe9-numeric-mode
  (record-checker!
   checker
   :safe9-numeric-mode-records
   (merge
    record
    {:operation (dispatch-arg-value args 0),
     :numeric-type (dispatch-arg-value args 1),
     :mode (dispatch-arg-value args 2),
     :overflow (dispatch-arg-value args 3),
     :failure-behavior (dispatch-arg-value args 4)}))
  :safe9-runtime-check
  (record-checker!
   checker
   :safe9-runtime-check-records
   (merge
    record
    {:check-kind (dispatch-arg-value args 0),
     :operation (dispatch-arg-value args 1),
     :condition (dispatch-arg-value args 2),
     :failure-behavior (dispatch-arg-value args 3),
     :status :runtime-checked}))
  :safe9-range-proof
  (record-checker!
   checker
   :safe9-range-proof-records
   (merge
    record
    {:operation (dispatch-arg-value args 0),
     :proof-id (dispatch-arg-value args 1),
     :source-facts (or (dispatch-arg-value args 2) #{}),
     :status (or (dispatch-arg-value args 3) :proven-safe)}))
  :safe9-floating-mode
  (record-checker!
   checker
   :safe9-floating-mode-records
   (merge
    record
    {:operation (dispatch-arg-value args 0),
     :format (dispatch-arg-value args 1),
     :rounding (dispatch-arg-value args 2),
     :nan-policy (dispatch-arg-value args 3),
     :determinism (dispatch-arg-value args 4),
     :relaxed? (boolean (dispatch-arg-value args 5))}))
  :safe9-elementary-approximation
  (record-checker!
   checker
   :safe9-elementary-approximation-records
   (merge
    record
    {:function (dispatch-arg-value args 0),
     :domain (dispatch-arg-value args 1),
     :accuracy (dispatch-arg-value args 2),
     :provider (dispatch-arg-value args 3),
     :evidence (or (dispatch-arg-value args 4) #{})}))
  :safe9-relaxed-approval
  (record-checker!
   checker
   :safe9-relaxed-approval-records
   (merge
    record
    {:operation (dispatch-arg-value args 0),
     :source-opt-in (dispatch-arg-value args 1),
     :approval (dispatch-arg-value args 2),
     :constraints (or (dispatch-arg-value args 3) #{})}))
  :safe9-optimization-proof
  (record-checker!
   checker
   :safe9-optimization-proof-records
   (merge
    record
    {:transform (dispatch-arg-value args 0),
     :proof-id (dispatch-arg-value args 1),
     :preserved-mode (dispatch-arg-value args 2),
     :status (or (dispatch-arg-value args 3) :preserved)}))
  :safe9-backend-lowering
  (record-checker!
   checker
   :safe9-backend-lowering-records
   (merge
    record
    {:operation (dispatch-arg-value args 0),
     :target (dispatch-arg-value args 1),
     :instruction (dispatch-arg-value args 2),
     :mode-preservation (dispatch-arg-value args 3)}))
  :safe9-conformance
  (record-checker!
   checker
   :safe9-conformance-records
   (merge
    record
    {:document :SAFE9,
     :status (or (dispatch-arg-value args 0) :complete),
     :positive-fixtures :passed,
     :negative-fixtures :passed}))
  semantic-early-record-boundary-safety-call!-unhandled))
