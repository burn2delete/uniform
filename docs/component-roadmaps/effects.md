# Effects component roadmap

Working name: **Effects** (proposed default, not an approved codename).
Semantic key: `effects`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler effects maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Make direct, latent and transitive behavior explicit and reject effects outside declarations and profile authority.

## Release outcome

Effected core and a sound call/effect graph covering runtime and build effects, closure/dispatch propagation and replay requirements, consumed separately by capability/profile/safety validation.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/087-c8-effect-checker-design.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own effect inference/checking, propagation, ordering and replay obligations. Capability grants are not inferred from effects; provider authorization and runtime enforcement have separate owners. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Typed core, function/call graph, declared effects, macro build-effect records, profile rules and capability requirement mappings.
- Outputs: Effect graph, function/module summaries, ordering constraints, build/replay records, capability requirements and diagnostics.
- Hard prerequisites: Types/Core/Resolve identities are required for real call edges. Authorization/profile schemas are parallel contracts; no runtime deployment or package completion is needed to infer effects.
- Parallel interfaces: Authorization, profile and runtime owners can implement consumers from declared effect summaries; generated expansions can supply replayable build-effect fixtures.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/c8_effect_checker/facts.clj).
The inspected graph sets each node's transitive effects to its direct effects and function inferred effects to declared latent effects. This local projection is not evidence of complete interprocedural inference. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

<!-- effects-task-projection:start -->
| Task | Status | Concrete exit evidence |
| --- | --- | --- |
| effects/1: Infer direct effects | Open; partial source evidence above is not closure | Classify actual core operations including allocation, IO, FFI, errors, concurrency, model/tool/memory and compile-time behavior with source references. C8 computation/domain criterion compiler/c8/effect-computation compiler/c8/effect-domains (Open): Compute actual typed-core expression, function, namespace, module and macro-output direct/latent/transitive/build/replay/residual effects under L6; every operation beyond pure construction has effect facts or a diagnostic. Preserve concrete registered labels, thrown-error effects and source/generated identities rather than inferring behavior from declarations or backend/runtime behavior. Maintain distinct reader-extension/macro/compile-time/plugin/probe/generator build effects, emitted-program runtime effects and dependency-resolution/artifact-generation package/build effects. A build-effectful macro can emit pure code and a pure macro can emit runtime effects; neither domain substitutes for the other. Required outputs: source-bound effect graph with module/node identity, direct/latent/transitive/ordering/source facts, function declared/inferred/latent/throws table, namespace declared/inferred and module summaries, build log and residual report. Positive fixtures compute real operation and both macro-domain combinations; reject missing facts, unknown unregistered labels and hidden runtime/build effects with C8-UNKNOWN/BUILD/VERIFY. Preserve runtime checks, dynamic dispatch, fallback-provider calls, residual capability checks and replay logging remaining after proof/specialization, rather than treating a declaration or empty placeholder as inference. |
| effects/2: Propagate latent effects | Open; partial source evidence above is not closure | Compute closure, call, dispatch and recursive-function effect summaries to a valid fixed point; distinguish declared from inferred effects. C8 propagation criterion (Open): Derive function latent and transitive effects from actual resolved typed call/closure/dispatch/recursive edges, not by copying declared allowances. Preserve ordered evaluation and conservative branch alternatives under L6; every branch contributes to legality even when alternatives remain distinct for optimization. Function/namespace/module summaries distinguish declared, inferred and thrown-error effects; calls consume actual latent effects. Preserve L6 escaping versus handled labels, handler identity/residual effects and separately checked handler type/profile/capability/continuation/replay facts; handling does not erase original labels or grant authority. Required outputs: actual graph/fixed-point derivation, latent/throws and namespace/module tables and positive/rejected higher-order, recursive, dispatch, branch and generated-code fixtures. Reject underreported reachability, missing call/type facts, hidden handler residuals or declaration-only summaries before checked-core handoff. Recompute or invalidate affected summaries when actual calls, types, declarations or governing semantic inputs change; a structurally present graph is not evidence of sound propagation. |
| effects/3: Check legality and ordering | Open; partial source evidence above is not closure | Reject undeclared effects and forbidden profile behavior, expose capability requirements, and preserve sequencing constraints without granting authority. C8 legality/capability/order criterion compiler/c8/capability-proof compiler/c8/effect-order (Open): Check the complete applicable intersection of function and namespace declarations, profile, package manifest, build/deployment grants, actual runtime/provider support and safety mode before MIR construction. Effects and capability sets remain distinct: derive required capabilities from registered effects and connect actual use to explicit authority evidence binding effect, core source, capability, provider, grant scope/principal/phase and status; reject missing or insufficient scoped grants before MIR. Backends/runtimes may narrow support but cannot grant authority absent from source/package/deployment. Authority evidence is not a safety proof; profile/safety/runtime-selection/audit consumers retain their own checks. Emit sequence, may-commute, must-not-duplicate, must-not-eliminate, resource acquire/release bracketing, volatile/MMIO/atomic and replay-order constraints; actual optimizers consume them before reordering/fusion/elimination. Required outputs: per-effect legality intersection, capability-proof records, ordering constraints and accepted/refused declaration/profile/grant/provider/safety fixtures, including resource/volatile/MMIO/atomic/workflow/AI order cases. Reject type-only legality, effect-name-derived grants, authority-widening handlers/providers and missing ordering evidence with C8-UNDECLARED/PROFILE/CAPABILITY/RUNTIME/ORDER and contextual diagnostics. |
| effects/4: Record replay and invalidation | Open; partial source evidence above is not closure | Track nondeterministic/build effects and replay inputs, rejecting unreplayable hermetic behavior; invalidate summaries on call graph or declaration changes. Decision criterion interface/language/effects/record-or-replay-nondeterminism: In record mode perform only admitted actions and append typed records; replay consumes the matching recorded result without repeating its external action. Preserve the qualified R6 graph-safe case only where R7/R8 recorded-output obligations do not apply. Reject: Reject missing/mismatched/reordered records, ungranted live actions and live nondeterminism in recorded-output segments; graph declarations do not grant authority. Required outputs: Typed event/effect/replay schema and record-versus-replay traces, qualified R6 case and all missing-record/authority/R7-R8 refusals. C8 replay/audit criterion compiler/c8/replay-audit (Open): Emit source-bound replay/audit obligations for time, randomness, external IO, external database reads, workflow events, model calls, tool calls, :ai/human-review and scheduler-sensitive operations, including distributed/AI effects. Actual workflow/AI/runtime/test consumers satisfy those obligations; a profile forbidding replay gaps rejects missing records before target lowering. Keep build/runtime/package domains distinct and enforce actual hermetic build grants, refusing ungranted build actions without performing them during this source reconciliation. Preserve the existing record-versus-replay criterion and its qualified R6/R7/R8 scope. Required outputs: build-effect log, replay-effect requirements and residual replay obligations, invalidation records and actual accepted/refused nondeterminism/hermetic fixtures. Reject hidden macro runtime effects, absent required replay/audit records, unreplayable hermetic behavior and ungranted build effects with C8-BUILD/REPLAY; obligation names or supplied success flags cannot replace actual recorded consumer evidence. |
| effects/5: Integrate checked-core consumers | Open; partial source evidence above is not closure | Feed profile/capability/safety validation and MIR with real propagated facts, including generated-code and higher-order negative cases. Criterion behavior/pkg6/derived-capability-manifest: accept Derive deterministic package requests/denials/privileged summaries from actual effect analysis plus explicit declarations; include per-profile/target/dependency/tool/agent/FFI/unsafe/review/handle/audit metadata and provenance/SBOM links. Reject A summary differs from analyzed effects, omits a privileged surface or required capability/SBOM field, or treats a request as a grant. Emit Normalized capability manifest, derivation report, deny tables, dependency diff, deployment requirements, audit schema and SBOM capability fields; PKG6001/PKG6002/PKG6007/PKG6008. Dependencies: L6, L15, SAFE10, PKG6, R11. C8 checked-core/artifact criterion (Open): Pass actual current effect graph, latent/throws tables, namespace/module summaries, capability proof, build log, replay obligations, ordering constraints, residual report and diagnostics to checked-core/profile/capability/safety validation and MIR; preserve effect annotations through actual MIR/optimizer/runtime/tool consumers. Consume actual C7 typed calls, L6 effect and L15 grant contracts, L9 throws, L12 build/hermetic policy, active profiles and runtime/package/provider/deployment artifacts. Required outputs: versioned source-bound products and actual positive/negative producer-consumer fixtures proving direct/latent/transitive/build/residual computation, function/namespace declarations, profile rejection, capability acceptance/refusal, hermetic rejection, replay emission, all order families and MIR preservation. Validate artifacts and reject malformed/stale or missing semantic facts with C8-VERIFY rather than accepting graph/schema presence alone. Retain all nine diagnostic meanings C8-UNDECLARED/PROFILE/CAPABILITY/BUILD/REPLAY/ORDER/RUNTIME/UNKNOWN/VERIFY and core-node/span/generated-origin/function/namespace/effect/capability/profile/target/provider/grant/remediation context for every failure class. These are future execution requirements, not current compiler/runtime/replay/probe results or independent acceptance. |
<!-- effects-task-projection:end -->

The table above is generated from the sole editable
[implementation task ledger](../../specs/effects-workflow/tasks.md#implementation-tasks).
Edit that ledger, then run `clojure -M tools/effects_task_projection.clj --write`;
verify with `--check`. This transfer changes no task status or release criteria.
Close a task only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, generated status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] Transitive/latent effects reflect actual reachable calls, including recursion and dispatch.
- [ ] Effects never automatically grant capabilities; undeclared or profile-forbidden behavior rejects before lowering.
- [ ] MIR receives ordering/replay facts tied to current source and type identities.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Effected-core handoff joins profile, capability, ownership and safety results before MIR.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/087-c8-effect-checker-design.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
