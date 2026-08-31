; Semantic decomposition of HEAD reader line 24702.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-hosted-lowering-manifests-and-graph
 [source-path state]
 (let
  [{:keys [jvm-manifest js-manifest wasm-manifest input-id]} state]
  (assoc
   {}
   :target-lowering-manifest
   [{:backend :gravity.backend/wasm,
     :target :wasm32-component,
     :artifact-kinds
     [:wasm-module :component-contract :canonical-abi :wit-bindings],
     :status :complete}
    {:backend :gravity.backend/jvm,
     :target :jvm-21,
     :artifact-kinds
     [:class-files :jar :interop-descriptor :debug-map],
     :status :complete}
    {:backend :gravity.backend/js-ts,
     :target :browser-esm,
     :artifact-kinds
     [:javascript
      :typescript-declarations
      :source-map
      :package-manifest],
     :status :complete}]
   :artifact-manifests
   [wasm-manifest jvm-manifest js-manifest]
   :artifact-graph
   {:artifact :gravity/artifact-graph,
    :nodes
    [{:id input-id, :kind :backend-interface}
     {:id (:content-hash wasm-manifest), :kind :wasm-component}
     {:id (:content-hash jvm-manifest), :kind :jar}
     {:id (:content-hash js-manifest), :kind :javascript-module}],
    :edges
    [{:from input-id,
      :to (:content-hash wasm-manifest),
      :pass :wasm-backend}
     {:from input-id,
      :to (:content-hash jvm-manifest),
      :pass :jvm-backend}
     {:from input-id,
      :to (:content-hash js-manifest),
      :pass :js-ts-backend}],
    :status :complete})))
