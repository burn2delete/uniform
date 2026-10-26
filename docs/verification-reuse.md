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
| RV2 | `contracts/stage0-incremental-test-dependencies.edn`; `sh01_component_test_dependencies.clj`, `sh01_impact_test_planner.clj`, `sh01_development_loop_wiring.clj` and the latter two test files under `bootstrap/clojure/test/gravity/self_hosting/` | Re-audit the pure Reader artifact-identity facade, five nested files and test; bind existing runners/contracts/classpath/inventories/shadows/runtime. Demonstrate a real test producer, unrelated docs/test-body reuse with original provenance, consumed-input invalidation, and preserved fresh policies. | Implemented locally; validation/review/integration pending. No report, cache or receipt schema change. |
| RV2a | Same dependency contract; `docs/self-hosting-slice-ownership.edn`; planner test | Four existing non-slice namespaces block catalog construction on the inventory base. Add exact source-grounded owners and fresh policies for parameter Reader invocation, keyword scalar controls, staged Reader closure and token spelling. Select their existing bodies without running them as cache controls. | Baseline failure under investigation; mapping repair is a prerequisite to RV2 plan acceptance. |
| RV3 | `bootstrap/clojure/test/gravity/self_hosting/sh15_diagnostic_boundary_test.clj` | Reuse one genuine immutable upstream case for the same diagnostic reference tests; preserve independently constructed changed-source/ordinal cases and resolved verification. | Already integrated at `c3f35fef`; representative case and references are delayed, with an explicit build counter. No duplicate reconstruction of that accepted fix. |
| RV4 | RV1-RV3 records, exact candidate diff, execution receipts and independent review | Close each inventory row by integrated regression evidence or a source-grounded necessary-check decision. Preserve failed, unrun and stale boundaries. | Open until independent review and integration. |

## Exact Reader leaf closure

The opt-in is only `gravity.c2-artifact-identity-test`. Its production closure is
`bootstrap/clojure/src/gravity/c2_artifact_identity.clj` and
`c2_artifact_identity/{artifacts,canonical,incremental,operations,projections}.clj`.
These files use core collection operations and supplied operation callbacks.
The test supplies deterministic hash/graph/span callbacks and literal carriers;
it does not load `gravity.bootstrap`, read Gravity source or authenticate C2
products. Its existing assertions explicitly reject those authority claims.

All test code, runner code, ownership/backlog/dependency contracts and
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

Execution receipts and review disposition will be recorded for the immutable
candidate. No current-main pass, speedup, saved JVM launch, proof, migration,
release or self-hosting completion is claimed by this draft.
