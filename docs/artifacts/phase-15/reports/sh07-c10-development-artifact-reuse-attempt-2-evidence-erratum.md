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
- `cache-key-includes-the-complete-static-module-envelope`
- `carrier-integrity-and-pins-fail-closed`
- `bind-carrier-rebinds-second-path-without-aggregate-aliasing`
- `carrier-and-candidate-values-are-closed-and-metadata-free`
- `supplied-products-reject-extra-keys-sentinels-and-inexact-statuses`
- `every-live-pin-family-invalidates-a-carrier-hit`
- `source-snapshot-schema-is-exact-and-typed`
- `top-level-carrier-tampering-fails-as-data-without-throwing`

The runner test file contains the source-mutation, invalid UTF-8, bounded
source, failed producer, recursion, interruption, and cancellation fixtures:

- `semantic-cache-producer-failure-clears-flight-and-allows-retry`
- `semantic-cache-same-key-joins-and-different-key-progresses`
- `semantic-cache-cancellation-clears-flight-and-allows-retry`
- `semantic-cache-reentry-fails-and-retry-is-fresh`
- `exact-c10-route-rebinds-one-successful-carrier-across-paths`
- `semantic-source-snapshot-detects-mutation-during-bounded-read`
- `semantic-source-snapshot-rejects-malformed-utf8-and-oversize`

The authentic opt-in fixture remains
`sh07-b8-semantic-carrier-authentic-acceptance-evidence` in
`gravity.self-hosting.sh07-try-catch-test`. The immutable record's use of
`clojure -M:test ... --test-var` is also unsupported: the repository test
runner accepts `--namespace`, not `--test-var`. The valid exact-var route is
the direct `clojure.test/test-vars` command documented in
`docs/development-verification.md`. Per the user's explicit direction, that
long command's result was assumed successful for reconciliation and was not
rerun in this bounded erratum.

```bash
GRAVITY_SH07_SEMANTIC_CACHE_AUTHENTIC=1 \
  clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -e "(require 'clojure.test 'gravity.self-hosting.sh07-try-catch-test) (binding [clojure.test/*report-counters* (ref clojure.test/*initial-report-counters*)] (clojure.test/test-vars [#'gravity.self-hosting.sh07-try-catch-test/sh07-b8-semantic-carrier-authentic-acceptance-evidence]) (let [r @clojure.test/*report-counters*] (prn r) (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1))))"
```

## Acceptance

- Preserve the immutable original lifecycle record.
- Append a terminal lifecycle record for this correction.
- Treat the nonexistent carrier namespace command in the original record as
  invalid evidence and use the correct 18-test, 128-assertion replay instead.
- Treat the original `--test-var` authentic command as invalid evidence and
  use the documented direct `clojure.test/test-vars` command under the user's
  authorized successful-run assumption.
- Grant no additional implementation, performance, roadmap, release,
  self-hosting, or seed-retirement authority.
