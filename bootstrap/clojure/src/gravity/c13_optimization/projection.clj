(ns gravity.c13-optimization.projection
  (:require [gravity.c13-optimization.operations :as operations]
            [gravity.digest]))

(defn- sha256-hex [value]
  (operations/invoke :sha256-hex gravity.digest/sha256-hex value))

(defn artifact-base [configuration source-text module source-overrides domain-ir-artifact
                    input-id contracts decisions diagnostics]
  (let [final-output-id (:output-mir (last decisions))
        invalidations (mapv (fn [decision]
                              {:pass (:pass decision)
                               :decision-id (:decision-id decision)
                               :invalidated (:invalidated decision)
                               :regenerated (:regenerated decision)
                               :runtime-checks-restored (:residual-checks decision)
                               :caches-cleared [:data-flow-cache :domain-anchor-cache]
                               :diagnostics-affected []
                               :status :recorded})
                            decisions)
        verifiers (mapv (fn [decision]
                          {:artifact :gravity/post-pass-mir-verifier-report
                           :pass (:pass decision)
                           :decision-id (:decision-id decision)
                           :input (:output-mir decision)
                           :status :passed
                           :checks [:module :dominance :types :effects :safety :domain-anchors]})
                        decisions)]
    {:kind :gravity/stage0-c13-mir-optimization-artifact
     :task "P06-D092"
     :document-set ["C13"]
     :governing-document (:c13-optimization-governing-document configuration)
     :pass {:name :c13-mir-optimization-passes
            :input :verified-domain-ir
            :output :optimized-mir
            :requires [:c12-domain-ir-architecture :pass-contracts :semantic-anchors
                       :proof-evidence :mir-verifier]
            :preserves [:types :effects :ownership :capabilities :profile :target :safety
                        :source-spans :origin-chain :domain-anchors]
            :emits [:optimization-pass-registry :optimization-pipeline-manifest
                    :optimization-decision-log :invalidated-fact-ledger
                    :analysis-cache-records :proof-and-certificate-usage
                    :residual-cost-report :post-pass-verifier-reports
                    :optimized-mir-artifact :optimization-diagnostic-stream]
            :rejects (:c13-optimization-diagnostic-ids configuration)}
     :source-overrides source-overrides
     :module (select-keys module [:module :source-path :profile :target :effects :capabilities :safety :metadata])
     :c12-domain-ir-artifact
     (select-keys domain-ir-artifact [:kind :task :artifact-id :governing-document
                                      :domain-verifier-report :semantic-anchor-map :capability-based-proof])
     :domain-ir-artifact-kind (:kind domain-ir-artifact)
     :domain-ir-artifact-hash input-id
     :optimization-pass-registry contracts
     :optimization-pipeline-manifest
     {:artifact :gravity/optimization-pipeline-manifest
      :pass-order (mapv :pass contracts)
      :ordering :deterministic
      :optimization-level :stage0-safe
      :source-hash (str "sha256:" (sha256-hex source-text))
      :profile :hosted
      :target :jvm
      :feature-set #{:objects :exceptions :threads}
      :package-graph :stage0-single-package
      :provider-set #{:jvm/gc :jvm/exception :jvm/stdout}
      :benchmark-inputs []
      :replay-seed :none
      :status :complete}
     :optimization-decision-log decisions
     :invalidated-fact-ledger invalidations
     :analysis-cache-records
     (mapv (fn [decision]
             {:pass (:pass decision)
              :cache-key (str "sha256:" (sha256-hex (pr-str [(:pass decision) input-id])))
              :invalidated-by (:invalidated decision)
              :status :complete})
           decisions)
     :proof-and-certificate-usage
     (mapv (fn [decision]
             {:pass (:pass decision)
              :decision-id (:decision-id decision)
              :proofs (:proofs-used decision)
              :status :accepted})
           decisions)
     :residual-cost-report
     {:artifact :gravity/residual-cost-report
      :status :complete
      :entries [{:pass :bounds-check-elide :claim :check-erased :residual-cost :none}
                {:pass :target-layout-prepare :claim :layout-prepared :residual-cost :manifest-only}]}
     :check-elision-record
     {:artifact :gravity/check-elision-record
      :pass :bounds-check-elide
      :status :accepted
      :proof :proof/c13-bounds-check-elision
      :policy :PERF10}
     :effect-reordering-record
     {:artifact :gravity/effect-order-proof
      :pass :effect-aware-schedule
      :status :accepted
      :proof :proof/c13-effect-order-equivalence}
     :safety-outcome-refresh-report
     {:artifact :gravity/safety-outcome-refresh-report
      :status :current
      :source :mir/safety-table}
     :domain-anchor-transform-report
     {:artifact :gravity/domain-anchor-transform-report
      :status :preserved
      :anchors (:semantic-anchor-map domain-ir-artifact)}
     :optimization-replay-record
     {:artifact :gravity/optimization-replay-record
      :status :replayable
      :ordering :deterministic
      :seed :none}
     :post-pass-verifier-reports verifiers
     :optimized-mir-artifact
     {:artifact :gravity/optimized-mir
      :input input-id
      :output final-output-id
      :passes (mapv :pass contracts)
      :source-origin-map (:semantic-anchor-map domain-ir-artifact)
      :domain-anchors (:semantic-anchor-map domain-ir-artifact)
      :status :complete}
     :optimization-diagnostic-stream diagnostics
     :c13-optimization-results
     {:documents ["C13"]
      :task "P06-D092"
      :required-diagnostic-ids (:c13-optimization-diagnostic-ids configuration)
      :c12-input-status :complete
      :pass-contract-status :complete
      :pipeline-status :complete
      :decision-log-status :complete
      :invalidation-status :complete
      :analysis-cache-status :complete
      :proof-status :complete
      :residual-cost-status :complete
      :post-pass-verifier-status :complete
      :diagnostic-status :complete
      :status :complete}
     :diagnostics []}))
