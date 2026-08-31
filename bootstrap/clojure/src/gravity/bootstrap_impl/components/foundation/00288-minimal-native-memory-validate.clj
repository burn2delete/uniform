

(defn minimal-native-memory-validate!
  [source-path artifact]
  (let [runtime-selection (:runtime-selection-artifact artifact)
        native (:minimal-native-runtime-manifest artifact)
        memory (:memory-runtime-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:minimal-native-memory-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-runtime-selection-artifact
                 (:kind runtime-selection))
      (minimal-native-memory-fail! "R3-MANIFEST" source-path
                                   runtime-selection
                                   {:missing-fields [:runtime-selection]}))
    (when-not (= :complete (get-in runtime-selection
                                   [:capability-based-proof :status]))
      (minimal-native-memory-fail! "R3-MANIFEST" source-path
                                   runtime-selection
                                   {:missing-fields [:runtime-selection-proof]}))
    (when-not (every? (get-in native [:services :linked])
                      [:startup :panic :atomics :runtime-checks])
      (minimal-native-memory-fail! "R3-SERVICE" source-path native
                                   {:missing-fields [:linked-services]}))
    (when-not (= :complete (get-in native
                                   [:allocator-provider-record :status]))
      (minimal-native-memory-fail! "R3-ALLOCATOR" source-path native
                                   {:missing-fields [:allocator-provider]}))
    (when-not (= :complete (get-in native
                                   [:panic-failure-policy :status]))
      (minimal-native-memory-fail! "R3-PANIC" source-path native
                                   {:missing-fields [:panic-policy]}))
    (when-not (= :complete
                 (get-in native
                         [:atomic-synchronization-provider-record :status]))
      (minimal-native-memory-fail! "R3-ATOMICS" source-path native
                                   {:missing-fields [:atomics]}))
    (when-not (true? (get-in native
                             [:ffi-helper-manifest
                              :safe7-boundary-metadata-preserved?]))
      (minimal-native-memory-fail! "R3-FFI" source-path native
                                   {:missing-fields [:ffi-metadata]}))
    (when (seq (get-in native
                       [:debug-release-behavior-record
                        :debug :authority-effects]))
      (minimal-native-memory-fail! "R3-CAPABILITY" source-path native
                                   {:missing-fields [:debug-authority]}))
    (when (true? (get-in native
                         [:debug-release-behavior-record
                          :release :debug-only-services-linked?]))
      (minimal-native-memory-fail! "R3-DEBUG" source-path native
                                   {:missing-fields [:release-debug-services]}))
    (when-not (every? (get-in native
                              [:managed-service-rejection-record
                               :rejected])
                      [:gc :reflection :dynamic-eval
                       :managed-exceptions])
      (minimal-native-memory-fail! "R3-MANAGED" source-path native
                                   {:missing-fields [:managed-rejections]}))
    (when-not (= :complete (:status native))
      (minimal-native-memory-fail! "R3-MANIFEST" source-path native
                                   {:missing-fields [:native-manifest]}))
    (when-not (= :complete (get-in memory
                                   [:provider-selection-record :status]))
      (minimal-native-memory-fail! "R5-PROVIDER" source-path memory
                                   {:missing-fields [:provider-selection]}))
    (when-not (= :complete (get-in memory
                                   [:allocation-deallocation-contract
                                    :status]))
      (minimal-native-memory-fail! "R5-ALLOC" source-path memory
                                   {:missing-fields [:allocation-contract]}))
    (when-not (= :complete (get-in memory
                                   [:region-arena-manifest :status]))
      (minimal-native-memory-fail! "R5-LIFETIME" source-path memory
                                   {:missing-fields [:region-arena]}))
    (when-not (= :complete (get-in memory
                                   [:linear-resource-ledger :status]))
      (minimal-native-memory-fail! "R5-LINEAR" source-path memory
                                   {:missing-fields [:linear-ledger]}))
    (when-not (every? #(= :audited (:status %))
                      (:raw-memory-unsafe-audit-records memory))
      (minimal-native-memory-fail! "R5-RAW" source-path memory
                                   {:missing-fields [:raw-memory-audit]}))
    (when-not (= :complete (get-in memory
                                   [:device-memory-provider-manifest
                                    :status]))
      (minimal-native-memory-fail! "R5-DEVICE" source-path memory
                                   {:missing-fields [:device-memory]}))
    (when-not (= :complete (get-in memory
                                   [:ownership-borrow-runtime-check-map
                                    :status]))
      (minimal-native-memory-fail! "R5-BOUNDS" source-path memory
                                   {:missing-fields [:runtime-check-map]}))
    (when (seq (get-in memory
                       [:runtime-check-proof-agreement
                        :unproved-elisions]))
      (minimal-native-memory-fail! "R5-PROOF" source-path memory
                                   {:missing-fields [:unproved-elisions]}))
    (when-not (true? (get-in memory
                             [:debug-allocation-trace-schema
                              :source-map-preserved?]))
      (minimal-native-memory-fail! "R5-DEBUG" source-path memory
                                   {:missing-fields [:debug-source-map]}))
    (when-not (= :complete (:status memory))
      (minimal-native-memory-fail! "R5-MANIFEST" source-path memory
                                   {:missing-fields [:memory-manifest]}))
    (when-not (= (set minimal-native-memory-diagnostic-ids) diagnostics)
      (minimal-native-memory-fail! "R3-MANIFEST" source-path
                                   (:minimal-native-memory-diagnostic-stream
                                    artifact)
                                   {:missing-fields [:diagnostics]})))
  :complete)

(defn minimal-native-memory-capability-proof
  [artifact]
  (let [native (:minimal-native-runtime-manifest artifact)
        memory (:memory-runtime-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:minimal-native-memory-diagnostic-stream
                                       :diagnostics])))]
    {:runtime-selection-input-verified?
     (= :complete (get-in artifact
                          [:runtime-selection-artifact
                           :capability-based-proof :status]))
     :minimal-native-services-declared?
     (every? (get-in native [:services :linked])
             [:startup :panic :atomics :runtime-checks])
     :allocator-provider-policy-complete?
     (= :complete (get-in native [:allocator-provider-record :status]))
     :panic-failure-policy-explicit?
     (= :complete (get-in native [:panic-failure-policy :status]))
     :atomics-safe8-preserved?
     (= :complete (get-in native
                          [:atomic-synchronization-provider-record
                           :status]))
     :ffi-safe7-metadata-preserved?
     (true? (get-in native
                    [:ffi-helper-manifest
                     :safe7-boundary-metadata-preserved?]))
     :debug-release-separated?
     (false? (get-in native
                     [:debug-release-behavior-record
                      :release :debug-only-services-linked?]))
     :hidden-managed-services-rejected?
     (every? (get-in native
                     [:managed-service-rejection-record :rejected])
             [:gc :reflection :dynamic-eval :managed-exceptions])
     :memory-providers-declared?
     (every? (set (keys (:provider-families memory)))
             [:no-allocation :stack :ownership :region :arena :gc
              :reference-counting :raw :foreign :pinned :device])
     :memory_runtime_preserves_compiler_checks?
     (true? (get-in memory
                    [:ownership-borrow-runtime-check-map
                     :compiler-facts-preserved?]))
     :linear-resource-ledger-complete?
     (= :complete (get-in memory [:linear-resource-ledger :status]))
     :raw-memory-audited-only?
     (= :audited-only (get-in memory
                              [:provider-families :raw :status]))
     :device-memory-recorded?
     (= :complete (get-in memory
                          [:device-memory-provider-manifest :status]))
     :proof-elision-agrees-with-safe15?
     (empty? (get-in memory
                     [:runtime-check-proof-agreement
                      :unproved-elisions]))
     :diagnostics-covered?
     (= (set minimal-native-memory-diagnostic-ids) diagnostics)
     :status :complete}))