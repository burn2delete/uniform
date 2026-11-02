# Reader artifact assembly extraction evidence

## Scope and contracts

This slice extracts the existing stage0 reader artifact assembly body into
`bootstrap/clojure/src/gravity/reader.clj`. The legacy two/three-argument
`gravity.bootstrap/read-source-artifact` remains the composition wrapper in
`bootstrap_impl/components/foundation/00004-max-reader-form-depth.clj`.
The stage takes its owning operations explicitly and never loads bootstrap.
The seam loads the new namespace without modifying `gravity/bootstrap.clj`,
which appears in another active reader candidate's recorded ownership.
The existing production-helper inventory registers the new source as owned
by the reader stage, with its exact bootstrap consumer and standalone test.
Primary component pairs and their counts remain unchanged.

Governing contracts are D0-D3, D6, D8-D9, L1, C1-C3, C15 and TEST2.
The [namespace boundary map](stage-owned-namespaces.md) assigns all requested
destinations and distinguishes proposed interfaces from implemented behavior.
No Gravity semantic owner, canonical reader algorithm, authenticated adapter,
identity encoder, diagnostic rule or frozen migration fingerprint is changed.

## Executed acceptance and rejection

The source baseline was captured before extraction at
`8f34bcea7549aa943e3209da353a3799b60fe5f8` using the accepted
`surface-syntax.gravity` fixture. The complete resulting artifact compared equal
after extraction. Its source identity remained
`sha256:b51c29e6b51746dbea9f71d41b34540957beaed970b980ba57551a105218a1fc`.
This is a before/after development comparison, not fresh bootstrap equivalence
or proof of a self-hosted reader.

`gravity.reader.artifact-test` executes real paired `surface-syntax.gravity`
and `surface-syntax.qst` inputs through the new stage, compatibility API and
file API. It verifies the emitted syntax product, original path/extension,
byte count, module context, metadata, namespace clauses and deterministic
source identity. Changed source, reader options and project identity must
change the source id. The tests execute both rejected delimiter fixtures with
unchanged `L1-DELIMITER` data and the existing `.gravity` namespace-shape
fixture with unchanged `L1-NS-SHAPE` data.

Interposition tests verify operation order, explicit project/options transport,
the stage invocation seam, and identity-preserving propagation of the exact
owner exception before downstream syntax construction. The stage's public API
and no-bootstrap dependency boundary are inspected. A separate fresh JVM
loaded `gravity.reader` and asserted `gravity.bootstrap` had not been loaded.
These tests do not authenticate arbitrary caller-provided operations.

## Validation observations

Commands ran from the repository root. Focused runs used fresh JVMs.

| Command | Result |
| --- | --- |
| `clojure -J-Xmx2g -M:dev-test --namespace gravity.reader.artifact-test --fail-fast` | 6 tests, 71 assertions; passed |
| `clojure -J-Xmx2g -M:dev-test --namespace gravity.bootstrap-test --exact reader-artifact-preserves-l1-syntax-data --exact reader-abbreviation-has-generated-origin --exact reader-source-unit-identity-preserves-path-extension-and-options --fail-fast` | 3 tests, 46 assertions; passed |
| `clojure -J-Xmx2g -M:dev-test --namespace gravity.reader-primitives-test --namespace gravity.c3-reader-expansions-test --fail-fast` | 10 tests, 138 assertions; passed |
| `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test` | 6 tests, 11 assertions; passed |
| `clojure -J-Xmx512m -M tools/ci/run_test.clj` | 11 tests, 101 assertions; passed with CI-pinned clj-kondo 2025.10.23 on PATH |
| `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj` | 240 documents, 19 phase indexes, ASCII and content checks; passed |
| `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj` | Passed |
| `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-ownership-test` | 17 tests, 749 assertions; passed |
| `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-stage0-leaf-test-runner-test` | 26 tests, 252 assertions; passed |
| `clojure -J-Xmx512m -M tools/acceptance_dependencies_test.clj` | 14 tests, 402 assertions; passed |
| `clojure -J-Xmx512m -M tools/sh_crosswalk_test.clj` | 12 tests, 145 assertions; passed |
| `clojure -J-Xmx512m -M tools/sh_crosswalk.clj --check` | Passed |
| `git diff --check` | Passed |

The first harness run had two errors because local `clj-kondo` was absent.
After installing the version already pinned by CI in a task-local temporary
directory, the complete harness passed. No failure was suppressed or changed
into an acceptance. The new reader tests are also registered in the existing
CI `reader-regression` command.

The ownership integration check initially rejected the new top-level source
because its registration was missing. The existing production-helper records
now join the actual reader namespace, bootstrap consumer, reader owner and
standalone test. Positive and tampered-registration checks pass without
changing primary component counts or weakening the inventory comparison.

GitHub projections then rejected that changed ownership file as a stale SH
reporting input (`ACCEPTANCE-DEPENDENCY`, two test errors). The existing
source-version mechanism now binds the committed ownership bytes at `7b0800bf`
through [an explicit successor](../../specs/sh-family-crosswalk/reader-assembly-source-version.edn).
Its predecessor pin and complete pending review are retained. Regeneration
checks the actual Git source before writing the derived crosswalk. Tests
conserve every prior ownership entry and reject an old or unavailable pin.
The mapping disposition remains `:pending-source-version-review`; all 180
current whole-criterion assessments stay unassessed. This updates reporting
provenance, not a frozen compiler migration fingerprint or semantic authority.

## Review and integration boundary

The slice began as draft PR 640. Independent review must identify the immutable
source commit/tree and current base; a self-audit cannot confer acceptance.
Record that review and the exact integration tuple in the PR, and run the
documented direct-candidate integration preflight on a clean named branch
immediately before merging. Any changed base or source requires the affected
composition and review to be reconciled first. Existing workstream dispositions
are preserved; this ordinary seed refactor does not promote an authoritative
module migration or rewrite another workstream's lifecycle metadata.

## Residual boundaries and unsupported claims

Only reader artifact assembly moved. Parsing, namespace validation, syntax
construction and source identity still use their existing seed dependencies.
The injected-operation API trusts implementation dependencies; it cannot confer
reader authentication, proof authority or cache-reuse authority. Stage0's
historical artifact schema and merge precedence are preserved.

The complete bootstrap suite, authoritative current-source C2/C3 provenance,
stage-N equivalence, backend conformance, full-language acceptance and release
proofs were not run for this narrow behavior-preserving extraction. The other
namespace interfaces in the map are proposed destinations, not new features.
There is no milestone-completion, performance, safety-certification,
self-hosting, seed-retirement or seedless-release claim.
