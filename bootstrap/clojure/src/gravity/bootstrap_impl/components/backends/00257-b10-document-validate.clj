

(defn b10-document-validate!
  [source-path artifact]
  (let [specialized (:specialized-lowering-artifact artifact)
        manifest (:workflow-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b10-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-specialized-lowering-artifact
                 (:kind specialized))
      (b10-document-fail! "B10-MANIFEST" source-path specialized
                          {:missing-fields [:specialized-lowering-artifact]}))
    (when-not (= :complete (get-in specialized
                                   [:capability-based-proof :status]))
      (b10-document-fail! "B10-MANIFEST" source-path specialized
                          {:missing-fields [:specialized-lowering-proof]}))
    (when-not (= :durable-workflow (get-in manifest [:target :runtime]))
      (b10-document-fail! "B10-MANIFEST" source-path manifest
                          {:missing-fields [:target-runtime]}))
    (when-not (= :complete (get-in manifest
                                   [:workflow-ir-handoff-record :status]))
      (b10-document-fail! "B10-MANIFEST" source-path manifest
                          {:missing-fields [:workflow-ir-handoff-record]}))
    (when-not (= :complete (get-in manifest
                                   [:workflow-graph-artifact :status]))
      (b10-document-fail! "B10-GRAPH" source-path manifest
                          {:missing-fields [:workflow-graph-artifact]}))
    (when-not (b10-document-workflow-graph-structurally-valid?
               b10-document-workflow-graph)
      (b10-document-fail! "B10-GRAPH" source-path manifest
                          {:missing-fields [:workflow-graph-structure]}))
    (when-not (= :complete (get-in manifest
                                   [:step-schema-bundle :status]))
      (b10-document-fail! "B10-SCHEMA" source-path manifest
                          {:missing-fields [:step-schema-bundle]}))
    (when-not (= :complete (get-in manifest
                                   [:event-log-schema :status]))
      (b10-document-fail! "B10-REPLAY" source-path manifest
                          {:missing-fields [:event-log-schema]}))
    (when-not (= :complete (get-in manifest [:replay-policy :status]))
      (b10-document-fail! "B10-REPLAY" source-path manifest
                          {:missing-fields [:replay-policy]}))
    (when-not (b10-document-replay-fixture-structurally-valid?
               b10-document-replay-fixture)
      (b10-document-fail! "B10-REPLAY" source-path manifest
                          {:missing-fields [:replay-fixture-structure]}))
    (when-not (= :complete (get-in manifest
                                   [:idempotency-key-map :status]))
      (b10-document-fail! "B10-IDEMPOTENCY" source-path manifest
                          {:missing-fields [:idempotency-key-map]}))
    (when-not (= :complete
                 (get-in manifest
                         [:retry-timeout-cancellation-compensation-table
                          :status]))
      (b10-document-fail! "B10-RETRY" source-path manifest
                          {:missing-fields [:retry-timeout-cancellation]}))
    (when-not (every? #(contains? % :compensation)
                      (vals (get-in manifest
                                    [:retry-timeout-cancellation-compensation-table
                                     :steps])))
      (b10-document-fail! "B10-COMPENSATION" source-path manifest
                          {:missing-fields [:compensation-handlers]}))
    (when-not (= :complete (get-in manifest
                                   [:external-capability-manifest
                                    :status]))
      (b10-document-fail! "B10-CAPABILITY" source-path manifest
                          {:missing-fields [:external-capability-manifest]}))
    (when-not (= :rejected (get-in manifest
                                   [:external-capability-manifest
                                    :ambient-authority]))
      (b10-document-fail! "B10-CAPABILITY" source-path manifest
                          {:missing-fields [:ambient-authority-rejection]}))
    (when-not (= :complete (get-in manifest [:policy-graph :status]))
      (b10-document-fail! "B10-POLICY" source-path manifest
                          {:missing-fields [:policy-graph]}))
    (when-not (= :complete (get-in manifest
                                   [:human-review-policy-graph
                                    :status]))
      (b10-document-fail! "B10-POLICY" source-path manifest
                          {:missing-fields [:human-review-policy-graph]}))
    (when-not (= :complete (get-in manifest
                                   [:taint-validation-report :status]))
      (b10-document-fail! "B10-TAINT" source-path manifest
                          {:missing-fields [:taint-validation-report]}))
    (when-not (empty? (get-in manifest
                              [:taint-validation-report
                               :unvalidated-trusted-sinks]))
      (b10-document-fail! "B10-TAINT" source-path manifest
                          {:missing-fields [:validated-trusted-sinks]}))
    (when-not (= :complete (get-in manifest
                                   [:graph-validation-report :status]))
      (b10-document-fail! "B10-GRAPH" source-path manifest
                          {:missing-fields [:graph-validation-report]}))
    (when-not (= :matched (get-in manifest
                                  [:differential-replay-record :status]))
      (b10-document-fail! "B10-REPLAY" source-path manifest
                          {:missing-fields [:differential-replay-record]}))
    (when-not (= :complete (get-in manifest
                                   [:audit-provenance-record :status]))
      (b10-document-fail! "B10-MANIFEST" source-path manifest
                          {:missing-fields [:audit-provenance-record]}))
    (when-not (= :preserved (get-in manifest [:source-debug-map :status]))
      (b10-document-fail! "B10-MANIFEST" source-path manifest
                          {:missing-fields [:source-debug-map]}))
    (when-not (every? #(contains? manifest %)
                      [:workflow-ir-handoff-record
                       :workflow-graph-artifact
                       :step-schema-bundle
                       :event-log-schema
                       :replay-policy
                       :replay-fixtures
                       :idempotency-key-map
                       :retry-timeout-cancellation-compensation-table
                       :external-capability-manifest
                       :tool-model-provider-manifest
                       :human-review-policy-graph
                       :policy-graph
                       :taint-validation-report
                       :audit-provenance-record
                       :graph-validation-report
                       :differential-replay-record
                       :source-debug-map
                       :external-runtime-validation-record])
      (b10-document-fail! "B10-MANIFEST" source-path manifest
                          {:missing-fields [:workflow-artifact-manifest]}))
    (when-not (= (set b10-document-diagnostic-ids) diagnostics)
      (b10-document-fail! "B10-MANIFEST" source-path
                          (:b10-diagnostic-stream artifact)
                          {:missing-fields [:b10-diagnostics]})))
  :complete)

(defn b10-document-capability-proof
  [artifact]
  (let [manifest (:workflow-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b10-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:specialized-lowering-artifact
                           :capability-based-proof :status]))
     :workflow-ir-handoff-covered?
     (= :complete (get-in manifest
                          [:workflow-ir-handoff-record :status]))
     :workflow-graph-emitted?
     (= :complete (get-in manifest
                          [:workflow-graph-artifact :status]))
     :workflow-graph-structurally-valid?
     (b10-document-workflow-graph-structurally-valid?
      b10-document-workflow-graph)
     :schema-bundle-covered?
     (= :complete (get-in manifest [:step-schema-bundle :status]))
     :event-log-schema-covered?
     (= :complete (get-in manifest [:event-log-schema :status]))
     :replay-policy-covered?
     (= :complete (get-in manifest [:replay-policy :status]))
     :replay-fixture-covered?
     (and (= :complete (get-in manifest [:replay-fixtures 0 :status]))
          (b10-document-replay-fixture-structurally-valid?
           b10-document-replay-fixture))
     :idempotency-covered?
     (= :complete (get-in manifest [:idempotency-key-map :status]))
     :retry-timeout-cancellation-covered?
     (= :complete
        (get-in manifest
                [:retry-timeout-cancellation-compensation-table
                 :status]))
     :compensation-covered?
     (every? #(contains? % :compensation)
             (vals (get-in manifest
                           [:retry-timeout-cancellation-compensation-table
                            :steps])))
     :capability-manifest-covered?
     (= :complete (get-in manifest
                          [:external-capability-manifest :status]))
     :ambient-authority-rejected?
     (= :rejected (get-in manifest
                          [:external-capability-manifest
                           :ambient-authority]))
     :tool-model-provider-manifest-covered?
     (= :complete (get-in manifest
                          [:tool-model-provider-manifest :status]))
     :human-review-policy-covered?
     (= :complete (get-in manifest
                          [:human-review-policy-graph :status]))
     :policy-graph-covered?
     (= :complete (get-in manifest [:policy-graph :status]))
     :taint-validation-covered?
     (and (= :complete (get-in manifest
                               [:taint-validation-report :status]))
          (empty? (get-in manifest
                          [:taint-validation-report
                           :unvalidated-trusted-sinks])))
     :audit-provenance-preserved?
     (= :complete (get-in manifest
                          [:audit-provenance-record :status]))
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :differential-replay-matched?
     (= :matched (get-in manifest
                         [:differential-replay-record :status]))
     :manifest-complete?
     (every? #(contains? manifest %)
             [:workflow-ir-handoff-record
              :workflow-graph-artifact
              :step-schema-bundle
              :event-log-schema
              :replay-policy
              :replay-fixtures
              :idempotency-key-map
              :retry-timeout-cancellation-compensation-table
              :external-capability-manifest
              :tool-model-provider-manifest
              :human-review-policy-graph
              :policy-graph
              :taint-validation-report
              :audit-provenance-record
              :graph-validation-report
              :differential-replay-record
              :source-debug-map
              :external-runtime-validation-record])
     :diagnostics-covered?
     (= (set b10-document-diagnostic-ids) diagnostics)
     :external-runtime-validation?
     (get-in manifest [:external-runtime-validation-record :status])
     :status :complete}))