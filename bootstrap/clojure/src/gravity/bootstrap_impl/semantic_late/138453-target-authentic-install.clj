; Semantic decomposition of committed HEAD reader line 138453.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/let
 [semantic-late-p15-s23-closed-runtime-target-record-authentic?-ingress
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-ingress
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-adapter
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-adapter
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-execution
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-execution
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-adapter-authentic
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-adapter-authentic
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-adapter-records
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-adapter-records
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-record-contract
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-record-contract
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-execution-replay
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-execution-replay
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-safety-provenance
  semantic-late-p15-s23-closed-runtime-target-record-authentic?-safety-provenance]
 (defn
  p15-s23-closed-runtime-target-record-authentic?
  ([record] false)
  ([record context]
   (try
    (clojure.core/let
     [state-0
      {:record record, :context context}
      state-1
      (semantic-late-p15-s23-closed-runtime-target-record-authentic?-ingress state-0)
      state-2
      (semantic-late-p15-s23-closed-runtime-target-record-authentic?-adapter state-1)
      state-3
      (semantic-late-p15-s23-closed-runtime-target-record-authentic?-execution state-2)]
     (clojure.core/and
      (semantic-late-p15-s23-closed-runtime-target-record-authentic?-adapter-authentic state-3)
      (semantic-late-p15-s23-closed-runtime-target-record-authentic?-adapter-records state-3)
      (semantic-late-p15-s23-closed-runtime-target-record-authentic?-record-contract state-3)
      (semantic-late-p15-s23-closed-runtime-target-record-authentic?-execution-replay state-3)
      (semantic-late-p15-s23-closed-runtime-target-record-authentic?-safety-provenance state-3)))
    (catch StackOverflowError _ false)
    (catch Exception _ false)))))
