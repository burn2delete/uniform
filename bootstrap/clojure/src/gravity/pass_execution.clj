(ns gravity.pass-execution
  "Compatibility facade for pure, non-authoritative pass execution evidence.

  Components own bounded canonical identity, validation, receipt construction,
  and DAG composition. This namespace preserves the original public API and
  private test seams."
  (:require [gravity.pass-execution.canonical :as canonical]
            [gravity.pass-execution.config :as config]
            [gravity.pass-execution.contract :as contract]
            [gravity.pass-execution.dag :as dag]
            [gravity.pass-execution.diagnostics :as diagnostics]
            [gravity.pass-execution.evidence :as evidence]
            [gravity.pass-execution.receipt :as receipt]
            [gravity.pass-execution.receipt-validation :as receipt-validation]
            [gravity.pass-execution.request :as request]
            [gravity.pass-execution.validation :as validation]))

;; Preserve every original private Var for leaf-level seam discovery.
(def ^:private maximum-depth config/maximum-depth)
(def ^:private maximum-nodes config/maximum-nodes)
(def ^:private maximum-canonical-bytes config/maximum-canonical-bytes)
(def ^:private maximum-integer-bits config/maximum-integer-bits)
(def ^:private maximum-evidence-records config/maximum-evidence-records)
(def ^:private maximum-dag-receipts config/maximum-dag-receipts)
(def ^:private sha256-pattern config/sha256-pattern)
(def ^:private pass-contract-fields config/pass-contract-fields)
(def ^:private semantic-binding-fields config/semantic-binding-fields)
(def ^:private provenance-fields config/provenance-fields)
(def ^:private request-authority-fields config/request-authority-fields)
(def ^:private external-root-fields config/external-root-fields)
(def ^:private execution-request-fields config/execution-request-fields)
(def ^:private execute-operation-fields config/execute-operation-fields)
(def ^:private receipt-validation-operation-fields
  config/receipt-validation-operation-fields)
(def ^:private verifier-report-fields config/verifier-report-fields)
(def ^:private evidence-record-fields config/evidence-record-fields)
(def ^:private receipt-authority-fields config/receipt-authority-fields)
(def ^:private receipt-fields config/receipt-fields)
(def ^:private authority-rank config/authority-rank)
(def ^:private evidence-dag-fields config/evidence-dag-fields)
(def ^:private evidence-dag-authority-fields
  config/evidence-dag-authority-fields)
(def ^:private public-api config/public-api)
(def ^:private namespace-contract config/namespace-contract)
(def ^:dynamic ^:private *diagnostic-context* {})
(defn- fail!
  [id message data]
  (diagnostics/default-fail! id message data))
(def ^{:private true :arglists '([value fields id label])}
  exact-map! validation/exact-map!)
(def ^{:private true :arglists '([value])} sha256-id? validation/sha256-id?)
(def ^{:private true :arglists '([field value])}
  require-sha256! validation/require-sha256!)
(def ^{:private true :arglists '([field value predicate])}
  distinct-vector! validation/distinct-vector!)
(def ^{:private true :arglists '([field value])}
  sorted-sha-vector! validation/sorted-sha-vector!)
(def ^{:private true :arglists '([value])} integral-tag canonical/integral-tag)
(def ^{:private true :arglists '([values])} canonical-sort canonical/canonical-sort)
(def ^{:private true :arglists '([text])}
  bounded-utf8-size canonical/bounded-utf8-size)
(def ^{:private true :arglists '([text])}
  escaped-string-byte-bound canonical/escaped-string-byte-bound)
(def ^{:private true :arglists '([value])}
  arbitrary-integer-bit-length
  canonical/arbitrary-integer-bit-length)
(def ^{:private true :arglists '([value])} integer-decimal-byte-bound!
  canonical/integer-decimal-byte-bound!)
(def ^{:private true :arglists '([state byte-bound])}
  preflight-account! canonical/preflight-account!)
(def ^{:private true :arglists '([value])} preflight-container-cardinality!
  canonical/preflight-container-cardinality!)
(def ^{:private true :arglists '([value state depth])}
  preflight-value! canonical/preflight-value!)
(def ^{:private true :arglists '([value])}
  preflight-canonical! canonical/preflight-canonical!)
(def ^{:private true :arglists '([state byte-estimate])}
  account-canonical! canonical/account-canonical!)
(def ^{:private true :arglists '([value state depth])}
  canonical-node canonical/canonical-node)
(def ^{:private true :arglists '([value])} canonical-bytes canonical/canonical-bytes)
(def ^{:private true :arglists '([domain value])} content-id canonical/content-id)
(def ^{:private true :arglists '([field value])} keyword-set! validation/keyword-set!)
(def ^{:private true :arglists '([field value])}
  authority-level! validation/authority-level!)
(def ^{:private true :arglists '([levels])}
  weakest-authority validation/weakest-authority)
(def ^{:private true :arglists '([operations expected id])}
  validate-operations! validation/validate-operations!)
(def ^{:private true :arglists '([bindings])} validate-semantic-bindings!
  validation/validate-semantic-bindings!)
(def ^{:private true :arglists '([provenance])}
  validate-provenance! validation/validate-provenance!)
(def ^{:private true :arglists '([authority ceiling input-artifact-ids])}
  validate-request-authority!
  validation/validate-request-authority!)
(def ^{:private true :arglists '([input-artifact-ids])}
  validate-input-artifact-ids!
  validation/validate-input-artifact-ids!)
(def ^{:private true
       :arglists '([external-root-inputs input-artifact-ids input-facts input-kind])}
  validate-external-root-inputs!
  validation/validate-external-root-inputs!)
(def ^{:private true :arglists '([request])}
  validate-request! request/validate-request!)
(def ^{:private true :arglists '([report output-id stage])}
  validate-verifier-report-shape!
  validation/validate-verifier-report-shape!)
(def ^{:private true :arglists '([record output-id])}
  validate-evidence-record-shape!
  validation/validate-evidence-record-shape!)
(def ^{:private true :arglists '([contract input-facts])}
  output-facts receipt/output-facts)
(def ^{:private true :arglists '([receipt])}
  receipt-id-projection receipt/receipt-id-projection)
(def ^{:private true :arglists '([receipt])}
  calculated-receipt-id receipt/calculated-receipt-id)
(def ^{:private true :arglists '([receipt contract])}
  validate-receipt-structure!
  receipt-validation/validate-receipt-structure!)
(def ^{:private true :arglists '([edges])} detect-cycle dag/detect-cycle)
(def ^{:private true :arglists '([dag])} dag-id-projection dag/dag-id-projection)

(defn validate-pass-contract!
  "Validate one exact, bounded C1/C16/C18 pass contract."
  [contract]
  (binding [diagnostics/*diagnostic-context* *diagnostic-context*
            diagnostics/*fail!* fail!]
    (gravity.pass-execution.contract/validate-pass-contract! contract)))

(defn canonical-pass-contract
  "Return the stable semantic projection used to identify a pass contract."
  [contract]
  (binding [diagnostics/*diagnostic-context* *diagnostic-context*
            diagnostics/*fail!* fail!]
    (gravity.pass-execution.contract/canonical-pass-contract contract)))

(defn pass-contract-id
  "Return the content identity of one validated pass contract."
  [contract]
  (binding [diagnostics/*diagnostic-context* *diagnostic-context*
            diagnostics/*fail!* fail!]
    (gravity.pass-execution.contract/pass-contract-id contract)))

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
  namespace-contract)
