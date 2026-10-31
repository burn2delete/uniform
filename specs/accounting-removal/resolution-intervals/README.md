# Five fixed Resolution diagnostic intervals

This source-only candidate extends the existing opt-in progress stream for one
exact selected test: sh06-unmetered-resolution-test/
digest-count-quota-is-gone-but-complete-ordered-requests-remain-required.
No C2/Resolution implementation, hash, request, selector, assertion, guard,
watchdog, heap, job ceiling, capture, cleanup or System.exit behavior changes.

The test calls the public runner helper observe-resolution-digest-interval!
with five literal labels: positive-resolve, count-and-hash-shape,
ordinal-negative, count-negative and shape-negative. Each uses the accepted
observe-progress! function around the original expression(s). The original
ExceptionInfo-catching negative helper is unchanged. Input construction remains
outside these intervals; a missing interval does not prove nonexecution.

The normal runner binds a private observational context only while invoking
this exact test-Var phase. It carries the original progress-state identity and
fixed namespace/Var names; it is not authority, custody or authentication. The
public helper reports only inside that same enabled state/context. Unrelated,
disabled, nested-new-state and unknown-label calls execute their body once
without reporting. No caller supplies names, paths, writers or policy.

Five new literal phase values use the unchanged v1 frame keys and existing
stderr encoder: resolution-digest-positive-resolve,
resolution-digest-count-and-hash-shape, resolution-digest-ordinal-negative,
resolution-digest-count-negative and resolution-digest-shape-negative. Each
emits enter before calling the original body and return/throw after it. Return
is not assertion PASS; an expected rejection caught within the body returns
normally. Exception classes, rather than raw errors/messages/stacks, are used.
This phase-vocabulary extension requires affected source review; no external
closed-vocabulary consumer compatibility is assumed.

The unchanged reporting helper retains format/write/flush failures, still runs
the original body/finally, preserves original values/errors and guarded causal
interrupt handling, and keeps failures sticky until the existing pre-success
barrier. Fixture catches cannot erase that reporting failure. No new timer or
worker is added. Existing acceptance of an unreadable Throwable edge is reused;
such an edge does not establish an interrupted cause.

A normal complete invocation adds ten rows/flush attempts, rather than an event
per digest request. Repeated fixture invocations retain their original callback
cardinality and can repeat these ten rows; no new global quota is claimed.
Logging, closure/context allocation, an enabled branch and flush costs are
additional and unmeasured. The test's direct dependency on the runner is already
loaded in the normal development route; another test route may pay its source
loading cost. Disabled helper calls do not create progress state or write frames.

Focused controls extract only the real reporting functions into a ci.* fixture
namespace, without loading Bootstrap, a semantic test namespace, compiler or
native hash. The existing progress-controls command also runs these controls.
Own small C2 dependency controls check that wrapping a three-request body keeps
its dynamic callback-switch observations and counts, with synthetic digest DATA
only. There is no hash substitution in product source, map sharing or caching.

The original run41216108296 is NONPASS. It supplied a complete captured final
enter row, not phase duration, exact native subcall, root cause, cleanup or
complete produced-output proof. Its last open test contains the positive call,
hash-shape assertions and three negative calls now distinguished here. Future
measurement requires actual source acceptance/current-base publication and its
own configured evidence; this candidate triggers no run or source push.
