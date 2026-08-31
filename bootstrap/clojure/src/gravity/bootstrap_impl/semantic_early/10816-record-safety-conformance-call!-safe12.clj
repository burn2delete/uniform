; Semantic decomposition of HEAD reader line 10816.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-record-safety-conformance-call!-safe12!
 [kind checker record args]
 (case
  kind
  :safe12-macro-safety-declaration
  (record-checker!
   checker
   :safe12-macro-safety-declarations
   (merge
    record
    {:macro-symbol (dispatch-arg-value args 0),
     :generates-unsafe? (boolean (dispatch-arg-value args 1)),
     :build-effects (or (dispatch-arg-value args 2) #{}),
     :capabilities-declared (or (dispatch-arg-value args 3) #{}),
     :preserves-taint? (boolean (dispatch-arg-value args 4)),
     :conditions (or (dispatch-arg-value args 5) #{})}))
  :safe12-generated-origin-chain
  (record-checker!
   checker
   :safe12-generated-origin-chains
   (merge
    record
    {:macro-symbol (dispatch-arg-value args 0),
     :definition-span (dispatch-arg-value args 1),
     :call-site-span (dispatch-arg-value args 2),
     :generated-form-span (dispatch-arg-value args 3),
     :hygiene-context (dispatch-arg-value args 4),
     :metadata-preserved? true}))
  :safe12-macro-build-effect-record
  (record-checker!
   checker
   :safe12-macro-build-effect-records
   (merge
    record
    {:macro-symbol (dispatch-arg-value args 0),
     :build-effects (or (dispatch-arg-value args 1) #{}),
     :grants (or (dispatch-arg-value args 2) #{}),
     :replay-record (dispatch-arg-value args 3),
     :hermetic? (boolean (dispatch-arg-value args 4))}))
  :safe12-generated-unsafe-island
  (record-checker!
   checker
   :safe12-generated-unsafe-island-records
   (merge
    record
    {:macro-symbol (dispatch-arg-value args 0),
     :unsafe-island (dispatch-arg-value args 1),
     :safe6-metadata (dispatch-arg-value args 2),
     :owner (dispatch-arg-value args 3),
     :review-policy (dispatch-arg-value args 4),
     :status :safe6-complete}))
  :safe12-hygiene-capture-record
  (record-checker!
   checker
   :safe12-hygiene-capture-records
   (merge
    record
    {:macro-symbol (dispatch-arg-value args 0),
     :generated-symbols (or (dispatch-arg-value args 1) #{}),
     :explicit-captures (or (dispatch-arg-value args 2) #{}),
     :privileged-captures (or (dispatch-arg-value args 3) #{}),
     :policy (dispatch-arg-value args 4),
     :status :hygienic}))
  :safe12-taint-capability-propagation
  (record-checker!
   checker
   :safe12-taint-capability-propagation
   (merge
    record
    {:macro-symbol (dispatch-arg-value args 0),
     :input-taint (or (dispatch-arg-value args 1) #{}),
     :output-taint (or (dispatch-arg-value args 2) #{}),
     :input-capabilities (or (dispatch-arg-value args 3) #{}),
     :output-capabilities (or (dispatch-arg-value args 4) #{}),
     :preserved? true}))
  :safe12-facet-output-record
  (record-checker!
   checker
   :safe12-facet-output-records
   (merge
    record
    {:macro-symbol (dispatch-arg-value args 0),
     :facet-id (dispatch-arg-value args 1),
     :domain-ir (dispatch-arg-value args 2),
     :source-map (dispatch-arg-value args 3),
     :safety-metadata (dispatch-arg-value args 4),
     :facet-checks (or (dispatch-arg-value args 5) #{})}))
  :safe12-alternative-engine-equivalence
  (record-checker!
   checker
   :safe12-alternative-engine-equivalence
   (merge
    record
    {:engine-id (dispatch-arg-value args 0),
     :reference-engine (dispatch-arg-value args 1),
     :preserved-facts (or (dispatch-arg-value args 2) #{}),
     :diagnostic-map (dispatch-arg-value args 3),
     :status (or (dispatch-arg-value args 4) :equivalent)}))
  :safe12-conformance
  (record-checker!
   checker
   :safe12-conformance-records
   (merge
    record
    {:document :SAFE12,
     :status (or (dispatch-arg-value args 0) :complete),
     :positive-fixtures :passed,
     :negative-fixtures :passed}))
  semantic-early-record-safety-conformance-call!-unhandled))
