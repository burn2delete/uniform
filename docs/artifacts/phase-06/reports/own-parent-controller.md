# Exact OWN positive controller binding

Source preparation only. The frozen child is the single
`real-owner-gravity-prefix-passes-fresh-replay` selector from `05e9f833`, tree
`3d115ebedfcd23078d3981a43bb1453c10c30095`. Its reviewed ten-selector patch
and PR579 remain unchanged; all ten genuine selectors remain UNRUN.

The initial current-source port was based on `2d0844f8f7f21321d920b9245341a2b98adc642e`,
tree `c5f5941ca3db4ce38695f870276e1b345a323339`. The PR581 modified
authenticated-pipeline test was not replayed: current main already carries the
newer PR579 cache-hit assertion and its related controls.

The PR582 source port was then based on the accepted PR581 composition
`22d0a1bf9ca06176e85942aac4d51149e4687012`, tree
`245b91f78cc5834200ad3eb520677e65a09ef961`; that composition already carries
the newer PR579 cache-hit assertion and its related controls. This selective
PR582 port began as a direct child of that accepted PR581 composition. Later
normal main composition retains the reviewed PR581 source and adds only this
reviewed single-broker acquisition delta; the coordinator binds the final
base/head/tree tuple at integration preflight. Original PR581 and PR582
ancestry are preserved without importing their superseded trees.

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

Before any operational Var resolution, the controller can bind the current
runner source bytes to SHA-256
`2c56243ced930f7cd253b9b10f2444267010f168fdd3e8e4b99b87e3c2f3e16c` and
the observed public signatures for `start-process`, two-arity
`process-result`, and one/two-arity `finalize-owned-process!`. A source-byte
or signature substitution fails closed; this helper performs no namespace
resolution or launch.

The order is admission, atomic one-shot output reservation, then the exact
guard order in the authoritative coverage decision. Exactly one guard must
declare `:acquisition :broker` and a supported `:resource-class`; at least one
canonical guard must declare `:acquisition :non-broker`. Every non-broker guard
binds its `:acquire-function` to the captured `:acquire-guard` port identity.
The separate `:acquire` port handles only the single broker guard, and binding
rejects the same callable supplied to both acquisition ports. Two broker
descriptors and implicit canonical acquisition refuse before the clock,
admission, reservation, acquisition or start. The declared guard vector and
coverage order remain exact vectors; this controller supplies no default order.
Every returned opaque handle is validated and retained for release through the
owner-supplied validation/release ports. The original normal broker acquire,
validate, release, runner `start-process` and private `process-result` remain
lazy; no canonical-lock implementation or generic broker, runner, security
policy or artifact codec is changed.

The same owner must separately supply source-bound admission, the non-broker
guard acquisition/validation/release semantics, atomic
reservation/publication, source/runtime observation and owned process
finalization ports. There are deliberately no operational defaults for these
missing bindings. The existing runner's natural root exit does not prove that
owned descendants are gone. The finalizer consumes the original runner cleanup
evidence, finalizes ownership once and must not kill/retry an already attempted
or unknown cleanup. Missing or incomplete terminal ownership cannot pass.
Neither normal runner cleanup flags nor a fabricated finalizer report can confer
that authority.

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
receipt failures. Fatal `ThreadDeath`, VM and linkage errors are never
converted into a failed publication: cleanup and release still run once, then
the original fatal object is rethrown. When a fatal cleanup error supersedes
an ordinary primary, the ordinary error remains suppressed on that fatal
object; later secondary errors are also attached to the active primary while
retaining their original identities. Both directly thrown interruption and the
normal runner's returned wait-interruption/error and the broker's wrapped
admission interruption are preserved after finalization. Interrupted causes
are found through an identity-tracked, cycle-safe cause walk without a depth
quota. Nonzero exit, timeout, missing refusal flags, capture failure, drift,
incomplete/unknown owned cleanup and nonfinalized release all prevent success.
Atomic output reservation prevents a repeated request from launching or
publishing again.
Launch-attempt state is separate from the returned token: nil and false are
invalid tokens, do not enter supervision or finalization, still release any
already-acquired guards, and do not publish a final receipt. A valid started
token uses an explicit started state for capture-before-release, terminal
rechecks and finalization rather than token truthiness. The final receipt is
attempted only after process finalization and all release attempts. Authority
publication is attempted only after a passing terminal observation, an exact
current observation match, every acquired release has a finalized
`:released` result and no primary/fatal error remains. A failed history is
returned as evidence only and is never published. An uncertain/failed write
is not retried; a post-write deadline failure stays nonpassing even if
tentative metadata reached storage.

Receipts project only declared metadata. Declared signed-64-bit counters may
be represented as Longs for receipt encoders, with representability checked;
raw results, opaque owner values, leases and original errors are preserved
separately. No compiler artifact is reencoded, canonicalized or granted
authority. Actual terminal/release observations remain in the returned raw
records, including exactly one release-attempt record with its original opaque
handle and any original failure. A metadata receipt alone is never execution acceptance.

## Compiler-free evidence and remaining decisions

`parent_controller_test.clj` uses only fake guard/process/clock/receipt ports.
Twenty-five tests and 556 assertions passed, with no real port resolution and
Bootstrap, broker and normal runner namespaces absent. Controls cover identity
and missing coverage before ports, single-broker/non-broker dispatch, nested
broker and implicit-canonical refusal, acquisition alias refusal, declared
order/reverse release, partial acquisition release, denial/no-start,
startup/release deadline charging, nonzero/timeout/capture failure, unknown
owned cleanup, fatal propagation after cleanup, fatal precedence and
suppression, deep/cyclic interruption, failed-history nonpublication,
interruption, duplicate reservation/publication, release-before-success,
false/nil launch-token refusal and preservation of the original error, plus
current runner source/ABI drift. Interruption assertions capture the flag at
return before reporting.

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
