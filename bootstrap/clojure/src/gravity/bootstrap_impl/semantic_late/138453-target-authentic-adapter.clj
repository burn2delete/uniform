; Semantic decomposition of committed HEAD reader line 138453.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-p15-s23-closed-runtime-target-record-authentic?-adapter
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
     contract-binding]}
   state
   authority-binding
   (:authority-binding record)
   adapter-record
   (:runtime-adapter-record context)
   adapter-decisions
   (:decision-records adapter-record)
   adapter-actions
   (:action-records adapter-record)
   validation
   (:validation record)
   io-write-active?
   (contains? (:observed-operation-set validation) :println)
   capture-invoked?
   (not= (:stdout-hash context) (str "sha256:" (sha256-hex "")))
   expected-adapter-record
   (p15-s23-reference-runtime-success-adapter-record
    (:plan-id context)
    (:source-id context)
    io-write-active?
    capture-invoked?)
   expected-decision-count
   (if io-write-active? 3 1)
   expected-action-count
   (if io-write-active? 2 1)
   expected-provider-ids
   (cond->
    #{:gravity.reference/jvm-managed-allocator}
    io-write-active?
    (conj :gravity.reference/transcript-capture))
   expected-grant-ids
   (cond->
    #{:gravity.reference/grant-managed-allocation}
    io-write-active?
    (conj :gravity.reference/grant-reference-stdout :gravity.reference/grant-test-fixture))
   required-decision-fields
   #{:package
     :provider-id
     :redaction
     :policy-id
     :phase
     :grant-id
     :capability
     :scope
     :action-id
     :result
     :deployment
     :lifetime
     :effect
     :decision-id
     :principal-id
     :reference-invocation}]
  (clojure.core/assoc
   state
   :authority-binding
   authority-binding
   :adapter-record
   adapter-record
   :adapter-decisions
   adapter-decisions
   :adapter-actions
   adapter-actions
   :validation
   validation
   :io-write-active?
   io-write-active?
   :capture-invoked?
   capture-invoked?
   :expected-adapter-record
   expected-adapter-record
   :expected-decision-count
   expected-decision-count
   :expected-action-count
   expected-action-count
   :expected-provider-ids
   expected-provider-ids
   :expected-grant-ids
   expected-grant-ids
   :required-decision-fields
   required-decision-fields)))
