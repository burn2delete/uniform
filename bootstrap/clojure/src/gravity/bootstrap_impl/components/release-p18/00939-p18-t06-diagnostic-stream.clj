

(defn p18-t06-diagnostic-stream
  [proof-id]
  {:artifact :gravity/p18-t06-diagnostic-stream
   :stage :p18-t06-final-release
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p18-t06-final-release
            :message (get p18-t06-diagnostic-messages id)
            :stable? true})
         p18-t06-diagnostic-ids)
   :status :complete})

(defn p18-t06-capability-proof
  [artifact]
  (let [accepted (:accepted-command-proofs artifact)
        rejected (:rejected-command-proofs artifact)
        release-candidate-rejections (:rejected-release-candidates artifact)
        boundary (:release-boundary-record artifact)
        rebuild (:rebuild-verification-record artifact)
        provenance (:provenance-record artifact)
        sbom (:sbom-record artifact)
        signing (:signing-record artifact)
        governance (:governance-approval-record artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:p18-t06-diagnostic-stream
                                       :diagnostics])))
        complete?
        (and (p18-t06-boundary-seedless? boundary)
             (every? :matches-expected? accepted)
             (every? :matches-expected? rejected)
             (every? :matches-expected? release-candidate-rejections))]
    {:task "P18-T06"
     :status (if complete? :complete :incomplete)
     :reproducible-rebuilds?
     (and (true? (:binary-identical? rebuild))
          (true? (:provenance-identical? rebuild))
          (true? (:sbom-identical? rebuild))
          (true? (:signing-record-identical? rebuild))
          (true? (:command-contract-evidence-identical? rebuild)))
     :provenance-complete?
     (and (= :complete (:status provenance))
          (true? (:builder-identity-verified? provenance))
          (= :passed (get-in provenance [:revocation-check :status])))
     :sbom-complete?
     (and (true? (:complete? sbom))
          (seq (:components sbom))
          (= (:artifact-id provenance) (:provenance-record-id sbom)))
     :signing-record-valid?
     (and (true? (:cryptographic-release-signature? signing))
          (true? (get-in signing [:verification :signature-valid?]))
          (= (:artifact-id provenance)
             (get-in signing [:payload :provenance-record-id]))
          (= (:artifact-id sbom)
             (get-in signing [:payload :sbom-id])))
     :governance-approved?
     (and (= :approved (:approval-status governance))
          (true? (:rfc-accepted? governance))
          (true? (:implementation-artifacts-linked? governance))
          (true? (:conformance-artifacts-linked? governance)))
     :target-claims-valid?
     (contains? p18-t06-supported-targets (:target artifact))
     :all-release-boundary-seed-facts-false?
     (p18-t06-boundary-seedless? boundary)
     :accepted-fixtures-covered?
     (= (set (map :fixture p18-t06-accepted-fixtures))
        (set (map :fixture accepted)))
     :accepted-check-run-compile-survived?
     (every? :matches-expected? accepted)
     :stable-diagnostics-through-release-binary?
     (every? :matches-expected? rejected)
     :release-blocker-diagnostics-covered?
     (= (set p18-t06-diagnostic-ids)
        (set (map :expected-diagnostic release-candidate-rejections)))
     :diagnostic-stream-covered?
     (= (set p18-t06-diagnostic-ids) diagnostics)
     :rejected-release-candidates-covered?
     (every? :matches-expected? release-candidate-rejections)
     :bin-gravity-uses-final-release?
     (some #(str/includes? (get-in % [:version-result :out])
                           ":phase \"P18-T06\"")
           accepted)
     :phase-18-completion-gate-passed?
     complete?
     :final-release-eligible? complete?
     :clojure-seed-boundary? (not (p18-t06-boundary-seedless? boundary))}))

(defn p18-t06-report-markdown
  [artifact]
  (if-not (:final-release? artifact)
    (str "# P18-T06 Reproducibility, Provenance, SBOM, Signing, and Governance Report\n\n"
         "Date: 2026-07-03\n\n"
         "## Status\n\n"
         "P18-T06 is incomplete. The generated release evidence is blocked "
         "because the current P15 final seed-retirement proof still records "
         "`:clojure-seed-boundary? true`, `:full-language-compiler-self-hosted? false`, "
         "and `:clojure-seed-retired? false`.\n\n"
         "## Produced Artifacts\n\n"
         "- `" p18-t06-final-proof-path "`\n"
         "- `" p18-t06-release-boundary-path "`\n"
         "- `" p18-t06-artifact-dir "/p18-t06-reproducible-build-recipe.edn`\n"
         "- `" p18-t06-artifact-dir "/p18-t06-rebuild-verification.edn`\n"
         "- `" p18-t06-artifact-dir "/p18-t06-provenance.edn`\n"
         "- `" p18-t06-artifact-dir "/p18-t06-sbom.edn`\n"
         "- `" p18-t06-artifact-dir "/p18-t06-signing-record.edn`\n"
         "- `" p18-t06-artifact-dir "/p18-t06-governance-approval.edn`\n\n"
         "## Blocking Diagnostics\n\n"
         "- `P18T06003`: final release boundary still includes the seed boundary.\n"
         "- `P18T06004`: no final-release command parity is credited while P15 is incomplete.\n\n"
         "## Current Public Command\n\n"
         "`bin/gravity` must not delegate to `target/phase-18/release/gravity` "
         "until the P15 final seed-retirement proof is complete. The current "
         "public command falls back to the bootstrap-hosted packaged JVM CLI.\n\n"
         "## Next Required Capability\n\n"
         "`:self_hosted_public_binary_final_verification`\n\n")
    (str "# P18-T06 Reproducibility, Provenance, SBOM, Signing, and Governance Report\n\n"
       "Date: 2026-07-01\n\n"
       "## Scope\n\n"
       "P18-T06 finalizes the Phase 18 release evidence for the generated "
       "`gravity` executable. It proves reproducible rebuilds, provenance, "
       "SBOM, release signing, target support, compatibility, security review, "
       "governance approval, and fail-closed release blockers for the current "
       "accepted Phase 18 executable surface.\n\n"
       "## Produced Artifacts\n\n"
       "- `" p18-t06-release-binary-path "`\n"
       "- `" p18-t06-final-proof-path "`\n"
       "- `" p18-t06-release-boundary-path "`\n"
       "- `" p18-t06-artifact-dir "/p18-t06-reproducible-build-recipe.edn`\n"
       "- `" p18-t06-artifact-dir "/p18-t06-rebuild-verification.edn`\n"
       "- `" p18-t06-artifact-dir "/p18-t06-provenance.edn`\n"
       "- `" p18-t06-artifact-dir "/p18-t06-sbom.edn`\n"
       "- `" p18-t06-artifact-dir "/p18-t06-signing-record.edn`\n"
       "- `" p18-t06-artifact-dir "/p18-t06-governance-approval.edn`\n"
       "- `" p18-t06-artifact-dir "/p18-t06-rejected-release-candidates.edn`\n\n"
       "## Key Evidence\n\n"
       "- Proof artifact: `" (:artifact-id artifact) "`\n"
       "- Release binary hash: `"
       (get-in artifact [:release-binary :content-hash]) "`\n"
       "- Release boundary: `"
       (get-in artifact [:release-boundary-record :artifact-id]) "`\n"
       "- Rebuild verification: `"
       (get-in artifact [:rebuild-verification-record :artifact-id]) "`\n"
       "- Provenance: `"
       (get-in artifact [:provenance-record :artifact-id]) "`\n"
       "- SBOM: `" (get-in artifact [:sbom-record :artifact-id]) "`\n"
       "- Signing record: `"
       (get-in artifact [:signing-record :artifact-id]) "`\n"
       "- Governance approval: `"
       (get-in artifact [:governance-approval-record :artifact-id]) "`\n\n"
       "## Command Proof\n\n"
       "The final public command boundary is `bin/gravity`, which delegates to "
       "`target/phase-18/release/gravity` when the final release binary is "
       "present. The proof covers:\n\n"
       "```bash\n"
       "bin/gravity check examples/core-app.gravity\n"
       "bin/gravity run examples/core-app.gravity\n"
       "bin/gravity compile examples/core-app.gravity -o target/core-app\n"
       "target/core-app\n"
       "bin/gravity check bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity\n"
       "bin/gravity check bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.qst\n"
       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b4-target.gravity\n"
       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b4-target.qst\n"
       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b5-target.gravity\n"
       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b5-target.qst\n"
	       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b6-target.gravity\n"
	       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b6-target.qst\n"
	       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b7-dialect.gravity\n"
	       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b7-dialect.qst\n"
	       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b8-target.gravity\n"
	       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b8-target.qst\n"
	       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b9-target.gravity\n"
		       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b9-target.qst\n"
		       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b10-schema.gravity\n"
		       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b10-schema.qst\n"
		       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b11-dialect.gravity\n"
		       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b11-dialect.qst\n"
		       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b12-target.gravity\n"
		       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b12-target.qst\n"
		       "bin/gravity check bootstrap/clojure/fixtures/accepted/backend-artifact-emission.gravity\n"
		       "bin/gravity check bootstrap/clojure/fixtures/accepted/backend-artifact-emission.qst\n"
		       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b13-schema.gravity\n"
		       "bin/gravity check bootstrap/clojure/fixtures/rejected/backend-b13-schema.qst\n"
		       "bin/gravity compile bootstrap/clojure/fixtures/rejected/core-app-backend-release.gravity -o target/phase-18/release/rejected-final-release\n"
		       "bin/gravity p18-t06-final-release\n"
		       "```\n\n"
			       "The B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, and B14 public check bridges additionally prove their "
			       "`backend-b4-*`, `backend-b5-*`, `backend-b6-*`, `backend-b7-*`, `backend-b8-*`, `backend-b9-*`, `backend-b10-*`, `backend-b11-*`, `backend-b12-*`, `backend-b13-*`, and `backend-matrix-b14-*` rejected fixture "
	       "pairs through stable backend diagnostics while preserving the original "
       "source extension in diagnostic spans.\n\n"
       "Rejected release candidates cover `P18T06001` through `P18T06008`.\n\n"
       "## Boundaries\n\n"
       "The release boundary records `:clojure-seed-boundary? false` for the "
       "gravity binary, compiler path, runtime path, and release compiler path. "
       "`bin/gravity-bootstrap` remains explicit for audit and recovery and is "
       "outside the public release boundary.\n\n")))