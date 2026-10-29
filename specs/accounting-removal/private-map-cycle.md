# Private Map entry-cycle accounting removal

This finite ACC-04 increment removes only the predicted graph traversal charge
from `sh08-pmk-acyclic`, `body-pmk-acyclic`, and
`p15-s23-pmk-acyclic`, and its debit in each owning `pmk-proof` caller.
It continues from PR534's indexing/type-view preparation; that accepted scope
does not cover this change or the remaining private-Map proof family.

The graph premise still rejects a path from the private function back to itself
with `:private-map-keys-entry-domain-required`. It computes the finite reachable
set to a fixed point. Shared descendants and disconnected cycles do not imply
private-function re-entry. Each nonterminal iteration adds a callee from the
finite input edge vector; no pass, depth, node or work allowance is needed.
The old function-record-derived pass refusal and predicted-cost gate are gone,
as are this premise's budget argument and work-count result. No numeric work
constant was increased or renamed.

## Semantic and consumer boundary

[STD3](../../docs/phase-16-standard-library/213-std3-collections-library-specification.md)
requires a complete private-entry proof without escape, mutation or re-entry,
whole Map type/member validation, actual normal-entry witnesses, source and
compatibility evidence, and no parameter refinement. C7 and C8 require current
typed facts and reconstructed constraints/effects. The current AGENTS accounting
removal instruction supersedes historical work-allowance admission clauses.

The actual crossings are:

| Owner | Cycle caller and descriptor path | Constraint/fact consumer |
| --- | --- | --- |
| C7 | `sh08-pmk-proof` -> `sh08-pmk-acyclic`; `sh08-pmk-header` -> proof | `sh08-pmk-derive`, `sh08-pmk-inferred-call`, `sh08-pmk-constraint`, `sh08-pmk-call-fact` |
| Effects | `body-pmk-proof` -> `body-pmk-acyclic`; `body-pmk-header` -> proof | `body-pmk-evidence`, row/fact censuses and private-entry evidence |
| Emitter | `p15-s23-pmk-proof` -> `p15-s23-pmk-acyclic`; `p15-s23-pmk-header` -> proof | `p15-s23-pmk-evidence`, row/fact censuses and private-entry evidence |

Only the cycle function and its call/debit change in each owner. Physical/native
ingress, source/census/parameter validation, permission, type/member domains,
normality and Never behavior, allocation/storage policy, full descriptor/row/fact
equality, and original-source reconstruction are conserved. In particular, a
coherently rewritten supplied descriptor/row/fact does not replace a fresh
descriptor reconstructed from original input. This finite premise adds no type,
shape, key-presence, extent, cardinality or specialization facts.

## Compiler-free controls

`gravity.types.private-map-cycle-test` reads actual source definitions with
read-eval disabled and evaluates only the selected Clojure-compatible models.
It does not require Bootstrap or compile/execute Gravity.

- All directed graphs on three vertices agree with an independent path search
  for each root in all three owners.
- A 140-function reversed chain accepts; its late edge back to the root rejects.
  Shared descendants, disconnected cycles and absent callees retain their
  original graph meaning. An unobserved opaque payload receives zero equality,
  hash or rendering callbacks.
- Actual Effects/emitter constraint/fact constructors and censuses reject stale
  proof fields, source-edge changes, changed permission, re-entry and coherent
  descriptor/type/row/fact forgeries. Their header and source/type permission
  predicates are explicit injected seams; this is consumer equality/control-flow
  evidence, not native/source/type-admission conformance.

The observed source-model run passed 3 tests / 4,638 assertions. Independent
source review and actual compiler composition remain separate gates. The source
conservation receipt records the exact modified definitions and unchanged
surrounding bodies. No old migration fingerprint is renewed.

## Remaining work and nonclaims

PMK physical/source/parameter/map-source reservations, metadata work/depth limits,
header cap, private Map compatibility work and outer inference/consumer pools
remain open. This does not complete PMK source/header/proof accounting removal.
Runtime enumeration/copy accounting is unchanged. ANE, DSE, general LQ/schema,
domain migration and other owners are outside this increment.

The existing hold on actual compiler/Native/runtime and composed semantic runs
remains. No host/broker/process probe, unknown historical operation, stopped PR
metadata update, self-hosting admission, seed retirement or release action is
performed. A passing source model cannot confer any of those outcomes.
