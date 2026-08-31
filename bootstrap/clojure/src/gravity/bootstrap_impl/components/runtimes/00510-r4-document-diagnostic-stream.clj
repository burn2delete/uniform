

(defn r4-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/r4-managed-runtime-diagnostic-stream
   :stage :r4-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :r4-document-coverage
            :document-id "R4"
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-r4-document-syntax-" index)
                      :artifact input-id}
            :profile (if (= "R4-PROFILE" id) :firmware :hosted)
            :target (case id
                      "R4-NULL" :javascript
                      "R4-EXCEPTION" :wasm-host
                      :jvm)
            :runtime-family :managed
            :host-runtime (case id
                            "R4-NULL" :javascript
                            "R4-EXCEPTION" :wasm-host
                            "R4-COLLECTION" :host-collection
                            "R4-RESOURCE" :managed-gc
                            :jvm)
            :host-symbol (case id
                           "R4-REFLECTION" 'java.lang.Class/forName
                           "R4-NULL" 'undefined
                           "R4-EXCEPTION" 'Promise.reject
                           nil)
            :host-package (case id
                            "R4-HOST" "undeclared.host"
                            "R4-COLLECTION" "host.collections"
                            nil)
            :gravity-type (case id
                            "R4-NULL" 'Option
                            "R4-EXCEPTION" 'Result
                            "R4-COLLECTION" 'PersistentVector
                            "R4-RESOURCE" 'LinearResource
                            'ManagedHostValue)
            :effect (case id
                      "R4-EXCEPTION" :host/error
                      "R4-REFLECTION" :host/reflect
                      "R4-RESOURCE" :resource/close
                      nil)
            :capability (case id
                          "R4-REFLECTION" :host/reflection
                          "R4-RESOURCE" :resource/cleanup
                          nil)
            :adapter (case id
                       "R4-NULL" :null-option-adapter
                       "R4-EXCEPTION" :exception-result-adapter
                       "R4-REFLECTION" :reflection-capability-adapter
                       "R4-COLLECTION" :persistent-collection-adapter
                       "R4-RESOURCE" :linear-resource-cleanup-adapter
                       :managed-host-adapter)
            :missing-policy (r4-document-missing-policy id)
            :source-generated-origin-chain
            [:minimal-native-runtime :managed-runtime :r4-document-coverage]
            :facts {:host-delegation-typed true
                    :nulls-and-exceptions-checked true
                    :reflection-capability-gated true
                    :gc-not-linear-cleanup-policy true}
            :remediation [{:kind :declare-host-runtime}
                          {:kind :translate-null-exception}
                          {:kind :gate-reflection}
                          {:kind :preserve-host-source-map}]
            :redactions []
            :ordering-key [id :r4-document-coverage]})
         r4-document-diagnostic-ids
         (range))
   :status :complete})

(defn r4-document-requirements-coverage
  [managed-artifact]
  (let [manifest (:managed-runtime-manifest managed-artifact)]
    {:artifact :gravity/r4-managed-runtime-requirements-coverage
     :managed-input (:artifact-id managed-artifact)
     :manifest-status (:status manifest)
     :family (:family manifest)
     :profile (:profile manifest)
     :target (:target manifest)
     :host-supported (get-in manifest [:host :supported])
     :host-target-statuses
     (set (map :status (:host-runtime-target-records managed-artifact)))
     :collection-status
     (get-in managed-artifact [:collection-implementation-manifest :status])
     :collection-divergences
     (get-in managed-artifact
             [:collection-implementation-manifest :divergences])
     :dynamic-state-status
     (get-in managed-artifact
             [:dynamic-variable-and-namespace-runtime-record :status])
     :implicit-capabilities?
     (get-in managed-artifact
             [:dynamic-variable-and-namespace-runtime-record
              :implicit-capabilities?])
     :translation-status
     (get-in managed-artifact [:exception-null-translation-map :status])
     :unchecked-null-or-exception?
     (get-in managed-artifact
             [:exception-null-translation-map
              :unchecked-null-or-exception?])
     :reflection-status
     (get-in managed-artifact
             [:reflection-and-dynamic-use-policy :status])
     :ambient-reflection?
     (get-in managed-artifact
             [:reflection-and-dynamic-use-policy
              :ambient-use-allowed?])
     :interop-status
     (get-in managed-artifact [:host-interop-adapter-manifest :status])
     :all-adapters-typed?
     (get-in managed-artifact
             [:host-interop-adapter-manifest :all-adapters-typed?])
     :cleanup-status
     (get-in managed-artifact [:resource-cleanup-manifest :status])
     :linear-cleanup?
     (get-in managed-artifact
             [:resource-cleanup-manifest
              :linear-resources-deterministic?])
     :gc-finalization-cleanup-policy?
     (get-in managed-artifact
             [:resource-cleanup-manifest
              :gc-finalization-cleanup-policy?])
     :source-map-status
     (get-in managed-artifact [:managed-source-debug-map :status])
     :host-failures-map-to-gravity?
     (get-in managed-artifact
             [:managed-source-debug-map
              :host-failures-map-to-gravity?])
     :generated-origin-chain-preserved?
     (get-in managed-artifact
             [:managed-source-debug-map
              :generated-origin-chain-preserved?])
     :profile-boundary-status
     (if (contains? (get-in manifest [:services :forbidden])
                    :hosted-leakage)
       :complete
       :missing)
     :status :complete}))

(defn r4-document-validate!
  [source-path artifact]
  (let [managed-artifact (:managed-runtime-artifact artifact)
        coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r4-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :complete (get-in managed-artifact
                                   [:capability-based-proof :status]))
      (r4-document-fail! "R4-MANIFEST" source-path managed-artifact
                         {:missing-fields [:managed-runtime-proof]}))
    (when-not (and (= :complete (:manifest-status coverage))
                   (= :managed (:family coverage)))
      (r4-document-fail! "R4-MANIFEST" source-path coverage
                         {:missing-fields [:managed-manifest]}))
    (when-not (every? (:host-supported coverage)
                      [:jvm :javascript :wasm-host])
      (r4-document-fail! "R4-HOST" source-path coverage
                         {:missing-fields [:host-targets]}))
    (when (true? (:unchecked-null-or-exception? coverage))
      (r4-document-fail! "R4-NULL" source-path coverage
                         {:missing-fields [:null-exception-translation]}))
    (when (true? (:ambient-reflection? coverage))
      (r4-document-fail! "R4-REFLECTION" source-path coverage
                         {:missing-fields [:reflection-policy]}))
    (when (seq (:collection-divergences coverage))
      (r4-document-fail! "R4-COLLECTION" source-path coverage
                         {:missing-fields [:collection-semantics]}))
    (when-not (true? (:linear-cleanup? coverage))
      (r4-document-fail! "R4-RESOURCE" source-path coverage
                         {:missing-fields [:linear-cleanup]}))
    (when (true? (:gc-finalization-cleanup-policy? coverage))
      (r4-document-fail! "R4-RESOURCE" source-path coverage
                         {:missing-fields [:gc-only-cleanup]}))
    (when-not (and (true? (:host-failures-map-to-gravity? coverage))
                   (true? (:generated-origin-chain-preserved? coverage)))
      (r4-document-fail! "R4-SOURCEMAP" source-path coverage
                         {:missing-fields [:source-map]}))
    (when-not (= :complete (:profile-boundary-status coverage))
      (r4-document-fail! "R4-PROFILE" source-path coverage
                         {:missing-fields [:profile-boundary]}))
    (when-not (= :passed (get-in artifact
                                 [:conformance-criteria-record :status]))
      (r4-document-fail! "R4-MANIFEST" source-path
                         (:conformance-criteria-record artifact)
                         {:missing-fields [:conformance]}))
    (when-not (= (set r4-document-diagnostic-ids) diagnostics)
      (r4-document-fail! "R4-MANIFEST" source-path
                         (:r4-diagnostic-stream artifact)
                         {:missing-fields [:r4-diagnostics]})))
  :complete)

(defn r4-document-capability-proof
  [artifact]
  (let [coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r4-diagnostic-stream
                                       :diagnostics])))]
    {:managed-runtime-input-verified?
     (= :complete (get-in artifact
                          [:managed-runtime-artifact
                           :capability-based-proof :status]))
     :host-targets-covered?
     (every? (:host-supported coverage) [:jvm :javascript :wasm-host])
     :null-exception-translation-covered?
     (false? (:unchecked-null-or-exception? coverage))
     :reflection-policy-covered?
     (false? (:ambient-reflection? coverage))
     :collection-semantics-covered?
     (empty? (:collection-divergences coverage))
     :linear-cleanup-covered?
     (and (true? (:linear-cleanup? coverage))
          (false? (:gc-finalization-cleanup-policy? coverage)))
     :source-map-covered?
     (and (true? (:host-failures-map-to-gravity? coverage))
          (true? (:generated-origin-chain-preserved? coverage)))
     :profile-boundary-covered?
     (= :complete (:profile-boundary-status coverage))
     :diagnostics-covered?
     (= (set r4-document-diagnostic-ids) diagnostics)
     :status :complete}))