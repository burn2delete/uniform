# P15-S23 native plan-specialization prerequisite

This slice adds an internal, authenticated plan-specialized C artifact.  The
new API,
`gravity.p15-native-plan-specialization/specialize-native-runtime-plan`, takes
an actual target-neutral stage2 runtime packet and its trusted source context.
It invokes the two-argument
`gravity.bootstrap/p15-s23-closed-runtime-packet-authentic?` predicate before
touching the plan, checks the bounded single-entrypoint shape, invokes the
public `c-backend-validate-runtime-plan!`, and emits direct C with the public
`c-backend-runtime-source`.

The production namespace intentionally does not expose a process runner.  The
existing C backend owns private staging, supervision, and cleanup; duplicating
that orchestration would create a second unsafe process boundary.  Focused
tests compile and run emitted C only in a test-owned private directory with a
small path-based helper.  That helper is test-only, kills only its leader on a
timeout, and supplies no descriptor-relative or process-tree containment
evidence.

The accepted fixtures are real `.gravity` and `.qst` sources.  Tests build
actual stage2 packets, authenticate them with trusted context, emit direct C,
and compare exact output.  Tampered packet/context cases reject with
`P15NS001` before the public validator or emitter.  An authenticated unsupported
builtin rejects with `P15NS002` before C emission.  An overbound tampered plan
is rejected by contextual packet authentication as `P15NS001`; it does not
claim that `P15NS003` was exercised.

The final stable-input run used the hardened heartbeat supervisor and canonical
shared lock.  It passed 4 tests and 48 assertions with no failures or errors in
783.481 seconds, peaked at 1,379,516,416 bytes RSS and two processes, and left
no surviving supervisor or JVM.  All snapshotted implementation, test, fixture,
and runner hashes matched after the run; the tracked input-comparison artifact
records each before/after value.  This is focused integrated semantic
evidence, not release authority.

Boundary accounting remains explicit:

- The generated child is a direct C program and has no Clojure/JVM runtime
  available; the generic host-C packet interpreter is unused.
- Authentication, plan validation, C emission, artifact construction, process
  and file I/O, the public wrapper, and the global compiler remain
  Clojure-seed-bound.
- The provider and compiler are not authored in Gravity; the public route,
  backend-complete, full-language, self-hosting, release, and seedless claims
  remain false.
