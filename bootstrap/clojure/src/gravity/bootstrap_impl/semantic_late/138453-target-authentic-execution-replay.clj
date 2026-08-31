; Semantic decomposition of committed HEAD reader line 138453.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-p15-s23-closed-runtime-target-record-authentic?-execution-replay
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
   (= :gravity/p15-s23-runtime-closed-plan-invocation-record (:artifact invocation))
   (=
    #{:invocation-count-scope
      :stdout-hash
      :plan-id
      :function-hash
      :function
      :status
      :artifact
      :runtime-artifact-hash
      :invocation-count
      :verification-replays-excluded?}
    (set (keys invocation)))
   (= 1 (:invocation-count invocation))
   (= :authoritative-packet-construction (:invocation-count-scope invocation))
   (true? (:verification-replays-excluded? invocation))
   (= :complete (:status invocation))
   (= (:executor-function record) (:function invocation))
   (= (:executor-function-hash record) (:function-hash invocation))
   (= (:runtime-artifact-hash record) (:runtime-artifact-hash invocation))
   (every? digest? [(:plan-id invocation) (:stdout-hash invocation)])
   (= :gravity/p15-s23-runtime-closed-plan-execution-record (:artifact execution))
   (= #{:stdout-hash :plan-id :status :entrypoint :artifact} (set (keys execution)))
   (= :complete (:status execution))
   (= (:plan-id validation) (:plan-id invocation))
   (= (:plan-id execution) (:plan-id invocation))
   (= (:entrypoint validation) (:entrypoint execution))
   (= (:stdout-hash invocation) (:stdout-hash execution))
   (=
    {:artifact :gravity/p15-s23-runtime-closed-plan-verification-replay-record,
     :function p15-s23-stage2-runtime-artifact-closed-plan-function,
     :replay-count 1,
     :count-scope :consumer-packet-authentication,
     :included-in-authoritative-invocation-count? false,
     :status :passed}
    verification-replay)
   (= (:plan-id context) (:plan-id invocation))
   (= (:entrypoint context) (:entrypoint execution))
   (= (:stdout-hash context) (:stdout-hash execution)))))
