# Types: current Gravity runtime boundary

## Scope and governing contracts

D1, D2, D3, D8, D9, L5, L17, C1, C7, C15, BOOT8 and TEST2 govern this
increment. The clean control is 9cd520974f5271db18e512278d5a328da32f247e,
tree 9ed75816b12902a50014d0abe3f1b02aff2d8c44. It contains the independently
accepted, not yet integrated, types products and ownership correction.

The extraction gives gravity.types compile-plan, check-primitives,
verify-primitives and verify-function-result, with explicit compiler/runtime
operations. Bootstrap supplies the existing Var operations and retains source
reading and orchestration order. The production typed-lowering handoff now uses
the stage for its existing C7 plan compilation and fresh function-result replay.
Gravity's existing c7_type_checker_engine.gravity owns all typing and diagnostic
semantics. The stage imports no Bootstrap, CLI, release or C7 consumer.

Inputs are explicit emitter/source records, compiler plans and native Core or
type-result data. Outputs are existing executable plan data, primitive typed
templates and replay reports. Compilation and runtime diagnostics propagate
unchanged; the stage invents no type rule and authenticates no caller premise.
The production typed-lowering handoff must compile C7 before lowering and
replay C7 before retained source intake, exactly as before.

## Actual development evidence

Execution uses control HEAD c17e403c01c55382d3d2229126068252725967ca with the
working extraction and tests in the original analysis checkout. No later
committed-head execution or migration authority is implied. The original seven
seed builder bodies are unchanged; four small fixed-entry runtime adapters and
their metadata are added. The stage has eleven public APIs, no imports, and
admission:none throughout. Caller operations and passing replay data supply no
authentication or proof authority.

Actual current C7 compilation emits an executable plan with 1065 functions.
Its complete data and serialized text equal direct existing compiler output;
the declared plan hash recomputes to
4ef112430794d8e31f3704ecb1731e89a68c7d4e69effe801b900d818bc4a9b7.
Source hash 9ec6e65adfa8a7288712ff1ec6394218027eb2606cb58879822bf6543f39adf1
matches the unchanged Gravity C7 file. Compiler/emitter/Core source-key
bookends match actual current sources, and C7 source text is unchanged across
the producer. A working-file snapshot taken after producer load verifies
unchanged before commit; it is not an execution bookend for Clojure code.

Paired primitive-if-join fixtures pass actual current Reader/C5/Core and C7
execution, emitting 40 Core nodes and 40 type facts per spelling. Both
if-branch-type-mismatch fixtures emit five Core nodes, three partial type facts
and C7-TYPE-MISMATCH for Integer versus Keyword branches. Replay reports verify
result consistency while the program retains its rejected status. Emptying the
emitted type table receives a rejected replay for every fixture. The production
function-result adapter also preserves actual rejection of malformed nil input;
no authenticated function-owner positive is exercised or claimed.

Seventeen complete and seventeen serialized record pairs match, including
direct/stage plans, type results, complete reports/rejections, source-key
bookends and full paired-extension typed outputs. Both paired identity inputs
are equal, addressing cross-extension determinism on this current route.
Declared Core/provenance hashes recompute, fresh C5/request/Core joins verify,
and source syntax/spans/origins/generated origins plus effect/capability
declarations survive in every accepted type fact. Accepted canonical Core ID
is 1c46e7c99409f08f850f8289391ea2ecbab4b8cc495d2c202b7b5d7614d79de9;
the rejected fixture's Core ID is
323119c9227ed5b262e88b61df830f850105a64fc6751addc31df168ac3df058.
Typed identity requests and diagnostic identity requests remain unresolved as
the existing API declares; no public typed-artifact digest is assigned here.

The actual source test passes 1 / 532. A separate mocked orchestration test
passes 1 / 11, covering C7-before-lowering plan order and rejected replay before
retained source intake. This control-flow test supplies no semantic or authority
evidence. Existing bootstrap-free C7 tests pass 7 / 204 with the unchanged
53-entry catalog/hash; ownership passes 21 / 821, language 6 / 11, docs 240 / 19,
roadmap and governance. Full-path lint, independent acceptance, applicable CI,
actual main composition and clean integration preflight remain required.

The separate capture command initially failed before execution because the
test namespace was absent from its classpath. Its failed receipt is retained;
the corrected command explicitly adds that test path and exits zero. The
producer and complete verifier also exit zero. No historical source-owner
failure, refusal or timeout is rerun or explained away by this harness correction.

## Unsupported boundaries

The primitive API explicitly requires a coordinator adapter and digest
resolution; structurally valid native Core data supplies neither authority.
Complete L5/C7 support, full function-owner admission, native ingress, frozen
Reader/C2 source authority, effectful provider or command execution, migration,
release, self-hosting and seed retirement are outside this increment. Historical
NONPASS and denied actions remain unchanged and are not retried. No new proof,
receipt, pin, accounting gate or alternate authority route is introduced.
