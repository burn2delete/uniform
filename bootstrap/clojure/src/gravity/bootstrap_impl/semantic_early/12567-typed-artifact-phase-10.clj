; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-10
 [artifact state]
 (let
  [{:keys [checked module handled-table escaping-effects]} state]
  (assoc
   artifact
   :alternative-memory-unsafe-boundary-audits
   (distinct-records
    (:alternative-memory-unsafe-boundary-audits checked))
   :task-scope-graphs
   (distinct-records (:task-scope-graphs checked))
   :safe10-conformance-records
   (distinct-records (:safe10-conformance-records checked))
   :safe11-taint-source-records
   (distinct-records (:safe11-taint-source-records checked))
   :module
   module
   :module-effect-summary
   {:escaping-effects escaping-effects, :handled-effects handled-table}
   :scheduler-runtime-manifests
   (distinct-records (:scheduler-runtime-manifests checked))
   :safe6-conformance-fixture
   (safe6-conformance-fixture checked)
   :safe11-conformance-fixture
   (safe11-conformance-fixture checked)
   :safe7-safe-wrapper-audits
   (distinct-records (:safe7-safe-wrapper-audits checked))
   :interop-safe-wrapper-audits
   (distinct-records (:interop-safe-wrapper-audits checked))
   :effect-registry-snapshot
   (effect-registry-snapshot)
   :safe13-prompt-provenance-records
   (distinct-records (:safe13-prompt-provenance-records checked))
   :safe-memory-provider-records
   (distinct-records (:safe-memory-provider-records checked))
   :safe14-conformance-records
   (distinct-records (:safe14-conformance-records checked))
   :interop-conformance-fixture
   (interop-conformance-fixture checked)
   :safe13-generated-code-safety-records
   (distinct-records (:safe13-generated-code-safety-records checked))
   :safe6-invariant-proof-links
   (distinct-records (:safe6-invariant-proof-links checked)))))
