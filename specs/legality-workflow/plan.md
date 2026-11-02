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
