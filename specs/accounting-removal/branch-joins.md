# C7 branch-join source successor

Planning: ACC-01/06/07/12/13. Accountable implementation owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. This finite PR502 successor is
stacked on PR593 at `c8f16877f822959b7a024aed6efefad511bfb868`, tree
`4dea01d8d3073b57a1dfd026068ffe71bd9080f2`. Parent and candidate acceptance
remain separate; neither is an accepted current-main implementation.

## Contracts and source changes

[L5](../../docs/phase-01-core-language/015-l5-type-system-specification.md)
requires independently established List and nil-bearing branch types, retained
actual branch facts and source-bound compatibility. Empty Lists do not invent an
element type; expected return declarations cannot supply missing branch facts.
Exact and Never rules remain in force. [C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md)
and [D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
require complete source/type relationships and evidence appropriate to each claim.

The C7 source restores the preserved nil/List join APIs from PR502 head
`6a4f87f013d0076b81aba9e0667650e70d28e635`. Seven complete forms, including the
unchanged atomic-type helper, match the historical definitions exactly. Type
joins, positional-member validation, normal-type selection, source derivation and
deferred scheduling no longer take a join allowance or return work counts.
The former 1,024-member width, work refusal and exact-payment premises are
removed. Checked-member counts, independent member agreement, actual branch
types, nil alternatives, source/evaluation/binding checks and invalidation remain.

`sh08-ft-infer-pass-products-prepared` calls the new three-argument step and no
longer charges conditional join work in scheduling. Real source-join failures
retain C7-VERIFY. Other inference, lexical, loop and association accounting is
unchanged and remains open; no whole-inference removal is claimed.

Effects and emitter implementations already contain later nil-or semantics.
They are not replaced with older preserved implementations. Their separate
accounting and any remaining width restrictions are outside this C7 increment;
wide C7 acceptance cannot establish wider cross-stage acceptance.

The restored internal names retain their historical `sh08-*` identities as legacy
naming debt and caller reconciliation. No producer, schema or diagnostic ID is
renamed. The independent source mismatch rule is preserved; the removed
join-work-limit reason is accounting history rather than a current semantic rule.

## Tests, callers and parameter-choice disposition

Five preserved SH08 tests are restored with complete bodies: nil join types,
nil join inference, List source joins, List join inference and List join types.
They preserve positive and negative member/source/condition/Never/unknown
controls while removing exact-payment and budget-only rejection assertions.
Six direct calls in the existing SH09 and stage2-runtime test files drop only
the retired C7 derive allowance argument. Those two files have no other changes;
Effects/emitter arguments and runtime implementation remain unchanged.

[The census](branch-joins-census.edn) records the seven historical forms, both
removed APIs, all direct source/test call arities, five complete test bodies and
the six dependent-call edits. All other parent definitions remain unchanged
apart from the named inference scheduling caller. Reader-generated anonymous
symbols are normalized only for test-form comparison; assertions, constants,
argument lists, types and schema values are not normalized.

The separate ordinary-parameter-choice delta needs no code rewrite: the complete
historical helper body appears exactly once, inline, in current
`sh08-ft-call-constraints-with-context`. That caller remains unchanged. The
census pins the helper expression and complete current caller hashes. This is a
source-grounded incorporation proposal pending independent disposition, not a
whole-caller equivalence or fresh semantic acceptance claim.
The unchanged caller correspondence is also checked at freshly observed main
`e3d990e9c6594f140a85397fd8e16a80078a4f10`. Its later tooling changes have not
been composed into this stacked candidate; current-main integration remains open.

## Observed checks and finite remaining acceptance

Pinned local Clojure source validation passed: complete-form conservation,
unique definitions, exact historical forms, direct call arities, test bodies,
restricted dependent edits and absence of retired C7 join APIs. The generator
and exact invocation are retained locally; its digest is in the census. No
Gravity form is evaluated by that comparison.

`clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`
passed 6 tests / 11 assertions. Contextual clj-kondo with `tools/ci/kondo`
reported zero errors / 223 warnings / one information finding across the three
changed Clojure files, exactly the parent's finding multiset. Canonical docs,
full-language roadmap and whitespace checks passed. No Bootstrap, compiler,
Effects/emitter, runtime, broker or held selector was launched locally.

Keep draft pending coordinator-routed independent source/result review and the
applicable execution assessment. No stopped review is replayed or rerouted.
Genuine acceptance remains UNRUN:

1. Run the five C7 join kernels with the actual current implementation, including
   wide positive and late-invalid members, independent empty/homogeneous facts,
   nil families, unknown/Never scheduling and complete source/binding mutations.
2. Run the affected SH09 and typed/runtime composition selectors using the new
   C7 derive arity. Preserve complete row/fact/type joins, coherent tampering and
   actual runtime values; do not infer them from arity checks or C7 helper success.
3. Independently disposition the exact inlined parameter-choice correspondence.
   Retain any unrelated ordinary-caller/source-verifier obligations separately.
4. Consume applicable CI, compose accepted dependencies with current main, review
   changed composition and integrate only exact accepted identities under the
   governing lifecycle. Routine CI is not the held semantic acceptance.

PR502, the affected ACC criteria, cross-owner accounting, source authentication,
release and self-hosting remain open or unclaimed.
