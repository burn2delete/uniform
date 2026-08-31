

(defn p15-s23-runtime-manifest-capability-enforcement-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-runtime-manifest-capability-enforcement-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :runtime-family
           (get-in artifact [:runtime-manifest :family])
           :decision-count
           (get-in artifact
                   [:p15-s23-runtime-manifest-capability-results
                    :decision-count])
           :authority-family-count
           (get-in artifact
                   [:p15-s23-runtime-manifest-capability-results
                    :authority-family-count])
           :deny-by-default?
           (get-in artifact
                   [:runtime-capability-manifest :deny-by-default?])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

(defn p15-s23-accepted-app-execution-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-accepted-app-execution-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :accepted-app-path
           (get-in artifact [:accepted-app-artifact :source :path])
           :accepted-stdout
           (get-in artifact [:accepted-output-comparison
                             :accepted-stdout])
           :compiled-plan-id
           (get-in artifact [:compiled-plan-execution-trace
                             :compiled-plan-id])
           :runtime-capability-proof-id
           (get-in artifact [:runtime-capability-artifact :proof-id])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)
           :clojure-instruction-runner?
           (get-in artifact [:trusted-boundary-record
                             :clojure-instruction-runner?])})
        (catch Exception _
          nil)))))

(defn p15-s23-rejected-app-diagnostic-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-rejected-app-diagnostic-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :rejected-fixture-count
           (get-in artifact
                   [:p15-s23-rejected-app-diagnostic-results
                    :verified-rejected-fixtures])
           :source-diagnostics
           (mapv :diagnostic (:rejected-app-diagnostic-records
                              artifact))
           :accepted-app-proof-id
           (get-in artifact [:accepted-app-execution-artifact :proof-id])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)
           :clojure-instruction-runner?
           (get-in artifact [:trusted-boundary-record
                             :clojure-instruction-runner?])})
        (catch Exception _
          nil)))))

(defn p15-s23-reproducible-rebuild-log-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-reproducible-rebuild-log-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :rebuild-stage-count
           (get-in artifact
                   [:p15-s23-reproducible-rebuild-results
                    :rebuild-stage-count])
           :all-artifact-identities-match?
           (get-in artifact
                   [:artifact-identity-comparison
                    :all-artifact-identities-match?])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)
           :seed-boundary
           (get-in artifact [:environment-provenance-record
                             :seed-boundary])})
        (catch Exception _
          nil)))))

(defn p15-s23-stage-comparison-report-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-stage-comparison-report-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :stage-comparison-row-count
           (get-in artifact
                   [:p15-s23-stage-comparison-results
                    :stage-comparison-row-count])
           :accepted-output-equivalent?
           (get-in artifact
                   [:accepted-output-stage-comparison
                    :accepted-output-equivalent?])
           :rejected-diagnostics-equivalent?
           (get-in artifact
                   [:rejected-diagnostic-stage-comparison
                    :rejected-diagnostics-equivalent?])
           :full-self-hosted-equivalence?
           (get-in artifact
                   [:stage-boundary-record
                    :full-self-hosted-equivalence?])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)
           :seed-boundary
           (get-in artifact [:stage-boundary-record :seed-stage])})
        (catch Exception _
          nil)))))

(defn p15-s23-self-hosting-conformance-report-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-self-hosting-conformance-report-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :suite-count
           (get-in artifact
                   [:p15-s23-self-hosting-conformance-results
                    :suite-count])
           :stage-support-conformant?
           (get-in artifact
                   [:stage-support-conformance-record
                    :stage-support-conformant?])
           :phase14-conformance-linked?
           (get-in artifact
                   [:conformance-suite-link-table
                    :phase14-conformance-linked?])
           :test13-self-hosting-linked?
           (get-in artifact
                   [:conformance-suite-link-table
                    :test13-self-hosting-linked?])
           :diagnostics-preserved?
           (get-in artifact
                   [:diagnostic-conformance-record
                    :diagnostics-preserved?])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
	           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
	        (catch Exception _
	          nil)))))

(defn p15-s23-provenance-attestation-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-provenance-attestation-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :provenance-record-id
           (get-in artifact [:bootstrap-provenance-record
                             :provenance-record-id])
           :canonical-payload-id
           (get-in artifact [:canonical-provenance-payload
                             :payload-id])
           :compiler-lineage-traversable?
           (get-in artifact [:compiler-lineage-graph
                             :lineage-traversable-to-seed?])
           :revocation-clear?
           (get-in artifact [:revocation-check-report
                             :revocation-clear?])
           :auditor-query-passed?
           (get-in artifact [:auditor-query-index
                             :auditor-query-passed?])
           :release-eligible?
           (get-in artifact [:release-policy-record :release-eligible?])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))