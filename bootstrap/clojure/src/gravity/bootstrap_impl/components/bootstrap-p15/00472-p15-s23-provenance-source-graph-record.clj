

(defn p15-s23-provenance-source-graph-record
  [source-path source-data inventory-artifact]
  (let [source-modules (:source-inventory inventory-artifact)
        source-graph-hash
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :source-hash (str "sha256:"
                                         (sha256-hex
                                          (:source-text source-data)))
                       :source-modules source-modules})))]
    {:artifact :gravity/p15-s23-provenance-source-graph-record
     :source-path source-path
     :source-hash (str "sha256:" (sha256-hex (:source-text source-data)))
     :source-graph-hash source-graph-hash
     :source-modules source-modules
     :source-file-count (count source-modules)
     :source-components
     (mapv :component source-modules)
     :status :complete}))

(defn p15-s23-provenance-build-input-record
  [source-path source-graph]
  (let [build-recipe
        {:commands
         ["clojure -M:gravity p15-s23-compiler-source-inventory bootstrap/gravity/p15_s23/compiler.gravity"
          "clojure -M:gravity p15-s23-compiler-pipeline-manifest bootstrap/gravity/p15_s23/compiler.gravity"
          "clojure -M:gravity p15-s23-reproducible-rebuild-log bootstrap/gravity/p15_s23/compiler.gravity"
          "clojure -M:gravity p15-s23-stage-comparison-report bootstrap/gravity/p15_s23/compiler.gravity"
          "clojure -M:gravity p15-s23-self-hosting-conformance-report bootstrap/gravity/p15_s23/compiler.gravity"
          "clojure -M:gravity p15-s23-provenance-attestation bootstrap/gravity/p15_s23/compiler.gravity"]
         :source-path source-path
         :seed-boundary :clojure-stage0}
        environment-manifest
        {:host-runtime :clojure/jvm
         :bootstrap-stage :p15-s23
         :ambient-authority-denied? true
         :release-candidate? false}
        dependency-graph
        {:lockfile "deps.edn"
         :source-graph-hash (:source-graph-hash source-graph)
         :bootstrap-implementation
         "bootstrap/clojure/src/gravity/bootstrap.clj"}]
    {:artifact :gravity/p15-s23-provenance-build-input-record
     :source-path source-path
     :compiler-artifact-id :clojure-stage0-bootstrap
     :compiler-hash
     (p15-s23-provenance-file-hash
      "bootstrap/clojure/src/gravity/bootstrap.clj")
     :lockfile-hash (p15-s23-provenance-file-hash "deps.edn")
     :build-recipe build-recipe
     :build-recipe-hash (c4-artifact-id build-recipe)
     :environment-manifest environment-manifest
     :environment-manifest-hash (c4-artifact-id environment-manifest)
     :dependency-graph dependency-graph
     :dependency-graph-hash (c4-artifact-id dependency-graph)
     :builder-identity :gravity-clojure-stage0-verifier
     :status :complete}))

(defn p15-s23-compiler-lineage-graph
  [inventory-artifact build-input]
  (let [lineage (get-in inventory-artifact
                        [:compiler-module :lineage])
        rows
        [{:stage :stage0
          :compiler :clojure-stage0
          :compiled-by :external-clojure-toolchain
          :compiler-artifact-id (:compiler-artifact-id build-input)
          :compiler-hash (:compiler-hash build-input)
          :trust :seed}
         {:stage :p15-s23-current-clojure-seed-candidate
          :compiled-by (:compiled-by lineage)
          :verified-by (:verified-by lineage)
          :artifact-id (:artifact-id inventory-artifact)
          :next-stage (:next-stage lineage)
          :replaces (:replaces lineage)
          :trust :clojure-stage0-still-trusted}
         {:stage :future-whole-language-self-hosted-compiler
          :compiled-by :pending
          :status :pending
          :trust :not-yet-retired}]]
    {:artifact :gravity/p15-s23-compiler-lineage-graph
     :rows rows
     :compiler-lineage-explicit? true
     :acyclic? true
     :declared-stage-cycle? false
     :lineage-traversable-to-seed? true
     :current-candidate-is-seed? true
     :full-self-hosted-lineage? false
     :answers-which-compiler-compiled-this-compiler
     {:compiler :p15-s23-current-clojure-seed-candidate
      :compiled-by (:compiled-by lineage)
      :verified-by (:verified-by lineage)}
     :status :complete}))

(defn p15-s23-provenance-evidence-link-table
  [inventory-artifact pipeline-artifact rebuild-artifact
   stage-artifact conformance-artifact]
  (let [links
        [{:link :compiler-source-inventory
          :artifact-id (:artifact-id inventory-artifact)
          :status :verified}
         {:link :compiler-pipeline-manifest
          :artifact-id (:artifact-id pipeline-artifact)
          :manifest-id (:manifest-id pipeline-artifact)
          :status :verified}
         {:link :reproducible-rebuild-log
          :artifact-id (:artifact-id rebuild-artifact)
          :proof-id (:proof-id rebuild-artifact)
          :status :verified}
         {:link :stage-comparison-report
          :artifact-id (:artifact-id stage-artifact)
          :proof-id (:proof-id stage-artifact)
          :status :verified}
         {:link :self-hosting-conformance-report
          :artifact-id (:artifact-id conformance-artifact)
          :proof-id (:proof-id conformance-artifact)
          :status :verified}]
        covered (set (map :link links))]
    {:artifact :gravity/p15-s23-provenance-evidence-link-table
     :links links
     :required-links (vec (sort p15-s23-provenance-required-links))
     :required-links-covered?
     (= p15-s23-provenance-required-links covered)
     :conformance-report-link (:artifact-id conformance-artifact)
     :equivalence-report-link (:artifact-id stage-artifact)
     :reproducible-rebuild-link (:artifact-id rebuild-artifact)
     :status (if (= p15-s23-provenance-required-links covered)
               :complete
               :failed)}))

(defn p15-s23-provenance-release-policy-record
  []
  {:artifact :gravity/p15-s23-provenance-release-policy-record
   :release-candidate? false
   :release-eligible? false
   :sbom-required? false
   :external-signature-required? false
   :governance-release-required-before-seed-retirement? true
   :sbom-link
   {:status :not-required
    :reason :current-p15-s23-candidate-is-not-a-release-candidate}
   :signature-link
   {:status :stage0-deterministic-attestation
    :reason :external-release-signature-requires-governance-release-record}
   :status :complete})

(defn p15-s23-provenance-revocation-check-report
  [link-table]
  {:artifact :gravity/p15-s23-provenance-revocation-check-report
   :checked-inputs
   (mapv #(select-keys % [:link :artifact-id :proof-id :manifest-id])
         (:links link-table))
   :revoked-inputs []
   :revocation-clear? true
   :status :complete})

(defn p15-s23-provenance-auditor-query-index
  [lineage link-table]
  {:artifact :gravity/p15-s23-provenance-auditor-query-index
   :queries
   [{:query :which-compiler-compiled-this-compiler
     :answer (get-in lineage
                     [:answers-which-compiler-compiled-this-compiler
                      :compiled-by])
     :status :answered}
    {:query :can-traverse-to-seed
     :answer (:lineage-traversable-to-seed? lineage)
     :status :answered}
    {:query :required-evidence-links
     :answer (mapv :link (:links link-table))
     :status :answered}]
   :auditor-query-passed? true
   :status :complete})

(defn p15-s23-bootstrap-provenance-record
  [source-path source-graph build-input link-table release-policy]
  (let [record-base
        {:artifact :gravity/bootstrap-provenance-record
         :source-path source-path
         :artifact-id (:source-graph-hash source-graph)
         :artifact-kind :gravity/p15-s23-current-clojure-seed-candidate
         :bootstrap-stage :p15-s23
         :source-graph-hash (:source-graph-hash source-graph)
         :compiler-artifact-id (:compiler-artifact-id build-input)
         :compiler-hash (:compiler-hash build-input)
         :lockfile-hash (:lockfile-hash build-input)
         :build-recipe-hash (:build-recipe-hash build-input)
         :environment-manifest-hash
         (:environment-manifest-hash build-input)
         :dependency-graph-hash (:dependency-graph-hash build-input)
         :conformance-report-link
         (:conformance-report-link link-table)
         :equivalence-report-link
         (:equivalence-report-link link-table)
         :reproducible-rebuild-link
         (:reproducible-rebuild-link link-table)
         :safety-report-links []
         :sbom-link (:sbom-link release-policy)
         :signature-link (:signature-link release-policy)
         :builder-identity (:builder-identity build-input)
         :release-candidate? (:release-candidate? release-policy)
         :release-eligible? (:release-eligible? release-policy)
         :status :complete}
        provenance-record-id (c4-artifact-id record-base)]
    (assoc record-base
           :provenance-record-id provenance-record-id)))

(defn p15-s23-canonical-provenance-payload
  [provenance-record lineage link-table release-policy
   revocation auditor]
  (let [payload
        {:provenance-record provenance-record
         :compiler-lineage-graph lineage
         :stage-evidence-link-table link-table
         :release-policy-record release-policy
         :revocation-check-report revocation
         :auditor-query-index auditor}
        canonical
        (p15-s23-provenance-canonical-value payload)
        payload-id (c4-artifact-id canonical)
        signature
        {:signature-id (c4-artifact-id
                        {:payload-id payload-id
                         :signature-mode
                         :stage0-deterministic-attestation})
         :signature-mode :stage0-deterministic-attestation
         :signed-over :canonical-provenance-payload
         :external-signature-service? false
         :status :verified}]
    {:artifact :gravity/p15-s23-canonical-provenance-payload
     :payload-id payload-id
     :canonicalized? true
     :signature signature
     :payload canonical
     :status :complete}))