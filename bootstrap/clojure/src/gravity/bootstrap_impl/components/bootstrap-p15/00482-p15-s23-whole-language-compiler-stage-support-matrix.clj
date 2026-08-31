

(defn p15-s23-whole-language-compiler-stage-support-matrix
  [source-path proof-contract inventory-artifact pipeline-artifact
   source-syntax-artifact core-artifact runtime-artifact]
  {:artifact :gravity/p15-s23-whole-language-compiler-stage-support-matrix
   :source-path source-path
   :support-level :current-stage-compiler-artifact
   :claimed-subset :current-implementation-language-subset
   :source-components
   (mapv :component (:source-inventory inventory-artifact))
   :canonical-pipeline
   (get-in pipeline-artifact [:compiler-pipeline-manifest :pipeline])
   :canonical-stage-count
   (count (get-in pipeline-artifact [:compiler-pipeline-manifest :pipeline]))
   :syntax-object-count
   (get-in source-syntax-artifact
           [:p15-s23-source-syntax-serialization-results
            :syntax-object-count])
   :core-node-count
   (get-in core-artifact
           [:p15-s23-core-diagnostic-preservation-results
            :core-node-count])
   :runtime-family (get-in runtime-artifact [:runtime-manifest :family])
   :stage0-executor-boundary
   (get-in proof-contract
           [:claimed-language-subset :stage0-executor-boundary])
   :unsupported-claims
   {:full-language-compiler-self-hosted? false
    :clojure-seed-retired? false
    :release-eligible? false}
   :status
   (if (and (= :gravity/p15-s23-compiler-source-inventory-artifact
               (:kind inventory-artifact))
            (= :gravity/p15-s23-compiler-pipeline-manifest-artifact
               (:kind pipeline-artifact))
            (= p15-s23-canonical-compiler-pipeline
               (get-in pipeline-artifact
                       [:compiler-pipeline-manifest :pipeline]))
            (pos? (or (get-in source-syntax-artifact
                               [:p15-s23-source-syntax-serialization-results
                                :syntax-object-count])
                      0))
            (pos? (or (get-in core-artifact
                               [:p15-s23-core-diagnostic-preservation-results
                                :core-node-count])
                      0)))
     :complete
     :failed)})

(defn p15-s23-whole-language-compiler-evidence-link-table
  [inventory-artifact pipeline-artifact source-syntax-artifact core-artifact
   runtime-artifact accepted-artifact rejected-artifact rebuild-artifact
   stage-comparison-artifact conformance-artifact provenance-artifact
   tcb-artifact unsafe-artifact]
  (let [links
        [{:link :compiler-source-inventory
          :artifact-id (:artifact-id inventory-artifact)
          :inventory-id (:inventory-id inventory-artifact)
          :status :verified}
         {:link :compiler-pipeline-manifest
          :artifact-id (:artifact-id pipeline-artifact)
          :manifest-id (:manifest-id pipeline-artifact)
          :status :verified}
         {:link :source-unit-and-syntax-serialization-proof
          :artifact-id (:artifact-id source-syntax-artifact)
          :proof-id (:proof-id source-syntax-artifact)
          :status :verified}
         {:link :core-lowering-and-diagnostic-preservation-report
          :artifact-id (:artifact-id core-artifact)
          :proof-id (:proof-id core-artifact)
          :status :verified}
         {:link :runtime-manifest-and-capability-enforcement-report
          :artifact-id (:artifact-id runtime-artifact)
          :proof-id (:proof-id runtime-artifact)
          :status :verified}
         {:link :accepted-app-execution-proof
          :artifact-id (:artifact-id accepted-artifact)
          :proof-id (:proof-id accepted-artifact)
          :status :verified}
         {:link :rejected-app-diagnostic-proof
          :artifact-id (:artifact-id rejected-artifact)
          :proof-id (:proof-id rejected-artifact)
          :status :verified}
         {:link :reproducible-rebuild-log
          :artifact-id (:artifact-id rebuild-artifact)
          :proof-id (:proof-id rebuild-artifact)
          :status :verified}
         {:link :stage-comparison-report
          :artifact-id (:artifact-id stage-comparison-artifact)
          :proof-id (:proof-id stage-comparison-artifact)
          :status :verified}
         {:link :self-hosting-conformance-report
          :artifact-id (:artifact-id conformance-artifact)
          :proof-id (:proof-id conformance-artifact)
          :status :verified}
         {:link :bootstrap-provenance-attestation
          :artifact-id (:artifact-id provenance-artifact)
          :proof-id (:proof-id provenance-artifact)
          :status :verified}
         {:link :trusted-computing-base-delta-record
          :artifact-id (:artifact-id tcb-artifact)
          :proof-id (:proof-id tcb-artifact)
          :status :verified}
         {:link :unsafe-audit-report
          :artifact-id (:artifact-id unsafe-artifact)
          :proof-id (:proof-id unsafe-artifact)
          :status :verified}]
        covered (set (map :link links))]
    {:artifact :gravity/p15-s23-whole-language-compiler-evidence-link-table
     :links links
     :required-links
     (vec (sort p15-s23-whole-language-compiler-required-links))
     :required-links-covered?
     (= p15-s23-whole-language-compiler-required-links covered)
     :status
     (if (= p15-s23-whole-language-compiler-required-links covered)
       :complete
       :failed)}))

(defn p15-s23-whole-language-compiler-accepted-record
  [accepted-artifact hosted-compiler-artifact]
  {:artifact :gravity/p15-s23-whole-language-compiler-accepted-application-compile-record
   :fixture p15-s23-accepted-app-source-path
   :hosted-compiler-artifact-id (:artifact-id hosted-compiler-artifact)
   :p15-s23-accepted-artifact-id (:artifact-id accepted-artifact)
   :compiled-plan-id
   (get-in accepted-artifact
           [:accepted-app-artifact :compiled-plan :plan-id])
   :compiler-report-id
   (get-in hosted-compiler-artifact [:compiler-report :report-id])
   :stdout
   (get-in accepted-artifact [:accepted-output-comparison
                              :accepted-stdout])
   :reference-stdout
   (get-in accepted-artifact [:accepted-output-comparison
                              :reference-stdout])
   :output-matches?
   (true?
    (get-in accepted-artifact [:accepted-output-comparison
                               :accepted-matches-reference?]))
   :compiled-plan-emitted?
   (true?
    (get-in accepted-artifact [:compiled-plan-execution-trace
                               :compiled-plan-emitted?]))
   :compiled-plan-executed?
   (true?
    (get-in accepted-artifact [:compiled-plan-execution-trace
                               :compiled-plan-executed?]))
   :status
   (if (and (true?
             (get-in accepted-artifact [:accepted-output-comparison
                                        :accepted-matches-reference?]))
            (true?
             (get-in accepted-artifact [:compiled-plan-execution-trace
                                        :compiled-plan-emitted?]))
            (true?
             (get-in accepted-artifact [:compiled-plan-execution-trace
                                        :compiled-plan-executed?]))
            (= :gravity/stage0-hosted-core-compiled-compiler-proof
               (:kind hosted-compiler-artifact)))
     :complete
     :failed)})

(defn p15-s23-whole-language-compiler-rejected-record
  [rejected-artifact]
  (let [records (:rejected-app-diagnostic-records rejected-artifact)
        diagnostics (mapv :diagnostic records)
        expected (mapv :expected-diagnostic records)]
    {:artifact :gravity/p15-s23-whole-language-compiler-rejected-application-diagnostic-record
     :fixtures (mapv :fixture records)
     :diagnostics diagnostics
     :expected-diagnostics expected
     :all-fixtures-rejected?
     (every? #(= :rejected (:status %)) records)
     :diagnostics-match-expected?
     (every? true? (map :matches-expected? records))
     :diagnostic-codes-stable? (= (set diagnostics) (set expected))
     :status
     (if (and (seq records)
              (every? #(= :rejected (:status %)) records)
              (every? true? (map :matches-expected? records))
              (= (set diagnostics) (set expected)))
       :complete
       :failed)}))

(defn p15-s23-whole-language-compiler-residual-boundary-record
  [accepted-artifact tcb-artifact]
  (let [accepted-boundary (:trusted-boundary-record accepted-artifact)
        tcb-boundaries
        (get-in tcb-artifact
                [:residual-trust-boundary-record :residual-boundaries])]
    {:artifact :gravity/p15-s23-whole-language-compiler-residual-trusted-boundary-record
     :compiler (:compiler accepted-boundary)
     :runtime (:runtime accepted-boundary)
     :instruction-plan? (:instruction-plan? accepted-boundary)
     :clojure-instruction-runner?
     (:clojure-instruction-runner? accepted-boundary)
     :clojure-stage0-still-required? true
     :self-hosted-compiler? false
     :clojure-seed-retired? false
     :residual-tcb-boundaries tcb-boundaries
     :retirement-condition :complete-governance-release-and-seed-retirement
     :status
     (if (and (true? (:clojure-instruction-runner?
                     accepted-boundary))
              (false? (:self-hosted-compiler? accepted-boundary))
              (false? (:clojure-seed-retired? accepted-boundary))
              (contains? (set tcb-boundaries) :clojure-stage0-bootstrap))
       :complete
       :failed)}))

(defn p15-s23-whole-language-compiler-lineage-record
  [source-path inventory-artifact provenance-artifact stage-comparison-artifact]
  {:artifact :gravity/p15-s23-whole-language-compiler-lineage-record
   :source-path source-path
   :compiler-artifact-source :gravity-source
   :compiled-by :clojure-stage0
   :verified-by :clojure-stage0
   :source-inventory-id (:inventory-id inventory-artifact)
   :bootstrap-provenance-record-id
   (get-in provenance-artifact [:bootstrap-provenance-record
                                :provenance-record-id])
   :stage-comparison-proof-id (:proof-id stage-comparison-artifact)
   :lineage-traversable-to-seed?
   (get-in provenance-artifact [:compiler-lineage-graph
                                :lineage-traversable-to-seed?])
   :status
   (if (and (re-find #"^sha256:" (str (:inventory-id inventory-artifact)))
            (true?
             (get-in provenance-artifact [:compiler-lineage-graph
                                          :lineage-traversable-to-seed?]))
            (true?
             (get-in stage-comparison-artifact
                     [:capability-based-proof
                      :current-candidate-equivalent-to-seed?])))
     :complete
     :failed)})