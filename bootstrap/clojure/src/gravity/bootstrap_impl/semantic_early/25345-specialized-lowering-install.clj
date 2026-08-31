; Semantic decomposition of HEAD reader line 25345.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 specialized-lowering-source-artifact
 [source-path source-text]
 (let
  [forms
   (mapv :form (read-source-form-records source-path source-text))
   _
   (validate-ns-syntax! source-path forms)
   module
   (parse-module source-path forms)
   source-overrides
   (specialized-lowering-source-overrides module)
   _
   (specialized-lowering-validate-source-overrides!
    source-path
    source-overrides)
   interface-artifact
   (backend-interface-source-artifact source-path source-text)
   input-id
   (:artifact-id interface-artifact)
   gpu-module
   "spirv.module @gravity_stage0_kernel { gpu.kernel @entry }"
   hdl-module
   "module gravity_stage0(input logic clk, output logic done); assign done = clk; endmodule\n"
   workflow-graph
   "{:workflow :gravity-stage0 :steps [:start :call :done]}"
   sql-module
   "select $1::bigint as gravity_value"
   mobile-bundle
   "{:bundle-id \"org.gravity.stage0\" :platform :ios}"
   gpu-manifest
   (specialized-lowering-artifact-manifest
    :gravity.backend/gpu
    :gpu-kernel-module
    :spir-v
    gpu-module
    input-id
    "proof-map:gpu-stage0")
   hdl-manifest
   (specialized-lowering-artifact-manifest
    :gravity.backend/hdl
    :systemverilog-module
    :systemverilog
    hdl-module
    input-id
    "proof-map:hdl-stage0")
   workflow-manifest
   (specialized-lowering-artifact-manifest
    :gravity.backend/workflow-graph
    :workflow-graph
    :durable-workflow
    workflow-graph
    input-id
    "proof-map:workflow-stage0")
   query-manifest
   (specialized-lowering-artifact-manifest
    :gravity.backend/query-relational
    :sql-statement
    :postgresql
    sql-module
    input-id
    "proof-map:query-stage0")
   mobile-manifest
   (specialized-lowering-artifact-manifest
    :gravity.backend/mobile
    :app-bundle
    :ios
    mobile-bundle
    input-id
    "proof-map:mobile-stage0")
   diagnostic-stream
   (specialized-lowering-diagnostic-stream source-path input-id)
   artifact-base
   (let
    [state
     {:interface-artifact interface-artifact,
      :mobile-bundle mobile-bundle,
      :hdl-manifest hdl-manifest,
      :gpu-module gpu-module,
      :query-manifest query-manifest,
      :sql-module sql-module,
      :_ _,
      :module module,
      :gpu-manifest gpu-manifest,
      :diagnostic-stream diagnostic-stream,
      :workflow-manifest workflow-manifest,
      :workflow-graph workflow-graph,
      :hdl-module hdl-module,
      :input-id input-id,
      :mobile-manifest mobile-manifest,
      :source-overrides source-overrides,
      :forms forms}]
    (merge
     (semantic-early-specialized-lowering-contract-and-input
      source-path
      state)
     (semantic-early-specialized-lowering-manifests-and-graph
      source-path
      state)
     (semantic-early-specialized-lowering-gpu-and-hdl-backends
      source-path
      state)
     (semantic-early-specialized-lowering-workflow-and-query-backends
      source-path
      state)
     (semantic-early-specialized-lowering-mobile-backend
      source-path
      state)
     (semantic-early-specialized-lowering-evidence-and-results
      source-path
      state)))
   _
   (specialized-lowering-validate! source-path artifact-base)
   capability-proof
   (specialized-lowering-capability-proof artifact-base)]
  (assoc
   artifact-base
   :capability-based-proof
   capability-proof
   :artifact-id
   (c4-artifact-id
    (assoc artifact-base :capability-based-proof capability-proof)))))

(clojure.core/doseq
 [symbol__1382__auto__
  '[semantic-early-specialized-lowering-contract-and-input
    semantic-early-specialized-lowering-manifests-and-graph
    semantic-early-specialized-lowering-gpu-and-hdl-backends
    semantic-early-specialized-lowering-workflow-and-query-backends
    semantic-early-specialized-lowering-mobile-backend
    semantic-early-specialized-lowering-evidence-and-results]]
 (clojure.core/ns-unmap clojure.core/*ns* symbol__1382__auto__))
