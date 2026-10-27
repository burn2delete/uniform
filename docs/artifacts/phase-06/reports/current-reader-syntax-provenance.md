# Current Reader to Syntax provenance regression

## Purpose

Exercise the selected-current Reader to Syntax handoff required by C2 and C3:
preserve Unicode source coordinates, metadata and origins; retain paired source
semantic identity and actual path provenance; reject malformed or tampered input.

## Correction

Reader source slices retain Unicode scalar offsets separately from legacy
nested `:char` offsets, which count UTF16 code units. The seed projection omitted
the scalar fields. Syntax then interpreted the legacy offsets as scalar offsets,
making coordinates after a supplementary character one scalar too large.

The hosted seed projection now copies explicit scalar coordinates from the
matching retained Reader root slices. Raw and adapted form IDs differ, so roots
are joined by the existing root order and exact byte intervals. Missing,
misaligned or malformed retained slices reject before seed delegation. Hosted
products without a retained Reader result keep their existing span shape.
Legacy nested character offsets remain unchanged.

Fresh Reader verification, source authentication, product hashing and Syntax
verification remain active. The helper projects already verified owner data;
it adds no authentication or cache authority. Explicit scalar fields enrich the
seed shape and can change freshly derived artifact identities. Historical
identities are not repinned or relabeled.

## Validation

The production baseline is retained at
`fbc5cc5bc89748f9f4b833975f4c7c3c658a6b8f`, tree
`c389f3521dec249786d0d7c5396dad4bf0d256e8`. The compiler-free scalar and existing
projection tests exited naturally 1: 11 tests, 53 passing assertions and 20
failures. This is a nonpass.

The corrected source was tested at
`4d57957f63f7c557f1962ae9af4abc1bcb74a449`, tree
`5bc8d78eaf8f71a81c05d979ce2eef8ee3b81861`:

- Compiler-free scalar and existing projection contracts: 11 tests, 75
  assertions, zero failures/errors, natural exit 0 at 256 MiB/60 seconds.
- Genuine selected-current paired source construction, fresh Syntax proofs,
  rejection controls, language boundary and LOC gates: 12 tests, 122
  assertions, zero failures/errors, natural exit 0 in 53.542 seconds at
  2 GiB/120 seconds through the normal serialized host guards.
- Source/control bookends were clean and identical; owner and reporting roots
  were restored. Source-run receipt SHA-256:
  `2eb5f2427274abeb13c22c136eb284eb55a6ac4dd3f769fe4e1143ccfcf69c19`.

The exact integration selector is
`gravity.self-hosting.sh04-current-reader-provenance-test/selected-current-reader-preserves-syntax-spans-origins-and-pair-identity`.
It invokes the genuine seven-argument C3 source constructor with explicit
current Reader invocation and nil precomputed products/candidate. Both actual
source extensions are constructed; there is no separately constructed C2 prefix.

The fixture includes actual supplementary Unicode and metadata. Assertions
independently calculate UTF8 byte slices, scalar offsets and line/column
positions, check Reader seed references and source/generated producer links,
and compare paired identities while preserving distinct provenance bindings.
Span, origin and metadata substitutions fail fresh owner proofs. Malformed
`12abc` produces `C2-NUMERIC` / `STAGE1READER007` before the Syntax builder runs.
Compiler-free controls also retain callback return identity, second-call
rejection, unchanged hosted spans and malformed root-mapping rejection.

The earlier genuine fixture at `f0de5f3b` exited naturally 1 with 112 passes and
10 failures. Six failures exposed the scalar bug; four additionally reflected
a test expectation that incorrectly compared nested seed line/column fields
with flat Syntax fields. The corrected oracle retains independent line/column
checks. That failed receipt remains separate from the passing correction.

## Scope and residual boundaries

This establishes only the finite selected-current Reader to Syntax handoff.
Its authentication remains `:development-unadmitted`, with the Clojure seed and
generic execution residuals explicit. It does not establish frozen public
admission, complete Reader/Syntax release, full-C11 C3/C5, C14, backend emission,
performance improvement or seed retirement. Earlier full-source timeouts and
other failed diagnostics retain their original nonpassing scope.
