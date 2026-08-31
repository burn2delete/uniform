(ns gravity.c6-core-lowering.artifact
  "Assembly of the complete hosted Stage0 C6 lowering artifact."
  (:require [gravity.c6-core-lowering.config :as config]
            [gravity.c6-core-lowering.context :as context]
            [gravity.c6-core-lowering.diagnostics :as diagnostics]
            [gravity.c6-core-lowering.lowering :as lowering]
            [gravity.c6-core-lowering.projection :as projection]
            [gravity.c6-core-lowering.verification :as verification]))

(defn c6-lowering-artifact [source-path module c5-artifact expanded-stream]
  (let [overrides
        (context/invoke-op :c6-lowering-source-overrides
                           diagnostics/c6-lowering-source-overrides module)
        _ (context/invoke-op :c6-lowering-validate-overrides!
                             diagnostics/c6-lowering-validate-overrides!
                             source-path module overrides)
        body-syntax (remove #(context/ns-form? (:form %)) expanded-stream)
        domain-boundaries
        (context/invoke-op :c6-domain-boundary-records
                           projection/c6-domain-boundary-records
                           module body-syntax c5-artifact)
        counter (atom 0)
        roots (vec (keep #(context/invoke-op
                           :c6-lower-form lowering/c6-lower-form
                           counter module % (:form %))
                         body-syntax))
        flat (vec (mapcat #(context/invoke-op
                            :c6-flatten-core lowering/c6-flatten-core %)
                          roots))
        surface-map
        (context/invoke-op :c6-surface-to-core-map
                           projection/c6-surface-to-core-map
                           roots domain-boundaries)
        trace (context/invoke-op :c6-desugaring-trace
                                 projection/c6-desugaring-trace roots)
        evaluation
        (context/invoke-op :c6-evaluation-order-records
                           projection/c6-evaluation-order-records flat)
        verifier
        (context/invoke-op :c6-core-verifier-report
                           verification/c6-core-verifier-report
                           flat domain-boundaries c5-artifact)
        invalidation
        (context/invoke-op :c6-rule-invalidation-record
                           projection/c6-rule-invalidation-record roots)
        diagnostic-ids (context/op-value :c6-lowering-diagnostic-ids
                                         config/c6-lowering-diagnostic-ids)
        rejected-designs (context/op-value :c6-lowering-rejected-designs
                                           config/c6-lowering-rejected-designs)
        artifact-base
        {:kind :gravity/stage0-c6-core-lowering-artifact
         :task "P06-D085"
         :document-set ["C6"]
         :governing-document
         (context/op-value :c6-lowering-governing-document
                           config/c6-lowering-governing-document)
         :pass {:name :c6-ast-and-core-lowering
                :input :c5-namespace-analysis
                :output :verified-core-ast
                :requires [:expanded-syntax-stream :binding-table
                           :namespace-analysis :profile :target]
                :preserves [:source-spans :generated-origin :metadata
                            :namespace-context :profile :effects
                            :capabilities :unsafe-metadata]
                :emits [:core-ast-module :surface-to-core-map
                        :desugaring-trace :evaluation-order-records
                        :domain-boundary-records :core-verifier-report
                        :core-lowering-diagnostics]
                :rejects diagnostic-ids}
         :source-overrides overrides
         :module (select-keys module
                              [:module :source-path :profile :target :effects
                               :capabilities :safety :metadata])
         :c5-name-resolution-artifact
         (select-keys c5-artifact
                      [:kind :artifact-id :namespace-analysis :binding-table
                       :alias-table :dependency-graph])
         :core-ast-module
         {:artifact :gravity/core-ast-module
          :module (:module module)
          :roots (mapv :node-id roots)
          :node-count (count flat)
          :domain-boundaries (mapv :domain domain-boundaries)
          :status :complete}
         :core-node-table flat
         :surface-to-core-map surface-map
         :desugaring-trace trace
         :evaluation-order-records evaluation
         :domain-boundary-records domain-boundaries
         :core-verifier-report verifier
         :lowering-rule-invalidation invalidation
         :preserved-declarations {:effects (:effects module)
                                  :capabilities (:capabilities module)
                                  :profile (:profile module)
                                  :target (:target module)}
         :core-lowering-diagnostics
         {:artifact :gravity/c6-core-lowering-diagnostics
          :required-diagnostic-ids diagnostic-ids
          :covered rejected-designs
          :status :complete}
         :rejected-design-coverage rejected-designs
         :diagnostics []}
        _ (context/invoke-op :c6-lowering-validate!
                             verification/c6-lowering-validate!
                             source-path artifact-base)
        capability-proof
        (context/invoke-op :c6-lowering-capability-proof
                           verification/c6-lowering-capability-proof
                           artifact-base)
        conformance
        {:documents ["C6"]
         :task "P06-D085"
         :required-diagnostic-ids diagnostic-ids
         :core-ast-status :complete
         :surface-map-status :complete
         :desugaring-trace-status :complete
         :evaluation-order-status :complete
         :domain-boundary-status :complete
         :core-verifier-status :passed
         :diagnostic-status :complete
         :invalidation-status :stable
         :status :complete}
        artifact (assoc artifact-base
                        :capability-based-proof capability-proof
                        :c6-lowering-results conformance)]
    (assoc artifact :artifact-id (context/c4-artifact-id artifact))))
