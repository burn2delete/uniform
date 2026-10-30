# Predicate source bodies under current validation guards

Owner: ACC-01. This records the independently reviewed conditional source
classification for exactly 33 original changed units, not an implementation
change or unconditional behavioral-equivalence result. The original 29-path
inventory is unchanged.

## Inputs and complete source equation

Historical PR502 is `6a4f87f013d0076b81aba9e0667650e70d28e635`.
Current main is `b1d590cc7edda61c81a53eeccbf879d7e4d2bcbf`, tree
`7c4c2733de6675701e2bd19b59b5dc10f7e9d82c`.
`predicate-guarded-incorporation.edn` pins all three complete source files,
the original inventory and every selected original/current definition.

Selection is the original predicate test's complete eleven-family table and
each family's C7, Effects and lowering source builder. Every selected unit is
verified as an original changed unit in the immutable inventory. All argument
lists match history. Each complete current body has exactly this structure:

```clojure
(if (NATIVE-MAP? validation)
  (if (= {:status :accepted} validation)
    HISTORICAL-BODY
    nil)
  nil)
```

The native helper is explicitly named and pinned for each owner. The whole
historical body is reader-identical at path `[3 2 2]` in the current definition.
Comparison uses complete serialized reader forms, without replacing numbers,
names, arguments, schema values, guards or historical inner logic. Raw comments
and token spellings are covered only by whole-file byte provenance; raw body
byte identity is not claimed.

## Proposed disposition and material change

Retain the current native-carrier and exact-cell guards and classify the
original inner source implementation as incorporated under those guards.
This proposal does not restore the historical permissive validation-cell
boundary. A status-accepted cell with extra fields is no longer admitted by
the exact-cell guard. This is an explicit input-contract difference, not
unconditional function equivalence.

All historical source, call, binding, evaluation-order, descriptor and query
logic inside the guarded branch remains intact. The record does not prove
native guard behavior, callback/exception behavior, termination, mutable var
behavior, transitive dependencies or the legality of other uses or numeric
conditions in the native helpers. L2, C7 and D9 govern those separate claims.
The pinned, unrebound bindings and L2 control/call semantics are assumptions;
no Gravity or Bootstrap definition is evaluated.

## Validation and remaining acceptance

All 33 complete source equations and original-inventory memberships pass.
Seven source corruptions per unit are refused: lost native guard, lost exact
cell guard, admission of a retired work field, changed arity, lost historical
body, changed inner result policy and changed inner integer representation.
The total is 231 inert source corruption controls, not runtime assertions.

Independent classification passes for exact `115dae50`, tree
`bf1d15b770b6763bc4032067662a8865e76db81e`, receipt
`e47a331ec7c613f6323e1d3dcad208b4adce6f690c9611413f4381800c93f9d2`.
The sealed receipt records 13,050 checks and 339 corruption refusals; the
reviewer's final handoff reports 13,052 checks. Only the sealed 13,050 are
credited, and both observations remain recorded. The 339 include all 231
authored cases independently reconstructed plus 108 additional controls.
The consumer verified the receipt, private artifacts, full evidence-tree
delta, all 33 equations, current file distinctions and policy pins. See
`predicate-guarded-classification-consumption.edn`.

The evidence commit and assessed main have different complete files in all
three owners; selected builder and guard forms match. Conditional source
classification retains the changed input contract. Formal disposition
adoption and any roadmap completion credit remain separate. The original
machine record's pending state is captured proposal history. The existing predicate correspondence/helper
records and PR625's restored test definitions retain their own exact scopes;
they do not prove this proposal's semantic or native boundaries. Genuine owner
controls remain UNRUN. No whole predicate accounting removal, ACC01/PR502
closure, release or self-hosting authority follows.
