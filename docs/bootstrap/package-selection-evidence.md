# Package: exact selection and supplied status policy

## Governing contracts and declared increment

D1, D2, D3, D8, D9, PKG1, PKG4, PKG5, PKG6, PKG8, PKG9, PKG10,
PKG12, BOOT8 and TEST2 govern this change. Initial base is actual main
13012158ec3cff4e0f8aa7468f7f33d4a066e946, tree
22e79703f29a2a2bab3195d11fa8bfdde31f3a9a. This scope declaration precedes
implementation and supplies no completion or admission credit.

The existing native exact-version selector and supplied yank/revocation policy
will move from gravity.build.package-selection and gravity.build.package-policy
into gravity.package. The old entry points remain compatibility callers.
Package policy and selection belong to the package stage, with build consuming
their results. The new stage must not import build, Bootstrap, CLI or release.
Explicit policy interposition must retain late Var behavior at the old API.

The public increment exposes decide-policy and select-exact. Inputs are supplied
package/lock records, exact direct constraints, registry scope/grants, profile,
target, policy, allowed effects/capabilities and safety state. Outputs are
deterministic selection/variant data, supplied policy/history records and stable
PKG rejection records. No operation authenticates a record or grants authority.
Existing PKG5001-7, PKG1007 and PACKAGE-POLICY-INPUT decisions remain unchanged.
Private registry refusal must continue to precede metadata reporting.

## Evidence and unsupported boundaries

Before/after controls must compare complete records and serialized output,
preserve supplied identities, provenance and full locked history, and exercise
actual version, capability, variant, unsafe-state, grant, yank and revocation
refusals. Development candidates may reference real repository file bytes and
their recomputed hashes, but are explicitly supplied unsigned metadata, not
published packages, verified downloads, signed indexes or release artifacts.

The existing supported subset is direct exact three-component numeric versions,
one registry and hosted/JVM-21. General version ranges, features, transitive
resolution, network fetch/install, lock writes, latent spaces, authenticated
provenance/signatures, SBOM, publication, full PKG conformance, migration,
release, self-hosting and seed retirement remain outside this increment.

Focused positive/adversarial tests, exact body/wrapper conservation, ownership
and dependency registration, language boundary, docs/roadmap/governance, contextual
lint, independent acceptance, own CI and clean exact current-main preflight
are required before integration. A maintained draft PR begins at this declaration.
