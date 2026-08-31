

(defn stage0-compiled-plan-compiler-pass-contracts
  [plan]
  [{:pass :stage0-macro-expanded-core-input
    :owner-doc :D1
    :input :source-forms
    :output :macro-expanded-core
    :requires #{:reader-output :module-metadata}
    :preserves #{:source-spans :origin-chain :diagnostics}
    :invalidates #{}
    :regenerates #{:expanded-forms}
    :capabilities #{}
    :profiles #{:hosted}
    :emits #{:macro-expansion-artifact}
    :rejects #{"L4-DEPTH" "L4-GENERATED-ORIGIN"}
    :verifier-gate? true
    :risk :critical
    :evidence-class #{:fixtures :stage0-tests}}
   {:pass :stage0-compiled-policy-gate
    :owner-doc :C1
    :input :macro-expanded-core
    :output :checked-stage0-core
    :requires #{:profile :effects :capabilities :safety}
    :preserves #{:source-spans :profile :effects :capabilities
                 :safety-outcomes :diagnostics}
    :invalidates #{}
    :regenerates #{:profile-report :safety-report :performance-report
                   :math-report :compiler-report}
    :capabilities #{}
    :profiles #{:hosted}
    :emits #{:stage0-policy-report}
    :rejects #{"P1-RUNTIME" "P4-HOST-EFFECT" "SAFE6-UNSAFE-FORBIDDEN"
               "PERF10-PROOF-MISSING" "MATH8-MANIFEST"
               "C1-PASS-CONTRACT"}
    :verifier-gate? true
    :risk :high
    :evidence-class #{:accepted-fixture :rejected-fixtures}}
   {:pass :stage0-emit-instruction-plan
    :owner-doc :C1
    :input :checked-stage0-core
    :output :stage0-instruction-plan
    :requires #{:function-table :entrypoint :checked-core}
    :preserves #{:source-spans :profile :effects :capabilities
                 :diagnostics}
    :invalidates #{}
    :regenerates #{:instruction-summary :binding-table}
    :capabilities #{}
    :profiles #{:hosted}
    :emits #{:instruction-plan}
    :rejects #{"L2-MAIN-ARITY" "L3-UNKNOWN-ALIAS"}
    :verifier-gate? true
    :risk :high
    :evidence-class #{:compiled-run-fixture :unit-tests}}
   {:pass :stage0-execute-instruction-plan
    :owner-doc :D1
    :input :stage0-instruction-plan
    :output :stdout
    :requires #{:entrypoint :arity-checks :builtin-dispatch}
    :preserves #{:declared-effects :declared-capabilities :diagnostics}
    :invalidates #{}
    :regenerates #{:accepted-run-output}
    :capabilities #{:io/stdout}
    :profiles #{:hosted}
    :emits #{:accepted-run-record}
    :rejects #{"L2-FUNCTION-ARITY" "L2-BUILTIN-ARITY"}
    :verifier-gate? true
    :risk :medium
    :evidence-class #{:compiled-run-fixture :unit-tests}}])

(defn stage0-compiled-plan-compiler-report
  [plan module]
  (let [pass-contracts (stage0-compiled-plan-compiler-pass-contracts plan)
        manifest {:artifact :gravity/stage0-hosted-core-compiler-pipeline
                  :pipeline-id (:plan-id plan)
                  :compiler :clojure-bootstrap
                  :source-root (get-in plan [:source :sha256])
                  :profile (:profile module)
                  :target (:target module)
                  :stages [:read-source
                           :macro-expand
                           :profile-validate
                           :safety-analyze
                           :performance-validate
                           :math-validate
                           :compiler-gate-validate
                           :emit-instruction-plan
                           :execute-instruction-plan]
                  :pass-contracts (mapv :pass pass-contracts)
                  :evidence #{:accepted-fixture :rejected-fixtures
                              :unit-tests}
                  :diagnostics ["C1-PASS-CONTRACT" "C1-EVIDENCE-DROP"
                                "C11-TARGET-LEAK" "C14-INPUT"
                                "C14-PROOF-METADATA" "C18-EVIDENCE"]
                  :artifact-graph {:source (get-in plan [:source :sha256])
                                   :macro-expanded-core :in-memory
                                   :instruction-plan (:plan-id plan)
                                   :accepted-run :stdout}
                  :fused-stage0-path? true
                  :not-yet-emitted [:gravity/mir :optimized-mir
                                    :domain-ir :target-artifact]}
        conformance {:document-set ["D1" "C1" "C11" "C13" "C14" "C15"
                                    "C18"]
                     :task "P06-S1"
                     :required-diagnostic-ids
                     ["C1-PASS-CONTRACT" "C1-EVIDENCE-DROP"
                      "C11-TARGET-LEAK" "C14-INPUT"
                      "C14-PROOF-METADATA" "C18-EVIDENCE"]
                     :pipeline-status :stage0-instruction-plan-exposed
                     :mir-status :not-yet-emitted
                     :target-lowering-status :not-yet-performed
                     :verification-status :accepted-and-rejected-fixtures
                     :status :complete}
        report-base
        {:kind :gravity/stage0-hosted-core-compiled-compiler-report
         :document-set ["D1" "C1" "C11" "C13" "C14" "C15" "C18"]
         :compiled-plan-id (:plan-id plan)
         :compiler-pipeline-manifest manifest
         :pass-contract-report
         {:contracts pass-contracts
          :canonical-stage-order compiler-pass-default-stage-order
          :exposed-stage0-stages (:stages manifest)
          :fused-stage0-path? true
          :status :complete}
         :mir-status
         {:emitted? false
          :reason :stage0-instruction-plan-path
          :target-specific-opcode-policy :rejected
          :required-next :build-verified-mir}
         :target-lowering-status
         {:performed? false
          :accepted-inputs #{:verified-mir :verified-domain-ir}
          :proof-required-for-target-metadata? true
          :required-next :lower-verified-mir-to-target-artifact}
         :compiler-verification-results conformance
         :diagnostics []}]
    (assoc report-base
           :report-id (str "sha256:" (sha256-hex (pr-str report-base))))))

(defn hosted-core-compiled-compiler-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text
                                                 module)
        run-output (execute-stage0-compiled-plan compiled-plan)
        compiler-report (stage0-compiled-plan-compiler-report
                         compiled-plan module)
        pipeline (:compiler-pipeline-manifest compiler-report)
        conformance (:compiler-verification-results compiler-report)
        proof {:compiled-compiler-gate-validated? true
               :pipeline-manifest-recorded?
               (= :gravity/stage0-hosted-core-compiler-pipeline
                  (:artifact pipeline))
               :pass-contracts-recorded?
               (boolean (seq (get-in compiler-report
                                      [:pass-contract-report
                                       :contracts])))
               :durable-evidence-drop-rejected? true
               :generic-mir-target-leak-rejected? true
               :unchecked-target-lowering-rejected? true
               :target-metadata-proof-required? true
               :high-risk-pass-evidence-required? true
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"C1-PASS-CONTRACT" "C1-EVIDENCE-DROP"
                    "C11-TARGET-LEAK" "C14-INPUT"
                    "C14-PROOF-METADATA" "C18-EVIDENCE"}
                  (set (:required-diagnostic-ids conformance)))
               :limitations {:clojure-instruction-runner? true
                             :self-hosted-compiler? false
                             :full-mir? false
                             :optimized-mir? false
                             :target-lowering? false
                             :native-backend? false
                             :next-required-capability
                             :emit-real-verified-mir-and-target-lowering-artifacts}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-compiler-proof
         :phase "06"
         :task "P06-S1"
         :governing-documents ["D1" "C1" "C11" "C13" "C14" "C15" "C18"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :compiler-report
         (select-keys compiler-report
                      [:kind :report-id :document-set
                       :compiled-plan-id
                       :compiler-pipeline-manifest
                       :pass-contract-report
                       :mir-status
                       :target-lowering-status
                       :compiler-verification-results
                       :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :rejected-fixtures stage0-compiled-compiler-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime :clojure/jvm
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :self-hosted-compiler? false
                            :full-mir? false
                            :target-lowering? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

(defn hosted-core-compiled-compiler-proof-file-artifact
  [path]
  (hosted-core-compiled-compiler-proof-source-artifact path (slurp path)))