

(def stage0-compiled-backend-rejected-fixtures
  [{:fixture "bootstrap/clojure/fixtures/rejected/core-app-backend-unverified-input.gravity"
    :diagnostic "B1-INPUT"
    :rejected-behavior :real_backend_claim_from_unverified_input}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-backend-jvm-manifest.gravity"
    :diagnostic "B5-MANIFEST"
    :rejected-behavior :incomplete_jvm_artifact_manifest}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-backend-jvm-null.gravity"
    :diagnostic "B5-NULL"
    :rejected-behavior :unchecked_jvm_null_flow}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-backend-provenance.gravity"
    :diagnostic "B13-PROVENANCE"
    :rejected-behavior :backend_artifact_missing_provenance}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-backend-release.gravity"
    :diagnostic "B13-RELEASE"
    :rejected-behavior :release_grade_artifact_from_development_evidence}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-backend-conformance.gravity"
    :diagnostic "B14-ARTIFACT"
    :rejected-behavior :invalid_backend_artifact_manifest_validation}])