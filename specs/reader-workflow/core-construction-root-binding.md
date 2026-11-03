# Current Core construction binding control

## Purpose and scope

This bounded source increment records the private construction invariant needed
when the selected execution binding is dynamically thread-bound. It is based on
current main `0e2b37cd2eefbab2f02e5898b960f9bbc4100fef`, and the candidate
source/test freeze is commit `82537d2cb6ec3bb0ac51f30e7c53f9ead2b59b96`, tree
`4c7b0bb1422cb4170624249408a93515a64cad92`. It changes only the 00897 guard,
the private 00898 construction fact, their focused controls, and the current
scalar-Set controls recorded below.

The guard still tracks every protected Var and still compares raw roots for all
ordinary protected Vars. The execution Var now records both its raw root and
its outer effective value. Its check always requires the raw root to be stable;
inside the private builder it additionally requires the effective value to be
the exact selected construction binding. Outside that builder it requires the
original outer effective value. Equal-looking replacements and changed roots
remain rejected.

The builder records `:execution-binding` in its private construction facts after
reading the existing current binding. This is an internal fact, not a caller
option, session field, manifest, digest, public authority flag or admission
rule. Existing Reader/Syntax/Macro source selection, source provenance, plan,
catalog, carrier and operation checks remain in place.

## Requirements and dependencies

- Preserve raw-root identity and effective-value identity as separate checks.
- Preserve the current selected Reader/Syntax/Macro source path and provenance.
- Keep public arities and the outer construction bookend unchanged.
- Reject root replacement, wrong selected objects, nil and equal-looking copies.
- Do not introduce a work quota, source-pin renewal, public dispatch or
  runtime/native acceptance claim.

The implementation depends on the existing 00897 protected Var list, 00898
private builder and current source-selection helpers. The focused test reads
those source forms as data in a disposable namespace; it does not load
Bootstrap, a Gravity plan, a Reader, an owner verifier or an operation.

## Outputs and evidence

`gravity.reader.core-construction-binding-test` passes 7 tests and 89
assertions. It covers distinct selected bindings, identical bindings, restored
outer identity, raw-root replacement, masked execution-root tampering, another
dynamic Var and source-hash/legality controls. It also checks that the private
facts expression records the selected object without changing the outer facts.

The current SH01 scalar-Set controls pass independently: the list-set runtime
kernel is 1 test and 100 assertions, the keyword-set membership kernel is 1
test and 147 assertions, and the literal keyword-set kernel is 1 test and 87
assertions. They cover explicit helper arities, same-printed-name keyword
identity, long representable values, raw-native membership, benign metadata,
cyclic metadata refusal, duplicate native storage, immutability and
construction observation. These are semantic/native-shape controls, not work
or input-budget checks. The module census controls remain 1 test and 42
assertions for native-set rejection and 1 test and 55 assertions for conversion
rejection, including a foreign `Seqable`; rejected results expose no partial
result or removed accounting counter.

The existing large-result control remains unchanged; its optional local run was
stopped after more than five minutes without a result and is not claimed as
evidence.

Pinned clj-kondo reports zero errors; the large SH01 file has 52 existing
warnings in unchanged portions and no finding on the added ranges. The smaller
changed reader and module-census files have no warnings. The source fragments
retain the repository's existing unresolved cross-file symbols when linted in
isolation. The language-boundary gate passes 6 tests and 11 assertions. These
are source and injected-fixture controls only.

## Conformance, acceptance and limits

The frozen source/test tuple is base `0e2b37cd2eefbab2f02e5898b960f9bbc4100fef`,
commit `82537d2cb6ec3bb0ac51f30e7c53f9ead2b59b96`, and tree
`4c7b0bb1422cb4170624249408a93515a64cad92`. The worktree was clean before
this documentation metadata commit. An independent affected review is still
required.

The controls do not prove a real Core construction, C5/C6 acceptance, C7/C8
composition, native behavior, migration authority, release readiness or
self-hosting. Historical failed attempts and source pins remain history; no
retry or blind fingerprint refresh is implied.
