(ns gravity.optimization-lowering.lowering
  "Derived C14 target-lowering requests, records, and manifests.")

(def target
  {:backend :jvm
   :triple "jvm-17"
   :features #{:objects :exceptions :threads}})

(defn lowering-request [input-id]
  {:artifact :gravity/lowering-request
   :input {:kind :verified-domain-ir
           :id input-id}
   :profile :hosted
   :target target
   :abi :jvm-hosted-stage0
   :runtime :hosted-jvm
   :providers {:allocator :jvm/gc
               :panic :jvm/exception
               :io :jvm/stdout}
   :required-evidence {:safety :mir/safety-table
                       :proofs :proof/c13-stage0
                       :capabilities :mir/capability-proof-table}})

(def proof-map
  {:artifact :gravity/proof-target-metadata-map
   :target :jvm
   :entries [{:target-metadata :bounds-check-elided
              :operation "mir-op-optimized-bounds-check-elide"
              :proof :proof/c13-bounds-check-elision}
             {:target-metadata :noalias
              :operation "mir-op-optimized-target-layout-prepare"
              :proof :proof/c13-layout-ownership}
             {:target-metadata :nonnull
              :operation "mir-op-optimized-dead-code-eliminate"
              :proof :proof/c13-safety-preserved}]})

(def target-eligibility-report
  {:artifact :gravity/target-eligibility-report
   :status :eligible
   :profile :hosted
   :target target
   :backend :jvm
   :reason :profile-target-provider-compatible})

(def abi-manifest
  {:artifact :gravity/abi-manifest
   :status :complete
   :calling-convention :jvm-static
   :data-layout :jvm-object
   :closure-representation :jvm-function-object
   :panic-strategy :exception})

(defn runtime-provider-manifest [request]
  {:artifact :gravity/runtime-provider-manifest
   :status :complete
   :runtime :hosted-jvm
   :providers (:providers request)})

(def provider-selection-records
  [{:provider :jvm/gc
    :capability :memory/allocator
    :status :selected}
   {:provider :jvm/stdout
    :capability :io/stdout
    :status :selected}
   {:provider :jvm/exception
    :capability :panic/raise
    :status :selected}])

(def layout-decision-record
  {:artifact :gravity/layout-decision-record
   :status :complete
   :alignment :jvm-default
   :proof :proof/c13-layout-ownership})

(defn source-generated-origin-map [domain-ir-artifact]
  {:artifact :gravity/source-generated-origin-map
   :status :complete
   :source-map (:semantic-anchor-map domain-ir-artifact)})

(def capability-preservation-report
  {:artifact :gravity/capability-preservation-report
   :status :preserved
   :denied-additions []})

(def unsupported-feature-report
  [{:feature :gpu-kernel
    :backend :jvm
    :profile :hosted
    :fallback :mir-scalar-kernel
    :fallback-status :available
    :diagnostic-id nil}])

(defn target-artifact-manifest [sha256-hex input-id final-output-id]
  {:artifact :gravity/target-artifact-manifest
   :input final-output-id
   :backend :jvm
   :profile :hosted
   :target (str "sha256:" (sha256-hex (pr-str target)))
   :artifacts [{:kind :jvm-bytecode-plan
                :hash (str "sha256:" (sha256-hex (pr-str final-output-id)))}]
   :source-map :gravity/source-generated-origin-map
   :proof-map :gravity/proof-target-metadata-map
   :effects :mir/effect-table
   :capabilities :mir/capability-proof-table
   :safety :mir/safety-table
   :runtime :gravity/runtime-provider-manifest
   :dependencies input-id
   :diagnostics []})
