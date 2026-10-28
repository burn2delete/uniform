# Normal Reader standard-handler dispatch seam

Task: `reader/finite-remaining-work/extension-policy-closure` (open).
Source base: `a2011d9e1a8d91116dba933bbd12c4d91d336da0`.
Predecessor PR540 merged at `0389e01073ffd7d45ef2f2d2223c1b8afed18b20`;
its current-base review receipt was
`0e804e2b79eb6566d04d91ed87db2d6cae9ed39dc9bc70f42169058c70891913`.
That completed operation is consumed, not retried.

## Implemented seam

The existing normal entry `c2-reader-products` still invokes
`sh03-reader-resolved-result!` and `sh03-reader-adapt-products!`. Producer result
and fresh-verifier checks remain in the resolved-result path. The source-unit
adapter still checks source-content identity and semantic-value closure before
the host-value walk. No synthetic producer or authenticator is installed.

The existing `sh03-reader-host-values!` now dispatches parsed standard tags
through `gravity.reader.dispatch/dispatch-standard-tag`, instead of converting
them in a hard-coded case branch. The reviewed handler table binds the standard
handler ids `gravity.reader.standard/read-inst` and `read-uuid` to executable
functions with the same converters and returned Date/UUID values as before. The complete parsed tag
population is checked before the first handler call. The source unit's four
policy-hash joins must match the canonical hash of the exact standard policy.
Caller handlers, extra policy fields, unknown tags and ambient authority reject.

The identity adapter consumes the host walk's value map and execution
observations separately. Normal products and the compiler C2 artifact expose
the observations with `:authority :unsealed-bootstrap-observation`. Records
identify the original source id/content hash, raw form id/span, actual policy
and handler, input hash and result hash. Input/result hashes use separate
explicit domains and the existing hosted C2 canonical encoding.

These observations are diagnostic data outside the authenticated SH03 product
and existing incremental invocation-hash contract. They confer no source,
policy, producer, envelope, cache or replay authority. Existing projected
invocation records, hashes, native carrier guards and authentication/replay
checks are retained. Provider callbacks and custom/effectful tags are not
introduced into this standard-tag seam; the prior leaf grant controls remain.

## Cheap fixture evidence

`gravity.reader.normal-dispatch-test` reads and evaluates exact named Clojure
definitions from their normal production files, including source-slice checks,
UTF-8 decoding, semantic-reference checks, the atomic decoder, the host-value
walk and identity-stage definition. It uses the real source-unit constructor
with explicit project identity and the actual co-canonical extension classifier.

Parsed graphs and semantic-value tables are injected fixtures. No Gravity
parser/producer, authenticated envelope, source-rule admission or fresh Gravity
reconstruction is executed. A negative fence throws at the existing semantic
closure check and observes zero dispatch calls; it never fakes successful
verification. Real Date/UUID results and invocation hashes are checked, along
with changed policy/hash refusal, unknown tags before any handler, original
source-slice joins, missing semantic references and source/payload hash changes.

- `clojure -J-Xmx512m -M:dev-test --namespace gravity.reader.normal-dispatch-test --namespace gravity.reader.dispatch-test`:
  13 tests / 126 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`:
  6 tests / 11 assertions, zero failures/errors.
- `git diff --check`: passed.

An initial fixture file had an unmatched delimiter and failed to load; after
repair, the commands above passed. That load failure is not execution evidence.

## Remaining gates

Source-bound projection controls passed: crosswalk 10 / 108, dependency controls
14 / 402, focused ownership inventory 1 / 554, Reader projection and SH generation
checks. The ownership pin references real source commit `4d96671d`; the new bridge
retains all prior versions and pending/unassessed mapping states.

The initial source review disposition is recorded below. The task remains open.
Required future evidence includes
genuine normal Reader parsing and authenticated execution at the new source,
fresh reconstruction, sealed policy/implementation/execution binding, custom
registered handlers and explicitly granted providers in the normal pipeline,
and Uniform-authored dispatch. Existing migration fingerprints and historical
evidence are not refreshed or promoted by these fixtures. No mapping promotion,
full Reader, release, migration, performance or self-hosting acceptance is claimed.

## Independent source review and main composition

Existing reviewer `01a183df-0570-76e0-adfa-796c573e66f7` accepted the finite
source/fixture scope at `afe5d20b3d013beabec45fa23768949ea05f2611` / tree
`618a5438fe015263fcc4c0d1c2b3ce74b4e145a4`, with no blocking findings.
Receipt SHA256:
`e05486a0e1e707a1c45e38a9b1e2a5bd53328058a58b6664cc315d136ebf448a`.
The reviewer reproduced 43 tests / 647 assertions and 1 adversarial test / 24
assertions, including exact-source projection/regeneration controls. No real
producer or authentication run occurred; current-base integration and full
adapter composition were not accepted by that source receipt.

Main `3ddff4b7d50dd874da8c8d62c8f75eef28647955` adds Build source mapping and
portable selected-source harness work. The overlap is only crosswalk metadata.
Composition retains incoming Build source pins/review history and Reader's
actual `4d96671d` ownership pin. Both source histories remain pending/unassessed,
and all reviewed Reader implementation/fixture bytes remain unchanged.

Composed crosswalk controls passed 11 / 133, dependency controls 14 / 402, and
Reader/Build projection checks. The crosswalk is regenerated by its documented
tooling with the actual composed fingerprint. The source review is consumed
only for unchanged source; independent changed-composition review and exact-head
CI remain pending. No mapping or full Reader acceptance is promoted.

## Static CI repair

Composition review at `05418bb2` passed 25 tests / 535 assertions and
byte-identical regeneration, but withheld integration acceptance after static
CI run `40787643788` failed. Supplemental receipt SHA256:
`ece376b2a47a86dce393a42d16b2b14adb475eebad7cfb593e8a36a2ff9b7c89`.
The two blocking warnings were the obsolete bootstrap `clojure.instant`
require and a redundant nested `let` in the product adapter. The successor
removes that require and joins the adjacent binding vectors without changing
evaluation order or the product map. Lint policy and configuration are unchanged.

After repair, focused dispatch controls passed 13 tests / 126 assertions and
the language-boundary gate passed 6 tests / 11 assertions. Local `clj-kondo`
is unavailable; hosted exact-head CI and independent delta review remain
required. The failed run and withheld integration disposition remain history.
