# Accounting removal plan

Baseline: main `176c4ee65bb3ceb545f51edfaa599902030a0cb7`, tree
`a887eae096998881ef3aef4fa18a6458ebb6ad20`, fetched 2026-10-26.
Use an isolated checkout; never update another writer's checkout.

## Exact owner map

| Alias | Repository paths | Current ownership |
| --- | --- | --- |
| C7 | `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` | Existing PR359 writer through current guard and Map/List publication; explicit release pending |
| Effects | `bootstrap/gravity/src/gravity/effects.gravity` | Same PR359 writer |
| Emitter | `bootstrap/gravity/p15_s23/emitter.gravity` | Same PR359 writer |
| Runtime | `bootstrap/gravity/src/gravity/runtime.gravity` | Accounting audits; actual writer identity unresolved before edits |
| MIR | `bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity`; `bootstrap/clojure/test/gravity/self_hosting/{sh12_c10_mir_adapter_test,sh12_literal_bool_specialization_test,mir_scalar_admission_test}.clj` | Existing PR378/421 owner |
| Native | `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh00-sh02/` Native adapter components and existing Native tests | Existing PR383/parameter writer; exact component paths to confirm |
| Opt/lower | PR370 producer/consumer source and tests | Existing PR370 writer; exact paths/remaining acceptance to confirm |
| Syntax | `bootstrap/gravity/src/gravity/bootstrap/syntax.gravity` and host syntax adapters | Accounting classification only; writer not established |
| Canonical schema | `bootstrap/clojure/src/gravity/self_hosting/a1_canonical_schema.clj` and `a1_canonical_schema/*.clj` | Accounting classification only; writer not established |
| Macro expansion | `bootstrap/clojure/src/gravity/macro_expansion/engine.clj` and related policy/tests | Accounting classification only; writer not established |

## Implementation order

Inventory and classify while PR359 finishes its already-started slice. After
explicit release, reconcile to its published head and diagnose LR on that exact
tree. Remove the three `lr-derive` premise `type-work` fields together only after
checking schema identity and invalidation; do not zero consumer telemetry.
Preserve `member-work` until independently classified and both checked extents.
Then advance disjoint eligible descriptor, recursive-validation, inference and
module work with finite per-function transactions. Coordinate runtime before
editing its cost hook producers/consumers. Existing Native, MIR and optimization
owners continue their slices rather than receiving duplicate implementations.

For each transaction: reproduce the actual owner defect; review a concrete
minimal patch; run cheap harness/serialization/saturation controls first; run
appropriate semantic and integration checks under existing resource controls;
independently review the immutable result; publish exact draft metadata and
read back the head; integrate only accepted completed work. Reuse unaffected
immutable evidence and record invalidation when dependencies differ.

## Retained versus unclassified controls

Retain source/callee identity, authenticated input reconstruction, equality,
map parity, valid indices/arity, every-member validation, Unicode scalar rules,
native class/metadata/callback safety, collision checks, target representability
and effect/capability authority. Checked extents record semantic coverage and
must agree with the actual full traversal. Counts alone do not prove traversal.

Retain real process resource controls. A variable called fuel/depth is not
automatically safe or removable: trace its user-visible semantic purpose.
Runtime recursion containment and MIR supported CFG-family restrictions require
their exact owner justification. Runtime equality/container depth 128, syntax
graph limits, macro depth 16 and canonical-schema work reservations are still
unclassified; they cannot be declared permanent safety requirements from names
or historical bounded-candidate decisions alone.

New implementation names use semantic subsystem/behavior words. Planning IDs
remain here; compatibility-bearing schema and diagnostic IDs are not renamed
incidentally. No shared runner/receipt/schema edits without one agreed writer.
