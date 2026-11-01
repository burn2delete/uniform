# Tasks: Reader implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: Reader maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `c40d74aded3a019ff2bdeb1f4050cf95dbb11c1e`.

## Implementation tasks

This is the sole editable Reader implementation task ledger. The roadmap's
[finite remaining work](../../docs/component-roadmaps/reader.md#finite-remaining-work)
is navigation to these records, not a second editable truth. Stable semantic
IDs do not change with ordering. An unchecked box means not accepted complete;
the evidence/status text distinguishes open, partial and unassessed evidence.
Check a box only with an explicit accepted outcome and its immutable receipt in
the evidence/status field. No task closes through this authority transfer.
Order is display order, not a dependency edge or an overall completion score.
Evidence references to the linked pilot resolve to the unchanged
[accepted finite invocation evidence](../../docs/component-roadmaps/reader.md#accepted-finite-invocation-evidence).
The governing contracts and source leaves are linked in that same roadmap.


<!-- reader-implementation-tasks:start -->

- [ ] `reader/finite-remaining-work/contract-coverage-reconciliation` Contract coverage reconciliation

Current evidence/status: Open; source leaves above and the two governing contracts exist. Reader maintainer records each literal, abbreviation, namespace shape, extension and output against actual implementation and fixtures.

Done when (yes/no): Every required surface/output has a concrete implementation location and accepted/rejected fixture, or an explicit failing case in the implementation change; no omitted family can be marked complete.

- [x] `reader/finite-remaining-work/decode-and-source-identity-closure` Decode and source identity closure

Current evidence/status: Accepted; the [selected-current decoding and identity reconciliation](https://github.com/burn2delete/uniform/blob/codex/reader-decode-source-identity-20261027/docs/artifacts/phase-06/reports/current-reader-decode-source-identity.md) covers both extensions, mixed newlines and Unicode spans, explicit roots/options, five malformed UTF-8 families and a genuine second process in a different working directory. Exact `1d6c2681` / tree `7e77bab3` passed population 2 tests / 2,712 assertions and foreign-directory comparison 2 tests / 1,158 assertions; both exited naturally 0 with fresh Reader/envelope verification. Earlier foreign-directory failure and broker-admission nonexecution are preserved. Separate composed-tree controls passed 15 tests / 51 assertions on `07e5a32d` without source replay. Semantic identities match while real invocation-root provenance remains distinct. Public admission, other Reader rows and downstream C5/C14 acceptance remain open.

Done when (yes/no): Both extensions preserve actual paths; byte/scalar and line/column spans, explicit roots/options and UTF-8 failures match contracts; ambient working-directory changes cannot alter identity.

- [x] `reader/finite-remaining-work/lexical-products-and-origins-closure` Lexical products and origins closure

Current evidence/status: Accepted; the [finite current lexical/origin reconciliation](https://github.com/burn2delete/uniform/blob/codex/reader-lexical-origin-closure-20261027/docs/artifacts/phase-06/reports/current-reader-lexical-products-origins.md) records the complete enabled collection/literal/abbreviation/metadata population, exact/deferred facts, trivia filtering, graph/origin joins and malformed numeric rejection. Exact `8a0d6a90` / tree `79ed93ab` passed seven genuine Reader calls in two tests / 6,241 assertions with fresh Reader/envelope checks; pure source-identity controls passed 9 tests / 146 assertions. Separate compiler-free composed-tree controls passed 20 tests / 169 assertions on `3b7c1280`. The earlier escape-offset baseline remains a nonpass. PR478/482/492 retain their original complementary evidence. New suffix grammars, extension authorization/effects, quota/authentication closure, release and downstream acceptance remain open.

Done when (yes/no): All collection/literal/abbreviation/metadata families emit faithful products; malformed numeric candidates reject; valid deferred values retain exact spelling; trivia modes and generated origins round-trip as specified.

- [ ] `reader/finite-remaining-work/extension-policy-closure` Extension policy closure

Current evidence/status: Extension records/projected views exist; accepted authorization behavior not established here. The [bootstrap dispatch development increment](https://github.com/burn2delete/uniform/blob/f27f076231c056909e83a6dd59f662542cd4864f/specs/reader-workflow/dispatch-evidence.md) executes two implementation-owned handlers with focused grant/rejection controls; normal Reader integration and independent acceptance remain open.

Done when (yes/no): Registered pure and explicitly granted handlers work; unknown/disallowed tags and undeclared effects reject without ambient execution; invocation records identify policy and handler.

- [ ] `reader/finite-remaining-work/authentication-and-quota-removal-closure` Authentication and quota-removal closure

Current evidence/status: Partial; merged current invocation and default/source-drift/tamper controls are evidenced by the pilot. The complete Reader fixture population remains open.

Done when (yes/no): Original-source reconstruction rejects substituted/missing/duplicated graph references and stale hashes while accepting otherwise valid input without project accounting refusals. Real machine failure is reported as failure, not semantic rejection or acceptance.

- [x] `reader/finite-remaining-work/fresh-syntax-handoff` Fresh Syntax handoff

Current evidence/status: Accepted; selected-current, development-unadmitted boundary in the [finite family reconciliation](https://github.com/burn2delete/uniform/blob/codex/reader-fresh-syntax-row-20261027/docs/artifacts/phase-06/reports/current-reader-fresh-syntax-handoff.md). Exact candidate `bcf9c72e` / tree `6043bc1d` completed four genuine constructions and fresh owner controls; independent review accepted scoped attribution while preserving the combined natural-exit-1 context failure (26 tests, 733 passes, one failure). Separate compiler-free composition passed 19 tests / 106 assertions on the same identity. PR471/478 retain their original span/provenance and ratio evidence. Other Reader rows, canonical Syntax generated-graph coverage, public admission, C5 and C14 remain open.

Done when (yes/no): Fresh Syntax consumes actual Reader output for accepted and malformed/tampered cases, preserves spans/metadata/origins and reports owning-stage failures. No cached historical pass grants authority to this run.

- [ ] `reader/finite-remaining-work/component-release-evidence` Component release evidence

Current evidence/status: Open; the accepted finite invocation receipt is not the complete Reader release outcome.

Done when (yes/no): Owner supplies immutable source/fixture/runtime identities, complete surface results, diagnostic goldens, fresh handoff and residual-boundary statement; independent review and Deus disposition explicitly accept this outcome.

<!-- reader-implementation-tasks:end -->

## Historical migration record

The following original scaffold record is frozen history at the transfer
baseline, not current implementation tasks or a completion denominator. Its
pending review/integration text records what that scaffold stated; it is not
a fresh claim about the merged PR. The current authority above supersedes its
old navigation-only rule. Original artifact preparation and evidence remain
inspectable; no historical checkbox is silently reclassified.

### Tasks: Reader workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: Frontend Spec Kit migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare `specs/reader-workflow/spec.md` and `plan.md` with retained contract and canonical task/acceptance links.
- [x] T002 [US2] Prepare `specs/reader-workflow/tasks.md` and `evidence.md` with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate all four artifacts, local links/anchors, ASCII and exact owned diff; record positive and negative checks in `evidence.md`.
- [ ] T004 [US2] Independently review the immutable package candidate and record scope and findings in `evidence.md`.
- [ ] T005 [US2] Integrate the reviewed documentation increment and record the verified PR/merge receipt in `evidence.md`.

## Dependencies and outcomes

T001/T002 prepare the package; T003 validates it; T004 reviews that candidate;
T005 integrates only that reviewed scope. This five-task denominator is workflow
migration, not a percentage of Reader implementation or language completion.
Implementation task status remains solely in [the canonical table](../../docs/component-roadmaps/reader.md#finite-remaining-work).
No existing open component task becomes complete because this package exists.
Partial work remains visible in a draft PR.

## New-name review

Only semantic `reader-workflow` documentation paths are added. No code names
or compatibility IDs change; [semantic naming guidance](../../.specify/semantic-names.md)
continues to govern later implementation.
