; Semantic decomposition of HEAD reader line 10125.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-record-boundary-safety-call!-safe7!
 [kind checker record args]
 (case
  kind
  :safe7-foreign-declaration
  (record-checker!
   checker
   :safe7-foreign-declaration-records
   (merge
    record
    {:foreign-source (dispatch-arg-value args 3),
     :boundary-id (dispatch-arg-value args 0),
     :required-metadata-complete? true,
     :type-signature (dispatch-arg-value args 4),
     :declared-capabilities (or (dispatch-arg-value args 6) #{}),
     :language (dispatch-arg-value args 1),
     :safety (dispatch-arg-value args 8),
     :ownership (dispatch-arg-value args 7),
     :declared-effects (or (dispatch-arg-value args 5) #{}),
     :threading (dispatch-arg-value args 10),
     :abi-or-protocol (dispatch-arg-value args 2),
     :profiles (or (dispatch-arg-value args 9) #{})}))
  :safe7-abi-record
  (record-checker!
   checker
   :safe7-abi-protocol-records
   (merge
    record
    {:boundary-id (dispatch-arg-value args 0),
     :abi (dispatch-arg-value args 1),
     :version (dispatch-arg-value args 2),
     :alignment (dispatch-arg-value args 3),
     :endianness (dispatch-arg-value args 4),
     :host-protocol-versioned? true}))
  :safe7-type-mapping
  (record-checker!
   checker
   :safe7-type-mapping-records
   (merge
    record
    {:alignment (dispatch-arg-value args 4),
     :foreign-type (dispatch-arg-value args 1),
     :nullability (dispatch-arg-value args 5),
     :allocates? (boolean (dispatch-arg-value args 8)),
     :size (dispatch-arg-value args 3),
     :conversion-failure (dispatch-arg-value args 7),
     :representation (dispatch-arg-value args 2),
     :ownership (dispatch-arg-value args 6),
     :gravity-type (dispatch-arg-value args 0)}))
  :safe7-ownership-lifetime
  (record-checker!
   checker
   :safe7-ownership-lifetime-maps
   (merge
    record
    {:foreign-value (dispatch-arg-value args 0),
     :ownership (dispatch-arg-value args 1),
     :lifetime (dispatch-arg-value args 2),
     :allocator (dispatch-arg-value args 3),
     :release (dispatch-arg-value args 4),
     :status (or (dispatch-arg-value args 5) :valid)}))
  :safe7-safe-wrapper
  (record-checker!
   checker
   :safe7-safe-wrapper-audits
   (merge
    record
    {:wrapper (dispatch-arg-value args 0),
     :foreign (dispatch-arg-value args 1),
     :preconditions (or (dispatch-arg-value args 2) #{}),
     :runtime-checks (or (dispatch-arg-value args 3) #{}),
     :failure-behavior (dispatch-arg-value args 4),
     :raw-call-visible? true,
     :audit-status :passed}))
  :safe7-error-translation
  (record-checker!
   checker
   :safe7-error-translation-maps
   (merge
    record
    {:foreign (dispatch-arg-value args 0),
     :foreign-failure (dispatch-arg-value args 1),
     :gravity-error (dispatch-arg-value args 2),
     :failure-behavior (dispatch-arg-value args 3),
     :untranslated? false}))
  :safe7-callback-safety
  (record-checker!
   checker
   :safe7-callback-safety-records
   (merge
    record
    {:callback-id (dispatch-arg-value args 0),
     :captures (or (dispatch-arg-value args 1) #{}),
     :lifetime (dispatch-arg-value args 2),
     :thread-affinity (dispatch-arg-value args 3),
     :reentrancy (dispatch-arg-value args 4),
     :release (dispatch-arg-value args 5),
     :bounded-captures? true}))
  :safe7-host-bridge
  (record-checker!
   checker
   :safe7-host-bridge-records
   (merge
    record
    {:bridge-id (dispatch-arg-value args 0),
     :host (dispatch-arg-value args 1),
     :runtime-version (dispatch-arg-value args 2),
     :profiles (or (dispatch-arg-value args 3) #{}),
     :exception-translation (dispatch-arg-value args 4),
     :object-rooting (dispatch-arg-value args 5),
     :threading (dispatch-arg-value args 6)}))
  :safe7-generated-binding
  (record-checker!
   checker
   :safe7-generated-binding-provenance
   (merge
    record
    {:generator (dispatch-arg-value args 0),
     :source-digest (dispatch-arg-value args 1),
     :unsafe-imports (or (dispatch-arg-value args 2) #{}),
     :generated-wrappers (or (dispatch-arg-value args 3) #{}),
     :conformance-tests (or (dispatch-arg-value args 4) #{}),
     :audit-metadata-preserved? true}))
  :safe7-conformance
  (record-checker!
   checker
   :safe7-conformance-records
   (merge
    record
    {:document :SAFE7,
     :status (or (dispatch-arg-value args 0) :complete),
     :positive-fixtures :passed,
     :negative-fixtures :passed}))
  semantic-early-record-boundary-safety-call!-unhandled))
