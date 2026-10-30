# Predicate caller accounting-remnant disposition proposal

Owner: ACC-01. This proposal covers exactly the original eleven predicate
families' C7 derive and inferred-call definitions: 22 original changed units.
It changes no compiler code and grants no independent acceptance by itself.

## Exact inputs and complete differences

Historical source is PR502 `6a4f87f013d0076b81aba9e0667650e70d28e635`.
Assessed main is `b1d590cc7edda61c81a53eeccbf879d7e4d2bcbf`, tree
`7c4c2733de6675701e2bd19b59b5dc10f7e9d82c`. The companion
`predicate-caller-remnants.edn` pins both complete C7 files, all complete
definitions, arguments, original-inventory memberships and exact differing
subtrees. It consumes the original predicate test's complete family table.

Every derive definition differs in exactly three subtrees:

- `(+ base 0)` becomes `base`.
- The LC validator work-limit choice of `C7-GENERIC` versus
  `C7-TYPE-MISMATCH` becomes the original semantic mismatch code.
- The LC validator work-limit choice of a family accounting reason versus its
  operand-type-required reason becomes the original semantic reason.

Every inferred-call definition differs in exactly two subtrees: the same
zero-charge expression and family reason choice. There are exactly 55 listed
differences. Applying only those replacements reconstructs each complete
current definition, with all other source and arguments reader-identical.
No numbers, names, arguments, schemas or guard expressions are normalized.

## Reused helper evidence and proposed decision

The independent validator receipt
`5b7b28a3881386b00333f989711d173fdeca9a4a766046e3f4ab6ae9d65c467c`
accepted only its original four callers/eight remnants. This new proposal
reuses its unchanged two-helper normal-return envelope as evidence, and does
not extend that caller decision to these 22 functions.

The whole current C7 file equals that receipt's assessed source. Both pinned
helper definitions match. The conservative envelope includes the six semantic
failure reasons, excludes normal `:work-limit` reasons and contains no root
`:work-count` result field. Each new caller binds `validation` once from one
direct `sh08-lc-validate-types` call with one argument; its helper name is not
locally shadowed. The inspected caller forms contain no assignment or nested
function that would invalidate that binding observation.

The proposed policy decision rejects restoring exactly these 55 accounting
remnants and retains every other complete caller form. It does not claim
arithmetic or number-object identity equivalence. The envelope remains a
normal-return overapproximation under pinned, unrebound bindings and L2
control semantics. Termination, native behavior, exceptions, mutation and
transitive effects remain unproved.

Outer caller budgets, their own refusal branches, work fields and other
capacity checks remain visible and unresolved. Their accounting reason names
may therefore still occur in the current functions. This proposal neither
approves retaining those mechanisms nor claims whole predicate accounting
removal. It concerns only the separately enumerated LC-result alternatives
and zero-charge expressions.

## Source validation and remaining acceptance

All 22 memberships, complete definition/argument pins, direct validator
bindings and 55-subtree reconstructions pass. Five effective source mutations
per caller are refused: restoring one remnant, changing retained arity,
substituting the source builder, changing a semantic diagnostic and changing
a retained integer representation. These are 110 inert source corruption
controls; no source helper or target program is executed.

Independent classification is consumed in
`predicate-caller-classification-consumption.edn`. Sealed receipt
`f29b9851934699293de651ca8552e5fdc833f5d2024b28d4d48fe6b0e2678ba0`
accepts rejecting restoration of exactly these 55 remnants after 3,928
source/data checks and 187 independent corruption refusals. Receipt, checker,
input/tree identities, all 22 complete form reconstruction equations, helper
pins and policy pins were rechecked against current main
`b1d590cc7edda61c81a53eeccbf879d7e4d2bcbf`. The evidence branch's whole C7
file differs from assessed main; all 22 selected forms match. No independent
checker rerun is claimed. The original machine proposal's pending status is
retained as historical input; the later consumption records its finite decision.

Canonical adoption and whole-scope completion remain separate. C7, L2, D9 and the repository's
accounting-removal policy govern the requested decision. The original 29-path
inventory and all earlier receipts remain unchanged. Genuine owner controls
are UNRUN. No whole ACC01/PR502, release or self-hosting claim follows.
