# Lower component roadmap

Working name: **Lower** (proposed default, not an approved codename).
Semantic key: `lower`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler/backend maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Translate verified IR into explicit backend/ABI/runtime decisions without legalizing forbidden behavior.

## Release outcome

A lowering interface for the unchanged selected staged Linux LLVM release candidate that accepts verified MIR or declared verified domain IR, records eligible target/ABI/layout/provider decisions, preserves proofs and provenance, and rejects unsupported operations or unjustified target metadata. Hosted development paths retain their explicitly recorded seed/host boundary and do not replace that release target.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/093-c14-target-lowering-architecture.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own target eligibility, ABI/layout and provider selection, lowering decisions and backend-ready inputs. Emit owns byte/artifact production; Runtime owns services; hardware/OS platform extension is not silently included. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Verified optionally optimized MIR or verified domain IR, profile/target features, ABI policy, runtime/provider declarations, effect/capability/safety/proof evidence.
- Outputs: Lowering request, eligibility/layout/provider manifests, proof-to-target map, backend-ready artifact inputs, source maps and unsupported-feature diagnostics.
- Hard prerequisites: MIR acceptance is required; Opt is not mandatory when no optimization is requested. Runtime/provider and backend schemas are negotiated interfaces, not a cycle requiring completed releases of each.
- Parallel interfaces: Emit and Runtime can implement their sides of fixed manifests. Additional targets are separate declared extensions; they do not expand the accepted target matrix implicitly.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/c14_lowering/projection.clj).
The inspected projection hard-codes a hosted JVM target and emits a bytecode-plan hash with eligibility metadata. This is not evidence of executable target coverage or valid proofs for each metadata entry. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

Implementation task authority: [Spec Kit ledger](../../specs/lower-workflow/tasks.md#implementation-tasks).
The following table is generated; edit the ledger and run
`clojure -M tools/lower_task_projection.clj --write`.

<!-- lower-task-projection:start -->
| Task | Status | Concrete exit evidence |
| --- | --- | --- |
| lower/1: Validate lowering inputs | Open; partial source evidence above is not closure | Reject unverified/stale IR and incompatible profiles/features; expose explicit supported-operation and target matrices. P5: admit SIMD/intrinsics only with declared target features, numeric mode and alignment/layout evidence plus a valid fallback or rejection. A target wrapper does not justify unchecked intrinsics or change source legality; guarded variant selection and target lowering remain distinct interfaces. Foundation requirement gravity/multiversion-guard-manifest: For every dispatched backend variant emit target-feature guards, profile and numeric/safety constraints, fallback/rejection behavior and source-bound benchmark evidence; exercise each variant and guard failure. Refusals: Reject an unguarded unavailable instruction, widened profile, unjustified fast path, absent valid fallback or a benchmark from a different variant/target. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products; pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts. Foundation requirement gravity/target-lowering-operation-contract: For every MIR/domain operation in the declared input closure emit its explicit accept/lower/delegate/reject disposition with target, profile, ABI/provider and preservation obligations, and consume that disposition at target emission. Refusals: Reject an uncovered operation, stale/unverified IR, unsupported feature or delegate that widens effects/authority or drops source/safety/proof facts; emit an owning unsupported-operation diagnostic. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products; runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products. Criterion profile-gap/native/feature-guard (Open): Input: Native SIMD/intrinsic lowering or shipped variant with declared feature, numeric semantics, alignment/layout, portable wrapper or target namespace and fallback/rejection policy. Accept: Check feature-specific lowering eligibility and separately select a shipped variant only under guards matching target/numeric/layout assumptions; retain a valid fallback or explicit rejection. Unchecked intrinsics remain unsafe. Reject: Missing/unavailable target feature, implicit backend numeric behavior, unsupported alignment/layout, invalid guarded variant/fallback or unchecked intrinsic presented as safe. Required outputs: SIMD/target feature and numeric/layout/guard report, eligible/ineligible target and fallback fixtures, P5-SIMD/NUMERIC or lowering diagnostic with feature/provider/proof/origin context. Dependencies: backend-interface, opt/current-evidence-and-remaining-work/opt2-implement-structural-families, legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping native/feature-guard; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. |
| lower/2: Derive ABI and layout | Open; partial source evidence above is not closure | Implement calling conventions, data/closure/error/resource representations and FFI layouts from actual target rules, rejecting unrepresentable requests. |
| lower/3: Select explicit providers | Open; partial source evidence above is not closure | Match runtime services and providers to effects/capabilities/profile; reject hidden hosted services and authority-widening fallbacks. |
| lower/4: Preserve proofs and origins | Open; partial source evidence above is not closure | Map semantic proofs to target metadata only when assumptions hold, preserving source, safety and dependency records; reject proofless noalias/no-overflow/fast-math. Decision criterion interface/native/concurrency-order: Preserve declared task/thread/lock/message and atomic/fence ordering semantics and required provider/target facts through native lowering. Reject: Reject unsupported target order/atomic operations, detached invalid borrows, unsafe shared-state transformations and weakened ordering without lawful semantics. Required outputs: Native scheduler/atomic/lock/transfer and target-lowering records, ordering/lifetime preservation and unsupported-context refusals. |
| lower/5: Integrate target emission | Open; partial source evidence above is not closure | Feed Emit accepted backend-ready inputs and rejected unsupported-feature cases for the selected staged Linux LLVM release candidate; keep hosted development evidence separate, without claiming every platform. Foundation requirement gravity/native-target-artifact-emission: Emit a real C/LLVM native object or executable for D2's declared typed CLI slice from verified MIR, selected target ABI/runtime/provider records and complete source/compiler/artifact provenance; inspect and execute the claimed output where required by that slice. Refusals: Reject unchecked MIR, an unsupported operation/target, missing runtime/provider evidence and a metadata-only or nonnative output presented as the required native artifact. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products; lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs - Verified MIR/domain-operation disposition matrix and target/provider/preservation inputs; runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix - Locked source/compiler/provider/target build graph and canonical rebuild comparison. |
<!-- lower-task-projection:end -->

The tasks above are the canonical remaining list for this outcome. Close a row
only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, generated status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] Unsupported operations and unavailable providers reject before emission.
- [ ] Every ABI/layout and proof-sensitive target attribute has actual supporting evidence.
- [ ] Emit consumes manifests with complete source/profile/effect/capability/safety/dependency provenance.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Target handoff joins Emit and Runtime for the selected staged Linux LLVM candidate; hosted development remains a separate evidence boundary and a target change requires an explicit release decision.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Decision gate

Target-contract reconciliation: compiler/backend owner, assigned or confirmed by Deus, records exact target/ABI/runtime tuples and evidence requirements for the existing staged Linux LLVM release candidate. Recommendation: preserve that selection, as required by the [current migration cohort](../self-hosting-current-migration-cohort.md), and keep the hosted development boundary explicit. This is not a new target-selection decision. Any proposed target change needs a separate explicit disposition; existing metadata is not target conformance evidence.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/093-c14-target-lowering-architecture.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.

## Wider backend families

This page's selected Linux LLVM outcome does not discharge the other target
families. Their actual target adapters, ABI/layout and legality behavior are
owned by the named projects in [Backends](backends.md#release-outcome), which
consume this lowering interface without becoming blanket bootstrap prerequisites.
