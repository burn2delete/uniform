

(defn p11-memory-policy
  []
  {:artifact :gravity/ai-memory-policy
   :memory-id "support-memory"
   :item-schema "SupportMemoryItem"
   :metadata-schema "SupportMemoryMetadata"
   :embedding {:model "support-embedding" :dimension 1536}
   :effects #{:ai/memory-read :ai/memory-write :ai/embedding}
   :capabilities #{:memory/read :memory/write :ai/provider.embedding}
   :partition [:tenant-id :project-id]
   :cross-tenant :deny-by-default
   :retention {:days 90}
   :redaction [:secrets :customer-pii]
   :prompt-policy :quote-as-untrusted-data
   :retrieved-taint :retrieved-data
   :replay :record-result-ids
   :retrieval-record [:item-ids :schema-ids :content-hashes
                      :ranking-scores :embedding-identity
                      :query-hash :taint-labels :access-decision
                      :replay-token]
   :status :complete})

(defn p11-policy-manifest
  []
  {:artifact :gravity/ai-policy-manifest
   :policy-id :support-agent-policy
   :allow #{:ticket/read :ai/model-call :ai/memory-read
            :ai/output-validate}
   :deny #{:secrets/read :shell/exec :package/publish}
   :human-review {:required-for #{:ticket/write}
                  :action-schema "TicketPriorityUpdate"
                  :reviewer-role :support-lead
                  :expiry "15m"
                  :payload-hash :canonical}
   :taint {:ai-output :untrusted-until-schema-validated
           :retrieved-memory :data-only
           :tool-output :validated-before-use}
   :fallback {:requires-eval true}
   :generated-code {:must-compile true :must-pass-tests true}
   :deployment-promotion {:requires-eval [:support-triage-release]}
   :decision-output [:allow :deny :require-human-review
                     :require-validation :require-eval
                     :narrow-and-continue]
   :status :complete})

(defn p11-evaluation-report
  []
  {:artifact :gravity/ai-evaluation-report
   :eval-id :support-triage-release
   :subject "support-triage"
   :subject-hash (c4-artifact-id [:support-triage :release])
   :dataset "SupportTriageCases/v4"
   :dataset-schema "SupportTriageCaseSet"
   :dataset-provenance :recorded
   :metrics {:schema-validity {:direction :equals :threshold 1.0
                               :observed 1.0}
             :unsafe-tool-denial {:direction :equals :threshold 1.0
                                  :observed 1.0}
             :finding-precision {:direction :min :threshold 0.85
                                 :observed 0.91}}
   :probes [:prompt-injection :tool-escalation :secret-exfiltration
            :schema-failure :refusal-handling]
   :provider-policy :pinned-or-eval-gated
   :budget {:max-cost-usd 25.00 :observed-cost-usd 0.00}
   :release-gate :passed
   :redaction :protected-samples-hashed
   :status :complete})

(defn p11-human-review-manifest
  []
  {:artifact :gravity/human-review-manifest
   :human-review-id :update-priority-review
   :version 1
   :action-schema "TicketPriorityUpdate"
   :requires-role :support-lead
   :evidence-schema "ReviewEvidence"
   :payload-hash-rule :canonical-action-payload
   :expires-in "15m"
   :states #{:requested :granted :denied :expired :revoked
             :bypassed-by-emergency-policy :replayed
             :invalidated-by-payload-change}
   :authorizing-states #{:granted :replayed}
   :on-deny :finish-without-write
   :replay :reuse-recorded-decision-if-payload-matches
   :audit-storage :redacted-with-hashes
   :emergency-policy :not-enabled
   :status :complete})

(defn p11-injection-defense
  []
  {:artifact :gravity/prompt-injection-defense
   :authority-levels #{:system-trusted :developer-trusted
                       :tool-schema-trusted :tool-result-data
                       :retrieved-data :user-data :ai-output-data
                       :secret-data :generated-source-data}
   :prompt-authority-partition :preserved
   :untrusted-content-policy :data-authority-only
   :tool-authorization-table {:ticket/update-priority
                              [:manifest :capability :policy
                               :human-review]}
   :taint-rules {:retrieved-memory :data-only
                 :tool-output :tainted-until-schema-validated
                 :ai-output :untrusted-until-schema-validated}
   :secret-policy :unavailable-to-prompts-unless-explicitly-granted
   :generated-code-policy :compile-before-use
   :defense-probes [:prompt-injection :tool-escalation
                    :secret-exfiltration :policy-override]
   :runtime-monitors [:denied-tool-escalation
                      :policy-override-attempt
                      :protected-data-exposure]
   :incident-bundle [:prompt-record :taint-flow :tool-decision
                     :policy-rule :denial-record]
   :status :complete})

(defn p11-ai-document-record
  [document]
  (let [summary (p11-ai-document-summaries document)]
    (merge
     {:document document
      :task-id (p11-task-id document)
      :governing-doc (p11-ai-governing-documents document)
      :agent-id "support-triage"
      :workflow-id "triage-ticket"
      :diagnostics (p11-contract-diagnostics document)
      :evidence (p11-contract-evidence document)
      :conformance
      {:accepted-fixture "bootstrap/clojure/fixtures/accepted/ai-agentic.gravity"
       :rejected-fixture (str "bootstrap/clojure/fixtures/rejected/"
                              (p11-ai-rejected-fixture-names document))
       :artifact-evidence :ai-agentic-artifact
       :validation-command "clojure -M:test"
       :status :complete}}
     summary)))

(defn p11-ai-document-records
  []
  (into {} (map (fn [document] [document (p11-ai-document-record document)])
                p11-ai-documents)))

(defn p11-accepted-ai-fixtures
  []
  (mapv (fn [document]
          {:document document
           :fixture "bootstrap/clojure/fixtures/accepted/ai-agentic.gravity"
           :artifact (get-in (p11-ai-document-summaries document)
                             [:owned-surface])
           :evidence [(p11-task-id document)
                      (p11-ai-governing-documents document)]
           :status :accepted})
        p11-ai-documents))

(defn p11-rejected-ai-fixtures
  []
  (mapv (fn [document]
          {:document document
           :fixture (str "bootstrap/clojure/fixtures/rejected/"
                         (p11-ai-rejected-fixture-names document))
           :artifact :stable-ai-agentic-diagnostic
           :diagnostic (p11-ai-rejected-diagnostics document)
           :evidence [(p11-task-id document)
                      (p11-ai-governing-documents document)]
           :status :rejected})
        p11-ai-documents))

(defn p11-ai-conformance-evidence
  []
  (mapv (fn [document]
          {:document document
           :accepted-behavior (get-in (p11-ai-document-summaries document)
                                      [:accepted-behavior])
           :rejected-behavior (p11-ai-rejected-diagnostics document)
           :artifacts (get-in (p11-ai-document-summaries document)
                              [:artifact-keys])
           :validation ["clojure -M:test"
                        "clojure -M:gravity ai-agentic bootstrap/clojure/fixtures/accepted/ai-agentic.gravity"]
           :status :complete})
        p11-ai-documents))

(defn p11-ai-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/phase11-ai-agentic-diagnostic-stream
   :stage :ai-agentic
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           (let [document (p11-ai-diagnostic-document id)]
             {:artifact :gravity/diagnostic
              :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
              :diagnostic id
              :rule id
              :severity :error
              :stage :ai-agentic
              :document-id document
              :task (when document (p11-task-id document))
              :primary {:span (source-span source-path index)
                        :syntax-id (str "p11-ai-syntax-" index)
                        :artifact input-id}
              :missing-fact (or (some (fn [[_ [diagnostic missing-fact]]]
                                         (when (= id diagnostic) missing-fact))
                                       (get p11-ai-contracts document))
                                :ai_agentic_manifest)
              :remediation [{:kind :declare-typed-ai-artifact}
                            {:kind :preserve-taint-and-authority}
                            {:kind :require-capability-and-policy}
                            {:kind :record-replay-eval-human-review}]
              :ordering-key [id :ai-agentic]}))
         p11-ai-diagnostic-ids
         (range))
   :status :complete})

(defn p11-present?
  [value]
  (if (coll? value)
    (seq value)
    (some? value)))