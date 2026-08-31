

(defn b3-document-llvm-manifest
  [input-id]
  (let [ir-hash (c4-artifact-id b3-document-llvm-ir)]
    {:artifact :gravity/llvm-backend-manifest
     :backend :gravity.backend/llvm
     :accepts #{:gravity/mir :gravity/domain-ir}
     :emits #{:llvm-ir :bitcode :object :static-library :shared-library}
     :requires #{:target-triple :data-layout :abi :runtime-providers
                 :proof-table :source-map :safety-bundle}
     :metadata-gated #{:nuw :nsw :exact :noalias :nonnull
                       :dereferenceable :align :range :inbounds
                       :fast-math :tbaa}
     :rejects #{:proofless-llvm-metadata :implicit-llvm-ub
                :unpinned-data-layout :unsupported-runtime-service}
     :target-record {:triple "x86_64-unknown-linux-gnu"
                     :cpu "generic"
                     :feature-string ""
                     :pointer-width 64
                     :address-spaces {:generic 0}
                     :endianness :little
                     :integer-layout {:i64 {:abi 64 :preferred 64}}
                     :floating-layout {:f64 {:abi 64 :preferred 64}}
                     :aggregate-alignment 64
                     :vector-legal-widths [128]
                     :relocation-model :static
                     :code-model :small
                     :calling-conventions [:ccc]
                     :unwind-strategy :trap
                     :thread-local-storage :not-requested
                     :sanitizer-mode :none
                     :instrumentation-mode :none
                     :object-format :elf
                     :data-layout "e-m:e-p:64:64-i64:64-n8:16:32:64-S128"
                     :status :pinned}
     :llvm-ir-files [{:path "gravity_stage0_b3.ll"
                      :content b3-document-llvm-ir
                      :hash ir-hash}]
     :proof-to-llvm-metadata-map
     [{:metadata :range
       :operation :checked-add-overflow-guard
       :proof :proof/c18-interval-check}
      {:metadata :nonnull
       :operation :managed-reference
       :proof :proof/c18-check-dominance}
      {:metadata :align
       :operation :region-pointer
       :proof :proof/c18-layout-alignment}]
     :metadata-policy {:forbidden-without-proof
                       [:nuw :nsw :exact :nonnull :dereferenceable
                        :align :range :noalias :inbounds :fast-math
                        :tbaa]
                       :emitted-in-fixture []
                       :status :gated}
     :pointer-ownership-memory-map
     {:preserved-facts [:object-identity :address-space :provenance
                       :allocation-provider :lifetime-interval
                       :valid-byte-range :alignment :initialized-state
                       :mutability :aliasing-mode :nullability]
      :gep-inbounds-policy :proof-only
      :raw-pointer-casts :ffi-unsafe-island-or-intrinsic-only
      :lifetime-intrinsics :diagnostic-safe-only
      :status :complete}
     :numeric-floating-lowering
     {:integer-modes {:checked :branch-trap-or-helper
                      :wrapping :no-overflow-assumptions
                      :saturating :intrinsic-or-helper}
      :bounded-range-metadata :interval-proof-only
      :narrowing :checked-unless-proven
      :floating-modes [:strict :reproducible :relaxed :approximate
                       :target-native]
      :fast-math-flags :certificate-only
      :status :complete}
     :atomic-volatile-concurrency-lowering
     {:records [:operation :address-space :memory-order
                :synchronization-scope :alignment :failure-ordering
                :target-feature-requirement :fallback-helper]
      :mmio-policy :volatile-and-ordered
      :data-race-evidence :safe8-required
      :status :complete}
     :runtime-abi-helper-map
     {:helpers [:allocation :deallocation :region :arena :panic :trap
                :bounds-check :numeric-check :resource-cleanup
                :atomics :synchronization :math-provider :ffi-adapter
                :stack-probe :debug-hook]
      :hidden-runtime-services []
      :profile-selection :manifest-required
      :status :complete}
     :pass-pipeline-record
     {:llvm-version :stage0-recorded
      :target-backend :x86_64
      :optimization-level :o0
      :mandatory-verification-passes [:verify-after-emission
                                      :verify-after-pipeline]
      :instrumentation-and-sanitizer-passes []
      :vectorization-passes []
      :target-specific-passes []
      :metadata-preservation [:source-map :safety :capability
                              :proof :unsafe-audit :conformance]
      :disabled-passes [{:pass :unsafe-fast-math
                         :reason :missing-explicit-floating-certificate}
                        {:pass :metadata-stripping
                         :reason :would-erase-required-evidence}]
      :post-pass-verifier-results :requires-proof-command
      :status :complete}
     :source-debug-map {:source input-id
                        :generated-origin-chain [:mir
                                                 :c14-target-lowering
                                                 :b1-interface
                                                 :b3-llvm-backend]
                        :generated-files ["gravity_stage0_b3.ll"]
                        :status :preserved}
     :safety-capability-unsafe-audit-preservation-map
     {:preserved [:source-spans :debug-locations :proofs :safety
                  :capabilities :unsafe-audit-ids :conformance]
      :stripped []
      :status :preserved}
     :unsupported-feature-report
     {:unsupported []
      :fallbacks [{:feature :proofless-noalias
                   :fallback :omit-metadata}
                  {:feature :invalid-inbounds
                   :fallback :plain-gep-or-reject}
                  {:feature :unsupported-atomic-ordering
                   :fallback :runtime-helper-or-reject}]
      :status :complete}
     :llvm-verifier-record
     {:declared-command "clang -target x86_64-unknown-linux-gnu -x ir -S -o /tmp/gravity-p07-b3.s /tmp/gravity-p07-b3.ll"
      :proof-artifact "docs/artifacts/phase-07/reports/p07-d100-b3-llvm-backend-report.md"
      :status :requires-proof-command}
     :input-artifact input-id
     :status :complete}))

(defn b3-document-validate!
  [source-path artifact]
  (let [native (:native-lowering-artifact artifact)
        manifest (:llvm-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b3-diagnostic-stream
                                       :diagnostics])))
        forbidden-ir-flags [" nsw" " nuw" " inbounds" " fast"
                            "!tbaa"]]
    (when-not (= :gravity/stage0-native-lowering-artifact (:kind native))
      (b3-document-fail! "B3-MANIFEST" source-path native
                         {:missing-fields [:native-lowering-artifact]}))
    (when-not (= :complete (get-in native
                                   [:capability-based-proof :status]))
      (b3-document-fail! "B3-MANIFEST" source-path native
                         {:missing-fields [:native-lowering-proof]}))
    (when-not (= :pinned (get-in manifest [:target-record :status]))
      (b3-document-fail! "B3-TARGET" source-path manifest
                         {:missing-fields [:target-record]}))
    (when-not (= :gated (get-in manifest [:metadata-policy :status]))
      (b3-document-fail! "B3-METADATA" source-path manifest
                         {:missing-fields [:metadata-policy]}))
    (when (some #(str/includes? b3-document-llvm-ir %) forbidden-ir-flags)
      (b3-document-fail! "B3-UB" source-path manifest
                         {:missing-fields [:conservative-llvm-ir]}))
    (when-not (= :complete (get-in manifest
                                   [:pointer-ownership-memory-map
                                    :status]))
      (b3-document-fail! "B3-POINTER" source-path manifest
                         {:missing-fields [:pointer-ownership-memory-map]}))
    (when-not (= :complete (get-in manifest
                                   [:numeric-floating-lowering :status]))
      (b3-document-fail! "B3-NUMERIC" source-path manifest
                         {:missing-fields [:numeric-floating-lowering]}))
    (when-not (= :complete
                 (get-in manifest
                         [:atomic-volatile-concurrency-lowering :status]))
      (b3-document-fail! "B3-ATOMIC" source-path manifest
                         {:missing-fields [:atomic-volatile-lowering]}))
    (when-not (empty? (get-in manifest
                              [:runtime-abi-helper-map
                               :hidden-runtime-services]))
      (b3-document-fail! "B3-RUNTIME" source-path manifest
                         {:missing-fields [:hidden-runtime-services]}))
    (when-not (contains? (set (get-in manifest
                                      [:target-record
                                       :calling-conventions]))
                         :ccc)
      (b3-document-fail! "B3-ABI" source-path manifest
                         {:missing-fields [:calling-convention]}))
    (when-not (= :complete (get-in manifest
                                   [:pass-pipeline-record :status]))
      (b3-document-fail! "B3-PASS" source-path manifest
                         {:missing-fields [:pass-pipeline-record]}))
    (when-not (every? #(contains? manifest %)
                      [:target-record :llvm-ir-files
                       :proof-to-llvm-metadata-map :runtime-abi-helper-map
                       :pass-pipeline-record :source-debug-map
                       :safety-capability-unsafe-audit-preservation-map
                       :unsupported-feature-report])
      (b3-document-fail! "B3-MANIFEST" source-path manifest
                         {:missing-fields [:llvm-artifact-manifest]}))
    (when-not (= (set b3-document-diagnostic-ids) diagnostics)
      (b3-document-fail! "B3-MANIFEST" source-path
                         (:b3-diagnostic-stream artifact)
                         {:missing-fields [:b3-diagnostics]})))
  :complete)