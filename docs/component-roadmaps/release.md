# Release

Working name: **Release** (proposed). Semantic key: `release`.
Owner role: bootstrap/release maintainer; current implementation owner is not
reassigned here. Coordinator retains integration disposition. Baseline:
`c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit ledger](../../specs/release-workflow/tasks.md#implementation-tasks).
Edit the ledger and run `clojure -M tools/release_task_projection.clj --write`;
the remaining-task table below is generated.

## Purpose and scope

Assemble component evidence into a reproducible public compiler distribution
with honest seed and trust boundaries. Governing contracts:
[bootstrap](../phase-15-bootstrap-and-self-hosting/README.md) and
[binary release](../phase-18-binary-distribution-and-seedless-release/README.md).
No individual component fixture or source-file census grants seed-retirement,
full-language or whole-platform authority.

## Release outcome

The self-hosted public binary checks, runs and compiles the declared language
surface, accepts both canonical source extensions with truthful provenance,
rejects invalid programs with stable diagnostics, and carries reproducibility,
provenance, signing/SBOM and equivalence evidence with no Clojure seed in the
claimed release compiler/runtime path. Outcome open.

## Interfaces and dependencies

Inputs: accepted component outcomes, stage compatibility records, compiler/source
identities, exact build recipe, test/conformance artifacts, signing policy and
custody records. Outputs: executable distribution, source/artifact hashes,
equivalence report, trust-boundary inventory and release attestation.
Prerequisites for release admission: compiler, Runtime, Standard Library, Build,
CLI, Auth and domain outcomes for the claimed surface. These are not
prerequisites for ordinary component development with the existing seed.
Packaging and evidence schemas can develop in parallel against pinned fixtures.

## Remaining tasks and evidence

<!-- release-task-projection:start -->
| Task (all open) | Evidence baseline | Closing evidence |
| --- | --- | --- |
| Bind each claimed component to exact implementation and conformance | [Overall roadmap](../implementation-roadmap.md) | Component outcome receipts, positive/negative programs and explicit unsupported surface. Maintain the source-bound task/disposition matrix for all 240 original contracts and the separately declared release and Platform Product extensions. Keep every missing producer/checker/consumer interface, alias or split question, and architecture choice as finite named remaining work with criteria, dependencies and accountable role; an accountability link does not establish an implementation interface. Resolve those decisions and bind actual component evidence before claiming the corresponding release scope. This release-claim obligation does not block ordinary semantic development. |
| Complete stage compatibility/equivalence across the claimed compiler | [Equivalence contract](../phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md) | Fresh stage-to-stage outputs and diagnostics, residual trusted-code inventory BOOT1/BOOT3/BOOT5/BOOT7: Publish a versioned row for every claimed stage and authoritative module revision, naming source/compiler/artifact identities, supported and unsupported documents/features/profiles/backends/runtime/package/library/test closure, owned or deferred gaps, conformance links and release eligibility. Record exact compiler-module ownership and residual TCB with measurable migration delta. Compare both named compilers and canonical artifacts under declared applicable hash/manifest/diagnostic/conformance/runtime/IR/performance/accepted-delta modes; preserve stable codes/spans and provenance. Reject missing outputs, implicit inherited support, omitted stage rows, unsupported claims, stale matrix versions and unexplained drift; accept deltas only with policy-linked review. Performance bounds apply only when performance is part of acceptance; no compiler work quota is introduced. Execute the intermediate authoritative stage/module admission decision against this exact candidate and declared subset, consuming the fresh conformance, equivalence/accepted-delta, matrix, provenance, TCB and applicable audit records. Record admit or deny with candidate/source/compiler/artifact identities and failed predicates. Deny failing applicable suites, stale or missing required evidence, unexplained drift, unsupported claimed support and missing ownership/matrix updates. Exercise both a valid intermediate admission and each refusal. Retain explicitly owned/deferred gaps outside the claimed subset; final release policy separately rejects unreviewed gaps for claimed release support. This decision does not produce the evidence it consumes, replace final release acceptance, or gate ordinary semantic development. |
| Produce actual self-hosted compiler/runtime execution | [Binary release status](../phase-18-binary-distribution-and-seedless-release/README.md) | Actually invoke the prior Gravity compiler process on the complete declared next-stage compiler/runtime/library/build closure, retain real outputs and logs/lineage, link/package and execute that next compiler on accepted and rejected compiler inputs without Clojure in candidate compilation/runtime/build; then public check/run/compile and output executable demonstrate absent seed boundaries. SH25 component construction, SH27 equivalence and final release acceptance remain distinct; supplied rebuild records or plans do not prove execution. |
| Build reproducible signed distributions with provenance/SBOM | [Artifact provenance](../phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md) | Rebuild identity and signature/provenance verification plus tamper rejection BOOT1/BOOT6/BOOT8: Retain locked dependencies, recipe and environment controls for time/locale/filesystem order/toolchain/network, compiler lineage and manifest/content-hash comparisons. Declare unsupported rebuild strategies; diverse rebuilds require independent toolchain identity. Reproducibility exceptions and accepted deltas require recorded review. Produce canonical signed provenance binding stage/artifact/source graph/compiler/lockfile/recipe/environment/dependencies/builder and conformance/equivalence/safety/SBOM/signature links. Auditor queries must traverse release compiler to seed inputs; permit only explicitly declared stage cycles. Reject lineage gaps, undeclared cycles, uncontrolled network inputs, noncanonical signing, missing required evidence, unexplained drift and revoked compiler/builder/dependency/signature inputs. |
| Obtain independent acceptance for the exact release candidate | [Binary release gates](../phase-18-binary-distribution-and-seedless-release/IMPLEMENTATION-ROADMAP.md) | Coordinator-reviewed candidate receipts and explicit release disposition BOOT1/BOOT3/BOOT5/BOOT6/BOOT8: For this exact release candidate, consume fresh source-bound claimed-stage/module conformance, equivalence, compatibility, provenance, TCB and applicable unsafe-audit evidence. Reject failing declared conformance, missing or stale evidence, unreviewed claimed-support gaps and revoked inputs. Preserve policy-qualified reproducibility exceptions. Intermediate stage/module authoritative admission is the separate consumer proposed under release task2 (complete-stage-compatibilityequivalence-across-the-claimed-compiler); ordinary development and final release acceptance remain separate from that migration decision. The task2 interface and executable evidence remain Open until canonically composed and independently accepted. This release gate does not implement compiler passes, native seed ABIs or their tests. |
<!-- release-task-projection:end -->

The baseline release index states the current wrapper is bootstrap-hosted and
final release incomplete. Historical bounded phase completion remains historical
bounded completion. No restricted action, stopped coordinator, authority proof
retry or migration run is authorized by this roadmap. Architecture decision
owner: release maintainer with coordinator review. Recommendation: keep recovery
seed tooling explicitly separate from the user-facing seedless distribution.

## Acceptance and integration

Yes/no: do required public commands and produced programs execute? Are source
extensions preserved? Do invalid programs fail with intended diagnostics? Do
repeat builds and stage equivalence agree? Is the claimed release path free of
seed fallback? Is independent acceptance attached to the exact candidate? All
must be yes. Integration milestones: honest bootstrap distribution; component
self-hosting composition; seedless candidate; independently accepted release.
No automatic status closure follows from documentation or test counts.

For the selected compiler release, Runtime and Standard Library prerequisites
mean their [compiler-service](runtime.md#selected-compiler-runtime-outcome) and
[compiler-support](stdlib.md#compiler-support-outcome) outcomes, with exact
source/service/export closure. Full runtime-family, general library and platform
outcomes remain wider-language/product releases, not prerequisites silently
added to compiler seed retirement. Domain outcomes are required only where the
declared compiler/release surface actually consumes them.

The CLI prerequisite is [selected compiler commands](cli.md#selected-compiler-cli-outcome),
not the wider CLI portfolio. It includes the actual public compiler, build,
package, test, audit, verification and explanation operations required by the
release recipe. Unrelated editor/AI/registry services remain wider outcomes;
commands actually invoked by the compiler recipe cannot be waived by slicing.
