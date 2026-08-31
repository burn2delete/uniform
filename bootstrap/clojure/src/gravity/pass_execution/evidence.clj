(ns gravity.pass-execution.evidence
  "Exact evidence-DAG envelope validation and semantic root recovery."
  (:require [gravity.pass-execution.canonical :as canonical]
            [gravity.pass-execution.config :as config]
            [gravity.pass-execution.dag :as dag]
            [gravity.pass-execution.diagnostics :as diagnostics]
            [gravity.pass-execution.validation :as validation]))

(defn evidence-root
  "Exact-validate, recompose, and return a pass evidence DAG semantic root."
  [value]
  (validation/exact-map! value config/evidence-dag-fields "C16-ENTRY"
                         :evidence-dag)
  ;; Reject metadata and unsupported nested values before recomposition.
  (canonical/canonical-bytes value)
  (when-not (and (= :gravity/pass-evidence-dag (:artifact value))
                 (= 1 (:schema-version value))
                 (validation/sha256-id? (:root-receipt-id value))
                 (validation/sha256-id? (:evidence-root-id value))
                 (vector? (:receipts value))
                 (vector? (:contracts value))
                 (vector? (:edges value)))
    (diagnostics/fail! "C16-ENTRY"
                       "pass evidence DAG envelope is malformed" {}))
  (validation/exact-map! (:authority value)
                         config/evidence-dag-authority-fields
                         "C16-POLICY" :evidence-dag-authority)
  (doseq [edge (:edges value)]
    (validation/exact-map! edge #{:from :to} "D1-ARTIFACT-GAP"
                           :evidence-edge)
    (validation/require-sha256! :edge-from (:from edge))
    (validation/require-sha256! :edge-to (:to edge)))
  (let [recomposed (dag/compose-evidence-dag (:receipts value)
                                             (:contracts value))]
    (when-not (= recomposed value)
      (diagnostics/fail!
       "C16-STALE"
       "pass evidence DAG differs from exact canonical recomposition"
       {:observed-root (:evidence-root-id value)
        :expected-root (:evidence-root-id recomposed)}))
    (:evidence-root-id recomposed)))
