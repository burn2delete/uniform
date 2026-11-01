# Hosted seed instruction execution

## Contract boundary

D1/D2/D3/D8/D9, L2, C1/C15, R1/R4/R11/R12, BOOT8 and TEST2 govern
this temporary seed extraction. Move the existing scoped instruction executor
body from component00592 into gravity.runtime/execute-instruction-scoped.
Preserve every arm, fast path, evaluation order, exception boundary, diagnostic
fact, writer behavior and opaque invocation scope. Bootstrap retains private/
public compatibility signatures and all supplied operation owners.

Inputs are trusted runtime/plan/environment/instruction carriers, scope and
explicit late function/data Vars. Outputs are the existing value, writer
effect, recurrence carrier, source throw or refusal. Runtime owns instruction
selection, core control flow and the moved L2-UNKNOWN-SYMBOL,
L2-THROW-ILLEGAL and L2-UNKNOWN-CORE-FORM decisions/facts. Existing operations
retain failure transport, effect legality, compiler-internal/module/native
authorization, recurrence representation and Gravity-authored formatting.
Dependency direction remains clojure.core to gravity.runtime to Bootstrap.
No reverse import, new primitive, plan admission or capability is introduced.

## Evidence obligations

Compile and execute paired real source fixtures for data/control/calls,
specialized builtins and stdout; reject real operand/arity failures and retain
first-failure behavior. Capture complete before/after source/plan/result/stdout/
refusal products and public current Core artifacts in this same physical
worktree with original serialization and recomputed identities. Use direct
host units for instruction-shape refusals, callback/exception order, scope
forwarding and opaque source-throw identity; never project an opaque token to
claim cross-process full serialization equality. Preserve the entire old body
except owner/explicit late Var ports, all other components and prior Runtime
bodies. Require focused/coupled, language/ownership, lint, contract validators,
independent acceptance, own green CI artifacts and exact clean preflight.

Initial source is candidate26ce2fc172a635aad1ee0a9127e352f629c7b33c,
tree f7c796a7799e9a8548de2dcc1b805b8f0201b636. This isolated successor
depends on PR755. Reconcile actual main after that predecessor integrates;
no predecessor work receives duplicate credit. This early draft grants no
implementation, acceptance or integration credit.

## Unsupported boundaries

No canonical Gravity collection/numeric/error/source-order semantics, full
L2/R4/R12/C15/runtime conformance, untrusted instruction/operation ingress,
new authority, native/backend execution, performance, migration, release,
self-hosting or seed retirement. Existing source-map and direct-list gaps
remain explicit. Historical restricted routes, pins and guards remain intact.
