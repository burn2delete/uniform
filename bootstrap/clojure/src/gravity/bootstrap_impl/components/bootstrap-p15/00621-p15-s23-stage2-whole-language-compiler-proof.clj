

(defn p15-s23-stage2-whole-language-compiler-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-stage2-whole-language-compiler-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat
              #(map :diagnostic (:diagnostics %))
              (:rejected-p15-s23-stage2-whole-language-compiler-fixtures
               artifact)))]
    {:stage2-whole-language-compiler-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :stage2-whole-language-compiler-present?
     (= :gravity/stage2-whole-language-compiler
        (get-in artifact [:proof-contract :artifact]))
     :source-subset-covered?
     (= :complete (get-in artifact [:source-record :status]))
     :stage2-driver-linked?
     (= :complete (get-in artifact [:stage-record :status]))
     :stage2-compiler-driver-executed?
     (true? (get-in artifact
                    [:boundary-record
                     :stage2-compiler-driver-executed?]))
     :stage2-runtime-kernel-used?
     (true? (get-in artifact
                    [:boundary-record :stage2-runtime-kernel-used?]))
     :accepted-output-equivalent?
     (true? (get-in artifact
                    [:accepted-record
                     :stage2-output-equivalent-to-current-stage?]))
     :rejected-diagnostics-equivalent?
     (true? (get-in artifact
                    [:rejected-record
                     :diagnostics-equivalent-to-current-stage?]))
     :evidence-links-covered?
     (true? (get-in artifact
                    [:evidence-link-record
                     :required-links-covered?]))
     :boundary-recorded?
     (= :complete (get-in artifact [:boundary-record :status]))
     :residual-clojure-verifier-recorded?
     (true? (get-in artifact
                    [:boundary-record :clojure-stage0-verifier?]))
     :residual-clojure-release-compiler-recorded?
     (true? (get-in artifact
                    [:boundary-record
                     :clojure-stage0-release-compiler?]))
     :does-not-use-clojure-stage0-runtime-host?
     (false? (get-in artifact
                     [:boundary-record
                      :clojure-stage0-runtime-host?]))
     :does-not-use-clojure-runtime-primitives?
     (false? (get-in artifact
                     [:boundary-record
                      :clojure-host-primitive-boundary?]))
     :gravity-runtime-primitives-used?
     (true? (get-in artifact
                    [:boundary-record :gravity-runtime-primitives?]))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset?
      (set p15-s23-stage2-whole-language-compiler-diagnostic-ids)
      rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-stage2-whole-language-compiler-diagnostic-ids)
        diagnostics)
     :limitations
     {:stage2-whole-language-compiler-stage-present? true
      :accepted-app-through-stage2-whole-language-stage? true
      :rejected-app-through-stage2-whole-language-stage? true
      :stage2-compiler-driver-executed? true
      :stage2-runtime-kernel-used? true
      :clojure-stage0-runtime-host? false
      :clojure-host-primitive-boundary? false
      :gravity-runtime-primitives? true
      :clojure-stage0-verifier? true
      :clojure-stage0-release-compiler? true
      :full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :next-required-capability
      :prove_whole_language_stage_equivalence_and_run_self_hosted_app}}))