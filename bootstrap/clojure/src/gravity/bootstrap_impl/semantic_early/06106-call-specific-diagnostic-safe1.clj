; Semantic decomposition of HEAD reader line 6106.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-call-specific-diagnostic-safe1!
 [operator node]
 (case
  operator
  safe1/no-outcome
  (typed-diagnostic!
   "SAFE1-NO-OUTCOME"
   "dangerous operation lacks a legal safety outcome"
   node
   "Classify the operation as :proven-safe, :runtime-checked, :rejected, or :unsafe-island."
   {:operation :buffer/read,
    :safety-outcome nil,
    :missing-fact :safety-classification,
    :safe-rule :SAFE1})
  safe1/fifth-outcome
  (typed-diagnostic!
   "SAFE1-NO-OUTCOME"
   "dangerous operation uses an unsupported safety outcome"
   node
   "Use exactly one of the four SAFE1 outcomes; no fifth path exists."
   {:operation :buffer/read,
    :safety-outcome :trusted-backend,
    :missing-fact :legal-safety-outcome,
    :safe-rule :SAFE1})
  safe1/proof-missing
  (typed-diagnostic!
   "SAFE1-PROOF-MISSING"
   "claimed static safety proof is absent"
   node
   "Attach a proof reference, retain a runtime check, reject the operation, or isolate it as an unsafe island."
   {:operation :buffer/read,
    :safety-outcome :proven-safe,
    :missing-fact :proof-reference,
    :safe-rule :SAFE1})
  safe1/check-missing
  (typed-diagnostic!
   "SAFE1-CHECK-MISSING"
   "runtime checking is required but no emitted check is recorded"
   node
   "Emit a runtime check with condition, source span, failure behavior, and artifact record."
   {:operation :buffer/read,
    :safety-outcome :runtime-checked,
    :missing-fact :runtime-check,
    :safe-rule :SAFE1})
  safe1/check-illegal
  (typed-diagnostic!
   "SAFE1-CHECK-ILLEGAL"
   "runtime check requires unavailable profile support"
   node
   "Use a profile-supported check, prove the operation statically, or reject the operation."
   {:operation :buffer/read,
    :active-profile (:profile node),
    :safety-mode (:safety node),
    :missing-fact :profile-check-support,
    :safe-rule :SAFE1})
  safe1/unsafe-policy
  (typed-diagnostic!
   "SAFE1-UNSAFE-POLICY"
   "unsafe island violates active safety mode or package policy"
   node
   "Move unsafe code to an allowed mode or provide package policy permitting the audited island."
   {:operation :raw/mmio-read32,
    :safety-outcome :unsafe-island,
    :safety-mode (:safety node),
    :missing-fact :unsafe-policy-approval,
    :safe-rule :SAFE1})
  safe1/unsafe-metadata
  (typed-diagnostic!
   "SAFE1-UNSAFE-METADATA"
   "unsafe island lacks required audit metadata"
   node
   "Record owner, reason, invariant, review policy, source span, effects, capabilities, and safe wrapper boundary."
   {:operation :raw/mmio-read32,
    :safety-outcome :unsafe-island,
    :missing-fact :unsafe-audit-metadata,
    :safe-rule :SAFE1})
  safe1/generated-provenance-missing
  (typed-diagnostic!
   "SAFE1-GENERATED-PROVENANCE"
   "generated unsafe code lacks origin provenance"
   node
   "Preserve both generated form and generator origin in the safety diagnostic and artifact."
   {:operation :generated/raw-read,
    :safety-outcome :unsafe-island,
    :missing-fact :generated-origin,
    :safe-rule :SAFE1})
  safe1/optimization-proof-missing
  (typed-diagnostic!
   "SAFE1-OPTIMIZATION-PROOF"
   "optimization removes a safety check without replacement proof"
   node
   "Retain the check or attach a proof record for the erased check."
   {:operation :buffer/read,
    :erased-check :bounds,
    :missing-fact :optimization-proof,
    :safe-rule :SAFE1})
  safe1/dependency-mode-error
  (typed-diagnostic!
   "SAFE1-DEPENDENCY-MODE"
   "dependency safety mode is weaker than the caller's safe claim"
   node
   "Use a certified safe facade, a reviewed unsafe wrapper, or reject the dependency."
   {:dependency :legacy/unsafe-lib,
    :caller-safety :safe,
    :dependency-safety :unsafe,
    :missing-fact :dependency-safety-certificate,
    :safe-rule :SAFE1})
  semantic-early-call-specific-diagnostic-unhandled))
