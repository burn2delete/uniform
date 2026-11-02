# Proposed package acceptance interfaces

This is the PKG1-PKG12 family's proposed addendum to the existing Build plan.
It is an input for central adoption, not a task ledger or accepted implementation.
The exact 70 existing candidate IDs and every source facet remain in
[package-family.edn](package-family.edn). Its guarded operations propose 64
task bindings, conserve the already integrated PKG5 amendment, and leave five
cross-component interfaces explicitly missing. All implementation is unassessed.

The five existing Build task IDs in
[the canonical task source](../build-workflow/tasks.md#implementation-tasks)
retain their current bytes and Open status. The paragraphs below refine their
closing evidence only if centrally adopted into the Build plan. They do not
authorize a build, registry operation, signing operation or runtime launch.

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
