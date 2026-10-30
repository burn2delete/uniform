# Tasks: Effects implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler effects maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `fcb23737be00f646a74200fc26faaa8908f77164`.

## Implementation tasks

This is the sole editable Effects implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/effects.md#current-evidence-and-remaining-work)
is generated from these records. IDs preserve the established phase/title slug
convention; the original `effects/1` through `effects/5` labels remain display
text. Order is display order, not dependency evidence. An unchecked box means
not accepted complete. Check a box only with explicit `Accepted;` status and an
immutable accepted-outcome receipt independently reviewed by the responsible
owner. Structural validation cannot grant acceptance. No task closes here.

<!-- effects-implementation-tasks:start -->

- [ ] `effects/current-evidence-and-remaining-work/effects1-infer-direct-effects` effects/1: Infer direct effects

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Classify actual core operations including allocation, IO, FFI, errors, concurrency, model/tool/memory and compile-time behavior with source references. C8 computation/domain criterion compiler/c8/effect-computation compiler/c8/effect-domains (Open): Compute actual typed-core expression, function, namespace, module and macro-output direct/latent/transitive/build/replay/residual effects under L6; every operation beyond pure construction has effect facts or a diagnostic. Preserve concrete registered labels, thrown-error effects and source/generated identities rather than inferring behavior from declarations or backend/runtime behavior. Maintain distinct reader-extension/macro/compile-time/plugin/probe/generator build effects, emitted-program runtime effects and dependency-resolution/artifact-generation package/build effects. A build-effectful macro can emit pure code and a pure macro can emit runtime effects; neither domain substitutes for the other. Required outputs: source-bound effect graph with module/node identity, direct/latent/transitive/ordering/source facts, function declared/inferred/latent/throws table, namespace declared/inferred and module summaries, build log and residual report. Positive fixtures compute real operation and both macro-domain combinations; reject missing facts, unknown unregistered labels and hidden runtime/build effects with C8-UNKNOWN/BUILD/VERIFY. Preserve runtime checks, dynamic dispatch, fallback-provider calls, residual capability checks and replay logging remaining after proof/specialization, rather than treating a declaration or empty placeholder as inference.

- [ ] `effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects` effects/2: Propagate latent effects

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Compute closure, call, dispatch and recursive-function effect summaries to a valid fixed point; distinguish declared from inferred effects. C8 propagation criterion (Open): Derive function latent and transitive effects from actual resolved typed call/closure/dispatch/recursive edges, not by copying declared allowances. Preserve ordered evaluation and conservative branch alternatives under L6; every branch contributes to legality even when alternatives remain distinct for optimization. Function/namespace/module summaries distinguish declared, inferred and thrown-error effects; calls consume actual latent effects. Preserve L6 escaping versus handled labels, handler identity/residual effects and separately checked handler type/profile/capability/continuation/replay facts; handling does not erase original labels or grant authority. Required outputs: actual graph/fixed-point derivation, latent/throws and namespace/module tables and positive/rejected higher-order, recursive, dispatch, branch and generated-code fixtures. Reject underreported reachability, missing call/type facts, hidden handler residuals or declaration-only summaries before checked-core handoff. Recompute or invalidate affected summaries when actual calls, types, declarations or governing semantic inputs change; a structurally present graph is not evidence of sound propagation.

- [ ] `effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering` effects/3: Check legality and ordering

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Reject undeclared effects and forbidden profile behavior, expose capability requirements, and preserve sequencing constraints without granting authority. C8 legality/capability/order criterion compiler/c8/capability-proof compiler/c8/effect-order (Open): Check the complete applicable intersection of function and namespace declarations, profile, package manifest, build/deployment grants, actual runtime/provider support and safety mode before MIR construction. Effects and capability sets remain distinct: derive required capabilities from registered effects and connect actual use to explicit authority evidence binding effect, core source, capability, provider, grant scope/principal/phase and status; reject missing or insufficient scoped grants before MIR. Backends/runtimes may narrow support but cannot grant authority absent from source/package/deployment. Authority evidence is not a safety proof; profile/safety/runtime-selection/audit consumers retain their own checks. Emit sequence, may-commute, must-not-duplicate, must-not-eliminate, resource acquire/release bracketing, volatile/MMIO/atomic and replay-order constraints; actual optimizers consume them before reordering/fusion/elimination. Required outputs: per-effect legality intersection, capability-proof records, ordering constraints and accepted/refused declaration/profile/grant/provider/safety fixtures, including resource/volatile/MMIO/atomic/workflow/AI order cases. Reject type-only legality, effect-name-derived grants, authority-widening handlers/providers and missing ordering evidence with C8-UNDECLARED/PROFILE/CAPABILITY/RUNTIME/ORDER and contextual diagnostics.

- [ ] `effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation` effects/4: Record replay and invalidation

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Track nondeterministic/build effects and replay inputs, rejecting unreplayable hermetic behavior; invalidate summaries on call graph or declaration changes. Decision criterion interface/language/effects/record-or-replay-nondeterminism: In record mode perform only admitted actions and append typed records; replay consumes the matching recorded result without repeating its external action. Preserve the qualified R6 graph-safe case only where R7/R8 recorded-output obligations do not apply. Reject: Reject missing/mismatched/reordered records, ungranted live actions and live nondeterminism in recorded-output segments; graph declarations do not grant authority. Required outputs: Typed event/effect/replay schema and record-versus-replay traces, qualified R6 case and all missing-record/authority/R7-R8 refusals. C8 replay/audit criterion compiler/c8/replay-audit (Open): Emit source-bound replay/audit obligations for time, randomness, external IO, external database reads, workflow events, model calls, tool calls, :ai/human-review and scheduler-sensitive operations, including distributed/AI effects. Actual workflow/AI/runtime/test consumers satisfy those obligations; a profile forbidding replay gaps rejects missing records before target lowering. Keep build/runtime/package domains distinct and enforce actual hermetic build grants, refusing ungranted build actions without performing them during this source reconciliation. Preserve the existing record-versus-replay criterion and its qualified R6/R7/R8 scope. Required outputs: build-effect log, replay-effect requirements and residual replay obligations, invalidation records and actual accepted/refused nondeterminism/hermetic fixtures. Reject hidden macro runtime effects, absent required replay/audit records, unreplayable hermetic behavior and ungranted build effects with C8-BUILD/REPLAY; obligation names or supplied success flags cannot replace actual recorded consumer evidence.

- [ ] `effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers` effects/5: Integrate checked-core consumers

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Feed profile/capability/safety validation and MIR with real propagated facts, including generated-code and higher-order negative cases. Criterion behavior/pkg6/derived-capability-manifest: accept Derive deterministic package requests/denials/privileged summaries from actual effect analysis plus explicit declarations; include per-profile/target/dependency/tool/agent/FFI/unsafe/review/handle/audit metadata and provenance/SBOM links. Reject A summary differs from analyzed effects, omits a privileged surface or required capability/SBOM field, or treats a request as a grant. Emit Normalized capability manifest, derivation report, deny tables, dependency diff, deployment requirements, audit schema and SBOM capability fields; PKG6001/PKG6002/PKG6007/PKG6008. Dependencies: L6, L15, SAFE10, PKG6, R11. C8 checked-core/artifact criterion (Open): Pass actual current effect graph, latent/throws tables, namespace/module summaries, capability proof, build log, replay obligations, ordering constraints, residual report and diagnostics to checked-core/profile/capability/safety validation and MIR; preserve effect annotations through actual MIR/optimizer/runtime/tool consumers. Consume actual C7 typed calls, L6 effect and L15 grant contracts, L9 throws, L12 build/hermetic policy, active profiles and runtime/package/provider/deployment artifacts. Required outputs: versioned source-bound products and actual positive/negative producer-consumer fixtures proving direct/latent/transitive/build/residual computation, function/namespace declarations, profile rejection, capability acceptance/refusal, hermetic rejection, replay emission, all order families and MIR preservation. Validate artifacts and reject malformed/stale or missing semantic facts with C8-VERIFY rather than accepting graph/schema presence alone. Retain all nine diagnostic meanings C8-UNDECLARED/PROFILE/CAPABILITY/BUILD/REPLAY/ORDER/RUNTIME/UNKNOWN/VERIFY and core-node/span/generated-origin/function/namespace/effect/capability/profile/target/provider/grant/remediation context for every failure class. These are future execution requirements, not current compiler/runtime/replay/probe results or independent acceptance.

<!-- effects-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Effects workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Migration owner: checked-IR documentation workstream.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare spec.md and plan.md with retained contract links and the canonical component task/acceptance anchors.
- [x] T002 [US2] Prepare tasks.md and evidence.md with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate the four owned artifacts, relative links/anchors, negative controls and existing document/roadmap checks; record results in evidence.md.
- [ ] T004 [US2] Independently review the exact candidate and record disposition in evidence.md or the linked PR.
- [ ] T005 [US2] Verify integration to main and record the merge receipt separately from local validation.

## Dependencies and outcomes

T003 follows artifact preparation; T004 follows successful validation; T005
follows independent acceptance. These five migration tasks are not a copy of
the [component implementation tasks](../../docs/component-roadmaps/effects.md#current-evidence-and-remaining-work).
No component implementation status is changed here. Existing compiler evidence
retains its original scope and source identity.

## New-name review

The new directory is semantic; contract planning IDs remain cross-references.
No legacy implementation renaming is required for this documentation slice.
