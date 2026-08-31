

(def stage0-compiled-managed-runtime-required-fields
  [:family :host-runtime :host-version :module-system :package-system
   :services :interop :collection-semantics :dynamic-state
   :exception-policy :nullability-policy :reflection-policy
   :interop-policy :source-debug-map :capability-enforcement
   :observability])

(defn stage0-compiled-runtime-suite
  [module]
  (get-in module [:metadata :runtime :compiled-gate] {}))

(defn stage0-compiled-runtime-suite-present?
  [module]
  (contains? (get-in module [:metadata :runtime] {}) :compiled-gate))

(defn validate-stage0-compiled-runtime-selection!
  [module selection]
  (let [source-path (:source-path module)
        required-missing (cond-> []
                           (not= :managed (:family selection))
                           (conj :family)
                           (not= :hosted (:profile selection))
                           (conj :profile)
                           (not= :jvm (:target selection))
                           (conj :target)
                           (not= :explicit (:selection-mode selection))
                           (conj :selection-mode))
        hidden-services (set (:hidden-services selection))
        used-forbidden-services (set/intersection
                                  (set (:used-services selection))
                                  (set (:forbidden selection)))]
    (when (seq required-missing)
      (runtime-selection-fail!
       "R1-SELECTION" source-path
       {:runtime-family (:family selection)
        :profile (:profile selection)
        :target (:target selection)
        :service-id :runtime-family-selection
        :artifact-id (:artifact-id selection)}
       {:missing-fields required-missing
        :remediation
        "Compiled hosted core runtime metadata must explicitly select the managed JVM runtime family for the :hosted profile before execution."}))
    (when (or (seq hidden-services)
              (seq used-forbidden-services))
      (runtime-selection-fail!
       "R1-FORBIDDEN" source-path
       {:runtime-family (:family selection)
        :profile (:profile selection)
        :target (:target selection)
        :service-id (or (first hidden-services)
                        (first used-forbidden-services)
                        :runtime-service)
        :artifact-id (:artifact-id selection)}
       {:hidden-services (vec (sort-by name hidden-services))
        :forbidden-services (vec (sort-by name used-forbidden-services))
        :missing-fields [:runtime-service-classification]
        :remediation
        "Compiled runtime services must be classified as linked, generated, delegated, external, or forbidden; hidden service dependencies are rejected."}))))

(defn validate-stage0-compiled-managed-runtime!
  [module managed-runtime]
  (let [source-path (:source-path module)
        missing-fields (compiler-pass-missing-fields
                        managed-runtime
                        stage0-compiled-managed-runtime-required-fields)]
    (when (seq missing-fields)
      (managed-runtime-fail!
       "R4-MANIFEST" source-path
       {:host-runtime (:host-runtime managed-runtime)
        :host-symbol :stage0-compiled-runtime
        :profile (:profile module)
        :target (:target module)}
       {:missing-fields missing-fields
        :remediation
        "Compiled hosted core runtime metadata must expose the managed host runtime manifest before execution."}))
    (doseq [flow (:null-flow managed-runtime)]
      (when (and (not (true? (:checked? flow)))
                 (not (contains? #{:option :result :opaque-foreign
                                   :opaque-checked}
                                 (:wrapper flow))))
        (managed-runtime-fail!
         "R4-NULL" source-path
         {:host-runtime (:host-runtime managed-runtime)
          :host-symbol (:host-symbol flow)
          :profile (:profile module)
          :target (:target module)}
         {:missing-fields [:checked-nullability-wrapper]
          :remediation
          "Managed host nulls must be checked or wrapped before they can enter safe Gravity runtime execution."})))))

(defn validate-stage0-compiled-runtime-capability!
  [module decision]
  (when-not (and (= :grant (:decision decision))
                 (:capability decision)
                 (:effect decision)
                 (:principal decision)
                 (:provider decision))
    (ai-repl-ffi-capability-fail!
     "R11-GRANT" (:source-path module)
     {:action-id (:action-id decision)
      :principal (:principal decision)
      :effect (:effect decision)
      :capability (:capability decision)
      :provider (:provider decision)
      :decision (:decision decision)
      :profile (:profile module)
      :target (:target module)}
     {:missing-fields [:matching-runtime-capability-grant]
      :remediation
      "A compiled runtime action may execute only when the effect, principal, provider, and capability decision produce an explicit grant."})))

(defn validate-stage0-compiled-runtime-observability!
  [module sink]
  (when (and (:sink sink)
             (not (true? (:sink-capability-granted? sink))))
    (runtime-observability-fail!
     "R12-SINK" (:source-path module)
     {:event-id (:event-id sink)
      :artifact-id (:artifact-id sink)
      :sink (:sink sink)
      :redaction-policy (:redaction-policy sink)
      :capability (:capability sink)
      :profile (:profile module)
      :target (:target module)}
     {:missing-fields [:observability-sink-capability]
      :remediation
      "Runtime observability sinks must be capability checked and redacted before compiled execution can emit events."})))

(defn validate-stage0-compiled-runtime!
  [module]
  (when (stage0-compiled-runtime-suite-present? module)
    (let [suite (stage0-compiled-runtime-suite module)]
      (doseq [selection (:selections suite)]
        (validate-stage0-compiled-runtime-selection! module selection))
      (doseq [managed-runtime (:managed-runtimes suite)]
        (validate-stage0-compiled-managed-runtime! module managed-runtime))
      (doseq [decision (:capability-decisions suite)]
        (validate-stage0-compiled-runtime-capability! module decision))
      (doseq [sink (:observability-sinks suite)]
        (validate-stage0-compiled-runtime-observability! module sink)))))

(def stage0-compiled-domain-slice-required-fields
  [:document-id :domain :slice-id :profile :target :effects :capabilities
   :artifacts :accepted-fixtures :rejected-fixtures :replacement-scope
   :conformance])

(defn stage0-compiled-domain-suite
  [module]
  (get-in module [:metadata :domain :compiled-gate] {}))

(defn stage0-compiled-domain-suite-present?
  [module]
  (contains? (get-in module [:metadata :domain] {}) :compiled-gate))