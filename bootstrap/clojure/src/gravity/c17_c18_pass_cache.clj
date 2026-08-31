(ns gravity.c17-c18-pass-cache
  "Non-authoritative generic-v2 cache continuation for C17 and C18.

  This adapter accepts the validated C15->C16 cache result, runs or reuses the
  real C17 and C18 producers, and recomposes all four producer receipts into
  one typed evidence DAG. It owns no plugin, verifier, proof, release, or
  self-hosting authority."
  (:require [gravity.c17-c18-pass-cache.cache-operation :as cache-operation]
            [gravity.c17-c18-pass-cache.contract :as contract]
            [gravity.c17-c18-pass-cache.envelope :as envelope]
            [gravity.c17-c18-pass-cache.orchestration :as orchestration]
            [gravity.c17-c18-pass-cache.request :as request-builder]
            [gravity.c17-c18-pass-cache.upstream-boundary :as upstream]
            [gravity.c17-c18-pass-cache.validation :as validation]
            [gravity.pass-cache :as pass-cache]
            [gravity.pass-execution :as pass-execution]))

(defn c17-stage-request
  "Build the exact C17 request from a validated C15->C16 result."
  [context upstream-result]
  (let [upstream-result
        (upstream/validate-upstream!
         {:evidence-root pass-execution/evidence-root} upstream-result)
        receipt (:c16-producer-receipt upstream-result)]
    (request-builder/build
     {:validate-context! validation/validate-context!
      :require-sha256! validation/require-sha256!
      :stage-cache-key pass-cache/stage-cache-key}
     context :c17 contract/c17-pass-contract
     (:output-artifact-id receipt) (:output-facts receipt))))

(defn c18-stage-request
  "Build the exact C18 request consuming one current C17 receipt."
  [context c17-receipt]
  (let [context (validation/validate-context! context)]
    (upstream/validate-c17-receipt!
     {:validate-execution-receipt! pass-execution/validate-execution-receipt!}
     context c17-receipt)
    (request-builder/build
     {:validate-context! validation/validate-context!
      :require-sha256! validation/require-sha256!
      :stage-cache-key pass-cache/stage-cache-key}
     context :c18 contract/c18-pass-contract
     (:output-artifact-id c17-receipt) (:output-facts c17-receipt))))

(defn lookup-or-compute!
  "Run or reuse C17/C18 and compose them with the validated upstream DAG."
  [store upstream-result context operations]
  (orchestration/lookup-or-compute!
   {:validate-upstream!
    #(upstream/validate-upstream!
      {:evidence-root pass-execution/evidence-root} %)
    :validate-context! validation/validate-context!
    :validate-operations! validation/validate-operations!
    :c17-stage-request c17-stage-request
    :c18-stage-request c18-stage-request
    :stage-cache-key pass-cache/stage-cache-key
    :cache-lookup-or-compute! pass-cache/lookup-or-compute!
    :stage-cache-operations cache-operation/stage-cache-operations
    :decode-envelope! envelope/decode!
    :compose-evidence-dag pass-execution/compose-evidence-dag
    :evidence-root pass-execution/evidence-root}
   store upstream-result context operations))

(defn c17-c18-pass-cache-contract
  "Return the exact non-authoritative downstream adapter contract."
  []
  contract/namespace-contract)
