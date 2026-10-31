# C17 emission data extraction

## Contract and scope

D1, D2, D3, D6, D8, D9, C1, C15, B1, B2, B13, B14 and TEST2 govern
this partial seed extraction into `gravity.backend.c`. The existing Gravity
owner is `bootstrap/gravity/src/gravity/backend/b2_c_backend_design.gravity`.
The implementation moves C17 expression, source, header and supporting
artifact-record construction out of Bootstrap. It preserves compatibility
arities, call ordering, source/proof associations and deterministic text.

The input is explicit operation and manifest data already checked by callers.
The output is C text and data records, not authenticated backend admission.
The existing B2-MANIFEST exact-comparison refusal remains a real diagnostic;
the existing owner retains diagnostic publication and genuine B1/C11 input
verification. No native toolchain, Gate A/Gate B, migration or release probe
is authorized by this data extraction. The lexical authority holder and full
guarded assembler remain in Bootstrap.

## Evidence status

This draft records the bounded change before implementation. Before integration
it requires paired original/extracted outputs, current Gravity data-kernel
execution, actual rejection, focused regression checks, exact independent
acceptance and successful candidate CI. No completed validation is claimed yet.

## Unsupported boundaries

Canonical public backend `emit`/`verify` remain proposed. This slice does not
establish full B2 conformance, authenticate supplied facts or callbacks, issue
proof certificates, execute C, verify the configured ABI/toolchain, implement
general MIR or domain IR lowering, or complete self-hosting or seed retirement.
Historical data labels such as proven-safe and proof-authorized are copied
records whose authority still depends on the unchanged upstream gates.
