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

Closing evidence: Real dependency resolution with conflict, profile and capability rejection. Apply declared yank/revocation policy to selection and installation; retain signature/revocation status in locked records and explain each policy decision without silently replacing or erasing locked history. Reject revoked packages unless policy explicitly permits historical rebuild, and report revoked/yanked rejection with PKG5006.

<!-- build-governance-resolution:start -->
Source requirement: `governance/package-policy-consumption`, continuing the
existing source-family binding to this same task. See
[GOV10 conformance](../../docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md#conformance-criteria)
and [SAFE14 requirements](../../docs/phase-02-safety/043-safe14-supply-chain-safety.md#requirements).

Additional source criteria, proposed current composition: Under SAFE14 and
GOV2/GOV10, apply the declared yank/revocation, advisory affected/patched-version,
compatibility and capability policy at both dependency selection and
installation before use. Consume complete version-bound advisory/yank and
accountable namespace/emergency decisions from the existing
`evolution/security-response`, `evolution/registry-yank`,
`evolution/namespace-decision` and `evolution/change-review` interfaces;
resolver output cannot produce or authorize those decisions.

Reject conflicting package safety manifests before compiling dependents;
safe/release builds require locked or equivalent pinned provenance. Validate
native source/digest/ABI/target/link/license/wrapper/capability records. Present
transitive runtime/build/unsafe/plugin/model/tool/secret/generated-artifact
authority diffs on updates without granting undeclared authority. Caller policy
checks unsafe summaries, current review, safe wrappers and certificates.
Exercise accepted policy application and rejection for active advisories,
revocation, hidden authority expansion, missing/stale/wrong-scope decisions and
unreviewed emergency lock changes. Preserve existing reproducible locks and
historical artifacts unless explicit emergency policy applies; reject silent
replacement or erasure. Retain PKG5006 and applicable SAFE14/GOV diagnostics,
actual selected/installed content identities, policy inputs and decision trace.
These criteria implement the package-policy consumer, not governance production
or a whole-governance completion prerequisite. Status remains Open.
<!-- build-governance-resolution:end -->

- [ ] `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects` Execute typed graph under declared build effects

Status: Open

Evidence baseline: [Build architecture](../../docs/phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md)

Closing evidence: Clean build node receipts and undeclared environment/network/shell rejection

- [ ] `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation` Implement dependency-aware caching and invalidation

Status: Open

Evidence baseline: [Pass cache](../../bootstrap/clojure/src/gravity/pass_cache.clj)

Closing evidence: Reuse on equal inputs; rebuild on source, compiler, grant, policy or target changes

- [ ] `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records` Produce and verify package/provenance/signing records

Status: Open

Evidence baseline: [Artifact contract](../../docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md)

Closing evidence: Actual published package retrieval/verification fixtures; tamper and missing-record rejection

<!-- build-governance-package:start -->
Source requirement: `governance/registry-release-admission`, continuing the
existing source-family binding to this same task. See
[GOV10 requirements](../../docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md#requirements)
and [SAFE14 conformance](../../docs/phase-02-safety/043-safe14-supply-chain-safety.md#conformance-criteria).

Additional source criteria, proposed current composition: Under SAFE14/SAFE15,
PKG9-PKG12 and GOV10, verify actual registry content digest, required signature,
manifest and attestation before use. Generated outputs retain generator/version,
source/output digests, effects/grants, reproducibility and performed safety
checks. Safe claims require reproducibility or accepted provenance attestations;
imported certificates bind the actual package/compiler/schema/profile/target,
provider trust and noninvalidated assumptions.

Validate stable identity, namespace, owner, version, license and signing status,
manifest/lock where applicable, hashes/provenance/SBOM, profile/target matrices
and conformance tied to compiler/target/profile/stdlib versions. Before
installation/publication expose effects/capabilities, GOV9 unsafe metadata,
FFI/host delegation and AI policy/eval/`:ai/human-review`/injection evidence.
Consume the actual applicable library/target/security/namespace/yank decisions
from `evolution/library-admission`, `evolution/target-support-admission`,
`evolution/security-review`, `evolution/namespace-decision` and
`evolution/registry-yank`; this gate does not adjudicate their policy.

Exercise declared registry accept/reject/quarantine behavior and real consumer
results. Reject tamper, wrong/stale/missing required evidence, unsigned content
when signatures are required, nonreproducible or malicious releases under the
declared policy, hidden authority, unsupported conformance, policy-forbidden
advisory/yanked dependencies, reserved-name abuse and unmitigated dependency
confusion. Quarantine unresolved security/provenance risk according to policy.
Preserve original SAFE14/SAFE15/PKG/GOV diagnostics, source and exact content/
policy/tool identities; metadata presence alone does not prove verification.
Actual publication and signing retain their authority boundaries. All existing
task IDs and Open statuses remain; no package operation was executed here.
<!-- build-governance-package:end -->

- [ ] `build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix` Close reproducibility and target matrix

Status: Open

Evidence baseline: [Reproducibility contract](../../docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md)

Closing evidence: Repeated locked builds with matching identities and explicit failing-target diagnostics
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
