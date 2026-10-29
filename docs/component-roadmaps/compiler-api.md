# Compiler API and Pass Host

Working name: **Compiler API** (proposed descriptive default).
Semantic key: `compiler-api`. Status: proposed; all outcomes below remain open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler API/pipeline maintainer, with plugin-host and compiler
verification specialists. Current owners are unassigned by this proposal;
Deus confirms existing reservations or assigns unowned work. No code ownership
is transferred by this page.

Implementation task authority: [Spec Kit ledger](../../specs/compiler-api-workflow/tasks.md#implementation-tasks).
Edit that ledger and run `clojure -M tools/compiler_api_task_projection.clj --write`;
the five task blocks below are generated views.

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

<!-- compiler-api-task-projection/pipeline:start -->
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

Foundation requirement gravity/canonical-pass-order: Resolve the retained D1 Core-AST-before-name-resolution versus C1/C6 resolved-lowering identity/order conflict through an explicit governing-source decision. Encode the adopted semantic boundaries and demonstrate their order on actual stage products, including fused execution without inventing another stage. Refusals: Reject unresolved or conflicting order claims, unresolved required names before typed acceptance and a fused pass that omits an adopted boundary; no manifest ordering label alone resolves the source conflict. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): tools/finite-service-outcomes-and-remaining-work/lint-analysis-tool-maintainer - Canonical D3 vocabulary/layer maps and source/manifest terminology check results.

Foundation requirement gravity/domain-anchor-preservation: Carry each domain operation's typed Core/MIR semantic anchor and required facts through optimization until its owning domain contract authorizes lowering; expose replacement or invalidation edges in the emitted artifacts. Refusals: Reject an unanchored opaque domain payload, a lost source/fact edge or premature lowering that prevents the required domain verification. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs - Verified MIR/domain-operation disposition matrix and target/provider/preservation inputs; pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts.

Foundation requirement gravity/feature-layer-classification: Attach each introduced feature to its owning source semantic layer, compile-time contract, domain IR, backend/runtime provider or tooling surface, with explicit dependencies and observable artifacts. Refusals: Reject a feature classified as portable source semantics solely because a host runtime or target supports it, or a backend used to defer compile-time profile/effect/authority legality. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): tools/finite-service-outcomes-and-remaining-work/lint-analysis-tool-maintainer - Canonical D3 vocabulary/layer maps and source/manifest terminology check results; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products.

Foundation requirement gravity/fused-pass-observability: For each physically fused pass expose the same adopted semantic input/output boundaries, preserved/invalidated/regenerated facts, diagnostics and artifacts as the canonical pipeline; compare fused and unfused accepted/refused cases. Refusals: Reject lost intermediate obligations, unobservable invalidation or a fused path that accepts an input the canonical required boundary rejects. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts.

Foundation requirement gravity/pass-contract-schema: Publish and validate versioned pass input/output schemas including required, preserved, invalidated and regenerated facts, profiles, effects, capabilities, diagnostics and emitted artifacts; actual registrations and consumers use those versions. Refusals: Reject absent or incompatible schema versions, undeclared invalidation, missing required facts and a pass output that violates the selected contract. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): schema - Versioned source schema, generated validators/codecs and parity artifacts; pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts.

Foundation requirement gravity/unified-source-semantics: Compile the same supported Gravity source across its declared profiles/targets with one source semantic model, recording only contract-permitted representation/provider differences and explicit unsupported cases. Refusals: Reject a host truth/null/equality/overflow rule substituted for Gravity semantics or a backend accepting source behavior already rejected by profile/effect/safety analysis. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products; lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs - Verified MIR/domain-operation disposition matrix and target/provider/preservation inputs; tools/finite-service-outcomes-and-remaining-work/lint-analysis-tool-maintainer - Canonical D3 vocabulary/layer maps and source/manifest terminology check results.

Source compiler/c1/compiler-program-legality: Apply ordinary Gravity type/effect/capability/profile/safety/artifact rules to compiler programs. Concrete acceptance: Compile an otherwise valid compiler module with a forbidden effect, missing grant, illegal profile operation or invalid safety fact; reject it by the same owning rule as an equivalent user module, without a compiler-internal exemption. Retain actual input/output and diagnostic identities plus positive and rejected-case artifacts; supplied success flags, proposal delivery and broad parent links are not executable evidence.

Bootstrap criterion bootstrap/compiler-meta-authority: Compile and invoke each declared migrated compiler module under meta or an explicitly narrower bootstrap profile. Check its declared effects and capabilities separately against scoped grants and declared compiler inputs; pure analysis remains pure unless declared compiler artifact IO is required. Record the admitted module/profile/input/grant/effect identities and actual allowed result. Before an effect occurs, reject undeclared compiler effects and ambient environment/time/random/network/filesystem/shell/registry access outside declared inputs. Exercise a valid narrow compiler-artifact operation and forbidden host access with BOOT3002/BOOT4001/BOOT4004. A trusted implementation label does not grant ambient authority; explicit audited implementation-provider exceptions retain their governing profile and unsafe boundary. Required outputs: module profile/effect/capability manifest; allowed invocation and denied-effect fixtures; source-bound authority diagnostics.

Bootstrap criterion bootstrap/generated-compiler-checking: Feed generated compiler code back through the normal reader/resolution/type/effect/profile/capability/ownership/safety pipeline before it becomes usable. Emit source and generated-origin links binding generator, module/pass/compiler, inputs and produced artifacts; preserve the ordinary code/span diagnostic contract. Reject missing generated-source provenance or skipped ordinary checks, including a generated illegal effect/profile/type program. A generator success flag or artifact link cannot bypass the checkers. Retain BOOT3004 and demonstrate both accepted generated compiler source and rejected generated source with traceable origins. Required outputs: checked generated compiler artifact; generated-origin provenance chain; positive and negative re-entry fixtures and BOOT3004 diagnostic.

Bootstrap criterion bootstrap/module-interface: Produce and validate a manifest for every declared migrated compiler module: role, input/output schemas, permitted effects and capabilities, preserved/transformed facts, generated artifacts, diagnostics, tests/fixtures and unsafe islands when present. Bind actual source/module/pass/compiler/input hashes and produced outputs, module conformance and preservation reports; do not substitute a generic pass schema for an instantiated migration interface. Reject a missing or inconsistent module field, unversioned ownership/matrix update or a manifest whose actual pass input/output/preservation behavior disagrees with its declaration. Positive, negative and preservation fixtures must execute the declared module boundary; source-file presence and a manifest alone do not prove migration. Required outputs: instantiated module migration and compiler-module manifests; module conformance/diagnostic/preservation reports; field-mismatch and missing-interface negative fixtures.

Bootstrap criterion bootstrap/pass-determinism: Run each declared compiler transformation on identical pinned inputs and compare actual ordered outputs, diagnostics and generated artifacts. Produce a deterministic-output report identifying module/pass/compiler/input identities. This verifies transformation behavior itself, separately from a preservation-contract validator or a deterministic scheduler. A fixture whose actual pass output/order differs for identical declared inputs must fail with BOOT4002; undeclared time/random/environment influence cannot be hidden in the comparison. Preserve the source's fixed-input condition rather than claiming all differing inputs must produce the same output. Required outputs: actual repeated-pass output and order comparisons; deterministic output report; nondeterministic transformation rejection fixture with BOOT4002.
<!-- compiler-api-task-projection/pipeline:end -->

## Plugin Host: finite remaining tasks

<!-- compiler-api-task-projection/plugins:start -->
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

Foundation requirement gravity/extension-compatibility-evidence: For each extension kind/version and claimed compiler/API/profile combination, publish actual accepted and rejected conformance fixtures and a compatibility report bound to the loaded extension and its output verifier. Refusals: Reject incompatible API/compiler/profile combinations, a stale report, absent required negative fixtures or invalid extension output; one successful version cannot confer all-version support. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products.

Foundation requirement gravity/extension-manifest-contract: Publish and validate the extension-kind manifest contract for owner, kind/version, inputs/outputs, profiles, effects, capabilities, safety, artifact schemas, conformance fixtures, diagnostics and compatibility; validate a distributable instance against that contract. Refusals: Reject missing or inconsistent required fields, unknown kinds and unsupported versions; keep the kind schema distinct from producing an actual distributable instance. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): schema - Versioned source schema, generated validators/codecs and parity artifacts; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products.

Foundation requirement gravity/extension-not-language-authority: Run an allowed extension through the normal source/type/effect/profile/capability/safety and output-verifier boundaries, preserving the compiler-owned meaning of its source constructs. Refusals: Reject extension code or output that bypasses a required check, changes source meaning through a host convention, suppresses a blocking diagnostic or consumes unchecked compiler input. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products; pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts.

Foundation requirement gravity/extension-provider-lifecycle-contract: At each extension seam declare and enforce the applicable allocator/resource lifetime, capability attenuation/revocation, effect ordering, numeric/provider/checker assumptions and AI model/tool/memory/review policy; record provider initialization/use/cleanup and invalidation. Refusals: Reject a missing applicable lifecycle contract, stale/revoked authority, escaped allocation, reordered effect or mismatched checker/model policy before the affected operation; do not require irrelevant lifecycle fields on unrelated kinds. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; math - EFIR, numeric modes and independently checked math certificates; ai - Typed provider/model/tool/memory policy and schema decision artifacts.
<!-- compiler-api-task-projection/plugins:end -->

## Pass Evidence: finite remaining tasks

<!-- compiler-api-task-projection/pass-evidence:start -->
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

Foundation requirement gravity/checker-actually-verifies: Invoke the declared checker on the exact submitted proof/certificate and current claim inputs before emitting a checked result; retain checker implementation/version, invocation input/output identity and accepted or rejected verdict. Refusals: Reject an unchecked supplied success flag, wrong checker, malformed proof and a certificate whose checker fails; producing a proof record or schema alone must never create checked status. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products.

Foundation requirement gravity/evidence-artifact-provenance: Emit inspectable verification, audit, replay, differential and benchmark artifacts with their distinct evidence kinds, exact subjects, source/compiler/checker/provider inputs and result identities; consumers can traverse each claim to its evidence. Refusals: Reject a missing subject/input edge, stale or tampered evidence and an artifact relabelled as a stronger evidence kind; preserve failed and historical attempts at their original identities. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix - Locked source/compiler/provider/target build graph and canonical rebuild comparison.

Foundation requirement gravity/proof-artifact-claim-binding: Produce proof/certificate evidence naming the exact claim, domain/assumptions, subject inputs, checker/version and accepted or rejected result, and preserve those bindings at each consuming artifact. Refusals: Reject substituted subject/claim/domain/assumptions/checker, missing result or stale input identities; proof production does not itself establish actual checking. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): math - EFIR, numeric modes and independently checked math certificates; pipeline - Versioned stage contracts and actual compiler stage/fact/origin products.

Foundation requirement gravity/verification-evidence-kind: Label each claim's proof, checked certificate, test, audit, replay, differential or benchmark evidence explicitly, bind its subject and scope and make consumers enforce the required kind. Refusals: Reject a benchmark used as correctness proof, an unchecked certificate called checked, a replay log without actual replay or an audit label presented as executable conformance. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion.

Criterion `domain/mir-pass-integration`: Demonstrate the declared typed MIR pass with before/after semantic/provenance and rejected missing-fact/authority evidence. Accepted fixture: Execute the declared typed MIR optimization with pass I/O, preserved facts, invalidations, effects/grants and semantic/provenance comparison. Negative fixture: Reject metadata loss, missing required facts or ungranted transformation effects. Required evidence: before/after MIR inspector artifacts and differential/rejection fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume pipeline, opt/current-evidence-and-remaining-work/opt1-execute-pass-contracts outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Bootstrap criterion bootstrap/pass-fact-preservation: Run positive, negative and preservation fixtures through actual compiler passes, comparing the declared input/output facts and emitted preservation report. Preserve source spans, types, effects, capabilities and safety facts or explicitly declare and verify their transformations; generated artifacts retain source/module/pass/compiler/input provenance. Deliberately drop each preserved fact or perform an undeclared transformation and require BOOT4003; omit preservation tests and require BOOT4007. A pass metadata declaration or proof-link presence does not substitute for a real transformed output and executed preservation check. Keep unsafe internals isolated/audited and stable diagnostic codes/spans as their separate governing boundaries. Required outputs: before/after pass preservation report; executed positive/negative/preservation fixture records; dropped-fact and missing-test diagnostics BOOT4003/BOOT4007.
<!-- compiler-api-task-projection/pass-evidence:end -->

## Compiler development ownership and verification boundaries

The following new authority record closes the SH-01 task-traceability gap while
preserving its historically completed implementation. Current acceptance stays
unassessed. Edit only the Compiler API Spec Kit implementation ledger.

<!-- compiler-api-task-projection/development-boundaries:start -->
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
<!-- compiler-api-task-projection/development-boundaries:end -->

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


## Domain IR registration and transition boundaries

<!-- compiler-api-task-projection/domain-ir-boundaries:start -->
Owner role: compiler domain-IR interface maintainer. All tasks open; acceptance
requires actual registered entry, verifier and exit calls, not registry-shaped data.

| Task | Concrete closing evidence |
| --- | --- |
| domain-entry | Source compiler/c12/domain-entry: Build domain IR through declared entry passes recording consumed, preserved, invalidated and translated source/IR facts. Concrete acceptance: Mutate each consumed/preserved/invalidated/translated fact edge or remove its source/core/MIR anchor; the entry verifier must reject or regenerate the affected fact before the domain result is consumed. Retain actual input/output and diagnostic identities plus positive and rejected-case artifacts; supplied success flags, proposal delivery and broad parent links are not executable evidence. |
| domain-fallback | Source compiler/c12/domain-fallback: For unsupported targets expose semantically legal fallback, residual MIR or explicit rejection with diagnostic and eligibility records. Concrete acceptance: Select an unsupported target with no fallback, a fallback that violates profile/effect/safety semantics, and an undeclared lowering; require C12-FALLBACK or C12-LOWERING with eligibility records, never fabricated executable success. Retain actual input/output and diagnostic identities plus positive and rejected-case artifacts; supplied success flags, proposal delivery and broad parent links are not executable evidence. |
| domain-proof-boundary | Source compiler/c12/domain-proof-boundary: Expose optimized domain results only with proof/certificate/translation-validation or accepted nonsemantic evidence; reject benchmark/search/provider claims as sole correctness evidence. Concrete acceptance: Supply benchmark results, optimizer search output or a provider assertion in place of required checked proof/certificate/translation evidence; reject with C12-PROOF. Nonsemantic conformance acceptance must not authorize semantic change. Retain actual input/output and diagnostic identities plus positive and rejected-case artifacts; supplied success flags, proposal delivery and broad parent links are not executable evidence. |
| domain-registration | Source compiler/c12/domain-registration: Register stable package-visible domain interfaces with owning schema/verifier/passes/profiles/lowerings/proof/fallback/diagnostic contracts, including plugin-provided domains. Concrete acceptance: Require EFIR registration and verification, schema IR registration and verification, workflow IR registration and verification, and registration and verification for one identified target-oriented domain IR (HDL/state-machine IR). Retain each family's package-visible registration, semantic-anchor map, verifier identity, actual invocation input/output and verdict. Reject a missing required family, an unverified family, a failed verifier or an untargeted registration substituted for the target-oriented case; one successful family cannot complete this population. Consume math, schema, workflow and backend-hdl producer interfaces for their corresponding family fixtures, not whole-component completion. Try absent schema/verifier/owner/anchor/entry/exit/profile/target/proof/fallback/diagnostic fields, unstable or conflicting package-visible identity and unauthorized opaque plugin payloads; reject registration before target use. Retain actual input/output and diagnostic identities plus positive and rejected-case artifacts; supplied success flags, proposal delivery and broad parent links are not executable evidence. |
| registered-domain-exit | Source compiler/c12/registered-domain-exit: Permit domain exit/target consumption only via registered paths after verifier acceptance and accepted lowering/equivalence evidence. Concrete acceptance: Submit a failed verifier result, missing exact lowering/equivalence evidence and a path absent from the registration; reject target consumption before emitting an artifact. Retain actual input/output and diagnostic identities plus positive and rejected-case artifacts; supplied success flags, proposal delivery and broad parent links are not executable evidence. |

Yes/no acceptance: do registered entry/verifier/exit operations execute with
exact anchors and complete facts? Do all invalid registrations, missing proofs,
illegal exits and unavailable-target fallback cases reject as specified? Both
must be yes. Domain owners retain their payload schemas and semantics.
<!-- compiler-api-task-projection/domain-ir-boundaries:end -->
