; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-01
 [artifact state]
 (let
  [{:keys [checked typed-roots build-log mir-effect-records]} state]
  (assoc
   artifact
   :interop-profile-rejection-records
   (distinct-records (:interop-profile-rejection-records checked))
   :type-environment
   (type-environment typed-roots)
   :constant-value-table
   (distinct-records (:constant-value-table checked))
   :standard-library-unsafe-wrapper-audits
   (distinct-records (:standard-library-unsafe-wrapper-audits checked))
   :build-effect-log
   build-log
   :alternative-type-fact-export-schemas
   (distinct-records (:alternative-type-fact-export-schemas checked))
   :safe8-synchronization-proof-records
   (distinct-records (:safe8-synchronization-proof-records checked))
   :mir-effect-annotations
   mir-effect-records
   :alternative-type-lowering-rules
   (distinct-records (:alternative-type-lowering-rules checked))
   :typed-core-ast
   typed-roots
   :safe1-optimization-check-erasure-justifications
   (distinct-records
    (:safe1-optimization-check-erasure-justifications checked))
   :safe10-capability-requirement-records
   (distinct-records (:safe10-capability-requirement-records checked))
   :safe-memory-exceptional-cleanup-records
   (distinct-records
    (:safe-memory-exceptional-cleanup-records checked))
   :alternative-type-compatibility-reports
   (distinct-records (:alternative-type-compatibility-reports checked))
   :safe1-safety-classification-records
   (distinct-records (:safe1-safety-classification-records checked))
   :safe12-macro-safety-declarations
   (distinct-records (:safe12-macro-safety-declarations checked))
   :memory-conformance-fixture
   (memory-conformance-fixture checked)
   :interop-generated-binding-provenance
   (distinct-records (:interop-generated-binding-provenance checked)))))
