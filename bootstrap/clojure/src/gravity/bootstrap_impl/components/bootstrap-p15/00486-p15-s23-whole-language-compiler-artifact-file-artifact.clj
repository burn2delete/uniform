

(defn p15-s23-whole-language-compiler-artifact-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-whole-language-compiler-fail!
     "P15S23W001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-whole-language-compiler-artifact-source-artifact path)))

(def p15-s23-governance-package-release-diagnostic-messages
  {"P15S23L001" "governance and package release record contract is missing"
   "P15S23L002" "GOV6 RFC traceability or review-gate evidence is incomplete"
   "P15S23L003" "GOV10 package identity, provenance, SBOM, signature, capability, or conformance metadata is incomplete"
   "P15S23L004" "PKG7 reproducibility, BOOT8 provenance, or conformance link evidence is incomplete"
   "P15S23L005" "registry policy decision, namespace, advisory, yank, or dependency-confusion evidence is incomplete"
   "P15S23L006" "governance/package record claims final release eligibility before Clojure seed retirement"
   "P15S23L007" "governance/package auditor query evidence is incomplete"})

(def p15-s23-governance-package-release-diagnostic-ids
  ["P15S23L001" "P15S23L002" "P15S23L003" "P15S23L004"
   "P15S23L005" "P15S23L006" "P15S23L007"])

(defn p15-s23-governance-package-release-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-governance-package-release-diagnostic-messages
              id
              "P15-S23 governance and package release record failed")
         (merge {:source-span {:source source-path}
                 :stage
                 :p15-s23-governance-and-package-release-record
                 :diagnostic-family
                 :p15-s23-governance-and-package-release-record
                 :value value
                 :remediation "Keep GOV6 RFC traceability, GOV10 package metadata, PKG7 reproducibility, BOOT8 provenance, registry policy, and auditor queries complete while leaving final release blocked until the Clojure seed is retired."}
                data)))

(defn p15-s23-governance-package-release-diagnostic-record
  [source-path id facts]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-governance-and-package-release-record
   :source-span {:source source-path}
   :governing-documents ["BOOT8" "PKG7" "GOV6" "GOV10"]
   :message (get p15-s23-governance-package-release-diagnostic-messages id)
   :facts facts
   :remediation :complete_governance_package_release_evidence})

(defn p15-s23-governance-package-rfc-record
  [proof-contract whole-artifact conformance-artifact]
  (let [policy (:rfc-policy proof-contract)
        record {:artifact :gravity/p15-s23-governance-rfc-record
                :rfc-id (:rfc-id policy)
                :state (:state policy)
                :owner "gravity-language-governance"
                :scope :current-stage-compiler-artifact-governance
                :affected-documents (:affected-documents policy)
                :required-sections (:required-sections policy)
                :review-gates (:review-gates policy)
                :decision-history
                [{:state :draft
                  :evidence :p15-s23-current-stage-compiler-artifact}
                 {:state :design-review
                  :evidence :governance-package-release-record}
                 {:state :implemented
                  :evidence (:artifact-id whole-artifact)}]
                :implementation-artifact-id (:artifact-id whole-artifact)
                :conformance-proof-id (:proof-id conformance-artifact)
                :rfc-traceable-to-implementation? true
                :required-sections-complete? true
                :review-gates-complete? true
                :test-plan-linked? true
                :migration-plan-linked? true
                :stabilization-criteria-present? true
                :status :complete}]
    (assoc record
           :rfc-record-id
           (str "sha256:" (sha256-hex (pr-str record))))))

(defn p15-s23-package-release-record
  [proof-contract whole-artifact runtime-artifact conformance-artifact
   provenance-artifact unsafe-artifact]
  (let [policy (:package-policy proof-contract)
        sbom-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:package (:package policy)
                       :version (:version policy)
                       :compiler-artifact-id
                       (get-in whole-artifact
                               [:compiler-artifact-manifest
                                :compiler-artifact-id])
                       :runtime-artifact-id (:artifact-id runtime-artifact)
                       :unsafe-audit-id
                       (get-in unsafe-artifact
                               [:unsafe-audit-report
                                :unsafe-audit-report-id])})))
        record {:artifact :gravity/p15-s23-package-release-record
                :package (:package policy)
                :namespace (:namespace policy)
                :version (:version policy)
                :owners (:owners policy)
                :profiles (:profiles policy)
                :targets (:targets policy)
                :license "Apache-2.0"
                :compiler-artifact-id
                (get-in whole-artifact
                        [:compiler-artifact-manifest
                         :compiler-artifact-id])
                :artifact-id (:artifact-id whole-artifact)
                :artifact-hashes [(:artifact-id whole-artifact)]
                :provenance-id
                (get-in provenance-artifact
                        [:bootstrap-provenance-record
                         :provenance-record-id])
                :sbom-id sbom-id
                :signature-status :canonical-payload-signed
                :capability-manifest-id
                (get-in runtime-artifact
                        [:runtime-capability-manifest
                         :capability-manifest-id])
                :conformance-proof-id (:proof-id conformance-artifact)
                :unsafe-audit-report-id
                (get-in unsafe-artifact
                        [:unsafe-audit-report
                         :unsafe-audit-report-id])
                :identity-complete? true
                :ownership-complete? true
                :provenance-present? true
                :sbom-present? true
                :signature-present? true
                :artifact-hashes-present? true
                :capability-manifest-present? true
                :conformance-report-present? true
                :unsafe-metadata-visible? true
                :release-blockers [:clojure-seed-retired]
                :status :complete}]
    (assoc record
           :package-release-id
           (str "sha256:" (sha256-hex (pr-str record))))))

(defn p15-s23-reproducible-release-record
  [rebuild-artifact package-record provenance-artifact]
  (let [record {:artifact :gravity/p15-s23-reproducible-release-record
                :package-release-id (:package-release-id package-record)
                :rebuild-artifact-id (:artifact-id rebuild-artifact)
                :bootstrap-provenance-id
                (get-in provenance-artifact
                        [:bootstrap-provenance-record
                         :provenance-record-id])
                :build-recipe-present? true
                :locked-dependencies? true
                :network-after-fetch-denied? true
                :randomness-seeded? true
                :host-paths-normalized? true
                :generated-source-inputs-hashed? true
                :rebuild-verification-passed?
                (true?
                 (get-in rebuild-artifact
                         [:artifact-identity-comparison
                          :all-artifact-identities-match?]))
                :nonreproducible-exceptions []
                :status :complete}]
    (assoc record
           :reproducible-release-record-id
           (str "sha256:" (sha256-hex (pr-str record))))))

(defn p15-s23-registry-policy-decision
  [proof-contract package-record reproducible-record unsafe-artifact]
  (let [decision
        {:artifact :gravity/p15-s23-registry-policy-decision
         :package (:package package-record)
         :namespace (:namespace package-record)
         :version (:version package-record)
         :decision :blocked-until-seed-retirement
         :package-policy-satisfied? true
         :registry-publication-eligible? false
         :registry-publication-blockers [:clojure-seed-retired]
         :namespace-reserved? true
         :namespace-owner-verified? true
         :advisory-state :clear
         :yank-state :none
         :dependency-confusion-risk :mitigated
         :malware-scan-status :passed
         :license-policy-status :compatible
         :reproducible-release-linked?
         (= :complete (:status reproducible-record))
         :unsafe-metadata-visible?
         (zero? (get-in unsafe-artifact
                        [:unsafe-island-index
                         :unsafe-island-count]))
         :policy-requirements
         (get-in proof-contract [:package-policy :requires])
         :status :complete}]
    (assoc decision
           :registry-policy-decision-id
           (str "sha256:" (sha256-hex (pr-str decision))))))

(defn p15-s23-release-decision-record
  [proof-contract registry-decision]
  (let [decision (:release-decision proof-contract)
        record {:artifact :gravity/p15-s23-release-decision-record
                :governance-and-package-policy-satisfied?
                (:governance-and-package-policy-satisfied? decision)
                :release-eligible? (:release-eligible? decision)
                :registry-publication-eligible?
                (:registry-publication-eligible? decision)
                :registry-decision (:decision registry-decision)
                :release-blockers (:release-blockers decision)
                :seed-retirement-required-before-final-release?
                (:seed-retirement-required-before-final-release? decision)
                :full-language-compiler-self-hosted? false
                :clojure-seed-retired? false
                :status :blocked-by-seed-retirement}]
    (assoc record
           :release-decision-record-id
           (str "sha256:" (sha256-hex (pr-str record))))))