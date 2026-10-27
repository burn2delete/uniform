# Reader

Working name: **Reader** (proposed descriptive default, not an approved brand).
Semantic key: `reader`. Status: implementation partial; finite invocation pilot
integrated, full Reader release outcome not accepted. Original source baseline:
`c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).

## Purpose and ownership

Turn declared source bytes into faithful, authenticated lexical products and
syntax seeds without executing source or making semantic legality decisions.
Owner role: Reader maintainer. Current owner: existing Reader implementation and
migration owners retain their reservations; this document does not reassign
code. Deus coordinates assignment confirmation, review and integration. Roadmap
editor: cloud component-roadmap lane. Syntax owns syntax-object completion;
Auth owns shared authentication mechanisms, not Reader parsing semantics.

## Release outcome

**reader/source-to-syntax-seeds**: the Uniform-authored Reader accepts the full
surface defined by [Surface syntax](../phase-01-core-language/011-l1-surface-syntax-specification.md)
and [Reader design](../phase-06-compiler-architecture/081-c2-reader-implementation-design.md),
emits their complete source/token/form/literal/seed/origin/hash products, and
rejects their malformed-input and unauthorized-extension cases. Fresh Syntax
consumption must preserve identity and original-source provenance. A release
receipt pins source, implementation, fixture population, runtime/seed boundary,
and acceptance results. Component completion is not whole-compiler self-hosting
or final seed retirement; any remaining host boundary must be named, not hidden.

## Scope and non-goals

In scope: UTF-8 decoding, both `.qst` and `.gravity`, actual supplied-path
provenance, explicit project-relative identity, all literal and collection
families, namespace-clause shape, abbreviations and origins, metadata, trivia,
registered extension dispatch, stable diagnostics, reproducible hashes and
fresh product authentication. Valid numeric spellings needing semantic judgment
(including `1/0`) retain exact components for later analysis.

Not in scope: expansion/hygiene assignment, name existence, types, effects,
profile legality, ownership, MIR, backend selection, full build/package service,
public seedless executable, or hardware/OS completion. Source byte/token/form/
node/depth/scalar limits that merely reject valid input are not acceptance
conditions. Preserve Unicode legality, representability, authentication and
actual machine protections. Historical accounting language in source contracts
is superseded by [current direction](../development-verification.md#remove-project-cost-and-budget-mechanisms).

## Inputs, outputs and interfaces

| Boundary | Actual contract |
| --- | --- |
| Source input | Bytes, declared encoding, explicit project-root identity, relative source path, actual supplied path/extension, reader options and enabled features; no implicit current-directory identity. |
| Extension input | Explicit tag-to-handler registry, declared build effects/capability grants and selected policy identity; empty registry is a valid development input. Build supplies the same contract later, not permission to start Reader. |
| Reader output | Source unit; tokens with exact spans/raw spelling/trivia; flat form graph; exact/deferred literal records; syntax seeds; abbreviation/generated origins; extension invocation records; diagnostics and deterministic hashes. |
| Syntax consumer | [Syntax](syntax.md#release-outcome) authenticates seed/source references, constructs syntax objects, preserves spans/metadata and owns syntax context/hygiene invariants. |
| Shared services | [Diagnostics](diag.md#release-outcome) formats stable Reader-owned failures; [Auth](auth.md#release-outcome) checks identity and tamper boundaries without substituting for original-byte reconstruction. |
| Tooling consumer | [CLI](cli.md#release-outcome) and formatter clients use supplied paths, trivia and diagnostic spans; they must not reinterpret malformed input. |

Existing artifact schemas, diagnostic identifiers and wire IDs remain unchanged
by this roadmap. New implementation names use `reader` and meaningful helper
names. Existing ID-bearing paths below are source locators, not naming examples.
Any later code rename needs an owner-scoped compatibility change, not a silent
protocol rename in a documentation patch.

## Original source-inspection baseline

These are source-inspection facts from the original baseline, not fresh
execution or acceptance results. Later accepted evidence is recorded below.

| Evidence | What exists; what it does not establish |
| --- | --- |
| [Uniform Reader source](../../bootstrap/gravity/src/gravity/bootstrap/reader.gravity) | Declares Reader entrypoints and source lineage, with staged execution bridges; the presence of bridges does not retire the seed. |
| [Surface Reader source](../../bootstrap/gravity/src/gravity/compiler/l1_c2_surface_syntax_reader.gravity) | Declares both source extensions, source-unit/diagnostic packaging and preserved facts; not an accepted whole-surface release. |
| [Source-unit leaf](../../bootstrap/clojure/src/gravity/source_unit.clj) | Owns extension metadata; explicitly does not own complete source identity or bytes. Records a Clojure seed boundary. |
| [Cursor leaf](../../bootstrap/clojure/src/gravity/reader_cursor.clj) | Skips whitespace/comments using a Clojure reader. Explicitly does not parse forms or build artifacts. |
| [Primitive leaf](../../bootstrap/clojure/src/gravity/reader_primitives.clj) | Classifies already-read values and excerpts, not source parsing. |
| [Product projection](../../bootstrap/clojure/src/gravity/c2_reader_product_projection.clj) | Projects already-produced products and explicitly disclaims reading, authentication and self-hosting authority. |
| [Source coverage fixture](../../bootstrap/clojure/test/gravity/self_hosting/sh07_reader_source_coverage_test.clj) and [surface coverage fixture](../../bootstrap/clojure/test/gravity/self_hosting/sh07_l1_c2_surface_reader_source_coverage_test.clj) | Pinned censuses and source-path expectations exist; assertions and historical counts do not prove current execution or complete compiler population. |
| [Current migration cohort](../self-hosting-current-migration-cohort.md) | First source-to-Core endpoint is proposed. The six frontend sources are not the whole required compiler population. |

## Accepted finite invocation evidence

The [Reader-to-Core pilot tasks](../../.specify/reader-core-invocation/tasks.md)
and [evidence](../../.specify/reader-core-invocation/evidence.md#minimal-affected-consumer-closure-and-final-verification)
record the completed selected increment, without duplicating this component's
release checklist. Real paired source construction, fresh public Core
verification, coherent substitution rejection, source/context controls and
minimal existing-consumer compatibility were independently reviewed.

[PR #388](https://github.com/burn2delete/uniform/pull/388) integrated that finite
outcome at `001ab9ed`; [PR #381](https://github.com/burn2delete/uniform/pull/381)
integrated its Spec Kit receipt. The
[verified integration record](../../.specify/reader-core-invocation/evidence.md#verified-integration-receipt)
preserves exact source/test trees, merge identity and residual boundaries.
Historical defaults remain fail-closed; current-source invocation grants no
migration or release authority. This is not complete Reader surface/extension
coverage, full SH-07/C01, a full compiler-owner census or self-hosting acceptance.

## Finite remaining work

This table is a generated compatibility projection of the sole editable
[Spec Kit implementation tasks](../../specs/reader-workflow/tasks.md#implementation-tasks).
Do not edit its rows directly. Run `clojure -M tools/reader_task_projection.clj --write`
after editing the task ledger, and `--check` to reject drift. Aggregate roadmaps
link this outcome, not copies of this list. `Open` means completion evidence has not been admitted,
not that no implementation exists. The scoped receipt updates partial evidence;
no full Reader release task is closed by this navigation change.

<!-- reader-task-projection:start -->
| Task | Current evidence/status | Done when (yes/no) |
| --- | --- | --- |
| Contract coverage reconciliation | Open; source leaves above and the two governing contracts exist. Reader maintainer records each literal, abbreviation, namespace shape, extension and output against actual implementation and fixtures. | Every required surface/output has a concrete implementation location and accepted/rejected fixture, or an explicit failing case in the implementation change; no omitted family can be marked complete. |
| Decode and source identity closure | Accepted; the [selected-current decoding and identity reconciliation](https://github.com/burn2delete/uniform/blob/codex/reader-decode-source-identity-20261027/docs/artifacts/phase-06/reports/current-reader-decode-source-identity.md) covers both extensions, mixed newlines and Unicode spans, explicit roots/options, five malformed UTF-8 families and a genuine second process in a different working directory. Exact `1d6c2681` / tree `7e77bab3` passed population 2 tests / 2,712 assertions and foreign-directory comparison 2 tests / 1,158 assertions; both exited naturally 0 with fresh Reader/envelope verification. Earlier foreign-directory failure is preserved. Semantic identities match while real invocation-root provenance remains distinct. Public admission, other Reader rows and downstream C5/C14 acceptance remain open. | Both extensions preserve actual paths; byte/scalar and line/column spans, explicit roots/options and UTF-8 failures match contracts; ambient working-directory changes cannot alter identity. |
| Lexical products and origins closure | Partial Reader and projection sources. The focused token-spelling reuse increment and its validation limits are recorded in [Reader evidence](https://github.com/burn2delete/uniform/blob/codex/reader-token-spelling-reuse-20261026/specs/reader-workflow/evidence.md#token-spelling-reuse); it does not close the complete lexical surface. | All collection/literal/abbreviation/metadata families emit faithful products; malformed numeric candidates reject; valid deferred values retain exact spelling; trivia modes and generated origins round-trip as specified. |
| Extension policy closure | Extension records/projected views exist; accepted authorization behavior not established here. | Registered pure and explicitly granted handlers work; unknown/disallowed tags and undeclared effects reject without ambient execution; invocation records identify policy and handler. |
| Authentication and quota-removal closure | Partial; merged current invocation and default/source-drift/tamper controls are evidenced by the pilot. The complete Reader fixture population remains open. | Original-source reconstruction rejects substituted/missing/duplicated graph references and stale hashes while accepting otherwise valid input without project accounting refusals. Real machine failure is reported as failure, not semantic rejection or acceptance. |
| Fresh Syntax handoff | Accepted; selected-current, development-unadmitted boundary in the [finite family reconciliation](https://github.com/burn2delete/uniform/blob/codex/reader-fresh-syntax-row-20261027/docs/artifacts/phase-06/reports/current-reader-fresh-syntax-handoff.md). Exact candidate `bcf9c72e` / tree `6043bc1d` completed four genuine constructions and fresh owner controls; independent review accepted scoped attribution while preserving the combined natural-exit-1 context failure (26 tests, 733 passes, one failure). Separate compiler-free composition passed 19 tests / 106 assertions on the same identity. PR471/478 retain their original span/provenance and ratio evidence. Other Reader rows, canonical Syntax generated-graph coverage, public admission, C5 and C14 remain open. | Fresh Syntax consumes actual Reader output for accepted and malformed/tampered cases, preserves spans/metadata/origins and reports owning-stage failures. No cached historical pass grants authority to this run. |
| Component release evidence | Open; the accepted finite invocation receipt is not the complete Reader release outcome. | Owner supplies immutable source/fixture/runtime identities, complete surface results, diagnostic goldens, fresh handoff and residual-boundary statement; independent review and Deus disposition explicitly accept this outcome. |
<!-- reader-task-projection:end -->

## Acceptance checklist

All answers must be **yes** for the declared release outcome:

- Does the executable Uniform Reader cover every governing surface family and
  both extensions, with accepted and rejected fixtures and actual output?
- Do source identity, exact spans, trivia modes, literal deferment and generated
  origins survive the Reader-to-Syntax interface?
- Do malformed input, graph tampering and unauthorized extension effects fail
  with stable, source-located diagnostics rather than host parser leakage?
- Are valid inputs free from project-cost and arbitrary capacity refusals,
  while intrinsic semantic and authentication checks remain effective?
- Is the complete fixture/source population pinned, with fresh execution and
  host/seed boundaries distinguished from whole-compiler release claims?
- Has independent review accepted that exact evidence and the coordinator
  recorded the outcome, rather than inferring it from test-count totals?

## Prerequisites, parallel work and integration

Prerequisites for semantic work: stable surface/source-unit contracts, a usable
existing seed execution path, declared source input and owner-safe code paths.
Neither the future Build component nor final compiler self-hosting is a start
prerequisite. A direct invocation with explicit source/policy records suffices.

Parallel work: Syntax can implement its consumer against pinned seed fixtures;
Diagnostics can implement rendering against fixed failure records; Build can
produce the explicit input contract; Auth can implement shared verification.
Their fixtures do not count as successful composition until the real handoff.

Integration milestones: (1) source-to-seed contract and semantic fixture closure;
(2) fresh Reader-to-Syntax accepted/rejected composition; (3) Reader outcome
receipt accepted for its stated seed/runtime boundary; (4) consume that receipt
in the wider source-to-Core and compiler rebuild outcomes. Later seed retirement
revalidates the relevant boundary; it does not retroactively relabel a hosted
result or block ordinary Reader development.

Decision gate: Reader and Syntax maintainers, coordinated by Deus, confirm the
seed/object boundary before changing their interface. Recommendation: Reader
owns bytes, lexical/form products and seed origins; Syntax owns context/hygiene
and complete syntax-object invariants, matching the canonical architecture.
Accepting that recommendation or an explicitly reviewed contract amendment
closes the decision; an unowned future redesign is not a finish line.

## Traceability only

Reader maps to SH-03 and contributes source fixtures to SH-07; Syntax owns
SH-04. SH-25 through SH-29 aggregate complete component/rebuild/release evidence
elsewhere. The mapping is not a task partition, a new status authority or a
claim that a Reader census completes SH-07. See the
[overall outcome rollup](README.md#integration-outcomes).
