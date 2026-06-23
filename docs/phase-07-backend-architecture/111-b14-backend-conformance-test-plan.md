# B14 - Backend Conformance Test Plan

Sequence: 111
Phase: 7 - Backend Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The backend conformance test plan verifies that every Phase 7 backend preserves
Gravity semantics, profile constraints, safety evidence, effects, capabilities,
debug/source metadata, artifact manifests, and expected rejection behavior.

Execution equivalence alone is insufficient. A backend that computes the same
answer but drops proof links, capability records, unsafe audit ids, source maps,
or profile diagnostics is non-conforming.

## Requirements

- The suite must cover the shared `B1` backend interface and each concrete
  backend from `B2` through `B13`.
- Tests must include positive lowering, negative lowering, differential
  execution or semantic comparison, artifact manifest validation, metadata
  preservation, and diagnostic-rule validation.
- Canonical MIR fixtures must cover values, calls, closures, records, structs,
  tagged unions, pattern matching, loops, allocation, regions, linear resources,
  errors, FFI, atomics, workflows, AI/tool calls, domain anchors, and runtime
  checks.
- Domain fixtures must cover EFIR/math, GPU kernels, hardware circuits,
  workflow graphs, relational queries, UI/mobile boundaries, and schemas.
- Negative fixtures must assert exact diagnostic ids and missing evidence.
- Tests involving nondeterminism must record or replay the nondeterminism.
- Target-specific tests may be skipped only by an explicit target availability
  record; shared manifest and rejection tests still run.
- Conformance packs must be attachable to artifact emission and release review.

## Dependencies

- `B1` through `B13` define backend obligations and artifact shapes.
- `C11`, `C12`, `C14`, and `C18` define MIR/domain input, lowering, and
  compiler verification.
- Profile, safety, performance, math, runtime, package, schema, workflow, AI,
  and tooling documents define fixture requirements consumed by backend tests.

## Outputs and Artifacts

- Backend conformance suite manifest.
- Fixture matrix.
- Target availability matrix.
- Positive lowering results.
- Negative diagnostic results.
- Differential execution or semantic comparison results.
- Metadata preservation report.
- Artifact manifest validation report.
- Nondeterminism and replay record.
- Backend risk and coverage report.
- Conformance evidence pack.

## Suite Manifest

```clojure
{:artifact :gravity/backend-conformance-suite
 :targets #{:c :llvm :wasm :jvm :js-ts :mlir :gpu :hdl
            :workflow-graph :query-relational :mobile}
 :checks #{:lowering :expected-rejection :differential-execution
           :metadata-preservation :artifact-manifest :diagnostic-id}
 :inputs #{:canonical-mir :domain-ir :profile-negative :safety-negative}
 :rejects #{:execution-only-claim :missing-negative-fixtures
            :metadata-loss :unreplayable-nondeterminism}}
```

The suite manifest records which backends ran, which were unavailable, which
fixtures were skipped, and why.

## Fixture Matrix

The canonical matrix includes:

- pure values and arithmetic,
- numeric modes and math certificates,
- records, structs, tuples, enums, and tagged unions,
- closures and calls,
- pattern matching and control flow,
- loops and runtime checks,
- allocation, regions, arenas, and linear resources,
- ownership and aliasing proofs,
- FFI and host interop,
- atomics, volatile access, synchronization, and concurrency,
- source maps through macro expansion and generated code,
- domain anchors for EFIR, GPU, HDL, workflow, query, and mobile/UI,
- artifact manifests and provenance graphs.

Each fixture names expected artifacts, source maps, proof references,
capability/effect summaries, and diagnostics.

## Backend-Specific Coverage

Backend-specific coverage includes:

- C: dialect, UB rejection, pointer provenance, ABI/layout, helper runtime.
- LLVM: target/data layout, proof-gated metadata, pass pipeline, verifier.
- Wasm: imports/exports, linear memory, component bindings, replay records.
- JVM: nullability, exception translation, reflection policy, classfiles.
- JS/TS: host globals, nullish flow, async effects, numeric representation.
- MLIR: dialect verification, conversion legality, pass metadata preservation.
- GPU: host/device boundary, transfers, synchronization, launch, math.
- HDL: widths, clocks, resets, CDC, timing, interfaces.
- Workflow graph: schemas, replay, idempotency, retry, compensation, policy.
- Query/relational: parameterization, taint, transactions, migrations, results.
- Mobile: permissions, lifecycle, threading, storage, platform bindings.
- Artifact emission: manifest schema, hashes, provenance, reproducibility.

No backend is stabilized until its shared and backend-specific fixture families
pass or have accepted exclusions recorded.

## Negative and Diagnostic Tests

Negative tests must cover:

- missing proof for safety-sensitive metadata,
- profile-ineligible backend selection,
- hidden runtime provider dependencies,
- missing capabilities and effects,
- unsafe pointer or host boundary behavior,
- numeric mode violations,
- unrecorded nondeterminism,
- schema-less boundaries,
- source/provenance metadata loss,
- incomplete artifact manifests.

Each negative fixture asserts a diagnostic id, source span or manifest entry,
active profile, target/backend, missing fact, and remediation text category.

## Differential and Replay Tests

Differential tests compare backend execution or semantic artifacts against MIR,
domain reference interpreters, simulator traces, database reference fixtures, or
workflow replay logs. When a target cannot execute locally, the suite must still
validate artifact shape, verifier output, source maps, metadata, and expected
diagnostics.

Replay tests record clocks, randomness, network, database, model, tool, and
`:ai/human-review` events. Unrecorded nondeterminism invalidates the fixture
rather than becoming flaky conformance evidence.

## Metadata and Artifact Validation

Metadata validation checks:

- source spans and generated-origin chains,
- type/effect/capability summaries,
- safety outcomes and proof references,
- unsafe audit ids,
- profile and target manifests,
- runtime/provider dependencies,
- ABI/layout records,
- conformance pack identity,
- content hashes and artifact graph edges.

Validation runs on both successful artifacts and rejected builds when a partial
artifact is emitted for diagnostics.

## Diagnostics

Backend conformance diagnostics use `B14` identifiers:

- `B14-COVERAGE` for missing required fixture families.
- `B14-TARGET` for unavailable target without an availability record.
- `B14-POSITIVE` for valid fixtures that fail lowering or execution.
- `B14-NEGATIVE` for rejected fixtures that compile or produce the wrong
  diagnostic.
- `B14-DIFFERENTIAL` for mismatched execution, simulation, query, or replay
  results.
- `B14-METADATA` for source, proof, safety, effect, capability, or audit
  metadata loss.
- `B14-ARTIFACT` for invalid backend or common artifact manifests.
- `B14-NONDETERMINISM` for unrecorded nondeterminism in tests.
- `B14-SKIP` for unsupported skips or exclusions.
- `B14-EVIDENCE` for incomplete conformance evidence packs.

Diagnostics must include backend, profile, target, fixture id, expected and
actual diagnostic when relevant, missing metadata, artifact id, and remediation.

## Rejected Designs

Gravity rejects execution-only backend conformance.

Gravity rejects conformance claims without negative profile and safety fixtures.

Gravity rejects test flakiness caused by unrecorded nondeterminism.

Gravity rejects target skips without an availability and coverage record.

Gravity rejects release evidence packs that omit metadata preservation and
artifact-manifest validation.

## Conformance Criteria

A conforming backend conformance plan must demonstrate:

- fixture matrices for all Phase 7 backends,
- shared MIR/domain fixture coverage,
- backend-specific positive and negative tests,
- exact diagnostic-id assertions,
- metadata preservation checks,
- artifact manifest validation,
- deterministic replay or recorded nondeterminism,
- target availability and skip records,
- conformance evidence packs consumed by artifact emission and release review.
