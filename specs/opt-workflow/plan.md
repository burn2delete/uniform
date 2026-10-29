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

`tools/opt_task_projection.clj` reuses the shared pure parser unchanged. A local
field-validation callback masks only the spaced ASCII arrow in a validation copy
before applying the shared strict link/Markdown checks. Stored and projected
fields remain original bytes, including Opt -> Lower. Bare angles, malformed
arrows, HTML, images, HTTP/userinfo and malformed links reject. Other components
retain their policies. No link helper or shared parser modification is needed.

The wrapper accepts only --check or --write and validates all records before
writing only its fixed roadmap table. There is no caller-selected path, registry
or discovery. Own seven files: roadmap, four workflow artifacts and focused
tool/test. Preserve the entire reviewed Additional contract interfaces section
as an exact suffix, taken from plan blob
`797a3147538954d2b4b61fe60777f45fcd1707b6`. No mapping artifact or other plan is
edited. Removing only added navigation/markers restores the whole original
roadmap, including all accounting, admission, compatibility and acceptance text.

## Validation and evidence

Run from repository root:

```bash
clojure -J-Xmx512m -M tools/opt_task_projection.clj --write
clojure -J-Xmx512m -M tools/opt_task_projection.clj --check
clojure -J-Xmx512m -M tools/opt_task_projection_test.clj
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Compare exact original/current table/cell bytes and five derived IDs, complete
historical tasks suffix, original roadmap reconstruction, prior spec/plan
navigation and the full reviewed appendix. Check five Markdown documents for
ASCII and file/heading links, including missing-file/missing-heading rejection.
Exercise malformed/missing/duplicate IDs and markers, title mismatch, explicit
acceptance consistency and stale projections. Prove original arrow preservation,
Opt-only allowance, unchanged shared rejection, and malformed-arrow/HTML/link
negative controls. Isolated subprocess fixtures verify read-only checks, fixed
writes, rejected extra paths and non-dispatching helper loads. Run whitespace and
clean exact-base/head/tree integration preflight. No compiler suite is required.

## Delivery and remaining work

Independent review inspects all seven public files, retained appendix and PR
body. Publish a validated draft with exact identities and compact actual
validation receipts for maintainer current-head review. Integration follows that
disposition and repository gates. All five outcomes remain Open; artifact
presence, assertion counts and generated metadata confer no compiler acceptance.

## Names, callers and compatibility

`opt-task-projection` follows existing semantic entrypoints. No compiler/runtime
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

## Performance acceptance interfaces

The following source-qualified cases refine the existing Open Opt outcomes in
[the canonical task source](tasks.md#implementation-tasks). They introduce no
task IDs or implementation acceptance. The bounded
[phase-04 proposal](../requirement-reconciliation/performance-family-proposal.edn)
preserves exact source occurrences, facets, prior decisions and unresolved
interfaces. Independent source review accepted these qualified criteria in
receipt `114b87550e9ac475bda1d7d957602485e947954d5ae196af55c0a38716ac6041`.
Implementation remains unassessed; the broad task text is not proof of full PERF
coverage.

- `opt/current-evidence-and-remaining-work/opt1-execute-pass-contracts` consumes
  PERF6 source, typed-core, MIR, compiler, profile, target, provider and workload
  identities. Exercise accepted, advisory, stale, rejected and required-missing
  profile-data states; reject required mismatches and silent use of stale data
  in release optimization. PERF3 reuse tests change each semantic key input,
  including dependency, provider, build-effect/replay and proof inputs, and
  require invalidation. This depends on actual typed/MIR identity producers.
- `opt/current-evidence-and-remaining-work/opt2-implement-structural-families`
  consumes PERF7 candidate spaces and type/effect/profile/capability/safety/math
  evidence before measurement. Invalid candidates never reach benchmarking.
  Exercise complete deterministic guards, overlap rejection and valid fallback
  when no guard matches. Retain generic-to-specialized source maps and
  compatibility metadata. This depends on the relevant proof checkers, target
  feature records and a benchmark producer; a fabricated measurement cannot
  close it. PERF8 fixtures retain lane/tail/mask, alias/bounds/alignment and
  numeric conditions, including strict-mode reassociation rejection and ordered
  volatile/MMIO/atomic/synchronization behavior.
- `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` and
  `opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate` consume
  PERF10 dominating proof facts and invalidation conditions at each transformed
  operation. Exercise each check class in its elision table, partial proof with
  narrower residual checks, missing/non-dominating/invalidated proof rejection,
  and backend width/alignment/order/exception/numeric assumption changes.
  Policy checks, including AI human review, require equivalent policy artifacts;
  speed alone never discharges them. This depends on Legality proof producers
  and Lower assumption-preservation results, not metadata asserting verification.
- `opt/current-evidence-and-remaining-work/opt5-integrate-reproducible-pipeline`
  exposes PERF2 before/after IR, equivalent handwritten form and actual erased
  and residual operations. Include allocation, boxing, dispatch, reflection,
  synchronization, checks, provider/FFI/host calls, closure/iterator state and
  guards; acceptable residual work is still reported. False zero-cost claims
  reject. PERF6 decisions retain hot/cold maps, pass/input/benefit/fact/proof
  records and reproduce from pinned data/compiler inputs. PERF7 selection
  records retain objective, constraints, rejected alternatives, source/MIR,
  compiler/backend/provider, target, benchmark and random-seed identities;
  runtime guard overhead is measured or modeled unless statically erased.

The benchmark producer and reviewed baseline-update authority in PERF5 remain
unlocated as exact canonical task interfaces. Profiler sample capture alone does
not entail benchmark statistics, correctness/safety gates or baseline history.
PERF9's physical latency/jitter contracts, required worst-case or fingerprinted
empirical evidence, bounded checks/providers and rejection of hidden services or
unbounded latency also remain explicit missing interfaces. Those physical
constraints are separate from removed project work accounting. PGO production
trace privacy/access/retention enforcement and the general non-macro compile-time
evaluator likewise remain unresolved beyond the scoped consuming tasks.
