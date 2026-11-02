# Syntax implementation planning entry

Select one existing unresolved task from the [sole ledger](tasks.md#implementation-tasks)
against the implementation behavior in [spec.md](spec.md). Preserve the original
transfer plan below and all accepted finite producer/consumer evidence. Resolve
current semantic implementation and transport paths from the
[stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md) and the
actual call graph before choosing edits.

The selected plan must name its complete C3 form/context/graph population,
explicit Reader inputs, generated-producer context, required output and actual
Expand/Resolve consumer. Include concrete positive source/generated artifacts
and negative shape, identity, span, origin, capture, phase, stale-fact or
serialization cases applicable to that task. Preserve stable diagnostic IDs.
Tests must exercise construction/transformation/verifier behavior and relevant
actual composition, with exact provenance and rejected substitutions; a copied
schema or injected passed report is not the outcome.

Use the existing focused semantic runner and proportionate downstream checks
under [development verification](../../docs/development-verification.md).
Update only the selected existing task's evidence/status when warranted;
`speckit-tasks` must reconcile that authority instead of producing a competing
checklist. Preserve all ownership reservations and actual runtime restrictions.
Independent semantic acceptance and coordinator integration stay separate from
local checks, and full Syntax release remains bounded by the component outcome.

## Historical task-authority-transfer plan

# Plan: Syntax task authority and roadmap navigation

Baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Authority and compatibility

Move the five existing [remaining-work rows](../../docs/component-roadmaps/syntax.md#current-evidence-and-remaining-work)
verbatim to [implementation tasks](tasks.md#implementation-tasks). Preserve
all original heading anchors and task criteria. The roadmap links directly to
those records; historical scaffold tasks remain frozen separately.

The established IDs use `syntax/current-evidence-and-remaining-work/` followed
by `syntax1-construct-canonical-syntax`, `syntax2-preserve-transformations`,
`syntax3-invalidate-facts`, `syntax4-serialize-and-verify`, and
`syntax5-integrate-consumers`. The original Markdown provided display labels,
not literal full IDs. Preserve this derived identity convention rather than
renumbering or changing IDs when presentation order changes.

## Implementation

Edit the sole Syntax task ledger without changing Reader. Checked boxes require
explicit `Accepted;` status. Semantic receipt acceptance remains independent
review, not a formatting check.

## Validation

From repository root:

```bash
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

Check the five task IDs, Markdown file/heading links, ASCII and whitespace. No
compiler behavior or whole migration completion follows from these checks.
Obtain independent exact-candidate review before integration.

## Residual work

The five implementation tasks remain open. Their owning source, interface,
acceptance and release decisions remain unchanged. Future evidence-backed task
updates change the ledger; the roadmap continues to link to it.
Other components require their own migration; no aggregate denominator is added.
