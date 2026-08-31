

(defn p15-s23-tcb-delta-record-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-tcb-delta-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-tcb-delta-fixtures artifact)))]
    {:tcb-delta-record-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :baseline-inventory-present?
     (= :complete (get-in artifact [:baseline-tcb-inventory :status]))
     :current-inventory-present?
     (= :complete (get-in artifact [:current-tcb-inventory :status]))
     :delta-classification-complete?
     (true?
      (get-in artifact [:tcb-delta-classification
                        :classification-complete?]))
     :residual-trust-boundaries-recorded?
     (empty?
      (get-in artifact [:residual-trust-boundary-record
                        :missing-required-residual-boundaries]))
     :clojure-seed-still-in-tcb?
     (true?
      (get-in artifact [:residual-trust-boundary-record
                        :clojure-seed-still-trusted?]))
     :required-evidence-links-covered?
     (true?
      (get-in artifact [:tcb-evidence-link-table
                        :required-links-covered?]))
     :no-unaccounted-trusted-components?
     (true?
      (get-in artifact [:tcb-auditor-query-record
                        :no-unaccounted-trusted-components?]))
     :whole-language-tcb-reduced?
     (true?
      (get-in artifact [:trust-reduction-summary
                        :whole-language-tcb-reduced?]))
     :does-not-claim-whole-language-tcb-reduction?
     (false?
      (get-in artifact [:trust-reduction-summary
                        :whole-language-tcb-reduced?]))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset? (set p15-s23-tcb-diagnostic-ids)
                  rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-tcb-diagnostic-ids) diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :current-candidate-is-clojure-seed? true
      :whole-language-tcb-reduced? false
      :full-self-hosted-toolchain? false
      :next-required-capability
      :implement_unsafe_audit_report}}))

(defn p15-s23-tcb-delta-record-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :tcb-delta-record source-path
   (fn [] (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-tcb-delta-record)
        inventory-artifact
        (p15-s23-compiler-source-inventory-source-artifact source-path)
        pipeline-artifact
        (p15-s23-compiler-pipeline-manifest-source-artifact source-path)
        runtime-artifact
        (p15-s23-runtime-manifest-capability-enforcement-source-artifact
         source-path)
        stage-artifact
        (p15-s23-stage-comparison-report-source-artifact source-path)
        conformance-artifact
        (p15-s23-self-hosting-conformance-report-source-artifact source-path)
        provenance-artifact
        (p15-s23-provenance-attestation-source-artifact source-path)
        baseline
        (p15-s23-tcb-baseline-inventory source-path provenance-artifact)
        current
        (p15-s23-tcb-current-inventory
         source-path source-data inventory-artifact pipeline-artifact
         runtime-artifact stage-artifact conformance-artifact
         provenance-artifact)
        classification
        (p15-s23-tcb-delta-classification baseline current)
        residual
        (p15-s23-tcb-residual-trust-boundary-record current)
        links
        (p15-s23-tcb-evidence-link-table
         inventory-artifact runtime-artifact stage-artifact
         conformance-artifact provenance-artifact)
        summary
        (p15-s23-tcb-trust-reduction-summary
         baseline current classification residual)
        auditor
        (p15-s23-tcb-auditor-query-record
         baseline current classification residual links)
        tcb-record
        (p15-s23-tcb-delta-record
         source-path baseline current classification residual
         links summary auditor)
        candidate {:proof-contract proof-contract
                   :baseline-tcb-inventory baseline
                   :current-tcb-inventory current
                   :tcb-delta-classification classification
                   :residual-trust-boundary-record residual
                   :tcb-evidence-link-table links
                   :trust-reduction-summary summary
                   :tcb-auditor-query-record auditor
                   :tcb-delta-record tcb-record}
        diagnostics
        (p15-s23-tcb-proof-diagnostics source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-tcb-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :proof-contract proof-contract
                       :baseline baseline
                       :current current
                       :classification classification
                       :residual residual
                       :links links
                       :summary summary
                       :auditor auditor
                       :tcb-record tcb-record})))
        rejected-records
        (p15-s23-tcb-rejected-records source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-tcb-delta-record-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-tcb-delta-record
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :compiler-source-inventory-artifact
         (select-keys inventory-artifact
                      [:kind :artifact-id :inventory-id
                       :capability-based-proof])
         :runtime-manifest-capability-artifact
         (select-keys runtime-artifact
                      [:kind :artifact-id :proof-id
                       :runtime-manifest-record
                       :capability-enforcement-record
                       :capability-based-proof])
         :stage-comparison-artifact
         (select-keys stage-artifact
                      [:kind :artifact-id :proof-id
                       :stage-equivalence-matrix
                       :stage-boundary-record
                       :capability-based-proof])
         :self-hosting-conformance-artifact
         (select-keys conformance-artifact
                      [:kind :artifact-id :proof-id
                       :stage-support-conformance-record
                       :conformance-suite-link-table
                       :capability-based-proof])
         :bootstrap-provenance-attestation-artifact
         (select-keys provenance-artifact
                      [:kind :artifact-id :proof-id
                       :bootstrap-provenance-record
                       :compiler-lineage-graph
                       :capability-based-proof])
         :baseline-tcb-inventory baseline
         :current-tcb-inventory current
         :tcb-delta-classification classification
         :residual-trust-boundary-record residual
         :tcb-evidence-link-table links
         :trust-reduction-summary summary
         :tcb-auditor-query-record auditor
         :tcb-delta-record tcb-record
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-tcb-delta-fixtures
         [{:fixture source-path
           :status :accepted
           :tcb-delta-record-id
           (:tcb-delta-record-id tcb-record)
           :baseline-trusted-count
           (:baseline-trusted-count summary)
           :current-residual-trusted-count
           (:current-residual-trusted-count summary)
           :whole-language-tcb-reduced?
           (:whole-language-tcb-reduced? summary)
           :clojure-seed-still-trusted?
           (:clojure-seed-still-trusted? residual)}]
         :rejected-p15-s23-tcb-delta-fixtures rejected-records
         :p15-s23-tcb-delta-diagnostic-stream
         (p15-s23-tcb-diagnostic-stream source-path proof-id)
         :p15-s23-tcb-delta-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count (count p15-s23-tcb-diagnostic-ids)
          :baseline-trusted-count
          (:baseline-trusted-count summary)
          :current-residual-trusted-count
          (:current-residual-trusted-count summary)
          :evidence-control-count
          (:evidence-control-count summary)
          :whole-language-tcb-reduced?
          (:whole-language-tcb-reduced? summary)
          :clojure-seed-still-trusted?
          (:clojure-seed-still-trusted? residual)
          :no-unaccounted-trusted-components?
          (:no-unaccounted-trusted-components? auditor)
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-tcb-delta-record-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))))

(defn p15-s23-tcb-delta-record-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-tcb-fail!
     "P15S23T001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-tcb-delta-record-source-artifact path)))