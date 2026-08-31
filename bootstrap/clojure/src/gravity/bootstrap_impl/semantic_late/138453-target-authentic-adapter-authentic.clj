; Semantic decomposition of committed HEAD reader line 138453.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-p15-s23-closed-runtime-target-record-authentic?-adapter-authentic
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
     required-decision-fields
     required-action-fields
     invocation
     verification-replay
     execution
     expected-validation-hash]}
   state]
  (clojure.core/and
   context-envelope-valid?
   runtime-source-file-valid?
   (= p15-s23-stage2-runtime-artifact-expected-source-content-hash runtime-source-file-hash)
   (digest? (:source-id context))
   (map? adapter-record)
   (= expected-adapter-record adapter-record)
   (=
    #{:action-records
      :reference-interpreter?
      :deployment-runtime?
      :runtime-contract-definition-hash
      :source-principal
      :authority
      :plan-id
      :mode
      :io-write-active?
      :provider-ids
      :function-hash
      :handler-principal
      :function
      :decision-records
      :grant-ids
      :self-hosted?
      :status
      :record-hash
      :runtime-contract-derived-facts-hash
      :source-id
      :artifact
      :runtime-artifact-hash
      :clojure-seed-boundary?}
    (set (keys adapter-record)))
   (= :gravity/p15-s23-reference-runtime-adapter-record (:artifact adapter-record))
   (= :complete (:status adapter-record))
   (= :closed-plan-reference (:mode adapter-record))
   (=
    p15-s23-stage2-runtime-artifact-expected-artifact-hash
    (:runtime-artifact-hash adapter-record))
   (=
    p15-s23-reference-runtime-expected-contract-definition-hash
    (:runtime-contract-definition-hash adapter-record))
   (=
    p15-s23-reference-runtime-expected-derived-facts-hash
    (:runtime-contract-derived-facts-hash adapter-record))
   (= p15-s23-stage2-runtime-artifact-closed-plan-function (:function adapter-record))
   (=
    (get
     p15-s23-reference-runtime-expected-function-hashes
     p15-s23-stage2-runtime-artifact-closed-plan-function)
    (:function-hash adapter-record))
   (= (:plan-id context) (:plan-id adapter-record))
   (= (:source-id context) (:source-id adapter-record))
   (= 'gravity.bootstrap.p15-s23.runtime (:source-principal adapter-record))
   (= :gravity.bootstrap/reference-harness (:handler-principal adapter-record))
   (= expected-provider-ids (:provider-ids adapter-record))
   (= expected-grant-ids (:grant-ids adapter-record)))))
