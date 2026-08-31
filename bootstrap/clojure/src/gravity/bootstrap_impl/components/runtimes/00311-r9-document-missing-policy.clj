

(defn r9-document-missing-policy
  [id]
  (case id
    "R9-PROFILE" :interactive-runtime-profile-legality
    "R9-CHECKS" :normal-compiler-pipeline-before-evaluation
    "R9-CAPABILITY" :interactive-effect-grants
    "R9-SESSION" :tracked-session-state
    "R9-HERMETICITY" :build-affecting-session-state-artifact
    "R9-HOT-RELOAD" :stale-artifact-invalidation
    "R9-DEBUG" :debugger-capability-and-secret-policy
    "R9-AUDIT" :session-transcript-and-evaluated-form-record
    :complete-repl-runtime-artifact))

(defn r9-document-fail!
  [id source-path subject extra]
  (fail! id
         "R9 REPL and interactive runtime document coverage failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :r9-repl-runtime-document
                 :stage :r9-document-coverage
                 :document-id "R9"
                 :profile (or (:profile subject) :meta)
                 :target (or (:target subject) :jvm)
                 :runtime-family :interactive
                 :session-id (:session-id subject)
                 :compiler-phase (:compiler-phase subject)
                 :capability (:capability subject)
                 :effect (:effect subject)
                 :artifact-id (:artifact-id subject)
                 :missing-policy (r9-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-D120 requires REPL manifests, session transcripts, evaluated-form artifacts, syntax/macro/typed/MIR snapshots, capability decisions, incremental invalidation, hot reload records, audit logs, and R9 conformance evidence."}
                extra)))

(defn r9-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get r9-document-override-diagnostics fail-kind)]
      (r9-document-fail!
       id source-path
       {:session-id "session/failing"
        :compiler-phase fail-kind
        :effect fail-kind
        :capability fail-kind
        :artifact-id (str "r9-document-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))

(defn r9-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/r9-repl-runtime-diagnostic-stream
   :stage :r9-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :r9-document-coverage
            :document-id "R9"
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-r9-document-syntax-" index)
                      :artifact input-id}
            :profile :meta
            :target :jvm
            :runtime-family :interactive
            :session-id "session/stage0"
            :compiler-phase (case id
                              "R9-CHECKS" :type-check
                              "R9-HOT-RELOAD" :incremental
                              :interactive-eval)
            :effect (case id
                      "R9-CAPABILITY" :build/read-file
                      "R9-DEBUG" :debug/read-state
                      nil)
            :capability (case id
                          "R9-CAPABILITY" :build/read-file
                          "R9-DEBUG" :debug/read-state
                          nil)
            :artifact-id input-id
            :missing-policy (r9-document-missing-policy id)
            :source-generated-origin-chain
            [:ai-repl-ffi-capability-runtime :r9-document-coverage]
            :facts {:interactive-eval-uses-compiler-pipeline true
                    :session-state-is-artifact true
                    :hot-reload-invalidates-stale-artifacts true
                    :debugger-capability-checked true}
            :remediation [{:kind :declare-repl-runtime-manifest}
                          {:kind :emit-session-transcript}
                          {:kind :run-normal-compiler-checks}
                          {:kind :invalidate-stale-artifacts}]
            :redactions []
            :ordering-key [id :r9-document-coverage]})
         r9-document-diagnostic-ids
         (range))
   :status :complete})

(defn r9-document-requirements-coverage
  [ai-artifact]
  (let [manifest (:repl-runtime-manifest ai-artifact)
        transcript (:session-transcript ai-artifact)
        evaluated (:evaluated-form-artifact ai-artifact)
        syntax (:syntax-object-snapshot ai-artifact)
        macro (:macro-expansion-diff ai-artifact)
        typed (:typed-core-snapshot ai-artifact)
        mir (:mir-domain-ir-snapshot ai-artifact)
        runtime-decisions (:runtime-decision-log ai-artifact)
        repl-decisions (:repl-capability-decision-log ai-artifact)
        invalidation (:incremental-invalidation-record ai-artifact)
        hot-reload (:hot-reload-record ai-artifact)]
    {:artifact :gravity/r9-repl-runtime-requirements-coverage
     :ai-runtime-input (:artifact-id ai-artifact)
     :manifest-status (:status manifest)
     :family (:family manifest)
     :profile (:profile manifest)
     :target (:target manifest)
     :services (:services manifest)
     :session-status (:status transcript)
     :session-id (:session-id transcript)
     :evaluated-form-status (:status evaluated)
     :compiler-checks-passed? (:compiler-checks-passed? evaluated)
     :syntax-status (:status syntax)
     :source-spans-preserved? (:source-spans-preserved? syntax)
     :macro-status (:status macro)
     :hygiene-preserved? (:hygiene-preserved? macro)
     :typed-status (:status typed)
     :typed? (:typed? typed)
     :effects-checked? (:effects-checked? typed)
     :mir-status (:status mir)
     :runtime-decision-status (:status runtime-decisions)
     :missing-required-audit (:missing-required-audit runtime-decisions)
     :repl-capability-status (:status repl-decisions)
     :invalidation-status (:status invalidation)
     :invalidates (:invalidates invalidation)
     :stale-artifacts (:stale-artifacts invalidation)
     :hot-reload-status (:status hot-reload)
     :stale-analysis-kept? (:stale-analysis-kept? hot-reload)
     :status :complete}))

(defn r9-document-validate!
  [source-path artifact]
  (let [ai-artifact (:ai-repl-ffi-capability-artifact artifact)
        coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r9-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :complete (get-in ai-artifact
                                   [:capability-based-proof :status]))
      (r9-document-fail! "R9-MANIFEST" source-path ai-artifact
                         {:missing-fields [:repl-proof]}))
    (when-not (and (= :complete (:manifest-status coverage))
                   (= :interactive (:family coverage))
                   (= :meta (:profile coverage)))
      (r9-document-fail! "R9-PROFILE" source-path coverage
                         {:missing-fields [:profile-legality]}))
    (when-not (true? (:compiler-checks-passed? coverage))
      (r9-document-fail! "R9-CHECKS" source-path coverage
                         {:missing-fields [:compiler-checks]}))
    (when-not (= :complete (:repl-capability-status coverage))
      (r9-document-fail! "R9-CAPABILITY" source-path coverage
                         {:missing-fields [:capability-decisions]}))
    (when-not (= :complete (:session-status coverage))
      (r9-document-fail! "R9-SESSION" source-path coverage
                         {:missing-fields [:session-transcript]}))
    (when-not (and (= :complete (:invalidation-status coverage))
                   (contains? (set (:invalidates coverage)) :package))
      (r9-document-fail! "R9-HERMETICITY" source-path coverage
                         {:missing-fields [:build-affecting-state]}))
    (when (or (seq (:stale-artifacts coverage))
              (true? (:stale-analysis-kept? coverage)))
      (r9-document-fail! "R9-HOT-RELOAD" source-path coverage
                         {:missing-fields [:stale-artifacts]}))
    (when (seq (:missing-required-audit coverage))
      (r9-document-fail! "R9-DEBUG" source-path coverage
                         {:missing-fields [:debug-audit]}))
    (when-not (= :complete (:evaluated-form-status coverage))
      (r9-document-fail! "R9-AUDIT" source-path coverage
                         {:missing-fields [:evaluated-form-record]}))
    (when-not (= (set r9-document-diagnostic-ids) diagnostics)
      (r9-document-fail! "R9-MANIFEST" source-path
                         (:r9-diagnostic-stream artifact)
                         {:missing-fields [:r9-diagnostics]})))
  :complete)

(defn r9-document-capability-proof
  [artifact]
  (let [coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r9-diagnostic-stream
                                       :diagnostics])))]
    {:interactive-runtime-input-verified?
     (= :complete (get-in artifact
                          [:ai-repl-ffi-capability-artifact
                           :capability-based-proof :status]))
     :manifest-profile-and-target-covered?
     (and (= :complete (:manifest-status coverage))
          (= :interactive (:family coverage))
          (= :meta (:profile coverage))
          (= :jvm (:target coverage)))
     :normal-compiler-checks-covered?
     (and (true? (:compiler-checks-passed? coverage))
          (= :complete (:typed-status coverage))
          (true? (:typed? coverage))
          (true? (:effects-checked? coverage)))
     :session-and-audit-recorded?
     (and (= :complete (:session-status coverage))
          (= :complete (:evaluated-form-status coverage))
          (= :complete (:runtime-decision-status coverage))
          (empty? (:missing-required-audit coverage)))
     :inspection-snapshots-covered?
     (and (= :complete (:syntax-status coverage))
          (true? (:source-spans-preserved? coverage))
          (= :complete (:macro-status coverage))
          (true? (:hygiene-preserved? coverage))
          (= :complete (:mir-status coverage)))
     :capability-decisions-covered?
     (= :complete (:repl-capability-status coverage))
     :hermeticity-and_hot_reload_covered?
     (and (= :complete (:invalidation-status coverage))
          (contains? (set (:invalidates coverage)) :package)
          (= :complete (:hot-reload-status coverage))
          (empty? (:stale-artifacts coverage))
          (false? (:stale-analysis-kept? coverage)))
     :diagnostics-covered?
     (= (set r9-document-diagnostic-ids) diagnostics)
     :status :complete}))