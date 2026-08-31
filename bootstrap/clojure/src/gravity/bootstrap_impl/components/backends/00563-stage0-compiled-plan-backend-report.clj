

(defn stage0-compiled-plan-backend-report
  [plan module]
  (let [source-id (get-in plan [:source :sha256])
        plan-id (:plan-id plan)
        artifact-content {:plan-id plan-id
                          :entrypoint (:entrypoint plan)
                          :instruction-summary (:instruction-summary plan)}
        content-hash (str "sha256:" (sha256-hex (pr-str artifact-content)))
        artifact-manifest
        {:schema-version 1
         :kind :stage0-instruction-plan
         :backend :gravity.backend/stage0-jvm-instruction-runner
         :profile (:profile module)
         :target (:target module)
         :content-hash content-hash
         :inputs {:source source-id
                  :instruction-plan plan-id
                  :verified-mir :not-emitted
                  :target-lowering :not-performed}
         :evidence {:safety :stage0-compiled-safety-gate
                    :profile :stage0-compiled-profile-gate
                    :performance :stage0-compiled-performance-gate
                    :math :stage0-compiled-math-gate
                    :compiler :stage0-compiled-compiler-gate
                    :effects (get-in plan [:effect-summary :declared])
                    :capabilities (get-in plan
                                          [:effect-summary :capabilities])
                    :conformance :p07-s1-compiled-backend-gate}
         :provenance {:source source-id
                      :compiler :clojure-bootstrap
                      :generator :stage0-compiled-backend-gate
                      :passes [:read-source :macro-expand
                               :profile-validate :safety-analyze
                               :performance-validate :math-validate
                               :compiler-gate-validate
                               :backend-artifact-gate
                               :emit-instruction-plan]
                      :dependencies []}
         :reproducibility {:timestamp-policy :none
                           :nondeterminism []
                           :environment-inputs []
                           :status :recorded}
         :release-grade? false}
        report-base
        {:kind :gravity/stage0-hosted-core-compiled-backend-report
         :document-set ["D1" "B1" "B5" "B13" "B14"]
         :compiled-plan-id plan-id
         :backend-input-record
         {:input-kind :stage0-instruction-plan-artifact
          :input-artifact-id plan-id
          :backend :gravity.backend/stage0-jvm-instruction-runner
          :profile (:profile module)
          :target (:target module)
          :b1-release-input-requirement #{:verified-mir :verified-domain-ir}
          :status :development-only-substitute}
         :jvm-backend-status
         {:backend :gravity.backend/stage0-jvm-instruction-runner
          :host-runtime :clojure-jvm
          :classfiles-emitted? false
          :jar-emitted? false
          :module-descriptor-emitted? false
          :java-interop? false
          :nullability-policy :not-crossing-java-boundary
          :exception-policy :not-crossing-java-boundary
          :reflection-policy :not-used
          :status :instruction-runner-only}
         :artifact-manifest artifact-manifest
         :artifact-graph
         {:artifact :gravity/artifact-graph
          :nodes [{:id source-id :kind :source}
                  {:id plan-id :kind :stage0-instruction-plan}
                  {:id content-hash :kind :stage0-backend-artifact}]
          :edges [{:from source-id
                   :to plan-id
                   :pass :stage0-compiled-core-plan}
                  {:from plan-id
                   :to content-hash
                   :pass :stage0-compiled-backend-gate}]
          :status :complete}
         :source-debug-map-record
         {:source source-id
          :generated-artifact content-hash
          :preserves [:source-spans :generated-origin :instruction-summary]
          :status :preserved}
         :backend-conformance-results
         {:document-set ["B1" "B5" "B13" "B14"]
          :task "P07-S1"
          :positive-results [{:fixture :core-app
                              :backend
                              :gravity.backend/stage0-jvm-instruction-runner
                              :status :passed}]
          :required-diagnostic-ids
          ["B1-INPUT" "B5-MANIFEST" "B5-NULL"
           "B13-PROVENANCE" "B13-RELEASE" "B14-ARTIFACT"]
          :artifact-manifest-validation :valid
          :metadata-preservation :preserved
          :differential-execution :not-claimed
          :release-grade-artifact-status :blocked-development-only
          :status :complete}
         :diagnostics []}]
    (assoc report-base
           :report-id (str "sha256:" (sha256-hex (pr-str report-base))))))

(defn hosted-core-compiled-backend-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text
                                                 module)
        run-output (execute-stage0-compiled-plan compiled-plan)
        backend-report (stage0-compiled-plan-backend-report compiled-plan
                                                            module)
        backend-status (:jvm-backend-status backend-report)
        manifest (:artifact-manifest backend-report)
        conformance (:backend-conformance-results backend-report)
        proof {:compiled-backend-gate-validated? true
               :development-backend-artifact-recorded? true
               :instruction-plan-content-addressed?
               (boolean (re-find #"^sha256:" (:content-hash manifest)))
               :backend-input-boundary-explicit?
               (= :development-only-substitute
                  (get-in backend-report
                          [:backend-input-record :status]))
               :release-grade-output-blocked?
               (= :blocked-development-only
                  (:release-grade-artifact-status conformance))
               :jvm-classfile-not-claimed?
               (false? (:classfiles-emitted? backend-status))
               :jar-not-claimed? (false? (:jar-emitted? backend-status))
               :metadata-preserved?
               (= :preserved (get-in backend-report
                                      [:source-debug-map-record
                                       :status]))
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"B1-INPUT" "B5-MANIFEST" "B5-NULL"
                    "B13-PROVENANCE" "B13-RELEASE"
                    "B14-ARTIFACT"}
                  (set (:required-diagnostic-ids conformance)))
               :limitations {:clojure-instruction-runner? true
                             :verified-mir-input? false
                             :target-lowering? false
                             :jvm-classfiles? false
                             :jar-artifact? false
                             :release-grade-artifact? false
                             :self-hosted-compiler? false
                             :next-required-capability
                             :lower-verified-mir-to-real-jvm-backend-artifacts}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-backend-proof
         :phase "07"
         :task "P07-S1"
         :governing-documents ["D1" "B1" "B5" "B13" "B14"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :backend-report
         (select-keys backend-report
                      [:kind :report-id :document-set
                       :compiled-plan-id
                       :backend-input-record
                       :jvm-backend-status
                       :artifact-manifest
                       :artifact-graph
                       :source-debug-map-record
                       :backend-conformance-results
                       :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :rejected-fixtures stage0-compiled-backend-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime :clojure/jvm
                            :backend
                            :gravity.backend/stage0-jvm-instruction-runner
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :verified-mir-input? false
                            :target-lowering? false
                            :self-hosted-compiler? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

(defn hosted-core-compiled-backend-proof-file-artifact
  [path]
  (hosted-core-compiled-backend-proof-source-artifact path (slurp path)))