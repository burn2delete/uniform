(ns gravity.c15-c16-pass-cache
  "Non-authoritative generic-v2 cache integration for the adjacent C15 and
  C16 Stage0 passes.

  Callers supply the real pass producers, validators, and artifact identity
  function.  This namespace owns only the exact C16 request projection, cache
  orchestration, and composition of the two producer receipts.  It cannot
  establish compiler, diagnostic, proof, release, or self-hosting authority."
  (:require [gravity.c15-c16-pass-cache.contract :as contract]
            [gravity.c15-c16-pass-cache.envelope :as envelope]
            [gravity.c15-c16-pass-cache.orchestration :as orchestration]
            [gravity.c15-c16-pass-cache.request :as request-builder]
            [gravity.c15-c16-pass-cache.validation :as validation]
            [gravity.pass-cache :as pass-cache]
            [gravity.pass-execution :as pass-execution]))

(defn c15-stage-request
  "Build and validate the exact C15 generic-pass request."
  [context]
  (request-builder/build
   {:validate-context! validation/validate-context!
    :require-sha256! validation/require-sha256!
    :stage-cache-key pass-cache/stage-cache-key}
   context :c15 contract/c15-pass-contract (:c14-artifact-id context)
   contract/c15-input-facts true))

(defn c16-stage-request
  "Build and validate the exact C16 request consuming one C15 artifact.

  The C15 input is deliberately not an external root: when both receipts are
  composed, it must resolve to the C15 producer and form a typed internal edge."
  [context c15-artifact-id]
  (request-builder/build
   {:validate-context! validation/validate-context!
    :require-sha256! validation/require-sha256!
    :stage-cache-key pass-cache/stage-cache-key}
   context :c16 contract/c16-pass-contract c15-artifact-id
   contract/c15-output-facts false))

(defn lookup-or-compute!
  "Reuse or execute the adjacent C15 and C16 passes and compose their receipts.

  The returned evidence DAG is explicitly non-authoritative.  C16 consumes the
  exact C15 artifact id, so a changed C15 output invalidates the C16 key and the
  composed DAG contains one real, typed internal edge."
  [store context operations]
  (orchestration/lookup-or-compute!
   {:validate-context! validation/validate-context!
    :validate-operations! validation/validate-operations!
    :c15-stage-request c15-stage-request
    :c16-stage-request c16-stage-request
    :stage-cache-key pass-cache/stage-cache-key
    :cache-lookup-or-compute! pass-cache/lookup-or-compute!
    :stage-cache-operations envelope/stage-cache-operations
    :decode-envelope! envelope/decode!
    :compose-evidence-dag pass-execution/compose-evidence-dag
    :evidence-root pass-execution/evidence-root
    :c15-pass-contract contract/c15-pass-contract
    :c16-pass-contract contract/c16-pass-contract}
   store context operations))

(defn c15-c16-pass-cache-contract
  "Return the exact non-authoritative adapter contract."
  []
  contract/namespace-contract)
