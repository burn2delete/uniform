(ns gravity.backend.gpu
  "Native B8 diagnostic and fixed document data; no GPU or input admission authority."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  {:namespace 'gravity.backend.gpu
   :governing-contracts '[D1 D2 D3 D8 D9 C1 C15 B1 B8 B13 B14 BOOT8 TEST2]
   :contract-boundary :native-b8-diagnostic-and-document-data
   :public-api {'source-overrides {:arglists '([module]) :admission :none}
                'missing-fact {:arglists '([id]) :admission :none}
                'native-kernel-text {:arglists '([]) :admission :none
                  :contract-boundary :fixed-native-gpu-document-text
                  :operation-inputs []
                  :artifact-inputs []
                  :artifact-outputs [:fixed-kernel-text]
                  :authenticates-inputs? false :grants-authority? false}
                'native-host-stub-text {:arglists '([]) :admission :none
                  :contract-boundary :fixed-native-gpu-document-text
                  :operation-inputs []
                  :artifact-inputs []
                  :artifact-outputs [:fixed-host-stub-text]
                  :authenticates-inputs? false :grants-authority? false}
                'native-kernel-structurally-valid? {:arglists '([source]) :admission :none
                  :contract-boundary :native-gpu-document-substring-check
                  :operation-inputs []
                  :artifact-inputs [:unverified-text]
                  :artifact-outputs [:boolean]
                  :authenticates-inputs? false :grants-authority? false}
                'native-host-stub-structurally-valid? {:arglists '([source]) :admission :none
                  :contract-boundary :native-gpu-document-substring-check
                  :operation-inputs []
                  :artifact-inputs [:unverified-text]
                  :artifact-outputs [:boolean]
                  :authenticates-inputs? false :grants-authority? false}
                'native-manifest {:arglists '([operations source-path input-id]) :admission :none
                  :contract-boundary :native-gpu-document-data
                  :operation-inputs [:artifact-id :kernel-text :host-stub-text]
                  :artifact-inputs [:unverified-source-path :unverified-input-id :fixed-fixture-texts]
                  :artifact-outputs [:legacy-gpu-document-manifest]
                  :authenticates-inputs? false :grants-authority? false}
                'validate-native-manifest! {:arglists '([operations source-path artifact]) :admission :none
                  :contract-boundary :native-gpu-document-checks
                  :operation-inputs [:fail! :kernel-text :host-stub-text :kernel-structurally-valid? :host-stub-structurally-valid? :diagnostic-ids]
                  :artifact-inputs [:unverified-document-data]
                  :artifact-outputs [:legacy-structural-completion-or-refusal]
                  :authenticates-inputs? false :grants-authority? false
                  :diagnostics ["B8-MANIFEST" "B8-TARGET" "B8-KERNEL" "B8-MEMORY"
                                "B8-TRANSFER" "B8-SYNC" "B8-ATOMIC" "B8-LAUNCH" "B8-MATH"]
                  :diagnostic-publication :caller-owned}
                'native-coverage-report {:arglists '([operations artifact]) :admission :none
                  :contract-boundary :native-gpu-document-data
                  :operation-inputs [:kernel-text :host-stub-text :kernel-structurally-valid? :host-stub-structurally-valid? :diagnostic-ids]
                  :artifact-inputs [:unverified-document-data]
                  :artifact-outputs [:legacy-document-coverage-record]
                  :authenticates-inputs? false :grants-authority? false}
                'kernel-id {:arglists '([id]) :admission :none}}
   :artifact-inputs [:native-module-metadata :native-b8-diagnostic-id
                     :unverified-document-data :unverified-source-path :unverified-input-id
                     :late-document-operations]
   :artifact-outputs [:borrowed-source-overrides :native-missing-fact :native-kernel-label
                      :fixed-kernel-text :fixed-host-stub-text :legacy-gpu-document-manifest
                      :legacy-structural-completion-or-refusal :legacy-document-coverage-record]
   :diagnostic-ownership {:stage [:native-override-precedence :native-rule-context-defaults
                                   :native-document-structural-refusal-decisions]
                          :existing-owner [:source-admission-refusal-decisions :failure-construction
                                           :diagnostic-stream :source-admission :publication]}
   :dependency-direction {:requires ['clojure.core 'clojure.string] :consumed-by ['gravity.bootstrap]
                          :forbids ['gravity.bootstrap 'gravity.cli 'gravity.release]}
   :authenticates-inputs? false :issues-certificates? false
   :supported [:fixed-document-texts :document-shape-checks :document-coverage-data
               :existing-native-projection :borrowed-identity :compatibility-var-interposition]
   :unsupported [:canonical-b8-semantics :gravity-b8-source-compilation
                 :verified-mir-or-domain-ingress :canonical-gpu-emission :gpu-verification
                 :kernel-legality :device-memory-lifetimes :transfer-or-synchronization-validation
                 :launch-or-target-validation :math-certificates :downstream-handoff
                 :source-authentication :public-diagnostic-redaction :migration :release
                 :self-hosting :seed-retirement]})

(defn source-overrides
  [module]
  (or (get-in module [:metadata :backend :b8-document])
      (get-in module [:metadata :backend :specialized-lowering])
      (get-in module [:metadata :backend :specialized])
      {}))

(defn missing-fact
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

(defn kernel-id
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

(defn native-kernel-text
  []
  (str "spirv.module @gravity_stage0_gpu attributes {gravity.profile = \"gpu\", gravity.target = \"spir-v\"} {\n"
       "  gpu.module @kernels {\n"
       "    gpu.func @gravity_stage0_kernel(%input: memref<1024xi32, 1>, %output: memref<1024xi32, 1>) kernel\n"
       "        attributes {gravity.effect = \"device\", gravity.capability = \"gpu/launch\", gravity.proof = \"proof/gpu-stage0-memory-sync\"} {\n"
       "      %lane = gpu.thread_id x\n"
       "      %value = memref.load %input[%lane] : memref<1024xi32, 1>\n"
       "      gpu.barrier\n"
       "      memref.store %value, %output[%lane] : memref<1024xi32, 1>\n"
       "      gpu.return\n"
       "    }\n"
       "  }\n"
       "}\n"))

(defn native-host-stub-text
  []
  (str "void gravity_stage0_launch(GravityGpuQueue queue, GravityGpuBuffer input, GravityGpuBuffer output) {\n"
       "  gravity_gpu_copy_host_to_device(queue, input);\n"
       "  gravity_gpu_launch(queue, \"gravity_stage0_kernel\", 1, 1, 1, 32, 1, 1);\n"
       "  gravity_gpu_copy_device_to_host(queue, output);\n"
       "}\n"))

(defn native-kernel-structurally-valid?
  [source]
  (and (str/includes? source "spirv.module")
       (str/includes? source "gpu.func @gravity_stage0_kernel")
       (str/includes? source "gravity.profile = \"gpu\"")
       (str/includes? source "gravity.proof")
       (str/includes? source "memref.load")
       (str/includes? source "gpu.barrier")
       (str/includes? source "memref.store")
       (not (str/includes? source "host_alloc"))
       (not (str/includes? source "throw"))
       (not (str/includes? source "eval"))))

(defn native-host-stub-structurally-valid?
  [source]
  (and (str/includes? source "gravity_gpu_copy_host_to_device")
       (str/includes? source "gravity_gpu_launch")
       (str/includes? source "gravity_gpu_copy_device_to_host")
       (not (str/includes? source "malloc"))))

(defn- native-document-port [operations key]
  (let [operation (get operations key)]
    (if (var? operation) @operation operation)))

(defn native-manifest
  [operations source-path input-id]
  (let [kernel-hash ((native-document-port operations :artifact-id) (native-document-port operations :kernel-text))
        host-stub-hash ((native-document-port operations :artifact-id) (native-document-port operations :host-stub-text))]
    {:artifact :gravity/gpu-backend-manifest
     :backend :gravity.backend/gpu
     :target {:api :spir-v
              :device-class :gpu
              :features #{:subgroups :fp16 :shared-memory}
              :binary-format :spir-v-ir}
     :emits #{:kernel-module :device-binary :host-stub
              :launch-descriptor}
     :requires #{:host-device-boundary :device-memory-lifetimes
                 :transfer-graph :sync-graph :numeric-mode}
     :rejects #{:host-effect-in-kernel :implicit-transfer
                :shared-state-without-sync :uncertified-fast-math}
     :host-device-boundary-artifact
     {:host-profile :native
      :host-provider :stage0-gpu-runtime
      :device-target :spir-v
      :feature-set #{:subgroups :fp16 :shared-memory}
      :kernel-symbol :gravity_stage0_kernel
      :argument-schemas [{:name :input
                          :gravity-type :DeviceBuffer/I32
                          :address-space :global
                          :layout :i32
                          :alignment 4
                          :aliasing :noalias
                          :lifetime :linear-kernel
                          :mutability :read-only
                          :transfer-state :host-to-device}
                         {:name :output
                          :gravity-type :DeviceBuffer/I32
                          :address-space :global
                          :layout :i32
                          :alignment 4
                          :aliasing :noalias
                          :lifetime :linear-kernel
                          :mutability :write-only
                          :transfer-state :device-to-host}]
      :device-buffer-ownership :linear
      :transfer-direction [:host-to-device :device-to-host]
      :synchronization-edges [[:copy-in :kernel-launch]
                              [:kernel-launch :copy-out]]
      :launch-order [:copy-in :kernel-launch :copy-out]
      :capability-grants #{:gpu/launch :memory/device}
      :source-generated-origin-links [:mir :c14-target-lowering
                                      :b1-interface :b8-gpu-backend]
      :status :declared}
     :kernel-ir-or-target-modules
     [{:path "gravity_stage0_gpu.spvasm"
       :content (native-document-port operations :kernel-text)
       :hash kernel-hash
       :kernel :gravity_stage0_kernel
       :status :complete}]
     :device-binary-or-intermediate-artifacts
     [{:path "gravity_stage0_gpu.spv"
       :format :spir-v-intermediate
       :source "gravity_stage0_gpu.spvasm"
       :hash kernel-hash
       :status :requires-proof-command}]
     :host-stub-artifact
     {:path "gravity_stage0_gpu_host.c"
      :content (native-document-port operations :host-stub-text)
      :hash host-stub-hash
      :profile :native
      :provider :stage0-gpu-runtime
      :status :complete}
     :kernel-lowering-map
     {:indices {:work-item :gpu.thread_id
                :lane :subgroup-local-id
                :subgroup :subgroup-id
                :workgroup :workgroup-id
                :grid :grid-id}
      :address-spaces {:global :memref-space-1
                       :shared :memref-space-3
                       :private :private
                       :constant :constant
                       :host-visible :mapped}
      :barriers [:gpu.barrier]
      :fences [:workgroup]
      :atomics [{:operation :atomic-add
                 :order :sequentially-consistent
                 :scope :device
                 :status :mapped}]
      :device-function-calls []
      :target-intrinsics [{:name :subgroup-size
                           :provider :stage0-gpu-runtime}]
      :rejected-constructs [:recursion :dynamic-allocation
                            :dynamic-dispatch :host-callback
                            :unsupported-closures]
      :status :complete}
     :device-memory-lifetime-report
     {:buffers [{:id :input
                 :allocation-provider :stage0-gpu-runtime
                 :address-space :global
                 :element-layout :i32
                 :alignment 4
                 :aliasing :noalias
                 :lifetime :linear
                 :transfer-state :host-to-device
                 :host-visibility :staged
                 :synchronization-status :copy-in-before-launch
                 :release-path :safe5-linear-release}
                {:id :output
                 :allocation-provider :stage0-gpu-runtime
                 :address-space :global
                 :element-layout :i32
                 :alignment 4
                 :aliasing :noalias
                 :lifetime :linear
                 :transfer-state :device-to-host
                 :host-visibility :after-copy-out
                 :synchronization-status :copy-out-after-launch
                 :release-path :safe5-linear-release}]
      :sharing-policy :provider-proof-required
      :hidden-transfer-insertion :rejected
      :status :complete}
     :transfer-graph
     {:nodes [:host-input :device-input :kernel :device-output
              :host-output]
      :edges [{:from :host-input
               :to :device-input
               :buffer :input
               :direction :host-to-device
               :synchronization :copy-in-complete}
              {:from :device-output
               :to :host-output
               :buffer :output
               :direction :device-to-host
               :synchronization :kernel-complete}]
      :implicit-transfers []
      :rejects [:unsynchronized-host-read :unsynchronized-device-write
                :use-after-release :double-release :invalid-aliasing]
      :status :complete}
     :synchronization-graph
     {:queue-stream-dependencies [[:queue-0 :copy-in :kernel-launch]
                                  [:queue-0 :kernel-launch :copy-out]]
      :kernel-launch-ordering [:copy-in :kernel-launch :copy-out]
      :host-device-fences [:copy-in-complete :copy-out-complete]
      :workgroup-barriers [:gpu.barrier]
      :subgroup-barriers []
      :atomics [{:operation :atomic-add
                 :memory-order :sequentially-consistent
                 :memory-scope :device}]
      :transfer-completion [:copy-in-complete :copy-out-complete]
      :event-dependencies [:event/copy-in :event/kernel :event/copy-out]
      :weakening-policy :reject-if-not-representable
      :status :complete}
     :launch-descriptor
     {:kernel :gravity_stage0_kernel
      :queue :queue-0
      :grid-size [1 1 1]
      :workgroup-size [32 1 1]
      :occupancy-assumptions {:max-resident-workgroups 1
                              :registers-per-thread 16
                              :shared-memory-bytes 0}
      :target-features #{:subgroups :fp16 :shared-memory}
      :status :complete}
     :target-feature-and-occupancy-report
     {:api :spir-v
      :device-class :gpu
      :features #{:subgroups :fp16 :shared-memory}
      :unsupported-features []
      :shared-local-memory-usage-bytes 0
      :register-pressure-assumptions {:registers-per-thread 16}
      :occupancy-assumptions {:workgroups-per-sm 1}
      :status :complete}
     :math-certificate-bundle
     {:precision :i32
      :rounding :not-applicable
      :fused-operation-policy :strict
      :denormal-behavior :not-applicable
      :reduction-associativity :not-used
      :approximation-bounds []
      :provider-implementation :stage0-gpu-runtime
      :certificates ["MATH8-stage0-strict-integer"
                     "MATH5-stage0-no-approximation"]
      :relaxed-reductions :rejected-without-certificate
      :status :complete}
     :source-debug-map
     {:source input-id
      :kernel :gravity_stage0_kernel
      :generated-origin-chain [:mir :c14-target-lowering
                               :b1-interface :b8-gpu-backend]
      :source-spans [(str source-path ":kernel")
                     (str source-path ":transfer")
                     (str source-path ":sync")]
      :proof-references ["proof/gpu-stage0-memory-sync"
                         "MATH8-stage0-strict-integer"]
      :capability-references [:gpu/launch :memory/device]
      :status :preserved}
     :spirv-validation-record
     {:declared-command "spirv-val /tmp/gravity-p07-b8-gpu.spvasm"
      :proof-artifact "docs/artifacts/phase-07/reports/p07-d105-b8-gpu-backend-report.md"
      :status :not-available-in-current-environment}
     :input-artifact input-id
     :status :complete}))
(defn validate-native-manifest!
  [operations source-path artifact]
  (let [specialized (:specialized-lowering-artifact artifact)
        manifest (:gpu-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b8-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-specialized-lowering-artifact
                 (:kind specialized))
      ((native-document-port operations :fail!) "B8-MANIFEST" source-path specialized
                         {:missing-fields [:specialized-lowering-artifact]}))
    (when-not (= :complete (get-in specialized
                                   [:capability-based-proof :status]))
      ((native-document-port operations :fail!) "B8-MANIFEST" source-path specialized
                         {:missing-fields [:specialized-lowering-proof]}))
    (when-not (= :spir-v (get-in manifest [:target :api]))
      ((native-document-port operations :fail!) "B8-TARGET" source-path manifest
                         {:missing-fields [:target-api]}))
    (when-not (= :declared (get-in manifest
                                   [:host-device-boundary-artifact
                                    :status]))
      ((native-document-port operations :fail!) "B8-KERNEL" source-path manifest
                         {:missing-fields [:host-device-boundary]}))
    (when-not (= :complete (get-in manifest
                                   [:kernel-lowering-map :status]))
      ((native-document-port operations :fail!) "B8-KERNEL" source-path manifest
                         {:missing-fields [:kernel-lowering-map]}))
    (when-not (= :complete (get-in manifest
                                   [:device-memory-lifetime-report
                                    :status]))
      ((native-document-port operations :fail!) "B8-MEMORY" source-path manifest
                         {:missing-fields [:device-memory-lifetime]}))
    (when-not (= :complete (get-in manifest [:transfer-graph :status]))
      ((native-document-port operations :fail!) "B8-TRANSFER" source-path manifest
                         {:missing-fields [:transfer-graph]}))
    (when-not (= :complete (get-in manifest
                                   [:synchronization-graph :status]))
      ((native-document-port operations :fail!) "B8-SYNC" source-path manifest
                         {:missing-fields [:synchronization-graph]}))
    (when-not (seq (get-in manifest
                           [:synchronization-graph :atomics]))
      ((native-document-port operations :fail!) "B8-ATOMIC" source-path manifest
                         {:missing-fields [:atomic-memory-scope]}))
    (when-not (= :complete (get-in manifest [:launch-descriptor :status]))
      ((native-document-port operations :fail!) "B8-LAUNCH" source-path manifest
                         {:missing-fields [:launch-descriptor]}))
    (when-not (= :complete
                 (get-in manifest
                         [:target-feature-and-occupancy-report
                          :status]))
      ((native-document-port operations :fail!) "B8-LAUNCH" source-path manifest
                         {:missing-fields [:occupancy-report]}))
    (when-not (= :complete (get-in manifest
                                   [:math-certificate-bundle :status]))
      ((native-document-port operations :fail!) "B8-MATH" source-path manifest
                         {:missing-fields [:math-certificate-bundle]}))
    (when-not (= :preserved (get-in manifest [:source-debug-map :status]))
      ((native-document-port operations :fail!) "B8-MANIFEST" source-path manifest
                         {:missing-fields [:source-debug-map]}))
    (when-not ((native-document-port operations :kernel-structurally-valid?)
               (native-document-port operations :kernel-text))
      ((native-document-port operations :fail!) "B8-KERNEL" source-path manifest
                         {:missing-fields [:kernel-module-structure]}))
    (when-not ((native-document-port operations :host-stub-structurally-valid?)
               (native-document-port operations :host-stub-text))
      ((native-document-port operations :fail!) "B8-MANIFEST" source-path manifest
                         {:missing-fields [:host-stub-structure]}))
    (when-not (every? #(contains? manifest %)
                      [:host-device-boundary-artifact
                       :kernel-ir-or-target-modules
                       :device-binary-or-intermediate-artifacts
                       :host-stub-artifact
                       :kernel-lowering-map
                       :device-memory-lifetime-report
                       :transfer-graph
                       :synchronization-graph
                       :launch-descriptor
                       :target-feature-and-occupancy-report
                       :math-certificate-bundle
                       :source-debug-map
                       :spirv-validation-record])
      ((native-document-port operations :fail!) "B8-MANIFEST" source-path manifest
                         {:missing-fields [:gpu-artifact-manifest]}))
    (when-not (= (set (native-document-port operations :diagnostic-ids)) diagnostics)
      ((native-document-port operations :fail!) "B8-MANIFEST" source-path
                         (:b8-diagnostic-stream artifact)
                         {:missing-fields [:b8-diagnostics]})))
  :complete)

(defn native-coverage-report
  [operations artifact]
  (let [manifest (:gpu-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b8-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:specialized-lowering-artifact
                           :capability-based-proof :status]))
     :target-feature-binary-format-covered?
     (= :spir-v (get-in manifest [:target :api]))
     :host-device-boundary-declared?
     (= :declared (get-in manifest
                          [:host-device-boundary-artifact :status]))
     :kernel-legality-covered?
     (= :complete (get-in manifest [:kernel-lowering-map :status]))
     :host-effect-absence-covered?
     (not (str/includes? (native-document-port operations :kernel-text) "host_alloc"))
     :device-memory-lifetimes-covered?
     (= :complete (get-in manifest
                          [:device-memory-lifetime-report :status]))
     :explicit-transfer-graph-covered?
     (= :complete (get-in manifest [:transfer-graph :status]))
     :synchronization-graph-covered?
     (= :complete (get-in manifest [:synchronization-graph :status]))
     :atomics-memory-scope-covered?
     (boolean (seq (get-in manifest
                           [:synchronization-graph :atomics])))
     :launch-occupancy-covered?
     (and (= :complete (get-in manifest [:launch-descriptor :status]))
          (= :complete
             (get-in manifest
                     [:target-feature-and-occupancy-report :status])))
     :math-certificate-covered?
     (= :complete (get-in manifest [:math-certificate-bundle :status]))
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :kernel-structurally-valid?
     ((native-document-port operations :kernel-structurally-valid?) (native-document-port operations :kernel-text))
     :host-stub-structurally-valid?
     ((native-document-port operations :host-stub-structurally-valid?) (native-document-port operations :host-stub-text))
     :manifest-complete?
     (every? #(contains? manifest %)
             [:host-device-boundary-artifact
              :kernel-ir-or-target-modules
              :device-binary-or-intermediate-artifacts
              :host-stub-artifact
              :kernel-lowering-map
              :device-memory-lifetime-report
              :transfer-graph
              :synchronization-graph
              :launch-descriptor
              :target-feature-and-occupancy-report
              :math-certificate-bundle
              :source-debug-map
              :spirv-validation-record])
     :diagnostics-covered?
     (= (set (native-document-port operations :diagnostic-ids)) diagnostics)
     :external-spirv-validation?
     (get-in manifest [:spirv-validation-record :status])
     :status :complete}))
(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
