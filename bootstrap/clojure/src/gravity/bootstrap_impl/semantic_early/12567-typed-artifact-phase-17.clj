; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-17
 [artifact state]
 (let
  [{:keys [checked protocol-table]} state]
  (assoc
   artifact
   :alternative-macro-syntax-serializations
   (distinct-records
    (:alternative-macro-syntax-serializations checked))
   :safe11-prompt-tool-policy-records
   (distinct-records (:safe11-prompt-tool-policy-records checked))
   :safe12-hygiene-capture-records
   (distinct-records (:safe12-hygiene-capture-records checked))
   :alternative-macro-build-effect-traces
   (distinct-records (:alternative-macro-build-effect-traces checked))
   :safe8-blocking-cancellation-records
   (distinct-records (:safe8-blocking-cancellation-records checked))
   :safe16-profile-matrix-reports
   (distinct-records (:safe16-profile-matrix-reports checked))
   :facet-privacy-boundary-records
   (distinct-records (:facet-privacy-boundary-records checked))
   :alternative-memory-layout-metadata
   (distinct-records (:alternative-memory-layout-metadata checked))
   :safe11-parameterization-records
   (distinct-records (:safe11-parameterization-records checked))
   :safe12-conformance-records
   (distinct-records (:safe12-conformance-records checked))
   :safe12-generated-unsafe-island-records
   (distinct-records (:safe12-generated-unsafe-island-records checked))
   :safe9-range-proof-records
   (distinct-records (:safe9-range-proof-records checked))
   :safe13-tool-call-traces
   (distinct-records (:safe13-tool-call-traces checked))
   :protocol-table
   protocol-table
   :safe14-authority-diff-records
   (distinct-records (:safe14-authority-diff-records checked))
   :lifetime-region-facts
   (distinct-records (:lifetime-region-facts checked))
   :safe15-check-erasure-records
   (distinct-records (:safe15-check-erasure-records checked))
   :standard-library-numeric-mode-records
   (distinct-records
    (:standard-library-numeric-mode-records checked)))))
