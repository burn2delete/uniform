; Semantic decomposition of HEAD reader line 24702.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-hosted-lowering-contract-and-input
 [source-path state]
 (let
  [{:keys [source-overrides module interface-artifact input-id]} state]
  (assoc
   {}
   :kind
   :gravity/stage0-hosted-lowering-artifact
   :task
   "P07-T03"
   :document-set
   ["B4" "B5" "B6" "B13" "B14"]
   :governing-documents
   hosted-lowering-governing-documents
   :pass
   {:name :hosted-lowering,
    :input :backend-interface-and-conformance-artifact,
    :output :hosted-wasm-jvm-js-ts-lowering-artifact,
    :requires
    [:verified-backend-interface
     :host-boundary-schema
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
     :safety
     :proofs
     :profile
     :target
     :artifact-provenance],
    :emits
    [:wasm-component
     :jvm-class-and-jar
     :javascript-module
     :typescript-declarations
     :host-boundary-manifests
     :artifact-manifests
     :backend-conformance-record],
    :rejects hosted-lowering-diagnostic-ids}
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
