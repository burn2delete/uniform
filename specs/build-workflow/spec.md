# Feature specification: Build workflow migration

Status: prepared; review and integration pending.
Migration owner: backend workflow migration owner. Component owner role: build/package maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Adopt the repository's Spec Kit artifact workflow for the Build component
without copying its normative semantics or creating a second implementation ledger.
[Component release outcome](../../docs/component-roadmaps/build.md#release-outcome) and [Build canonical remaining work](../../docs/component-roadmaps/build.md#remaining-tasks-and-evidence) retain outcome and acceptance scope; implementation records are edited only in [tasks.md](tasks.md#implementation-tasks), with direct roadmap links to the task ledger.
The following contracts remain canonical and are linked, not replaced:

- [PKG1 - Project File Specification](../../docs/phase-12-build-package-and-artifact-system/165-pkg1-project-file-specification.md)
- [PKG2 - Build System Architecture](../../docs/phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md)
- [PKG3 - Artifact Model Specification](../../docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md)
- [PKG4 - Package Manager Specification](../../docs/phase-12-build-package-and-artifact-system/168-pkg4-package-manager-specification.md)
- [PKG5 - Dependency Resolution Specification](../../docs/phase-12-build-package-and-artifact-system/169-pkg5-dependency-resolution-specification.md)
- [PKG6 - Capability and Permission Manifest Specification](../../docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md)
- [PKG7 - Reproducible Build Specification](../../docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md)
- [PKG8 - Package Safety and Audit Metadata Specification](../../docs/phase-12-build-package-and-artifact-system/172-pkg8-package-safety-and-audit-metadata-specification.md)
- [PKG9 - Private Registry and Latent Package Space Design](../../docs/phase-12-build-package-and-artifact-system/173-pkg9-private-registry-and-latent-package-space-design.md)
- [PKG10 - Supply-Chain Security and Provenance Specification](../../docs/phase-12-build-package-and-artifact-system/174-pkg10-supply-chain-security-and-provenance-specification.md)
- [PKG11 - Cross-Compilation and Target Matrix Specification](../../docs/phase-12-build-package-and-artifact-system/175-pkg11-cross-compilation-and-target-matrix-specification.md)
- [PKG12 - Artifact Signing, Verification and SBOM Specification](../../docs/phase-12-build-package-and-artifact-system/176-pkg12-artifact-signing-verification-and-sbom-specification.md)

## User scenarios and acceptance

### US1 - Find one authoritative path from work to evidence

A maintainer starts here, follows plan.md to the existing remaining tasks and
acceptance criteria, and records the exact implementation evidence there.
Acceptance: all four package artifacts exist, every local link and anchor
resolves, ownership/dependencies are explicit, and no implementation task is
duplicated or marked complete merely by this migration. A missing contract,
broken task link or unsupported completion claim rejects package acceptance.

### US2 - Preserve the component boundary

Manifest, dependency and graph work can proceed against recorded interfaces. Existing seed/toolchain support remains explicit; a future self-hosted Build is not required before earlier compiler components can develop. Package transport and graph execution remain separate.
Acceptance: review confirms these boundaries agree with the linked roadmap.
Changing a target, grant, semantic contract or release claim requires its own
implementation decision and evidence, not a checkbox in this package.

## Scope and compatibility

Owned files are `specs/build-workflow/{spec,plan,tasks,evidence}.md` and
the task link in `docs/component-roadmaps/build.md`. Existing contracts, phase tasks,
other packages, compiler/runtime source, shared helpers and product behavior
remain unchanged.
This is artifact workflow migration, not component implementation, wholesale
contract replacement, external Specify CLI execution or automatic skill discovery.

## Evidence and outcome ownership

The migration owner prepares and checks artifacts. The component role retains
semantic ownership; this package does not assign an individual implementation
owner. An independent reviewer accepts exact package evidence; Deus owns
integration. See [evidence](evidence.md) for prepared, checked, reviewed and
integrated states, kept separate from component acceptance.

## Unresolved items

Independent package review and main integration remain pending. Existing
component decisions and open implementation evidence remain in the canonical
roadmap; no new generic approval gate is introduced.

## Implementation naming review

`build-workflow` identifies the subsystem and purpose. No new implementation
names or protocol identifiers are introduced. Follow the
[semantic naming rule](../../.specify/semantic-names.md); legacy planning IDs
remain traceability metadata only.
