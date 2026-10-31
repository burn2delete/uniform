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

## Evidence status

This draft precedes implementation. Integration requires real emitted Wasm
bytes, real rejection, current Gravity data-kernel parity, paired original and
extracted complete/text outputs, source/identity conservation, focused and
adversarial regression checks, independent exact acceptance, successful final
candidate CI and clean current-main preflight. No completed result is claimed.

## Unsupported boundaries

Data emission and integer decoding do not authenticate MIR or B1 packets,
admit proof evidence, verify complete modules or execute Wasm. Canonical public
emit/verify, generalized MIR/domain lowering, imports, memory, tables, WASI,
components, WIT, native host/toolchain support, full B4 conformance, migration,
release, self-hosting and seed retirement remain unsupported by this slice.
