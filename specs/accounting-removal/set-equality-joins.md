# Preserved bare-Set mixed-ledger controls

This ACC-01 test restoration depends on PR631 at
`df1688b26123247e2c20eaf218ebcf7592481b15`, tree
`f97d45e232e67cb3097b7c7fc54cd7e447202de5`, including unchanged PR568 DSE
implementation. Original source is PR502 head
`6a4f87f013d0076b81aba9e0667650e70d28e635`, tree
`f8440ffcf2d2fa7e2f898178ee299ac64ef59d32`.

[C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md),
[C14](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md)
and [development verification](../../docs/development-verification.md) require
source-bound facts and complete semantic validation independently of accounting.
PR568's Clojure source model does not establish the original genuine test results.
Its denied source selector remains stopped.

## Original obligations and adaptations

The original `sh01_checked_bare_set_equality_mixed_ledger_test.clj` is absent in
the parent. Restore its two semantic tests in the semantic namespace/path
`gravity.types.set-equality-joins-test`. Their eighteen assertion source forms
remain identical, including complete emitted instruction equality; this count
is a static source count, not executed assertions.

The positive test constructs ordinary rows/facts through actual C7 functions,
alongside the DSE producer. It selects both row orders and four vector/map fact
representations/orders, checks the selected complete DSE row and fact, and asks
typed lowering for the exact binary equality instruction. The negative test
retains explicit malformed ordinary tags, missing/stripped tags, duplicate and
orphan joins, wrong map keys, stale forms, evaluation order, binding class and
duplicate original source records. The opaque object retains its zero-callback
assertion. Coherent descriptor mutants rebuild rows and facts before lowering.

The old shared forgery helper is absent from the parent C7 test namespace. Copy
its entire body and parameters into this new test file as `descriptor-forgeries`:
all fifteen original descriptor mutants survive, including runtime policy,
family, refinement and source substitutions. This adds no production helper.

Fifteen explicit raw adaptations rename the namespace and DSE APIs, remove the
retired derive/census allowance arguments, remove work telemetry from diagnostic
printing, and call the local copied forgery helper. Outer typed-child lowering
keeps its existing interface. The payment/one-short test is removed; its only
semantic completion assertion already exists in the positive test's full case
table. No new allowance, zero counter or replacement accounting is introduced.

[The census](set-equality-joins-census.edn) records every substitution, original
file/helper/payment-test hashes, unchanged assertion forms, and eleven actual
dependency definitions/argument lists across three files. All parent files,
including production, model, fixtures and denied selector, stay unchanged.

## Validation and open gates

Source checks use the Clojure reader with evaluation disabled. Bootstrap stays
unloaded; the new genuine tests and all Gravity code are **UNRUN**. This patch
does not add a substitute Clojure model or launch a new acceptance route.

The language boundary passes 6 tests/11 assertions. Pinned clj-kondo 2025.10.23
with repository configuration, reproducible mode and cache disabled reports zero
errors, warnings or infos on the new test and census. Canonical docs, roadmap
and staged/full parent-relative whitespace checks pass.

Independent review, permitted genuine semantic acceptance, dependency/current-
main reconciliation, applicable CI and coordinator integration remain separate.
No authenticated input, C7/Effects/lowering/runtime acceptance, parent acceptance,
whole ACC-01, PR502 closure, release or self-hosting claim follows. Stable legacy
schema/diagnostic IDs and host helper references are preserved; the new file,
namespace and local helper use semantic names. The untracked lint cache stays
untouched, with no clean-preflight claim.
