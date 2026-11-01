# Hosted seed evaluation carriers

## Contract boundary

D1/D2/D3/D8/D9, L2, C1/C15, R1/R4/R11/R12, BOOT8 and TEST2 govern
this temporary seed extraction. Move the existing binder, non-tail value,
argument-vector, map-entry and instruction-sequence evaluation bodies from
component00590 into gravity.runtime. Preserve input-instruction/entry order,
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

## Implemented traversal and observed limits

Five bodies move with only public owner names, explicit operation parameters
and six late Var ports. Bootstrap keeps private compatibility argument lists
and the existing map-helper docstring,
the recurrence class/token and non-tail diagnostic construction, source throw,
compiler-internal dispatch and scope/admission decisions. The stage selects
the non-tail operation before evaluating an instruction, reselects value
operations at each original call site and forwards the opaque scope unchanged.

Real paired source plans execute23 positive cases and reject8 operand failures
per spelling. Fixtures cover zero/small/generic vectors and function binders,
map collisions/nesting, sequential let/do, loop and function recurrence, and
first-failure ordering in vectors, map keys/values, sequences, bindings,
arguments and loop initializers. Accepted source
3ed57ab5adbe30b4a1d80f77b89e66fa175faa830dff3c25fad082c5a5451d5a
has plan sha256:8247d96e84754af8047c720902a7b55877da769631fcd4d3b19e9224e6e4a8d3.
Rejected source
f3e99417c2e203a698202adf1d70cee8aaa82121307471a2ae591c5da95236b2
has plan sha256:1eddd87aa32cb09d3800c18c968b30e3c0a0c11e3b722c7a59785659d4ca5c6d.

The initial baseline oracle assumed source-map order for the dynamic duplicate
key case. Actual unchanged seed emission places the literal key :a with value2
before the local key with value1 and returns {:a 1}. That source-order gap is retained and
explicitly prevents canonical L2/map conformance credit. Only the oracle was
corrected; source/compiler behavior was unchanged. Direct host tests separately
prove last-key-wins in supplied entry order. Direct list instruction traversal
also retains its existing stop at nil/false, unlike vector traversal; canonical
instruction admission is outside this API. No host behavior is promoted to a
Gravity contract. The original failed oracle and a temporary extractor syntax
failure before product writes remain recorded.

Initial focused checks pass31 tests/1629 assertions. Coupled Reader/CLI/diagnostics/
Release/Runtime/Safety/literal checks pass84/2447; ownership/language47/1153.
Direct host units verify small/generic and vector/list paths, metadata/value
identity, duplicate keys, scope forwarding, operation lookup order, original
ExceptionInfo identity, real recurrence-carrier failures and private wrappers.
These anonymous host operations are separate from actual source evidence and
never stand in for a successful privileged guard.

The first contextual lint run evaluated working source against an unchanged
HEAD diff and classified three pre-existing private compatibility helpers at
shifted lines as blockers. Their unused-private-var findings are retained as
history; no helper, privacy, suppression or policy changes are made to remove
them. A committed-source contextual comparison must classify the actual diff
before lint can pass. Final controls also retain the original private helper
docstring; this metadata restoration changes no body or operation port.
