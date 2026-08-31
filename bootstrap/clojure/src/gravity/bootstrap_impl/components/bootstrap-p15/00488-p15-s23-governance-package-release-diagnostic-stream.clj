

(defn p15-s23-governance-package-release-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-governance-package-release-diagnostic-stream
   :stage :p15-s23-governance-and-package-release-record
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic id
            :message
            (get p15-s23-governance-package-release-diagnostic-messages id)
            :severity :error
            :stage :p15-s23-governance-and-package-release-record})
         p15-s23-governance-package-release-diagnostic-ids)
   :status :complete})

(defn p15-s23-governance-package-release-rejected-candidates
  [accepted-candidate]
  [{:fixture :internal-p15-s23-governance-package-missing-record
    :candidate (assoc accepted-candidate :proof-contract nil)
    :expected-diagnostic "P15S23L001"}
   {:fixture :internal-p15-s23-governance-package-rfc-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:rfc-record
                              :rfc-traceable-to-implementation?]
                             false)
                   (assoc-in [:rfc-record :review-gates-complete?]
                             false))
    :expected-diagnostic "P15S23L002"}
   {:fixture :internal-p15-s23-governance-package-metadata-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:package-release-record :sbom-present?]
                             false)
                   (assoc-in [:package-release-record
                              :capability-manifest-present?]
                             false))
    :expected-diagnostic "P15S23L003"}
   {:fixture :internal-p15-s23-governance-package-reproducibility-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:reproducible-release-record
                              :rebuild-verification-passed?]
                             false)
                   (assoc-in [:release-provenance-link
                              :compiler-lineage-traversable?]
                             false))
    :expected-diagnostic "P15S23L004"}
   {:fixture :internal-p15-s23-governance-package-registry-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:registry-policy-decision
                              :package-policy-satisfied?]
                             false)
                   (assoc-in [:registry-policy-decision
                              :dependency-confusion-risk]
                             :unmitigated))
    :expected-diagnostic "P15S23L005"}
   {:fixture :internal-p15-s23-governance-package-release-overclaim
    :candidate (-> accepted-candidate
                   (assoc-in [:proof-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:proof-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true)
                   (assoc-in [:release-decision-record
                              :release-eligible?]
                             true)
                   (assoc-in [:governance-and-package-release-record
                              :release-eligible?]
                             true))
    :expected-diagnostic "P15S23L006"}
   {:fixture :internal-p15-s23-governance-package-auditor-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:auditor-query-record
                              :all-queries-passed?]
                             false)
                   (assoc-in [:auditor-query-record
                              :package-metadata-passed?]
                             false))
    :expected-diagnostic "P15S23L007"}])

(defn p15-s23-governance-package-release-rejected-records
  [source-path accepted-candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-governance-package-release-proof-diagnostics
            source-path candidate)})
        (p15-s23-governance-package-release-rejected-candidates
         accepted-candidate)))

(defn p15-s23-governance-package-release-record-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-governance-package-release-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-governance-package-release-fixtures
                      artifact)))]
    {:governance-package-release-record-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :rfc-traceability-complete?
     (true? (get-in artifact [:rfc-record
                              :rfc-traceable-to-implementation?]))
     :rfc-review-gates-complete?
     (true? (get-in artifact [:rfc-record :review-gates-complete?]))
     :package-metadata-complete?
     (and (true? (get-in artifact [:package-release-record
                                   :identity-complete?]))
          (true? (get-in artifact [:package-release-record
                                   :provenance-present?]))
          (true? (get-in artifact [:package-release-record
                                   :sbom-present?]))
          (true? (get-in artifact [:package-release-record
                                   :signature-present?]))
          (true? (get-in artifact [:package-release-record
                                   :capability-manifest-present?]))
          (true? (get-in artifact [:package-release-record
                                   :conformance-report-present?])))
     :reproducible-release-evidence-complete?
     (true? (get-in artifact [:reproducible-release-record
                              :rebuild-verification-passed?]))
     :boot8-provenance-linked?
     (true? (get-in artifact [:release-provenance-link
                              :compiler-lineage-traversable?]))
     :registry-policy-decision-complete?
     (and (true? (get-in artifact [:registry-policy-decision
                                   :package-policy-satisfied?]))
          (= :blocked-until-seed-retirement
             (get-in artifact [:registry-policy-decision
                               :decision])))
     :governance-and-package-policy-satisfied?
     (true? (get-in artifact [:release-decision-record
                              :governance-and-package-policy-satisfied?]))
     :release-blocked-by-seed-retirement?
     (= [:clojure-seed-retired]
        (get-in artifact [:release-decision-record :release-blockers]))
     :does-not-claim-release-eligibility?
     (false? (get-in artifact [:release-decision-record
                               :release-eligible?]))
     :does-not-claim-registry-publication?
     (false? (get-in artifact [:registry-policy-decision
                               :registry-publication-eligible?]))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :auditor-queries-passed?
     (true? (get-in artifact [:auditor-query-record
                              :all-queries-passed?]))
     :rejected-candidates-covered?
     (set/subset?
      (set p15-s23-governance-package-release-diagnostic-ids)
      rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-governance-package-release-diagnostic-ids)
        diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :release-eligible? false
      :registry-publication-eligible? false
      :governance-and-package-policy-satisfied? true
      :release-blockers [:clojure-seed-retired]
      :next-required-capability :retire_clojure_seed_boundary}}))