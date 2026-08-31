

;; SH-07 authoritative proof transactions remove repeated verification work
;; inside one module without changing the public verifier contracts.  Receipts
;; are private, thread-confined, phase-confined, identity-bound, and retained
;; only for passed immutable reports.  Construction receipts are destroyed
;; before the independent audit begins.
(def ^:dynamic ^:private *sh07-proof-transaction-context* nil)
(def ^:dynamic ^:private *sh07-proof-transaction-cleanup-observer* nil)