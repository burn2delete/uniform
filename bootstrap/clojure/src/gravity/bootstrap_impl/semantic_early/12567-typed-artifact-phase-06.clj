; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-06
 [artifact state]
 (let
  [{:keys [checked type-facts]} state]
  (assoc
   artifact
   :safe-memory-conformance-records
   (distinct-records (:safe-memory-conformance-records checked))
   :safe11-residual-constraint-records
   (distinct-records (:safe11-residual-constraint-records checked))
   :host-error-normalization-records
   (distinct-records (:host-error-normalization-records checked))
   :alternative-macro-hygiene-records
   (distinct-records (:alternative-macro-hygiene-records checked))
   :safe6-dependency-unsafe-summaries
   (distinct-records (:safe6-dependency-unsafe-summaries checked))
   :standard-library-resource-records
   (distinct-records (:standard-library-resource-records checked))
   :safe16-backend-preservation-reports
   (distinct-records (:safe16-backend-preservation-reports checked))
   :actor-channel-schemas
   (distinct-records (:actor-channel-schemas checked))
   :interop-boundary-metadata
   (distinct-records (:interop-boundary-metadata checked))
   :safe10-scope-check-records
   (distinct-records (:safe10-scope-check-records checked))
   :safe6-generated-unsafe-provenance
   (distinct-records (:safe6-generated-unsafe-provenance checked))
   :provider-replacement-records
   (distinct-records (:provider-replacement-records checked))
   :mmio-capability-records
   (distinct-records (:mmio-capability-records checked))
   :safe8-race-analysis-reports
   (distinct-records (:safe8-race-analysis-reports checked))
   :alternative-memory-runtime-checks
   (distinct-records (:alternative-memory-runtime-checks checked))
   :concurrency-effect-records
   (distinct-records (:concurrency-effect-records checked))
   :safe-memory-unsafe-audit-records
   (distinct-records (:safe-memory-unsafe-audit-records checked))
   :error-conformance-fixture
   (error-conformance-fixture checked type-facts))))
