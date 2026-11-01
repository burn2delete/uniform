# Current Reader lossless-literal Syntax handoff

## Purpose

Preserve the existing exact and deferred ratio products when Syntax consumes
current Reader seeds with explicit Unicode scalar coordinates. This is a C2/C3
compatibility correction, not a new numeric or language family.

## Correction and preserved checks

PR471 retained explicit scalar coordinates in Reader seeds while preserving
legacy form spans. The lossless descriptor still compared a seed span exactly
with the older form-span shape. The compiler-free reproduction showed that
otherwise matching `1/2` and `1/0` products were declined solely because the
current seed carried the additional scalar fields.

For a plain native C2 artifact with a retained seed stream, the comparison now
uses the matching retained occurrence at its legal form ordinal. Form and
syntax IDs must match. Every legacy span field, including the form ordinal,
remains exact; explicit scalar coordinates must be complete, legal and equal
to the supplied seed. Present-but-invalid retained streams do not fall back.
Products without a retained stream preserve the original comparison.

Opaque artifact maps retain their original field lookup path. Native artifact
entry selection uses key identity, preserving callback behavior even with an
opaque colliding key. The helper compares values from already authenticated
products; it grants no authentication, cache or proof authority.

Original form/token/raw-spelling/literal/deferment equality checks, fresh
integrity callbacks and authenticated-envelope verification remain active.
No source pins, numeric classification, source authentication or runtime
resource controls changed. The accepted Unicode/metadata/origin evidence from
PR471 is retained without replay.

## Retained validation

The compiler-free baseline at
`116e2d26bc07693614aeeb3d0a2c7b0178344a14`, tree
`a6c8cafec6764571c6e4d6322a755ca8f094c758`, exited naturally 1: 8 tests,
81 passing assertions and 15 failures. This is a nonpass, not genuine source
constructor acceptance. A subsequent test-only type-hint read error stopped
the first candidate before tests; its correction did not change production code.

Corrected leaf validation at
`e61aecc18f02eb852650bad60dbbd42707e6d962`, tree
`449813903e76a0f92f6a942b003df53ace1d0e5a`, passed 10 tests / 102 assertions,
natural exit 0 at 256 MiB/60 seconds, with bootstrap unloaded and clean identical
bookends. Controls cover current and legacy seed shapes, exact/deferred values,
synthetic metadata wrappers, malformed/mismatched retained occurrences, scalar
substitution, callback freshness, original thrown-object identity and opaque keys.

Genuine source validation at
`6d55ee5a6c06821de384eb844b9aa7d703f0af34`, tree
`9c561828d21ea1aa4605c7f856d39cf0b4d2228b`, passed 12 tests / 60 assertions,
natural exit 0 in 30.489 seconds under the unchanged serialized 2 GiB/120-second
profile. Source/control bookends matched and owner/reporting roots were restored.
Receipt SHA-256:
`0cc4c868607af4219717e937833296d257ea2b8d4150848428fe6d7db5487ec7`.

The exact selector is
`gravity.self-hosting.sh04-current-reader-lossless-test/current-reader-lossless-and-deferred-ratios-retain-authenticated-seed-spans`.
It constructs one actual eight-byte `.gravity` C3 source through the genuine
seven-argument constructor, with explicit current Reader invocation and nil
precomputed products/candidate. There is no separately built C2 prefix.
Fresh owner verification accepts the exact native descriptor and the retained
zero-denominator deferment. Descriptor-value, deferment-status and retained
Reader-seed scalar substitutions fail fresh proofs. Language-boundary and LOC
gates pass in the same bounded batch.

The `.qst` fixture is byte-identical, but this run does not claim a fresh `.qst`
C3 construction. Paired source/provenance behavior and Unicode span/origin
coverage retain their separate PR471 provenance.

## Limits

Authentication remains selected-current and `:development-unadmitted`, with
Clojure seed/generic execution residuals. This finite result does not establish
the complete numeric surface, Reader/Syntax release, full-C11 C3/C5, C14,
backend emission, performance improvement or seed retirement. Earlier incomplete
full-source diagnostics retain their original nonpassing scope.
