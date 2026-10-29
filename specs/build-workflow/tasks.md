# Build implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

These five records are the sole editable task authority. IDs derive from the
existing component/section/title-slug convention; the original table had titles,
not literal individual IDs. No external dashboard object identity or new task
population is claimed. All rows remain Open; transfer is not implementation
acceptance. Original outcome and integration boundaries remain in the roadmap.

<!-- build-implementation-tasks:start -->
- [ ] `build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution` Implement project/lock normalization and constraint resolution

Status: Open

Evidence baseline: [Package report](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00569-stage0-compiled-plan-package-report.clj)

Closing evidence: Real dependency resolution with conflict, profile and capability rejection. Apply declared yank/revocation policy to selection and installation; retain signature/revocation status in locked records and explain each policy decision without silently replacing or erasing locked history. Reject revoked packages unless policy explicitly permits historical rebuild, and report revoked/yanked rejection with PKG5006. For [SAFE14](../../docs/phase-02-safety/043-safe14-supply-chain-safety.md), reject conflicting package safety manifests before compiling dependents; require safe/release locks and validate native source/digest/ABI/target/link/license/wrapper/capability metadata. Compute and present transitive authority diffs including build/runtime, unsafe, plugins, model/tool/secret and generated artifacts; dependency updates cannot silently expand grants. Caller policy checks unsafe summaries, expired reviews and safe exported wrappers/certificates. Apply advisory affected/patched-version, compatibility and capability policy at selection and installation. Reject policy violations without granting undeclared authority. Preserve existing reproducible locks and historical artifacts unless explicit emergency policy applies.

- [ ] `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects` Execute typed graph under declared build effects

Status: Open

Evidence baseline: [Build architecture](../../docs/phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md)

Closing evidence: Clean build node receipts and undeclared environment/network/shell rejection. SAFE14 also denies undeclared postinstall, model/tool, compiler IR and unproven native generation/registry authority; permitted effects carry provider, scope, grant and trace, and hermetic release effects are replayable or have pinned outputs. Negative fixtures prove denial occurs before the external effect.

- [ ] `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation` Implement dependency-aware caching and invalidation

Status: Open

Evidence baseline: [Pass cache](../../bootstrap/clojure/src/gravity/pass_cache.clj)

Closing evidence: Reuse on equal inputs; rebuild on source, compiler, grant, policy or target changes

- [ ] `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records` Produce and verify package/provenance/signing records

Status: Open

Evidence baseline: [Artifact contract](../../docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md)

Closing evidence: Actual published package retrieval/verification fixtures; tamper and missing-record rejection. SAFE14 verifies registry digest/signature/manifest/attestation before use; generated outputs retain generator/version, source/output digests, effects/grants, reproducibility and performed safety checks. Safe package claims require reproducibility or accepted provenance attestations, not registry identity alone. SAFE15 certificate imports verify matching package, compiler/schema, profile/target, provider trust and noninvalidated assumptions before use. Exercise registry accept, reject and quarantine decisions under declared policy. Reject missing identity, owner, provenance, SBOM, hashes, capability/profile/target metadata or required signatures and conformance reports. Expose unsafe, AI, FFI and host-delegation surfaces before installation; reject hidden authority, malicious releases and unmitigated dependency confusion.

- [ ] `build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix` Close reproducibility and target matrix

Status: Open

Evidence baseline: [Reproducibility contract](../../docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md)

Closing evidence: Repeated locked builds with matching identities and explicit failing-target diagnostics Foundation requirement gravity/artifact-graph-reproducibility: Rebuild the declared artifact graph from recorded source/compiler/pass/policy/provider/target and dependency identities; compare canonical graph edges and outputs under an explicit accepted-delta policy. Refusals: Reject omitted inputs or edges, substituted provider/compiler identities and unexplained output drift; preserve the failed comparison and any policy-reviewed exception. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products; runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence.
<!-- build-implementation-tasks:end -->

## Historical workflow migration record

The entire original file follows unchanged as historical workflow evidence.
Its checkboxes and earlier task-source statements are separate from the
current implementation authority and do not imply product completion.

# Tasks: Build workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Build canonical remaining work](../../docs/component-roadmaps/build.md#remaining-tasks-and-evidence); do not copy its checklist here.

- [x] T001 [US1] Prepare the four owned package artifacts, preserving canonical
  contract, component and phase links. Evidence: the files in this directory.
- [x] T002 [US1] Validate all local links/anchors, ASCII, exact owned paths and
  preserved implementation status; retain the result in evidence.md.
- [ ] T003 [US2] Obtain independent review of the exact package candidate,
  including dependencies, boundary/nonclaims and semantic names.
- [ ] T004 [US1] Integrate the reviewed package and record its merge receipt.

## Dependencies and outcomes

T002 checks T001; T003 consumes the checked candidate; T004 consumes independent
acceptance. Parallel packages do not block this package's local preparation.
Component completion is not a prerequisite for its workflow migration, and
migration completion does not close the component's implementation tasks.
Partial work stays draft. No budget, task quota or repeated authorization gate.

## New-name review

Review `build-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
