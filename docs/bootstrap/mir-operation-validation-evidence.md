# MIR: native operation parity rules

D1, D2, D3, D8, D9, C1, C6-C11, C15, BOOT8 and TEST2 govern this
bounded extraction. Its source parent is reviewed pending candidate
fbbc1613114d89823a56285c5642f56ce5de5a58, tree
ebbcf39e6406d9cbd04ce0a2adebb23a7c1daef8. Parent integration, review
and checks do not transfer acceptance to this increment.

The existing validate-operations! body moves from Bootstrap into gravity.mir.
Fourteen explicit owner ports supply operation traversal, parameter and runtime
check ids, expected placement, node facts/opcodes/operands, runtime-check
reference records, diagnostic construction, four schema sets and set union.
Function ports remain live Vars; schema data is read at each wrapper call.
The original relative rules, evaluation order and native return record remain.
The stage has no imports or Bootstrap dependency. The wrapper keeps its public
signature; callers retain construction, fact-table/runtime-check validation,
authentication and admission.

Inputs are supplied native checked-Core/MIR data and owner operations/schema
data. Outputs are the existing node, operation, runtime-node and block products
or original owner refusal. The stage owns relative operation count/placement,
block and operation shape, comparison schema, fact references, direct effects,
safety reference and source parity decisions. The owner require operation
constructs and throws C11-MODULE, C11-DOMINANCE, C11-BLOCK, C11-TARGET-LEAK,
C11-TYPE, C11-SAFETY, C11-EFFECT and C11-ORIGIN diagnostics.

The paired normalized Gravity fixture and current Gravity C11 builder execute
through the ordinary fixed seed and emit real MIR. Native operation parity
accepts five operations for each extension; changing one emitted opcode to
:llvm/add rejects C11-TARGET-LEAK, missing checked-core-operation-parity.
Complete source texts/hashes, checked input, emitted MIR, native products,
changed MIR and full diagnostics are captured before and after at the same
paths. Complete EDN/raw text equality and source hash recomputation pass.

Tests exercise changed operation types, source, fact references, effects,
block shape, operation membership and instruction ordering. They preserve
callback order, exact exceptions, late Vars, schema identity and wrapper
arguments. The first ordering test reversed a single-instruction block and
therefore changed nothing; its failed refusal assertions remain history. The
corrected test selects an existing block with multiple instructions. No
production rule or original positive input changed to force acceptance.
Local lint also required an explicit clojure.set declaration in the new test;
the corrected test imports the namespace it references. Its initial lint
nonpass is retained separately.

Native proof/authenticated/derived/pending labels remain supplied data. These
relative rules do not authenticate inputs, schemas, callbacks or certificates,
and do not admit source or produce canonical C11 verification. The positive
fixture has no runtime-check nodes. General CFGs, loops, matches, exceptions,
runtime-check legality, diagnostic identity/redaction/publication, full MIR,
backend/native execution, performance, migration, release, self-hosting and
seed retirement remain unsupported. No restricted native/private factory,
frozen Reader, package/archive/release builder or Attempt30 route is executed.

Focused and boundary tests, contextual lint, contract validators, exact source
conservation, independent review, maintained draft PR, own applicable CI and
artifacts, parent integration and immediate exact clean current-main preflight
remain integration gates. Final receipts and immutable identities are recorded
in the PR; this file confers no release or migration authority.
