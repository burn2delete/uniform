

(defn ai-repl-ffi-capability-validate!
  [source-path artifact]
  (let [upstream (:concurrency-distributed-artifact artifact)
        ai (:ai-runtime-manifest artifact)
        model (:model-call-ledger artifact)
        prompt (:prompt-provenance-digest-record artifact)
        tools (:tool-invocation-log artifact)
        output (:structured-output-validation-report artifact)
        memory (:memory-access-retention-record artifact)
        review (:policy-human-review-decision-record artifact)
        budget (:ai-budget-trace artifact)
        replay (:ai-replay-barrier-record artifact)
        repl (:repl-runtime-manifest artifact)
        session (:session-transcript artifact)
        evaluated (:evaluated-form-artifact artifact)
        invalidation (:incremental-invalidation-record artifact)
        hot-reload (:hot-reload-record artifact)
        ffi (:ffi-runtime-manifest artifact)
        binding (:binding-manifest artifact)
        layout (:abi-layout-validation-report artifact)
        wrapper (:safe-wrapper-contract artifact)
        handles (:foreign-handle-lifetime-table artifact)
        callbacks (:callback-adapter-manifest artifact)
        cap-manifest (:runtime-capability-manifest artifact)
        decision-log (:runtime-decision-log artifact)
        delegated (:delegated-handle-record artifact)
        revocation (:revocation-record artifact)
        redaction (:redaction-secret-handling-record artifact)
        conformance (:capability-conformance-evidence artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:ai-repl-ffi-capability-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-concurrency-distributed-runtime-artifact
                 (:kind upstream))
      (ai-repl-ffi-capability-fail! "R8-MANIFEST" source-path upstream
                                    {:missing-fields [:concurrency-distributed-artifact]}))
    (when-not (= :complete (get-in upstream
                                   [:capability-based-proof :status]))
      (ai-repl-ffi-capability-fail! "R8-MANIFEST" source-path upstream
                                    {:missing-fields [:upstream-proof]}))
    (when-not (= :complete (:status ai))
      (ai-repl-ffi-capability-fail! "R8-MANIFEST" source-path ai
                                    {:missing-fields [:ai-runtime]}))
    (when (seq (:missing-provider-effect-capability-schema-budget-or-replay
                model))
      (ai-repl-ffi-capability-fail! "R8-MODEL" source-path model
                                    {:missing-fields [:model-call-ledger]}))
    (when (seq (:role-policy-violations prompt))
      (ai-repl-ffi-capability-fail! "R8-PROMPT" source-path prompt
                                    {:missing-fields [:prompt-policy]}))
    (when (seq (:effects-outside-grants tools))
      (ai-repl-ffi-capability-fail! "R8-TOOL" source-path tools
                                    {:missing-fields [:tool-grants]}))
    (when (seq (:unvalidated-trusted-sink-flows output))
      (ai-repl-ffi-capability-fail! "R8-TAINT" source-path output
                                    {:missing-fields [:taint-validation]}))
    (when (seq (:secret-leaks redaction))
      (ai-repl-ffi-capability-fail! "R8-SECRET" source-path redaction
                                    {:missing-fields [:secret-redaction]}))
    (when (seq (:invalid-retention-or-privacy memory))
      (ai-repl-ffi-capability-fail! "R8-MEMORY" source-path memory
                                    {:missing-fields [:memory-policy]}))
    (when (seq (:missing-required-reviews review))
      (ai-repl-ffi-capability-fail! "R8-HUMAN-REVIEW" source-path review
                                    {:missing-fields [:human-review]}))
    (when (seq (:live-calls-in-replay replay))
      (ai-repl-ffi-capability-fail! "R8-REPLAY" source-path replay
                                    {:missing-fields [:replay-record]}))
    (when (seq (:violations budget))
      (ai-repl-ffi-capability-fail! "R8-BUDGET" source-path budget
                                    {:missing-fields [:budget]}))
    (when-not (= :compiler-checked-before-execution
                 (:generated-code-validation output))
      (ai-repl-ffi-capability-fail! "R8-GENERATED" source-path output
                                    {:missing-fields [:generated-code-gate]}))
    (when-not (= :complete (:status repl))
      (ai-repl-ffi-capability-fail! "R9-MANIFEST" source-path repl
                                    {:missing-fields [:repl-runtime]}))
    (when-not (true? (:compiler-checks-passed? evaluated))
      (ai-repl-ffi-capability-fail! "R9-CHECKS" source-path evaluated
                                    {:missing-fields [:compiler-checks]}))
    (when-not (= :complete (:status session))
      (ai-repl-ffi-capability-fail! "R9-SESSION" source-path session
                                    {:missing-fields [:session-state]}))
    (when (true? (:stale-analysis-kept? hot-reload))
      (ai-repl-ffi-capability-fail! "R9-HOT-RELOAD" source-path hot-reload
                                    {:missing-fields [:invalidation]}))
    (when-not (= :complete (:status invalidation))
      (ai-repl-ffi-capability-fail! "R9-HERMETICITY" source-path invalidation
                                    {:missing-fields [:hermetic-session-artifact]}))
    (when-not (= :complete (:status ffi))
      (ai-repl-ffi-capability-fail! "R10-MANIFEST" source-path ffi
                                    {:missing-fields [:ffi-runtime]}))
    (when-not (= :complete (:status binding))
      (ai-repl-ffi-capability-fail! "R10-BINDING" source-path binding
                                    {:missing-fields [:binding]}))
    (when (seq (:mismatches layout))
      (ai-repl-ffi-capability-fail! "R10-ABI" source-path layout
                                    {:missing-fields [:layout-validation]}))
    (when-not (= :complete (:status wrapper))
      (ai-repl-ffi-capability-fail! "R10-WRAPPER" source-path wrapper
                                    {:missing-fields [:safe-wrapper]}))
    (when (seq (:missing-lifetimes handles))
      (ai-repl-ffi-capability-fail! "R10-POINTER" source-path handles
                                    {:missing-fields [:handle-lifetime]}))
    (when (seq (:violations callbacks))
      (ai-repl-ffi-capability-fail! "R10-CALLBACK" source-path callbacks
                                    {:missing-fields [:callback-adapter]}))
    (when-not (true? (:deny-by-default? cap-manifest))
      (ai-repl-ffi-capability-fail! "R11-GRANT" source-path cap-manifest
                                    {:missing-fields [:deny-by-default]}))
    (when (seq (:missing-required-audit decision-log))
      (ai-repl-ffi-capability-fail! "R11-AUDIT" source-path decision-log
                                    {:missing-fields [:decision-log]}))
    (when (seq (:unscoped-handles delegated))
      (ai-repl-ffi-capability-fail! "R11-DELEGATE" source-path delegated
                                    {:missing-fields [:delegated-handle-scope]}))
    (when (some :use-after-revocation? (:records revocation))
      (ai-repl-ffi-capability-fail! "R11-REVOKE" source-path revocation
                                    {:missing-fields [:revocation]}))
    (when-not (= :complete (:status conformance))
      (ai-repl-ffi-capability-fail! "R11-MANIFEST" source-path conformance
                                    {:missing-fields [:capability-evidence]}))
    (when-not (= (set ai-repl-ffi-capability-diagnostic-ids) diagnostics)
      (ai-repl-ffi-capability-fail! "R11-MANIFEST" source-path
                                    (:ai-repl-ffi-capability-diagnostic-stream
                                     artifact)
                                    {:missing-fields [:diagnostics]})))
  :complete)

(defn ai-repl-ffi-capability-proof
  [artifact]
  (let [model (:model-call-ledger artifact)
        tools (:tool-invocation-log artifact)
        output (:structured-output-validation-report artifact)
        review (:policy-human-review-decision-record artifact)
        replay (:ai-replay-barrier-record artifact)
        budget (:ai-budget-trace artifact)
        evaluated (:evaluated-form-artifact artifact)
        invalidation (:incremental-invalidation-record artifact)
        hot-reload (:hot-reload-record artifact)
        binding (:binding-manifest artifact)
        layout (:abi-layout-validation-report artifact)
        handles (:foreign-handle-lifetime-table artifact)
        callbacks (:callback-adapter-manifest artifact)
        cap-manifest (:runtime-capability-manifest artifact)
        delegated (:delegated-handle-record artifact)
        redaction (:redaction-secret-handling-record artifact)
        conformance (:capability-conformance-evidence artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:ai-repl-ffi-capability-diagnostic-stream
                                       :diagnostics])))]
    {:concurrency-distributed-input-verified?
     (= :complete (get-in artifact
                          [:concurrency-distributed-artifact
                           :capability-based-proof :status]))
     :model-tool-memory-policy-complete?
     (and (empty? (:missing-provider-effect-capability-schema-budget-or-replay
                   model))
          (empty? (:effects-outside-grants tools))
          (empty? (:unvalidated-trusted-sink-flows output)))
     :human-review-replay-budget-complete?
     (and (empty? (:missing-required-reviews review))
          (empty? (:live-calls-in-replay replay))
          (empty? (:violations budget)))
     :interactive-eval-uses-normal-checks?
     (true? (:compiler-checks-passed? evaluated))
     :interactive-state-invalidation-recorded?
     (and (= :complete (:status invalidation))
          (false? (:stale-analysis-kept? hot-reload)))
     :ffi-bindings-wrappers-and_handles_safe?
     (and (= :complete (:status binding))
          (empty? (:mismatches layout))
          (empty? (:missing-lifetimes handles))
          (empty? (:violations callbacks)))
     :runtime-capabilities-deny-by-default?
     (true? (:deny-by-default? cap-manifest))
     :delegation-and-redaction-safe?
     (and (empty? (:unscoped-handles delegated))
          (empty? (:secret-leaks redaction)))
     :capability-conformance-covered?
     (= :complete (:status conformance))
     :diagnostics-covered?
     (= (set ai-repl-ffi-capability-diagnostic-ids) diagnostics)
     :status :complete}))