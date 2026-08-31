

(defn b4-document-validate!
  [source-path artifact]
  (let [hosted (:hosted-lowering-artifact artifact)
        manifest (:wasm-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b4-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-hosted-lowering-artifact (:kind hosted))
      (b4-document-fail! "B4-MANIFEST" source-path hosted
                         {:missing-fields [:hosted-lowering-artifact]}))
    (when-not (= :complete (get-in hosted
                                   [:capability-based-proof :status]))
      (b4-document-fail! "B4-MANIFEST" source-path hosted
                         {:missing-fields [:hosted-lowering-proof]}))
    (when-not (= :pinned (get-in manifest
                                 [:target-feature-record :status]))
      (b4-document-fail! "B4-TARGET" source-path manifest
                         {:missing-fields [:target-feature-record]}))
    (when-not (= :complete (get-in manifest
                                   [:component-contract-manifest :status]))
      (b4-document-fail! "B4-COMPONENT" source-path manifest
                         {:missing-fields [:component-contract]}))
    (when-not (= :complete (get-in manifest
                                   [:canonical-abi-manifest :status]))
      (b4-document-fail! "B4-CANONICAL-ABI" source-path manifest
                         {:missing-fields [:canonical-abi]}))
    (when-not (= :declared (get-in manifest
                                   [:import-capability-manifest :status]))
      (b4-document-fail! "B4-IMPORT" source-path manifest
                         {:missing-fields [:import-capabilities]}))
    (when-not (= :complete (get-in manifest
                                   [:export-schema-manifest :status]))
      (b4-document-fail! "B4-EXPORT" source-path manifest
                         {:missing-fields [:export-schemas]}))
    (when-not (= :complete (get-in manifest
                                   [:linear-memory-and-table-plan :status]))
      (b4-document-fail! "B4-MEMORY" source-path manifest
                         {:missing-fields [:linear-memory-plan]}))
    (when-not (= [:safe2-bounds :safe2-lifetime]
                 (get-in manifest
                         [:linear-memory-and-table-plan
                          :bounds-and-lifetime-proofs]))
      (b4-document-fail! "B4-BOUNDS" source-path manifest
                         {:missing-fields [:bounds-proof]}))
    (when-not (= :complete (get-in manifest
                                   [:replay-and-nondeterminism-record
                                    :status]))
      (b4-document-fail! "B4-NONDETERMINISM" source-path manifest
                         {:missing-fields [:replay-record]}))
    (when-not (= :complete (get-in manifest
                                   [:wasi-component-async-abi-manifest
                                    :status]))
      (b4-document-fail! "B4-WASI-ASYNC" source-path manifest
                         {:missing-fields [:wasi-async-abi]}))
    (when-not (= :complete (get-in manifest
                                   [:simd-feature-record :status]))
      (b4-document-fail! "B4-SIMD" source-path manifest
                         {:missing-fields [:simd-feature-record]}))
    (when-not (= :complete (get-in manifest
                                   [:atomic-feature-record :status]))
      (b4-document-fail! "B4-ATOMIC" source-path manifest
                         {:missing-fields [:atomic-feature-record]}))
    (when-not (= :complete (get-in manifest
                                   [:host-boundary-schema-manifest :status]))
      (b4-document-fail! "B4-HOST-SCHEMA" source-path manifest
                         {:missing-fields [:host-boundary-schema]}))
    (when-not (b4-document-wat-structurally-valid? b4-document-wat)
      (b4-document-fail! "B4-MANIFEST" source-path manifest
                         {:missing-fields [:wat-structural-validation]}))
    (when-not (every? #(contains? manifest %)
                      [:target-feature-record
                       :linear-memory-and-table-plan
                       :wasm-modules
                       :component-model-artifact
                       :component-contract-manifest
                       :canonical-abi-manifest
                       :import-capability-manifest
                       :export-schema-manifest
                       :host-boundary-schema-manifest
                       :wasi-component-async-abi-manifest
                       :component-composition-plan
                       :host-binding-stubs
                       :runtime-helper-manifest
                       :source-map-and-generated-origin-map
                       :replay-and-nondeterminism-record])
      (b4-document-fail! "B4-MANIFEST" source-path manifest
                         {:missing-fields [:wasm-artifact-manifest]}))
    (when-not (= (set b4-document-diagnostic-ids) diagnostics)
      (b4-document-fail! "B4-MANIFEST" source-path
                         (:b4-diagnostic-stream artifact)
                         {:missing-fields [:b4-diagnostics]})))
  :complete)

(defn b4-document-capability-proof
  [artifact]
  (let [manifest (:wasm-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b4-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:hosted-lowering-artifact
                           :capability-based-proof :status]))
     :target-feature-record-pinned?
     (= :pinned (get-in manifest [:target-feature-record :status]))
     :component-contract-covered?
     (= :complete (get-in manifest
                          [:component-contract-manifest :status]))
     :canonical-abi-covered?
     (= :complete (get-in manifest
                          [:canonical-abi-manifest :status]))
     :imports-capability-declared?
     (= :declared (get-in manifest
                          [:import-capability-manifest :status]))
     :exports-schema-declared?
     (= :complete (get-in manifest
                          [:export-schema-manifest :status]))
     :linear-memory-pointer-policy-covered?
     (= :complete (get-in manifest
                          [:linear-memory-and-table-plan :status]))
     :bounds-proof-covered?
     (= [:safe2-bounds :safe2-lifetime]
        (get-in manifest
                [:linear-memory-and-table-plan
                 :bounds-and-lifetime-proofs]))
     :replay-nondeterminism-covered?
     (= :complete (get-in manifest
                          [:replay-and-nondeterminism-record :status]))
     :async-and-wasi-async-covered?
     (= :complete (get-in manifest
                          [:wasi-component-async-abi-manifest :status]))
     :simd-feature-covered?
     (= :complete (get-in manifest [:simd-feature-record :status]))
     :atomic-feature-covered?
     (= :complete (get-in manifest [:atomic-feature-record :status]))
     :host-boundary-schema-covered?
     (= :complete (get-in manifest
                          [:host-boundary-schema-manifest :status]))
     :manifest-complete?
     (every? #(contains? manifest %)
             [:target-feature-record
              :linear-memory-and-table-plan
              :wasm-modules
              :component-model-artifact
              :component-contract-manifest
              :canonical-abi-manifest
              :import-capability-manifest
              :export-schema-manifest
              :host-boundary-schema-manifest
              :wasi-component-async-abi-manifest
              :component-composition-plan
              :host-binding-stubs
              :runtime-helper-manifest
              :source-map-and-generated-origin-map
              :replay-and-nondeterminism-record])
     :wat-structurally-valid?
     (b4-document-wat-structurally-valid? b4-document-wat)
     :diagnostics-covered?
     (= (set b4-document-diagnostic-ids) diagnostics)
     :external-wasm-toolchain-validation?
     :not-available-in-current-environment
     :status :complete}))