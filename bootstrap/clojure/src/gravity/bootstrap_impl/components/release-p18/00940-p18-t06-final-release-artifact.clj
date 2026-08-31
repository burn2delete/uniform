

(defn p18-t06-final-release-artifact!
  []
  (let [p18-t05-artifact (p18-t06-existing-p18-t05-artifact)
        p15-final-proof (p18-t06-current-p15-final-seed-proof)
        p15-seed-retired?
        (p18-t06-p15-final-seed-retired? p15-final-proof)
        release-binary (p18-t06-write-final-release-binary!)
        boundary-base (p18-t06-final-release-boundary-record
                       release-binary p18-t05-artifact)
        boundary (if p15-seed-retired?
                   boundary-base
                   (p18-t06-blocked-release-boundary-record
                    boundary-base p15-final-proof))
        recipe (p18-t06-reproducible-build-recipe
                release-binary boundary p18-t05-artifact)
        rebuild (p18-t06-rebuild-verification-record release-binary recipe)
        provenance (p18-t06-provenance-record
                    release-binary boundary recipe rebuild)
        sbom (p18-t06-sbom-record release-binary boundary provenance)
        signing (p18-t06-signing-record
                 release-binary boundary recipe rebuild provenance sbom)
        target-policy (p18-t06-target-support-policy)
        compatibility (p18-t06-compatibility-record target-policy)
        security-review (p18-t06-security-review-record
                         boundary provenance sbom)
        release-notes (p18-t06-release-notes release-binary)
        governance (p18-t06-governance-approval-record
                    release-notes target-policy compatibility
                    security-review signing)
        accepted-records
        (if p15-seed-retired?
          (mapv p18-t06-accepted-command-record p18-t06-accepted-fixtures)
          [])
        rejected-command-records
        (if p15-seed-retired?
          (mapv p18-t06-rejected-command-record
                p18-t06-rejected-command-fixtures)
          [])
        candidate-base
        {:kind :gravity/p18-t06-final-release-proof
         :task "P18-T06"
         :status (if p15-seed-retired? :complete :incomplete)
         :phase :binary-distribution-and-seedless-release
         :target :portable-posix-shell
         :release-binary release-binary
         :p15-final-seed-retirement-proof
         (select-keys p15-final-proof
                      [:kind :artifact :artifact-id :proof-id :status
                       :full-language-compiler-self-hosted?
                       :clojure-seed-retired?
                       :clojure-seed-boundary?
                       :next-required-capability])
         :release-boundary-record boundary
         :reproducible-build-recipe recipe
         :rebuild-verification-record rebuild
         :provenance-record provenance
         :sbom-record sbom
         :signing-record signing
         :target-support-policy target-policy
         :compatibility-record compatibility
         :security-review-record security-review
         :release-notes release-notes
         :governance-approval-record governance
         :accepted-command-proofs accepted-records
         :rejected-command-proofs rejected-command-records
         :final-release? p15-seed-retired?
         :seedless-release? p15-seed-retired?
         :clojure-seed-boundary? (not p15-seed-retired?)
         :next-required-capability
         (if p15-seed-retired?
           :none
           :self_hosted_public_binary_final_verification)}
        diagnostics (p18-t06-final-release-diagnostics
                     :p18-t06-final-release candidate-base)
        rejected-release-candidates
        (p18-t06-rejected-release-candidates candidate-base)
        proof-id
        (c4-artifact-id
         {:release-binary (:content-hash release-binary)
          :boundary-id (:artifact-id boundary)
          :rebuild-id (:artifact-id rebuild)
          :provenance-id (:artifact-id provenance)
          :sbom-id (:artifact-id sbom)
          :signing-id (:artifact-id signing)
          :governance-id (:artifact-id governance)})
        artifact-base
        (assoc candidate-base
               :rejected-release-candidates rejected-release-candidates
               :p18-t06-diagnostic-stream
               (p18-t06-diagnostic-stream proof-id)
               :diagnostics diagnostics)
        proof (p18-t06-capability-proof artifact-base)
        artifact (assoc artifact-base
                        :capability-based-proof proof
                        :artifact-id
                        (c4-artifact-id
                         (assoc artifact-base
                                :capability-based-proof proof)))]
    artifact))

(defn p18-t06-write-final-release-artifacts!
  []
  (let [artifact (p18-t06-final-release-artifact!)]
    (p18-t02-write-edn! p18-t06-final-proof-path artifact)
    (p18-t02-write-edn! p18-t06-release-boundary-path
                        (:release-boundary-record artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-reproducible-build-recipe.edn")
     (:reproducible-build-recipe artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-rebuild-verification.edn")
     (:rebuild-verification-record artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-provenance.edn")
     (:provenance-record artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-sbom.edn")
     (:sbom-record artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-signing-record.edn")
     (:signing-record artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-release-notes.edn")
     (:release-notes artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-target-support-policy.edn")
     (:target-support-policy artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-compatibility-record.edn")
     (:compatibility-record artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-security-review.edn")
     (:security-review-record artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-governance-approval.edn")
     (:governance-approval-record artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-accepted-command-proofs.edn")
     (:accepted-command-proofs artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-rejected-command-proofs.edn")
     (:rejected-command-proofs artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-rejected-release-candidates.edn")
     (:rejected-release-candidates artifact))
    (p18-t02-write-edn!
     (str p18-t06-artifact-dir "/p18-t06-diagnostic-stream.edn")
     (:p18-t06-diagnostic-stream artifact))
    (p18-ensure-dir! (.getParent (java.io.File. p18-t06-report-path)))
    (spit p18-t06-report-path (p18-t06-report-markdown artifact))
    artifact))

(def p18-t04-public-self-host-verify-proof-path
  (str p18-t04-artifact-dir
       "/p18-t04-public-self-host-verify-command-proof.edn"))

(def p18-t04-public-self-host-verify-diagnostics-path
  (str p18-t04-artifact-dir
       "/p18-t04-public-self-host-verify-diagnostics.edn"))

(defn p18-t04-self-host-verify-compiler-source
  []
  (p18-t04-semantics/compiler-source
   {:source-path p15-s23-compiler-source-path
    :source-extension gravity-source-extension
    :source-kind gravity-source-kind
    :source-exists? #(.isFile (java.io.File. %))
    :source-extensions co-canonical-source-extensions}))

(defn p18-t04-self-host-verify-complete?
  [p15-final-proof p18-final-proof]
  (p18-t04-semantics/complete? p15-final-proof p18-final-proof))

(defn p18-t04-public-self-host-verify-proof
  [artifact]
  (p18-t04-semantics/proof
   {:artifact artifact
    :complete? (= :complete (:status artifact))
    :source-path p15-s23-compiler-source-path
    :source-extension gravity-source-extension
    :p15-final-proof (:p15-final-seed-retirement-proof artifact)
    :p18-final-proof (:p18-final-release-proof artifact)
    :diagnostics (:diagnostics artifact)}))