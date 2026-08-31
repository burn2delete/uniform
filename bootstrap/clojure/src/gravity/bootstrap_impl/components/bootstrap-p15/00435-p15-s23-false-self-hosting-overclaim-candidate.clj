

(def p15-s23-false-self-hosting-overclaim-candidate
  {:candidate-id :false-full-self-hosting-overclaim
   :scope :whole-language-compiler
   :full-language-compiler-self-hosted? true
   :clojure-seed-retired? true
   :clojure-seed-boundary? true
   :evidence {}})

(defn p15-s23-whole-language-self-hosting-gate-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-whole-language-self-hosting-diagnostic-stream
                           :diagnostics])))
        overclaim-diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:rejected-p15-s23-self-hosting-candidates 0
                           :diagnostics])))
        missing-evidence (set (:missing-evidence artifact))
        complete?
        (and (empty? diagnostics)
             (empty? missing-evidence)
             (true? (:full-language-compiler-self-hosted? artifact))
             (true? (:clojure-seed-retired? artifact))
             (false? (:clojure-seed-boundary? artifact)))]
    {:gate-active? true
     :status (if complete? :complete :incomplete)
     :task "P15-S23"
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :full-self-hosting-claim-supported? complete?
     :clojure-seed-retirement-claim-supported? complete?
     :required-evidence-enumerated?
     (= (count p15-s23-whole-language-self-hosting-required-evidence)
        (count (:required-evidence artifact)))
     :missing-evidence-recorded?
     (if complete?
       (empty? missing-evidence)
       (set/subset? #{:clojure-seed-retired} missing-evidence))
     :governance-package-release-record-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :governance-and-package-release-record]))
     :stage2-compiler-nucleus-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :stage2-compiler-nucleus]))
	     :stage2-plan-emitter-present?
	     (p15-s23-evidence-present?
	      (get-in artifact
	              [:current-candidate :evidence
	               :stage2-plan-emitter]))
	     :stage2-runtime-kernel-present?
	     (p15-s23-evidence-present?
	      (get-in artifact
	              [:current-candidate :evidence
	               :stage2-runtime-kernel]))
	     :stage2-runtime-executor-present?
	     (p15-s23-evidence-present?
	      (get-in artifact
              [:current-candidate :evidence
               :stage2-runtime-executor]))
     :stage2-front-end-executor-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :stage2-front-end-executor]))
     :stage2-source-front-end-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :stage2-source-front-end]))
	     :stage2-compiler-driver-present?
	     (p15-s23-evidence-present?
	      (get-in artifact
	              [:current-candidate :evidence
	               :stage2-compiler-driver]))
	     :stage2-whole-language-compiler-present?
	     (p15-s23-evidence-present?
	      (get-in artifact
	              [:current-candidate :evidence
	               :stage2-whole-language-compiler]))
     :stage3-seedless-compiler-candidate-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :stage3-seedless-compiler-candidate]))
     :stage3-equivalence-bundle-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :stage3-equivalence-bundle]))
     :stage3-self-hosted-application-execution-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :stage3-self-hosted-application-execution]))
     :final-seed-retirement-proof-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :final-seed-retirement-proof]))
	     :whole-language-compiler-artifact-present?
	     (p15-s23-evidence-present?
	      (get-in artifact
	              [:current-candidate :evidence
               :whole-language-compiler-artifact]))
     :compiler-pipeline-manifest-proof-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence :compiler-pipeline-manifest]))
     :source-syntax-serialization-proof-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :source-unit-and-syntax-serialization-proof]))
     :core-lowering-diagnostic-preservation-report-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :core-lowering-and-diagnostic-preservation-report]))
     :runtime-capability-proof-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :runtime-manifest-and-capability-enforcement-report]))
     :accepted-app-proof-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :accepted-app-execution-proof]))
     :rejected-app-proof-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :rejected-app-diagnostic-proof]))
     :rebuild-log-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :reproducible-rebuild-log]))
     :stage-comparison-report-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :stage-comparison-report]))
	     :conformance-report-present?
	     (p15-s23-evidence-present?
	      (get-in artifact
	              [:current-candidate :evidence
	               :conformance-report]))
     :provenance-attestation-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :provenance-attestation]))
     :tcb-delta-record-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :tcb-delta-record]))
     :unsafe-audit-report-present?
     (p15-s23-evidence-present?
      (get-in artifact
              [:current-candidate :evidence
               :unsafe-audit-report]))
     :false-self-hosting-overclaim-rejected?
     (contains? overclaim-diagnostics "P15S23016")
     :seed-retirement-overclaim-rejected?
     (contains? overclaim-diagnostics "P15S23014")
     :diagnostics-covered?
     (if complete?
       (empty? diagnostics)
       (set/subset? #{"P15S23014"} diagnostics))
     :limitations
     {:full-language-compiler-self-hosted? complete?
      :clojure-seed-retired? complete?
      :nontrivial-gravity-app-through-current-compiled-path? true
      :whole-language-compiler-artifact-present? true
	      :governance-package-release-record-present? true
	      :stage2-compiler-nucleus-present? true
	      :stage2-plan-emitter-present? true
	      :stage2-runtime-kernel-present? true
	      :stage2-runtime-executor-present? true
	      :stage2-front-end-executor-present? true
		      :stage2-source-front-end-present? true
      :stage2-compiler-driver-present? true
      :stage2-whole-language-compiler-present? true
      :stage3-seedless-compiler-candidate-present? true
      :stage3-equivalence-bundle-present? true
      :stage3-self-hosted-application-execution-present? true
      :stage3-self-hosted-application-run? true
      :final-seed-retirement-proof-present? complete?
      :full-self-hosted-toolchain? complete?
      :next-required-capability
      (if complete?
        :advance_to_phase_16
        :emit_final_seed_retirement_proof)}}))