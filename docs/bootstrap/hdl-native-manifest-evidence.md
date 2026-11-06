# Native HDL manifest ownership

## Scope and governing contracts

This slice moves B9 manifest construction, artifact checks and coverage
reporting from the temporary Clojure bootstrap into `gravity.backend.hdl`.
The stage already owns the fixed HDL, testbench and timing texts, their
substring predicates, diagnostic assembly and override handling. D1, D2, D3,
D8, D9, B1, B9, B13 and B14 govern the implementation and evidence boundary.
Implementation and checks are pending in this early draft.

The starting source is `ce5cebf6de745402db1dde36170861b06f1429ab`, tree
`1c515afeae2457354f07e5dbe9867cbd214e4290`. Published main
`fa14c00518690feb113f9e506d3c8c0b8a96ebea` is already its ancestor.
Inherited slices retain their own evidence; their CI is not transferred here.

## Intended API and dependency direction

The new APIs are `native-manifest [operations source-path input-id]`,
`validate-native-manifest! [operations source-path artifact]` and
`native-coverage-report [operations artifact]`. They consume unverified
document data and explicit late operations, and produce the existing legacy
HDL manifest, structural completion/refusal and coverage records. Bootstrap
names and arities remain thin adapters. The stage continues to depend only on
Clojure core and `clojure.string` and is consumed by Bootstrap.

Late ports retain the original C4 hash callback, three text values, three
structural predicates, diagnostic IDs and failure operation. Preserve HDL,
testbench and timing hash-call order and later independent content reads.
Preserve all artifact checks, including port-width presence, clock/reset
records, unmediated crossing refusal and hardware-audit checks. Diagnostic
payload assembly already belongs to the stage; Bootstrap retains throw
transport, source admission and final source/file-artifact assembly.

## Verification and artifacts

Required evidence includes actual `.sv`, testbench and `.sdc` output bytes,
stable callback-produced hashes, source/input identity and origin maps,
positive and negative document fixtures, exact diagnostic order, late
interposition, old/new complete record comparisons, cold stage tests,
compatibility regression, language-boundary checks and independent reviews.
No result is credited before its actual receipt is recorded.

## Unsupported boundaries

The inherited manifest's acceptance, proof, synthesis, simulation and audit
labels are unverified document data. Shape checks do not authenticate hardware
IR or validate numeric widths, signedness, clock/reset semantics, CDC proofs,
finite control, interfaces or target timing. Width-key presence is not width
legality. Empty port or audit collections can satisfy existing `every?`
checks; the coverage report's `:complete` status is not a certificate.
Substring predicates inspect the fixed text values rather than parse HDL or
authenticate manifest content. No simulator, synthesis tool or hardware
execution is claimed by this slice.

The retained upstream C2-HASH reader refusal remains held (expected source
bytes 231576, observed 246369). Do not invoke source/file-artifact builders,
retry or rebind that route, repin source, substitute upstream artifacts or
reconstruct an admitted packet. Forged in-memory document maps are test data
only. No source-to-HDL, verified hardware/domain ingress, B1/B9/B13/B14
conformance, migration, release, self-hosting or seed-retirement credit is
claimed.
