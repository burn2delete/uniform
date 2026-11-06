# Native GPU document data ownership

## Scope and governing contracts

This slice moves the existing fixed B8 kernel and host-stub document text,
their structural predicates, manifest constructor, validation checks and
coverage projection from the Clojure bootstrap into `gravity.backend.gpu`.
D1, D2, D3, D8, D9, B1, B8, B13 and B14 govern the architecture and evidence
boundary. Draft PR #1265 was opened at scope commit
`e8cf19adc16202988f671e5ac80a3387cbaae703` before implementation.

The starting source is `20770feccaeb4d880e2e72e95a55cd156573b01a`, tree
`5640d7e41f24daaa7bd5a6acf4645ea1d17a055f`. Published main
`fa14c00518690feb113f9e506d3c8c0b8a96ebea` is already its ancestor. This
slice does not import unmerged foreign source or transfer historical CI credit.

## Public API and dependency direction

The stage owns the two fixed texts, the existing substring predicates,
manifest data construction, existing structural refusals and coverage data.
Bootstrap names and arities remain compatibility adapters. Explicit late Var
ports retain callback and value lookup order, including the separate kernel
and host-stub hash reads. The stage must load without `gravity.bootstrap`;
Bootstrap consumes the stage, never the reverse. The original failure
publisher retains diagnostic construction and publication. Source paths,
input identities, generated origins, source/debug maps and hashes must survive
the move.

The seven new APIs are `native-kernel-text []`, `native-host-stub-text []`,
`native-kernel-structurally-valid? [source]`,
`native-host-stub-structurally-valid? [source]`,
`native-manifest [operations source-path input-id]`,
`validate-native-manifest! [operations source-path artifact]` and
`native-coverage-report [operations artifact]`. They extend the three existing
diagnostic context lookups. The stage requires only Clojure core and
`clojure.string`. Every new API records a non-admitting contract, required
operations, inputs, outputs, and caller-owned diagnostic publication.

## Verification and artifacts

Required evidence includes emitted fixed kernel and C host-stub bytes,
deterministic fixture identities, malformed data refusals with the original
B8 identifiers, complete old/new returns and refusals, late callback
interposition, cold loading, source conservation, focused regression,
language-boundary checks and independent semantic, adversarial and composition
review. Checks receive credit only after their actual results are recorded.

Development results on the implemented source:

- The new cold namespace passed 11 tests and 163 assertions. It requires no
  Bootstrap or source builder, uses an explicit SHA256 callback for unverified
  caller data, and writes two fixed files under
  `target/validation/gpu-native-manifest/`.
- The kernel text is 561 bytes, SHA256
  `706378fe4ca515f437ae57420b494cd62afe55f7795f0b4631f049f0a46700f4`.
  The C host-stub text is 275 bytes, SHA256
  `076ac445f431a88449df8bd0a4983dd5df5f3b03244d57ccfddef5b4153c938f`.
- Existing Bootstrap compatibility tests passed 5 tests and 53 assertions
  both before and after extraction. Language-boundary and ownership checks
  passed 48 tests and 1202 assertions. The CI harness passed 54 tests and
  601 assertions, with the existing Kondo executable supplied on PATH.
- A separate comparison uses the original Bootstrap C4 operation and rebinds
  all seven original roots together from the immutable scope source. Complete
  raw and parsed records match for 56 outcomes: 20 returns and 36 refusals.
  These include every shape guard, required/forbidden text policies, returning
  and throwing callbacks, late diagnostic IDs, separate kernel/stub hash and
  content reads, predicate interposition and callee-before-subject selection.
  Receipt `/tmp/uniform-gpu-manifest-parity.edn`, SHA256
  `2b03b90e0d3c02fe2fedb5d869a7bab5b504ba9ce8c2a6d034d20fb4125097fb`.
- A fresh JVM loaded the stage and new native test namespace without loading
  Bootstrap; ten public stage APIs were present. Formatting, document and
  roadmap validation passed.
- The exact appended CI command passed all 11 tests and 163 assertions at
  2g/180 seconds. The inspected four-file transformation matches the applied
  production source; the original diagnostic function bodies remain
  byte-identical. Thirteen protected paths, the workflow and all thirteen
  prior contract command vectors are unchanged. Conservation receipt
  `/tmp/uniform-gpu-manifest-conservation.edn`, SHA256
  `69a8a74db31b40dabdc717665f2dc967990ae29e95143252c349fa8a4da2f8ce`.

The CI catalog appends `gpu-manifest-native-validation` for the complete cold
namespace at the existing 2g/180-second native-check settings. Committed-source
independent acceptance, full current-base lint context, hosted CI and
integration remain pending.

## Unsupported boundaries

These functions describe legacy document data. Labels such as `:complete`,
`:passed`, proof and capability names do not authenticate inputs or verify
Gravity semantics. The kernel text is MLIR-like text despite its legacy
`.spvasm` path; it is not evidence of valid SPIR-V assembly or an emitted
device binary. The host stub names external runtime APIs and is not linked or
executed by this slice. Substring predicates are not parsers or verifiers.
Existing status-only checks, fixed launch assumptions and other validation
gaps must remain explicitly non-authoritative.

The retained upstream C2-HASH reader refusal observed during the LLVM slice
remains held (expected source bytes 231576, observed 246369). This slice does
not invoke source/file-artifact builders, retry or rebind the reader, repin
source, substitute an upstream result, or reconstruct an admitted artifact.
Direct forged document maps are test data only. No source-to-backend, verified
MIR/domain ingress, GPU profile legality, device-memory lifetime, transfer,
synchronization, atomics, launch, numeric certificate, toolchain, hardware,
B1/B8/B13/B14 conformance, migration, release, self-hosting or seed-retirement
support is claimed. Source admission and final artifact assembly remain upstream.
