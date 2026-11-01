# Direct exact-version selection increment

Owner: existing PKG5 source chat `01a18531-79da-7d23-985f-f572e5cfa13c`.
Base: `a0603b3d3fd54a10c0c3247433f5de8a29c95443`, tree
`4913575baefaff3f814ecdea65c36a0eab5f6ff1`.
Branch: `codex/package-exact-selection-01a18531`.

This advances the existing task
`build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution`
in [the sole task ledger](tasks.md#implementation-tasks). Its status remains
Open. No original240 mapping, task population or count changes are made.
The merged PR556 policy seam and its historical reviews remain unchanged.
Supported thread and source inspection found no parallel resolver implementer;
the existing P12 report and SH23 fixed bootstrap surface remain distinct.

## Implemented subset

[PKG5](../../docs/phase-12-build-package-and-artifact-system/169-pkg5-dependency-resolution-specification.md)
requires deterministic explainable resolution with explicit constraints and
policy, rather than implicit version, target or authority choices.
[Source](../../bootstrap/clojure/src/gravity/build/package_selection.clj)
exports `gravity.build.package-selection/select-exact`.
It selects direct package records and concrete artifact variants from supplied
metadata, without executing a compiler or using a registry transport.

The request has exactly `:constraints`, `:candidates`, `:locked-records`,
`:mode`, `:context`, `:allowed-effects`, `:allowed-capabilities` and
`:unsafe-policy`. Context contains one registry/visibility/read-grant table,
one profile/target pair and the existing explicit yank/revocation policy map.
The supported pair is `:hosted` / `:jvm-21`; other pairs are refused. Allowed
effects/capabilities are explicit caller policy sets, not runtime grants.
Only unsafe policy `:deny` and supplied safety state `:reviewed` with an empty
unsafe-island vector can pass. These are metadata checks, not safety proofs.

Each constraint contains `:package`, `:version` and an ordered `:chain`.
Version syntax is exactly three nonnegative decimal components separated by
dots, with no leading zeros except zero itself. Versions are compared by exact
string equality. Ranges, prereleases and other constraint syntax are refused,
not interpreted. This increment makes no version-precedence claim.

Records retain the P12 `:package`, `:version`, `:registry`, `:content-hash`,
`:provenance`, `:capabilities` and `:safety` fields, the PR556 status fields,
and explicit `:effects`, `:unsafe-islands`, `:variants`, `:features` and
`:dependencies`. Features/dependencies must both be empty vectors. Variants
are explicit profile/target/artifact maps, unique per pair. Unknown record or
request fields are refused so unsupported policy cannot be silently ignored.

Constraints are sorted by package/version/chain and deduplicated. Candidate
records are normalized, sorted by package/version/registry and deduplicated.
Effect/capability vectors and variant ordering are canonicalized without
dropping facts. All normalized inputs are recorded. Different records with the
same selection identity fail with `PKG5007`; no order-based tie-break occurs.

An exact-version conflict emits `PKG5001` with two deterministically chosen
contradictory constraints; unavailable versions carry the one unsatisfied
constraint. Selections are all-or-nothing, ordered by package. Profile/target
misses emit `PKG5003`, authority expansion `PKG5002`, safety-policy mismatch
`PKG1007`. Malformed or unsupported inputs emit `PKG5007`.
`PKG5002` identifies the denied capability/effect and includes sorted complete
denied-capability and denied-effect lists, distinct from runtime grants.

`:select` uses candidates. `:locked` uses locked records without consulting
candidates for replacement; missing locked selection emits `PKG5004`.
`:historical-rebuild` selects current candidates only if they match locked
history apart from the four current status fields. The existing
[package policy seam](package-policy.md) decides yank/revocation exceptions
and emits `PKG5006`. Even active historical selections require a matching lock.
Original lock-vector order, duplicate historical records and old status facts
are preserved in the output; no lockfile write occurs.

Registry authorization and record-scope checks precede metadata reporting.
Denied/private, latent, mismatched-registry and malformed inputs do not echo
package records/history. `PKG5005` refuses unsupported registry access.
No supplied function/provider is called. The caller still owns authorization
before obtaining metadata and actual grant authenticity.

## Controls and evidence

[Compiler-free fixtures](../../bootstrap/clojure/test/gravity/build/package_selection_test.clj)
exercise canonical input reordering/deduplication, selected hashes and variants,
conflicting/exact-unavailable versions, ambiguous content identities, unsupported
syntax/fields/features/transitives, variant absence, unsafe metadata, authority
expansion, private/mismatched scopes, locked selection, missing locks and the
real PR556 yank/revocation policy join. No fixture admits an artifact.

Observed commands on 2026-10-28:

- `clojure -J-Xmx256m -M:dev-test --namespace gravity.build.package-selection-test --namespace gravity.build.package-policy-test`:
  16 tests, 243 assertions, zero failures/errors, exit 0.
- `clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`:
  6 tests, 11 assertions, exit 0.
- `clojure -J-Xmx256m -M tools/build_task_projection.clj --check`: exit 0.
- Canonical document and full-language roadmap validators: exit 0.

The frozen commit/tree, patch identity and independent source-review disposition
belong to the draft PR receipt. Source author checks do not confer acceptance.
OWN returned changes required on predecessor
`e39b5bfcfd0c2ed6d539bfeeb5a9ee717987627a`, tree
`380b6e20ae9f4b1b56c8f783d9159d7950131410`: authority refusals omitted the
denied capability/effect. Receipt SHA256
`a59223ae5ce230c882a6799c972f971f43dc4208c8f2f598e1bc2a0c0aa902ea`.
The predecessor's 15 tests/235 assertions and 28 reviewer controls passed, but
did not close that diagnostic gap. This successor adds deterministic authority
attribution and separate capability-only/effect-only regression controls;
independent successor disposition remains required.

## Remaining criteria

Every result remains `:authority :non-admitting`, `:admission :not-evaluated`,
`:status-origin :supplied`, with no effects or capability grants. Supplied
signature/provenance labels are retained, never verified by this selector.
General project normalization, semantic version ranges, transitive solving,
feature unification, multi-registry priorities, other profile/target pairs,
ABI/platform constraint solving, authentic package/status/signature/provenance
verification, installation/build execution and release acceptance remain open.
This increment introduces no runtime, broker, provider or transport execution.
