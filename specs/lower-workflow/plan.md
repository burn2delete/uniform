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
