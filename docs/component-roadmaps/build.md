# Build

Working name: **Build** (proposed). Semantic key: `build`.
Owner role: build/package maintainer; current owner unassigned by this proposal.
Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit ledger](../../specs/build-workflow/tasks.md#implementation-tasks).
Edit the ledger and run `clojure -M tools/build_task_projection.clj --write`;
the remaining-task table below is generated.

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

<!-- build-task-projection:start -->
| Task (all open) | Evidence baseline | Closing evidence |
| --- | --- | --- |
| Implement project/lock normalization and constraint resolution | [Package report](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00569-stage0-compiled-plan-package-report.clj) | Real dependency resolution with conflict, profile and capability rejection. Apply declared yank/revocation policy to selection and installation; retain signature/revocation status in locked records and explain each policy decision without silently replacing or erasing locked history. Reject revoked packages unless policy explicitly permits historical rebuild, and report revoked/yanked rejection with PKG5006. For [SAFE14](../phase-02-safety/043-safe14-supply-chain-safety.md), reject conflicting package safety manifests before compiling dependents; require safe/release locks and validate native source/digest/ABI/target/link/license/wrapper/capability metadata. Compute and present transitive authority diffs including build/runtime, unsafe, plugins, model/tool/secret and generated artifacts; dependency updates cannot silently expand grants. Caller policy checks unsafe summaries, expired reviews and safe exported wrappers/certificates. Apply advisory affected/patched-version, compatibility and capability policy at selection and installation. Reject policy violations without granting undeclared authority. Preserve existing reproducible locks and historical artifacts unless explicit emergency policy applies. |
| Execute typed graph under declared build effects | [Build architecture](../phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md) | Clean build node receipts and undeclared environment/network/shell rejection. SAFE14 also denies undeclared postinstall, model/tool, compiler IR and unproven native generation/registry authority; permitted effects carry provider, scope, grant and trace, and hermetic release effects are replayable or have pinned outputs. Negative fixtures prove denial occurs before the external effect. |
| Implement dependency-aware caching and invalidation | [Pass cache](../../bootstrap/clojure/src/gravity/pass_cache.clj) | Reuse on equal inputs; rebuild on source, compiler, grant, policy or target changes |
| Produce and verify package/provenance/signing records | [Artifact contract](../phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md) | Actual published package retrieval/verification fixtures; tamper and missing-record rejection. SAFE14 verifies registry digest/signature/manifest/attestation before use; generated outputs retain generator/version, source/output digests, effects/grants, reproducibility and performed safety checks. Safe package claims require reproducibility or accepted provenance attestations, not registry identity alone. SAFE15 certificate imports verify matching package, compiler/schema, profile/target, provider trust and noninvalidated assumptions before use. Exercise registry accept, reject and quarantine decisions under declared policy. Reject missing identity, owner, provenance, SBOM, hashes, capability/profile/target metadata or required signatures and conformance reports. Expose unsafe, AI, FFI and host-delegation surfaces before installation; reject hidden authority, malicious releases and unmitigated dependency confusion. |
| Close reproducibility and target matrix | [Reproducibility contract](../phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md) | Repeated locked builds with matching identities and explicit failing-target diagnostics Foundation requirement gravity/artifact-graph-reproducibility: Rebuild the declared artifact graph from recorded source/compiler/pass/policy/provider/target and dependency identities; compare canonical graph edges and outputs under an explicit accepted-delta policy. Refusals: Reject omitted inputs or edges, substituted provider/compiler identities and unexplained output drift; preserve the failed comparison and any policy-reviewed exception. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products; runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence. |
<!-- build-task-projection:end -->

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
