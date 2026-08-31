; Semantic decomposition of HEAD reader line 10125.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-record-boundary-safety-call!-safe8!
 [kind checker record args]
 (case
  kind
  :safe8-concurrency-graph
  (record-checker!
   checker
   :safe8-concurrency-graphs
   (merge
    record
    {:graph-id (dispatch-arg-value args 0),
     :tasks (or (dispatch-arg-value args 1) #{}),
     :shared-locations (or (dispatch-arg-value args 2) #{}),
     :ordering (or (dispatch-arg-value args 3) #{}),
     :race-status (or (dispatch-arg-value args 4) :race-free)}))
  :safe8-task-capture
  (record-checker!
   checker
   :safe8-task-capture-records
   (merge
    record
    {:task-id (dispatch-arg-value args 0),
     :captures (or (dispatch-arg-value args 1) #{}),
     :lifetime (dispatch-arg-value args 2),
     :task-kind (dispatch-arg-value args 3),
     :status (or (dispatch-arg-value args 4) :valid)}))
  :safe8-ownership-transfer
  (record-checker!
   checker
   :safe8-ownership-transfer-records
   (merge
    record
    {:value-id (dispatch-arg-value args 0),
     :from-task (dispatch-arg-value args 1),
     :to-task (dispatch-arg-value args 2),
     :mode (or (dispatch-arg-value args 3) :moved),
     :source-consumed? true}))
  :safe8-shared-state-access
  (record-checker!
   checker
   :safe8-shared-state-access-records
   (merge
    record
    {:location (dispatch-arg-value args 0),
     :access (dispatch-arg-value args 1),
     :synchronization (dispatch-arg-value args 2),
     :representation (dispatch-arg-value args 3),
     :status (or (dispatch-arg-value args 4) :ordered)}))
  :safe8-synchronization-proof
  (record-checker!
   checker
   :safe8-synchronization-proof-records
   (merge
    record
    {:primitive (dispatch-arg-value args 0),
     :proof-id (dispatch-arg-value args 1),
     :effects-covered (or (dispatch-arg-value args 2) #{}),
     :blocking (dispatch-arg-value args 3),
     :cancellation (dispatch-arg-value args 4),
     :poisoning (dispatch-arg-value args 5)}))
  :safe8-atomic-order
  (record-checker!
   checker
   :safe8-atomic-memory-order-records
   (merge
    record
    {:operation (dispatch-arg-value args 0),
     :memory-order (dispatch-arg-value args 1),
     :target (dispatch-arg-value args 2),
     :target-support (dispatch-arg-value args 3),
     :fence (dispatch-arg-value args 4)}))
  :safe8-blocking-cancellation
  (record-checker!
   checker
   :safe8-blocking-cancellation-records
   (merge
    record
    {:api (dispatch-arg-value args 0),
     :blocking (dispatch-arg-value args 1),
     :cancellation (dispatch-arg-value args 2),
     :failure-behavior (dispatch-arg-value args 3)}))
  :safe8-backend-preservation
  (record-checker!
   checker
   :safe8-backend-preservation-records
   (merge
    record
    {:primitive (dispatch-arg-value args 0),
     :target (dispatch-arg-value args 1),
     :preserved-facts (or (dispatch-arg-value args 2) #{}),
     :status (or (dispatch-arg-value args 3) :preserved)}))
  :safe8-race-analysis
  (record-checker!
   checker
   :safe8-race-analysis-reports
   (merge
    record
    {:location (dispatch-arg-value args 0),
     :conflicting-accesses (or (dispatch-arg-value args 1) #{}),
     :synchronization (dispatch-arg-value args 2),
     :status (or (dispatch-arg-value args 3) :race-free),
     :evidence (dispatch-arg-value args 4)}))
  :safe8-conformance
  (record-checker!
   checker
   :safe8-conformance-records
   (merge
    record
    {:document :SAFE8,
     :status (or (dispatch-arg-value args 0) :complete),
     :positive-fixtures :passed,
     :negative-fixtures :passed}))
  semantic-early-record-boundary-safety-call!-unhandled))
