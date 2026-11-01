# Backend Projects

Working portfolio name: **Backends** (proposed); semantic key `backends`.
Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`. Every project below is
proposed and its release acceptance is **open**. Current individual owners are
**unassigned by this proposal**; named roles describe accountability, not an
ownership transfer. Each section is its canonical finite roadmap and outcome.

## Purpose and boundaries

[Backend contracts](../phase-07-backend-architecture/README.md) define target
adapters, not new source semantics. Verified MIR or a declared verified domain
IR, profile, target, ABI, runtime/provider, safety, proof, effect, capability and
source manifests are the common inputs. Actual target artifacts and preservation
records are outputs. No backend can legalize a rejected program.

The selected staged **Linux LLVM** release candidate remains unchanged in
[Lower](lower.md#release-outcome) and [Emit](emit.md#release-outcome). Wider
backend families are independent outcomes, not new prerequisites for compiler
bootstrap or that candidate. A family's completed development fixture does not
close its whole contract. Broader family support must name its exact supported
matrix and expose unsupported combinations as rejections, not silently reduce
the normative obligation.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## Release outcome

Each named project closes only on its own artifact, positive/negative fixture
and metadata-preservation evidence. The portfolio is complete only when the
canonical family outcomes and shared contracts below are accepted; it is not a
second task list for the selected Linux LLVM candidate.

Implementation task authority: [Spec Kit family ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## Backend Interface and ABI

Key `backend-interface`; owner role: backend interface/ABI maintainer.
Contract: [common interface](../phase-07-backend-architecture/098-b1-backend-interface-specification.md).
Outcome: every backend consumes the same verified eligibility envelope and
publishes explicit ABI/layout/runtime decisions. Inputs: verified IR and the
common manifests above. Outputs: eligibility report, calling/layout policy,
provider dependencies and unsupported-feature diagnostics. Not a universal ABI:
each target keeps its real calling, data, error and ownership representations.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## C

Key `backend-c`; owner role: C backend maintainer. Contract:
[C backend](../phase-07-backend-architecture/099-b2-c-backend-design.md).
Outcome: verified IR becomes disciplined C source/headers and a reproducible
build manifest without importing C undefined behavior. Inputs: verified IR,
declared dialect, pointer/lifetime facts and ABI/helper policy. Outputs: C
translation units, headers, helpers, source/proof maps and compiled conformance
artifacts. Non-goal: replacing Gravity semantics with C rules.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## LLVM

Key `backend-llvm`; owner role: LLVM backend maintainer. Contract:
[LLVM backend](../phase-07-backend-architecture/100-b3-llvm-backend-design.md).
Outcome: declared LLVM targets emit valid IR/bitcode/objects/libraries with
proof-backed attributes, real data layouts and preserved safety/debug facts.
Inputs: verified IR, triple/CPU/features, ABI, numeric mode, unwind/relocation
policy and proof records. Outputs: LLVM artifacts, pipeline and metadata map,
runtime helper map and native conformance evidence.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## Wasm

Key `backend-wasm`; owner role: Wasm/component ABI maintainer. Contract:
[Wasm backend](../phase-07-backend-architecture/101-b4-wasm-backend-design.md).
Outcome: core modules/components and declared host bindings execute with explicit
imports, canonical ABI ownership and feature/replay constraints. Inputs: verified
IR, wasm32/wasm64 policy, feature set, import/export schemas, component world and
runtime grants. Outputs: modules/components, interface bindings, memory/resource
maps and sandbox manifests. Non-goal: ambient host access through an import.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## JVM

Key `backend-jvm`; owner role: JVM backend maintainer. Contract:
[JVM backend](../phase-07-backend-architecture/102-b5-jvm-backend-design.md).
Outcome: class/JAR/module artifacts preserve Gravity null/error/resource/authority
semantics on a declared JVM range. Inputs: verified IR, classfile/JVM policy,
interop descriptors and managed services. Outputs: classes, JAR/module descriptors,
null/exception maps, reflection manifest and debug/provenance records.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## JavaScript and TypeScript

Key `backend-js`; owner role: JS/TS backend maintainer. Contract:
[JS/TS backend](../phase-07-backend-architecture/103-b6-javascript-typescript-backend-design.md).
Outcome: executable JS modules and accurate TS declarations preserve numeric,
async/error, host-value and capability semantics. Inputs: verified IR, runtime/
ECMAScript/module policy, export schemas and host grants. Outputs: modules,
declarations, source maps, package manifest and host/async boundary records.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## MLIR

Key `backend-mlir`; owner role: MLIR dialect/conversion maintainer. Contract:
[MLIR backend](../phase-07-backend-architecture/104-b7-mlir-backend-design.md).
Outcome: verified dialect modules and conversion pipelines preserve semantic
facts through a declared downstream handoff. Inputs: verified MIR/domain IR,
dialect registry, conversion legality and proof maps. Outputs: MLIR modules,
verifier reports, pass logs and LLVM/SPIR-V/other declared handoff artifacts.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## GPU Backend

Key `backend-gpu`; owner role: GPU target/emission maintainer. Contract:
[GPU backend](../phase-07-backend-architecture/105-b8-gpu-backend-design.md).
Outcome: verified GPU IR yields actual device modules and host dispatch artifacts
with checked transfers, lifetimes, barriers and numeric modes. Inputs: GPU IR,
address-space/layout facts, target features, launch and provider policy. Outputs:
device binary/intermediate, host stub, launch/transfer/synchronization graphs.
The [GPU domain project](domains.md#gpu) owns semantic IR; this project owns
target adaptation, not duplicate domain checking tasks.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## HDL

Key `backend-hdl`; owner role: hardware emitter maintainer. Contract:
[HDL backend](../phase-07-backend-architecture/106-b9-hdl-backend-design.md).
Outcome: verified hardware IR becomes synthesizable HDL and source-linked
clock/reset/timing/simulation evidence. Inputs: fixed-width hardware IR,
clock/reset domains, interfaces, state transitions and target constraints.
Outputs: HDL, port schemas, timing constraints, testbench and cycle traces.
Not the whole hardware/OS product in [Platform](platform.md).

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## Workflow Backend

Key `backend-workflow`; owner role: durable graph emitter maintainer. Contract:
[Workflow backend](../phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md).
Outcome: verified workflow IR emits an executable durable graph with versioned
step/event schemas, replay policy and explicit retry/compensation/review edges.
Inputs: workflow IR, schemas, provider/grant policy and migration semantics.
Outputs: graph, event contracts, idempotency/retry/cancellation maps and audit
metadata. Workflow domain owns semantics; Runtime owns durable execution.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## AI Artifacts

Key `backend-ai`; owner role: AI artifact/adapter maintainer. Outcome: AI domain
artifacts become executable provider-bound agent/tool/model/review packages with
schema, taint, evaluation and provenance records. Inputs: verified AI IR,
prompts/schemas, model/tool/provider identities, grants and review policy.
Outputs: agent/prompt/tool/provider manifests, workflow handoff and evaluation
bindings. This is a distinct artifact adapter project under
[AI contracts](../phase-11-ai-and-agentic-programming/README.md) and the
[workflow backend contract](../phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md),
not an invented additional numbered normative backend document.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## Query and Relational

Key `backend-query`; owner role: relational emitter maintainer. Contract:
[Query backend](../phase-07-backend-architecture/108-b11-query-relational-backend-design.md).
Outcome: verified relational/schema IR yields executable parameterized queries,
checked row adapters and migration/transaction records. Inputs: relational IR,
schema, database dialect/version, isolation and grants. Outputs: SQL/prepared
bindings, plans, result adapters and schema-diff/migration artifacts.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## Mobile

Key `backend-mobile`; owner role: mobile backend maintainer. Contract:
[Mobile backend](../phase-07-backend-architecture/109-b12-mobile-backend-design.md).
Outcome: actual declared iOS/Android application bundles preserve permission,
lifecycle, threading, error/resource and UI semantics on simulator/device.
Inputs: verified MIR/UI/platform IR, OS/architecture/bundle policy, runtime/UI
bridge and capability records. Outputs: bundles, platform bindings, permission/
resource manifests, lifecycle maps and device conformance evidence.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## Artifact Emission Contract

Key `backend-emission`; owner role: artifact infrastructure maintainer. Contract:
[artifact emission](../phase-07-backend-architecture/110-b13-artifact-emission-specification.md).
Outcome: every family emits content-addressable artifacts with mutually
consistent provenance, source/debug, ABI/runtime, effect/capability, safety and
conformance records. Inputs: actual family artifacts and producing manifests.
Outputs: versioned artifact graph, content identities and reproducibility records.
This shared extension outcome does not duplicate selected-candidate
[Emit](emit.md#release-outcome) tasks.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).

## Backend Conformance

Key `backend-conformance`; owner role: independent backend conformance maintainer.
Contract: [conformance plan](../phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md).
Outcome: each claimed backend has executable positive, exact negative diagnostic,
semantic/differential, metadata and artifact-validation evidence tied to its
actual target/compiler identity. Inputs: canonical IR/domain fixtures, backend
outputs, expected diagnostics and target availability. Outputs: fixture/target
matrix, execution and preservation reports, replay records and evidence pack.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).
