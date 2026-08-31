; Semantic decomposition of committed HEAD reader line 140134.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-c-backend-source-artifact-artifact-base
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
     source-map-hash
     manifest-input
     manifest-hash
     provenance
     provenance-hash
     identity-input]}
   state
   artifact-base
   {:runtime-artifact-source-path
    (when runtime-derived? (:runtime-artifact-source-path stage2-runtime-rule)),
    :runtime-artifact-capabilities
    (when
     runtime-derived?
     (get-in stage2-runtime-rule [:runtime-artifact-plan :module :capabilities])),
    :capabilities (get-in plan [:module :capabilities]),
    :runtime-engine (when runtime-derived? (:runtime-engine stage2-runtime-rule)),
    :seed-boundary {:clojure-seed-boundary? true, :self-hosted? false, :final-release? false},
    :input-plan-hash plan-hash,
    :diagnostics [],
    :runtime-artifact-generic-bridge-residual?
    (when runtime-derived? (:runtime-artifact-generic-bridge-residual? stage2-runtime-rule)),
    :closed-runtime-validation-context
    (when runtime-derived? (p15-s23-closed-runtime-target-context shared-packet)),
    :runtime-artifact-source
    (when
     runtime-derived?
     {:println-function (:runtime-artifact-println-function stage2-runtime-rule),
      :concat-function (:runtime-artifact-concat-function stage2-runtime-rule),
      :function (:runtime-artifact-function stage2-runtime-rule),
      :kind :gravity-source,
      :sha256 (:runtime-artifact-source-content-hash stage2-runtime-rule),
      :println-two-function (:runtime-artifact-println-two-function stage2-runtime-rule),
      :println-over-two-boundary (:runtime-artifact-println-over-two-boundary stage2-runtime-rule),
      :artifact-hash (:runtime-artifact-hash stage2-runtime-rule),
      :generic-bridge-residual? (:runtime-artifact-generic-bridge-residual? stage2-runtime-rule)}),
    :input-plan-id (:plan-id plan),
    :task "HOSTED-C-TARGET",
    :runtime-artifact-host-runner
    (when runtime-derived? :gravity-stage2-runtime-artifact-host-runner),
    :source-map-hash source-map-hash,
    :closed-plan-execution-record (when runtime-derived? closed-plan-execution),
    :expression-lowering-artifact
    (when runtime-derived? (dissoc stage2-compiler-artifact-record :source-path)),
    :runtime-kernel-rule-hash
    (when runtime-derived? (:runtime-kernel-rule-hash stage2-runtime-rule)),
    :runtime-artifact-closed-plan-function
    (when runtime-derived? (:runtime-artifact-closed-plan-function stage2-runtime-rule)),
    :compiler-driver-rule-hash (when runtime-derived? (:driver-rule-hash stage2-driver-rule)),
    :input-plan-kind (:kind plan),
    :closed-plan-invocation-record (when runtime-derived? closed-plan-invocation),
    :provenance-hash provenance-hash,
    :effect-summary (:effect-summary plan),
    :runtime-rule-hash (when runtime-derived? (:runtime-rule-hash stage2-runtime-rule)),
    :manifest (assoc manifest-input :manifest-hash manifest-hash),
    :source
    {:path source-path,
     :extension (gravity-source-extension source-path),
     :kind (gravity-source-kind source-path),
     :sha256 source-hash},
    :instruction-summary (:instruction-summary plan),
    :closed-plan-runtime-target-record (when runtime-derived? closed-plan-target-record),
    :manifest-hash manifest-hash,
    :stage2-compiler-driver-record (when runtime-derived? stage2-driver-run),
    :runtime-rule-source-path (when runtime-derived? (:runtime-source-path stage2-runtime-rule)),
    :runtime-artifact-effects
    (when runtime-derived? (get-in stage2-runtime-rule [:runtime-artifact-plan :module :effects])),
    :runtime-rule-source (when runtime-derived? (:runtime-rule-source stage2-runtime-rule)),
    :clojure-instruction-runner-comparison
    {:boundary (if runtime-derived? :comparison-only :not-used),
     :authoritative-runtime? false,
     :stdout clojure-stage0-output},
    :status :complete,
    :compiler-driver-rule-source (when runtime-derived? (:driver-rule-source stage2-driver-rule)),
    :runtime-artifact-closed-plan-function-hash
    (when runtime-derived? (:runtime-artifact-closed-plan-function-hash stage2-runtime-rule)),
    :safety {:mode (get-in plan [:module :safety]), :unsafe-islands [], :status :preserved},
    :kind :gravity/c-backend-artifact,
    :runtime-artifact-println-two-function
    (when runtime-derived? (:runtime-artifact-println-two-function stage2-runtime-rule)),
    :runtime-artifact-concat-function
    (when runtime-derived? (:runtime-artifact-concat-function stage2-runtime-rule)),
    :source-map (assoc source-map :source-map-hash source-map-hash),
    :closed-plan-validation-record (when runtime-derived? closed-plan-validation),
    :provenance
    (cond->
     (assoc provenance :provenance-hash provenance-hash)
     runtime-derived?
     (assoc
      :actual-paths
      {:stage2-expression-lowering-source stage2-compiler-artifact-source-path,
       :stage2-runtime-artifact-source (:runtime-artifact-source-path stage2-runtime-rule)})),
    :c-source-hash c-source-hash,
    :stage2-compiler-driver-used? (when runtime-derived? true),
    :target
    (cond->
     {:backend :c, :dialect dialect, :target target, :runtime :hosted-libc-stdout}
     runtime-derived?
     (assoc :lowering-mode :runtime-derived)),
    :runtime-artifact-hash (when runtime-derived? (:runtime-artifact-hash stage2-runtime-rule)),
    :runtime-artifact-println-over-two-boundary
    (when runtime-derived? (:runtime-artifact-println-over-two-boundary stage2-runtime-rule)),
    :runtime-artifact-function
    (when runtime-derived? (:runtime-artifact-function stage2-runtime-rule)),
    :runtime-artifact-println-function
    (when runtime-derived? (:runtime-artifact-println-function stage2-runtime-rule)),
    :c-source c-source,
    :stage2-runtime-execution-record (when runtime-derived? stage2-runtime-execution-record),
    :runtime-kernel-engine (when runtime-derived? (:runtime-kernel-engine stage2-runtime-rule)),
    :compiler-engine (when runtime-derived? (:driver-engine stage2-driver-rule)),
    :stdout stdout,
    :compiler-stage (when runtime-derived? :p15-s23-stage2-compiler-driver)}]
  (clojure.core/assoc state :artifact-base artifact-base)))
