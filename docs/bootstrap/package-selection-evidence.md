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

The selector's existing supported subset is direct exact three-component numeric
versions, one registry and hosted/JVM-21. The status-only policy operation takes
supplied profile/target keywords without evaluating program legality. General
version ranges, features, transitive
resolution, network fetch/install, lock writes, latent spaces, authenticated
provenance/signatures, SBOM, publication, full PKG conformance, migration,
release, self-hosting and seed retirement remain outside this increment.

Focused positive/adversarial tests, exact body/wrapper conservation, ownership
and dependency registration, language boundary, docs/roadmap/governance, contextual
lint, independent acceptance, own CI and clean exact current-main preflight
are required before integration. A maintained draft PR begins at this declaration.

## Working implementation and observations

Draft PR724 begins at scopefa4a174c6194e393aa9bcb20e03d9e8750b7417e.
Before controls run to completion with that clean source, before implementation.
The complete status-policy and selection bodies now live in gravity.package.
One private package-record predicate is renamed to avoid the two existing
predicates colliding. Selection takes an explicit policy operation; its default
uses the stage Var, and the old build selector passes the old policy Var. Both
legacy public arglists remain exact. No policy, legality, identity, normalization,
privacy or diagnostic algorithm changes.

Controls retain two actual repository files, full text/path and recomputed
SHA-256, as explicitly unsigned supplied metadata. Their target-variant handles
say fixture:unbound; they are not compiled JVM outputs or authenticated packages.
Twelve actual selection reports cover ordinary/reordered selection, locked and
historical rebuild views, plus version, capability, effect, target, private grant,
unsafe-state, yank and revocation refusals. All sixteen complete and serialized
before/after pairs match, including requests, policies, source inputs and full
history. Hashes and supplied provenance references survive. Eight real refusals
retain PKG5001/2/3/5/6 and PKG1007 with no selected output or granted authority.

Every original policy/selection body and both legacy public arglists conserve
against the frozen main. The comparison normalizes only reader-generated
anonymous argument names and regex pattern text/flags. Initial verifier checks
used native Pattern identity and unnormalized reader gensyms and rejected these
equal-source forms; correcting the verifier required no implementation change.
One extra delimiter in the new test and one in the temporary control were caught
by strict parsing before execution and removed.

The original two suites pass16/243 before/after. The new stage and compatibility
suites together pass19/281, including late operation ports, private denial,
file-hash/provenance transport, preserved duplicate lock history and non-admitting
status. Ownership initially fails1/995 because two old compatibility files were
incorrectly added to the counted module set. Removing those two entries keeps
their explicit consumer relationships and preserves the original formula.
Corrected ownership passes32/993; language6/11, docs240/19 and roadmap/governance
pass. The new nested standalone test and helper use SH-23/sh-package-build;
primary component pairs, the53-entry leaf catalog/hash and count formulas stay
unchanged. Full SH-23, PKG and namespace completion receive no credit.

Records are under /tmp/uniform-package-selection-evidence/; controls, verifier
and logs use /tmp/uniform-package-selection-*. Exact lint, independent review,
own CI and clean actual-main composition/preflight remain required.
