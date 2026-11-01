# Hosted seed evaluation carriers

## Contract boundary

D1/D2/D3/D8/D9, L2, C1/C15, R1/R4/R11/R12, BOOT8 and TEST2 govern
this temporary seed extraction. Move the existing binder, non-tail value,
argument-vector, map-entry and instruction-sequence evaluation bodies from
component00590 into gravity.runtime. Preserve left-to-right evaluation,
vector/list paths, persistent results, recurrence checks, exception identity
and opaque invocation-scope forwarding. No algorithm or authority changes.

The proposed APIs are bind-values, execute-value-scoped,
execute-values-scoped, execute-map-entries-scoped and
execute-instructions-scoped. Inputs are existing trusted runtime/plan/env/
instruction carriers and explicit late operation Vars; outputs are original
values, persistent collections, sequence results or existing refusals. Runtime
owns carrier traversal and check placement. Existing owners retain instruction
semantics, recurrence class/token and diagnostics, compiler-internal dispatch,
source throw, native scopes and all admission. Dependency direction is
clojure.core to gravity.runtime to Bootstrap; no reverse import is allowed.

## Evidence obligations

Compile and execute paired real .gravity/.qst collection, binding, sequence and
recurrence fixtures; reject real operand failures at evaluation boundaries.
Compare complete source/plan/result/refusal records and public current Core
products in this same physical worktree, with original serialization and
recomputed identities. Direct host units cover vector/list differences,
duplicate evaluated map keys, scope forwarding, late callback lookup and
recurrence/exception ordering separately from source admission evidence.
Require focused/coupled, language/ownership, lint, contract validators,
independent exact acceptance, own green CI artifacts and a clean current-main
integration preflight before merging.

Initial source is PR754 candidate2fd142d2694df2bf1258903ceae588dd91bb0574,
tree b0a7201b064bc6fdbc76415130f09a884d13df4e. This isolated successor depends
on754 and cannot integrate until that predecessor lands and the actual main
base is reconciled and verified. Scope publication confers no implementation
or integration credit. Maintain this early draft as evidence develops.

## Unsupported boundaries

No portable Gravity collection/numeric/error semantics, full L2/R4/R12/C15 or
runtime-family conformance, untrusted carrier/operation ingress, new authority,
native/backend execution, performance claim, migration, release, self-hosting
or seed retirement. Preserve historical restricted routes, pins and guards.
