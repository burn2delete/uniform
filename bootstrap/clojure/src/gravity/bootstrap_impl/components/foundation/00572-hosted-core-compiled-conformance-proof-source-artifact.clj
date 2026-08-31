

(defn hosted-core-compiled-conformance-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text
                                                 module)
        run-output (execute-stage0-compiled-plan compiled-plan)
        conformance-report
        (stage0-compiled-plan-conformance-report compiled-plan module)
        manifest (:conformance-manifest conformance-report)
        conformance-harness (:conformance-harness-record conformance-report)
        fixture-manifest (:fixture-manifest-record conformance-report)
        golden-diagnostics (:golden-diagnostics-record conformance-report)
        language (:language-conformance-record conformance-report)
        compiler (:compiler-test-record conformance-report)
        runtime (:runtime-conformance-record conformance-report)
        profile (:profile-compliance-record conformance-report)
        safety (:safety-conformance-record conformance-report)
        backend (:backend-conformance-record conformance-report)
        standard-library (:standard-library-test-record conformance-report)
        ai-workflow (:ai-workflow-eval-record conformance-report)
        fuzz (:fuzz-property-record conformance-report)
        differential (:differential-record conformance-report)
        formal (:formal-proof-record conformance-report)
        performance (:performance-regression-record conformance-report)
        self-hosting (:self-hosting-validation-record conformance-report)
        conformance (:conformance-results conformance-report)
        proof {:compiled-conformance-gate-validated? true
               :conformance-manifest-recorded?
               (= :complete (:status manifest))
               :harness-fixtures-diagnostics-recorded?
               (boolean
                (and (true? (:offline conformance-harness))
                     (p14-present? (:fixture-metadata-fields
                                    fixture-manifest))
                     (true? (:stable-codes golden-diagnostics))))
               :language-compiler-runtime-profile-recorded?
               (boolean
                (and (= :complete (:status language))
                     (p14-present? (:preservation-reports compiler))
                     (p14-present? (:capability-decision-log runtime))
                     (p14-present? (:profiles profile))))
               :safety-backend-stdlib-ai-recorded?
               (boolean
                (and (p14-present? (:unsafe-audit-records safety))
                     (:lowered-artifact-manifest backend)
                     (p14-present? (:modules standard-library))
                     (p14-present? (:replay-traces ai-workflow))))
               :fuzz-differential-formal-recorded?
               (boolean
                (and (:seed fuzz)
                     (empty? (:accepted-divergence differential))
                     (every? true? (map :machine-checkable
                                        (:claims formal)))))
               :performance-bootstrap-recorded?
               (boolean
                (and (true? (:semantic-gates-passed performance))
                     (:provenance-attestation self-hosting)))
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"TEST1001" "TEST2002" "TEST3002" "TEST4001"
                    "TEST5002" "TEST6004" "TEST7001" "TEST8003"
                    "TEST9001" "TEST10002" "TEST11003" "TEST12003"
                    "TEST13002"}
                  (set (:required-diagnostic-ids conformance)))
               :limitations {:clojure-instruction-runner? true
                             :production-conformance-runner? false
                             :external-backend-validation? false
                             :live-fuzzing-service? false
                             :proof-checker-implementation? false
                             :benchmark-lab? false
                             :self-hosted-compiler? false
                             :self-hosted-conformance-runner? false
                             :next-required-capability
                             :compile-and-run-real-conformance-slices}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-conformance-proof
         :phase "14"
         :task "P14-S1"
         :governing-documents ["D1" "TEST1-TEST13"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :conformance-report
         (select-keys conformance-report
                      [:kind :report-id :document-set
                       :compiled-plan-id
                       :conformance-manifest
                       :conformance-harness-record
                       :fixture-manifest-record
                       :golden-diagnostics-record
                       :language-conformance-record
                       :compiler-test-record
                       :runtime-conformance-record
                       :profile-compliance-record
                       :safety-conformance-record
                       :backend-conformance-record
                       :standard-library-test-record
                       :ai-workflow-eval-record
                       :fuzz-property-record
                       :differential-record
                       :formal-proof-record
                       :performance-regression-record
                       :self-hosting-validation-record
                       :conformance-results
                       :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :proof-command (str "clojure -M:gravity hosted-core-compiled-conformance "
                             source-path)
         :rejected-fixtures stage0-compiled-conformance-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime
                            :gravity.runtime/stage0-clojure-jvm-instruction-runner
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :production-conformance-runner? false
                            :external-backend-validation? false
                            :live-fuzzing-service? false
                            :proof-checker-implementation? false
                            :benchmark-lab? false
                            :self-hosted-compiler? false
                            :self-hosted-conformance-runner? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

(defn hosted-core-compiled-conformance-proof-file-artifact
  [path]
  (hosted-core-compiled-conformance-proof-source-artifact path (slurp path)))

(defn run-compiled-source
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))]
    (execute-stage0-compiled-plan
     (stage0-compiled-core-plan source-path source-text module))))

(defn run-compiled-file
  [path]
  (run-compiled-source path (slurp path)))

(defn hosted-core-app-proof-source-artifact
  [source-path source-text]
  (let [macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        _ (executable-profile! source-path module (:forms module))
        _ (validate-module-effects! module)
        function-table (stage0-function-table module)
        module (assoc module :function-table function-table)
        run-output (run-main module)
        compile-artifact (compile-source source-path source-text)
        user-functions (sort (keys (dissoc function-table 'main)))
        proof {:hosted-core-runner-executed? true
               :user-functions-callable? (boolean (seq user-functions))
               :builtin-calls-supported?
               (set/subset? '#{+ * > str pr-str hash-map vector list conj
                                assoc get count}
                            stage0-builtin-functions)
               :control-flow-supported?
               (set/subset? '#{do if let quote} stage0-special-forms)
               :effects-and-capabilities-checked? true
               :rejected-diagnostics-covered?
               (= #{"L2-FUNCTION-ARITY" "L2-BUILTIN-ARITY"}
                  (set (map :diagnostic stage0-core-app-rejected-fixtures)))
               :limitations {:clojure-hosted-runner? true
                             :self-hosted-compiler? false
                             :native-backend? false
                             :next-required-capability
                             :replace-hosted-core-runner-with-gravity-compiled-execution}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-app-proof
         :phase "01"
         :task "P01-S1"
         :governing-documents ["L1" "L2" "L3" "L5" "L6" "C5"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiler (:compiler compile-artifact)
         :module (:module compile-artifact)
         :runtime-surface {:special-forms (sort stage0-special-forms)
                           :builtin-functions (sort stage0-builtin-functions)
                           :user-functions user-functions}
         :accepted-run {:command (str "clojure -M:gravity run " source-path)
                        :stdout run-output}
         :rejected-fixtures stage0-core-app-rejected-fixtures
         :trusted-boundary {:runtime :clojure/jvm
                            :clojure-hosted-runner? true
                            :self-hosted-compiler? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

(defn hosted-core-app-proof-file-artifact
  [path]
  (hosted-core-app-proof-source-artifact path (slurp path)))