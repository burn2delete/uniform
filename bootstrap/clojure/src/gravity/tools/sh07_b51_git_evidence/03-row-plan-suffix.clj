
(def ^:private row-plan-suffix
  [
   {:ordinal 10 :id :boot4 :document-ordinals [16]
    :sections [(contract-section
                16 ["## Module Rules" "## Requirements"
                    "## Outputs and Artifacts" "## Conformance Criteria"]
                false)]
    :disposition :applies
    :obligations [:closed-module-manifest :effects-capabilities
                  :deterministic-output :fact-provenance
                  :positive-negative-preservation-tests]
    :binding-specs
    [(contract-binding :boot4-source-closure :runtime :coordinator
                       [:source-closure] [:closed-module-manifest])
     (contract-binding :boot4-host-surface :runtime :coordinator
                       [:host-surface] [:effects-capabilities])
     (contract-binding :boot4-semantic-witness :runtime
                       :semantic-witness-runtime [:witness :boot4]
                       [:effects-capabilities :deterministic-output
                        :fact-provenance
                        :positive-negative-preservation-tests])]
    :test-ids [:sh07-b51-v9-contract-boot4]
    :diagnostic :SH07-B51-FORBIDDEN-VAR}
   {:ordinal 11 :id :boot5 :document-ordinals [17]
    :sections [(contract-section
                17 ["## Matrix Columns" "## Requirements"
                    "## Outputs and Artifacts" "## Conformance Criteria"]
                false)]
    :disposition :preserved
    :obligations [:compatibility-row :compatibility-digest :explicit-gaps
                  :no-stage-advance]
    :binding-specs
    [(contract-binding :boot5-compatibility :pre-evidence
                       :boot5-compatibility []
                       [:compatibility-row :compatibility-digest
                        :explicit-gaps :no-stage-advance])
     (contract-binding :boot5-runtime :runtime :self-hosting-runtime []
                       [:no-stage-advance])]
    :test-ids [:sh07-b51-v9-contract-boot5]
    :diagnostic :SH07-B51-CONTRACT-DISCHARGE}
   {:ordinal 12 :id :boot6-8 :document-ordinals [18 19 20]
    :sections
    (mapv #(contract-section
            % ["## Requirements" "## Outputs and Artifacts"
               "## Rejection Rules" "## Conformance Criteria"] false)
          (range 18 21))
    :disposition :applies
    :obligations [:fresh-processes :stage-comparison :tcb-delta :provenance
                  :cleanup :replay]
    :binding-specs
    [(contract-binding :boot6-8-launcher :runtime :launcher []
                       [:fresh-processes :provenance :cleanup])
     (contract-binding :boot6-8-coordinator :runtime :coordinator []
                       [:fresh-processes :provenance])
     (contract-binding :boot6-8-runtime :runtime :bootstrap-runtime []
                       [:stage-comparison :tcb-delta :provenance :cleanup
                        :replay])
     (contract-binding :boot6-8-i9 :post-i9 :phase-evidence
                       [:carriers :provenance] [:provenance])
     (contract-binding :boot6-8-g9 :post-g9 :phase-evidence
                       [:carriers :provenance] [:provenance])
     (contract-binding :boot6-8-p9 :post-p9 :phase-evidence
                       [:carriers :provenance] [:provenance])]
    :test-ids [:sh07-b51-v9-contract-boot6-8]
    :diagnostic :SH07-B51-CHECKER-PROCESS}
   {:ordinal 13 :id :test10 :document-ordinals [21]
    :sections [(contract-section
                21 ["## Oracles" "## Requirements"
                    "## Outputs and Artifacts" "## Rejection Rules"
                    "## Conformance Criteria"] false)]
    :disposition :applies
    :obligations [:oracle-identities :applicability :execution-context
                  :observables :diagnostics :permitted-divergence
                  :reproducer]
    :binding-specs
    [(contract-binding :test10-runtime :runtime :differential-runtime []
                       [:oracle-identities :applicability :execution-context
                        :observables :diagnostics :permitted-divergence
                        :reproducer])
     (contract-binding :test10-main-i9 :post-i9 :phase-evidence
                       [:carriers :test10-result]
                       [:oracle-identities :applicability])]
    :test-ids [:sh07-b51-v9-contract-test10]
    :diagnostic :SH07-B51-NORMALIZE-DIVERGENCE}
   {:ordinal 14 :id :test11 :document-ordinals [22]
    :sections [(contract-section
                22 ["## Requirements" "## Outputs and Artifacts"
                    "## Rejection Rules" "## Conformance Criteria"] false)]
    :disposition :applies
    :obligations [:assumptions :trusted-basis :checker-input-claim-ids
                  :invalidation :counterexamples :validation-not-proof]
    :binding-specs
    [(contract-binding :test11-proof-scope :pre-evidence :proof-scope []
                       [:validation-not-proof])
     (contract-binding :test11-runtime :runtime :verification-runtime []
                       [:assumptions :trusted-basis
                        :checker-input-claim-ids :invalidation
                        :counterexamples])]
    :test-ids [:sh07-b51-v9-contract-test11]
    :diagnostic :SH07-B51-PROOF-SCOPE}
   {:ordinal 15 :id :test13 :document-ordinals [23]
    :sections [(contract-section
                23 ["## Stages" "## Requirements"
                    "## Outputs and Artifacts" "## Rejection Rules"
                    "## Conformance Criteria"] false)]
    :disposition :preserved
    :obligations [:stage-trusted-inputs :generated-artifacts
                  :source-compiler-lock-target-output-ids :expected-deltas
                  :tcb-delta :no-stage-advance]
    :binding-specs
    [(contract-binding :test13-runtime :runtime :self-hosting-runtime []
                       [:stage-trusted-inputs :generated-artifacts
                        :source-compiler-lock-target-output-ids
                        :expected-deltas :tcb-delta :no-stage-advance])
     (contract-binding :test13-evidence :post-i9 :phase-evidence
                       [:carriers :test13-result] [:generated-artifacts])]
    :test-ids [:sh07-b51-v9-contract-test13]
    :diagnostic :SH07-B51-PROOF-SCOPE}
   {:ordinal 16 :id :sh07-proof-contract :document-ordinals [24]
    :sections [(contract-section 24 [] true)]
    :disposition :applies
    :obligations [:active-scope :fresh-process :nonauthoritative-cache
                  :source-rechecks :catalog-ids :bounds :cleanup]
    :binding-specs
    [(contract-binding :proof-contract-document :pre-evidence
                       :document-read-receipt [:documents 24]
                       [:active-scope :nonauthoritative-cache :bounds])
     (contract-binding :proof-contract-projection :pre-evidence :proof-scope
                       [:proof-contract] [:active-scope])
     (contract-binding :proof-contract-runtime :runtime
                       :verification-runtime []
                       [:fresh-process :source-rechecks :catalog-ids
                        :cleanup])]
    :test-ids [:sh07-b51-v9-proof-contract-scope]
    :diagnostic :SH07-B51-PROOF-SCOPE}
   {:ordinal 17 :id :backlog-ownership :document-ordinals [25 26]
    :sections
    [(contract-section
      25 ["## Countable Backlog" "## Dependency Waves and Parallel Ownership"
          "## Progress Accounting" "## Outputs and Artifacts"
          "## Conformance Criteria"] false)
     (contract-section 26 [] true)]
    :disposition :applies
    :obligations [:partial-progress :allocations :reservations :handoffs
                  :open-exit-gates]
    :binding-specs
    [(contract-binding :backlog-document :pre-evidence
                       :document-read-receipt [:documents 25]
                       [:partial-progress])
     (contract-binding :ownership-plan :pre-evidence :ownership-plan []
                       [:allocations :open-exit-gates])
     (contract-binding :backlog-progress :pre-evidence :progress []
                       [:partial-progress])
     (contract-binding :ownership-i9 :post-i9 :phase-evidence
                       [:carriers :ownership-result]
                       [:reservations :handoffs])
     (contract-binding :ownership-g9 :post-g9 :phase-evidence
                       [:carriers :ownership-result]
                       [:reservations :handoffs])
     (contract-binding :ownership-p9 :post-p9 :phase-evidence
                       [:carriers :ownership-result]
                       [:reservations :handoffs])]
    :test-ids [:sh07-b51-v9-backlog-ownership]
    :diagnostic :SH07-B51-OWNER}
   {:ordinal 18 :id :catalog-checked-core-governance
    :document-ordinals [27 28 29 30 31 32]
    :sections
    [(contract-section 27 [] true)
     (contract-section 28 [] true)
     (contract-section 29 ["## Lifecycle" "## Admission"] false)
     (contract-section 30 [] true)
     (contract-section 31 [] true)
     (contract-section 32 [] true)]
    :disposition :applies
    :obligations [:catalog-schema-authority :checked-core-revision
                  :lifecycle-transitions :review-admission-receipts
                  :final-cleanup]
    :binding-specs
    [(contract-binding :catalog-document :pre-evidence
                       :document-read-receipt [:documents 27]
                       [:catalog-schema-authority])
     (contract-binding :catalog-projection :pre-evidence
                       :catalog-projection [] [:catalog-schema-authority])
     (contract-binding :governance-rules :pre-evidence :governance-rules []
                       [:lifecycle-transitions])
     (contract-binding :checked-core-source :runtime :source-session-m
                       [:source-descriptors 2] [:checked-core-revision])
     (contract-binding :catalog-launcher-cleanup :runtime :launcher
                       [:cleanup] [:final-cleanup])
     (contract-binding :governance-i9 :post-i9 :phase-evidence
                       [:carriers :governance-result]
                       [:review-admission-receipts])
     (contract-binding :governance-g9 :post-g9 :phase-evidence
                       [:carriers :governance-result]
                       [:review-admission-receipts])
     (contract-binding :governance-p9 :post-p9 :phase-evidence
                       [:carriers :governance-results]
                       [:review-admission-receipts :final-cleanup])]
    :test-ids [:sh07-b51-v9-catalog-governance]
    :diagnostic :SH07-B51-VERIFY-CATALOG}])

(def row-plan (into row-plan-prefix row-plan-suffix))

(def row-plan-id
  (codec/H [:gravity/sh07-b51-contract-row-plan-id-v1 row-plan]))

(def phases
  [:pre-evidence :runtime :post-i9 :post-g9 :post-p9])

(def planned-binding-count
  (reduce + (map #(count (:binding-specs %)) row-plan)))

(defn- context-program
  [row phase obligation]
  [:and
   [:equal [:lookup :context [:row-ordinal]] [:literal (:ordinal row)]]
   [:equal [:lookup :context [:row-id]] [:literal (:id row)]]
   [:equal [:lookup :context [:phase]] [:literal phase]]
   [:equal [:lookup :context [:obligation]] [:literal obligation]]])

(defn- subject-program
  [row phase binding subject-index]
  [:and
   [:exact-keys [:lookup :subjects [subject-index]]
    [:literal [:row-ordinal :id :phase :carrier :path :obligations
               :value :value-id]]]
   [:equal [:lookup :subjects [subject-index :row-ordinal]]
    [:literal (:ordinal row)]]
   [:equal [:lookup :subjects [subject-index :id]]
    [:literal (:id binding)]]
   [:equal [:lookup :subjects [subject-index :phase]] [:literal phase]]
   [:equal [:lookup :subjects [subject-index :carrier]]
    [:literal (:carrier binding)]]
   [:equal [:lookup :subjects [subject-index :path]]
    [:literal (:path binding)]]
   [:equal [:lookup :subjects [subject-index :obligations]]
    [:literal (:obligations binding)]]
   [:equal [:lookup :subjects [subject-index :value]]
    [:lookup :carriers
     (into [(:carrier binding)] (:path binding))]]
   [:h-equal [:literal :gravity/sh07-b51-contract-binding-v2]
    [:lookup :subjects [subject-index]]
    [:lookup :subjects [subject-index :value-id]]
    [:literal [[:value-id]]]]])
