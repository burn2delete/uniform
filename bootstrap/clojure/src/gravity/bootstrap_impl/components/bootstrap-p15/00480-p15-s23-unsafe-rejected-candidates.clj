

(defn p15-s23-unsafe-rejected-candidates
  [accepted-candidate]
  [{:fixture :internal-p15-s23-unsafe-missing-report
    :candidate (assoc accepted-candidate :proof-contract {})
    :expected-diagnostic "P15S23U001"}
   {:fixture :internal-p15-s23-unsafe-inventory-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:unsafe-island-index
                              :unsafe-forms-scanned?]
                             false)
                   (assoc-in [:unsafe-island-index
                              :unaudited-unsafe-form-count]
                             1)
                   (assoc-in [:unsafe-island-index :status] :failed))
    :expected-diagnostic "P15S23U002"}
   {:fixture :internal-p15-s23-unsafe-wrapper-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:safe-wrapper-boundary-table
                              :safe-wrapper-coverage-complete?]
                             false)
                   (assoc-in [:safe-wrapper-boundary-table
                              :missing-safe-wrapper-islands]
                             [:p15-s23-unaudited-unsafe-1])
                   (assoc-in [:safe-wrapper-boundary-table :status] :failed))
    :expected-diagnostic "P15S23U003"}
   {:fixture :internal-p15-s23-unsafe-package-metadata-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:package-safety-metadata
                              :schema-validated?]
                             false)
                   (assoc-in [:package-safety-metadata
                              :review-state]
                             :unreviewed)
                   (assoc-in [:package-safety-metadata :status] :failed))
    :expected-diagnostic "P15S23U004"}
   {:fixture :internal-p15-s23-unsafe-stale-review
    :candidate (-> accepted-candidate
                   (assoc-in [:review-and-revalidation-record :stale?]
                             true)
                   (assoc-in [:review-and-revalidation-record
                              :revalidation-triggers]
                             [])
                   (assoc-in [:review-and-revalidation-record :status]
                             :failed))
    :expected-diagnostic "P15S23U005"}
   {:fixture :internal-p15-s23-unsafe-evidence-link-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:unsafe-evidence-link-table
                              :required-links-covered?]
                             false)
                   (assoc-in [:unsafe-evidence-link-table :status]
                             :failed))
    :expected-diagnostic "P15S23U006"}
   {:fixture :internal-p15-s23-unsafe-overclaim
    :candidate (-> accepted-candidate
                   (assoc-in [:proof-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:proof-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true)
                   (assoc-in [:unsafe-audit-report
                              :release-eligible?]
                             true))
    :expected-diagnostic "P15S23U007"}])

(defn p15-s23-unsafe-rejected-records
  [source-path accepted-candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-unsafe-proof-diagnostics source-path candidate)})
        (p15-s23-unsafe-rejected-candidates accepted-candidate)))

(defn p15-s23-unsafe-audit-report-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-unsafe-audit-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-unsafe-audit-fixtures artifact)))]
    {:unsafe-audit-report-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :unsafe-island-index-present?
     (= :complete (get-in artifact [:unsafe-island-index :status]))
     :no-gravity-unsafe-islands?
     (zero? (get-in artifact [:unsafe-island-index
                              :unsafe-island-count]))
     :unsafe-operation-inventory-complete?
     (= :complete (get-in artifact [:unsafe-operation-inventory
                                    :status]))
     :safe-wrapper-boundaries-complete?
     (true?
      (get-in artifact [:safe-wrapper-boundary-table
                        :safe-wrapper-coverage-complete?]))
     :package-safety-metadata-complete?
     (and (= :complete (get-in artifact [:package-safety-metadata
                                         :status]))
          (true? (get-in artifact [:package-safety-metadata
                                   :schema-validated?])))
     :review-current?
     (and (= :complete (get-in artifact
                               [:review-and-revalidation-record :status]))
          (false? (get-in artifact
                          [:review-and-revalidation-record :stale?])))
     :required-evidence-links-covered?
     (true? (get-in artifact [:unsafe-evidence-link-table
                              :required-links-covered?]))
     :external-seed-boundaries-separated?
     (true?
      (get-in artifact [:external-seed-boundary-audit
                        :host-trust-boundaries-not-counted-as-safe-gravity?]))
     :does-not-claim-release-eligibility?
     (false? (get-in artifact [:unsafe-audit-report
                               :release-eligible?]))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset? (set p15-s23-unsafe-diagnostic-ids)
                  rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-unsafe-diagnostic-ids) diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :current-candidate-is-clojure-seed? true
      :release-eligible? false
      :external-host-boundaries-still-trusted? true
      :next-required-capability
      :implement_whole_language_compiler_artifact}}))