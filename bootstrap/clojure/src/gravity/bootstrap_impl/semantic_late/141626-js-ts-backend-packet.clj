; Semantic decomposition of committed HEAD reader line 141626.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-js-ts-backend-source-artifact-packet
 [state]
 (clojure.core/let
  [{:keys [source-path source-text target output-path emit?]}
   state
   node-version
   (js-ts-backend-node-version! source-path)
   packet
   (stage2-runtime-derived-packet
    source-path
    source-text
    target
    {:validate-plan!
     (fn [candidate-plan] (js-ts-backend-validate-plan! source-path candidate-plan))})
   compiler-artifact-record
   (:stage2-compiler-artifact-record packet)
   _
   (when-not
    (p15-s23-stage2-compiler-artifact-record-authentic? compiler-artifact-record)
    (js-ts-backend-fail!
     "C14-INPUT"
     "JS/TS backend received inconsistent stage2 compiler artifact metadata"
     source-path
     compiler-artifact-record
     {:missing-fact :authenticated-stage2-compiler-artifact-record}))
   _
   (when-not
    (p15-s23-closed-runtime-packet-authentic?
     packet
     (p15-s23-closed-runtime-packet-context source-path source-text target))
    (js-ts-backend-fail!
     "C14-INPUT"
     "JS/TS backend received an unauthenticated closed runtime execution"
     source-path
     packet
     {:missing-fact :authenticated-closed-runtime-execution}))
   compiler-artifact-source-path
   (get-in packet [:provenance :actual-paths :stage2-expression-lowering-source])
   driver-record
   (:stage2-compiler-driver-record packet)
   runtime-record
   (:stage2-runtime-execution-record packet)
   runtime-rule
   (:stage2-runtime-rule packet)
   driver-rule
   (:stage2-compiler-driver-rule packet)
   closed-plan-runtime
   (p15-s23-closed-runtime-target-record packet)
   closed-runtime-context
   (p15-s23-closed-runtime-target-context packet)
   _
   (when-not
    (p15-s23-closed-runtime-target-record-authentic? closed-plan-runtime closed-runtime-context)
    (js-ts-backend-fail!
     "C14-INPUT"
     "JS/TS backend received an unauthenticated closed runtime target record"
     source-path
     closed-plan-runtime
     {:missing-fact :authenticated-closed-runtime-target-record}))
   plan
   (:plan packet)
   _
   (when-not
    (p15-s23-stage2-compiler-artifact-record-matches-plan? compiler-artifact-record plan)
    (js-ts-backend-fail!
     "C14-INPUT"
     "JS/TS backend received a stage2 compiler record that does not match its plan"
     source-path
     compiler-artifact-record
     {:missing-fact :authenticated-stage2-compiler-artifact-record}))
   _
   (when-not
    (= :safe (get-in plan [:module :safety]))
    (js-ts-backend-fail!
     "C14-PROFILE"
     "bounded JS/TS target does not lower unsafe modules"
     source-path
     plan
     {:observed-safety-mode (get-in plan [:module :safety]),
      :supported-safety-modes [:safe],
      :missing-fact :safe-js-ts-module}))
   _
   (when-not
    (c-backend-runtime-plan-supported? plan)
    (js-ts-backend-fail!
     "C14-UNSUPPORTED"
     "JS/TS backend cannot lower this stage2 instruction plan"
     source-path
     plan
     {:missing-fact :js-ts-lowering-rule, :fallback-status :rejected}))
   _
   (js-ts-backend-validate-plan! source-path plan)
   plan-hash
   (c4-artifact-id
    (c-backend-canonical-value
     (select-keys plan [:kind :entrypoint :functions :instruction-summary :effect-summary])))]
  (clojure.core/assoc
   state
   :node-version
   node-version
   :packet
   packet
   :compiler-artifact-record
   compiler-artifact-record
   :compiler-artifact-source-path
   compiler-artifact-source-path
   :driver-record
   driver-record
   :runtime-record
   runtime-record
   :runtime-rule
   runtime-rule
   :driver-rule
   driver-rule
   :closed-plan-runtime
   closed-plan-runtime
   :closed-runtime-context
   closed-runtime-context
   :plan
   plan
   :plan-hash
   plan-hash)))
