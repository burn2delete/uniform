# Release implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

These five records are the sole editable task authority. IDs derive from the
existing component/section/title-slug convention; the original table had titles,
not literal individual IDs. No external dashboard object identity or new task
population is claimed. All rows remain Open; transfer is not implementation
acceptance. Original outcome and integration boundaries remain in the roadmap.

<!-- release-implementation-tasks:start -->
- [ ] `release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance` Bind each claimed component to exact implementation and conformance

Status: Open

Evidence baseline: [Overall roadmap](../../docs/implementation-roadmap.md)

Closing evidence: Component outcome receipts, positive/negative programs and explicit unsupported surface

- [ ] `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler` Complete stage compatibility/equivalence across the claimed compiler

Status: Open

Evidence baseline: [Equivalence contract](../../docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md)

Closing evidence: Fresh stage-to-stage outputs and diagnostics, residual trusted-code inventory

- [ ] `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution` Produce actual self-hosted compiler/runtime execution

Status: Open

Evidence baseline: [Binary release status](../../docs/phase-18-binary-distribution-and-seedless-release/README.md)

Closing evidence: Actually invoke the prior Gravity compiler process on the complete declared next-stage compiler/runtime/library/build closure, retain real outputs and logs/lineage, link/package and execute that next compiler on accepted and rejected compiler inputs without Clojure in candidate compilation/runtime/build; then public check/run/compile and output executable demonstrate absent seed boundaries. SH25 component construction, SH27 equivalence and final release acceptance remain distinct; supplied rebuild records or plans do not prove execution.

- [ ] `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom` Build reproducible signed distributions with provenance/SBOM

Status: Open

Evidence baseline: [Artifact provenance](../../docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md)

Closing evidence: Rebuild identity and signature/provenance verification plus tamper rejection

- [ ] `release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate` Obtain independent acceptance for the exact release candidate

Status: Open

Evidence baseline: [Binary release gates](../../docs/phase-18-binary-distribution-and-seedless-release/IMPLEMENTATION-ROADMAP.md)

Closing evidence: Coordinator-reviewed candidate receipts and explicit release disposition
<!-- release-implementation-tasks:end -->

## Historical workflow migration record

The entire original file follows unchanged as historical workflow evidence.
Its checkboxes and earlier task-source statements are separate from the
current implementation authority and do not imply product completion.

# Tasks: Release workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Release canonical remaining work](../../docs/component-roadmaps/release.md#remaining-tasks-and-evidence); do not copy its checklist here.

- [x] T001 [US1] Prepare the four owned package artifacts, preserving canonical
  contract, component and phase links. Evidence: the files in this directory.
- [x] T002 [US1] Validate all local links/anchors, ASCII, exact owned paths and
  preserved implementation status; retain the result in evidence.md.
- [ ] T003 [US2] Obtain independent review of the exact package candidate,
  including dependencies, boundary/nonclaims and semantic names.
- [ ] T004 [US1] Integrate the reviewed package and record its merge receipt.

## Dependencies and outcomes

T002 checks T001; T003 consumes the checked candidate; T004 consumes independent
acceptance. Parallel packages do not block this package's local preparation.
Component completion is not a prerequisite for its workflow migration, and
migration completion does not close the component's implementation tasks.
Partial work stays draft. No budget, task quota or repeated authorization gate.

## New-name review

Review `release-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
