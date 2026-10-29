# Implementation plan: Build workflow migration

Spec: [spec.md](spec.md). Owner: backend workflow migration owner.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts).
The workflow records work and evidence; it does not replace normative authority.

## Approach and exact files

Keep the four workflow artifacts, generated roadmap task region and
`tools/build_task_projection{,_test}.clj` in scope. Edit implementation
records only in [tasks.md](tasks.md#implementation-tasks); the roadmap task link
below is a generated view. Retain the entire original migration file as history.
Shared helpers remain unchanged. Shared migration indexes are owned
by the migration coordinator and are not edited by this package.

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/build.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Build canonical remaining work](../../docs/component-roadmaps/build.md#remaining-tasks-and-evidence) | the five remaining-task rows; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/build.md#acceptance-and-integration) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-12-build-package-and-artifact-system/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: build/package maintainer. Interface owners: [emit](../../docs/component-roadmaps/emit.md#release-outcome), [runtime](../../docs/component-roadmaps/runtime.md#release-outcome), [release](../../docs/component-roadmaps/release.md#release-outcome).
Manifest, dependency and graph work can proceed against recorded interfaces. Existing seed/toolchain support remains explicit; a future self-hosted Build is not required before earlier compiler components can develop. Package transport and graph execution remain separate.

## Validation and evidence

Check ASCII, the exact seven-file set, local Markdown target/anchor resolution,
semantic package names and absence of duplicated component checklists.
Negative acceptance cases are broken/missing links, treating artifact presence
as implementation completion, or narrowing retained contracts without disposition.
Run the existing structural documentation and full-language roadmap validators.
Run focused projector, link-preservation and language-boundary checks; no
compiler/service execution suite is claimed. Record commands/results and exact review identity in evidence.md.

## Delivery and remaining work

Publish partial artifact work in a draft PR. Keep package review and integration
unaccepted until their actual receipts exist. Deus owns final integration;
repository validation cannot confer semantic component or release acceptance.
Do not add a recurring acceptance loop solely because evidence text changes.

## Names, callers and compatibility

No source names, callers, wire/schema identities, load order or historical pins
change. Retain original task identities and phase anchors. Future source work
uses subsystem/behavior names under the semantic naming rule, separately reviewed.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [L12](../../docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md#requirements): Compile-time file/environment/provider inputs and granted effects participate in reproducibility and cache invalidation; grant checking alone is not evaluation.
- [PERF3](../../docs/phase-04-performance-model/061-perf3-specialization-and-partial-evaluation-design.md#requirements): Cache and replay inputs include source/profile/target/provider/build-effect/proof dependencies; changed inputs invalidate specialized artifacts.
- [PERF6](../../docs/phase-04-performance-model/064-perf6-profile-guided-optimization-design.md#requirements): Record accepted/advisory/stale/rejected/required-missing data status and reproducible profile-data inputs; stale data cannot silently guide releases.
- [GOV2](../../docs/phase-17-governance-and-evolution/232-gov2-compatibility-policy.md#requirements): Consume package/schema/lock compatibility classifications and migration metadata. Compatibility decision authority remains a governance gap in the central mapping.
- [GOV10](../../docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md#requirements): Publication/retrieval verifies package identity, signatures, provenance, SBOM, capability/profile/target metadata and policy-bound yanks/advisories. Namespace adjudication and emergency policy authority remain proposed governance work, not Build authority.
- [SAFE14](../../docs/phase-02-safety/043-safe14-supply-chain-safety.md#requirements): Resolve pinned lockfile dependencies, record generator/source/build-effect provenance and produce reproducible builds or accepted attestations. Auth retains artifact trust and grants; build evidence cannot silently expand dependency authority.

## Yank and revocation task interface

Bind the existing proposed requirement `behavior/pkg5/yank-revocation-admission`
to the existing first Build task
`build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution`
under `build-implementation-tasks`. PKG5 lines 44, 61, 108 and 119 require
policy checks, recorded signature/revocation status, the explicit historical
rebuild exception and diagnostic `PKG5006`. All five IDs and Open statuses stay.
This refines closing evidence; it does not implement a resolver or accept the
requirement. Build/package maintainer remains the semantic owner role.

[The exact amendment](yank-interface.edn) changes only the preserved PR445
requirement's task-reference and task-gap fields. It is a replayable amendment
receipt, not a task/status ledger. Its isolated applied manifest remains
separate from the original; main does not acquire the full draft manifest.
All four facet identities/bytes, historical support, atomicity/nonoverlap and
implementation assessment are conserved. The old missing-interface reason is
retained inside the amended gap qualification. No other requirement or alias
is changed; GOV10 source-binding repair is excluded.

Check the four source spans, current task hash/marker/ID, acceptance predicates,
wrong hash/marker/ID controls, unchanged task population/status and the generated
Build/SH projections. Preserve predecessor SH review as history and leave the
changed-source mapping pending independent review. The 577-interface inventory
is not recomputed; accepted isolated 586 and proposed-unverified 470 remain
distinct. No live resolver, compiler, runtime or broker action is required.


## Source-qualified package interfaces

The following criteria refine the existing five Open Build tasks. Independent
source review accepted the finite PKG1-PKG12 proposal in receipt
`66377faf6d627e534284dfa6bed107b22730a1251dfb2bd2595caaa894ef1b4d`.
Implementation acceptance and the explicitly missing cross-component
producer/consumer interfaces remain unassessed.

## Project and dependency normalization

Task: `build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution`.

PKG1 requires data-only parsing, deterministic normalization/hash, explicit
fields, legal profile/target pairs, declared registry/local sources, generated
roots with provenance, explicit unsafe policy, and release lock completeness.
Effects, capability requests and denials are separate; normalization grants no
authority. Reject effectful configuration, invalid pairs, ungranted dependency
sources, hidden authority expansion, missing generated provenance or unsafe
policy, and missing required artifacts. Preserve PKG1001-PKG1008 context.
Entrypoint namespace/var resolution is an unresolved producer interface.

PKG4/PKG5 require a deterministic solve with specified tie-breaks, explicit
features, direct and transitive lock metadata, per-profile/target/ABI variants,
source/signature/revocation status and sufficient minimal-known conflict
evidence. Test equal canonical inputs and complete offline locks positively;
reject incomplete locks, denied authority, unavailable variants, ungranted
private/latent sources, implicit feature effects and inadequate conflict reports.
Keep PKG5001-PKG5008 and policy decisions in the report. The exact four-facet
[yank amendment](../build-workflow/yank-interface.edn) already supplies the
historical-rebuild exception and PKG5006; do not apply it a second time.

## Typed graph and cache decisions

Task: `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects`.

PKG2 requires graph inputs from project/lock, typed nodes with inputs, outputs,
effects, tool/environment identity and provenance edges. Admit cycles only when
explicitly marked compiler-bootstrap fixed points. Re-enter generated source
through the normal reader and checkers. Reject undeclared shell/network/effects,
hidden plugin authority and missing generator edges. A failure names its actual
node, input edge and policy, with PKG2001-PKG2008 as applicable. Source legality
is a compiler producer result; graph execution cannot override it.

Task: `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation`.

PKG2 requires positive reuse with equal source, lock, compiler, profile, target,
policy and relevant environment inputs. Negative cases change each consumed
identity, safety policy or grant independently and require invalidation. A cache
decision log names the differing field; no producer or policy check is skipped
merely because a content hash exists.

## Package, registry and artifact records

Task: `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records`.

PKG3 requires schema/kind registries, canonical identities, manifests for every
emitted kind, declared target-byte hash formats, generator inputs, source/compiler
links, authority summaries, safety evidence and conditional release links.
Verify before consumption; reject absent manifests, unknown schemas/kinds,
mismatched content, noncanonical signed data and absent required evidence.
Exercise PKG3001-PKG3008 at the actual consumer boundary.

PKG4 requires structured records for add/remove/update/resolve/fetch/verify/audit/
explain/publish/yank/mirror/vendor, including declared plugin effects and
reviewable capability, unsafe, target, license and provenance diffs before
mutation. Verify downloaded hashes and policy-required signatures; offline
installation needs a complete lock/cache/verification population. Redact tokens
from human and machine outputs. Reject hidden diffs, missing lock metadata,
incomplete offline caches, invalid publication evidence, credential leakage and
yanks erasing rebuild history. Preserve PKG4001-PKG4008.

PKG6 update diffs consume analyzed effect/capability summaries and expose
expansion. Derivation, grant issuance, transitive denial across deployment and
runtime permission audit remain the explicit missing interfaces below; a Build
record cannot manufacture those results.

PKG8 requires schema-valid unsafe-island/wrapper/FFI/taint/proof/checker/reviewer
metadata and visible safety diffs. Preserve unreviewed, reviewed, requires-recheck,
quarantined, revoked and accepted-exception states. Test missing wrapper evidence,
ABI/lifetime assumptions, taint/capability fields, revoked proof and quarantine
denial. An unreviewed unsafe release requires the matching policy or scoped
exception. Invalidate dependent release gates on revoked safety claims; retain
PKG8001-PKG8008. Producing metadata does not prove the wrapped code safe.

PKG9 requires private read grants before disclosure, privacy-safe errors, signed
indexes when required, recorded source selection, latent-state transitions and
generated-candidate lineage. Only published packages resolve normally; other
states need explicit matching grants and policy. Reject unauthorized disclosure,
ordinary resolution of latent names, missing review before publication, and
mirrors lacking original signatures or verifiable attestations. Preserve yank/
revocation rebuild history and PKG9001-PKG9008. Namespace-policy adjudication
remains with governance; this interface consumes its decision.

PKG10 requires exact release provenance, verified builders/platforms, linked
lock/source/compiler/recipe/dependency/evidence identities, declared blob
source/hash/license/safety and generated-source taint. Reject missing dependency
provenance, unknown builders/schemas, untracked generators/blobs, mismatched
subjects and revoked inputs under the active policy. Preserve the explicit
historical-rebuild exception. Keyless records include issuer/subject/audience,
certificate validity, trusted root and transparency evidence. Keep
PKG10001-PKG10010; a signature alone cannot establish provenance.

PKG12 requires canonical payload signing, exact subject/manifest binding,
policy-scoped valid keys, transitive SBOM dependency/license/capability/unsafe/
generator/blob/advisory data, and complete verification reports without secrets.
Attestations include builder/platform/material/lock/compiler/recipe identity,
freshness and declared isolation exceptions. Check required level/track before
consumer use; reject silent downgrades, stale/missing/revoked/unsigned/mismatched
mandatory evidence and omitted failed checks. Keyless fixtures vary issuer,
subject, audience, certificate identity/validity, root metadata, log inclusion,
timestamp/checkpoint/consistency evidence independently. Test all applicable
PKG12001-PKG12020 failures. Isolation/hermetic/reproducible/certified claims need
their own matching evidence; Build consumes governance-approved trust roots.

## Rebuild and target matrix

Task: `build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix`.

PKG7 requires locked repeat builds, canonical manifest/content comparison,
recipes and controlled source/project/lock/compiler/toolchain/graph/environment/
target/generator/blob/FFI input identities. Normalize timestamps, traversal and
host paths; record seeds and permitted external inputs. Reject unrecorded
network/random inputs, path leakage, absent generator hashes and filename-only
comparison. Mark nonreproducible outputs explicitly and deny reproducible release
claims/signing when required policy is unmet. Preserve PKG7001-PKG7008.

PKG11 requires concrete profile/backend/target/ABI/runtime/features/dependency/
capability/artifact/test/support entries. Test target-specific variant selection,
compile-time legality and independent conformance reports. Reject implicit host
ABI/paths, unsupported pairs, missing variants, transferred capability legality,
missing identity or evidence, and undeclared fallback. Report failing pairs with
PKG11001-PKG11008; default-target success cannot stand for another target.
Product/ISA/hardware/OS/application scope decisions are not selected here.

## Explicit unresolved producer and consumer interfaces

These are missing acceptance interfaces, not additional implementation tasks:

- `behavior/pkg1/project-entrypoint-resolution`: exact Resolver output by
  artifact kind and PKG1003 diagnostic handoff to Build.
- `behavior/pkg6/derived-capability-manifest`: analyzed effects and declarations
  to capability summary, including deterministic derivation and PKG6008 mismatch.
- `behavior/pkg6/request-grant-separation`: package requests/denials to deployment
  grants and scoped runtime handles, including excess-grant rejection PKG6006.
- `behavior/pkg6/runtime-permission-audit`: participating runtime requested/
  granted/denied/used audit schema and records; Build metadata is insufficient.
- `behavior/pkg6/transitive-deny-precedence`: missing-edge and explicit-denial
  rejection through source/manifest/resolver/deployment/runtime, with PKG6004.

Historical source reviews and the integrated PKG5 task-interface review retain
their exact scope. No historical fixture or migration checkbox is promoted to
current implementation acceptance. Central reconciliation owns alias decisions,
composition, adoption and aggregate counts.
