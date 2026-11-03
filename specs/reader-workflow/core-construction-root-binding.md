# Current Core construction binding control

## Purpose and scope

This bounded source increment records the private construction invariant needed
when the selected execution binding is dynamically thread-bound. It is based on
current main `a5b6427cab15f40a8e1d61a6562a04b4a0f63efb`, and the frozen source
implementation is commit `c0bb8f0a8518e1ab535078e6830c87c566782039`, tree
`2a1ca714446f56c04fd1a9e4483872f024bf2ab2`. It changes only the 00897 guard,
the private 00898 construction fact, and their focused controls.

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

The two affected scalar Set controls pass separately: native set rejection is
42 assertions and conversion rejection, including a foreign `Seqable`, is 55
assertions. Rejected results expose no partial result or removed accounting
counter. The existing large-result control remains unchanged; its optional
local run was stopped after more than five minutes without a result and is not
claimed as evidence.

Pinned clj-kondo reports zero errors and warnings for the changed test files;
the source fragments retain the repository's existing unresolved cross-file
symbols when linted in isolation. The language-boundary gate passes 6 tests and
11 assertions. These are source and injected-fixture controls only.

## Conformance, acceptance and limits

The frozen tuple is base `a5b6427cab15f40a8e1d61a6562a04b4a0f63efb`, source
commit `c0bb8f0a8518e1ab535078e6830c87c566782039`, and source tree
`2a1ca714446f56c04fd1a9e4483872f024bf2ab2`. The worktree was clean before
this documentation commit. An independent affected review is still required.

The controls do not prove a real Core construction, C5/C6 acceptance, C7/C8
composition, native behavior, migration authority, release readiness or
self-hosting. Historical failed attempts and source pins remain history; no
retry or blind fingerprint refresh is implied.
