

(defn hosted-core-compiled-ai-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text
                                                 module)
        run-output (execute-stage0-compiled-plan compiled-plan)
        ai-report (stage0-compiled-plan-ai-report compiled-plan module)
        manifest (:ai-manifest ai-report)
        ai-program (:ai-program-record ai-report)
        model (:model-record ai-report)
        prompt (:prompt-record ai-report)
        tool (:tool-record ai-report)
        agent (:agent-record ai-report)
        workflow (:workflow-record ai-report)
        memory (:memory-record ai-report)
        policy (:policy-record ai-report)
        evaluation (:evaluation-record ai-report)
        human-review (:human-review-record ai-report)
        injection-defense (:injection-defense-record ai-report)
        conformance (:ai-conformance-results ai-report)
        proof {:compiled-ai-gate-validated? true
               :ai-manifest-recorded?
               (= :complete (:status manifest))
               :ai-program-surface-recorded?
               (contains? (:ai-effects ai-program) :ai/model-call)
               :provider-and-prompt-recorded?
               (and (= :redacted (:credential-redaction model))
                    (= :system-trusted
                       (get-in prompt [:authority :system])))
               :tool-agent-memory-policy-recorded?
               (and (= :required-for-high-priority
                       (:human-review tool))
                    (seq (:eval-gates agent))
                    (= :deny-by-default (:cross-tenant memory))
                    (= :untrusted-until-schema-validated
                       (get-in policy [:taint :ai-output])))
               :workflow-replay-recorded?
               (= :recorded-effects (:replay-mode workflow))
               :evaluation-and-human-review-recorded?
               (and (= :passed (:release-gate evaluation))
                    (= :canonical-action-payload
                       (:payload-hash-rule human-review)))
               :injection-defense-recorded?
               (contains? (set (:runtime-monitors injection-defense))
                          :denied-tool-escalation)
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"AI004" "A2001" "A3003" "A4005" "A5005"
                    "A6001" "A7004" "A8004" "A9001" "A10005"
                    "A11002"}
                  (set (:required-diagnostic-ids conformance)))
               :limitations {:clojure-instruction-runner? true
                             :live-model-provider? false
                             :tool-execution? false
                             :memory-store? false
                             :workflow-engine? false
                             :human-review-service? false
                             :production-policy-runtime? false
                             :self-hosted-ai-tooling? false
                             :next-required-capability
                             :compile-and-run-real-ai-agentic-slices}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-ai-proof
         :phase "11"
         :task "P11-S1"
         :governing-documents ["D1" "A1-A11"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :ai-report
         (select-keys ai-report
                      [:kind :report-id :document-set
                       :compiled-plan-id
                       :ai-manifest
                       :ai-program-record
                       :model-record
                       :prompt-record
                       :tool-record
                       :agent-record
                       :workflow-record
                       :memory-record
                       :policy-record
                       :evaluation-record
                       :human-review-record
                       :injection-defense-record
                       :ai-conformance-results
                       :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :proof-command (str "clojure -M:gravity hosted-core-compiled-ai "
                             source-path)
         :rejected-fixtures stage0-compiled-ai-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime
                            :gravity.runtime/stage0-clojure-jvm-instruction-runner
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :live-model-provider? false
                            :tool-execution? false
                            :memory-store? false
                            :workflow-engine? false
                            :human-review-service? false
                            :production-policy-runtime? false
                            :self-hosted-ai-tooling? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

(defn hosted-core-compiled-ai-proof-file-artifact
  [path]
  (hosted-core-compiled-ai-proof-source-artifact path (slurp path)))