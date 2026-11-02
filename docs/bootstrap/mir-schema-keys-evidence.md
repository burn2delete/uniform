# MIR: native block, operation and terminator key schemas

D1, D2, D3, D8, D9, C1, C6-C11, C15, BOOT8 and TEST2 govern this
extraction. Its source parent is a0b9220f9922f6f415829baba54578800439281e,
tree 5bb259d3a87bcb2bc8eca0f9ff261a2a91b29325, pending in draft PR832.
Parent source acceptance and checks do not transfer to this increment.

Three existing native schema sets move from Bootstrap to gravity.mir:
block-keys, operation-keys and terminator-keys. The literal key sets stay
unchanged. Legacy Vars remain snapshot aliases, matching the earlier module
and function schema aliases. Call-time legacy Var interposition still reaches
the existing operation and terminator validators. The component's envelope
wrapper remains byte-identical.

Inputs are the existing native schema definitions and supplied MIR block,
operation and terminator data. Outputs are the three closed key sets, consumed
by the native validation rules already owned by the stage. Existing callers
retain input authentication, complete semantic legality, diagnostic
construction and admission. The stage imports nothing and is consumed by
Bootstrap. Key equality is relative native schema checking, not authority.

Before extraction, the ordinary fixed seed executes current Gravity C11 and
emits MIR for both normalized Gravity fixtures. Existing native operation and
CFG validation accept each. Adding an extra field to an emitted block rejects
C11-BLOCK, missing well-formed-mir-block. Captures retain complete
source/compiler text and hashes, checked input, MIR, all schema sets,
validation products, changed MIR and full diagnostics before and after.

Tests compare all three schemas to actual emitted blocks/operations/terminators
and preserve snapshot identity, legacy call-time schema interposition and the
real malformed-block refusal. Whole two-file conservation preserves the three
literal sets and all surrounding implementation except the ownership aliases
and namespace contract. Final checks, exact source identities, complete/raw
product equality and independent review are maintained in the draft PR.

Native proof/authenticated/verifier/passed labels remain unverified data.
These sets do not establish full MIR, general CFG or runtime-check legality,
authentication, source admission, certificates, backend/native execution,
performance, migration, release, self-hosting or seed retirement. No restricted
native/private factory, frozen Reader, package/archive/release builder or
Attempt30 route is executed. Successful applicable CI, current-main
composition, independent acceptance and an immediate exact clean integration
preflight remain required before merge.
