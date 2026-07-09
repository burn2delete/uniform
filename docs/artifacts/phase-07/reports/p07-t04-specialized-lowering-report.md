# P07-T04 Specialized Backend Lowering Proof Report

Date: 2026-06-29
Task: `P07-T04`
Status: complete (stage0 specialized GPU/HDL/workflow/query/mobile lowering capability)

## Governing Documents Read

- `docs/phase-07-backend-architecture/105-b8-gpu-backend-design.md`
- `docs/phase-07-backend-architecture/106-b9-hdl-backend-design.md`
- `docs/phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md`
- `docs/phase-07-backend-architecture/108-b11-query-relational-backend-design.md`
- `docs/phase-07-backend-architecture/109-b12-mobile-backend-design.md`
- `docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md`
- `docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md`
- `docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b8-*.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b9-*.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b10-*.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b11-*.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b12-*.gravity`
- `docs/artifacts/phase-07/backend/stage0-p07-t04-specialized-lowering-proof.edn`

The `specialized-lowering` command emits
`:gravity/stage0-specialized-lowering-artifact` from the current P07-T01
backend interface artifact. It records target-lowering manifests for GPU, HDL,
workflow graph, query/relational, and mobile backends; GPU host-device boundary,
launch, memory, transfer, synchronization, and math-certificate records; HDL
interface, clock/reset, state-machine, timing, and testbench records; workflow
graph, schema, event-log, replay, idempotency, retry/timeout/compensation,
capability, human-review, and audit records; query SQL, prepared binding, plan,
typed result, transaction/isolation, migration, capability, and taint records;
mobile platform, bundle, binding, permission, lifecycle/threading, UI bridge,
storage/sync, store-audit, and simulator-conformance records; B13 artifact
manifests; an artifact graph; metadata preservation; backend conformance;
diagnostics; and capability-based proof.

## Validation

```text
clojure -M:gravity specialized-lowering bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-specialized-lowering-artifact,
 :task "P07-T04",
 :targets [:gravity.backend/gpu
           :gravity.backend/hdl
           :gravity.backend/workflow-graph
           :gravity.backend/query-relational
           :gravity.backend/mobile],
 :artifact-manifests 5,
 :diagnostics 51,
 :negative-results 51,
 :proof :complete}
```

Artifact hash:

```text
sha256:2ede3115d58c06d1c2048ce87157dc5b7c9dad704c12389c1687f6437c5dbc0a
```

```text
clojure -M:test
Ran 74 tests containing 4170 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e <phase-07 backend EDN parse>
parsed 4 phase-07 backend EDN proof files
```

```text
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_gravity_docs.py
validation passed: 240 docs, 18 phase indexes, ASCII, no placeholders
```

```text
git diff --check
passed with no output
```

## Rejected Diagnostics

The rejected fixture suite covers all current P07-T04 specialized lowering
diagnostic IDs:

- `B8-TARGET`, `B8-KERNEL`, `B8-HOST-EFFECT`, `B8-MEMORY`, `B8-TRANSFER`, `B8-SYNC`, `B8-ATOMIC`, `B8-LAUNCH`, `B8-MATH`, `B8-MANIFEST`
- `B9-TARGET`, `B9-WIDTH`, `B9-CLOCK`, `B9-RESET`, `B9-CDC`, `B9-RUNTIME`, `B9-UNBOUNDED`, `B9-INTERFACE`, `B9-TIMING`, `B9-MANIFEST`
- `B10-SCHEMA`, `B10-REPLAY`, `B10-IDEMPOTENCY`, `B10-RETRY`, `B10-COMPENSATION`, `B10-CAPABILITY`, `B10-POLICY`, `B10-TAINT`, `B10-GRAPH`, `B10-MANIFEST`
- `B11-DIALECT`, `B11-SCHEMA`, `B11-TAINT`, `B11-PARAMETER`, `B11-CAPABILITY`, `B11-TRANSACTION`, `B11-NULL`, `B11-MIGRATION`, `B11-RESULT`, `B11-PLAN`, `B11-MANIFEST`
- `B12-TARGET`, `B12-PERMISSION`, `B12-LIFECYCLE`, `B12-THREAD`, `B12-NULL`, `B12-ERROR`, `B12-BACKGROUND`, `B12-STORAGE`, `B12-RESOURCE`, `B12-MANIFEST`

## Proof Records

- `docs/artifacts/phase-07/backend/stage0-p07-t04-specialized-lowering-proof.edn`

## Remaining Limits

This completes `P07-T04` for deterministic Clojure stage0 emission of
specialized GPU, HDL, workflow graph, query/relational, and mobile target
artifacts plus semantic-anchor, schema, capability, artifact-manifest, and
conformance records. It does not claim external GPU driver/toolchain execution,
HDL synthesis or simulation, workflow runtime execution, database execution,
mobile simulator execution, production specialized backend stabilization,
release readiness, or full backend conformance coverage.
