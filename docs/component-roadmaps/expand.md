# Expand component roadmap

Working name: **Expand** (proposed default, not an approved codename).
Semantic key: `expand`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler expand maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Expand compile-time language abstractions without losing hygiene, authority or provenance.

## Release outcome

A deterministic syntax-to-syntax macro engine with replayable build-effect authorization, explicit capture, generated-unsafe declarations and equivalent downstream checking of handwritten and generated code.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own macro environment, invocation, trace, hygiene propagation and expansion cache validation. Do not own Reader, runtime eval, type checking or package fetching. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Verified syntax graph, macro definitions/versions, namespace context, profile/target and explicit build grants/replay inputs.
- Outputs: Expanded syntax, expansion trace, generated-origin maps, capture/safety declarations, effect log and validated cache identity.
- Hard prerequisites: Syntax schema and macro binding/environment records are required interfaces. Namespace macro lookup can use declared module artifacts before the full Resolve release; no later Build completion prerequisite.
- Parallel interfaces: Resolve, Types, Effects and authorization consumers can work on recorded expansions and grants while the engine is implemented.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/macro_expansion.clj).
The current namespace describes a small hosted macro/template engine and compatibility projection, not canonical macro or self-hosting authority. It retains a max-expansion-depth setting requiring review under current accounting-removal policy. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

<!-- expand-task-projection:start -->
| Task | Status | Concrete exit evidence |
| --- | --- | --- |
| expand/1: Execute syntax macros | Open; partial source evidence above is not closure | Implement syntax-object invocation and macro environment/version checks, validating returned graphs and compile phases. |
| expand/2: Record hygiene and safety | Open; partial source evidence above is not closure | Preserve definition/call/generated origins, explicit capture and unsafe declarations; reject undeclared authority capture or missing unsafe metadata. |
| expand/3: Authorize compile-time effects | Open; partial source evidence above is not closure | Require declared grants, redacted logs and replay inputs for effectful expansion; reject ambient host effects and unreplayable hermetic expansion. |
| expand/4: Validate reuse and termination | Open; partial source evidence above is not closure | Bind cache reuse to every semantic input and replay record; diagnose actual expansion cycles/phase errors without node, size or work quotas. |
| expand/5: Integrate generated programs | Open; partial source evidence above is not closure | Feed generated core forms through the same resolution/type/effect/profile/safety path as handwritten forms and retain expansion trace comparison evidence. Source obligation `language/facet/migrate-stable-contract`: Emit compatibility and migration diagnostics, and feasible automatic rewrites, when stable facet source/IR/lowering/profile/artifact contracts change. Accept: For a stable facet change to source syntax, domain IR, lowering, profile support or artifact schema, emit version compatibility and migration diagnostics and records, plus an automatic rewrite where feasible; record an explicit feasibility decision when a rewrite cannot preserve semantics. Reject: Reject silent stable contract drift, missing compatibility/migration evidence and an unchecked or semantics-changing proposed rewrite. Required outputs: Old/new facet/version/schema identities, migration diagnostics/records, checked rewrite or justified infeasibility and positive/negative fixtures. Dependencies consume versioned facts/receipts from build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts, effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering; they do not require blanket component completion. Implementation status remains Open. Source obligation `language/facet/preserve-disclosure-boundary`: Preserve private witness/value/credential/proof disclosure labels and provenance across facet conversions unless a checked boundary explicitly declassifies them. Accept: Carry privacy labels, witness-generation provenance, reveal reason and public-output schema through conversions of private witnesses/encrypted values/credentials/proofs unless a checked boundary explicitly declassifies them. Facet IR exposes the relation, public/private split, custody, disclosure policy, provider trust and verification-artifact shape; test zk composition with schema/security/formal artifacts as source data. Reject: Reject an opaque proof/private-computation backend call or any erased custody/label/provenance/disclosure field; L14-PRIVACY-BOUNDARY includes witness/value ID, public schema, reveal reason when present and erasing edge. Required outputs: Typed facet privacy/declassification records, relation/custody/provider/verifier schema, preserved/erased-boundary fixtures and structured diagnostics. Dependencies consume versioned facts/receipts from auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts, effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering; they do not require blanket component completion. Implementation status remains Open. Source obligation `language/interop/emit-scoped-migration-shim`: Emit a migration shim with an explicit incumbent/Gravity boundary contract recording preserved, narrowed or rejected behavior, ownership, generated files, effects/authority, stability/owner and scoped parity evidence. Accept: Generate a shim for a declared native/host/schema/process/service incumbent boundary, naming incumbent source, Gravity namespace, preserved/emulated/narrowed/rejected behavior, effects/grants, safety, generated files, parity tests, owner and stability. Keep it temporary unless promoted; bind evidence to provider/schema/target/lock versions. Compare golden behavior, directional type round trips, errors, ownership/release, concurrency/blocking, performance where sensitive and unsupported-profile rejection; supply at least one incumbent-language/schema parity report. Reject: Reject obscured behavior loss, stale source/provider/schema/target/lock evidence, missing ownership/release/error mapping or failed incumbent parity; report L19-MIGRATION-PARITY. Required outputs: Generated shim/provenance, compatibility/deprecation records, scoped parity/round-trip/error/lifetime/concurrency reports and negative diagnostics. Dependencies consume versioned facts/receipts from runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects; they do not require blanket component completion. Implementation status remains Open. |
<!-- expand-task-projection:end -->

The table above is generated from the sole editable
[implementation task ledger](../../specs/expand-workflow/tasks.md#implementation-tasks).
Edit that ledger, then run `clojure -M tools/expand_task_projection.clj --write`;
verify with `--check`. This transfer changes no task status or release criteria.
Close a task only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, generated status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] Same pinned inputs and replay records produce identical expanded syntax and traces.
- [ ] Accidental capture, undeclared build effects and invalid generated syntax reject deterministically.
- [ ] Generated unsafe/effectful programs cannot bypass downstream checks; no well-formed program is rejected solely by project quotas.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Expanded-source handoff feeds Core/Resolve; generated-program legality is established with Types, Effects and safety/profile consumers, not by expansion alone.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
