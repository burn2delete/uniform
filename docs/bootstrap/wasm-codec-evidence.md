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

## Compiler and type-checker parent

Candidate d82cc1cd82e9e7d585139bbf40e5390065226908, tree
434671c9af1769379d91f9da748af46f3ecf5a28, passed all ten enabled jobs in
CI 41232679238 against main 79f285f. Tested merge
ee666eda80ff3ae147e7b9ad6f6c21dfc8cc339a has those exact parents and tree.
Verified receipts record Macro 16/293 and Resolution 54/2307, exit zero under
the configured 900-second host watchdog. Resolution elapsed 668.737 seconds;
this is not an old-600-second pass. Receipt manifest digests are
c485ed877118604e9a723e452bb6decdb3b4d45635ee4375ceaeccd1fccd8b24
(Macro), 586f7d113754d552d58e5ccaa4b711eacb18dc5a99cff9ec1a01c0bafc18465a
(Resolution), and 0dc322d4e799178376344de7a6eeec354b7a7a6610b472801b344340bed7ce51
(Bootstrap). That green belongs only to its exact source/base; no merge used it
after main advanced.

The actual new main parent is 1fa559ab38ae8059f7c2e47cb3d2e927cb60b041, tree
d886151ee4fd6a2eea5b774f438f6e5cf8442f5b. It includes PRs 709, 712, 713 and
714: core-form deduplication, normalized crosswalk payloads, separated C7
quotient kernels and shared persistent-set policy construction. The
conflict-free composition preserves all seventeen incoming paths unchanged.
Wasm/LLVM bodies, tests, consumers, registrations and namespace rows remain
byte-identical to d82cc1cd.

Fresh Wasm/LLVM plus normalized C7 quotient tests pass 14/291, ownership
26/905, language 6/11, documentation 240/19 and roadmap/governance. The C7
selection executes only normalized development data, without authenticated
source-composition or restricted migration tests. Fresh production matches all
three original complete/text captures and both binaries; source hashes, plan
IDs, B4 data identities and five-file current-base reconstruction pass. These
are working composition checks, not committed-head authority bookends. Final
exact acceptance, full semantic CI and clean current-main preflight remain
required; no prior green is transferred.

## Unused seed-proof projection parent

Main advanced to f2bfea387b60077a64e0dcbf2573c6afad0126e9, tree
7ede33bfb5e239bdc1b51a8067b9a87f33ce05cc, through PR 708. The conflict-free
composition of d00ca397decdce4e6b0077a3666ce090b2a93d1f preserves both incoming
semantic-mid files exactly; stage source, tests, consumers and registrations
remain unchanged. Fresh focused regressions pass 14/291, ownership 26/905,
language 6/11, documentation 240/19 and roadmap/governance. Fresh complete
captures, deterministic identities and five-file current-base reconstruction
pass. The initial producer launch omitted the test classpath and failed before
execution; the corrected explicit classpath launch and verifier pass, with both
logs retained under /tmp/uniform-wasm-codec-main-f2bf. No production change or
authority substitution was involved. These working checks do not transfer CI
or acceptance to a new commit. Exact independent review, final committed lint,
full semantic CI and clean current-main integration preflight remain required.

## Unsupported boundaries

Data emission and integer decoding do not authenticate MIR or B1 packets,
admit proof evidence, verify complete modules or execute Wasm. Canonical public
emit/verify, generalized MIR/domain lowering, imports, memory, tables, WASI,
components, WIT, native host/toolchain support, full B4 conformance, migration,
release, self-hosting and seed retirement remain unsupported by this slice.
