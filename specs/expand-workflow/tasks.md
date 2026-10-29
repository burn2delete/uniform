# Tasks: Expand implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler expand maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Implementation tasks

This is the sole editable Expand implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/expand.md#current-evidence-and-remaining-work)
is generated from these records. IDs preserve the established phase/title slug convention; the original
`expand/1` through `expand/5` labels remain unchanged display text. Order is
display order, not dependency evidence. An unchecked box means not accepted complete. Check a box only with
explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by the responsible owner. Structural validation cannot
grant acceptance. No task closes through this authority transfer.

<!-- expand-implementation-tasks:start -->

- [ ] `expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros` expand/1: Execute syntax macros

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Implement syntax-object invocation and macro environment/version checks, validating returned graphs and compile phases.

- [ ] `expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety` expand/2: Record hygiene and safety

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Preserve definition/call/generated origins, explicit capture and unsafe declarations; reject undeclared authority capture or missing unsafe metadata. Decision allocation interface/safety/safe12/hygiene (Open). Role: macro expansion/hygiene maintainer. Prevent accidental or malicious privileged/user binding capture, allowing only marked traced capture with capability/policy checks for authority-bearing values. Refusals and conditional cases: Reject accidental or malicious capture of unsafe helpers, capability/provider/secret/raw-memory/compiler-internal or user bindings. Allow intentional capture only through marked operations in the expansion trace; authority-bearing capture additionally requires explicit capability and policy acceptance. Required outputs: Hygiene/capture records, marked intentional-capture trace, authority-policy verdict and privileged/user capture refusal diagnostics. Consume actual dependency interface outputs: auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.

- [ ] `expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects` expand/3: Authorize compile-time effects

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Require declared grants, redacted logs and replay inputs for effectful expansion; reject ambient host effects and unreplayable hermetic expansion.

- [ ] `expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination` expand/4: Validate reuse and termination

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Bind cache reuse to every semantic input and replay record; diagnose actual expansion cycles/phase errors without node, size or work quotas.

- [ ] `expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs` expand/5: Integrate generated programs

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Feed generated core forms through the same resolution/type/effect/profile/safety path as handwritten forms and retain expansion trace comparison evidence. Source obligation `language/facet/migrate-stable-contract`: Emit compatibility and migration diagnostics, and feasible automatic rewrites, when stable facet source/IR/lowering/profile/artifact contracts change. Accept: For a stable facet change to source syntax, domain IR, lowering, profile support or artifact schema, emit version compatibility and migration diagnostics and records, plus an automatic rewrite where feasible; record an explicit feasibility decision when a rewrite cannot preserve semantics. Reject: Reject silent stable contract drift, missing compatibility/migration evidence and an unchecked or semantics-changing proposed rewrite. Required outputs: Old/new facet/version/schema identities, migration diagnostics/records, checked rewrite or justified infeasibility and positive/negative fixtures. Dependencies consume versioned facts/receipts from build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts, effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering; they do not require blanket component completion. Implementation status remains Open. Source obligation `language/facet/preserve-disclosure-boundary`: Preserve private witness/value/credential/proof disclosure labels and provenance across facet conversions unless a checked boundary explicitly declassifies them. Accept: Carry privacy labels, witness-generation provenance, reveal reason and public-output schema through conversions of private witnesses/encrypted values/credentials/proofs unless a checked boundary explicitly declassifies them. Facet IR exposes the relation, public/private split, custody, disclosure policy, provider trust and verification-artifact shape; test zk composition with schema/security/formal artifacts as source data. Reject: Reject an opaque proof/private-computation backend call or any erased custody/label/provenance/disclosure field; L14-PRIVACY-BOUNDARY includes witness/value ID, public schema, reveal reason when present and erasing edge. Required outputs: Typed facet privacy/declassification records, relation/custody/provider/verifier schema, preserved/erased-boundary fixtures and structured diagnostics. Dependencies consume versioned facts/receipts from auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts, effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering; they do not require blanket component completion. Implementation status remains Open. Source obligation `language/interop/emit-scoped-migration-shim`: Emit a migration shim with an explicit incumbent/Gravity boundary contract recording preserved, narrowed or rejected behavior, ownership, generated files, effects/authority, stability/owner and scoped parity evidence. Accept: Generate a shim for a declared native/host/schema/process/service incumbent boundary, naming incumbent source, Gravity namespace, preserved/emulated/narrowed/rejected behavior, effects/grants, safety, generated files, parity tests, owner and stability. Keep it temporary unless promoted; bind evidence to provider/schema/target/lock versions. Compare golden behavior, directional type round trips, errors, ownership/release, concurrency/blocking, performance where sensitive and unsupported-profile rejection; supply at least one incumbent-language/schema parity report. Reject: Reject obscured behavior loss, stale source/provider/schema/target/lock evidence, missing ownership/release/error mapping or failed incumbent parity; report L19-MIGRATION-PARITY. Required outputs: Generated shim/provenance, compatibility/deprecation records, scoped parity/round-trip/error/lifetime/concurrency reports and negative diagnostics. Dependencies consume versioned facts/receipts from runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects; they do not require blanket component completion. Implementation status remains Open.

<!-- expand-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Expand workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: Frontend Spec Kit migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare `specs/expand-workflow/spec.md` and `plan.md` with retained contract and canonical task/acceptance links.
- [x] T002 [US2] Prepare `specs/expand-workflow/tasks.md` and `evidence.md` with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate all four artifacts, local links/anchors, ASCII and exact owned diff; record positive and negative checks in `evidence.md`.
- [ ] T004 [US2] Independently review the immutable package candidate and record scope and findings in `evidence.md`.
- [ ] T005 [US2] Integrate the reviewed documentation increment and record the verified PR/merge receipt in `evidence.md`.

## Dependencies and outcomes

T001/T002 prepare the package; T003 validates it; T004 reviews that candidate;
T005 integrates only that reviewed scope. This five-task denominator is workflow
migration, not a percentage of Expand implementation or language completion.
Implementation task status remains solely in [the canonical table](../../docs/component-roadmaps/expand.md#current-evidence-and-remaining-work).
No existing open component task becomes complete because this package exists.
Partial work remains visible in a draft PR.

## New-name review

Only semantic `expand-workflow` documentation paths are added. No code names
or compatibility IDs change; [semantic naming guidance](../../.specify/semantic-names.md)
continues to govern later implementation.
