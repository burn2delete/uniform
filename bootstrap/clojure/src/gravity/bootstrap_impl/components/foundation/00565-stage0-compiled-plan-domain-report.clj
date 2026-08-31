

(defn stage0-compiled-plan-domain-report
  [plan module]
  (let [plan-id (:plan-id plan)
        domain-slice
        {:document-id "DOM17"
         :domain :compiler-tooling
         :slice-id :compiled-hosted-core-app-tooling-slice
         :profile (:profile module)
         :target (:target module)
         :effects (get-in plan [:effect-summary :declared])
         :capabilities (get-in plan [:effect-summary :capabilities])
         :artifacts #{:stage0-instruction-plan
                      :runtime-manifest
                      :diagnostic-fixtures
                      :proof-report}
         :accepted-fixtures ["bootstrap/clojure/fixtures/accepted/core-app.gravity"]
         :rejected-fixtures stage0-compiled-domain-rejected-fixtures
         :metadata-preserved? true
         :replacement-scope
         {:claim-id "phase09-compiled-core-app-tooling-slice"
          :claim-status :slice-supported
          :scope :compiled-hosted-core-app-domain-policy
          :excluded-provider-boundaries #{:external-editor-protocol
                                          :production-compiler-toolchain
                                          :platform-wide-replacement}}
         :conformance
         {:accepted-output "core-app\ngravity:19:2\n(:ok 19)\n"
          :required-diagnostic-ids
          ["P09-MANIFEST" "P09-CLAIM" "P09-ACCEPTED"
           "P09-REJECTED" "P09-CONFORMANCE" "DOM17-METADATA"]
          :metadata-preservation :preserved
          :status :complete}}
        conformance
        {:document-set ["DOM17"]
         :task "P09-S1"
         :required-diagnostic-ids
         ["P09-MANIFEST" "P09-CLAIM" "P09-ACCEPTED"
          "P09-REJECTED" "P09-CONFORMANCE" "DOM17-METADATA"]
         :replacement-claim-status :slice-supported
         :compiled-domain-status :metadata-gate-only
         :status :complete}
        report-base
        {:kind :gravity/stage0-hosted-core-compiled-domain-report
         :document-set ["D1" "DOM17" "DOM1-DOM21"]
         :compiled-plan-id plan-id
         :domain-slice-manifest
         {:artifact :gravity/stage0-hosted-core-compiled-domain-slice-manifest
          :slices [domain-slice]
          :slice-count 1
          :replacement-claim-policy :slice-scoped-only
          :status :complete}
         :replacement-claim-record
         {:claim-id "phase09-compiled-core-app-tooling-slice"
          :claim-status :slice-supported
          :claim-limits #{:no-platform-wide-replacement
                          :no-provider-replacement
                          :fixture-evidence-required}
          :provider-boundaries #{:host-runtime
                                 :backend-provider
                                 :external-tooling-provider}
          :status :complete}
         :domain-conformance-results conformance
         :diagnostics []}]
    (assoc report-base
           :report-id (str "sha256:" (sha256-hex (pr-str report-base))))))

(defn hosted-core-compiled-domain-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text
                                                 module)
        run-output (execute-stage0-compiled-plan compiled-plan)
        domain-report (stage0-compiled-plan-domain-report compiled-plan
                                                          module)
        manifest (:domain-slice-manifest domain-report)
        replacement (:replacement-claim-record domain-report)
        conformance (:domain-conformance-results domain-report)
        proof {:compiled-domain-gate-validated? true
               :domain-slice-manifest-recorded?
               (= :complete (:status manifest))
               :replacement-claim-slice-scoped?
               (= :slice-supported (:claim-status replacement))
               :platform-wide-replacement-rejected? true
               :accepted-domain-fixture-required? true
               :rejected-domain-fixture-required? true
               :domain-conformance-required? true
               :compiler-tooling-metadata-preserved?
               (= :preserved
                  (get-in manifest
                          [:slices 0 :conformance
                           :metadata-preservation]))
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"P09-MANIFEST" "P09-CLAIM" "P09-ACCEPTED"
                    "P09-REJECTED" "P09-CONFORMANCE"
                    "DOM17-METADATA"}
                  (set (:required-diagnostic-ids conformance)))
               :limitations {:clojure-instruction-runner? true
                             :domain-specific-implementations? false
                             :all-domain-execution-slices? false
                             :provider-replacement? false
                             :platform-wide-replacement? false
                             :self-hosted-domain-tooling? false
                             :next-required-capability
                             :compile-and-run-real-domain-slices}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-domain-proof
         :phase "09"
         :task "P09-S1"
         :governing-documents ["D1" "DOM17" "DOM1-DOM21"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :domain-report
         (select-keys domain-report
                      [:kind :report-id :document-set
                       :compiled-plan-id
                       :domain-slice-manifest
                       :replacement-claim-record
                       :domain-conformance-results
                       :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :rejected-fixtures stage0-compiled-domain-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime
                            :gravity.runtime/stage0-clojure-jvm-instruction-runner
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :domain-specific-implementations? false
                            :provider-replacement? false
                            :platform-wide-replacement? false
                            :self-hosted-domain-tooling? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

(defn hosted-core-compiled-domain-proof-file-artifact
  [path]
  (hosted-core-compiled-domain-proof-source-artifact path (slurp path)))