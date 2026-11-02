# Build

Working name: **Build** (proposed). Semantic key: `build`.
Owner role: build/package maintainer; current owner unassigned by this proposal.
Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit ledger](../../specs/build-workflow/tasks.md#implementation-tasks).

## Purpose and scope

Turn declared projects and dependencies into reproducible typed artifacts.
Own project/lockfile normalization, dependency resolution, graph execution,
incremental cache policy, package provenance and publication verification.
Governed by [build/package contracts](../phase-12-build-package-and-artifact-system/README.md).
Not a source semantics owner, authority grantor or implicit shell convention.

## Release outcome

A real clean and incremental build resolves a locked dependency graph, executes
only declared effects, produces the selected target artifacts and their
provenance/SBOM/signature records, and rejects incompatible dependencies,
unauthorized effects and invalid cache hits. Outcome open.

## Interfaces and dependencies

Inputs: project manifest, lockfile, source graph, compiler identity, target
matrix, policy, environment and explicit grants. Outputs: build graph/node logs,
cache decisions, artifacts, package records and reproducibility evidence.
Actual compiler invocation requires the relevant component outputs; manifest,
resolver and graph work can proceed against recorded fixture interfaces.
Existing seed/toolchain can build early components. A future self-hosted Build
is not a prerequisite for Reader or any earlier component's development.

## Remaining tasks and evidence

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/build-workflow/tasks.md#implementation-tasks).

The inspected package report labels its gate `:metadata-gate-only`; its nested
complete statuses do not establish a functioning resolver/build/package system.
Architecture decision owner: build maintainer. Recommendation: keep graph
execution independent of package transport, with locked provider interfaces;
record adopted transport/cache policy before package publication support.

## Acceptance and integration

Yes/no: do clean and incremental outputs agree? Are cache invalidations complete?
Does every node have input/output/effect provenance? Are unauthorized effects
rejected before execution? Are signatures verified against explicit policy?
All must be yes. Integration: compiler artifact build, locked package build,
then Release consumption. No cost model, project budget or input quota is an
admission requirement.
