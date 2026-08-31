

(defn p15-s23-provenance-attestation-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :provenance-attestation source-path
   (fn [] (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-provenance-attestation)
        inventory-artifact
        (p15-s23-compiler-source-inventory-source-artifact source-path)
        pipeline-artifact
        (p15-s23-compiler-pipeline-manifest-source-artifact source-path)
        rebuild-artifact
        (p15-s23-reproducible-rebuild-log-source-artifact source-path)
        stage-artifact
        (p15-s23-stage-comparison-report-source-artifact source-path)
        conformance-artifact
        (p15-s23-self-hosting-conformance-report-source-artifact source-path)
        source-graph
        (p15-s23-provenance-source-graph-record
         source-path source-data inventory-artifact)
        build-input
        (p15-s23-provenance-build-input-record source-path source-graph)
        lineage
        (p15-s23-compiler-lineage-graph inventory-artifact build-input)
        link-table
        (p15-s23-provenance-evidence-link-table
         inventory-artifact pipeline-artifact rebuild-artifact
         stage-artifact conformance-artifact)
        release-policy
        (p15-s23-provenance-release-policy-record)
        revocation
        (p15-s23-provenance-revocation-check-report link-table)
        auditor
        (p15-s23-provenance-auditor-query-index lineage link-table)
        provenance-record
        (p15-s23-bootstrap-provenance-record
         source-path source-graph build-input link-table release-policy)
        canonical-payload
        (p15-s23-canonical-provenance-payload
         provenance-record lineage link-table release-policy
         revocation auditor)
        candidate {:proof-contract proof-contract
                   :bootstrap-provenance-record provenance-record
                   :compiler-lineage-graph lineage
                   :stage-evidence-link-table link-table
                   :canonical-provenance-payload canonical-payload
                   :revocation-check-report revocation
                   :auditor-query-index auditor}
        diagnostics
        (p15-s23-provenance-proof-diagnostics source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-provenance-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :proof-contract proof-contract
                       :provenance-record provenance-record
                       :lineage lineage
                       :link-table link-table
                       :canonical-payload-id
                       (:payload-id canonical-payload)
                       :revocation revocation
                       :auditor auditor})))
        rejected-records
        (p15-s23-provenance-rejected-records source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-provenance-attestation-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-provenance-attestation
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :compiler-source-inventory-artifact
         (select-keys inventory-artifact
                      [:kind :artifact-id :inventory-id
                       :source-inventory :capability-based-proof])
         :compiler-pipeline-manifest-artifact
         (select-keys pipeline-artifact
                      [:kind :artifact-id :manifest-id
                       :capability-based-proof])
         :reproducible-rebuild-artifact
         (select-keys rebuild-artifact
                      [:kind :artifact-id :proof-id
                       :artifact-identity-comparison
                       :environment-provenance-record
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
         :source-graph-record source-graph
         :build-input-record build-input
         :bootstrap-provenance-record provenance-record
         :compiler-lineage-graph lineage
         :stage-evidence-link-table link-table
         :release-policy-record release-policy
         :canonical-provenance-payload canonical-payload
         :revocation-check-report revocation
         :auditor-query-index auditor
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-provenance-attestation-fixtures
         [{:fixture source-path
           :status :accepted
           :provenance-record-id
           (:provenance-record-id provenance-record)
           :canonical-payload-id (:payload-id canonical-payload)
           :compiler-lineage-traversable?
           (:lineage-traversable-to-seed? lineage)
           :revocation-clear? (:revocation-clear? revocation)
           :auditor-query-passed?
           (:auditor-query-passed? auditor)}]
         :rejected-p15-s23-provenance-attestation-fixtures
         rejected-records
         :p15-s23-provenance-attestation-diagnostic-stream
         (p15-s23-provenance-diagnostic-stream source-path proof-id)
         :p15-s23-provenance-attestation-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-provenance-diagnostic-ids)
          :required-field-count
          (count p15-s23-provenance-required-fields)
          :required-link-count
          (count p15-s23-provenance-required-links)
          :lineage-traversable-to-seed?
          (:lineage-traversable-to-seed? lineage)
          :canonical-payload-signed?
          (= :verified
             (get-in canonical-payload [:signature :status]))
          :revocation-clear? (:revocation-clear? revocation)
          :auditor-query-passed? (:auditor-query-passed? auditor)
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-provenance-attestation-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))))

(defn p15-s23-provenance-attestation-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-provenance-fail!
     "P15S23P001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-provenance-attestation-source-artifact path)))

(def p15-s23-tcb-required-preserves
  #{:artifact-provenance :compiler-lineage
    :tcb-component-inventory :tcb-delta-classification
    :residual-trust-boundaries :evidence-linkage})

(def p15-s23-tcb-required-residual-boundaries
  #{:clojure-stage0-bootstrap
    :clojure-stage0-verifier
    :jvm-runtime
    :host-filesystem-source-loading
    :deps-lockfile})

(def p15-s23-tcb-required-evidence-links
  #{:bootstrap-provenance-attestation
    :compiler-source-inventory
    :runtime-manifest-and-capability-enforcement-report
    :stage-comparison-report
    :self-hosting-conformance-report})

(def p15-s23-tcb-diagnostic-messages
  {"P15S23T001" "P15-S23 trusted-computing-base delta record is missing"
   "P15S23T002" "P15-S23 TCB baseline or current component inventory is incomplete"
   "P15S23T003" "P15-S23 TCB delta classification is incomplete"
   "P15S23T004" "P15-S23 residual trust boundaries are missing or underreported"
   "P15S23T005" "P15-S23 TCB delta is missing required evidence links"
   "P15S23T006" "P15-S23 TCB delta count or trust-reduction summary is inconsistent"
   "P15S23T007" "P15-S23 TCB delta record makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-tcb-diagnostic-ids
  ["P15S23T001" "P15S23T002" "P15S23T003" "P15S23T004"
   "P15S23T005" "P15S23T006" "P15S23T007"])

(defn p15-s23-tcb-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-tcb-diagnostic-messages
              id
              "P15-S23 trusted-computing-base delta record failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-tcb-delta-record
                 :diagnostic-family :p15-s23-tcb-delta-record
                 :value value
                 :remediation "Provide an explicit TCB delta with baseline and current component inventories, residual Clojure seed boundaries, provenance links, and measured trust-reduction status without claiming full self-hosting or seed retirement."}
                data)))

(defn p15-s23-tcb-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-tcb-delta-record
   :source-span {:source source-path}
   :message (get p15-s23-tcb-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_tcb_delta_record})