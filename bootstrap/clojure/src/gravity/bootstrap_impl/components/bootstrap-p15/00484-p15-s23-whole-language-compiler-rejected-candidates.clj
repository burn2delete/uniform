

(defn p15-s23-whole-language-compiler-rejected-candidates
  [accepted-candidate]
  [{:fixture :internal-p15-s23-whole-language-compiler-missing-artifact
    :candidate (assoc accepted-candidate :proof-contract {})
    :expected-diagnostic "P15S23W001"}
   {:fixture :internal-p15-s23-whole-language-compiler-evidence-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:compiler-evidence-link-table
                              :required-links-covered?]
                             false)
                   (assoc-in [:compiler-evidence-link-table :status]
                             :failed))
    :expected-diagnostic "P15S23W002"}
   {:fixture :internal-p15-s23-whole-language-compiler-stage-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:stage-support-matrix :status] :failed)
                   (assoc-in [:accepted-application-compile-record
                              :output-matches?]
                             false)
                   (assoc-in [:accepted-application-compile-record :status]
                             :failed))
    :expected-diagnostic "P15S23W003"}
   {:fixture :internal-p15-s23-whole-language-compiler-provenance-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:compiler-artifact-lineage-record :status]
                             :failed)
                   (assoc-in [:compiler-artifact-lineage-record
                              :lineage-traversable-to-seed?]
                             false)
                   (assoc-in [:stage-comparison-report-artifact
                              :capability-based-proof
                              :current-candidate-equivalent-to-seed?]
                             false))
    :expected-diagnostic "P15S23W004"}
   {:fixture :internal-p15-s23-whole-language-compiler-overclaim
    :candidate (-> accepted-candidate
                   (assoc-in [:proof-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:proof-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true)
                   (assoc-in [:residual-trusted-boundary-record
                              :clojure-stage0-still-required?]
                             false)
                   (assoc-in [:residual-trusted-boundary-record :status]
                             :failed))
    :expected-diagnostic "P15S23W005"}
   {:fixture :internal-p15-s23-whole-language-compiler-diagnostic-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:rejected-application-diagnostic-record
                              :diagnostics-match-expected?]
                             false)
                   (assoc-in [:rejected-application-diagnostic-record
                              :diagnostic-codes-stable?]
                             false)
                   (assoc-in [:rejected-application-diagnostic-record
                              :status]
                             :failed))
    :expected-diagnostic "P15S23W006"}])

(defn p15-s23-whole-language-compiler-rejected-records
  [source-path accepted-candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-whole-language-compiler-proof-diagnostics
            source-path candidate)})
        (p15-s23-whole-language-compiler-rejected-candidates
         accepted-candidate)))

(defn p15-s23-whole-language-compiler-artifact-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-whole-language-compiler-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-whole-language-compiler-fixtures
                      artifact)))]
    {:whole-language-compiler-artifact-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :compiler-artifact-emitted?
     (boolean
      (re-find #"^sha256:"
               (str (get-in artifact [:compiler-artifact-manifest
                                      :compiler-artifact-id]))))
     :current-claimed-subset-compiled?
     (= :complete (get-in artifact
                          [:accepted-application-compile-record
                           :status]))
     :accepted-application-ran?
     (true? (get-in artifact
                    [:accepted-application-compile-record
                     :output-matches?]))
     :rejected-applications-fail-closed?
     (true? (get-in artifact
                    [:rejected-application-diagnostic-record
                     :diagnostic-codes-stable?]))
     :evidence-links-covered?
     (true? (get-in artifact
                    [:compiler-evidence-link-table
                     :required-links-covered?]))
     :reproducible-rebuild-linked?
     (true? (get-in artifact
                    [:reproducible-rebuild-log-artifact
                     :artifact-identity-comparison
                     :all-artifact-identities-match?]))
     :stage-comparison-linked?
     (true? (get-in artifact
                    [:stage-comparison-report-artifact
                     :capability-based-proof
                     :current-candidate-equivalent-to-seed?]))
     :conformance-linked?
     (true? (get-in artifact
                    [:self-hosting-conformance-report-artifact
                     :stage-support-conformance-record
                     :stage-support-conformant?]))
     :provenance-linked?
     (true? (get-in artifact
                    [:compiler-artifact-lineage-record
                     :lineage-traversable-to-seed?]))
     :tcb-and-unsafe-linked?
     (and (= :gravity/p15-s23-tcb-delta-record-artifact
             (get-in artifact [:tcb-delta-record-artifact :kind]))
          (= :gravity/p15-s23-unsafe-audit-report-artifact
             (get-in artifact [:unsafe-audit-report-artifact :kind])))
     :residual-clojure-boundary-recorded?
     (true? (get-in artifact
                    [:residual-trusted-boundary-record
                     :clojure-stage0-still-required?]))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset? (set p15-s23-whole-language-compiler-diagnostic-ids)
                  rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-whole-language-compiler-diagnostic-ids)
        diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :current-stage-compiler-artifact? true
      :residual-clojure-stage0-boundary? true
      :release-eligible? false
      :next-required-capability
      :complete_governance_and_package_release_record}}))