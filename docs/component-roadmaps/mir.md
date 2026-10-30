# MIR component roadmap

Working name: **MIR** (proposed default, not an approved codename).
Semantic key: `mir`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler mir maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Provide target-independent verified intermediate code carrying the semantic facts required for safe optimization and lowering.

## Release outcome

Serializable verified modules/functions/blocks/operations for the governing MIR families, with dominance, type/effect/ownership/safety/origin and domain-anchor checks that gate every downstream consumer.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/090-c11-gravity-mir-specification.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own MIR construction, schema, verifier and fact tables. Domain-specific semantics, optimization choices, backend instructions and execution are outside this component. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Checked core plus type, effect, profile, capability, ownership/lifetime, safety and proof tables; validated semantic anchor references when present.
- Outputs: MIR module, metadata tables, runtime-check/proof references, serialization and verifier report/diagnostics.
- Hard prerequisites: Real checked-core facts are prerequisites for integrated construction. Domain anchors are explicit optional interfaces for programs using them, not a license to execute restricted domain work or a universal prerequisite for plain MIR.
- Parallel interfaces: Opt/Lower can use pinned verified MIR fixtures while upstream analyses mature; schema and diagnostic work need no completed build system.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/c11_mir/records.clj).
Inspected records map operation families to MIR opcodes; validation code has missing-fact diagnostics and fixture fallbacks. Presence of these structures does not prove complete generated control-flow verification. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

Implementation task authority: [Spec Kit ledger](../../specs/mir-workflow/tasks.md#implementation-tasks).
The following table is generated; edit the ledger and run
`clojure -M tools/mir_task_projection.clj --write`.

<!-- mir-task-projection:start -->
| Task | Status | Concrete exit evidence |
| --- | --- | --- |
| mir/1: Construct semantic MIR | Open; partial source evidence above is not closure | Lower checked-core control flow, calls/closures, data, memory/regions/resources, errors, FFI/concurrency and declared domain/workflow/AI operations without target-specific instructions. C11 construction criterion compiler/c11/semantic-mir-construction (Open): Construct the canonical target-independent MIR module/function/block/operation records from checked core, exposing control-flow and data-flow graphs and stable artifact identities. Cover the complete C11 families: constants/locals/block arguments; calls/tail calls/closures and direct/dictionary/vtable/dynamic dispatch; records/structs/tuples/enums/unions and field/index/slice/buffer operations; arithmetic/conversions; allocation/initialization/load/store/move/drop; region/arena and linear-resource lifecycle; branches/loops/matches and errors; FFI/host boundaries; atomics/locks/channels/tasks/synchronization; workflow suspension/resume/events; AI model/tool/memory/policy; domain anchors; runtime checks and proof/certificate references. Every operation has opcode, operands, result, type, effects/order, source/generated origin, profile and verifier status with applicable fact references; no-result operations use nil result and Unit or bottom as appropriate. Blocks have ordered operations and exactly one legal C11 terminator, including proof-qualified unreachable, profile-legal suspend/yield and legal tail call. SSA values or block arguments may represent joins, but public data-flow edges remain explicit. The module references actual checked core, profile, target request, functions/globals and metadata/anchor tables; functions retain parameters, return type, latent effects, entry/blocks and origin. A target request may constrain legality but cannot redefine opcode semantics. Required outputs: actual MIR/CFG/DFG, origin and anchor records plus operation-family/schema coverage fixtures under declared profiles. Reject malformed records/joins, missing required operation fields and target-specific instruction semantics in generic MIR with applicable C11-MODULE/BLOCK/TARGET-LEAK diagnostics. Positive construction produces verifiable artifacts, not a fabricated verifier success. |
| mir/2: Attach fact tables | Open; partial source evidence above is not closure | Bind every operation to current types, effects, capabilities, ownership, safety, runtime checks, proof and origin records. C11 fact-binding criterion compiler/c11/mir-fact-binding (Open): Before constructing admitted MIR, consume actual resolved/checked core and name/type/effect/profile/capability/ownership/safety products; unavailable required semantic facts cannot be deferred to a backend. Bind each module and operation to current source/compiler/module/pass identities and the applicable type, effect, ownership/lifetime, capability, safety-outcome, runtime-check, proof/certificate, source-origin, profile/target and domain-anchor tables. Safety-sensitive operations retain their check/proof/audited-unsafe references. A transformation preserves valid references or emits explicit invalidation/replacement records; stale references cannot be consumed until the required facts are regenerated and verified. Required outputs: actual fact/reference tables, operation-to-fact edges, invalidation/replacement and rejected-staleness fixtures. Accept fully bound checked inputs and correctly regenerated/reverified transformations; reject absent or mismatched core/fact/provenance, lost source/generated origin, stale proof/check/ownership or incompatible profile evidence with applicable C11-TYPE/EFFECT/SAFETY/ORIGIN/DOMAIN/VERIFY. Source obligations gravity/semantic-facts-before-mir and compiler/c11/mir-fact-binding share this input/reference boundary, not a new task. The actual upstream semantic producers retain their checks. C11 effect-order criterion compiler/c11/mir-effect-order (Open): Carry the actual effect set and required ordering constraints on each effectful operation and retain the corresponding semantic dependency edges. Accept transformations only with preserved or explicitly revalidated ordering; identical output values cannot justify silently reordered effects. Required outputs: operation/order graph and actual preserved/revalidated transformation fixtures. Reject a missing effect/order fact or an effectful reorder not permitted by the declared effect semantics with C11-EFFECT and the offending operation/source/fact context. Consume the actual Effect checker and transformation products without requiring whole-component completion. |
| mir/3: Verify graph and legality | Open; partial source evidence above is not closure | Check block termination, dominance, operand schemas, effect ordering, guard dominance, resource facts and valid domain anchors; reject missing or stale evidence. C11 verifier criterion compiler/c11/mir-verifier-gate (Open): Verify every block terminates, definitions dominate uses, operands/types match operation schemas, effect-order constraints are present, safety-sensitive operations have actual outcomes/check references, origins and profile constraints are valid, ownership/resource operations match C9 facts, runtime checks guard the intended operation, domain anchors are valid and target-specific opcodes occur only in target-lowering metadata. Required outputs: subject-bound actual verifier report, MIR diagnostic stream and accepted/rejected fixtures for every listed invariant, retaining module/function/block/operation, source span/origin, profile/target request, missing fact and remediation. Preserve C11-MODULE/BLOCK/DOMINANCE/TYPE/EFFECT/SAFETY/ORIGIN/DOMAIN/TARGET-LEAK/VERIFY code meanings. Positive fixtures verify valid typed/effected/safe MIR; every malformed-flow/fact/origin/profile/guard/anchor/target-leak fixture fails before optimization or target consumption. A metadata flag or syntactically present table is not an actual verifier result. |
| mir/4: Serialize stable modules | Open; partial source evidence above is not closure | Round-trip operations and shared tables with deterministic identity and preserve analysis invalidation hooks. C11 serialization criterion (Open): Round-trip actual module/function/block/operation records, explicit control/data-flow edges and shared metadata tables with deterministic identity and valid origin/provenance links. Preserve the domain anchor's domain/anchor ID, MIR operation links, external semantic artifact, equivalence-proof reference and fallback MIR subgraph for EFIR/schema/workflow/HDL/query/GPU anchors as applicable; a domain payload cannot replace its semantic anchor. Preserve fact invalidation/replacement hooks rather than silently renewing a stale table or proof. Required outputs: serialized MIR/table/anchor artifacts and actual round-trip identity/edge/invalidation comparisons. Reject lost or substituted table IDs, origins, graph edges, anchor/proof/fallback references or stale-state erasure with applicable C11-MODULE/ORIGIN/DOMAIN/VERIFY. This representation/identity contract does not make EML tree identity mathematical equality or independently validate a proof provider. |
| mir/5: Gate downstream consumption | Open; partial source evidence above is not closure | Run actual verifier on constructed and mutated MIR; retain positive and negative artifacts for malformed flow, missing facts, target leaks and invalid origins before Opt/Lower consumption. C11 consumption criterion gravity/mir-verification-after-transform (Open): Run the actual MIR verifier after construction and after each transformation that may invalidate invariants. Bind each result to the exact current module, transformation and fact identities; reject failed, missing or stale verification before public Opt/Lower consumption. Required outputs: before/after MIR, actual verifier reports, preserved/invalidated/recomputed fact records and positive/negative consumer-gate results. Runtime checks remain safety-referenced MIR operations and generic dead-code elimination cannot remove them; check elision consumes the applicable checked PERF10 evidence with subject binding and proof/guard dominance, or retains the check/rejects. Exercise invalidating transforms, malformed flow/missing types/effects/safety/origins, invalid profile facts, invalid anchors, unchecked source/core/MIR delivery and bounds-check erasure without surviving proof. Target-family consumption also uses actual declared profile/target/runtime-provider support; the backend cannot legalize rejected MIR or redefine generic op semantics. The owning optimizer/lowerer and proof checker supply their actual products; MIR gate ownership does not implement them. Ordinary source editing and negative-fixture preparation do not require release or migration admission, and these future execution requirements confer no current implementation acceptance. |
<!-- mir-task-projection:end -->

The tasks above are the canonical remaining list for this outcome. Close a row
only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, generated status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] No unverified or stale MIR reaches optimization or lowering.
- [ ] All governing operation families have structural and fact-preservation coverage; unavailable domain semantics are explicitly rejected, not fabricated.
- [ ] Runtime checks, source origins and proof references survive serialization and consumer handoff.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Verified-MIR handoff enables independent Opt and Lower integration; specialized domain semantics remain separately governed.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/090-c11-gravity-mir-specification.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
