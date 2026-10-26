# Compiler API and Pass Host

Working name: **Compiler API** (proposed descriptive default).
Semantic key: `compiler-api`. Status: proposed; all outcomes below remain open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler API/pipeline maintainer, with plugin-host and compiler
verification specialists. Current owners are unassigned by this proposal;
Deus confirms existing reservations or assigns unowned work. No code ownership
is transferred by this page.

## Purpose and scope

Provide the public compiler artifact/context interface and the executable host
for built-in and extension passes. Own API compatibility, pass registration,
pipeline invocation, compiler-scoped extension execution, verifier scheduling,
fact invalidation and compiler trust reporting. Individual semantic components
still own their pass implementations, output schemas and semantic verifiers.

This is not CLI argument dispatch, package transport, generic authorization or
a new programming language. [Build](build.md) invokes this API and transports
cache/package records; [Auth](auth.md) supplies grant/trust decisions. Neither
substitutes for actual pass/plugin execution or verifier enforcement here.
Language-facet and domain semantics belong to their named component outcomes;
this project owns their registration boundary, not their implementations.

Governing contracts: [compiler architecture](../phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md),
[incremental compilation](../phase-06-compiler-architecture/095-c16-incremental-compilation-design.md),
[plugin/pass API](../phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md),
and [compiler verification](../phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md).
Foundation D1 governs canonical pipeline meaning; any ordering difference in
stage summaries is reconciled against D1 rather than silently legalized here.

## Release outcome

The compiler has a real versioned API whose invocations run declared passes,
retain observable stage artifacts and diagnostics, verify outputs and refuse
unchecked or stale artifacts before downstream use. An extension host loads
only compatible, explicitly authorized plugins, executes them within actual
compiler/build-effect restrictions, and validates their registrations and
outputs. Compiler trust reports derive from executed evidence, not declared
success fields.

The aggregate consists of the independent finite outcomes below:

- **Pipeline / `pipeline`**: built-in API, pass registry, real orchestration and
  compiler-specific reuse/invalidation for the declared compiler population.
- **Plugin Host / `plugins`**: version-compatible manifest loading, scoped
  execution and pass/facet/domain registration through the same public API.
- **Pass Evidence / `pass-evidence`**: execution-bound verifier/risk/equivalence
  evidence and truthful compiler trust/release-gate reports.

The selected staged Linux LLVM compiler candidate consumes Pipeline and Pass
Evidence for its exact compiler closure, plus Plugin Host only for extensions
that closure actually loads. General third-party extension support and wider
facet/domain registration remain explicit wider-language outcomes, not new
prerequisites for Reader or the selected no-extension compiler path. A release
that claims plugin support must satisfy Plugin Host; a disabled extension API
must reject requests honestly rather than claim support.

## Actual interfaces and dependencies

Inputs: explicit source or stage-artifact requests, compiler/API/schema versions,
profile/target, pass manifests and versions, required fact identities, pipeline
configuration, plugin package/manifest identity, scoped grants, trust policy,
dependencies, replay records and verifier registrations.

Outputs: structured API result/diagnostic records, pass/extension registrations,
stage artifacts, pipeline/provenance graph, execution traces, invalidation and
reuse decisions, actual verifier results, counterexamples and compiler trust
reports. Diagnostic-only requests cannot emit runnable artifacts.

Hard prerequisites for an integrated pass are its actual input schema, semantic
implementation and verifier. The registry/API can be built against fixed small
pass fixtures before all compiler components complete. Built-in execution does
not require a completed plugin host. Plugin execution requires API/schema and
grant/enforcement interfaces, not a finished package manager: a supplied pinned
package is sufficient for development. Cache storage may be supplied by Build;
compiler key derivation and consumer-side revalidation remain owned here.
Verification consumes pass executions without requiring final Release first;
Release later consumes its reports. No self-build dependency is introduced.

## Pinned source evidence

These are inspected source facts, not new execution receipts or accepted outcomes.

| Existing source | What it establishes and does not establish |
| --- | --- |
| [Compiler pass manifest](../../bootstrap/clojure/src/gravity/compiler_pass_manifest.clj) | Constructs/validates stage0 contracts and pipeline-related records; explicitly compatibility-only and not canonical compiler, release or self-hosting authority. |
| [Pass execution contract](../../bootstrap/clojure/src/gravity/pass_execution/config.clj) | Defines receipt/fact-flow/evidence-DAG interfaces; explicitly does not own pass implementation, cache storage, proof or aggregate authority. A valid receipt is not an executed compiler. |
| [Incremental projection](../../bootstrap/clojure/src/gravity/build/incremental/projection.clj) | Constructs a stage key with fixed stage0 identity strings for several fields. It is not proof that actual producer inputs are fully hashed or that changed policies invalidate executed passes. |
| [Plugin artifact assembly](../../bootstrap/clojure/src/gravity/c17_plugin/artifact.clj) | Assembles compatibility, trust, trace and conformance records with success statuses. It does not by that construction establish that plugin code was loaded, sandboxed or executed. |
| [Plugin validation](../../bootstrap/clojure/src/gravity/c17_plugin/validation.clj) | Checks fields and reported statuses, including verifier results. Actual output verification and denied effects must be independently executed before support is claimed. |
| [Verification contract](../../bootstrap/clojure/src/gravity/c18_verification/contract.clj) | Explicitly denies canonical proof, translation-validation, trust-report and release-gate authority, and records an incomplete verification model. Existing projections cannot close Pass Evidence. |

Historical planning identifiers in these paths are source locators, not proposed
implementation names. New namespaces/APIs use semantic subsystem names. Existing
protocol/schema/diagnostic IDs remain stable unless a versioned migration is
explicitly approved. Historical quota-bearing receipt code is not an accepted
requirement: remove project-cost, byte/node/depth/work allowances without
weakening semantic identity, intrinsic representability or tamper checks.

## Built-in compiler pipeline outcome

Actual public compiler invocations execute the registered built-in stage graph
with validated input/output artifacts, preserved facts and deterministic reuse
decisions for the selected compiler closure. Source-to-Core and checked-IR
composition consume this outcome at their respective stage endpoints; neither
requires the complete extension host or final backend before demonstrating
that endpoint. Final selected-target composition consumes its complete
source-to-artifact path and the applicable Pass Evidence below.

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

## Plugin Host: finite remaining tasks

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

## Pass Evidence: finite remaining tasks

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

## Integration milestones and decision gate

1. API/schema and built-in pass registration handoff with component owners;
   execute a real valid pass and a rejected contract/output case.
2. Pipeline plus Pass Evidence on the selected compiler closure, including
   fresh source-to-artifact and clean-versus-reused comparisons.
3. Plugin Host independently demonstrates actual compatible execution,
   pre-load rejection, sandbox denial and output-verifier failure.
4. Build/CLI/Tools consume accepted outcomes; Release incorporates exact
   compiler evidence without copying these task lists or auto-closing milestones.

Decision owner: compiler API maintainer with plugin/runtime security and
verification maintainers, coordinated by Deus. Recommendation: one versioned
artifact/context API for built-ins and plugins, immutable input facts by
default, explicit operation-scoped writes, and an execution isolation mechanism
whose denial properties can be demonstrated on the supported host. Record the
chosen host isolation and compatibility policy before claiming sandbox support;
until then untrusted loading remains unsupported and denied. This finite design
record does not block built-in pipeline or independent semantic development.

No compiler/plugin execution, restricted domain action, migration attempt or
test was run for this documentation change. No quota, source-function census,
fixed task allowance or new release target defines its finish line.
