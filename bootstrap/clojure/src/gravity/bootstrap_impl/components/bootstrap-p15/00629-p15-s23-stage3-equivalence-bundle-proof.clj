

(defn p15-s23-stage3-equivalence-bundle-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-stage3-equivalence-bundle-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat
              #(map :diagnostic (:diagnostics %))
              (:rejected-p15-s23-stage3-equivalence-bundle-fixtures
               artifact)))]
    {:stage3-equivalence-bundle-authored-in-gravity? true
     :status :complete
     :task "P15-S23"
     :stage3-equivalence-bundle-present?
     (= :gravity/stage3-equivalence-bundle
        (get-in artifact [:proof-contract :artifact]))
     :stage3-candidate-linked?
     (= :complete (get-in artifact [:bundle-record :status]))
     :accepted-output-equivalent?
     (true? (get-in artifact
                    [:accepted-record :accepted-output-equivalent?]))
     :rejected-diagnostics-equivalent?
     (true? (get-in artifact
                    [:rejected-record
                     :rejected-diagnostics-equivalent?]))
     :rebuild-equivalence-complete?
     (= :complete (get-in artifact [:rebuild-record :status]))
     :conformance-evidence-complete?
     (= :complete (get-in artifact [:conformance-record :status]))
     :evidence-links-covered?
     (true? (get-in artifact
                    [:evidence-link-record
                     :required-links-covered?]))
     :boundary-recorded?
     (= :complete (get-in artifact [:boundary-record :status]))
     :does-not-claim-final-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-final-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset?
      (set p15-s23-stage3-equivalence-bundle-diagnostic-ids)
      rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-stage3-equivalence-bundle-diagnostic-ids)
        diagnostics)
     :limitations
     {:stage3-equivalence-bundle-complete? true
      :full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :final-self-hosted-application-run? false
      :next-required-capability
      :emit_and_run_stage3_self_hosted_application}}))

(defn p15-s23-stage3-equivalence-bundle-source-artifact*
  [source-path]
  (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-stage3-equivalence-bundle)
        candidate-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage3-seedless-compiler-candidate)
        accepted-artifact
        (p15-s23-current-candidate-artifact-evidence
         :accepted-app-execution-proof)
        rejected-artifact
        (p15-s23-current-candidate-artifact-evidence
         :rejected-app-diagnostic-proof)
        rebuild-artifact
        (p15-s23-current-candidate-artifact-evidence
         :reproducible-rebuild-log)
        comparison-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage-comparison-report)
        conformance-artifact
        (p15-s23-current-candidate-artifact-evidence
         :conformance-report)
        provenance-artifact
        (p15-s23-current-candidate-artifact-evidence
         :provenance-attestation)
        tcb-artifact
        (p15-s23-current-candidate-artifact-evidence
         :tcb-delta-record)
        unsafe-artifact
        (p15-s23-current-candidate-artifact-evidence
         :unsafe-audit-report)
        bundle-record
        (p15-s23-stage3-equivalence-bundle-record
         source-path proof-contract)
        accepted-record
        (p15-s23-stage3-equivalence-accepted-record
         candidate-artifact accepted-artifact)
        rejected-record
        (p15-s23-stage3-equivalence-rejected-record
         candidate-artifact rejected-artifact)
        rebuild-record
        (p15-s23-stage3-equivalence-rebuild-record
         candidate-artifact rebuild-artifact comparison-artifact)
        conformance-record
        (p15-s23-stage3-equivalence-conformance-record
         conformance-artifact provenance-artifact tcb-artifact
         unsafe-artifact)
        evidence-link-record
        (p15-s23-stage3-equivalence-bundle-link-record
         candidate-artifact accepted-artifact rejected-artifact
         rebuild-artifact comparison-artifact conformance-artifact
         provenance-artifact tcb-artifact unsafe-artifact)
        boundary-record
        (p15-s23-stage3-equivalence-boundary-record
         proof-contract candidate-artifact)
        candidate {:proof-contract proof-contract
                   :bundle-record bundle-record
                   :accepted-record accepted-record
                   :rejected-record rejected-record
                   :rebuild-record rebuild-record
                   :conformance-record conformance-record
                   :evidence-link-record evidence-link-record
                   :boundary-record boundary-record}
        diagnostics
        (p15-s23-stage3-equivalence-bundle-proof-diagnostics
         source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-stage3-equivalence-bundle-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :proof-contract proof-contract
                       :bundle-record bundle-record
                       :accepted-record accepted-record
                       :rejected-record rejected-record
                       :rebuild-record rebuild-record
                       :conformance-record conformance-record
                       :evidence-link-record evidence-link-record
                       :boundary-record boundary-record})))
        rejected-records
        (p15-s23-stage3-equivalence-bundle-rejected-records
         source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-stage3-equivalence-bundle-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-stage3-equivalence-bundle
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :stage3-seedless-compiler-candidate-artifact
         (select-keys candidate-artifact
                      [:kind :artifact-id :proof-id :accepted-record
                       :rejected-record :boundary-record
                       :capability-based-proof])
         :accepted-app-execution-artifact
         (select-keys accepted-artifact
                      [:kind :artifact-id :proof-id
                       :accepted-output-comparison
                       :capability-based-proof])
         :rejected-app-diagnostic-artifact
         (select-keys rejected-artifact
                      [:kind :artifact-id :proof-id
                       :rejected-app-diagnostic-records
                       :capability-based-proof])
         :reproducible-rebuild-log-artifact
         (select-keys rebuild-artifact
                      [:kind :artifact-id :proof-id
                       :capability-based-proof])
         :stage-comparison-report-artifact
         (select-keys comparison-artifact
                      [:kind :artifact-id :proof-id
                       :capability-based-proof])
         :self-hosting-conformance-report-artifact
         (select-keys conformance-artifact
                      [:kind :artifact-id :proof-id
                       :capability-based-proof])
         :bootstrap-provenance-attestation-artifact
         (select-keys provenance-artifact
                      [:kind :artifact-id :proof-id
                       :capability-based-proof])
         :tcb-delta-record-artifact
         (select-keys tcb-artifact
                      [:kind :artifact-id :proof-id
                       :capability-based-proof])
         :unsafe-audit-report-artifact
         (select-keys unsafe-artifact
                      [:kind :artifact-id :proof-id
                       :capability-based-proof])
         :bundle-record bundle-record
         :accepted-record accepted-record
         :rejected-record rejected-record
         :rebuild-record rebuild-record
         :conformance-record conformance-record
         :evidence-link-record evidence-link-record
         :boundary-record boundary-record
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-stage3-equivalence-bundle-fixtures
         [{:fixture p15-s23-accepted-app-source-path
           :status :accepted
           :stdout (:candidate-output accepted-record)}]
         :rejected-p15-s23-stage3-equivalence-bundle-fixtures
         rejected-records
         :p15-s23-stage3-equivalence-bundle-diagnostic-stream
         (p15-s23-stage3-equivalence-bundle-diagnostic-stream
          source-path proof-id)
         :p15-s23-stage3-equivalence-bundle-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-stage3-equivalence-bundle-diagnostic-ids)
          :accepted-app-output (:candidate-output accepted-record)
          :rejected-diagnostics
          (:candidate-diagnostics rejected-record)
          :rebuild-equivalence-complete?
          (= :complete (:status rebuild-record))
          :conformance-evidence-complete?
          (= :complete (:status conformance-record))
          :full-language-compiler-self-hosted? false
          :clojure-seed-retired? false
          :status :complete}
         :diagnostics []}
        proof (p15-s23-stage3-equivalence-bundle-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))