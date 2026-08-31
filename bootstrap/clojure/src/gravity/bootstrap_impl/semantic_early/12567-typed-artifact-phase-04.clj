; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-04
 [artifact state]
 (let
  [{:keys [checked build-log handled-table effect-facts]} state]
  (assoc
   artifact
   :safe7-generated-binding-provenance
   (distinct-records (:safe7-generated-binding-provenance checked))
   :interop-ownership-lifetime-maps
   (distinct-records (:interop-ownership-lifetime-maps checked))
   :safe9-floating-mode-records
   (distinct-records (:safe9-floating-mode-records checked))
   :interop-compatibility-records
   (distinct-records (:interop-compatibility-records checked))
   :safe12-taint-capability-propagation
   (distinct-records (:safe12-taint-capability-propagation checked))
   :safe10-conformance-fixture
   (safe10-conformance-fixture checked)
   :runtime-provider-manifests
   (distinct-records (:runtime-provider-manifests checked))
   :safe16-unsafe-audit-inspections
   (distinct-records (:safe16-unsafe-audit-inspections checked))
   :safe11-conformance-records
   (distinct-records (:safe11-conformance-records checked))
   :alternative-type-provider-declarations
   (distinct-records (:alternative-type-provider-declarations checked))
   :memory-regime-annotations
   (distinct-records (:memory-facts checked))
   :safe7-host-bridge-records
   (distinct-records (:safe7-host-bridge-records checked))
   :safe8-conformance-records
   (distinct-records (:safe8-conformance-records checked))
   :effect-conformance-fixture
   (effect-conformance-fixture effect-facts build-log handled-table)
   :safe-memory-transfer-records
   (distinct-records (:safe-memory-transfer-records checked))
   :safe15-certificates
   (distinct-records (:safe15-certificates checked))
   :alternative-memory-device-maps
   (distinct-records (:alternative-memory-device-maps checked))
   :interop-error-translation-maps
   (distinct-records (:interop-error-translation-maps checked)))))
