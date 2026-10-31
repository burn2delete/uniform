# Tasks: Diagnostics implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

This marked region is the sole editable implementation task ledger. All four
outcomes remain open. Evidence baselines are references, not acceptance. The
linked component roadmap is navigation to this ledger. Historical migration
checkboxes below are separate from implementation completion.

<!-- diag-implementation-tasks:start -->

- [ ] `diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema` Reconcile stage carriers with canonical record schema

Status: Open

Evidence baseline: [Base carrier](../../bootstrap/clojure/src/gravity/diagnostics.clj), [records](../../bootstrap/clojure/src/gravity/c15_diagnostics/records.clj)

Closing evidence: Record/schema parity fixtures from each producer, including malformed-record rejection

- [ ] `diag/remaining-tasks-and-evidence/preserve-primaryrelatedgenerated-origins-and-stable-order` Preserve primary/related/generated origins and stable order

Status: Open

Evidence baseline: [Projection](../../bootstrap/clojure/src/gravity/c15_diagnostics/projection.clj)

Closing evidence: Macro and optimized-source diagnostics with original and generated locations

- [ ] `diag/remaining-tasks-and-evidence/close-textjsonredaction-and-explain-contracts` Close text/JSON/redaction and explain contracts

Status: Open

Evidence baseline: [CLI presentation](../../bootstrap/clojure/src/gravity/cli/diagnostic_presentation.clj)

Closing evidence: Golden text/JSON equivalence, secret redaction and known-ID explanation

- [ ] `diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift` Integrate runtime/build/editor consumers without rule drift

Status: Open

Evidence baseline: [Tooling contracts](../../docs/phase-13-tooling-and-developer-experience/README.md)

Closing evidence: Same underlying diagnostic identity across public consumers and failure paths

<!-- diag-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Diagnostics workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).
Migration owner role: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare the four owned artifacts with retained-contract links and exact canonical task anchors.
- [x] T002 [US1] Validate local targets/anchors, semantic names, ASCII, existing document/roadmap checks and negative controls; record results in evidence.md.
- [ ] T003 [US2] Independent review accepts the exact artifact tree, mapping and nonclaims; record disposition in evidence.md.
- [ ] T004 [US2] Integrate the reviewed change and record its merge receipt without claiming implementation completion.

## Dependencies and outcomes

T002 uses prepared artifacts; T003 uses validated exact content; T004 follows accepted review. These are documentation-migration tasks only. Implementation tasks remain at the links in plan.md, not copied into this ledger. Current implementation acceptance is unassessed by this package. Existing accepted finite increments retain their original scope and evidence.

## New-name review

All new paths use `diag-workflow` and ordinary artifact names. No planning-ID implementation name or compatibility identifier is changed.
