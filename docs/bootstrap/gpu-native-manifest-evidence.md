# Native GPU document data ownership

## Scope and governing contracts

This slice moves the existing fixed B8 kernel and host-stub document text,
their structural predicates, manifest constructor, validation checks and
coverage projection from the Clojure bootstrap into `gravity.backend.gpu`.
D1, D2, D3, D8, D9, B1, B8, B13 and B14 govern the architecture and evidence
boundary. Implementation and checks are pending in this early draft.

The starting source is `20770feccaeb4d880e2e72e95a55cd156573b01a`, tree
`5640d7e41f24daaa7bd5a6acf4645ea1d17a055f`. Published main
`fa14c00518690feb113f9e506d3c8c0b8a96ebea` is already its ancestor. This
slice does not import unmerged foreign source or transfer historical CI credit.

## Intended API and dependency direction

The stage will own the two fixed texts, the existing substring predicates,
manifest data construction, existing structural refusals and coverage data.
Bootstrap names and arities remain compatibility adapters. Explicit late Var
ports retain callback and value lookup order, including the separate kernel
and host-stub hash reads. The stage must load without `gravity.bootstrap`;
Bootstrap consumes the stage, never the reverse. The original failure
publisher retains diagnostic construction and publication. Source paths,
input identities, generated origins, source/debug maps and hashes must survive
the move.

## Verification and artifacts

Required evidence includes emitted fixed kernel and C host-stub bytes,
deterministic fixture identities, malformed data refusals with the original
B8 identifiers, complete old/new returns and refusals, late callback
interposition, cold loading, source conservation, focused regression,
language-boundary checks and independent semantic, adversarial and composition
review. Checks receive credit only after their actual results are recorded.

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
