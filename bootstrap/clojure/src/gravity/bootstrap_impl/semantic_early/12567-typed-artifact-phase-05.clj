; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-05
 [artifact state]
 (let
  [{:keys [checked type-coverage mir-records effect-facts]} state]
  (assoc
   artifact
   :safe-memory-reset-invalidation-records
   (distinct-records (:safe-memory-reset-invalidation-records checked))
   :memory-facts
   (distinct-records (:memory-facts checked))
   :capability-supply-chain-conformance-fixture
   (capability-supply-chain-conformance-fixture checked)
   :replay-effect-log
   (distinct-records (:replay-effect-log checked))
   :type-category-coverage
   type-coverage
   :mir-type-preservation-records
   mir-records
   :safe14-signature-attestation-records
   (distinct-records (:safe14-signature-attestation-records checked))
   :safe8-conformance-fixture
   (safe8-conformance-fixture checked)
   :match-schema-validation-links
   (distinct-records (:pattern-schema-validation-links checked))
   :safe12-facet-output-records
   (distinct-records (:safe12-facet-output-records checked))
   :safe8-atomic-memory-order-records
   (distinct-records (:safe8-atomic-memory-order-records checked))
   :safe13-model-output-taint-records
   (distinct-records (:safe13-model-output-taint-records checked))
   :standard-library-compatibility-records
   (distinct-records (:standard-library-compatibility-records checked))
   :safe12-conformance-fixture
   (safe12-conformance-fixture checked)
   :generated-form-provenance-records
   (distinct-records (:generated-form-provenance-records checked))
   :effect-environment
   effect-facts
   :safe9-backend-lowering-records
   (distinct-records (:safe9-backend-lowering-records checked))
   :safe1-runtime-check-records
   (distinct-records (:safe1-runtime-check-records checked)))))
