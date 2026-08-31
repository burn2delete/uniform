; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-13
 [artifact state]
 (let
  [{:keys
    [checked
     module
     module-effects
     interface-artifacts
     method-signatures
     protocol-table
     implementation-table
     multimethods
     dispatch-records
     host-dispatch-records]}
   state]
  (assoc
   artifact
   :kind
   :gravity/stage0-typed-core-artifact
   :safe-memory-lifetime-interval-maps
   (distinct-records (:safe-memory-lifetime-interval-maps checked))
   :safe13-tool-schema-validation-records
   (distinct-records (:safe13-tool-schema-validation-records checked))
   :safe11-secret-redaction-records
   (distinct-records (:safe11-secret-redaction-records checked))
   :provider-conformance-results
   (distinct-records (:provider-conformance-results checked))
   :namespace-effect-summary
   {:declared (:effects module), :inferred module-effects}
   :interface-lowering-artifacts
   interface-artifacts
   :capability-value-records
   (distinct-records (:capability-value-records checked))
   :safe15-imported-certificate-verifications
   (distinct-records
    (:safe15-imported-certificate-verifications checked))
   :safe14-generated-artifact-provenance
   (distinct-records (:safe14-generated-artifact-provenance checked))
   :dispatch-conformance-fixture
   (dispatch-conformance-fixture
    protocol-table
    implementation-table
    method-signatures
    dispatch-records
    multimethods
    interface-artifacts
    host-dispatch-records)
   :safe8-concurrency-graphs
   (distinct-records (:safe8-concurrency-graphs checked))
   :safe12-alternative-engine-equivalence
   (distinct-records (:safe12-alternative-engine-equivalence checked))
   :safe7-conformance-fixture
   (safe7-conformance-fixture checked)
   :safe16-certificate-inspections
   (distinct-records (:safe16-certificate-inspections checked))
   :alternative-type-ownership-facts
   (distinct-records (:alternative-type-ownership-facts checked))
   :match-decision-tree-artifact
   (distinct-records (:match-decision-trees checked))
   :concurrency-facts
   (distinct-records (:concurrency-facts checked)))))
