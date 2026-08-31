; Semantic decomposition of HEAD reader line 25345.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-specialized-lowering-contract-and-input
 [source-path state]
 (let
  [{:keys [source-overrides module interface-artifact input-id]} state]
  (assoc
   {}
   :kind
   :gravity/stage0-specialized-lowering-artifact
   :task
   "P07-T04"
   :document-set
   ["B8" "B9" "B10" "B11" "B12" "B13" "B14"]
   :governing-documents
   specialized-lowering-governing-documents
   :pass
   {:name :specialized-lowering,
    :input :backend-interface-and-conformance-artifact,
    :output :specialized-gpu-hdl-workflow-query-mobile-artifact,
    :requires
    [:verified-backend-interface
     :domain-anchor
     :schema-bundle
     :runtime-provider
     :capability-summary
     :effect-summary
     :source-debug-map
     :artifact-manifest],
    :preserves
    [:source-spans
     :generated-origins
     :types
     :effects
     :capabilities
     :schemas
     :safety
     :proofs
     :profile
     :target
     :artifact-provenance],
    :emits
    [:gpu-kernel-module
     :hdl-module
     :workflow-graph
     :sql-statement
     :mobile-bundle
     :artifact-manifests
     :backend-conformance-record],
    :rejects specialized-lowering-diagnostic-ids}
   :source-overrides
   source-overrides
   :module
   (select-keys
    module
    [:module
     :source-path
     :profile
     :target
     :effects
     :capabilities
     :safety
     :metadata])
   :backend-interface-artifact
   (select-keys
    interface-artifact
    [:kind
     :task
     :artifact-id
     :capability-based-proof
     :backend-interface-results])
   :backend-interface-artifact-kind
   (:kind interface-artifact)
   :backend-interface-artifact-hash
   input-id)))
