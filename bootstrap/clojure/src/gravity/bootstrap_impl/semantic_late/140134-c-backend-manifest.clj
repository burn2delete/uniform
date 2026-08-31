; Semantic decomposition of committed HEAD reader line 140134.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-c-backend-source-artifact-manifest
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
     plan
     stage2-driver-run
     stage2-runtime-execution
     closed-plan-validation
     closed-plan-execution
     closed-plan-invocation
     closed-plan-target-record
     clojure-stage0-output
     stdout
     c-source
     source-hash
     plan-input
     plan-hash
     stage2-runtime-execution-record
     c-source-hash
     output-hash
     source-map
     source-map-hash]}
   state
   manifest-input
   (cond->
    {:source-content-hash source-hash,
     :capabilities (get-in plan [:module :capabilities]),
     :lowering-strategy
     (if runtime-derived? :runtime-derived-instruction-lowering :verified-stage0-output-lowering),
     :safety-mode (get-in plan [:module :safety]),
     :input-plan-hash plan-hash,
     :stdout-hash output-hash,
     :schema-version "gravity.c.backend-manifest/v1",
     :input-plan-kind (:kind plan),
     :effect-summary (:effect-summary plan),
     :oracle
     (when
      runtime-derived?
      {:runtime-engine (:runtime-engine stage2-runtime-rule),
       :runtime-artifact-generic-bridge-residual?
       (:runtime-artifact-generic-bridge-residual? stage2-runtime-rule),
       :runtime-artifact-host-runner :gravity-stage2-runtime-artifact-host-runner,
       :purpose :parity-only,
       :runtime-kernel-rule-hash (:runtime-kernel-rule-hash stage2-runtime-rule),
       :compiler-driver-rule-hash (:driver-rule-hash stage2-driver-rule),
       :runtime-rule-hash (:runtime-rule-hash stage2-runtime-rule),
       :compiler-driver-engine (:driver-engine stage2-driver-rule),
       :clojure-instruction-runner-comparison
       {:boundary :comparison-only, :authoritative-runtime? false},
       :kind :gravity-stage2-runtime-execution,
       :runtime-artifact-println-two-function
       (:runtime-artifact-println-two-function stage2-runtime-rule),
       :runtime-artifact-concat-function (:runtime-artifact-concat-function stage2-runtime-rule),
       :authoritative-runtime? false,
       :runtime-artifact-hash (:runtime-artifact-hash stage2-runtime-rule),
       :runtime-artifact-println-over-two-boundary
       (:runtime-artifact-println-over-two-boundary stage2-runtime-rule),
       :runtime-artifact-function (:runtime-artifact-function stage2-runtime-rule),
       :runtime-artifact-println-function (:runtime-artifact-println-function stage2-runtime-rule),
       :runtime-kernel-engine (:runtime-kernel-engine stage2-runtime-rule)}),
     :instruction-summary (:instruction-summary plan),
     :stage2-compiler-driver-record
     (when
      runtime-derived?
      {:artifact :gravity/p15-s23-stage2-compiler-driver-run-record,
       :driver-engine (:driver-engine stage2-driver-rule),
       :driver-rule-hash (:driver-rule-hash stage2-driver-rule),
       :plan-id plan-hash,
       :runtime-execution-status
       (get-in stage2-driver-run [:stage2-runtime-execution-record :status]),
       :accepted-output-equivalent? (:accepted-output-equivalent? stage2-driver-run),
       :status (:status stage2-driver-run)}),
     :runtime :hosted-libc-stdout,
     :kind :gravity/c-backend-manifest,
     :c-source-hash c-source-hash,
     :target target,
     :stage2-runtime-execution-record
     (when
      runtime-derived?
      {:artifact :gravity/p15-s23-stage2-runtime-execution-record,
       :runtime-engine (:runtime-engine stage2-runtime-rule),
       :plan-id (:plan-id stage2-runtime-execution-record),
       :stdout-hash output-hash,
       :status (:status stage2-runtime-execution-record)}),
     :backend :c,
     :profile (get-in plan [:module :profile]),
     :seedless-release? false,
     :runtime-derived? runtime-derived?,
     :target-eligibility (when runtime-derived? (:target-eligibility shared-packet)),
     :compile-time-evaluated? (not runtime-derived?),
     :dialect dialect}
    runtime-derived?
    (assoc
     :compiler-stage
     :p15-s23-stage2-compiler-driver
     :expression-lowering-artifact-hash
     (:artifact-hash stage2-compiler-artifact-record)
     :expression-lowering-source-content-hash
     (:source-content-hash stage2-compiler-artifact-record)
     :expression-lowering-semantic-hash
     (:semantic-hash stage2-compiler-artifact-record)
     :expression-lowering-invoked?
     (:invoked? stage2-compiler-artifact-record)
     :expression-lowering-generic-bridge-residual?
     (:generic-bridge-residual? stage2-compiler-artifact-record)
     :plan-assembly-function
     (:plan-assembly-function stage2-compiler-artifact-record)
     :plan-assembly-artifact-hash
     (:plan-assembly-artifact-hash stage2-compiler-artifact-record)
     :plan-assembly-source-content-hash
     (:plan-assembly-source-content-hash stage2-compiler-artifact-record)
     :plan-assembly-semantic-hash
     (:plan-assembly-semantic-hash stage2-compiler-artifact-record)
     :plan-assembly-invoked?
     (:plan-assembly-invoked? stage2-compiler-artifact-record)
     :plan-assembly-generic-bridge-residual?
     (:plan-assembly-generic-bridge-residual? stage2-compiler-artifact-record)
     :compiler-engine
     (:driver-engine stage2-driver-rule)
     :plan-emitter-stage
     :p15-s23-stage2-plan-emitter
     :plan-emitter-engine
     (get-in plan [:compiler :rule-engine])
     :compiler-source-rule-hash
     (:source-rule-hash stage2-rule)
     :compiler-driver-rule-hash
     (:driver-rule-hash stage2-driver-rule)
     :compiler-driver-rule-source
     (:driver-rule-source stage2-driver-rule)
     :stage2-compiler-driver-used?
     true
     :runtime-engine
     (:runtime-engine stage2-runtime-rule)
     :runtime-kernel-engine
     (:runtime-kernel-engine stage2-runtime-rule)
     :runtime-rule-hash
     (:runtime-rule-hash stage2-runtime-rule)
     :runtime-kernel-rule-hash
     (:runtime-kernel-rule-hash stage2-runtime-rule)
     :runtime-artifact-hash
     (:runtime-artifact-hash stage2-runtime-rule)
     :runtime-artifact-function
     (:runtime-artifact-function stage2-runtime-rule)
     :runtime-artifact-concat-function
     (:runtime-artifact-concat-function stage2-runtime-rule)
     :runtime-artifact-println-function
     (:runtime-artifact-println-function stage2-runtime-rule)
     :runtime-artifact-println-two-function
     (:runtime-artifact-println-two-function stage2-runtime-rule)
     :runtime-artifact-closed-plan-function
     (:runtime-artifact-closed-plan-function stage2-runtime-rule)
     :runtime-artifact-closed-plan-function-hash
     (:runtime-artifact-closed-plan-function-hash stage2-runtime-rule)
     :closed-plan-validation-record
     closed-plan-validation
     :closed-plan-execution-record
     closed-plan-execution
     :closed-plan-invocation-record
     closed-plan-invocation
     :closed-plan-runtime-target-record
     closed-plan-target-record
     :runtime-artifact-println-over-two-boundary
     (:runtime-artifact-println-over-two-boundary stage2-runtime-rule)
     :runtime-artifact-effects
     (:runtime-artifact-effects stage2-runtime-rule)
     :runtime-artifact-capabilities
     (:runtime-artifact-capabilities stage2-runtime-rule)
     :runtime-artifact-generic-bridge-residual?
     (:runtime-artifact-generic-bridge-residual? stage2-runtime-rule)
     :runtime-artifact-source
     {:kind :gravity-source,
      :sha256 (:runtime-artifact-source-content-hash stage2-runtime-rule),
      :artifact-hash (:runtime-artifact-hash stage2-runtime-rule),
      :function (:runtime-artifact-function stage2-runtime-rule),
      :concat-function (:runtime-artifact-concat-function stage2-runtime-rule),
      :println-function (:runtime-artifact-println-function stage2-runtime-rule),
      :println-two-function (:runtime-artifact-println-two-function stage2-runtime-rule),
      :println-over-two-boundary (:runtime-artifact-println-over-two-boundary stage2-runtime-rule)}
     :runtime-artifact-host-runner
     :gravity-stage2-runtime-artifact-host-runner))
   manifest-hash
   (str
    "sha256:"
    (sha256-hex
     (pr-str
      (c-backend-canonical-value
       (update
        manifest-input
        :closed-plan-runtime-target-record
        p15-s23-closed-runtime-target-semantic-record)))))]
  (clojure.core/assoc state :manifest-input manifest-input :manifest-hash manifest-hash)))
