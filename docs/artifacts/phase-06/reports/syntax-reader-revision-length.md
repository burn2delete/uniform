# Syntax Reader revision byte length

## Scope and contracts

This finite ACC-13 correction removes only the `1048576` upper bound on the
Reader implementation's declared `:source-byte-count` in
`c3-reader-source-revision?`. It follows the accounting-removal requirement in
[C2](../../../phase-06-compiler-architecture/081-c2-reader-implementation-design.md#literal-decoding)
and preserves [C3 source identity](../../../phase-06-compiler-architecture/082-c3-syntax-object-model.md#syntax-verification).
The independent source/composition review identified this remaining condition
in PR512 and PR544. It is also present on this increment's main baseline,
`6276c94b0b753c888ef8fecc794bcc81245ea3b3`, tree
`9f464ed0f03866bd4978e4213a488f3b1ae48683`.

Nonnegative integer validation, exact revision keys, artifact/schema/owner,
source language/path, entry/verifier functions and every SHA-256 shape check
remain. The Reader binding/revision identity comparisons are unchanged. A
well-formed digest is not by itself an authenticated source identity.

## Compiler-free controls

```bash
clojure -J-Xmx256m -M:dev-test --namespace gravity.syntax.reader-revision-test
clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
git diff --check
```

The focused controls pass 3 tests / 58 assertions; the language boundary passes
6 tests / 11 assertions. Controls read the exact seven pure source predicates
with reader evaluation disabled and evaluate only those definitions as Clojure
in a temporary namespace. They do not load Bootstrap or execute Gravity.
Declared lengths zero, at the former ceiling, above it and `Long/MAX_VALUE`
pass. Negative and malformed lengths, missing/extra keys, wrong owner/function
fields, malformed hashes and each of the five mismatched binding/revision
identity joins reject. No large source input is constructed.

A separate source-form comparison against the baseline confirms that removing
this one conditional produces every candidate source form exactly. The original
extracted predicate accepts `1048576` and rejects `1048577`, establishing that
the new above-ceiling control distinguishes the correction.

## Evidence limits and next gate

Independent exact-head review and adoption remain separate. This increment
does not import PR544's selected-source implementation or close ACC-13. The
original PR512 Guest result remains bound to its original source/compiler/
emitter/runtime tuple. No selected C5/Guest-AE execution, real generated
diagnostic composition, fresh Reader authentication, SH07 selected-source
support or SH25 proof renewal is established. Historical proof pins are not
rewritten. The existing Syntax implementation tasks remain open; no release,
self-hosting or seed-retirement claim follows from these host controls.
