# Hosted seed arithmetic slice

## Contract boundary

This slice moves only the existing add, subtract, multiply and divide helper
bodies from Bootstrap component00587-02 into gravity.runtime. D1/D2/D3/D8/D9,
L2, C1/C15, SAFE9, R1/R4/R11/R12, BOOT8 and TEST2 govern the boundary. It
preserves temporary Clojure seed behavior and does not define Gravity numeric
modes through host-language assumptions.

The proposed public APIs are hosted-add, hosted-subtract, hosted-multiply and
hosted-divide, each taking the existing already-evaluated argument sequence.
They return the existing host numeric result or propagate its existing host
failure. They require only clojure.core. Bootstrap retains private compatibility
Vars, caller arity checks, error-to-diagnostic translation, execution context,
source/provenance, effects, capabilities and all authority checks. No admission,
provider authority, source loading or native target execution is added.

## Evidence requirements

Real paired Gravity source programs must compile to actual seed plans and
execute all four operators through the unchanged dispatcher. Positive controls
cover existing zero/unary/binary/ternary/variadic paths where legal. Division by
zero, overflow and illegal arity controls must retain complete actual diagnostics.
Full before/after source, plan, result and diagnostic records must preserve
provenance and recomputed deterministic identities.

Direct helper tests remain separate from genuine source controls. They cover
host result classes, exceptions, compatibility arities and Var interposition.
Whole-component conservation must leave context/authentication checks, remaining
helpers and the entire dispatcher unchanged. Registry and stage-map changes must
preserve other owners and stable helper identities. Independent exact-source
review, proportionate regression checks, own CI and clean current-main preflight
remain required before integration.

## Unsupported boundaries

This is temporary hosted seed arithmetic. It establishes no full L2/SAFE9 or
runtime-family conformance, numeric-mode selection, provider mapping, source/Core
authentication, new authority, native/backend execution, performance guarantee,
migration, release, self-hosting or seed retirement. Historical restricted
Reader/H6/native and default pinned routes remain unused. Scope publication
contains no implementation or acceptance evidence.
