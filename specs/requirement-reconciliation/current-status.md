# Current project task list and remaining reconciliation

Read-only view of canonical task source `4325a9f46292472a7f984c5adfef81209874bb5c`. Requirement proposal snapshot SHA256 `f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690`. This view changes no task or acceptance state.

## Current finite component-task list

24 components; 138 implementation outcome records: 2 accepted, 2 partial, 133 open, 1 unassessed. Accepted-outcome fraction is 2/138 (about 1.4%) for this coarse population only. It is not the whole-project implementation percentage. There are no explicit Blocked status labels; dependency/frontier eligibility is separate.

The accepted rows are Reader decode/source identity and fresh Syntax handoff, with their selected-current/development-unadmitted scopes and all limitations retained in the exact source statuses. Historical scoped completed work is not erased by Open aggregate rows. Migration checkboxes, selected-scope and SH/FL milestone views are not added to this total.

## Requirement coverage and concrete remaining gate

All 240 originals plus five declared extension/policy sources are represented: 10,096 nonblank source dispositions and 29,177 exact facets. No original source is missing. The 1231 candidate records are still proposed as a whole model; their current implementation acceptance is unassessed. There are 25 independently confirmed alias dispositions and 65 newly applied Compiler/Testing aliases pending applied-graph review. All original records and source facets are retained.

Current task-interface coverage: {:primary-interface 596, :no-current-canonical-task-interface 577, :additional-interface-only 58}. These gaps include constraints and pending folds; they are not automatically new implementation tasks.

All requested family review batches have been delivered. Six batches/961 original records are incorporated, including 40 Compiler and 25 Testing alias applications plus four Testing predicate corrections; Foundation82 and Math/Backend134 (216 records) await central incorporation. Remaining decisions in incorporated batches are still explicit. The bottleneck is applying the delivered decisions, not waiting for new research. The exact per-ID queue and the full proposed work inventory are in the paired EDN view.

## Delivered decision queue

| Family | Records | Incorporated | Actions |
| --- | ---: | --- | --- |
| profiles-performance | 79 | true | {:split 36, :alias 14, :keep 22, :correct 3, :confirm-existing-alias 4} |
| ai-build-tools | 245 | true | {:keep 99, :alias 105, :split 39, :correct 2} |
| safety | 119 | true | {:alias 53, :split 31, :keep 32, :correct-hold 2, :keep-join-hold 1} |
| language-runtime-domain-schema | 295 | true | {:keep 236, :split 37, :alias 14, :preserve-retired-alias 6, :correct 2} |
| compiler | 119 | true | {:ownership 25, :alias 40, :keep 53, :correction 1} |
| testing | 104 | true | {:keep 41, :alias 25, :facet-refinement 15, :split 14, :correct 4, :keep-join-hold 1, :conditional-refinement 3, :correction-hold 1} |
| foundation | 82 | false | {:split 3, :keep 70, :alias 9} |
| math-backends | 134 | false | {:keep 124, :correction 7, :split 3} |

Additional central scope work: BOOT/GOV34; Standard Library67 plus five overlap questions/31 mixed accounting facets; extension/policy43 including binding conduct roles and future product outcomes. Known-scope implementation details stay acceptance qualifications, not new scope blockers.

Freeze gate: apply the finite split/fold/role decisions with source/evidence lineage, resolve actual overlap and task gaps, obtain exact-model review, and version the resulting nonoverlapping in-scope set. Evidence assessment may remain visibly unassessed inside that set; it must not be removed to improve a rate. Current requirement acceptance projection is unfinished.

## Exact canonical implementation records and done criteria

### auth / `auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/auth-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Typed provider fixtures with deterministic selection and incompatible-provider rejection

### auth / `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/auth-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Real allowed operation and denied widening/revoked/wrong-phase operations

### auth / `auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/auth-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

No externally visible operation after denial; explicit grant provenance end to end

### auth / `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/auth-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Tamper, stale-policy and wrong-source rejection at actual consuming boundary

### auth / `auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/auth-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Accepted/rejected matrix and named residual trust roots for each provider

### backends / `backend-interface`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Remaining tasks: implement required-input/version validation; define per-target
ABI/layout mappings and reject unrepresentable values; connect runtime/provider
selection and proof-to-target assumptions; exercise accepted and missing-proof,
wrong-profile, unsupported-operation and incompatible-ABI handoffs. Yes/no:
are unchecked inputs rejected, assumptions justified and ABI decisions explicit?
All must be yes. Evidence: [existing interface artifact](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00225-b1-document-source-artifact.clj).
Decision owner: interface maintainer; recommendation: versioned shared envelope
with target-specific ABI records, not one host-shaped layout. Integration needs
MIR/Lower facts; envelope fixtures and individual backends proceed in parallel.

### backends / `backend-c`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

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

### backends / `backend-llvm`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

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

### backends / `backend-wasm`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

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

### backends / `backend-jvm`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

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

### backends / `backend-js`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

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

### backends / `backend-mlir`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

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

### backends / `backend-gpu`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

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

### backends / `backend-hdl`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

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

### backends / `backend-workflow`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Remaining tasks: implement graph/provider lowering; emit schemas and replay/
idempotency policy; encode retry/compensation/review transitions; run emitted
graphs through a declared runtime including restart/replay; reject unrecorded
nondeterminism, missing review and repeated external effects. Yes/no: can the
graph resume/replay correctly, are external effects guarded and origins retained?
Evidence: [workflow graph fixture](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00255-b10-document-workflow-graph.clj)
is fixed graph/event text, not general executable lowering. Decision owner:
graph maintainer; recommendation: provider-neutral graph plus a versioned runtime
adapter. Parallel to native backends; depends on Workflow/Schema/Auth interfaces.

### backends / `backend-ai`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

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

### backends / `backend-query`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

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

### backends / `backend-mobile`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

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

### backends / `backend-emission`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

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

### backends / `backend-conformance`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/backends-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

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

### build / `build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/build-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Real dependency resolution with conflict, profile and capability rejection

### build / `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/build-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Clean build node receipts and undeclared environment/network/shell rejection

### build / `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/build-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Reuse on equal inputs; rebuild on source, compiler, grant, policy or target changes

### build / `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/build-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Actual published package retrieval/verification fixtures; tamper and missing-record rejection

### build / `build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/build-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Repeated locked builds with matching identities and explicit failing-target diagnostics

### cli / `cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/cli-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Command matrix that distinguishes implemented, rejected and unavailable operations

### cli / `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/cli-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Executed success/failure fixtures and artifact paths at public command boundary

### cli / `cli/remaining-tasks-and-evidence/connect-replformatlintdocinspectprofileauditairegistryexplain-services`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/cli-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Each declared command has a genuine service result and negative fixture; no metadata-only success

### cli / `cli/remaining-tasks-and-evidence/normalize-json-text-exit-codes-and-redaction`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/cli-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Golden success/error outputs including source, target, grants and redacted secrets

### cli / `cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/cli-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Installed invocation, source-extension provenance and unsupported-option rejection

### compiler-api / `pipeline`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/compiler-api-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Owner role: compiler API/pipeline maintainer. All tasks open; source evidence
above is partial implementation evidence, not closure.

| Task | Concrete closing evidence |
| --- | --- |
| Stabilize public request/context/result API | Versioned schema and compatibility fixtures for source and stage-artifact calls; missing versions, incompatible schemas and hidden mutable-context access reject. A direct caller and CLI adapter receive the same semantic results. |
| Register and schedule actual passes | Built-in registration binds implementation/version to complete requires/preserves/invalidates/regenerates/effects/profile contracts; execute declared order and reject missing facts, duplicate/conflicting registration and illegal ordering before consumption. |
| Execute and retain stage products | Actual selected compiler invocation records input/output identities, diagnostics, source origins and provenance for every required stage, including fused-pass observable boundaries; verifier failure stops downstream execution. |
| Derive cache keys and invalidate facts | Use actual source/compiler/pass/plugin/grant/replay/policy/target inputs; unchanged-input reuse agrees with a clean run, and every contract invalidation class triggers revalidation or recomputation. Stale proofs, diagnostics and speculative release reuse reject. |
| Integrate compiler consumers | Build, CLI and inspection callers consume the same API artifacts; positive and negative selected-target programs demonstrate no raw/unchecked backend input and no runnable output from diagnostic-only mode. |

Yes/no acceptance: are passes actually invoked? Are all required stage facts
verified and retained? Are clean and reused results semantically equivalent?
Are stale inputs and invalid stage orders rejected? Does public API behavior
remain independent of ambient mutable compiler state? All must be yes.

### compiler-api / `plugins`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/compiler-api-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Owner role: compiler extension-host maintainer. All tasks open.

| Task | Concrete closing evidence |
| --- | --- |
| Check manifest compatibility before code loading | Actual loader validates package identity, API/compiler version, declared passes/artifacts, trust and grants; incompatible/malformed/rejected packages produce diagnostics without executing initialization code. |
| Execute scoped plugin context | Run a deterministic read-only analysis and authorized transformation through actual scoped IR APIs; read-only writes, unauthorized namespaces/phases and hidden compiler mutation reject at the operation boundary. |
| Enforce real sandbox/build-effect boundaries | Exercise sandboxed and policy-trusted plugin execution; filesystem/network/process/environment/model/tool/artifact operations without grants are denied before effects, including host escape paths. Trust labels alone do not disable scoped checks. |
| Register passes, facets and domain interfaces | Require declared schema, verifier, profile support, effects, lowering path and conformance records as applicable; execute registered schema/verifier callbacks and reject opaque, conflicting or unauthorized registration. Component owners retain domain semantics. |
| Verify output, trace execution and revalidate reuse | Run the declared output verifier, reject invalid transformed artifacts, record actual plugin/package/grant/input/output/replay identities, and invalidate cache entries on changed versions, policy or dependencies. |

Yes/no acceptance: can incompatible plugin initialization run? Can a plugin
perform ungranted effects or mutate hidden state? Can an invalid output enter
the pipeline? All must be no. Are allowed sandboxed/trusted invocations real,
traceable and reproducible under their declared replay policy? Must be yes.

### compiler-api / `pass-evidence`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/compiler-api-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Owner role: compiler verification maintainer. All tasks open.

| Task | Concrete closing evidence |
| --- | --- |
| Classify real pass population | Enumerate every pass in the declared compiler closure with version, risk, affected profiles/targets and governing minimum evidence; reject omitted passes or incompatible evidence classes. |
| Execute required verification gates | Invoke actual schema/semantic verifiers at required compiler boundaries; bind reports to input/output identity and stop affected artifact production on failure rather than accepting supplied success flags. |
| Bind transformation evidence | Changed MIR functions receive the governing translation validation or checked proof evidence; check-elision proofs/certificates are checked, and target lowering has actual differential/backend conformance receipts for claimed targets. |
| Capture failures and preserve trust boundaries | Record counterexample input/output/property/diagnostic and source identities, retain the prior valid artifact after rejected transformation, and turn reproducible failures into regression fixtures. |
| Emit truthful trust and release-gate records | Reports enumerate supported profiles/targets, residual evidence gaps and experimental gates; missing/high-risk/stale evidence blocks affected release artifacts. Release consumes this decision but independently owns final acceptance. |

Yes/no acceptance: do reports derive from actual checks on exact artifacts?
Are plugin and backend passes included when used? Are absent evidence and
counterexamples visible? Do failures block the affected claimed release path?
All must be yes. No claim of a fully mechanized compiler is implied.

### compiler-api / `development-boundaries`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/compiler-api-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Owner role: compiler development infrastructure maintainer. Current acceptance unassessed.
This is the new task-authority record for the existing SH-01 delivery, not a
claim of missing implementation or another pipeline/pass-evidence task. The
original accepted E01 scope and four-test/57-assertion report remain historical
completion at `cbc55cd1477deca08a9c042c91db98005833021a`; the
[source-pinned audit](https://github.com/burn2delete/uniform/blob/15a5499e97798621d270c36dd2546ce98ddd62a9/docs/self-hosting-sh00-sh06-evidence-audit.md)
retains the later S-after-repair qualifications. Open tracks unassessed current
acceptance, and does not reopen the historically completed implementation.

| Task | Concrete closing evidence |
| --- | --- |
| Publish semantic module and API ownership | One actual semantic owner for each compiler stage and public API; total source-pinned ownership projection with duplicate and missing-owner rejection. |
| Preserve fixture and shared-routing ownership | Fixture, test, artifact-generation, coverage and shared dispatch paths have explicit owners; executable shared files have no competing semantic writers. |
| Permit dedicated focused tests | Add and select a focused owning test without unrelated changes to the monolithic bootstrap namespace; retain the actual dedicated discovery result. |
| Delegate compatibility entrypoints | Compatibility wrappers call the owning implementation; representative checks reject a second semantic implementation hidden behind an adapter. |
| Select affected verification truthfully | Exact namespace/selector selection covers affected callers and rejects unknown selectors; failed, interrupted and skipped results retain their actual disposition. |
| Verify representative ownership boundaries | Source-pinned leaf and shared changes exercise routing and ownership with affected checks, without unrelated compiler modifications. |

Yes/no acceptance: are semantic and fixture/shared-path owners total and unique,
dedicated test changes possible, compatibility entrypoints delegated, affected
selectors strict, and representative leaf/shared changes verified? All must be
yes for current acceptance. Preserve historical completion at its original scope.
Stop at this existing boundary; no new coordination framework, compiler feature,
mass refactor, per-change admission wrapper or task-count gate is required.

### core / `core/current-evidence-and-remaining-work/core1-lower-core-forms`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/core-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Implement quote, if, do, let, fn, loop/recur, def, var, set!, try/throw and match with specified evaluation order and shape validation.

### core / `core/current-evidence-and-remaining-work/core2-preserve-binding-structure`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/core-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Represent lexical identity, closure captures, recursion and mutation explicitly; attach resolved references before downstream typed-core acceptance.

### core / `core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/core-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Lower surface conveniences while recording source/generated origins, effects, capabilities, metadata and unsafe declarations.

### core / `core/current-evidence-and-remaining-work/core4-verify-and-serialize`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/core-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Reject malformed node graphs, illegal control positions, unresolved required bindings and lost origins; round-trip valid modules.

### core / `core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/core-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Use the same core interface for handwritten/generated input and feed Types/Effects with accepted control-flow programs and rejected malformed/illegal core fixtures.

### diag / `diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/diag-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Record/schema parity fixtures from each producer, including malformed-record rejection

### diag / `diag/remaining-tasks-and-evidence/preserve-primaryrelatedgenerated-origins-and-stable-order`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/diag-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Macro and optimized-source diagnostics with original and generated locations

### diag / `diag/remaining-tasks-and-evidence/close-textjsonredaction-and-explain-contracts`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/diag-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Golden text/JSON equivalence, secret redaction and known-ID explanation

### diag / `diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/diag-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Same underlying diagnostic identity across public consumers and failure paths

### domains / `math`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/domains-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Outcome: [math contracts](../../docs/phase-05-mathematical-and-elementary-function-system/README.md) execute with the declared
numeric semantics and checked justification for transformations. Remaining
finite tasks, all open: implement numeric-mode/domain checking; implement EFIR
lowering and evaluation; connect EML/certificate checking to justified rewrites;
implement certified approximation/interval and symbolic operations; run the
contract conformance matrix with rejected unproved transformations.

Yes/no acceptance: do domain and branch errors reject? Do proof-dependent
rewrites require checked evidence? Do strict-mode results match the selected
semantics? Integration: Types/MIR to EFIR, Opt proof preservation, Lower/Stdlib
numeric APIs. Decision owner: numeric maintainer; recommendation: keep EML as
adjacent proof/search representation, never ordinary runtime equality authority.

### domains / `schema`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/domains-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Outcome: generated consumers enforce the same source schema. Remaining tasks,
all open: implement schema normalization and validators; implement canonical
serialization and version identities; generate GraphQL/OpenAPI and ABI/config
artifacts; implement checked migration mapping; execute source/generated parity
and weakening/taint/invalid-data rejection fixtures.

Yes/no acceptance: is schema identity stable? Do generated consumers reject
invalid values? Are migration loss and secret exposure prevented or explicitly
rejected? Integration: Types to schema, Emit generated artifacts, Build schema
identity, Query/AI/Stdlib consumers. Decision owner: schema maintainer;
recommendation: shared canonical schema IR with explicit target adapters.

### domains / `query`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/domains-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Outcome: real parameterized queries return validated rows and respect declared
transaction/provider semantics. Remaining tasks, all open: implement relational
IR and typed parameters; lower to a declared provider; connect row/schema and
transaction checks; execute result-equivalence, injection, missing-grant and
invalid-row fixtures. Yes/no acceptance: are parameters separated from query
syntax? Are results typed and grants enforced? Do transaction failures preserve
semantics? Integration: Schema/MIR to Query, Runtime provider, Stdlib database
API. Decision owner: query maintainer; recommendation: explicit provider dialect
contract, not host SQL strings as semantic authority.

### domains / `workflow`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/domains-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Outcome: a workflow executes and replays without repeating recorded external
effects. Remaining tasks, all open: implement graph validation/lowering;
implement event-log schema and durable transitions; connect retry/compensation/
timer providers; execute crash/restart/replay and unrecorded-nondeterminism
rejection fixtures. Yes/no acceptance: is replay deterministic? Are external
calls deduplicated? Are invalid transitions rejected? Integration: MIR/Schema,
Runtime durability, AI review steps, Stdlib workflows. Decision owner: workflow
maintainer; recommendation: versioned event log as replay input, explicit
provider boundary for storage rather than a hidden database choice.

### domains / `ai`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/domains-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Outcome: the declared AI workflow executes through schema, capability, taint
and human-review boundaries. Remaining tasks, all open: implement declaration
lowering; connect model/tool/memory providers with explicit policies; implement
structured-output and taint validation; connect human review and replay;
execute evaluations plus prompt-injection, missing-review, cross-tenant and
unvalidated-output rejection fixtures. Yes/no acceptance: does untrusted output
remain data? Are actions denied without grants/review? Are evaluations bound to
exact prompt/model/tool identities? Integration: Schema/Auth, Workflow/Runtime,
Stdlib AI. Decision owner: AI maintainer; recommendation: provider-independent
artifacts and explicit model identity, never a vendor SDK side path.

### domains / `gpu`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/domains-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Outcome: kernels execute on the adopted target with validated memory/barrier
behavior. Remaining tasks, all open: adopt target and kernel ABI; implement GPU
IR/checking; lower kernel and host dispatch; execute numeric/result and illegal
memory-space/barrier fixtures. Yes/no acceptance: do kernels produce the
specified results? Are invalid synchronization and target features rejected?
Integration: MIR/Legality, Lower/Emit, explicit Runtime dispatch. Decision owner:
accelerator maintainer; recommendation: one named reference target first,
additional targets only by their own conformance evidence. Target choice is
not silently adopted by this roadmap.

### domains / `ui`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/domains-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Outcome: an actual UI artifact renders, handles typed events and enforces
capabilities through the adopted adapter. Remaining tasks, all open: lock UI
IR/state/event contract; implement lowering and rendering adapter; connect
runtime events and source maps; execute render/update and invalid-event/unsafe
DOM/authority rejection fixtures. Yes/no acceptance: do state transitions and
rendering follow typed semantics? Are unsafe or unauthorized actions rejected?
Integration: Types/MIR, Lower/Emit, Runtime and Stdlib UI. Decision owner: UI
maintainer; recommendation: explicit adapter contract rather than browser host
semantics defining the language. Backend choice remains an explicit decision.

### effects / `effects/current-evidence-and-remaining-work/effects1-infer-direct-effects`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/effects-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Classify actual core operations including allocation, IO, FFI, errors, concurrency, model/tool/memory and compile-time behavior with source references.

### effects / `effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/effects-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Compute closure, call, dispatch and recursive-function effect summaries to a valid fixed point; distinguish declared from inferred effects.

### effects / `effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/effects-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Reject undeclared effects and forbidden profile behavior, expose capability requirements, and preserve sequencing constraints without granting authority.

### effects / `effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/effects-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Track nondeterministic/build effects and replay inputs, rejecting unreplayable hermetic behavior; invalidate summaries on call graph or declaration changes.

### effects / `effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/effects-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Feed profile/capability/safety validation and MIR with real propagated facts, including generated-code and higher-order negative cases.

### emit / `emit/current-evidence-and-remaining-work/emit1-reconcile-selected-target-contracts`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/emit-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Record exact target/ABI/runtime tuples, emitted kinds and supported operations for the existing staged Linux LLVM release candidate; distinguish hosted development artifacts, validate Lower input and reject undeclared targets.

### emit / `emit/current-evidence-and-remaining-work/emit2-emit-real-outputs`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/emit-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Produce declared executable/object/library or hosted artifacts through the selected backend adapters, with explicit runtime linkage and surfaced tool failures.

### emit / `emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/emit-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Hash actual outputs and attach source/compiler/dependency identity, source/debug maps, profile/effect/capability/safety/proof and ABI/runtime records.

### emit / `emit/current-evidence-and-remaining-work/emit4-establish-conformance`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/emit-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Execute accepted target fixtures and reject illegal or unsupported inputs; compare observable behavior and verify metadata preservation, tamper detection and reproducibility.

### emit / `emit/current-evidence-and-remaining-work/emit5-integrate-artifact-consumers`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/emit-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Deliver actual artifact/manifests to Runtime and Build, retaining source-pinned receipts and distinguishing development binaries from seedless signed release artifacts.

### expand / `expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/expand-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Implement syntax-object invocation and macro environment/version checks, validating returned graphs and compile phases.

### expand / `expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/expand-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Preserve definition/call/generated origins, explicit capture and unsafe declarations; reject undeclared authority capture or missing unsafe metadata.

### expand / `expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/expand-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Require declared grants, redacted logs and replay inputs for effectful expansion; reject ambient host effects and unreplayable hermetic expansion.

### expand / `expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/expand-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Bind cache reuse to every semantic input and replay record; diagnose actual expansion cycles/phase errors without node, size or work quotas.

### expand / `expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/expand-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Feed generated core forms through the same resolution/type/effect/profile/safety path as handwritten forms and retain expansion trace comparison evidence.

### legality / `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/legality-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Allowed profile matrix and hidden-allocation/reflection/effect rejection before lowering

### legality / `legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/legality-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Real use-after-move, escape, invalid borrow and linear-resource fixtures with graphs

### legality / `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/legality-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Every dangerous operation classified and unclassified operations rejected

### legality / `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/legality-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Surviving check/proof/source edges; missing-check and unjustified-elision rejection

### legality / `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/legality-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Accepted/rejected source fixtures, emitted facts and diagnostics for each claimed profile

### lower / `lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/lower-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Reject unverified/stale IR and incompatible profiles/features; expose explicit supported-operation and target matrices.

### lower / `lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/lower-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Implement calling conventions, data/closure/error/resource representations and FFI layouts from actual target rules, rejecting unrepresentable requests.

### lower / `lower/current-evidence-and-remaining-work/lower3-select-explicit-providers`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/lower-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Match runtime services and providers to effects/capabilities/profile; reject hidden hosted services and authority-widening fallbacks.

### lower / `lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/lower-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Map semantic proofs to target metadata only when assumptions hold, preserving source, safety and dependency records; reject proofless noalias/no-overflow/fast-math.

### lower / `lower/current-evidence-and-remaining-work/lower5-integrate-target-emission`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/lower-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Feed Emit accepted backend-ready inputs and rejected unsupported-feature cases for the selected staged Linux LLVM release candidate; keep hosted development evidence separate, without claiming every platform.

### mir / `mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/mir-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Lower checked-core control flow, calls/closures, data, memory/regions/resources, errors, FFI/concurrency and declared domain/workflow/AI operations without target-specific instructions.

### mir / `mir/current-evidence-and-remaining-work/mir2-attach-fact-tables`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/mir-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Bind every operation to current types, effects, capabilities, ownership, safety, runtime checks, proof and origin records.

### mir / `mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/mir-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Check block termination, dominance, operand schemas, effect ordering, guard dominance, resource facts and valid domain anchors; reject missing or stale evidence.

### mir / `mir/current-evidence-and-remaining-work/mir4-serialize-stable-modules`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/mir-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Round-trip operations and shared tables with deterministic identity and preserve analysis invalidation hooks.

### mir / `mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/mir-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Run actual verifier on constructed and mutated MIR; retain positive and negative artifacts for malformed flow, missing facts, target leaks and invalid origins before Opt/Lower consumption.

### opt / `opt/current-evidence-and-remaining-work/opt1-execute-pass-contracts`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/opt-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Validate required/preserved/invalidated facts and deterministic ordering, then run actual constant/branch/dead-code/common-expression transformations.

### opt / `opt/current-evidence-and-remaining-work/opt2-implement-structural-families`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/opt-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Implement inlining/specialization, closure/allocation/region/resource transformations, dispatch/loop/vectorization preparation and effect-safe scheduling with explicit decisions.

### opt / `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/opt-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Require governing proof for check elision and domain/math transformations; retain checks or reject unsupported transformations and preserve anchors/source provenance.

### opt / `opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/opt-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Actually verify mutated MIR, regenerate invalidated analyses or keep conservative checks, and translation-validate or prove high-risk changes.

### opt / `opt/current-evidence-and-remaining-work/opt5-integrate-reproducible-pipeline`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/opt-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Record source/compiler/pass identities, no-change decisions, residual work and replay inputs; demonstrate changed semantics rejection and unchanged semantics across Opt -> Lower.

### platform / `hardware`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/platform-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Adopt clock/reset/HDL target contract; implement state-machine lowering and simulation; verify timing/reset invariants and reject illegal dynamic/runtime operations; emit traceable hardware artifact

### platform / `firmware`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/platform-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Adopt device memory/startup target; implement interrupt/MMIO/startup and explicit allocation; verify image/link map and rejected unsupported services on execution or named emulator

### platform / `kernel`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/platform-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Adopt kernel ABI/execution model; implement initialization, memory and scheduling interface slice required by contract; verify unsafe boundaries, no hidden hosted services and artifact provenance

### platform / `devices`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/platform-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Adopt device/provider interfaces; implement ownership-aware MMIO/interrupt/transfer adapters; validate denial without grants, lifetime handling and device failure translation

### reader / `reader/finite-remaining-work/contract-coverage-reconciliation`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/reader-workflow/tasks.md#implementation-tasks)

Status: Open; source leaves above and the two governing contracts exist. Reader maintainer records each literal, abbreviation, namespace shape, extension and output against actual implementation and fixtures.

Done criteria:

Every required surface/output has a concrete implementation location and accepted/rejected fixture, or an explicit failing case in the implementation change; no omitted family can be marked complete.

### reader / `reader/finite-remaining-work/decode-and-source-identity-closure`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/reader-workflow/tasks.md#implementation-tasks)

Status: Accepted; the [selected-current decoding and identity reconciliation](https://github.com/burn2delete/uniform/blob/codex/reader-decode-source-identity-20261027/docs/artifacts/phase-06/reports/current-reader-decode-source-identity.md) covers both extensions, mixed newlines and Unicode spans, explicit roots/options, five malformed UTF-8 families and a genuine second process in a different working directory. Exact `1d6c2681` / tree `7e77bab3` passed population 2 tests / 2,712 assertions and foreign-directory comparison 2 tests / 1,158 assertions; both exited naturally 0 with fresh Reader/envelope verification. Earlier foreign-directory failure and broker-admission nonexecution are preserved. Separate composed-tree controls passed 15 tests / 51 assertions on `07e5a32d` without source replay. Semantic identities match while real invocation-root provenance remains distinct. Public admission, other Reader rows and downstream C5/C14 acceptance remain open.

Done criteria:

Both extensions preserve actual paths; byte/scalar and line/column spans, explicit roots/options and UTF-8 failures match contracts; ambient working-directory changes cannot alter identity.

### reader / `reader/finite-remaining-work/lexical-products-and-origins-closure`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/reader-workflow/tasks.md#implementation-tasks)

Status: Partial Reader and projection sources. The focused token-spelling reuse increment and its validation limits are recorded in [Reader evidence](https://github.com/burn2delete/uniform/blob/codex/reader-token-spelling-reuse-20261026/specs/reader-workflow/evidence.md#token-spelling-reuse); it does not close the complete lexical surface.

Done criteria:

All collection/literal/abbreviation/metadata families emit faithful products; malformed numeric candidates reject; valid deferred values retain exact spelling; trivia modes and generated origins round-trip as specified.

### reader / `reader/finite-remaining-work/extension-policy-closure`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/reader-workflow/tasks.md#implementation-tasks)

Status: Extension records/projected views exist; accepted authorization behavior not established here.

Done criteria:

Registered pure and explicitly granted handlers work; unknown/disallowed tags and undeclared effects reject without ambient execution; invocation records identify policy and handler.

### reader / `reader/finite-remaining-work/authentication-and-quota-removal-closure`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/reader-workflow/tasks.md#implementation-tasks)

Status: Partial; merged current invocation and default/source-drift/tamper controls are evidenced by the pilot. The complete Reader fixture population remains open.

Done criteria:

Original-source reconstruction rejects substituted/missing/duplicated graph references and stale hashes while accepting otherwise valid input without project accounting refusals. Real machine failure is reported as failure, not semantic rejection or acceptance.

### reader / `reader/finite-remaining-work/fresh-syntax-handoff`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/reader-workflow/tasks.md#implementation-tasks)

Status: Accepted; selected-current, development-unadmitted boundary in the [finite family reconciliation](https://github.com/burn2delete/uniform/blob/codex/reader-fresh-syntax-row-20261027/docs/artifacts/phase-06/reports/current-reader-fresh-syntax-handoff.md). Exact candidate `bcf9c72e` / tree `6043bc1d` completed four genuine constructions and fresh owner controls; independent review accepted scoped attribution while preserving the combined natural-exit-1 context failure (26 tests, 733 passes, one failure). Separate compiler-free composition passed 19 tests / 106 assertions on the same identity. PR471/478 retain their original span/provenance and ratio evidence. Other Reader rows, canonical Syntax generated-graph coverage, public admission, C5 and C14 remain open.

Done criteria:

Fresh Syntax consumes actual Reader output for accepted and malformed/tampered cases, preserves spans/metadata/origins and reports owning-stage failures. No cached historical pass grants authority to this run.

### reader / `reader/finite-remaining-work/component-release-evidence`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/reader-workflow/tasks.md#implementation-tasks)

Status: Open; the accepted finite invocation receipt is not the complete Reader release outcome.

Done criteria:

Owner supplies immutable source/fixture/runtime identities, complete surface results, diagnostic goldens, fresh handoff and residual-boundary statement; independent review and Deus disposition explicitly accept this outcome.

### release / `release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/release-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Component outcome receipts, positive/negative programs and explicit unsupported surface

### release / `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/release-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Fresh stage-to-stage outputs and diagnostics, residual trusted-code inventory

### release / `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/release-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Actually invoke the prior Gravity compiler process on the complete declared next-stage compiler/runtime/library/build closure, retain real outputs and logs/lineage, link/package and execute that next compiler on accepted and rejected compiler inputs without Clojure in candidate compilation/runtime/build; then public check/run/compile and output executable demonstrate absent seed boundaries. SH25 component construction, SH27 equivalence and final release acceptance remain distinct; supplied rebuild records or plans do not prove execution.

### release / `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/release-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Rebuild identity and signature/provenance verification plus tamper rejection

### release / `release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/release-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Coordinator-reviewed candidate receipts and explicit release disposition

### resolve / `resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/resolve-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Cover locals, loop/recur, vars, macros, types, protocols, constructors, capabilities and special positions with stable identity and legal shadowing.

### resolve / `resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/resolve-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Resolve aliases/imports/exports against actual module artifacts; reject absent, private or ambiguous names rather than synthesizing unsupported external bindings.

### resolve / `resolve/current-evidence-and-remaining-work/resolve3-check-graph-legality`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/resolve-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Record dependency versions and initialization edges; reject unapproved cycles and illegal cross-profile imports, preserving effect/capability metadata.

### resolve / `resolve/current-evidence-and-remaining-work/resolve4-publish-stable-indexes`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/resolve-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Serialize scope/binding graphs and invalidate dependent references when export, profile, package or semantic identity changes.

### resolve / `resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/resolve-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Connect Core/Types and diagnostic/LSP consumers; demonstrate accepted shadowing and imports plus rejected ambiguity, visibility, cycle and stale-export cases.

### runtime / `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/runtime-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Actual service link/load inspection for every claimed profile/target and forbidden-service rejection

### runtime / `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/runtime-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Executed startup/failure/resource fixtures and manifest-to-service agreement

### runtime / `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/runtime-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Allocation, lifetime, transfer, race and foreign-failure conformance with explicit providers

### runtime / `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/runtime-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Recorded nondeterminism, replay equality, no duplicated external effect, REPL grant denial

### runtime / `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/runtime-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Denial at actual service hook, redacted diagnostics and source-map preservation

### stdlib / `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/stdlib-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Per-export source/implementation/fixture map, with absent exports explicitly open

### stdlib / `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/stdlib-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Compiled examples and type/arity/value rejection through real APIs

### stdlib / `stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/stdlib-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Provider-mediated executions, missing-grant rejection and safe resource teardown

### stdlib / `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/stdlib-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

APIs consuming corresponding domain/runtime outcomes, with boundary failures exercised

### stdlib / `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/stdlib-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Actual supported platform API fixtures, unsafe audits and compatibility/stability report

### syntax / `syntax/current-evidence-and-remaining-work/syntax1-construct-canonical-syntax`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/syntax-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Implement every required form kind with stable semantic identity, namespace/profile/phase and metadata; reject malformed fields and source references.

### syntax / `syntax/current-evidence-and-remaining-work/syntax2-preserve-transformations`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/syntax-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Record generated producer/input origins, explicit metadata changes, hygiene marks and intentional captures; reject accidental capture.

### syntax / `syntax/current-evidence-and-remaining-work/syntax3-invalidate-facts`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/syntax-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Version attached facts and invalidate them when the producing input or transformation changes; reject stale fact reuse.

### syntax / `syntax/current-evidence-and-remaining-work/syntax4-serialize-and-verify`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/syntax-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Round-trip all required form kinds and generated graphs, including shared origin references; demonstrate identity stability and tamper rejection.

### syntax / `syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/syntax-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Exercise Reader -> Syntax -> Expand and Resolve consumption with original/generated diagnostic spans; record source-pinned accepted and rejected artifacts.

### tools / `tools/finite-service-outcomes-and-remaining-work/format-formatter-maintainer`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/tools-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Reader output and semantic syntax identity preserved? Comments retained? Invalid input rejected? Generated-file policy respected? [Formatter](../../docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md)

### tools / `tools/finite-service-outcomes-and-remaining-work/lint-analysis-tool-maintainer`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/tools-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Rule source/facts and applicability visible? Unsafe fixes rejected? Baseline/fail-on-new reproducible? [Linter](../../docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md)

### tools / `tools/finite-service-outcomes-and-remaining-work/language-server-editor-maintainer`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/tools-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Incremental state converges to CLI checks? Visibility/profile rules preserved? Stale edits and unsafe rename rejected? [Language server](../../docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md)

### tools / `tools/finite-service-outcomes-and-remaining-work/debug-debugger-maintainer`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/tools-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Source maps resolve? Unavailable versus optimized-away values distinguished? Unauthorized mutation and secret inspection rejected? [Debugger](../../docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md)

### tools / `tools/finite-service-outcomes-and-remaining-work/repl-interactive-runtime-tooling-maintainer`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/tools-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Same legality as compiled code? Grant denial before effect? Load/inspect/evaluate run in actual session? [REPL](../../docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md)

### tools / `tools/finite-service-outcomes-and-remaining-work/ir-explorer-compiler-tool-maintainer`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/tools-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Version/profile/target and artifact IDs present? Missing/stale stages shown honestly? Check-elision evidence visible? [IR inspector](../../docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md)

### tools / `tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/tools-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Samples measured rather than generated? Unavailable counters explicit? No unrelated grant or proofless elision? [Profiler](../../docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md)

### tools / `tools/finite-service-outcomes-and-remaining-work/docs-documentation-tool-maintainer`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/tools-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Signatures/effects/profiles match exports? Failing examples and stale docs rejected? [Documentation generator](../../docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md)

### tools / `tools/finite-service-outcomes-and-remaining-work/dev-server-development-session-maintainer`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/tools-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Changes produce actual updated artifacts? Incompatible hot reload restarts? Grants remain explicit? [Dev server](../../docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md)

### tools / `tools/finite-service-outcomes-and-remaining-work/registry-view-package-tool-maintainer`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/tools-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

CLI/API package facts agree? Capability changes visible before install? Unauthorized package existence not disclosed? [Registry UX](../../docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md)

### tools / `tools/finite-service-outcomes-and-remaining-work/safety-explorer-safety-tool-maintainer`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/tools-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Requested/granted/denied/used authority distinguished? Missing evidence visible? Certificate/checker identities retained? [Safety audit](../../docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md)

### tools / `tools/finite-service-outcomes-and-remaining-work/ai-development-ai-tool-maintainer`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/tools-workflow/tasks.md#implementation-tasks)

Status: Open

Done criteria:

Repository/tool-output instructions remain untrusted data? Unreviewed writes rejected? Applied change has model/prompt/tool/reviewer identity? [AI tooling](../../docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md)

### types / `types/current-evidence-and-remaining-work/types1-solve-core-constraints`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/types-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Implement literal, binding, function/application, branch, collection and error-result constraints with actionable mismatch diagnostics.

### types / `types/current-evidence-and-remaining-work/types2-check-declared-abstractions`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/types-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Validate generics, protocols/dispatch, records and schema-derived types against their declarations and resolved identities.

### types / `types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/types-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Require checked casts and representability/numeric conversions where specified; reject implicit unsafe narrowing and unsupported profile-dependent dynamic behavior.

### types / `types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/types-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Serialize type solutions and layout requirements with source/profile/ownership/effect references; invalidate on changed nodes, bindings or contracts.

### types / `types/current-evidence-and-remaining-work/types5-integrate-typed-core`

[Owning task source](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/types-workflow/tasks.md#implementation-tasks)

Status: Open; partial source evidence above is not closure

Done criteria:

Feed Effects and MIR prerequisites from actual solutions, with accepted inferred/declared cases and rejected unsatisfied, stale and illegal-conversion cases.


## Other retained Spec Kit ledgers

These nine ledgers are also part of the current Spec Kit inventory. Their scopes and checklist granularities differ from the 138 component outcome records; they are retained here without adding them into a misleading whole-project percentage. Completed bounded implementation/cohort work, adoption/documentation steps and pending integration remain attributed to their exact sources. Checked steps do not close parent component outcomes.

| Ledger | Scope | Checked steps | Unchecked steps |
| --- | --- | ---: | ---: |
| [.specify/naming-pilot/tasks.md](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/.specify/naming-pilot/tasks.md) | retained-bounded-pilot-or-increment | 4 | 1 |
| [.specify/reader-core-invocation/tasks.md](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/.specify/reader-core-invocation/tasks.md) | retained-bounded-pilot-or-increment | 8 | 0 |
| [specs/001-paired-source-core/tasks.md](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/001-paired-source-core/tasks.md) | retained-bounded-pilot-or-increment | 5 | 3 |
| [specs/accounting-removal/tasks.md](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/accounting-removal/tasks.md) | explicit-ACC-outcomes | 1 | 12 |
| [specs/build-incremental-helper-names/tasks.md](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/build-incremental-helper-names/tasks.md) | retained-bounded-pilot-or-increment | 6 | 0 |
| [specs/build-incremental-operation-names/tasks.md](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/build-incremental-operation-names/tasks.md) | retained-bounded-pilot-or-increment | 4 | 2 |
| [specs/build-incremental-projection-names/tasks.md](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/build-incremental-projection-names/tasks.md) | retained-bounded-pilot-or-increment | 7 | 0 |
| [specs/mir-carrier-semantic-regressions/tasks.md](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/mir-carrier-semantic-regressions/tasks.md) | bounded-regression-increment | 0 | 1 |
| [specs/resolve-owner-source-core-verification/tasks.md](https://github.com/burn2delete/uniform/blob/4325a9f46292472a7f984c5adfef81209874bb5c/specs/resolve-owner-source-core-verification/tasks.md) | retained-bounded-pilot-or-increment | 1 | 0 |

The component workflow files also contain preparation/adoption checkboxes outside their implementation markers. Those remain separate from implementation progress. This snapshot introduces no new task IDs, changes no checkboxes and makes no current-main completion claim from a historical cohort result alone.
