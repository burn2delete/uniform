# MIR: native opcode and source-operation vocabularies

D1, D2, D3, D8, D9, C1, C6-C11, C15, BOOT8 and TEST2 govern this
extraction. Its source parent is ee460c1003fd3ead39bd75be26e72343555ef530,
tree 57b6fdf1c30702af50fa41014032dc63816defce, pending in draft PR833 at
source creation. Parent checks and acceptance do not transfer to this slice.

Two existing native sets move from Bootstrap to gravity.mir:
allowed-opcodes and allowed-source-operations. The literal vocabularies are
unchanged. Legacy Vars remain snapshot aliases, and call-time legacy Var
interposition still reaches the existing operation validator. The component's
source pins, carrier prefix, digest and diagnostic tail remain byte-identical.
No accounting, source-binding or diagnostic policy is changed.

Inputs are the existing native opcode/source-operation definitions and native
operation data consumed by the stage's existing relative rules. Outputs are
the two closed keyword sets. The stage owns these vocabularies beside its
operation validation body; callers retain complete legality, profile/effect/
capability/safety checks, diagnostic construction, input authentication and
admission. The stage imports nothing and is consumed by Bootstrap.

Before extraction, the ordinary fixed seed executes current Gravity C11 and
emits MIR for both normalized Gravity fixtures. Native operation checks accept
each. Replacing an emitted opcode with :llvm/add rejects C11-TARGET-LEAK,
missing checked-core-operation-parity. Captures retain complete source/compiler
text and hashes, checked input, MIR, both vocabulary sets, native validation
products, changed MIR and full diagnostics before and after.

Tests compare vocabulary membership with actual emitted operations and retain
snapshot identity, legacy call-time interposition and original refusals for
opcode and source-operation corruption. The latter preserves the original
C11-MODULE classification, confirmed on the unmodified parent. Whole two-file
conservation preserves both literal sets and all surrounding implementation
except the ownership aliases and namespace contract. Final source identities,
checks, complete/raw product equality and independent review are maintained in
the draft PR.

Membership alone does not establish operation legality. In particular the
retained runtime-check vocabulary does not establish a supported runtime-check
program; compiled positive fixtures contain none. Native proof/authenticated/
verifier/passed labels remain unverified data. Full MIR, arbitrary CFG or
runtime-check legality, input/callback authentication, source admission,
certificates, backend/native execution, performance, migration, release,
self-hosting and seed retirement remain unsupported. No restricted native/
private factory, frozen Reader, package/archive/release builder or Attempt30
route is executed. Successful applicable CI, current-main composition,
independent acceptance and an immediate exact clean integration preflight
remain required before merge.
