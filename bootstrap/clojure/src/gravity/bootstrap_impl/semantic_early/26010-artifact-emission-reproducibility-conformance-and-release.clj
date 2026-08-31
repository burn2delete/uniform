; Semantic decomposition of HEAD reader line 26010.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-artifact-emission-reproducibility-conformance-and-release
 [source-path state]
 (let
  [{:keys [manifest-hashes]} state]
  (assoc
   {}
   :reproducibility-record
   {:artifact :gravity/reproducibility-record,
    :hashes manifest-hashes,
    :timestamp-policy :none,
    :environment-inputs ["clojure-stage0" "source-text"],
    :nondeterminism [],
    :development-build? true,
    :status :recorded}
   :conformance-evidence-reference
   {:artifact :gravity/conformance-evidence-reference,
    :packs
    ["backend-conformance-pack:p07-t01"
     "backend-conformance-pack:p07-t02"
     "backend-conformance-pack:p07-t03"
     "backend-conformance-pack:p07-t04"],
    :manifest-validation :passed,
    :metadata-preservation :passed,
    :status :complete}
   :release-gate-record
   {:artifact :gravity/artifact-release-gate,
    :release-grade-artifact-status :blocked-development-only,
    :reason :stage0-development-evidence,
    :blocked-downstream
    [:signing :packaging :deployment :release-governance],
    :diagnostic-on-release-attempt "B13-RELEASE",
    :status :complete})))
