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

## Implemented boundary and real controls

The238-line body now lives in execute-instruction-scoped. Only the public
owner name, explicit operation parameter and26 late Var ports change; three
ports carry formatter names as data. No operation is eagerly dereferenced.
Complete component reconstruction conserves declarations and the public
wrapper. All22 previous Runtime bodies, Bootstrap and nine other consumer/
owner components remain exact, including effect/compiler authorization,
recurrence/token representation and formatter definitions.

Actual paired source plans execute30 accepted cases and reject10 real arity/
operand failures per spelling. Controls capture values, actual classes and
stdout, including output before failure. Accepted source
d623fb0649d95ffc11984230ad67eb03c055de153fb8d56871187407c2c97e4b
has plan sha256:b0cacd213fabdb60f9d0bf5327aad83eed56e8779f37fd183ac3e6b7fffbd03b.
Rejected source
f64b68ea0f0155b5a59dcc47ce738325a79b5a17df88e9e4f7e97178146da74b
has plan sha256:53fca99a35284d988527edc39d9f74ef39d06639a080b386591a4e5892945d82.
These are ordinary fixed-seed compilation and native Clojure execution; no
target backend or runtime-family admission is inferred.

All seven complete data and original serialized pairs pass: four source/plan/
value/stdout/refusal records, arguments and two complete public current Core
artifacts. Before96b21e659121d4335a8a8230d8a0217b0b1a099c, tree
b56dde5e7f45df744604823b029475201297c2a5, and working after controls run in
this same physical Records worktree. Both Core spellings retain25 nodes, one
target read, source8e63 and recomputed Core16ec. No root, pin, mode, authority
callback or returned field is substituted.

Focused tests pass41/1929; coupled literal/CLI/diagnostics/Reader/Release/
Runtime/Safety tests94/2747; ownership/language47/1153. Host units cover
literal/reference identity, local nil/false, actual shape diagnostics, ordered
if/let and recurrence-arity checks, scope forwarding and all26 ports. Opaque
source throw retains the real private Bootstrap token and payload by identity
within one process. It is not rewritten into a purported cross-process error
artifact. Effect/compiler/module/artifact port units deny rather than grant
privileged entry; real source products use unchanged owners. Unary artifact
exceptions retain their distinct transport boundary.

## Existing regression-port correction

The first nine selected existing kernels report nine failures and one error:
two assoc tests still interpose the unscoped execute-value helper, although
the original executor already calls execute-value-scoped. The exact untouched
parent26ce2fc1 in the separate clean Imports checkout reproduces nine failures
and one error. This is a pre-existing test-port mismatch, not a change to the
executor or a reason to route around scoped execution.

Only those two tests change their Var name/callback arity to the actual scoped
port and add nil-scope assertions for the public wrapper. All original result,
evaluation-order, exception and recurrence assertions remain. The corrected
nine kernels pass9/67 on both the unchanged parent implementation and current
extraction. The temporary baseline test copy is restored; Imports remains
clean at26ce2fc1. Initial current/parent nonpasses and both corrected runs are
retained. The candidate therefore also owns this ten-line test correction;
its final source scope contains ten paths rather than nine.

Scripts/logs/products use uniform-runtime-instructions-* under /tmp; complete
Core controls use runtime-instructions-before/after under
uniform-cli-core-arguments-evidence. Contextual lint, final contract checks,
current-main reconciliation, independent exact acceptance, own CI and clean
integration preflight still govern integration. Scope/predecessor checks
confer no implementation acceptance or complete namespace credit.
