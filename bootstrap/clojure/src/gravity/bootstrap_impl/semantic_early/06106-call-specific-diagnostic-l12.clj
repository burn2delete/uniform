; Semantic decomposition of HEAD reader line 6106.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-call-specific-diagnostic-l12!
 [operator node]
 (case
  operator
  compile/pure-effect
  (typed-diagnostic!
   "L12-PURE-EFFECT"
   "pure compile-time evaluation attempts a build effect"
   node
   "Move the effectful work to an authorized build provider or keep the compile-time expression pure.")
  compile/ambient-input
  (typed-diagnostic!
   "L12-HERMETIC-INPUT"
   "hermetic compile-time evaluation observes an undeclared input"
   node
   "Declare the input with a content digest or remove the ambient read.")
  compile/unreplayed-random
  (typed-diagnostic!
   "L12-NONDETERMINISM"
   "compile-time nondeterminism lacks replay policy"
   node
   "Attach replay records or deterministic seeds for time, random, network, model, and tool effects.")
  compile/bad-constant
  (typed-diagnostic!
   "L12-CONST-REPRESENTATION"
   "compile-time value cannot be represented in the target artifact"
   node
   "Return a stable Gravity value, generated form, or artifact reference instead of a host object.")
  compile/generated-illegal
  (typed-diagnostic!
   "L12-GENERATED-ILLEGAL"
   "generated code fails normal syntax, type, effect, capability, memory, or safety validation"
   node
   "Validate generated forms through the ordinary pipeline and include the generated-origin chain.")
  compile/runtime-capture
  (typed-diagnostic!
   "L12-PHASE-CAPTURE"
   "compile-time code captures a runtime-only value"
   node
   "Pass serializable compile-time inputs explicitly or defer the expression to runtime.")
  compile/cache-unsafe
  (typed-diagnostic!
   "L12-CACHE-UNSAFE"
   "cached compile-time result was created under incompatible policy"
   node
   "Invalidate the cache entry and rebuild it under matching grants, target manifests, and replay records.")
  compile/secret-leak
  (typed-diagnostic!
   "L12-SECRET-LEAK"
   "secret material would leak into generated output, diagnostics, or public provenance"
   node
   "Record only the secret name and redaction policy in private provenance.")
  compile/fuel-loop
  (typed-diagnostic!
   "L12-FUEL"
   "compile-time evaluation exceeded deterministic fuel"
   node
   "Provide a totality proof, reduce the compile-time computation, or permit explicit runtime fallback.")
  semantic-early-call-specific-diagnostic-unhandled))
