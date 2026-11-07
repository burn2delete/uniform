# Native HDL document data ownership

## Scope and governing contracts

This slice moves the existing B9 document manifest constructor, 22 ordered
validation checks and coverage projection into `gravity.backend.hdl`.
D1, D2, D3, D8, D9, B1, B9, B13 and B14 govern its architecture and evidence
boundary. Draft PR #1267 was opened at scope commit
`270895eaeeb38b8e11b15f38280906a37764c2d9` before implementation.

The starting source is `ce5cebf6de745402db1dde36170861b06f1429ab`, tree
`1c515afeae2457354f07e5dbe9867cbd214e4290`. Published main
`fa14c00518690feb113f9e506d3c8c0b8a96ebea` is already its ancestor.
Prior slice CI results confer no acceptance on this source.

## Public API, artifacts and dependency direction

The three new APIs are `native-manifest [operations source-path input-id]`,
`validate-native-manifest! [operations source-path artifact]` and
`native-coverage-report [operations artifact]`. They extend the twelve existing
diagnostic context, failure, override, fixed text and structural predicate APIs.
Each records its operation ports, unverified inputs, legacy outputs and explicit
non-admission. The stage requires only Clojure core and `clojure.string`.
Bootstrap consumes the stage; the stage must load without Bootstrap.

The manifest copies an unverified input identity into three existing fields,
interpolates a source path into two simulation links and four debug locations,
retains the generated-origin chain and hashes three fixed reference texts.
It does not open the source path, authenticate the identity or verify IR.
The output is legacy manifest data containing HDL, testbench, timing, interface,
width, clock/reset, CDC, state, simulation, source-map and audit records.
The external synthesis status remains `:not-available-in-current-environment`.

The stage owns the original structural decisions. Bootstrap adapters retain
their names and arities, original diagnostic construction/publication,
source admission, final artifact assembly and final artifact hashing.
The stage resolves caller Vars at each original use. Hash calls read HDL,
testbench, then timing; later content reads use HDL, timing, then testbench.
Callbacks may deliberately change later values and produce hash/content
mismatches. This extraction adds no snapshot or consistency check.
Validation evaluates text predicates in HDL, timing, testbench order.
Coverage preserves the original compiled large-map evaluation order,
testbench, HDL, timing. That order differs from textual entry order.

## Verification and artifacts

The new cold namespace requires no Bootstrap or source builder. It emits three
fixed files under `target/validation/hdl-native-manifest/`, asserts deterministic
hashes and borrowed identity, rejects missing reference tokens and malformed
records, exercises all 22 refusal steps, and checks late callback lookup,
first-failure exception identity, callee-before-subject selection and distinct
hash/content and predicate orders. The supplied SHA256 function hashes only
explicit unverified test data; it supplies no source admission authority.

Development results on the implemented source:

- The cold namespace and existing compatibility namespace passed 27 tests and
  305 assertions: 12 new tests/163 assertions and 15 existing tests/142 assertions.
- Language-boundary and ownership checks passed 48 tests and 1202 assertions.
- The CI harness passed 54 tests and 608 assertions. The exact appended
  2g/180-second native command passed all 12 tests and 163 assertions.
- All three original Bootstrap roots were rebound together from the immutable
  scope source for a separate comparison using the original C4 hash operation.
  Complete raw and parsed records matched for 51 outcomes: 14 returns and 37
  refusals. Cases cover all record guards, twelve missing reference tokens,
  returning and throwing callbacks, late diagnostic IDs, hash/value mutation,
  actual predicate evaluation order and callee-before-subject selection.
  Receipt `/tmp/uniform-hdl-manifest-parity.edn`, SHA256
  `9cd708ce0c36c07bbbb55994aa8500d7712f3859be6de9a3af2964163f6344db`.
- A fresh JVM loaded the stage and native test namespace with fifteen public
  APIs and without Bootstrap. Document and roadmap validation and CI formatting
  passed. The three production files match the inspected transformation;
  all twelve prior API bodies remain byte-identical. Fourteen protected paths,
  the workflow and all fourteen prior contract command vectors are unchanged.
  Conservation receipt `/tmp/uniform-hdl-manifest-conservation.edn`, SHA256
  `445549fd8005b46ca4b6407bb0fae48db0e8eb81bb5b2f74ba21a933f99b4e54`.
- The initial test command used an unknown compatibility namespace and exited
  before tests. The first valid run caught an incorrect coverage-order test
  expectation. The expectation was corrected after inspecting the original
  large-map body; no production body changed. Both failures remain history.
- The emitted HDL text is 744 bytes, SHA256
  `bddf15e7fde20828f1a615c9f1fb78c50cecbbd2727a3d27a9f977d2d4858fa5`;
  the testbench is 421 bytes, SHA256
  `9c4c046a82ca2f154c8d373d102cc30194e5b28199c3cbdf05c2172d6c397b1f`;
  timing is 149 bytes, SHA256
  `a580b6b5b757d1d50e97c3e8049eaef9855342c7d718630866b4ce22cc527406`.

The CI catalog appends `hdl-manifest-native-validation` for the complete cold
namespace at the existing native 2g/180-second settings. Results receive credit
only when actually observed. Committed-source independent final acceptance,
complete current-base context, hosted CI and integration remain pending.

## Unsupported boundaries

These are legacy document-data functions. Substring predicates do not parse
SystemVerilog, testbenches or SDC. Validation reads fixed text Vars rather than
the supplied manifest's text content. Presence of a width key does not establish
a legal width; empty port/audit collections pass the retained `every?` checks.
Status, proof, accepted-by and audit labels do not authenticate or prove their
contents. Coverage returns `:complete` even when individual flags are false.
No validation gap is silently repaired or promoted into authority by the move.

The retained upstream C2-HASH reader refusal remains held (expected source
bytes 231576, observed 246369). This slice does not invoke source/file-artifact
builders, retry or rebind the reader, repin source, substitute an upstream
result, or reconstruct an admitted artifact. Forged maps are explicitly
unverified test data. No source-to-backend compilation, verified hardware/domain
IR, profile/target/provider admission, width/numeric legality, clock/reset/CDC
proof, timing validity, synthesis, simulation, hardware execution, B1/B9/B13/B14
conformance, migration, release, self-hosting or seed-retirement support is
claimed. Those boundaries remain incomplete.
