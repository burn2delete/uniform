(ns gravity.c14-lowering.projection
  (:require [gravity.c14-lowering.operations :as operations]
            [gravity.digest]))
(defn- sha256-hex [value] (operations/invoke :sha256-hex gravity.digest/sha256-hex value))
(defn- source-span [path index]
  (operations/invoke :source-span (fn [p i] {:source p :form-index i}) path index))
(defn artifact-base [configuration source-path source-text module source-overrides optimization-artifact diagnostics]
  (let [optimized-mir (:optimized-mir-artifact optimization-artifact)
        input-id (:artifact-id optimization-artifact)
        target {:backend :jvm :triple "jvm-17" :features #{:objects :exceptions :threads}}
        lowering-request {:artifact :gravity/lowering-request
                          :input {:kind :optimized-mir :id input-id :optimized-mir (:output optimized-mir)}
                          :profile :hosted :target target :abi :jvm-hosted-stage0 :runtime :hosted-jvm
                          :providers {:allocator :jvm/gc :panic :jvm/exception :io :jvm/stdout}
                          :required-evidence {:safety :mir/safety-table :proofs :proof/c14-stage0
                                              :capabilities :mir/capability-proof-table}}
        proof-map {:artifact :gravity/proof-target-metadata-map :target :jvm
                   :entries [{:target-metadata :bounds-check-elided
                              :operation "mir-op-optimized-bounds-check-elide" :proof :proof/c13-bounds-check-elision}
                             {:target-metadata :noalias
                              :operation "mir-op-optimized-target-layout-prepare" :proof :proof/c13-layout-ownership}
                             {:target-metadata :nonnull
                              :operation "mir-op-optimized-dead-code-eliminate" :proof :proof/c13-safety-preserved}]}
        target-artifacts [{:kind :jvm-bytecode-plan
                           :hash (str "sha256:" (sha256-hex (pr-str (:output optimized-mir))))}]]
    {:kind :gravity/stage0-c14-target-lowering-artifact
     :task "P06-D093" :document-set ["C14"]
     :governing-document (:c14-lowering-governing-document configuration)
     :pass {:name :c14-target-lowering :input :optimized-mir :output :target-artifact-manifest
            :requires [:c13-optimized-mir :profile :target :abi :runtime :providers :effects
                       :capabilities :safety :proofs]
            :preserves [:source-spans :origin-chain :profile :target :effects :capabilities
                        :safety :proofs :dependencies]
            :emits [:lowering-request :target-eligibility-report :abi-manifest
                    :runtime-provider-manifest :provider-selection-records :layout-decision-record
                    :proof-to-target-metadata-map :source-generated-origin-map
                    :unsupported-feature-report :target-artifact-manifest :lowering-diagnostic-stream]
            :rejects (:c14-lowering-diagnostic-ids configuration)}
     :source-overrides source-overrides
     :module (select-keys module [:module :source-path :profile :target :effects :capabilities :safety :metadata])
     :c13-optimization-artifact
     (select-keys optimization-artifact [:kind :task :artifact-id :governing-document
                                         :optimized-mir-artifact :capability-based-proof])
     :optimization-artifact-kind (:kind optimization-artifact)
     :optimization-artifact-hash input-id
     :lowering-request lowering-request
     :target-eligibility-report {:artifact :gravity/target-eligibility-report :status :eligible
                                 :profile :hosted :target target :backend :jvm
                                 :reason :profile-target-provider-compatible}
     :abi-manifest {:artifact :gravity/abi-manifest :status :complete :calling-convention :jvm-static
                    :exported-symbols ["compiler_c14_lowering_main"] :data-layout :jvm-object
                    :alignment :jvm-default :enum-representation :jvm-tagged-object
                    :closure-representation :jvm-function-object :panic-strategy :exception
                    :resource-handle-representation :jvm-object-ref :ffi-boundary :jvm-interop
                    :gc-policy :jvm-gc :debug-unwind :jvm-stacktrace}
     :runtime-provider-manifest {:artifact :gravity/runtime-provider-manifest :status :complete
                                 :runtime :hosted-jvm :providers (:providers lowering-request)}
     :provider-selection-records
     [{:provider :jvm/gc :capability :memory/allocator :effect :memory/allocate :status :selected}
      {:provider :jvm/stdout :capability :io/stdout :effect :io/write :status :selected}
      {:provider :jvm/exception :capability :panic/raise :effect :error/throw :status :selected}]
     :layout-decision-record {:artifact :gravity/layout-decision-record :status :complete
                              :alignment :jvm-default :proof :proof/c13-layout-ownership
                              :ownership-facts :mir/ownership-table :safety-facts :mir/safety-table}
     :proof-to-target-metadata-map proof-map
     :source-generated-origin-map {:artifact :gravity/source-generated-origin-map :status :complete
                                   :source-map (get-in optimization-artifact [:optimized-mir-artifact :source-origin-map])
                                   :generated-origin-map []}
     :capability-preservation-report {:artifact :gravity/capability-preservation-report :status :preserved
                                      :denied-additions [] :preserved-capabilities (:capabilities module)}
     :unsupported-feature-report
     [{:mir-op "c11-mir-op-gpu-kernel" :required-feature :gpu-kernel :backend :jvm :profile :hosted
       :source-span (source-span source-path 0) :available-alternatives [:mir-scalar-kernel]
       :fallback :mir-scalar-kernel :fallback-status :available :diagnostic-id "C14-UNSUPPORTED"}]
     :target-artifact-manifest
     {:artifact :gravity/target-artifact-manifest :input (:output optimized-mir) :backend :jvm
      :profile :hosted :target (str "sha256:" (sha256-hex (pr-str target))) :artifacts target-artifacts
      :source-map :gravity/source-generated-origin-map :proof-map :gravity/proof-target-metadata-map
      :effects :mir/effect-table :capabilities :mir/capability-proof-table :safety :mir/safety-table
      :runtime :gravity/runtime-provider-manifest :dependencies input-id :diagnostics []}
     :lowering-diagnostic-stream diagnostics
     :c14-lowering-results {:documents ["C14"] :task "P06-D093"
                            :required-diagnostic-ids (:c14-lowering-diagnostic-ids configuration)
                            :c13-input-status :complete :lowering-request-status :complete
                            :target-eligibility-status :complete :abi-status :complete
                            :runtime-provider-status :complete :proof-metadata-status :complete
                            :manifest-status :complete :diagnostic-status :complete :status :complete}
     :diagnostics []}))
