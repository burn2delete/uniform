# Pure helper selected-function closure correction

Base: `f29df65b0b2b30f63db6c94bcdaf65f1af6de927` /
tree `aa198808f5eebd447c8bef997ffe8b9760e99bf4`.
Task: `reader/finite-remaining-work/extension-policy-closure` (`FL-P01-T01`).

## Consumed finding and correction

Independent receipt `/tmp/reader-pure-independent-f29/receipt.edn`, SHA256
`17e68504825ca9ef0e55c8254e8ca1b562784c3801bf6724106045e5a62a28ea`,
requires changes for one blocking finding:
`:new-helper-outside-Reader-selected-function-closure`.
The actual plan builder selects only functions matching its configured
`sh03-reader-function-prefix`. The new `reader-pure-tags?` definition was
excluded although selected `sh03-reader-policy-valid?` called it.

Rename that definition and its call to `sh03-reader-pure-tags?`. No function
body, prefix filter, builtin table, historical pin, namespace guard, policy,
result verification or effectful refusal semantics changes. The prefix is an
executable selection contract, not an optional planning label. F29 remains
immutable changes-required history; this correction does not reinterpret its
host-model checks as compiler or plan-construction evidence.

## Exact source/DATA regression

`gravity.reader.plan-selection-test` reads production source with
`*read-eval* false`. From `sh03-reader-build-plan!` in
`00809-sh03-reader-plan-instruction-summary.clj`, it extracts the actual
`selected-functions` let-binding expression, including its selection predicate.
From `00808-sh03-reader-expected-source-byte-count.clj`, it reads the actual
prefix constant. It evaluates only that collection-selection expression on a
map of source definition DATA; neither the builder nor Gravity bodies execute.

It computes the transitive defined-function call edges reachable from
`sh03-reader-policy-valid?` and requires every target to survive that selection.
The positive check includes the helper's real definition and call edge. The
negative removes the configured prefix from both definition and call as DATA,
then applies the same production selector. It detects exactly
`[sh03-reader-policy-valid? reader-pure-tags?]` as the missing selected edge.
This is a defined-function selection regression, not a complete compiler,
builtin-resolution or runtime correctness proof. No integration fixtures parse.

## Observed checks

```sh
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '(require (quote gravity.reader.plan-selection-test)) (let [r (clojure.test/run-tests (quote gravity.reader.plan-selection-test))] (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))'
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
git diff --check
```

Selection regression: 2 tests / 8 assertions, zero failures/errors.
Boundary: 6 tests / 11 assertions, zero failures/errors.
Whitespace check passed. The preceding 7 tests / 49 assertions remain attributed
to F29's unchanged host-model/test bodies; the independent 34 negative controls
and behavioral mutant are not rerun or extended to new source. Their source
scope remains distinct from the new closure regression.

## Owned paths and remaining scope

Only the Gravity Reader's two identifier occurrences, the new source/DATA test,
and this evidence file differ from F29. All other F29 production, fixture,
modeled-test and evidence files are conserved. Main/PR652 composition is
separate and not imported. No publication, reviewer assignment or integration
occurs. Original blocked goal and held PR571 history, resource work, task and
lifecycle records remain unchanged. Actual plan construction, genuine pure
Reader/handler execution, sealed-product admission, effectful acceptance and
whole task closure remain unclaimed.
