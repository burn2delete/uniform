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

## Implemented boundary and source evidence

The five bodies now live in Runtime, with15 late function/data Var ports.
Only public owner names, explicit dependency arguments and corresponding
Bootstrap compatibility wrappers change. Complete reconstruction checks prove
both modified components exactly; all31 retained Runtime forms and all other
Bootstrap/Gravity source remain unchanged. Opaque recurrence representation,
throw tokens, authorization and external execution retain their owners.

Real paired fixtures compile and execute12 accepted cases and reject6 actual
arity/operand failures per spelling, including output before failure. Accepted
source2693242a76fc6bf64214937e6ceb828e430ea0f58fea33e0ce6c89512f46c5ef
has plan sha256:ab515f1c9747fe6d1a4e6da7948c28885014b3cf211c3abbcca43ab262466269.
Rejected source
3d0c918db4d72c467b62be1cbc7353f0fc7c968c9a7d358270e56bc0c04a65ec
has plan sha256:7f92d1d27ce9eb1a9349f0fd41399cfd029134021d4d4decf21b8c725cd2eb20.
These are ordinary fixed-seed source plans and native Clojure execution,
not checked-IR/backend admission or canonical recurrence semantics.

Focused tests initially pass54/2391. After adding formatter/call exception
checks, the affected literal/CLI/diagnostics/Reader/Release/Runtime/Safety
composition passes108/3217. Language/ownership tests pass47/1153. Host units
cover sorted comparator isolation, nil/false entries, recurrence facts and
original-value identity, late failure selection, all operation inventories,
compatibility signatures, formatter data lookup after arguments, compiler
denial, authorization before arguments and unchanged exception identity.
Anonymous operation Vars are sequencing units, never admission evidence.

Baselinef11ae98f578c9e6664aae302bd5511838bd76c66, tree
92a382d698032df7a69dfc7ff7e15a6f96526af5, contains the original production
source and four new fixtures. Both baseline processes terminate before source
changes in this same physical Imports checkout. After controls execute the
working extraction at that HEAD; they do not claim execution at a later clean
commit. Scripts/logs/products use uniform-runtime-control-* under /tmp;
complete Core records use runtime-control-before/after under
uniform-cli-core-arguments-evidence. Product comparisons, contextual lint,
final checks, independent exact acceptance, actual-main reconciliation and own
CI/artifact integration evidence remain separate final gates.
