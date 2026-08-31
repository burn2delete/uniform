; Semantic decomposition of HEAD reader line 6106.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-call-specific-diagnostic-l05-l06!
 [operator node]
 (case
  operator
  unchecked/cast
  (typed-diagnostic!
   "L5-CAST-UNSAFE"
   "unchecked cast is unsafe in safe typed core"
   node
   "Use a checked cast, runtime check, or audited unsafe island.")
  implicit/cast
  (typed-diagnostic!
   "L5-CAST-UNSAFE"
   "unchecked implicit cast would silently change a Gravity type"
   node
   "Use an explicit checked cast, profile-defined coercion, runtime check, or audited unsafe island.")
  host/null
  (typed-diagnostic!
   "L5-TYPE-MISMATCH"
   "host null cannot enter a non-null Gravity type without a check"
   node
   "Normalize host null through an option/result value or runtime check boundary."
   {:host-null true})
  uninit/read
  (typed-diagnostic!
   "L5-UNINIT-READ"
   "code reads an uninitialized value"
   node
   "Prove initialization before reading or keep the value typed as Uninit.")
  linear/dup
  (typed-diagnostic!
   "L5-LINEAR-DUP"
   "linear value is duplicated illegally"
   node
   "Move, consume, or transfer the linear value exactly once.")
  schema/weaken
  (typed-diagnostic!
   "L5-SCHEMA-WEAKEN"
   "generated type would weaken the source schema"
   node
   "Preserve schema identity, validation boundaries, nullability, and refinements.")
  effect/erase-latent
  (typed-diagnostic!
   "L5-LATENT-EFFECT-MISSING"
   "function type metadata would erase required latent effect facts"
   node
   "Preserve latent effects in the function type artifact.")
  build/ambient-read-file
  (typed-diagnostic!
   "L6-BUILD-EFFECT"
   "build-time file access is ambient and ungranted"
   node
   "Move build effects to compile-time provider policy and record replayable build grants.")
  effect/unknown
  (typed-diagnostic!
   "L6-EFFECT-UNKNOWN"
   "effect kind is unknown or lacks governance registration"
   node
   "Register the effect with profile legality, capability requirements, ordering, and artifact representation.")
  effect/reorder
  (typed-diagnostic!
   "L6-EFFECT-ORDER"
   "effectful operations were reordered without proof"
   node
   "Preserve effect order or attach proof that the reordering is legal.")
  handler/type-mismatch
  (typed-diagnostic!
   "L6-HANDLER-TYPE"
   "handled effect request or response type does not match the handler"
   node
   "Use a handler whose request, response, and result types match the handled effect.")
  handler/resume-twice
  (typed-diagnostic!
   "L6-HANDLER-CONTINUATION"
   "handler resumes a continuation unsafely"
   node
   "Resume affine continuations at most once and do not transfer illegal state across suspension.")
  handler/replay-side-effect
  (typed-diagnostic!
   "L6-HANDLER-REPLAY"
   "handler would replay an external side effect"
   node
   "Read a typed replay record instead of re-executing replay-sensitive external work.")
  semantic-early-call-specific-diagnostic-unhandled))
