# SH-07 C10 Development Artifact Reuse Attempt 2 Evidence Erratum

## Purpose

This append-only erratum corrects path and focused-test identifiers in the
immutable lifecycle record for
`sh07-c10-development-artifact-reuse-attempt-2`. It does not modify the
semantic carrier, iteration-cache runner, or their tests.

## Corrections

The integrated implementation paths are:

- `bootstrap/clojure/test/gravity/self_hosting/sh07_semantic_carrier.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_semantic_carrier_test.clj`

The immutable record incorrectly calls both files
`sh07_semantic_run_product_carrier...`. Its other five owned paths are
correct.

The focused carrier namespace is
`gravity.self-hosting.sh07-semantic-carrier-test`. The immutable record's
`gravity.self-hosting.sh07-semantic-run-product-carrier-test` command names a
namespace that does not exist and therefore is not passing evidence. The
correct command passed 18 tests and 128 assertions after integration.

The carrier test file contains the following relevant exact fixtures rather
than the composite fixture names recorded originally:

- `construct-carrier-neutralizes-exactly-eleven-slots-and-closes-shape`
- `carrier-integrity-and-pins-fail-closed`
- `bind-carrier-rebinds-second-path-without-aggregate-aliasing`
- `carrier-and-candidate-values-are-closed-and-metadata-free`
- `every-live-pin-family-invalidates-a-carrier-hit`
- `source-snapshot-schema-is-exact-and-typed`
- `top-level-carrier-tampering-fails-as-data-without-throwing`

The runner test file contains the source-mutation, invalid UTF-8, bounded
source, failed producer, recursion, interruption, and cancellation fixtures:

- `semantic-cache-producer-failure-clears-flight-and-allows-retry`
- `semantic-cache-cancellation-clears-flight-and-allows-retry`
- `semantic-cache-reentry-fails-and-retry-is-fresh`
- `exact-c10-route-rebinds-one-successful-carrier-across-paths`
- `semantic-source-snapshot-detects-mutation-during-bounded-read`
- `semantic-source-snapshot-rejects-malformed-utf8-and-oversize`

The authentic opt-in fixture remains
`sh07-b8-semantic-carrier-authentic-acceptance-evidence` in
`gravity.self-hosting.sh07-try-catch-test`. Per the user's explicit direction,
its long execution result was assumed successful for reconciliation and was
not rerun in this bounded erratum.

## Acceptance

- Preserve the immutable original lifecycle record.
- Append a terminal lifecycle record for this correction.
- Treat the nonexistent carrier namespace command in the original record as
  invalid evidence and use the correct 18-test, 128-assertion replay instead.
- Grant no additional implementation, performance, roadmap, release,
  self-hosting, or seed-retirement authority.
