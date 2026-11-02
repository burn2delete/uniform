

(defn p13-tooling-proof
  [artifact]
  (let [documents (:document-contracts artifact)
        accepted-docs (set (map :document (:accepted-tooling-fixtures artifact)))
        rejected-docs (set (map :document (:rejected-tooling-fixtures artifact)))
        conformance-docs (set (map :document
                                   (:tooling-conformance-evidence artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:tooling-diagnostic-stream
                                       :diagnostics])))]
    {:cli-and-project-workflow-covered?
     (and (= :complete (:status (:cli-command-set artifact)))
          (true? (get-in artifact [:cli-command-set :json-output]))
          (contains? (set (get-in artifact
                                  [:cli-command-set
                                   :capability-prompts
                                   :shown-denials]))
                     :shell/exec))
     :repl-and-incremental-dev-covered?
     (and (= :complete (:status (:repl-session-artifact artifact)))
          (true? (get-in artifact
                         [:repl-session-artifact :transcript-redacted]))
          (= :complete (:status (:dev-server-session artifact))))
     :formatter-linter-docs-covered?
     (and (true? (get-in artifact
                         [:formatter-fixture :reader-round-trip]))
          (set/subset? #{:types :effects :capabilities :profile :artifacts}
                       (set (get-in artifact
                                    [:linter-diagnostic-report
                                     :compiler-facts])))
          (true? (get-in artifact [:documentation-artifact :structured-docs])))
     :lsp-and-debugger-covered?
     (and (true? (get-in artifact
                         [:lsp-capability-matrix
                          :diagnostics-match-cli]))
          (= :passed (get-in artifact
                             [:debugger-trace
                              :source-map-validation :status])))
     :dev-registry-and-inspectors-covered?
     (boolean
      (and (true? (get-in artifact
                          [:registry-ux-record :update-diff
                           :capability-diff-visible]))
           (p13-present? (get-in artifact
                                 [:ir-inspector-bundle
                                  :source-span-maps]))
           (p13-present? (get-in artifact
                                 [:profiler-report
                                  :check-elision-report :evidence]))
           (p13-present? (get-in artifact
                                 [:safety-audit-report :proof-index]))))
     :ai-assisted-tooling-covered?
     (and (true? (get-in artifact
                         [:ai-tooling-record
                          :patch-artifact :validated]))
          (= :approved
             (get-in artifact
                     [:ai-tooling-record
                      :human-review-record :decision]))
          (false? (get-in artifact
                          [:ai-tooling-record
                           :replay-trace
                           :hidden-tool-use-detected])))
     :document-coverage-complete?
     (= (set p13-tooling-documents) (set (keys documents)))
     :accepted-fixtures-covered?
     (= (set p13-tooling-documents) accepted-docs)
     :rejected-fixtures-covered?
     (= (set p13-tooling-documents) rejected-docs)
     :conformance-evidence-covered?
     (= (set p13-tooling-documents) conformance-docs)
     :diagnostics-covered?
     (set/subset? (set p13-tooling-diagnostic-ids) diagnostics)
     :task-statuses (p13-task-statuses)
     :status :complete}))