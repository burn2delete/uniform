# Uniform component projects

Status: **component-roadmap navigation in use**; the finite Reader invocation
pilot is integrated. Component release acceptance remains separate.
Working names below are concrete descriptive defaults, not approved branding.
Original source-inspection baseline: main
`c21068a6d39259b7110845bbbcefaf9bf6901232`, inspected 2026-10-25.
Later accepted increments are linked explicitly below, not treated as a new
whole-repository audit.

## Purpose and authority

Organize real architectural components into finite, independently reviewable
projects that compose into the existing overall roadmap. [Reader](reader.md)
is the first fully worked example. Every component page owns its release
outcome, interface, finite remaining tasks, owner role and yes/no acceptance.
This index owns composition, not a duplicate task ledger. A page being written
is not a component being implemented or accepted.

The [canonical architecture](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md),
[release ordering](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md),
[overall implementation roadmap](../implementation-roadmap.md),
[full-language gap map](../full-language-implementation-gap-map.md), and
[existing milestone acceptance plan](../self-hosting-milestone-completion-plan.md)
retain their authority. Contract IDs and historical milestone IDs are
traceability metadata, not component boundaries or implementation names.

This is a successor decomposition to the already delivered complete roadmap,
not a claim that the earlier artifact was missing. Its preserved source package
and editorial provenance are recorded in [baseline and decisions](baseline.md).
No phase/task status is auto-closed or overwritten by navigation updates. Deus is
sole review/integration coordinator; existing code owners keep their paths.
Unassigned owners on component pages are explicit assignment decisions, not
permission for a new writer to take reserved files.

## Component registry

Each name links to the sole canonical outcome and task list for that project.
Component release outcomes remain open until a linked receipt accepts the
complete declared outcome; an accepted finite increment is not that receipt.
Written plans are not completed implementation, and accepted finite increments
do not close larger release outcomes. Components are semantic boundaries, not
one project per file, numbered contract, test, or historical task.

| Working name / semantic key | Purpose and boundary | Canonical outcome |
| --- | --- | --- |
| Compiler API / `compiler-api` | Public compiler artifacts, pass pipeline and scoped plugin execution | [Compiler API outcomes](compiler-api.md#release-outcome) |
| Reader / `reader` | Bytes, lexical products, source identity and syntax seeds | [Reader outcome](reader.md#release-outcome) |
| Syntax / `syntax` | Syntax objects, context, hygiene/origin representation | [Syntax outcome](syntax.md#release-outcome) |
| Expand / `expand` | Macro expansion under declared compile-time authority | [Expand outcome](expand.md#release-outcome) |
| Resolve / `resolve` | Namespace/import and binding identity | [Resolve outcome](resolve.md#release-outcome) |
| Core / `core` | Canonical core forms, executable semantic lowering | [Core outcome](core.md#release-outcome) |
| Types / `types` | Type inference/checking and typed-core facts | [Types outcome](types.md#release-outcome) |
| Effects / `effects` | Direct/transitive effects and declarations | [Effects outcome](effects.md#release-outcome) |
| Legality / `legality` | Profile, ownership/lifetime and safe-code checks | [Legality outcome](legality.md#release-outcome) |
| Auth / `auth` | Capability grants and artifact authentication boundaries | [Auth outcome](auth.md#release-outcome) |
| MIR / `mir` | Verified target-independent IR | [MIR outcome](mir.md#release-outcome) |
| Opt / `opt` | Proof-preserving required transformations | [Opt outcome](opt.md#release-outcome) |
| Lower / `lower` | Target/ABI/runtime-service lowering | [Lower outcome](lower.md#release-outcome) |
| Emit / `emit` | Target bytes and provenance-bearing artifacts | [Emit outcome](emit.md#release-outcome) |
| Backends / `backends` | Independently verified target-family adapters and artifacts | [Backend family outcomes](backends.md#release-outcome) |
| Runtime / `runtime` | Declared native/managed execution services | [Runtime outcome](runtime.md#release-outcome) |
| Standard Library / `stdlib` | Profile/effect-declared user and compiler library | [Standard Library outcome](stdlib.md#release-outcome) |
| Build / `build` | Package resolution, incremental/hermetic build graph | [Build outcome](build.md#release-outcome) |
| CLI / `cli` | Public commands and developer-tool interfaces | [CLI outcome](cli.md#release-outcome) |
| Developer Tools / `tools` | Formatter, editor, debugger, REPL and inspection services | [Developer Tools outcomes](tools.md#release-outcome) |
| Diagnostics / `diag` | Common stable failure records and rendering | [Diagnostics outcome](diag.md#release-outcome) |
| Domains / `domains` | Registry of separately named domain artifact projects | [Domain outcomes](domains.md#release-outcome) |
| Platform / `platform` | Separate full hardware/firmware/OS/device extension | [Platform outcome](platform.md#release-outcome) |
| Release / `release` | Whole compiler rebuild, equivalence and seedless distribution | [Release outcome](release.md#release-outcome) |
| Governance and Evolution / `evolution` | Subject-bound governance artifact producers and policy decisions | [Governance outcome](evolution.md#release-outcome) |

Domains is a portfolio, not a new monolithic compiler pass; its named project
outcomes must retain independent completion evidence. Platform product choices
are not silently folded into the compiler's currently selected release target.
Runtime families and target lowerers can likewise land independently without
claiming untested families or all targets.

## Integration outcomes

This rollup refers to component outcomes, never reproduces their tasks. Each
integration requires actual positive and negative composition, provenance and
an exact receipt, not merely all individual pages having checkmarks.

| Integrated outcome | Required component outcomes / interface | Evidence finish line |
| --- | --- | --- |
| Source-to-syntax | [Reader](reader.md#release-outcome), [Syntax](syntax.md#release-outcome), diagnostic and authentication interfaces | Fresh source bytes become faithful syntax objects; malformed/tampered inputs reject with original-source spans. |
| Source-to-Core | Source-to-syntax plus [Expand](expand.md#release-outcome), [Resolve](resolve.md#release-outcome), [Core](core.md#release-outcome) | Actual required source population reaches fresh verified canonical Core with preserved provenance and illegal-form rejection. A six-file first endpoint is not the whole SH-07 population. |
| Checked compiler IR | Source-to-Core plus [Types](types.md#release-outcome), [Effects](effects.md#release-outcome), [Legality](legality.md#release-outcome), [Auth](auth.md#release-outcome), [MIR](mir.md#release-outcome) | Legal source produces verified MIR carrying all required facts; illegal type/effect/profile/capability/ownership/safety cases reject before lowering. |
| Runnable selected target | Checked IR plus [Opt](opt.md#release-outcome), [Lower](lower.md#release-outcome), [Emit](emit.md#release-outcome), [selected compiler runtime services](runtime.md#selected-compiler-runtime-outcome), [compiler-support library](stdlib.md#compiler-support-outcome) | Selected-target artifacts actually execute accepted programs; unchecked IR and undeclared runtime service use reject. Target and residual host boundary are explicit. |
| Reproducible compiler product | Runnable target plus [Build](build.md#release-outcome), [selected compiler commands](cli.md#selected-compiler-cli-outcome), [Diagnostics](diag.md#release-outcome), [Release](release.md#release-outcome) | Complete compiler source closure, actual next-stage rebuild/equivalence, hermetic package/provenance and public seedless release satisfy existing gates. |
| Wider language/platform | Relevant [domain outcomes](domains.md#release-outcome) and separately decided [Platform](platform.md#release-outcome) | Each declared domain/profile/target demonstrates its own executable accepted/rejected surface. The product does not inherit universal hardware/OS support from the compiler release. |

## Accepted scoped increments

The [Reader-to-Core invocation pilot](../../.specify/reader-core-invocation/spec.md)
completed specification, implementation, independent review and integration.
Its sole finite [task ledger](../../.specify/reader-core-invocation/tasks.md) and
[verified evidence](../../.specify/reader-core-invocation/evidence.md#verified-integration-receipt)
record the accepted scope: current development source-to-Core invocation plus
minimal existing-consumer compatibility. Implementation
[PR #388](https://github.com/burn2delete/uniform/pull/388) merged at
`001ab9ed`; the completed pilot documentation merged in
[PR #381](https://github.com/burn2delete/uniform/pull/381).

Reader remains partial, and the full source-to-Core/SH-07 source population and
component release outcomes above remain open. This receipt supplies neither a
whole-component percentage nor a release acceptance. The older 30-milestone
rollup preserved in [baseline and decisions](baseline.md#source-baseline) remains
historical context, not a replacement for component pages and scoped receipts.

## Dependency and parallel-interface view

```text
explicit source and policy records
  -> Reader -> Syntax -> Expand
  -> Core surface + Resolve binding interface -> canonical resolved Core
  -> Types -> Effects -> Legality + Auth facts -> MIR
  -> domain-specific IR where applicable -> Opt -> Lower -> Emit
                                            Runtime + Standard Library
Build consumes these components and supplies explicit source/policy/lock inputs
CLI consumes compiler/build/runtime results; Diagnostics spans every boundary
Release consumes actual complete build, execution, equivalence and provenance
Platform extension consumes declared target/domain/runtime interfaces separately
```

This is a dependency summary, not permission to rewrite D1's canonical pipeline.
Core construction and resolution expose their specified intermediate contracts;
a fused implementation must retain the same observable facts and diagnostics.
Domain IR placement follows its governing contract. Capability/profile checks
cannot move after lowering, and runtime cannot legalize rejected source.

**Prerequisite is not completion dependency.** Reader starts with an explicit
source/policy record and existing seed, not the later Build implementation.
Compiler components can develop against pinned producer/consumer fixtures.
Build and CLI can develop against declared compiler interfaces concurrently.
Actual composition and release must replace mocks and historical receipts with
fresh evidence. Compiler self-compilation is a later rebuild of a working
compiler, not a circular prerequisite for implementing its Reader.

Parallel work is safe when file ownership and interface versions are compatible.
No project-cost, work-unit, task-count, function-count, source-capacity or
failed-attempt quota is introduced. Independently meaningful semantic bounds
and real host safeguards are not renamed or removed by this statement.

## Traceability, not project names

| Existing milestone/contracts | Component outcomes they inform |
| --- | --- |
| SH-00, SH-01, SH-02 | Release baseline, owner-safe component interfaces, Auth |
| SH-03, SH-04; L1, C2, C3 | Reader, Syntax |
| SH-05, SH-06, SH-07; C4, C5, C6 | Expand, Resolve, Core; Reader source closure contributes but does not complete SH-07 |
| SH-08 through SH-11; C7 through C10 | Types, Effects, Auth, Legality |
| SH-12 through SH-14; C11 | MIR, Core, Lower and Runtime semantic/layout interfaces |
| SH-15 through SH-20; C13 through C15, backend/runtime contracts | Diagnostics, Opt, Lower, Emit, Runtime |
| SH-21 through SH-24; C16 through C18, package/tooling/library contracts | [Compiler API pipeline/plugin/evidence](compiler-api.md#release-outcome), Legality, Build, CLI, Standard Library and Release verification |
| SH-25 through SH-29; bootstrap and phase-18 release contracts | Release composes all required compiler components, not a frontend-only census |
| B1 through B14; phase-07 backend inventory | [Named backend-family and interface/conformance outcomes](backends.md#release-outcome), with selected Linux LLVM consumed by Lower/Emit |
| Domain, schema, math, AI and platform contracts | Domains' individual projects and separate Platform extension |

Implementation filenames, namespaces and new APIs use real semantic subsystem
names such as `reader`, `syntax`, `expand`, `resolve`, `core`, `types`, `effects`,
`mir`, `opt`, `lower`, `emit`, `runtime`, `stdlib`, `build`, `cli`, `diag`, `auth`.
Do not introduce SH07/C2/C16 prefixes or arbitrary codename API prefixes. Existing
contract IDs, protocol schemas, diagnostic IDs and wire IDs do not silently
change; owner-scoped migrations must account for compatibility and provenance.

## Spec Kit use and maintenance

Use [current Spec Kit integration](../../.specify/README.md) for an explicitly
selected implementation increment. Its spec links the component outcome and
owning canonical contracts; its plan names owned paths/interfaces; its tasks
cover only that increment; its evidence links exact results back to the outcome.
Do not fork the normative specs or duplicate this component's release task list
in an overall rollup. The completed [pilot tasks](../../.specify/reader-core-invocation/tasks.md)
remain the sole task ledger for that selected increment; this index links them
rather than copying their checklist.

A status update needs: exact source/candidate identity, what actually ran,
accepted/rejected evidence, remaining boundaries and reviewer disposition.
Source inspection, tests, composed integration, migration admission and release
are separate evidence classes. A changed consumer or source invalidates only
the evidence it affects, never silently promotes an old receipt to a new SHA.

The merged index and Spec Kit guidance support the completed finite pilot.
Further component-boundary, ownership and outcome changes remain subject to
review by Deus with relevant maintainers. Keep wire identifiers stable; working
names do not establish approved branding or component acceptance. Naming
preference is not a reason to stop independent implementation.

### Domain project navigation

These are independent named outcomes within the domain portfolio, not unnamed
future tasks: [Math](domains.md#math), [Schema](domains.md#schema),
[Query](domains.md#query), [Workflow](domains.md#workflow), [AI](domains.md#ai),
[GPU](domains.md#gpu), and [UI](domains.md#ui). Each section owns its finite work,
interfaces, specialist role, decision recommendation and acceptance. Splitting
one into a separate page later is navigation only, not a new implementation
component or a reason to duplicate its task list.

Developer-service outcomes are owned by [Developer Tools](tools.md#release-outcome),
not by CLI argument routing. Wider-language integration includes those named
service outcomes; a selected compiler release consumes only services actually
required by its declared public surface. Completion of every editor/profiler
service is not an added prerequisite for basic compiler development.
See [original roadmap validation](validation.md) for the baseline documentation
checks; later navigation updates retain their own PR validation receipts.

### Compiler execution and target-family integration

[Compiler API](compiler-api.md#release-outcome) owns actual pass registration,
compiler invocation, compatibility and isolated extension execution; Auth's
grants and Build's graph/cache records do not implement those operations.
The selected compiler consumes its built-in pipeline outcome and any extension
it actually requires; optional plugin families remain separate outcomes.

[Backends](backends.md#release-outcome) owns named target-family outcomes beyond
the selected Linux LLVM path. Lower/Emit describe that selected compiler path
and common lowering/emission handoffs; they do not claim all backend families.
Wider C, Wasm, JVM, JavaScript/TypeScript, MLIR, GPU, HDL, Workflow, AI artifacts, Mobile
and Query/Relational outcomes are independently owned and evidenced. Their
completion is not an
added prerequisite for the preserved selected bootstrap target.

Source-to-Core, checked-IR and runnable-target rollups each require the
[actual built-in compiler pipeline](compiler-api.md#built-in-compiler-pipeline-outcome)
for their stated endpoint, with [Pass Evidence](compiler-api.md#pass-evidence-finite-remaining-tasks)
for the passes claimed. This is real invocation/registration/verifier work,
not a metadata-only orchestration record. Only extension code actually loaded
by that closure adds [Plugin Host](compiler-api.md#plugin-host-finite-remaining-tasks)
as a selected-release prerequisite; full plugin support remains a separate
wider-language outcome.

## Cleanup completion boundaries

This existing index is the sole shared navigation for the six cleanup outcomes.
The detailed contracts below own their own finite IDs and acceptance; this table
does not duplicate their task status or the component ledgers. A merged planning
document is not implementation completion. Each outcome remains accountable for
its entire declared backlog, actual evidence, independent review and integration.

| Outcome / stable IDs | Accountable outcome role | Sole detailed backlog and acceptance contract |
| --- | --- | --- |
| Accounting removal / ACC-01 through ACC-13 | Accounting removal maintainer, after existing source-writer handoff | [Accounting tasks](../../specs/accounting-removal/tasks.md) and [acceptance contract](../../specs/accounting-removal/spec.md), integrated with the finite runtime increment in [#433](https://github.com/burn2delete/uniform/pull/433). The full accounting outcome remains open. Existing source-writer reservations and independently required guard classifications remain in force. |
| Verification reuse / RV1, RV2, RV2a, RV3, RV4 | Verification reuse maintainer | [Detailed backlog and acceptance](../verification-reuse.md); [#432](https://github.com/burn2delete/uniform/pull/432) integrated finite Reader identity reuse and catalog correction. The broader reuse outcome remains open, and real fresh verification remains required. |
| Coordination mechanics / CO1 through CO4 | Result handoff maintainer | [Detailed backlog and acceptance](../result-handoff.md#finite-audited-backlog); [#428](https://github.com/burn2delete/uniform/pull/428) integrated the finite handoff tooling. Extraction does not grant acceptance or retry authority. |
| End-to-end ownership / OWN-01 through OWN-06 | Cross-component ownership maintainer | [Interface ownership contract](../end-to-end-contracts.md) owns exact producer/consumer joins and unresolved ownership; [#429](https://github.com/burn2delete/uniform/pull/429) integrated the documentation only. No duplicate compiler implementation checklist. |
| Assurance separation / AS1 through AS4 | Assurance boundary maintainer | [Assurance boundaries](../assurance-boundaries.md) owns detailed acceptance; [#431](https://github.com/burn2delete/uniform/pull/431) integrated the finite runner correction. Construction, independent validation, resource/capture containment and actual admission remain distinct responsibilities. |
| Finite dependencies and closure / FIN-01 through FIN-05 | Dependency and completion-boundary maintainer | [Finite acceptance dependencies](../acceptance-dependencies.md#finite-fin-backlog), with [staged full-language gates](../full-language-implementation-gap-map.md#dependency-stages-and-finite-acceptance). |

Unpublished locators above are explicit owner reservations, not claims that the
files exist on main. Replace them with their accepted public links when the
same owner publishes; do not recreate or take over that work. A pending or denied
publication remains a blocker for that owner's deliverable. No actor or route
substitution is authorized by this navigation.

The overall cleanup closes only when all six detailed outcome contracts are
satisfied and integrated with their tested boundaries. A role assignment alone
does not satisfy a missing implementation owner or producer. The dependency
owner audits finite/circular definitions; the ownership owner resolves actual
source responsibility. Neither may discard hard remaining work, sum overlapping
views, reopen unchanged accepted evidence, or infer completion from tests/PR
counts. The 240 language contracts and the separate full-language and release
gates remain in force. C12 execution remains restricted; the unresolved original
C7 publication operation is not retried or transferred by these contracts.

The supporting task-authority migration has a fixed population: the registered
component pages and all their existing named finite task sections, including
Compiler API suboutcomes, backend families, tools, domains and selected/wider
runtime/library outcomes. Its existing owner continues the transfers and
links. The [finite migration stopping rule](../acceptance-dependencies.md#finite-task-authority-migration-population)
requires one editable task source with direct roadmap links for every section,
then stops. It adds no endless successor requirement and
cannot close the implementation those rows describe.
