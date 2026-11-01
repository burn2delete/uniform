# Vector header preserved-source disposition proposal

Owner: ACC-01. This proposal covers the original vector-empty and vector-rest
header definitions in C7, Effects and lowering: six original production units.
It changes no compiler code and grants no independent acceptance.

## Complete source differences

Historical input is PR502 `6a4f87f013d0076b81aba9e0667650e70d28e635`.
Assessed main is `b1d590cc7edda61c81a53eeccbf879d7e4d2bcbf`. The companion
EDN pins complete files, original inventory membership, arguments and every
differing subtree. Applying exactly seventeen replacements reconstructs all
six current definitions, without renaming or numeric normalization.

Sixteen differences concern LC validator accounting: zero-charge expressions,
old validator charge extraction, old two-argument metered validation calls and
work-limit diagnostic choices. Six exact helper definitions, two per owner,
have conservative normal-return envelopes with semantic failure reasons and
no root work count or work-limit reason. The earlier independent C7 envelope
receipt is cited as prior evidence; its caller decisions are not extended.
Effects and lowering summaries require their own independent acceptance.

The proposed policy decision rejects restoring those sixteen accounting
subtrees. Existing caller budget limits, construction charges, work fields and
outer work-limit refusals remain visible and unendorsed. Arithmetic identity,
number-object identity, native callbacks, exceptions, termination, mutable
bindings and transitive behavior are not proved by the return envelopes.

## Separate C7 result expression

The seventeenth difference is C7's result type metadata: historical
`(vec (rest elements))` versus current `(subvec elements 1 length)`, inside
the unchanged zero-length branch. This is explicitly outside the accounting
decision. Independent review must decide conditional ordered metadata-value
correspondence under an established finite Vector input, whose length is the
member count. The zero case avoids slicing; positive length establishes
`1 <= length` and the suffix endpoints. Native view validation and the exact
vector-kind predicate are pinned as source dependencies, not execution proof.

STD3 governs Vector slicing and Vector rest. The header's `:element-types`
Vector describes the resulting List type; it is not the returned runtime List.
No allocation, physical sharing, backing retention, lifetime or runtime List
storage equivalence is inferred. The independent result-expression decision is
pending; it must not be silently folded into accounting removal.

## Validation and acceptance

Complete six-form reconstruction, argument and original-unit conservation,
six helper envelopes and retained support pins pass inert source checks.
Twenty-four header corruption controls refuse restored accounting, changed
arguments, lost semantic diagnostics and substituted source owners. Twenty-four
helper controls refuse introduced work-limit reasons, returned work counts,
unknown tail calls and unknown loop return values. These are source/data
checks; no Bootstrap, Gravity helper, compiler, native provider or runtime is
executed. The author checker's initial parenthesis error is corrected and
retained as history, not credited as a passing run.

C7, C8, C14, L2, STD3, D9 and current accounting-removal policy govern review.
Independent receipt
`73205201ca01ef77a5dca10811b015fff2857fc590a6d9f5b80771dd862dbbd4`
accepts rejecting restoration of the sixteen accounting remnants and holds
the one metadata correspondence. It independently assesses all six envelopes,
including four new Effects/lowering helper assessments. It passes 1,346
source/data checks, forty header corruption refusals and thirty-six helper
return corruption refusals. `vector-header-classification-consumption.edn`
rehashes both independent analyzers, binds the frozen tree and inputs,
reconstructs all six source equations and checks helper, support and policy
pins against unchanged current main. No reviewer checker rerun is claimed.

The hold requires an existing Gravity finite-List `vec` conversion contract or
a pinned qualified source correspondence for ordered values. The String
overload does not establish that List case. This is not an accounting rejection
or a requirement for allocation, physical sharing or runtime List-storage
equivalence. No host-language conversion assumption clears the hold.

The original machine proposal remains historical input. Canonical adoption
and whole-scope completion remain separate. The original
29-path inventory and stable ACC IDs are unchanged. No whole PR569/ACC01/PR502,
milestone, release or self-hosting credit follows.
