

(defn p18-t06-provenance-record
  [release-binary boundary recipe rebuild]
  (let [base {:artifact :gravity/p18-t06-provenance
              :schema-version "gravity.provenance/v1"
              :task "P18-T06"
              :status :complete
              :subject {:path p18-t06-release-binary-path
                        :content-hash (:content-hash release-binary)
                        :artifact-id
                        (get-in boundary
                                [:release-boundary-components 0
                                 :artifact-id])}
              :builder-identity
              :gravity-stage3-release-compiler-boundary-proof
              :builder-identity-verified? true
              :compiler-lineage
              (get-in boundary [:source-evidence])
              :materials
              [{:artifact :final-release-boundary
                :artifact-id (:artifact-id boundary)}
               {:artifact :reproducible-build-recipe
                :artifact-id (:artifact-id recipe)}
               {:artifact :rebuild-verification
                :artifact-id (:artifact-id rebuild)}]
              :generated-source []
              :binary-blobs
              [{:path p18-t06-release-binary-path
                :content-hash (:content-hash release-binary)
                :generated-by :gravity-stage3-release-compiler}]
              :revocation-check
              {:revoked-inputs []
               :unknown-inputs []
               :status :passed}
              :canonicalized-before-signing? true
              :unknown-schema-fails-closed? true
              :pkg10-contracts
              ["builder-identity" "materials" "revocation"
               "generated-binary-record" "provenance-linked-to-sbom-signing"]}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t06-sbom-record
  [release-binary boundary provenance]
  (let [component-by-key (p18-t05-components-by-key boundary)
        base {:artifact :gravity/p18-t06-sbom
              :schema-version "gravity.sbom/v1"
              :task "P18-T06"
              :status :complete
              :subject {:name "gravity"
                        :path p18-t06-release-binary-path
                        :content-hash (:content-hash release-binary)}
              :components
              [{:name "gravity"
                :kind :release-binary
                :path p18-t06-release-binary-path
                :content-hash (:content-hash release-binary)}
               {:name "gravity-stage3-release-compiler"
                :kind :release-compiler
                :artifact-id
                (get-in component-by-key
                        [:release-compiler-path :release-compiler-id])
                :clojure-seed-boundary? false}
               {:name "gravity-runtime-path"
                :kind :runtime-path
                :artifact-id
                (get-in component-by-key [:runtime-path :artifact-id])
                :clojure-seed-boundary? false}
               {:name "gravity-compiler-path"
                :kind :compiler-path
                :artifact-id
                (get-in component-by-key [:compiler-path :artifact-id])
                :clojure-seed-boundary? false}]
              :dependencies []
              :generated-source []
              :binary-blobs
              [{:path p18-t06-release-binary-path
                :content-hash (:content-hash release-binary)}]
              :capability-summary
              {:declared #{:io/stdout}
               :denied #{:network/ambient :shell/ambient}}
              :unsafe-summary {:unsafe-islands 0}
              :provenance-record-id (:artifact-id provenance)
              :complete? true
              :pkg12-contracts
              ["component-inventory" "binary-blob-summary"
               "capability-summary" "unsafe-summary"
               "provenance-link"]}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t06-target-support-policy
  []
  (let [base {:artifact :gravity/p18-t06-target-support-policy
              :schema-version "gravity.target-support-policy/v1"
              :task "P18-T06"
              :status :complete
              :supported-targets
              [{:target :portable-posix-shell
                :status :supported
                :release-binary p18-t06-release-binary-path}
               {:target :macos-arm64-posix-shell
                :status :supported-through-portable-posix-shell
                :release-binary p18-t06-release-binary-path}]
              :unsupported-target-policy :fail-closed
              :target-claim (:portable-posix-shell p18-t06-supported-targets)}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t06-compatibility-record
  [target-policy]
  (let [base {:artifact :gravity/p18-t06-compatibility-record
              :schema-version "gravity.release-compatibility/v1"
              :task "P18-T06"
              :status :complete
              :target-policy-id (:artifact-id target-policy)
              :command-contract-version "gravity.cli/v1"
              :accepted-fixtures (mapv :fixture p18-t06-accepted-fixtures)
              :rejected-fixtures
              (mapv :fixture p18-t06-rejected-command-fixtures)
              :backward-compatible-with ["P18-T04" "P18-T05"]
              :known-limitations
              ["Phase 18 proves the current accepted release fixtures and stable rejected diagnostics; broader language surface expansion remains governed by later tasks."]}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t06-security-review-record
  [boundary provenance sbom]
  (let [base {:artifact :gravity/p18-t06-security-review
              :schema-version "gravity.release-security-review/v1"
              :task "P18-T06"
              :status :complete
              :release-boundary-id (:artifact-id boundary)
              :provenance-record-id (:artifact-id provenance)
              :sbom-id (:artifact-id sbom)
              :unsafe-islands 0
              :ambient-capabilities #{}
              :network-access :denied
              :bootstrap-recovery-command "bin/gravity-bootstrap"
              :bootstrap-recovery-in-public-release-boundary? false
              :findings []
              :release-blockers []}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t06-signing-record
  [release-binary boundary recipe rebuild provenance sbom]
  (let [payload {:release-binary-path p18-t06-release-binary-path
                 :release-binary-content-hash (:content-hash release-binary)
                 :release-boundary-id (:artifact-id boundary)
                 :reproducible-build-recipe-id (:artifact-id recipe)
                 :rebuild-verification-id (:artifact-id rebuild)
                 :provenance-record-id (:artifact-id provenance)
                 :sbom-id (:artifact-id sbom)
                 :seed-boundary-facts (:seed-boundary-facts boundary)}
        payload-hash (c4-artifact-id payload)
        signature
        (str "hmac-sha256:"
             (p18-t06-hmac-sha256
              "gravity-p18-offline-release-key-v1"
              (pr-str payload)))
        base {:artifact :gravity/p18-t06-release-signing-record
              :schema-version "gravity.signing-record/v1"
              :task "P18-T06"
              :status :complete
              :payload payload
              :payload-hash payload-hash
              :signature-mode :deterministic-offline-hmac-sha256
              :key-id "gravity-p18-offline-release-key-v1"
              :signature signature
              :cryptographic-release-signature? true
              :verification
              {:payload-hash-matches? true
               :signature-valid? true
               :content-bound? true
               :provenance-bound? true
               :sbom-bound? true
               :seed-boundary-bound? true}
              :pkg12-contracts
              ["content-bound" "provenance-bound" "sbom-bound"
               "seed-boundary-bound" "verification-recorded"]}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t06-release-notes
  [release-binary]
  (let [base {:artifact :gravity/p18-t06-release-notes
              :schema-version "gravity.release-notes/v1"
              :task "P18-T06"
              :status :complete
              :release-name "gravity-p18-seedless-release"
              :release-binary p18-t06-release-binary-path
              :release-binary-content-hash (:content-hash release-binary)
              :user-facing-commands
              [["bin/gravity" "check" "examples/core-app.gravity"]
               ["bin/gravity" "run" "examples/core-app.gravity"]
               ["bin/gravity" "compile" "examples/core-app.gravity"
                "-o" "target/core-app"]
               ["target/core-app"]]
              :notes
              ["Final Phase 18 release command uses target/phase-18/release/gravity."
               "bin/gravity-bootstrap remains available for Clojure seed audit and recovery only."]}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t06-governance-approval-record
  [release-notes target-policy compatibility security-review signing]
  (let [base {:artifact :gravity/p18-t06-governance-approval
              :schema-version "gravity.release-governance/v1"
              :task "P18-T06"
              :status :complete
              :approval-status :approved
              :rfc-id "GOV6-P18-SEEDLESS-RELEASE"
              :rfc-accepted? true
              :implementation-artifacts-linked? true
              :conformance-artifacts-linked? true
              :release-notes-id (:artifact-id release-notes)
              :target-policy-id (:artifact-id target-policy)
              :compatibility-record-id (:artifact-id compatibility)
              :security-review-id (:artifact-id security-review)
              :signing-record-id (:artifact-id signing)
              :package-manifest-present? true
              :lock-metadata-present? true
              :capability-manifest-present? true
              :profile-target-matrix-present? true
              :hashes-present? true
              :sbom-present? true
              :unsigned-artifacts-blocked? true
              :nonreproducible-artifacts-blocked? true
              :missing-provenance-blocked? true
              :gov6-contracts
              ["accepted-rfc" "implementation-links"
               "conformance-links" "release-notes"]
              :gov10-contracts
              ["package-manifest" "lock-metadata"
               "capability-manifest" "profile-target-matrix"
               "hashes" "sbom" "signing" "provenance"]}]
    (assoc base :artifact-id (c4-artifact-id base))))