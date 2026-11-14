# C7 collection call inference: verified development checkpoint

## Goal and contracts

Improve actual Uniform type inference under L5 and C7. A valid helper that
uses `keys` must be able to learn its Map parameter type from a later caller.
An unresolved receiver must not prevent the compiler from visiting the call
that supplies the missing evidence. Unknown or invalid types must still be
rejected if the fixed point cannot establish the required facts.

Governing documents:

- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-06-compiler-architecture/086-c7-type-checker-design.md`
- `docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md`
- `docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md`
- `docs/development-verification.md`

## Preserved implementation

Verified published base: `4a49deb8d487798991a62680d3ba3efcbad7be1e`.
The first source checkpoint is published as
`1e707f1a0fe68a4850aa3eaea5a6d67bdbf326d2` in draft PR #1426.
It preserves the exact source tree first recorded locally as
`48fb81a42cdc4d6ccc7a38c1ddc68896170895e1`.

The completed implementation increment is published as
`5b170508f1d8b541cdfbb1fc1738372b8f2a7a65`, with tree
`f11486ccce7b2f6956614b47427060858f0617e5`. Its C7 engine SHA-256 is
`682c00dd42161abbb4c70ac248e5cc35490bbf762f9ed355fcd1c942a47ff9e5`.
The final verification record changes this document only.

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
- [x] Pass the accepted current Reader/C5/SH07/C7 source composition and independent
  type-result verifier; reject the incompatible caller fixture.
- [x] Check adjacent inference cases and address any concrete defect in a separate
  commit with a failing regression first.
- [x] Pass the language-boundary gate, affected catalog tests, changed-file lint
  and `git diff --check`.
- [x] Keep this draft's current source, verification results and next action saved
  in GitHub after each useful increment.

## Reproduction commands

Run from the repository root with Java and the Clojure CLI available:

```bash
clojure -J-Xmx2g -M:language-test --namespace gravity.self-hosting.sh08-collection-call-inference-kernel-test --fail-fast
clojure -J-Xmx2g -M:compiler-composition-test --namespace gravity.self-hosting.sh08-collection-call-inference-test --progress --fail-fast
clojure -J-Xmx2g -M:language-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-map-keys-cold-development-kernel --fail-fast
clojure -J-Xmx2g -M:language-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-map-keys-physical-preflight-development-kernel --fail-fast
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -M tools/ci/run_test.clj
clojure -J-Xmx2g -M:dev-test --namespace gravity.project-tests.test-scope-runner-test --fail-fast
clojure -J-Xmx2g -M tools/ci/lint.clj 4a49deb8d487798991a62680d3ba3efcbad7be1e bootstrap/clojure/test/gravity/self_hosting/sh08_collection_call_inference_kernel_test.clj bootstrap/clojure/test/gravity/self_hosting/sh08_collection_call_inference_test.clj tools/ci/run.clj tools/ci/run_test.clj tools/gravity/project_tests/scope.clj
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

The first implementation checkpoint passed 7 tests and 68 assertions with zero
failures/errors before the additional diagnostic and mixed-invalid regressions.

An isolated unchanged-base reproduction establishes the original behavior:
the same caller yields one constraint and a concrete Map parameter when
visited first, but zero constraints and no parameter type after the unresolved
`keys` call. The ordering and conflicting-caller tests produced five expected
assertion failures. The whole baseline test invocation also produced errors
from direct tests of the newly introduced helper, which does not exist on the
base; those errors are not evidence for the inference bug.

The diagnostic regression first failed with 15 expected assertions and zero
errors against the first checkpoint. Final focused local verification on
2026-11-14 passed:

| Check | Result |
| --- | --- |
| Collection call-inference kernels | 8 tests / 143 assertions; zero failures/errors |
| Existing map-keys regressions | 2 tests / 219 assertions; zero failures/errors |
| Current-source compiler composition | 2 tests / 25 assertions; zero failures/errors |
| CI command/catalog harness | 77 tests / 1,201 assertions; zero failures/errors |
| Project test scope runner | 5 tests / 10 assertions; zero failures/errors |
| Language-boundary gate | 6 tests / 11 assertions; zero failures/errors |
| Changed-file lint | Zero baseline warnings, blocking warnings or errors |
| Whitespace | `git diff --check` passed |

Source composition accepted both `.gravity` and `.qst` fixtures through the
current Reader/C5/SH07/C7 pipeline and type-result verifier. It established
concrete Map parameter types across the forwarding chain, Integer return types
for the count helpers and main, and a Bool return type for `same-keys?`. The
String-key caller's `.gravity` fixture was rejected with its expected rule ID,
actual type and source/provenance fields; kernel checks cover parity of the
rejected `.qst` fixture. These results verify the current finite Keyword-key Map
subset; they do not expand `keys` to other Map key families.

Independent read-only review found no blocking issue within this scope and no
new acceptance or type-safety weakening. Its findings about simultaneous error
precedence and malformed structured type diagnostics are documented above;
mixed-invalid regressions preserve rejection behavior.

Hosted CI run
[43635648703](https://github.com/burn2delete/uniform/actions/runs/43635648703)
on implementation commit `5b170508f1d8b541cdfbb1fc1738372b8f2a7a65` passed
static, contracts, portable regressions, Darwin regressions and the
unsupported-host job at the last observation. The contracts job explicitly ran
and passed `c7-collection-call-inference-kernels`. Broader resolution, macro,
bootstrap and runtime jobs were still running or queued; the full hosted suite
was not yet complete. This is a timestamped checkpoint, not a claim about the
latest workflow state after subsequent commits.

Next: inspect CI on the draft's latest head, address any attributable failure,
then review this bounded language improvement. The focused local checks are
complete; do not repeat the expensive composition run solely because a session
was interrupted. Re-run relevant checks if the implementation or its inputs
change. Keep subsequent implementation and verification checkpoints on the
same draft PR.

This is development evidence. No integration, release, native backend,
self-hosting or seed-retirement claim is made. The temporary host remains
Clojure; language semantics remain in the existing Uniform implementation.
