# Compiler API implementation planning entry

Select one of the five literal [existing outcomes](tasks.md#implementation-tasks),
preserving its full criteria, current status and historical/qualified receipts.
Preserve the transfer/composition plan below and the separate unintegrated
requirement-reconciliation lineage. Read [spec.md](spec.md), the complete
selected C1/C12/C16/C17/C18 and applicable provider/facet contracts.

Bind actual request/context/schema versions, source/compiler/pass/plugin/
policy/grant/profile/target inputs, semantic producer/verifier and real consumer.
Plan actual registered execution or artifact verification plus relevant invalid
ordering, missing/stale facts, scope/phase/effect, invalid registration/output,
cache or evidence-subject negatives. Keep actual callback/process execution
separate from record validation and honor all existing operation holds.

Use focused owner tests and affected actual stage/Build/CLI/Tools composition
through [development verification](../../docs/development-verification.md) and
the [stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).
Refine only the selected existing outcome. Retain exact passed/failed/unrun
artifacts and counterexamples without changing ownership, accepting pending
source amendments, refreshing migration evidence or claiming whole-pass/release
support from local checks.

## Complete domain IR source criteria

The existing `domain-ir-boundaries` and `pass-evidence` tasks already contain
the three complete existing/records/proof criterion blocks from the preserved
`domain-complete-current-facet-trace.edn`. Keep their text and statuses. The
retained [C12 review](review-inputs/domain-source-review.edn) contains all 115
source facets, fifty occurrences, 34 semantic conjunctions and six owner joins;
its scoped PASS remains frozen at `bec12ac1`. Reuse that archive, keeping
current plan review, actual execution and independent acceptance separate.

| Boundary and owning task | Required implementation and decisive evidence |
| --- | --- |
| Registry and schema; `domain-ir-boundaries` | Register every [required field](../../docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md#requirements): owning document/schema, semantic anchor, entry/exit passes, actual verifier, profiles, target lowerings, optimization rules and diagnostics. Retain proof/fallback policy and stable package-visible registration for plugins/libraries and self-hosting. Reject missing/foreign schema, opaque payload, invalid registration and plugin-policy bypass. An identifier or registry seed is not the owning payload schema or its verifier. |
| Source and fact continuity; `domain-ir-boundaries` consumes MIR/domain products | Preserve [artifact identity and owning payload](../../docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md#domain-artifact-shape), source syntax/span/origins and valid typed-core or MIR anchors, plus downstream type/effect/ownership/capability/profile/target/safety/provenance facts. Enter through expanded source, typed Core, MIR, provider/package/schema declarations or plugin output, recording consumed/preserved/invalidated/translated facts. Mutated, missing or stale anchors/facts must reject or regenerate before consumption. Domain IR refines Gravity semantics; it is not another untyped language. |
| Verifier and legal exits; `domain-ir-boundaries` with Lower | Invoke the actual [domain verifier](../../docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md#domain-verifier) for schema, anchors, provenance, compatible facts, domain invariants, safety/proofs, target preconditions, fallback and diagnostics. Failed verification blocks that artifact's emission. Each [exit family](../../docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md#exit-passes), including MIR, provider call, backend IR, runtime manifest, generated code, package or verification artifact, needs its registered path and actual equivalence/proof/certificate/accepted-lowering evidence. Lower and Backends own target derivation/emission; Runtime owns actual calls. |
| Optimization evidence; `pass-evidence` with Opt and domain/checker owners | Expose results only through actual [proof replay, certificate validation, translation validation or nonsemantic conformance acceptance](../../docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md#optimization-boundary), otherwise reject with diagnostics. Nonsemantic fixtures never authorize a semantic rewrite. Search, benchmark, provider assertions, EML tree identity and assigned accepted flags cannot establish correctness. Preserve C18's applicable checked matrix minimum and PERF10's exact check-elision evidence. |
| Unsupported target and diagnostics; `domain-ir-boundaries` | Produce eligibility plus semantically legal fallback, residual MIR or explicit rejection. Reject unregistered exits, unsupported emission and fallback violating profile/effect/safety rules. Preserve all nine [C12 diagnostic meanings](../../docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md#diagnostics) with domain/artifact/source span/anchor/owner/profile/target/verifier/missing fact/remediation. Capture authentic positive/refused products and actual unavailable results. |

Retain all nine [baseline families](../../docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md#standard-domain-ir-families):
EFIR (with adjacent EML proof/search), schema, workflow, AI agent/tool, HDL/state
machine, GPU, query, FFI boundary and package/artifact graph. Domain owners retain
their detailed schemas, semantics, entry/exit transformations and verifiers.
Math, Schema, Workflow, AI, GPU and Query use the existing Domain tasks; Platform's
`hardware` supplies HDL; Runtime's typed-FFI owner supplies foreign-boundary
semantics with the owning compiler/ABI contracts; Build supplies package/graph
semantics. The compiler registry/transition task composes these products without
duplicating them or turning every listed backend/runtime into universal support.

The required initial [conformance demonstration](../../docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md#conformance-criteria)
includes actual EFIR, schema, workflow and the already identified HDL registration
and verification, source-through-MIR anchors, accepted proof/certificate
optimization and missing-evidence refusal, eligibility/fallback, plugin-policy
and named diagnostic cases. This four-family demonstration does not remove the
other baseline families. The [existing dependency declarations](../../docs/acceptance-dependencies.md#future-task-construction-and-composition)
now bind `domain-ir-boundaries` to thirteen selected producer results, including
all nine family owners, MIR verification, independent Math checking, plugin
policy and registered Lower emission. These are result interfaces, not thirteen
new tasks or whole-component start prerequisites. The independent Math checker
is one concrete proof/certificate fixture route, not a universal checker for
every domain or every legal evidence method.

Start registry, schema, anchor and refusal fixtures against explicit versioned
producer/verifier interfaces. Composition requires actual selected family
products, callbacks, evidence and consumer results; C12-to-Lower staging keeps
verified domain products available before the downstream target emission used
by final conformance. The graph distinguishes declared interfaces from those
real results. Whole-component acceptance and fresh migration proof are not
ordinary editing prerequisites. Preserve the unresolved compiler ordering and
every C12/callback/provider/checker/target/publication hold; this plan runs none.
Keep hosted projection records, quoted Gravity models and narrow evidence/layout
adapters as partial code, with their existing nonclaims. Removed input/work
allowances are superseded; real type/index/ABI representability and provenance
conditions remain. No registry count, proof label or generated report creates
implementation, release, self-hosting or integration acceptance.

## Complete plugin and pass API source criteria

Keep the existing `plugins` and `pipeline` predicates unchanged. The three
existing/records/pipeline spans from the preserved
`plugin-complete-current-facet-trace.edn` are already literal current task text;
the retained [source review](review-inputs/plugin-source-review.edn) records
108 facets, 45 occurrences, thirty semantic conjunctions and six owner joins.
The trace at `726b04bbb4add5092074876522ced4792584ae30` has SHA-256
`084a0a1d7cccff88074193da58788a06063c5ab86838d04cd16c916dd1874e57`.
Its scoped PASS at `45fa1763` remains frozen to that source subject. Current
planning entailment and implementation assessment do not inherit acceptance.

| Boundary and owner | Implementation and acceptance route |
| --- | --- |
| Manifest before code; `plugins` | Parse the complete [C17 manifest](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md#plugin-manifest), including package/API/compiler compatibility, trust, meta profile, effects, grants, passes, emitted artifacts and conformance fixtures. Resolve authentic package/trust/signature/policy inputs before initialization. Demonstrate accepted manifests and malformed, incompatible, rejected-trust or excessive-authority refusals with no initialization effects. Package identity, signature or audit approval alone grants no compiler capability. |
| Context and sandbox; `plugins` | Enforce all sixteen [compiler scope families](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md#compiler-capabilities) and artifact-kind/namespace/phase/package restrictions. Context facts are read-only without explicit write authority; `read-mir` never permits rewriting. Preserve all five [trust levels](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md#sandbox-and-trust). Demonstrate actual sandboxed and trusted execution under granted authority, plus filesystem/network/process/environment/IR/model-tool/artifact-write denials and hermetic build-effect denial. Request fields or assigned sandbox results do not establish enforcement. |
| Registered pass execution; `pipeline` with `plugins` | Use the same actual input/output/requires/preserves/invalidates/regenerates/proof-obligations/emits [pass contract](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md#pass-api) for builtins and plugins. Pipeline owns stage ordering, facts and orchestration; plugins owns registration, versioned public access and grant enforcement. Consume actual stage artifacts and checkers; reject missing facts, illegal phases, hidden mutable state, undeclared effects and invalid output. Sample MIR/fusion/fact values are illustrative, not universal defaults. Unresolved pipeline ordering remains explicit. |
| Facets and domain IR; `plugins` consumes their owners | Route [extensions](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md#domain-and-facet-extensions) through L14 and C12 artifacts with schema, verifier, profiles, effects/grants, lowering, diagnostics and fixtures. Owning registries/checkers supply actual registration and callback results. Demonstrate acceptance, malformed registration, opaque/verifierless payload and failed-output refusal without accepting whole C12/L14 or bypassing their holds. |
| Trace and cache; `plugins` produces, Build and Bootstrap consume | Bind actual plugin/pass/input/output/grants/build effects/decisions/diagnostics/verifier results in the [execution trace](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md#execution-trace). Cache identity includes plugin package/version/manifest/grants/dependencies/replay. Demonstrate changed-identity invalidation and actual trace consumption by incremental cache and self-hosting comparison. Their owners retain reuse/comparison admission; emitting a trace alone proves neither. |
| Diagnostics and evidence; `plugins` with Diag and `pass-evidence` | Preserve all ten [C17 diagnostic meanings](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md#diagnostics) and plugin/package/version/pass/manifest-span-or-entry/requested-capability/trust/API/source-or-artifact/remediation context. Keep authentic fixtures, outputs, checker results and refusals for every [conformance criterion](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md#conformance-criteria). Pass-evidence consumes execution-bound results under C18; assigned compatible/verified/passed records and fixture names cannot substitute for actual behavior. |

Start local manifest/context/registration construction and negative fixtures
against explicit package, grant, stage, registry and verifier interfaces. The
stage0 adapter's C16-shaped input is not a universal prerequisite for ordinary
plugin development. Composition needs the actual selected producer products,
registered execution and output verifier results; cache and bootstrap acceptance
need their actual consumers. Full dependency acceptance and fresh migration
proof are not ordinary editing prerequisites. Existing plugin, callback,
sandbox, C12/provider/checker, target and publication holds remain effective;
this source plan authorizes no held execution.

Preserve current partial code and narrow receipts. The hosted compatibility
leaf constructs fixed manifests/grants/registration/output/trace records and
checks their fields/statuses; its policy excludes loading, execution, sandbox,
signature and authority ownership. The Gravity record model uses quoted
constructors. The separate W5 leaf validates supplied requests and recomputes
an identity-result record, with seed/JVM and non-authority boundaries. Its
supplied verified-signature, sandbox and verifier fields do not establish a
production loader, cryptographic verification, process isolation or arbitrary
plugin execution. Assess actual applicable behavior at the selected increment;
retain unsupported targets and missing execution evidence explicitly. No task
status, implementation, release, self-hosting or integration credit follows
from this review.

## Complete compiler-verification source criteria

The preserved `c18-complete-current-facet-trace.edn` binds three criterion
roles at historical source `984f1e74f98905f53b4b20c63b8f76d2a948dae3`:
the existing pass-evidence block, the detailed C18 records clause and the
existing backend-conformance block. The first and third are already literal
current text and stay unchanged. Restore only the missing records clause
under the same `pass-evidence` task; no new task or status is introduced.
Trace SHA-256:
`a5761a9b95de0d9f670edca8fd33e2c81b8323a54fe6b5caeb8f5656222e095d`.
The tested historical GitHub trace reference is unavailable. Use current
canonical sections and exact task witnesses without a dead link, aggregate
publication or stopped-handoff retry. The old source PASS retains its frozen
subject and qualified exclusions; independent current review is pending.

| Required boundary | Canonical conditions and actual completion evidence |
| --- | --- |
| Pass population, risk and evidence matrix | Enumerate the actual selected compiler closure with pass/version/risk/reasons/profile/target/evidence/release-gate identities. Preserve all four [risk meanings](../../docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md#pass-risk-classification) and all ten [matrix rows](../../docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md#verification-matrix), with checked minima distinct from optional stronger methods. Reject omitted passes, mismatched risk/profile and below-minimum evidence. Full mechanization of every pass is not an initial prerequisite. |
| Actual transformation validation | For changed MIR functions consume actual per-function validation, with pass/input/output/changed-function/result/effect/safety/method/proof/counterexample/verdict fields under [the validation contract](../../docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md#translation-validation). Reject failed transformations and retain the prior valid artifact. Stronger proof labels, structural verification or generic fixtures cannot waive the checked matrix minimum. Actual proof/certificate replay remains required for the applicable check-elision or domain case. |
| Trust and scoped release gates | Emit [trust reports](../../docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md#trust-report) binding compiler/pass/risk/evidence, profile requirements/blocked passes, target/artifact-kind dependencies, plugins and known experimental gaps. Enforce every [release-gate condition](../../docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md#release-gates), with explicit feature/profile gates and artifact markings for shipped experiments. Missing/stale/critical/high-risk evidence refuses affected release artifacts; pass-evidence does not own final Release admission. |
| Failures, provenance and diagnostics | Capture [available counterexamples](../../docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md#counterexamples) with source/input/output/property/diagnostic/minimized-reproducer/pass-version, turn reproducible failures into regressions and record actual unavailability without fabrication. Preserve original/generated origins and all [nine C18 diagnostic meanings and context fields](../../docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md#diagnostics), with actual accepted/refused cases. |
| Backend and downstream consumption | Consume the unchanged [backend-conformance task](../backends-workflow/tasks.md#implementation-tasks): actual output execution or semantic comparison, independent metadata/refusal checks, provenance/replay and explicit unavailable-target records. Compare source/Core/MIR intent with emitted behavior for the supported tuple. Actual stage owners, checkers, backend producers and release/bootstrap consumers supply their products; source clauses and unavailable execution cannot create success. |

The `pass-evidence` owner classifies the actual pass closure and orchestrates
subject-bound verification/trust gates. Each stage owns its actual product and
semantic verifier; Backends owns its conformance pack; Release independently
owns final artifact admission. Opt's current application consumes the checked
per-function matrix minimum. Stronger evidence is optional unless the applicable
claim policy requires it, and an unchecked stronger label never replaces the
minimum. A mechanized method can establish the required validation only with
actual checked affected-function coverage. Preserve the six original owner
candidates and the separate proposed backend source join without promoting
global owner resolution or whole-backend acceptance.

Start local record construction and refusal fixtures against explicit stage,
checker, evidence and backend interfaces. Composition needs actual current
products, checker invocations, verdicts and consumer refusals for the claim;
ordinary editing does not require whole-component release or fresh migration
admission. Release/bootstrap trust reports and compiler-release self-hosting
comparisons remain real later consumer obligations. The unresolved pipeline
ordering and every C12/provider/checker/target/publication hold remain effective.

Retain exact source/compiler/pass/version/profile/target/input/output/checker/
proof/diagnostic/counterexample/trust/gate identities, actual results and explicit
unavailable evidence. The canonical record examples are illustrative, not
default grants, universal risk assignments or accepted verifier results.
Source conservation, implementation, independent acceptance, integration and
seedless release remain distinct.

## Historical workflow-transfer and source-composition plan

# Implementation plan: Compiler API workflow migration

Spec: [spec.md](spec.md) | Migration owner: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the retained contracts linked in spec.md and [constitution](../../.specify/memory/constitution.md). The current requested migration extends the original pilot scope, without changing language semantics or execution restrictions. Use [development verification](../../docs/development-verification.md) when an actual implementation slice is selected; this documentation change requires structural/link checks, not another compiler proof run.

## Canonical task source

Edit implementation records only in [tasks.md](tasks.md#implementation-tasks). The existing outcome sections below retain their headings and acceptance boundaries and link directly to that ledger:

- [built in compiler pipeline outcome](../../docs/component-roadmaps/compiler-api.md#built-in-compiler-pipeline-outcome)
- [plugin host finite remaining tasks](../../docs/component-roadmaps/compiler-api.md#plugin-host-finite-remaining-tasks)
- [pass evidence finite remaining tasks](../../docs/component-roadmaps/compiler-api.md#pass-evidence-finite-remaining-tasks)

## Approach and exact files

1. `spec.md`: scope, retained contract navigation and accepted/rejected workflow behavior.
2. `plan.md`: canonical task anchors, interfaces and dependency boundaries.
3. `tasks.md`: three literal-key implementation records and the entire unchanged historical migration file, clearly separated.
4. `evidence.md`: observed checks and implementation nonclaims.

Central migration inventory/schema is owned separately. This package does not write shared indexes or transfer implementation ownership.

## Interfaces and dependencies

Pass input schemas and semantic verifiers are per-stage prerequisites. Plugin grant enforcement is separate from built-in execution. Build, CLI and Tools consume actual API products; Release consumes evidence later.

## Validation and evidence

Check all local Markdown targets and anchors, four-artifact presence, ASCII and semantic names. Negative controls must reject an absent file/anchor and duplicate implementation task truth. Run existing document and full-language roadmap validators against the candidate. Evidence is documentation-only; no compiler, target execution, external CLI or upstream workflow discovery is run for this slice.

## Delivery and remaining work

Publish the prepared slice as a draft. An independent reviewer checks mapping, anchors, scope and evidence; Deus integrates only the reviewed exact tree. Central inventory links and any remaining per-contract semantic-routing questions stay explicit, not silently marked accepted.

## Names, callers and compatibility

Package key `compiler-api-workflow` is semantic. No imports, load order, manifests, source hashes, schemas or public APIs are changed. Historical planning IDs in canonical links remain traceability only.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [TEST1](../../docs/phase-14-testing-verification-and-conformance/190-test1-language-conformance-test-plan.md#requirements): Compose portable language conformance from the owning front-end passes, with fixture/profile/target/type/effect identities and stable diagnostic spans; orchestration does not own each language rule.

## Finite source composition over current main

Use the existing task authority without copying the ALL-240
reconciliation database. Preserve the four prior main outcome predicates as
exact prefixes and all Open statuses. Extract the reviewed Plugin Host,
Pipeline, domain-IR and Pass Evidence criterion bodies. The fifth literal outcome is the existing canonical domain-IR
record, not a row-level task expansion. Keep historical workflow text intact.

Archive the exact C17/C12 receipts and their reviewed task-file bytes, retaining
immutable Git adoption references as history. Verify their current raw predicate
spans. Run source conservation/refusal controls, affected SH checks, boundary and
document validators. Append
the domain routing key after the prior four routing positions so integer
selectors retain their literal associations. Pin both source files and the
new composition artifact to commits that actually contain them. Generated
reports remain pending independent affected-composition review. Inspect the
exact clean main-based candidate; governed integration requires independent
review and coordinator action, separately from this source-only preparation.
