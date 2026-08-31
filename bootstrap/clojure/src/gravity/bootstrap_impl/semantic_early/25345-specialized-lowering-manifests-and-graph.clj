; Semantic decomposition of HEAD reader line 25345.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-specialized-lowering-manifests-and-graph
 [source-path state]
 (let
  [{:keys
    [mobile-manifest
     hdl-manifest
     query-manifest
     gpu-manifest
     workflow-manifest
     input-id]}
   state]
  (assoc
   {}
   :target-lowering-manifest
   [{:backend :gravity.backend/gpu,
     :target :spir-v,
     :artifact-kinds
     [:kernel-module
      :device-binary-manifest
      :host-stub
      :launch-descriptor],
     :status :complete}
    {:backend :gravity.backend/hdl,
     :target :systemverilog,
     :artifact-kinds
     [:hdl :testbench :timing-constraints :interface-schema],
     :status :complete}
    {:backend :gravity.backend/workflow-graph,
     :target :durable-workflow,
     :artifact-kinds
     [:workflow-graph :event-log-schema :replay-fixture :policy-graph],
     :status :complete}
    {:backend :gravity.backend/query-relational,
     :target :postgresql,
     :artifact-kinds
     [:sql :prepared-bindings :query-plan :typed-result-adapter],
     :status :complete}
    {:backend :gravity.backend/mobile,
     :target :ios,
     :artifact-kinds
     [:app-bundle
      :platform-bindings
      :permission-manifest
      :store-audit],
     :status :complete}]
   :artifact-manifests
   [gpu-manifest
    hdl-manifest
    workflow-manifest
    query-manifest
    mobile-manifest]
   :artifact-graph
   {:artifact :gravity/artifact-graph,
    :nodes
    [{:id input-id, :kind :backend-interface}
     {:id (:content-hash gpu-manifest), :kind :gpu-kernel}
     {:id (:content-hash hdl-manifest), :kind :hdl}
     {:id (:content-hash workflow-manifest), :kind :workflow-graph}
     {:id (:content-hash query-manifest), :kind :sql}
     {:id (:content-hash mobile-manifest), :kind :mobile-bundle}],
    :edges
    [{:from input-id,
      :to (:content-hash gpu-manifest),
      :pass :gpu-backend}
     {:from input-id,
      :to (:content-hash hdl-manifest),
      :pass :hdl-backend}
     {:from input-id,
      :to (:content-hash workflow-manifest),
      :pass :workflow-backend}
     {:from input-id,
      :to (:content-hash query-manifest),
      :pass :query-backend}
     {:from input-id,
      :to (:content-hash mobile-manifest),
      :pass :mobile-backend}],
    :status :complete})))
