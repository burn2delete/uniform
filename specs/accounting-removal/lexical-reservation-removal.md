# ACC-07 derived-access lexical reservation removal

## Scope and original requirement

Base: `4ba50abe797bc7fe429f0dc3e5ec635d7ecc3e93`, tree
`f45910b43e6e3bdd0d22c87982081b96ddecbb10`.
Original ACC-07 requires removing inference allowances, debits and reservations
while preserving fixed-point, cycle/conflict and genuine fresh verification.
This finite source cut removes one enforcing reservation, not the whole family.

L2 retains immutable binding and initializer semantics. L5/C7 retain type,
source, proof and constraint obligations. The existing accepted v2 access proof
establishes index/range legality and derives a normal access type; it is not a
work allowance and is preserved.

## Source change

In `sh08-ft-infer-pass-products-prepared`, an already accepted get/nth access
with `:sh08-bounded-unit-step-v2` and a known non-Never type may immediately
advance its exact immutable lexical initializer bindings. Previously this also
reserved `2 * count(lexical-bindings)` work units, suppressed the update when
that reservation exceeded the remaining pool, emitted
`:bounded-unit-access-lexical-work-limit`, and added the reservation to the
returned work debit.

The reservation, limit predicate, refusal branch and debit are removed. The
existing `lexical-now?` predicate, selected-record traversal and
`sh08-ft-bind-lexical-types` call are unchanged. No policy, bound increase or
replacement cost proof is introduced. Other work consumers and outer inference
guards remain for their separate coherent removal work.

## Controls and preservation

The actual existing test definition
`sh08-bounded-unit-access-current-lexical-development-kernel` keeps its get/nth,
v1/v2, mutable/wrong-initializer, pending operand, Never and unvisited-binding
controls. Only its low-budget invocation, its work-count local used solely by
that invocation and its three reservation-refusal assertions are retired.
An added held case retains the derived type with unrelated lexical records and
requires the same remaining work count; this detects a lingering lexical debit.

New compiler-free DATA controls exercise both access kinds, unrelated record
counts, rejected/deferred lookups, wrong proof versions, opaque kinds, unknown/
Never types, mutable records, wrong initializers and stale access identities.
They retain unrelated bindings, diagnostics, constraints and observations.
The model only describes this local update for the Keyword/Bool fixture family;
it neither authenticates a lookup nor replaces general type/proof validation.

Exact source erasure checks must show that only this reservation family changed
and that every other C7 function is identical. The existing actual semantic
assertions must remain in source, apart from the three budget-only assertions.
Semantic source verification, conflicts, aliases, constructor growth, rounds,
fresh reconstruction and identity checks remain unchanged.

## Evidence boundary

Core Clojure inert-source/DATA, reader, boundary and diff checks are permitted.
The actual Gravity function and its test namespace, compiler, Bootstrap,
authentication, composition, runtime and native acceptance remain unrun/held.
No general convergence or finite-growth proof follows. ACC-07 remains Open.

Freeze the exact local successor and its mode/blob manifest for Accounting
intake, then one Reconciliation-routed independent affected review. No reviewer
dispatch, external publication, CI or merge is part of this source cut.
