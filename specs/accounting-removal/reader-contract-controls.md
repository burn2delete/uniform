# Restored reader-only source contracts

Owner: ACC-01. Base main `e047157d711652a5624012f0da7d6911ce086718`.
Historical source: PR502 `6a4f87f013d0076b81aba9e0667650e70d28e635`.
This finite draft restores three original authored reader-test paths. It changes
no production or preexisting test bytes and executes no Gravity code.

## Restorations and explicit List-query adaptation

The private Map-route and List-construction contract files are byte-identical to
history. They retain complete source reconstruction, private-route precedence,
semantic field mutation, duplicate/missing definition, schema and consumer full
equality checks. Their reader-only results are 2 tests/223 assertions and
4 tests/167 assertions, with zero failures/errors.

The original List-query suite is preserved as historical evidence and rerun
unchanged against the same pinned source: 7 tests, 257 passing assertions,
5 failures, zero errors. The original NONPASS remains in the results record;
this draft does not relabel that unchanged test passing.

The adapted List-query file makes nine exact raw edits, all recorded in the
machine results: add the two explicit native-map helper correspondences; update
the obsolete routing commentary/test name; use the repaired public/private
Map-key predicate for all owners; and assert full current wrapper parity rather
than the superseded emitter disagreement. Existing exact predicate/delegate,
argument shape, source/proof/header/result drift, malformed extraction, unknown
helper and consumer full-equality controls remain. No semantic field, number,
argument list or unknown helper is normalized away.

One additional reader-only control checks all three complete native-map guard
bodies without helper normalization, detects mutation of their actual
`bootstrap-native-carrier-header` call, and detects removal of private-Map
routing from every constraint/fact wrapper. Guard-body equality is source
evidence, not proof of native or callback behavior. The adapted suite passes
8 tests/276 assertions; its result belongs to this changed test alone.

## Evidence and validation

`reader-contract-controls-results.edn` pins historical/candidate files, all
three production files, exact edits, added-control digest, original NONPASS
capture digest and separate candidate results. Source bookends match. The
local Clojure runner reads with evaluation disabled, removes the original test
namespace before loading the adapted suite, and verifies Bootstrap remains
unloaded. The original private Map-route/List-construction copies are checked
against immutable Git bytes.

- Restored candidate suites: PASS, 14 tests/666 assertions total, zero
  failures/errors. These are Clojure reader/source comparisons only.
- Language boundary: PASS, 6 tests/11 assertions.
- clj-kondo 2025.10.23 with repository config/cache disabled: zero errors,
  zero warnings and infos for the corrected candidate. The original file's unused-import warning is retained as history.
- Canonical docs, full-language roadmap and whitespace: PASS.

The restoration script initially had a reader delimiter error before file
creation. The added guard mutation initially assumed a direct `class` call,
causing six failed assertions confined to the new control. Reading the actual
body showed its native-header bridge; the corrected mutation and complete
reader suites pass. Those preparation failures do not overwrite the separate
original five-failure NONPASS or imply compiler execution.

Independent source/adaptation disposition remains pending. All genuine compiler,
Effects, lowering, authenticated source, runtime and callback acceptance remain
open. C7/C8 and D9 require those distinct evidence boundaries. Coordinator alone
routes integration; no whole-path semantic acceptance, ACC/PR502 closure,
release or self-hosting authority follows from the source tests.

## CI correction

Predecessor `bcf0bb4b` static job `123752212872`, run `40960649429`, failed
strict changed-file lint on the newly restored List-query file's unused
`clojure.test/testing` refer at line 7, column 46. The historical file's warning
was not an unchanged baseline: that file is absent on main. The original CI
NONPASS is retained; no lint policy or baseline was relaxed.

The successor removes only that unused refer and refreshes its exact adaptation
and file hashes. The same reader results pass 14/666, with the original query's
257-pass/5-failure NONPASS separate. Boundary passes 6/11. The unchanged actual
`ci.lint/run-comparison!` API, using its normal executor with only a fresh local
output directory, reports zero baseline and zero blocking warnings/errors.
Source/runner/macro/report hashes and exact original CI identities are recorded
in the results. Successor automatic CI remains a separate pending check.
