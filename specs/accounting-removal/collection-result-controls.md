# Preserved collection result controls

This ACC-01 test-only change restores semantic assertions missing from current
main `0f508ea87b75ce0718000d11d588c191c2123838`. Original source is PR502 head
`6a4f87f013d0076b81aba9e0667650e70d28e635`, tree
`f8440ffcf2d2fa7e2f898178ee299ac64ef59d32`. The immutable 29-path inventory and
its original counts remain unchanged.

The governing [C7 contract](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md)
requires valid type facts and preserved derived constraints. [Development
verification](../../docs/development-verification.md) requires complete semantic
validation and late-invalid refusals independently of removed accounting. This
draft supplies missing genuine test definitions; it grants no execution or
acceptance authority.

## Exact change

In the existing vector-rest cold test, restore the original assertion that its
derived result is accepted by `sh08-lc-validate-types`. This checks the complete
`{:status :accepted}` result, not a selected field. Restore the original positive
depth-192 case alongside current depth-128/129 checks. Current width-1025 inputs
already participate in the full result/descriptor/consumer case table; retain
that stronger coverage rather than duplicating the historical standalone width
assertion. Current late-invalid member and result-shape assertions stay intact.

In the existing List-prepend test, restore the complete original nested-type
block byte for byte: depths 127, 128 and 129; exact result, descriptor return and
descriptor result-type equality; and the equally deep unknown-leaf rejection.
This strengthens the current depth-128 result check and preserves its existing
depth-127 positive case. All other tests, cases, mutants and bytes are unchanged.
No production source, shared helper, accounting mechanism or outer interface
changes. Existing accounting-only tests and mechanisms remain separately open;
they are not endorsed or restored by this patch.

[The census](collection-result-controls-census.edn) records exact before/after
interval hashes, copied assertions and the original nested block, the unchanged
C7 source hash and five direct dependency definition/argument-list pins.
Other dependency semantics and genuine results are not inferred from hashes.

## Verification and remaining gates

Reader-only checks verify the exact two test edits, original-source provenance,
unchanged other definitions and unchanged production bytes with evaluation
disabled and Bootstrap absent. Language-boundary, document, roadmap, static lint
comparison and staged/full parent-relative whitespace results are recorded with
the candidate. The actual two genuine owner tests are **UNRUN**; their previous
receipts are not results for this candidate.

Observed local checks: language boundary 6 tests/11 assertions; canonical docs
and roadmap passed. Pinned clj-kondo 2025.10.23, reproducible mode/cache disabled,
finds zero errors and 71 warnings on both complete base/candidate test inputs.
Complete finding multisets match after source-position fields are removed;
there are no new findings or suppression changes. This local comparison is not
a claim that the separate hosted canonical CI gate passed.

Independent source review, genuine semantic acceptance where permitted,
current-main reconciliation, applicable CI and coordinator integration remain
open. No source-authentication, Effects/emitter/runtime, whole-file incorporation,
ACC-01 completion, PR502 closure, release or self-hosting claim is made.

The changed test names and helper names are existing legacy interfaces; no new
implementation name is introduced. The retained untracked lint cache is not
deleted, moved or excluded, and this draft makes no clean-preflight claim.
