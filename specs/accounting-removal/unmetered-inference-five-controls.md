# Unmetered inference: five finite controls

## Purpose and scope

C7 and D9 require honest inference observations and evidence scope. The two
new test namespaces retain the original five-control panel without changing
production inference or introducing an allowance, round guard or cycle solver.
The original controls come from commit `6a8b6855`; their only adaptations are a
distinct namespace, an updated API comment and removal of three retired round
arguments. The static contract checks descriptive method metadata and its
source structure without evaluating Gravity.

## Observed result and limits

At `335a4978cd61bf7485d4df51c089a5346fd64381`, five exact selectors ran once,
sequentially, in fresh JVMs: 5 tests / 24 assertions, zero failures or errors,
five natural zero exits. Ten clean bookends corroborated sixteen input pins.
Independent RESULT acceptance is retained as
`acc07-five-result-review/receipt.edn`, SHA-256
`2dcad3a746309f17e5902c1819809c959d02d73986e7fbdd59e334f39d1089df`.
The underlying result is `acc07-five-actual-RESULT.edn`, SHA-256
`6c1578a0653cd04e877f2cdbd61a4f9de04cbc940f7fdf721a7e587dfab16c76`.
These receipts are retained outside the repository; their hashes identify the
accepted evidence and do not themselves authenticate a compiler product.

| Control | Observed assertions | Limit |
| --- | --- | --- |
| Constructor growth | Four pass steps; five exact, distinct binding states | Internal DATA, not a full solver call or admitted source |
| Alias oscillation | Two pass steps swap bindings and return to the seed | Internal DATA, not a full solver call or admitted source |
| Acyclic chain | Converged integer calls; binding/type/return replay equality | Selected fields, not a complete artifact replay |
| Unresolved fact | Converged status, Unknown type and first unknown node | No profile admission or complete diagnostic claim |
| Return conflict | Conflict status, rule, reason, expected Integer and actual String | Asserted first diagnostic fields only |

Boundary 6 tests / 11 assertions and static source checks 2 / 15 are separate
from those five semantic results. The six static negative cases alter the
source projection DATA; they are not complete production AST mutations.
Timing records are nonauthoritative development telemetry.

## Current-main composition

This composition starts from main
`0be1c7d4ff8f4a0bc1acba68c9799e6f461be2e7`. It preserves all existing main
content, including the checked-core assembly extraction. C7, the existing
SH08 harness and the direct compiler/runtime inputs match the accepted test
candidate. The checked-core namespace, upstream guard, constructor and verifier
changed since that candidate. Their actual input delta remains explicit in
the frozen composition packet; the old result is not relabeled as execution
of this new tuple. Publication and configured CI remain separate decisions.

## Acceptance and remaining criterion

The source and finite fixture RESULT were independently accepted with their
stated limits. Current-main applicability and publication intake are pending.
The static source contract distinguishes observed state equality from the
existing narrow checked recursive rule; neither is a general termination proof.

Source admission of the feedback graphs requires its own construction and
binding premises. The local-let source seam is examined separately; no growth
or oscillation fixture is sent through the full solver to establish admission.
Historical `753a779` and `526b6e7` dispositions remain scoped history. General
growth/cycle convergence, genuine authenticated C7 product evidence and whole
ACC07 completion remain open. ACC06's thirteen-test and UTF16 preservation
requirements are separate.
