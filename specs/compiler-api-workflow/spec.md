# Compiler API and pipeline implementation specification

Implementation scope added under the 2026-11-01 request as an isolated successor
to the preserved source-reconciliation candidate. Existing semantic owners,
literal task criteria and pending review scopes remain unchanged. The original
specification/composition text below is retained in full.

## Behavior and interfaces

Implement [the Compiler API outcomes](../../docs/component-roadmaps/compiler-api.md#release-outcome)
under D1/D3/D7/D9, C1/C12/C16/C17/C18, L14-L18 and the retained testing/source
contracts below. Explicit versioned source/stage requests, context/grants,
registered pass/provider/plugin contracts and actual artifacts become real
pipeline execution, verified stage products, cache/invalidation decisions,
extension/domain registration, subject-bound evidence/trust and diagnostics
consumed by Build, CLI, Tools and Release.

Passes consume explicit artifacts, not hidden mutable compiler state. Fused
execution retains the canonical observable inputs/outputs/facts/diagnostics/
verification boundaries. Resolve the documented D1 versus C1/C5/C6
representation-order qualification through its existing architecture decision;
no newly named intermediate stage or ordering label can resolve it here.

## Existing implementation routes and acceptance

The [sole marked ledger](tasks.md#implementation-tasks) already contains five
literal outcomes with detailed criteria and source amendments. Retain them as
the editable authority; their individual table rows are not new task IDs.

| Existing outcome | Behavior and required accepted/rejected evidence |
| --- | --- |
| `pipeline` | Real versioned request/context/result calls, built-in pass registration/scheduling, stage execution, verification and provenance, C16 stage-specific reuse/invalidation and actual API/CLI/Build composition. Reject missing versions/facts, invalid ordering, hidden context mutation, stale proofs/diagnostics and unchecked backend consumption. Diagnostic-only mode cannot emit runnable artifacts. |
| `plugins` | Actual C17 pre-load manifest/API/compiler/package/trust checks, scoped grants and sandbox/trusted execution, complete pass/facet/domain registration, output verification, trace and cache identities. Reject incompatible initialization before code runs, read-only/namespace/phase violations, hidden effects/state, invalid output and opaque registration. Package trust is distinct from compiler authority. |
| `pass-evidence` | Classify the real pass closure and invoke required C18 schema/semantic verifiers, changed-function translation validation and applicable checked proof/certificate/differential/domain evidence. Bind source/input/output/checker/profile/target identities, counterexamples and release/trust decisions. Reject omitted passes, supplied success flags, stale/wrong-subject evidence, lost origins and verification failures reaching affected release artifacts. |
| `domain-ir-boundaries` | Actually register, enter, verify and exit C12 domain artifacts through owning schemas/anchors and registered lowerings; preserve full semantic/safety/authority/provenance facts and valid fallback/residual/rejection paths. Retain the existing required EFIR/schema/workflow/identified HDL population. Reject missing family/schema/anchor/fact/proof, failed verification, illegal fallback and unregistered exits; search/benchmark/provider claims alone prove no correctness. |
| `development-boundaries` | Maintain actual unique semantic and fixture/shared-path ownership, delegated compatibility entrypoints, dedicated focused tests and strict truthful affected selection. Exercise representative leaf/shared boundaries and missing/duplicate-owner/unknown-selector refusals. Preserve historical SH-01 completion and unassessed current acceptance; do not introduce a second implementation or a routine admission framework. |

L14 facets remain explicit namespace-scoped, versioned and checked, including
ambiguity, generated-code and privacy/disclosure boundaries. Alternative macro,
type and memory providers replace machinery under L16-L18, preserve core/profile
safety and exported fact/equivalence contracts, and cannot redefine language
meaning. Component/domain/provider owners implement those semantics; the API
owns their declared registration, invocation and consumer boundaries.

## Starting work and completion

Versioned input/fact/verifier interfaces and fixtures allow local API or pass
work before complete producer components. Real composition uses actual selected
passes, verifiers, grants and consumers. Preserve C12 and other recorded
execution/publication holds; no callback or plugin run is authorized by this
source entry. Each outcome completes through its existing full criteria and
real positive/negative artifacts, with independent acceptance and coordinator
integration separate from author checks, migration and release.

## Historical workflow-transfer and source-composition specification

# Feature specification: Compiler API workflow migration

Status: prepared; independent review and integration pending.
Migration owner role: documentation migration maintainer.
Component owner role: compiler API/pipeline maintainer; person assignment remains unverified.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Public artifact/context APIs, built-in pass orchestration, plugin host registration and execution-bound pass evidence.

Adopt a four-artifact Spec Kit workflow around the [canonical component roadmap](../../docs/component-roadmaps/compiler-api.md). Language rules stay in their retained contracts; implementation tasks are edited only in [tasks.md](tasks.md#implementation-tasks), with direct roadmap links to the task ledger. This is workflow migration, not a rewrite of normative specifications or a new implementation result.

[Inventory](../../docs/document-inventory.json) entries routed for this component are listed below as reviewable navigation, not proof of semantic conformance or exclusive ownership. The routing proposal must be reconciled centrally; cross-component links are legitimate.

- [D0 - Gravity Vision & Design Thesis](../../docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md)
- [D1 - System Architecture Overview](../../docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
- [D2 - Implementation Roadmap & Milestones](../../docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md)
- [D3 - Terminology & Concept Model](../../docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md)
- [D7 - Extensibility Philosophy](../../docs/phase-00-foundation-and-thesis/008-d7-extensibility-philosophy.md)
- [D9 - Verifiability & Mathematical Correctness Charter](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
- [L14 - Language Facet System Specification](../../docs/phase-01-core-language/024-l14-language-facet-system-specification.md)
- [L16 - Alternative Macro System Contract](../../docs/phase-01-core-language/026-l16-alternative-macro-system-contract.md)
- [L17 - Alternative Type System Contract](../../docs/phase-01-core-language/027-l17-alternative-type-system-contract.md)
- [L18 - Alternative Memory Model Contract](../../docs/phase-01-core-language/028-l18-alternative-memory-model-contract.md)
- [SAFE15 - Safety Proof and Certificate Model](../../docs/phase-02-safety/044-safe15-safety-proof-and-certificate-model.md)
- [P13 - Profile Compatibility Matrix](../../docs/phase-03-profile-system/058-p13-profile-compatibility-matrix.md)
- [C1 - Compiler Architecture Overview](../../docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md)
- [C12 - Domain IR Architecture](../../docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md)
- [C16 - Incremental Compilation Design](../../docs/phase-06-compiler-architecture/095-c16-incremental-compilation-design.md)
- [C17 - Compiler Plugin and Pass API Specification](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md)
- [C18 - Compiler Verification and Pass-Correctness Strategy](../../docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md)
- [DOM17 - Compiler and Language Tooling Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/140-dom17-compiler-and-language-tooling-domain-specification.md)
- [TEST2 - Compiler Test Strategy](../../docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md)
- [TEST9 - Fuzzing and Property Testing Plan](../../docs/phase-14-testing-verification-and-conformance/198-test9-fuzzing-and-property-testing-plan.md)
- [TEST10 - Differential Testing Strategy](../../docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md)

## User scenarios and acceptance

### US1 - Find and execute the next canonical slice

A contributor starts here, follows plan.md to the existing task source, selects a finite owned slice and records actual validation in its implementation evidence. Acceptance: all four artifacts and retained-contract/task links resolve; there is no second editable implementation checklist. Rejection: a missing target, substituted normative authority, fabricated passing result or duplicate editable implementation ledger blocks workflow acceptance.

### US2 - Distinguish workflow adoption from product completion

A reader sees preparation, independent review and integration separately in tasks.md and evidence.md. Component implementation remains unassessed by this package. Acceptance requires this distinction to survive linking and integration; a directory, checkbox or merged documentation PR cannot close a component outcome.

## Scope and compatibility

Own `specs/compiler-api-workflow/{spec.md,plan.md,tasks.md,evidence.md}` and its direct roadmap navigation. No compiler/runtime source, existing contract, historical pilot, protocol ID or permission changes. No Specify CLI execution or automatic discovery is claimed.

## Evidence and outcome ownership

Documentation migration maintainer owns these artifacts and link checks. The compiler API/pipeline maintainer retains implementation semantics. Independent review is pending; Deus coordinates integration. [Evidence](evidence.md) records observed checks and nonclaims.

## Unresolved items

Plugin isolation and version-compatibility policy remain decisions of the compiler API and plugin maintainers; workflow adoption does not enable plugin execution. These implementation decisions do not prevent preparing and reviewing this workflow.

## Implementation naming review

The semantic key `compiler-api` names this package. Planning IDs appear only in contract references. No implementation namespaces are added or renamed; meaningful protocol and diagnostic IDs remain unchanged. Follow [semantic naming](../../.specify/semantic-names.md).

## Finite plugin and domain source composition

This later increment extracts the reviewed C17/C12 consumer and prerequisite
producer criteria, initially over main `70dcb2220701529dbb02bd2317a822eb3ac72fed`.
The current declared base is `787637b9ae780a7cfef7ca5763611882a8f2b629`.
The [composition record](domain-contract-source-composition.edn) binds both
archived exact source review receipts and six exact current criterion spans. Four
prior main outcomes and their Open statuses remain, and the existing canonical
domain-IR outcome becomes the fifth local record. No global task denominator
is adopted. The four source files are copied exactly from their declared
reviewed-source context; normative language documents and compiler/runtime code
remain unchanged.

The isolated main composition is a new candidate requiring independent affected
composition review. Earlier source acceptance is conditional evidence for these
exact predicates, not acceptance of this new main tree, whole dependencies,
all of C1/C18, or implementation support. The small SH update preserves prior
source-version history and numeric routing associations, adds the literal domain
key and current source pins, and keeps every whole-criterion assessment
unassessed. The retained Pass Evidence criterion names one source-context file;
its [scoped excerpt](../requirement-reconciliation/domain-gap-completion.edn)
contains only that criterion's original record and ten source-facet witnesses,
with captured before/after fields explicitly historical. The global database,
other 69 records and their acceptance are not imported.
