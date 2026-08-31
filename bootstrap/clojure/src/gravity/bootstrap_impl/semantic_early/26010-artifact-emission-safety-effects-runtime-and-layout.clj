; Semantic decomposition of HEAD reader line 26010.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-artifact-emission-safety-effects-runtime-and-layout
 [source-path state]
 (let
  [{:keys [manifests module]} state]
  (assoc
   {}
   :safety-proof-certificate-bundle
   {:artifact :gravity/safety-proof-certificate-bundle,
    :safety "safety-bundle:stage0",
    :proofs
    ["proof-table:stage0"
     "proof-map:c-stage0"
     "proof-map:llvm-stage0"
     "proof-map:mlir-stage0"
     "proof-map:wasm-stage0"
     "proof-map:jvm-stage0"
     "proof-map:js-stage0"
     "proof-map:gpu-stage0"
     "proof-map:hdl-stage0"
     "proof-map:workflow-stage0"
     "proof-map:query-stage0"
     "proof-map:mobile-stage0"],
    :certificates ["cert/c18-safety-check-elision"],
    :unsafe-audit-records [],
    :status :complete}
   :effect-capability-summary
   {:artifact :gravity/effect-capability-summary,
    :effects (:effects module),
    :capabilities (:capabilities module),
    :manifest-count (count manifests),
    :status :complete}
   :runtime-provider-summary
   {:artifact :gravity/runtime-provider-summary,
    :providers
    [:clojure-jvm-stage0
     :stage0-native
     :stage0-hosted
     :stage0-specialized],
    :hidden-dependencies [],
    :status :complete}
   :target-runtime-abi-layout-summary
   {:artifact :gravity/target-runtime-abi-layout-summary,
    :targets (set (map :target manifests)),
    :backends (set (map :backend manifests)),
    :abi-layout-records
    [:jvm-stage0
     :native-stage0
     :wasm-component-stage0
     :gpu-host-device-stage0
     :hdl-port-schema-stage0
     :query-result-stage0
     :mobile-platform-stage0],
    :runtime-providers [:clojure-jvm-stage0 :stage0-provider-records],
    :status :complete})))
