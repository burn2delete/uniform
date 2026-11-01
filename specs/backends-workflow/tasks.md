# Tasks: Backends family implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

The existing fifteen backend family keys identify grouped implementation records.
This marked region is their sole editable task authority. Every record remains
Open and unchecked. Each record preserves its original Remaining tasks paragraph,
including acceptance questions, evidence limits and integration context. These are
family records, not newly numbered individual clauses or completion percentages.
Historical migration checkboxes below remain separate from implementation status.

<!-- backends-implementation-tasks:start -->

- [ ] `backend-interface` Backend Interface and ABI

Status: Open

Projected task block:
Remaining tasks: implement required-input/version validation; define per-target
ABI/layout mappings and reject unrepresentable values; connect runtime/provider
selection and proof-to-target assumptions; exercise accepted and missing-proof,
wrong-profile, unsupported-operation and incompatible-ABI handoffs. Yes/no:
are unchecked inputs rejected, assumptions justified and ABI decisions explicit?
All must be yes. Evidence: [existing interface artifact](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00225-b1-document-source-artifact.clj).
Decision owner: interface maintainer; recommendation: versioned shared envelope
with target-specific ABI records, not one host-shaped layout. Integration needs
MIR/Lower facts; envelope fixtures and individual backends proceed in parallel.

- [ ] `backend-c` C

Status: Open

Projected task block:
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

- [ ] `backend-llvm` LLVM

Status: Open

Projected task block:
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

- [ ] `backend-wasm` Wasm

Status: Open

Projected task block:
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

- [ ] `backend-jvm` JVM

Status: Open

Projected task block:
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

- [ ] `backend-js` JavaScript and TypeScript

Status: Open

Projected task block:
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

- [ ] `backend-mlir` MLIR

Status: Open

Projected task block:
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

- [ ] `backend-gpu` GPU Backend

Status: Open

Projected task block:
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

- [ ] `backend-hdl` HDL

Status: Open

Projected task block:
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

- [ ] `backend-workflow` Workflow Backend

Status: Open

Projected task block:
Remaining tasks: implement graph/provider lowering; emit schemas and replay/
idempotency policy; encode retry/compensation/review transitions; run emitted
graphs through a declared runtime including restart/replay; reject unrecorded
nondeterminism, missing review and repeated external effects. Yes/no: can the
graph resume/replay correctly, are external effects guarded and origins retained?
Evidence: [workflow graph fixture](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00255-b10-document-workflow-graph.clj)
is fixed graph/event text, not general executable lowering. Decision owner:
graph maintainer; recommendation: provider-neutral graph plus a versioned runtime
adapter. Parallel to native backends; depends on Workflow/Schema/Auth interfaces.

- [ ] `backend-ai` AI Artifacts

Status: Open

Projected task block:
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

- [ ] `backend-query` Query and Relational

Status: Open

Projected task block:
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

- [ ] `backend-mobile` Mobile

Status: Open

Projected task block:
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

- [ ] `backend-emission` Artifact Emission Contract

Status: Open

Projected task block:
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

- [ ] `backend-conformance` Backend Conformance

Status: Open

Projected task block:
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

<!-- backends-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Backends workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Backends canonical remaining work](../../docs/component-roadmaps/backends.md#release-outcome); do not copy its checklist here.

- [x] T001 [US1] Prepare the four owned package artifacts, preserving canonical
  contract, component and phase links. Evidence: the files in this directory.
- [x] T002 [US1] Validate all local links/anchors, ASCII, exact owned paths and
  preserved implementation status; retain the result in evidence.md.
- [ ] T003 [US2] Obtain independent review of the exact package candidate,
  including dependencies, boundary/nonclaims and semantic names.
- [ ] T004 [US1] Integrate the reviewed package and record its merge receipt.

## Dependencies and outcomes

T002 checks T001; T003 consumes the checked candidate; T004 consumes independent
acceptance. Parallel packages do not block this package's local preparation.
Component completion is not a prerequisite for its workflow migration, and
migration completion does not close the component's implementation tasks.
Partial work stays draft. No budget, task quota or repeated authorization gate.

## New-name review

Review `backends-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
