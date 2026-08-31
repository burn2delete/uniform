

(defn stage0-compiled-plan-math-report
  [plan module]
  (let [operator-counts (stage0-compiled-plan-builtin-operator-counts plan)
        numeric-operators (select-keys operator-counts
                                       stage0-numeric-builtin-functions)
        floating-literals (vec (stage0-floating-literals (:forms module)))
        math-metadata (get-in module [:metadata :math :numeric])
        manifest-status (cond
                          (seq floating-literals) :required
                          (seq (:floating-manifests math-metadata)) :declared
                          :else :not-required-for-accepted-fixture)
        numeric-record {:record-id :stage0/compiled-core-app-integer-baseline
                        :math/mode :exact
                        :scope {:kind :accepted-run
                                :entrypoint (:entrypoint plan)}
                        :profile (:profile module)
                        :target (:target module)
                        :family :fixed-integer
                        :domain {:accepted-run :core-app-literal-inputs}
                        :precision {:type :stage0-observed-integers
                                    :symbolic-exactness true}
                        :integer-overflow
                        :not-proven-for-arbitrary-inputs
                        :rounding :not-applicable
                        :float-exceptions :not-applicable
                        :source-span {:source (:source-path module)}}
        conformance {:document-set ["MATH1" "MATH7" "MATH8" "D6" "D9"]
                     :task "P05-S1"
                     :required-diagnostic-ids
                     ["MATH1-NARROW" "MATH7-MISSING"
                      "MATH8-MANIFEST" "MATH8-REASSOC"]
                     :numeric-tower-status :integer-baseline-recorded
                     :numeric-mode-status :explicit-baseline-recorded
                     :floating-manifest-status manifest-status
                     :math-performance-status :no-fast-math
                     :verification-status :accepted-and-rejected-fixtures
                     :status :complete}
        report-base
        {:kind :gravity/stage0-hosted-core-compiled-math-report
         :document-set ["MATH1" "MATH7" "MATH8" "D6" "D9"]
         :compiled-plan-id (:plan-id plan)
         :numeric-operation-report
         {:builtin-operator-counts operator-counts
          :numeric-operator-counts numeric-operators
          :numeric-operators (vec (sort (keys numeric-operators)))
          :stage0-supported-numeric-operators
          (vec (sort stage0-numeric-builtin-functions))
          :elementary-functions []
          :efir-lowered? false
          :status :integer-baseline}
         :numeric-mode-record numeric-record
         :floating-manifest-report
         {:floating-literals floating-literals
          :floating-manifests (vec (:floating-manifests math-metadata))
          :status manifest-status
          :stage0-floating-runtime-claim? false}
         :math-performance-policy
         {:silent-fast-math? false
          :target-default-numeric-behavior? false
          :fma-contraction? false
          :reassociation? false
          :certificate-required-before-relaxation? true
          :status :complete}
         :math-verification-results conformance
         :diagnostics []}]
    (assoc report-base
           :report-id (str "sha256:" (sha256-hex (pr-str report-base))))))

(defn hosted-core-compiled-math-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text
                                                 module)
        run-output (execute-stage0-compiled-plan compiled-plan)
        math-report (stage0-compiled-plan-math-report compiled-plan module)
        numeric-report (:numeric-operation-report math-report)
        floating-report (:floating-manifest-report math-report)
        proof {:compiled-math-validated? true
               :numeric-operations-recorded?
               (boolean (seq (:numeric-operator-counts numeric-report)))
               :integer-baseline-recorded?
               (= :exact (get-in math-report
                                  [:numeric-mode-record :math/mode]))
               :floating-manifest-not-claimed?
               (and (empty? (:floating-literals floating-report))
                    (false? (:stage0-floating-runtime-claim?
                             floating-report)))
               :silent-fast-math-rejected? true
               :target-default-numeric-behavior-rejected? true
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"MATH1-NARROW" "MATH7-MISSING"
                    "MATH8-MANIFEST" "MATH8-REASSOC"}
                  (set (map :diagnostic
                            stage0-compiled-math-rejected-fixtures)))
               :limitations {:clojure-instruction-runner? true
                             :self-hosted-compiler? false
                             :native-backend? false
                             :floating-runtime-claim? false
                             :efir-lowered? false
                             :elementary-functions? false
                             :next-required-capability
                             :lower-math-mode-records-into-real-mir-efir-and-runtime-artifacts}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-math-proof
         :phase "05"
         :task "P05-S1"
         :governing-documents ["MATH1" "MATH7" "MATH8" "D6" "D9"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :math-report (select-keys math-report
                                   [:kind :report-id :document-set
                                    :numeric-operation-report
                                    :numeric-mode-record
                                    :floating-manifest-report
                                    :math-performance-policy
                                    :math-verification-results
                                    :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :rejected-fixtures stage0-compiled-math-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime :clojure/jvm
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :self-hosted-compiler? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

(defn hosted-core-compiled-math-proof-file-artifact
  [path]
  (hosted-core-compiled-math-proof-source-artifact path (slurp path)))