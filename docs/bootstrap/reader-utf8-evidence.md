# Reader UTF-8 decoding boundary

## Governing contracts and scope

D1/D2/D3/D8/D9, L1, C1/C2/C3/C15, BOOT1/BOOT8 and TEST2 govern this
increment. L1 and C2 require deterministic strict UTF-8 decoding and exact
source-byte/hash and diagnostic joins. They distinguish source provenance from
semantic identity, and syntax acceptance from downstream semantic legality.

Move only the existing native `decode-gravity-source-bytes` body from
component00778 into `gravity.reader/decode-source-bytes`. Bootstrap retains the
compatibility wrapper, source-extension policy, file opening, parsing and all
surrounding compilation behavior. Explicit late raw-byte hash and diagnostic
Var ports preserve lookup/evaluation order and the existing L1-SOURCE-ENCODING
failure message/data. The stage owns the decoding decision and delegates
diagnostic construction to the existing owner. It performs no file IO.

## Evidence obligations

Initial actual main is0e01fc5937d5b224dd03e14751566f0e541b6529. Capture
complete before/after accepted and malformed-byte decoding results, full
diagnostics with source id, exact byte span, raw-byte hash and decoder state.
Cover empty input, ASCII, multibyte Unicode, supplementary scalars, CRLF,
overlong encodings, surrogates, truncation and out-of-range code points.

Emit real source/Reader artifacts and genuine Core artifacts from complete
Gravity and QST files through the documented development-current-reader API.
Compare complete values/serialization, exact source provenance and recomputed
identities. Preserve the entire original component except its decoder wrapper;
keep existing artifact assembly, compatibility dispatch and source policy.
Run focused and affected tests, language boundary, docs/roadmap/governance,
contextual lint, independent adversarial review, own CI and clean current-main
integration preflight. This initial scope conveys no implementation credit.

## Implemented development evidence

The initial native controls accept9 valid UTF-8 cases and reject11 malformed
byte sequences, with exact raw-source hash, failure message/data and offset
joins. Existing Reader tests pass6/71 before extraction and10/196 afterward.
The decoder does not consult error/hash ports for valid input; malformed-input
hashing precedes the late diagnostic Var read. Original hash exceptions propagate
unchanged. Real malformed files of both source spellings stop before form reads.

Both complete `compiler-exact-keys.gravity` and `.qst` subjects emit real Core
artifacts through the documented public current-reader route before and after
extraction:25 nodes, one API target read, source SHA
`8e63ea9e37ab40a8e66975b3e58dc8eb5b9b74aa339d2625d5bd67070802758d`
and recomputed Core id
`sha256:16ec6d50a8514188f3010880f3022e88f7f1054fe4058a5caa6b74367ee98f0e`.
Actual source paths/extensions remain distinct. The whole390-function current
Core implementation is compiled for the public route; its generic-seed,
development-unadmitted, incomplete and non-self-hosted boundaries remain.

Four complete and serialized product pairs compare exactly: native decoding,
argument context and both complete Core products. The original decoder body
and parameters conserve except the two explicit late Var ports. Whole
component00778, existing artifact assembly and Bootstrap conserve, except the
single decoder compatibility wrapper. File opening, extension policy and
surrounding source/compiler behavior remain unchanged. Ownership37/1076,
language6/11 and selected public ingress4/50 pass; no pinned constructor is
executed by those selections.

The first new-test launch stopped during compilation because an extra closing
parenthesis placed a local outside its scope; no tests ran. Its log is retained
as `/tmp/uniform-reader-utf8-test-scope-nonpass.log`. Correcting that test scope
passes10/196 without changing product code, source policy, guards or pins.

Scripts are `/tmp/uniform-reader-utf8-{control,verify}.clj` and the public Core
control `/tmp/uniform-cli-core-arguments-control.clj`. Native products use
`/tmp/uniform-reader-utf8-evidence/{before,after}-native.edn`; Core products use
`/tmp/uniform-cli-core-arguments-evidence/reader-{before,after}-*`. Logs use
`/tmp/uniform-reader-utf8-*`. These results bind actual0e source inputs; newer
main requires genuine composition, affected checks, exact review, own CI and
clean preflight. Namespace presence is not Reader completion.

## Verified Build main composition

Own CI41269521720 passes nine enabled jobs on4d068936/base0e, with
snapshot/prerelease skips. Main then advances through the Gravity compiler
finalizer change and verified Build PR737 to
3b2b7bb267b8a8b1c6a2811b89abba376a0ca2cc/tree
65efa902052c808cb9083b5188a7312cf64ef583. The normal Reader composition has
no conflicts; the independent namespace-map rows merge. Decoder, wrapper and
tests remain unchanged. All incoming compiler/Build source, fixtures, unique
helper/test identities and ownership metadata are preserved. Fresh actual-main
native and genuine public Core controls, complete comparisons, affected checks,
exact review, own CI and clean preflight remain required. The previous green
does not accept the new tuple or renew migration/release authority.

## Unsupported boundaries

This remains temporary native Clojure seed decoding. Trusted operation ports
are host dependencies, not source-callable authority or authenticated evidence.
No new encoding, replacement decoding, source-byte quota, tag handler, file
grant, canonical C2 authentication, complete Reader/Core support, migration,
release, self-hosting or seed-retirement claim follows. Heap/array/ByteBuffer
and process restrictions remain actual host boundaries. Historical denied
Reader, H6, native and full-compatibility routes remain unused.
