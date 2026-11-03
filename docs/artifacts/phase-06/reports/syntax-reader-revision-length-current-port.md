# PR566 current Syntax port: Reader revision length

## Scope and governing contracts

This scoped current-source port carries the useful PR566 correction onto the
current Syntax implementation. It removes only the `1048576` upper bound on
declared `:source-byte-count` in `c3-reader-source-revision?`. A declared
length remains a nonnegative integer; it is not a project work allowance or a
source-construction budget.

The original PR566 head is
`75a973668750e0c351d5493ef375c799ed7de358` (tree
`5ff52f66382e4bf6e245ef6880ffef8f07dcf0ed`), authored from
`6276c94b0b753c888ef8fecc794bcc81245ea3b3`. The current integration base is
`f43e77992fe86b6dfb55df3516b27cfcdea7fa40` (tree
`a0e433f9f185b651f6f5ebc54340a085a5cd6721`). Implementation commit
`4b11aa85951e3ef25c4eb6461b9f9bfb21a7adc7` (tree
`0a2a78fdbd1dd42a67d74ebafba1f52693e2bea0`) applies the current source and
compiler-free controls. Commit
`6c275b7aa8e3ba62b150d601c5a8ad6aa4682e10` preserves the original PR head as
an ancestry-only second parent; it does not replay the stale PR tree.

The changed Gravity source is
`bootstrap/gravity/src/gravity/bootstrap/syntax.gravity`. Its resulting
SHA-256 is
`a5554ba54b53fc0b75eabd115eb2cc703bb6acaa6e3c7e3fd5ea33fc44973710`.
Relative to the current base, the source diff is exactly two deleted lines:
the upper-bound conditional and its obsolete false branch. Exact revision
keys, artifact/schema/owner and source language/path checks, entry and
verifier function checks, every SHA-256 identity check, and Reader
binding/revision identity comparisons remain in place.

This follows C2's requirement that the Reader impose no project source-byte,
token, form, scalar, result-node, or construction-work quota while retaining
UTF-8/Unicode, lexical, source identity, native carrier, metadata, callback,
and target-representability checks. C3 syntax identity and provenance remain
separate downstream obligations.

## Compiler-free controls

The new test
`bootstrap/clojure/test/gravity/syntax/reader_revision_test.clj` reads only
the seven pure current-source predicates with read-eval disabled in a
temporary namespace. It does not load Bootstrap or construct a source Reader
result.

```text
clojure -J-Xmx256m -M:dev-test --namespace gravity.syntax.reader-revision-test
```

Natural exit 0: 3 tests / 58 assertions, 0 failures, 0 errors. The controls
accept declared lengths `0`, `1048576`, `1048577`, and `Long/MAX_VALUE`.
Negative, nil, boolean, string, floating-point, and ratio lengths reject. They
also retain rejection for wrong artifact/schema/owner/language/path/function
fields, missing or extra keys, malformed SHA-256 IDs, and each of the five
Reader binding/revision identity joins. No large source input is constructed.
The test source SHA-256 is
`e6f3e597f32c008004af4889b7d92e17441513fec760d23a90322148d60c3da7`.

The required language-boundary gate was also run:

```text
clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

Natural exit 0: 6 tests / 11 assertions; boundary validation reported 0
skipped namespaces. `git diff --check` passed with no output before the
implementation commit.

## Evidence limits and next gate

These are bounded compiler-free controls over declared metadata. They do not
construct a large source, run the authenticated C2 Reader, exercise C3 graph
construction, renew a source or compiler authority pin, or establish Guest,
C5, C12, release, self-hosting, or seed-retirement evidence. The historical
PR566 report remains tied to its original source tuple; this report records
the current-source port and does not relabel that historical evidence.

Independent acceptance and coordinator integration remain required. The
current port does not close ACC-13 or any broader C2/C3 implementation row.
