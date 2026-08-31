; Semantic decomposition of committed HEAD reader line 138453.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-p15-s23-closed-runtime-target-record-authentic?-execution
 [state]
 (clojure.core/let
  [{:keys
    [record
     context
     digest?
     context-envelope-valid?
     expected-record-hash
     expected-actual-path-binding-base
     expected-actual-path-binding
     runtime-source-file
     runtime-source-file-valid?
     runtime-source-file-hash
     contract-binding
     authority-binding
     adapter-record
     adapter-decisions
     adapter-actions
     validation
     io-write-active?
     capture-invoked?
     expected-adapter-record
     expected-decision-count
     expected-action-count
     expected-provider-ids
     expected-grant-ids
     required-decision-fields]}
   state
   required-action-fields
   #{:remediation
     :source-span
     :diagnostic
     :provider-id
     :output-committed?
     :redaction
     :action-status
     :artifact-id
     :operation
     :generated-origin-chain
     :capability
     :action-id
     :result-committed?
     :runtime-function
     :effect
     :target
     :record-id
     :action-started?
     :profile}
   invocation
   (:invocation record)
   verification-replay
   (:verification-replay record)
   execution
   (:execution record)
   expected-validation-hash
   (str "sha256:" (sha256-hex (pr-str (c-backend-canonical-value validation))))]
  (clojure.core/assoc
   state
   :required-action-fields
   required-action-fields
   :invocation
   invocation
   :verification-replay
   verification-replay
   :execution
   execution
   :expected-validation-hash
   expected-validation-hash)))
