

(defn p15-s23-self-hosting-conformance-report-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :self-hosting-conformance-report source-path
   (fn [] (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-self-hosting-conformance-report)
        stage-artifact
        (p15-s23-stage-comparison-report-source-artifact source-path)
        phase14-artifact
        (let [artifact-fn
              (resolve
               'gravity.bootstrap/hosted-core-compiled-conformance-proof-file-artifact)]
          (when-not artifact-fn
            (p15-s23-self-hosting-conformance-fail!
             "P15S23H003" source-path nil
             {:missing-fields [:phase14-conformance-helper]}))
          (p15-s23-context-artifact
           :hosted-core-compiled-conformance-proof
           "bootstrap/clojure/fixtures/accepted/core-app.gravity"
           (fn []
             (artifact-fn
              "bootstrap/clojure/fixtures/accepted/core-app.gravity"))))
        support-record
        (p15-s23-stage-support-conformance-record
         source-path stage-artifact phase14-artifact)
        link-table
        (p15-s23-conformance-suite-link-table
         stage-artifact phase14-artifact)
        diagnostic-record
        (p15-s23-diagnostic-conformance-record
         stage-artifact phase14-artifact)
        gap-record (p15-s23-conformance-gap-record)
        candidate {:proof-contract proof-contract
                   :stage-support-conformance-record support-record
                   :conformance-suite-link-table link-table
                   :diagnostic-conformance-record diagnostic-record
                   :stage-comparison-artifact stage-artifact
                   :phase14-conformance-artifact phase14-artifact}
        diagnostics
        (p15-s23-self-hosting-conformance-proof-diagnostics
         source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-self-hosting-conformance-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :proof-contract proof-contract
                       :stage-artifact (:artifact-id stage-artifact)
                       :phase14-artifact (:artifact-id phase14-artifact)
                       :phase14-report-id
                       (get-in phase14-artifact
                               [:conformance-report :report-id])
                       :support support-record
                       :links link-table
                       :diagnostics diagnostic-record})))
        rejected-records
        (p15-s23-self-hosting-conformance-rejected-records
         source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-self-hosting-conformance-report-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-self-hosting-conformance-report
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :stage-comparison-artifact
         (select-keys stage-artifact
                      [:kind :artifact-id :proof-id
                       :stage-equivalence-matrix
                       :accepted-output-stage-comparison
                       :rejected-diagnostic-stage-comparison
                       :rejected-app-diagnostic-artifact
                       :capability-based-proof])
         :phase14-conformance-artifact
         (select-keys phase14-artifact
                      [:kind :artifact-id :conformance-report
                       :trusted-boundary :capability-based-proof])
         :stage-support-conformance-record support-record
         :conformance-suite-link-table link-table
         :diagnostic-conformance-record diagnostic-record
         :conformance-gap-record gap-record
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-self-hosting-conformance-fixtures
         [{:fixture source-path
           :status :accepted
           :suite-count
           (count p15-s23-self-hosting-conformance-scope)
           :stage-support-conformant?
           (:stage-support-conformant? support-record)
           :diagnostics-preserved?
           (:diagnostics-preserved? diagnostic-record)}]
         :rejected-p15-s23-self-hosting-conformance-fixtures
         rejected-records
         :p15-s23-self-hosting-conformance-diagnostic-stream
         (p15-s23-self-hosting-conformance-diagnostic-stream
          source-path proof-id)
         :p15-s23-self-hosting-conformance-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-self-hosting-conformance-diagnostic-ids)
          :suite-count (count p15-s23-self-hosting-conformance-scope)
          :stage-support-conformant?
          (:stage-support-conformant? support-record)
          :diagnostics-preserved?
          (:diagnostics-preserved? diagnostic-record)
          :status :in-progress}
         :diagnostics []}
        proof
        (p15-s23-self-hosting-conformance-report-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))))

(defn p15-s23-self-hosting-conformance-report-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-self-hosting-conformance-fail!
     "P15S23H001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-self-hosting-conformance-report-source-artifact path)))

(def p15-s23-provenance-required-fields
  #{:artifact-id :artifact-kind :bootstrap-stage
    :source-graph-hash :compiler-artifact-id :compiler-hash
    :lockfile-hash :build-recipe-hash
    :environment-manifest-hash :dependency-graph-hash
    :conformance-report-link :equivalence-report-link
    :reproducible-rebuild-link :sbom-link :signature-link
    :builder-identity})

(def p15-s23-provenance-required-links
  #{:compiler-source-inventory
    :compiler-pipeline-manifest
    :reproducible-rebuild-log
    :stage-comparison-report
    :self-hosting-conformance-report})

(def p15-s23-provenance-required-preserves
  #{:artifact-provenance :compiler-lineage :source-hash
    :lockfile-hash :build-recipe-hash :environment-manifest-hash
    :dependency-graph-hash :conformance-report-link
    :equivalence-report-link :reproducibility-link
    :revocation-status :auditor-query-index})

(def p15-s23-provenance-diagnostic-messages
  {"P15S23P001" "P15-S23 bootstrap provenance attestation is missing"
   "P15S23P002" "P15-S23 bootstrap provenance record is missing required fields or preservation facts"
   "P15S23P003" "P15-S23 compiler lineage is incomplete, cyclic, or not traversable to the seed"
   "P15S23P004" "P15-S23 provenance attestation is missing required evidence links"
   "P15S23P005" "P15-S23 canonical provenance payload or deterministic attestation signature is invalid"
   "P15S23P006" "P15-S23 revoked input check or auditor lineage query failed"
   "P15S23P007" "P15-S23 provenance attestation makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-provenance-diagnostic-ids
  ["P15S23P001" "P15S23P002" "P15S23P003" "P15S23P004"
   "P15S23P005" "P15S23P006" "P15S23P007"])

(defn p15-s23-provenance-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-provenance-diagnostic-messages
              id
              "P15-S23 bootstrap provenance attestation failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-provenance-attestation
                 :diagnostic-family :p15-s23-provenance-attestation
                 :value value
                 :remediation "Provide a BOOT8 provenance attestation with explicit compiler lineage, canonical payload, evidence links, revocation checks, and auditor queries while keeping full self-hosting and seed-retirement claims false until the full evidence bundle exists."}
                data)))

(defn p15-s23-provenance-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-provenance-attestation
   :source-span {:source source-path}
   :message (get p15-s23-provenance-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_bootstrap_provenance_attestation})

(defn p15-s23-provenance-file-hash
  [path]
  (if (.isFile (java.io.File. path))
    (str "sha256:" (sha256-hex (slurp path)))
    "sha256:missing"))

(defn p15-s23-provenance-canonical-value
  [value]
  (cond
    (map? value)
    (into (sorted-map)
          (map (fn [[k v]]
                 [k (p15-s23-provenance-canonical-value v)])
               value))

    (vector? value)
    (mapv p15-s23-provenance-canonical-value value)

    (sequential? value)
    (mapv p15-s23-provenance-canonical-value value)

    (set? value)
    (mapv p15-s23-provenance-canonical-value
          (sort value))

    :else value))