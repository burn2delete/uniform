# Direct synchronous selector forwarding in the existing runner

This is a finite source-only caller increment based on ACC06's qualified
SOURCE/MODEL candidate `eefd1ed5f6ee4bfc9209fbc9696f922d758cd890`, tree
`7dc2db4e5b2df9e962bd7482a2f5016c5d133e2c`. Its receipt is
`/tmp/custody-independent-eefd1ed5/receipt.edn`, SHA256
`7fe69cb97c7b7bf8fb2c577ad80485874de6f5309ee27cb0dc9bab46bdebaad0`.
No acceptance is inherited for this new source. Original PR600 remains complete.

## Existing caller and smallest source gap

The stock `run-cli -> build-namespace-plan -> execute-plan` path cannot express
the supplied exact development-test argv through its CLI, and scheduled workers
are not synchronous inside an external acquiring-thread lease. The separate
development-loop brokered-result manually acquires/releases and remains outside
ACC06's finite scope. The fixed legacy profiling script is an unrelated workload,
not a reusable selected-test caller. No complete existing source caller meeting
the requested selection/resource/inheritance tuple was identified.

The existing public `run-namespace-process` does directly call start-process
and the normal process-result on its own thread. It already owns command
construction, CWD, launch observer, result normalization and output reporting.
This patch extends that function rather than adding a controller, worker,
external wrapper, alternate finalizer or planner catalog membership.

`namespace-command` now optionally accepts a nonempty string vector
`:exact-test-vars`, emitting repeated `--exact NAME` after its existing
`--namespace NS`, followed by optional explicit boolean `:child-fail-fast?`.
Its command prefix remains caller supplied and unchanged; for this use the
caller supplies the pinned direct Java/development-test-runner prefix. The
child runner retains its own exact-name validation and unknown-name refusal.
No OWN-specific names or test semantics are encoded in production.

`run-namespace-process` optionally accepts `:resource-broker` containing the
existing with-lease options, and requires an explicit job resource class.
It calls with-lease internally around the existing operation, synchronously
on the acquiring thread. The default launcher returns an actual Process;
the actual process-result retains/validates its token and registers post-token
custody under ACC06. A resource-owned call refuses an injected result map
instead of treating it as a managed child. No separate wait/finalization,
manual lease release, async handoff or clearance is added.

Both old public arities remain. Without these options, command construction
and the original unleased operation remain unchanged, including legacy map
injection for unit tests. The existing owner-required-namespace guard runs
before any lease or child. This API does not make a fake planner job, add an
unknown namespace to the scheduler/catalog, or bypass the guarded-owner refusal.
Accountable callers still supply their actual source-owned namespace/class.

The unchanged start-process receives exactly argv and CWD. It leaves
ProcessBuilder.environment untouched and inherits its actual Java parent's
environment. This source preservation is not effective parent/child JVM,
provider or native configuration attestation. Existing command results retain
the full child command; the caller must freeze its actual parent command,
working directory and inherited configuration separately. No environment
override, credential, native setting, executable or classpath default is added.

## Inert controls and evidence boundary

Run `check.clj` directly using a 256MiB Clojure 1.12.5 core-JAR-only tooling JVM.
The script loads only broker tooling and reads source with read-eval disabled.
It evaluates the actual selected command/caller, ownership-record/token,
process-result, normalization and Throwable helper forms in a private namespace.
Native start, wait/capture, finalization, policy discovery and broker acquire/
release are explicit local models. Fake Process objects refuse real wait,
alive/exit observation and termination. No actual runner/planner, selected
test namespace, Bootstrap or Gravity runtime is loaded.

The controls cover exact generic three-selector argv/CWD, unchanged defaults,
malformed-selection refusal, synchronous acquiring/retaining-thread order,
actual Process/token identity, raw failed/timeout status and payload
preservation, incomplete root/tracked/capture quarantine, original error/fatal/
interruption and suppressed cleanup identity, acquisition/owner refusal,
explicit class/options, resource-owned map refusal, and legacy unleased maps.
No real lease, lock, process probe, child or selected workload is executed.

A separate pure source proof reconstructs the exact supplied OWN child argv
from `/tmp/own-alias-action-command-pins-20261030.edn` using the unchanged
prefix and the new selector options. It computes command data only, without
calling run-namespace-process or executing that command. It does not supply
an actual parent command or retarget the frozen OWN source/configuration.

Final owner model: 9 tests / 226 assertions, zero failures/errors, core-JAR-only
OpenJDK 26.0.1 tooling JVM, heap256MiB. Separate language-boundary gate: 6 / 11.
These populations are not summed. Coherent source mutations are refused:
omitting the lease call yields 16 failures / 2 errors; dropping selector
forwarding yields 17 failures / 0 errors, both natural exit1. Changing the
unchanged default CWD fails source-form conservation. The pure command proof
matches all 16 pinned child arguments and three selectors without invocation.
Lint reports zero errors and only the three inherited runner warnings; the
new checker has no findings and passes formatting. No broad shared-file
formatting changes are made.

## Exclusions and required next handoffs

Accepted eefd custody begins only after the token is validated/registered.
Native start failure, returned-Process/observer/retention errors before that
registration, supervisor death, async thunk escape, manual leases and the
development-loop path remain excluded. The model deliberately records the
base's empty-registry release on a pre-token throw; this is a known gap, not
a claim of safe absence of a child. Held startup candidate `36e62b7f` is not
imported, changed, retested or accepted by this patch.

Accounting must separately decide new review eligibility; this increment stops
before any review dispatch. The failed/no-ack startup review and held OWN
configuration review are not resent, rephrased, rerouted or replaced here.
Later exact source/configuration composition must reconcile this caller with
an actually accepted startup interface and independently disposed OWN config,
then bind the real parent/child argv/CWD/environment, helper creation set,
limits, report/output custody and applicable platform requirements. This source
forwarding increment alone supplies no genuine operation or runtime admission.
No collector, Git bookend, canonical-lock/report method, publication, CI,
merge, closure or denied/unknown operation is changed or attempted.
