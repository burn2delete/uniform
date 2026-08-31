

(defn ai-repl-ffi-capability-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (ai-repl-ffi-capability-source-overrides module)
        _ (ai-repl-ffi-capability-validate-source-overrides!
           source-path source-overrides)
        upstream-artifact
        (concurrency-distributed-file-artifact
         ai-repl-ffi-capability-upstream-artifact-path)
        input-id (:artifact-id upstream-artifact)
        repl-artifacts (repl-session-artifacts source-path input-id)
        ffi-artifacts (ffi-runtime-artifacts input-id)
        cap-artifacts (runtime-capability-artifacts source-path input-id)
        diagnostic-stream
        (ai-repl-ffi-capability-diagnostic-stream source-path input-id)
        artifact-base
        (merge
         {:kind :gravity/stage0-ai-repl-ffi-capability-runtime-artifact
          :task "P08-T05"
          :document-set ["R8" "R9" "R10" "R11"]
          :governing-documents ai-repl-ffi-capability-governing-documents
          :pass {:name :ai-repl-ffi-capability-runtime
                 :input :concurrency-distributed-runtime-artifact
                 :output :ai-repl-ffi-capability-runtime-artifact
                 :requires [:concurrency-distributed-runtime-artifact
                            :safe10-capability-policy
                            :safe11-taint-policy
                            :safe12-generated-code-gates
                            :safe13-ai-tool-policy]
                 :preserves [:source-spans :generated-origin :types
                             :effects :capabilities :taint :errors
                             :replay-records :audit-records
                             :artifact-provenance]
                 :emits [:ai-runtime-manifest :agent-runtime-state-record
                         :model-call-ledger
                         :prompt-provenance-digest-record
                         :tool-invocation-log
                         :structured-output-validation-report
                         :memory-access-retention-record
                         :policy-human-review-decision-record
                         :ai-budget-trace :ai-replay-barrier-record
                         :repl-runtime-manifest :session-transcript
                         :evaluated-form-artifact
                         :syntax-object-snapshot :macro-expansion-diff
                         :typed-core-snapshot :mir-domain-ir-snapshot
                         :repl-capability-decision-log
                         :incremental-invalidation-record
                         :hot-reload-record :ffi-runtime-manifest
                         :binding-manifest :symbol-resolution-record
                         :abi-layout-validation-report
                         :generated-adapter-artifact
                         :safe-wrapper-contract
                         :foreign-handle-lifetime-table
                         :callback-adapter-manifest
                         :ffi-unsafe-audit-record
                         :runtime-capability-manifest
                         :capability-table :principal-identity-record
                         :runtime-decision-log
                         :delegated-handle-record :revocation-record
                         :denial-diagnostic-record
                         :redaction-secret-handling-record
                         :capability-conformance-evidence
                         :ai-repl-ffi-capability-diagnostic-stream
                         :conformance-criteria-record]
                 :rejects ai-repl-ffi-capability-diagnostic-ids}
          :source-overrides source-overrides
          :module (select-keys module
                               [:module :source-path :profile :target
                                :effects :capabilities :safety :metadata])
          :concurrency-distributed-artifact
          (select-keys upstream-artifact
                       [:kind :task :artifact-id :capability-based-proof
                        :concurrency-distributed-results])
          :concurrency-distributed-artifact-kind (:kind upstream-artifact)
          :concurrency-distributed-artifact-hash input-id
          :upstream-artifact-source
          ai-repl-ffi-capability-upstream-artifact-path
          :ai-runtime-manifest (ai-runtime-manifest input-id)
          :agent-runtime-state-record (ai-runtime-state-record input-id)
          :model-call-ledger (model-call-ledger source-path input-id)
          :prompt-provenance-digest-record
          (prompt-provenance-digest-record input-id)
          :tool-invocation-log (tool-invocation-log input-id)
          :structured-output-validation-report
          (structured-output-validation-report input-id)
          :memory-access-retention-record
          (memory-access-retention-record input-id)
          :policy-human-review-decision-record
          (policy-human-review-decision-record input-id)
          :ai-budget-trace (ai-budget-trace input-id)
          :ai-replay-barrier-record (ai-replay-barrier-record input-id)
          :repl-runtime-manifest (repl-runtime-manifest input-id)
          :ffi-runtime-manifest (ffi-runtime-manifest input-id)
          :rejected-design-coverage
          (mapv (fn [id]
                  {:design (keyword (str/lower-case id))
                   :diagnostic id
                   :status :rejected})
                ai-repl-ffi-capability-diagnostic-ids)
          :conformance-criteria-record
          {:artifact :gravity/ai-repl-ffi-capability-conformance-record
           :ai-model-tool-memory-policy :complete
           :ai-human-review-replay-budget :complete
           :repl-session-and-pipeline-checks :complete
           :repl-hot-reload-and-invalidation :complete
           :ffi-binding-wrapper-handle-callback-safety :complete
           :runtime-capability-deny-delegate-revoke-audit :complete
           :secret-redaction-and-taint-policy :complete
           :status :passed}
          :ai-repl-ffi-capability-diagnostic-stream diagnostic-stream
          :ai-repl-ffi-capability-results
          {:documents ["R8" "R9" "R10" "R11"]
           :task "P08-T05"
           :required-diagnostic-ids ai-repl-ffi-capability-diagnostic-ids
           :concurrency-distributed-input-status :complete
           :ai-runtime-status :complete
           :model-ledger-status :complete
           :tool-log-status :complete
           :taint-validation-status :complete
           :human-review-status :complete
           :replay-status :complete
           :budget-status :complete
           :repl-runtime-status :complete
           :interactive-check-status :complete
           :hot-reload-status :complete
           :ffi-runtime-status :complete
           :ffi-binding-status :complete
           :ffi-wrapper-status :complete
           :ffi-callback-status :complete
           :capability-runtime-status :complete
           :delegation-status :complete
           :audit-redaction-status :complete
           :diagnostic-status :complete
           :status :complete}
          :diagnostics []}
         repl-artifacts
         ffi-artifacts
         cap-artifacts)
        _ (ai-repl-ffi-capability-validate! source-path artifact-base)
        capability-proof
        (ai-repl-ffi-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn ai-repl-ffi-capability-file-artifact
  [path]
  (ai-repl-ffi-capability-source-artifact path (slurp path)))