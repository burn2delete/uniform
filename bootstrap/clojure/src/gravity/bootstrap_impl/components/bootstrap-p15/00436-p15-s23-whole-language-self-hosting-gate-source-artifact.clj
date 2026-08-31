

(defn p15-s23-whole-language-self-hosting-gate-source-artifact*
  [path source-text]
  (let [formal-artifact
        (stage1-reader-formal-release-governance-seed-retirement-source-artifact
         path source-text)
        candidate (p15-s23-current-seed-candidate formal-artifact)
        diagnostics
        (p15-s23-whole-language-self-hosting-gate-diagnostics
         path candidate)
        overclaim-diagnostics
        (p15-s23-whole-language-self-hosting-gate-diagnostics
         path p15-s23-false-self-hosting-overclaim-candidate)
        complete?
        (and (empty? diagnostics)
             (true? (:full-language-compiler-self-hosted? candidate))
             (true? (:clojure-seed-retired? candidate))
             (false? (:clojure-seed-boundary? candidate)))
        artifact-base
        {:kind :gravity/p15-s23-whole-language-self-hosting-gate-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-whole-language-self-hosting-gate
         :source-path path
         :source-id (str "sha256:" (sha256-hex source-text))
         :basis-artifact-id (:artifact-id formal-artifact)
         :basis-task (:task formal-artifact)
         :status (if complete? :complete :incomplete)
         :full-language-compiler-self-hosted?
         (:full-language-compiler-self-hosted? candidate)
         :clojure-seed-retired? (:clojure-seed-retired? candidate)
	         :clojure-seed-boundary? (:clojure-seed-boundary? candidate)
	         :next-required-capability
	         (if complete?
	           :advance_to_phase_16
	           :emit_final_seed_retirement_proof)
         :required-evidence
         p15-s23-whole-language-self-hosting-required-evidence
         :missing-evidence (mapv :evidence-key diagnostics)
         :current-candidate candidate
         :p15-s23-whole-language-self-hosting-diagnostic-stream
         (p15-s23-whole-language-self-hosting-diagnostic-stream
          path candidate diagnostics)
         :rejected-p15-s23-self-hosting-candidates
         [{:fixture :internal-false-self-hosting-overclaim
           :candidate p15-s23-false-self-hosting-overclaim-candidate
           :status :rejected
           :diagnostics overclaim-diagnostics}]
         :diagnostics []
	         :notes
         (if complete?
           ["P15-S23 final seed retirement is complete for the implemented whole-language compiler scope."
            "The final proof records a seedless stage3 compiler path, stage3 equivalence and application execution, release-governance closure, TCB seed-boundary retirement, and provenance closure."
            "The Clojure bootstrap verifier remains recorded only as proof-production host and is not part of the release compiler/runtime boundary."]
           ["P15-S23 is not complete."
            "The stage3 seedless compiler candidate, equivalence bundle, and self-hosted application execution proof remove the Clojure verifier and release-compiler boundary for the candidate compile path, prove equivalence against the current stage, and run the accepted and rejected application fixtures through the stage3 path, but final seed retirement remains blocked until the final release proof is complete."
	            "Do not set :full-language-compiler-self-hosted? or :clojure-seed-retired? true until every required evidence item is present."])}]
    (let [proof (p15-s23-whole-language-self-hosting-gate-proof
                 artifact-base)]
      (assoc artifact-base
             :capability-based-proof proof
	             :artifact-id
             (c4-artifact-id
              (assoc artifact-base :capability-based-proof proof))))))