# Build and package implementation specification

Implementation scope added under the 2026-11-01 request. Existing build/package,
trust, policy and release owners retain their boundaries. Historical workflow
migration below establishes no resolver, graph execution or package acceptance.

## Behavior and interfaces

Implement [the Build outcome](../../docs/component-roadmaps/build.md#release-outcome)
under the complete PKG1-PKG12 contracts retained below, with actual Compiler
API/Emit/Runtime, Auth, source-schema and package/governance interfaces.
Non-executing project normalization, exact source/lock/dependency graph,
profile/target/features, policy/grants, compiler/toolchain and controlled
environment inputs produce real selected dependencies and typed graph
execution, generated-and-rechecked source, per-target artifacts, cache decisions,
package operation/diff records, reproducible recipes and required verified
provenance/safety/SBOM/signing evidence.

Project requests and dependency selection never grant authority. Package
transport/installation and compiler graph execution remain separate operations.
Auth checks actual grants and trust; Build carries their inputs and enforces
the selected policy at real fetch/install/action/consumer boundaries. Registry
publication, signing custody and language/package policy decisions retain their
existing authority and restrictions.

## Existing implementation routes and acceptance

Use [the sole marked ledger](tasks.md#implementation-tasks) under
`build/remaining-tasks-and-evidence/`, preserving its exact criteria/status,
source-family refinements, yank-policy amendment and receipts.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `implement-projectlock-normalization-and-constraint-resolution` | Read project data without executing it; normalize explicit package/source/entry/profile/target/effect/capability/artifact/policy inputs. Deterministically solve actual versions/features/variants with complete transitive locked metadata, explicit registry sources/tie-breakers and actionable conflicts; support genuinely complete offline inputs. | Reject effectful config, invalid profile/target/entry/source roots, incomplete locks/cache, denied authority, missing target variants and ungranted private/latent space. Apply actual yank/revocation/historical-rebuild policy with retained signature/status/history and PKG5006; a resolver result cannot mint grants. |
| `execute-typed-graph-under-declared-build-effects` | Actually execute declared nodes and capture input/output/tool/effect/diagnostic/provenance edges, generated-source normal-pipeline re-entry and required test/artifact production. Package operations expose structured reviewable policy-relevant diffs before mutation. | Reject hidden shell/network/environment access, missing provenance/evidence and unmarked graph cycles. A declared fixed-point bootstrap cycle must use its existing stage contract, not silently admit arbitrary cycles. Real failure identifies the node/input edge and stops affected production. |
| `implement-dependency-aware-caching-and-invalidation` | Reuse stage/node artifacts only under actual matching source/manifest/lock/compiler/pass/plugin/profile/target/provider/grant/policy/environment/replay/proof inputs and producing contracts. Compare clean/reused semantics and diagnostics. | Changed inputs revalidate/rebuild affected consumers; reject stale safety/proofs/diagnostics, incomplete keys, timestamp-only reuse and speculative release output. Trace actual dependencies rather than accepting supplied reuse flags. |
| `produce-and-verify-packageprovenancesigning-records` | Exercise actual package fetch/verify/install/update/audit/publish/yank/mirror/vendor behavior within authorized scope, retaining content/signature verification, registry source, capability/safety/license/target/provenance diffs and review states. Bind actual content to canonical manifests, complete SBOM and subject-bound attestation/signing evidence. | Reject tamper, wrong subject/source/policy, missing summaries/evidence, quarantined/revoked required inputs, secret leaks and policy-incompatible publication. Private metadata requires authorization before disclosure; latent nonpublished candidates do not satisfy ordinary dependencies. Mirrors preserve verified signatures or actual attestations, and yanks preserve rebuild history. |
| `close-reproducibility-and-target-matrix` | Execute repeated locked builds under the same controlled recipe; compare actual canonical manifests/content and declared output identities. Normalize each concrete profile/backend/target/ABI/runtime/feature/support tuple with its dependency variants, grants, artifacts and conformance. | Reject unrecorded randomness/network/environment/path/time influences, missing generator inputs, implicit host ABI/target, hidden target failures and undeclared fallback. Nonreproducible or unavailable targets stay explicitly classified; passing one target cannot satisfy another. |

PKG8 safety/wrapper/FFI/taint/proof/review metadata and PKG10/12 provenance,
builder/identity/log/root/revocation and consumer policy are actual verification
inputs. Required unknown schemas fail closed, and weaker attestations cannot
downgrade the required level/track. Emit supplies real bytes and common artifact
evidence; Build packages and consumes them without treating a manifest as
execution or replacing the owning semantic checker.

## Starting work and completion

Explicit manifest/graph/compiler/provider interfaces and pinned fixtures allow
local resolver or graph development before a future self-hosted Build exists.
Real composition needs actual services, fetch/action boundaries and artifact
consumers for each claim. Completion requires real resolution/graph/package
operations, complete positive/negative policy fixtures and repeated per-target
build evidence. Preserve writers, held actions and custody; local development,
independent acceptance, integration and final self-hosted release stay separate.

## Historical workflow-transfer specification

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
