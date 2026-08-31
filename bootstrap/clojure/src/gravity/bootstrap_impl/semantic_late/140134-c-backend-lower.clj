; Semantic decomposition of committed HEAD reader line 140134.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-c-backend-source-artifact-lower
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
     closed-plan-target-record]}
   state
   clojure-stage0-output
   (if
    runtime-derived?
    (:reference-output shared-packet)
    (try
     (execute-stage0-compiled-plan plan)
     (catch clojure.lang.ExceptionInfo ex (throw ex))
     (catch
      Exception
      ex
      (c-backend-fail!
       "B2-UNSUPPORTED"
       "stage0 plan could not be represented by the C backend"
       source-path
       target
       nil
       {:cause-message (.getMessage ex), :missing-fact :closed-c-runtime-semantics}))))
   stdout
   (if runtime-derived? (:stdout stage2-runtime-execution) clojure-stage0-output)
   c-source
   (if runtime-derived? (c-backend-runtime-source plan) (c-backend-source stdout))
   source-hash
   (str "sha256:" (sha256-hex source-text))
   plan-input
   (select-keys plan [:kind :entrypoint :functions :instruction-summary :effect-summary])
   plan-hash
   (c4-artifact-id (c-backend-canonical-value plan-input))
   stage2-runtime-execution-record
   (when runtime-derived? (assoc stage2-runtime-execution :plan-id plan-hash))
   c-source-hash
   (str "sha256:" (sha256-hex c-source))
   output-hash
   (str "sha256:" (sha256-hex stdout))]
  (clojure.core/assoc
   state
   :clojure-stage0-output
   clojure-stage0-output
   :stdout
   stdout
   :c-source
   c-source
   :source-hash
   source-hash
   :plan-input
   plan-input
   :plan-hash
   plan-hash
   :stage2-runtime-execution-record
   stage2-runtime-execution-record
   :c-source-hash
   c-source-hash
   :output-hash
   output-hash)))
