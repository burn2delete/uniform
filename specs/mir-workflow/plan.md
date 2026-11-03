# MIR implementation planning entry

Select one unresolved [existing task](tasks.md#implementation-tasks), preserving
the historical transfer plan below. Read [spec.md](spec.md), C11 and the owning
source rule for each selected operation/fact. Resolve actual Gravity semantics,
temporary transport and current focused fixtures through
[development verification](../../docs/development-verification.md) and the
[stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

Name actual Core and analysis producers, operation families, CFG/data-flow/fact
tables, source/proof edges, verifier and Opt/Lower consumer. Provide real legal
construction plus targeted malformed-flow, dominance, stale/missing-fact,
ordering, check-guard, origin and target-leak mutations relevant to the slice.
Use an explicit fixture interface for local development and actual producers
for composition; do not accept a supplied passed report as verification.

Run focused owner checks and affected producer/verifier/consumer composition.
Record exact artifacts, serialization/invalidation observations and passed,
failed or unrun cases in the selected existing authority. Preserve C12 and
other recorded restrictions and reserved writers. Retain independent acceptance,
complete operation-family coverage and release as separate outcomes.

## Concurrency dependency interface

Use the [concurrency producer and consumer order](../legality-workflow/plan.md#concurrency-producer-and-consumer-order) and existing selected-result table. MIR3 consumes actual graph/fact tables and checked source obligations before its independent verifier result; Legality's later end-to-end preservation consumes that verdict. A passed field or copied proof is insufficient.

Task bodies/statuses, source criteria and qualified reviews stay unchanged. Current source/dependency entailment and implementation acceptance remain pending; preserve every actual hold.

## Complete MIR source criteria

Six preserved criteria from `c11-complete-current-facet-trace.edn` now refine
the existing five task records. Historical task source:
`bcb98c1c377cbdc79ed1e23a6814727a8f16254e`; trace SHA-256:
`6feb7ad5fc592ab66b5e29f46c8b703016f3dd4b82fcea3528291925be20c243`.
The tested GitHub context reference is unavailable; current canonical sections
below and the exact current task witnesses govern implementation without a
dead link or aggregate publication. All task IDs/statuses and historical
records remain unchanged. The earlier independent source review retains its
exact frozen subject and exclusions; current acceptance is pending.

| Existing criterion | Canonical conjunction and actual owning result |
| --- | --- |
| `compiler/c11/semantic-mir-construction` | The existing construction task implements [module](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md#module-shape), [function/block](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md#function-and-block-model) and [operation](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md#operation-schema) records and all [twenty operation families](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md#operation-families). Preserve actual CFG/data-flow edges, complete fields, legal terminators and applicable facts; no-result operations use nil with Unit/bottom as appropriate. Accept legal SSA/block-argument joins and reject malformed fields/flow and target-specific semantics in generic MIR. |
| `compiler/c11/mir-fact-binding` | The existing fact-table task consumes actual checked/resolved Core and current type/effect/profile/capability/ownership/safety facts before admitted MIR, as required by [D1](../../docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md#requirements) and [C11 tables](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md#metadata-tables). Bind all ten applicable metadata tables and operation references to actual source/compiler/module/pass inputs; preserve valid references or explicitly invalidate, regenerate and verify them. Reject absent, unrelated, substituted or stale facts and lost origins. Upstream producers retain semantic analysis. |
| `compiler/c11/mir-effect-order` | The same fact-table task carries actual effect sets, required ordering and semantic dependency edges under [C11 requirements](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md#requirements). Compare a preserving/revalidated transformation and an illegal reorder; identical return values cannot justify changed effects. Effects supplies actual ordering facts and the transformer supplies its decision; MIR neither invents those facts nor grants authority. |
| `compiler/c11/mir-verifier-gate` | The existing verifier task implements all [eleven C11 verification invariants](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md#mir-verification), with a subject-bound report and owning diagnostic for each actual accepted/refused fixture. Check termination, dominance, operand/type schema, effect order, safety, origins/profiles, C9 resource relationships, intended check guards, anchors and target independence. Reject before Opt/Lower; a table or supplied passed flag is insufficient. |
| `C11 serialization criterion` | The existing serialization task round-trips actual records, control/data-flow edges, shared tables, origins and [domain anchor fields](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md#domain-anchors), including applicable semantic-artifact, proof and fallback references. Preserve deterministic identity and invalidation/replacement hooks. Reject lost/substituted references, graph edges or stale-state erasure. This does not execute domain producers or establish mathematical equality from EML tree identity. |
| `gravity/mir-verification-after-transform` | The existing consumer-gate task invokes the actual verifier after construction and each potentially invalidating transform under [D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md#verification-obligations). Bind module/pass/fact inputs and refuse failed, missing or stale verification at the actual Opt/Lower boundary. [Runtime checks](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md#runtime-checks) survive generic DCE; elision requires applicable checked PERF10 evidence and dominance, otherwise retain/reject. Actual selected profile/target/runtime-provider support remains a separate consumed product. |

The verifier task owns the checker implementation; the consumer-gate task
reuses it and owns current-subject applicability and downstream admission.
Construction, reference binding and serialization do not create a second
verifier or fabricate a successful status. Actual positive and negative cases
preserve all [ten C11 diagnostic meanings and context fields](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md#diagnostics).
C15 infrastructure formats those owning failures; it does not decide MIR rules.
Native/network/call/EFIR and placeholder identities in schema examples are
illustrative values, not universal profile, effect, target or proof defaults.

Start local construction/checker/serialization work against declared checked-
Core/fact schemas and explicit fixtures. Composition consumes actual C6-C10
products, operation-to-fact/order edges, source/generated origins, supported
profile/target/provider dispositions and subject-bound verifier results.
Transforms expose preserved/invalidated/regenerated facts; failed or stale
results cannot reach the selected consumer. Lower may consume valid verified
unoptimized MIR. No full-component completion, fresh migration proof or release
admission is required merely to edit source or prepare negative fixtures.

The fourteen selected D1/D2/D9 MIR predicates retain actual pre-MIR facts,
MIR/CFG/DFG, verifier products, source preservation, malformed-type/effect/
profile refusals and proof-dependent check retention. They do not accept whole
foundation documents or Milestone 4's native/runtime/release obligations.
Seventy unrelated attached foundation facets retain separate scope. Domain
anchor representation and relationship checks do not authorize restricted C12,
provider, proof-checker, target-allocation or domain-producer execution.
Preserve every existing hold and the unresolved D1/C1/C6 pipeline qualification.

For the selected increment retain exact source/Core/compiler/pass/profile/
target/fact/check/proof/anchor/consumer identities, actual before/after graphs,
serialized forms, invalidation and verifier records, refusal diagnostics and
real boundary results. Focused source work, composition, source review,
independent acceptance, migration, integration and release remain distinct.

## Complete Core semantic interface

Use the [complete Core source criteria](../core-workflow/plan.md#complete-core-source-criteria). Consume actual verified and semantically checked Core, preserving source/control/order/metadata and real analysis decisions. A Core-node or passed-field projection is not required evidence of MIR legality.

Task identities/statuses, prior source/review subjects and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held execution or task transfer is authorized.

## Core dependency interface

Use the [Core producer and consumer order](../core-workflow/plan.md#core-producer-and-consumer-order) and existing selected-result table. Construct actual target-independent graphs/tables from checked Core and required facts, verify real subjects, round-trip complete tables/anchors and gate downstream consumption. Verifier availability precedes transforms; new-subject verdicts follow them.

Existing task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current selected-interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete Types source interface

Use the [complete Types source criteria](../types-workflow/plan.md#complete-types-source-criteria). Consume actual current typed/checked Core with function/dispatch/schema/dynamic/cast/ownership/layout facts and required semantic verdicts; preserve identities and invalidation rather than accepting a type-name table.

Current task identities/statuses, historical review subjects and original C7 publication/other actual restrictions remain unchanged. This source proposal grants no implementation or current independent acceptance.

## Types dependency interface

Use the [Types producer and consumer order](../types-workflow/plan.md#types-producer-and-consumer-order) and existing selected-result table. Bind actual current type/signature/instantiation/dispatch/conversion/schema/layout/resource facts and complete local/private/transfer premises to real MIR nodes/tables. Verify and round-trip whole subjects, rejecting stale or substituted facts and preserving checks before consumption.

Existing task bodies/statuses, source criteria, frozen review scopes and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete pattern-matching source interface

Use the [complete pattern-matching source criteria](../core-workflow/plan.md#complete-pattern-matching-source-criteria). Preserve actual decision/control/guard/binding/narrowing/coverage/effect/resource/schema products and per-pattern/branch origins from checked Core. Verify current complete references and required checks, not a skeleton or assigned semantic-passed field.

Task IDs/statuses, historical review scopes and actual restrictions remain unchanged. Current complete source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Pattern-matching dependency interface

Use the [pattern-matching producer and consumer order](../core-workflow/plan.md#pattern-matching-producer-and-consumer-order) and existing selected-result table. Construct, bind, verify and round-trip actual checked decision control and complete narrowing/coverage/effect/resource/validation/source tables before consumption. Reject stale/substituted products or reordered observable guards; future target/conformance results are later consumers.

Task bodies/statuses, source criteria, frozen review scopes and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete protocol and dispatch source interface

Use the [complete protocol and dispatch source criteria](../types-workflow/plan.md#complete-protocol-and-dispatch-source-criteria). Carry actual selected method/mode/implementation, effect/grant/profile/interface/host/schema/proof references through real graphs/tables, verification and serialization. Verify current owner references rather than complete-status labels.

Task identities/statuses, historical review scopes and actual restrictions remain. Current full source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Historical task-authority-transfer plan

# Implementation plan: MIR task authority

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler MIR maintainer.
Input revision: `d5d8e15c773929337e1d9646da4f32af2d461e17`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[C11 MIR specification](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md).
The [specification's canonical contracts](spec.md#purpose-and-canonical-contracts)
retain the original package navigation.
The workflow records tasks and evidence without replacing normative authority.

Retain the existing navigation and semantic ownership boundaries:

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/mir.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [MIR canonical remaining work](../../docs/component-roadmaps/mir.md#current-evidence-and-remaining-work) | mir/1 through mir/5; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/mir.md#acceptance-criteria-yesno) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-06-compiler-architecture/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: compiler MIR maintainer. Interface owners: [core](../../docs/component-roadmaps/core.md#release-outcome), [legality](../../docs/component-roadmaps/legality.md#release-outcome), [opt](../../docs/component-roadmaps/opt.md#release-outcome), [lower](../../docs/component-roadmaps/lower.md#release-outcome).
Real checked-Core facts are required for integrated MIR construction. Opt and Lower may use pinned verified fixtures while upstream analyses mature. Domain anchors apply only to programs using them; no restricted domain work is enabled.

## Approach and exact files

Move only the five real implementation records into the marked
[implementation region](tasks.md#implementation-tasks). Each preserves a stable
semantic ID, display title, unchecked checkbox, exact evidence/status and concrete
exit criteria. Preserve the full historical tasks file as a frozen suffix. Its
four migration tasks remain separate from compiler completion.

The roadmap links to the task ledger, which retains all admission and acceptance
wording, source links and milestones. No producer or consumer receipt
is imported as task completion. New task authority is an editing location, not
an admission result or a change to the requirement for checked producer facts.

## Validation and evidence

Run from repository root:

```bash
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Compare the preserved task criteria, IDs and historical suffix. Check the
affected Markdown documents for ASCII and file/heading links, preserve the
canonical-truth links and C11 reference, and run whitespace plus clean
exact-base/head/tree integration preflight. No compiler semantic suite is
required here.

## Delivery and remaining work

Independent review inspects the complete public payload and proposed PR body.
Publish a draft with exact identities and validation receipts for maintainer
current-head review. Normal integration follows maintainer disposition and
repository gates. All five implementation outcomes remain open; no source,
producer, consumer or final artifact acceptance follows from this transfer.

## Names, callers and compatibility

Preserve existing anchors, status/acceptance text, task identities and historical
pins. No compiler caller, runtime API, wire/schema identity, diagnostic, manifest
hash or load-order change is included. Semantic tool names follow the accepted
component entrypoint convention.
