; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-15
 [artifact state]
 (let
  [{:keys [checked]} state]
  (assoc
   artifact
   :capability-scope-audit-logs
   (distinct-records (:capability-scope-audit-logs checked))
   :safe-memory-region-lifetime-maps
   (distinct-records (:safe-memory-region-lifetime-maps checked))
   :generic-instantiation-records
   (:generic-instantiation-records checked)
   :pass
   (semantic-early-typed-artifact-pass)
   :workflow-replay-records
   (distinct-records (:workflow-replay-records checked))
   :safe10-secret-redaction-records
   (distinct-records (:safe10-secret-redaction-records checked))
   :pattern-conformance-fixture
   (match-conformance-fixture checked)
   :alternative-type-proof-artifacts
   (distinct-records (:alternative-type-proof-artifacts checked))
   :alternative-type-runtime-check-records
   (distinct-records (:alternative-type-runtime-check-records checked))
   :capability-attenuation-records
   (distinct-records (:capability-attenuation-records checked))
   :compile-time-capability-proof-records
   (distinct-records (:compile-time-capability-proof-records checked))
   :safe15-backend-preservation-records
   (distinct-records (:safe15-backend-preservation-records checked))
   :safe-memory-proof-records
   (distinct-records (:safe-memory-proof-records checked))
   :hermetic-replay-records
   (distinct-records (:hermetic-replay-records checked))
   :safe-memory-runtime-borrow-check-records
   (distinct-records
    (:safe-memory-runtime-borrow-check-records checked))
   :safe15-proof-provider-records
   (distinct-records (:safe15-proof-provider-records checked))
   :safe-memory-conformance-fixture
   (safe-memory-conformance-fixture checked)
   :interop-foreign-binding-declarations
   (distinct-records (:interop-foreign-binding-declarations checked)))))
