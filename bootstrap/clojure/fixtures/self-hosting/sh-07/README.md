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

## Rejected pairs

Rejected fixture metadata below `[:sh07]` is an expected-result oracle only.
It is not executable input and must not select or dispatch a failure. The
dedicated adapter must derive a genuine SH-05/SH-06 product from the source and
exercise the stated lowering or verifier boundary directly.

| Base name | Expected rule | Rejection family |
| --- | --- | --- |
| `lowering-gap` | `C6-LOWERING-GAP` | unsupported `if` in the SH-07-A subset |
| `core-shape` | `C6-CORE-SHAPE` | function parameter form is not a vector |
| `missing-origin` | `C6-ORIGIN` | generated `defn` output loses its origin |
| `unauthenticated-projection` | `C6-VERIFY` | SH-06 projection authentication fails |

These fixtures cover only the current SH-07-A `def`, `fn`, `quote`, literal,
collection, evaluation-order, origin, and verifier boundary. They do not claim
the remaining core forms, C7-C10 facts, MIR lowering, seed retirement, or
release readiness.
