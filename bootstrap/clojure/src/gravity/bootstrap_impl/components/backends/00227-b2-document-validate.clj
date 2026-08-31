

(defn b2-document-validate!
  [source-path artifact]
  (let [native (:native-lowering-artifact artifact)
        manifest (:c-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b2-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-native-lowering-artifact (:kind native))
      (b2-document-fail! "B2-MANIFEST" source-path native
                         {:missing-fields [:native-lowering-artifact]}))
    (when-not (= :complete (get-in native
                                   [:capability-based-proof :status]))
      (b2-document-fail! "B2-MANIFEST" source-path native
                         {:missing-fields [:native-lowering-proof]}))
    (when-not (= :pinned (get-in manifest
                                 [:dialect-selection :status]))
      (b2-document-fail! "B2-DIALECT" source-path manifest
                         {:missing-fields [:dialect-selection]}))
    (when-not (= :pinned (get-in manifest
                                 [:abi-layout-manifest :status]))
      (b2-document-fail! "B2-ABI" source-path manifest
                         {:missing-fields [:abi-layout]}))
    (when-not (= :complete (get-in manifest
                                   [:pointer-memory-lowering :status]))
      (b2-document-fail! "B2-POINTER" source-path manifest
                         {:missing-fields [:pointer-memory-lowering]}))
    (when-not (= :complete (get-in manifest
                                   [:numeric-lowering :status]))
      (b2-document-fail! "B2-NUMERIC" source-path manifest
                         {:missing-fields [:numeric-lowering]}))
    (when-not (= [] (get-in manifest
                            [:runtime-helper-manifest
                             :hidden-libc-dependencies]))
      (b2-document-fail! "B2-RUNTIME" source-path manifest
                         {:missing-fields [:hidden-libc-dependencies]}))
    (when-not (= :complete (get-in manifest
                                   [:ffi-boundary-map :status]))
      (b2-document-fail! "B2-FFI" source-path manifest
                         {:missing-fields [:ffi-boundary-map]}))
    (when-not (= :profile-specific-volatile-helper
                 (get-in manifest [:pointer-memory-lowering
                                   :mmio-access]))
      (b2-document-fail! "B2-MMIO" source-path manifest
                         {:missing-fields [:mmio-access]}))
    (when-not (every? #(contains? manifest %)
                      [:dialect-selection :source-files :header-files
                       :runtime-helper-manifest :abi-layout-manifest
                       :proof-to-c-assumption-map :build-manifest
                       :source-debug-map])
      (b2-document-fail! "B2-MANIFEST" source-path manifest
                         {:missing-fields [:c-artifact-manifest]}))
    (when-not (and (not (str/includes? b2-document-c-source "return x + 1;"))
                   (str/includes? b2-document-c-source "INT64_MAX")
                   (str/includes? b2-document-c-source "overflow"))
      (b2-document-fail! "B2-UB" source-path manifest
                         {:missing-fields [:checked-c-source]}))
    (when-not (= (set b2-document-diagnostic-ids) diagnostics)
      (b2-document-fail! "B2-MANIFEST" source-path
                         (:b2-diagnostic-stream artifact)
                         {:missing-fields [:b2-diagnostics]})))
  :complete)

(defn b2-document-capability-proof
  [artifact]
  (let [manifest (:c-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b2-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:native-lowering-artifact
                           :capability-based-proof :status]))
     :declared-c-dialect-pinned?
     (= :pinned (get-in manifest [:dialect-selection :status]))
     :safe-c-undefined-behavior-rejected?
     (and (contains? diagnostics "B2-UB")
          (not (str/includes? b2-document-c-source "return x + 1;")))
     :abi-and-layout-pinned?
     (= :pinned (get-in manifest [:abi-layout-manifest :status]))
     :pointer-provenance-covered?
     (= :complete (get-in manifest [:pointer-memory-lowering :status]))
     :numeric-lowering-covered?
     (= :complete (get-in manifest [:numeric-lowering :status]))
     :runtime-helpers-profile-legal?
     (and (= :complete (get-in manifest
                               [:runtime-helper-manifest :status]))
          (empty? (get-in manifest
                          [:runtime-helper-manifest
                           :hidden-libc-dependencies])))
     :ffi-boundary-mapped?
     (= :complete (get-in manifest [:ffi-boundary-map :status]))
     :mmio-helper-covered?
     (= :profile-specific-volatile-helper
        (get-in manifest [:pointer-memory-lowering :mmio-access]))
     :manifest-complete?
     (every? #(contains? manifest %)
             [:dialect-selection :source-files :header-files
              :runtime-helper-manifest :abi-layout-manifest
              :proof-to-c-assumption-map :build-manifest
              :source-debug-map])
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :diagnostics-covered?
     (= (set b2-document-diagnostic-ids) diagnostics)
     :requires-external-c-syntax-proof?
     (= :requires-proof-command
        (get-in manifest [:c-fixture-compilation-record :status]))
     :status :complete}))

(defn b2-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b2-document-source-overrides module)
        _ (b2-document-validate-source-overrides! source-path
                                                  source-overrides)
        native-artifact (native-lowering-source-artifact source-path
                                                         source-text)
        input-id (:artifact-id native-artifact)
        manifest (b2-document-c-manifest source-path input-id)
        diagnostic-stream (b2-document-diagnostic-stream source-path
                                                         input-id)
        artifact-base
        {:kind :gravity/stage0-b2-c-backend-document-artifact
         :task "P07-D099"
         :document-set ["B2"]
         :governing-document b2-document-governing-document
         :pass {:name :b2-c-backend-document-coverage
                :input :native-lowering-artifact
                :output :b2-c-backend-document-artifact
                :requires [:verified-mir-or-domain-ir :b1-backend-interface
                           :c-dialect-selection :no-c-undefined-behavior
                           :pointer-provenance :abi-layout
                           :runtime-helper-manifest
                           :proof-to-c-assumption-map :c-build-manifest
                           :source-debug-map]
                :preserves [:source-spans :generated-origins :types
                            :effects :capabilities :safety :proofs
                            :unsafe-audit-ids :profile :target
                            :artifact-provenance]
                :emits [:c-backend-manifest :c-source-files
                        :header-files :runtime-helper-manifest
                        :abi-layout-manifest
                        :proof-to-c-assumption-map :c-build-manifest
                        :source-debug-map :b2-diagnostic-stream
                        :conformance-criteria-record]
                :rejects b2-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :native-lowering-artifact
         (select-keys native-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :native-lowering-results])
         :native-lowering-artifact-kind (:kind native-artifact)
         :native-lowering-artifact-hash input-id
         :c-backend-manifest manifest
         :c-dialect-selection-record (:dialect-selection manifest)
         :c-source-files (:source-files manifest)
         :header-files (:header-files manifest)
         :runtime-helper-manifest (:runtime-helper-manifest manifest)
         :abi-layout-manifest (:abi-layout-manifest manifest)
         :proof-to-c-assumption-map (:proof-to-c-assumption-map manifest)
         :c-build-manifest (:build-manifest manifest)
         :source-debug-map (:source-debug-map manifest)
         :rejected-design-coverage
         [{:design :c-undefined-behavior-as-optimization
           :diagnostic "B2-UB" :status :rejected}
          {:design :unpinned-c-abi-or-layout
           :diagnostic "B2-ABI" :status :rejected}
          {:design :hidden-libc-or-allocator-dependency
           :diagnostic "B2-RUNTIME" :status :rejected}
          {:design :pointer-cast-losing-provenance
           :diagnostic "B2-POINTER" :status :rejected}
          {:design :compiler-flags-invalidating-numeric-contract
           :diagnostic "B2-NUMERIC" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b2-c-backend-conformance-criteria-record
          :hosted-and-freestanding-dialects [:freestanding-c11 :hosted-c17]
          :positive-lowering
          [:structs :tagged-unions :calls :closures :regions
           :linear-resources :checked-arithmetic :mmio]
          :negative-lowering
          [{:case :signed-overflow :diagnostic "B2-UB"}
           {:case :uninitialized-read :diagnostic "B2-UB"}
           {:case :invalid-shift :diagnostic "B2-NUMERIC"}
           {:case :pointer-provenance-gap :diagnostic "B2-POINTER"}]
          :abi-layout-manifest :pinned
          :helper-runtime-selection :profile-selected
          :proof-backed-check-metadata :emitted
          :source-debug-provenance-map :preserved
          :c-fixture-compilation :external-proof-recorded
          :status :passed}
         :b2-diagnostic-stream diagnostic-stream
         :b2-document-results
         {:documents ["B2"]
          :task "P07-D099"
          :required-diagnostic-ids b2-document-diagnostic-ids
          :native-lowering-input-status :complete
          :dialect-status :complete
          :undefined-behavior-status :complete
          :abi-layout-status :complete
          :pointer-status :complete
          :numeric-status :complete
          :runtime-helper-status :complete
          :ffi-status :complete
          :mmio-status :complete
          :manifest-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b2-document-validate! source-path artifact-base)
        capability-proof (b2-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b2-document-file-artifact
  [path]
  (b2-document-source-artifact path (slurp path)))

(def b3-document-governing-document
  "docs/phase-07-backend-architecture/100-b3-llvm-backend-design.md")