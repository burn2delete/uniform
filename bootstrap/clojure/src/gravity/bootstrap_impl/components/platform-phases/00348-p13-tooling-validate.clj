

(defn p13-tooling-validate!
  [source-path artifact]
  (let [documents (:document-contracts artifact)
        accepted (:accepted-tooling-fixtures artifact)
        rejected (:rejected-tooling-fixtures artifact)
        conformance (:tooling-conformance-evidence artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:tooling-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= (set p13-tooling-documents)
                 (set (:document-set artifact)))
      (p13-tooling-fail! "P13-MANIFEST" source-path artifact
                         {:missing-fields [:document-set]}))
    (doseq [artifact-key p13-tooling-artifact-keys]
      (when-not (p13-present? (get artifact artifact-key))
        (p13-tooling-fail! "P13-MANIFEST" source-path artifact
                           {:missing-fields [artifact-key]}))
      (when-not (= :complete (:status (get artifact artifact-key)))
        (p13-tooling-fail! "P13-MANIFEST" source-path
                           (get artifact artifact-key)
                           {:missing-fields [:status]})))
    (when-not (every? #(contains? documents %) p13-tooling-documents)
      (p13-tooling-fail! "P13-MANIFEST" source-path documents
                         {:missing-fields [:document-contracts]}))
    (doseq [document p13-tooling-documents
            :let [record (get documents document)
                  summary (p13-tooling-document-summaries document)]]
      (doseq [field [:document :task-id :governing-doc :tool-id
                     :diagnostics :evidence :owned-surface
                     :accepted-behavior :rejected-behavior :artifact-keys
                     :conformance]]
        (when-not (p13-present? (get record field))
          (p13-tooling-fail! "P13-MANIFEST" source-path record
                             {:missing-fields [field]})))
      (when-not (= (:owned-surface summary) (:owned-surface record))
        (p13-tooling-fail! "P13-MANIFEST" source-path record
                           {:missing-fields [:owned-surface]}))
      (doseq [diagnostic (p13-tooling-diagnostics-by-document document)]
        (when-not (and (contains? (set (:diagnostics record)) diagnostic)
                       (get-in record [:evidence
                                       (keyword (str/lower-case diagnostic))]))
          (p13-tooling-fail! diagnostic source-path record
                             {:missing-fields [(keyword (str/lower-case diagnostic))]}))))
    (when-not (= (set p13-tooling-documents) (set (map :document accepted)))
      (p13-tooling-fail! "P13-ACCEPTED" source-path accepted
                         {:missing-fields [:accepted-tooling-fixtures]}))
    (when-not (= (set p13-tooling-documents) (set (map :document rejected)))
      (p13-tooling-fail! "P13-REJECTED" source-path rejected
                         {:missing-fields [:rejected-tooling-fixtures]}))
    (when-not (= (set p13-tooling-documents) (set (map :document conformance)))
      (p13-tooling-fail! "P13-CONFORMANCE" source-path conformance
                         {:missing-fields [:tooling-conformance-evidence]}))
    (when-not (set/subset?
               #{:check :build :test :run :repl :fmt :lint :doc :package
                 :registry :audit :verify :inspect-ir :profile :ai :explain}
               (set (get-in artifact [:cli-command-set :commands])))
      (p13-tooling-fail! "T1006" source-path (:cli-command-set artifact)
                         {:missing-fields [:commands]}))
    (when-not (true? (get-in artifact [:cli-command-set :json-output]))
      (p13-tooling-fail! "T1002" source-path (:cli-command-set artifact)
                         {:missing-fields [:json-output]}))
    (when-not (contains? (set (get-in artifact
                                      [:cli-command-set
                                       :capability-prompts
                                       :shown-denials]))
                         :shell/exec)
      (p13-tooling-fail! "T1003" source-path (:cli-command-set artifact)
                         {:missing-fields [:capability-denial]}))
    (when-not (and (:profile (:repl-session-artifact artifact))
                   (:target (:repl-session-artifact artifact)))
      (p13-tooling-fail! "T2001" source-path (:repl-session-artifact artifact)
                         {:missing-fields [:profile :target]}))
    (when-not (seq (get-in artifact
                           [:repl-session-artifact :capability-grants]))
      (p13-tooling-fail! "T2002" source-path (:repl-session-artifact artifact)
                         {:missing-fields [:capability-grants]}))
    (when-not (true? (get-in artifact
                             [:repl-session-artifact
                              :transcript-redacted]))
      (p13-tooling-fail! "T2004" source-path (:repl-session-artifact artifact)
                         {:missing-fields [:transcript-redacted]}))
    (when-not (true? (get-in artifact
                             [:formatter-fixture :reader-round-trip]))
      (p13-tooling-fail! "T3002" source-path (:formatter-fixture artifact)
                         {:missing-fields [:reader-round-trip]}))
    (when-not (and (true? (get-in artifact
                                  [:formatter-fixture :comments-preserved]))
                   (true? (get-in artifact
                                  [:formatter-fixture :metadata-preserved])))
      (p13-tooling-fail! "T3005" source-path (:formatter-fixture artifact)
                         {:missing-fields [:comments-preserved
                                           :metadata-preserved]}))
    (when-not (set/subset?
               #{:types :effects :capabilities :profile :artifacts}
               (set (get-in artifact
                            [:linter-diagnostic-report :compiler-facts])))
      (p13-tooling-fail! "T4004" source-path
                         (:linter-diagnostic-report artifact)
                         {:missing-fields [:compiler-facts]}))
    (when-not (true? (get-in artifact
                             [:lsp-capability-matrix
                              :diagnostics-match-cli]))
      (p13-tooling-fail! "T5001" source-path (:lsp-capability-matrix artifact)
                         {:missing-fields [:diagnostics-match-cli]}))
    (when-not (true? (get-in artifact
                             [:lsp-capability-matrix :trace-redacted]))
      (p13-tooling-fail! "T5005" source-path (:lsp-capability-matrix artifact)
                         {:missing-fields [:trace-redacted]}))
    (when-not (= :passed (get-in artifact
                                 [:debugger-trace
                                  :source-map-validation :status]))
      (p13-tooling-fail! "T6001" source-path (:debugger-trace artifact)
                         {:missing-fields [:source-map-validation]}))
    (when-not (true? (get-in artifact [:documentation-artifact :redacted]))
      (p13-tooling-fail! "T7005" source-path
                         (:documentation-artifact artifact)
                         {:missing-fields [:redacted]}))
    (when-not (zero? (get-in artifact
                             [:documentation-artifact
                              :example-validation-report :failed]))
      (p13-tooling-fail! "T7003" source-path
                         (:documentation-artifact artifact)
                         {:missing-fields [:example-validation-report]}))
    (when-not (true? (get-in artifact
                             [:dev-server-session :bug-report-redacted]))
      (p13-tooling-fail! "T8004" source-path (:dev-server-session artifact)
                         {:missing-fields [:bug-report-redacted]}))
    (when-not (some #(= :restart (:decision %))
                    (get-in artifact
                            [:dev-server-session :hot-reload-decisions]))
      (p13-tooling-fail! "T8003" source-path (:dev-server-session artifact)
                         {:missing-fields [:hot-reload-decisions]}))
    (when-not (true? (get-in artifact
                             [:registry-ux-record :update-diff
                              :capability-diff-visible]))
      (p13-tooling-fail! "T9001" source-path (:registry-ux-record artifact)
                         {:missing-fields [:capability-diff-visible]}))
    (when-not (p13-present? (get-in artifact
                                    [:ir-inspector-bundle
                                     :source-span-maps]))
      (p13-tooling-fail! "T10002" source-path
                         (:ir-inspector-bundle artifact)
                         {:missing-fields [:source-span-maps]}))
    (when-not (p13-present? (get-in artifact
                                    [:profiler-report
                                     :check-elision-report :evidence]))
      (p13-tooling-fail! "T11003" source-path (:profiler-report artifact)
                         {:missing-fields [:check-elision-evidence]}))
    (when-not (p13-present? (get-in artifact
                                    [:safety-audit-report :proof-index]))
      (p13-tooling-fail! "T12005" source-path (:safety-audit-report artifact)
                         {:missing-fields [:proof-index]}))
    (when-not (and (true? (get-in artifact
                                  [:ai-tooling-record
                                   :patch-artifact :validated]))
                   (= :approved
                      (get-in artifact
                              [:ai-tooling-record
                               :human-review-record :decision]))
                   (every? #(= :passed %)
                           (vals (get-in artifact
                                         [:ai-tooling-record
                                          :validation-report]))))
      (p13-tooling-fail! "T13002" source-path (:ai-tooling-record artifact)
                         {:missing-fields [:checked-generated-source]}))
    (when-not (set/subset? (set p13-tooling-diagnostic-ids) diagnostics)
      (p13-tooling-fail! "P13-MANIFEST" source-path
                         (:tooling-diagnostic-stream artifact)
                         {:missing-fields [:diagnostics]})))
  :complete)

(defn p13-task-statuses
  []
  (merge (zipmap ["P13-T01" "P13-T02" "P13-T03"
                  "P13-T04" "P13-T05" "P13-T06"]
                 (repeat :complete))
         (zipmap (map p13-task-id p13-tooling-documents)
                 (repeat :complete))))