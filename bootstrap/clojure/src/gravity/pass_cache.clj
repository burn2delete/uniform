(ns gravity.pass-cache
  "Generic local compiler-pass cache with receipt-first revalidation.

  The cache is deliberately a hosted Stage0 leaf.  It stores immutable,
  content-addressed artifacts and pass execution receipts for local or
  speculative reuse; it never grants release, proof, equivalence, or
  self-hosting authority."
  (:require [gravity.pass-cache.canonical-decode :as canonical-decode]
            [gravity.pass-cache.canonical-encode :as canonical-encode]
            [gravity.pass-cache.entry-schema :as entry-schema]
            [gravity.pass-cache.execution :as execution]
            [gravity.pass-cache.lookup :as lookup]
            [gravity.pass-cache.path-policy :as path-policy]
            [gravity.pass-cache.policy :as policy]
            [gravity.pass-cache.publication :as publication]
            [gravity.pass-cache.receipt-validation :as receipt-validation]
            [gravity.pass-cache.request-key :as request-key]
            [gravity.pass-cache.store-open :as store-open]))

;; These private compatibility vars remain observable to the existing hostile
;; filesystem tests. Public entrypoints bind their current values into the leaf
;; policy so with-redefs and subprocess crash hooks retain their exact behavior.
(def ^:private maximum-depth policy/maximum-depth)
(def ^:private maximum-nodes policy/maximum-nodes)
(def ^:private maximum-file-bytes policy/maximum-file-bytes)
(def ^:private maximum-canonical-bytes policy/maximum-canonical-bytes)
(def ^:private maximum-entry-bytes policy/maximum-entry-bytes)
(def ^:private maximum-blob-bytes policy/maximum-blob-bytes)
(def ^:private maximum-entry-count policy/maximum-entry-count)
(def ^:private maximum-blob-count policy/maximum-blob-count)
(def ^:private maximum-receipt-count policy/maximum-receipt-count)
(def ^:private maximum-lock-count policy/maximum-lock-count)
(def ^:private maximum-staging-count policy/maximum-staging-count)
(def ^:private maximum-store-bytes policy/maximum-store-bytes)
(def ^:private active-staging policy/active-staging)
(def ^:dynamic ^:private *publication-hook* nil)

(defmacro ^:private with-compatibility-policy
  [& body]
  `(binding [policy/maximum-depth maximum-depth
             policy/maximum-nodes maximum-nodes
             policy/maximum-file-bytes maximum-file-bytes
             policy/maximum-canonical-bytes maximum-canonical-bytes
             policy/maximum-entry-bytes maximum-entry-bytes
             policy/maximum-blob-bytes maximum-blob-bytes
             policy/maximum-entry-count maximum-entry-count
             policy/maximum-blob-count maximum-blob-count
             policy/maximum-receipt-count maximum-receipt-count
             policy/maximum-lock-count maximum-lock-count
             policy/maximum-staging-count maximum-staging-count
             policy/maximum-store-bytes maximum-store-bytes
             policy/*publication-hook* *publication-hook*]
     ~@body))

(defn pass-cache-contract
  "Return the non-authoritative ownership and dependency contract."
  []
  policy/pass-cache-contract-record)

(defn- canonical-bytes
  ([value]
   (with-compatibility-policy
     (canonical-encode/canonical-bytes value)))
  ([value byte-limit]
   (with-compatibility-policy
     (canonical-encode/canonical-bytes value byte-limit))))

(defn- encoded-value
  [value byte-limit]
  (with-compatibility-policy
    (canonical-decode/encoded-value value byte-limit)))

(defn- entry-id
  [entry]
  (with-compatibility-policy
    (receipt-validation/entry-id entry)))

(defn- blob-id
  [bytes]
  (with-compatibility-policy
    (receipt-validation/blob-id bytes)))

(defn- validate-entry-record!
  [key entry]
  (with-compatibility-policy
    (entry-schema/validate-entry-record! key entry)))

(defn- relative-name!
  [value]
  (with-compatibility-policy
    (path-policy/relative-name! value)))

(defn stage-cache-key
  "Build the exact, bounded semantic cache key for one pass request."
  [request]
  (with-compatibility-policy
    (request-key/stage-cache-key request)))

(defn open-local-store
  "Open the explicit local cache below exactly `.cpcache/compiler-pass/v2`."
  [base-path]
  (with-compatibility-policy
    (store-open/open-local-store base-path)))

(defn lookup!
  "Read and fully revalidate one immutable cache entry, if present."
  [store key validation-ops]
  (with-compatibility-policy
    (lookup/lookup! store key validation-ops)))

(defn store!
  "Validate and immutably publish an accepted artifact and producer receipt."
  [store key artifact producer-receipt validation-ops]
  (with-compatibility-policy
    (publication/store! store key artifact producer-receipt validation-ops)))

(defn lookup-or-compute!
  "Reuse a fully revalidated hit, or execute one producer pass and publish it."
  [store key execution-request operations]
  (with-compatibility-policy
    (execution/lookup-or-compute!
     store key execution-request operations)))
