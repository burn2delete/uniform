# ACC-08 Effects aggregate acceptance preparation

This package covers only PR533's aggregate helper and its sole builder call.
ACC-08 remains partial. The source helper, original two semantic selectors,
3/47 compiler-free controls and original independent static review are retained.
No other accounting task ID or status changes here.

Preparation observed remote main `6276c94b0b753c888ef8fecc794bcc81245ea3b3`,
tree `9f464ed0f03866bd4978e4213a488f3b1ae48683`, and original candidate
`eb9258a238d620a05d8cda0354fa418f3270ffcb`, tree
`a5860fa3d9574f46c7163994501a4c575a168d6d`. At those exact identities C7,
public C8, upstream C7 tests and the development runner match. Effects and the
owning test differ only by the original PR533 change. This observation is not
current-main composed execution or a merge authorization.

The successor normally reconciled main
`dfa4c5b653d8049ac78dd30962172e0d91c768e2` (tree
`000c3e4cce476c196b61814a4732a48b9be95540`). Effects, C7, public C8 and the
five selector names were unchanged; Reader/bootstrap dependencies changed, so
their old composition evidence is not credited to this new dependency tuple.

## Concrete gaps and corrections

The existing development runner sorts qualified test names, selects exact vars
after loading their namespaces, and reports counters through
`gravity.development-test-runner/*machine-result-sink*`. Its machine result does
not bind source revisions, dependency trees or affected consumer APIs. A successful
result for a different selector, stale resource or failed namespace load could
therefore be misattributed by a caller which only checked exit status.

`tools/effects_acceptance.clj` prepares a source-bound exact selection without
requiring any compiler namespace. It checks committed resource bytes, complete
tracked production/test/fixture/Gravity/contract/tool tree identities and clean
dependency paths. Resource hashes bind the owning tests, C7, Effects, public C8
forwarders, development runner, bootstrap load order, PMK fixture pair and deps.
The tree pins conservatively include tracked transitive source and resources;
they do not claim a minimal semantic dependency set or external runtime closure.

The adapter verifies the sole helper call and public build/verify forwarding
forms. `prepare` returns the exact existing runner command, in its actual sorted
selector order. `bind-result` requires matching before/after snapshots, exact
selector and consumer identities, all selected test counters and coherent runner
outcome/exit status. A missing sink is a driver/load/fixture failure, never a
Gravity refusal. This is same-user provenance checking, not receipt authentication.

The existing PMK kernel calls internal effect joins and checks descriptor,
entry, key-domain, source/fact and native callback rejection. It does not call
the authenticated builder. The added
`body-private-map-keys-authenticated-builder-composition` uses the existing
`fresh-products` source/C7/seal path on the PMK fixture, calls the real builder
and verifier, checks PMK allocation and rejects a changed request binding with
`C8-VERIFY` / `:untrusted-sh08-body-effect-input`. It has not run. No ANE domain
or descriptor migration is introduced.

That same selector now passes the exact PMK triplet through the existing
`compile-c8-module-bundle` public C8 build/verify entrypoints, compares the
public result to the leaf result and preserves the same wrong-binding refusal.
It stops before public compilation if upstream PMK products failed. The
nonlaunching adapter checks these exact selector-to-consumer call forms;
synthetic controls reject a missing public compiler call or lost negative input.

## Actual owner selectors and affected consumers

All selectors below belong to `gravity.self-hosting.sh09-body-effect-test`.

| Selector | Actual covered route and negative case | Execution |
| --- | --- | --- |
| `body-module-vectors-have-no-cumulative-quota` | Gravity leaf helper; caller and combined producer vectors beyond removed ceilings | Held; Clojure body control previously passed |
| `body-module-vectors-check-caller-and-every-producer` | Gravity leaf helper; malformed nodes/function vectors in caller and late producer | Held; Clojure body control previously passed |
| `body-private-map-keys-development-kernel` | C7 PMK typed input -> Effects LP/LQ context, call joins, node allocation; forged family, type/fact/source, missing/duplicated entries and native callback negatives | Held |
| `body-private-map-keys-authenticated-builder-composition` | Actual PMK source -> C5/C6/C7 -> Effects builder/verifier -> compiled public C8 builder/verifier; mismatched request binding refusal at both boundaries | Held; public composition added |
| `body-effect-public-forwarding-source-boundary` | Reader checks exact public C8 build/verify forwarding forms and exports | Source-only; not public compiled module composition |

`verify-body-effect-result` reconstructs via `build-body-effect-result`, which
calls `body-module-vectors-valid?`. The public C8 consumer exports
`sh09-build-authenticated-body-effect-result` (arity 3) and
`sh09-verify-authenticated-body-effect-result` (arity 4), forwarding to those
two Effects exports. Their source wiring is covered by the selected boundary
test. Compiling/executing the public C8 bundle is now covered by the same held
PMK selector; the general existing authenticated paired test uses a broader
fixture and is not silently included in this PMK-only selection.

The changed helper's malformed-vector diagnostic is behind authenticated-input
validation. Direct helper negatives test vector shape; the builder PMK negative
tests authentication. Neither is credited as execution of the other refusal.
The preserved source controls check the exact diagnostic migration. Genuine
execution of the new diagnostic branch is not claimed by this package.

## Portable inspection and future result binding

From a checkout containing the pinned source and tool revisions:

```bash
clojure -Sdeps '{:paths ["tools"]}' -M -m effects-acceptance . --snapshot MAIN_COMMIT
clojure -Sdeps '{:paths ["tools"]}' -M -m effects-acceptance . IDENTITY_RECEIPT
clojure -Sdeps '{:paths ["tools"]}' -M tools/effects_acceptance_test.clj
```

These commands are compiler-free. Capture the snapshot output in a private
portable EDN identity receipt after committing the source/tool package. Its
location is local evidence, not public PR metadata. The identity file records an observed current
main, candidate commit/tree, dependency trees and resource hashes. Moving main
requires a fresh observation and source reconciliation; the historical main pin
must not be called current afterward. Later package-only commits can preserve
the semantic resource hashes, but require a newly reviewed snapshot before use.

After separate authorization and applicable host admission, a genuine caller
must prepare against that exact snapshot, bind the existing runner's
`*machine-result-sink*` to capture its actual map, call `run-cli!` with the
prepared command arguments, capture a second snapshot, then use `bind-result`.
No map reconstructed from stdout, expected status, synthetic control or CI can
replace that actual sink. An absent result or changed snapshot fails binding.
The adapter contains no compiler launch or broker acquisition.

## Exact request process-port binding

`validation-request` binds the frozen argv, CWD, source/runtime/classpath and
five-selector tuple to the complete source hash and named ports of the existing
`sh01-parallel-test-runner`. The names include public `start-process` and its
internal `process-result`, `terminate-process-tree!` and `restore-interrupt!`
boundaries. Internal ports are source-pinned bindings, not new public runner
APIs. The adapter neither resolves nor loads them and supplies no default real
ports. Actual installation must match this exact reviewed source at admission.
The actual caller must hold its existing memory-heavy admission before invoking
the start port and retain it through cleanup. The new child wrapper does not
acquire a second lease; its prepared root argument is identity data only.

`observe-validation!` accepts explicit ports for source reread, the monotonic
clock, start, result/capture, cleanup, publication and interrupt restoration.
It rehashes the actual port source through that reread port before start.
Sink parsing is owned by the adapter, not replaceable by a caller consumer.
The frozen schema, selection, canonical CWD, ordered classpath pins and exact
Java/heap/operation/identity-file argv are validated before the start port.
The request hash is appended as the final child argument, avoiding a hash
self-reference in the frozen argv template. The new child wrapper echoes it
beside the actual machine-sink map in one `EFFECTS_VALIDATION_RESULT` envelope;
`read-validation-result` refuses missing or duplicate envelopes. This format is
distinct from the preserved older frozen operation's untagged output.
The deadline starts before start. Elapsed startup time is subtracted from the
normal result port's timeout; a late returned zero result is refused. Timeout,
nonzero/error exit, failed/forced/truncated capture and incomplete cleanup cannot
reach result consumption. A missing payload or wrong request hash cannot publish.
Cleanup is attempted once when needed, and its failure/uncertainty is preserved.
Publication is attempted once; an exception remains `:unknown`, never retried.

The frozen execution budget is 3,600,000 ms. Each stream retains at most
1,048,576 bytes and characters, with a 5,000 ms capture wait and 100 ms post-close
wait. The existing cleanup timeout is 1,500 ms with a 100 ms post-cleanup wait.
The accounted reclamation base is therefore 11,800 ms: 1,500 + 100 +
2 * (5,000 + 100). The 100 ms graceful-termination phase is inside the
1,500 ms cleanup deadline, not added again. Normal cleanup also allows a
100 ms `onExit` wait per observed handle; the full accounting is the base plus
100 ms times the observed handle count, without inventing a handle-count cap.
The adapter binds both the source grace constant and the actual reap-call form.
These are host process/capture bounds, not compiler accounting.
The adapter refuses results returned after the execution deadline, even where
normal cleanup/capture overhead contributed to that elapsed time.

These are cooperative bounds on returning ports. Uninterruptible native start,
wait, close, filesystem IO and JVM initialization have no invented hard bound.
ProcessHandle enumeration, native handle operations and scheduler delays also
have no hard bound; enumeration precedes the runner's cleanup deadline.
Result consumption and publication IO are outside the runner's process deadline.
ProcessHandle cleanup covers observed owned handles only: discovery failure,
survivors, failed cleanup or unproven timeout containment refuse consumption.
An unobserved cross-session escape cannot be disproved by this mechanism; an
external supervisor's containment remains a real launch prerequisite when the
execution policy requires that stronger guarantee. No broker/root configuration
or live capacity observation is added.

`tools/effects_process_binding_test.clj` exercises only fake process/clock ports.
It does not rerun the unchanged 6/44 source-control file, whose Git-fixture test
would spawn Git. Neither Bootstrap nor the normal runner namespace is loaded.
The old frozen operation and the new immutable request/port hashes are retained
as separate local artifacts; the old tuple is not silently relabeled current.

## Remaining prerequisites and evidence classification

| Item | Classification | Present disposition |
| --- | --- | --- |
| Separate permission for genuine Gravity/Native/compiler execution | Present instruction safeguard | Still held by the parent activation boundary |
| Existing host resource admission, runtime/classpath provenance and source authentication for an actual launch | Present execution safeguards | Not inspected, acquired, assumed or proven here; launcher must establish them |
| Exact selector/source/consumer binding and honest failure attribution | Concrete driver gap | Nonlaunching adapter and synthetic positive/negative controls supplied |
| PMK authenticated builder selector | Concrete coverage gap | Implemented, unrun; needs genuine owner execution |
| Public compiled C8 PMK composition | Concrete coverage gap corrected | Same PMK selector now exercises public builder/verifier and wrong binding; execution unrun/held |
| Historical unknown message/operation and failed process inspection | Historical unresolved evidence | Preserved and unrepeatable; no demonstrated current interference, so no blanket capacity gate inferred |
| Stopped metadata writes or rejected actions elsewhere | Historical stopped operations | Remain stopped; this package performs no retry or alternate route |
| Current-main composed validation and independent acceptance | Integration prerequisite | Source reconciliation and independently reviewed real composition required before merge |

The exact next permitted action is compiler-free preparation and review of this
package. The first genuine acceptance action, once separately authorized with
real launch prerequisites established, is the five exact selectors emitted by
`prepare`, using the existing development runner and its machine sink, including
the public C8 PMK composition in the same selector. Independent composed
review remains required. No full ACC-08, integration, release, self-hosting or seed-retirement
credit follows from the synthetic harness or routine CI.
