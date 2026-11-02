# Canonical contract to Spec Kit mapping

Source baseline: `063989e153b88c00f3ac3f69c2f7a04b5981ab2d` (2026-10-26).
Mapping state: proposed for independent review; no replacement accepted.

Current successor, 2026-11-01: the existing
[Governance and Evolution implementation package](../specs/evolution-workflow/spec.md)
has been resumed from reconciliation commit
`55fed8d91eb6c9b3fb5c50c8ca5dbdf7d67545f6`, retaining its ten Open task records
byte-for-byte. Its [plan](../specs/evolution-workflow/plan.md) and
[evidence](../specs/evolution-workflow/evidence.md) distinguish governance
producers from Build/Release consumers and preserve qualified source-review
limits. The four evolution-package gaps in the historical matrix below now
have that explicit proposed implementation route; accountable individual
ownership, exact current composition and independent acceptance remain pending.
The historical population, dispositions and validation command below retain
their original bounded candidate scope. They are not current coverage counts
or a validator for the expanded project-wide change; use the
[readiness report](implementation-readiness.md) for current observations.

### Package evolution-workflow

[spec](../specs/evolution-workflow/spec.md) | [plan](../specs/evolution-workflow/plan.md) | [tasks](../specs/evolution-workflow/tasks.md) | [evidence](../specs/evolution-workflow/evidence.md)

Accountable component role: language-evolution/governance maintainer, individual
unassigned. Acceptance boundary: actual subject-bound change/compatibility,
RFC, experiment, library, namespace, yank, security response/review,
stabilization and target-tier producer decisions with scoped review and
consumer evidence. Build/Release remain consumers. This resumed proposed
authority supplies no governance implementation, policy adoption or independent
current-composition acceptance.

## Historical mapping population and dispositions

The [240-document inventory](../docs/document-inventory.json) remains semantic
and implementation-contract authority. This artifact gives every inventory ID
a deliberate disposition and names the accountable component interfaces and
acceptance boundaries. It is navigation and a bounded gap proposal, not a new
implementation ledger, language specification or conformance report.

## Coverage and meaning

| Measure | Baseline | This candidate |
| --- | --- | --- |
| Canonical contracts | 240 | 240 |
| Existing specs-only four-artifact packages | 29 | 29 |
| Existing specs-only package artifacts | 116 | 116 |
| Historical .specify four-artifact packages | 2 | 2 |
| Historical .specify package artifacts | 8 | 8 |
| Total retained packages / artifacts | 31 / 124 | 31 / 124 |
| Contracts directly linked from package artifacts | 218 | 236 |
| Contracts with no direct package reference | 22 unlinked | 4 explicit evolution-workflow proposals |
| Contracts with an explicit disposition here | No consolidated matrix | 240 |
| Accepted contract replacements or retirements | 0 | 0 |

The 218 baseline links are retained as scoped interfaces, including historical
finite increments. Eighteen previously unlinked contracts gain package-plan
references; L9, L11, L12, L16, L18, SAFE12, SAFE14 and PERF10 gain additional
interfaces after source review. GOV1, GOV6, GOV7 and GOV8 remain without an
owning package; their proposed workflow below is not counted as mapped. Do not count a matrix row,
constitution mention, phase index or proposed package name as a direct package
reference. D6's earlier constitution/adoption mention was therefore not counted.

No independent acceptance is inferred for existing routing proposals or this
candidate. Review may accept navigation independently of implementation;
semantic completion still needs actual source-pinned evidence. Existing task
IDs, statuses, historical rows, source provenance, evidence receipts and
acceptance wording are preserved. Phase 18 remains a release extension outside
the 240-contract denominator. Project cost/fuel/attempt quotas remain removed
under AGENTS.md; residual runtime work, target representability and external
billing are separate semantics, not reinstated project accounting.

## Package accountability and acceptance boundaries

Roles below identify accountable components, not newly assigned people. Each
package's plan retains the current task/acceptance locations; follow that plan
rather than treating this mapping as a second task ledger. For every row,
implementation acceptance requires both the source criteria and the applicable
component criteria. A source requirement may span several components; all
listed links are nonexclusive. Historical finite packages are supporting
evidence only. No package is a replacement for a canonical contract.

### Package 001-paired-source-core

[spec](../specs/001-paired-source-core/spec.md) | [plan](../specs/001-paired-source-core/plan.md) | [tasks](../specs/001-paired-source-core/tasks.md) | [evidence](../specs/001-paired-source-core/evidence.md)

Accountability: the finite increment owner named in its spec/plan. Acceptance boundary: only that pinned paired-source, resolution-verification or incremental-naming increment. It supports the component workflow; it does not own the full linked contract or receive fresh acceptance here.

### Package auth-workflow

[spec](../specs/auth-workflow/spec.md) | [plan](../specs/auth-workflow/plan.md) | [tasks](../specs/auth-workflow/tasks.md) | [evidence](../specs/auth-workflow/evidence.md)

Accountable component role: Capability/artifact-trust maintainer. Acceptance boundary: Declared providers/grants and artifact trust; effect inference and signing custody stay separate.

### Package backends-workflow

[spec](../specs/backends-workflow/spec.md) | [plan](../specs/backends-workflow/plan.md) | [tasks](../specs/backends-workflow/tasks.md) | [evidence](../specs/backends-workflow/evidence.md)

Accountable component role: Backend interface and family maintainers. Acceptance boundary: Each declared family/matrix needs actual target conformance; the selected LLVM release does not close the portfolio.

### Package build-incremental-helper-names

[spec](../specs/build-incremental-helper-names/spec.md) | [plan](../specs/build-incremental-helper-names/plan.md) | [tasks](../specs/build-incremental-helper-names/tasks.md) | [evidence](../specs/build-incremental-helper-names/evidence.md)

Accountability: the finite increment owner named in its spec/plan. Acceptance boundary: only that pinned paired-source, resolution-verification or incremental-naming increment. It supports the component workflow; it does not own the full linked contract or receive fresh acceptance here.

### Package build-incremental-operation-names

[spec](../specs/build-incremental-operation-names/spec.md) | [plan](../specs/build-incremental-operation-names/plan.md) | [tasks](../specs/build-incremental-operation-names/tasks.md) | [evidence](../specs/build-incremental-operation-names/evidence.md)

Accountability: the finite increment owner named in its spec/plan. Acceptance boundary: only that pinned paired-source, resolution-verification or incremental-naming increment. It supports the component workflow; it does not own the full linked contract or receive fresh acceptance here.

### Package build-incremental-projection-names

[spec](../specs/build-incremental-projection-names/spec.md) | [plan](../specs/build-incremental-projection-names/plan.md) | [tasks](../specs/build-incremental-projection-names/tasks.md) | [evidence](../specs/build-incremental-projection-names/evidence.md)

Accountability: the finite increment owner named in its spec/plan. Acceptance boundary: only that pinned paired-source, resolution-verification or incremental-naming increment. It supports the component workflow; it does not own the full linked contract or receive fresh acceptance here.

### Package build-workflow

[spec](../specs/build-workflow/spec.md) | [plan](../specs/build-workflow/plan.md) | [tasks](../specs/build-workflow/tasks.md) | [evidence](../specs/build-workflow/evidence.md)

Accountable component role: Build/package maintainer. Acceptance boundary: Real locked resolution/build/cache/package verification; no authority grants or registry-policy adjudication.

### Package cli-workflow

[spec](../specs/cli-workflow/spec.md) | [plan](../specs/cli-workflow/plan.md) | [tasks](../specs/cli-workflow/tasks.md) | [evidence](../specs/cli-workflow/evidence.md)

Accountable component role: CLI maintainer. Acceptance boundary: Real command delegation to services; parsing or metadata alone is not supported execution.

### Package compiler-api-workflow

[spec](../specs/compiler-api-workflow/spec.md) | [plan](../specs/compiler-api-workflow/plan.md) | [tasks](../specs/compiler-api-workflow/tasks.md) | [evidence](../specs/compiler-api-workflow/evidence.md)

Accountable component role: Compiler API/pipeline maintainer and evidence/plugin specialists. Acceptance boundary: Executed pass graph, registrations, verifiers and trust reports; individual passes retain semantic ownership.

### Package core-workflow

[spec](../specs/core-workflow/spec.md) | [plan](../specs/core-workflow/plan.md) | [tasks](../specs/core-workflow/tasks.md) | [evidence](../specs/core-workflow/evidence.md)

Accountable component role: Core maintainer. Acceptance boundary: Core forms/desugaring/evaluation order; Types, Effects and Legality own their analyses.

### Package diag-workflow

[spec](../specs/diag-workflow/spec.md) | [plan](../specs/diag-workflow/plan.md) | [tasks](../specs/diag-workflow/tasks.md) | [evidence](../specs/diag-workflow/evidence.md)

Accountable component role: Diagnostic infrastructure maintainer. Acceptance boundary: Canonical structured diagnostic identity, deterministic ordering, original/generated origins, redaction and equivalent text/JSON/public rendering across compiler, runtime, CLI and editor consumers. Stage owners retain rule semantics; diagnostic infrastructure acceptance does not establish source-rule completion.

### Package domains-workflow

[spec](../specs/domains-workflow/spec.md) | [plan](../specs/domains-workflow/plan.md) | [tasks](../specs/domains-workflow/tasks.md) | [evidence](../specs/domains-workflow/evidence.md)

Accountable component role: Domain architecture maintainer and domain specialists. Acceptance boundary: Separate Math/Schema/Query/Workflow/AI/GPU/UI outcomes and sector fixtures; one domain cannot close another.

### Package effects-workflow

[spec](../specs/effects-workflow/spec.md) | [plan](../specs/effects-workflow/plan.md) | [tasks](../specs/effects-workflow/tasks.md) | [evidence](../specs/effects-workflow/evidence.md)

Accountable component role: Effect-system maintainer. Acceptance boundary: Direct/latent/transitive effects and ordering; authority grants remain with Auth.

### Package emit-workflow

[spec](../specs/emit-workflow/spec.md) | [plan](../specs/emit-workflow/plan.md) | [tasks](../specs/emit-workflow/tasks.md) | [evidence](../specs/emit-workflow/evidence.md)

Accountable component role: Artifact-emission maintainer. Acceptance boundary: Real target artifacts, manifests, maps and provenance; emission is not execution or release.

### Package expand-workflow

[spec](../specs/expand-workflow/spec.md) | [plan](../specs/expand-workflow/plan.md) | [tasks](../specs/expand-workflow/tasks.md) | [evidence](../specs/expand-workflow/evidence.md)

Accountable component role: Macro-expansion maintainer. Acceptance boundary: Deterministic syntax expansion and declared build effects; generated forms still pass semantic checks.

### Package legality-workflow

[spec](../specs/legality-workflow/spec.md) | [plan](../specs/legality-workflow/plan.md) | [tasks](../specs/legality-workflow/tasks.md) | [evidence](../specs/legality-workflow/evidence.md)

Accountable component role: Legality maintainer. Acceptance boundary: Compile-time profile, ownership and safety outcomes; runtime checks cannot legalize rejected source.

### Package lower-workflow

[spec](../specs/lower-workflow/spec.md) | [plan](../specs/lower-workflow/plan.md) | [tasks](../specs/lower-workflow/tasks.md) | [evidence](../specs/lower-workflow/evidence.md)

Accountable component role: Target-lowering maintainer. Acceptance boundary: Eligibility, ABI/layout/service realization and rejection; no target invents source legality.

### Package mir-workflow

[spec](../specs/mir-workflow/spec.md) | [plan](../specs/mir-workflow/plan.md) | [tasks](../specs/mir-workflow/tasks.md) | [evidence](../specs/mir-workflow/evidence.md)

Accountable component role: MIR maintainer. Acceptance boundary: Actual verified MIR and preserved fact identities; a passed metadata field is not a verifier run.

### Package opt-workflow

[spec](../specs/opt-workflow/spec.md) | [plan](../specs/opt-workflow/plan.md) | [tasks](../specs/opt-workflow/tasks.md) | [evidence](../specs/opt-workflow/evidence.md)

Accountable component role: Optimization maintainer. Acceptance boundary: Verified transformations, invalidation and surviving proofs; no-change is legal and speed is not correctness.

### Package platform-workflow

[spec](../specs/platform-workflow/spec.md) | [plan](../specs/platform-workflow/plan.md) | [tasks](../specs/platform-workflow/tasks.md) | [evidence](../specs/platform-workflow/evidence.md)

Accountable component role: Platform architecture and target maintainers. Acceptance boundary: Adopted Hardware/Firmware/Kernel/Devices targets and separate product decisions; no hardware selection is made here.

### Package reader-workflow

[spec](../specs/reader-workflow/spec.md) | [plan](../specs/reader-workflow/plan.md) | [tasks](../specs/reader-workflow/tasks.md) | [evidence](../specs/reader-workflow/evidence.md)

Accountable component role: Reader maintainer. Acceptance boundary: Syntax seeds, strict decoding and spans; source decoding cannot claim typed semantics.

### Package release-workflow

[spec](../specs/release-workflow/spec.md) | [plan](../specs/release-workflow/plan.md) | [tasks](../specs/release-workflow/tasks.md) | [evidence](../specs/release-workflow/evidence.md)

Accountable component role: Bootstrap/release maintainer. Acceptance boundary: Exact claimed compiler closure, stage equivalence, reproducibility and absent seed fallback; no whole-platform credit.

### Package resolve-owner-source-core-verification

[spec](../specs/resolve-owner-source-core-verification/spec.md) | [plan](../specs/resolve-owner-source-core-verification/plan.md) | [tasks](../specs/resolve-owner-source-core-verification/tasks.md) | [evidence](../specs/resolve-owner-source-core-verification/evidence.md)

Accountability: the finite increment owner named in its spec/plan. Acceptance boundary: only that pinned paired-source, resolution-verification or incremental-naming increment. It supports the component workflow; it does not own the full linked contract or receive fresh acceptance here.

### Package resolve-workflow

[spec](../specs/resolve-workflow/spec.md) | [plan](../specs/resolve-workflow/plan.md) | [tasks](../specs/resolve-workflow/tasks.md) | [evidence](../specs/resolve-workflow/evidence.md)

Accountable component role: Name-resolution maintainer. Acceptance boundary: Real lexical/module identities; binding tables are not type solutions.

### Package runtime-workflow

[spec](../specs/runtime-workflow/spec.md) | [plan](../specs/runtime-workflow/plan.md) | [tasks](../specs/runtime-workflow/tasks.md) | [evidence](../specs/runtime-workflow/evidence.md)

Accountable component role: Runtime-family maintainers. Acceptance boundary: Executed declared services, cleanup, denial and replay; compiler-service acceptance is narrower than all families.

### Package stdlib-workflow

[spec](../specs/stdlib-workflow/spec.md) | [plan](../specs/stdlib-workflow/plan.md) | [tasks](../specs/stdlib-workflow/tasks.md) | [evidence](../specs/stdlib-workflow/evidence.md)

Accountable component role: Library maintainer and namespace specialists. Acceptance boundary: Per-export semantics/profile/effect/stability conformance; compiler-support subset does not close general libraries.

### Package syntax-workflow

[spec](../specs/syntax-workflow/spec.md) | [plan](../specs/syntax-workflow/plan.md) | [tasks](../specs/syntax-workflow/tasks.md) | [evidence](../specs/syntax-workflow/evidence.md)

Accountable component role: Syntax maintainer. Acceptance boundary: Syntax identity, hygiene and origins; construction is not expansion or typing.

### Package tools-workflow

[spec](../specs/tools-workflow/spec.md) | [plan](../specs/tools-workflow/plan.md) | [tasks](../specs/tools-workflow/tasks.md) | [evidence](../specs/tools-workflow/evidence.md)

Accountable component role: Developer-tooling maintainer and service specialists. Acceptance boundary: Actual service sessions and artifact inspection; service record assembly is not a working tool.

### Package types-workflow

[spec](../specs/types-workflow/spec.md) | [plan](../specs/types-workflow/plan.md) | [tasks](../specs/types-workflow/tasks.md) | [evidence](../specs/types-workflow/evidence.md)

Accountable component role: Type-system maintainer. Acceptance boundary: Solved typed-core constraints and checked conversions; no invented effect/ownership facts.

### Package reader-core-invocation

[spec](reader-core-invocation/spec.md) | [plan](reader-core-invocation/plan.md) | [tasks](reader-core-invocation/tasks.md) | [evidence](reader-core-invocation/evidence.md)

Historical .specify package: the finite Reader-to-Core invocation owner retains the pinned paired-source/current-source invocation and rejection evidence. Its recorded PR388 integration is attributed history, not fresh Reader, Resolve or Core component acceptance. Its C2/C5/C6 links are supporting interfaces already covered by the component workflows.

### Package naming-pilot

[spec](naming-pilot/spec.md) | [plan](naming-pilot/plan.md) | [tasks](naming-pilot/tasks.md) | [evidence](naming-pilot/evidence.md)

Historical .specify package: the naming-adoption owner retains its bounded guidance, generated-command and sampled legacy-audit scope. It contains no direct canonical-document links and receives no implementation-rename or whole-component acceptance here. Both historical packages and all eight original artifacts remain unchanged and included in the 31-package/124-artifact total; their presence adds no unique contract to the 236 linked count.

## Contract dispositions

Each source link names its Requirements section; `criteria` names its
Conformance Criteria. The short basis is navigation into those obligations,
not substitute acceptance text. Package links resolve to the accountability
registry above and its four actual artifacts. `Retained` preserves baseline
package navigation, including proposals already labeled as such in their
evidence. `Added` is a justified mapping proposal in this candidate. `Extended` adds scoped interfaces to an already-linked contract. None
means independent semantic acceptance. `Gap` has no fitting existing package;
its explicit proposed disposition is in [the evolution proposal](#evolution-workflow-proposal).

| Contract and acceptance source | Package interfaces | Requirement-to-component basis | Disposition |
| --- | --- | --- | --- |
| [D0](../docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md#requirements) / [criteria](../docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow) | Semantic/profile obligations on every pipeline stage | Retained |
| [D1](../docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md#requirements) / [criteria](../docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow), [legality-workflow](#package-legality-workflow) | Canonical stage order and preserved artifact boundaries | Retained |
| [D2](../docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md#requirements) / [criteria](../docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow) | Evidence-bearing executable milestone ordering | Retained |
| [D3](../docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md#requirements) / [criteria](../docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow) | Shared meanings of profile, target, runtime and authority | Retained |
| [D4](../docs/phase-00-foundation-and-thesis/005-d4-universal-computing-coverage-charter.md#requirements) / [criteria](../docs/phase-00-foundation-and-thesis/005-d4-universal-computing-coverage-charter.md#conformance-criteria) | [domains-workflow](#package-domains-workflow), [platform-workflow](#package-platform-workflow) | Domain slices need executable coverage and artifacts | Retained |
| [D5](../docs/phase-00-foundation-and-thesis/006-d5-language-replacement-strategy.md#requirements) / [criteria](../docs/phase-00-foundation-and-thesis/006-d5-language-replacement-strategy.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Incumbent replacement needs named parity slices | Added |
| [D6](../docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md#requirements) / [criteria](../docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md#conformance-criteria) | [opt-workflow](#package-opt-workflow), [tools-workflow](#package-tools-workflow) | Safe optimization and reproducible performance claims | Added |
| [D7](../docs/phase-00-foundation-and-thesis/008-d7-extensibility-philosophy.md#requirements) / [criteria](../docs/phase-00-foundation-and-thesis/008-d7-extensibility-philosophy.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow), [domains-workflow](#package-domains-workflow) | Registered extensions must expose profiles and evidence | Retained |
| [D8](../docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md#requirements) / [criteria](../docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [legality-workflow](#package-legality-workflow) | Compile-time safety classification and authority boundaries | Retained |
| [D9](../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md#requirements) / [criteria](../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow) | Checkers, proofs and evidence invalidate conservatively | Retained |
| [L1](../docs/phase-01-core-language/011-l1-surface-syntax-specification.md#requirements) / [criteria](../docs/phase-01-core-language/011-l1-surface-syntax-specification.md#conformance-criteria) | [reader-workflow](#package-reader-workflow), [syntax-workflow](#package-syntax-workflow) | Reader syntax objects retain origin and metadata | Retained |
| [L2](../docs/phase-01-core-language/012-l2-core-language-semantics.md#requirements) / [criteria](../docs/phase-01-core-language/012-l2-core-language-semantics.md#conformance-criteria) | [001-paired-source-core](#package-001-paired-source-core), [core-workflow](#package-core-workflow) | Core forms preserve evaluation order and desugaring | Retained |
| [L3](../docs/phase-01-core-language/013-l3-namespace-and-module-system-specification.md#requirements) / [criteria](../docs/phase-01-core-language/013-l3-namespace-and-module-system-specification.md#conformance-criteria) | [resolve-workflow](#package-resolve-workflow) | Lexical and namespace binding identities | Retained |
| [L4](../docs/phase-01-core-language/014-l4-macro-system-specification.md#requirements) / [criteria](../docs/phase-01-core-language/014-l4-macro-system-specification.md#conformance-criteria) | [expand-workflow](#package-expand-workflow) | Syntax-to-syntax expansion preserves hygiene and origins | Retained |
| [L5](../docs/phase-01-core-language/015-l5-type-system-specification.md#requirements) / [criteria](../docs/phase-01-core-language/015-l5-type-system-specification.md#conformance-criteria) | [types-workflow](#package-types-workflow) | Constraints produce typed core and latent effects | Retained |
| [L6](../docs/phase-01-core-language/016-l6-effect-system-specification.md#requirements) / [criteria](../docs/phase-01-core-language/016-l6-effect-system-specification.md#conformance-criteria) | [effects-workflow](#package-effects-workflow) | Direct, latent and transitive effects remain visible | Retained |
| [L7](../docs/phase-01-core-language/017-l7-pattern-matching-specification.md#requirements) / [criteria](../docs/phase-01-core-language/017-l7-pattern-matching-specification.md#conformance-criteria) | [core-workflow](#package-core-workflow), [effects-workflow](#package-effects-workflow), [legality-workflow](#package-legality-workflow), [types-workflow](#package-types-workflow) | Match decisions, narrowed bindings, guard effects and moves | Added |
| [L8](../docs/phase-01-core-language/018-l8-protocols-interfaces-and-dispatch-specification.md#requirements) / [criteria](../docs/phase-01-core-language/018-l8-protocols-interfaces-and-dispatch-specification.md#conformance-criteria) | [types-workflow](#package-types-workflow) | Protocol contracts and dispatch constraints | Retained |
| [L9](../docs/phase-01-core-language/019-l9-error-handling-specification.md#requirements) / [criteria](../docs/phase-01-core-language/019-l9-error-handling-specification.md#conformance-criteria) | [core-workflow](#package-core-workflow), [diag-workflow](#package-diag-workflow), [runtime-workflow](#package-runtime-workflow) | Structured errors and their diagnostic surface | Extended |
| [L10](../docs/phase-01-core-language/020-l10-memory-model-specification.md#requirements) / [criteria](../docs/phase-01-core-language/020-l10-memory-model-specification.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Memory regimes, ownership and raw-memory legality | Retained |
| [L11](../docs/phase-01-core-language/021-l11-concurrency-model-specification.md#requirements) / [criteria](../docs/phase-01-core-language/021-l11-concurrency-model-specification.md#conformance-criteria) | [effects-workflow](#package-effects-workflow), [legality-workflow](#package-legality-workflow), [runtime-workflow](#package-runtime-workflow) | Concurrency effects and safe sharing constraints | Extended |
| [L12](../docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md#requirements) / [criteria](../docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [build-workflow](#package-build-workflow), [opt-workflow](#package-opt-workflow) | Hermetic compile-time evaluation requires declared effects | Extended |
| [L13](../docs/phase-01-core-language/023-l13-standard-library-design-principles.md#requirements) / [criteria](../docs/phase-01-core-language/023-l13-standard-library-design-principles.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Public library APIs expose profiles and authority | Retained |
| [L14](../docs/phase-01-core-language/024-l14-language-facet-system-specification.md#requirements) / [criteria](../docs/phase-01-core-language/024-l14-language-facet-system-specification.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow), [domains-workflow](#package-domains-workflow) | Facet registration and domain semantic anchors | Retained |
| [L15](../docs/phase-01-core-language/025-l15-capability-provider-specification.md#requirements) / [criteria](../docs/phase-01-core-language/025-l15-capability-provider-specification.md#conformance-criteria) | [auth-workflow](#package-auth-workflow) | Explicit providers and capability grants | Retained |
| [L16](../docs/phase-01-core-language/026-l16-alternative-macro-system-contract.md#requirements) / [criteria](../docs/phase-01-core-language/026-l16-alternative-macro-system-contract.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow), [expand-workflow](#package-expand-workflow) | Alternative macro engines preserve reference semantics | Extended |
| [L17](../docs/phase-01-core-language/027-l17-alternative-type-system-contract.md#requirements) / [criteria](../docs/phase-01-core-language/027-l17-alternative-type-system-contract.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow), [types-workflow](#package-types-workflow) | Alternative typing produces accepted typed core | Retained |
| [L18](../docs/phase-01-core-language/028-l18-alternative-memory-model-contract.md#requirements) / [criteria](../docs/phase-01-core-language/028-l18-alternative-memory-model-contract.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow), [legality-workflow](#package-legality-workflow), [runtime-workflow](#package-runtime-workflow) | Alternative memory providers preserve safety evidence | Extended |
| [L19](../docs/phase-01-core-language/029-l19-language-interoperability-and-migration-specification.md#requirements) / [criteria](../docs/phase-01-core-language/029-l19-language-interoperability-and-migration-specification.md#conformance-criteria) | [backends-workflow](#package-backends-workflow), [domains-workflow](#package-domains-workflow), [runtime-workflow](#package-runtime-workflow) | Foreign ABI/host/schema boundaries and migration parity | Added |
| [SAFE1](../docs/phase-02-safety/030-safe1-safe-gravity-semantics.md#requirements) / [criteria](../docs/phase-02-safety/030-safe1-safe-gravity-semantics.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Exactly one safety outcome per dangerous operation | Retained |
| [SAFE2](../docs/phase-02-safety/031-safe2-memory-safety-model.md#requirements) / [criteria](../docs/phase-02-safety/031-safe2-memory-safety-model.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Initialization, bounds and lifetime-safe memory access | Retained |
| [SAFE3](../docs/phase-02-safety/032-safe3-ownership-borrowing-and-lifetimes.md#requirements) / [criteria](../docs/phase-02-safety/032-safe3-ownership-borrowing-and-lifetimes.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Exclusive mutation, borrow and move validity | Retained |
| [SAFE4](../docs/phase-02-safety/033-safe4-region-and-arena-safety.md#requirements) / [criteria](../docs/phase-02-safety/033-safe4-region-and-arena-safety.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Region/arena lifetime and escape validity | Retained |
| [SAFE5](../docs/phase-02-safety/034-safe5-linear-resource-safety.md#requirements) / [criteria](../docs/phase-02-safety/034-safe5-linear-resource-safety.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Exactly-once resource terminal states | Retained |
| [SAFE6](../docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md#requirements) / [criteria](../docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Explicit reviewed unsafe islands and safe wrappers | Retained |
| [SAFE7](../docs/phase-02-safety/036-safe7-ffi-safety.md#requirements) / [criteria](../docs/phase-02-safety/036-safe7-ffi-safety.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Typed foreign boundaries and ABI/ownership audits | Retained |
| [SAFE8](../docs/phase-02-safety/037-safe8-concurrency-and-data-race-safety.md#requirements) / [criteria](../docs/phase-02-safety/037-safe8-concurrency-and-data-race-safety.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Race, synchronization and concurrency safety | Retained |
| [SAFE9](../docs/phase-02-safety/038-safe9-numeric-safety.md#requirements) / [criteria](../docs/phase-02-safety/038-safe9-numeric-safety.md#conformance-criteria) | [domains-workflow](#package-domains-workflow), [legality-workflow](#package-legality-workflow), [types-workflow](#package-types-workflow) | Numeric modes and checked mathematical assumptions | Retained |
| [SAFE10](../docs/phase-02-safety/039-safe10-capability-security-model.md#requirements) / [criteria](../docs/phase-02-safety/039-safe10-capability-security-model.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [legality-workflow](#package-legality-workflow) | Explicit authority and deny-by-default capability policy | Retained |
| [SAFE11](../docs/phase-02-safety/040-safe11-taint-tracking-and-input-safety.md#requirements) / [criteria](../docs/phase-02-safety/040-safe11-taint-tracking-and-input-safety.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [legality-workflow](#package-legality-workflow) | Taint, secret propagation and redaction | Retained |
| [SAFE12](../docs/phase-02-safety/041-safe12-macro-safety.md#requirements) / [criteria](../docs/phase-02-safety/041-safe12-macro-safety.md#conformance-criteria) | [legality-workflow](#package-legality-workflow), [expand-workflow](#package-expand-workflow) | Macro hygiene, origins, safety declarations and build effects | Extended |
| [SAFE13](../docs/phase-02-safety/042-safe13-ai-tool-safety.md#requirements) / [criteria](../docs/phase-02-safety/042-safe13-ai-tool-safety.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [domains-workflow](#package-domains-workflow), [legality-workflow](#package-legality-workflow) | AI/tool authority and validated outputs | Retained |
| [SAFE14](../docs/phase-02-safety/043-safe14-supply-chain-safety.md#requirements) / [criteria](../docs/phase-02-safety/043-safe14-supply-chain-safety.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [build-workflow](#package-build-workflow) | Supply-chain integrity and artifact trust | Extended |
| [SAFE15](../docs/phase-02-safety/044-safe15-safety-proof-and-certificate-model.md#requirements) / [criteria](../docs/phase-02-safety/044-safe15-safety-proof-and-certificate-model.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [compiler-api-workflow](#package-compiler-api-workflow), [legality-workflow](#package-legality-workflow) | Proof/certificate validity for check elision | Retained |
| [SAFE16](../docs/phase-02-safety/045-safe16-safety-conformance-test-plan.md#requirements) / [criteria](../docs/phase-02-safety/045-safe16-safety-conformance-test-plan.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Accepted, rejected and unsafe safety conformance | Retained |
| [P1](../docs/phase-03-profile-system/046-p1-profile-system-specification.md#requirements) / [criteria](../docs/phase-03-profile-system/046-p1-profile-system-specification.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Compile-time profile legality and manifest validation | Retained |
| [P2](../docs/phase-03-profile-system/047-p2-core-profile-specification.md#requirements) / [criteria](../docs/phase-03-profile-system/047-p2-core-profile-specification.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Portable pure core legality | Retained |
| [P3](../docs/phase-03-profile-system/048-p3-meta-profile-specification.md#requirements) / [criteria](../docs/phase-03-profile-system/048-p3-meta-profile-specification.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Compiler/metaprogramming legality and build effects | Retained |
| [P4](../docs/phase-03-profile-system/049-p4-hosted-profile-specification.md#requirements) / [criteria](../docs/phase-03-profile-system/049-p4-hosted-profile-specification.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Declared hosted services and dynamic boundaries | Retained |
| [P5](../docs/phase-03-profile-system/050-p5-native-profile-specification.md#requirements) / [criteria](../docs/phase-03-profile-system/050-p5-native-profile-specification.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Native legality and explicit runtime assumptions | Retained |
| [P6](../docs/phase-03-profile-system/051-p6-firmware-profile-specification.md#requirements) / [criteria](../docs/phase-03-profile-system/051-p6-firmware-profile-specification.md#conformance-criteria) | [legality-workflow](#package-legality-workflow), [platform-workflow](#package-platform-workflow) | Firmware memory, interrupt and service constraints | Retained |
| [P7](../docs/phase-03-profile-system/052-p7-kernel-profile-specification.md#requirements) / [criteria](../docs/phase-03-profile-system/052-p7-kernel-profile-specification.md#conformance-criteria) | [legality-workflow](#package-legality-workflow), [platform-workflow](#package-platform-workflow) | Kernel privilege, allocation and service constraints | Retained |
| [P8](../docs/phase-03-profile-system/053-p8-hardware-profile-specification.md#requirements) / [criteria](../docs/phase-03-profile-system/053-p8-hardware-profile-specification.md#conformance-criteria) | [legality-workflow](#package-legality-workflow), [platform-workflow](#package-platform-workflow) | Hardware bounded structures and clock/reset facts | Retained |
| [P9](../docs/phase-03-profile-system/054-p9-distributed-profile-specification.md#requirements) / [criteria](../docs/phase-03-profile-system/054-p9-distributed-profile-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow), [legality-workflow](#package-legality-workflow) | Distributed effects and replay legality | Retained |
| [P10](../docs/phase-03-profile-system/055-p10-ai-profile-specification.md#requirements) / [criteria](../docs/phase-03-profile-system/055-p10-ai-profile-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow), [legality-workflow](#package-legality-workflow) | AI provider/tool/model legality | Retained |
| [P11](../docs/phase-03-profile-system/056-p11-gpu-accelerator-profile-specification.md#requirements) / [criteria](../docs/phase-03-profile-system/056-p11-gpu-accelerator-profile-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow), [legality-workflow](#package-legality-workflow) | GPU address spaces and host/device restrictions | Retained |
| [P12](../docs/phase-03-profile-system/057-p12-formal-verification-profile-specification.md#requirements) / [criteria](../docs/phase-03-profile-system/057-p12-formal-verification-profile-specification.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Formal totality, proof and trusted basis | Retained |
| [P13](../docs/phase-03-profile-system/058-p13-profile-compatibility-matrix.md#requirements) / [criteria](../docs/phase-03-profile-system/058-p13-profile-compatibility-matrix.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow), [legality-workflow](#package-legality-workflow) | Cross-profile compatibility and explicit adapters | Retained |
| [PERF1](../docs/phase-04-performance-model/059-perf1-performance-model-specification.md#requirements) / [criteria](../docs/phase-04-performance-model/059-perf1-performance-model-specification.md#conformance-criteria) | [opt-workflow](#package-opt-workflow) | Claim identity, preserved facts and guarded variants | Added |
| [PERF2](../docs/phase-04-performance-model/060-perf2-zero-cost-abstractions-specification.md#requirements) / [criteria](../docs/phase-04-performance-model/060-perf2-zero-cost-abstractions-specification.md#conformance-criteria) | [opt-workflow](#package-opt-workflow) | Before/after IR and truthful residual abstraction work | Added |
| [PERF3](../docs/phase-04-performance-model/061-perf3-specialization-and-partial-evaluation-design.md#requirements) / [criteria](../docs/phase-04-performance-model/061-perf3-specialization-and-partial-evaluation-design.md#conformance-criteria) | [build-workflow](#package-build-workflow), [opt-workflow](#package-opt-workflow) | Complete specialization keys, guards and invalidation | Added |
| [PERF4](../docs/phase-04-performance-model/062-perf4-memory-layout-optimization-design.md#requirements) / [criteria](../docs/phase-04-performance-model/062-perf4-memory-layout-optimization-design.md#conformance-criteria) | [lower-workflow](#package-lower-workflow), [opt-workflow](#package-opt-workflow) | Layout transformations preserve ABI and address identity | Added |
| [PERF5](../docs/phase-04-performance-model/063-perf5-benchmark-suite-and-performance-governance.md#requirements) / [criteria](../docs/phase-04-performance-model/063-perf5-benchmark-suite-and-performance-governance.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | Target-fingerprinted benchmark evidence and regression policy | Retained |
| [PERF6](../docs/phase-04-performance-model/064-perf6-profile-guided-optimization-design.md#requirements) / [criteria](../docs/phase-04-performance-model/064-perf6-profile-guided-optimization-design.md#conformance-criteria) | [build-workflow](#package-build-workflow), [opt-workflow](#package-opt-workflow), [tools-workflow](#package-tools-workflow) | PGO identity, staleness, privacy and decision replay | Added |
| [PERF7](../docs/phase-04-performance-model/065-perf7-autotuning-and-multiversioning-design.md#requirements) / [criteria](../docs/phase-04-performance-model/065-perf7-autotuning-and-multiversioning-design.md#conformance-criteria) | [backends-workflow](#package-backends-workflow), [opt-workflow](#package-opt-workflow) | Valid candidate selection, guarded variants and fallback | Added |
| [PERF8](../docs/phase-04-performance-model/066-perf8-simd-vectorization-and-cache-optimization-strategy.md#requirements) / [criteria](../docs/phase-04-performance-model/066-perf8-simd-vectorization-and-cache-optimization-strategy.md#conformance-criteria) | [backends-workflow](#package-backends-workflow), [lower-workflow](#package-lower-workflow), [opt-workflow](#package-opt-workflow) | Lane/alias/bounds proofs and guarded target intrinsics | Added |
| [PERF9](../docs/phase-04-performance-model/067-perf9-realtime-and-deterministic-latency-performance-model.md#requirements) / [criteria](../docs/phase-04-performance-model/067-perf9-realtime-and-deterministic-latency-performance-model.md#conformance-criteria) | [platform-workflow](#package-platform-workflow) | Worst-case latency and deterministic runtime assumptions | Retained |
| [PERF10](../docs/phase-04-performance-model/068-perf10-performance-safety-check-elision-rules.md#requirements) / [criteria](../docs/phase-04-performance-model/068-perf10-performance-safety-check-elision-rules.md#conformance-criteria) | [legality-workflow](#package-legality-workflow), [opt-workflow](#package-opt-workflow) | Surviving proof or retained runtime safety check | Extended |
| [MATH1](../docs/phase-05-mathematical-and-elementary-function-system/069-math1-numeric-tower-specification.md#requirements) / [criteria](../docs/phase-05-mathematical-and-elementary-function-system/069-math1-numeric-tower-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Numeric types and explicit conversion/overflow semantics | Retained |
| [MATH2](../docs/phase-05-mathematical-and-elementary-function-system/070-math2-elementary-function-system-specification.md#requirements) / [criteria](../docs/phase-05-mathematical-and-elementary-function-system/070-math2-elementary-function-system-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Elementary functions expose domain and error contracts | Retained |
| [MATH3](../docs/phase-05-mathematical-and-elementary-function-system/071-math3-elementary-function-ir-efir-specification.md#requirements) / [criteria](../docs/phase-05-mathematical-and-elementary-function-system/071-math3-elementary-function-ir-efir-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | EFIR preserves elementary semantic anchors | Retained |
| [MATH4](../docs/phase-05-mathematical-and-elementary-function-system/072-math4-eml-normalization-and-search-design.md#requirements) / [criteria](../docs/phase-05-mathematical-and-elementary-function-system/072-math4-eml-normalization-and-search-design.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | EML proof/search traces justify EFIR transformations | Retained |
| [MATH5](../docs/phase-05-mathematical-and-elementary-function-system/073-math5-certified-approximation-specification.md#requirements) / [criteria](../docs/phase-05-mathematical-and-elementary-function-system/073-math5-certified-approximation-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Approximation certificates bind domain and error | Retained |
| [MATH6](../docs/phase-05-mathematical-and-elementary-function-system/074-math6-interval-arithmetic-and-real-proof-engine.md#requirements) / [criteria](../docs/phase-05-mathematical-and-elementary-function-system/074-math6-interval-arithmetic-and-real-proof-engine.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Checked real intervals and inclusion-preserving proofs | Retained |
| [MATH7](../docs/phase-05-mathematical-and-elementary-function-system/075-math7-numeric-modes-and-precision-contracts.md#requirements) / [criteria](../docs/phase-05-mathematical-and-elementary-function-system/075-math7-numeric-modes-and-precision-contracts.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Explicit numeric mode and precision contracts | Retained |
| [MATH8](../docs/phase-05-mathematical-and-elementary-function-system/076-math8-floating-point-semantics-specification.md#requirements) / [criteria](../docs/phase-05-mathematical-and-elementary-function-system/076-math8-floating-point-semantics-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Floating formats, rounding and exceptional values | Retained |
| [MATH9](../docs/phase-05-mathematical-and-elementary-function-system/077-math9-symbolic-math-and-rewrite-system-specification.md#requirements) / [criteria](../docs/phase-05-mathematical-and-elementary-function-system/077-math9-symbolic-math-and-rewrite-system-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Domain-qualified rewrite proofs and source origins | Retained |
| [MATH10](../docs/phase-05-mathematical-and-elementary-function-system/078-math10-elementary-function-optimization-strategy.md#requirements) / [criteria](../docs/phase-05-mathematical-and-elementary-function-system/078-math10-elementary-function-optimization-strategy.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Verified EFIR optimization with checked certificates | Retained |
| [MATH11](../docs/phase-05-mathematical-and-elementary-function-system/079-math11-math-verification-and-conformance-test-plan.md#requirements) / [criteria](../docs/phase-05-mathematical-and-elementary-function-system/079-math11-math-verification-and-conformance-test-plan.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Positive/negative math fixtures and certificate validation | Retained |
| [C1](../docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow) | Data-oriented pass graph and preserved facts | Retained |
| [C2](../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md#conformance-criteria) | [reader-core-invocation](#package-reader-core-invocation), [reader-workflow](#package-reader-workflow) | Deterministic decoding, tokens, forms and spans | Retained |
| [C3](../docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md#conformance-criteria) | [syntax-workflow](#package-syntax-workflow) | Syntax identity, hygiene and origin chains | Retained |
| [C4](../docs/phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md#conformance-criteria) | [expand-workflow](#package-expand-workflow) | Deterministic macro traces and declared build effects | Retained |
| [C5](../docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md#conformance-criteria) | [reader-core-invocation](#package-reader-core-invocation), [001-paired-source-core](#package-001-paired-source-core), [resolve-owner-source-core-verification](#package-resolve-owner-source-core-verification), [resolve-workflow](#package-resolve-workflow) | Binding identities in executable and declaration positions | Retained |
| [C6](../docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md#conformance-criteria) | [reader-core-invocation](#package-reader-core-invocation), [001-paired-source-core](#package-001-paired-source-core), [core-workflow](#package-core-workflow), [resolve-owner-source-core-verification](#package-resolve-owner-source-core-verification) | Explicit core nodes and source-to-core maps | Retained |
| [C7](../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md#conformance-criteria) | [types-workflow](#package-types-workflow) | Solved constraints, conversions and typed-core facts | Retained |
| [C8](../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md#conformance-criteria) | [effects-workflow](#package-effects-workflow) | Direct/latent effects and call graph enforcement | Retained |
| [C9](../docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Borrow, move and linear resource analysis | Retained |
| [C10](../docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Complete safety operation inventory and outcomes | Retained |
| [C11](../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md#conformance-criteria) | [mir-workflow](#package-mir-workflow) | Verified typed/effected MIR and fact references | Retained |
| [C12](../docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow), [domains-workflow](#package-domains-workflow) | Domain schema registration and source/MIR anchors | Retained |
| [C13](../docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md#conformance-criteria) | [opt-workflow](#package-opt-workflow) | Pass contracts, deterministic order and verified transforms | Retained |
| [C14](../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md#conformance-criteria) | [lower-workflow](#package-lower-workflow) | Verified input eligibility and explicit target lowering | Retained |
| [C15](../docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md#conformance-criteria) | [diag-workflow](#package-diag-workflow) | Stable structured diagnostics and generated origins | Retained |
| [C16](../docs/phase-06-compiler-architecture/095-c16-incremental-compilation-design.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/095-c16-incremental-compilation-design.md#conformance-criteria) | [build-incremental-helper-names](#package-build-incremental-helper-names), [build-incremental-operation-names](#package-build-incremental-operation-names), [build-incremental-projection-names](#package-build-incremental-projection-names), [compiler-api-workflow](#package-compiler-api-workflow) | Stage-specific cache identity and conservative invalidation | Retained |
| [C17](../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow) | Versioned plugin manifests, grants and pass execution | Retained |
| [C18](../docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md#requirements) / [criteria](../docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow) | Risk-appropriate actual verifier/proof evidence | Retained |
| [B1](../docs/phase-07-backend-architecture/098-b1-backend-interface-specification.md#requirements) / [criteria](../docs/phase-07-backend-architecture/098-b1-backend-interface-specification.md#conformance-criteria) | [backends-workflow](#package-backends-workflow) | Verified shared backend envelope and ABI assumptions | Retained |
| [B2](../docs/phase-07-backend-architecture/099-b2-c-backend-design.md#requirements) / [criteria](../docs/phase-07-backend-architecture/099-b2-c-backend-design.md#conformance-criteria) | [backends-workflow](#package-backends-workflow) | Disciplined C emission without target undefined behavior | Retained |
| [B3](../docs/phase-07-backend-architecture/100-b3-llvm-backend-design.md#requirements) / [criteria](../docs/phase-07-backend-architecture/100-b3-llvm-backend-design.md#conformance-criteria) | [backends-workflow](#package-backends-workflow) | LLVM triple/layout/features and preserved semantic facts | Retained |
| [B4](../docs/phase-07-backend-architecture/101-b4-wasm-backend-design.md#requirements) / [criteria](../docs/phase-07-backend-architecture/101-b4-wasm-backend-design.md#conformance-criteria) | [backends-workflow](#package-backends-workflow) | Wasm module/component interfaces and resources | Retained |
| [B5](../docs/phase-07-backend-architecture/102-b5-jvm-backend-design.md#requirements) / [criteria](../docs/phase-07-backend-architecture/102-b5-jvm-backend-design.md#conformance-criteria) | [backends-workflow](#package-backends-workflow) | JVM class/module artifacts and hosted boundaries | Retained |
| [B6](../docs/phase-07-backend-architecture/103-b6-javascript-typescript-backend-design.md#requirements) / [criteria](../docs/phase-07-backend-architecture/103-b6-javascript-typescript-backend-design.md#conformance-criteria) | [backends-workflow](#package-backends-workflow) | JS/TS runtime/module declarations and source maps | Retained |
| [B7](../docs/phase-07-backend-architecture/104-b7-mlir-backend-design.md#requirements) / [criteria](../docs/phase-07-backend-architecture/104-b7-mlir-backend-design.md#conformance-criteria) | [backends-workflow](#package-backends-workflow) | Declared MLIR dialects and verified operation mapping | Retained |
| [B8](../docs/phase-07-backend-architecture/105-b8-gpu-backend-design.md#requirements) / [criteria](../docs/phase-07-backend-architecture/105-b8-gpu-backend-design.md#conformance-criteria) | [backends-workflow](#package-backends-workflow) | GPU kernel/device boundaries and supported features | Retained |
| [B9](../docs/phase-07-backend-architecture/106-b9-hdl-backend-design.md#requirements) / [criteria](../docs/phase-07-backend-architecture/106-b9-hdl-backend-design.md#conformance-criteria) | [backends-workflow](#package-backends-workflow) | HDL widths, clock/reset and hardware artifacts | Retained |
| [B10](../docs/phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md#requirements) / [criteria](../docs/phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md#conformance-criteria) | [backends-workflow](#package-backends-workflow) | Workflow graph, schemas, replay and durable effects | Retained |
| [B11](../docs/phase-07-backend-architecture/108-b11-query-relational-backend-design.md#requirements) / [criteria](../docs/phase-07-backend-architecture/108-b11-query-relational-backend-design.md#conformance-criteria) | [backends-workflow](#package-backends-workflow) | Typed relational plans, dialects and parameter bindings | Retained |
| [B12](../docs/phase-07-backend-architecture/109-b12-mobile-backend-design.md#requirements) / [criteria](../docs/phase-07-backend-architecture/109-b12-mobile-backend-design.md#conformance-criteria) | [backends-workflow](#package-backends-workflow) | Mobile bundles, lifecycle and platform permissions | Retained |
| [B13](../docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md#requirements) / [criteria](../docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md#conformance-criteria) | [backends-workflow](#package-backends-workflow), [emit-workflow](#package-emit-workflow) | Common artifact manifests and provenance graph | Retained |
| [B14](../docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md#requirements) / [criteria](../docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md#conformance-criteria) | [backends-workflow](#package-backends-workflow), [emit-workflow](#package-emit-workflow) | Per-backend positive/negative and differential conformance | Retained |
| [R1](../docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md#requirements) / [criteria](../docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md#conformance-criteria) | [runtime-workflow](#package-runtime-workflow) | Explicit linked/generated/delegated service selection | Retained |
| [R2](../docs/phase-08-runtime-architecture/113-r2-no-runtime-execution-model.md#requirements) / [criteria](../docs/phase-08-runtime-architecture/113-r2-no-runtime-execution-model.md#conformance-criteria) | [runtime-workflow](#package-runtime-workflow) | No-runtime artifacts reject forbidden services | Retained |
| [R3](../docs/phase-08-runtime-architecture/114-r3-minimal-native-runtime-design.md#requirements) / [criteria](../docs/phase-08-runtime-architecture/114-r3-minimal-native-runtime-design.md#conformance-criteria) | [runtime-workflow](#package-runtime-workflow) | Declared native startup, panic, allocator and FFI services | Retained |
| [R4](../docs/phase-08-runtime-architecture/115-r4-managed-runtime-design.md#requirements) / [criteria](../docs/phase-08-runtime-architecture/115-r4-managed-runtime-design.md#conformance-criteria) | [runtime-workflow](#package-runtime-workflow) | Managed host versions and normalized null/errors | Retained |
| [R5](../docs/phase-08-runtime-architecture/116-r5-memory-runtime-design.md#requirements) / [criteria](../docs/phase-08-runtime-architecture/116-r5-memory-runtime-design.md#conformance-criteria) | [runtime-workflow](#package-runtime-workflow) | Memory provider manifests and lifetime constraints | Retained |
| [R6](../docs/phase-08-runtime-architecture/117-r6-concurrency-runtime-design.md#requirements) / [criteria](../docs/phase-08-runtime-architecture/117-r6-concurrency-runtime-design.md#conformance-criteria) | [runtime-workflow](#package-runtime-workflow) | Schedulers, cancellation, sharing and synchronization | Retained |
| [R7](../docs/phase-08-runtime-architecture/118-r7-distributed-runtime-design.md#requirements) / [criteria](../docs/phase-08-runtime-architecture/118-r7-distributed-runtime-design.md#conformance-criteria) | [runtime-workflow](#package-runtime-workflow) | Distributed schemas, event logs and replay | Retained |
| [R8](../docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md#requirements) / [criteria](../docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md#conformance-criteria) | [runtime-workflow](#package-runtime-workflow) | AI provider, prompt, tool and policy ledgers | Retained |
| [R9](../docs/phase-08-runtime-architecture/120-r9-repl-and-interactive-runtime-design.md#requirements) / [criteria](../docs/phase-08-runtime-architecture/120-r9-repl-and-interactive-runtime-design.md#conformance-criteria) | [runtime-workflow](#package-runtime-workflow), [tools-workflow](#package-tools-workflow) | REPL session grants and normal compiler checks | Retained |
| [R10](../docs/phase-08-runtime-architecture/121-r10-ffi-runtime-design.md#requirements) / [criteria](../docs/phase-08-runtime-architecture/121-r10-ffi-runtime-design.md#conformance-criteria) | [runtime-workflow](#package-runtime-workflow) | FFI ABI, ownership, callbacks and cleanup | Retained |
| [R11](../docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md#requirements) / [criteria](../docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [runtime-workflow](#package-runtime-workflow) | Runtime grant decisions at actual service hooks | Retained |
| [R12](../docs/phase-08-runtime-architecture/123-r12-runtime-observability-and-diagnostics-design.md#requirements) / [criteria](../docs/phase-08-runtime-architecture/123-r12-runtime-observability-and-diagnostics-design.md#conformance-criteria) | [diag-workflow](#package-diag-workflow), [runtime-workflow](#package-runtime-workflow) | Schema-bound observability and secret redaction | Retained |
| [DOM1](../docs/phase-09-domain-specific-computing-coverage/124-dom1-hardware-computing-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/124-dom1-hardware-computing-domain-specification.md#conformance-criteria) | [platform-workflow](#package-platform-workflow) | Hardware clock/reset, IR and bounded-state conformance | Retained |
| [DOM2](../docs/phase-09-domain-specific-computing-coverage/125-dom2-firmware-and-embedded-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/125-dom2-firmware-and-embedded-domain-specification.md#conformance-criteria) | [platform-workflow](#package-platform-workflow) | Firmware startup, memory maps and interrupt behavior | Retained |
| [DOM3](../docs/phase-09-domain-specific-computing-coverage/126-dom3-operating-system-and-kernel-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/126-dom3-operating-system-and-kernel-domain-specification.md#conformance-criteria) | [platform-workflow](#package-platform-workflow) | Kernel privilege, syscalls and unsafe memory boundaries | Retained |
| [DOM4](../docs/phase-09-domain-specific-computing-coverage/127-dom4-drivers-and-device-interaction-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/127-dom4-drivers-and-device-interaction-domain-specification.md#conformance-criteria) | [platform-workflow](#package-platform-workflow) | Device register schemas and volatile/MMIO contracts | Retained |
| [DOM5](../docs/phase-09-domain-specific-computing-coverage/128-dom5-high-performance-native-computing-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/128-dom5-high-performance-native-computing-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Native performance with proof and scalar fallback | Retained |
| [DOM6](../docs/phase-09-domain-specific-computing-coverage/129-dom6-web-frontend-and-ui-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/129-dom6-web-frontend-and-ui-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Web UI artifacts and browser capabilities | Retained |
| [DOM7](../docs/phase-09-domain-specific-computing-coverage/130-dom7-mobile-application-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/130-dom7-mobile-application-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Mobile lifecycle, permissions and bundles | Retained |
| [DOM8](../docs/phase-09-domain-specific-computing-coverage/131-dom8-backend-services-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/131-dom8-backend-services-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Typed service routes and validated input | Retained |
| [DOM9](../docs/phase-09-domain-specific-computing-coverage/132-dom9-distributed-systems-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/132-dom9-distributed-systems-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Distributed schemas, replay and recorded nondeterminism | Retained |
| [DOM10](../docs/phase-09-domain-specific-computing-coverage/133-dom10-database-and-storage-engine-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/133-dom10-database-and-storage-engine-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Database/query safety and storage layout contracts | Retained |
| [DOM11](../docs/phase-09-domain-specific-computing-coverage/134-dom11-data-query-and-analytics-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/134-dom11-data-query-and-analytics-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Schema-bound datasets, aggregates and sinks | Retained |
| [DOM12](../docs/phase-09-domain-specific-computing-coverage/135-dom12-scientific-and-numeric-computing-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/135-dom12-scientific-and-numeric-computing-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Scientific domains, precision and math certificates | Retained |
| [DOM13](../docs/phase-09-domain-specific-computing-coverage/136-dom13-gpu-and-accelerator-computing-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/136-dom13-gpu-and-accelerator-computing-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | GPU kernels and host/device memory ownership | Retained |
| [DOM14](../docs/phase-09-domain-specific-computing-coverage/137-dom14-game-engine-and-simulation-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/137-dom14-game-engine-and-simulation-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Simulation determinism and replayable frame behavior | Retained |
| [DOM15](../docs/phase-09-domain-specific-computing-coverage/138-dom15-security-and-cryptography-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/138-dom15-security-and-cryptography-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Secret-bearing crypto/domain artifacts and policy | Retained |
| [DOM16](../docs/phase-09-domain-specific-computing-coverage/139-dom16-blockchain-and-smart-contract-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/139-dom16-blockchain-and-smart-contract-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Deterministic contract state, ABI and events | Retained |
| [DOM17](../docs/phase-09-domain-specific-computing-coverage/140-dom17-compiler-and-language-tooling-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/140-dom17-compiler-and-language-tooling-domain-specification.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow), [tools-workflow](#package-tools-workflow) | Compiler tools consume actual typed artifacts | Retained |
| [DOM18](../docs/phase-09-domain-specific-computing-coverage/141-dom18-ai-and-agentic-computing-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/141-dom18-ai-and-agentic-computing-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | AI agent/tool/model artifacts and authority | Retained |
| [DOM19](../docs/phase-09-domain-specific-computing-coverage/142-dom19-formal-verification-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/142-dom19-formal-verification-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Formal claims, trusted basis and checked proofs | Retained |
| [DOM20](../docs/phase-09-domain-specific-computing-coverage/143-dom20-scripting-shell-and-automation-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/143-dom20-scripting-shell-and-automation-domain-specification.md#conformance-criteria) | [cli-workflow](#package-cli-workflow), [domains-workflow](#package-domains-workflow) | Explicit script/process effects and command schemas | Retained |
| [DOM21](../docs/phase-09-domain-specific-computing-coverage/144-dom21-low-code-visual-programming-and-workflow-domain-specification.md#requirements) / [criteria](../docs/phase-09-domain-specific-computing-coverage/144-dom21-low-code-visual-programming-and-workflow-domain-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Typed visual graphs expose effects and execution origins | Retained |
| [S1](../docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md#requirements) / [criteria](../docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Schema manifests, validators and generated types | Retained |
| [S2](../docs/phase-10-schema-data-and-interop/146-s2-serialization-specification.md#requirements) / [criteria](../docs/phase-10-schema-data-and-interop/146-s2-serialization-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Versioned codecs validate untrusted input | Retained |
| [S3](../docs/phase-10-schema-data-and-interop/147-s3-canonical-data-format-specification.md#requirements) / [criteria](../docs/phase-10-schema-data-and-interop/147-s3-canonical-data-format-specification.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [domains-workflow](#package-domains-workflow) | Canonical byte encoding and deterministic ordering | Retained |
| [S4](../docs/phase-10-schema-data-and-interop/148-s4-graphql-generation-design.md#requirements) / [criteria](../docs/phase-10-schema-data-and-interop/148-s4-graphql-generation-design.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | GraphQL schema/resolver/nullability generation | Retained |
| [S5](../docs/phase-10-schema-data-and-interop/149-s5-openapi-generation-design.md#requirements) / [criteria](../docs/phase-10-schema-data-and-interop/149-s5-openapi-generation-design.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | OpenAPI routes and validated HTTP boundaries | Retained |
| [S6](../docs/phase-10-schema-data-and-interop/150-s6-database-mapping-and-migration-design.md#requirements) / [criteria](../docs/phase-10-schema-data-and-interop/150-s6-database-mapping-and-migration-design.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | SQL mappings and explicit migration compatibility | Retained |
| [S7](../docs/phase-10-schema-data-and-interop/151-s7-binary-encoding-and-abi-schema-specification.md#requirements) / [criteria](../docs/phase-10-schema-data-and-interop/151-s7-binary-encoding-and-abi-schema-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Binary layout and ABI schema compatibility | Retained |
| [S8](../docs/phase-10-schema-data-and-interop/152-s8-typed-configuration-and-environment-specification.md#requirements) / [criteria](../docs/phase-10-schema-data-and-interop/152-s8-typed-configuration-and-environment-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Config source precedence and explicit authority | Retained |
| [S9](../docs/phase-10-schema-data-and-interop/153-s9-artifact-schema-specification.md#requirements) / [criteria](../docs/phase-10-schema-data-and-interop/153-s9-artifact-schema-specification.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [domains-workflow](#package-domains-workflow) | Artifact schemas, provenance and canonical identities | Retained |
| [A1](../docs/phase-11-ai-and-agentic-programming/154-a1-ai-programming-model-specification.md#requirements) / [criteria](../docs/phase-11-ai-and-agentic-programming/154-a1-ai-programming-model-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | AI artifacts expose provider, schema and replay boundaries | Retained |
| [A2](../docs/phase-11-ai-and-agentic-programming/155-a2-model-provider-specification.md#requirements) / [criteria](../docs/phase-11-ai-and-agentic-programming/155-a2-model-provider-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Model/provider capability and declared call effects | Retained |
| [A3](../docs/phase-11-ai-and-agentic-programming/156-a3-prompt-and-structured-output-specification.md#requirements) / [criteria](../docs/phase-11-ai-and-agentic-programming/156-a3-prompt-and-structured-output-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Prompt authority partitions and validated structured output | Retained |
| [A4](../docs/phase-11-ai-and-agentic-programming/157-a4-tool-definition-specification.md#requirements) / [criteria](../docs/phase-11-ai-and-agentic-programming/157-a4-tool-definition-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Typed tool interfaces and visible effects | Retained |
| [A5](../docs/phase-11-ai-and-agentic-programming/158-a5-agent-definition-specification.md#requirements) / [criteria](../docs/phase-11-ai-and-agentic-programming/158-a5-agent-definition-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Agent dependencies and declared tool authority | Retained |
| [A6](../docs/phase-11-ai-and-agentic-programming/159-a6-agent-workflow-specification.md#requirements) / [criteria](../docs/phase-11-ai-and-agentic-programming/159-a6-agent-workflow-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Recorded AI effects and replayable workflow graphs | Retained |
| [A7](../docs/phase-11-ai-and-agentic-programming/160-a7-memory-and-retrieval-specification.md#requirements) / [criteria](../docs/phase-11-ai-and-agentic-programming/160-a7-memory-and-retrieval-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Scoped memory access, retention and provenance | Retained |
| [A8](../docs/phase-11-ai-and-agentic-programming/161-a8-ai-policy-and-safety-model.md#requirements) / [criteria](../docs/phase-11-ai-and-agentic-programming/161-a8-ai-policy-and-safety-model.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [domains-workflow](#package-domains-workflow), [legality-workflow](#package-legality-workflow) | Explicit policy decisions and auditable denials | Retained |
| [A9](../docs/phase-11-ai-and-agentic-programming/162-a9-ai-evaluation-framework-design.md#requirements) / [criteria](../docs/phase-11-ai-and-agentic-programming/162-a9-ai-evaluation-framework-design.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | Versioned eval datasets, thresholds and invalidation | Retained |
| [A10](../docs/phase-11-ai-and-agentic-programming/163-a10-human-in-the-loop-and-human-review-workflow-specification.md#requirements) / [criteria](../docs/phase-11-ai-and-agentic-programming/163-a10-human-in-the-loop-and-human-review-workflow-specification.md#conformance-criteria) | [domains-workflow](#package-domains-workflow), [tools-workflow](#package-tools-workflow) | Payload-bound human-review decisions and replay | Retained |
| [A11](../docs/phase-11-ai-and-agentic-programming/164-a11-prompt-injection-and-tool-misuse-defense-specification.md#requirements) / [criteria](../docs/phase-11-ai-and-agentic-programming/164-a11-prompt-injection-and-tool-misuse-defense-specification.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [domains-workflow](#package-domains-workflow), [legality-workflow](#package-legality-workflow) | Untrusted content remains data without authority escalation | Retained |
| [PKG1](../docs/phase-12-build-package-and-artifact-system/165-pkg1-project-file-specification.md#requirements) / [criteria](../docs/phase-12-build-package-and-artifact-system/165-pkg1-project-file-specification.md#conformance-criteria) | [build-workflow](#package-build-workflow) | Data-only project manifest and profile/target identity | Retained |
| [PKG2](../docs/phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md#requirements) / [criteria](../docs/phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md#conformance-criteria) | [build-workflow](#package-build-workflow) | Typed build graph with declared effects and cache keys | Retained |
| [PKG3](../docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md#requirements) / [criteria](../docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md#conformance-criteria) | [build-workflow](#package-build-workflow) | Schema-validated artifacts with source provenance | Retained |
| [PKG4](../docs/phase-12-build-package-and-artifact-system/168-pkg4-package-manager-specification.md#requirements) / [criteria](../docs/phase-12-build-package-and-artifact-system/168-pkg4-package-manager-specification.md#conformance-criteria) | [build-workflow](#package-build-workflow) | Verified package transport and visible dependency risks | Retained |
| [PKG5](../docs/phase-12-build-package-and-artifact-system/169-pkg5-dependency-resolution-specification.md#requirements) / [criteria](../docs/phase-12-build-package-and-artifact-system/169-pkg5-dependency-resolution-specification.md#conformance-criteria) | [build-workflow](#package-build-workflow) | Deterministic constraint resolution and offline locks | Retained |
| [PKG6](../docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md#requirements) / [criteria](../docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [build-workflow](#package-build-workflow) | Derived capability summaries and explicit grants | Retained |
| [PKG7](../docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md#requirements) / [criteria](../docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md#conformance-criteria) | [build-workflow](#package-build-workflow) | Reproducible locked builds and declared environment | Retained |
| [PKG8](../docs/phase-12-build-package-and-artifact-system/172-pkg8-package-safety-and-audit-metadata-specification.md#requirements) / [criteria](../docs/phase-12-build-package-and-artifact-system/172-pkg8-package-safety-and-audit-metadata-specification.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [build-workflow](#package-build-workflow) | Safety/unsafe package metadata and audit links | Retained |
| [PKG9](../docs/phase-12-build-package-and-artifact-system/173-pkg9-private-registry-and-latent-package-space-design.md#requirements) / [criteria](../docs/phase-12-build-package-and-artifact-system/173-pkg9-private-registry-and-latent-package-space-design.md#conformance-criteria) | [build-workflow](#package-build-workflow) | Registry identity, trust and package distribution records | Retained |
| [PKG10](../docs/phase-12-build-package-and-artifact-system/174-pkg10-supply-chain-security-and-provenance-specification.md#requirements) / [criteria](../docs/phase-12-build-package-and-artifact-system/174-pkg10-supply-chain-security-and-provenance-specification.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [build-workflow](#package-build-workflow) | Supply-chain provenance and tamper rejection | Retained |
| [PKG11](../docs/phase-12-build-package-and-artifact-system/175-pkg11-cross-compilation-and-target-matrix-specification.md#requirements) / [criteria](../docs/phase-12-build-package-and-artifact-system/175-pkg11-cross-compilation-and-target-matrix-specification.md#conformance-criteria) | [build-workflow](#package-build-workflow) | Cross-target artifact matrices and incompatibility rejection | Retained |
| [PKG12](../docs/phase-12-build-package-and-artifact-system/176-pkg12-artifact-signing-verification-and-sbom-specification.md#requirements) / [criteria](../docs/phase-12-build-package-and-artifact-system/176-pkg12-artifact-signing-verification-and-sbom-specification.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [build-workflow](#package-build-workflow) | Signature verification, key policy and SBOM identities | Retained |
| [T1](../docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md#requirements) / [criteria](../docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md#conformance-criteria) | [cli-workflow](#package-cli-workflow) | CLI command families delegate to actual services | Retained |
| [T2](../docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md#requirements) / [criteria](../docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | REPL session UX preserves compiler and authority facts | Retained |
| [T3](../docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md#requirements) / [criteria](../docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | Formatting preserves syntax and source semantics | Retained |
| [T4](../docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md#requirements) / [criteria](../docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | Lint findings use real semantic facts and stable diagnostics | Retained |
| [T5](../docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md#requirements) / [criteria](../docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | Editor protocol uses current syntax/types/diagnostics | Retained |
| [T6](../docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md#requirements) / [criteria](../docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | Debugger sessions preserve target and source identity | Retained |
| [T7](../docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md#requirements) / [criteria](../docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | Documentation derives from declared API metadata | Retained |
| [T8](../docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md#requirements) / [criteria](../docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | Development server records builds and runtime boundaries | Retained |
| [T9](../docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md#requirements) / [criteria](../docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | Registry views expose package trust and capabilities | Retained |
| [T10](../docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md#requirements) / [criteria](../docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | IR inspection exposes actual stage artifacts and origins | Retained |
| [T11](../docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md#requirements) / [criteria](../docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | Performance inspection preserves measured subject identity | Retained |
| [T12](../docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md#requirements) / [criteria](../docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | Safety inspection exposes outcomes, audits and proofs | Retained |
| [T13](../docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md#requirements) / [criteria](../docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | AI tooling preserves schemas, policy and human review | Retained |
| [TEST1](../docs/phase-14-testing-verification-and-conformance/190-test1-language-conformance-test-plan.md#requirements) / [criteria](../docs/phase-14-testing-verification-and-conformance/190-test1-language-conformance-test-plan.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow) | Portable language fixtures with types/effects and stable diagnostics | Added |
| [TEST2](../docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md#requirements) / [criteria](../docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow) | Pass-level positive/negative and composition regressions | Retained |
| [TEST3](../docs/phase-14-testing-verification-and-conformance/192-test3-runtime-test-strategy.md#requirements) / [criteria](../docs/phase-14-testing-verification-and-conformance/192-test3-runtime-test-strategy.md#conformance-criteria) | [runtime-workflow](#package-runtime-workflow) | Per-runtime execution, denial, cleanup and replay fixtures | Added |
| [TEST4](../docs/phase-14-testing-verification-and-conformance/193-test4-profile-compliance-test-plan.md#requirements) / [criteria](../docs/phase-14-testing-verification-and-conformance/193-test4-profile-compliance-test-plan.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Profile matrix acceptance and forbidden-service rejection | Retained |
| [TEST5](../docs/phase-14-testing-verification-and-conformance/194-test5-safety-conformance-test-plan.md#requirements) / [criteria](../docs/phase-14-testing-verification-and-conformance/194-test5-safety-conformance-test-plan.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Safety outcomes, unsafe audits and negative cases | Retained |
| [TEST6](../docs/phase-14-testing-verification-and-conformance/195-test6-backend-conformance-test-plan.md#requirements) / [criteria](../docs/phase-14-testing-verification-and-conformance/195-test6-backend-conformance-test-plan.md#conformance-criteria) | [backends-workflow](#package-backends-workflow) | Target matrix semantics, ABI and source-map conformance | Added |
| [TEST7](../docs/phase-14-testing-verification-and-conformance/196-test7-standard-library-test-strategy.md#requirements) / [criteria](../docs/phase-14-testing-verification-and-conformance/196-test7-standard-library-test-strategy.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Per-export library behavior and metadata conformance | Retained |
| [TEST8](../docs/phase-14-testing-verification-and-conformance/197-test8-ai-and-workflow-evaluation-strategy.md#requirements) / [criteria](../docs/phase-14-testing-verification-and-conformance/197-test8-ai-and-workflow-evaluation-strategy.md#conformance-criteria) | [domains-workflow](#package-domains-workflow) | AI/workflow evaluation and replay evidence | Retained |
| [TEST9](../docs/phase-14-testing-verification-and-conformance/198-test9-fuzzing-and-property-testing-plan.md#requirements) / [criteria](../docs/phase-14-testing-verification-and-conformance/198-test9-fuzzing-and-property-testing-plan.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow) | Fuzz/property generators and reproducible failures | Retained |
| [TEST10](../docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md#requirements) / [criteria](../docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md#conformance-criteria) | [compiler-api-workflow](#package-compiler-api-workflow) | Differential comparison under declared semantic modes | Retained |
| [TEST11](../docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md#requirements) / [criteria](../docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md#conformance-criteria) | [domains-workflow](#package-domains-workflow), [effects-workflow](#package-effects-workflow), [legality-workflow](#package-legality-workflow), [types-workflow](#package-types-workflow) | Formal claims bind checker, assumptions and input hashes | Retained |
| [TEST12](../docs/phase-14-testing-verification-and-conformance/201-test12-performance-regression-test-plan.md#requirements) / [criteria](../docs/phase-14-testing-verification-and-conformance/201-test12-performance-regression-test-plan.md#conformance-criteria) | [tools-workflow](#package-tools-workflow) | Environment-bound performance regression decisions | Retained |
| [TEST13](../docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md#requirements) / [criteria](../docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md#conformance-criteria) | [release-workflow](#package-release-workflow) | Stage rebuild equivalence, provenance and TCB delta | Added |
| [BOOT1](../docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md#requirements) / [criteria](../docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md#conformance-criteria) | [release-workflow](#package-release-workflow) | Declared trusted inputs and stage conformance | Retained |
| [BOOT2](../docs/phase-15-bootstrap-and-self-hosting/204-boot2-seed-compiler-design.md#requirements) / [criteria](../docs/phase-15-bootstrap-and-self-hosting/204-boot2-seed-compiler-design.md#conformance-criteria) | [release-workflow](#package-release-workflow) | Seed subset and explicit unsupported forms | Retained |
| [BOOT3](../docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md#requirements) / [criteria](../docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md#conformance-criteria) | [release-workflow](#package-release-workflow) | Module-by-module self-hosting with stage equivalence | Retained |
| [BOOT4](../docs/phase-15-bootstrap-and-self-hosting/206-boot4-compiler-in-gravity-coding-standard.md#requirements) / [criteria](../docs/phase-15-bootstrap-and-self-hosting/206-boot4-compiler-in-gravity-coding-standard.md#conformance-criteria) | [release-workflow](#package-release-workflow) | Compiler modules obey meta profile and effect rules | Retained |
| [BOOT5](../docs/phase-15-bootstrap-and-self-hosting/207-boot5-stage-compatibility-matrix.md#requirements) / [criteria](../docs/phase-15-bootstrap-and-self-hosting/207-boot5-stage-compatibility-matrix.md#conformance-criteria) | [release-workflow](#package-release-workflow) | Stage support/unsupported compatibility matrix | Retained |
| [BOOT6](../docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md#requirements) / [criteria](../docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md#conformance-criteria) | [release-workflow](#package-release-workflow) | Locked reproducible bootstrap and compiler lineage | Retained |
| [BOOT7](../docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md#requirements) / [criteria](../docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md#conformance-criteria) | [release-workflow](#package-release-workflow) | Pinned compiler equivalence and reviewed differences | Retained |
| [BOOT8](../docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md#requirements) / [criteria](../docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md#conformance-criteria) | [release-workflow](#package-release-workflow) | Traversable stage provenance and verifiable attestations | Retained |
| [STD1](../docs/phase-16-standard-library/211-std1-standard-library-architecture.md#requirements) / [criteria](../docs/phase-16-standard-library/211-std1-standard-library-architecture.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Per-export profile/effect/stability inventory | Retained |
| [STD2](../docs/phase-16-standard-library/212-std2-core-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/212-std2-core-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Portable core library operations preserve semantics | Retained |
| [STD3](../docs/phase-16-standard-library/213-std3-collections-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/213-std3-collections-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Persistent collection value semantics and allocation metadata | Retained |
| [STD4](../docs/phase-16-standard-library/214-std4-string-and-text-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/214-std4-string-and-text-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Immutable text, Unicode and explicit byte conversions | Retained |
| [STD5](../docs/phase-16-standard-library/215-std5-numeric-and-math-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/215-std5-numeric-and-math-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Numeric modes, representability and math evidence | Retained |
| [STD6](../docs/phase-16-standard-library/216-std6-memory-and-resource-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/216-std6-memory-and-resource-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Ownership, lifetimes and exactly-once resource APIs | Retained |
| [STD7](../docs/phase-16-standard-library/217-std7-concurrency-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/217-std7-concurrency-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Structured concurrency and declared scheduling effects | Retained |
| [STD8](../docs/phase-16-standard-library/218-std8-io-and-filesystem-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/218-std8-io-and-filesystem-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Root-scoped filesystem capabilities and stream behavior | Retained |
| [STD9](../docs/phase-16-standard-library/219-std9-network-and-http-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/219-std9-network-and-http-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Network authority, protocols and cancellation policy | Retained |
| [STD10](../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Schema validation, codecs and canonical bytes | Retained |
| [STD11](../docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Parameterized database operations and explicit transactions | Retained |
| [STD12](../docs/phase-16-standard-library/222-std12-workflow-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/222-std12-workflow-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Typed workflow graphs, replay and compensation | Retained |
| [STD13](../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | AI provider/tool/memory authority and validated output | Retained |
| [STD14](../docs/phase-16-standard-library/224-std14-testing-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/224-std14-testing-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Structured test artifacts and reproducible fixtures | Retained |
| [STD15](../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Compiler support preserves syntax and pass contracts | Retained |
| [STD16](../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Explicit platform services and host/target distinction | Retained |
| [STD17](../docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Hardware register metadata and interrupt safety | Retained |
| [STD18](../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Cryptographic provider policy, secret lifecycle and vectors | Retained |
| [STD19](../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md#requirements) / [criteria](../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Typed UI state/events and target availability | Retained |
| [STD20](../docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md#requirements) / [criteria](../docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Export stability surfaces and compatibility evidence | Retained |
| [GOV1](../docs/phase-17-governance-and-evolution/231-gov1-language-evolution-process.md#requirements) / [criteria](../docs/phase-17-governance-and-evolution/231-gov1-language-evolution-process.md#conformance-criteria) | [Proposed evolution workflow](#evolution-workflow-proposal) | Owned language-change records and decision history | Gap |
| [GOV2](../docs/phase-17-governance-and-evolution/232-gov2-compatibility-policy.md#requirements) / [criteria](../docs/phase-17-governance-and-evolution/232-gov2-compatibility-policy.md#conformance-criteria) | [build-workflow](#package-build-workflow), [release-workflow](#package-release-workflow) | Baseline-scoped release compatibility and migration records | Added |
| [GOV3](../docs/phase-17-governance-and-evolution/233-gov3-standard-library-governance.md#requirements) / [criteria](../docs/phase-17-governance-and-evolution/233-gov3-standard-library-governance.md#conformance-criteria) | [stdlib-workflow](#package-stdlib-workflow) | Owned standard modules and export stability | Retained |
| [GOV4](../docs/phase-17-governance-and-evolution/234-gov4-security-review-process.md#requirements) / [criteria](../docs/phase-17-governance-and-evolution/234-gov4-security-review-process.md#conformance-criteria) | [auth-workflow](#package-auth-workflow), [legality-workflow](#package-legality-workflow) | Security-impact reviews and residual risk records | Retained |
| [GOV5](../docs/phase-17-governance-and-evolution/235-gov5-target-support-policy.md#requirements) / [criteria](../docs/phase-17-governance-and-evolution/235-gov5-target-support-policy.md#conformance-criteria) | [backends-workflow](#package-backends-workflow), [platform-workflow](#package-platform-workflow) | Owned target tiers, profile matrices and promotion evidence | Added |
| [GOV6](../docs/phase-17-governance-and-evolution/236-gov6-rfc-process.md#requirements) / [criteria](../docs/phase-17-governance-and-evolution/236-gov6-rfc-process.md#conformance-criteria) | [Proposed evolution workflow](#evolution-workflow-proposal) | RFC decision artifacts and affected-contract review | Gap |
| [GOV7](../docs/phase-17-governance-and-evolution/237-gov7-experimental-feature-policy.md#requirements) / [criteria](../docs/phase-17-governance-and-evolution/237-gov7-experimental-feature-policy.md#conformance-criteria) | [Proposed evolution workflow](#evolution-workflow-proposal) | Explicit experiment opt-in, expiry and rollback | Gap |
| [GOV8](../docs/phase-17-governance-and-evolution/238-gov8-deprecation-and-stabilization-policy.md#requirements) / [criteria](../docs/phase-17-governance-and-evolution/238-gov8-deprecation-and-stabilization-policy.md#conformance-criteria) | [Proposed evolution workflow](#evolution-workflow-proposal) | Evidence-backed stabilization and scheduled deprecation | Gap |
| [GOV9](../docs/phase-17-governance-and-evolution/239-gov9-unsafe-code-governance-policy.md#requirements) / [criteria](../docs/phase-17-governance-and-evolution/239-gov9-unsafe-code-governance-policy.md#conformance-criteria) | [legality-workflow](#package-legality-workflow) | Owned unsafe islands and auditable safe wrappers | Retained |
| [GOV10](../docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md#requirements) / [criteria](../docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md#conformance-criteria) | [build-workflow](#package-build-workflow) | Build consumes registry evidence; namespace-policy ownership remains proposed | Added |

## Decisions for uncovered or narrowly routed contracts

These interfaces were checked against actual source requirements, package
plans, task sources and evidence limits. Each entry's acceptance is the linked
contract criteria plus the named component boundary above; adding its link
never checks an implementation task. Auth and MIR workflow artifacts are
untouched to avoid overlap with task-authority migrations.

### D5

- [domains-workflow](#package-domains-workflow): Domain replacement claims require named incumbent slices, unsupported behavior, typed interop artifacts and accepted/rejected parity fixtures. A domain demo cannot establish whole-language replacement.

### D6

- [opt-workflow](#package-opt-workflow): Safe optimization must preserve or regenerate safety facts and expose complete performance claims. Throughput cannot justify proofless check elision.
- [tools-workflow](#package-tools-workflow): Performance inspection and benchmark reporting must retain source/compiler/target/harness identity. Reports do not establish optimizer correctness or realtime latency.

### L7

- [core-workflow](#package-core-workflow): Lower match decision trees with branch origins and exactly-once ordered guards; do not decide type narrowing here.
- [effects-workflow](#package-effects-workflow): Include guard effects and preserve their evaluation order, including non-returning control transfer.
- [legality-workflow](#package-legality-workflow): Validate owned/linear pattern moves and evidence for trusted closed shapes; structural matching alone does not validate external data.
- [types-workflow](#package-types-workflow): Check narrowed branch bindings, closed-match exhaustiveness and Boolean normally returning guards after expansion.

### L9

- [core-workflow](#package-core-workflow): Core error/control forms preserve throw/try evaluation and propagation semantics; diagnostic formatting alone cannot implement error behavior.
- [runtime-workflow](#package-runtime-workflow): Normalize host/FFI failures, cleanup and target error representations without leaking foreign exceptions into safe Gravity.

### L11

- [legality-workflow](#package-legality-workflow): Reject races and invalid shared-mutation/ownership transfer before execution; effect labels alone do not establish concurrency safety.
- [runtime-workflow](#package-runtime-workflow): Implement the declared scheduler/task/channel/cancellation behavior with profile-specific service manifests and observable concurrency traces.

### L12

- [build-workflow](#package-build-workflow): Compile-time file/environment/provider inputs and granted effects participate in reproducibility and cache invalidation; grant checking alone is not evaluation.
- [opt-workflow](#package-opt-workflow): Partial evaluation preserves types, numeric modes, profile legality and generated provenance; pure evaluation cannot acquire undeclared host effects.

### L19

- [backends-workflow](#package-backends-workflow): Lower native/managed/Wasm foreign declarations with explicit ABI, layout and unsupported-profile diagnostics.
- [domains-workflow](#package-domains-workflow): Schema-generated bindings and migration shims retain schema digests, error mappings, provenance and incumbent parity; this is not a blanket interop-support claim.
- [runtime-workflow](#package-runtime-workflow): Implement declared host/FFI ABI, ownership, callback, cleanup and error-translation boundaries; host behavior cannot define portable semantics.

### PERF1

- [opt-workflow](#package-opt-workflow): Optimization decisions retain profile/target/input/layout identity, proof state and guard artifacts; reject loss of semantic or safety facts.

### PERF2

- [opt-workflow](#package-opt-workflow): Compare before/after IR against an equivalent lower-level form and report residual allocation, dispatch, checks and host calls honestly.

### PERF3

- [build-workflow](#package-build-workflow): Cache and replay inputs include source/profile/target/provider/build-effect/proof dependencies; changed inputs invalidate specialized artifacts.
- [opt-workflow](#package-opt-workflow): Specialized variants preserve generic semantics under complete guards, source maps and proof inputs.

### PERF4

- [lower-workflow](#package-lower-workflow): Realize target alignment, packing and boundary adapters from explicit layout manifests; reject unsupported or unsafe representations.
- [opt-workflow](#package-opt-workflow): Layout transformations need alias, ownership and address-identity evidence and cannot silently change fixed ABI/persistence boundaries.

### PERF6

- [build-workflow](#package-build-workflow): Record accepted/advisory/stale/rejected/required-missing data status and reproducible profile-data inputs; stale data cannot silently guide releases.
- [opt-workflow](#package-opt-workflow): PGO consumers validate source/typed-core/MIR/compiler/workload identity, staleness policy and preserved facts before using measurements.
- [tools-workflow](#package-tools-workflow): Profile-data producers expose identity, redaction/retention and hot/cold maps; collected data alone is not an accepted PGO transformation.

### PERF7

- [backends-workflow](#package-backends-workflow): Generated target variants preserve source maps and compatibility metadata, with explicit feature guards and truthful dispatch overhead.
- [opt-workflow](#package-opt-workflow): Reject invalid candidates before benchmarking; emit reproducible selection evidence, complete guards and a valid baseline fallback.

### PERF8

- [backends-workflow](#package-backends-workflow): Target intrinsic maps and scalar/guarded fallback must match declared features; strict floating reduction cannot silently reassociate.
- [lower-workflow](#package-lower-workflow): Lower lane/tail/alignment plans only under target feature and numeric/certificate facts; retain checks or reject missing evidence.
- [opt-workflow](#package-opt-workflow): Vector/cache preparation preserves lane independence, alias/bounds proofs, effect order, numeric modes and volatile/MMIO constraints.

### TEST1

- [compiler-api-workflow](#package-compiler-api-workflow): Compose portable language conformance from the owning front-end passes, with fixture/profile/target/type/effect identities and stable diagnostic spans; orchestration does not own each language rule.

### TEST3

- [runtime-workflow](#package-runtime-workflow): Exercise each claimed runtime family with explicit artifact/profile/target/grants and accepted/rejected cleanup, replay, ABI and redaction evidence. Selected compiler services do not close all families.

### TEST6

- [backends-workflow](#package-backends-workflow): Conformance covers the declared target/backend/runtime matrix, observable semantics, unsupported diagnostics, ABI/layout, manifests and source maps; compile-only evidence is labeled as such.

### TEST13

- [release-workflow](#package-release-workflow): Stage admission requires declared trusted inputs, conformance, rebuild comparisons, provenance and TCB delta. Gravity source presence or a wrapper does not establish seed retirement.

### GOV2

- [build-workflow](#package-build-workflow): Consume package/schema/lock compatibility classifications and migration metadata. Compatibility decision authority remains a governance gap in the central mapping.
- [release-workflow](#package-release-workflow): Consume baseline/profile/target/artifact compatibility reports and migration records for release claims; Release does not approve semantic breaks.

### GOV5

- [backends-workflow](#package-backends-workflow): Target support records bind owner, tier, supported/unsupported profiles and actual lowering/conformance evidence; backend availability is not tier promotion.
- [platform-workflow](#package-platform-workflow): Adopted platform targets require explicit tier, runtime/service limits, diagnostics and conformance. This reference selects no ISA, device or physical product.

### GOV10

- [build-workflow](#package-build-workflow): Publication/retrieval verifies package identity, signatures, provenance, SBOM, capability/profile/target metadata and policy-bound yanks/advisories. Namespace adjudication and emergency policy authority remain proposed governance work, not Build authority.

## Grouped semantic review corrections

Diagnostics retains the actual C15 infrastructure boundary in its registry
entry, rather than finite-pilot scope. The following already-linked IDs gain
only the independently identified primary interfaces; their existing links and
source requirements/conformance criteria remain authoritative. These additions
change neither the 236 unique linked count nor the four governance gaps.

- L16 / [expand-workflow](#package-expand-workflow): Reference-equivalence conformance preserves L4 expansion semantics, hygiene, phase separation, declared/granted/replayable build effects and generated-origin chains. Compiler API retains provider registration; generated forms still undergo ordinary downstream validation.
- L18 / [runtime-workflow](#package-runtime-workflow): Execute the declared memory-provider families and test allocation/failure, lifetimes, cleanup/leak behavior, ownership transfer and FFI/device boundaries. Provider registration and compile-time safety facts do not establish actual runtime conformance.
- PERF10 / [opt-workflow](#package-opt-workflow): Elide checks only under a named dominating proof valid after intervening transforms; record proof/pass/profile/target and invalidation. Regenerate invalidated proof, retain residual checks or reject no-checks compilation; never remove policy checks merely for speed.
- SAFE12 / [expand-workflow](#package-expand-workflow): Produce hygiene/capture, macro safety declarations, build-effect traces and generated-origin/unsafe metadata; reject declaration/expansion disagreement. Legality retains downstream safety classification and generated code must pass normal checks.
- SAFE14 / [build-workflow](#package-build-workflow): Resolve pinned lockfile dependencies, record generator/source/build-effect provenance and produce reproducible builds or accepted attestations. Auth retains artifact trust and grants; build evidence cannot silently expand dependency authority.

GOV4 navigation through Auth and Legality covers their authority/trust and
safety/security evidence interfaces. It does not transfer embargo handling,
advisory publication, patch/backport coordination or release decisions to
those components. Those residual responsibilities require the existing
security-response, affected-component and release owners under GOV4, with
governance ownership made explicit where not yet assigned; no new package
link or reassignment is inferred here.

## Evolution workflow proposal

At this mapping's historical source baseline, no package owned the complete change/RFC/experiment/stabilization
lifecycle required by these four contracts. The Compiler API owns executable
passes and extension registration, while Release consumes exact accepted
outcomes. Neither boundary authorizes language policy decisions. The existing
stdlib stability workflow is limited to standard-library exports.

| Unmapped contract | Required workflow output and acceptance boundary |
| --- | --- |
| GOV1 | Durable owned change record, affected-contract list, state/history and links through implementation, conformance and release; reject ownerless changes or untraceable acceptance. |
| GOV6 | RFC record with required design/impact/test/migration sections, review roles and final decision history; accepted RFCs must trace to implementation and validation before release. |
| GOV7 | Owned experiment record with explicit default-off opt-in, profiles, expiry, rollback and stabilization/removal criteria; on/off fixtures and release records must expose every dependency. |
| GOV8 | Exact stabilization surfaces and conformance/compatibility history, or deprecation replacement/diagnostic/schedule/migration record; preserve tests until governed removal. |

The historical proposed key was `evolution-workflow`. Its existing later
reconciliation package is now resumed at the current successor link above.
Suggested accountable role: language-evolution/governance maintainer, with
contract and conformance specialists. This proposal does not assign a person.
Its finite first increment would define the owned record interfaces and one
traceable lifecycle example with accepted/rejected records, using the four
canonical contracts directly. It would not implement a new approval service,
scheduler or routine permission gate, nor change any language semantics.

Historical proposal decisions: confirm that accountable role and cross-component
interfaces, choose the initial lifecycle example, and accept the bounded
package scope. At that baseline the four IDs remained intentional package gaps, not
lost contracts or implied implementation completion. Existing authorized
compiler development continues independently.

Three mapped governance contracts also have policy boundaries that must remain
visible. GOV2 is consumed by Release/Build for baseline compatibility and
migration records; classification/exception approval is not transferred to
those components. GOV5's backend/platform maintainers produce target evidence,
but policy-level tier promotion and deprecation still require governed records.
GOV10's Build interface covers publication/retrieval and policy enforcement;
namespace disputes/transfers, takeover/reservation decisions and emergency yank
policy need a governance owner. These residuals belong in the proposed
lifecycle interface or a separately scoped ecosystem-governance workflow if
its owner determines that boundary is distinct. They are not silently closed
by their package references.

## Validation and review boundary

Run from the repository root:

```bash
clojure -J-Xmx256m -M .specify/validation/contract-mapping.clj
clojure -J-Xmx256m -M tools/validate_gravity_docs.clj
clojure -J-Xmx256m -M tools/validate_full_language_roadmap.clj
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
git diff --check
```

The focused read-only checker validates inventory equality, unique IDs, actual
package references, four-artifact packages, all mapping and changed-plan local
paths/headings, ASCII, explicit gaps and preservation against the named source
baseline. It also exercises missing-ID, duplicate-ID, missing-file,
missing-heading, false-disposition and workflow-misclassification rejection
cases. The last control distinguishes component accountability from historical
finite-pilot scope; it does not prove the semantics of the stated boundary.
It does not implement compiler semantics, approve mapping policy or authenticate historical passing
claims. Existing task/evidence bytes are compared, not rewritten.

The 18 new dispositions and eight extended interfaces have source-requirement
and component-boundary review notes above. The 218 retained dispositions
preserve each existing package's limited scope; a historical finite package remains supporting evidence rather
than owner of its full contract. Whole-language conformance, runtime/backend
execution, performance and seed retirement remain unassessed by this mapping.
The grouped successor addresses independent review findings, including the
Diagnostics boundary and the retained historical-package inventory. Independent
review of the corrected complete matrix and exact patch is pending; no
self-audit or structural validator is recorded as independent acceptance.
