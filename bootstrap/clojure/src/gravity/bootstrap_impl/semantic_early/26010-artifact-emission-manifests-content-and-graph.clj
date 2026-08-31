; Semantic decomposition of HEAD reader line 26010.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-artifact-emission-manifests-content-and-graph
 [source-path state]
 (let
  [{:keys
    [manifests
     input-id
     interface-artifact
     graph-nodes
     specialized-artifact
     hosted-artifact
     native-artifact]}
   state]
  (assoc
   {}
   :artifact-manifests
   manifests
   :content-hash-records
   (mapv
    (fn
     [manifest]
     {:artifact :gravity/content-hash-record,
      :artifact-kind (:kind manifest),
      :backend (:backend manifest),
      :content-hash (:content-hash manifest),
      :manifest-digest (c4-artifact-id manifest),
      :status :complete})
    manifests)
   :artifact-graph
   {:artifact :gravity/artifact-graph,
    :root input-id,
    :nodes graph-nodes,
    :edges
    (vec
     (concat
      [{:from (:artifact-id interface-artifact),
        :to (:artifact-id native-artifact),
        :pass :native-lowering}
       {:from (:artifact-id interface-artifact),
        :to (:artifact-id hosted-artifact),
        :pass :hosted-lowering}
       {:from (:artifact-id interface-artifact),
        :to (:artifact-id specialized-artifact),
        :pass :specialized-lowering}]
      (mapv
       (fn
        [manifest]
        {:from (:artifact-id interface-artifact),
         :to (:content-hash manifest),
         :pass :artifact-emission,
         :backend (:backend manifest)})
       manifests))),
    :invalidation-rules
    [:source-change
     :compiler-change
     :pass-change
     :target-change
     :dependency-change
     :evidence-change],
    :status :complete})))
