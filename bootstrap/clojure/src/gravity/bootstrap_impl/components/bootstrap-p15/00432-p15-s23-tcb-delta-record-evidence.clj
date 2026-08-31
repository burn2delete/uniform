

(defn p15-s23-tcb-delta-record-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-tcb-delta-record-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :tcb-delta-record-id
           (get-in artifact [:tcb-delta-record :tcb-delta-record-id])
           :baseline-trusted-count
           (get-in artifact [:trust-reduction-summary
                             :baseline-trusted-count])
           :current-residual-trusted-count
           (get-in artifact [:trust-reduction-summary
                             :current-residual-trusted-count])
           :whole-language-tcb-reduced?
           (get-in artifact [:trust-reduction-summary
                             :whole-language-tcb-reduced?])
           :clojure-seed-still-trusted?
           (get-in artifact [:residual-trust-boundary-record
                             :clojure-seed-still-trusted?])
           :no-unaccounted-trusted-components?
           (get-in artifact [:tcb-auditor-query-record
                             :no-unaccounted-trusted-components?])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

(defn p15-s23-unsafe-audit-report-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-unsafe-audit-report-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :unsafe-audit-report-id
           (get-in artifact [:unsafe-audit-report :unsafe-audit-report-id])
           :unsafe-island-count
           (get-in artifact [:unsafe-island-index :unsafe-island-count])
           :unsafe-operation-count
           (get-in artifact [:unsafe-operation-inventory
                             :unsafe-operation-count])
           :package-safety-metadata-id
           (get-in artifact [:package-safety-metadata
                             :package-safety-metadata-id])
           :review-state
           (get-in artifact [:review-and-revalidation-record
                             :review-state])
           :external-seed-boundaries-separated?
           (get-in artifact [:external-seed-boundary-audit
                             :host-trust-boundaries-not-counted-as-safe-gravity?])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

(defn p15-s23-whole-language-compiler-artifact-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-whole-language-compiler-artifact-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :compiler-artifact-id
           (get-in artifact [:compiler-artifact-manifest
                             :compiler-artifact-id])
           :stage-support-level
           (get-in artifact [:stage-support-matrix
                             :support-level])
           :accepted-fixture-count
           (get-in artifact [:p15-s23-whole-language-compiler-results
                             :accepted-fixtures])
           :rejected-fixture-count
           (get-in artifact [:p15-s23-whole-language-compiler-results
                             :rejected-fixtures])
           :accepted-app-output
           (get-in artifact [:accepted-application-compile-record
                             :stdout])
           :diagnostics
           (get-in artifact [:rejected-application-diagnostic-record
                             :diagnostics])
           :residual-clojure-boundary?
           (get-in artifact [:residual-trusted-boundary-record
                             :clojure-stage0-still-required?])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

(defn p15-s23-governance-and-package-release-record-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-governance-and-package-release-record-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :governance-package-record-id
           (get-in artifact [:governance-and-package-release-record
                             :governance-package-record-id])
           :rfc-id (get-in artifact [:rfc-record :rfc-id])
           :package-release-id
           (get-in artifact [:package-release-record :package-release-id])
           :registry-decision
           (get-in artifact [:registry-policy-decision
                             :decision])
           :governance-and-package-policy-satisfied?
           (get-in artifact [:release-decision-record
                             :governance-and-package-policy-satisfied?])
           :release-eligible?
           (get-in artifact [:release-decision-record
                             :release-eligible?])
           :registry-publication-eligible?
           (get-in artifact [:registry-policy-decision
                             :registry-publication-eligible?])
           :release-blockers
           (get-in artifact [:release-decision-record
                             :release-blockers])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

	(declare p15-s23-stage2-compiler-nucleus-evidence)
	(declare p15-s23-stage2-plan-emitter-evidence)
	(declare p15-s23-stage2-runtime-kernel-evidence)
	(declare p15-s23-stage2-runtime-executor-evidence)
	(declare p15-s23-stage2-front-end-executor-evidence)
	(declare p15-s23-stage2-source-front-end-evidence)
	(declare p15-s23-stage2-compiler-driver-evidence)
	(declare p15-s23-stage2-whole-language-compiler-evidence)
	(declare p15-s23-stage3-seedless-compiler-candidate-evidence)
	(declare p15-s23-stage3-equivalence-bundle-evidence)
	(declare p15-s23-stage3-self-hosted-application-evidence)
	(declare p15-s23-final-seed-retirement-evidence)
	(declare p15-s23-final-seed-retirement-evidence-from-evidence)
	(declare p15-s23-cached-source-artifact)

(defn p15-s23-artifact-file-evidence-summary
  [path proof-keys]
  (when (.isFile (java.io.File. path))
    (try
      (let [artifact (edn/read-string (slurp path))
            proof (:capability-based-proof artifact)]
        (merge
         {:status :verified
          :artifact (:kind artifact)
          :kind (:kind artifact)
          :artifact-id (:artifact-id artifact)
          :proof-id (:proof-id artifact)
          :source-path (:source-path artifact)
          :artifact-path path
          :capability-based-proof proof}
         (select-keys proof proof-keys)
         (select-keys artifact
                      [:full-language-compiler-self-hosted?
                       :clojure-seed-retired?
                       :clojure-seed-boundary?
                       :inventory-id
                       :source-inventory
                       :manifest-id
                       :compiler-pipeline-manifest
                       :compiler-artifact-manifest
                       :accepted-application-compile-record
                       :rejected-application-diagnostic-record
                       :residual-trusted-boundary-record
                       :runtime-capability-manifest
                       :runtime-manifest
                       :artifact-identity-comparison
                       :stage-support-conformance-record
                       :bootstrap-provenance-record
                       :compiler-lineage-graph
                       :canonical-provenance-payload
                       :revocation-check-report
                       :tcb-delta-record
                       :residual-trust-boundary-record
                       :unsafe-audit-report
                       :unsafe-island-index
                       :package-safety-metadata
                       :rule-record
                       :accepted-record
                       :rejected-record
                       :boundary-record
                       :accepted-output-comparison
                       :compiled-plan-execution-trace
                       :rejected-app-diagnostic-records])))
      (catch Exception _
        nil))))

(defn p15-s23-stage3-seedless-compiler-candidate-artifact-evidence
  []
  (p15-s23-artifact-file-evidence-summary
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage3-seedless-compiler-candidate.edn"
   [:seedless-compiler-candidate-present?
    :compiler-path-seedless?
    :accepted-output-equivalent?
    :rejected-diagnostics-equivalent?
    :clojure-stage0-verifier-absent?
    :clojure-stage0-release-compiler-absent?]))