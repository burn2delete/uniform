# Finite yank and revocation policy seam

Owner: PKG5 source chat `01a18531-79da-7d23-985f-f572e5cfa13c`.
Base commit: `6276c94b0b753c888ef8fecc794bcc81245ea3b3`.
Base tree: `9f464ed0f03866bd4978e4213a488f3b1ae48683`.
Branch: `codex/pkg-yank-policy-01a18531`.

This is source evidence for the existing task
`build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution`.
The task stays Open in [the sole implementation ledger](tasks.md#implementation-tasks).
Original240 requirement identities, counts, dispositions and mapping remain
with their existing owner; this record changes none of them.

## Contract and interface

[PKG5](../../docs/phase-12-build-package-and-artifact-system/169-pkg5-dependency-resolution-specification.md)
owns policy checks, locked status retention, historical rebuild and `PKG5006`.
[PKG4](../../docs/phase-12-build-package-and-artifact-system/168-pkg4-package-manager-specification.md)
requires preserved yank history;
[PKG6](../../docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md)
separates requests and grants;
[PKG9](../../docs/phase-12-build-package-and-artifact-system/173-pkg9-private-registry-and-latent-package-space-design.md)
requires access control before private metadata;
[PKG10](../../docs/phase-12-build-package-and-artifact-system/174-pkg10-supply-chain-security-and-provenance-specification.md)
and [PKG12](../../docs/phase-12-build-package-and-artifact-system/176-pkg12-artifact-signing-verification-and-sbom-specification.md)
retain actual provenance/signature verification obligations.

Inspection found the existing P12 resolution report is a zero-argument static
metadata fixture. SH23 validates a fixed offline bootstrap lock/build request;
it does not expose a general package candidate selection interface. Its
`:revoked` boolean and provenance label are not signature verification.
Neither surface is promoted or modified here. Held compiler/runtime execution
is not needed to test this pure Clojure seam.

[Implementation](../../bootstrap/clojure/src/gravity/build/package_policy.clj)
exports `gravity.build.package-policy/decide`. It consumes the existing P12
lock-record fields `:package`, `:version`, `:registry`, `:content-hash` and
`:provenance`, preserving all other fields. Four supplied status enums add
`:yank-status`, `:revocation-status`, `:signature-status`, `:provenance-status`.
Missing, mistyped or unknown statuses reject evaluation. Labels including
`:verified` are explicitly supplied facts with no authentication authority.

The explicit closed policy map has `:id`, `:yanked` and `:revoked` keys.
Each restriction is either `:reject` or `:historical-rebuild`. An exception
requires `:mode :historical-rebuild` and a matching locked record. Comparison
excludes only the four current status facts: newly discovered revocation must
not require rewriting historical status. All other fields must match, including
content identity, provenance, capabilities and any additional metadata. The
input lock vector, order, duplicates, old statuses and custom history survive
verbatim. There is no candidate sorting, selection, installation or lock write.

Registry visibility and read grants come from caller policy, separately from
package metadata. This seam evaluates one registry: candidate and all supplied
locked records must name that registry before any metadata is returned.
Private evaluation requires `:read` for the exact registry;
latent/unknown visibility fails closed in this limited interface. Access denial
returns `PKG5005` without package metadata/history. Malformed-input reports also
omit supplied records/history. No callback is accepted or
invoked. A future metadata provider must authorize before fetching; this pure
function cannot undo an earlier unauthorized retrieval or authenticate grants.

Policy refusals carry `PKG5006`, package, range, selected version, constraint
chain, profile, target, registry and policy rule/id. Malformed inputs use
`PACKAGE-POLICY-INPUT`. When both restrictions fail, diagnostics are ordered
yank then revocation. Permits remain `:authority :non-admitting`,
`:admission :not-evaluated`, `:status-origin :supplied`; they grant no capability
and perform no effect. Invalid or missing signature/provenance labels can pass
only this yank/revocation predicate; they cannot authorize artifact consumption.

## Observed controls

[Synthetic tests](../../bootstrap/clojure/test/gravity/build/package_policy_test.clj)
exercise default yank/revocation refusal, revoked signature/provenance refusal,
explicit historical exceptions, wrong mode/policy/locked identity, malformed
statuses and context, exact private grants, metadata suppression before policy
inspection, zero callback effects, deterministic diagnostics, EDN round-trip
and unchanged complete supplied lock history. No fixture has admission authority.

Commands run from the candidate checkout on 2026-10-28:

| Command | Observed result |
| --- | --- |
| `clojure -J-Xmx256m -M:dev-test --namespace gravity.build.package-policy-test` | Exit 0; 7 tests, 148 assertions, zero failures/errors |
| `clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test` | Exit 0; 6 tests, 11 assertions |
| `clojure -J-Xmx256m -M tools/build_task_projection.clj --check` | Exit 0; no implementation acceptance inferred |
| `clojure -J-Xmx256m -M tools/build_task_projection_test.clj` | Exit 0; 5 tests, 78 assertions |
| `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` | Exit 0; 240 docs, 19 phase indexes, ASCII, no placeholders |
| `clojure -J-Xmx256m -M tools/validate_full_language_roadmap.clj` | Exit 0 |

## Remaining evidence

Independent reviewer `01a183df-1fb7-74c1-8456-625260c888c2` returned
changes required on candidate `6d5abdedf27d7e8edcbe0ad572dbcf5ba0a22a4f`,
tree `904dc450739e60ebe5fd4d30cc5c151af18a66d6`: mismatched registry input
was rejected while its record was still echoed. Review receipt SHA256:
`a02a02cbcab7ea44bf04dcf38a3d204c1ffc1b0301ecf5661fe41c1483063245`.
That candidate remains rejected review evidence. The successor checks registry
scope before report construction and omits raw inputs from malformed reports;
regressions cover candidate mismatch, mixed lock history and malformed history.
The successor requires its own exact-source disposition.

Exact source review is requested only after commit, through the existing
eligible reviewer routed by the coordinator. No self-review is acceptance.
The PR/reviewer receipt binds the exact candidate commit/tree and diff.
Integration remains with the coordinator and applicable exact-identity gates.

General constraint resolution, project/lock normalization, authentic status
production, current revocation evidence, package retrieval and cryptographic
verification, installation composition, capability compatibility, release
acceptance, self-hosting and seed retirement remain open. No package network,
registry mutation, key/credential change, signing, purchase, broker probe or
compiler/runtime execution is part of this increment.
