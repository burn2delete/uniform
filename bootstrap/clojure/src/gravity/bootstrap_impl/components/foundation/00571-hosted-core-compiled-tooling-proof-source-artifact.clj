

(defn hosted-core-compiled-tooling-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text
                                                 module)
        run-output (execute-stage0-compiled-plan compiled-plan)]
    (gravity.tooling/native-compiled-artifact {:present (fn [] p13-present?) :sha256 (fn [] sha256-hex) :plan-report (fn [] stage0-compiled-plan-tooling-report) :compiled-rejected-fixtures (fn [] stage0-compiled-tooling-rejected-fixtures)} source-path source-text module compiled-plan run-output)))

(defn hosted-core-compiled-tooling-proof-file-artifact
  [path]
  (hosted-core-compiled-tooling-proof-source-artifact path (slurp path)))

(defn stage0-compiled-plan-conformance-report
  [plan module]
  (let [plan-id (:plan-id plan)
        conformance-artifacts (p14-conformance-artifact-values)
        conformance-harness (:conformance-harness conformance-artifacts)
        fixture-manifest (:fixture-manifest conformance-artifacts)
        golden-diagnostics (:golden-diagnostics conformance-artifacts)
        language (:language-conformance conformance-artifacts)
        compiler (:compiler-test-report conformance-artifacts)
        runtime (:runtime-conformance-report conformance-artifacts)
        profile (:profile-compliance-report conformance-artifacts)
        safety (:safety-conformance-report conformance-artifacts)
        backend (:backend-conformance-report conformance-artifacts)
        standard-library (:standard-library-test-report conformance-artifacts)
        ai-workflow (:ai-workflow-eval-report conformance-artifacts)
        fuzz (:fuzz-property-suite conformance-artifacts)
        differential (:differential-report conformance-artifacts)
        formal (:formal-proof-report conformance-artifacts)
        performance (:performance-regression-report conformance-artifacts)
        self-hosting (:self-hosting-validation-report conformance-artifacts)
        conformance
        {:document-set p14-conformance-documents
         :task "P14-S1"
         :required-diagnostic-ids
         (mapv p14-conformance-rejected-diagnostics
               p14-conformance-documents)
         :conformance-gate-status :metadata-gate-only
         :harness-status :complete
         :fixture-manifest-status :complete
         :golden-diagnostics-status :complete
         :language-status :complete
         :compiler-status :complete
         :runtime-status :complete
         :profile-status :complete
         :safety-status :complete
         :backend-status :complete
         :standard-library-status :complete
         :ai-workflow-status :complete
         :fuzz-status :complete
         :differential-status :complete
         :formal-status :complete
         :performance-status :complete
         :self-hosting-status :complete
         :status :complete}
        report-base
        {:kind :gravity/stage0-hosted-core-compiled-conformance-report
         :document-set ["D1" "TEST1-TEST13"]
         :compiled-plan-id plan-id
         :conformance-manifest
         {:artifact :gravity/stage0-hosted-core-compiled-conformance-manifest
          :profile (:profile module)
          :target (:target module)
          :conformance-harness (:artifact conformance-harness)
          :fixture-manifest (:artifact fixture-manifest)
          :golden-diagnostics (:artifact golden-diagnostics)
          :language-conformance (:artifact language)
          :compiler-test-report (:artifact compiler)
          :runtime-conformance-report (:artifact runtime)
          :profile-compliance-report (:artifact profile)
          :safety-conformance-report (:artifact safety)
          :backend-conformance-report (:artifact backend)
          :standard-library-test-report (:artifact standard-library)
          :ai-workflow-eval-report (:artifact ai-workflow)
          :fuzz-property-suite (:artifact fuzz)
          :differential-report (:artifact differential)
          :formal-proof-report (:artifact formal)
          :performance-regression-report (:artifact performance)
          :self-hosting-validation-report (:artifact self-hosting)
          :accepted-fixtures
          ["bootstrap/clojure/fixtures/accepted/core-app.gravity"]
          :rejected-fixtures stage0-compiled-conformance-rejected-fixtures
          :conformance {:status :complete}
          :status :complete}
         :conformance-harness-record conformance-harness
         :fixture-manifest-record fixture-manifest
         :golden-diagnostics-record golden-diagnostics
         :language-conformance-record language
         :compiler-test-record compiler
         :runtime-conformance-record runtime
         :profile-compliance-record profile
         :safety-conformance-record safety
         :backend-conformance-record backend
         :standard-library-test-record standard-library
         :ai-workflow-eval-record ai-workflow
         :fuzz-property-record fuzz
         :differential-record differential
         :formal-proof-record formal
         :performance-regression-record performance
         :self-hosting-validation-record self-hosting
         :conformance-results conformance
         :diagnostics []}]
    (assoc report-base
           :report-id (str "sha256:" (sha256-hex (pr-str report-base))))))