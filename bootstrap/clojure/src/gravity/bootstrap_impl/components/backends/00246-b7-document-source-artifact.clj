

(defn b7-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b7-document-source-overrides module)
        _ (b7-document-validate-source-overrides! source-path
                                                  source-overrides)
        native-artifact (native-lowering-source-artifact source-path
                                                         source-text)
        input-id (:artifact-id native-artifact)
        manifest (b7-document-mlir-manifest input-id)
        diagnostic-stream (b7-document-diagnostic-stream source-path
                                                         input-id)
        artifact-base
        {:kind :gravity/stage0-b7-mlir-backend-document-artifact
         :task "P07-D104"
         :document-set ["B7"]
         :governing-document b7-document-governing-document
         :pass {:name :b7-mlir-backend-document-coverage
                :input :native-lowering-artifact
                :output :b7-mlir-backend-document-artifact
                :requires [:verified-mir-or-domain-ir :b1-backend-interface
                           :c11-mir :c12-domain-ir :c13-invalidation
                           :c14-target-lowering :dialect-registry
                           :conversion-legality :proof-map
                           :metadata-preservation-policy]
                :preserves [:source-spans :generated-origins :types
                            :effects :capabilities :ownership
                            :safety :proofs :profile :target
                            :diagnostics :artifact-provenance]
                :emits [:mlir-backend-manifest :dialect-registry
                        :gravity-dialect-operation-schema :mlir-module
                        :conversion-legality-report :pass-pipeline-log
                        :mlir-verifier-report
                        :proof-to-dialect-attribute-map
                        :source-debug-map :downstream-handoff-manifest
                        :b7-diagnostic-stream
                        :conformance-criteria-record]
                :rejects b7-document-diagnostic-ids}
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
         :mlir-backend-manifest manifest
         :dialect-registry-manifest
         (:dialect-registry-manifest manifest)
         :gravity-dialect-operation-schema
         (:gravity-dialect-operation-schema manifest)
         :standard-dialect-fact-mapping
         (:standard-dialect-fact-mapping manifest)
         :operation-and-type-mapping-record
         (:operation-and-type-mapping-record manifest)
         :mlir-modules (:mlir-modules manifest)
         :conversion-target-and-legality-report
         (:conversion-target-and-legality-report manifest)
         :pass-pipeline-log (:pass-pipeline-log manifest)
         :mlir-verifier-report (:mlir-verifier-report manifest)
         :proof-to-dialect-attribute-map
         (:proof-to-dialect-attribute-map manifest)
         :source-debug-map (:source-debug-map manifest)
         :downstream-handoff-manifests
         (:downstream-handoff-manifests manifest)
         :metadata-preservation-policy
         (:metadata-preservation-policy manifest)
         :semantic-authority-record
         (:semantic-authority-record manifest)
         :rejected-design-coverage
         [{:design :mlir-verifier-as-gravity-proof
           :diagnostic "B7-VERIFY" :status :rejected}
          {:design :silent-effect-memory-numeric-capability-change
           :diagnostic "B7-EFFECT" :status :rejected}
          {:design :dialect-boundary-metadata-loss
           :diagnostic "B7-METADATA" :status :rejected}
          {:design :downstream-handoff-without-legality
           :diagnostic "B7-HANDOFF" :status :rejected}
          {:design :target-behavior-round-trip-into-generic-mir
           :diagnostic "B7-CONVERSION" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b7-mlir-backend-conformance-criteria-record
          :gravity-dialect-operation-type-schemas :complete
          :standard-dialect-metadata-mapping :complete
          :mlir-verifier-acceptance-rejection-fixtures :covered
          :pass-pipeline-preservation-invalidation-logs :complete
          :proof-to-dialect-attribute-maps :complete
          :downstream-llvm-gpu-handoff-fixtures :complete
          :metadata-loss-illegal-conversion-effect-pass-rejection :covered
          :differential-execution :mir-domain-reference-recorded
          :status :passed}
         :b7-diagnostic-stream diagnostic-stream
         :b7-document-results
         {:documents ["B7"]
          :task "P07-D104"
          :required-diagnostic-ids b7-document-diagnostic-ids
          :native-lowering-input-status :complete
          :dialect-status :complete
          :operation-schema-status :complete
          :standard-dialect-mapping-status :complete
          :operation-type-mapping-status :complete
          :module-status :complete
          :conversion-status :complete
          :pass-status :complete
          :verifier-status :complete
          :proof-map-status :complete
          :source-debug-status :complete
          :handoff-status :complete
          :metadata-status :complete
          :semantic-authority-status :complete
          :manifest-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b7-document-validate! source-path artifact-base)
        capability-proof (b7-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b7-document-file-artifact
  [path]
  (b7-document-source-artifact path (slurp path)))

(def b8-document-governing-document
  "docs/phase-07-backend-architecture/105-b8-gpu-backend-design.md")

(def b8-document-diagnostic-ids
  ["B8-TARGET"
   "B8-KERNEL"
   "B8-HOST-EFFECT"
   "B8-MEMORY"
   "B8-TRANSFER"
   "B8-SYNC"
   "B8-ATOMIC"
   "B8-LAUNCH"
   "B8-MATH"
   "B8-MANIFEST"])

(def b8-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             b8-document-diagnostic-ids)))

(defn b8-document-source-overrides
  [module]
  (or (get-in module [:metadata :backend :b8-document])
      (get-in module [:metadata :backend :specialized-lowering])
      (get-in module [:metadata :backend :specialized])
      {}))

(defn b8-document-missing-fact
  [id]
  (case id
    "B8-TARGET" :gpu-api-device-feature-binary-format
    "B8-KERNEL" :gpu-profile-kernel-legality
    "B8-HOST-EFFECT" :host-effect-absence-proof
    "B8-MEMORY" :device-memory-lifetime-layout-address-space
    "B8-TRANSFER" :explicit-transfer-graph
    "B8-SYNC" :synchronization-graph-barrier-fence
    "B8-ATOMIC" :atomic-order-memory-scope-map
    "B8-LAUNCH" :launch-geometry-occupancy-shared-memory-record
    "B8-MATH" :numeric-mode-math-certificate
    "B8-MANIFEST" :complete-gpu-artifact-manifest
    :b8-document-evidence))

(defn b8-document-kernel-id
  [id]
  (case id
    "B8-TARGET" :gravity_stage0_kernel
    "B8-KERNEL" :gravity_stage0_kernel
    "B8-HOST-EFFECT" :gravity_stage0_kernel
    "B8-MEMORY" :gravity_stage0_kernel
    "B8-TRANSFER" :gravity_stage0_kernel
    "B8-SYNC" :gravity_stage0_kernel
    "B8-ATOMIC" :gravity_stage0_atomic
    "B8-LAUNCH" :gravity_stage0_kernel
    "B8-MATH" :gravity_stage0_reduce
    "B8-MANIFEST" :gravity_stage0_kernel
    :gravity_stage0_kernel))

(defn b8-document-fail!
  [id source-path subject extra]
  (fail! id
         "B8 GPU backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :b8-gpu-backend-document
                 :stage (or (:stage subject)
                            :b8-gpu-backend-document-coverage)
                 :backend :gravity.backend/gpu
                 :profile (or (:profile subject) :gpu)
                 :kernel-id (or (:kernel-id subject)
                                (b8-document-kernel-id id))
                 :mir-op (or (:mir-op subject) :gpu-kernel)
                 :domain-anchor (or (:domain-anchor subject) :gpu-kernel)
                 :device-target (or (:device-target subject) :spir-v)
                 :address-space (or (:address-space subject) :global)
                 :buffer-id (or (:buffer-id subject) :input)
                 :launch-descriptor
                 (or (:launch-descriptor subject)
                     {:grid [1 1 1] :workgroup [32 1 1]})
                 :missing-proof-or-feature
                 (or (:missing-proof-or-feature subject)
                     (b8-document-missing-fact id))
                 :fallback-status (or (:fallback-status subject) :rejected)
                 :remediation "Emit GPU artifacts only from verified GPU/domain IR with explicit host/device boundaries, kernel legality, device memory lifetimes, transfer and synchronization graphs, atomic scope maps, launch descriptors, target features, numeric certificates, source/debug maps, and complete GPU manifests."}
                extra)))

(defn b8-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b8-document-override-diagnostics fail-kind)]
      (b8-document-fail!
       id source-path
       {:stage :b8-gpu-backend-document-coverage
        :artifact-id (str "b8-document-" (name fail-kind))
        :missing-proof-or-feature fail-kind
        :kernel-id (keyword (str "b8-document-" (name fail-kind)))}
       {:missing-fields [fail-kind]}))))