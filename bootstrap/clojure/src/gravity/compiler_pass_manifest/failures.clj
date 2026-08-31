(ns gravity.compiler-pass-manifest.failures
  "Structured compiler pass manifest diagnostic construction."
  (:require [gravity.diagnostics :as diagnostics]))

(defn compiler-pass-fail!
  [id source-path manifest record extra]
  (diagnostics/fail! id
         (case id
           "C1-PIPELINE" "compiler pipeline order does not expose the canonical stages"
           "C1-PASS-CONTRACT" "compiler pass contract is incomplete"
           "C1-EVIDENCE-DROP" "compiler pass drops durable evidence without replacement"
           "C1-UNCHECKED-BACKEND" "target lowering consumes unchecked compiler input"
           "C1-MANIFEST" "compiler pipeline manifest is missing required graph fields"
           "C15-SCHEMA" "compiler diagnostic schema is malformed"
           "C15-ID" "compiler diagnostic ids are unstable or duplicate"
           "C15-SPAN" "compiler diagnostic lacks a primary span"
           "C15-ORIGIN" "generated diagnostic lacks an origin chain"
           "C15-FACTS" "compiler diagnostic lacks structured facts"
           "C15-REMEDIATION" "actionable compiler diagnostic lacks remediation"
           "C15-REDACTION" "compiler diagnostic leaks private or secret material"
           "C15-ORDER" "compiler diagnostic stream order is nondeterministic"
           "C16-KEY" "incremental cache key is incomplete"
           "C16-ENTRY" "incremental cache entry is incomplete"
           "C16-PROOF" "stale proof or certificate was reused"
           "C16-SPECULATIVE" "speculative cache reuse reached a publishable boundary"
           "C17-MANIFEST" "compiler plugin manifest is incomplete"
           "C17-API" "compiler plugin API version is incompatible"
           "C17-CAPABILITY" "compiler plugin capability scope is missing or excessive"
           "C17-PASS-CONTRACT" "compiler plugin pass contract is invalid"
           "C17-OUTPUT" "compiler plugin output failed verification"
           "C18-RISK" "compiler pass risk classification is missing"
           "C18-EVIDENCE" "compiler pass lacks required correctness evidence"
           "C18-TRUST-REPORT" "compiler trust report omits a pass"
           "C18-RELEASE-GATE" "compiler release gate passed despite evidence gaps"
           "compiler pass manifest record is invalid")
         (merge {:source-span {:source source-path}
                 :profile (or (:profile record) (:profile manifest))
                 :target (or (:target record) (:target manifest))
                 :stage (or (:stage record) (:pass record))
                 :pass-id (:pass record)
                 :artifact-id (or (:artifact-id record) (:artifact record))
                 :input-artifact-id (:input record)
                 :output-artifact-id (:output record)
                 :plugin-id (:plugin record)
                 :package-id (get-in record [:package :name])
                 :compiler-api-version (:api-version record)
                 :trust-level (:trust record)
                 :cache-key (:cache-key record)
                 :risk-class (:risk record)
                 :available-evidence (:available-evidence record)
                 :required-evidence (:minimum-evidence record)
                 :affected-profiles (:affected-profiles record)
                 :affected-targets (:affected-targets record)
                 :release-gate (:release-gate record)
                 :diagnostic-family :compiler-pass-contract}
                extra)))

(defn compiler-pass-missing-fields
  [record required-fields]
  (vec (remove #(and (contains? record %) (some? (get record %)))
               required-fields)))
