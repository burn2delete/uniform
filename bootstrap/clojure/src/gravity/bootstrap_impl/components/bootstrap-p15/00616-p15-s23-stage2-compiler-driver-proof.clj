

(defn p15-s23-stage2-compiler-driver-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-stage2-compiler-driver-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-stage2-compiler-driver-fixtures
                      artifact)))
        accepted-record (:accepted-record artifact)
        rejected-record (:rejected-record artifact)
        boundary-record (:boundary-record artifact)]
    {:stage2-compiler-driver-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :stage2-compiler-driver-present?
     (= :gravity/stage2-compiler-driver
        (get-in artifact [:driver-contract :artifact]))
     :stage2-compiler-driver-executed?
     (true? (:stage2-compiler-driver-executed?
             boundary-record))
     :stage0-compiler-driver-replaced?
     (true? (:stage0-compiler-driver-replaced?
             boundary-record))
     :stage0-rule-runner-replaced?
     (true? (:stage0-rule-runner-replaced? boundary-record))
     :stage0-reader-replaced?
     (true? (:stage0-reader-replaced? boundary-record))
     :stage0-macro-expander-replaced?
     (true? (:stage0-macro-expander-replaced? boundary-record))
     :stage2-front-end-executor-used?
     (true? (:stage2-front-end-executor-used? boundary-record))
     :stage2-front-end-host-replaced?
     (true? (:stage2-front-end-host-replaced? boundary-record))
	     :stage2-source-front-end-used?
	     (true? (:stage2-source-front-end-used? boundary-record))
	     :stage2-runtime-kernel-used?
	     (true? (:stage2-runtime-kernel-used? boundary-record))
	     :stage2-runtime-host-replaced?
	     (true? (:stage2-runtime-host-replaced? boundary-record))
	     :stage2-runtime-primitives-replaced?
	     (true? (:stage2-runtime-primitives-replaced?
	             boundary-record))
	     :gravity-runtime-primitives-used?
	     (true? (:gravity-runtime-primitives? boundary-record))
	     :stage2-plan-emitted?
	     (true? (:stage2-plan-emitted? accepted-record))
     :stage2-runtime-executed?
     (true? (:stage2-runtime-executed? accepted-record))
     :accepted-output-equivalent?
     (true? (:accepted-output-equivalent? accepted-record))
     :function-instructions-equivalent?
     (true? (:function-instructions-equivalent? accepted-record))
     :rejected-diagnostics-equivalent?
     (= :complete (:status rejected-record))
     :residual-clojure-driver-host-recorded?
     (true? (:clojure-stage0-driver-host? boundary-record))
	     :does-not-use-clojure-stage2-front-end-host?
	     (false? (:clojure-stage2-front-end-host? boundary-record))
	     :does-not-use-clojure-stage0-runtime-host?
	     (false? (:clojure-stage0-runtime-host? boundary-record))
	     :does-not-use-clojure-runtime-primitives?
	     (false? (:clojure-host-primitive-boundary? boundary-record))
     :does-not-use-clojure-stage0-rule-runner?
     (false? (:clojure-stage0-rule-runner? boundary-record))
     :does-not-use-clojure-stage0-reader?
     (false? (:clojure-stage0-reader? boundary-record))
     :does-not-use-clojure-stage0-macro-expander?
     (false? (:clojure-stage0-macro-expander? boundary-record))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (= (set p15-s23-stage2-compiler-driver-diagnostic-ids)
        rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-stage2-compiler-driver-diagnostic-ids)
        diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :stage2-compiler-driver-authored-in-gravity? true
      :stage2-compiler-driver-executed? true
      :stage0-compiler-driver-replaced? true
      :stage0-rule-runner-replaced? true
      :stage0-reader-replaced? true
      :stage0-macro-expander-replaced? true
	      :stage2-front-end-executor-used? true
	      :stage2-front-end-host-replaced? true
	      :stage2-source-front-end-used? true
	      :stage2-runtime-kernel-used? true
	      :stage2-runtime-host-replaced? true
	      :stage2-runtime-primitives-replaced? true
	      :gravity-runtime-primitives? true
	      :clojure-stage0-driver-host? true
      :clojure-stage0-reader? false
	      :clojure-stage0-macro-expander? false
	      :clojure-stage2-front-end-host? false
	      :clojure-stage0-runtime-host? false
	      :clojure-host-primitive-boundary? false
	      :clojure-stage0-rule-runner? false
	      :full-self-hosted-toolchain? false
	      :next-required-capability
	      :implement_whole_language_compiler_stage_without_clojure_seed}}))