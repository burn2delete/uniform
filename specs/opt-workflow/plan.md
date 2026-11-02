# Optimization implementation planning entry

Select one unresolved [existing task](tasks.md#implementation-tasks), preserving
the historical transfer plan and its reviewed interface appendix below. Read
[spec.md](spec.md), C13 and each applicable performance/safety/math rule.
Locate current semantic implementations and temporary transports through
[development verification](../../docs/development-verification.md) and the
[stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

Name the actual verified input, selected pass and analyses, proof assumptions,
preserved/invalidated/regenerated facts and real verifier/Lower consumers.
Supply an actual changed and unchanged positive, plus semantic, stale-fact,
ordering/check/anchor or target-assumption negatives relevant to that pass.
Retain exact before/after artifacts and actual verification. Benchmark or PGO
work must first satisfy correctness and the applicable identity/privacy policy.

Use focused pass/verifier tests and affected MIR/Lower or domain composition;
do not execute held domain/provider paths. Refine only the selected existing
task, recording exact evidence, residual checks/work, unavailable target facts
and unrun cases. A fixture/helper cannot confer whole-pass, performance,
independent acceptance or release authority.

## Historical task-authority-transfer plan

# Implementation plan: Opt task authority

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler optimization maintainer.
Input revision: `963f70c41b1da261da3a3acfdfe055f23cee6971`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts).
C13 and the reviewed additional interfaces below remain normative inputs;
workflow artifacts do not replace semantic authority.

Retain the original canonical navigation:

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/opt.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Opt canonical remaining work](../../docs/component-roadmaps/opt.md#current-evidence-and-remaining-work) | opt/1 through opt/5; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/opt.md#acceptance-criteria-yesno) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-06-compiler-architecture/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: compiler optimization maintainer. Interface owners: [mir](../../docs/component-roadmaps/mir.md#release-outcome), [lower](../../docs/component-roadmaps/lower.md#release-outcome).
MIR verification and facts gate integrated optimization. Lower may consume unoptimized verified MIR. A pass-specific proof or benchmark is not blanket correctness evidence, and legal no-change decisions remain valid.

## Approach and exact files

Move only the five real rows to the marked
[implementation region](tasks.md#implementation-tasks), preserving semantic IDs,
display labels, exact evidence/status and exit criteria. Keep the complete
original migration tasks file as a frozen suffix with unchanged checkboxes.

Own the roadmap and four workflow artifacts. Preserve the entire reviewed Additional contract interfaces section
as an exact suffix, taken from plan blob
`797a3147538954d2b4b61fe60777f45fcd1707b6`. No mapping artifact or other plan is
edited. Preserve the roadmap's accounting, admission, compatibility and
acceptance text while linking directly to the ledger.

## Validation and evidence

Run from repository root:

```bash
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Compare the preserved task criteria, five derived IDs, complete historical tasks
suffix, prior spec/plan navigation and the full reviewed appendix. Check the
Markdown documents for
ASCII and file/heading links, then run whitespace and clean exact-base/head/tree
integration preflight. No compiler suite is required.

## Delivery and remaining work

Independent review inspects the roadmap, four workflow artifacts, retained appendix and PR
body. Publish a validated draft with exact identities and compact actual
validation receipts for maintainer current-head review. Integration follows that
disposition and repository gates. All five outcomes remain Open; artifact
presence, assertion counts and generated metadata confer no compiler acceptance.

## Names, callers and compatibility

API, wire/schema identity, diagnostic, manifest hash, source name or admission
policy changes. Historical planning IDs remain traceability references only.

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
