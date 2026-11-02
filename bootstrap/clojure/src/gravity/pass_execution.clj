(ns gravity.pass-execution
  "Compatibility facade for pure, non-authoritative pass execution evidence.

  Components own type-sensitive canonical identity, validation, receipt construction,
  and DAG composition. This namespace preserves the original public API and
  diagnostic interposition."
  (:require [gravity.pass-execution.config :as config]
            [gravity.pass-execution.contract :as contract]
            [gravity.pass-execution.dag :as dag]
            [gravity.pass-execution.diagnostics :as diagnostics]
            [gravity.pass-execution.evidence :as evidence]
            [gravity.pass-execution.receipt :as receipt]
            [gravity.pass-execution.receipt-validation :as receipt-validation]))

(def ^:dynamic ^:private *diagnostic-context* {})
(defn- fail!
  [id message data]
  (diagnostics/default-fail! id message data))

(defn validate-pass-contract!
  "Validate one exact C1/C16/C18 pass contract."
  [contract]
  (binding [diagnostics/*diagnostic-context* *diagnostic-context*
            diagnostics/*fail!* fail!]
    (contract/validate-pass-contract! contract)))

(defn canonical-pass-contract
  "Return the stable semantic projection used to identify a pass contract."
  [contract]
  (binding [diagnostics/*diagnostic-context* *diagnostic-context*
            diagnostics/*fail!* fail!]
    (contract/canonical-pass-contract contract)))

(defn pass-contract-id
  "Return the content identity of one validated pass contract."
  [contract]
  (binding [diagnostics/*diagnostic-context* *diagnostic-context*
            diagnostics/*fail!* fail!]
    (contract/pass-contract-id contract)))

(defn execute-pass!
  "Execute and validate one injected pass exactly once, then emit its receipt."
  [request operations]
  (binding [diagnostics/*diagnostic-context* *diagnostic-context*
            diagnostics/*fail!* fail!]
    (receipt/execute-pass! request operations)))

(defn validate-execution-receipt!
  "Revalidate one receipt and invoke each supplied evidence validator once."
  [receipt contract operations]
  (binding [diagnostics/*diagnostic-context* *diagnostic-context*
            diagnostics/*fail!* fail!]
    (receipt-validation/validate-execution-receipt!
     receipt contract operations)))

(defn compose-evidence-dag
  "Validate and compose an order-invariant evidence DAG of pass receipts."
  [receipts contracts]
  (binding [diagnostics/*diagnostic-context* *diagnostic-context*
            diagnostics/*fail!* fail!]
    (dag/compose-evidence-dag receipts contracts)))

(defn evidence-root
  "Exact-validate, recompose, and return a pass evidence DAG semantic root."
  [dag]
  (binding [diagnostics/*diagnostic-context* *diagnostic-context*
            diagnostics/*fail!* fail!]
    (evidence/evidence-root dag)))

(defn pass-execution-contract
  "Return the private machine contract for this non-authoritative leaf."
  []
  config/namespace-contract)
