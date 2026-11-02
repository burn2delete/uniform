# Operand-work descriptor retirement

Planning: ACC-01/04/09/12. Accountable owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. This standalone PR502 successor
starts at current main `8f9af1be55f99a076992086ac95f91364a22924e` rather than
depending on the earlier ACC-01 source stack. It removes one obsolete exported
accounting field coherently across its three actual source owners.

## Contract and implementation

[C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md),
[C8](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md),
[C14](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md)
and [D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
require complete current type/source/effect artifacts, independent reconstruction,
full equality and honest evidence. Accounting-removal instructions require
retiring artificial work metadata and payment-only assertions without inventing
replacement accounting or dropping semantic fields.

The only production edits delete `:operand-type-work (get validation :work-count)`
from `sh08-st-source`, `body-st-source` and `p15-s23-st-source`. Each complete
resulting definition equals the preserved definition at PR502 head
`6a4f87f013d0076b81aba9e0667650e70d28e635`. All other production definitions,
parameter lists, result fields, checked extents, policies, source relationships,
effects, capabilities, diagnostics and dispatch identities remain unchanged.
There is no new fee, constant, unbounded sentinel or renamed accounting field.

All three complete source bodies agree under the census's explicit helper-name
correspondences. This does not infer helper equivalence or actual cross-stage
execution. The C7 inferred-call, Effects call-validity and emitter typed-compile
consumers are unchanged and retain their complete reconstructed descriptor
comparisons. The scan of tracked Gravity and Clojure implementation sources
finds no remaining literal `operand-type-work` reference; this is a literal
reference observation, not a general proof about generated code or call graphs.

All 33 current predicate source builders also omit the field. Five retained
runtime-test assertions incorrectly required predicate operand-work totals;
they now require absence instead. Every other byte in that test file is
preserved, including source/type, effects, runtime value, transfer, arity,
provenance and forgery controls.

## Focused test changes

The two existing C7 String/Keyword Vector-to-Set kernels now assert the field is
absent and submit coherently typed operator descriptors with zero and nonzero
obsolete-field values to the actual inferred-call checker. They expect the
existing source-mismatch rejection. This uses the existing genuine helper ABI;
no production logic is modeled in Clojure.

In these two edited kernels, exact-payment and one-short assertions are retired,
and the quota-only 1,025-member invalid sample is removed. The Keyword input
policy assertion preserves version, family, element type, output type and output
refinements while ceasing to require the old maximum-input field. Every other
independent original assertion and malformed case remains. Positive samples,
Never behavior, allocation/effects, checked element count, source/owner joins and
constraint relationships are retained. Other work APIs, width enforcement,
value policies and runtime accounting still exist and remain open; this is not
complete Set-conversion accounting removal or proof of wider input support.

The existing Effects controls already regenerate complete rows/facts from a
descriptor containing `:operand-type-work 0` and require rejection. Existing
emitter/lowering controls likewise include that forgery. Those test definitions
are unchanged and remain part of genuine affected acceptance.

## Observed evidence and finite acceptance

[The census](operand-work-census.edn) pins base/current source bytes, the three
historical forms, unchanged full-equality consumers, explicit source aliases,
C7 assertion conservation and five exact runtime assertion replacements. Local
Clojure validation reads forms with evaluation disabled. It verifies the three
field-only deletions, exact historical bodies, complete source parity, 33
predicate builders, retained semantic assertions/cases and no tracked production
references. The first two local validation attempts failed on a delimiter in
its case-vector helper; corrected source validation passed. Generator and exact
invocation remain local, with the digest in the census.

The language boundary passed 6 tests / 11 assertions. Contextual lint across the
two changed Clojure files reports zero errors / 147 warnings, exactly the base
finding multiset. Canonical docs, full-language roadmap and whitespace checks
passed. No Bootstrap, Gravity/compiler, authenticated pipeline, C12,
Effects/emitter execution, runtime, broker or held selector ran locally.

Keep draft pending coordinator-routed independent source/result disposition and
applicable execution assessment. No stopped review or denied operation is
replayed, transferred or routed through this draft. Genuine acceptance is UNRUN:

1. Run the two changed C7 conversion kernels, including empty/nonempty String and
   Keyword inputs, Never, checked extents, source mutations and both obsolete-field
   values. Record actual semantic results separately from remaining legacy quotas.
2. Run the existing Effects String/Keyword conversion source/consumer controls and
   typed lowering/runtime controls. Retain coherent row/fact regeneration, complete
   equality, provenance, effects, allocation and actual values.
3. Run the five affected predicate composition selectors and verify field absence
   without weakening their existing semantic and runtime controls.
4. Review the exact independent main-based source increment, consume applicable CI,
   compose current main and integrate only exact accepted identities. Old artifact
   or migration receipts do not automatically survive the descriptor shape change.

No full accounting, source-authentication, runtime, release, self-hosting or PR502
closure follows from source parity, a field scan or routine CI.
