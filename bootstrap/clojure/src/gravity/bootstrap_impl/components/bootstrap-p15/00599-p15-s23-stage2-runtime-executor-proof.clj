

(defn p15-s23-stage2-runtime-executor-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-stage2-runtime-executor-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-stage2-runtime-executor-fixtures
                      artifact)))
        accepted-record (:accepted-record artifact)
        rejected-record (:rejected-record artifact)
        boundary-record (:boundary-record artifact)]
    {:stage2-runtime-executor-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :stage2-runtime-executor-present?
     (= :gravity/stage2-runtime-executor
        (get-in artifact [:runtime-contract :artifact]))
     :stage2-runtime-executed?
     (true? (:stage2-runtime-executed? accepted-record))
     :stage0-instruction-runner-replaced?
     (true? (:stage0-instruction-runner-replaced?
             boundary-record))
     :accepted-output-equivalent?
     (true? (:accepted-output-equivalent? accepted-record))
     :entrypoint-result-equivalent?
     (true? (:entrypoint-result-equivalent? accepted-record))
     :instruction-summary-equivalent?
     (true? (:instruction-summary-equivalent? accepted-record))
     :effect-summary-equivalent?
     (true? (:effect-summary-equivalent? accepted-record))
	     :rejected-diagnostics-equivalent?
	     (= :complete (:status rejected-record))
	     :stage2-runtime-kernel-used?
	     (true? (:stage2-runtime-kernel-used? boundary-record))
	     :stage2-runtime-host-replaced?
	     (true? (:stage2-runtime-host-replaced? boundary-record))
	     :stage2-runtime-primitives-replaced?
	     (true? (:stage2-runtime-primitives-replaced?
	             boundary-record))
	     :gravity-runtime-primitives-used?
	     (true? (:gravity-runtime-primitives? boundary-record))
	     :does-not-use-clojure-stage0-runtime-host?
	     (false? (:clojure-stage0-runtime-host? boundary-record))
	     :does-not-use-clojure-runtime-primitives?
	     (false? (:clojure-host-primitive-boundary? boundary-record))
	     :residual-clojure-rule-runner-recorded?
	     (true? (:clojure-stage0-rule-runner? boundary-record))
     :does-not-use-clojure-instruction-runner?
     (false? (:clojure-instruction-runner? boundary-record))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (= (set p15-s23-stage2-runtime-executor-diagnostic-ids)
        rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-stage2-runtime-executor-diagnostic-ids)
        diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
	      :stage2-runtime-executor-authored-in-gravity? true
	      :stage2-runtime-executed? true
	      :stage0-instruction-runner-replaced? true
	      :stage2-runtime-kernel-used? true
	      :stage2-runtime-host-replaced? true
	      :stage2-runtime-primitives-replaced? true
	      :gravity-runtime-primitives? true
	      :clojure-stage0-runtime-host? false
	      :clojure-host-primitive-boundary? false
	      :clojure-stage0-rule-runner? true
	      :clojure-instruction-runner? false
	      :full-self-hosted-toolchain? false
	      :next-required-capability
	      :implement_whole_language_compiler_stage_without_clojure_seed}}))

(defn p15-s23-stage2-runtime-executor-source-artifact
  [source-path]
  (let [source-data (p15-s23-compiler-source-form-record source-path)
        runtime
        (p15-s23-compiler-def-value
         source-path (:forms source-data)
         'p15-s23-stage2-runtime-executor)
        emitter
        (p15-s23-compiler-def-value
         source-path (:forms source-data)
         'p15-s23-stage2-plan-emitter)
        rule-record
        (p15-s23-stage2-runtime-executor-rule-record runtime)
        accepted-record
        (p15-s23-stage2-runtime-executor-accepted-record
         runtime emitter)
        rejected-fixture-records
        (p15-s23-stage2-runtime-executor-rejected-records
         runtime emitter)
	        rejected-record
	        (p15-s23-stage2-runtime-executor-rejected-record
	         rejected-fixture-records)
	        runtime-kernel-artifact
	        (p15-s23-stage2-runtime-kernel-source-artifact source-path)
	        plan-emitter-artifact
	        (p15-s23-stage2-plan-emitter-source-artifact source-path)
        nucleus-artifact
        (p15-s23-stage2-compiler-nucleus-source-artifact source-path)
        accepted-artifact
        (p15-s23-accepted-app-execution-source-artifact source-path)
        rejected-artifact
        (p15-s23-rejected-app-diagnostic-source-artifact source-path)
	        evidence-link-record
	        (p15-s23-stage2-runtime-executor-evidence-link-record
	         runtime-kernel-artifact plan-emitter-artifact
	         nucleus-artifact accepted-artifact
	         rejected-artifact)
        boundary-record
        (p15-s23-stage2-runtime-executor-boundary-record runtime)
        candidate {:runtime-contract runtime
                   :rule-record rule-record
                   :accepted-record accepted-record
                   :rejected-record rejected-record
                   :evidence-link-record evidence-link-record
                   :boundary-record boundary-record}
        diagnostics
        (p15-s23-stage2-runtime-executor-diagnostics source-path
                                                    candidate)
        _ (when (seq diagnostics)
            (p15-s23-stage2-runtime-executor-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :runtime runtime
                       :stage2-plan-id
                       (:stage2-plan-id accepted-record)
                       :runtime-output
                       (:stage2-runtime-output accepted-record)
                       :rejected-diagnostics
                       (:observed-diagnostics rejected-record)})))
        rejected-proof-records
        (p15-s23-stage2-runtime-executor-rejected-proof-records
         source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-stage2-runtime-executor-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-stage2-runtime-executor
         :source-path source-path
         :proof-id proof-id
         :runtime-contract runtime
         :rule-record rule-record
         :accepted-record accepted-record
         :rejected-record rejected-record
         :evidence-link-record evidence-link-record
         :boundary-record boundary-record
	         :linked-artifacts
	         {:stage2-runtime-kernel
	          (select-keys runtime-kernel-artifact
	                       [:kind :artifact-id :proof-id])
	          :stage2-plan-emitter
	          (select-keys plan-emitter-artifact
	                       [:kind :artifact-id :proof-id])
          :stage2-compiler-nucleus
          (select-keys nucleus-artifact [:kind :artifact-id :proof-id])
          :accepted-app-execution-proof
          (select-keys accepted-artifact [:kind :artifact-id :proof-id])
          :rejected-app-diagnostic-proof
          (select-keys rejected-artifact [:kind :artifact-id :proof-id])}
         :full-language-compiler-self-hosted?
         (get-in runtime
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in runtime [:self-hosting-claims
                          :clojure-seed-retired?])
         :accepted-p15-s23-stage2-runtime-executor-fixtures
         [{:fixture p15-s23-accepted-app-source-path
           :status :accepted
           :stage2-plan-id (:stage2-plan-id accepted-record)
           :stdout (:stage2-runtime-output accepted-record)}]
         :verified-p15-s23-stage2-runtime-executor-rejected-fixtures
         rejected-fixture-records
         :rejected-p15-s23-stage2-runtime-executor-fixtures
         rejected-proof-records
         :p15-s23-stage2-runtime-executor-diagnostic-stream
         (p15-s23-stage2-runtime-executor-diagnostic-stream
          source-path proof-id)
         :p15-s23-stage2-runtime-executor-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-fixture-records)
          :internal-rejected-fixtures (count rejected-proof-records)
          :diagnostic-count
          (count p15-s23-stage2-runtime-executor-diagnostic-ids)
          :stage2-plan-id (:stage2-plan-id accepted-record)
          :accepted-output (:stage2-runtime-output accepted-record)
          :rejected-diagnostics (:observed-diagnostics rejected-record)
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-stage2-runtime-executor-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))

(defn p15-s23-stage2-runtime-executor-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-stage2-runtime-executor-fail!
     "P15S23X001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-stage2-runtime-executor-source-artifact path))