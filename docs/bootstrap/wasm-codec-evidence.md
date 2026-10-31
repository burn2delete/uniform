# Wasm emission and canonical integer codecs

## Contract and scope

D1, D2, D3, D6, D8, D9, C1, C11, C14, C15, B1, B4, B13, B14 and TEST2
govern this extraction into `gravity.backend.wasm`. The existing semantic
owner remains `bootstrap/gravity/src/gravity/backend/b4_wasm_backend_design.gravity`.
The stage will own scalar Wasm byte emission and canonical unsigned/signed
32-bit LEB codecs. Its explicit inputs are operation/function data, byte
vectors, offsets and compatibility operations. Its outputs are module bytes,
reconstruction records, decoded integers and format/operation refusals.

Bootstrap retains source binding, input admission, opaque authority holders,
instruction/module verification, native host execution and publication. The
stage imports no orchestration owner. Compatibility ports must preserve
original arities, callback ordering, source associations and deterministic
identity. Byte range, canonical encoding, target width and byte-offset checks
are actual format requirements.

## Implementation and evidence

Eighteen functions and one comparison-opcode constant move from Bootstrap
components 001/002/003 into the stage. Twenty-five explicit Var ports preserve
callback ordering and late opcode-map reads. The stage imports no namespace;
Bootstrap consumes it. Every public Var records its exact API, artifacts,
diagnostic responsibility, dependency direction and non-admission boundary.
The original unused mir parameter is named _mir only in the stage. Two
compatibility wrappers forward their entire input maps and explicitly restore
the original destructured public arglist metadata.

Two explicit data fixtures exercise a 21-operation straight-line module and a
four-block diamond. They cover signed i32 endpoints, booleans, nil, all five
forwarded forms, truthiness, five comparisons and a conditional join. The
emitter input tuple contains only function, block order, operations and index;
it is never fabricated as an authenticated packet or actual preflight result.
The current Gravity B4 source is compiled and its function-instruction,
evaluation, section and integer-encoding kernels execute on the same data and
compare equal. Its full builder receives nil and genuinely rejects
B1-INPUT/:verified-bounded-wasm-b1-packet. No source admission, authority,
instruction/module verifier, Node or other native execution path is invoked.

Actual emitted files are 146-byte and 63-byte core modules. Their headers,
export records and no-import/no-runtime-helper surface are checked. Real
negative inputs reject unsupported emission and mismatched conditional joins.
Canonical integer decoding rejects truncation, overlong and overflowing LEBs,
invalid octets, booleans and floating octets. Golden integer vectors cover
u32/s32 width endpoints, sign transitions and nonzero offsets. Five-byte LEB
limits enforce 32-bit target representability; offset limits enforce actual
buffer boundaries. These are format checks, not input or work quotas.

Three complete EDN/text capture pairs and two raw binary pairs match exactly.
Each capture includes original input source/origin associations, reconstruction
records, integer vectors and complete structured refusals. Both Gravity source
hashes and declared plan identities recompute through the unchanged canonical
identity protocol. The existing B4 data identity operation gives equal IDs for
both module pairs; these are data identities, not admitted artifact identities.
Read-only reconstruction from immutable main
aa503b03136033a3cc932f555dc1692978d3720e matches the complete stage, all three
consumer files and Bootstrap require delta. It conserves every unmoved
authority, preflight, instruction/module verifier and native-execution form.

The original capture used the original implementation over scope commit
91f3f2fd9 with the new test untracked. The corrected extracted capture used
the working extraction over that commit. These are ordinary development
captures, not committed-head migration or authority bookends. The stage
preserves input data and operation/order references; it does not invent a new
source/debug map or claim full proof/safety/capability admission.

Focused regressions pass 6/151, ownership 25/888 and the language gate 6/11.
Documentation validates 240 documents and 19 phase indexes; roadmap/governance
pass. Initial exact committed lint covers all eight changed Clojure/EDN paths:
3490 unchanged baseline warnings and zero blockers. Both independent reviewers
accept source d2ed7eb0105c735d378183ddd19bfeaa568fc54f, tree
f52e4200ee57b497199d4eeba0eaebd4cc22affe, conditionally. Final exact acceptance,
CI and clean current-main preflight remain required. Registration uses the
existing SH-00 Wasm owner without
renewing its historical evidence or awarding new milestone/release credit.
Primary inventory counts remain unchanged. The direct-child inventory counts
direct helpers separately from complete nested module ownership.

## Retained nonpasses

The first baseline test shadowed the native helper with a captured map,
producing 26 assertion failures and two errors. Renaming that test binding
corrected the test; the original implementation then passed 2/113. The first
extraction incorrectly forwarded two destructuring forms as reconstructed maps
instead of forwarding the original input map. The unchanged join-binding
check rejected the resulting incomplete data. The wrapper correction forwards
the actual map, preserves the original public arglists, and passes all paired
captures and regressions. No rejection check or authority gate was weakened.
These failed observations remain in their original local logs.

Reproduction:

```bash
clojure -M:dev-test --namespace gravity.backend.wasm.pipeline-test
clojure -M:test --namespace gravity.self-hosting.sh01-ownership-test
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

Local scripts are `/tmp/uniform-wasm-codec-{evidence,extract,verify}.clj`;
captures are in `/tmp/uniform-wasm-codec-evidence/`. The verifier captures
reconstructed writes in memory and compares complete source without rewriting
the checkout. The forwarding correction was reconstructed from immutable main,
never by rerunning extraction on compatibility wrappers.

## Main composition

Main advanced to c4bf8f55629855720d58c33a02cb405053c4f2e1 through the normal
LLVM data integration. Its tree is 35c7bff6b268f8a9ea6a76e4f7a57889a42a7ff5.
The composition uses the complete source and main parents. Five actual
conflicts are resolved in the Bootstrap require list, ownership test, component
inventory, namespace map and ownership EDN. Both helper registrations and both
adversarial ownership tests survive; LLVM retains index 10 and Wasm follows at
index 11. The original primary inventory counts do not change.

Wasm stage, test and all three consumer bodies remain byte-identical to the
source candidate. LLVM stage, test, four consumers and evidence remain
byte-identical to main. Combined focused tests pass 12/217, composed ownership
26/905, language 6/11, documentation 240/19 and roadmap/governance. A fresh
composed producer matches all three original EDN/text captures and both binary
modules; source hashes, plan IDs and B4 data identities recompute. Whole-source
reconstruction against the current main parent passes all five files. These
are working composition checks, not committed-head authority bookends.

The temporary composition script initially parsed only a prefix due to an
extra delimiter; its mutation section did not execute. A whole-file reader
then caught the unmatched delimiter. The corrected command parses all forms
before execution and constructs the five paths from complete parents. The
script is `/tmp/uniform-wasm-codec-compose.clj`. This real composition makes no
causal-remedy claim about any earlier CI result. Final checks and independent
acceptance must bind the final exact commit and current integration base.

## Current CI-policy parent

The accepted composition 7eb2a008c1d360deccc7ce171cadfb8926c82e5d, tree
c8cd953f15f83263e6f5b1e90ecfa13aef1d73f7, remains failed historical evidence.
Run 41230850970 timed out Resolution at 600 seconds, exit 124; the other nine
enabled jobs passed. Its downloaded checksum manifest verifies at
`/tmp/uniform-wasm-codec-ci-resolution-41230850970-nonpass`, digest
a3fdcfc74a4736731865881c2cb9f52f450036e0c674f279f45f9cd88dd14f39.
Tested merge 31dd00c791e1894d9b77bf7ee431794df5a16ba9 has exact parents
c4bf8f55629855720d58c33a02cb405053c4f2e1 and 7eb2a008, and tree c8cd953f.
The log has namespace headings but no completed summary or active test id.
Cause and process cleanup remain unknown. The failed head is not rerun.

Current main 79f285f30350362ca4ea8fb99bf0159014c32139, tree
8c7e1c22ab679c458b22cb6ab4d9690abfc35d8d, independently integrated PR 711's
CI selection and host-watchdog policy. The new composition incorporates its
three CI files unchanged, without conflicts or an authored CI delta. They set
Macro/Resolution child watchdogs to 900 seconds and workflow jobs to 20 minutes.
This source-changing slice requires full semantic selection. These settings
and any future result do not repair the old failure, establish its cause,
prove an old-bound pass or transfer prior green checks.

Every Wasm/LLVM source, test, consumer, registration and namespace row remains
byte-identical to 7eb2a008. Fresh current-main focused tests pass 12/217,
ownership 26/905, language 6/11, documentation 240/19 and roadmap/governance.
Fresh production matches all three original complete/text captures and both
binary modules; source hashes, plan IDs, B4 data identities and five-file
current-base reconstruction pass. These remain working composition checks,
not committed-head authority bookends. Final exact acceptance, full semantic
CI and clean current-main integration preflight are still required.

## Unsupported boundaries

Data emission and integer decoding do not authenticate MIR or B1 packets,
admit proof evidence, verify complete modules or execute Wasm. Canonical public
emit/verify, generalized MIR/domain lowering, imports, memory, tables, WASI,
components, WIT, native host/toolchain support, full B4 conformance, migration,
release, self-hosting and seed retirement remain unsupported by this slice.
