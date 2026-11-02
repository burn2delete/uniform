# PR568 current-main checked bare-Set equality port

This report records the bounded current-main implementation of the checked
bare-Set equality (DSE) producer and consumers. It preserves the original
PR568 source-review preparation as historical evidence; it does not claim
source-authenticated, native, runtime, migration, release, self-hosting, or
whole-PR502 completion.

## Immutable identities and composition

| Item | Commit | Tree |
| --- | --- | --- |
| Original PR568 head | `f4c9fdcd5a3ca24be5b8e24a72ad9d908800796a` | `71b343d4cc131ef3be1601c8f8ba12509744b31e` |
| Original PR568 authored base | `ec085f5feb9f0f9719c6393e8b3f7d9b62d89eec` | `66304198a8c44f59adc4349030541fd26d674e29` |
| Current-main base | `dbeda7c7dc9a7bb880baa90af1a89046828db327` | `576075197b5b4e815b78bae7d949bfb5fe2d0437` |
| Current DSE candidate | `0572cc602732d8a6393c3066719f6afb3f4bcc98` | `de165b642e676ce0e118699fad49748472829dc6` |

The candidate is on `codex/review-568-current` in
`/Users/matt/.codex/worktrees/review-combined-454-462/uniform`. Its three
implementation commits are `ce2eb75b7d9b3108c4f7591aa26ee6fa033832f0`,
`badeb7313116545d2256c746a6d9613ba5de43c5`, and
`9a000fd9355510ea5d90a5dad8b13d90f4b6db0c`, followed by the focused guard
test commit `0572cc602`. The branch was created from the exact current-main
commit; no obsolete production tree was replayed.

## Current useful implementation

The port adds one source-bound DSE implementation to each current owner:

- C7 `types-set-equality-*` uses the current eight-argument derive/header
  shape and reconstructs the source node, call, edge, binding, owner record,
  forms, profile, target, type result, constraint and fact.
- Effects `effects-set-equality-*` uses the current eight-argument header and
  rebuilds the same source joins and effect/fact products.
- P15-S23 `emit-set-equality-*` uses the current eight-argument header and
  two-argument typed-node/child lowering. It emits the existing binary `=`
  builtin in source operand order; it adds no runtime implementation.

All three owners validate complete native DATA with their existing read-only
carrier/view/list helpers, reject unsupported comparator/classes, metadata,
malformed views and cycles, and preserve profile, target, effects,
capabilities, provenance, native-class and callback guards. The descriptor
has no work, budget, quota, debit, reservation or replacement-accounting
field. It supports the existing finite homogeneous Keyword-or-String runtime
domain and reports non-invoked Set/Never transfers without inventing a member
refinement. Existing BKA/KSE dispatch and fallback behavior remains in place.

The current row/fact census distinguishes absent fields on ordinary owner
records from reserved DSE fields. Ordinary rows and facts from other owner
families are retained; malformed or forged DSE rows/facts, duplicates,
orphans, stale source joins, wrong domains, and descriptor mismatches reject.
This is a semantic guard, not a general ledger or runtime admission system.

## Changed paths and original-path disposition

| Path | Disposition |
| --- | --- |
| `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` | current C7 DSE producer and joins added |
| `bootstrap/gravity/src/gravity/effects.gravity` | current Effects DSE producer and joins added |
| `bootstrap/gravity/p15_s23/emitter.gravity` | current emitter DSE producer, census and lowering added |
| `bootstrap/clojure/test/gravity/types/set_equality_test.clj` | three-owner source/data model controls |
| `bootstrap/clojure/test/gravity/types/set_equality_joins_test.clj` | actual current C7 -> Effects -> emitter mixed-row/fact controls |
| `bootstrap/clojure/test/gravity/self_hosting/descriptor_reconstruction_test.clj` | current descriptor parity and tamper controls |
| `bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj` | removes two stale negatives that are now valid DSE type pairs |
| `bootstrap/clojure/fixtures/self-hosting/sh-08/preserved/local-runtime-bare-set-equality.gravity` | original PR568 staging fixture retained only in the historical PR object; not copied because its selector used retired APIs |
| `bootstrap/clojure/fixtures/self-hosting/sh-08/preserved/local-runtime-bare-set-equality.qst` | same historical-only disposition |
| `bootstrap/clojure/test/gravity/types/set_equality_source_check.clj` | historical preparation selector not replayed; its old 65536/API route is not current evidence |
| `bootstrap/clojure/test/gravity/types/set_equality_source_check_test.clj` | historical preparation controls replaced by current focused controls above |
| `specs/accounting-removal/set-equality.md` | historical PR568 source-review record remains at its original immutable commit; this current report is separate |

The PR631 descriptor control was adapted to current C7/Effects/emitter
interfaces and has no old work allowance. The PR633 mixed controls were
adapted into the current join test. Their historical source identities and
nonclaims remain in their original Git objects; this port does not claim
those old PRs were merged or that their full original scopes are complete.

## Focused evidence

| Check | Result |
| --- | --- |
| `gravity.types.set-equality-test` | 6 tests / 233 assertions, 0 failures, 0 errors |
| `gravity.types.set-equality-joins-test` | 2 tests / 112 assertions, 0 failures, 0 errors |
| `gravity.self-hosting.descriptor-reconstruction-test` | 2 tests / 17 assertions, 0 failures, 0 errors |
| BKA compatibility selector | 1 test / 25 assertions, 0 failures, 0 errors |
| KSE compatibility selector | 1 test / 122 assertions, 0 failures, 0 errors |
| Effects BKA selector | 1 test / 134 assertions, 0 failures, 0 errors |
| Effects KSE selector | 1 test / 96 assertions, 0 failures, 0 errors |
| Existing finite runtime equality control | 1 test / 104 assertions, 0 failures, 0 errors |
| `git diff --check` | passed |

The finite runtime selector exercises the existing main runtime provider; it
does not establish a new DSE runtime implementation. The source/data and join
tests execute current Clojure model/owner helpers and current stage-2 emitter
plans with finite synthetic contexts. They do not establish authenticated
Reader/Core/C5/C11/C12 composition, paired fixture consumption, native-C or
restricted host execution, migration authority, release or self-hosting.

The exact candidate tuple is the commit/tree/base shown above. The checkout
was clean at the final commit before this report was added; after committing
this report, recompute the final tree and clean-status tuple before integration.
Coordinator review, current-main integration preflight and any broader
acceptance remain required.
