

(def b8-document-kernel-module
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

(def b8-document-host-stub
  (str "void gravity_stage0_launch(GravityGpuQueue queue, GravityGpuBuffer input, GravityGpuBuffer output) {\n"
       "  gravity_gpu_copy_host_to_device(queue, input);\n"
       "  gravity_gpu_launch(queue, \"gravity_stage0_kernel\", 1, 1, 1, 32, 1, 1);\n"
       "  gravity_gpu_copy_device_to_host(queue, output);\n"
       "}\n"))

(defn b8-document-kernel-structurally-valid?
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

(defn b8-document-host-stub-structurally-valid?
  [source]
  (and (str/includes? source "gravity_gpu_copy_host_to_device")
       (str/includes? source "gravity_gpu_launch")
       (str/includes? source "gravity_gpu_copy_device_to_host")
       (not (str/includes? source "malloc"))))

(defn b8-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/b8-gpu-backend-diagnostic-stream
   :stage :b8-gpu-backend-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b8-gpu-backend-document-coverage
            :backend :gravity.backend/gpu
            :message-key (keyword "backend-gpu" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b8-document-syntax-" index)
                      :artifact input-id}
            :kernel-id (b8-document-kernel-id id)
            :mir-op (case id
                      "B8-HOST-EFFECT" :host-effect-capture
                      "B8-MEMORY" :device-buffer
                      "B8-TRANSFER" :host-device-transfer
                      "B8-SYNC" :gpu-barrier
                      "B8-ATOMIC" :gpu-atomic
                      "B8-LAUNCH" :kernel-launch
                      "B8-MATH" :gpu-numeric-lowering
                      :gpu-kernel)
            :domain-anchor :gpu-kernel
            :device-target :spir-v
            :address-space (case id
                             "B8-SYNC" :shared
                             "B8-ATOMIC" :global
                             :global)
            :buffer-id (case id
                         "B8-MEMORY" :input
                         "B8-TRANSFER" :output
                         :input)
            :launch-descriptor {:grid [1 1 1]
                                :workgroup [32 1 1]
                                :shared-memory-bytes 0}
            :missing-proof-or-feature (b8-document-missing-fact id)
            :fallback-status :rejected
            :facts {:host-device-boundary-required true
                    :implicit-transfer-policy :rejected
                    :device-buffer-policy :linear
                    :numeric-policy :certificate-required}
            :remediation [{:kind :declare-gpu-target-and-kernel}
                          {:kind :emit-device-memory-transfer-sync-graphs}
                          {:kind :attach-launch-and-math-certificate-records}]
            :redactions []
            :ordering-key [id :b8-gpu-backend-document-coverage
                           :spir-v]})
         b8-document-diagnostic-ids
         (range))
   :status :complete})