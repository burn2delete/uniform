# Release

Working name: **Release** (proposed). Semantic key: `release`.
Owner role: bootstrap/release maintainer; current implementation owner is not
reassigned here. Coordinator retains integration disposition. Baseline:
`c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit ledger](../../specs/release-workflow/tasks.md#implementation-tasks).

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

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/release-workflow/tasks.md#implementation-tasks).

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
