

(defn b8-document-gpu-manifest
  [source-path input-id]
  (let [kernel-hash (c4-artifact-id b8-document-kernel-module)
        host-stub-hash (c4-artifact-id b8-document-host-stub)]
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
       :content b8-document-kernel-module
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
      :content b8-document-host-stub
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