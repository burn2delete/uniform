; Semantic decomposition of committed HEAD reader line 140134.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-c-backend-source-artifact-identity
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
     provenance-hash]}
   state
   identity-input
   (cond->
    {:source-content-hash source-hash,
     :input-plan-hash plan-hash,
     :source-map-hash source-map-hash,
     :stdout-hash output-hash,
     :provenance-hash provenance-hash,
     :manifest-hash manifest-hash,
     :kind :gravity/c-backend-artifact,
     :c-source-hash c-source-hash,
     :target target,
     :backend :c,
     :dialect dialect}
    runtime-derived?
    (assoc
     :compiler-driver-rule-hash
     (:driver-rule-hash stage2-driver-rule)
     :expression-lowering-artifact-hash
     (:artifact-hash stage2-compiler-artifact-record)
     :plan-assembly-artifact-hash
     (:plan-assembly-artifact-hash stage2-compiler-artifact-record)
     :runtime-artifact-hash
     (:runtime-artifact-hash stage2-runtime-rule)
     :closed-plan-execution-hash
     (:execution-hash closed-plan-invocation)
     :closed-plan-function-hash
     (:function-hash closed-plan-invocation)
     :closed-plan-target-record-hash
     (:record-hash closed-plan-target-record)))]
  (clojure.core/assoc state :identity-input identity-input)))
