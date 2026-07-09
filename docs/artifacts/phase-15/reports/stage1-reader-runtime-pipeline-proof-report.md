# Stage1 Reader Runtime Pipeline Proof Report

Status: complete for the stage1 reader runtime pipeline bridge

This report records the eleventh post-stage0 self-hosting reduction step. The
Clojure seed now loads `stage1-read-source-runtime-pipeline` from
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`, executes that
Gravity-authored entrypoint through the remaining Clojure runtime interpreter,
and records Gravity-authored runtime records for source-character production
and evaluator ownership.

## Capability

```bash
clojure -M:gravity stage1-reader-runtime-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity > docs/artifacts/phase-15/bootstrap/stage1-reader-runtime-pipeline-proof.edn
```

The command emits a `:gravity/stage1-reader-runtime-pipeline-artifact` with:

- artifact id `sha256:367651e5737d1d6eb3a35b43ec1589c39a3d8f5532ea31b833f58167e0d6002e`
- runtime pipeline id `sha256:4246eb59dc38ba0d93239d22e5c04da1e8e661ede73c3a28788e62876935c3dd`
- Gravity entrypoint `stage1-read-source-runtime-pipeline`
- explicit host primitive boundary `[]`
- Gravity runtimes `[:stage1-reader-evaluator-runtime :stage1-reader-source-runtime]`
- Gravity executors `[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`
- no `:reader/source-characters`, `:reader/run-token-automaton`,
  `:reader/build-forms`, `:reader/forms-from-tokens`,
  `:reader/realize-tokens`, `:reader/tokens-from-classifier`,
  `:reader/tokens-from-characters`, `:reader/scan-tokens`, or
  `:reader/read-with-table` host primitive in this bridge
- Gravity-authored source runtime engine `:gravity-reader-source-runtime-v1`
- Gravity-authored evaluator runtime engine `:gravity-reader-evaluator-runtime-v1`
- Gravity-authored token automaton executor engine
  `:gravity-reader-token-automaton-executor-v1`
- Gravity-authored form builder executor engine
  `:gravity-reader-form-builder-executor-v1`
- 506 character records with source spans
- 82 token records with source spans
- 4 top-level forms read from the accepted fixture
- form parity with the stage0 reader
- 10 stable diagnostics across runtime-pipeline and malformed-input failures

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`

The accepted fixture is read through the Gravity-authored runtime pipeline
entrypoint. The pipeline emits a character-stream artifact through the
Gravity-authored source runtime record, applies the Gravity-authored token
classifier, token realizer, token automaton, token automaton executor,
form-builder, and form-builder executor records, then emits form records with
source spans and stage0 parity evidence.

## Rejected Fixtures

- `stage1-reader-unexpected-close.gravity` -> `STAGE1READER001`
- `stage1-reader-unclosed-list.gravity` -> `STAGE1READER002`
- `stage1-reader-unclosed-string.gravity` -> `STAGE1READER003`
- `stage1-reader-unsupported-dispatch.gravity` -> `STAGE1READER004`
- `stage1-reader-odd-map.gravity` -> `STAGE1READER005`

The artifact also records fail-closed runtime-pipeline diagnostics:

- missing Gravity reader runtime-pipeline entrypoint -> `STAGE1RUN001`
- unsupported executable Gravity form -> `STAGE1RUN002`
- unsupported host primitive -> `STAGE1RUN003`
- invalid runtime record or stream -> `STAGE1RUN004`
- stage0 form divergence -> `STAGE1RUN005`

## Validation Commands

```text
$ clojure -M -e '(require (quote [clojure.edn :as edn])) (let [a (edn/read-string (slurp "docs/artifacts/phase-15/bootstrap/stage1-reader-runtime-pipeline-proof.edn")) p (:capability-based-proof a)] (println (:kind a)) (println (:artifact-id a)) (println (:runtime-pipeline-id a)) (println (:host-primitives a)) (println (:gravity-runtimes a)) (println (:gravity-executors a)) (println (get-in a [:stage1-reader-source-runtime :engine])) (println (get-in a [:stage1-reader-evaluator-runtime :engine])) (println (get-in a [:stage1-reader-character-stream :source-runtime-engine])) (println (get-in a [:stage1-reader-character-stream :character-count])) (println (get-in a [:stage1-reader-token-stream :token-count])) (println (count (:stage1-reader-records a))) (println (get-in a [:stage0-comparison :forms-equal?])) (println (:source-characters-host-primitive-removed? p)) (println (:host-primitive-boundary-empty? p)) (println (get-in p [:limitations :clojure-runtime-interpreter?])) (println (get-in p [:limitations :clojure-seed-retired?])) (println (:status p)))'
:gravity/stage1-reader-runtime-pipeline-artifact
sha256:367651e5737d1d6eb3a35b43ec1589c39a3d8f5532ea31b833f58167e0d6002e
sha256:4246eb59dc38ba0d93239d22e5c04da1e8e661ede73c3a28788e62876935c3dd
[]
[:stage1-reader-evaluator-runtime :stage1-reader-source-runtime]
[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]
:gravity-reader-source-runtime-v1
:gravity-reader-evaluator-runtime-v1
:gravity-reader-source-runtime-v1
506
82
4
true
true
true
true
false
:complete

$ clojure -M:test
Ran 134 tests containing 8388 assertions.
0 failures, 0 errors.
```

## Conformance Argument

This bridge proves that a Gravity-authored reader runtime pipeline entrypoint is
executable by the remaining Clojure runtime interpreter and can drive accepted
and rejected behavior without using the older `:reader/read-with-table`,
`:reader/scan-tokens`, `:reader/tokens-from-characters`,
`:reader/tokens-from-classifier`, `:reader/realize-tokens`,
`:reader/forms-from-tokens`, `:reader/run-token-automaton`,
`:reader/build-forms`, or `:reader/source-characters` host primitives. The
artifact records source runtime coverage, evaluator runtime coverage, executor
coverage, character coverage, token coverage, form parity, source spans,
diagnostics, and the remaining trusted interpreter boundary.

## Residual Risks

This is not a self-hosted reader. The entrypoint, pipeline shape, source
runtime record, evaluator runtime record, token classifier, token realizer
specification, token automaton specification, token-automaton executor record,
form-builder specification, and form-builder executor record are authored in
Gravity, but the Clojure runtime interpreter, Clojure character-stream
implementation, and Clojure seed builtins are still trusted Clojure code. The
next capability gate is to replace the Clojure runtime interpreter with a
Gravity-compiled reader while preserving accepted forms, rejected diagnostics,
source spans, character coverage, token-classifier coverage, token-realizer
coverage, token-automaton coverage, executor coverage, form-builder coverage,
token coverage, artifact provenance, and stage0 parity.
