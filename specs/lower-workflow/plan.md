# Target lowering implementation planning entry

Select one unresolved [existing task](tasks.md#implementation-tasks), preserving
the transfer plan below. Read [spec.md](spec.md), C14/B1 and the actual selected
backend/runtime/ABI and operation contracts. Locate current semantic source,
temporary adapters and focused tests through
[development verification](../../docs/development-verification.md) and the
[stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

Name verified input/fact identities, supported operation, exact target/ABI/
runtime tuple, provider/grants, target assumptions and actual Emit consumer.
Provide one real legal lowering and relevant stale/unverified/unsupported,
layout/service/authority or proof-metadata negatives. Observe emitted decisions,
diagnostics and preserved check/source/proof edges. Fixtures may establish a
local interface, while acceptance of actual code generation needs the selected
real adapter/toolchain and services.

Run focused owner tests and affected MIR/Opt/Emit/Runtime composition; preserve
existing domain and execution restrictions. Refine only the selected task,
recording exact passed, failed or unrun cases and residual target/host bounds.
Do not replace actual layouts or helper linkage with manifest assertions.
Independent acceptance, other target families and release remain separate.

## Compile-time evaluation dependency interface

Use the [compile-time evaluation producer and consumer order](../expand-workflow/plan.md#compile-time-evaluation-producer-and-consumer-order) and existing selected-result table. Expose truthful fixed target/ABI/alignment rules before eligible queries, then derive Q layout from actual solved Q types and values. A constant query cannot wait for its own future layout; target representation and canonical data encoding remain separate.

Task bodies/statuses and finite review subjects stay unchanged. Current source/interface entailment and independent acceptance remain pending; preserve all actual holds.

## Provider and capability source interface

Use the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria). Preserve actual grant/provider/scope/phase/lifetime/check/source facts in emitted service hooks, invalidating changed assumptions. Lowering cannot widen policy or substitute metadata for an executing boundary.

Historical finite reviews remain scoped; current whole-source/interface review and implementation acceptance remain pending. Preserve all actual holds and existing task authority.

## Concurrency dependency interface

Use the [concurrency producer and consumer order](../legality-workflow/plan.md#concurrency-producer-and-consumer-order) and existing selected-result table. Lower4 consumes actual verified MIR, current facts, compatible target/provider contracts and affected revalidation, preserving valid unoptimized MIR. Actual target operations precede backend artifacts and later end-to-end checks.

Task bodies/statuses, source criteria and qualified reviews stay unchanged. Current source/dependency entailment and implementation acceptance remain pending; preserve every actual hold.

## Complete concurrency source interface

Use the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria). Lower4 preserves actual task/synchronization/order/fence/source/proof products and invalidates changed assumptions before target consumption. Historical finite reviews remain scoped; current source composition and implementation acceptance remain pending. Preserve every held operation.

## Foundation source criteria

Three preserved clauses from the
[foundation context](https://github.com/burn2delete/uniform/blob/df11890b21f71d3f246cc47e9f63d866239a0d68/specs/requirement-reconciliation/foundation-residual-interfaces.edn)
now refine two existing Open tasks; no target selection or task identity changes.

| Existing requirement | Canonical conditions and existing outcome |
| --- | --- |
| `gravity/target-lowering-operation-contract` | Input validation enumerates the declared MIR/domain input closure and its accept/lower/delegate/reject dispositions under [D1 lowering requirements](../../docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md#requirements) and [C14 eligibility](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md#eligibility-checks). The real emitter consumes target/profile/ABI/provider and preservation decisions; reject uncovered operations, stale IR and illegal delegation. |
| `gravity/multiversion-guard-manifest` | The same input task records and exercises each emitted variant's feature dispatch, profile/numeric/safety guards, legal fallback or rejection and source-bound benchmark identity under [D6 multiversioning](../../docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md#optimization-families). Reject unavailable instructions, widened profiles, unjustified fast paths and mismatched variant/target measurements. Benchmark success cannot supply missing correctness or proof. |
| `gravity/native-target-artifact-emission` | Target-emission integration consumes verified MIR and actual ABI/runtime/provider inputs to emit the typed native CLI slice and required provenance from [D2 M4](../../docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md#milestone-4---mir-and-native-lowering). Inspect and execute the claimed artifact through actual Emit/backend/Runtime consumers, and reject metadata-only output, unsupported operations/targets and absent services/proof. |

The selected Linux LLVM compiler release path is unchanged; the C/LLVM wording
in D2 does not adopt a second release target or promote hosted output to native.
Use verified MIR and actual pass/domain facts, Legality outcomes, Runtime
service contracts and Compiler API's pass-evidence products as listed in the
clauses. Preserve actual emitted bytes/hashes, operation-to-disposition and
proof-to-target maps, source/generated origins, target/runtime/provider and
checker identities, guard/fallback outcomes and unsupported diagnostics. Local
interfaces permit focused construction; actual production and consumption are
required for composition. Existing C12/native/provider execution holds remain.

## Complete target-lowering source criteria

Five preserved criteria from `c14-complete-current-facet-trace.edn` refine
the existing five task records. Historical task source:
`e94de7388589c8251f40fbb1da4a6af6da6f5918`. The tested historical GitHub
trace reference is unavailable; current canonical sections and exact current
task witnesses supply the implementation inputs without a dead link or
aggregate publication. Task identities, statuses and prior criteria remain
unchanged. The earlier source review retains its frozen subject and excludes
later-head acceptance; independent current review is pending.

| Existing task | Canonical conditions and actual required evidence |
| --- | --- |
| Input validation | Validate the actual [lowering request](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md#lowering-request), current verified IR and every [eligibility condition](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md#eligibility-checks). Retain machine-readable operation/feature/profile acceptance and refusal, including unsupported features and their alternatives. Missing, stale or supplied-success evidence cannot admit the input. |
| ABI/layout derivation | Derive every [ABI/layout representation](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md#abi-and-layout) and consume the selected [PERF4 boundary facts](../../docs/phase-04-performance-model/062-perf4-memory-layout-optimization-design.md#requirements). Preserve actual typed fields, alignment, padding/packing, alias/ownership, device-transfer and source/debug records. Exercise safe/unsafe target alignment and packing, initialization/padding-byte preservation, host/device mismatch and real consumer access. |
| Provider selection | Explicitly select or refuse all [service families](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md#runtime-and-provider-selection), binding actual target/profile/effect/capability/safety support. Exercise missing/forbidden services, hidden hosted dependencies and authority-widening fallback. A provider name neither implements the service nor grants authority. |
| Proof/origin preservation | Bind every [proof-to-target metadata entry](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md#proof-to-metadata-map) to actual operation, target, current assumptions and checked evidence. Exercise absent, stale, mismatched and invalidated proofs. Missing optional optimization evidence cannot erase required volatile, atomic or source safety semantics. |
| Target-emission integration | Deliver accepted backend-ready input and complete [artifact manifests](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md#emitted-artifact-manifest) to actual Emit/backend producers and package consumers. Retain real artifacts/content hashes and complete [unsupported/fallback records](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md#unsupported-features); reject incomplete manifests, hidden services, widened authority and undefined-behavior paths. |

The five criteria jointly preserve all [ten C14 diagnostic meanings and context
fields](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md#diagnostics).
The selected layout scope retains PERF4-LAYOUT/ALIGN/PACKED/DEVICE/PROOF
conditions and type/field/span/target/profile/boundary/transform/missing-proof
context. Examples such as kernel/LLVM/x86/SSE2 and Particle/F32/SoA/4/64 are
illustrative, not target, type, strategy or alignment defaults.

Lower consumes upstream layout transformation correctness, alias/ownership,
address-identity and boundary evidence. Opt owns the transformation decisions;
the proof checker owns proof validity; Runtime/Auth supply actual service and
authority products. Lower owns ABI/layout selection and preserving these facts
for Emit and backend/package/debugger/serializer/safety consumers. Emit/backend
owners produce target bytes; Lower integration verifies the actual handoff and
consumption. This creates no second optimizer, proof checker, emitter or task
authority. The 23 selected PERF4 predicates do not accept the whole layout
optimizer, its transformation families, cache claims or every diagnostic.

Start focused construction and refusal fixtures against declared verified-IR,
ABI/provider and consumer interfaces. Actual composition consumes current
C11/C13 subjects and preserved/invalidated/regenerated facts, profile/backend
eligibility, checked proof assumptions and actual provider support. Opt remains
optional for valid unoptimized verified MIR. Domain IR verification is required
when a domain input is used, without clearing C12 or other execution holds.
Whole-component release or fresh migration admission is not a prerequisite for
ordinary source work. The staged Linux LLVM release path remains selected;
this generic contract does not adopt another target or prove wider support.

Retain exact request/source/compiler/pass/profile/target/ABI/runtime/provider/
proof/consumer identities, actual products and positive/refusal diagnostics.
Source reconciliation, implementation, independent acceptance, integration and
seedless release remain separate. No native/provider/domain/proof execution is
authorized or claimed by restoring these criteria.

## Macro dependency interface

Use the [macro producer and consumer order](../expand-workflow/plan.md#macro-producer-and-consumer-order) and existing selected-result table. Preserve actual generated checked-Q origins, safety and caller target facts into selected lowering; early fixed target rules are distinct from later derived Q layout and release comparison.

Task bodies/statuses and frozen review subjects stay unchanged. Declared interfaces permit local construction; actual composition, acceptance and release retain their separate required results. Current independent source/interface acceptance is pending and every actual restriction remains.

## Complete Core semantic interface

Use the [complete Core source criteria](../core-workflow/plan.md#complete-core-source-criteria). Preserve actual L2 Core value/control/dispatch/error semantics on the selected target or reject them; profile/ABI/runtime availability cannot legalize source or erase actual required safety evidence.

Task identities/statuses, prior source/review subjects and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held execution or task transfer is authorized.

## Core dependency interface

Use the [Core producer and consumer order](../core-workflow/plan.md#core-producer-and-consumer-order) and existing selected-result table. Admit real verified raw MIR or selected optimized/domain input, derive actual ABI/provider/proof/source maps and emit or refuse. Preserve conditional optimization and valid unoptimized paths; no target attribute or runtime availability supplies source legality.

Existing task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current selected-interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete Types source interface

Use the [complete Types source criteria](../types-workflow/plan.md#complete-types-source-criteria). Use actual checked type/dispatch/ABI/layout/schema facts and explicit target representation limits. Early target-query contracts precede Q inference; derived Q layouts follow it, with no host-default or future-layout circular premise.

Current task identities/statuses, historical review subjects and original C7 publication/other actual restrictions remain unchanged. This source proposal grants no implementation or current independent acceptance.

## Types dependency interface

Use the [Types producer and consumer order](../types-workflow/plan.md#types-producer-and-consumer-order) and existing selected-result table. Provide real early ABI/representation rules, derive selected layouts from real concrete type premises before final typed verification where needed, and consume actual admitted instantiation/dispatch/conversion/schema/layout facts for target work. No Q future layout or universal optimizer gate is introduced.

Existing task bodies/statuses, source criteria, frozen review scopes and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete pattern-matching source interface

Use the [complete pattern-matching source criteria](../core-workflow/plan.md#complete-pattern-matching-source-criteria). Consume actual checked decision products and layout/profile/service facts, preserving selected tests, guard calls, branches, moves/borrows and diagnostic/source maps or rejecting unsupported targets. No universal optimizer or future runtime-output prerequisite is introduced.

Task IDs/statuses, historical review scopes and actual restrictions remain unchanged. Current complete source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Pattern-matching dependency interface

Use the [pattern-matching producer and consumer order](../core-workflow/plan.md#pattern-matching-producer-and-consumer-order) and existing selected-result table. Use actual verified raw or selected transformed decisions and real layout/check/proof/source maps for selected target eligibility/emission or refusal. Early ABI rules precede source checking, later program layouts/output follow and no universal optimizer gate is added.

Task bodies/statuses, source criteria, frozen review scopes and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete protocol and dispatch source interface

Use the [complete protocol and dispatch source criteria](../types-workflow/plan.md#complete-protocol-and-dispatch-source-criteria). Produce actual stable selected method layouts and direct/vtable/dictionary/host/C-ABI/Wasm/HDL artifacts where governing profiles allow them. Source protocols remain semantic authority; early ABI interfaces precede final actual checked layouts/emission.

Task identities/statuses, historical review scopes and actual restrictions remain. Current full source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Protocol and dispatch dependency interface

Use the [protocol and dispatch producer and consumer order](../types-workflow/plan.md#protocol-and-dispatch-producer-and-consumer-order) and existing selected-result table. Early method ABI/layout/service support precedes source checking; later admitted verified input yields actual selected interface artifacts/bytes/readback or refusal, preserving protocol semantics and source diagnostics.

Task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Complete error-handling source interface

Use the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria). Use real verified admitted input and selected profile/runtime/error contracts to emit actual exception/Result/trap/panic/workflow/AI failure artifacts or refusal. Early ABI/service rules precede callers; actual target products preserve source errors and authority.

Task identities/statuses, historical review scopes and actual restrictions remain. Current full source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Historical task-authority-transfer plan

# Implementation plan: Lower task authority

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler/backend maintainer.
Input revision: `b394258a06950bcc69d57cae99f9ff6b582d0ad8`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts).
C14 and the reviewed additional interfaces below remain normative inputs;
workflow artifacts do not replace semantic authority.

Retain the existing canonical navigation:

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/lower.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Lower canonical remaining work](../../docs/component-roadmaps/lower.md#current-evidence-and-remaining-work) | lower/1 through lower/5; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/lower.md#acceptance-criteria-yesno) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-06-compiler-architecture/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: compiler/backend maintainer. Interface owners: [mir](../../docs/component-roadmaps/mir.md#release-outcome), [opt](../../docs/component-roadmaps/opt.md#release-outcome), [emit](../../docs/component-roadmaps/emit.md#release-outcome), [runtime](../../docs/component-roadmaps/runtime.md#release-outcome), [backends](../../docs/component-roadmaps/backends.md#release-outcome).
Preserve the selected staged Linux LLVM release candidate and distinguish hosted development. MIR acceptance is required; Opt is optional when optimization is not requested. Runtime/provider schemas are negotiated interfaces, not a circular prerequisite for completed releases.

## Approach and exact files

Move only the five real rows into the marked
[implementation region](tasks.md#implementation-tasks), preserving stable
semantic IDs, display labels, exact evidence/status and concrete exit criteria.
Keep the full original migration tasks file as a frozen suffix with unchanged
checkboxes. Migration delivery remains separate from implementation completion.

Own the Lower roadmap and four workflow artifacts.
Preserve the entire reviewed Additional contract interfaces appendix as an exact
suffix from plan blob `d5a086b43c7e26e2fcfd0aea6e987300ac6fadf6`. No mapping
artifact, other plan, shared helper, compiler implementation or compiler test
is edited. Preserve target selection, hosted boundary, accounting,
producer/admission rules, proof obligations, wider-backend scope and acceptance
while linking the roadmap directly to the task ledger.

## Validation and evidence

Run from repository root:

```bash
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Compare the preserved task criteria, historical suffix, prior navigation and
reviewed appendix. Check the Markdown files for ASCII and file/heading links,
then run whitespace and clean exact-base/head/tree integration preflight. These
checks prove structure, not compiler/runtime, producer or selected-target
acceptance.

## Delivery and remaining work

Independent review inspects all seven files, retained appendix and proposed PR
body. Publish a validated draft with exact identities and compact actual
final-tree receipts for maintainer current-head review. Integration follows that
disposition and actual repository gates. All five implementation outcomes remain
Open. No compiler semantic suite is required for this authority transfer.

## Names, callers and compatibility

runtime API, diagnostic, wire/schema identity, manifest hash, target selection,
admission policy or legacy source name changes. Planning IDs remain traceability.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [PERF4](../../docs/phase-04-performance-model/062-perf4-memory-layout-optimization-design.md#requirements): Realize target alignment, packing and boundary adapters from explicit layout manifests; reject unsupported or unsafe representations.
- [PERF8](../../docs/phase-04-performance-model/066-perf8-simd-vectorization-and-cache-optimization-strategy.md#requirements): Lower lane/tail/alignment plans only under target feature and numeric/certificate facts; retain checks or reject missing evidence.
