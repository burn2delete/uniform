# Fixed Resolution observation intervals

## Purpose

This bounded port adds five literal observation intervals to the existing
`sh06-unmetered-resolution-test` digest test and three intervals to its current
source-artifact construction, verification, and serialization test. The helper
reports only while the exact selected test-Var and progress state are active.
Outside that scope, including disabled, nested, unknown-label, or unrelated
calls, the body executes once without a frame.

## Requirements and dependencies

The original digest requests, count/hash assertions, malformed-request
rejections, artifact fields, authentication, round-trip, and `ExceptionInfo`
handling are unchanged. The five digest labels are positive resolve,
count/hash shape, ordinal negative, count negative, and shape negative. The
artifact labels cover construction, verification, and serialization. Each
interval emits only enter and return/throw frames through the runner's existing
v1 encoder. Return means the expression returned; it is not an assertion-pass,
cleanup, or process-completion claim.

The current Syntax-source-selection and development-current Macro-source fields
remain intact. No C2 implementation, hash algorithm, request selector, heap,
watchdog, capture, cleanup, or System/exit behavior changes. The five fixed
intervals add ten synchronous stderr frames per invocation when enabled; their
cost is unmeasured and is not equivalent to disabled execution.

## Outputs and artifacts

The source change is in
`bootstrap/clojure/test/gravity/self_hosting/sh06_unmetered_resolution_test.clj`.
Synthetic interval controls are in `tools/ci/resolution_interval_test.clj`; the
exact source checkpoint, logs, and nonclaims are in
`../pr697-current-port.edn`.

## Conformance and acceptance limits

The focused interval controls passed 4 tests/15 assertions. They use synthetic
bodies and digest DATA; they do not run a Bootstrap, native hash, compiler,
foreign callback, genuine Reader, or hosted Resolution workload. The historical
PR697 Resolution run remains a nonpass record and is not replaced by this
source-only evidence. Independent review and current-head CI remain required.
