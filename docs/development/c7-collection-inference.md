# C7 collection call inference: implementation checkpoint

## Goal and contracts

Improve actual Uniform type inference under L5 and C7. A valid helper that
uses `keys` must be able to learn its Map parameter type from a later caller.
An unresolved receiver must not prevent the compiler from visiting the call
that supplies the missing evidence. Unknown or invalid types must still be
rejected if the fixed point cannot establish the required facts.

Governing documents:

- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-06-compiler-architecture/086-c7-type-checker-design.md`
- `docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md`
- `docs/development-verification.md`

## Preserved implementation

Verified published base: `4a49deb8d487798991a62680d3ba3efcbad7be1e`.
The first source checkpoint is published as
`1e707f1a0fe68a4850aa3eaea5a6d67bdbf326d2` in draft PR #1426.
It preserves the exact source tree first recorded locally as
`48fb81a42cdc4d6ccc7a38c1ddc68896170895e1`.

The semantic change is in
`language/compiler/src/gravity/compiler/c7_type_checker_engine.gravity`.
It continues gathering call constraints only for a genuine core `keys` call
whose unary receiver type is unresolved. Its diagnostic remains in the current
iteration and is recomputed by the existing fixed-point checker. Concrete
invalid types, malformed calls, source/binding mismatches and transferring
arguments keep their rejection behavior.

The checkpoint includes paired `.gravity`/`.qst` accepted and rejected source
fixtures, normalized kernel regressions, current-source compiler composition
tests and the focused test catalog/CI entries. It changes no language contract.

## Completion criteria

- Reproduce the caller-evidence failure against the unchanged base.
- Pass the focused positive and negative kernels on the candidate.
- Pass the accepted current Reader/C5/SH07/C7 source composition and independent
  type-result verifier; reject the incompatible caller fixture.
- Check adjacent inference cases and address any concrete defect in a separate
  commit with a failing regression first.
- Pass the language-boundary gate, affected catalog tests, changed-file lint
  and `git diff --check`.
- Keep this draft's current source, verification results and next action saved
  in GitHub after each useful increment.

## Resume commands

Run from the repository root with Java and the Clojure CLI available:

```bash
clojure -J-Xmx2g -M:language-test --namespace gravity.self-hosting.sh08-collection-call-inference-kernel-test --fail-fast
clojure -J-Xmx2g -M:compiler-composition-test --namespace gravity.self-hosting.sh08-collection-call-inference-test --exact collection-caller-inference-current-source-composition --fail-fast
clojure -J-Xmx2g -M:compiler-composition-test --namespace gravity.self-hosting.sh08-collection-call-inference-test --exact invalid-collection-callers-current-source-composition --fail-fast
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
git diff --check
```

## Verified checkpoint and next action

The published source tree passes a fresh kernel run: 7 tests, 68 assertions,
zero failures and zero errors. Independent read-only review found no blocking
correctness issue in the preserved source delta.

Next: distinguish ordinary `keys` operand type errors and missing inference
evidence from compiler artifact verification failures, and exercise caller
evidence flowing through an intermediate helper. The current positive and
negative composition checks, baseline reproduction and remaining gates are
still in progress. Keep each implementation and verification checkpoint on
the same draft PR.

This is development evidence. No integration, release, native backend,
self-hosting or seed-retirement claim is made. The temporary host remains
Clojure; language semantics remain in the existing Uniform implementation.
