# Hosted seed builtin dispatch

## Contract boundary

D1/D2/D3/D8/D9, L2, C1/C15, R1/R4/R11/R12, BOOT8 and TEST2 govern
this temporary Clojure seed extraction. Move ordinary hosted builtin selection
and its existing arity-operation calls from component00588 into gravity.runtime.
Keep all five declared-input/compiler-native cases, their authorization, the
outer exception translation and every caller in Bootstrap. Do not infer portable
Gravity semantics from the host operations.

The proposed hosted-builtin API accepts plan, callee, evaluated arguments and
an explicit map of trusted late operation Vars. It returns the existing value
or throws the existing failure. Runtime owns ordinary selection and arity-check
placement. Supplied operations own arity facts, seed-readable behavior and
numeric helpers; Bootstrap retains reserved dispatch and exception transport.
Dependency direction is clojure.core to gravity.runtime to Bootstrap. No reverse
import, new primitive, plan authentication or capability is introduced.

## Evidence obligations

Compile/execute paired real .gravity/.qst fixtures covering ordinary branches
and reject real arity and operand failures. Compare complete before/after
source/plan/value/refusal records and public current Core products in this same
physical worktree, retaining original serialization and deterministic identities.
Test late helper selection and refusal ordering separately with explicit unit
operations. Verify unchanged reserved arms, catch blocks, callers, authority
checks and existing Runtime bodies. Run focused/coupled, language/ownership,
lint and contract validators; require exact independent acceptance, own green
CI artifacts and clean current-main integration preflight before merging.

Initial base is main056fde5aaaf9b484b681cebe9a80ed5f97af0a0b, tree
45fe1296c1f6c3062c073d131209b9c0b2947345. This scope has no implementation
or integration credit. The draft PR remains current as evidence develops.

## Unsupported boundaries

No canonical Gravity formatting/collection/numeric/error semantics, complete
runtime family, full L2/R4/R12/C15 conformance, untrusted operation/plan ingress,
new authority, native/backend execution, migration, release, self-hosting or seed
retirement. Existing diagnostic source gaps remain explicit. No historical
restricted route, source pin, guard or admission mode is changed or replayed.

## Implemented boundary and focused evidence

The143 ordinary-arm lines now live in hosted-builtin. The only body changes
are17 explicit late Var ports. A private immutable Bootstrap map stores Vars,
not captured roots; the stage resolves only the operation selected by the arm,
at its original call position. Direct clojure.core operations remain temporary
host behavior. Supplied arity helpers retain their existing internal global
failure calls; the fail-call-arity! port is used directly only by the moved
assoc branch. The stage does not catch or translate exceptions.

Complete component reconstruction verifies the five reserved cases/helpers,
outer catches and all three public argument lists remain exact. All16 existing
Runtime bodies, Bootstrap loader and four invocation callers remain exact,
including the unary and specialized get fast paths. No authority decision,
source pin, compiler context or source primitive changes.

Paired real source plans execute42 accepted cases and reject20 arity/host
failures per spelling. Accepted source
cb40898638da54eedea3917ca3b1fea6f16d704182c36675249c88aff9239441
produces plan sha256:848f84d2cc32655ce81351ba92bea1bd2011758431c1bfcb548880c232f1eeef.
Rejected source
930628e38afd7c94792d06af9fb628768921e86caa3acd2d85d320f53ac461e4
produces plan sha256:0501f2718f242ccd3e3fe988a9278f1f5059cc6d41664ce5bb9fa5d5e6395979.
Original source/plan/result/refusal records retain actual paths and classes.

Direct host units cover generic arms bypassed by the existing unary/get fast
paths. They verify the17-key inventory, late helper selection after arity,
no unrelated port reads, refusal before argument indexing, unknown-callee
translation, unchanged ExceptionInfo identity, all wrapper signatures and
late stage interposition. Reserved names have no stage port. Anonymous unit
Vars and host contexts are separate from genuine source evidence; no fake
successful privileged guard is used.

Focused tests pass24/1286, coupled literal/CLI/diagnostics/Reader/Release/
Runtime/Safety checks77/2104, ownership/language47/1153. The initial focused
run recorded eight incorrect class expectations: existing hosted hash-map
returns PersistentHashMap and count returns Integer, unlike test literals.
The captured before/after classes agree; only expectations changed. A runner
invocation with an unowned namespace executed no tests; the proper ownership
and language namespaces then passed. Both nonpasses remain recorded.

Current-base contextual lint passes3 paths with zero warnings/blockers.
Docs240/19 indexes, roadmap and governance185 pass. Terminal lint receipts
are archived outside the checkout.

All seven complete data and original serialized pairs pass: four source/plan/
result/refusal records, arguments and two complete public current Core products.
The genuine before source is e55e5f9d545fc544517602590a86e8e2fed7965f,
tree a59269f058ca2d6f7f1b4b50d174afe9c3605e24, in this same physical Records
worktree. Both Core spellings retain25 nodes, one target read, source8e63 and
recomputed Core16ec. No root, source pin, admission mode, returned field or
authentication callback is substituted. Scripts/logs/products use
uniform-runtime-dispatch-* under /tmp; complete Core controls use
runtime-dispatch-before/after labels under uniform-cli-core-arguments-evidence.
Exact independent acceptance, own implementation CI and clean current-main
integration preflight remain required. This is partial hosted seed extraction,
not complete Runtime or portable semantic conformance.
