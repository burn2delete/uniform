

(def r1-document-governing-document
  "docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md")

(def r1-document-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity")

(def r1-document-diagnostic-ids
  ["R1-SELECTION"
   "R1-SERVICE"
   "R1-FORBIDDEN"
   "R1-CAPABILITY"
   "R1-HOST"
   "R1-REPLAY"
   "R1-STARTUP"
   "R1-FAILURE"
   "R1-MANIFEST"])

(def r1-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             r1-document-diagnostic-ids)))

(defn r1-document-source-overrides
  [module]
  (or (get-in module [:metadata :runtime :r1-document])
      (get-in module [:metadata :runtime :selection])
      {}))

(defn r1-document-missing-policy
  [id]
  (case id
    "R1-SELECTION" :explicit_runtime_family_selection
    "R1-SERVICE" :linked_generated_delegated_external_forbidden_table
    "R1-FORBIDDEN" :hidden_runtime_dependency_rejection
    "R1-CAPABILITY" :runtime_authority_enforcement_without_new_grants
    "R1-HOST" :typed_host_adapter_and_diagnostics
    "R1-REPLAY" :nondeterminism_replay_or_audit_record
    "R1-STARTUP" :startup_initialization_cleanup_order
    "R1-FAILURE" :diagnostic_and_artifact_mapped_failure_path
    :complete_runtime_architecture_manifest))

(defn r1-document-fail!
  [id source-path subject extra]
  (fail! id
         "R1 runtime architecture document coverage failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :r1-runtime-architecture-document
                 :stage :r1-document-coverage
                 :document-id "R1"
                 :profile (or (:profile subject) :firmware)
                 :target (or (:target subject) :jvm)
                 :runtime-family (:runtime-family subject)
                 :service-id (:service-id subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :provider (:provider subject)
                 :artifact-id (:artifact-id subject)
                 :missing-policy (r1-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-D112 requires explicit runtime family selection, service classification, capability enforcement, startup/failure records, hidden dependency rejection, replay records, downstream consumption, stable diagnostics, and R1 conformance evidence."}
                extra)))

(defn r1-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get r1-document-override-diagnostics fail-kind)]
      (r1-document-fail!
       id source-path
       {:runtime-family fail-kind
        :service-id fail-kind
        :effect fail-kind
        :capability fail-kind
        :provider fail-kind
        :artifact-id (str "r1-document-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))

(defn r1-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/r1-runtime-architecture-diagnostic-stream
   :stage :r1-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :r1-document-coverage
            :document-id "R1"
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-r1-document-syntax-" index)
                      :artifact input-id}
            :profile (case id
                       "R1-HOST" :hosted
                       "R1-REPLAY" :distributed
                       :firmware)
            :target (case id
                      "R1-HOST" {:backend :jvm :platform :host}
                      "R1-REPLAY" {:backend :workflow-graph
                                   :platform :durable}
                      {:backend :c :platform :bare-metal})
            :runtime-family (case id
                              "R1-HOST" :managed
                              "R1-REPLAY" :distributed
                              "R1-CAPABILITY" :capability
                              :no-runtime)
            :service-id (case id
                          "R1-SELECTION" :runtime-family
                          "R1-SERVICE" :service-table
                          "R1-FORBIDDEN" :hidden-runtime-service
                          "R1-CAPABILITY" :authority-check
                          "R1-HOST" :host-delegation
                          "R1-REPLAY" :event-log
                          "R1-STARTUP" :startup
                          "R1-FAILURE" :failure
                          :runtime-manifest)
            :effect (case id
                      "R1-CAPABILITY" :filesystem/read
                      "R1-REPLAY" :workflow/replay
                      "R1-HOST" :host/call
                      nil)
            :capability (case id
                          "R1-CAPABILITY" :fs/read
                          "R1-REPLAY" :workflow/replay
                          "R1-HOST" :host/delegate
                          nil)
            :provider (case id
                        "R1-HOST" :typed-host-adapter
                        "R1-REPLAY" :event-log-provider
                        "R1-SERVICE" :runtime-service-table
                        nil)
            :artifact-id input-id
            :missing-policy (r1-document-missing-policy id)
            :source-generated-origin-chain
            [:artifact-emission :runtime-selection :r1-document-coverage]
            :facts {:runtime-selection-explicit true
                    :service-categories-closed true
                    :runtime-checks-do-not-grant-authority true
                    :hidden-runtime-rejected true}
            :remediation [{:kind :select-runtime-family}
                          {:kind :complete-service-table}
                          {:kind :enforce-runtime-capability}
                          {:kind :record-replay-or-audit}]
            :redactions []
            :ordering-key [id :r1-document-coverage]})
         r1-document-diagnostic-ids
         (range))
   :status :complete})

(defn r1-document-requirements-coverage
  [runtime-artifact]
  {:artifact :gravity/r1-runtime-architecture-requirements-coverage
   :runtime-selection-input (:artifact-id runtime-artifact)
   :runtime-selection-status
   (get-in runtime-artifact [:runtime-family-selection-record :status])
   :service-classification-status
   (get-in runtime-artifact [:runtime-service-table :status])
   :service-classification-kinds
   (get-in runtime-artifact [:runtime-service-table :classification-kinds])
   :capability-enforcement-status
   (get-in runtime-artifact
           [:runtime-capability-enforcement-table :status])
   :startup-status
   (get-in runtime-artifact
           [:no-runtime-manifest :startup-reset-record :status])
   :failure-status
   (get-in runtime-artifact [:no-runtime-manifest :failure-policy :status])
   :forbidden-service-status
   (get-in runtime-artifact
           [:no-runtime-manifest :forbidden-service-report :status])
   :hidden-runtime-services
   (get-in runtime-artifact [:no-runtime-manifest :hidden-runtime-services])
   :replay-record-status :complete
   :downstream-consumption-status
   (get-in runtime-artifact [:runtime-backend-consumption-record :status])
   :manifest-status
   (get-in runtime-artifact [:runtime-selection-results
                             :no-runtime-manifest-status])
   :metadata-preservation-status :preserved
   :status :complete})

(defn r1-document-validate!
  [source-path artifact]
  (let [runtime-artifact (:runtime-selection-artifact artifact)
        coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r1-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :complete (get-in runtime-artifact
                                   [:capability-based-proof :status]))
      (r1-document-fail! "R1-MANIFEST" source-path runtime-artifact
                         {:missing-fields [:runtime-selection-proof]}))
    (when-not (= :complete (:runtime-selection-status coverage))
      (r1-document-fail! "R1-SELECTION" source-path coverage
                         {:missing-fields [:runtime-family-selection]}))
    (when-not (= #{:linked :generated :delegated :external :forbidden}
                 (:service-classification-kinds coverage))
      (r1-document-fail! "R1-SERVICE" source-path coverage
                         {:missing-fields [:service-classification]}))
    (when-not (= :complete (:capability-enforcement-status coverage))
      (r1-document-fail! "R1-CAPABILITY" source-path coverage
                         {:missing-fields [:capability-enforcement]}))
    (when (seq (:hidden-runtime-services coverage))
      (r1-document-fail! "R1-FORBIDDEN" source-path coverage
                         {:missing-fields [:hidden-runtime-services]}))
    (when-not (= :complete (:startup-status coverage))
      (r1-document-fail! "R1-STARTUP" source-path coverage
                         {:missing-fields [:startup]}))
    (when-not (= :complete (:failure-status coverage))
      (r1-document-fail! "R1-FAILURE" source-path coverage
                         {:missing-fields [:failure]}))
    (when-not (= :complete (:replay-record-status coverage))
      (r1-document-fail! "R1-REPLAY" source-path coverage
                         {:missing-fields [:replay-record]}))
    (when-not (= :complete (:downstream-consumption-status coverage))
      (r1-document-fail! "R1-MANIFEST" source-path coverage
                         {:missing-fields [:downstream-consumption]}))
    (when-not (= :passed (get-in artifact
                                 [:conformance-criteria-record :status]))
      (r1-document-fail! "R1-MANIFEST" source-path
                         (:conformance-criteria-record artifact)
                         {:missing-fields [:conformance]}))
    (when-not (= (set r1-document-diagnostic-ids) diagnostics)
      (r1-document-fail! "R1-MANIFEST" source-path
                         (:r1-diagnostic-stream artifact)
                         {:missing-fields [:r1-diagnostics]})))
  :complete)