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
