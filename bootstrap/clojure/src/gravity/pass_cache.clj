(ns gravity.pass-cache
  "Generic local compiler-pass cache with receipt-first revalidation.

  The cache is deliberately a hosted Stage0 leaf.  It stores immutable,
  content-addressed artifacts and pass execution receipts for local or
  speculative reuse; it never grants release, proof, equivalence, or
  self-hosting authority."
  (:require [gravity.pass-cache.execution :as execution]
            [gravity.pass-cache.lookup :as lookup]
            [gravity.pass-cache.policy :as policy]
            [gravity.pass-cache.publication :as publication]
            [gravity.pass-cache.request-key :as request-key]
            [gravity.pass-cache.store-open :as store-open]))

(defn pass-cache-contract
  "Return the non-authoritative ownership and dependency contract."
  []
  policy/pass-cache-contract-record)

(defn stage-cache-key
  "Build the exact semantic cache key for one pass request."
  [request]
  (request-key/stage-cache-key request))

(defn open-local-store
  "Open the explicit local cache below exactly `.cpcache/compiler-pass/v2`."
  [base-path]
  (store-open/open-local-store base-path))

(defn lookup!
  "Read and fully revalidate one immutable cache entry, if present."
  [store key validation-ops]
  (lookup/lookup! store key validation-ops))

(defn store!
  "Validate and immutably publish an accepted artifact and producer receipt."
  [store key artifact producer-receipt validation-ops]
  (publication/store! store key artifact producer-receipt validation-ops))

(defn lookup-or-compute!
  "Reuse a fully revalidated hit, or execute one producer pass and publish it."
  [store key execution-request operations]
  (execution/lookup-or-compute!
   store key execution-request operations))
