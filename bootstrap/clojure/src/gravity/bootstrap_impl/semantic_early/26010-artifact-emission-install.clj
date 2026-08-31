; Semantic decomposition of HEAD reader line 26010.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 artifact-emission-source-artifact
 [source-path source-text]
 (let
  [forms
   (mapv :form (read-source-form-records source-path source-text))
   _
   (validate-ns-syntax! source-path forms)
   module
   (parse-module source-path forms)
   source-overrides
   (artifact-emission-source-overrides module)
   _
   (artifact-emission-validate-source-overrides!
    source-path
    source-overrides)
   interface-artifact
   (backend-interface-source-artifact source-path source-text)
   native-artifact
   (native-lowering-source-artifact source-path source-text)
   hosted-artifact
   (hosted-lowering-source-artifact source-path source-text)
   specialized-artifact
   (specialized-lowering-source-artifact source-path source-text)
   input-id
   (:artifact-id specialized-artifact)
   interface-manifest
   (artifact-emission-interface-manifest interface-artifact)
   manifests
   (vec
    (concat
     [interface-manifest]
     (:artifact-manifests native-artifact)
     (:artifact-manifests hosted-artifact)
     (:artifact-manifests specialized-artifact)))
   manifest-hashes
   (mapv :content-hash manifests)
   graph-nodes
   (vec
    (concat
     [{:id (:artifact-id interface-artifact),
       :kind (:kind interface-artifact)}
      {:id (:artifact-id native-artifact),
       :kind (:kind native-artifact)}
      {:id (:artifact-id hosted-artifact),
       :kind (:kind hosted-artifact)}
      {:id (:artifact-id specialized-artifact),
       :kind (:kind specialized-artifact)}]
     (mapv
      (fn
       [manifest]
       {:id (:content-hash manifest),
        :kind (:kind manifest),
        :backend (:backend manifest)})
      manifests)))
   diagnostic-stream
   (artifact-emission-diagnostic-stream source-path input-id)
   artifact-base
   (let
    [state
     {:interface-artifact interface-artifact,
      :_ _,
      :manifest-hashes manifest-hashes,
      :manifests manifests,
      :graph-nodes graph-nodes,
      :module module,
      :interface-manifest interface-manifest,
      :specialized-artifact specialized-artifact,
      :native-artifact native-artifact,
      :hosted-artifact hosted-artifact,
      :diagnostic-stream diagnostic-stream,
      :input-id input-id,
      :source-overrides source-overrides,
      :forms forms}]
    (merge
     (semantic-early-artifact-emission-contract-and-backend-inputs
      source-path
      state)
     (semantic-early-artifact-emission-manifests-content-and-graph
      source-path
      state)
     (semantic-early-artifact-emission-provenance source-path state)
     (semantic-early-artifact-emission-safety-effects-runtime-and-layout
      source-path
      state)
     (semantic-early-artifact-emission-reproducibility-conformance-and-release
      source-path
      state)
     (semantic-early-artifact-emission-diagnostics-and-results
      source-path
      state)))
   _
   (artifact-emission-validate! source-path artifact-base)
   capability-proof
   (artifact-emission-capability-proof artifact-base)]
  (assoc
   artifact-base
   :capability-based-proof
   capability-proof
   :artifact-id
   (c4-artifact-id
    (assoc artifact-base :capability-based-proof capability-proof)))))

(clojure.core/doseq
 [symbol__1382__auto__
  '[semantic-early-artifact-emission-contract-and-backend-inputs
    semantic-early-artifact-emission-manifests-content-and-graph
    semantic-early-artifact-emission-provenance
    semantic-early-artifact-emission-safety-effects-runtime-and-layout
    semantic-early-artifact-emission-reproducibility-conformance-and-release
    semantic-early-artifact-emission-diagnostics-and-results]]
 (clojure.core/ns-unmap clojure.core/*ns* symbol__1382__auto__))
