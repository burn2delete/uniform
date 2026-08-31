

(defn r3-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/r3-minimal-native-diagnostic-stream
   :stage :r3-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :r3-document-coverage
            :document-id "R3"
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-r3-document-syntax-" index)
                      :artifact input-id}
            :profile :native
            :target {:backend :llvm :platform :linux}
            :runtime-family :minimal-native
            :service-id (case id
                          "R3-SERVICE" :startup
                          "R3-ALLOCATOR" :allocator/provider
                          "R3-PANIC" :panic
                          "R3-ATOMICS" :atomics
                          "R3-FFI" :ffi-trampoline
                          "R3-CAPABILITY" :debug-stack
                          "R3-DEBUG" :debug-stack-trace
                          "R3-MANAGED" :gc
                          :minimal-native-runtime)
            :provider (case id
                        "R3-ALLOCATOR" :allocator/region-arena
                        "R3-ATOMICS" :native-mutex
                        nil)
            :effect (case id
                      "R3-ALLOCATOR" :memory/allocate
                      "R3-FFI" :ffi/call
                      "R3-CAPABILITY" :filesystem/read
                      nil)
            :capability (case id
                          "R3-ALLOCATOR" :memory/arena
                          "R3-FFI" :ffi/c
                          "R3-CAPABILITY" :fs/read
                          nil)
            :helper (case id
                      "R3-PANIC" :gravity_abort
                      "R3-DEBUG" :debug-stack
                      "R3-FFI" :call-trampoline
                      nil)
            :artifact-id input-id
            :missing-policy (r3-document-missing-policy id)
            :source-generated-origin-chain
            [:runtime-selection :minimal-native-runtime :r3-document-coverage]
            :facts {:linked-services-declared true
                    :runtime-helpers-do-not-grant-authority true
                    :ffi-safe7-preserved true
                    :managed-services-rejected true}
            :remediation [{:kind :declare-native-service}
                          {:kind :attach-allocator-policy}
                          {:kind :preserve-ffi-boundary-metadata}
                          {:kind :separate-debug-release}]
            :redactions []
            :ordering-key [id :r3-document-coverage]})
         r3-document-diagnostic-ids
         (range))
   :status :complete})

(defn r3-document-requirements-coverage
  [minimal-artifact]
  (let [manifest (:minimal-native-runtime-manifest minimal-artifact)]
    {:artifact :gravity/r3-minimal-native-requirements-coverage
     :minimal-native-input (:artifact-id minimal-artifact)
     :manifest-status (:status manifest)
     :family (:family manifest)
     :profile (:profile manifest)
     :target (:target manifest)
     :linked-services (get-in manifest [:services :linked])
     :forbidden-services (get-in manifest [:services :forbidden])
     :linked-support-object-statuses
     (set (map :status (:linked-support-objects manifest)))
     :startup-status (get-in manifest [:startup-record :status])
     :allocator-status
     (get-in manifest [:allocator-provider-record :status])
     :panic-status (get-in manifest [:panic-failure-policy :status])
     :atomics-status
     (get-in manifest [:atomic-synchronization-provider-record :status])
     :safe8-preservation
     (get-in manifest
             [:atomic-synchronization-provider-record :safe8-preservation])
     :ffi-status (get-in manifest [:ffi-helper-manifest :status])
     :safe7-preserved?
     (get-in manifest [:ffi-helper-manifest
                       :safe7-boundary-metadata-preserved?])
     :runtime-check-status
     (get-in manifest [:runtime-check-helper-manifest :status])
     :debug-release-status
     (get-in manifest [:debug-release-behavior-record :status])
     :debug-only-services-linked?
     (get-in manifest [:debug-release-behavior-record :release
                       :debug-only-services-linked?])
     :capability-status
     (get-in manifest [:capability-enforcement-table :status])
     :helpers-grant-authority?
     (get-in manifest [:capability-enforcement-table
                       :runtime-helpers-grant-authority?])
     :managed-rejection-status
     (get-in manifest [:managed-service-rejection-record :status])
     :status :complete}))

(defn r3-document-validate!
  [source-path artifact]
  (let [minimal-artifact (:minimal-native-memory-artifact artifact)
        coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r3-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :complete (get-in minimal-artifact
                                   [:capability-based-proof :status]))
      (r3-document-fail! "R3-MANIFEST" source-path minimal-artifact
                         {:missing-fields [:minimal-native-proof]}))
    (when-not (= :minimal-native (:family coverage))
      (r3-document-fail! "R3-MANIFEST" source-path coverage
                         {:missing-fields [:minimal-native-family]}))
    (when-not (every? (:linked-services coverage)
                      [:startup :panic :runtime-checks :atomics])
      (r3-document-fail! "R3-SERVICE" source-path coverage
                         {:missing-fields [:linked-services]}))
    (when-not (= :complete (:startup-status coverage))
      (r3-document-fail! "R3-SERVICE" source-path coverage
                         {:missing-fields [:startup]}))
    (when-not (= :complete (:allocator-status coverage))
      (r3-document-fail! "R3-ALLOCATOR" source-path coverage
                         {:missing-fields [:allocator]}))
    (when-not (= :complete (:panic-status coverage))
      (r3-document-fail! "R3-PANIC" source-path coverage
                         {:missing-fields [:panic]}))
    (when-not (and (= :complete (:atomics-status coverage))
                   (= :complete (:safe8-preservation coverage)))
      (r3-document-fail! "R3-ATOMICS" source-path coverage
                         {:missing-fields [:atomics-safe8]}))
    (when-not (and (= :complete (:ffi-status coverage))
                   (true? (:safe7-preserved? coverage)))
      (r3-document-fail! "R3-FFI" source-path coverage
                         {:missing-fields [:ffi-safe7]}))
    (when (true? (:helpers-grant-authority? coverage))
      (r3-document-fail! "R3-CAPABILITY" source-path coverage
                         {:missing-fields [:helper-capability]}))
    (when (true? (:debug-only-services-linked? coverage))
      (r3-document-fail! "R3-DEBUG" source-path coverage
                         {:missing-fields [:release-debug-services]}))
    (when-not (= :complete (:managed-rejection-status coverage))
      (r3-document-fail! "R3-MANAGED" source-path coverage
                         {:missing-fields [:managed-rejection]}))
    (when-not (= :passed (get-in artifact
                                 [:conformance-criteria-record :status]))
      (r3-document-fail! "R3-MANIFEST" source-path
                         (:conformance-criteria-record artifact)
                         {:missing-fields [:conformance]}))
    (when-not (= (set r3-document-diagnostic-ids) diagnostics)
      (r3-document-fail! "R3-MANIFEST" source-path
                         (:r3-diagnostic-stream artifact)
                         {:missing-fields [:r3-diagnostics]})))
  :complete)

(defn r3-document-capability-proof
  [artifact]
  (let [coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r3-diagnostic-stream
                                       :diagnostics])))]
    {:minimal-native-input-verified?
     (= :complete (get-in artifact
                          [:minimal-native-memory-artifact
                           :capability-based-proof :status]))
     :linked-services-covered?
     (every? (:linked-services coverage)
             [:startup :panic :runtime-checks :atomics])
     :allocator-policy-covered?
     (= :complete (:allocator-status coverage))
     :panic-policy-covered?
     (= :complete (:panic-status coverage))
     :atomics-safe8-covered?
     (= :complete (:safe8-preservation coverage))
     :ffi-safe7-covered?
     (true? (:safe7-preserved? coverage))
     :helper-capability-covered?
     (false? (:helpers-grant-authority? coverage))
     :debug-release-covered?
     (false? (:debug-only-services-linked? coverage))
     :managed-services-rejected?
     (= :complete (:managed-rejection-status coverage))
     :diagnostics-covered?
     (= (set r3-document-diagnostic-ids) diagnostics)
     :status :complete}))