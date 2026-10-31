# Types: hosted seed product builders

## Scope and contracts

D1, D2, D3, D8, D9, L5, L17, C1, C7, C15, BOOT8 and TEST2 govern this
extraction. Seven existing implementations in gravity.c7-type-checker.inference
move to gravity.types: literal-type, node-operator, node-type, type-fact,
type-environment, constraint-ledger and function-table. Existing arities and
compatibility interposition remain intact.

Inputs are owner-supplied native hosted Core nodes, explicit classifier
callbacks and type-fact data. Outputs are the existing seed classifications,
node/type associations, ordered assignment constraints and function signature
data. These are partial hosted compatibility products, not canonical L5/C7
typing proofs. Fixed Dynamic, layout, ownership, throws and status labels do
not prove the corresponding legality or authority. No semantic expansion or
accounting requirement is proposed.

Gravity C7 remains the semantic owner. Source acquisition, full hosted artifact
assembly and existing C7 verifier diagnostics remain with their current owners.
The new stage consumes no Bootstrap, CLI, release or downstream checker API.

## Development control and remaining evidence

This branch starts from integrated main
f32f11bd12470e23f0ac8f4e38929755b82008b5. Before implementation, record paired
actual hosted source artifacts and a malformed-constraint verifier rejection.
Then compare complete artifacts, products, diagnostic records and deterministic
identities after extraction, with positive/negative and callback interposition
tests, affected regressions and the language boundary.

This early draft still requires implementation, actual before/after evidence,
focused checks, independent exact-candidate review, applicable final-head CI and
clean current-main integration preflight. Namespace ownership is partial; no
canonical type soundness, later legality, migration, release, self-hosting or
seed retirement is claimed.
