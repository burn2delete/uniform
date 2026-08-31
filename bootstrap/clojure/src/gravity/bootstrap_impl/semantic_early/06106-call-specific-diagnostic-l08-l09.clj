; Semantic decomposition of HEAD reader line 6106.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-call-specific-diagnostic-l08-l09!
 [operator node]
 (case
  operator
  dispatch/protocol-method-mismatch
  (typed-diagnostic!
   "L8-PROTOCOL-METHOD"
   "implementation does not satisfy the protocol method contract"
   node
   "Match the declared method arity, return type, effects, and capabilities.")
  dispatch/ambiguous
  (typed-diagnostic!
   "L8-DISPATCH-AMBIGUOUS"
   "multiple implementations match with no priority rule"
   node
   "Add a priority rule, seal the implementation set, or make the dispatch target explicit.")
  dispatch/missing
  (typed-diagnostic!
   "L8-DISPATCH-MISSING"
   "no implementation is available for the dispatch target"
   node
   "Provide an implementation or reject the call before dispatch.")
  dispatch/reflective
  (typed-diagnostic!
   "L8-DYNAMIC-FORBIDDEN"
   "active profile rejects dynamic or reflective dispatch"
   node
   "Use direct, dictionary, vtable, or proven closed dispatch in constrained profiles.")
  dispatch/method-effect
  (typed-diagnostic!
   "L8-METHOD-EFFECT"
   "method implementation widens undeclared effects"
   node
   "Expose the widened method effects in the protocol contract or remove the effect.")
  dispatch/host-unsafe
  (typed-diagnostic!
   "L8-HOST-DISPATCH"
   "host dispatch boundary lacks null, exception, or type contract"
   node
   "Normalize host nulls, exceptions, ownership, and type identity before dispatch.")
  dispatch/tool-unsafe
  (typed-diagnostic!
   "L8-TOOL-DISPATCH"
   "tool dispatch lacks schema or capability evidence"
   node
   "Attach a tool schema, tool identity, and explicit tool capability before dispatch.")
  error/missing-throw-effect
  (typed-diagnostic!
   "L9-THROW-EFFECT"
   "thrown error is missing from visible function effects"
   node
   "Expose :error/throw in the function and namespace effect contract or return Result data.")
  error/unhandled
  (typed-diagnostic!
   "L9-UNHANDLED"
   "required error path is not handled or propagated"
   node
   "Handle the error path, propagate it in the type/effect contract, or return Result data.")
  panic/illegal
  (typed-diagnostic!
   "L9-PANIC-PROFILE"
   "panic is illegal or lacks lowering in the active profile"
   node
   "Use a profile-defined panic lowering or a recoverable error representation.")
  host/error-unsafe
  (typed-diagnostic!
   "L9-HOST-ERROR"
   "host exception or null crosses into Gravity unchecked"
   node
   "Normalize host nulls and exceptions into typed Gravity error contracts.")
  ffi/error-unsafe
  (typed-diagnostic!
   "L9-FFI-ERROR"
   "FFI error convention lacks typed mapping"
   node
   "Map errno, exceptions, nullability, ownership, and cleanup into a Gravity error type.")
  workflow/failure-unsafe
  (typed-diagnostic!
   "L9-WORKFLOW-ERROR"
   "workflow failure lacks durable replay record"
   node
   "Record workflow step id, schemas, retry, compensation, and replay evidence.")
  ai/error-unsafe
  (typed-diagnostic!
   "L9-AI-ERROR"
   "AI or tool failure lacks structured policy/audit artifact"
   node
   "Record provider, tool, schema, policy, budget, and audit failure details.")
  semantic-early-call-specific-diagnostic-unhandled))
