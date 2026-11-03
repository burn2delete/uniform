# Rejected ASCII prefix experiment

## Scope and contracts

D6, D9, C2, C3 and TEST2 govern this temporary seed experiment. A JVM sampling
profile of the unchanged Resolution replay test observed source-position
prefix scans among its sampled frames. That observation did not establish the
CI timeout cause. The proposed change tests an ASCII-only regex scan before
the existing UTF-16/UTF-8 loop in gravity.source-span.

The public API, source location fields, source paths and caller-owned
diagnostics are unchanged. The intended guard preserves Java replacement-byte
behavior for unpaired surrogates, including prefixes ending inside a pair.
It introduces no cache, admission, authority, input quota or check elision.

## Observed rejection

The experiment is rejected for performance. On OpenJDK 26.0.1, macOS aarch64,
512 evenly spaced coordinates of the actual 67,125-unit C15 source were
measured after two warmups, with three samples:

- Existing loop: 25.529375, 25.335792 and 23.868167 milliseconds.
- Proposed regex path: 83.498875, 109.436333 and 93.659416 milliseconds.

The input source SHA-256 is
4dea49ae632a5d07399ec5db45112103f44c510534b097a1199c7720da86aacf.
The original source-span helper SHA-256 is
3ef839bbd785116333a12ac2c448587340c12c4d3c6447e677d06d557a9e9ead;
the experiment helper SHA-256 is
0b0005026654de4c5bc19c89bc4b95a44e45b50ec9f2e911a9e88f9548ffd567.
These are host observations, not Gravity target, backend, safety or realtime
performance claims. No CI deadline or selector is changed.

The Clojure capture script and complete original location records are retained
at /tmp/uniform-reader-ascii-prefix-capture.clj and
/tmp/uniform-reader-ascii-prefix-evidence/. The before/after logs are
/tmp/uniform-reader-ascii-prefix-before.log and
/tmp/uniform-reader-ascii-prefix-after.log. The existing main baseline also
compiled all 108 C15 functions and retained C15-SCHEMA and C15-FACTS refusals;
no experiment-source C15 compilation or full integration result is claimed.

## Disposition

Complete original before/after location records match in serialized and EDN
form. The focused source-span run recorded 6 tests, 1,132 assertions and one
failure: its existing narrow dependency-contract assertion rejects the added
Pattern dependency. That assertion is preserved, not relaxed. The language
boundary passed 6 tests and 11 assertions. The first equality-check launcher
had a command-line expression syntax error; that nonpass is retained and the
correct file-based read-only comparison passed separately.

Keep this experiment unmerged and grant no roadmap credit. Focused semantic
checks and independent review may characterize the source, but cannot turn
the measured slowdown into acceptance. The prior PR846 timeout remains a
failure. Source authentication, release, migration, self-hosting and seed
retirement are not claimed.
