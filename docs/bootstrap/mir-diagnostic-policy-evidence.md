# MIR: native diagnostic rule lookup, display and exception view

D1, D2, D3, D8, D9, C1, C6-C11, C15, BOOT8 and TEST2 govern this
extraction. Its source parent is 89223f8446357cfba629701f57d2305976f2da0b,
tree d3e6da109b937846d522ea2f74882e86c049a40d, integrated through PR834.
Parent acceptance and checks do not transfer to this increment.

Three existing bodies move from Bootstrap into gravity.mir:
diagnostic-rule-contract, diagnostic-message and throw-record!. Explicit data
inputs supply the original rule-contract catalog and local diagnostic ids;
live owner Vars supply the message operations. Wrappers preserve signatures,
arguments, original fallback wording, borrowed catalog identity, exception
data and live interposition. Context construction, catalog assembly, carrier
checks, record construction/identity, sanitization and containment remain
byte-identical at their existing boundaries.

Inputs are native rule/catalog data and a supplied native diagnostic record.
Outputs are the first matching borrowed contract or nil, original display
text, and the original C11 ExceptionInfo view. The stage owns this C11-specific
lookup and display/throw policy. Callers retain record and identity binding,
class/callback admission, redaction, privacy and publication policy. The stage
imports nothing and is consumed by Bootstrap.

Before extraction, the ordinary fixed seed executes current Gravity C11 and
emits MIR for both normalized Gravity fixtures. Native operation checks accept
each. A real emitted opcode changed to :llvm/add rejects C11-TARGET-LEAK,
missing checked-core-operation-parity. Rethrowing the resulting native record
preserves complete exception data and display. Changing only the message
operation preserves diagnostic id and all other fields. Captures retain full
fixture/C11/emitter texts and hashes, checked input, MIR, changed MIR, full
diagnostics, borrowed rule contract, message, rethrow and display-change data.

Tests cover real refusal records and wording-independent identity, first-match
ordering, borrowed record identity, foreign-rule fallback without a message
call, original owner arguments, late Vars and exact callback exception identity.
Whole three-file conservation permits only names and explicit ports while
preserving surrounding policy and implementations. Final checks, complete/raw
product equality, immutable source identities and independent review are
maintained in the draft PR.

The legacy word authenticated in native text or family labels is compatibility
data, not an authentication result. These helpers do not authenticate records,
resolve canonical C15 identity, redact secrets or produce a public-safe
projection. Canonical C15 conformance, diagnostic publication, full MIR,
runtime-check legality, source admission, certificates, backend/native
execution, performance, migration, release, self-hosting and seed retirement
remain unsupported. No restricted native/private factory, frozen Reader,
package/archive/release builder or Attempt30 route is executed. Successful
applicable CI, current-main composition, independent acceptance and an
immediate exact clean integration preflight remain required before merge.
