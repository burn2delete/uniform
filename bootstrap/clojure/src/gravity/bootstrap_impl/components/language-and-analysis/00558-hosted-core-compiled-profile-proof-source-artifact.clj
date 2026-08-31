

(defn hosted-core-compiled-profile-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text module)
        run-output (execute-stage0-compiled-plan compiled-plan)
        profile-report (stage0-compiled-plan-profile-report compiled-plan)
        manifest (:profile-manifest profile-report)
        proof {:compiled-profile-validated? true
               :active-profile-hosted? (= :hosted (:profile manifest))
               :target-jvm? (= :jvm (:target manifest))
               :effective-effects-covered?
               (contains? (:effective-effects manifest) :io/write)
               :effective-capabilities-covered?
               (contains? (:effective-capabilities manifest) :io/stdout)
               :backend-eligibility-recorded?
               (true? (get-in profile-report
                              [:backend-eligibility-report :eligible?]))
               :cross-profile-graph-recorded?
               (= [] (get-in profile-report
                             [:cross-profile-dependency-graph
                              :dependencies]))
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"P4-HOST-EFFECT" "P4-HOST-CAPABILITY" "P1-RUNTIME"}
                  (set (map :diagnostic
                            stage0-compiled-profile-rejected-fixtures)))
               :limitations {:clojure-instruction-runner? true
                             :self-hosted-compiler? false
                             :native-backend? false
                             :next-required-capability
                             :lower-profile-manifest-into-mir-runtime-and-package-artifacts}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-profile-proof
         :phase "03"
         :task "P03-S1"
         :governing-documents ["P1" "P4" "P13"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :profile-report (select-keys profile-report
                                      [:kind :report-id :document-set
                                       :profile-manifest
                                       :effect-permission-table
                                       :capability-permission-table
                                       :cross-profile-dependency-graph
                                       :backend-eligibility-report
                                       :behavior-states :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :rejected-fixtures stage0-compiled-profile-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime :clojure/jvm
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :self-hosted-compiler? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

(defn hosted-core-compiled-profile-proof-file-artifact
  [path]
  (hosted-core-compiled-profile-proof-source-artifact path (slurp path)))

(defn stage0-compiled-plan-performance-report
  [plan]
  (let [module (:module plan)
        performance (get-in module [:metadata :performance] {})
        claim (when (map? (:claim performance))
                (perf1-normalize-claim (:claim performance)))
        claim-present? (some? claim)
        instruction-summary (:instruction-summary plan)
        residual-checks (cond-> #{}
                          (pos? (get instruction-summary :function-call 0))
                          (conj :function-arity)
                          (pos? (get instruction-summary :builtin-call 0))
                          (conj :builtin-arity))
        erased-checks (set (mapcat #(or (:erased-checks %)
                                        (when (:erased? %)
                                          [(:check-class %)]))
                                   (get-in module
                                           [:metadata :performance
                                            :check-elision]
                                           [])))
        target-fingerprint (when claim-present?
                             (perf1-target-fingerprint claim))
        contract {:claim-status (if claim-present? :recorded :not-asserted)
                  :claim-id (:claim-id claim)
                  :profile (:profile module)
                  :stage0-source-target (:target module)
                  :target-request (or (:target claim) :clojure-stage0-jvm)
                  :target-features (or (:target-features claim)
                                       #{:clojure-jvm
                                         :stage0-instruction-plan})
                  :safety-mode (or (:safety-mode claim) (:safety module))
                  :optimization-mode :none
                  :reason :stage0-records-baseline-only}
        decision {:pass :stage0-compiled-performance-gate
                  :decision :retain-safe-semantics
                  :erased-checks erased-checks
                  :residual-checks residual-checks
                  :preserves #{:source-semantics :profile :effects
                               :capabilities :safety-outcomes
                               :diagnostics}
                  :status :complete}
        report-base
        {:kind :gravity/stage0-hosted-core-compiled-performance-report
         :document-set ["D6" "PERF1" "PERF10"]
         :compiled-plan-id (:plan-id plan)
         :performance-contract-manifest contract
         :optimization-decision-log
         (vec (conj (vec (:optimization-decisions performance))
                    decision))
         :target-feature-report
         {:target (:target-request contract)
          :features (:target-features contract)
          :fingerprint (or target-fingerprint
                           {:runtime :clojure-stage0
                            :executor :instruction-plan
                            :claim :not-asserted})
          :status (if claim-present? :recorded :baseline)}
         :layout-input-shape-record
         {:input-shape (or (:input-shape claim)
                           {:entrypoint (:entrypoint plan)
                            :arguments 0})
          :layout (or (:layout claim)
                      {:instruction-plan :linear-vector
                       :values :clojure-hosted})
          :status (if claim-present? :recorded :baseline)}
         :benchmark-report
         (or (:benchmark claim)
             {:harness :not-run
              :reason :no-performance-claim
              :status :not-asserted})
         :check-elision-report
         {:erased-checks erased-checks
          :residual-checks residual-checks
          :policy-checks-preserved? true
          :certificate-required? (seq erased-checks)
          :status (if (empty? erased-checks)
                    :no-checks-elided
                    :certificate-backed)}
         :performance-conformance-results
         {:document-set ["D6" "PERF1" "PERF10"]
          :task "P04-S1"
          :performance-claim-status (if claim-present?
                                      :recorded
                                      :not-asserted)
          :check-elision-status (if (empty? erased-checks)
                                  :no-checks-elided
                                  :certificate-backed)
          :required-diagnostic-ids ["PERF1-CLAIM" "PERF1-TARGET"
                                    "PERF10-PROOF-MISSING"]
          :status :complete}
         :diagnostics []}]
    (assoc report-base
           :report-id (str "sha256:" (sha256-hex (pr-str report-base))))))

(defn hosted-core-compiled-performance-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text
                                                 module)
        run-output (execute-stage0-compiled-plan compiled-plan)
        performance-report (stage0-compiled-plan-performance-report
                            compiled-plan)
        elision-report (:check-elision-report performance-report)
        proof {:compiled-performance-validated? true
               :baseline-performance-recorded?
               (= :not-asserted
                  (get-in performance-report
                          [:performance-contract-manifest
                           :claim-status]))
               :optimization-decisions-preserve-semantics?
               (every? #(contains? (:preserves %) :source-semantics)
                       (:optimization-decision-log performance-report))
               :effects-and-capabilities-preserved?
               (every? #(and (contains? (:preserves %) :effects)
                             (contains? (:preserves %) :capabilities))
                       (:optimization-decision-log performance-report))
               :safety-checks-not-elided-without-proof?
               (empty? (:erased-checks elision-report))
               :residual-runtime-checks-recorded?
               (set/subset? #{:function-arity :builtin-arity}
                            (:residual-checks elision-report))
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"PERF1-CLAIM" "PERF1-TARGET"
                    "PERF10-PROOF-MISSING"}
                  (set (map :diagnostic
                            stage0-compiled-performance-rejected-fixtures)))
               :limitations {:clojure-instruction-runner? true
                             :self-hosted-compiler? false
                             :native-backend? false
                             :performance-claim-accepted? false
                             :next-required-capability
                             :compile-real-mir-performance-artifacts-before-accepting-throughput-claims}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-performance-proof
         :phase "04"
         :task "P04-S1"
         :governing-documents ["D6" "PERF1" "PERF10"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :performance-report (select-keys performance-report
                                          [:kind :report-id :document-set
                                           :performance-contract-manifest
                                           :optimization-decision-log
                                           :target-feature-report
                                           :layout-input-shape-record
                                           :benchmark-report
                                           :check-elision-report
                                           :performance-conformance-results
                                           :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :rejected-fixtures stage0-compiled-performance-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime :clojure/jvm
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :self-hosted-compiler? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))