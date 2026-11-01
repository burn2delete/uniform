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

## Unsupported boundaries

This remains temporary native Clojure seed decoding. Trusted operation ports
are host dependencies, not source-callable authority or authenticated evidence.
No new encoding, replacement decoding, source-byte quota, tag handler, file
grant, canonical C2 authentication, complete Reader/Core support, migration,
release, self-hosting or seed-retirement claim follows. Heap/array/ByteBuffer
and process restrictions remain actual host boundaries. Historical denied
Reader, H6, native and full-compatibility routes remain unused.
