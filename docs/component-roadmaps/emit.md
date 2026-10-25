# Emit component roadmap

Working name: **Emit** (proposed default, not an approved codename).
Semantic key: `emit`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler/backend maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Turn backend-ready inputs into real target artifacts with content identity and complete provenance.

## Release outcome

Actual emitted artifacts for the unchanged selected staged Linux LLVM release candidate, with validated manifests, reproducible content identities, source/debug maps and backend conformance evidence consumable by Build and Runtime. Hosted development artifacts retain their explicit seed/host boundary and are not substitutes for that candidate.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [artifact emission contract](../phase-07-backend-architecture/110-b13-artifact-emission-specification.md) and [backend conformance contract](../phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own backend output realization and artifact records, not source legality, runtime implementation, package publication or full hardware/OS target coverage. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Lowering-approved target instructions/plans and ABI/runtime/proof manifests, source/debug maps, compiler identity and dependency hashes.
- Outputs: Target bytes/files, content hashes, target artifact manifests, provenance graph, reproducibility records and backend conformance results.
- Hard prerequisites: Lower must establish legal backend input; Runtime supplies declared service/ABI contracts. Build consumes emitted records later, so completing the package system is not an emission prerequisite.
- Parallel interfaces: Backend adapters can implement fixed input/output contracts independently; Build may use fixture artifacts while real target emission is integrated.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_early/26010-artifact-emission-contract-and-backend-inputs.clj).
The inspected stage0 emission contract assembles backend artifacts and lists required hashes, source maps, provenance and conformance references. Listing required records does not demonstrate real executable output. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

| Task | Status | Concrete exit evidence |
| --- | --- | --- |
| emit/1: Reconcile selected target contracts | Open; partial source evidence above is not closure | Record exact target/ABI/runtime tuples, emitted kinds and supported operations for the existing staged Linux LLVM release candidate; distinguish hosted development artifacts, validate Lower input and reject undeclared targets. |
| emit/2: Emit real outputs | Open; partial source evidence above is not closure | Produce declared executable/object/library or hosted artifacts through the selected backend adapters, with explicit runtime linkage and surfaced tool failures. |
| emit/3: Bind provenance to bytes | Open; partial source evidence above is not closure | Hash actual outputs and attach source/compiler/dependency identity, source/debug maps, profile/effect/capability/safety/proof and ABI/runtime records. |
| emit/4: Establish conformance | Open; partial source evidence above is not closure | Execute accepted target fixtures and reject illegal or unsupported inputs; compare observable behavior and verify metadata preservation, tamper detection and reproducibility. |
| emit/5: Integrate artifact consumers | Open; partial source evidence above is not closure | Deliver actual artifact/manifests to Runtime and Build, retaining source-pinned receipts and distinguishing development binaries from seedless signed release artifacts. |

The tasks above are the canonical remaining list for this outcome. Close a row
only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, generated status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] Declared artifacts exist as real bytes and execute or validate in the target's appropriate conformance harness.
- [ ] Content IDs and provenance correspond to actual outputs; missing/tampered records reject.
- [ ] Declared matrix has positive and negative behavior evidence; nonselected targets receive no implicit completion credit.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Artifact handoff joins Runtime for executable vertical slices and Build for reproducibility/package integration; seedless release remains a separate aggregate gate.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Decision gate

Target-contract reconciliation is shared with Lower, not separately reinvented. Recommendation: preserve the staged Linux LLVM release candidate recorded by the [current migration cohort](../self-hosting-current-migration-cohort.md), keeping hosted development and its remaining seed/host boundary separate. Compiler/backend owner assigned or confirmed by Deus records the exact selected tuples before acceptance. Additional backends are separate extensions and cannot silently replace the selected release candidate.

## Traceability

[artifact emission contract](../phase-07-backend-architecture/110-b13-artifact-emission-specification.md) and [backend conformance contract](../phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
