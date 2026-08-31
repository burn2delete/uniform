

(defn p15-s23-release-provenance-link
  [whole-artifact provenance-artifact package-record reproducible-record
   release-decision]
  (let [record
        {:artifact :gravity/p15-s23-release-provenance-link
         :compiler-artifact-id
         (get-in whole-artifact
                 [:compiler-artifact-manifest :compiler-artifact-id])
         :package-release-id (:package-release-id package-record)
         :reproducible-release-record-id
         (:reproducible-release-record-id reproducible-record)
         :bootstrap-provenance-record-id
         (get-in provenance-artifact
                 [:bootstrap-provenance-record
                  :provenance-record-id])
         :compiler-lineage-traversable?
         (true?
          (get-in provenance-artifact
                  [:compiler-lineage-graph
                   :lineage-traversable-to-seed?]))
         :canonical-payload-signed?
         (= :verified
            (or (get-in provenance-artifact
                        [:canonical-provenance-payload
                         :signature-status])
                (get-in provenance-artifact
                        [:canonical-provenance-payload
                         :signature
                         :status])))
         :revocation-clear?
         (true?
          (get-in provenance-artifact
                  [:revocation-check-report
                   :revocation-clear?]))
         :release-blockers (:release-blockers release-decision)
         :status :complete}]
    (assoc record
           :release-provenance-link-id
           (str "sha256:" (sha256-hex (pr-str record))))))

(defn p15-s23-governance-package-auditor-query-record
  [rfc-record package-record reproducible-record registry-decision
   provenance-link release-decision]
  (let [record {:artifact :gravity/p15-s23-governance-package-auditor-query-record
                :rfc-traceability-passed?
                (and (:rfc-traceable-to-implementation? rfc-record)
                     (:required-sections-complete? rfc-record)
                     (:review-gates-complete? rfc-record))
                :package-metadata-passed?
                (and (:identity-complete? package-record)
                     (:ownership-complete? package-record)
                     (:provenance-present? package-record)
                     (:sbom-present? package-record)
                     (:signature-present? package-record)
                     (:artifact-hashes-present? package-record)
                     (:capability-manifest-present? package-record)
                     (:conformance-report-present? package-record)
                     (:unsafe-metadata-visible? package-record))
                :reproducibility-passed?
                (:rebuild-verification-passed? reproducible-record)
                :registry-policy-passed?
                (and (:package-policy-satisfied? registry-decision)
                     (false?
                      (:registry-publication-eligible?
                       registry-decision))
                     (= [:clojure-seed-retired]
                        (:registry-publication-blockers
                         registry-decision)))
                :provenance-link-passed?
                (and (:compiler-lineage-traversable? provenance-link)
                     (:canonical-payload-signed? provenance-link)
                     (:revocation-clear? provenance-link))
                :release-blocker-explicit?
                (= [:clojure-seed-retired]
                   (:release-blockers release-decision))
                :final-release-not-claimed?
                (and (false? (:release-eligible? release-decision))
                     (false?
                      (:registry-publication-eligible?
                       release-decision)))
                :status :complete}
        all-passed?
        (every? true?
                ((juxt :rfc-traceability-passed?
                       :package-metadata-passed?
                       :reproducibility-passed?
                       :registry-policy-passed?
                       :provenance-link-passed?
                       :release-blocker-explicit?
                       :final-release-not-claimed?)
                 record))]
    (assoc record
           :all-queries-passed? all-passed?
           :auditor-query-record-id
           (str "sha256:" (sha256-hex (pr-str record))))))

(defn p15-s23-governance-and-package-release-record-summary
  [rfc-record package-record reproducible-record registry-decision
   provenance-link release-decision auditor-query]
  (let [record {:artifact :gravity/governance-and-package-release-record
                :rfc-record-id (:rfc-record-id rfc-record)
                :package-release-id (:package-release-id package-record)
                :reproducible-release-record-id
                (:reproducible-release-record-id reproducible-record)
                :registry-policy-decision-id
                (:registry-policy-decision-id registry-decision)
                :release-provenance-link-id
                (:release-provenance-link-id provenance-link)
                :release-decision-record-id
                (:release-decision-record-id release-decision)
                :auditor-query-record-id
                (:auditor-query-record-id auditor-query)
                :governance-and-package-policy-satisfied? true
                :release-eligible? false
                :registry-publication-eligible? false
                :final-release-blocked-by-seed-retirement? true
                :status :complete}]
    (assoc record
           :governance-package-record-id
           (str "sha256:" (sha256-hex (pr-str record))))))

(defn p15-s23-governance-package-release-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        rfc-record (:rfc-record candidate)
        package-record (:package-release-record candidate)
        reproducible-record (:reproducible-release-record candidate)
        registry-decision (:registry-policy-decision candidate)
        provenance-link (:release-provenance-link candidate)
        release-decision (:release-decision-record candidate)
        auditor-query (:auditor-query-record candidate)
        summary (:governance-and-package-release-record candidate)]
    (vec
     (keep identity
           [(when-not (= :gravity/governance-and-package-release-record
                         (:artifact proof-contract))
              (p15-s23-governance-package-release-diagnostic-record
               source-path "P15S23L001"
               {:observed (:artifact proof-contract)}))
            (when-not (and (= :implemented (:state rfc-record))
                           (:rfc-traceable-to-implementation? rfc-record)
                           (:required-sections-complete? rfc-record)
                           (:review-gates-complete? rfc-record)
                           (:test-plan-linked? rfc-record)
                           (:migration-plan-linked? rfc-record))
              (p15-s23-governance-package-release-diagnostic-record
               source-path "P15S23L002"
               {:rfc-id (:rfc-id rfc-record)
                :state (:state rfc-record)}))
            (when-not (and (:identity-complete? package-record)
                           (:ownership-complete? package-record)
                           (:provenance-present? package-record)
                           (:sbom-present? package-record)
                           (:signature-present? package-record)
                           (:artifact-hashes-present? package-record)
                           (:capability-manifest-present? package-record)
                           (:conformance-report-present? package-record)
                           (:unsafe-metadata-visible? package-record))
              (p15-s23-governance-package-release-diagnostic-record
               source-path "P15S23L003"
               {:package (:package package-record)
                :namespace (:namespace package-record)}))
            (when-not (and (:build-recipe-present? reproducible-record)
                           (:locked-dependencies? reproducible-record)
                           (:network-after-fetch-denied? reproducible-record)
                           (:host-paths-normalized? reproducible-record)
                           (:rebuild-verification-passed?
                            reproducible-record)
                           (:compiler-lineage-traversable?
                            provenance-link)
                           (:canonical-payload-signed? provenance-link)
                           (:revocation-clear? provenance-link))
              (p15-s23-governance-package-release-diagnostic-record
               source-path "P15S23L004"
               {:rebuild-status (:status reproducible-record)
                :provenance-status (:status provenance-link)}))
            (when-not (and (:package-policy-satisfied? registry-decision)
                           (= :blocked-until-seed-retirement
                              (:decision registry-decision))
                           (false?
                            (:registry-publication-eligible?
                             registry-decision))
                           (:namespace-reserved? registry-decision)
                           (:namespace-owner-verified? registry-decision)
                           (= :clear (:advisory-state registry-decision))
                           (= :none (:yank-state registry-decision))
                           (= :mitigated
                              (:dependency-confusion-risk
                               registry-decision)))
              (p15-s23-governance-package-release-diagnostic-record
               source-path "P15S23L005"
               {:decision (:decision registry-decision)
                :registry-publication-eligible?
                (:registry-publication-eligible? registry-decision)}))
            (when (or (true?
                       (get-in proof-contract
                               [:self-hosting-claims
                                :full-language-compiler-self-hosted?]))
                      (true?
                       (get-in proof-contract
                               [:self-hosting-claims
                                :clojure-seed-retired?]))
                      (true? (:release-eligible? release-decision))
                      (true?
                       (:registry-publication-eligible?
                        release-decision))
                      (true? (:release-eligible? summary))
                      (true?
                       (:registry-publication-eligible? summary)))
              (p15-s23-governance-package-release-diagnostic-record
               source-path "P15S23L006"
               {:full-language-compiler-self-hosted?
                (get-in proof-contract
                        [:self-hosting-claims
                         :full-language-compiler-self-hosted?])
                :clojure-seed-retired?
                (get-in proof-contract
                        [:self-hosting-claims
                         :clojure-seed-retired?])
                :release-eligible? (:release-eligible? release-decision)}))
            (when-not (and (:all-queries-passed? auditor-query)
                           (:governance-and-package-policy-satisfied?
                            summary)
                           (= [:clojure-seed-retired]
                              (:release-blockers release-decision)))
              (p15-s23-governance-package-release-diagnostic-record
               source-path "P15S23L007"
               {:all-queries-passed? (:all-queries-passed?
                                      auditor-query)
                :release-blockers (:release-blockers release-decision)}))]))))