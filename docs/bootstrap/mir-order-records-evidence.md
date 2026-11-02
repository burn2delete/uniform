# MIR: native pass contract, block order and operation sequence

D1, D2, D3, D8, D9, C1, C6-C11, C15, BOOT8 and TEST2 govern this
extraction. Its source parent is 9ab1c8be61d86548a3260599c6bfa30f7a2d7bd9,
tree 49c64b7b04cb7f82381b5555ae87d471adcdd112, pending in draft PR831.
PR830's node records are also pending. Parent checks and reviews do not
transfer acceptance; inherited CI formatting and resolution watchdog failures
remain nonpasses.

Four existing bodies move from Bootstrap to gravity.mir: pass-contract-record,
canonical-block-order, operation-sequence and path-prefix?. The operation
sequence receives the original live canonical-block-order Var explicitly.
All earlier fact-table wrappers in the component remain byte-identical.
The pass-contract map is unchanged data, including its owner, verifier,
supported operation/profile/target and conditional-shape fields. Returning
this record does not execute its pass or establish any of its evidence claims.

Inputs are supplied native MIR module/function/block records, paths and the
block-order operation. Outputs are the original pass-contract map, ordered
block ids, operation vector and path predicate. The stage owns these native
record/projection bodies. Existing callers retain module/function/operation
legality, diagnostics, source and callback authentication, verification and
admission. The stage imports nothing and is consumed by Bootstrap.

Before extraction, the ordinary fixed seed executes current Gravity C11 and
emits MIR for the paired normalized Gravity fixtures. Each native module has
four canonical blocks and five operations. Native operation validation accepts
both. Reversing a block with multiple instructions changes the real product
and rejects C11-DOMINANCE, missing
canonical-checked-core-instruction-order-and-placement. Captures retain complete
source/compiler text and hashes, checked input, MIR, block and operation order,
pass contract, changed MIR and complete diagnostics before and after.

Tests compare the records and sequences to actual emitted MIR and preserve
borrowed operation identity, caller-supplied order and duplicates, missing
instruction defaults, original vector path semantics, live Vars, wrapper
arguments and exact exception identity. Whole two-file conservation permits
only function names and the explicit block-order port. Final checks, raw/data
equality, immutable source identities and review receipts are maintained in
the draft PR.

These helpers do not validate arbitrary graphs or multiple functions. They
preserve the original single-entry/conditional-diamond projection and native
path equality semantics. Native owner/proof/authenticated/verifier/passed
labels are unverified data. Full MIR, runtime-check legality, authentication,
source admission, certificates, general CFGs, backend/native execution,
performance, migration, release, self-hosting and seed retirement remain
unsupported. No restricted native/private factory, frozen Reader,
package/archive/release builder or Attempt30 route is executed.
Successful applicable CI, current-main composition, independent acceptance and
an immediate exact clean integration preflight remain required before merge.
