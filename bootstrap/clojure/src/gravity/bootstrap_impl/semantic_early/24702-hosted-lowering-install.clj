; Semantic decomposition of HEAD reader line 24702.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 hosted-lowering-source-artifact
 [source-path source-text]
 (let
  [forms
   (mapv :form (read-source-form-records source-path source-text))
   _
   (validate-ns-syntax! source-path forms)
   module
   (parse-module source-path forms)
   source-overrides
   (hosted-lowering-source-overrides module)
   _
   (hosted-lowering-validate-source-overrides!
    source-path
    source-overrides)
   interface-artifact
   (backend-interface-source-artifact source-path source-text)
   input-id
   (:artifact-id interface-artifact)
   wasm-module
   "(module (func $gravity_entry (param i64) (result i64) local.get 0))"
   jvm-class
   "class gravity.stage0.Hosted { static long entry(long x) { return x; } }"
   js-module
   "export function gravityEntry(x) { return x; }\n"
   ts-declarations
   "export declare function gravityEntry(x: bigint): bigint;\n"
   wasm-manifest
   (hosted-lowering-artifact-manifest
    :gravity.backend/wasm
    :wasm-component
    :wasm32-component
    wasm-module
    input-id
    "proof-map:wasm-stage0")
   jvm-manifest
   (hosted-lowering-artifact-manifest
    :gravity.backend/jvm
    :jar
    :jvm-21
    jvm-class
    input-id
    "proof-map:jvm-stage0")
   js-manifest
   (hosted-lowering-artifact-manifest
    :gravity.backend/js-ts
    :javascript-module
    :browser-esm
    js-module
    input-id
    "proof-map:js-ts-stage0")
   diagnostic-stream
   (hosted-lowering-diagnostic-stream source-path input-id)
   artifact-base
   (let
    [state
     {:interface-artifact interface-artifact,
      :_ _,
      :jvm-manifest jvm-manifest,
      :wasm-module wasm-module,
      :module module,
      :js-manifest js-manifest,
      :jvm-class jvm-class,
      :wasm-manifest wasm-manifest,
      :diagnostic-stream diagnostic-stream,
      :js-module js-module,
      :input-id input-id,
      :ts-declarations ts-declarations,
      :source-overrides source-overrides,
      :forms forms}]
    (merge
     (semantic-early-hosted-lowering-contract-and-input
      source-path
      state)
     (semantic-early-hosted-lowering-manifests-and-graph
      source-path
      state)
     (semantic-early-hosted-lowering-wasm-backend source-path state)
     (semantic-early-hosted-lowering-jvm-backend source-path state)
     (semantic-early-hosted-lowering-javascript-typescript-backend
      source-path
      state)
     (semantic-early-hosted-lowering-evidence-and-results
      source-path
      state)))
   _
   (hosted-lowering-validate! source-path artifact-base)
   capability-proof
   (hosted-lowering-capability-proof artifact-base)]
  (assoc
   artifact-base
   :capability-based-proof
   capability-proof
   :artifact-id
   (c4-artifact-id
    (assoc artifact-base :capability-based-proof capability-proof)))))

(clojure.core/doseq
 [symbol__1382__auto__
  '[semantic-early-hosted-lowering-contract-and-input
    semantic-early-hosted-lowering-manifests-and-graph
    semantic-early-hosted-lowering-wasm-backend
    semantic-early-hosted-lowering-jvm-backend
    semantic-early-hosted-lowering-javascript-typescript-backend
    semantic-early-hosted-lowering-evidence-and-results]]
 (clojure.core/ns-unmap clojure.core/*ns* symbol__1382__auto__))
