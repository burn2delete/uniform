# Repeated verification cleanup

This is the detailed backlog and acceptance record for the verification-reuse
outcome. Stable IDs below belong to this cleanup, not language completion.
The shared cleanup navigation lives in the component roadmap index.

## Governing contracts and inventory

[D6](phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md),
[D9](phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md),
[C18](phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md)
and [TEST2](phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md)
require surviving evidence, invalidation and independent verification.
[Development verification](development-verification.md#explicit-stage2-runtime-execution-attribution)
already distinguishes original producer evidence from its later application.
Saved reports and equality of logical parents do not confer authority.

Inventory base: `176c4ee65bb3ceb545f51edfaa599902030a0cb7`.
Inspected interfaces: Resolve construction inputs and its public verifier;
Core construction verification tests; the incremental dependency contract,
planner, child cache wiring and explicit input revalidation; SH15 diagnostic
case construction; current Native/MIR source/composition receipts.

| ID | Exact boundary and files | Dependency and acceptance | Current disposition |
| --- | --- | --- | --- |
| RV1 | `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/resolution-construction-inputs.clj`; later Resolve construction proof and public verifier | Any additional Macro check elision requires transitive live-plan, dynamic-dispatch and AE identity stability, plus mutation observation across the whole proposed reuse seam. Same logical parent is insufficient. Preserve error order, fallback, invocation/transaction isolation and fresh public/tamper checks. | PR396 already removed one immediate duplicate (4 to 3). Further removal remains unproved; retain those checks. No repeat fixture experiment or new elision in this change. |
| RV2 | `contracts/stage0-incremental-test-dependencies.edn`; `sh01_component_test_dependencies.clj`, `sh01_impact_test_planner.clj`, `sh01_development_loop_wiring.clj` and the latter two test files under `bootstrap/clojure/test/gravity/self_hosting/` | Re-audit the pure Reader artifact-identity facade, five nested files and test; bind existing runners/contracts/classpath/inventories/shadows/runtime. Demonstrate a real test producer, unrelated docs/test-body reuse with original provenance, consumed-input invalidation, and preserved fresh policies. | Implemented and independently code/result reviewed; exact receipts below. Integration pending. No report, cache or receipt schema change. |
| RV2a | Same dependency contract; `docs/self-hosting-slice-ownership.edn`; planner test | Four existing non-slice namespaces block catalog construction on the inventory base. Add exact source-grounded owners and fresh policies for parameter Reader invocation, keyword scalar controls, staged Reader closure and token spelling. Select their existing bodies without running them as cache controls. | Baseline failure reproduced; exact mapping repair passes planner validation. Integration pending. |
| RV3 | `bootstrap/clojure/test/gravity/self_hosting/sh15_diagnostic_boundary_test.clj` | Reuse one genuine immutable upstream case for the same diagnostic reference tests; preserve independently constructed changed-source/ordinal cases and resolved verification. | Already integrated at `c3f35fef`; representative case and references are delayed, with an explicit build counter. No duplicate reconstruction of that accepted fix. |
| RV4 | RV1-RV3 records, exact candidate diff, execution receipts and independent review | Close each inventory row by integrated regression evidence or a source-grounded necessary-check decision. Preserve failed, unrun and stale boundaries. | Open until independent review and integration. |

## Exact Reader leaf closure

The opt-in is only `gravity.c2-artifact-identity-test`. Its leaf production closure is
`bootstrap/clojure/src/gravity/c2_artifact_identity.clj` and
`c2_artifact_identity/{artifacts,canonical,incremental,operations,projections}.clj`.
These files use core collection operations and supplied operation callbacks.
The test supplies deterministic hash/graph/span callbacks and literal carriers;
it does not load `gravity.bootstrap`, read Gravity source or authenticate C2
products. Its existing assertions explicitly reject those authority claims.

The consumed runner dependency `bootstrap/clojure/src/gravity/digest.clj` is
also explicit: both the wiring and cache load it to compute identity hashes.
Changing its implementation must invalidate reuse even if its hash outputs
are preserved. All test code, runner code, ownership/backlog/dependency contracts and
`deps.edn` remain consumed inputs. Existing classpath ordering, `.cljc` and
compiled shadows, data readers, class inventories, runtime executable and
effective child configuration checks remain. The actual test JVM still starts;
only an unchanged complete selection may reuse a non-authoritative result.
The original producer revision and applying revision remain distinct.

Unrelated documentation or an unrelated existing test body is outside this
closure. A governing contract or any consumed test change invalidates it.
Adding/removing a dedicated test path changes the catalog inventory and also
invalidates it. This is no blanket exemption for docs or test changes.

## Necessary fresh work and remaining scope

The standalone current-source Reader, Core, MIR and Resolve controls remain
fresh. The compatibility test closures retain the complete-repository fallback:
their bootstrap/component loader dependencies have not been re-audited for
narrow reuse. Undeclared selections also execute fresh. This is conservative
invalidation, not a proof that every such repeat is necessary; further
source-grounded duplication discoveries remain work for this outcome.

Native/MIR owners retain their serializer, harness and source validation. The
failed PR421 source timeout is a nonpass on its original tuple. Changed Reader
and canonical dependencies justified that attempt; unchanged docs do not turn
it into accepted evidence. Public final-report/source-pin admission remains
open. C12 execution and the unresolved C7 publication operation remain outside
the authorized actions here.

## Completion and evidence

100% requires every inventoried redundant computation to be eliminated with
integration/regression evidence or proved necessary and classified outside
redundancy. It does not mean zero verification, a closed compiler pipeline or
a complete language. Unproved reuse prerequisites, unexplored conservative
fallbacks, independent review and integration are open boundaries, not credit.

The finite Reader closure/mapping increment is published in
[PR #432](https://github.com/burn2delete/uniform/pull/432). Independent code
review and separate actual-result readback found no blocking finding on
`6159eb721840e33fdda465f0d5dcd8a921415598`, tree
`123d1fc50b787cf51b0a08c79a4db53830d4b96b`.

| Observed scope | Result |
| --- | --- |
| Planner on the tested source tree | 59 tests / 518 assertions, zero failures/errors |
| Wiring, including all five nested-input invalidations and provenance controls | 43 / 398, zero failures/errors |
| Documents; language boundary and LOC gates | 240 documents / 19 indexes; 11 / 23, passed |
| Actual child producer, 256 MiB under the existing broker | 18 / 930, natural exit 0, non-authoritative |
| Unrelated documentation and existing test-body changes | Two child launches, both hits, `tests-executed? false`; original producer provenance retained and applying snapshots distinct |
| Changed consumed contract | Cache miss and real fresh 18 / 930, natural exit 0 |

Temporary control edits were restored and clean exact head/tree bookends
checked. Hit assertion totals describe the original producer, not newly run
assertions. The failed first harness report path, preliminary baseline command
error and initial catalog failures remain failed/unrun history. An unchanged
base one-test control reproduced the four orphan namespace catalog error;
their bodies were not executed for this mapping repair.

Normal reconciliation with main `48f81c4bf2d7e7ecb01905949586253b797aaf98`
produced `101d3bb4b08b528d326e2b4d6d8c67ca51194fbf`, tree
`21c8e05d286499f2a2190568deb762739b4fd25b`. Every consumed bootstrap,
contract, classpath declaration, backlog and ownership blob, and the dedicated
test path inventory, is unchanged from the tested source tree. The 13 incoming
documentation/tool paths exactly match main. Fresh scoped composition checks
passed 70 tests / 541 assertions plus documents 240 / 19. Independent review
accepted that composition and retained the original child evidence without
claiming a new child execution. This usage/evidence-only successor changes no
consumed input; it requires documentation validation and review, not another
compiler replay.

The original branch publication was denied and did not execute. After new
direct user publication/integration intent, one supported reassessment of the
same action succeeded; no actor or route substitution was used. This does not
waive any remaining restriction or grant compiler authority. No speedup, saved
JVM launch, proof, migration, release or self-hosting completion is claimed.

### Independent review correction: consumed digest implementation

Subsequent parent review found a blocking omission in `2982760e`: the Reader
closure alone omitted `gravity/digest.clj`, although both included runner
namespaces require it. The earlier peer reviews and actual child results above
remain historical evidence on the incomplete closure; they do not establish
that a digest implementation edit would invalidate those old results.

The narrow successor adds that exact production input, updates the expected
closure regression and adds a copied-repository control that changes only a
comment in the consumed digest file. That preserves its behavior and hash
outputs, but must reject the old hit, change the request key and execute the
new request. Corrected planner/wiring validation passed 103 tests / 922
assertions with zero failures/errors. Independent correction review and final
integration remain pending. No full compiler or heavy source replay is needed
for this runner-input correction.

### Current-main Runtime catalog integration

Main subsequently added `runtime_module_reads_test.clj` without a standalone
catalog mapping. A one-selector planner control reproduced that catalog error
without loading or running the Runtime provider. The existing source ownership
maps `runtime.gravity` to SH-19 / `:sh-runtime`; its owner confirmed that mapping
and required freshness. The namespace is now discoverable under that owner.

Its established source execution needs the common host broker, host-exclusive
and canonical-heavy guards. No committed route currently represents all three.
Plans therefore retain the namespace and explicitly mark execution unsupported.
The existing parallel runner recomputes that policy and rejects before
scheduling, broker acquisition or launch, even if a caller omits or forges the
shard marker or relabels the job exclusive. Owner-local guarded execution remains
separate; this mapping does not claim Runtime acceptance or authorize a generic
source run. No new runner or resource-limit substitution is introduced.

Planner validation passed 60 tests / 537 assertions. The exact injected runner
negative passed 1 / 13 with zero worker/launcher calls. The initial design that
rejected plan construction also blocked six legitimate discovery tests; it was
replaced by preserved plan discovery and rejection at actual execution admission.
That nonpassing intermediate result remains history, not acceptance evidence.
