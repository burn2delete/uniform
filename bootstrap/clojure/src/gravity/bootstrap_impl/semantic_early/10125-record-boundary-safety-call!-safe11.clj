; Semantic decomposition of HEAD reader line 10125.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-record-boundary-safety-call!-safe11!
 [kind checker record args]
 (case
  kind
  :safe11-taint-source
  (record-checker!
   checker
   :safe11-taint-source-records
   (merge
    record
    {:source-id (dispatch-arg-value args 0),
     :category (dispatch-arg-value args 1),
     :origin (dispatch-arg-value args 2),
     :trust-boundary (dispatch-arg-value args 3),
     :capability (dispatch-arg-value args 4)}))
  :safe11-taint-flow
  (record-checker!
   checker
   :safe11-taint-flow-records
   (merge
    record
    {:value-id (dispatch-arg-value args 0),
     :categories (or (dispatch-arg-value args 1) #{}),
     :from (dispatch-arg-value args 2),
     :to (dispatch-arg-value args 3),
     :validators (or (dispatch-arg-value args 4) #{})}))
  :safe11-validator-contract
  (record-checker!
   checker
   :safe11-validator-contracts
   (merge
    record
    {:validator (dispatch-arg-value args 0),
     :accepts (or (dispatch-arg-value args 1) #{}),
     :clears (or (dispatch-arg-value args 2) #{}),
     :retains (or (dispatch-arg-value args 3) #{}),
     :residual (or (dispatch-arg-value args 4) #{}),
     :failure-type (dispatch-arg-value args 5)}))
  :safe11-residual-constraint
  (record-checker!
   checker
   :safe11-residual-constraint-records
   (merge
    record
    {:value-id (dispatch-arg-value args 0),
     :residual (or (dispatch-arg-value args 1) #{}),
     :sink (dispatch-arg-value args 2),
     :status (or (dispatch-arg-value args 3) :accepted)}))
  :safe11-sink-authorization
  (record-checker!
   checker
   :safe11-sink-authorization-records
   (merge
    record
    {:sink (dispatch-arg-value args 0),
     :accepted-state (dispatch-arg-value args 1),
     :required-handling (or (dispatch-arg-value args 2) #{}),
     :capability (dispatch-arg-value args 3),
     :status (or (dispatch-arg-value args 4) :authorized)}))
  :safe11-parameterization
  (record-checker!
   checker
   :safe11-parameterization-records
   (merge
    record
    {:api (dispatch-arg-value args 0),
     :syntax-source (dispatch-arg-value args 1),
     :parameters (or (dispatch-arg-value args 2) #{}),
     :status (or (dispatch-arg-value args 3) :parameterized)}))
  :safe11-deserialization
  (record-checker!
   checker
   :safe11-deserialization-records
   (merge
    record
    {:input (dispatch-arg-value args 0),
     :schema (dispatch-arg-value args 1),
     :validation (dispatch-arg-value args 2),
     :residual-taint (or (dispatch-arg-value args 3) #{})}))
  :safe11-secret-redaction
  (record-checker!
   checker
   :safe11-secret-redaction-records
   (merge
    record
    {:secret-id (dispatch-arg-value args 0),
     :sink (dispatch-arg-value args 1),
     :redaction (dispatch-arg-value args 2),
     :artifact-policy (dispatch-arg-value args 3),
     :secret-value-emitted? false}))
  :safe11-prompt-tool-policy
  (record-checker!
   checker
   :safe11-prompt-tool-policy-records
   (merge
    record
    {:prompt-id (dispatch-arg-value args 0),
     :tool (dispatch-arg-value args 1),
     :categories (or (dispatch-arg-value args 2) #{}),
     :policy (dispatch-arg-value args 3),
     :capability (dispatch-arg-value args 4),
     :policy-mediated? true}))
  :safe11-generated-taint
  (record-checker!
   checker
   :safe11-generated-taint-propagation
   (merge
    record
    {:generator (dispatch-arg-value args 0),
     :source-categories (or (dispatch-arg-value args 1) #{}),
     :source-form (dispatch-arg-value args 2),
     :generated-form (dispatch-arg-value args 3),
     :status (or (dispatch-arg-value args 4) :preserved)}))
  :safe11-unsafe-clear-audit
  (record-checker!
   checker
   :safe11-unsafe-clear-audits
   (merge
    record
    {:value-id (dispatch-arg-value args 0),
     :categories (or (dispatch-arg-value args 1) #{}),
     :unsafe-island (dispatch-arg-value args 2),
     :owner (dispatch-arg-value args 3),
     :review (dispatch-arg-value args 4),
     :audit-status :passed}))
  :safe11-conformance
  (record-checker!
   checker
   :safe11-conformance-records
   (merge
    record
    {:document :SAFE11,
     :status (or (dispatch-arg-value args 0) :complete),
     :positive-fixtures :passed,
     :negative-fixtures :passed}))
  semantic-early-record-boundary-safety-call!-unhandled))
