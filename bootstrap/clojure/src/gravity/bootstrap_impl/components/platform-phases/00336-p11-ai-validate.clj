

(defn p11-ai-validate!
  [source-path artifact]
  (let [documents (:document-contracts artifact)
        accepted (:accepted-ai-fixtures artifact)
        rejected (:rejected-ai-fixtures artifact)
        conformance (:ai-conformance-evidence artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:ai-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= (set p11-ai-documents) (set (:document-set artifact)))
      (p11-ai-fail! "P11-MANIFEST" source-path artifact
                    {:missing-fields [:document-set]}))
    (doseq [artifact-key p11-ai-artifact-keys]
      (when-not (p11-present? (get artifact artifact-key))
        (p11-ai-fail! "P11-MANIFEST" source-path artifact
                      {:missing-fields [artifact-key]}))
      (when-not (= :complete (:status (get artifact artifact-key)))
        (p11-ai-fail! "P11-MANIFEST" source-path (get artifact artifact-key)
                      {:missing-fields [:status]})))
    (when-not (every? #(contains? documents %) p11-ai-documents)
      (p11-ai-fail! "P11-MANIFEST" source-path documents
                    {:missing-fields [:document-contracts]}))
    (doseq [document p11-ai-documents
            :let [record (get documents document)
                  summary (p11-ai-document-summaries document)
                  contract (p11-ai-contracts document)]]
      (doseq [field [:document :task-id :governing-doc :agent-id
                     :workflow-id :diagnostics :evidence :owned-surface
                     :accepted-behavior :rejected-behavior :artifact-keys
                     :conformance]]
        (when-not (p11-present? (get record field))
          (p11-ai-fail! "P11-MANIFEST" source-path record
                        {:missing-fields [field]})))
      (when-not (= (:owned-surface summary) (:owned-surface record))
        (p11-ai-fail! "P11-MANIFEST" source-path record
                      {:missing-fields [:owned-surface]}))
      (doseq [[fact [diagnostic _]] contract]
        (when-not (and (contains? (set (:diagnostics record)) diagnostic)
                       (get-in record [:evidence fact]))
          (p11-ai-fail! diagnostic source-path record
                        {:missing-fields [fact]}))))
    (when-not (= (set p11-ai-documents) (set (map :document accepted)))
      (p11-ai-fail! "P11-ACCEPTED" source-path accepted
                    {:missing-fields [:accepted-ai-fixtures]}))
    (when-not (= (set p11-ai-documents) (set (map :document rejected)))
      (p11-ai-fail! "P11-REJECTED" source-path rejected
                    {:missing-fields [:rejected-ai-fixtures]}))
    (when-not (= (set p11-ai-documents) (set (map :document conformance)))
      (p11-ai-fail! "P11-CONFORMANCE" source-path conformance
                    {:missing-fields [:ai-conformance-evidence]}))
    (when-not (contains? (get-in artifact [:tool-schema :capabilities])
                         :ticket/write)
      (p11-ai-fail! "AI004" source-path (:tool-schema artifact)
                    {:missing-fields [:ticket-write-capability]}))
    (when-not (= :required-for-high-priority
                 (get-in artifact [:tool-schema :human-review]))
      (p11-ai-fail! "A4005" source-path (:tool-schema artifact)
                    {:missing-fields [:human-review]}))
    (when-not (seq (get-in artifact [:agent-manifest :eval-gates]))
      (p11-ai-fail! "A5005" source-path (:agent-manifest artifact)
                    {:missing-fields [:eval-gates]}))
    (when-not (= :recorded-effects
                 (get-in artifact [:workflow-graph :replay-mode]))
      (p11-ai-fail! "A6001" source-path (:workflow-graph artifact)
                    {:missing-fields [:replay-mode]}))
    (when-not (= :deny-by-default
                 (get-in artifact [:memory-policy :cross-tenant]))
      (p11-ai-fail! "A7004" source-path (:memory-policy artifact)
                    {:missing-fields [:tenant-partition]}))
    (when-not (= :untrusted-until-schema-validated
                 (get-in artifact [:policy-manifest :taint :ai-output]))
      (p11-ai-fail! "A8004" source-path (:policy-manifest artifact)
                    {:missing-fields [:taint-policy]}))
    (when-not (= :passed
                 (get-in artifact [:evaluation-report :release-gate]))
      (p11-ai-fail! "A9001" source-path (:evaluation-report artifact)
                    {:missing-fields [:eval-gate]}))
    (when-not (= :canonical-action-payload
                 (get-in artifact
                         [:human-review-manifest :payload-hash-rule]))
      (p11-ai-fail! "A10005" source-path (:human-review-manifest artifact)
                    {:missing-fields [:payload-hash-rule]}))
    (when-not (contains? (set (get-in artifact
                                      [:injection-defense
                                       :runtime-monitors]))
                         :denied-tool-escalation)
      (p11-ai-fail! "A11002" source-path (:injection-defense artifact)
                    {:missing-fields [:denied-tool-escalation]}))
    (when-not (set/subset? (set p11-ai-diagnostic-ids) diagnostics)
      (p11-ai-fail! "P11-MANIFEST" source-path (:ai-diagnostic-stream artifact)
                    {:missing-fields [:diagnostics]})))
  :complete)

(defn p11-task-statuses
  []
  (merge (zipmap ["P11-T01" "P11-T02" "P11-T03"
                  "P11-T04" "P11-T05" "P11-T06"]
                 (repeat :complete))
         (zipmap (map p11-task-id p11-ai-documents)
                 (repeat :complete))))

(defn p11-ai-proof
  [artifact]
  (let [documents (:document-contracts artifact)
        accepted-docs (set (map :document (:accepted-ai-fixtures artifact)))
        rejected-docs (set (map :document (:rejected-ai-fixtures artifact)))
        conformance-docs (set (map :document (:ai-conformance-evidence artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:ai-diagnostic-stream
                                       :diagnostics])))]
    {:ai-programming-surface-covered?
     (= :complete (:status (:ai-program-manifest artifact)))
     :provider-and-prompt-covered?
     (and (= :redacted
             (:credential-redaction (:model-manifest artifact)))
          (= :system-trusted
             (get-in artifact [:prompt-artifact :authority :system])))
     :tool-agent-memory-policy-covered?
     (and (= :required-for-high-priority
             (:human-review (:tool-schema artifact)))
          (seq (:eval-gates (:agent-manifest artifact)))
          (= :deny-by-default
             (:cross-tenant (:memory-policy artifact)))
          (= :untrusted-until-schema-validated
             (get-in artifact [:policy-manifest :taint :ai-output])))
     :workflow-replay-covered?
     (= :recorded-effects
        (:replay-mode (:workflow-graph artifact)))
     :evaluation-and-human-review-covered?
     (and (= :passed (:release-gate (:evaluation-report artifact)))
          (= :canonical-action-payload
             (:payload-hash-rule (:human-review-manifest artifact))))
     :injection-defense-covered?
     (contains? (set (get-in artifact
                             [:injection-defense :runtime-monitors]))
                :denied-tool-escalation)
     :document-coverage-complete?
     (= (set p11-ai-documents) (set (keys documents)))
     :accepted-fixtures-covered?
     (= (set p11-ai-documents) accepted-docs)
     :rejected-fixtures-covered?
     (= (set p11-ai-documents) rejected-docs)
     :conformance-evidence-covered?
     (= (set p11-ai-documents) conformance-docs)
     :diagnostics-covered?
     (set/subset? (set p11-ai-diagnostic-ids) diagnostics)
     :task-statuses (p11-task-statuses)
     :status :complete}))