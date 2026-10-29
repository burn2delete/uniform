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

## Actual owner selectors and affected consumers

All selectors below belong to `gravity.self-hosting.sh09-body-effect-test`.

| Selector | Actual covered route and negative case | Execution |
| --- | --- | --- |
| `body-module-vectors-have-no-cumulative-quota` | Gravity leaf helper; caller and combined producer vectors beyond removed ceilings | Held; Clojure body control previously passed |
| `body-module-vectors-check-caller-and-every-producer` | Gravity leaf helper; malformed nodes/function vectors in caller and late producer | Held; Clojure body control previously passed |
| `body-private-map-keys-development-kernel` | C7 PMK typed input -> Effects LP/LQ context, call joins, node allocation; forged family, type/fact/source, missing/duplicated entries and native callback negatives | Held |
| `body-private-map-keys-authenticated-builder-composition` | Actual PMK source -> C5/C6/C7 -> `build-body-effect-result` -> `verify-body-effect-result`; mismatched request binding refusal | Held; new test |
| `body-effect-public-forwarding-source-boundary` | Reader checks exact public C8 build/verify forwarding forms and exports | Source-only; not public compiled module composition |

`verify-body-effect-result` reconstructs via `build-body-effect-result`, which
calls `body-module-vectors-valid?`. The public C8 consumer exports
`sh09-build-authenticated-body-effect-result` (arity 3) and
`sh09-verify-authenticated-body-effect-result` (arity 4), forwarding to those
two Effects exports. Their source wiring is covered by the selected boundary
test. Compiling/executing the public C8 bundle remains a separate, held consumer
composition step; the general existing authenticated paired test uses a broader
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

## Remaining prerequisites and evidence classification

| Item | Classification | Present disposition |
| --- | --- | --- |
| Separate permission for genuine Gravity/Native/compiler execution | Present instruction safeguard | Still held by the parent activation boundary |
| Existing host resource admission, runtime/classpath provenance and source authentication for an actual launch | Present execution safeguards | Not inspected, acquired, assumed or proven here; launcher must establish them |
| Exact selector/source/consumer binding and honest failure attribution | Concrete driver gap | Nonlaunching adapter and synthetic positive/negative controls supplied |
| PMK authenticated builder selector | Concrete coverage gap | Implemented, unrun; needs genuine owner execution |
| Public compiled C8 PMK composition | Concrete remaining consumer evidence | Source forwarding known; compiled execution unrun/held |
| Historical unknown message/operation and failed process inspection | Historical unresolved evidence | Preserved and unrepeatable; no demonstrated current interference, so no blanket capacity gate inferred |
| Stopped metadata writes or rejected actions elsewhere | Historical stopped operations | Remain stopped; this package performs no retry or alternate route |
| Current-main composed validation and independent acceptance | Integration prerequisite | Source reconciliation and independently reviewed real composition required before merge |

The exact next permitted action is compiler-free preparation and review of this
package. The first genuine acceptance action, once separately authorized with
real launch prerequisites established, is the five exact selectors emitted by
`prepare`, using the existing development runner and its machine sink. Follow
that with separately scoped public C8 PMK composition and independent composed
review. No full ACC-08, integration, release, self-hosting or seed-retirement
credit follows from the synthetic harness or routine CI.
