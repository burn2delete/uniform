

(defn runtime-observability-source-overrides
  [module]
  (get-in module [:metadata :runtime :observability] {}))

(defn runtime-observability-missing-policy
  [id]
  (case id
    "R12-SINK" :observability_sink_capability_grant
    "R12-SCHEMA" :runtime_event_schema_stable_identifier
    "R12-SOURCE" :source_generated_origin_or_artifact_link
    "R12-SECRET" :secret_taint_raw_memory_prompt_redaction
    "R12-SEMANTICS" :observability_does_not_change_semantics
    "R12-SAMPLING" :mandatory_audit_event_preservation
    "R12-REPLAY" :replay_trace_nondeterminism_event_log_links
    "R12-BUNDLE" :complete_diagnostic_bundle
    :complete_runtime_observability_artifact))

(defn runtime-observability-fail!
  [id source-path subject extra]
  (fail! id
         "P08 runtime observability validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :runtime-observability
                 :stage :runtime-observability
                 :profile (or (:profile subject) :ai)
                 :target (or (:target subject) :jvm)
                 :runtime-family :observability
                 :event-id (:event-id subject)
                 :artifact-id (:artifact-id subject)
                 :sink (:sink subject)
                 :redaction-policy (:redaction-policy subject)
                 :capability (:capability subject)
                 :missing-schema (:missing-schema subject)
                 :missing-link (:missing-link subject)
                 :missing-policy (runtime-observability-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-T06 requires runtime event schemas, logs, traces, metrics, panic/safety/capability/replay reports, redaction policy, diagnostic bundles, sink capability checks, source/provenance links, and semantics-preserving observability."}
                extra)))

(defn runtime-observability-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get runtime-observability-override-diagnostics fail-kind)]
      (runtime-observability-fail!
       id source-path
       {:event-id (str "event-" (name fail-kind))
        :artifact-id (str "artifact-" (name fail-kind))
        :sink fail-kind
        :redaction-policy fail-kind
        :capability fail-kind
        :missing-schema fail-kind
        :missing-link fail-kind}
       {:missing-fields [fail-kind]}))))

(defn runtime-observability-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/runtime-observability-diagnostic-stream
   :stage :runtime-observability
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :runtime-observability
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-observability-syntax-" index)
                      :artifact input-id}
            :runtime-family :observability
            :artifact-id input-id
            :event-id (case id
                        "R12-REPLAY" "event/replay"
                        "R12-SECRET" "event/secret"
                        "R12-SINK" "event/sink"
                        (str "event/" (str/lower-case id)))
            :sink (case id
                    "R12-SINK" :network-telemetry
                    :local-diagnostic-bundle)
            :redaction-policy (case id
                                "R12-SECRET" :required
                                :digest-or-redacted)
            :capability (case id
                          "R12-SINK" :observability/write
                          nil)
            :missing-schema (when (= "R12-SCHEMA" id) :event-schema)
            :missing-link (when (= "R12-SOURCE" id) :source-or-artifact)
            :missing-policy (runtime-observability-missing-policy id)
            :source-generated-origin-chain
            [:ai-repl-ffi-capability-runtime :runtime-observability]
            :facts {:observability-does-not-grant-authority true
                    :redaction-preserves-secret-policy true
                    :mandatory-audit-events-preserved true
                    :replay-events-linked-to-event-log true}
            :remediation [{:kind :declare-event-schema}
                          {:kind :attach-source-artifact-link}
                          {:kind :apply-redaction-policy}
                          {:kind :require-sink-capability}]
            :redactions []
            :ordering-key [id :runtime-observability]})
         runtime-observability-diagnostic-ids
         (range))
   :status :complete})

(defn runtime-observability-manifest
  [input-id]
  {:artifact :gravity/runtime-observability
   :input-artifact input-id
   :events #{:panic :trap :allocation :task :actor :workflow :model :tool
             :ffi :capability :safety-check :replay :metric}
   :requires #{:source-map :artifact-manifest :redaction-policy
               :sink-capability :event-schema-registry}
   :emits #{:structured-log :trace :metric :diagnostic-bundle}
   :rejects #{:secret-leak :ungranted-observability-sink
              :diagnostic-without-source-or-artifact
              :semantics-changing-observability}
   :development-only? false
   :status :complete})