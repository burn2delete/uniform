; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-07
 [artifact state]
 (let
  [{:keys [checked method-signatures type-facts]} state]
  (assoc
   artifact
   :runtime-check-records
   (:runtime-check-records checked)
   :alternative-type-optimization-proofs
   (distinct-records (:alternative-type-optimization-proofs checked))
   :branch-effect-summary
   (distinct-records (:branch-effect-summary checked))
   :safe9-conformance-fixture
   (safe9-conformance-fixture checked)
   :workflow-failure-records
   (distinct-records (:workflow-failure-records checked))
   :safe10-revocation-records
   (distinct-records (:safe10-revocation-records checked))
   :safe1-conformance-records
   (distinct-records (:safe1-conformance-records checked))
   :alternative-macro-provider-declarations
   (distinct-records
    (:alternative-macro-provider-declarations checked))
   :safe7-ownership-lifetime-maps
   (distinct-records (:safe7-ownership-lifetime-maps checked))
   :safe8-task-capture-records
   (distinct-records (:safe8-task-capture-records checked))
   :method-signature-records
   method-signatures
   :facet-conformance-fixture
   (facet-conformance-fixture checked)
   :alternative-macro-explicit-capture-records
   (distinct-records
    (:alternative-macro-explicit-capture-records checked))
   :safe7-error-translation-maps
   (distinct-records (:safe7-error-translation-maps checked))
   :safe8-ownership-transfer-records
   (distinct-records (:safe8-ownership-transfer-records checked))
   :safe15-trust-records
   (distinct-records (:safe15-trust-records checked))
   :safe6-review-status-records
   (distinct-records (:safe6-review-status-records checked))
   :type-facts
   type-facts)))
