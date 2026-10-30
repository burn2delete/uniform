# Syntax pipeline extraction

## Boundary and governing contracts

This increment moves the existing C3 source artifact construction and complete
reader-to-syntax association verification bodies out of `gravity.bootstrap`
into `gravity.syntax`. The public stage API is `build [request operations]`,
`verify [source-path artifact operations]`, and
`reader-products-authentic? [stream reader boundary invocation operations]`.
The old bootstrap entry points retain their arities and supply explicit seed
dependencies. Callable Vars preserve the bootstrap's late interposition.

D1, D2, D3, D9, L1, C1, C2, C3, C15 and TEST2 govern this slice. C3 owns
syntax construction, spans, origin links, hygiene, metadata, fact invalidation,
serialization and verifier ordering. The existing Gravity syntax program and
reader authentication adapters retain their authority. Supplied operations are
trusted seed composition dependencies and cannot confer proof authority.

Inputs are decoded source, the explicit source/project context, reader
selection and invocation, authenticated reader products when internally
provided, and the existing owning operations. Outputs are the unchanged
`:gravity/stage0-c3-syntax-object-artifact`, syntax records, ledgers, verifier
report and serialization records. Stable C2/C3 diagnostic ids and source spans
remain with the existing diagnostic producers. The stage never loads or
resolves bootstrap vars; the bootstrap wrapper supplies every dependency.

## Development evidence

Before extraction, a genuine selected-current reader invocation for source
text `1` produced two syntax records, a passed syntax verifier, a complete
capability report and artifact identity
`sha256:c1e2b235f9efb1087cb1c8ddf2df2f53873853f0cd419b3034d466d11b643859`.
The full product was captured before editing and compared equal after the
extraction, not merely by its id. A fresh JVM loads `gravity.syntax` without
loading bootstrap. The direct stage tests construct genuine selected-current
products for both source extensions, compare compatibility products, and
recompute verification. They reject actual lexical and C3 shape fixtures and
substituted syntax, reader revision, source hash, envelope ownership and
serialization. Caller-supplied reader/candidate combinations stop before
producer execution. Captured callable Vars retain late interposition.
The actual source fixture also executes with its namespace, nested forms,
reader abbreviations, source metadata, generated hygiene/capture records and
Unicode text. Its exact UTF-8 content hash, origin inputs, attached/invalidated
facts and recomputed artifact identity survive the stage boundary. The paired
QST fixture has identical source bytes; both source extensions additionally
execute the small genuine construction and tamper fixture population above.

The old compatibility fixture reproduced four failures and one arity error
when the three original bootstrap definitions were re-evaluated from base
`ae0cd80165b2085e7ce362d0c7bfc5eeec79b2de` in a fresh JVM. Its arity assertions
and interposed proof callback had not tracked the existing invocation-aware
APIs. The fixture now describes those actual APIs. No production arity was
removed. The stage verification wrapper preserves the existing nested
operation context rather than rebuilding it on interposition reentry.

Executed development checks:

| Command | Result |
| --- | --- |
| `clojure -J-Xmx2g -M:dev-test --namespace gravity.syntax.pipeline-test --fail-fast` | Five small-product tests passed with 61 assertions; the added full-source case is checked in the combined CI command below |
| `clojure -J-Xmx2g -M:dev-test --namespace gravity.bootstrap-compatibility.c3-test --fail-fast` | 9 tests, 99 assertions; passed |
| `clojure -J-Xmx2g -M:dev-test --namespace gravity.syntax.pipeline-test --namespace gravity.bootstrap-compatibility.c3-test --fail-fast` | 15 tests, 174 assertions; passed, including six stage tests with 75 assertions |
| `clojure -J-Xmx512m -M:dev-test --namespace gravity.c3-syntax-verification-test --namespace gravity.c3-syntax-construction-test --namespace gravity.c3-artifact-identity-test` | 12 tests, 163 assertions; passed |
| `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-ownership-test` | 18 tests, 762 assertions; passed |
| `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-stage0-leaf-test-runner-test` | 26 tests, 252 assertions; passed |
| `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test` | 6 tests, 11 assertions; passed |
| `clojure -J-Xmx512m -M tools/ci/run_test.clj` | 11 tests, 101 assertions; passed with CI-pinned clj-kondo |
| `clojure -J-Xmx512m -M tools/sh_crosswalk_test.clj` | 13 tests, 171 assertions; passed |
| `clojure -J-Xmx512m -M tools/acceptance_dependencies_test.clj` | 14 tests, 402 assertions; passed |
| `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj` | 240 documents, 19 phase indexes; passed |
| `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj` | Passed |
| `clojure -J-Xmx512m -M tools/validate_workstream_governance.clj` | Passed; existing dispositions unchanged |
| Changed-file lint and `git diff --check` | Passed; no new lint findings |

The existing production-helper records join the new namespace to its actual
bootstrap constructor, syntax owner and standalone tests. Primary component
counts are unchanged. The CI syntax regression command uses the same 2 GiB
host class as the genuine construction tests; its process watchdog is a host
safeguard, not a compiler admission allowance.

The [ownership source-version successor](../../specs/sh-family-crosswalk/syntax-pipeline-source-version.edn)
binds the new entries to committed source `c7804fa6`. It preserves the prior
reader ownership and compiler-domain composition records and their pending
review history. Source and generated crosswalk hashes are current. The mapping
remains pending and all whole-criterion assessments remain unassessed; no
compiler migration fingerprint, task completion or historical receipt is
rewritten by this reporting update.

## Remaining work and nonclaims

This is an incremental stage extraction, not completion of every syntax
implementation boundary. At this increment, existing syntax helper namespaces
and SH04 Gravity plan execution, digest/envelope binding and host adaptation
remained. The [runtime successor](syntax-runtime-evidence.md) moves plan
execution and digest resolution into the stage. The other
remaining bootstrap-owned implementation must be accounted for before the
namespace migration can be called complete. The reader and diagnostic slices
also remain partial; other destinations in the namespace map remain open.

This slice adds no language features and grants no migration, proof,
self-hosting, seed-retirement, backend or release authority. Existing
authenticated products, caller-supplied input refusals, extension provenance
and deterministic identity must survive. No frozen compiler migration
fingerprint is renewed by this refactor.
