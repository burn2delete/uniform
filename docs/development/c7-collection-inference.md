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

- [x] Reproduce the caller-evidence failure against the unchanged base.
- [x] Pass the focused positive and negative kernels on the candidate.
- [ ] Pass the accepted current Reader/C5/SH07/C7 source composition and independent
  type-result verifier; reject the incompatible caller fixture.
- [x] Check adjacent inference cases and address any concrete defect in a separate
  commit with a failing regression first.
- [ ] Pass the language-boundary gate, affected catalog tests, changed-file lint
  and `git diff --check`.
- [x] Keep this draft's current source, verification results and next action saved
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

## Diagnostic correction

The second implementation increment distinguishes an ordinary invalid `keys`
operand (`C7-TYPE-MISMATCH`) from missing operand type evidence
(`C7-ANNOTATION`). Both the node and call-constraint routes use the same narrow
classification. Concrete operand types and the required finite Keyword-key Map
domain survive into the structured diagnostic, with actionable remediation.
Existing source/evidence checks and inference deferral decisions retain their
rejection behavior and ordering. This change classifies actionable operand
errors on otherwise valid applications; it does not establish a new precedence
between simultaneous source and operand errors. Mixed-invalid inputs remain
rejected, and grounding their operand exposes the original source-verification
failure. Malformed structured type descriptions also retain the existing
node/call diagnostic distinction.

The accepted source fixture now forwards its Map through an intermediate
`forwarded-key-count` helper before `key-count` uses it. The rejected fixture
checks the actual String-key Map type and source/provenance fields in the final
diagnostic.

## Verification checkpoints and next action

The published source tree passes a fresh kernel run: 7 tests, 68 assertions,
zero failures and zero errors. Independent read-only review found no blocking
correctness issue in the preserved source delta.

An isolated unchanged-base reproduction establishes the original behavior:
the same caller yields one constraint and a concrete Map parameter when
visited first, but zero constraints and no parameter type after the unresolved
`keys` call. The ordering and conflicting-caller tests produced five expected
assertion failures. The whole baseline test invocation also produced errors
from direct tests of the newly introduced helper, which does not exist on the
base; those errors are not evidence for the inference bug.

The diagnostic regression first failed with 15 expected assertions and zero
errors against the first checkpoint. After the correction:

- Focused kernels: 8 tests, 135 assertions, zero failures/errors.
- CI command/catalog harness: 77 tests, 1,201 assertions, zero failures/errors.
- Project test scope runner: 5 tests, 10 assertions, zero failures/errors.
- `git diff --check`: passed.

Next: finish the fresh complete source composition (both accepted extensions
and the rejected String-key caller), language-boundary gate, changed-file lint
and final independent review. These checks are in progress at this checkpoint;
their results must be recorded before claiming this slice verified. Keep the
implementation and verification checkpoints on the same draft PR.

This is development evidence. No integration, release, native backend,
self-hosting or seed-retirement claim is made. The temporary host remains
Clojure; language semantics remain in the existing Uniform implementation.
