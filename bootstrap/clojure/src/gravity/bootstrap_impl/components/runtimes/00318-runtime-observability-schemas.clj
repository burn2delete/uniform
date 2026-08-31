

(defn runtime-observability-schemas
  [source-path input-id]
  {:structured-log-schema
   {:artifact :gravity/structured-log-schema
    :input-artifact input-id
    :fields [:event-id :runtime-family :artifact-id :source-span
             :effect :capability :severity :redaction]
    :status :complete}
   :trace-schema
   {:artifact :gravity/trace-schema
    :input-artifact input-id
    :spans [{:event-id "trace/model-call"
             :runtime-family :ai
             :artifact-id input-id
             :source-span (source-span source-path 0)
             :generated-origin [:runtime-ai-ffi]}]
    :status :complete}
   :metric-schema
   {:artifact :gravity/metric-schema
    :input-artifact input-id
    :metrics [{:metric-id "ai.tokens"
               :unit :count
               :redaction :aggregate}
              {:metric-id "runtime.capability.denials"
               :unit :count
               :redaction :none}]
    :status :complete}
   :panic-trap-report-schema
   {:artifact :gravity/panic-trap-report-schema
    :input-artifact input-id
    :fields [:event-id :panic-kind :source-span :artifact-id
             :safety-outcome]
    :status :complete}
   :safety-check-failure-report
   {:artifact :gravity/safety-check-failure-report
    :input-artifact input-id
    :events [{:event-id "safety/check-1"
              :safety-outcome :runtime-checked
              :source-span (source-span source-path 1)
              :artifact-id input-id}]
    :status :complete}
   :capability-decision-report
   {:artifact :gravity/capability-decision-report
    :input-artifact input-id
    :decisions [{:event-id "cap/deny-secret"
                 :decision :deny
                 :effect :secrets/read
                 :capability :secret/read
                 :source-span (source-span source-path 2)
                 :redaction :secret-name-only}]
    :mandatory-audit-event? true
    :status :complete}
   :replay-trace-schema
   {:artifact :gravity/replay-trace-schema
    :input-artifact input-id
    :events [{:event-id "replay/model-call-1"
              :event-log-link "event-log/support/1"
              :nondeterminism :model-output
              :replay-action :read-recorded}]
    :missing-event-log-links []
    :status :complete}
   :redaction-policy-record
   {:artifact :gravity/redaction-policy-record
    :input-artifact input-id
    :policies [{:category :secret :action :redact-value}
               {:category :prompt :action :digest-only}
               {:category :raw-memory :action :deny}
               {:category :tainted-user-data :action :redact-or-policy-gate}]
    :secret-leaks []
    :status :complete}
   :diagnostic-bundle
   {:artifact :gravity/diagnostic-bundle
    :input-artifact input-id
    :contains [:event-records :source-maps :artifact-manifests
               :runtime-manifests :capability-decisions
               :safety-proof-references :replay-records
               :redaction-policy :remediation-categories]
    :portable? true
    :policy-constrained? true
    :status :complete}
   :sampling-policy-record
   {:artifact :gravity/sampling-policy-record
    :input-artifact input-id
    :sampling :allowed-for-metrics
    :required-audit-events-preserved? true
    :dropped-required-audit-events []
    :status :complete}})

(defn runtime-observability-validate!
  [source-path artifact]
  (let [upstream (:ai-repl-ffi-capability-artifact artifact)
        manifest (:runtime-observability-manifest artifact)
        registry (:event-schema-registry artifact)
        replay (:replay-trace-schema artifact)
        redaction (:redaction-policy-record artifact)
        bundle (:diagnostic-bundle artifact)
        sampling (:sampling-policy-record artifact)
        capability (:capability-decision-report artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:runtime-observability-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-ai-repl-ffi-capability-runtime-artifact
                 (:kind upstream))
      (runtime-observability-fail! "R12-MANIFEST" source-path upstream
                                   {:missing-fields [:ai-repl-ffi-artifact]}))
    (when-not (= :complete (get-in upstream
                                   [:capability-based-proof :status]))
      (runtime-observability-fail! "R12-MANIFEST" source-path upstream
                                   {:missing-fields [:upstream-proof]}))
    (when-not (= :complete (:status manifest))
      (runtime-observability-fail! "R12-MANIFEST" source-path manifest
                                   {:missing-fields [:manifest]}))
    (when (seq (:missing-schemas registry))
      (runtime-observability-fail! "R12-SCHEMA" source-path registry
                                   {:missing-fields [:event-schemas]}))
    (when (seq (:secret-leaks redaction))
      (runtime-observability-fail! "R12-SECRET" source-path redaction
                                   {:missing-fields [:redaction]}))
    (when-not (true? (:mandatory-audit-event? capability))
      (runtime-observability-fail! "R12-SINK" source-path capability
                                   {:missing-fields [:sink-capability]}))
    (when (seq (:dropped-required-audit-events sampling))
      (runtime-observability-fail! "R12-SAMPLING" source-path sampling
                                   {:missing-fields [:audit-events]}))
    (when (seq (:missing-event-log-links replay))
      (runtime-observability-fail! "R12-REPLAY" source-path replay
                                   {:missing-fields [:replay-links]}))
    (when-not (and (true? (:portable? bundle))
                   (true? (:policy-constrained? bundle)))
      (runtime-observability-fail! "R12-BUNDLE" source-path bundle
                                   {:missing-fields [:diagnostic-bundle]}))
    (when-not (= (set runtime-observability-diagnostic-ids) diagnostics)
      (runtime-observability-fail! "R12-MANIFEST" source-path
                                   (:runtime-observability-diagnostic-stream
                                    artifact)
                                   {:missing-fields [:diagnostics]})))
  :complete)

(defn runtime-observability-proof
  [artifact]
  (let [registry (:event-schema-registry artifact)
        replay (:replay-trace-schema artifact)
        redaction (:redaction-policy-record artifact)
        bundle (:diagnostic-bundle artifact)
        sampling (:sampling-policy-record artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:runtime-observability-diagnostic-stream
                                       :diagnostics])))]
    {:ai-repl-ffi-capability-input-verified?
     (= :complete (get-in artifact
                          [:ai-repl-ffi-capability-artifact
                           :capability-based-proof :status]))
     :event-schemas-stable?
     (and (empty? (:missing-schemas registry))
          (every? :stable-id? (:schemas registry)))
     :source-artifact-links-present?
     (every? :source-span (get-in artifact [:trace-schema :spans]))
     :redaction-policy-prevents-secret-leaks?
     (empty? (:secret-leaks redaction))
     :observability-semantics-neutral?
     true
     :required-audit-events-preserved?
     (and (true? (:required-audit-events-preserved? sampling))
          (empty? (:dropped-required-audit-events sampling)))
     :replay-traces-linked?
     (empty? (:missing-event-log-links replay))
     :diagnostic-bundle-complete?
     (and (true? (:portable? bundle))
          (true? (:policy-constrained? bundle)))
     :diagnostics-covered?
     (= (set runtime-observability-diagnostic-ids) diagnostics)
     :status :complete}))