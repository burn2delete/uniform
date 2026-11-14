# C7 eager normal-return inference

## Goal and governing contracts

Do not commit a normal result type while a required evaluated child still has
an unresolved type. A later pass can prove that child never returns normally.
Prematurely using the last value of a sequence can otherwise freeze a wider
conditional result into an inferred function return and reject a valid program.

The governing contracts are:

- `docs/phase-01-core-language/012-l2-core-language-semantics.md`: left-to-right
  evaluation of `do`, binding initializers and call arguments; typed control
  transfer.
- `docs/phase-01-core-language/015-l5-type-system-specification.md`: bottom,
  independently established branch joins and evidence-producing inference.
- `docs/phase-06-compiler-architecture/086-c7-type-checker-design.md`: stable
  function types, constraints and typed-core verification.
- `docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md`
  and `docs/development-verification.md`: focused semantics, affected composition
  and separate migration authority.

Base: `9c89f5d90897ad2ffdb282419ce35c1f618a009a`, which integrates PR #1426.
The implementation checkpoint is
`9637e1a4b2aff7908f0ed7b6d74d4a45ea1026ff` in
[PR #1427](https://github.com/burn2delete/uniform/pull/1427).

## Concrete failure

```clojure
(defn stop [] (throw "stop"))
(defn choose [flag :- Bool]
  (if flag (do (stop) nil) :ok))
```

Before the fix, the strict child scan treats the `do` as returning nil before
the local call's return is established. This can produce a provisional
nil-or-Keyword function return. Once `stop` is established as Never, the
conditional's actual normal result is Keyword, conflicting with that frozen
provisional return. The same dependency exists in `let` initializers and call
arguments.

## Implementation scope

The owning function is `sh08-ft-normal-return-type` in
`language/compiler/src/gravity/compiler/c7_type_checker_engine.gravity`.
The eager child scan now defers a normal result when a child is unresolved.
It continues scanning because an established Never child anywhere in an eager
evaluation proves that the enclosing operation has no normal result. A nominal
Never result remains Never even with an unresolved operand, as required for
`throw` and other already established transfers.

Function construction and the separate `if`, `try` and alternative-join rules
retain their existing policies. This increment does not claim to repair every
possible provisional fact in those distinct paths. The existing monotone
function-return and declared-return mismatch checks are unchanged.

## Reproduced failure and verification

- The focused regression failed on the base: 4 tests, 44 assertions,
  17 failures and zero errors.
- The same fresh kernel run passes on the candidate: 4 tests, 44 assertions,
  zero failures/errors.
- The isolated baseline at the exact base completed the current Reader/C5/SH07
  path and passed SH07 verification. C7 then rejected the valid example with
  `C7-TYPE-MISMATCH` / `:function-result-type-mismatch`, expecting
  `{:kind :gravity.type/nil-or :value-type :gravity.type/keyword}` and observing
  `:gravity.type/keyword`. The diagnostic retained the actual `choose` source.
- Existing bottom/loop/throw regressions passed: 4 tests, 58 assertions.
- Existing nil-join and monotone-return regressions passed: 2 tests,
  114 assertions.
- Language-boundary passed: 6 tests, 11 assertions.
- CI command/catalog harness passed: 77 tests, 1,201 assertions.
- Project test scopes passed: 5 tests, 10 assertions.
- Changed-file lint reported zero baseline warnings or blocking warnings/errors;
  `git diff --check` passed.
- Independent review found no blocking issue within the eager-expression scope.

All passing test groups above report zero failures/errors. The reviewed and
tested C7 source SHA-256 is
`6e820c5a2de17d1b7bc8ef0bfc35233e63146139c84cce56a147e8911059b3a6`.

The source suite checks paired `.gravity` and `.qst` accepted source through
the current Reader/C5/SH07/C7 verifier and C8 body-effect consumer/verifier.
It checks eager `do`, `let` and call arguments, a two-hop nonreturning helper,
and an ordinary returning predecessor. The rejected source fixture checks the
concrete Keyword-versus-String declared-return mismatch and its attribution.
Accepted-byte seed observations check normal results and source-throw payloads;
they are temporary single-source seed observations, not typed lowering or a
native/JVM ABI claim.

The PR description is the durable execution record for source composition,
hosted CI and landing. Consult that record and the latest exact-head checks for
their final results; the implementation note does not predict a running test's
outcome or duplicate changing CI state.

## Completion criteria

- Reproduce the premature eager result with a failing semantic regression.
- Pass the focused regression without weakening return conflict checks.
- Accept complete current source in both extensions and pass C7 verification.
- Preserve source throw facts through C8 and its verifier.
- Observe normal and throwing paths on the same accepted source bytes.
- Reject the actual declared-return mismatch with stable source diagnostics.
- Pass proportionate existing regressions, language-boundary, lint and CI.
- Complete independent review and exact clean integration preflight.
- Land the verified improvement on main, as authorized by the user.

## Reproduction and continuation

```bash
clojure -J-Xmx2g -M:language-test --namespace gravity.self-hosting.sh08-normal-return-inference-kernel-test --fail-fast
clojure -J-Xmx2g -M:compiler-composition-test --namespace gravity.self-hosting.sh08-normal-return-inference-test --progress --fail-fast
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -M tools/ci/run_test.clj
clojure -J-Xmx2g -M:dev-test --namespace gravity.project-tests.test-scope-runner-test --fail-fast
git diff --check
```

On resumption, first inspect PR #1427 and current main. If it is already merged,
do not replay it. Otherwise retain the exact tested source, use the PR's recorded
results to avoid repeating completed checks, and address the earliest remaining
failure before integration. Save useful increments to the same draft PR. No
release, self-hosting, seed-retirement or migration authority is claimed.
