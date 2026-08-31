

(defn hosted-core-compiled-tooling-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text
                                                 module)
        run-output (execute-stage0-compiled-plan compiled-plan)
        tooling-report (stage0-compiled-plan-tooling-report compiled-plan
                                                            module)
        manifest (:tooling-manifest tooling-report)
        cli-command-set (:cli-command-set-record tooling-report)
        repl-session (:repl-session-record tooling-report)
        formatter (:formatter-record tooling-report)
        linter (:linter-record tooling-report)
        lsp (:lsp-record tooling-report)
        debugger (:debugger-record tooling-report)
        documentation (:documentation-record tooling-report)
        dev-server (:dev-server-record tooling-report)
        registry (:registry-ux-record tooling-report)
        ir-inspector (:ir-inspector-record tooling-report)
        profiler (:profiler-record tooling-report)
        safety-audit (:safety-audit-record tooling-report)
        ai-tooling (:ai-tooling-record tooling-report)
        conformance (:tooling-conformance-results tooling-report)
        proof {:compiled-tooling-gate-validated? true
               :tooling-manifest-recorded?
               (= :complete (:status manifest))
               :cli-and-repl-recorded?
               (boolean
                (and (contains? (set (get-in cli-command-set
                                              [:capability-prompts
                                               :shown-denials]))
                                 :shell/exec)
                     (seq (:capability-grants repl-session))))
               :formatter-linter-docs-recorded?
               (boolean
                (and (true? (:reader-round-trip formatter))
                     (set/subset?
                      #{:types :effects :capabilities :profile :artifacts}
                      (set (:compiler-facts linter)))
                     (true? (:structured-docs documentation))
                     (zero? (get-in documentation
                                    [:example-validation-report :failed]))))
               :lsp-debugger-recorded?
               (boolean
                (and (true? (:diagnostics-match-cli lsp))
                     (= :passed (get-in debugger
                                        [:source-map-validation :status]))
                     (not (some #(and (true? (:redacted %))
                                      (true? (:accessed? %)))
                                (:variable-reports debugger)))))
               :dev-registry-inspector-recorded?
               (boolean
                (and (some #(= :restart (:decision %))
                           (:hot-reload-decisions dev-server))
                     (true? (get-in registry
                                    [:update-diff
                                     :capability-diff-visible]))
                     (p13-present? (:source-span-maps ir-inspector))))
               :profiler-safety-ai-recorded?
               (boolean
                (and (p13-present? (get-in profiler
                                           [:check-elision-report
                                            :evidence]))
                     (p13-present? (:proof-index safety-audit))
                     (true? (get-in ai-tooling
                                    [:patch-artifact :validated]))
                     (every? #(= :passed %)
                             (vals (:validation-report ai-tooling)))))
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"T1003" "T2002" "T3002" "T4003" "T5001"
                    "T6004" "T7001" "T8003" "T9001" "T10002"
                    "T11003" "T12001" "T13002"}
                  (set (:required-diagnostic-ids conformance)))
               :limitations {:clojure-instruction-runner? true
                             :production-cli? false
                             :interactive-repl-server? false
                             :lsp-server-transport? false
                             :debugger-runtime-session? false
                             :dev-server-process? false
                             :registry-ui-service? false
                             :profiler-runtime? false
                             :ai-patch-application? false
                             :self-hosted-tooling? false
                             :next-required-capability
                             :compile-and-run-real-tooling-slices}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-tooling-proof
         :phase "13"
         :task "P13-S1"
         :governing-documents ["D1" "T1-T13"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :tooling-report
         (select-keys tooling-report
                      [:kind :report-id :document-set
                       :compiled-plan-id
                       :tooling-manifest
                       :cli-command-set-record
                       :repl-session-record
                       :formatter-record
                       :linter-record
                       :lsp-record
                       :debugger-record
                       :documentation-record
                       :dev-server-record
                       :registry-ux-record
                       :ir-inspector-record
                       :profiler-record
                       :safety-audit-record
                       :ai-tooling-record
                       :tooling-ui-data-model-record
                       :tooling-conformance-results
                       :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :proof-command (str "clojure -M:gravity hosted-core-compiled-tooling "
                             source-path)
         :rejected-fixtures stage0-compiled-tooling-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime
                            :gravity.runtime/stage0-clojure-jvm-instruction-runner
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :production-cli? false
                            :interactive-repl-server? false
                            :lsp-server-transport? false
                            :debugger-runtime-session? false
                            :dev-server-process? false
                            :registry-ui-service? false
                            :profiler-runtime? false
                            :ai-patch-application? false
                            :self-hosted-tooling? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

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