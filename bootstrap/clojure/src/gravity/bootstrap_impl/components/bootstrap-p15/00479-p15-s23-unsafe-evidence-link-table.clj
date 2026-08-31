

(defn p15-s23-unsafe-evidence-link-table
  [inventory-artifact pipeline-artifact runtime-artifact
   provenance-artifact tcb-artifact external-boundary-audit]
  (let [links
        [{:link :compiler-source-inventory
          :artifact-id (:artifact-id inventory-artifact)
          :status :verified}
         {:link :compiler-pipeline-manifest
          :artifact-id (:artifact-id pipeline-artifact)
          :manifest-id (:manifest-id pipeline-artifact)
          :status :verified}
         {:link :runtime-manifest-and-capability-enforcement-report
          :artifact-id (:artifact-id runtime-artifact)
          :proof-id (:proof-id runtime-artifact)
          :status :verified}
         {:link :bootstrap-provenance-attestation
          :artifact-id (:artifact-id provenance-artifact)
          :proof-id (:proof-id provenance-artifact)
          :status :verified}
         {:link :trusted-computing-base-delta-record
          :artifact-id (:artifact-id tcb-artifact)
          :proof-id (:proof-id tcb-artifact)
          :tcb-delta-record-id
          (get-in tcb-artifact [:tcb-delta-record
                                :tcb-delta-record-id])
          :status :verified}]
        covered (set (map :link links))]
    {:artifact :gravity/p15-s23-unsafe-evidence-link-table
     :links links
     :required-links (vec (sort p15-s23-unsafe-required-evidence-links))
     :required-links-covered?
     (= p15-s23-unsafe-required-evidence-links covered)
     :external-boundaries-separated?
     (:host-trust-boundaries-not-counted-as-safe-gravity?
      external-boundary-audit)
     :status (if (and (= p15-s23-unsafe-required-evidence-links covered)
                      (= :complete (:status external-boundary-audit)))
               :complete
               :failed)}))

(defn p15-s23-unsafe-auditor-query-record
  [island-index operation-inventory wrapper-table package-metadata
   review-record external-boundary-audit link-table]
  {:artifact :gravity/p15-s23-unsafe-auditor-query-record
   :queries
   [{:query :which-gravity-unsafe-islands-exist
     :answer (mapv :unsafe-island-id (:unsafe-islands island-index))
     :status :answered}
    {:query :which-unsafe-operation-families-exist
     :answer (:unsafe-operation-families operation-inventory)
     :status :answered}
    {:query :is-package-safety-metadata-schema-validated
     :answer (:schema-validated? package-metadata)
     :status :answered}
    {:query :are-external-host-boundaries-separated-from-gravity-unsafe
     :answer (:host-trust-boundaries-not-counted-as-safe-gravity?
              external-boundary-audit)
     :status :answered}
    {:query :which-evidence-links-support-unsafe-audit
     :answer (mapv :link (:links link-table))
     :status :answered}]
   :no-hidden-gravity-unsafe-islands?
   (and (zero? (:unsafe-island-count island-index))
        (zero? (:unsafe-operation-count operation-inventory)))
   :safe-wrapper-policy-satisfied?
   (:safe-wrapper-coverage-complete? wrapper-table)
   :package-safety-metadata-complete?
   (and (= :complete (:status package-metadata))
        (:schema-validated? package-metadata))
   :review-current?
   (and (= :complete (:status review-record))
        (false? (:stale? review-record)))
   :required-links-covered? (:required-links-covered? link-table)
   :status :complete})

(defn p15-s23-unsafe-audit-report
  [source-path island-index operation-inventory wrapper-table
   package-metadata review-record external-boundary-audit link-table
   auditor-query]
  (let [report-base
        {:artifact :gravity/unsafe-audit-report
         :source-path source-path
         :bootstrap-stage :p15-s23
         :unsafe-island-index-id (c4-artifact-id island-index)
         :unsafe-operation-inventory-id
         (c4-artifact-id operation-inventory)
         :safe-wrapper-boundary-table-id (c4-artifact-id wrapper-table)
         :package-safety-metadata-id
         (:package-safety-metadata-id package-metadata)
         :review-and-revalidation-record-id
         (c4-artifact-id review-record)
         :external-seed-boundary-audit-id
         (c4-artifact-id external-boundary-audit)
         :evidence-link-table-id (c4-artifact-id link-table)
         :auditor-query-record-id (c4-artifact-id auditor-query)
         :unsafe-island-count (:unsafe-island-count island-index)
         :unsafe-operation-count
         (:unsafe-operation-count operation-inventory)
         :safe-wrapper-count (:safe-wrapper-count wrapper-table)
         :review-state (:review-state review-record)
         :stale? (:stale? review-record)
         :no-gravity-unsafe-islands?
         (:no-hidden-gravity-unsafe-islands? auditor-query)
         :external-seed-boundaries-separated?
         (:host-trust-boundaries-not-counted-as-safe-gravity?
          external-boundary-audit)
         :release-eligible? false
         :full-language-compiler-self-hosted? false
         :clojure-seed-retired? false
         :status :complete}
        report-id (c4-artifact-id report-base)]
    (assoc report-base :unsafe-audit-report-id report-id)))

(defn p15-s23-unsafe-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        island-index (:unsafe-island-index candidate)
        operation-inventory (:unsafe-operation-inventory candidate)
        wrapper-table (:safe-wrapper-boundary-table candidate)
        package-metadata (:package-safety-metadata candidate)
        review-record (:review-and-revalidation-record candidate)
        external-boundary-audit (:external-seed-boundary-audit candidate)
        link-table (:unsafe-evidence-link-table candidate)
        report (:unsafe-audit-report candidate)
        auditor-query (:unsafe-auditor-query-record candidate)
        claims (:self-hosting-claims proof-contract)
        preserves (set (:preserves proof-contract))
        missing-preserves
        (set/difference p15-s23-unsafe-required-preserves preserves)]
    (vec
     (concat
      (when-not (= :gravity/unsafe-audit-report
                   (:artifact proof-contract))
        [(p15-s23-unsafe-diagnostic-record
          source-path "P15S23U001" proof-contract
          {:missing-fields [:artifact]})])
      (when (or (seq missing-preserves)
                (not= :complete (:status island-index))
                (not= :complete (:status operation-inventory))
                (not (:unsafe-forms-scanned? island-index))
                (pos? (:unaudited-unsafe-form-count island-index))
                (not= (:unsafe-island-count island-index)
                      (:unsafe-operation-count operation-inventory)))
        [(p15-s23-unsafe-diagnostic-record
          source-path "P15S23U002"
          {:unsafe-island-index island-index
           :unsafe-operation-inventory operation-inventory}
          {:missing-preserves (vec (sort missing-preserves))
           :required [:unsafe-forms-scanned
                      :zero-unaudited-unsafe-forms
                      :matching-operation-inventory]})])
      (when-not
       (and (= :complete (:status wrapper-table))
            (true? (:safe-wrapper-coverage-complete? wrapper-table)))
        [(p15-s23-unsafe-diagnostic-record
          source-path "P15S23U003" wrapper-table
          {:required [:safe-wrapper-boundaries
                      :invariant-or-runtime-check-evidence]})])
      (when-not
       (and (= :complete (:status package-metadata))
            (true? (:schema-validated? package-metadata))
            (= :reviewed (:review-state package-metadata))
            (false? (:release-eligible? package-metadata)))
        [(p15-s23-unsafe-diagnostic-record
          source-path "P15S23U004" package-metadata
          {:required [:schema-validated-package-safety-metadata
                      :reviewed-state
                      :no-release-eligibility-overclaim]})])
      (when-not
       (and (= :complete (:status review-record))
            (= :reviewed (:review-state review-record))
            (false? (:stale? review-record))
            (seq (:revalidation-triggers review-record)))
        [(p15-s23-unsafe-diagnostic-record
          source-path "P15S23U005" review-record
          {:required [:current-review
                      :stale-audit-release-block
                      :revalidation-triggers]})])
      (when-not
       (and (= :complete (:status link-table))
            (true? (:required-links-covered? link-table))
            (= p15-s23-unsafe-required-evidence-links
               (set (map :link (:links link-table))))
            (= :complete (:status external-boundary-audit))
            (true?
             (:host-trust-boundaries-not-counted-as-safe-gravity?
              external-boundary-audit)))
        [(p15-s23-unsafe-diagnostic-record
          source-path "P15S23U006"
          {:unsafe-evidence-link-table link-table
           :external-seed-boundary-audit external-boundary-audit}
          {:required-links
           (vec (sort p15-s23-unsafe-required-evidence-links))
           :required-external-boundaries
           p15-s23-unsafe-external-seed-boundaries})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims))
                (true? (:release-eligible? report))
                (true? (:full-language-compiler-self-hosted? report))
                (true? (:clojure-seed-retired? report)))
        [(p15-s23-unsafe-diagnostic-record
          source-path "P15S23U007"
          {:claims claims :report report}
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)
           :release-eligible? (:release-eligible? report)})])))))

(defn p15-s23-unsafe-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-unsafe-audit-diagnostic-stream
   :stage :p15-s23-unsafe-audit-report
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-unsafe-audit-report
            :message (get p15-s23-unsafe-diagnostic-messages id)})
         p15-s23-unsafe-diagnostic-ids)
   :status :complete})