# Vector-first inferred-call source disposition proposal

Owner: ACC-01. This proposal covers the original changed
`sh08-vf-inferred-call` definition and its sole direct source call. It changes
no production code. Independent classification remains pending.

## Exact source correspondence

Historical source is PR502 `6a4f87f013d0076b81aba9e0667650e70d28e635`.
Assessed main is `b1d590cc7edda61c81a53eeccbf879d7e4d2bcbf`. The companion
EDN binds complete file/function hashes and original-inventory membership.
The complete current function reconstructs from history by removing only:

- The `budget` argument, unused in the original function body.
- The local `used` binding initialized to zero.
- The outer LC-result work-limit refusal branch.
- The `:work-count used` fields in both semantic result maps.

Validation input ordering, complete LC validation, header derivation, finishing,
the applied-Vector-first kind test, complete operator descriptor equality,
result-type equality and both semantic rejection reasons are unchanged. No
number, identifier or diagnostic normalization is used in this reconstruction.

The sole direct caller drops its final payment expression, retaining every
other actual argument in order and matching the new arity. Its whole enclosing
function has other changes and is not declared equivalent. Removing the payment
expression is a policy disposition, not a claim that evaluating and omitting
it have identical behavior in every host numeric or exception context.

## Reused evidence and limits

The unchanged two-helper normal-return envelope from independent receipt
`5b7b28a3881386b00333f989711d173fdeca9a4a766046e3f4ab6ae9d65c467c`
excludes normal work-limit reasons and root work-count fields. Its helper and
complete assessed C7 source pins are rechecked. It supplies evidence for this
new proposal; its earlier caller decisions are not extended automatically.
Direct validation, ordered-child-type, source-builder and finish dependencies
are pinned with their historical equality or difference explicitly recorded.
Source identity does not establish transitive behavior.

The proposed decision rejects restoring precisely the enumerated accounting
and retired API. It preserves ordinary validation and semantic result fields.
C7, L2, D9 and the repository accounting-removal policy govern that decision.
Eight effective source corruptions refuse restored arguments, bindings,
refusals or telemetry, wrong descriptor kind, changed operator identity,
changed semantic diagnostics and a substituted validator. These are inert
source checks; no Gravity helper, Bootstrap, compiler, native provider or
runtime executes.

Independent review, canonical adoption and whole-scope completion remain
separate. Outer caller accounting remains unresolved and unendorsed. No
arithmetic/object identity, exception, termination, native, runtime, complete
Vector-first support or whole PR569/ACC01/PR502 acceptance is claimed.
