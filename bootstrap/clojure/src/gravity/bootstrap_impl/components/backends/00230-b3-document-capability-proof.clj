

(defn b3-document-capability-proof
  [artifact]
  (let [manifest (:llvm-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b3-diagnostic-stream
                                       :diagnostics])))
        forbidden-ir-flags [" nsw" " nuw" " inbounds" " fast"
                            "!tbaa"]]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:native-lowering-artifact
                           :capability-based-proof :status]))
     :target-and-data-layout-pinned?
     (= :pinned (get-in manifest [:target-record :status]))
     :proof-gated-metadata-covered?
     (= :gated (get-in manifest [:metadata-policy :status]))
     :safe-llvm-undefined-behavior-rejected?
     (and (contains? diagnostics "B3-UB")
          (not-any? #(str/includes? b3-document-llvm-ir %)
                    forbidden-ir-flags))
     :pointer-ownership-memory-covered?
     (= :complete (get-in manifest
                          [:pointer-ownership-memory-map :status]))
     :numeric-floating-lowering-covered?
     (= :complete (get-in manifest
                          [:numeric-floating-lowering :status]))
     :atomic-volatile-ordering-covered?
     (= :complete
        (get-in manifest
                [:atomic-volatile-concurrency-lowering :status]))
     :runtime-helpers-profile-legal?
     (and (= :complete (get-in manifest
                               [:runtime-abi-helper-map :status]))
          (empty? (get-in manifest
                          [:runtime-abi-helper-map
                           :hidden-runtime-services])))
     :abi-record-covered?
     (contains? (set (get-in manifest
                             [:target-record :calling-conventions]))
                :ccc)
     :pass-pipeline-verification-covered?
     (= :complete (get-in manifest
                          [:pass-pipeline-record :status]))
     :source-debug-proof-safety-capability-preserved?
     (= :preserved
        (get-in manifest
                [:safety-capability-unsafe-audit-preservation-map
                 :status]))
     :manifest-complete?
     (every? #(contains? manifest %)
             [:target-record :llvm-ir-files
              :proof-to-llvm-metadata-map :runtime-abi-helper-map
              :pass-pipeline-record :source-debug-map
              :safety-capability-unsafe-audit-preservation-map
              :unsupported-feature-report])
     :diagnostics-covered?
     (= (set b3-document-diagnostic-ids) diagnostics)
     :requires-external-llvm-verifier-proof?
     (= :requires-proof-command
        (get-in manifest [:llvm-verifier-record :status]))
     :status :complete}))

(defn b3-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b3-document-source-overrides module)
        _ (b3-document-validate-source-overrides! source-path
                                                  source-overrides)
        native-artifact (native-lowering-source-artifact source-path
                                                         source-text)
        input-id (:artifact-id native-artifact)
        manifest (b3-document-llvm-manifest input-id)
        diagnostic-stream (b3-document-diagnostic-stream source-path
                                                         input-id)
        artifact-base
        {:kind :gravity/stage0-b3-llvm-backend-document-artifact
         :task "P07-D100"
         :document-set ["B3"]
         :governing-document b3-document-governing-document
         :pass {:name :b3-llvm-backend-document-coverage
                :input :native-lowering-artifact
                :output :b3-llvm-backend-document-artifact
                :requires [:verified-mir-or-domain-ir :b1-backend-interface
                           :c14-target-lowering :target-triple
                           :data-layout :abi :runtime-providers
                           :proof-table :source-map :safety-bundle
                           :llvm-pass-pipeline]
                :preserves [:source-spans :generated-origins :types
                            :effects :capabilities :safety :proofs
                            :unsafe-audit-ids :profile :target
                            :artifact-provenance]
                :emits [:llvm-backend-manifest :target-record
                        :llvm-ir-files :proof-to-llvm-metadata-map
                        :runtime-abi-helper-map :pass-pipeline-record
                        :source-debug-map
                        :safety-capability-unsafe-audit-preservation-map
                        :unsupported-feature-report
                        :b3-diagnostic-stream
                        :conformance-criteria-record]
                :rejects b3-document-diagnostic-ids}
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
         :llvm-backend-manifest manifest
         :target-record (:target-record manifest)
         :llvm-ir-files (:llvm-ir-files manifest)
         :proof-to-llvm-metadata-map
         (:proof-to-llvm-metadata-map manifest)
         :runtime-abi-helper-map (:runtime-abi-helper-map manifest)
         :pass-pipeline-record (:pass-pipeline-record manifest)
         :source-debug-map (:source-debug-map manifest)
         :safety-capability-unsafe-audit-preservation-map
         (:safety-capability-unsafe-audit-preservation-map manifest)
         :unsupported-feature-report (:unsupported-feature-report manifest)
         :rejected-design-coverage
         [{:design :llvm-undefined-behavior-as-implementation-detail
           :diagnostic "B3-UB" :status :rejected}
          {:design :proofless-llvm-metadata
           :diagnostic "B3-METADATA" :status :rejected}
          {:design :host-inferred-data-layout
           :diagnostic "B3-TARGET" :status :rejected}
          {:design :pass-pipeline-erases-evidence
           :diagnostic "B3-PASS" :status :rejected}
          {:design :hidden-no-runtime-profile-dependency
           :diagnostic "B3-RUNTIME" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b3-llvm-backend-conformance-criteria-record
          :target-and-data-layout-manifest :validated
          :positive-lowering
          [:calls :closures :tagged-unions :regions :linear-resources
           :atomics :errors :ffi :checked-arithmetic :vector-operations]
          :negative-lowering
          [{:case :proofless-metadata :diagnostic "B3-METADATA"}
           {:case :invalid-inbounds :diagnostic "B3-POINTER"}
           {:case :poison-producing-shift :diagnostic "B3-UB"}
           {:case :unchecked-overflow :diagnostic "B3-NUMERIC"}
           {:case :unpinned-abi :diagnostic "B3-ABI"}]
          :numeric-mode-tests [:strict :wrapping :checked :saturating
                               :relaxed-floating]
          :volatile-mmio-atomic-ordering :preserved
          :llvm-verifier-success :external-proof-recorded
          :metadata-preservation :preserved
          :runtime-helper-selection :profile-selected
          :differential-execution :mir-reference-recorded
          :status :passed}
         :b3-diagnostic-stream diagnostic-stream
         :b3-document-results
         {:documents ["B3"]
          :task "P07-D100"
          :required-diagnostic-ids b3-document-diagnostic-ids
          :native-lowering-input-status :complete
          :target-record-status :complete
          :metadata-policy-status :complete
          :undefined-behavior-status :complete
          :pointer-status :complete
          :numeric-status :complete
          :atomic-status :complete
          :runtime-helper-status :complete
          :abi-status :complete
          :pass-pipeline-status :complete
          :manifest-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b3-document-validate! source-path artifact-base)
        capability-proof (b3-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b3-document-file-artifact
  [path]
  (b3-document-source-artifact path (slurp path)))

(def b4-document-governing-document
  "docs/phase-07-backend-architecture/101-b4-wasm-backend-design.md")

(def b4-document-diagnostic-ids
  ["B4-TARGET"
   "B4-COMPONENT"
   "B4-CANONICAL-ABI"
   "B4-IMPORT"
   "B4-EXPORT"
   "B4-MEMORY"
   "B4-BOUNDS"
   "B4-NONDETERMINISM"
   "B4-ASYNC"
   "B4-WASI-ASYNC"
   "B4-SIMD"
   "B4-ATOMIC"
   "B4-HOST-SCHEMA"
   "B4-MANIFEST"])

(def b4-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             b4-document-diagnostic-ids)))

(defn b4-document-source-overrides
  [module]
  (or (get-in module [:metadata :backend :b4-document])
      (get-in module [:metadata :backend :hosted-lowering])
      (get-in module [:metadata :backend :hosted])
      {}))

(defn b4-document-missing-evidence
  [id]
  (case id
    "B4-TARGET" :wasm-target-kind-and-feature-record
    "B4-COMPONENT" :component-contract-world-resource-plan
    "B4-CANONICAL-ABI" :canonical-abi-record
    "B4-IMPORT" :import-effect-capability-schema-provider
    "B4-EXPORT" :export-schema-ownership-lifetime
    "B4-MEMORY" :linear-memory-lifetime-pointer-policy
    "B4-BOUNDS" :bounds-check-elision-proof
    "B4-NONDETERMINISM" :replay-nondeterminism-record
    "B4-ASYNC" :async-effect-scheduling-metadata
    "B4-WASI-ASYNC" :typed-async-stream-future-abi-record
    "B4-SIMD" :simd-feature-and-certificate-record
    "B4-ATOMIC" :shared-memory-ordering-record
    "B4-HOST-SCHEMA" :host-boundary-schema
    "B4-MANIFEST" :complete-wasm-artifact-manifest
    :b4-document-evidence))