

(defn p15-s23-unsafe-audit-report-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :unsafe-audit-report source-path
   (fn [] (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-unsafe-audit-report)
        inventory-artifact
        (p15-s23-compiler-source-inventory-source-artifact source-path)
        pipeline-artifact
        (p15-s23-compiler-pipeline-manifest-source-artifact source-path)
        runtime-artifact
        (p15-s23-runtime-manifest-capability-enforcement-source-artifact
         source-path)
        provenance-artifact
        (p15-s23-provenance-attestation-source-artifact source-path)
        tcb-artifact
        (p15-s23-tcb-delta-record-source-artifact source-path)
        island-index
        (p15-s23-unsafe-island-index source-path source-data)
        operation-inventory
        (p15-s23-unsafe-operation-inventory island-index)
        wrapper-table
        (p15-s23-safe-wrapper-boundary-table island-index)
        package-metadata
        (p15-s23-package-safety-metadata
         source-path island-index operation-inventory wrapper-table)
        review-record
        (p15-s23-unsafe-review-and-revalidation-record
         source-path source-data package-metadata)
        external-boundary-audit
        (p15-s23-external-seed-boundary-audit tcb-artifact)
        link-table
        (p15-s23-unsafe-evidence-link-table
         inventory-artifact pipeline-artifact runtime-artifact
         provenance-artifact tcb-artifact external-boundary-audit)
        auditor-query
        (p15-s23-unsafe-auditor-query-record
         island-index operation-inventory wrapper-table package-metadata
         review-record external-boundary-audit link-table)
        report
        (p15-s23-unsafe-audit-report
         source-path island-index operation-inventory wrapper-table
         package-metadata review-record external-boundary-audit link-table
         auditor-query)
        candidate {:proof-contract proof-contract
                   :unsafe-island-index island-index
                   :unsafe-operation-inventory operation-inventory
                   :safe-wrapper-boundary-table wrapper-table
                   :package-safety-metadata package-metadata
                   :review-and-revalidation-record review-record
                   :external-seed-boundary-audit external-boundary-audit
                   :unsafe-evidence-link-table link-table
                   :unsafe-auditor-query-record auditor-query
                   :unsafe-audit-report report}
        diagnostics
        (p15-s23-unsafe-proof-diagnostics source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-unsafe-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :proof-contract proof-contract
                       :island-index island-index
                       :operation-inventory operation-inventory
                       :wrapper-table wrapper-table
                       :package-metadata package-metadata
                       :review-record review-record
                       :external-boundary-audit external-boundary-audit
                       :link-table link-table
                       :auditor-query auditor-query
                       :report report})))
        rejected-records
        (p15-s23-unsafe-rejected-records source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-unsafe-audit-report-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-unsafe-audit-report
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :compiler-source-inventory-artifact
         (select-keys inventory-artifact
                      [:kind :artifact-id :inventory-id
                       :capability-based-proof])
         :compiler-pipeline-manifest-artifact
         (select-keys pipeline-artifact
                      [:kind :artifact-id :manifest-id
                       :capability-based-proof])
         :runtime-manifest-capability-artifact
         (select-keys runtime-artifact
                      [:kind :artifact-id :proof-id
                       :runtime-manifest-record
                       :capability-enforcement-record
                       :capability-based-proof])
         :bootstrap-provenance-attestation-artifact
         (select-keys provenance-artifact
                      [:kind :artifact-id :proof-id
                       :bootstrap-provenance-record
                       :compiler-lineage-graph
                       :capability-based-proof])
         :tcb-delta-record-artifact
         (select-keys tcb-artifact
                      [:kind :artifact-id :proof-id
                       :tcb-delta-record
                       :residual-trust-boundary-record
                       :capability-based-proof])
         :unsafe-island-index island-index
         :unsafe-operation-inventory operation-inventory
         :safe-wrapper-boundary-table wrapper-table
         :package-safety-metadata package-metadata
         :review-and-revalidation-record review-record
         :external-seed-boundary-audit external-boundary-audit
         :unsafe-evidence-link-table link-table
         :unsafe-auditor-query-record auditor-query
         :unsafe-audit-report report
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-unsafe-audit-fixtures
         [{:fixture source-path
           :status :accepted
           :unsafe-audit-report-id
           (:unsafe-audit-report-id report)
           :unsafe-island-count (:unsafe-island-count island-index)
           :unsafe-operation-count
           (:unsafe-operation-count operation-inventory)
           :review-state (:review-state review-record)
           :external-seed-boundaries-separated?
           (:host-trust-boundaries-not-counted-as-safe-gravity?
            external-boundary-audit)}]
         :rejected-p15-s23-unsafe-audit-fixtures rejected-records
         :p15-s23-unsafe-audit-diagnostic-stream
         (p15-s23-unsafe-diagnostic-stream source-path proof-id)
         :p15-s23-unsafe-audit-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count (count p15-s23-unsafe-diagnostic-ids)
          :unsafe-island-count (:unsafe-island-count island-index)
          :unsafe-operation-count
          (:unsafe-operation-count operation-inventory)
          :safe-wrapper-count (:safe-wrapper-count wrapper-table)
          :package-safety-schema-validated?
          (:schema-validated? package-metadata)
          :review-state (:review-state review-record)
          :review-stale? (:stale? review-record)
          :required-evidence-links-covered?
          (:required-links-covered? link-table)
          :external-seed-boundaries-separated?
          (:host-trust-boundaries-not-counted-as-safe-gravity?
           external-boundary-audit)
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-unsafe-audit-report-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))))

(defn p15-s23-unsafe-audit-report-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-unsafe-fail!
     "P15S23U001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-unsafe-audit-report-source-artifact path)))

(def p15-s23-whole-language-compiler-required-preserves
  #{:source-spans :syntax-identity :diagnostic-codes
    :artifact-provenance :pipeline-stage-contracts
    :runtime-capability-manifest :accepted-app-output
    :rejected-app-diagnostic-trace :compiler-lineage
    :tcb-component-inventory :unsafe-island-index})

(def p15-s23-whole-language-compiler-required-links
  #{:compiler-source-inventory
    :compiler-pipeline-manifest
    :source-unit-and-syntax-serialization-proof
    :core-lowering-and-diagnostic-preservation-report
    :runtime-manifest-and-capability-enforcement-report
    :accepted-app-execution-proof
    :rejected-app-diagnostic-proof
    :reproducible-rebuild-log
    :stage-comparison-report
    :self-hosting-conformance-report
    :bootstrap-provenance-attestation
    :trusted-computing-base-delta-record
    :unsafe-audit-report})

(def p15-s23-whole-language-compiler-diagnostic-messages
  {"P15S23W001" "P15-S23 whole-language compiler artifact contract is missing"
   "P15S23W002" "P15-S23 whole-language compiler artifact is missing source, pipeline, or evidence links"
   "P15S23W003" "P15-S23 whole-language compiler artifact does not compile and run the claimed application subset"
   "P15S23W004" "P15-S23 whole-language compiler artifact is missing reproducibility, equivalence, conformance, or provenance linkage"
   "P15S23W005" "P15-S23 whole-language compiler artifact has an unsupported self-hosting, seed-retirement, or trusted-boundary claim"
   "P15S23W006" "P15-S23 whole-language compiler artifact does not preserve rejected diagnostic coverage"})

(def p15-s23-whole-language-compiler-diagnostic-ids
  ["P15S23W001" "P15S23W002" "P15S23W003"
   "P15S23W004" "P15S23W005" "P15S23W006"])

(defn p15-s23-whole-language-compiler-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-whole-language-compiler-diagnostic-messages
              id
              "P15-S23 whole-language compiler artifact failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-whole-language-compiler-artifact
                 :diagnostic-family
                 :p15-s23-whole-language-compiler-artifact
                 :value value
                 :remediation "Emit a current-stage compiler artifact with source, pipeline, accepted execution, rejected diagnostics, rebuild, comparison, conformance, provenance, TCB, and unsafe-audit links. Keep Clojure seed retirement false until the full release evidence bundle exists."}
                data)))

(defn p15-s23-whole-language-compiler-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-whole-language-compiler-artifact
   :source-span {:source source-path}
   :message (get p15-s23-whole-language-compiler-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_p15_s23_whole_language_compiler_artifact})