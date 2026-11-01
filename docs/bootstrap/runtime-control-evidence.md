# Hosted seed recurrence facts and call sequencing

## Contract boundary

D1/D2/D3/D8/D9, L2, C1/C15, R1/R4/R11/R12, BOOT8 and TEST2 govern
this extraction. Move the existing map-entry lookup, recurrence failure facts,
nontail-value check, generic builtin call sequencing and ordinary call
sequencing from components00590/00593 into gravity.runtime. Preserve exact
lookup, evaluation, failure and authorization order through explicit late Vars.
Bootstrap retains the recurrence class, source throw token, compiler-internal
and external/module authorization, external execution, formatter and error
transport. No supplied operation confers authority.

Proposed APIs are map-entry, fail-recur!, nontail-value!,
execute-generic-builtin-call-scoped and execute-call-scoped. Inputs are trusted
native values, existing runtime/plan/environment/instruction data, original
invocation scope and late function/data Vars. Outputs are the original map
entry, value, recurrence refusal or call result. Runtime owns lookup and
sequencing decisions and L2-RECUR-TARGET facts; supplied owners retain actual
scope admission, compiler/external execution and diagnostic publication.
Dependency direction remains clojure.core to gravity.runtime to Bootstrap.

## Evidence obligations

Compile and run paired real call/recurrence sources; reject real arity and
operand failures with unchanged stdout and diagnostics. Capture complete
before/after source/plan/result/refusal products and both public current Core
artifacts in this same physical checkout, with original serialization and
recomputed identity. Test sorted-map comparator isolation, nil entries,
recurrence identity, late operation lookup, exceptions, authorization before
arguments and all compatibility wrappers. Prove exact body conservation and
unchanged retained authority/representation owners. Run proportionate focused,
coupled, language/ownership, lint and contract checks, independent acceptance,
own CI/artifacts and exact clean current-main integration preflight.

Initial source is PR761 candidatecf1bcfa37fda1122d49fb1c8e3533be95fff5639,
tree49a048a2db686351da788fd8908e0774229b0e60. This isolated successor
depends on that union and requires reconciliation with actual main before
integration. Scope publication grants no implementation or acceptance credit.

## Unsupported boundaries

No canonical Gravity map/error/recurrence semantics, complete runtime or L2/
R4/R12/C15 conformance, untrusted operation ingress, plan authentication,
capability grants, native/backend execution, performance, migration, release,
self-hosting or seed retirement. Historical restricted routes, pins and guards
remain intact. This slice does not implement the remaining module authority
or reserved builtin adapters.
