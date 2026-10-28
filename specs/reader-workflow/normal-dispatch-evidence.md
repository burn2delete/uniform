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
them in a hard-coded case branch. The reviewed handler table contains actual
`gravity.reader.standard/read-inst` and `read-uuid` functions with the same
converters and returned Date/UUID values as before. The complete parsed tag
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

Independent exact-source/negative-control review is pending. The task remains
open. Required future evidence includes
genuine normal Reader parsing and authenticated execution at the new source,
fresh reconstruction, sealed policy/implementation/execution binding, custom
registered handlers and explicitly granted providers in the normal pipeline,
and Uniform-authored dispatch. Existing migration fingerprints and historical
evidence are not refreshed or promoted by these fixtures. No mapping promotion,
full Reader, release, migration, performance or self-hosting acceptance is claimed.
