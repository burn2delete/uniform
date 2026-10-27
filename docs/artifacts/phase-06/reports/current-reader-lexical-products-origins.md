# Current Reader lexical products and origins

This reconciles the original
`reader/finite-remaining-work/lexical-products-and-origins-closure` row for the
existing selected-current, development-unadmitted Reader interface. It closes
the finite enabled population below. It does not reopen the accepted decoding
or fresh Syntax handoff rows, enable a new literal grammar, or establish public
admission, extension authorization, quota/authentication closure or release.

## Governing population and evidence

C2 owns lexical products and raw facts; L1 owns the enabled surface grammar.
The existing byte-identical `complete-reader-surface.gravity` and `.qst`
fixtures cover the following families through real selected-current Reader
construction, independent fresh Reader verification and actual authenticated
envelope construction/verification. The panel calls the normal five-argument
`c2-reader-products` interface. No precomputed products or caller integrity
report are supplied.

| Required family or output | Concrete executed checks |
| --- | --- |
| Nil and booleans | Exact native values and raw descriptors for nil, true and false. |
| Integers | Signed decimal, binary and hexadecimal values; radix/sign/exactness facts; arbitrary-precision digit spelling. |
| Decimals | Fraction and exponent spelling, accepted ordinary values and intrinsic host-independent-range deferment with exact components. |
| Ratios | Exact numerator/denominator spelling and values; zero-denominator deferment remains source-valid. |
| Strings | Exact UTF-8 raw slices, decoded Unicode/escape contents and ordered escape locations, including escaped backslash followed by another escape. |
| Characters | Scalar, named, Unicode, octal and delimiter spellings; one additional genuine supplementary-character `.gravity` construction. |
| Symbols and keywords | Qualified spellings and exact name codepoints. |
| List, vector, map and set | Complete form-kind census, unique IDs, ordered roots, resolvable open/close tokens and reciprocal parent/child membership. |
| Registered tagged literals | Original inst/uuid spellings, actual payload-child links and native C2 Date/UUID values. Authorization/effect policy is a separate row. |
| Metadata | Map, keyword and symbol metadata; wrapper metadata matches its decoded value while the target remains a separate referenced value. |
| Abbreviations and generated origins | Quote, syntax quote, unquote, splice-unquote, deref and metadata retain prefix slices, exact expanded operators/values and actual input child references. |
| Tokens, literals and trivia | Raw slices match every token/form; enabled trivia reconstructs the entire source. A fresh filtered counterpart has no trivia and preserves ordered nontrivia tokens and parsed values. |
| Malformed numeric candidates | Existing invalid-radix, incomplete-exponent and repeated-exponent fixtures reject as C2-NUMERIC/STAGE1READER007, retaining exact raw/byte spans and actual source paths, with no committed form graph. Fresh Reader verification verifies each rejection. |

The paired complete fixtures execute both extensions. The supplementary `.qst`
file has byte-parity evidence only; its counterpart is not claimed as a new
construction. Fixed-width suffix positions remain conditional on a separately
specified enabled numeric grammar; this change adds no suffix syntax.

PR471 retains the earlier glued numeric-candidate rejection. PR478 and PR482
retain their exact/deferred ratio and fresh Syntax-origin evidence at their
original identities. PR492 retains its mixed-newline/Unicode coordinate,
explicit identity, trivia and genuine foreign-working-directory evidence.
Those receipts are reused as complementary properties, not relabeled as new
executions of this candidate. No C3, Core, C5, C11 or C12 construction is part
of the new panel.

## Reproduced escape-location defect and correction

The hosted C2 literal-record leaf zipped regex escape matches with every raw
backslash position. An escaped backslash contributes two positions but only
one match; a following escape consequently received an earlier, incorrect
offset. For example, after an escaped backslash the next newline escape was
reported at raw UTF-16 offset 3 instead of 4.

The compiler-free baseline `3dd81bec79d9da9338abdefdc7c3fb0f85a34eb3`, tree
`32239ac5718dca9c6fc77a7b7bf13b13bd3d0c54`, reproduced the error: one test,
nine passing assertions, eight failures, no errors, natural exit 1 in 0.325
seconds. Its receipt remains a nonpass. Explicit raw-offset expectations also
check each reported substring, supplementary Unicode before escapes, Unicode
escapes, escaped quotes, no escapes and ordinary adjacent escapes.

The correction uses each complete match's start position from a fresh local
Matcher. The regex, matching order, returned record shape and existing UTF-16
`:character-offset` contract are unchanged. Byte/scalar source spans retain
their separate existing contract. No tokenization, decoding, numeric grammar,
authentication, cache, source pin or admission rule changes.

The complete pure source-identity namespace passed on
`f4e8c8638da35a4704f20ad7c1d9cde04871c84d`, tree
`c489ada3d1afe953a1c95f398dfcfa5643792457`: nine tests / 146 assertions,
natural exit 0 in 0.352 seconds, 256 MiB/60 seconds, Bootstrap unloaded and
identical clean bookends. The source and pure-test blobs remain unchanged in
the later genuine candidate; only pre-execution lexical test oracles changed.
These supplied-form leaf controls grant no product authentication authority.

## Genuine execution and row disposition

The immutable seven-call panel executed
`8a0d6a90f00e53fa90a9c0e9fe2c2207c0b09190`, tree
`79ed93abcb3fd6b2a7e61ed9ec993028cd419a37`. Two exact selectors passed 6,241
assertions with no failures or errors, natural exit 0 in 27.299 seconds, actual
2 GiB and the unchanged 120-second watchdog inclusive of Bootstrap and all
constructor/verifier work. Both brokers and the canonical heavy lock serialize
the execution. All seven start/return pairs completed; before/after source,
tree and launcher bookends match, owner/report roots are restored and both
output streams are empty.

The panel receipt SHA-256 is
`d1ad952c6d7ab5a7e6c2921d3c208923a98f68d7d6b4387414f90b2548eda25b`.
The exact launcher SHA-256 is
`60660765095582bcb45f972568288188875e9f799e4b6e31a6fcf41cf5d31a00`.
Compiler-free harness controls passed 21/21, including fixed-cardinality
progress capture and constructor arity/owner-route checks. Independent review
accepted the source correction, pure baseline/candidate evidence and genuine
fixed-catalog result. Pre-run test-shape and syntax corrections were never
executed as source acceptance attempts.

## Current-main composition

Main `4fa8575b` updates homogeneous List-return premises/domains to v2, removes
their accounting fields and adjusts typed constraint census handling. Those
changes are preserved by normal reconciliation. That route consumes checked
Core/type facts; the exercised Reader route builds its `sh03-` closure through
the unchanged seed compiler and normal Reader runtime. The full emitter source
and binding hashes change, so the genuine receipt keeps its original tuple.

Composed commit `3b7c1280a0a5e731e6415dc9e89f2f83e38e354a`, tree
`8049ebfcd8eb0d37a70a5ecdd79762308b704bd1`, passed the compiler-free source-identity
namespace and language/LOC gates: 20 tests / 169 assertions, natural exit 0 in
1.308 seconds, actual 512 MiB/60 seconds, Bootstrap unloaded and matching clean
bookends. Both output streams were empty. All three owned source/test blobs
remain identical to the genuine candidate. The composition receipt SHA-256 is
`30a55357d21d1b437b461271f4bf1cef75541460240a7b1ae3cd9d5adc76127f`.
No seven-call replay or reissued source acceptance is claimed.

This original lexical-products/origins row is complete at the stated enabled
boundary. Historical fixture-README quota expectations remain historical and
are not current acceptance rules. In particular, huge-decimal deferment here
is intrinsic representability, not a spelling-length/work allowance. The
separate extension-policy, authentication/quota-removal, contract-coverage and
component-release rows remain open. No full-source C5/C14 result, public
admission, whole Reader release or speedup is inferred from these receipts.
