; Semantic decomposition of committed HEAD reader line 140134.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-c-backend-source-artifact-packet
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
     provenance-path]}
   state
   runtime-derived?
   (or (= :runtime-derived lowering-mode) (= true runtime-derived?))
   shared-packet
   (when
    runtime-derived?
    (stage2-runtime-derived-packet
     source-path
     source-text
     target
     {:validate-plan!
      (fn
       [candidate-plan]
       (when-not
        (and
         (= :gravity/stage2-hosted-core-compiled-plan (:kind candidate-plan))
         (= :p15-s23-stage2-plan-emitter (get-in candidate-plan [:compiler :rule-source]))
         (map? (:functions candidate-plan))
         (symbol? (:entrypoint candidate-plan))
         (contains? (:functions candidate-plan) (:entrypoint candidate-plan))
         (vector? (get-in candidate-plan [:functions (:entrypoint candidate-plan) :instructions])))
        (c-backend-fail!
         "B2-UNSUPPORTED"
         "Gravity stage2 plan emitter returned a mismatched plan"
         source-path
         target
         candidate-plan
         {:compiler-stage :p15-s23-stage2-plan-emitter,
          :observed-plan-kind (:kind candidate-plan),
          :observed-rule-source (get-in candidate-plan [:compiler :rule-source]),
          :p15-diagnostic "P15S23Q003",
          :missing-fact :stage2-plan-integrity}))
       (c-backend-validate-plan! source-path target candidate-plan)
       (c-backend-validate-runtime-plan! source-path target candidate-plan))}))
   macro-artifact
   (when-not runtime-derived? (macro-source-artifact source-path source-text))
   module
   (when-not
    runtime-derived?
    (assoc (:module macro-artifact) :forms (:expanded-forms macro-artifact)))
   stage2-rule
   (:stage2-plan-emitter-rule shared-packet)
   stage2-compiler-artifact-record
   (:stage2-compiler-artifact-record shared-packet)
   _
   (when
    runtime-derived?
    (when-not
     (p15-s23-stage2-compiler-artifact-record-authentic? stage2-compiler-artifact-record)
     (c-backend-fail!
      "C14-INPUT"
      "C backend received inconsistent stage2 compiler artifact metadata"
      source-path
      target
      stage2-compiler-artifact-record
      {:missing-fact :authenticated-stage2-compiler-artifact-record})))
   stage2-compiler-artifact-source-path
   (get-in shared-packet [:provenance :actual-paths :stage2-expression-lowering-source])
   stage2-runtime-rule
   (:stage2-runtime-rule shared-packet)
   stage2-driver-rule
   (:stage2-compiler-driver-rule shared-packet)
   _
   (when
    (and
     runtime-derived?
     (not
      (p15-s23-closed-runtime-packet-authentic?
       shared-packet
       (p15-s23-closed-runtime-packet-context source-path source-text target))))
    (c-backend-fail!
     "C14-INPUT"
     "C backend received an unauthenticated closed runtime execution"
     source-path
     target
     shared-packet
     {:missing-fact :authenticated-closed-runtime-execution}))
   plan
   (if
    runtime-derived?
    (:plan shared-packet)
    (stage0-compiled-core-plan source-path source-text module))
   _
   (when
    (and
     runtime-derived?
     (not
      (p15-s23-stage2-compiler-artifact-record-matches-plan?
       stage2-compiler-artifact-record
       plan)))
    (c-backend-fail!
     "C14-INPUT"
     "C backend received a stage2 compiler record that does not match its plan"
     source-path
     target
     stage2-compiler-artifact-record
     {:missing-fact :authenticated-stage2-compiler-artifact-record}))
   _
   (when-not runtime-derived? (c-backend-validate-plan! source-path target plan))]
  (clojure.core/assoc
   state
   :runtime-derived?
   runtime-derived?
   :shared-packet
   shared-packet
   :macro-artifact
   macro-artifact
   :module
   module
   :stage2-rule
   stage2-rule
   :stage2-compiler-artifact-record
   stage2-compiler-artifact-record
   :stage2-compiler-artifact-source-path
   stage2-compiler-artifact-source-path
   :stage2-runtime-rule
   stage2-runtime-rule
   :stage2-driver-rule
   stage2-driver-rule
   :plan
   plan)))
