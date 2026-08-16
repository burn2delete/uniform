# SH-07 Core Lowering Fixtures

Every source case has a byte-identical co-canonical `.gravity` / `.qst` peer.
The physical path and extension are provenance only and do not participate in
semantic core identity.

## Accepted pairs

- `macro-def-fn-literals` exercises SH-05 `defn` expansion followed by SH-06
  binding resolution and SH-07-A lowering of `def`, `fn`, `quote`, and the
  supported scalar literals.
- `quoted-carrier-payloads` exercises ordinary and quoted collections whose
  semantic values and map keys resemble the private SH-07 digest-reference
  carrier. Those payloads are language data and must remain unchanged.
- `latent-function-order` exercises a function body with nested collections.
  Body nodes belong to the function-body evaluation region; creation of the
  function does not evaluate the body.
- `control-flow-order` exercises nested `if` and nonempty `do` lowering with
  condition-first, branch-exclusive, and left-to-right evaluation records.
- `control-flow-truthiness` exercises the L2 rule that only `nil` and `false`
  are falsey.

## Rejected pairs

Rejected fixture metadata below `[:sh07]` is an expected-result oracle only.
It is not executable input and must not select or dispatch a failure. The
dedicated adapter must derive a genuine SH-05/SH-06 product from the source and
exercise the stated lowering or verifier boundary directly.

| Base name | Expected rule | Rejection family |
| --- | --- | --- |
| `lowering-gap` | `C6-LOWERING-GAP` | unsupported `try` in the SH-07-B4 subset |
| `core-shape` | `C6-CORE-SHAPE` | function parameter form is not a vector |
| `missing-origin` | `C6-ORIGIN` | generated `defn` output loses its origin |
| `unauthenticated-projection` | `C6-VERIFY` | SH-06 projection authentication fails |
| `if-missing-branch` | `C6-CORE-SHAPE` | `if` omits its else branch |
| `if-extra-branch` | `C6-CORE-SHAPE` | `if` carries an extra branch |
| `empty-do` | `C6-CORE-SHAPE` | empty `do` is outside the current subset |
| `nested-def` | `C6-CORE-SHAPE` | `def` appears below the top-level boundary |

These fixtures cover only the current SH-07-A/B1/B2 `def`, `fn`, `quote`,
`if`, nonempty `do`, reference, symbol-headed call, literal, collection,
evaluation-order, control-flow, origin, and verifier boundary. They do not
claim the remaining core forms, C7-C10 facts, MIR lowering, seed retirement,
or release readiness.
