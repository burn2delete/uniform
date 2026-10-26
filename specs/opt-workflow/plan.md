# Implementation plan: Opt workflow migration

Spec: [spec.md](spec.md). Owner: backend workflow migration owner.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts).
The workflow records work and evidence; it does not replace normative authority.

## Approach and exact files

Create only `specs/opt-workflow/spec.md`, `plan.md`, `tasks.md` and `evidence.md`,
using the existing project-local overrides. Shared migration indexes are owned
by the migration coordinator and are not edited by this package.

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/opt.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Opt canonical remaining work](../../docs/component-roadmaps/opt.md#current-evidence-and-remaining-work) | opt/1 through opt/5; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/opt.md#acceptance-criteria-yesno) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-06-compiler-architecture/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: compiler optimization maintainer. Interface owners: [mir](../../docs/component-roadmaps/mir.md#release-outcome), [lower](../../docs/component-roadmaps/lower.md#release-outcome).
MIR verification and facts gate integrated optimization. Lower may consume unoptimized verified MIR. A pass-specific proof or benchmark is not blanket correctness evidence, and legal no-change decisions remain valid.

## Validation and evidence

Check ASCII, the exact four-file set, local Markdown target/anchor resolution,
semantic package names and absence of duplicated component checklists.
Negative acceptance cases are broken/missing links, treating artifact presence
as implementation completion, or narrowing retained contracts without disposition.
Run the existing structural documentation and full-language roadmap validators.
No compiler tests are necessary for these Markdown-only additions, and none
are claimed. Record commands/results and exact review identity in evidence.md.

## Delivery and remaining work

Publish partial artifact work in a draft PR. Keep package review and integration
unaccepted until their actual receipts exist. Deus owns final integration;
repository validation cannot confer semantic component or release acceptance.
Do not add a recurring acceptance loop solely because evidence text changes.

## Names, callers and compatibility

No source names, callers, wire/schema identities, load order or historical pins
change. Retain original task identities and phase anchors. Future source work
uses subsystem/behavior names under the semantic naming rule, separately reviewed.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [D6](../../docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md#requirements): Safe optimization must preserve or regenerate safety facts and expose complete performance claims. Throughput cannot justify proofless check elision.
- [L12](../../docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md#requirements): Partial evaluation preserves types, numeric modes, profile legality and generated provenance; pure evaluation cannot acquire undeclared host effects.
- [PERF1](../../docs/phase-04-performance-model/059-perf1-performance-model-specification.md#requirements): Optimization decisions retain profile/target/input/layout identity, proof state and guard artifacts; reject loss of semantic or safety facts.
- [PERF2](../../docs/phase-04-performance-model/060-perf2-zero-cost-abstractions-specification.md#requirements): Compare before/after IR against an equivalent lower-level form and report residual allocation, dispatch, checks and host calls honestly.
- [PERF3](../../docs/phase-04-performance-model/061-perf3-specialization-and-partial-evaluation-design.md#requirements): Specialized variants preserve generic semantics under complete guards, source maps and proof inputs.
- [PERF4](../../docs/phase-04-performance-model/062-perf4-memory-layout-optimization-design.md#requirements): Layout transformations need alias, ownership and address-identity evidence and cannot silently change fixed ABI/persistence boundaries.
- [PERF6](../../docs/phase-04-performance-model/064-perf6-profile-guided-optimization-design.md#requirements): PGO consumers validate source/typed-core/MIR/compiler/workload identity, staleness policy and preserved facts before using measurements.
- [PERF7](../../docs/phase-04-performance-model/065-perf7-autotuning-and-multiversioning-design.md#requirements): Reject invalid candidates before benchmarking; emit reproducible selection evidence, complete guards and a valid baseline fallback.
- [PERF8](../../docs/phase-04-performance-model/066-perf8-simd-vectorization-and-cache-optimization-strategy.md#requirements): Vector/cache preparation preserves lane independence, alias/bounds proofs, effect order, numeric modes and volatile/MMIO constraints.
- [PERF10](../../docs/phase-04-performance-model/068-perf10-performance-safety-check-elision-rules.md#requirements): Elide checks only under a named dominating proof valid after intervening transforms; record proof/pass/profile/target and invalidation. Regenerate invalidated proof, retain residual checks or reject no-checks compilation; never remove policy checks merely for speed.
