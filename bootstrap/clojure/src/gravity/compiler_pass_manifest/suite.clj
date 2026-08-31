(ns gravity.compiler-pass-manifest.suite
  "Manifest override assembly for the compiler pass contract suite."
  (:require [gravity.compiler-pass-manifest.contracts :as contracts]
            [gravity.compiler-pass-manifest.diagnostics :as diagnostic-data]
            [gravity.compiler-pass-manifest.incremental :as incremental]
            [gravity.compiler-pass-manifest.plugins :as plugins]
            [gravity.compiler-pass-manifest.verification :as verification]))

(defn compiler-pass-merge-record-overrides
  [defaults overrides id-key]
  (if (seq overrides)
    (let [by-id (into {} (map (juxt id-key identity) overrides))]
      (mapv #(merge % (get by-id (get % id-key) {})) defaults))
    defaults))

(defn compiler-pass-suite
  [manifest]
  (let [source-suite (get-in manifest [:metadata :compiler :passes] {})
        map-value (fn [key override-key default]
                    (cond
                      (contains? source-suite key) (get source-suite key)
                      (contains? source-suite override-key)
                      (merge default (get source-suite override-key))
                      :else default))
        vector-value (fn [key override-key defaults id-key]
                       (cond
                         (contains? source-suite key) (vec (get source-suite key))
                         (contains? source-suite override-key)
                         (compiler-pass-merge-record-overrides
                          defaults (get source-suite override-key) id-key)
                         :else defaults))
        contracts (vector-value :contracts :contract-overrides
                                contracts/compiler-pass-default-contracts :pass)
        risk-records (vector-value :risk-classification :risk-overrides
                                   (verification/compiler-pass-default-risk-classification
                                    contracts)
                                   :pass)
        trust-report (map-value :compiler-trust-report
                                :compiler-trust-report-overrides
                                (verification/compiler-pass-default-trust-report
                                 contracts risk-records))]
    (assoc source-suite
           :stage-order
           (or (:stage-order source-suite) contracts/compiler-pass-default-stage-order)
           :contracts contracts
           :pipeline-manifest
           (map-value :pipeline-manifest :pipeline-manifest-overrides
                      {:artifact :gravity/compiler-pipeline
                       :pipeline-id "sha256:stage0-compiler-pipeline"
                       :compiler :gravity-stage0-clojure-bootstrap
                       :source-root "sha256:stage0-source-root"
                       :profile :meta
                       :target {:backend :jvm :triple "stage0"}
                       :stages (:stage-order source-suite
                                             contracts/compiler-pass-default-stage-order)
                       :pass-contracts (mapv :pass contracts)
                       :evidence [:types :effects :ownership :capabilities
                                  :safety :proofs :diagnostics]
                       :diagnostics "sha256:stage0-compiler-diagnostics"
                       :artifact-graph "sha256:stage0-artifact-graph"})
           :diagnostic-schema
           (map-value :diagnostic-schema :diagnostic-schema-overrides
                      diagnostic-data/compiler-pass-default-diagnostic-schema)
           :diagnostic-catalog
           (vector-value :diagnostic-catalog :diagnostic-catalog-overrides
                         diagnostic-data/compiler-pass-default-diagnostic-catalog :rule)
           :diagnostic-fixtures
           (vector-value :diagnostic-fixtures :diagnostic-fixture-overrides
                         diagnostic-data/compiler-pass-default-diagnostic-fixtures
                         :diagnostic-id)
           :cache-key-schema
           (map-value :cache-key-schema :cache-key-schema-overrides
                      incremental/compiler-pass-default-cache-key-schema)
           :cache-keys
           (vector-value :cache-keys :cache-key-overrides
                         incremental/compiler-pass-default-cache-keys :stage)
           :cache-entries
           (vector-value :cache-entries :cache-entry-overrides
                         incremental/compiler-pass-default-cache-entries :stage)
           :proof-reuse-records
           (vector-value :proof-reuse-records :proof-reuse-overrides
                         incremental/compiler-pass-default-proof-reuse-records :proof-id)
           :speculative-reuse-records
           (vector-value :speculative-reuse-records
                         :speculative-reuse-overrides
                         incremental/compiler-pass-default-speculative-reuse-records
                         :artifact-id)
           :plugin-manifest
           (map-value :plugin-manifest :plugin-manifest-overrides
                      plugins/compiler-pass-default-plugin-manifest)
           :plugin-pass-contracts
           (vector-value :plugin-pass-contracts
                         :plugin-pass-contract-overrides
                         plugins/compiler-pass-default-plugin-pass-contracts :pass)
           :plugin-execution-traces
           (vector-value :plugin-execution-traces
                         :plugin-execution-trace-overrides
                         plugins/compiler-pass-default-plugin-execution-traces :pass)
           :risk-classification risk-records
           :compiler-trust-report trust-report
           :release-gate-report
           (map-value :release-gate-report :release-gate-report-overrides
                      verification/compiler-pass-default-release-gate-report))))
