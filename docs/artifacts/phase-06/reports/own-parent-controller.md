# Exact OWN positive controller binding

Source preparation only. The frozen child is the single
`real-owner-gravity-prefix-passes-fresh-replay` selector from `05e9f833`, tree
`3d115ebedfcd23078d3981a43bb1453c10c30095`. Its reviewed ten-selector patch
and PR579 remain unchanged; all ten genuine selectors remain UNRUN.

## Implementation and port boundary

`bootstrap/clojure/test/gravity/artifact_pipeline/parent_controller.clj`
implements one outer lifecycle with explicit captured port function values.
It fixes the selector, child argv, source commit/tree and tracked-input
inventory. The supplied observation must exactly match the request's CWD,
ordered runtime/classpath/JAR/executable/configuration identities, port-source
identities, unique attempt/stdout/stderr/final outputs, limits and guard data.
Mismatch fails before even the clock or admission port is called.

Coverage DATA is configuration, not permission. Missing authoritative
participant/root coverage, ordered source-bound guards, runtime configuration
or unique output identities returns the exact missing fields before any port.
Even complete DATA must pass the captured real owner admission port. There is
no default broker directory, root mapping, H configuration or inferred release.
The binding caller must authenticate the actual function values and their
transitive source/configuration identity through the existing owner protocol;
matching saved labels alone cannot do that.

The order is admission, atomic one-shot output reservation, then the exact
guard order in the authoritative coverage decision. Exactly one guard must
declare `:acquisition :broker` and a supported `:resource-class`. A canonical
guard must declare `:acquisition :non-broker`; all non-broker guards must bind
`:acquire-function` to the captured `:acquire-guard` port's source identity.
The separate `:acquire` port handles only the single broker guard. Binding
rejects the same callable supplied to both acquisition ports. Two broker
descriptors, including different roots, refuse before clock, admission,
reservation, acquisition or start. No acquisition order is supplied
as a default by this controller. Both order fields must be vectors; sets cannot
supply an iteration-dependent order. Every returned
opaque handle is validated and retained for release. The original normal
broker acquire/validate/release functions and runner `start-process` and
private `process-result` are exposed only by a lazy resolution helper. It is
not called by loading the controller or by the injected tests. No generic
broker, runner, security policy or artifact codec is changed.

The same owner must separately supply source-bound admission, non-broker
guards, atomic reservation/publication, source/runtime observation and owned
process finalization ports. There are deliberately no operational defaults for
these missing bindings. The existing runner's natural root exit does not prove
that owned descendants are gone. The finalizer consumes the original runner
cleanup evidence, finalizes ownership once and must not kill/retry an already
attempted or unknown cleanup. Missing or incomplete terminal ownership cannot
pass. Neither normal runner cleanup flags nor a fabricated finalizer report
can confer that authority.

## Failure, deadline and receipt behavior

The clock is an owner-bound monotonic millisecond clock. Acquisitions receive
remaining whole-operation time. After startup returns, supervision receives
the reduced time, charging startup elapsed time. Later supervision, capture,
owned finalization, release and publication are checked against the same
deadline. This is accounted deadline enforcement, not an absolute wall bound:
native ProcessBuilder/startup, filesystem operations, JVM initialization,
process enumeration, injected callbacks and publication may block or fail
before control returns. Existing normal runner timeout cleanup is best effort;
its unproven containment remains a nonpass.
The owned finalizer receives that same deadline and captured clock together
with its explicitly declared cleanup interval. The actual normal runner's
source-bound internal cleanup intervals remain separate runtime inputs; this
controller does not change them or claim that passing a map overrides them.

Started ownership is finalized once; partial guard acquisitions release once
in reverse order. Original failure objects survive later cleanup, release or
receipt failures. Both directly thrown interruption and the normal runner's
returned wait-interruption/error and the broker's wrapped admission interruption
are preserved after finalization. Nonzero exit,
timeout, missing refusal flags, capture failure, drift, incomplete/unknown
owned cleanup and nonfinalized release all prevent success. Atomic output
reservation prevents a repeated request from launching or publishing again.
The final receipt is attempted only after process finalization and all release
attempts. An uncertain/failed write is not retried; a post-write deadline
failure stays nonpassing even if tentative metadata reached storage.

Receipts project only declared metadata. Declared signed-64-bit counters may
be represented as Longs for receipt encoders, with representability checked;
raw results, opaque owner values, leases and original errors are preserved
separately. No compiler artifact is reencoded, canonicalized or granted
authority. Actual terminal/release observations remain in the returned raw
records, including exactly one release-attempt record with its original opaque
handle and any original failure. A metadata receipt alone is never execution acceptance.

## Compiler-free evidence and remaining decisions

`parent_controller_test.clj` uses only fake guard/process/clock/receipt ports.
The original seventeen controls retain their semantics with explicit fake
non-broker guards. Nineteen tests and 400 assertions passed, with no real port resolution and
Bootstrap, broker and normal runner namespaces absent. Controls cover identity
and missing coverage before ports, ordering, partial acquisition release,
denial/no-start, startup/release deadline charging, nonzero/timeout/capture
failure, unknown owned cleanup, interruption, duplicate reservation/publication,
release-before-success and preservation of the original error. The initial
delimiter error and an interrupt assertion sampled after test reporting remain
historical failures; the corrected assertion captures the flag at return.

## Proposed future profile and configuration evidence

This correction supersedes the three-broker-capable fixture in PR581; it does
not adopt a root or authorize a run. The existing RV source assessment
`/tmp/rv1-broker-guard-applicability-20261029.edn` and the CI/verification
owner's audit supply reused evidence: the no-nesting rule has no accepted
exception, and live-lease validation does not supersede it. Their conditional
future method is one exclusive broker lease at a confirmed shared root plus a
separate immediate canonical file-lock attempt, released in reverse order.
The declaration order remains an explicit authority decision.

| Binding | Proposed value or supported configuration surface | Still required |
| --- | --- | --- |
| Broker acquisition | One `:exclusive` lease, `:acquisition :broker` | Source-backed participating-caller coverage and one confirmed shared root |
| Canonical guard | Explicit `:acquire-guard` non-broker function using immediate tryLock | Exact function source, channel/lock ownership, validation and release bindings |
| Development loop | `:broker-root`, then `:coordination-root`, otherwise Git-common shared-root broker | Actual participating launch configuration |
| Execution lanes | `*coordination-root*`, `GRAVITY_SH01_COORDINATION_ROOT`, CLI `--coordination-root` | Actual participating launch configuration |
| SH07 bounded runner | `:coordination-root` or `GRAVITY_SH01_COORDINATION_ROOT` | Actual participating launch configuration |
| Pass profiler | Source fixes legacy `/private/tmp/gravity-sh01-resource-broker-v1` | Reconcile if this caller participates; no inferred coverage |
| Configuration acceptance | Existing development-verification owner and uniform-coordinator roles | Current coordinator `01a172c9-3648-702c-b7ac-03dab5501018` acceptance |

The configuration surfaces above come from the existing verification audit
and local source readback; they are not observations of active holders or
availability. No all-OS root registry is required. New OWN evidence consists
only of the controller's single-broker pre-port gate, distinct acquisition
dispatch, two-broker/implicit-function refusal controls, and one-broker plus
canonical positive/reverse-release controls. Saved coverage labels cannot
replace authenticated source/configuration coverage or the current
coordinator's acceptance through the real admission binding. Neither has been
supplied here; the old paused coordinator is not an acceptance dependency.

The following are still unbound operational facts, not implementation credit:

- The authority source, decision and complete actual participant-to-root
  coverage, including reconciliation of Git-common and legacy owner-local
  roots, and the real acquisition order.
- Exact non-broker guard and admission function source bindings, owned-process
  tracker/finalizer and their interference/terminal semantics.
- An actual frozen-source CWD matching the original child tuple, ordered
  classpath/JAR/executable/runtime/configuration identities and source observer.
- Current approved whole-operation/capture/cleanup bounds and unique output
  paths with atomic reservation/publication bindings. Historical 2 GiB/1,200 s
  alone is not current admission.

Next: independent finite source/harness review, then the existing owner/config
decision must supply and authenticate those bindings. A later action-specific
assessment may submit only the frozen positive through that same normal
route. This patch performs no lease acquisition, source observer, normal-runner,
Bootstrap/compiler/child execution, host/holder probe, unknown-operation
recovery or lock reacquisition. Reader/DSE denials and R4/22232 unknowns are
unchanged. No OWN-02 closure, v3, runtime acceptance or integration eligibility.
