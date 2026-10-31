# LLVM scalar lowering data extraction

## Contract and scope

D1, D2, D3, D6, D8, D9, C1, C11, C14, C15, B1, B3, B13, B14 and TEST2
govern this incremental extraction into `gravity.backend.llvm`. Gravity's
existing semantic owner remains
`bootstrap/gravity/src/gravity/backend/b3_llvm_backend_design.gravity`.

The bounded change moves scalar operation legality, ordering, LLVM instruction
rendering, evaluation and unverified module reconstruction from the existing
seed into the stage namespace. Explicit operations and policies cross the
boundary. The full B1/C11 admission, source binding, opaque authority holders,
native execution, process and publication owners remain in Bootstrap.

## Evidence status

The draft precedes implementation. Integration requires actual emitted LLVM
text, meaningful data rejection, paired original/extracted records and text,
current Gravity data-kernel execution, provenance and identity checks, focused
regression tests, independent exact acceptance, successful candidate CI and a
clean current-main preflight. None of those results is claimed yet.

## Unsupported boundaries

Data helpers do not authenticate MIR, validate callbacks or policies, issue
proof certificates, run LLVM verification or native code, or admit sealed
artifacts. Reconstructed record labels remain data. Canonical public backend
emit/verify, full B3 conformance, generalized MIR/domain lowering, proof-gated
metadata, native toolchain/ABI support, migration, release, self-hosting and
seed retirement remain unsupported by this extraction.
