# Legality implementation planning entry

Select one unresolved record from [tasks.md](tasks.md#implementation-tasks),
preserving the historical transfer plan below. Read the complete applicable
profile, safety and C9/C10 rules in [contract alignment](#contract-alignment)
and [spec.md](spec.md); retain existing source-family refinements and ownership.
Locate current semantic source and temporary transport through
[development verification](../../docs/development-verification.md) and the
[stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

Name actual Core/control-flow inputs, profile/target/provider assumptions,
effects/grants, memory model, source origins and affected consumer. For each
selected rule, supply a legal program and a forbidden or missing-evidence case
whose difference exercises the rule. For memory/continuation work include the
relevant exit, suspension, cancellation and cleanup paths. For proof or runtime
checks bind the exact operation and demonstrate changed-input invalidation.

Use focused owner tests and affected Types/Effects/Auth/MIR or runtime/backend
composition, including an actual retained check where claimed. Do not replace
semantic analysis with supplied verdicts or turn real target constraints into
compiler accounting. Refine only the selected existing authoritative task.
Record exact source/fixture/consumer identities and passed, failed or unrun work;
preserve operation restrictions and separate author checks, independent
acceptance, integration and the complete profile/provider matrix.

## Root safety and unsafe-audit source criteria

Use the complete [SAFE1 root semantics](../../docs/phase-02-safety/030-safe1-safe-gravity-semantics.md)
and [SAFE6 unsafe/audit contract](../../docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md)
with the four existing profile, classification, handoff and conformance tasks.
The all-240 census supplies 71 SAFE1 and 62 SAFE6 facet identities, 123 source
occurrences and thirteen original owner allocations. Preserve those identities
and conjoin preambles, examples, lists and following conditions within their
source sections; source conservation is distinct from semantic entailment.

The preserved safety107 review contains six exact applicable allocations:
SAFE1 mode-policy and profile-legality; SAFE6 dependency-policy,
generated-unsafe, isolation and review-policy. Their literal criteria are
restored under the same existing tasks, preserving the frozen positive,
negative, output, dependency and nonclaim predicates. Review `6fb01a54` covers
those finite source/task/context/dependency/refusal allocations; applicability
`5cbcceae` explicitly excludes whole shared-facet/implementation acceptance and
later-added predicates. This current restoration and whole-source proposal
require their own review; no old receipt accepts the new source conjunctions.

| Existing task | Required source products and decisive cases |
| --- | --- |
| Profile restrictions | Restore the exact SAFE1 profile criterion and require the real available proof/check/provider/runtime and profile-safety report. Portable Core, declared Hosted/Native services and constrained/hardware/distributed/AI policies determine actual accept/reject/narrow results, before lowering. Unavailable machinery refuses. |
| Safety classification | Restore mode-policy plus four SAFE6 criteria. Enforce all [seven safety modes](../../docs/phase-02-safety/030-safe1-safe-gravity-semantics.md#safety-modes), four operation outcomes, weaker-dependency API eligibility, [all unsafe families](../../docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md#unsafe-operations), complete metadata, safe-wrapper precondition/failure/cleanup and raw-authority protection. Audits remain visible even when permitted. Generated code binds requesting source and generator; safe and internal-only boundaries stay distinct. |
| Safety classification: review and dependency decisions | Apply [forbidden/local/domain/certificate/audit-only policy](../../docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md#review-policy) with actual source/island/reviewer/date/evidence/expiry and affected-source invalidation. Audit-only cannot establish safe eligibility. Inspect dependency counts/families/reviews/wrappers/profiles/authority/certificates and reject hidden exports or invalid caller policy. Auth, Build and reviewers retain their actual roles. |
| MIR/runtime handoff | Preserve outcomes/checks/proofs/audits, mode/profile, dependency and generated-origin identities through real compiler, linked artifact, runtime, package, release and tooling consumers. Changed assumptions require valid preservation or invalidation/rechecking; no check erasure by assertion. Supply stable safety/audit/wrapper/review/dependency/certificate inputs and actual consumer/refusal evidence. |
| Profile conformance | Exercise [SAFE1](../../docs/phase-02-safety/030-safe1-safe-gravity-semantics.md#conformance-criteria) and [SAFE6](../../docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md#conformance-criteria) positives, failures and artifact inspections, all nineteen diagnostic meanings, generated-source context and mode/profile/dependency exceptions. Feed the existing SAFE15/SAFE16/Release consumers; a fixture verdict is distinct from an operation outcome and a source review is distinct from implementation acceptance. |

The thirteen preserved allocations keep their actual tasks: profile-legality
uses profile validation; preservation uses handoff; all other root outcome,
runtime-check, artifact, unsafe-boundary, audit, wrapper, isolation, generated,
review and dependency decisions use classification. Family conformance consumes
their results. Full specialized SAFE2-SAFE5/SAFE7-SAFE16 and profile/provider
semantics remain separate obligations, even when a root facet inherits their
owners. Canonical examples establish shape and relationships rather than
default permission, approved reviews, executable checks or real certificates.

Start focused fixtures against declared producer interfaces. Types/Effects,
Core/Expand, Auth trust/grants, Build dependency/certificate policy and selected
Runtime/Domain contracts retain the current C9/C10 producer roles. The additional
SAFE1 profile and SAFE6 review/package/tooling consumer routes remain explicit
dependency-reconciliation work; the current selected C10 rows do not establish
their whole-source order. Reviewers own actual unsafe approval, and package,
release, runtime and tooling owners implement their own consumers. Preserve all
actual holds and source-bound invalidation; no review, provider, native,
callback, probe, replay, migration, signing or publication action runs here.

## Complete safety-pipeline source criteria

Use the three existing safety-classification, MIR/runtime-handoff and
profile-conformance tasks for the complete
[C10 coordinator contract](../../docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md).
The preserved all-240 census contains 122 C10 facet identities, 47 source
occurrences and six owner allocations. Retain those identities and their
pending full-predicate dispositions. The four current criterion blocks refine
three existing Open tasks; inventory and decisions share the classification
owner rather than introducing another task ledger. No C10-specific independent
semantic review is supplied by this proposal.

| Existing task / criterion | Implementation boundary and decisive cases |
| --- | --- |
| Safety classification / inventory | Derive all [operation families](../../docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md#operation-inventory) from actual typed/effected Core, with profile, target, grants, ownership and origins. Run specialized analyses before the affected MIR construction. Reject missing inventory/facts, duplicate/conflicting/missing outcomes and unknown operations. Set equality alone cannot establish exactly one outcome. |
| Safety classification / decisions | Apply [SAFE1](../../docs/phase-02-safety/030-safe1-safe-gravity-semantics.md#safety-outcome-model) per operation. Bind every check to its real condition, guarding location, facts/effects, profile-legal failure and invalidation. Emit every applicable [proof obligation](../../docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md#proof-obligations), full unsafe audit, taint/capability report and generated-source diagnostics. Refuse missing or stale proof, illegal checks, forbidden unsafe and unsupported imports/foreign/numeric/tool claims. |
| MIR/runtime handoff / preservation | Preserve the exact analysis products through actual consumers. [SAFE15](../../docs/phase-02-safety/044-safe15-safety-proof-and-certificate-model.md#check-erasure) and [PERF10](../../docs/phase-04-performance-model/068-perf10-performance-safety-check-elision-rules.md#dominance-and-invalidation) govern proof validity, dominance, residual checks and restoration/rejection. Policy checks require equivalent policy evidence. Opt owns transformations; this task composes their surviving safety evidence with MIR, lowering, runtime and diagnostics. |
| Profile conformance / cases and artifacts | Exercise all [C10 conformance cases](../../docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md#conformance-criteria), ten diagnostic meanings and complete context. Inspect actual checks/proofs/audits/origins and optimization/backend preservation. Supply family fixtures/results to existing [SAFE16](../../docs/phase-02-safety/045-safe16-safety-conformance-test-plan.md#fixture-format)/Release consumers, keeping fixture verdicts distinct from per-operation safety outcomes. |

The six existing allocations remain: SAFE1 outcomes, runtime checks and safety
artifact plus C10 unsafe extraction and generated origins use classification;
C10 proof lifecycle uses handoff. Conformance consumes their products. Broad
inherited owner attachments are preserved as candidate joins, not accepted
full-owner predicates. Source list preambles, lists, examples and following
qualifiers are conjoined within their source sections; only headings and file
provenance receive context-only credit. Artifact samples define required shape
and relationships, not default operation IDs, support or execution evidence.

C10 coordinates SAFE2-SAFE14; it does not replace their specialized analyzers.
Types/Effects supply real facts, Auth supplies actual grants and scope, C9
supplies ownership/lifetime/region/resource results, and the profile task
validates supported mechanisms and failure behavior. Runtime supplies declared
provider/check/failure contracts and owns actual execution. Compiler API owns
subject-bound proof checking; Build/Package owns imported certificate and
dependency trust; source/generated producers retain origin/taint identities.
Domain/model/tool owners retain their policy implementations. Classification
may start on declared interfaces with focused fixtures; composed acceptance
requires the selected actual producer results. Whole components, final Release
or fresh authoritative migration are not ordinary construction prerequisites.

The [safety producer and consumer order](#safety-producer-and-consumer-order)
extends the three shared dependency rows with selected C10 products while
retaining every C9 producer. These are current dependency proposals, not full
C10, SAFE1-SAFE16, PERF10 or broader profile entailment. Preserve every
actual native/provider/callback/probe/replay/migration/private-store/signing/
publication hold. Ordinary source/plan review does not authorize those actions.

The hosted adapter emits fixed inventory/outcome/proof/audit examples and checks
set equality, field presence and assigned states. It is a partial compatibility
surface, not complete analysis. Later Gravity code contains normalized numeric
classification and scoped authenticated persistent-read and numeric adapters;
assess those exact kernels and their retained seed/digest/operation-identity
boundaries separately. Structural input quotas remain removed requirements;
actual integer width/representability, authentication, type and index legality
remain required. This planning slice executes none of those compiler paths.

## Safety producer and consumer order

The [existing selected-result table](../../docs/acceptance-dependencies.md#future-task-construction-and-composition)
extends three current consumers with twenty C10 producer-result joins. No
adopted result is needed to start their ordinary construction. Existing task
bodies, source criteria and statuses stay unchanged; the table adds selected
composition inputs rather than another implementation ledger.

1. Core and Expand supply actual operations, control flow, generated origins,
   taint and unsafe declarations; Types/Effects supply their real facts.
   Ownership/profile/Auth and selected Runtime contracts retain their C9 roles.
   Auth trust and Build/Package supply exact content/certificate/authority/
   build-effect validation. Math supplies applicable numeric/EFIR/mode semantics
   and independently checked selected claims. AI/Workflow supply admitted tool,
   schema, taint, review, idempotency and replay policy products. These are
   producer contract/analysis results before executing external calls. Local
   analysis fixtures may reject absent evidence or retain a legal check; they
   need neither final package publication nor all producer components complete.
2. The classification task derives the actual inventory and per-operation
   disposition using the selected specialized analyses, trust and policy.
   Construction against declared interfaces is distinct from a claim that
   actual upstream products were composed. A proof-dependent static decision
   consumes the actual checked subject; proof production or a method label
   cannot check itself. Safe wrappers, grants and certificate trust remain
   separate obligations. Unavailable machinery produces an explicit refusal.
3. Handoff composes these facts with actual MIR construction/attachment/
   verification, Opt check protection and regeneration, Lower proof/assumption
   preservation, Runtime failure/cleanup and distributed/AI policy execution,
   Auth enforcement/trust, Emit byte provenance and Diag reporting. Policy
   records precede execution; actual retained checks, failures, review/replay
   preservation and changed-function proof results are required where claimed.
   Valid unoptimized verified MIR remains usable; this integration task's final
   checkbox never becomes a per-program construction or lowering gate.
4. Conformance consumes the existing analysis/classification/handoff/profile
   products for complete selected C9/C10 family cases and artifact inspection.
   Its results feed existing Release execution and independent admission.
   Neither final Release nor a second harness is a prerequisite for fixtures.

The coarse producer view can expose mutual dependencies between math proof
checking, formal/safety preservation and complete family composition. Resolve
selected work with declared interfaces, concrete candidate proof production,
independent checking and downstream application in that order; broader family
integration consumes the resulting records later. Applicable proof methods and
policy products are selected by the actual operation, not an all-method or
whole-parent completion gate. Keep unresolved real interface conflicts visible;
an acyclic staged projection alone does not prove semantic executability.
All execution holds and the requirement for current independent review remain.

## Complete ownership-checker source criteria

Use the existing ownership, safety, handoff and conformance tasks for the whole
[C9 contract](../../docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md).
The preserved all-240 census already contains 130 C9 facet identities, fifty
source occurrences and seven owner allocations. Reuse those identities; its
candidate task links and context classifications do not establish full predicate
entailment. The current proposal binds each complete source occurrence to the
four existing criterion blocks below, preserving the original task text and
statuses. No standalone C9 independent review is present; current semantic
review and implementation acceptance remain pending/unassessed.

| Existing outcome | Actual implementation and decisive cases |
| --- | --- |
| Ownership/borrow/initialization/lifetime analysis | Derive [ownership and borrow graphs](../../docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md#borrow-graph), moves/consumption, all lifetime classes, regions/generations, escapes and linear flow from real typed/effected Core. Persistent immutable sharing differs from owned mutable movement; no implicit repair copies or GC fallback. Permit disjoint range splitting only with proof; otherwise assume potential aliasing. Reject active-borrow moves/consumes and conflicting aliases. Preserve interval start/end/owner/allowed-escape/invalidation and all source/generated identities. |
| Ownership analysis: region, resource and unsafe-reference products | Apply [L10](../../docs/phase-01-core-language/020-l10-memory-model-specification.md#ownership-and-borrowing), [SAFE3](../../docs/phase-02-safety/032-safe3-ownership-borrowing-and-lifetimes.md#escape-rules), [SAFE4](../../docs/phase-02-safety/033-safe4-region-and-arena-safety.md#escape-rules), [SAFE5](../../docs/phase-02-safety/034-safe5-linear-resource-safety.md#failure-and-cancellation) and [SAFE8](../../docs/phase-02-safety/037-safe8-concurrency-and-data-race-safety.md#ownership-transfer) at their actual selected interfaces. Cover every C9 escape destination, nested inner/outer lifetimes, arena reset invalidation, explicit destination ownership and all modeled terminal paths. The destination must own cleanup; audit references for manual lifetime/alias/resource handling are producer products, not safety approval. Reject leaks/double release/reuse/wrong-provider/invalid capture. |
| Safe-code classification | Consume actual analysis products and apply [C9's runtime-check conditions](../../docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md#runtime-checks) for borrow, region/arena generation, provider scope and terminal state. Each operation has an existing four-way safety outcome. Dynamic checks require actual value/provider state, profile/provider support, declared failure and explicit check records. Reject unavailable metadata/allocation/failure mechanisms. Runtime implements and executes the check; this task validates its required safety conditions. Preserve existing full safe-wrapper obligations. |
| MIR/runtime handoff | Consume analysis and compose explicit function/task/actor/workflow/FFI/callback/tool/model transfers with source consumption, destination lifetime/provider and cleanup. Attach relevant ownership/lifetime/region/linear facts to allocation/load/store/call/spawn/send/receive/release/cleanup MIR operations, preserving checks/audits/provenance through actual consumers. Refuse lost/stale/reordered facts or unsupported transfers/checks. This task owns boundary composition; ownership analysis remains the sole state-transition producer. |
| Profile-surface conformance | Exercise the complete [C9 conformance cases](../../docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md#conformance-criteria), all [twelve diagnostic meanings and context fields](../../docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md#diagnostics), generated-resource preservation and actual supported/unsupported check/failure cases. Conformance consumes the owning analysis/check/boundary products and supplies its fixtures/results to Release's existing harness; it does not create a second harness or infer whole-profile acceptance. |

The seven preserved allocations retain their existing tasks: ownership move,
borrow exclusivity, lifetime escape, unsafe lifetime and terminal flow remain
with ownership analysis; generic runtime checks remain with safe-code
classification; ownership-transfer composition remains with MIR/runtime handoff.
The conformance task consumes those products. Original source facets sometimes
attach broad inherited owner sets; the current per-occurrence conjunction joins
are proposals and do not accept every attached owner or its other documents.

The census's generic context fragments include semantic list preambles. Preserve
their original kind and identity, and conjoin every substantive preamble/list/
following condition within its complete source occurrence. Only headings and
file provenance receive context-only credit. The artifact example describes
required identities/facts and illustrates shape; its sample IDs, empty lists
and state labels are not execution evidence or default support. Source byte
conservation does not establish semantic entailment.

Start focused analysis/check/refusal fixtures against explicit typed-Core,
effects, provider/profile/grant and consumer interfaces. Types/Effects supply
real input facts; Auth supplies grants; profile validation owns support rules;
Runtime/Backends implement actual checks, cleanup and transfer behavior. Actual
composed acceptance needs those products and surviving failures, not complete
component releases or fresh migration evidence before ordinary editing. C9's
hosted adapter is partial; later Gravity state/identity/runtime-borrow kernels
must be assessed at their exact scope rather than discarded or promoted into
whole ownership support. Keep physical/provider/alignment/type/index validity
distinct from removed project accounting. Preserve all C7/C12/provider/native/
callback/probe/migration/publication holds and do not run them for this plan.

## Ownership producer and consumer order

The [existing selected-result dependency table](../../docs/acceptance-dependencies.md#future-task-construction-and-composition)
now declares the four current C9 consumer routes under their existing task IDs.
Each starts against declared interfaces with no adopted-result prerequisite;
its composition consumes only the selected actual outputs named in the table.
These rows do not claim complete P1-P13/C10/other-source dependencies for the
broader Legality tasks, independent acceptance or implementation.

1. Types supplies actual typed-Core ownership/resource/call annotations; Effects
   supplies inferred facts before C9, not an already accepted final ownership
   result. Profile, Auth and Runtime owners supply selected support/grant/
   provider contracts. Ownership analysis derives real state/borrow/lifetime/
   region/linear/escape products and refusal evidence.
2. Safety classification consumes those products with actual checked proof/
   audit evidence and profile/provider metadata/failure availability. It emits
   the operation's legal disposition and explicit dynamic-check obligations.
   Capability authority, memory validity and proof remain separate checks.
3. Ownership facts and dispositions are available before MIR construction.
   MIR owns actual construction, fact attachment, verification and downstream
   admission. Opt owns any selected mutation/regeneration; Lower/Emit own
   actual target products/conformance; Runtime owns real transfer/check/cleanup/
   failure execution; Diag owns consumer reporting. The handoff task consumes
   these results for its final C9 boundary evidence rather than duplicating
   their implementations or waiting for its own final checkbox before MIR can
   be constructed. Valid unoptimized verified MIR remains usable by Lower.
4. Family conformance composes analysis, safety, handoff and profile results,
   preserving actual supported/unsupported tuples, source/check/provider/
   diagnostic identities and unrun evidence. Its fixtures/results feed Release's
   existing harness and later independent admission. It does not require final
   Release first or create another harness.

The selected producer counts describe source declarations, not work allowances
or acceptance. Conditional checks/transfers/target paths must keep their actual
support and evidence scope; a runtime-contract record cannot substitute for
runtime execution, and a hosted check cannot prove native release. All existing
execution/publication restrictions remain. Local work may proceed on versioned
interfaces while actual composition and required review stay unassessed.

## Foundation source criteria

Three preserved clauses from the
[foundation context](https://github.com/burn2delete/uniform/blob/df11890b21f71d3f246cc47e9f63d866239a0d68/specs/requirement-reconciliation/foundation-residual-interfaces.edn)
now refine the existing profile and safe-code tasks. All remain Open and their
source/facet conjunctions and implementation acceptance remain unassessed.

| Existing requirement | Canonical conditions and existing outcome |
| --- | --- |
| `gravity/profile-contract-declaration` | Profile checking publishes permitted/rejected/checked/delegated behavior and exact runtime/provider/backend dependencies, then enforces expanded caller code under [D0 profiles](../../docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md#profile-thesis) and [P1 validation order](../../docs/phase-03-profile-system/046-p1-profile-system-specification.md#validation-order). Keep effects and capabilities distinct and apply the narrowest source/profile/package/provider/deployment policy; backend/runtime availability cannot legalize rejected source. |
| `gravity/safety-family-conformance` | Safe-code classification exercises each claimed family with proven-safe and runtime-checked positives, actual failure and rejected cases, and audited unsafe cases only where mode/profile/policy permits them under [D8](../../docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md#conformance-criteria) and [SAFE1](../../docs/phase-02-safety/030-safe1-safe-gravity-semantics.md#safety-outcome-model). Exactly one outcome applies per dangerous operation; a fixture's overall verdict cannot substitute for its operation records. |
| `gravity/unsafe-island-safe-api-eligibility` | The same safe-code task checks each safe wrapper's full preconditions, invariant, failure and cleanup obligations with actual tests/proofs and source-bound audits under [D8 islands](../../docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md#unsafe-islands) and [SAFE6 wrappers](../../docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md#safe-wrappers). Reject leaked invalid states/raw authority and stale/missing evidence; explicit internal-only unsafe code remains distinct from a safe public API. |

Actual Types/Effects/source origins, Auth grants, Runtime service/check execution
and Compiler API pass-evidence products supply the listed dependencies. Local
fixture interfaces do not require entire producer completion. Composition must
retain actual operation/profile/provider/check/proof/audit identities and
positive/negative results through the relevant consumers. Safe modes continue
to reject forbidden islands; an unsafe-island fixture is not permission to
accept one in every mode. Preserve conditional review/certificate policy,
generated origins and invalidation. Physical memory/timing/format requirements
remain distinct from removed project work accounting and execution holds.

## Historical task-authority-transfer plan

# Implementation plan: Legality task authority

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner role: profile, ownership and safety maintainer.
Input revision: `a57aae5f5c018434dfeca5c8d5106e67ba6f043c`.

## Contract alignment

The [canonical inventory](../../docs/document-inventory.json) and owning
contracts remain normative. The [constitution](../../.specify/memory/constitution.md)
and [development workflow](../../docs/development-verification.md) retain their
semantic, provenance and execution boundaries. Only task editing authority changes; historical acceptance is not reopened.

The following retained references are routed here for navigation. Cross-cutting
contracts may also be used by other component packages; inclusion is not
exclusive ownership or semantic conformance acceptance.

- [D8: Safety Philosophy & Charter](../../docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md)
- [L10: Memory Model Specification](../../docs/phase-01-core-language/020-l10-memory-model-specification.md)
- [L18: Alternative Memory Model Contract](../../docs/phase-01-core-language/028-l18-alternative-memory-model-contract.md)
- [SAFE1: Safe Gravity Semantics](../../docs/phase-02-safety/030-safe1-safe-gravity-semantics.md)
- [SAFE2: Memory Safety Model](../../docs/phase-02-safety/031-safe2-memory-safety-model.md)
- [SAFE3: Ownership, Borrowing & Lifetimes](../../docs/phase-02-safety/032-safe3-ownership-borrowing-and-lifetimes.md)
- [SAFE4: Region and Arena Safety](../../docs/phase-02-safety/033-safe4-region-and-arena-safety.md)
- [SAFE5: Linear Resource Safety](../../docs/phase-02-safety/034-safe5-linear-resource-safety.md)
- [SAFE6: Unsafe Code and Audit Model](../../docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md)
- [SAFE7: FFI Safety](../../docs/phase-02-safety/036-safe7-ffi-safety.md)
- [SAFE8: Concurrency and Data-Race Safety](../../docs/phase-02-safety/037-safe8-concurrency-and-data-race-safety.md)
- [SAFE9: Numeric Safety](../../docs/phase-02-safety/038-safe9-numeric-safety.md)
- [SAFE10: Capability Security Model](../../docs/phase-02-safety/039-safe10-capability-security-model.md)
- [SAFE11: Taint Tracking and Input Safety](../../docs/phase-02-safety/040-safe11-taint-tracking-and-input-safety.md)
- [SAFE12: Macro Safety](../../docs/phase-02-safety/041-safe12-macro-safety.md)
- [SAFE13: AI Tool Safety](../../docs/phase-02-safety/042-safe13-ai-tool-safety.md)
- [SAFE15: Safety Proof and Certificate Model](../../docs/phase-02-safety/044-safe15-safety-proof-and-certificate-model.md)
- [SAFE16: Safety Conformance Test Plan](../../docs/phase-02-safety/045-safe16-safety-conformance-test-plan.md)
- [P1: Profile System Specification](../../docs/phase-03-profile-system/046-p1-profile-system-specification.md)
- [P2: :core Profile Specification](../../docs/phase-03-profile-system/047-p2-core-profile-specification.md)
- [P3: :meta Profile Specification](../../docs/phase-03-profile-system/048-p3-meta-profile-specification.md)
- [P4: :hosted Profile Specification](../../docs/phase-03-profile-system/049-p4-hosted-profile-specification.md)
- [P5: :native Profile Specification](../../docs/phase-03-profile-system/050-p5-native-profile-specification.md)
- [P6: :firmware Profile Specification](../../docs/phase-03-profile-system/051-p6-firmware-profile-specification.md)
- [P7: :kernel Profile Specification](../../docs/phase-03-profile-system/052-p7-kernel-profile-specification.md)
- [P8: :hardware Profile Specification](../../docs/phase-03-profile-system/053-p8-hardware-profile-specification.md)
- [P9: :distributed Profile Specification](../../docs/phase-03-profile-system/054-p9-distributed-profile-specification.md)
- [P10: :ai Profile Specification](../../docs/phase-03-profile-system/055-p10-ai-profile-specification.md)
- [P11: :gpu / Accelerator Profile Specification](../../docs/phase-03-profile-system/056-p11-gpu-accelerator-profile-specification.md)
- [P12: :formal Verification Profile Specification](../../docs/phase-03-profile-system/057-p12-formal-verification-profile-specification.md)
- [P13: Profile Compatibility Matrix](../../docs/phase-03-profile-system/058-p13-profile-compatibility-matrix.md)
- [PERF10: Performance/Safety Check Elision Rules](../../docs/phase-04-performance-model/068-perf10-performance-safety-check-elision-rules.md)
- [C9: Ownership, Lifetime and Region Checker Design](../../docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md)
- [C10: Safety Analysis Pipeline Design](../../docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md)
- [A8: AI Policy and Safety Model](../../docs/phase-11-ai-and-agentic-programming/161-a8-ai-policy-and-safety-model.md)
- [A11: Prompt Injection and Tool Misuse Defense Specification](../../docs/phase-11-ai-and-agentic-programming/164-a11-prompt-injection-and-tool-misuse-defense-specification.md)
- [TEST4: Profile Compliance Test Plan](../../docs/phase-14-testing-verification-and-conformance/193-test4-profile-compliance-test-plan.md)
- [TEST5: Safety Conformance Test Plan](../../docs/phase-14-testing-verification-and-conformance/194-test5-safety-conformance-test-plan.md)
- [TEST11: Formal Semantics and Verification Plan](../../docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md)
- [GOV4: Security Review Process](../../docs/phase-17-governance-and-evolution/234-gov4-security-review-process.md)
- [GOV9: Unsafe Code Governance Policy](../../docs/phase-17-governance-and-evolution/239-gov9-unsafe-code-governance-policy.md)

## Approach and exact files

The marked [implementation region](tasks.md#implementation-tasks) is the sole
editable task authority. Preserve all five component/heading/title-slug IDs,
titles, Open statuses, evidence baselines, closing criteria and historical
migration records. The [roadmap](../../docs/component-roadmaps/legality.md#remaining-tasks-and-evidence)
links directly to the sole task ledger; its [acceptance](../../docs/component-roadmaps/legality.md#acceptance-and-integration)
and [release outcome](../../docs/component-roadmaps/legality.md#release-outcome)
remain unchanged.

The ledger retains each task's evidence baseline, closing criteria, status and
historical migration boundary. No duplicate link catalog or generated table is
maintained. No checkbox alone grants acceptance; historical checkboxes remain
outside the implementation records.

Types and Effects feed profile legality; Auth validates grants from intermediate
facts; ownership and safety consume them before MIR. This transfer changes none
of those dependencies and introduces no whole-component completion cycle.

## Validation and evidence

Run from repository root:

```bash
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Check the five workflow documents for ASCII and local links. Run the language
boundary and document validators; these checks do not establish implementation
acceptance. Preserve the entire historical tasks suffix and record actual
evidence separately from review and integration.

## Delivery and remaining work

Independent review inspects all public files and the proposed PR body. Publish
a draft for maintainer current-head review; merge only after integration
disposition and repository gates. No implementation outcome closes here.

## Names, callers and compatibility

The new semantic tool names follow existing component entrypoints. No compiler
API, runtime schema, diagnostic, manifest hash or wire identity changes.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [L7](../../docs/phase-01-core-language/017-l7-pattern-matching-specification.md#requirements): Validate owned/linear pattern moves and evidence for trusted closed shapes; structural matching alone does not validate external data.
- [L11](../../docs/phase-01-core-language/021-l11-concurrency-model-specification.md#requirements): Reject races and invalid shared-mutation/ownership transfer before execution; effect labels alone do not establish concurrency safety.
