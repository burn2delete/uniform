

(defn p15-s23-accepted-app-rejected-candidates
  [accepted-candidate]
  [{:fixture :internal-p15-s23-accepted-app-missing-proof
    :candidate (assoc accepted-candidate :proof-contract {})
    :expected-diagnostic "P15S23A001"}
   {:fixture :internal-p15-s23-accepted-app-fixture-gap
    :candidate (assoc-in accepted-candidate
                         [:accepted-app-artifact :kind]
                         :gravity/wrong-accepted-app-artifact)
    :expected-diagnostic "P15S23A002"}
   {:fixture :internal-p15-s23-accepted-app-output-mismatch
    :candidate (-> accepted-candidate
                   (assoc-in [:accepted-output-comparison
                              :accepted-stdout]
                             "wrong\n")
                   (assoc-in [:accepted-output-comparison
                              :accepted-matches-reference?]
                             false)
                   (assoc-in [:accepted-output-comparison
                              :accepted-matches-expected?]
                             false)
                   (assoc-in [:accepted-output-comparison :status]
                             :failed))
    :expected-diagnostic "P15S23A003"}
   {:fixture :internal-p15-s23-accepted-app-artifact-link-gap
    :candidate (assoc accepted-candidate
                      :runtime-capability-artifact {:kind :wrong})
    :expected-diagnostic "P15S23A004"}
   {:fixture :internal-p15-s23-accepted-app-trusted-boundary-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:trusted-boundary-record
                              :clojure-instruction-runner?]
                             false)
                   (assoc-in [:trusted-boundary-record
                              :self-hosted-compiler?]
                             true))
    :expected-diagnostic "P15S23A005"}
   {:fixture :internal-p15-s23-accepted-app-overclaim
    :candidate (-> accepted-candidate
                   (assoc-in [:proof-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:proof-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true))
    :expected-diagnostic "P15S23A006"}])

(defn p15-s23-accepted-app-rejected-records
  [source-path accepted-candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-accepted-app-proof-diagnostics source-path candidate)})
        (p15-s23-accepted-app-rejected-candidates accepted-candidate)))

(defn p15-s23-accepted-app-execution-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-accepted-app-execution-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-app-execution-fixtures artifact)))
        output-comparison (:accepted-output-comparison artifact)
        execution-trace (:compiled-plan-execution-trace artifact)
        trusted-boundary (:trusted-boundary-record artifact)
        runtime-use (:runtime-capability-use-record artifact)]
    {:accepted-app-execution-proof-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :accepted-fixture-linked?
     (= p15-s23-accepted-app-source-path
        (get-in artifact [:accepted-app-artifact :source :path]))
     :compiled-plan-emitted?
     (true? (:compiled-plan-emitted? execution-trace))
     :compiled-plan-executed?
     (true? (:compiled-plan-executed? execution-trace))
     :accepted-output-matches-reference?
     (true? (:accepted-matches-reference? output-comparison))
     :accepted-output-matches-expected?
     (true? (:accepted-matches-expected? output-comparison))
     :runtime-capability-proof-linked?
     (= :gravity/p15-s23-runtime-manifest-capability-enforcement-artifact
        (get-in artifact [:runtime-capability-artifact :kind]))
     :effects-and-capabilities-recorded?
     (and (= #{:io/write} (:application-effects runtime-use))
          (contains? (:application-capabilities runtime-use) :io/stdout)
          (true? (:effect-capability-check-passed? runtime-use)))
     :trusted-boundaries-explicit?
     (and (true? (:clojure-instruction-runner? trusted-boundary))
          (false? (:self-hosted-compiler? trusted-boundary))
          (false? (:clojure-seed-retired? trusted-boundary)))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset? (set p15-s23-accepted-app-diagnostic-ids)
                  rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-accepted-app-diagnostic-ids) diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :clojure-stage0-compiler? true
      :clojure-instruction-runner? true
      :full-self-hosted-toolchain? false
      :next-required-capability
      :implement_rejected_app_diagnostic_proof}}))