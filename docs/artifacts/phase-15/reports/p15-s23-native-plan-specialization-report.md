# P15-S23 native plan-specialization prerequisite

This transition keeps the internal, authenticated plan-specialized C artifact
but moves target-source construction into the new Gravity helper
`bootstrap/gravity/p15_s23/native_plan_c_emitter.gravity`.  The API,
`gravity.p15-native-plan-specialization/specialize-native-runtime-plan`, takes
an actual target-neutral stage2 runtime packet and its trusted source context.
It invokes the two-argument
`gravity.bootstrap/p15-s23-closed-runtime-packet-authentic?` predicate before
touching the plan, checks the bounded single-entrypoint shape, invokes the
public `c-backend-validate-runtime-plan!`, then loads and hashes one bounded
NOFOLLOW regular-file UTF-8 snapshot of the tracked helper.  The pinned
stage2 plan-emitter rule compiles that helper and the public stage2 runtime
function runner invokes `p15-s23-native-c-emit-plan` with the explicit
compiler-artifact host flag.  The helper owns traversal and deterministic C
construction for printable-ASCII string `println`/`str` plans; the adapter
proves that precondition and preserves the public validator and packet bounds.

The production namespace intentionally does not expose a process runner.  The
existing C backend owns private staging, supervision, and cleanup; duplicating
that orchestration would create a second unsafe process boundary.  Focused
tests compile and run emitted C only in a test-owned private directory with a
small path-based helper.  That helper is test-only, kills only its leader on a
timeout, and supplies no descriptor-relative or process-tree containment
evidence.

The accepted fixtures are real `.gravity` and `.qst` sources.  The focused
test namespace builds actual stage2 packets, authenticates them with trusted
context, and compiles/runs the helper-emitted C only in a
test-owned private directory.  Tampered packet/context cases reject with
`P15NS001` before the public validator or helper source loader.  An
authenticated unsupported builtin rejects with `P15NS002` before helper
loading.  Authenticated validator-accepted boolean, non-ASCII, control-string,
and C11-trigraph plans reach the Gravity helper and reject with `P15GCE002` before C
source compilation.  A tampered helper snapshot rejects with `P15GCE001`
before helper invocation.  An overbound tampered plan is rejected by
contextual packet authentication as `P15NS001`; it does not claim that
`P15NS003` was exercised.

The first supervised namespace attempt truthfully recorded three copies of one
top-level `:missing-fact` projection mismatch while the nested Gravity helper
facts and stable `P15GCE002` diagnostic were correct.  After the narrow
projection correction, the final canonical-lock run used the stable SH07
iteration-cache runner, one cache entry, and six exact vars in fail-fast order
with the native compile/run last.  Run
`0963c1a3-c0be-4f09-8c9a-09f528b64587` passed 6 tests and 89 assertions with
zero failures/errors in 1594.489 seconds.  The supervisor recorded peak RSS
1,397,178,368 bytes, peak process count 3, no timeout or signal, and matching
before/after hashes for the implementation, test, Gravity helper, fixtures,
and runner.  The evidence is focused integrated semantic evidence, not a
release-authority or whole-suite receipt.

Boundary accounting remains explicit:

- The generated child is a direct C program and has no Clojure/JVM runtime
  available; the generic host-C packet interpreter is unused.
- C-source traversal/construction semantics are owned by the pinned Gravity
  helper. Authentication, plan validation, helper source loading, helper execution via
  the stage2 Clojure rule runner, artifact construction, process and file I/O,
  the public wrapper, and the global compiler remain Clojure-seed-bound.  The
  helper's `pr-str` primitive is an explicit host boundary and is used only
  after the adapter's printable-ASCII proof. The entire C-emitter execution
  boundary therefore remains Clojure-seed-bound even though semantic ownership
  of this narrow translation moved into Gravity source.
- The provider and compiler are not authored in Gravity; the public route,
  backend-complete, full-language, self-hosting, release, and seedless claims
  remain false.
