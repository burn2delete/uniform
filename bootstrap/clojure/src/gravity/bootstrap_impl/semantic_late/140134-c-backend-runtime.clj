; Semantic decomposition of committed HEAD reader line 140134.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-c-backend-source-artifact-runtime
 [state]
 (clojure.core/let
  [{:keys
    [source-path
     source-text
     target
     dialect
     emit-dir
     compile?
     lowering-mode
     runtime-derived?
     executable-path
     c-source-path
     manifest-path
     source-map-path
     provenance-path
     shared-packet
     macro-artifact
     module
     stage2-rule
     stage2-compiler-artifact-record
     stage2-compiler-artifact-source-path
     stage2-runtime-rule
     stage2-driver-rule
     plan]}
   state
   stage2-driver-run
   (:stage2-compiler-driver-record shared-packet)
   stage2-runtime-execution
   (:stage2-runtime-execution-record shared-packet)
   closed-plan-validation
   (:closed-plan-validation-record shared-packet)
   closed-plan-execution
   (:closed-plan-execution-record shared-packet)
   closed-plan-invocation
   (:closed-plan-invocation-record shared-packet)
   closed-plan-target-record
   (when runtime-derived? (p15-s23-closed-runtime-target-record shared-packet))
   _
   (when
    (and
     runtime-derived?
     (not
      (p15-s23-closed-runtime-target-record-authentic?
       closed-plan-target-record
       (p15-s23-closed-runtime-target-context shared-packet))))
    (c-backend-fail!
     "C14-INPUT"
     "C backend received an unauthenticated closed runtime target record"
     source-path
     target
     closed-plan-target-record
     {:missing-fact :authenticated-closed-runtime-target-record}))]
  (clojure.core/assoc
   state
   :stage2-driver-run
   stage2-driver-run
   :stage2-runtime-execution
   stage2-runtime-execution
   :closed-plan-validation
   closed-plan-validation
   :closed-plan-execution
   closed-plan-execution
   :closed-plan-invocation
   closed-plan-invocation
   :closed-plan-target-record
   closed-plan-target-record)))
