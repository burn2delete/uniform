; Semantic decomposition of HEAD reader line 6106.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-call-specific-diagnostic-l13!
 [operator node]
 (case
  operator
  stdlib/profile-mismatch
  (typed-diagnostic!
   "L13-PROFILE"
   "standard-library namespace is used outside its declared profile set"
   node
   "Import a namespace available in the active profile or add a compatibility event for changed profile support.")
  stdlib/effect-missing
  (typed-diagnostic!
   "L13-EFFECT"
   "standard-library API effect exceeds the caller effect allowance"
   node
   "Declare the effect in the caller contract or choose a pure/profile-supported API.")
  stdlib/capability-missing
  (typed-diagnostic!
   "L13-CAPABILITY"
   "standard-library API requires a missing capability provider or grant"
   node
   "Pass an explicit capability or configure a provider grant for this profile and target.")
  stdlib/alloc-illegal
  (typed-diagnostic!
   "L13-ALLOC"
   "standard-library API allocation behavior is illegal in the active profile"
   node
   "Use a fixed-capacity, static, region, or no-allocation API variant.")
  stdlib/resource-leak
  (typed-diagnostic!
   "L13-RESOURCE"
   "standard-library resource API violates lifetime or release rules"
   node
   "Close, transfer, or return resources according to the public contract.")
  stdlib/numeric-target-default
  (typed-diagnostic!
   "L13-NUMERIC-MODE"
   "numeric API relies on target-default behavior outside the declared numeric mode"
   node
   "Select checked, wrapping, saturating, exact, deterministic, or proof-backed numeric behavior explicitly.")
  stdlib/unsafe-unproven
  (typed-diagnostic!
   "L13-UNSAFE-INVARIANT"
   "safe standard-library wrapper lacks proof for its unsafe invariant"
   node
   "Attach invariant tests, proof, runtime checks, and audit evidence to the wrapper.")
  stdlib/example-fail
  (typed-diagnostic!
   "L13-EXAMPLE"
   "standard-library documentation example fails compilation or profile checks"
   node
   "Compile examples in the conformance suite for every claimed major profile.")
  stdlib/compat-break
  (typed-diagnostic!
   "L13-COMPAT"
   "standard-library compatibility event removes or changes public profile support illegally"
   node
   "Publish a major compatibility record, migration note, and shim where feasible.")
  semantic-early-call-specific-diagnostic-unhandled))
