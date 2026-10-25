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

All remaining tasks below are open. Source links identify inspected baseline
starting points, not fresh execution evidence. No compiler or backend tests were
run for this roadmap. Numeric constraints needed for ABI representability,
hardware widths or target semantics are not arbitrary project/input quotas.
Existing protocol/schema/diagnostic IDs remain compatibility data; new
implementation names use semantic backend names.

## Release outcome

Each named project closes only on its own artifact, positive/negative fixture
and metadata-preservation evidence. The portfolio is complete only when the
canonical family outcomes and shared contracts below are accepted; it is not a
second task list for the selected Linux LLVM candidate.

## Backend Interface and ABI

Key `backend-interface`; owner role: backend interface/ABI maintainer.
Contract: [common interface](../phase-07-backend-architecture/098-b1-backend-interface-specification.md).
Outcome: every backend consumes the same verified eligibility envelope and
publishes explicit ABI/layout/runtime decisions. Inputs: verified IR and the
common manifests above. Outputs: eligibility report, calling/layout policy,
provider dependencies and unsupported-feature diagnostics. Not a universal ABI:
each target keeps its real calling, data, error and ownership representations.

Remaining tasks: implement required-input/version validation; define per-target
ABI/layout mappings and reject unrepresentable values; connect runtime/provider
selection and proof-to-target assumptions; exercise accepted and missing-proof,
wrong-profile, unsupported-operation and incompatible-ABI handoffs. Yes/no:
are unchecked inputs rejected, assumptions justified and ABI decisions explicit?
All must be yes. Evidence: [existing interface artifact](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00225-b1-document-source-artifact.clj).
Decision owner: interface maintainer; recommendation: versioned shared envelope
with target-specific ABI records, not one host-shaped layout. Integration needs
MIR/Lower facts; envelope fixtures and individual backends proceed in parallel.

## C

Key `backend-c`; owner role: C backend maintainer. Contract:
[C backend](../phase-07-backend-architecture/099-b2-c-backend-design.md).
Outcome: verified IR becomes disciplined C source/headers and a reproducible
build manifest without importing C undefined behavior. Inputs: verified IR,
declared dialect, pointer/lifetime facts and ABI/helper policy. Outputs: C
translation units, headers, helpers, source/proof maps and compiled conformance
artifacts. Non-goal: replacing Gravity semantics with C rules.

Remaining tasks: lock dialect/compiler/ABI matrix; lower values/control/errors/
resources with explicit overflow/shift/narrowing behavior; generate helpers and
freestanding/hosted dependency records; compile/run positive fixtures and reject
missing provenance, hidden libc/allocator and unsafe assumptions. Yes/no: do
results and diagnostics agree with Gravity, is safe code free of target UB, and
are helper dependencies explicit? Evidence: [C manifest assembly](../../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_late/140134-c-backend-manifest.clj)
records source, dialect, plan, execution and provenance inputs, not full C
conformance. Decision owner: C maintainer; recommendation: preserve the contract's
explicit freestanding C11 versus hosted C17 distinction. Integrates Lower,
Runtime and Emit; C expansion does not gate the selected LLVM candidate.

## LLVM

Key `backend-llvm`; owner role: LLVM backend maintainer. Contract:
[LLVM backend](../phase-07-backend-architecture/100-b3-llvm-backend-design.md).
Outcome: declared LLVM targets emit valid IR/bitcode/objects/libraries with
proof-backed attributes, real data layouts and preserved safety/debug facts.
Inputs: verified IR, triple/CPU/features, ABI, numeric mode, unwind/relocation
policy and proof records. Outputs: LLVM artifacts, pipeline and metadata map,
runtime helper map and native conformance evidence.

Remaining tasks: reconcile the selected Linux candidate with the full target
contract without changing its selection; implement missing operation/layout/
atomic/error/FFI mappings; admit attributes only from matching proofs; run LLVM
verification, real native execution and missing-proof/poison/target-mismatch
rejection; attach reproducibility and debug/provenance evidence. Yes/no: are
attributes justified, undefined/poison shortcuts absent, target facts pinned
and emitted artifacts executable? Evidence: [Linux artifact implementation](../../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_llvm/036-linux-artifact-record.clj).
Decision owner: LLVM maintainer; recommendation: retain the adopted Linux
candidate and treat additional triples as explicit family extensions, not
silent release-target substitution. Integrates Lower/Emit/Runtime. The selected
candidate is a stage of this broader outcome, not evidence of all LLVM targets.

## Wasm

Key `backend-wasm`; owner role: Wasm/component ABI maintainer. Contract:
[Wasm backend](../phase-07-backend-architecture/101-b4-wasm-backend-design.md).
Outcome: core modules/components and declared host bindings execute with explicit
imports, canonical ABI ownership and feature/replay constraints. Inputs: verified
IR, wasm32/wasm64 policy, feature set, import/export schemas, component world and
runtime grants. Outputs: modules/components, interface bindings, memory/resource
maps and sandbox manifests. Non-goal: ambient host access through an import.

Remaining tasks: record core/component/embedder target matrix; implement memory,
resource-handle and canonical ABI lowering; implement typed async stream/future
completion/cancellation/backpressure for claimed async targets; generate/import
bindings; validate and execute modules with bounds, missing-grant, unsupported-
feature, resource-lifetime and replay rejection fixtures. Yes/no: are exports
schema-correct, imports authorized, resource lifetimes enforced and selected
features actually supported? Evidence: [feature mapping](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00231-b4-document-feature-requirement.clj)
is document coverage, not a working component runtime. Decision owner: Wasm
maintainer; recommendation: explicit core versus component acceptance tracks and
pinned embedder versions; do not treat synchronous core support as async-component
completion. Parallel to LLVM; integrates Schema/Auth/Runtime and Emit.

## JVM

Key `backend-jvm`; owner role: JVM backend maintainer. Contract:
[JVM backend](../phase-07-backend-architecture/102-b5-jvm-backend-design.md).
Outcome: class/JAR/module artifacts preserve Gravity null/error/resource/authority
semantics on a declared JVM range. Inputs: verified IR, classfile/JVM policy,
interop descriptors and managed services. Outputs: classes, JAR/module descriptors,
null/exception maps, reflection manifest and debug/provenance records.

Remaining tasks: fix supported JVM/classloader/package matrix; complete bytecode
and interop lowering; enforce nullability, exception translation, deterministic
linear cleanup and dynamic-use grants; execute packaged programs and reject
ungranted reflection/native access, unchecked null and missing metadata. Yes/no:
do real class/JAR artifacts execute, do host boundaries preserve semantics, and
are cleanup/grants enforced? Evidence: [JVM manifest assembly](../../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_late/142709-jvm-backend-manifest.clj)
consumes class/JAR and execution fields; inspection is not execution proof.
Decision owner: JVM maintainer; recommendation: a pinned baseline classfile/JVM
pair plus explicit compatibility matrix. Integrates managed Runtime and Build;
this hosted path does not replace the selected native release.

## JavaScript and TypeScript

Key `backend-js`; owner role: JS/TS backend maintainer. Contract:
[JS/TS backend](../phase-07-backend-architecture/103-b6-javascript-typescript-backend-design.md).
Outcome: executable JS modules and accurate TS declarations preserve numeric,
async/error, host-value and capability semantics. Inputs: verified IR, runtime/
ECMAScript/module policy, export schemas and host grants. Outputs: modules,
declarations, source maps, package manifest and host/async boundary records.

Remaining tasks: declare Node/browser/edge adapter matrix; lower numeric and
control/resource semantics; generate declarations and checked null/undefined/
promise adapters; preserve source maps/package side-effect policy; execute
programs and reject unauthorized globals, lossy numeric conversions and unsafe
imports. Yes/no: do exports match declarations, numeric modes survive, host
values are validated and denied globals stay inaccessible? Evidence:
[JS manifest assembly](../../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_late/141626-js-ts-backend-manifest.clj)
explicitly records Node host globals; it does not establish every JS environment.
Decision owner: JS maintainer; recommendation: separate runtime adapters sharing
one semantic lowering layer. Parallel to JVM/LLVM; integrates Schema/Runtime/UI.

## MLIR

Key `backend-mlir`; owner role: MLIR dialect/conversion maintainer. Contract:
[MLIR backend](../phase-07-backend-architecture/104-b7-mlir-backend-design.md).
Outcome: verified dialect modules and conversion pipelines preserve semantic
facts through a declared downstream handoff. Inputs: verified MIR/domain IR,
dialect registry, conversion legality and proof maps. Outputs: MLIR modules,
verifier reports, pass logs and LLVM/SPIR-V/other declared handoff artifacts.

Remaining tasks: pin MLIR version/dialects and operation coverage; implement
types/effects/ownership/proof attributes and verifiers; implement conversions and
metadata-preservation checks; run actual verification/conversion and reject
illegal targets or fact loss. Yes/no: do modules verify, conversions preserve
facts and downstream consumers accept the result? Evidence:
[MLIR manifest](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00244-b7-document-mlir-manifest.clj)
uses `stage0-recorded` version and a narrow operation map; nested complete flags
are not tool execution. Decision owner: MLIR maintainer; recommendation: explicit
versioned dialect registry and legal conversion pipeline. Optional MLIR use
must not become a prerequisite for backends that do not require it.

## GPU Backend

Key `backend-gpu`; owner role: GPU target/emission maintainer. Contract:
[GPU backend](../phase-07-backend-architecture/105-b8-gpu-backend-design.md).
Outcome: verified GPU IR yields actual device modules and host dispatch artifacts
with checked transfers, lifetimes, barriers and numeric modes. Inputs: GPU IR,
address-space/layout facts, target features, launch and provider policy. Outputs:
device binary/intermediate, host stub, launch/transfer/synchronization graphs.
The [GPU domain project](domains.md#gpu) owns semantic IR; this project owns
target adaptation, not duplicate domain checking tasks.

Remaining tasks: adopt API/device/toolchain matrix; implement kernel emission
and argument ABI; generate explicit transfer/launch/cleanup stubs; execute real
or declared simulator kernels and reject host-only effects, invalid barriers,
implicit transfers and uncertified relaxed math. Yes/no: do kernels run, buffer
lifetimes hold, synchronization matches IR and numeric results meet mode?
Evidence: [GPU manifest](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00248-b8-document-gpu-manifest.clj)
records a SPIR-V-shaped fixture, not device execution. Decision owner: GPU
maintainer; recommendation: one explicit reference API/device pair first with
named additional target obligations. Integrates GPU domain/Runtime/Emit;
accelerator availability does not gate the ordinary compiler release.

## HDL

Key `backend-hdl`; owner role: hardware emitter maintainer. Contract:
[HDL backend](../phase-07-backend-architecture/106-b9-hdl-backend-design.md).
Outcome: verified hardware IR becomes synthesizable HDL and source-linked
clock/reset/timing/simulation evidence. Inputs: fixed-width hardware IR,
clock/reset domains, interfaces, state transitions and target constraints.
Outputs: HDL, port schemas, timing constraints, testbench and cycle traces.
Not the whole hardware/OS product in [Platform](platform.md).

Remaining tasks: adopt HDL/toolchain and synthesis target record; implement
state/port/memory lowering and explicit width behavior; preserve clock/reset
crossing proofs; generate testbenches and simulate/synthesize; reject hidden
runtime, illegal crossings and unrepresentable hardware behavior. Yes/no: does
HDL synthesize, do cycle traces match source, and are width/reset/crossing rules
enforced? Evidence: [HDL module fixture](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00251-b9-document-hdl-module.clj)
is a fixed string, not general lowering. Decision owner: hardware emitter
maintainer; recommendation: explicit SystemVerilog reference flow, subject to
architecture adoption and tool availability. Hardware IR is prerequisite for
integration; neither fabrication nor a completed OS is required for emitter work.

## Workflow Backend

Key `backend-workflow`; owner role: durable graph emitter maintainer. Contract:
[Workflow backend](../phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md).
Outcome: verified workflow IR emits an executable durable graph with versioned
step/event schemas, replay policy and explicit retry/compensation/review edges.
Inputs: workflow IR, schemas, provider/grant policy and migration semantics.
Outputs: graph, event contracts, idempotency/retry/cancellation maps and audit
metadata. Workflow domain owns semantics; Runtime owns durable execution.

Remaining tasks: implement graph/provider lowering; emit schemas and replay/
idempotency policy; encode retry/compensation/review transitions; run emitted
graphs through a declared runtime including restart/replay; reject unrecorded
nondeterminism, missing review and repeated external effects. Yes/no: can the
graph resume/replay correctly, are external effects guarded and origins retained?
Evidence: [workflow graph fixture](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00255-b10-document-workflow-graph.clj)
is fixed graph/event text, not general executable lowering. Decision owner:
graph maintainer; recommendation: provider-neutral graph plus a versioned runtime
adapter. Parallel to native backends; depends on Workflow/Schema/Auth interfaces.

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

Remaining tasks: implement AI artifact/provider binding; emit structured-output,
taint and human-review boundary records; connect workflow/runtime adapters;
execute accepted model/tool fixtures and reject injection, wrong provider,
missing review and unvalidated output; bind evaluations to emitted identities.
Yes/no: do artifacts execute only permitted actions, preserve schema/taint and
review, and retain exact model/prompt/tool provenance? Baseline evidence:
[workflow graph fixture](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00255-b10-document-workflow-graph.clj)
contains model/tool/review nodes but proves no live provider completion.
Decision owner: AI adapter maintainer; recommendation: provider-neutral artifacts
with explicit adapters, not a vendor SDK as semantics. Integrates AI domain,
Workflow/Schema/Auth/Runtime; no live model call is required for unrelated builds.

## Query and Relational

Key `backend-query`; owner role: relational emitter maintainer. Contract:
[Query backend](../phase-07-backend-architecture/108-b11-query-relational-backend-design.md).
Outcome: verified relational/schema IR yields executable parameterized queries,
checked row adapters and migration/transaction records. Inputs: relational IR,
schema, database dialect/version, isolation and grants. Outputs: SQL/prepared
bindings, plans, result adapters and schema-diff/migration artifacts.

Remaining tasks: adopt dialect/provider matrix; implement parameterized lowering
and null/collation/numeric mappings; generate row/transaction/migration adapters;
execute queries and reject tainted SQL, unauthorized writes, schema drift and
unapproved data loss. Yes/no: do rows match types, parameterization survive and
transactions/migrations respect policy? Evidence:
[query fixtures](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00259-b11-document-validate-source-overrides.clj)
contain one parameterized select and no-op migration, not general query support.
Decision owner: relational maintainer; recommendation: one explicit dialect
adapter with provider/version records, then separate conformance per extension.
Integrates Query domain/Schema/Runtime; no database service gates compiler bootstrap.

## Mobile

Key `backend-mobile`; owner role: mobile backend maintainer. Contract:
[Mobile backend](../phase-07-backend-architecture/109-b12-mobile-backend-design.md).
Outcome: actual declared iOS/Android application bundles preserve permission,
lifecycle, threading, error/resource and UI semantics on simulator/device.
Inputs: verified MIR/UI/platform IR, OS/architecture/bundle policy, runtime/UI
bridge and capability records. Outputs: bundles, platform bindings, permission/
resource manifests, lifecycle maps and device conformance evidence.

Remaining tasks: adopt platform/version/package matrix; implement typed platform
bindings and UI/lifecycle/threading adapters; emit resources/permissions and
package metadata; run simulator/device apps and reject absent permissions,
wrong-thread UI, invalid lifecycle use and hidden background work; attach source
maps/store-audit records. Yes/no: does the bundle launch, do lifecycle and denial
paths execute correctly, and are host values/errors normalized? Evidence:
[mobile fixture](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00263-b12-document-app-bundle.clj)
is an iOS-shaped string validated by substring checks, not an actual app bundle.
Decision owner: mobile maintainer; recommendation: independent iOS and Android
adapters and explicit simulator/device evidence, not one generic mobile claim.
Integrates UI/Runtime/Emit/Build; external signing/store publication remains a
separate authorized action and does not gate unrelated compiler components.

## Artifact Emission Contract

Key `backend-emission`; owner role: artifact infrastructure maintainer. Contract:
[artifact emission](../phase-07-backend-architecture/110-b13-artifact-emission-specification.md).
Outcome: every family emits content-addressable artifacts with mutually
consistent provenance, source/debug, ABI/runtime, effect/capability, safety and
conformance records. Inputs: actual family artifacts and producing manifests.
Outputs: versioned artifact graph, content identities and reproducibility records.
This shared extension outcome does not duplicate selected-candidate
[Emit](emit.md#release-outcome) tasks.

Remaining tasks: implement common record/schema checks across all artifact
kinds; bind hashes and provenance to actual bytes and dependencies; enforce
missing/stale/contradictory evidence rejection; attach per-family conformance
packs before package/release consumers. Yes/no: do records identify real bytes,
all required edges exist and tampering/staleness reject? Evidence:
[emission contract implementation](../../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_early/26010-artifact-emission-contract-and-backend-inputs.clj)
assembles required records; existence is not family execution evidence. Decision
owner: artifact maintainer; recommendation: one versioned envelope with
kind-specific validation. Independent family fixtures may develop in parallel;
Build/Release consume accepted records later.

## Backend Conformance

Key `backend-conformance`; owner role: independent backend conformance maintainer.
Contract: [conformance plan](../phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md).
Outcome: each claimed backend has executable positive, exact negative diagnostic,
semantic/differential, metadata and artifact-validation evidence tied to its
actual target/compiler identity. Inputs: canonical IR/domain fixtures, backend
outputs, expected diagnostics and target availability. Outputs: fixture/target
matrix, execution and preservation reports, replay records and evidence pack.

Remaining tasks: reconcile canonical operation/domain fixture matrix; implement
family adapters and exact rejection assertions; execute or semantically compare
real outputs; check evidence/provenance preservation and nondeterminism replay;
record unavailable targets explicitly without converting skips to passes;
publish exact-source family coverage. Yes/no: does each claim have positive and
negative execution evidence, are metadata checks independent of result equality,
and are unavailable targets honestly open? Evidence:
[existing test-matrix artifact](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00224-backend-test-matrix-file-artifact.clj)
is a starting integration surface, not acceptance. Decision owner: conformance
maintainer; recommendation: shared fixture semantics plus target-specific runners,
with explicit availability records. Conformance interface work is parallel;
family acceptance awaits its actual backend. No automatic release closure follows.
